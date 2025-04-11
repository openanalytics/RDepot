/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.python.synchronization;

import com.google.gson.Gson;
import eu.openanalytics.rdepot.base.storage.exceptions.CheckSumCalculationException;
import eu.openanalytics.rdepot.base.storage.exceptions.CleanUpAfterSynchronizationException;
import eu.openanalytics.rdepot.base.storage.exceptions.OrganizePackagesException;
import eu.openanalytics.rdepot.base.synchronization.RepoResponse;
import eu.openanalytics.rdepot.base.synchronization.RepositorySynchronizer;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.base.synchronization.exceptions.SendSynchronizeRequestException;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import eu.openanalytics.rdepot.python.storage.PythonStorage;
import eu.openanalytics.rdepot.python.storage.utils.PopulatedRepositoryContent;
import eu.openanalytics.rdepot.python.technology.PythonLanguage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PythonRepositorySynchronizer extends RepositorySynchronizer<PythonRepository> {
    public static final Comparator<PythonPackage> PACKAGE_COMPARATOR = Comparator.comparingInt(PythonPackage::getId);
    private final PythonStorage storage;
    private final PythonPackageService packageService;
    private final PythonRequestBodyPartitioner pythonRequestBodyPartitioner;
    private final RestTemplate rest;

    @Value("${local-storage.max-request-size}")
    private Integer maxRequestSize;

    @Override
    @Transactional
    public void storeRepositoryOnRemoteServer(PythonRepository repository, String dateStamp)
            throws SynchronizeRepositoryException {
        LinkedHashSet<PythonPackage> packages = new LinkedHashSet<>(packageService.findActiveByRepository(repository));
        List<PythonPackage> allPackages = new LinkedList<>(packages);
        storeRepositoryOnRemoteServer(repository, dateStamp, allPackages);
    }

    /**
     * Triggers a series of actions to generate necessary files and synchronize the remote repository
     * with the current state of the local repository.
     * <p>
     * This process involves organizing the provided packages into the correct storage structure,
     * generating required files, and sending them to the repo application for synchronization.
     * The {@code dateStamp} parameter acts as a unique identifier to differentiate synchronizations that
     * occur at the same time.
     * </p>
     *
     * <p><b>Note:</b> The {@code organizePackagesInStorage} method is responsible for organizing the
     * Python-related folder structure and linking it as the latest version of the repository.</p>
     *
     * @param repository the repository that needs to be synchronized
     * @param dateStamp  a unique timestamp used as a hash to differentiate synchronization instances
     * @param packages   the packages to be stored and synchronized in the remote repository
     * @throws SynchronizeRepositoryException if there is an issue during the synchronization process
     */
    private synchronized void storeRepositoryOnRemoteServer(
            PythonRepository repository, String dateStamp, List<PythonPackage> packages)
            throws SynchronizeRepositoryException {
        try {
            synchronizeRepository(storage.organizePackagesInStorage(dateStamp, packages, repository), repository);
        } catch (OrganizePackagesException e) {
            log.error(e.getMessage(), e);
            throw new SynchronizeRepositoryException();
        }
    }

    /**
     * Generates a request to be sent to the repository application, specifying which packages
     * should be uploaded or deleted in the remote repository.
     * <p>
     * The request will include the packages that need to be uploaded or removed. If a package has
     * been modified, it should be marked for upload as well. Once the request is successfully sent,
     * the {@code cleanUpAfterSynchronization} method will be invoked to remove all temporary files
     * generated during the synchronization process from the storage.
     * </p>
     *
     * @param populatedRepositoryContent the current content of the repository that has been populated
     * @param repository                 the repository that is being synchronized
     * @throws SynchronizeRepositoryException if an error occurs during the synchronization process
     */
    private void synchronizeRepository(
            PopulatedRepositoryContent populatedRepositoryContent, PythonRepository repository)
            throws SynchronizeRepositoryException {

        String[] serverAddressComponents = repository.getServerAddress().split("/");
        if (serverAddressComponents.length < 4) {
            throw new IllegalStateException("Incorrect server address: " + repository.getServerAddress());
        }

        String serverAndPort = serverAddressComponents[0] + "//" + serverAddressComponents[2];
        String repositoryDirectory = String.join(
                "/",
                Arrays.stream(serverAddressComponents, 3, serverAddressComponents.length)
                        .toArray(String[]::new));

        Gson gson = new Gson();

        ResponseEntity<String> response = rest.getForEntity(
                attachTechnologyIfNeeded(serverAndPort, repositoryDirectory, PythonLanguage.instance), String.class);

        List<String> remotePackages = new ArrayList<>(Arrays.asList(gson.fromJson(response.getBody(), String[].class)));

        String versionBefore = remotePackages.remove(0);

        try {
            SynchronizeRepositoryRequestBody requestBody = storage.buildSynchronizeRequestBody(
                    populatedRepositoryContent, remotePackages, repository, versionBefore);

            sendSynchronizeRequest(requestBody, serverAndPort, repositoryDirectory);
            storage.cleanUpAfterSynchronization(populatedRepositoryContent);
        } catch (SendSynchronizeRequestException | IOException | CheckSumCalculationException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new SynchronizeRepositoryException();
        } catch (CleanUpAfterSynchronizationException e) {
            // TODO #32884 We should somehow inform the administrator that it failed and/or revert it
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Divides the given request into smaller chunks if it exceeds the maximum allowed request size,
     * and sends each chunk as a separate request to the repository application.
     * <p>
     * If the size of the request exceeds the maximum request size, the method will partition the request
     * into manageable chunks. Each chunk is then sent in a separate request to the remote repo app
     * at the provided server address. The packages will be stored in the specified repository directory.
     * </p>
     *
     * @param request             the request body prepared in the {@code synchronizeRepository} method
     * @param serverAddress       the address of the remote server where the repository resides
     * @param repositoryDirectory the directory where the repository's packages should be stored remotely
     * @throws SendSynchronizeRequestException if there is an issue with sending the synchronization request
     * @throws IOException                     if there is an I/O issue during communication with the server
     * @throws CheckSumCalculationException    if an error occurs while calculating the checksum for verification
     */
    private void sendSynchronizeRequest(
            SynchronizeRepositoryRequestBody request, String serverAddress, String repositoryDirectory)
            throws SendSynchronizeRequestException, IOException, CheckSumCalculationException {

        List<MultiValueMap<String, Object>> chunks = pythonRequestBodyPartitioner.toChunks(request, maxRequestSize);
        int chunksSize = chunks.size();

        log.debug("Sending chunk to repo...");

        try {
            String id = "";
            for (MultiValueMap<String, Object> chunk : chunks) {
                chunk.add("id", id);

                HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(chunk);
                ResponseEntity<RepoResponse> httpResponse = rest.postForEntity(
                        attachTechnologyIfNeeded(serverAddress, repositoryDirectory, PythonLanguage.instance),
                        entity,
                        RepoResponse.class);

                if (!httpResponse.getStatusCode().is2xxSuccessful()
                        || !Objects.equals(
                                Objects.requireNonNull(httpResponse.getBody()).getMessage(), "OK")) {
                    throw new SendSynchronizeRequestException();
                }

                id = httpResponse.getBody().getId();
            }
        } catch (RestClientException e) {
            log.error("{}: {}", e.getClass().getCanonicalName(), e.getMessage(), e);
            throw new SendSynchronizeRequestException();
        } finally {
            IntStream.range(0, chunksSize).forEachOrdered(chunkNo -> {
                Path dirToRemovePath = FilesHierarchy.getArchiveDirectory(
                        request.getFilesToUpload().get(0), chunkNo);
                File dirToRemove = dirToRemovePath.toFile();
                if (dirToRemove.exists()) {
                    FilesHierarchy.deleteDirectory(dirToRemove);
                }
            });
        }
    }
}

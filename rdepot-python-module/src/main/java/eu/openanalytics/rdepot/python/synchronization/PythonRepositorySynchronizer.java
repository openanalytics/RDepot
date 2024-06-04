/*
 * RDepot
 *
 * Copyright (C) 2012-2024 Open Analytics NV
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
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class PythonRepositorySynchronizer implements RepositorySynchronizer<PythonRepository> {
    private final PythonStorage storage;
    private final PythonPackageService packageService;
    private final RestTemplate rest;

    @Value("${local-storage.max-request-size}")
    private Integer maxRequestSize;

    @Value("${declarative}")
    private String declarative;

    private int chunksSize = 0;

    public static final Comparator<PythonPackage> PACKAGE_COMPARATOR = Comparator.comparingInt(PythonPackage::getId);

    @Override
    public void storeRepositoryOnRemoteServer(PythonRepository repository, String dateStamp)
            throws SynchronizeRepositoryException {
        LinkedHashSet<PythonPackage> packages = new LinkedHashSet<>(packageService.findActiveByRepository(repository));
        List<PythonPackage> allPackages = new LinkedList<>(packages);
        storeRepositoryOnRemoteServer(repository, dateStamp, allPackages);
    }

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

    private void synchronizeRepository(
            PopulatedRepositoryContent populatedRepositoryContent, PythonRepository repository)
            throws SynchronizeRepositoryException {

        String[] serverAddressComponents = repository.getServerAddress().split("/");
        if (serverAddressComponents.length < 4) {
            throw new IllegalStateException("Incorrect server address: " + repository.getServerAddress());
        }

        String serverAndPort = serverAddressComponents[0] + "//" + serverAddressComponents[2];
        String repositoryDirectory = serverAddressComponents[3];

        Gson gson = new Gson();

        ResponseEntity<String> response =
                rest.getForEntity(serverAndPort + "/python/" + repositoryDirectory + "/", String.class);

        List<String> remotePackages = new ArrayList<>(Arrays.asList(gson.fromJson(response.getBody(), String[].class)));

        String versionBefore = remotePackages.remove(0);

        try {
            SynchronizeRepositoryRequestBody requestBody = storage.buildSynchronizeRequestBody(
                    populatedRepositoryContent, remotePackages, repository, versionBefore);

            sendSynchronizeRequest(requestBody, serverAndPort, repositoryDirectory);
            storage.cleanUpAfterSynchronization(populatedRepositoryContent);
        } catch (SendSynchronizeRequestException | IOException | CheckSumCalculationException e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw new SynchronizeRepositoryException();
        } catch (CleanUpAfterSynchronizationException e) {
            // TODO #32884 We should somehow inform the administrator that it failed and/or revert it
            e.printStackTrace();
        }
    }

    private void sendSynchronizeRequest(
            SynchronizeRepositoryRequestBody request, String serverAddress, String repositoryDirectory)
            throws SendSynchronizeRequestException, IOException, CheckSumCalculationException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        List<MultiValueMap<String, Object>> chunks = request.toChunks(maxRequestSize);
        chunksSize = chunks.size();

        log.debug("Sending chunk to repo...");

        try {
            String id = "";
            for (MultiValueMap<String, Object> chunk : chunks) {
                chunk.add("id", id);

                HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(chunk);
                ResponseEntity<RepoResponse> httpResponse = rest.postForEntity(
                        serverAddress + "/python/" + repositoryDirectory, entity, RepoResponse.class);

                if (!httpResponse.getStatusCode().is2xxSuccessful()
                        || !Objects.equals(httpResponse.getBody().getMessage(), "OK")) {
                    throw new SendSynchronizeRequestException();
                }

                id = httpResponse.getBody().getId();
            }
        } catch (RestClientException e) {
            log.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
            throw new SendSynchronizeRequestException();
        } finally {
            IntStream.range(0, chunksSize).forEachOrdered(chunkNo -> {
                String dirToRemovePath = FilesHierarchy.getArchiveDirectory(
                        request.getFilesToUpload().get(0), chunkNo);
                File dirToRemove = new File(dirToRemovePath);
                if (dirToRemove.exists()) {
                    FilesHierarchy.deleteDirectory(dirToRemove);
                }
            });
        }
    }
}

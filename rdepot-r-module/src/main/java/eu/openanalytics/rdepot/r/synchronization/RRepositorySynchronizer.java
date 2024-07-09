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
package eu.openanalytics.rdepot.r.synchronization;

import com.google.gson.Gson;
import eu.openanalytics.rdepot.base.storage.exceptions.CleanUpAfterSynchronizationException;
import eu.openanalytics.rdepot.base.storage.exceptions.OrganizePackagesException;
import eu.openanalytics.rdepot.base.synchronization.RepoResponse;
import eu.openanalytics.rdepot.base.synchronization.RepositorySynchronizer;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.base.synchronization.exceptions.SendSynchronizeRequestException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.storage.RStorage;
import eu.openanalytics.rdepot.r.storage.utils.PopulatedRepositoryContent;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class RRepositorySynchronizer extends RepositorySynchronizer<RRepository> {

    private final RStorage storage;
    private final RPackageService packageService;
    private final RestTemplate rest;

    @Value("${local-storage.max-request-size}")
    private Integer maxRequestSize;

    public static final Comparator<RPackage> PACKAGE_COMPARATOR = Comparator.comparingInt(RPackage::getId);

    @Override
    @Transactional
    public void storeRepositoryOnRemoteServer(RRepository repository, String dateStamp)
            throws SynchronizeRepositoryException {
        LinkedHashSet<RPackage> packages = new LinkedHashSet<>(packageService.findActiveByRepository(repository));
        List<RPackage> allPackages = new LinkedList<>(packages);

        Set<RPackage> latestPackageSet = packageService.filterLatest(packages);
        packages.removeAll(latestPackageSet);

        List<RPackage> archivePackages = new LinkedList<>(packages);
        List<RPackage> latestPackages = new LinkedList<>(latestPackageSet);

        archivePackages.sort(PACKAGE_COMPARATOR);
        latestPackages.sort(PACKAGE_COMPARATOR);
        allPackages.sort(PACKAGE_COMPARATOR);

        storeRepositoryOnRemoteServer(repository, dateStamp, allPackages, archivePackages, latestPackages);
    }

    private synchronized void storeRepositoryOnRemoteServer(
            RRepository repository,
            String dateStamp,
            List<RPackage> packages,
            List<RPackage> archivePackages,
            List<RPackage> latestPackages)
            throws SynchronizeRepositoryException {
        try {
            synchronizeRepository(
                    storage.organizePackagesInStorage(dateStamp, packages, latestPackages, archivePackages, repository),
                    repository);
        } catch (OrganizePackagesException e) {
            log.error(e.getMessage(), e);
            throw new SynchronizeRepositoryException();
        }
    }

    private void synchronizeRepository(PopulatedRepositoryContent populatedRepositoryContent, RRepository repository)
            throws SynchronizeRepositoryException {

        String[] serverAddressComponents = repository.getServerAddress().split("/");
        if (serverAddressComponents.length < 4) {
            throw new IllegalStateException("Incorrect server address: " + repository.getServerAddress());
        }

        String serverAndPort = serverAddressComponents[0] + "//" + serverAddressComponents[2];
        StringBuilder repositoryDirectory = new StringBuilder();
        int i = 3;
        for (; i < serverAddressComponents.length - 1; i++) {
            repositoryDirectory.append(serverAddressComponents[i]);
            repositoryDirectory.append("/");
        }
        repositoryDirectory.append(serverAddressComponents[i]);

        Gson gson = new Gson();

        try {
            ResponseEntity<String> response =
                    rest.getForEntity(serverAndPort + "/" + repositoryDirectory + "/", String.class);

            List<String> remoteLatestPackages =
                    new ArrayList<>(Arrays.asList(gson.fromJson(response.getBody(), String[].class)));
            response = rest.getForEntity(serverAndPort + "/" + repositoryDirectory + "/archive/", String.class);
            List<String> remoteArchivePackages =
                    new ArrayList<>(Arrays.asList(gson.fromJson(response.getBody(), String[].class)));

            String versionBefore = remoteLatestPackages.remove(0);

            SynchronizeRepositoryRequestBody requestBody = storage.buildSynchronizeRequestBody(
                    populatedRepositoryContent, remoteLatestPackages, remoteArchivePackages, repository, versionBefore);

            sendSynchronizeRequest(requestBody, serverAndPort, repositoryDirectory.toString());
            storage.cleanUpAfterSynchronization(populatedRepositoryContent);
        } catch (SendSynchronizeRequestException | RestClientException e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw new SynchronizeRepositoryException();
        } catch (CleanUpAfterSynchronizationException e) {
            // TODO #32884 We should somehow inform the administrator that it failed and/or revert it
            log.error(e.getMessage(), e);
        }
    }

    private void sendSynchronizeRequest(
            SynchronizeRepositoryRequestBody request, String serverAddress, String repositoryDirectory)
            throws SendSynchronizeRequestException {
        List<MultiValueMap<String, Object>> chunks = request.toChunks(maxRequestSize);

        log.debug("Sending chunk to repo...");

        try {
            String id = "";
            for (MultiValueMap<String, Object> chunk : chunks) {
                chunk.add("id", id);

                final HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_TYPE, ContentType.MULTIPART_FORM_DATA.getMimeType());
                HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(chunk, headers);
                ResponseEntity<RepoResponse> httpResponse =
                        rest.postForEntity(serverAddress + "/r/" + repositoryDirectory, entity, RepoResponse.class);

                if (!httpResponse.getStatusCode().is2xxSuccessful()
                        || !Objects.equals(
                                Objects.requireNonNull(httpResponse.getBody()).getMessage(), "OK")) {
                    throw new SendSynchronizeRequestException();
                }

                id = httpResponse.getBody().getId();
            }
        } catch (RestClientException e) {
            log.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
            throw new SendSynchronizeRequestException();
        }
    }
}

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
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class RRepositorySynchronizer extends RepositorySynchronizer<RRepository> {

    public static final Comparator<RPackage> PACKAGE_COMPARATOR = Comparator.comparingInt(RPackage::getId);
    private final RStorage storage;
    private final RPackageService packageService;
    private final RestTemplate rest;
    private final RRequestBodyPartitioner rRequestBodyPartitioner;

    @Value("${local-storage.max-request-size}")
    private Integer maxRequestSize;

    @Override
    @Transactional
    public void storeRepositoryOnRemoteServer(RRepository repository, String dateStamp)
            throws SynchronizeRepositoryException {

        LinkedHashSet<RPackage> sourcePackages =
                new LinkedHashSet<>(packageService.findSourcePackagesByRepository(repository));
        LinkedHashSet<RPackage> binaryPackages =
                new LinkedHashSet<>(packageService.findBinaryPackagesByRepository(repository));

        List<RPackage> allSourcePackages = new LinkedList<>(sourcePackages);
        List<RPackage> allBinaryPackages = new LinkedList<>(binaryPackages);

        Set<RPackage> latestSourcePackageSet = packageService.filterLatest(sourcePackages);
        sourcePackages.removeAll(latestSourcePackageSet);

        Set<RPackage> latestBinaryPackageSet = packageService.filterLatest(binaryPackages);
        binaryPackages.removeAll(latestBinaryPackageSet);

        List<RPackage> archiveSourcePackages = new LinkedList<>(sourcePackages);
        List<RPackage> latestSourcePackages = new LinkedList<>(latestSourcePackageSet);
        List<RPackage> archiveBinaryPackages = new LinkedList<>(binaryPackages);
        List<RPackage> latestBinaryPackages = new LinkedList<>(latestBinaryPackageSet);

        archiveSourcePackages.sort(PACKAGE_COMPARATOR);
        latestSourcePackages.sort(PACKAGE_COMPARATOR);
        allSourcePackages.sort(PACKAGE_COMPARATOR);
        archiveBinaryPackages.sort(PACKAGE_COMPARATOR);
        latestBinaryPackages.sort(PACKAGE_COMPARATOR);
        allBinaryPackages.sort(PACKAGE_COMPARATOR);

        storeRepositoryOnRemoteServer(
                repository,
                dateStamp,
                allSourcePackages,
                archiveSourcePackages,
                latestSourcePackages,
                allBinaryPackages,
                archiveBinaryPackages,
                latestBinaryPackages);
    }

    private synchronized void storeRepositoryOnRemoteServer(
            RRepository repository,
            String dateStamp,
            List<RPackage> sourcePackages,
            List<RPackage> archiveSourcePackages,
            List<RPackage> latestSourcePackages,
            List<RPackage> binaryPackages,
            List<RPackage> archiveBinaryPackages,
            List<RPackage> latestBinaryPackages)
            throws SynchronizeRepositoryException {
        try {
            synchronizeRepository(
                    storage.organizePackagesInStorage(
                            dateStamp,
                            sourcePackages,
                            latestSourcePackages,
                            archiveSourcePackages,
                            binaryPackages,
                            latestBinaryPackages,
                            archiveBinaryPackages,
                            repository),
                    repository);
        } catch (OrganizePackagesException e) {
            log.error(e.getMessage(), e);
            throw new SynchronizeRepositoryException();
        }
    }

    private RemoteState getRemoteState(String serverAndPort, String repositoryDirectory, boolean archive) {
        final Gson gson = new Gson();

        final ResponseEntity<String> response = rest.getForEntity(
                serverAndPort + "/" + repositoryDirectory + (archive ? "/archive/" : "/"), String.class);

        final List<String> remotePackages =
                new ArrayList<>(Arrays.asList(gson.fromJson(response.getBody(), String[].class)));

        final List<String> remoteSourcePackages = remotePackages.stream()
                .filter(file -> StringUtils.contains(file, "src/contrib/"))
                .map(file -> StringUtils.substringAfterLast(file, "/"))
                .toList();

        // <path = "bin/... , filename>
        final MultiValueMap<String, String> remoteBinaryPackages = new LinkedMultiValueMap<>();
        remotePackages.stream()
                .filter(file -> StringUtils.startsWith(file, "bin/"))
                .forEach(file -> remoteBinaryPackages.add(
                        StringUtils.substringBeforeLast(file, "/"), StringUtils.substringAfterLast(file, "/")));

        return new RemoteState(remoteSourcePackages, remoteBinaryPackages, remotePackages);
    }

    private void synchronizeRepository(PopulatedRepositoryContent populatedRepositoryContent, RRepository repository)
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

        try {
            final RemoteState remoteLatestState = getRemoteState(serverAndPort, repositoryDirectory, false);
            final RemoteState remoteArchiveState = getRemoteState(serverAndPort, repositoryDirectory, true);
            final String versionBefore = remoteLatestState.getPackages().remove(0);

            final SynchronizeRepositoryRequestBody requestBody = storage.buildSynchronizeRequestBody(
                    populatedRepositoryContent,
                    remoteLatestState.getSourcePackages(),
                    remoteArchiveState.getSourcePackages(),
                    remoteLatestState.getBinaryPackages(),
                    remoteArchiveState.getBinaryPackages(),
                    repository,
                    versionBefore);

            sendSynchronizeRequest(requestBody, serverAndPort, repositoryDirectory);
            storage.cleanUpAfterSynchronization(populatedRepositoryContent);
        } catch (SendSynchronizeRequestException | RestClientException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new SynchronizeRepositoryException();
        } catch (CleanUpAfterSynchronizationException e) {
            // TODO #32884 We should somehow inform the administrator that it failed and/or revert it
            log.error(e.getMessage(), e);
        }
    }

    private void sendSynchronizeRequest(
            SynchronizeRepositoryRequestBody request, String serverAddress, String repositoryDirectory)
            throws SendSynchronizeRequestException {
        final ChunksData chunksData = rRequestBodyPartitioner.toChunks(request, maxRequestSize);

        log.debug("Sending chunk to repo...");

        try {
            String id = "";
            for (MultiValueMap<String, Object> chunk : chunksData.getChunks()) {
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

            revertFileNamesChange(chunksData.getOldFilenames());

        } catch (RestClientException e) {
            log.error("{}: {}", e.getClass().getCanonicalName(), e.getMessage(), e);
            throw new SendSynchronizeRequestException();
        }
    }

    private void revertFileNamesChange(Map<File, String> oldFilenames) {

        oldFilenames.forEach((file, oldFilename) -> {
            final File withoutPrefixFile =
                    new File(file.getParent() + "/" + StringUtils.substringAfter(file.getName(), "_"));
            try {
                FileUtils.moveFile(file, withoutPrefixFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new IllegalStateException(
                        "Could not properly revert file names changes after sending the chunks!");
            }
        });
    }

    @Getter
    @AllArgsConstructor
    private static class RemoteState {
        List<String> sourcePackages;
        MultiValueMap<String, String> binaryPackages;
        List<String> packages;
    }
}

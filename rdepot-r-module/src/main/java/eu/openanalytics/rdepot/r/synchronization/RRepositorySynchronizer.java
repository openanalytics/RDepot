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
import eu.openanalytics.rdepot.base.synchronization.checksums.Checksum;
import eu.openanalytics.rdepot.base.synchronization.checksums.Checksums;
import eu.openanalytics.rdepot.base.synchronization.exceptions.SendSynchronizeRequestException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.storage.population.PopulatedRepositoryContent;
import eu.openanalytics.rdepot.r.storage.population.RPopulator;
import eu.openanalytics.rdepot.r.synchronization.partitioning.RRequestBodyPartitioner;
import eu.openanalytics.rdepot.r.synchronization.partitioning.structs.ChunkedRequestBody;
import eu.openanalytics.rdepot.r.technology.RLanguage;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private final RPopulator populator;
    private final RPackageService packageService;
    private final RestTemplate repoApiClient;
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
            final List<String> binaryPlatforms = getBinaryPlatformsForRepository(repository);
            synchronizeRepository(
                    populator.organizePackagesInStorage(
                            dateStamp,
                            sourcePackages,
                            latestSourcePackages,
                            archiveSourcePackages,
                            binaryPackages,
                            latestBinaryPackages,
                            archiveBinaryPackages,
                            binaryPlatforms,
                            repository),
                    repository);
        } catch (OrganizePackagesException e) {
            log.error(e.getMessage(), e);
            throw new SynchronizeRepositoryException();
        }
    }

    /**
     * @return e.g. "bin/linux/x86_64/centos/4.5"
     */
    private List<String> getBinaryPlatformsForRepository(RRepository repository) {
        final Gson gson = new Gson();
        final ServerAddressPortAndDirectory serverAddressPortAndDirectory = parseServerAddress(repository);

        final String serverAndPort = serverAddressPortAndDirectory.serverAndPort();
        final String repositoryDirectory = serverAddressPortAndDirectory.repositoryDirectory();

        final ResponseEntity<String> response = repoApiClient.getForEntity(
                serverAndPort
                        + (repositoryDirectory.startsWith("r/") ? "/" : "/r/")
                        + repositoryDirectory + "/platforms",
                String.class);

        return new ArrayList<>(Arrays.asList(gson.fromJson(response.getBody(), String[].class)));
    }

    private RemoteState getRemoteState(
            String serverAndPort, String repositoryDirectory, Checksums checksums, boolean archive) {
        final Gson gson = new Gson();

        final ResponseEntity<String> response = repoApiClient.getForEntity(
                attachTechnologyIfNeeded(serverAndPort, repositoryDirectory, RLanguage.instance)
                        + (archive ? "archive/" : ""),
                String.class);

        final List<String> remotePackages =
                new ArrayList<>(Arrays.asList(gson.fromJson(response.getBody(), String[].class)));

        final List<String> remoteSourcePackages = new ArrayList<>();
        remotePackages.stream()
                .filter(file -> StringUtils.contains(file, "src/contrib/"))
                .forEach(file -> {
                    String filePath = file.substring(0, file.indexOf("="));
                    remoteSourcePackages.add(StringUtils.substringAfterLast(filePath, "/"));
                    checksums.addChecksum(new Checksum(filePath, file.substring(file.indexOf("=") + 1)));
                });

        // <path = "bin/... , filename>
        final MultiValueMap<String, String> remoteBinaryPackages = new LinkedMultiValueMap<>();
        remotePackages.stream()
                .filter(file -> StringUtils.startsWith(file, "bin/"))
                .forEach(file -> {
                    String filePath = file.substring(0, file.indexOf("="));
                    remoteBinaryPackages.add(
                            StringUtils.substringBeforeLast(filePath, "/"),
                            StringUtils.substringAfterLast(filePath, "/"));
                    checksums.addChecksum(new Checksum(filePath, file.substring(file.indexOf("=") + 1)));
                });

        return new RemoteState(remoteSourcePackages, remoteBinaryPackages, remotePackages);
    }

    private record ServerAddressPortAndDirectory(String serverAndPort, String repositoryDirectory) {}

    private ServerAddressPortAndDirectory parseServerAddress(RRepository repository) {
        final String[] serverAddressComponents = repository.getServerAddress().split("/");
        if (serverAddressComponents.length < 4) {
            throw new IllegalStateException("Incorrect server address: " + repository.getServerAddress());
        }

        final String serverAndPort = serverAddressComponents[0] + "//" + serverAddressComponents[2];
        final String repositoryDirectory = String.join(
                "/",
                Arrays.stream(serverAddressComponents, 3, serverAddressComponents.length)
                        .toArray(String[]::new));
        return new ServerAddressPortAndDirectory(serverAndPort, repositoryDirectory);
    }

    private void synchronizeRepository(PopulatedRepositoryContent populatedRepositoryContent, RRepository repository)
            throws SynchronizeRepositoryException {

        final ServerAddressPortAndDirectory serverAddressPortAndDirectory = parseServerAddress(repository);
        final String repositoryDirectory = serverAddressPortAndDirectory.repositoryDirectory();
        final String serverAndPort = serverAddressPortAndDirectory.serverAndPort();

        try {
            final Checksums checksums = new Checksums();
            final RemoteState remoteLatestState = getRemoteState(serverAndPort, repositoryDirectory, checksums, false);
            final RemoteState remoteArchiveState = getRemoteState(serverAndPort, repositoryDirectory, checksums, true);
            final String versionBefore = remoteLatestState.getPackages().remove(0);

            final SynchronizeRepositoryRequestBody requestBody = populator.buildSynchronizeRequestBody(
                    populatedRepositoryContent,
                    remoteLatestState.getSourcePackages(),
                    remoteArchiveState.getSourcePackages(),
                    remoteLatestState.getBinaryPackages(),
                    remoteArchiveState.getBinaryPackages(),
                    checksums,
                    repository,
                    versionBefore);

            sendSynchronizeRequest(requestBody, serverAndPort, repositoryDirectory);
            populator.cleanUpAfterSynchronization(populatedRepositoryContent);
        } catch (SendSynchronizeRequestException | RestClientException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new SynchronizeRepositoryException();
        } catch (CleanUpAfterSynchronizationException e) {
            // TODO #32884 We should somehow inform the administrator that it failed and/or revert it
            log.error(e.getMessage(), e);
        }
    }

    private String postChunk(MultiValueMap<String, Object> chunk, String serverAddress, String repositoryDirectory)
            throws SendSynchronizeRequestException {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, ContentType.MULTIPART_FORM_DATA.getMimeType());
        final HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(chunk, headers);
        final ResponseEntity<RepoResponse> httpResponse = repoApiClient.postForEntity(
                attachTechnologyIfNeeded(serverAddress, repositoryDirectory, RLanguage.instance),
                entity,
                RepoResponse.class);

        if (!httpResponse.getStatusCode().is2xxSuccessful()
                || !Objects.equals(
                        Objects.requireNonNull(httpResponse.getBody()).getMessage(), "OK")) {
            throw new SendSynchronizeRequestException();
        }

        return httpResponse.getBody().getId();
    }

    private void sendSynchronizeRequest(
            SynchronizeRepositoryRequestBody request, String serverAddress, String repositoryDirectory)
            throws SendSynchronizeRequestException {
        final ChunkedRequestBody chunks = rRequestBodyPartitioner.partition(request, maxRequestSize);
        log.debug("Sending chunk to repo...");

        try {
            final String id = postChunk(chunks.firstChunkToMap(), serverAddress, repositoryDirectory);
            for (MultiValueMap<String, Object> chunk : chunks.otherChunksToMaps(id)) {
                postChunk(chunk, serverAddress, repositoryDirectory);
            }
        } catch (RestClientException e) {
            log.error("{}: {}", e.getClass().getCanonicalName(), e.getMessage(), e);
            throw new SendSynchronizeRequestException();
        } finally {
            revertFileNamesChange(chunks.getOriginalFileNames());
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

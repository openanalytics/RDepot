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

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@Component
public class RRequestBodyPartitioner {

    private static final String PACKAGES = "PACKAGES";

    private File prepareFile(File originalFile, String destinationPath, Map<File, String> originalFilenames) {

        String prefix = destinationPath.replaceAll("[^a-zA-Z0-9]", "");

        final File withPrefixFile = originalFile
                .toPath()
                .getParent()
                .resolve(FilenameUtils.getName(prefix + "_" + originalFile.getName()))
                .toFile();

        try {
            FileUtils.moveFile(originalFile, withPrefixFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException("Could not properly prepare files in chunks!");
        }

        originalFilenames.put(withPrefixFile, originalFile.getName());

        return withPrefixFile;
    }

    private String prepareFileName(String fileName, String destinationPath) {
        return destinationPath.replaceAll("[^a-zA-Z0-9]", "") + "_" + fileName;
    }

    private void addPackagesFilesToChunk(
            List<Map<File, String>> chunks,
            Map<String, File> files,
            Map<File, String> originalFilenames,
            int elementsPerChunk,
            boolean archive) {
        files.forEach((path, file) -> {
            String latestOrArchive = archive ? "archive" : "latest";

            if (!chunks.isEmpty()) {
                int numberOfChunks = chunks.size();
                int lastChunkSize = chunks.get(numberOfChunks - 1).size();

                if (lastChunkSize < elementsPerChunk) {
                    chunks.get(numberOfChunks - 1)
                            .put(prepareFile(file, path + latestOrArchive, originalFilenames), path);
                } else {
                    chunks.add(new HashMap<>() {

                        @Serial
                        private static final long serialVersionUID = 2812478604460246280L;

                        {
                            put(prepareFile(file, path + latestOrArchive, originalFilenames), path);
                        }
                    });
                }
            } else {
                chunks.add(new HashMap<>() {

                    @Serial
                    private static final long serialVersionUID = -784770924031218164L;

                    {
                        put(prepareFile(file, path + latestOrArchive, originalFilenames), path);
                    }
                });
            }
        });
    }

    private void addBinaryFilesToChunk(
            List<Map<File, String>> chunks,
            MultiValueMap<String, File> files,
            Map<File, String> originalFilenames,
            int elementsPerChunk) {
        files.forEach((binaryPath, packageList) -> packageList.forEach(packageBag -> {
            if (!chunks.isEmpty()) {
                int numberOfChunks = chunks.size();
                int lastChunkSize = chunks.get(numberOfChunks - 1).size();

                if (lastChunkSize < elementsPerChunk) {
                    chunks.get(numberOfChunks - 1)
                            .put(prepareFile(packageBag, binaryPath, originalFilenames), binaryPath);
                } else {
                    chunks.add(new HashMap<>() {

                        @Serial
                        private static final long serialVersionUID = 5756027664009855699L;

                        {
                            put(prepareFile(packageBag, binaryPath, originalFilenames), binaryPath);
                        }
                    });
                }
            } else {
                chunks.add(new HashMap<>() {

                    @Serial
                    private static final long serialVersionUID = -3047969254961434248L;

                    {
                        put(prepareFile(packageBag, binaryPath, originalFilenames), binaryPath);
                    }
                });
            }
        }));
    }

    private List<Map<File, String>> getRecentChunks(
            int elementsPerChunk, Map<File, String> originalFilenames, SynchronizeRepositoryRequestBody requestBody) {
        return getChunks(elementsPerChunk, originalFilenames, requestBody, false);
    }

    private List<Map<File, String>> getChunks(
            int elementsPerChunk,
            Map<File, String> originalFilenames,
            SynchronizeRepositoryRequestBody requestBody,
            boolean archive) {
        List<Map<File, String>> chunks = new ArrayList<>(ListUtils.partition(
                        archive
                                ? requestBody.getSourcePackagesToUploadToArchive()
                                : requestBody.getSourcePackagesToUpload(),
                        elementsPerChunk))
                .stream()
                        .map(chunk -> chunk.stream()
                                .map(file -> prepareFile(file, requestBody.getSourceDirectoryPath(), originalFilenames))
                                .collect(Collectors.toMap(
                                        Function.identity(), f -> requestBody.getSourceDirectoryPath())))
                        .collect(Collectors.toList());
        addBinaryFilesToChunk(
                chunks,
                archive ? requestBody.getBinaryPackagesToUploadToArchive() : requestBody.getBinaryPackagesToUpload(),
                originalFilenames,
                elementsPerChunk);
        addPackagesFilesToChunk(
                chunks,
                archive ? requestBody.getPackagesFilesForArchive() : requestBody.getPackagesFiles(),
                originalFilenames,
                elementsPerChunk,
                archive);
        addPackagesFilesToChunk(
                chunks,
                archive ? requestBody.getPackagesGzFilesForArchive() : requestBody.getPackagesGzFiles(),
                originalFilenames,
                elementsPerChunk,
                archive);
        return chunks;
    }

    private List<Map<File, String>> getArchiveChunks(
            int elementsPerChunk, Map<File, String> originalFilenames, SynchronizeRepositoryRequestBody requestBody) {
        return getChunks(elementsPerChunk, originalFilenames, requestBody, true);
    }

    public ChunksData toChunks(SynchronizeRepositoryRequestBody requestBody, int elementsPerChunk) {
        List<MultiValueMap<String, Object>> chunks = new ArrayList<>();
        Map<File, String> originalFilenames = new HashMap<>();

        List<Map<File, String>> recentChunks = getRecentChunks(elementsPerChunk, originalFilenames, requestBody);
        List<Map<File, String>> archiveChunks = getArchiveChunks(elementsPerChunk, originalFilenames, requestBody);

        int pageCount = Integer.max(recentChunks.size(), archiveChunks.size());

        int currentVersion = Integer.parseInt(requestBody.getVersionBefore());

        for (int currentPage = 1; currentPage <= pageCount; currentPage++) {

            // <file_name, checksum>
            final Map<String, String> checksumsForChunk = new HashMap<>();
            // <file_name, path>
            final Map<String, String> pathsForChunk = new HashMap<>();
            // <file_name, path>
            final Map<String, String> archivePathsForChunk = new HashMap<>();

            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

            if (!recentChunks.isEmpty()) {
                Map<File, String> recentChunk = recentChunks.remove(0);
                recentChunk.forEach((file, filePath) -> {
                    map.add("files", new FileSystemResource(file));
                    String getChecksum = file.getName().contains(PACKAGES)
                            ? "recent/" + StringUtils.substringAfter(file.getName(), "_")
                            : StringUtils.substringAfter(file.getName(), "_");
                    checksumsForChunk.put(
                            file.getName(),
                            requestBody.getChecksums().get(filePath).get(getChecksum));
                    pathsForChunk.put(file.getName(), filePath);
                });
            }

            if (!archiveChunks.isEmpty()) {
                Map<File, String> archiveChunk = archiveChunks.remove(0);
                archiveChunk.forEach((file, filePath) -> {
                    map.add("files_archive", new FileSystemResource(file));
                    String getChecksum = file.getName().contains(PACKAGES)
                            ? "archive/" + StringUtils.substringAfter(file.getName(), "_")
                            : StringUtils.substringAfter(file.getName(), "_");
                    checksumsForChunk.put(
                            file.getName(),
                            requestBody.getChecksums().get(filePath).get(getChecksum));
                    archivePathsForChunk.put(file.getName(), filePath);
                });
            }

            if (currentPage == 1) {
                Map<String, String> pathsToDelete = new HashMap<>();
                Map<String, String> pathsToDeleteFromArchive = new HashMap<>();

                requestBody.getSourcePackagesToDelete().forEach(packageName -> {
                    String nameWithPrefix = prepareFileName(packageName, requestBody.getSourceDirectoryPath());
                    map.add("to_delete", nameWithPrefix);
                    pathsToDelete.put(nameWithPrefix, requestBody.getSourceDirectoryPath());
                });

                requestBody.getSourcePackagesToDeleteFromArchive().forEach(packageName -> {
                    String nameWithPrefix = prepareFileName(packageName, requestBody.getSourceDirectoryPath());
                    map.add("to_delete_archive", nameWithPrefix);
                    pathsToDeleteFromArchive.put(nameWithPrefix, requestBody.getSourceDirectoryPath());
                });

                requestBody
                        .getBinaryPackagesToDelete()
                        .forEach((binaryPath, packageList) -> packageList.forEach(packageName -> {
                            String nameWithPrefix = prepareFileName(packageName, binaryPath);
                            map.add("to_delete", nameWithPrefix);
                            pathsToDelete.put(nameWithPrefix, binaryPath);
                        }));

                requestBody
                        .getBinaryPackagesToDeleteFromArchive()
                        .forEach((binaryPath, packageList) -> packageList.forEach(packageName -> {
                            String nameWithPrefix = prepareFileName(packageName, binaryPath);
                            map.add("to_delete_archive", nameWithPrefix);
                            pathsToDeleteFromArchive.put(nameWithPrefix, binaryPath);
                        }));

                map.add("to_delete_paths", pathsToDelete);
                map.add("to_delete_paths_archive", pathsToDeleteFromArchive);
            } else {
                map.add("to_delete_paths", new HashMap<String, String>());
                map.add("to_delete_paths_archive", new HashMap<String, String>());
            }

            map.add("version_before", String.valueOf(currentVersion));

            currentVersion = currentVersion == Integer.MAX_VALUE ? 0 : currentVersion;
            map.add("version_after", String.valueOf(++currentVersion));

            map.add("page", currentPage + "/" + pageCount);
            map.add("paths", pathsForChunk);
            map.add("paths_archive", archivePathsForChunk);
            map.add("checksums", checksumsForChunk);

            chunks.add(map);
        }

        return new ChunksData(chunks, originalFilenames);
    }
}

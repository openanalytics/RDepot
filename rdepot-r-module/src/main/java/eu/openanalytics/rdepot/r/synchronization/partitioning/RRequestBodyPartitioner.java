/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
package eu.openanalytics.rdepot.r.synchronization.partitioning;

import eu.openanalytics.rdepot.r.synchronization.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.r.synchronization.partitioning.structs.ChunkedRequestBody;
import eu.openanalytics.rdepot.r.synchronization.partitioning.structs.RequestBodyChunk;
import eu.openanalytics.rdepot.r.synchronization.partitioning.structs.RequestBodyFirstChunk;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
@Slf4j
public class RRequestBodyPartitioner {
    private RequestBodyFirstChunk prepareFirstChunk(final SynchronizeRepositoryRequestBody requestBody) {
        final RequestBodyFirstChunk firstChunk = new RequestBodyFirstChunk();
        firstChunk.addSourceFilesToDelete(requestBody.getSourcePackagesToDelete());
        firstChunk.addSourceFilesToDeleteFromArchive(requestBody.getSourcePackagesToDeleteFromArchive());
        requestBody
                .getBinaryPackagesToDelete()
                .forEach((path, filenames) ->
                        filenames.forEach(filename -> firstChunk.addBinaryFileToDelete(path, filename)));
        requestBody
                .getBinaryPackagesToDeleteFromArchive()
                .forEach((path, filenames) ->
                        filenames.forEach(filename -> firstChunk.addBinaryFileToDeleteFromArchive(path, filename)));
        return firstChunk;
    }

    public ChunkedRequestBody partition(
            final SynchronizeRepositoryRequestBody requestBody, final int elementsPerChunk) {
        log.debug("Splitting the synchronization request into {} chunks", elementsPerChunk);
        final RequestBodyFirstChunk firstChunk = prepareFirstChunk(requestBody);

        final Map<String, String> checksums = requestBody.getChecksums().toMap();
        final List<List<File>> sourceFiles =
                ListUtils.partition(requestBody.getSourcePackagesToUpload(), elementsPerChunk);
        final List<List<File>> sourceArchiveFiles =
                ListUtils.partition(requestBody.getSourcePackagesToUploadToArchive(), elementsPerChunk);
        final List<MultiValueMap<String, File>> binaryFiles =
                partitionMap(requestBody.getBinaryPackagesToUpload(), elementsPerChunk);
        final List<MultiValueMap<String, File>> binaryArchiveFiles =
                partitionMap(requestBody.getBinaryPackagesToUploadToArchive(), elementsPerChunk);
        final List<Map<String, File>> packagesFiles = partitionMap(requestBody.getPackagesFiles(), elementsPerChunk);
        final List<Map<String, File>> packagesGzFiles =
                partitionMap(requestBody.getPackagesGzFiles(), elementsPerChunk);
        final List<Map<String, File>> packagesFilesForArchive =
                partitionMap(requestBody.getPackagesFilesForArchive(), elementsPerChunk);
        final List<Map<String, File>> packagesGzFilesForArchive =
                partitionMap(requestBody.getPackagesGzFilesForArchive(), elementsPerChunk);
        final List<Map<String, File>> indexes = partitionMap(requestBody.getIndexes(), elementsPerChunk);
        final List<Map<String, File>> indexesForArchive =
                partitionMap(requestBody.getIndexesForArchive(), elementsPerChunk);

        final int chunkCount = maxSize(
                sourceFiles,
                sourceArchiveFiles,
                binaryFiles,
                binaryArchiveFiles,
                packagesFiles,
                packagesGzFiles,
                packagesFilesForArchive,
                packagesGzFilesForArchive,
                indexes,
                indexesForArchive);

        final List<RequestBodyChunk> secondAndOtherChunks = new LinkedList<>();
        final Map<File, String> originalFilenames = new LinkedHashMap<>();
        for (int i = 0; i < chunkCount; i++) {
            RequestBodyChunk chunk;
            if (i == 0) {
                chunk = firstChunk;
            } else {
                chunk = new RequestBodyChunk();
                secondAndOtherChunks.add(chunk);
            }

            addSourceFilesIfStillAny(i, sourceFiles, chunk, originalFilenames, false, checksums);
            addSourceFilesIfStillAny(i, sourceArchiveFiles, chunk, originalFilenames, true, checksums);
            addBinaryFilesIfStillAny(i, binaryFiles, chunk, originalFilenames, false, checksums);
            addBinaryFilesIfStillAny(i, binaryArchiveFiles, chunk, originalFilenames, true, checksums);
            addMapChunkIfStillAny(i, packagesFiles, chunk, originalFilenames, false, checksums);
            addMapChunkIfStillAny(i, packagesGzFiles, chunk, originalFilenames, false, checksums);
            addMapChunkIfStillAny(i, packagesFilesForArchive, chunk, originalFilenames, true, checksums);
            addMapChunkIfStillAny(i, packagesGzFilesForArchive, chunk, originalFilenames, true, checksums);
            addMapChunkIfStillAny(i, indexes, chunk, originalFilenames, false, checksums);
            addMapChunkIfStillAny(i, indexesForArchive, chunk, originalFilenames, true, checksums);
        }
        return new ChunkedRequestBody(
                firstChunk, secondAndOtherChunks, originalFilenames, Integer.parseInt(requestBody.getVersionBefore()));
    }

    private void addBinaryFilesIfStillAny(
            int i,
            List<MultiValueMap<String, File>> maps,
            RequestBodyChunk chunk,
            Map<File, String> originalFilenames,
            boolean archive,
            Map<String, String> checksums) {
        if (i + 1 <= maps.size()) {
            for (Map.Entry<String, List<File>> entry : maps.get(i).entrySet()) {
                final String binaryDestinationPath = entry.getKey();
                for (File file : entry.getValue()) {

                    addFileToChunk(file, binaryDestinationPath, chunk, originalFilenames, archive, checksums);
                }
            }
        }
    }

    private void addSourceFilesIfStillAny(
            int i,
            List<List<File>> files,
            RequestBodyChunk chunk,
            Map<File, String> originalFilenames,
            boolean archive,
            Map<String, String> checksums) {
        if (i + 1 <= files.size()) {
            final String sourceDestinationPath = "src/contrib";
            for (File file : files.get(i)) {
                addFileToChunk(file, sourceDestinationPath, chunk, originalFilenames, archive, checksums);
            }
        }
    }

    private void addMapChunkIfStillAny(
            int i,
            List<Map<String, File>> maps,
            RequestBodyChunk chunk,
            Map<File, String> originalFilenames,
            boolean archive,
            Map<String, String> checksums) {
        if (i + 1 <= maps.size()) {
            for (Map.Entry<String, File> entry : maps.get(i).entrySet()) {
                final String destinationPath = entry.getKey();
                addFileToChunk(entry.getValue(), destinationPath, chunk, originalFilenames, archive, checksums);
            }
        }
    }

    private List<MultiValueMap<String, File>> partitionMap(
            MultiValueMap<String, File> binaryPackagesToUpload, int elementsPerChunk) {
        final List<MultiValueMap<String, File>> partitions = new LinkedList<>();

        final List<Pair<String, File>> pathFilePairs = flatten(binaryPackagesToUpload);
        final List<List<Pair<String, File>>> flatPartitions = ListUtils.partition(pathFilePairs, elementsPerChunk);
        for (List<Pair<String, File>> flatPartition : flatPartitions) {
            final MultiValueMap<String, File> partition = new LinkedMultiValueMap<>();
            for (Pair<String, File> pair : flatPartition) {
                partition.add(pair.getLeft(), pair.getRight());
            }
            partitions.add(partition);
        }

        return partitions;
    }

    private List<Map<String, File>> partitionMap(Map<String, File> files, int elementsPerChunk) {
        final List<Map<String, File>> partitions = new LinkedList<>();
        final List<Map.Entry<String, File>> mapEntries =
                files.entrySet().stream().toList();
        final List<List<Map.Entry<String, File>>> partitionedEntries =
                ListUtils.partition(mapEntries, elementsPerChunk);

        for (List<Map.Entry<String, File>> partition : partitionedEntries) {
            final Map<String, File> map = new LinkedHashMap<>();
            for (Map.Entry<String, File> entry : partition) {
                map.put(entry.getKey(), entry.getValue());
            }
            partitions.add(map);
        }

        return partitions;
    }

    private List<Pair<String, File>> flatten(MultiValueMap<String, File> files) {
        final List<Pair<String, File>> pathFilePairs = new LinkedList<>();
        for (Map.Entry<String, List<File>> entry : files.entrySet()) {
            final String path = entry.getKey();
            for (File file : entry.getValue()) {
                pathFilePairs.add(Pair.of(path, file));
            }
        }
        return pathFilePairs;
    }

    private record PreparedFile(File file, String originalFilename) {}

    private void addFileToChunk(
            File toPrepare,
            String destinationPath,
            RequestBodyChunk chunk,
            Map<File, String> originalFilenames,
            boolean archive,
            Map<String, String> checksums) {
        if (!checksums.containsKey(toPrepare.getAbsolutePath())) {
            throw new IllegalStateException("No checksum was calculated for: " + toPrepare.getAbsolutePath());
        }
        final String checksum = checksums.get(toPrepare.getAbsolutePath());

        final PreparedFile preparedFile = prepareFile(toPrepare, destinationPath);
        final File preparedFileFile = preparedFile.file;
        originalFilenames.put(preparedFileFile, preparedFile.originalFilename);
        final FileSystemResource fileSystemResource = new FileSystemResource(preparedFileFile);

        if (!archive) {
            chunk.addFile(fileSystemResource);
            chunk.addPath(preparedFileFile.getName(), destinationPath);
        } else {
            chunk.addFileArchive(fileSystemResource);
            chunk.addPathInArchive(preparedFileFile.getName(), destinationPath);
        }
        chunk.addChecksum(fileSystemResource.getFilename(), checksum);
    }

    /**
     * Changes filename of provided package (or metadata) file
     * by adding its destination path with separators and all
     * non-alphanumeric characters removed. For example:
     * <code>abc_1.0.0.tar.gz</code> would become
     * <code>src/contrib/Archive/abc/abc_1.0.0.tar.gz</code>
     * This is needed because otherwise the same packages
     * but of different types (i.e. binary and source) could be mixed up
     * or replace one another in the request body.
     * @param originalFile original file
     * @param destinationPath e.g. <code>src/contrib/Archive/abc</code>
     */
    private PreparedFile prepareFile(File originalFile, String destinationPath) {
        final File withPrefixFile = originalFile
                .toPath()
                .getParent()
                .resolve(prepareFileName(originalFile.getName(), destinationPath))
                .toFile();

        try {
            // TODO: Should this happen here or in storage?
            //  This is a temporary file so it seems to be the right place
            //  Should be thought of in #22383
            FileUtils.moveFile(originalFile, withPrefixFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException("Could not properly prepare files in chunks!");
        }

        return new PreparedFile(withPrefixFile, originalFile.getName());
    }

    private String prepareFileName(String fileName, String destinationPath) {
        return destinationPath.replaceAll("[^a-zA-Z0-9]", "") + "_" + fileName;
    }

    private int maxSize(List<?>... lists) {
        int max = -1;
        for (final List<?> l : lists) {
            if (l.size() > max) {
                max = l.size();
            }
        }
        return max;
    }
}

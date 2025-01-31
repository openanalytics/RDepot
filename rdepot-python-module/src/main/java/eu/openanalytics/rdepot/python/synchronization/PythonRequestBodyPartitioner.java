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

import eu.openanalytics.rdepot.base.storage.exceptions.CheckSumCalculationException;
import eu.openanalytics.rdepot.python.entities.enums.HashMethod;
import eu.openanalytics.rdepot.python.mediator.hash.HashCalculator;
import eu.openanalytics.rdepot.python.storage.PythonPackageArchiver;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import org.apache.commons.collections4.ListUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class PythonRequestBodyPartitioner {

    private final PythonPackageArchiver archiver;

    public PythonRequestBodyPartitioner(PythonPackageArchiver pythonPackageArchiver) {
        this.archiver = pythonPackageArchiver;
    }

    /**
     * Splits the files into chunks, ensuring each chunk contains a limited number of files to prevent exceeding size limits.
     * <p>
     * The method divides the {@code filesToUpload} into smaller chunks based on the provided {@code elementsPerChunk} value.
     * Each chunk will contain a specific number of files, ensuring the size of each chunk remains manageable for the
     * repository application, which will process these chunks separately.
     * </p>
     * <p>
     * While there are remaining chunks to process, the method takes each unprocessed chunk and uses the
     * {@code prepareChunkFiles} method to process them. Each chunk will include the following details:
     * <ul>
     *   <li><b>files:</b> the files to upload in this chunk</li>
     *   <li><b>checksums:</b> the checksum for each file in the chunk</li>
     *   <li><b>hash_method:</b> the method used to calculate the checksums</li>
     *   <li><b>version_before:</b> the repository version before uploading the chunk</li>
     *   <li><b>version_after:</b> the repository version after uploading the chunk (resets to 0 if it exceeds max integer)</li>
     *   <li><b>page:</b> the chunk's order number to track the sequence</li>
     * </ul>
     * At the end of this process, if there are any files marked for remote deletion, their names are added to the first chunk.
     * </p>
     *
     * @param elementsPerChunk the number of files each chunk should contain
     * @return a list of prepared chunks ready for upload
     * @throws IOException if an I/O error occurs during file processing
     * @throws CheckSumCalculationException if an error occurs while calculating file checksums
     */
    public List<MultiValueMap<String, Object>> toChunks(
            SynchronizeRepositoryRequestBody requestBody, int elementsPerChunk)
            throws IOException, CheckSumCalculationException {
        List<MultiValueMap<String, Object>> chunks = new ArrayList<>();
        List<List<File>> recentChunks =
                new ArrayList<>(ListUtils.partition(requestBody.getFilesToUpload(), elementsPerChunk));
        List<FileSystemResource> fileArchives;

        int pageCount = recentChunks.size();
        int currentPage = 1;

        int currentVersion = Integer.parseInt(requestBody.getVersionBefore());
        int chunkNo = 0;

        while (!recentChunks.isEmpty()) {
            final Map<String, String> checksumsForChunk = new HashMap<>();

            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            List<File> recentChunk = recentChunks.remove(0);

            fileArchives = prepareChunkFiles(
                    recentChunk,
                    FilesHierarchy.getArchiveDirectory(recentChunk.get(0), chunkNo++),
                    requestBody.getChecksums(),
                    requestBody.getHashMethod());
            fileArchives.forEach(file -> {
                map.add("files", file);
                checksumsForChunk.put(
                        file.getFilename(), requestBody.getChecksums().get(file.getFilename()));
            });
            map.add("hash_method", requestBody.getHashMethod().getValue());
            map.add("checksums", checksumsForChunk);
            map.add("version_before", String.valueOf(currentVersion));
            currentVersion = currentVersion == Integer.MAX_VALUE ? 0 : currentVersion;
            map.add("version_after", String.valueOf(++currentVersion));
            map.add("page", (currentPage++) + "/" + pageCount);
            chunks.add(map);
        }

        MultiValueMap<String, Object> firstChunk = new LinkedMultiValueMap<>();
        if (!chunks.isEmpty()) {
            firstChunk = chunks.get(0);
        }

        for (String packageName : requestBody.getFilesToDelete()) {
            firstChunk.add("to_delete", packageName);
        }
        return chunks;
    }

    /**
     * Prepares and archives a list of files into separate chunks based on their parent directories,
     * creating a tar archive for each package. Since the application does not have direct access to the list of package names,
     * it groups files by their parent directory name, which, in a Python repository, corresponds to the package name.
     * This is crucial because each package directory contains an index.html file, and grouping files by package ensures
     * the index.html is correctly associated with its corresponding package.
     * <p>
     * The method creates a tar archive for each package, storing them in the specified {@code archiveDir}.
     * For each archive, it calculates a checksum using the defined hash method and saves it in a checksum map.
     * It returns a list of {@code FileSystemResource} objects representing the created tar archives.
     * </p>
     *
     * <p><b>Process:</b>
     * <ol>
     *   <li>Files with the same parent directory (the same packagename) are grouped together.</li>
     *   <li>Once a group of files is ready, it is archived into a tar file using {@code tarPackages}.</li>
     *   <li>The checksum of the tar file is calculated and stored.</li>
     *   <li>If any files remain at the end, they are archived and processed in the same way.</li>
     * </ol>
     * </p>
     *
     * @param files the list of files to be archived and processed
     * @param archiveDir the directory where the tar archives should be stored
     * @return a list of {@code FileSystemResource} objects representing the archived file chunks
     * @throws IOException if an I/O error occurs during the file archiving process
     * @throws CheckSumCalculationException if an error occurs during checksum calculation
     */
    private List<FileSystemResource> prepareChunkFiles(
            List<File> files, Path archiveDir, Map<String, String> checksums, HashMethod hashMethod)
            throws IOException, CheckSumCalculationException {
        List<FileSystemResource> fileArchives = new ArrayList<>();
        List<File> filesToArchive = new ArrayList<>();
        String packageName = "";

        for (File file : files) {
            if (file.getParent().equals(packageName)) {
                filesToArchive.add(file);
                continue;
            }
            if (!filesToArchive.isEmpty()) {
                final FileSystemResource fsResource = archiver.tarPackages(filesToArchive, archiveDir);
                fileArchives.add(fsResource);
                HashCalculator hashCalculator = new HashCalculator(
                        hashMethod, Objects.requireNonNull(fsResource).getFile());
                checksums.put(fsResource.getFilename(), hashCalculator.calculateHash());
            }
            filesToArchive = new ArrayList<>();
            filesToArchive.add(file);
            packageName = file.getParent();
        }
        if (!filesToArchive.isEmpty()) {
            final FileSystemResource fsResource = archiver.tarPackages(filesToArchive, archiveDir);
            fileArchives.add(fsResource);
            HashCalculator hashCalculator = new HashCalculator(
                    hashMethod, Objects.requireNonNull(fsResource).getFile());
            checksums.put(fsResource.getFilename(), hashCalculator.calculateHash());
        }
        return fileArchives;
    }
}

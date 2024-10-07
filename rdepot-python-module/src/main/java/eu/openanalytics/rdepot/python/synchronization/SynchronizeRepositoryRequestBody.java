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

import eu.openanalytics.rdepot.base.storage.exceptions.CheckSumCalculationException;
import eu.openanalytics.rdepot.python.entities.enums.HashMethod;
import eu.openanalytics.rdepot.python.mediator.hash.HashCalculator;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Getter
@Setter
@AllArgsConstructor
public class SynchronizeRepositoryRequestBody {
    private List<File> filesToUpload;
    private List<String> filesToDelete;
    private String versionBefore;
    private String versionAfter;
    private String repository;
    private String page;
    private String id;
    private final HashMethod hashMethod;
    private final Map<String, String> checksums;

    private final String separator = FileSystems.getDefault().getSeparator();

    public SynchronizeRepositoryRequestBody(
            List<File> filesToUpload,
            List<String> filesToDelete,
            String versionBefore,
            String repositoryName,
            HashMethod hashMethod,
            Map<String, String> checksums) {
        this.filesToUpload = filesToUpload;
        this.filesToDelete = filesToDelete;
        this.versionBefore = versionBefore;
        this.repository = repositoryName;
        this.hashMethod = hashMethod;
        this.checksums = checksums;
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
    public List<MultiValueMap<String, Object>> toChunks(int elementsPerChunk)
            throws IOException, CheckSumCalculationException {
        List<MultiValueMap<String, Object>> chunks = new ArrayList<>();
        List<List<File>> recentChunks = new ArrayList<>(ListUtils.partition(filesToUpload, elementsPerChunk));
        List<FileSystemResource> fileArchives;

        int pageCount = recentChunks.size();
        int currentPage = 1;

        int currentVersion = Integer.parseInt(this.versionBefore);
        int chunkNo = 0;

        while (!recentChunks.isEmpty()) {
            final Map<String, String> checksumsForChunk = new HashMap<>();

            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            List<File> recentChunk = recentChunks.remove(0);

            fileArchives =
                    prepareChunkFiles(recentChunk, FilesHierarchy.getArchiveDirectory(recentChunk.get(0), chunkNo++));
            fileArchives.forEach(file -> {
                map.add("files", file);
                checksumsForChunk.put(file.getFilename(), checksums.get(file.getFilename()));
            });
            map.add("hash_method", hashMethod.getValue());
            map.add("checksums", checksumsForChunk);
            map.add("version_before", String.valueOf(currentVersion));
            currentVersion = currentVersion == Integer.MAX_VALUE ? 0 : currentVersion;
            map.add("version_after", String.valueOf(++currentVersion));
            map.add("page", (currentPage++) + "/" + pageCount);
            chunks.add(map);
        }

        MultiValueMap<String, Object> firstChunk = new LinkedMultiValueMap<>();
        if (chunks.size() > 0) {
            firstChunk = chunks.get(0);
        }

        for (String packageName : filesToDelete) {
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
    private List<FileSystemResource> prepareChunkFiles(List<File> files, String archiveDir)
            throws IOException, CheckSumCalculationException {
        List<FileSystemResource> fileArchives = new ArrayList<FileSystemResource>();
        List<File> filesToArchive = new ArrayList<File>();
        String packageName = "";

        for (File file : files) {
            if (file.getParent().equals(packageName)) {
                filesToArchive.add(file);
            } else {
                if (filesToArchive.size() > 0) {
                    final FileSystemResource fsResource = tarPackages(filesToArchive, archiveDir);
                    fileArchives.add(fsResource);
                    HashCalculator hashCalculator = new HashCalculator(
                            hashMethod, Objects.requireNonNull(fsResource).getFile());
                    checksums.put(fsResource.getFilename(), hashCalculator.calculateHash());
                }
                filesToArchive = new ArrayList<File>();
                filesToArchive.add(file);
                packageName = file.getParent();
            }
        }
        if (filesToArchive.size() > 0) {
            final FileSystemResource fsResource = tarPackages(filesToArchive, archiveDir);
            fileArchives.add(fsResource);
            HashCalculator hashCalculator = new HashCalculator(
                    hashMethod, Objects.requireNonNull(fsResource).getFile());
            checksums.put(fsResource.getFilename(), hashCalculator.calculateHash());
        }
        return fileArchives;
    }

    /**
     * Archives the given list of files into a compressed tar.gz archive and stores it in the specified directory.
     * <p>
     * This method creates a compressed .tar.gz archive from the provided list of {@code files} and saves it in the specified {@code archiveDir}.
     * The archive is named after the package, which is determined by the parent directory of the first file. However, if the file is part of a repository directory,
     * the archive will be named "index.tar.gz" (as both packages and repositories contain an index.html file).
     * </p>
     * <p>
     * The process involves:
     * <ol>
     *   <li>Creating a tar archive entry for each file in the list.</li>
     *   <li>Compressing the tar archive using GZIP compression.</li>
     *   <li>Writing the tar.gz file to the specified archive directory.</li>
     * </ol>
     * </p>
     *
     * @param files the list of files to be archived
     * @param archiveDir the directory where the tar.gz archive will be stored
     * @return a {@code FileSystemResource} representing the created tar.gz archive, or {@code null} if the file list is empty
     * @throws IOException if an I/O error occurs during the archiving process
     */
    private FileSystemResource tarPackages(List<File> files, String archiveDir) throws IOException {
        // TODO: #33225 Remove logic from here

        if (files.size() > 0) {
            String archiveName = files.get(0).getParentFile().getName();
            if (FilesHierarchy.isRepositoryDir(files.get(0))) {
                archiveName = "index";
            }
            String archivePath = archiveDir + separator + archiveName + ".tar.gz";
            OutputStream packagesArchive = Files.newOutputStream(Paths.get(archivePath));
            BufferedOutputStream buffOut = new BufferedOutputStream(packagesArchive);
            GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(buffOut);
            TarArchiveOutputStream tOut = new TarArchiveOutputStream(gzOut);
            for (File file : files) {
                TarArchiveEntry tarEntry = new TarArchiveEntry(file, file.getName());
                tOut.putArchiveEntry(tarEntry);
                Files.copy(Paths.get(file.getAbsolutePath()), tOut);
                tOut.closeArchiveEntry();
            }
            tOut.finish();
            gzOut.close();
            buffOut.close();
            packagesArchive.close();
            return new FileSystemResource(archivePath);
        }
        return null;
    }
}

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

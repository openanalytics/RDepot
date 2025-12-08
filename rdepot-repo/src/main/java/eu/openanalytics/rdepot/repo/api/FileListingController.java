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
package eu.openanalytics.rdepot.repo.api;

import eu.openanalytics.rdepot.repo.exception.GetRepositoryVersionException;
import eu.openanalytics.rdepot.repo.hash.HashCalculator;
import eu.openanalytics.rdepot.repo.hash.model.HashMethod;
import eu.openanalytics.rdepot.repo.r.storage.CranStorageService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@Controller
@AllArgsConstructor
public class FileListingController {
    private final CranStorageService storageService;
    private final HashCalculator hashCalculator;

    @GetMapping("/{repository}/")
    public ResponseEntity<List<String>> recentUploads(@PathVariable("repository") String repository) {
        ArrayList<String> uploads = new ArrayList<>();
        try {
            uploads.add(storageService.getRepositoryVersion(repository));
        } catch (GetRepositoryVersionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        try {
            List<Path> files = storageService.getRecentPackagesFromRepository(repository);
            Map<String, List<Path>> binaryFiles = storageService.getRecentBinaryPackagesFromRepository(repository);

            for (Path file : files) {
                uploads.add(fileAndHash(file, repository));
            }

            for (String path : binaryFiles.keySet()) {
                for (Path file : binaryFiles.get(path)) {
                    uploads.add(fileAndHash(file, repository));
                }
            }

        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok(uploads);
    }

    private String fileAndHash(Path file, String repository) throws IOException {
        return StringUtils.substringAfter(file.toString(), repository + "/")
                + "="
                + hashCalculator
                        .calculate(Files.newInputStream(file), HashMethod.MD5)
                        .get();
    }

    @GetMapping("/{repository}/archive/")
    public ResponseEntity<List<String>> archiveUploads(@PathVariable("repository") String repository) {
        try {
            List<String> uploads = new ArrayList<>();
            Map<String, List<Path>> paths = storageService.getArchiveFromRepository(repository);
            for (List<Path> files : paths.values()) {
                for (Path file : files) {
                    uploads.add(fileAndHash(file, repository));
                }
            }
            return ResponseEntity.ok(uploads);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{repository}/{source}/{packagesFile}")
    public void downloadPackagesFile(
            @PathVariable("repository") String repository,
            @PathVariable("packagesFile") String packagesFile,
            @PathVariable("source") String source,
            HttpServletResponse response) {

        final boolean archive = Objects.equals(source, "archive");
        final Map<String, File> packagesFiles = storageService.getPackagesFiles(repository, archive);

        final File returnedFile = packagesFiles.get(packagesFile);
        if (returnedFile != null) {
            try {
                final InputStream is = new FileInputStream(returnedFile);
                IOUtils.copy(is, response.getOutputStream());
                response.flushBuffer();
            } catch (IOException e) {
                log.error("Could not write file to output stream! Filename: {}", returnedFile.getName(), e);
            }
        } else {
            try {
                response.sendError(404);
            } catch (IOException e) {
                log.error("Could not send error response!", e);
            }
        }
    }
}

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
package eu.openanalytics.rdepot.repo.api;

import com.google.gson.Gson;
import eu.openanalytics.rdepot.repo.exception.GetRepositoryVersionException;
import eu.openanalytics.rdepot.repo.r.storage.CranStorageService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping("/{repository}/")
    public ResponseEntity<List<String>> recentUploads(@PathVariable("repository") String repository) {
        ArrayList<String> uploads = new ArrayList<String>();
        try {
            uploads.add(storageService.getRepositoryVersion(repository));
        } catch (GetRepositoryVersionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        List<File> files = storageService.getRecentPackagesFromRepository(repository);
        files.forEach(file -> uploads.add(file.getName()));

        return ResponseEntity.ok(uploads);
    }

    @GetMapping("/{repository}/archive/")
    public ResponseEntity<List<String>> archiveUploads(@PathVariable("repository") String repository) {

        Map<String, List<File>> files = storageService.getArchiveFromRepository(repository);
        ArrayList<String> uploads = new ArrayList<>();

        for (Entry<String, List<File>> entry : files.entrySet()) {
            entry.getValue().forEach(file -> uploads.add(file.getName()));
        }

        return ResponseEntity.ok(uploads);
    }

    @GetMapping("/{repository}/checksum")
    public ResponseEntity<String> checksum(@PathVariable("repository") String repository) {
        List<String> recentUploads = new ArrayList<String>();
        List<String> archiveUploads = new ArrayList<String>();

        List<File> recentFiles = storageService.getRecentPackagesFromRepository(repository);
        recentFiles.forEach(file -> recentUploads.add(file.getName()));

        Map<String, List<File>> archiveFiles = storageService.getArchiveFromRepository(repository);
        for (Entry<String, List<File>> entry : archiveFiles.entrySet()) {
            entry.getValue().forEach(file -> archiveUploads.add(file.getName()));
        }

        Gson gson = new Gson();
        String recentJson = gson.toJson(recentUploads);
        String archiveJson = gson.toJson(archiveUploads);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        String recentChecksum = DatatypeConverter.printHexBinary(digest.digest(recentJson.getBytes()));
        String archiveChecksum = DatatypeConverter.printHexBinary(digest.digest(archiveJson.getBytes()));

        return ResponseEntity.ok().body(recentChecksum + archiveChecksum);
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
                log.error("Could not write file to output stream! Filename: " + returnedFile.getName(), e);
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

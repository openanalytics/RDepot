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
package eu.openanalytics.rdepot.repo.python.api;

import eu.openanalytics.rdepot.repo.api.FileUploadController;
import eu.openanalytics.rdepot.repo.exception.GetRepositoryVersionException;
import eu.openanalytics.rdepot.repo.hash.model.HashMethod;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryResponseBody;
import eu.openanalytics.rdepot.repo.python.chunks.coordination.PythonUploadRequestChunkCoordinator;
import eu.openanalytics.rdepot.repo.python.model.SynchronizePythonRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.python.storage.PythonFileSystemStorageService;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping(value = "/python")
public class PythonFileUploadController extends FileUploadController<SynchronizePythonRepositoryRequestBody> {

    private final PythonFileSystemStorageService storageService;

    public PythonFileUploadController(
            PythonUploadRequestChunkCoordinator requestCoordinator, PythonFileSystemStorageService storageService) {
        super(requestCoordinator);
        this.storageService = storageService;
    }

    @PostMapping("/{repository:.+}")
    public ResponseEntity<SynchronizeRepositoryResponseBody> handleSynchronizeRequest(
            @PathVariable("repository") String repository,
            @RequestPart(value = "files", required = false) MultipartFile[] filesToUpload,
            @RequestParam(value = "to_delete", required = false) String[] filesToDelete,
            @RequestParam("version_before") String versionBefore,
            @RequestParam("version_after") String versionAfter,
            @RequestParam("page") String page,
            @RequestParam("id") String id,
            @RequestPart("checksums") Map<String, String> checksums,
            @RequestParam("hash_method") HashMethod hashMethod) {
        SynchronizePythonRepositoryRequestBody requestBody = new SynchronizePythonRepositoryRequestBody(
                id, filesToUpload, filesToDelete, versionBefore, versionAfter, page, repository, checksums, hashMethod);
        return handleSynchronizeRequest(requestBody);
    }

    @GetMapping("/{repository}/")
    public ResponseEntity<List<String>> recentUploads(@PathVariable("repository") String repository) {
        ArrayList<String> uploads = new ArrayList<String>();
        try {
            uploads.add(storageService.getRepositoryVersion(repository));
        } catch (GetRepositoryVersionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        try {
            List<Path> files = storageService.getRecentPackagesFromRepository(repository);
            files.forEach(file -> uploads.add(file.getParent().getFileName().toString() + "/"
                    + file.getFileName().toString()));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok(uploads);
    }
}

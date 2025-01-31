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
package eu.openanalytics.rdepot.repo.r.api;

import eu.openanalytics.rdepot.repo.api.FileUploadController;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryResponseBody;
import eu.openanalytics.rdepot.repo.r.model.SynchronizeCranRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.r.upload.CranUploadRequestChunkCoordinator;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/r")
public class CranFileUploadController extends FileUploadController<SynchronizeCranRepositoryRequestBody> {

    public CranFileUploadController(CranUploadRequestChunkCoordinator uploadRequestCoordinator) {
        super(uploadRequestCoordinator);
    }

    @PostMapping("/{repository:.+}")
    public ResponseEntity<SynchronizeRepositoryResponseBody> handleSynchronizeRequest(
            @PathVariable("repository") String repository,
            @RequestPart(value = "files", required = false) MultipartFile[] filesToUpload,
            @RequestParam(value = "to_delete", required = false) String[] filesToDelete,
            @RequestPart(value = "files_archive", required = false) MultipartFile[] filesToUploadToArchive,
            @RequestParam(value = "to_delete_archive", required = false) String[] filesToDeleteFromArchive,
            @RequestParam("version_before") String versionBefore,
            @RequestParam("version_after") String versionAfter,
            @RequestParam("page") String page,
            @RequestParam("id") String id,
            @RequestPart("paths") Map<String, String> pathsToUpload,
            @RequestPart("paths_archive") Map<String, String> pathsToUploadToArchive,
            @RequestPart("to_delete_paths") Map<String, String> pathsToDelete,
            @RequestPart("to_delete_paths_archive") Map<String, String> pathsToDeleteFromArchive,
            @RequestPart("checksums") Map<String, String> checksums) {

        SynchronizeCranRepositoryRequestBody requestBody = new SynchronizeCranRepositoryRequestBody(
                id,
                filesToUpload,
                filesToUploadToArchive,
                filesToDelete,
                filesToDeleteFromArchive,
                versionBefore,
                versionAfter,
                page,
                repository,
                pathsToUpload,
                pathsToUploadToArchive,
                pathsToDelete,
                pathsToDeleteFromArchive,
                checksums);

        return handleSynchronizeRequest(requestBody);
    }
}

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
package eu.openanalytics.rdepot.repo.r;

import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.repo.FileUploadController;
import eu.openanalytics.rdepot.repo.collection.QueueMap;
import eu.openanalytics.rdepot.repo.exception.StorageFileNotFoundException;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryResponseBody;
import eu.openanalytics.rdepot.repo.r.model.CranRepositoryBackup;
import eu.openanalytics.rdepot.repo.r.model.SynchronizeCranRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.r.storage.CranStorageService;

@Controller
@RequestMapping(value = "/r")
public class CranFileUploadController extends FileUploadController<
	SynchronizeCranRepositoryRequestBody, 
	CranRepositoryBackup> {
	
	public CranFileUploadController(CranStorageService storageService, 
    		@Qualifier("responseCranMap")
    		QueueMap<String, SynchronizeRepositoryResponseBody> responseMap, 
    		@Qualifier("requestCranQueue")
    		BlockingQueue<SynchronizeCranRepositoryRequestBody> requestQueue) {
        super(requestQueue, responseMap, storageService);
    }

    @PostMapping("/{repository:.+}")
    @ResponseBody
    public ResponseEntity<SynchronizeRepositoryResponseBody> handleSynchronizeRequest(
    		@PathVariable("repository") String repository,
    		@RequestParam(value = "files", required = false) MultipartFile[] filesToUpload, 
    		@RequestParam(value = "to_delete", required = false) String[] filesToDelete,
    		@RequestParam(value = "files_archive", required = false) MultipartFile[] filesToUploadToArchive,
    		@RequestParam(value = "to_delete_archive", required = false) String[] filesToDeleteFromArchive,
    		@RequestParam("version_before") String versionBefore,
    		@RequestParam("version_after") String versionAfter,
    		@RequestParam("page") String page,
    		@RequestParam("id") String id) {

		SynchronizeCranRepositoryRequestBody requestBody = new SynchronizeCranRepositoryRequestBody(id,
				filesToUpload, filesToUploadToArchive, filesToDelete, 
				filesToDeleteFromArchive, versionBefore, versionAfter, page, 
				repository);
    		
		return handleSynchronizeRequest(requestBody);
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
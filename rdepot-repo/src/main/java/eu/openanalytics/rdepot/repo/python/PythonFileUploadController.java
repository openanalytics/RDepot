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
package eu.openanalytics.rdepot.repo.python;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import eu.openanalytics.rdepot.repo.FileUploadController;
import eu.openanalytics.rdepot.repo.collection.QueueMap;
import eu.openanalytics.rdepot.repo.exception.GetRepositoryVersionException;
import eu.openanalytics.rdepot.repo.exception.StorageFileNotFoundException;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryResponseBody;
import eu.openanalytics.rdepot.repo.python.model.PythonRepositoryBackup;
import eu.openanalytics.rdepot.repo.python.model.SynchronizePythonRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.storage.StorageService;

@Controller
@RequestMapping(value = "/python")
public class PythonFileUploadController extends FileUploadController<
	SynchronizePythonRepositoryRequestBody, 
	PythonRepositoryBackup> {
	
    public PythonFileUploadController(StorageService<SynchronizePythonRepositoryRequestBody> storageService, 
    		@Qualifier("responsePythonMap")
    		QueueMap<String, SynchronizeRepositoryResponseBody> responseMap,
    		@Qualifier("requestPythonQueue")
    		BlockingQueue<SynchronizePythonRepositoryRequestBody> requestQueue) {
        super(requestQueue, responseMap, storageService);
    }
    
    @PostMapping("/{repository:.+}")
    @ResponseBody
    public ResponseEntity<SynchronizeRepositoryResponseBody> handleSynchronizeRequest(
    		@PathVariable("repository") String repository,
    		@RequestParam(value = "files", required = false) MultipartFile[] filesToUpload, 
    		@RequestParam(value = "to_delete", required = false) String[] filesToDelete,
    		@RequestParam("version_before") String versionBefore,
    		@RequestParam("version_after") String versionAfter,
    		@RequestParam("page") String page,
    		@RequestParam("id") String id) 
    				throws JsonMappingException, JsonProcessingException {
    	SynchronizePythonRepositoryRequestBody requestBody =
    			new SynchronizePythonRepositoryRequestBody(id,  filesToUpload, filesToDelete, versionBefore, versionAfter, page, repository);
		return handleSynchronizeRequest(requestBody);
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/{repository}/")
	public ResponseEntity<List<String>> recentUploads(@PathVariable("repository") String repository) {
		ArrayList<String> uploads = new ArrayList<String>();
		try {
			uploads.add(storageService.getRepositoryVersion(repository));
		} catch (GetRepositoryVersionException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
		
		List<File> files = storageService.getRecentPackagesFromRepository(repository);
		files.forEach(file -> uploads.add(file.getParentFile().getName() + "/"  + file.getName()));
		
		return ResponseEntity.ok(uploads);
	}

}

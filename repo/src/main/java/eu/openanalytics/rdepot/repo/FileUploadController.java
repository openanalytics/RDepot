/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.repo;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.repo.collection.QueueMap;
import eu.openanalytics.rdepot.repo.exception.InitTransactionException;
import eu.openanalytics.rdepot.repo.exception.InvalidRequestPageNumberException;
import eu.openanalytics.rdepot.repo.exception.NoSuchTransactionException;
import eu.openanalytics.rdepot.repo.exception.StorageFileNotFoundException;
import eu.openanalytics.rdepot.repo.messaging.SharedMessageCodes;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryResponseBody;
import eu.openanalytics.rdepot.repo.storage.StorageService;

@Controller
public class FileUploadController {

	Logger logger = LoggerFactory.getLogger(FileUploadController.class);
	
    private final StorageService storageService;

    @Autowired
    BlockingQueue<SynchronizeRepositoryRequestBody> requestQueue;
    
    @Autowired
    QueueMap<String, SynchronizeRepositoryResponseBody> responseMap;
    
    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // This code protects Spring Core from a "Remote Code Execution" attack (dubbed "Spring4Shell").
        // By applying this mitigation, you prevent the "Class Loader Manipulation" attack vector from firing.
        // For more details, see this post: https://www.lunasec.io/docs/blog/spring-rce-vulnerabilities/
        String[] blackList = {"class.*","Class.*","*.class.*",".*Class.*"};
        binder.setDisallowedFields(blackList);
    }
    
    @PostMapping("/{repository:.+}")
    @ResponseBody
    public ResponseEntity<SynchronizeRepositoryResponseBody> handleSynchronizeRequest(
    		@PathVariable String repository,
    		@RequestParam(value = "files", required = false) MultipartFile[] filesToUpload, 
    		@RequestParam(value = "to_delete", required = false) String[] filesToDelete,
    		@RequestParam(value = "files_archive", required = false) MultipartFile[] filesToUploadToArchive,
    		@RequestParam(value = "to_delete_archive", required = false) String[] filesToDeleteFromArchive,
    		@RequestParam("version_before") String versionBefore,
    		@RequestParam("version_after") String versionAfter,
    		@RequestParam("page") String page,
    		@RequestParam("id") String id) {
    	
    	try {
    		logger.info("Received request.");

    		SynchronizeRepositoryRequestBody requestBody = new SynchronizeRepositoryRequestBody(id,
    				filesToUpload, filesToUploadToArchive, filesToDelete, 
    				filesToDeleteFromArchive, versionBefore, versionAfter, page, 
    				repository);
    		
    	
			if(requestBody.isFirstChunk()) {
    			String generatedId = storageService.initTransaction(repository, versionBefore);
    			requestBody.setId(generatedId);
    		} else {
    			if(!responseMap.containsKey(id))
    				throw new NoSuchTransactionException(repository, id);
    		}
    		
    		requestQueue.add(requestBody);
    		
    		SynchronizeRepositoryResponseBody response = responseMap.getLastItem(requestBody.getId());
    		if(requestBody.isLastChunk())
    			responseMap.remove(requestBody.getId());
    		
    		if(Objects.equals(response.getMessage(), SharedMessageCodes.RESPONSE_OK)) {
    			return ResponseEntity.ok(response);
    		} else {
    			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    		}
    		
    	} catch(InvalidRequestPageNumberException | InterruptedException | 
    			InitTransactionException | NoSuchTransactionException e) {
    		logger.error(e.getClass().getCanonicalName() + ": " + e.getMessage());
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
    				new SynchronizeRepositoryResponseBody(id, SharedMessageCodes.RESPONSE_ERROR));
    	} finally {
    		if(id != null) {
    			if(!Objects.equals(id, "") && responseMap.containsKey(id))
    				responseMap.remove(id);
    		}
    	}
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}

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
package eu.openanalytics.rdepot.repo;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import eu.openanalytics.rdepot.repo.collection.QueueMap;
import eu.openanalytics.rdepot.repo.exception.InitTransactionException;
import eu.openanalytics.rdepot.repo.exception.InvalidRequestPageNumberException;
import eu.openanalytics.rdepot.repo.exception.NoSuchTransactionException;
import eu.openanalytics.rdepot.repo.messaging.SharedMessageCodes;
import eu.openanalytics.rdepot.repo.model.RepositoryBackup;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryResponseBody;
import eu.openanalytics.rdepot.repo.storage.StorageService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class FileUploadController<
	R extends SynchronizeRepositoryRequestBody, 
	B extends RepositoryBackup> {
	
    protected final BlockingQueue<R> requestQueue;
    protected final QueueMap<String, SynchronizeRepositoryResponseBody> responseMap;
    protected final StorageService<R> storageService;
	
    public FileUploadController(BlockingQueue<R> requestQueue, 
    		QueueMap<String, SynchronizeRepositoryResponseBody> responseMap,
    		StorageService<R> storageService) {
    	this.requestQueue = requestQueue;
    	this.responseMap = responseMap;
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
    
	protected ResponseEntity<SynchronizeRepositoryResponseBody> handleSynchronizeRequest(R requestBody) {
		final String repository = requestBody.getRepository();
		final String versionBefore = requestBody.getVersionBefore();
		final String id = requestBody.getId();
		
		try {
    		log.info("Received request.");
    	
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
    		log.error(e.getClass().getCanonicalName() + ": " + e.getMessage());
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
    				new SynchronizeRepositoryResponseBody(id, SharedMessageCodes.RESPONSE_ERROR));
    	}
	}
}
/**
 * R Depot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.repo.listener;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.repo.collection.QueueMap;
import eu.openanalytics.rdepot.repo.exception.ProcessRequestException;
import eu.openanalytics.rdepot.repo.messaging.SharedMessageCodes;
import eu.openanalytics.rdepot.repo.model.RepositoryBackup;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryResponseBody;
import eu.openanalytics.rdepot.repo.storage.StorageService;

@Component
public class RequestQueueListener implements DisposableBean, Runnable {

	Thread thread;
	
	Logger logger = LoggerFactory.getLogger(RequestQueueListener.class);
	
	@Autowired
	StorageService storageService;
	
	@Autowired
	BlockingQueue<SynchronizeRepositoryRequestBody> requestQueue;
	
	@Autowired
	HashMap<String, RepositoryBackup> backupMap;
	
	@Autowired
	QueueMap<String, SynchronizeRepositoryRequestBody> requestMap;
	
	@Autowired
	QueueMap<String, SynchronizeRepositoryResponseBody> responseMap;
	
	public RequestQueueListener() {
		thread = new Thread(this);
		thread.start();
	}
	
	private void processRequest(SynchronizeRepositoryRequestBody request) throws InterruptedException {
		logger.debug("Received chunk from the manager. Processing started...");

		try {
			requestMap.put(request.getId(), request);
			storageService.processLastRequest();
			
			responseMap.put(request.getId(), 
					new SynchronizeRepositoryResponseBody(
							request.getId(), SharedMessageCodes.RESPONSE_OK));
		} catch(ProcessRequestException e) {
			logger.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
			responseMap.put(request.getId(), 
					new SynchronizeRepositoryResponseBody(
							request.getId(), SharedMessageCodes.RESPONSE_ERROR));
		}
	}
	
	@Override
	public void run() {
		try {
			while(requestQueue == null || requestMap == null || storageService == null)
				Thread.sleep(1000);
			
			Supplier<SynchronizeRepositoryRequestBody> requestSupplier = new Supplier<SynchronizeRepositoryRequestBody>() {

				@Override
				public SynchronizeRepositoryRequestBody get() {
					try {
						return requestQueue.take();
					} catch (InterruptedException e) {
						logger.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
					}
					return null;
				}
			
			};
			
			Stream.generate(requestSupplier).forEach(r -> {
				try {
					processRequest(r);
				} catch (InterruptedException e) {
					logger.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);

				}
			});
		} catch(InterruptedException e) {
			logger.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
		}
	}

	@Override
	public void destroy() throws Exception {
		//Nothing to do
	}

}

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
package eu.openanalytics.rdepot.repo.listener;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

import eu.openanalytics.rdepot.repo.collection.QueueMap;
import eu.openanalytics.rdepot.repo.exception.ProcessRequestException;
import eu.openanalytics.rdepot.repo.messaging.SharedMessageCodes;
import eu.openanalytics.rdepot.repo.model.RepositoryBackup;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryResponseBody;
import eu.openanalytics.rdepot.repo.storage.StorageService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class RequestQueueListener<T extends SynchronizeRepositoryRequestBody, R extends RepositoryBackup> 
	implements DisposableBean, Runnable {

	Thread thread;
	
	@Autowired
	StorageService<T> storageService;
	
	protected final BlockingQueue<T> requestQueue;
	protected final HashMap<String, R> backupMap;
	protected final QueueMap<String, T> requestMap;
	protected final QueueMap<String, SynchronizeRepositoryResponseBody> responseMap;
	
	public RequestQueueListener(BlockingQueue<T> requestQueue, 
			HashMap<String, R> backupMap,
			QueueMap<String, T> requestMap,
			QueueMap<String, SynchronizeRepositoryResponseBody> responseMap) {
		this.requestQueue = requestQueue;
		this.backupMap = backupMap;
		this.requestMap = requestMap;
		this.responseMap = responseMap;
		thread = new Thread(this);
		thread.start();
	}
	
	private void processRequest(T request) throws InterruptedException {
		log.debug("Received chunk from the manager. Processing started...");

		try {
			requestMap.put(request.getId(), request);
			storageService.processLastRequest();
			
			responseMap.put(request.getId(), 
					new SynchronizeRepositoryResponseBody(
							request.getId(), SharedMessageCodes.RESPONSE_OK));
		} catch(ProcessRequestException e) {
			log.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
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
			
			Supplier<T> requestSupplier = new Supplier<T>() {

				@Override
				public T get() {
					try {
						return requestQueue.take();
					} catch (InterruptedException e) {
						log.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
					}
					return null;
				}
			
			};
			
			Stream.generate(requestSupplier).forEach(r -> {
				try {
					processRequest(r);
				} catch (InterruptedException e) {
					log.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
				}
			});
		} catch(InterruptedException e) {
			log.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
		}
	}

	@Override
	public void destroy() throws Exception {
		//Nothing to do
	}

}
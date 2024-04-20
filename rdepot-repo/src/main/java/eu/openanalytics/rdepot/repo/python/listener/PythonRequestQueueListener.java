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
package eu.openanalytics.rdepot.repo.python.listener;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.repo.collection.QueueMap;
import eu.openanalytics.rdepot.repo.listener.RequestQueueListener;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryResponseBody;
import eu.openanalytics.rdepot.repo.python.model.PythonRepositoryBackup;
import eu.openanalytics.rdepot.repo.python.model.SynchronizePythonRepositoryRequestBody;

@Component
public class PythonRequestQueueListener 
	extends RequestQueueListener<SynchronizePythonRepositoryRequestBody, PythonRepositoryBackup> {
	
	public PythonRequestQueueListener(
			@Qualifier("requestPythonQueue")
			BlockingQueue<SynchronizePythonRepositoryRequestBody> requestQueue, 
			@Qualifier("backupPythonMap")
			HashMap<String, PythonRepositoryBackup> backupMap,
			@Qualifier("requestPythonMap")
			QueueMap<String, SynchronizePythonRepositoryRequestBody> requestMap,
			@Qualifier("responsePythonMap")
			QueueMap<String, SynchronizeRepositoryResponseBody> responseMap) {
			super(requestQueue, backupMap, requestMap, responseMap);
	}
}

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
package eu.openanalytics.rdepot.repo.r.config;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.openanalytics.rdepot.repo.collection.QueueMap;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryResponseBody;
import eu.openanalytics.rdepot.repo.r.model.CranRepositoryBackup;
import eu.openanalytics.rdepot.repo.r.model.SynchronizeCranRepositoryRequestBody;

@Configuration
public class CranBeanConfig {
	
	/**
	 * The queue containing incoming synchronize requests.
	 *
	 * @return LinkedBlockingQueue
	 */
	@Bean
	BlockingQueue<SynchronizeCranRepositoryRequestBody> requestCranQueue() {
		return new LinkedBlockingQueue<SynchronizeCranRepositoryRequestBody>();
	}
	
	/**
	 * The map containing requests with their identifiers.
	 * It is used to provide correct order of chunks upload.
	 * @return
	 */
	@Bean
	QueueMap<String, SynchronizeCranRepositoryRequestBody> requestCranMap() {
		return new QueueMap<String, SynchronizeCranRepositoryRequestBody>();
	}
	
	/**
	 * The map containing responses with their identifiers.
	 * It is used to provide correct order of chunks responses;
	 * @return
	 */
	@Bean
	QueueMap<String, SynchronizeRepositoryResponseBody> responseCranMap() {
		return new QueueMap<String, SynchronizeRepositoryResponseBody>();
	}
	
	/**
	 * The map containing backup of updated repository
	 * @return
	 */
	@Bean
	HashMap<String, CranRepositoryBackup> backupCranMap() {
		return new HashMap<String, CranRepositoryBackup>();
	}
}
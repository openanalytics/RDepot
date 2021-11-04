/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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
package eu.openanalytics.rdepot.runnable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.openanalytics.rdepot.model.Mirror;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.service.MirrorService;

public class SynchronizeMirrorTask implements Runnable {

	MirrorService mirrorService;
	Repository repository;
	Mirror mirror;
	Logger logger = LoggerFactory.getLogger(SynchronizeMirrorTask.class);
	
	public SynchronizeMirrorTask(MirrorService mirrorService, Mirror mirror, Repository repository) {
		this.mirrorService = mirrorService;
		this.repository = repository;
		this.mirror = mirror;
	}
	
	@Override
	public void run() {
		logger.info("Synchronizing repository " + repository.getName() 
			+ " with mirror " + mirror.getUri());
		mirrorService.synchronize(repository, mirror);
	}
}

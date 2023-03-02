/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
package eu.openanalytics.rdepot.base.runnable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.openanalytics.rdepot.base.mirroring.Mirror;
import eu.openanalytics.rdepot.base.mirroring.MirrorSynchronizer;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredPackage;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredRepository;

public class SynchronizeMirrorTask<R extends MirroredRepository<P, M>, P extends MirroredPackage, M extends Mirror<P>> 
	implements Runnable {

	MirrorSynchronizer<R, P, M> mirrorService;
	R repository;
	M mirror;
	Logger logger = LoggerFactory.getLogger(SynchronizeMirrorTask.class);
	
	public SynchronizeMirrorTask(MirrorSynchronizer<R, P, M> mirrorService, 
			M mirror, R repository) {
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

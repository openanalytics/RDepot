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
package eu.openanalytics.rdepot.python.strategy.update;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.strategy.update.UpdatePackageStrategy;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.base.time.DateProvider;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import eu.openanalytics.rdepot.python.synchronization.PythonRepositorySynchronizer;

public class PythonPackageUpdateStrategy extends UpdatePackageStrategy<PythonPackage> {

	private final PythonRepositorySynchronizer repositorySynchronizer;
	
	public PythonPackageUpdateStrategy(PythonPackage resource, 
			NewsfeedEventService eventService, 
			PythonPackageService service,
			User requester, 
			PythonPackage updatedPackage, 
			Storage<?, PythonPackage> storage,
			BestMaintainerChooser bestMaintainerChooser,
			PythonRepositorySynchronizer repositorySynchronizer) {
		super(resource, eventService, service, requester, updatedPackage, 
				updatedPackage, storage, bestMaintainerChooser);
		this.repositorySynchronizer = repositorySynchronizer;
	}

	@Override
	protected void publishPackageRepository(PythonPackage packageBag) throws SynchronizeRepositoryException {
		repositorySynchronizer.storeRepositoryOnRemoteServer(packageBag.getRepository(),
        DateProvider.getCurrentDateStamp());
	}

}

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
package eu.openanalytics.rdepot.r.strategy.update;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.strategy.update.UpdatePackageStrategy;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.synchronization.RRepositorySynchronizer;

public class RPackageUpdateStrategy extends UpdatePackageStrategy<RPackage> {

	private final RRepositorySynchronizer repositorySynchronizer;
	
	public RPackageUpdateStrategy(RPackage resource, 
			NewsfeedEventService eventService, 
			RPackageService service,
			User requester, 
			RPackage updatedPackage, 
			Storage<?, RPackage> storage,
			BestMaintainerChooser bestMaintainerChooser,
			RRepositorySynchronizer repositorySynchronizer) {
		super(resource, eventService, service, requester, updatedPackage, 
				updatedPackage, storage, bestMaintainerChooser);
		this.repositorySynchronizer = repositorySynchronizer;
	}

	@Override
	protected void publishPackageRepository(RPackage packageBag) throws SynchronizeRepositoryException {
		repositorySynchronizer.storeRepositoryOnRemoteServer(packageBag.getRepository(), "");
	}

}

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
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.strategy.update.UpdateRepositoryStrategy;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import eu.openanalytics.rdepot.python.services.PythonRepositoryService;
import eu.openanalytics.rdepot.python.synchronization.PythonRepositorySynchronizer;

public class PythonRepositoryUpdateStrategy extends UpdateRepositoryStrategy<PythonRepository> {

	public PythonRepositoryUpdateStrategy(
			PythonRepository resource,
			NewsfeedEventService eventService, 
			PythonRepositoryService service,
			User requester, 
			PythonRepository updatedResource, 
			PythonRepository oldResourceCopy,
			PythonRepositorySynchronizer repositorySynchronizer,
			RepositoryMaintainerService repositoryMaintainerService, 
			PackageMaintainerService packageMaintainerService,
			PythonPackageService packageService) {
		super(resource, 
				eventService,
				service,
				requester, 
				updatedResource, 
				oldResourceCopy,
				repositorySynchronizer,
				repositoryMaintainerService, 
				packageMaintainerService, 
				packageService);
	}
	
}
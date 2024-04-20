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
package eu.openanalytics.rdepot.python.mediator.deletion;

import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.mediator.deletion.PackageDeleter;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import eu.openanalytics.rdepot.python.synchronization.PythonRepositorySynchronizer;
import eu.openanalytics.rdepot.python.utils.PythonPackageRepositoryResolver;

@Component
public class PythonPackageDeleter extends PackageDeleter<PythonPackage, PythonRepository> {
	
	public PythonPackageDeleter(NewsfeedEventService newsfeedEventService,
			PythonPackageService resourceService,
			Storage<?, PythonPackage> storage,
			SubmissionService submissionService,
			PythonRepositorySynchronizer repositorySynchronizer,
			PythonPackageRepositoryResolver pythonPackageRepositoryResolver) {
		super(newsfeedEventService, resourceService, storage, submissionService, 
				repositorySynchronizer, pythonPackageRepositoryResolver);
	}
}

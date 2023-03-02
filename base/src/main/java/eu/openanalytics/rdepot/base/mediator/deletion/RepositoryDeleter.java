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
package eu.openanalytics.rdepot.base.mediator.deletion;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.storage.Storage;

public abstract class RepositoryDeleter<R extends Repository<R, ?>, P extends Package<P, ?>> extends ResourceDeleter<R> {

	private final PackageMaintainerService packageMaintainerService;
	private final RepositoryMaintainerService repositoryMaintainerService;
	private final NewsfeedEventService newsfeedEventService;
	private final SubmissionDeleter submissionDeleter;
	
	public RepositoryDeleter(NewsfeedEventService newsfeedEventService, 
			RepositoryService<R> resourceService, 
			PackageMaintainerService packageMaintainerService,
			RepositoryMaintainerService repositoryMaintainerService,
			PackageService<P> packageService, 
			Storage<R, P> storage, SubmissionDeleter submissionDeleter) {
		super(newsfeedEventService, resourceService);
		this.packageMaintainerService = packageMaintainerService;
		this.repositoryMaintainerService = repositoryMaintainerService;
		this.newsfeedEventService = newsfeedEventService;
		this.submissionDeleter = submissionDeleter;
	}

	@Override
	public void delete(R resource) throws DeleteEntityException {
		for(PackageMaintainer maintainer : resource.getPackageMaintainers()) {			
			newsfeedEventService.deleteRelatedEvents(maintainer);
			packageMaintainerService.delete(maintainer);
		}
		
		for(RepositoryMaintainer maintainer : resource.getRepositoryMaintainers()) {		
			newsfeedEventService.deleteRelatedEvents(maintainer);
			repositoryMaintainerService.delete(maintainer);
		}
		
		for(Package<?,?> packageBag : resource.getPackages()) {
			submissionDeleter.delete(packageBag.getSubmission());
		}
		
		super.delete(resource);
	}
}

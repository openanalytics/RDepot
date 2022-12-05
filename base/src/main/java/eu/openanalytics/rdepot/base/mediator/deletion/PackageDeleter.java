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
package eu.openanalytics.rdepot.base.mediator.deletion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.storage.exceptions.SourceFileDeleteException;

public abstract class PackageDeleter<P extends Package<P, ?>> extends ResourceDeleter<P> {

	private final Storage<?, P> storage;
	private final static Logger logger = LoggerFactory.getLogger(PackageDeleter.class);
	private final SubmissionService submissionService;
	private final SubmissionDeleter submissionDeleter;
	
	public PackageDeleter(NewsfeedEventService newsfeedEventService, 
			PackageService<P> resourceService, 
			Storage<?, P> storage,
			SubmissionService submissionService,
			SubmissionDeleter submissionDeleter) {
		super(newsfeedEventService, resourceService);
		this.storage = storage;
		this.submissionService = submissionService;
		this.submissionDeleter = submissionDeleter;
	}

	@Override
	public void delete(P resource) throws DeleteEntityException {
		try {
			storage.removePackageSource(resource);
			newsfeedEventService.deleteRelatedEvents(resource.getSubmission());
//			newsfeedEventService.deleteRelatedPackageEvents(resource.getId());
			submissionDeleter.delete(resource.getSubmission());
//			resourceService.delete(resource);
		} catch (SourceFileDeleteException e) {
			logger.error(e.getMessage(), e);
			throw new DeleteEntityException();
		}
		
		
	}
	
}

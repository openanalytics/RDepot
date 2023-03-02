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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.storage.exceptions.MovePackageSourceException;
import eu.openanalytics.rdepot.base.storage.exceptions.SourceFileDeleteException;

public abstract class PackageDeleter<P extends Package<P, ?>> extends ResourceDeleter<P> {

	protected final Storage<?, P> storage;
	protected final static Logger logger = LoggerFactory.getLogger(PackageDeleter.class);
	protected final SubmissionService submissionService;
	protected final PackageService<P> packageService;
	
	public PackageDeleter(NewsfeedEventService newsfeedEventService, 
			PackageService<P> resourceService, 
			Storage<?, P> storage,
			SubmissionService submissionService,
			PackageService<P> packageService) {
		super(newsfeedEventService, resourceService);
		this.storage = storage;
		this.submissionService = submissionService;
		this.packageService = packageService;
	}

	@Override
	public void delete(P resource) throws DeleteEntityException {
		String recycledPackageSourcePath = null;
		final String oldPackageSourcePath = resource.getSource();
		
		try {
			recycledPackageSourcePath = storage.moveToTrashDirectory(resource);
			resource.setSource(recycledPackageSourcePath);
			
			newsfeedEventService.deleteRelatedEvents(resource.getSubmission());
			newsfeedEventService.deleteRelatedEvents(resource);
			submissionService.delete(resource.getSubmission());
			
			storage.removePackageSource(recycledPackageSourcePath);
		} catch (MovePackageSourceException e) {
			logger.error(e.getMessage(), e);
			throw new DeleteEntityException();
		} catch (SourceFileDeleteException e) {
			logger.error(e.getMessage(), e);
		} catch (DataAccessException dae) {
			logger.error(dae.getMessage(), dae);
			try {
				resource.setSource(storage.moveSource(resource, oldPackageSourcePath));
			} catch (MovePackageSourceException mpse) {
				logger.error("Could not restore package source after failed delete!");
				logger.error(mpse.getMessage(), mpse);
			}
			throw new DeleteEntityException();
		}
	}
	
	public void deleteForSubmission(Submission submission) throws DeleteEntityException {
		delete(packageService.findById(submission.getPackage().getId()).orElseThrow(DeleteEntityException::new));
	}
}

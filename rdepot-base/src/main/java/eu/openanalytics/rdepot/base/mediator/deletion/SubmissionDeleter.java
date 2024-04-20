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
package eu.openanalytics.rdepot.base.mediator.deletion;

import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.Service;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;

/**
 * Permanently deletes {@link Submission Submissions} and related
 * {@link eu.openanalytics.rdepot.base.entities.Package Packages}.
 */
public abstract class SubmissionDeleter extends ResourceDeleter<Submission> {

	protected final PackageDeleter<?,?> packageDeleter;
	
	public SubmissionDeleter(NewsfeedEventService newsfeedEventService,
			Service<Submission> resourceService,
			PackageDeleter<?,?> packageDeleter) {
		super(newsfeedEventService, resourceService);
		this.packageDeleter = packageDeleter;
	}

	@Override
	public void delete(Submission resource) throws DeleteEntityException {
		packageDeleter.deleteForSubmission(resource);
	}
}

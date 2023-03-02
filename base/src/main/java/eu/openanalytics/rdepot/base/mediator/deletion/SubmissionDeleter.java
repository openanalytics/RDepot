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
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.Service;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.technology.ServiceResolver;
import eu.openanalytics.rdepot.base.technology.Technology;
import eu.openanalytics.rdepot.base.technology.TechnologyNotSupported;

@Component
public class SubmissionDeleter extends ResourceDeleter<Submission> {

	private final ServiceResolver serviceResolver;
	private final static Logger logger = LoggerFactory.getLogger(SubmissionDeleter.class);
	
	
	public SubmissionDeleter(NewsfeedEventService newsfeedEventService,
			Service<Submission> resourceService,
			ServiceResolver serviceResolver) {
		super(newsfeedEventService, resourceService);
		this.serviceResolver = serviceResolver;
	}

	@Override
	public void delete(Submission resource) throws DeleteEntityException {
		Technology technology = resource.getPackage().getTechnology();
		
		try {
			serviceResolver.packageDeleter(technology).deleteForSubmission(resource);
		} catch(TechnologyNotSupported e) {
			logger.error(e.getMessage(), e);
			throw new DeleteEntityException();
		}
	}
}

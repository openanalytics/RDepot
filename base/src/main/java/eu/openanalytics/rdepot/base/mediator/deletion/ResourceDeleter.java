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

import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.Service;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;

/**
 * Mediator that deletes resource of a specific type, 
 * resources related to it and all related events.
 * @param <T> Resource type
 */
public abstract class ResourceDeleter<T extends Resource> {
	
	protected final NewsfeedEventService newsfeedEventService;
	protected final Service<T> resourceService;
	
	public ResourceDeleter(NewsfeedEventService newsfeedEventService,
			Service<T> resourceService) {
		this.newsfeedEventService = newsfeedEventService;
		this.resourceService = resourceService;
	}
	
	/**
	 * Deletes resource together with its related events.
	 * @param resource
	 */
	@Transactional
	public void delete(T resource) throws DeleteEntityException {
		newsfeedEventService.deleteRelatedEvents(resource);
		resourceService.delete(resource);
	}
}

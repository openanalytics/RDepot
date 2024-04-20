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
package eu.openanalytics.rdepot.base.strategy.create;

import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.Service;
import eu.openanalytics.rdepot.base.service.exceptions.CreateEntityException;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.FatalStrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyReversionFailure;

/**
 * Strategy template for creating resources.
 * @param <T> Resource entity
 */	
public abstract class CreateStrategy<T extends Resource> extends Strategy<T> {

	protected CreateStrategy(T resource, Service<T> service, User requester, 
			NewsfeedEventService newsfeedEventService) {
		super(resource, service, requester, newsfeedEventService);
	}
	
	@Override
	protected T actualStrategy() throws StrategyFailure {
		try {
			return service.create(resource);
		} catch (CreateEntityException e) {
			logger.error(e.getMessage(), e);
			throw new FatalStrategyFailure(e);
		}
	}
	
	@Override
	public void revertChanges() throws StrategyReversionFailure {
		try {
			service.delete(processedResource);
		} catch (DeleteEntityException e) {
			logger.error(e.getMessage(), e);
			throw new StrategyReversionFailure(e);
		}
	}
}

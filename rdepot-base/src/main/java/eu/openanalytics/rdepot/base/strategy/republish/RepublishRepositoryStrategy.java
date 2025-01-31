/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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
package eu.openanalytics.rdepot.base.strategy.republish;

import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.synchronization.RepositorySynchronizer;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;

public abstract class RepublishRepositoryStrategy<T extends Repository> extends Strategy<T> {

    private final RepositorySynchronizer<T> repositorySynchronizer;

    protected RepublishRepositoryStrategy(
            T resource,
            NewsfeedEventService newsfeedEventService,
            RepositoryService<T> service,
            User requester,
            RepositorySynchronizer<T> repositorySynchronizer) {
        super(resource, service, requester, newsfeedEventService);
        this.repositorySynchronizer = repositorySynchronizer;
    }

    @Override
    protected T actualStrategy() throws StrategyFailure {
        try {
            repositorySynchronizer.storeRepositoryOnRemoteServer(resource);
        } catch (SynchronizeRepositoryException e) {
            throw new StrategyFailure(e);
        }

        return resource;
    }

    @Override
    protected NewsfeedEvent generateEvent(T resource) {
        return new NewsfeedEvent(requester, NewsfeedEventType.REPUBLISH, resource);
    }
}

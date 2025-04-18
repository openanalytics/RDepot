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
package eu.openanalytics.rdepot.base.strategy.create;

import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import java.util.Objects;

/**
 * Creates {@link Repository}.
 * @param <T>
 */
public abstract class CreateRepositoryStrategy<T extends Repository> extends CreateStrategy<T> {

    protected CreateRepositoryStrategy(
            T resource, RepositoryService<T> service, User requester, NewsfeedEventService newsfeedEventService) {
        super(resource, service, requester, newsfeedEventService);
    }

    @Override
    protected T actualStrategy() throws StrategyFailure {
        if (Objects.isNull(resource.getRequiresAuthentication())) {
            resource.setRequiresAuthentication(true);
        }
        return super.actualStrategy();
    }

    @Override
    public void postStrategy() throws StrategyFailure {}

    @Override
    protected NewsfeedEvent generateEvent(T resource) {
        return new NewsfeedEvent(requester, NewsfeedEventType.CREATE, resource);
    }
}

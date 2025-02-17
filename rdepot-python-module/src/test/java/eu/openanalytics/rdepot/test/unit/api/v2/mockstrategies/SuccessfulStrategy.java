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
package eu.openanalytics.rdepot.test.unit.api.v2.mockstrategies;

import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.Service;
import eu.openanalytics.rdepot.base.strategy.create.CreateStrategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyReversionFailure;

public class SuccessfulStrategy<T extends Resource> extends CreateStrategy<T> {
    public SuccessfulStrategy(T resource, NewsfeedEventService eventService, Service<T> service, User requester) {
        super(resource, service, requester, eventService);
    }

    @Override
    protected T actualStrategy() throws StrategyFailure {
        return resource;
    }

    @Override
    public void postStrategy() throws StrategyFailure {}

    @Override
    public void revertChanges() throws StrategyReversionFailure {}

    @Override
    protected NewsfeedEvent generateEvent(T resource) {
        return null;
    }
}

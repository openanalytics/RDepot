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
package eu.openanalytics.rdepot.base.strategy.update;

import eu.openanalytics.rdepot.base.entities.EventChangedVariable;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.Service;
import eu.openanalytics.rdepot.base.service.exceptions.CreateEntityException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import java.util.HashSet;
import java.util.Set;

/**
 * Strategy template for updating resources.
 * @param <T> Resource entity
 */
public abstract class UpdateStrategy<T extends Resource> extends Strategy<T> {

    protected final Set<EventChangedVariable> changedValues = new HashSet<>();
    protected final T updatedResource;
    protected final T oldResourceCopy;
    private final NewsfeedEventService newsfeedEventService;

    protected UpdateStrategy(
            T resource,
            Service<T> service,
            NewsfeedEventService newsfeedEventService,
            User requester,
            T updatedResource,
            T oldResourceCopy) {
        super(resource, service, requester, newsfeedEventService);
        this.updatedResource = updatedResource;
        this.oldResourceCopy = oldResourceCopy;
        this.newsfeedEventService = newsfeedEventService;
    }

    @Override
    protected void registerEvent(NewsfeedEvent event) throws CreateEntityException {
        if (!changedValues.isEmpty()) {
            NewsfeedEvent registered = newsfeedEventService.create(event);
            newsfeedEventService.attachVariables(registered, changedValues);
        }
    }
}

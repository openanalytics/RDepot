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
package eu.openanalytics.rdepot.base.strategy;

import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.Service;
import eu.openanalytics.rdepot.base.service.exceptions.CreateEntityException;
import eu.openanalytics.rdepot.base.strategy.exceptions.FatalStrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyReversionFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Strategy is an object that coordinates RDepot's business logic.
 * It is created for every request made by either user or the system
 * ending always with a single {@link NewsfeedEvent} describing what happened.
 * Every strategy can be applied ("performed") and reverted.
 * It requires provision of all the singletons
 * that would participate in the requested operation.
 * @param <T> resource type (entity class) which strategy is related to
 */
public abstract class Strategy<T extends Resource> {

    protected final T resource;
    protected T processedResource;
    protected final Service<T> service;
    protected final User requester;
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final NewsfeedEventService newsfeedEventService;

    /**
     * @param resource related resource
     * @param newsfeedEventService used to register event
     * @param service main service related to the resource
     * @param requester user performing a request
     */
    protected Strategy(T resource, Service<T> service, User requester, NewsfeedEventService newsfeedEventService) {
        this.resource = resource;
        this.service = service;
        this.requester = requester;
        this.newsfeedEventService = newsfeedEventService;
    }

    /**
     * Actual business logic of the strategy.
     * @return created, modified or unchanged (in case of deletion) object
     * @throws StrategyFailure in case any step could not succeed.
     */
    protected abstract T actualStrategy() throws StrategyFailure;

    /**
     * Generates event describing what happened in the strategy.
     * @param resource related resource
     */
    protected abstract NewsfeedEvent generateEvent(T resource);

    /**
     * Operations performed after the event is registered and operation performed.
     * May be used to link another strategy that
     * would be performed as the next operation in the chain.
     * Override this method if its needed
     */
    protected void postStrategy() throws StrategyFailure {}

    /**
     * Rolls back every operation performed in the strategy.
     * May be used for failure recovery.
     * Override this method if its needed
     */
    public void revertChanges() throws StrategyReversionFailure {}

    /**
     * Performs the strategy.
     * This method coordinates the basic flow of every strategy.
     * @return created, modified or unchanged (in case of deletion) object
     * @throws StrategyFailure contains information about what went wrong in the strategy
     */
    public T perform() throws StrategyFailure {
        processedResource = actualStrategy();
        try {
            registerEvent(generateEvent(processedResource));
        } catch (CreateEntityException e) {
            throw new FatalStrategyFailure(e);
        }
        postStrategy();
        return processedResource;
    }

    /**
     * Saves event in the database.
     */
    protected void registerEvent(NewsfeedEvent event) throws CreateEntityException {
        newsfeedEventService.create(event);
    }
}

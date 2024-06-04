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
package eu.openanalytics.rdepot.test.unit.api.v2.mockstrategies;

import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.Service;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyReversionFailure;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageDuplicateWithReplaceOff;

public class DuplicatePackageStrategy extends Strategy<Submission> {

    /**
     * @param resource             related resource
     * @param service              main service related to the resource
     * @param requester            user performing a request
     * @param newsfeedEventService used to register event
     */
    public DuplicatePackageStrategy(
            Submission resource,
            Service<Submission> service,
            User requester,
            NewsfeedEventService newsfeedEventService) {
        super(resource, service, requester, newsfeedEventService);
    }

    @Override
    protected Submission actualStrategy() throws StrategyFailure {
        throw new StrategyFailure(new PackageDuplicateWithReplaceOff(resource), false);
    }

    @Override
    protected NewsfeedEvent generateEvent(Submission resource) {
        return null;
    }

    @Override
    protected void postStrategy() throws StrategyFailure {}

    @Override
    public void revertChanges() throws StrategyReversionFailure {}
}

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

import eu.openanalytics.rdepot.base.entities.AccessToken;
import eu.openanalytics.rdepot.base.entities.EventChangedVariable;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.service.AccessTokenService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyReversionFailure;
import javax.naming.OperationNotSupportedException;

/**
 * Updates the {@link AccessToken Access Token}.
 * Can only deactivate the token or update its name.
 */
public class UpdateAccessTokenStrategy extends UpdateStrategy<AccessToken> {

    public UpdateAccessTokenStrategy(
            AccessToken resource,
            User requester,
            AccessToken updatedResource,
            NewsfeedEventService newsfeedEventService,
            AccessTokenService accessTokenService) {
        super(
                resource,
                accessTokenService,
                newsfeedEventService,
                requester,
                updatedResource,
                new AccessToken(resource));
    }

    @Override
    protected AccessToken actualStrategy() throws StrategyFailure {
        if (updatedResource.isActive() && !resource.isActive()) {
            throw new StrategyFailure(new OperationNotSupportedException());
        } else if (!updatedResource.isActive()) {
            resource.setActive(false);
            changedValues.add(new EventChangedVariable("active", "true", "false"));
        }

        if (!resource.getName().equals(updatedResource.getName())) {
            changedValues.add(new EventChangedVariable("name", resource.getName(), updatedResource.getName()));
            resource.setName(updatedResource.getName());
        }

        return resource;
    }

    @Override
    protected NewsfeedEvent generateEvent(AccessToken resource) {
        return new NewsfeedEvent(requester, NewsfeedEventType.UPDATE, resource);
    }

    @Override
    protected void postStrategy() throws StrategyFailure {}

    @Override
    public void revertChanges() throws StrategyReversionFailure {}
}

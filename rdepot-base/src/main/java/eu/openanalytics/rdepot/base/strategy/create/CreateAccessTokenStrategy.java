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

import eu.openanalytics.rdepot.base.entities.AccessToken;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.service.AccessTokenService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;

/**
 * Creates {@link AccessToken Access Token} for a certain {@link User}.
 */
public class CreateAccessTokenStrategy 
	extends CreateStrategy<AccessToken> {
	
	public CreateAccessTokenStrategy(
			AccessToken resource, 
			User requester,
			NewsfeedEventService newsfeedEventService,
			AccessTokenService accessTokenService) {
		super(resource, accessTokenService, requester, newsfeedEventService);
	}
	
	@Override
	protected NewsfeedEvent generateEvent(AccessToken resource) {
		return new NewsfeedEvent(requester, NewsfeedEventType.CREATE, resource);
	}

	@Override
	protected void postStrategy() throws StrategyFailure {}
}

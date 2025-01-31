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
package eu.openanalytics.rdepot.r.strategy.republish;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.strategy.republish.RepublishRepositoryStrategy;
import eu.openanalytics.rdepot.base.synchronization.RepositorySynchronizer;
import eu.openanalytics.rdepot.r.entities.RRepository;

public class RRepositoryRepublishStrategy extends RepublishRepositoryStrategy<RRepository> {

    public RRepositoryRepublishStrategy(
            RRepository resource,
            NewsfeedEventService newsfeedEventService,
            RepositoryService<RRepository> service,
            User requester,
            RepositorySynchronizer<RRepository> repositorySynchronizer) {
        super(resource, newsfeedEventService, service, requester, repositorySynchronizer);
    }
}

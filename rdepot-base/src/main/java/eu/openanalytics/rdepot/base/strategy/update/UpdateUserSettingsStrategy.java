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

import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.UserSettings;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.UserSettingsService;
import eu.openanalytics.rdepot.base.service.exceptions.CreateEntityException;
import eu.openanalytics.rdepot.base.strategy.exceptions.FatalStrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyReversionFailure;
import org.springframework.transaction.annotation.Transactional;

/**
 * Updates {@link UserSettings}.
 * It generates no {@link NewsfeedEvent events}.
 */
public class UpdateUserSettingsStrategy extends UpdateStrategy<UserSettings> {

    private final boolean toCreate;

    public UpdateUserSettingsStrategy(
            UserSettings resource,
            UserSettingsService service,
            NewsfeedEventService newsfeedEventService,
            User requester,
            UserSettings updatedResource,
            boolean toCreate) {
        super(resource, service, newsfeedEventService, requester, updatedResource, new UserSettings(resource));
        this.toCreate = toCreate;
    }

    @Override
    protected void registerEvent(NewsfeedEvent event) {}

    @Override
    @Transactional(rollbackFor = FatalStrategyFailure.class)
    protected UserSettings actualStrategy() throws StrategyFailure {
        if (!resource.getLanguage().equals(updatedResource.getLanguage())) {
            resource.setLanguage(updatedResource.getLanguage());
        }
        if (!resource.getTheme().equals(updatedResource.getTheme())) {
            resource.setTheme(updatedResource.getTheme());
        }
        if (resource.getPageSize() != updatedResource.getPageSize()) {
            resource.setPageSize(updatedResource.getPageSize());
        }
        if (toCreate) {
            try {
                return service.create(resource);
            } catch (CreateEntityException e) {
                throw new FatalStrategyFailure(e);
            }
        }
        return resource;
    }

    @Override
    protected NewsfeedEvent generateEvent(UserSettings resource) {
        return null;
    }

    @Override
    protected void postStrategy() throws StrategyFailure {}

    @Override
    public void revertChanges() throws StrategyReversionFailure {}
}

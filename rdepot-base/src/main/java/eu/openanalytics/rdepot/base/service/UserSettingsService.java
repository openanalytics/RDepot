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
package eu.openanalytics.rdepot.base.service;

import eu.openanalytics.rdepot.base.config.props.UserSettingsConfiguration;
import eu.openanalytics.rdepot.base.daos.UserSettingsDao;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.UserSettings;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Reads and write {@link UserSettings} from/into the database.
 */
@Service
public class UserSettingsService extends eu.openanalytics.rdepot.base.service.Service<UserSettings> {

    private final UserSettingsDao userSettingsDao;
    private final UserSettingsConfiguration config;

    public UserSettingsService(UserSettingsDao userSettingsDao, UserSettingsConfiguration config) {
        super(userSettingsDao);
        this.userSettingsDao = userSettingsDao;
        this.config = config;
    }

    /**
     * Fetches settings for a given user.
     */
    public Optional<UserSettings> findSettingsByUser(User user) {
        return userSettingsDao.findByUserId(user.getId());
    }

    /**
     * Retrieves current {@link UserSettings settings} for a given {@link User user}.
     * If they are not yet set, {@link #getDefaultSettings() defaults}
     * are assigned and returned.
     * @return current or default user settings
     */
    public UserSettings getUserSettings(User user) {
        UserSettings settings = user.getUserSettings();
        if (Objects.isNull(settings)) {
            settings = getDefaultSettings();
            settings.setUser(user);
        }
        return settings;
    }

    /**
     * @return Default user settings,
     *  based on values from {@link UserSettingsConfiguration}.
     */
    public UserSettings getDefaultSettings() {
        return new UserSettings(false, config.getLanguage(), config.getTheme(), config.getPageSize());
    }
}

/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
package eu.openanalytics.rdepot.test.strategy.update;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.UserSettings;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.UserSettingsService;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.update.UpdateUserSettingsStrategy;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.strategy.StrategyTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class UpdateUserSettingsStrategyTest extends StrategyTest {

    @Mock
    NewsfeedEventService eventService;

    @Mock
    UserSettingsService userSettingsService;

    private Strategy<UserSettings> strategy;

    private UserSettings userSettings;
    private UserSettings updatedUserSettings;

    private static User user;

    @BeforeAll
    public static void init() {
        user = UserTestFixture.GET_REGULAR_USER();
    }

    @BeforeEach
    public void initEach() {
        userSettings = new UserSettings(1, false, "en-EN", "light", 10, user);
        updatedUserSettings = new UserSettings(userSettings);
    }

    @Test
    public void updateUserSettings_shouldChangeLanguage() throws Exception {
        updatedUserSettings.setLanguage("en-EN");
        strategy = new UpdateUserSettingsStrategy(
                userSettings, userSettingsService, eventService, user, updatedUserSettings, false);
        strategy.perform();
        assertEquals(userSettings.getLanguage(), updatedUserSettings.getLanguage());
    }

    @Test
    public void updateUserSettings_shouldChangeTheme() throws Exception {
        updatedUserSettings.setTheme("dark");
        strategy = new UpdateUserSettingsStrategy(
                userSettings, userSettingsService, eventService, user, updatedUserSettings, false);
        strategy.perform();
        assertEquals(userSettings.getLanguage(), updatedUserSettings.getLanguage());
    }

    @Test
    public void updateUserSettings_shouldChangePageSize() throws Exception {
        updatedUserSettings.setPageSize(17);
        strategy = new UpdateUserSettingsStrategy(
                userSettings, userSettingsService, eventService, user, updatedUserSettings, false);
        strategy.perform();
        assertEquals(userSettings.getLanguage(), updatedUserSettings.getLanguage());
    }
}

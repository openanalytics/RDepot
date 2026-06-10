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
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.openanalytics.rdepot.base.entities.AccessToken;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.AccessTokenService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.update.UpdateAccessTokenStrategy;
import eu.openanalytics.rdepot.test.fixture.AccessTokenTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.strategy.StrategyTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class UpdateAccessTokenStrategyTest extends StrategyTest {

    @Mock
    NewsfeedEventService eventService;

    @Mock
    AccessTokenService accessTokenService;

    private Strategy<AccessToken> strategy;

    private AccessToken accessToken;
    private AccessToken updatedAccessToken;

    private static User user;

    @BeforeAll
    public static void init() {
        user = UserTestFixture.GET_REGULAR_USER();
    }

    @BeforeEach
    public void initEach() {
        accessToken = AccessTokenTestFixture.GET_FIXTURE_ACCESS_TOKEN(user);
        updatedAccessToken = new AccessToken(accessToken);
    }

    @Test
    public void updateAccessToken_shouldChangeName() throws Exception {
        updatedAccessToken.setName("New token name");
        strategy =
                new UpdateAccessTokenStrategy(accessToken, user, updatedAccessToken, eventService, accessTokenService);
        strategy.perform();
        assertEquals(accessToken.getName(), updatedAccessToken.getName());
    }

    @Test
    public void updateAccessToken_shouldDeactivateToken() throws Exception {
        updatedAccessToken.setActive(false);
        strategy =
                new UpdateAccessTokenStrategy(accessToken, user, updatedAccessToken, eventService, accessTokenService);
        strategy.perform();
        assertEquals(accessToken.isActive(), updatedAccessToken.isActive());
    }

    @Test
    public void updateAccessToken_throwsFailureWhenReactivatingToken() {
        accessToken.setActive(false);
        updatedAccessToken.setActive(true);
        strategy =
                new UpdateAccessTokenStrategy(accessToken, user, updatedAccessToken, eventService, accessTokenService);
        assertThrows(StrategyFailure.class, () -> strategy.perform());
    }
}

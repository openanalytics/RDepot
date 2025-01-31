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
package eu.openanalytics.rdepot.test.strategy.create;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.openanalytics.rdepot.base.entities.AccessToken;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.AccessTokenService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.create.CreateAccessTokenStrategy;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.strategy.StrategyTest;
import java.time.Instant;
import java.util.Calendar;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class CreateAccessTokenStrategyTest extends StrategyTest {

    @Mock
    NewsfeedEventService eventService;

    @Mock
    AccessTokenService service;

    private Strategy<AccessToken> strategy;

    private AccessToken resource;

    private static User user;
    private AccessToken expectedToken;

    @BeforeAll
    public static void init() {
        user = UserTestFixture.GET_REGULAR_USER();
    }

    private void createAccessToken() throws Exception {
        expectedToken = new AccessToken();
        expectedToken.setId(7);
        expectedToken.setName("expected test token");
        expectedToken.setValue("$2b$10$0hClsEu3CHpEC00DwE6wTeYZhEwuzJ.SvbJuG9ZuQ56ry7VpvczOK");

        final Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.NOVEMBER, 27);
        final Instant creationDate = cal.toInstant();
        cal.set(2023, Calendar.DECEMBER, 11);
        final Instant expirationDate = cal.toInstant();

        expectedToken.setCreationDate(creationDate);
        expectedToken.setExpirationDate(expirationDate);
        expectedToken.setActive(true);
        expectedToken.setDeleted(false);
        expectedToken.setPlainValue("ECPPn5HcNubvqIp39mF5VbIfdBEvjTQh");

        when(service.create(any())).thenReturn(expectedToken);

        strategy = new CreateAccessTokenStrategy(resource, user, eventService, service);
    }

    @Test
    public void createAccessToken_shouldCallServiceCreateMethodOnce() throws Exception {
        doAnswer(invocation -> invocation.getArgument(0)).when(eventService).create(any());

        createAccessToken();
        AccessToken actualToken = strategy.perform();
        assertEquals(actualToken, expectedToken);

        verify(service, times(1)).create(resource);
    }

    @Test
    public void createAccessToken_shouldCallEventServiceCreateMethodOnce() throws Exception {
        doAnswer(invocation -> invocation.getArgument(0)).when(eventService).create(any());

        createAccessToken();
        strategy.perform();

        verify(eventService, times(1)).create(any());
    }
}

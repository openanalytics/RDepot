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
package eu.openanalytics.rdepot.test.mediator.deleter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.openanalytics.rdepot.base.entities.AccessToken;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.deletion.AccessTokenDeleter;
import eu.openanalytics.rdepot.base.service.AccessTokenService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.test.fixture.AccessTokenTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class AccessTokenDeleterTest extends UnitTest {

    @Mock
    NewsfeedEventService newsfeedEventService;

    @Mock
    AccessTokenService accessTokenService;

    @InjectMocks
    AccessTokenDeleter deleter;

    AccessToken accessToken;
    User user;

    @BeforeEach
    public void setUpResources() {
        user = UserTestFixture.GET_REGULAR_USER();
        accessToken = AccessTokenTestFixture.GET_FIXTURE_ACCESS_TOKEN(user);
    }

    @Test
    public void delete() throws Exception {
        doNothing().when(newsfeedEventService).deleteRelatedEvents(any(AccessToken.class));
        doNothing().when(accessTokenService).delete(any(AccessToken.class));
        deleter.delete(accessToken);

        verify(newsfeedEventService, times(1)).deleteRelatedEvents(any(AccessToken.class));
        verify(accessTokenService, times(1)).delete(any(AccessToken.class));
    }

    @Test
    public void delete_throwsNPE_whenTryingToDeleteNullAccessToken() throws Exception {
        assertThrows(NullPointerException.class, () -> deleter.delete(null));

        verify(newsfeedEventService, times(0)).deleteRelatedEvents(any(AccessToken.class));
        verify(accessTokenService, times(0)).delete(any(AccessToken.class));
    }
}

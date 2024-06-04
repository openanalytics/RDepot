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
package eu.openanalytics.rdepot.test.fixture;

import eu.openanalytics.rdepot.base.entities.AccessToken;
import eu.openanalytics.rdepot.base.entities.User;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AccessTokenTestFixture {

    public static final String NAME = "test token";
    public static final String VALUE =
            "sJqAopeLsh2BBjI8gPkd73NFtWK6RwF"; // 31 characters, cause one char is added in function
    public static final LocalDate CREATION_DATE = LocalDate.of(2023, 11, 24);
    public static final LocalDate EXPIRATION_DATE = LocalDate.of(2023, 12, 23);
    public static final boolean ACTIVE = true;
    public static final boolean DELETED = false;

    public static List<AccessToken> GET_FIXTURE_ACCESS_TOKENS(User user, int count) {

        List<AccessToken> accessTokens = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            AccessToken token = new AccessToken(
                    i,
                    NAME + Integer.toString(i),
                    VALUE + Integer.toString(i),
                    CREATION_DATE.plusDays(i),
                    EXPIRATION_DATE.plusDays(i),
                    ACTIVE,
                    DELETED,
                    user);

            accessTokens.add(token);
        }

        return accessTokens;
    }

    public static AccessToken GET_FIXTURE_ACCESS_TOKEN(User user) {
        return GET_FIXTURE_ACCESS_TOKENS(user, 1).get(0);
    }
}

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
package eu.openanalytics.rdepot.test.fixture;

import eu.openanalytics.rdepot.base.entities.AccessToken;
import eu.openanalytics.rdepot.base.entities.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

public class AccessTokenTestFixture {
    private static final Calendar cal = Calendar.getInstance();

    public static final String NAME = "test token";
    public static final String VALUE =
            "sJqAopeLsh2BBjI8gPkd73NFtWK6RwF"; // 31 characters, cause one char is added in function
    public static final Instant CREATION_DATE; // Instant.of(2023, 11, 24);
    public static final Instant EXPIRATION_DATE; // = LocalDate.of(2023, 12, 23);
    public static final Instant LAST_USED;

    static {
        cal.set(2023, Calendar.NOVEMBER, 24);
        CREATION_DATE = cal.toInstant();
        cal.set(2023, Calendar.DECEMBER, 23);
        EXPIRATION_DATE = cal.toInstant();
        cal.set(2023, Calendar.DECEMBER, 21, 12, 30);
        LAST_USED = cal.toInstant();
    }

    public static final boolean ACTIVE = true;
    public static final boolean DELETED = false;

    public static List<AccessToken> GET_FIXTURE_ACCESS_TOKENS(User user, int count) {

        List<AccessToken> accessTokens = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            AccessToken token = new AccessToken(
                    i,
                    NAME + i,
                    VALUE + i,
                    CREATION_DATE.plus(i, ChronoUnit.DAYS),
                    EXPIRATION_DATE.plus(i, ChronoUnit.DAYS),
                    ACTIVE,
                    DELETED,
                    user,
                    LAST_USED);

            accessTokens.add(token);
        }

        return accessTokens;
    }

    public static AccessToken GET_FIXTURE_ACCESS_TOKEN(User user) {
        return GET_FIXTURE_ACCESS_TOKENS(user, 1).get(0);
    }

    public static Page<AccessToken> GET_EXAMPLE_ACCESS_TOKENS_FOR_USER_PAGED(User user) {
        return new PageImpl<>(GET_FIXTURE_ACCESS_TOKENS(user, 3));
    }

    public static Page<AccessToken> GET_EXAMPLE_ACCESS_TOKENS_PAGED() {
        User user1 = UserTestFixture.GET_REGULAR_USER(111);
        User user2 = UserTestFixture.GET_REGULAR_USER(222);
        User user3 = UserTestFixture.GET_REGULAR_USER(333);

        List<AccessToken> tokens = GET_FIXTURE_ACCESS_TOKENS(user1, 2);
        tokens.addAll(GET_FIXTURE_ACCESS_TOKENS(user2, 2));
        tokens.addAll(GET_FIXTURE_ACCESS_TOKENS(user3, 2));

        return new PageImpl<>(tokens);
    }
}

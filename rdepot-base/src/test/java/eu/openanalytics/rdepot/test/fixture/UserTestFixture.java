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

import eu.openanalytics.rdepot.base.entities.User;
import java.time.Instant;
import java.util.Calendar;

public class UserTestFixture {

    public static final String LOGIN = "test";
    public static final String EMAIL = "@example.org";
    public static final String NAME = "Test ";
    public static final Boolean ACTIVE = true;
    public static final Boolean DELETED = false;
    private static final Calendar cal = Calendar.getInstance();

    public static User GET_REGULAR_USER() {
        cal.set(2022, Calendar.JUNE, 6);
        final Instant lastLoggedInOn = cal.toInstant();
        cal.set(1970, Calendar.JANUARY, 1);
        final Instant createdOn = cal.toInstant();
        User user = new User(
                123,
                RoleTestFixture.ROLE.USER,
                NAME + "User",
                "testuser" + EMAIL,
                LOGIN + "user",
                ACTIVE,
                DELETED,
                lastLoggedInOn,
                createdOn);

        return user;
    }

    public static User GET_REGULAR_USER(int id) {
        User user = GET_REGULAR_USER();
        user.setId(id);

        return user;
    }

    public static User GET_PACKAGE_MAINTAINER() {
        cal.set(2022, Calendar.JUNE, 6);
        final Instant lastLoggedInOn = cal.toInstant();
        cal.set(1970, Calendar.JANUARY, 1);
        final Instant createdOn = cal.toInstant();
        User maintainer = new User(
                123,
                RoleTestFixture.ROLE.PACKAGE_MAINTAINER,
                NAME + "Package Maintainer",
                "packagemaintainer" + EMAIL,
                LOGIN + "packagemaintainer",
                ACTIVE,
                DELETED,
                lastLoggedInOn,
                createdOn);

        return maintainer;
    }

    public static User GET_PACKAGE_MAINTAINER(int id) {
        User maintainer = GET_PACKAGE_MAINTAINER();
        maintainer.setId(id);

        return maintainer;
    }

    public static User GET_REPOSITORY_MAINTAINER() {
        cal.set(2022, Calendar.JUNE, 6);
        final Instant lastLoggedInOn = cal.toInstant();
        cal.set(1970, Calendar.JANUARY, 1);
        final Instant createdOn = cal.toInstant();
        User maintainer = new User(
                123,
                RoleTestFixture.ROLE.REPOSITORY_MAINTAINER,
                NAME + "Repo Maintainer",
                "repomaintainer" + EMAIL,
                LOGIN + "repomaintainer",
                ACTIVE,
                DELETED,
                lastLoggedInOn,
                createdOn);

        return maintainer;
    }

    public static User GET_REPOSITORY_MAINTAINER(int id) {
        User maintainer = GET_REPOSITORY_MAINTAINER();
        maintainer.setId(id);

        return maintainer;
    }

    public static User GET_ADMIN() {
        cal.set(2022, Calendar.JUNE, 6);
        final Instant lastLoggedInOn = cal.toInstant();
        cal.set(1970, Calendar.JANUARY, 1);
        final Instant createdOn = cal.toInstant();
        User admin = new User(
                123,
                RoleTestFixture.ROLE.ADMIN,
                NAME + "Admin",
                "admin" + EMAIL,
                LOGIN + "admin",
                ACTIVE,
                DELETED,
                lastLoggedInOn,
                createdOn);

        return admin;
    }

    public static User GET_ADMIN(int id) {
        User admin = GET_ADMIN();
        admin.setId(id);

        return admin;
    }
}

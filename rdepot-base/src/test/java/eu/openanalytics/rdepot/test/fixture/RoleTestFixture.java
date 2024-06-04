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

import eu.openanalytics.rdepot.base.entities.Role;
import java.util.List;

public class RoleTestFixture {
    public static final class ROLE {
        public static Role ADMIN = new Role(0, 3, "admin", "Admin");
        public static Role REPOSITORY_MAINTAINER = new Role(1, 2, "repositorymaintainer", "Repository Maintainer");
        public static Role PACKAGE_MAINTAINER = new Role(2, 1, "packagemaintainer", "Package Maintainer");
        public static Role USER = new Role(3, 0, "user", "User");
        public static List<Role> ROLES = List.of(USER, PACKAGE_MAINTAINER, REPOSITORY_MAINTAINER, ADMIN);
    }
    ;

    public static Role GET_BY_NAME(String name) {
        switch (name) {
            case "admin":
                return ROLE.ADMIN;
            case "repository_maintainer":
                return ROLE.REPOSITORY_MAINTAINER;
            case "package_maintainer":
                return ROLE.PACKAGE_MAINTAINER;
            default:
                return ROLE.USER;
        }
    }
}

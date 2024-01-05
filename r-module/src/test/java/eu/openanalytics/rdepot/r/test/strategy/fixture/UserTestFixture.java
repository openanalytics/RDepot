/**
 * R Depot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.r.test.strategy.fixture;

import java.time.LocalDate;

import eu.openanalytics.rdepot.base.entities.User;

public class UserTestFixture {
	
	public static User GET_REGULAR_USER() {
		User user = new User();
		
		user.setId(123);
		user.setActive(true);
		user.setDeleted(false);
		user.setEmail("testuser123@example.org");
		user.setLastLoggedInOn(LocalDate.of(2022, 06, 06));
		user.setLogin("testuser123");
		user.setName("John Smith");
		user.setRole(RoleTestFixture.ROLE.USER);
		user.setCreatedOn(LocalDate.of(1970, 1, 1));
		
		return user;
	}
	
	public static User GET_ADMIN() {
		User admin = GET_REGULAR_USER();
		admin.setRole(RoleTestFixture.ROLE.ADMIN);
		
		return admin;
	}

	public static User GET_REPOSITORY_MAINTAINER() {
		User maintainer = GET_REGULAR_USER();
		maintainer.setRole(RoleTestFixture.ROLE.REPOSITORY_MAINTAINER);
		
		return maintainer;
	}

	public static User GET_PACKAGE_MAINTAINER() {
		User maintainer = GET_REGULAR_USER();
		maintainer.setRole(RoleTestFixture.ROLE.PACKAGE_MAINTAINER);
		
		return maintainer;
	}
}

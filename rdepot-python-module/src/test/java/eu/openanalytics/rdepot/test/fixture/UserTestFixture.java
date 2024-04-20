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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;

public class UserTestFixture {

	public static final class ROLE {
		public static Role ADMIN = new Role(0, 3, "admin", "Admin");
		public static Role REPOSITORY_MAINTAINER = new Role(1, 2, "repositorymaintainer", "RepositoryMaintainer");
		public static Role PACKAGE_MAINTAINER = new Role(2, 1, "packagemaintainer", "Package");
		public static Role USER = new Role(3, 0, "user", "User");
	};
	
	public static final String LOGIN = "testusername";
	public static final String EMAIL = "test@example.org";
	public static final String NAME = "Test User";
	public static final Boolean ACTIVE = true;
	public static final Boolean DELETED = false;
	
	public static Optional<User> GET_FIXTURE_ADMIN() {
		User adminTmp = new User(
				0, 
				ROLE.ADMIN,
				NAME + "0",
				"0" + EMAIL,
				"admin_" + LOGIN + "0",
				ACTIVE,
				DELETED);
		
		Optional<User> admin = Optional.of(adminTmp);
		return admin;
	}
	
	public static List<User> GET_FIXTURE_USERS(int repositoryMaintainerCount, int packageMaintainerCount, int userCount, int shift) {
		List<User> users = new ArrayList<>();
		
		for(int i = shift; i < repositoryMaintainerCount + shift; i++) {
			User user = new User(
					i,
					ROLE.REPOSITORY_MAINTAINER,
					NAME + Integer.toString(i),
					Integer.toString(i) + EMAIL,
					"repositorymaintainer_" + LOGIN + Integer.toString(i),
					ACTIVE,
					DELETED);
			
			Instant lastLoggedInOn = LocalDateTime.of(2017, 8, 11, 15, 30).atZone(ZoneId.of("Etc/UTC")).toInstant();
			user.setLastLoggedInOn(LocalDate.ofInstant(lastLoggedInOn, ZoneId.of("Etc/UTC")));
			
			users.add(user);
		}
		
		int localShift = users.size() + shift;
		for(int i = localShift; i < packageMaintainerCount + localShift; i++) {
			User user = new User(
					i,
					ROLE.PACKAGE_MAINTAINER,
					NAME + Integer.toString(i),
					Integer.toString(i) + EMAIL,
					"packagemaintainer_" + LOGIN + Integer.toString(i),
					ACTIVE,
					DELETED);
			
			Instant lastLoggedInOn = LocalDateTime.of(2018, 7, 2, 7, 0).atZone(ZoneId.of("Etc/UTC")).toInstant();
			user.setLastLoggedInOn(LocalDate.ofInstant(lastLoggedInOn, ZoneId.of("Etc/UTC")));
			
			users.add(user);
		}
		
		localShift = users.size() + shift;
		for(int i = localShift; i < userCount + localShift; i++) {
			User user = new User(
					i,
					ROLE.USER,
					NAME + Integer.toString(i),
					Integer.toString(i) + EMAIL,
					"user_" + LOGIN + Integer.toString(i),
					ACTIVE,
					DELETED);
			
			Instant lastLoggedInOn = LocalDateTime.of(2018, 8, 30, 15, 30).atZone(ZoneId.of("Etc/UTC")).toInstant();
			user.setLastLoggedInOn(LocalDate.ofInstant(lastLoggedInOn, ZoneId.of("Etc/UTC")));
			
			users.add(user);
		}
		
		return users;
	}
	
	public static List<User> GET_FIXTURE_USERS(int repositoryMaintainerCount, int packageMaintainerCount, int userCount) {
		return GET_FIXTURE_USERS(repositoryMaintainerCount, packageMaintainerCount, userCount, 0);
	}
	
	public static User GET_FIXTURE_USER_REPOSITORYMAINTAINER() {
		return GET_FIXTURE_USERS(1, 0, 0).get(0);
	}
	
	public static User GET_FIXTURE_USER_REPOSITORYMAINTAINER(int shift) {
		return GET_FIXTURE_USERS(1, 0, 0, shift).get(0);
	}
	
	public static User GET_FIXTURE_USER_PACKAGEMAINTAINER() {
		return GET_FIXTURE_PACKAGEMAINTAINER(0);
	}
	
	public static User GET_FIXTURE_USER() {
		return GET_FIXTURE_USERS(0, 0, 1).get(0);
	}
	
	public static User GET_FIXTURE_PACKAGEMAINTAINER(int shift) {
		return GET_FIXTURE_USERS(0, 1, 0, shift).get(0);
	}
}

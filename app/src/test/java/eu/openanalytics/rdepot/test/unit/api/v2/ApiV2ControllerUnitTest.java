/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
package eu.openanalytics.rdepot.test.unit.api.v2;


import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.MirrorService;
import eu.openanalytics.rdepot.service.PackageEventService;
import eu.openanalytics.rdepot.service.PackageMaintainerEventService;
import eu.openanalytics.rdepot.service.PackageMaintainerService;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.RepositoryEventService;
import eu.openanalytics.rdepot.service.RepositoryMaintainerEventService;
import eu.openanalytics.rdepot.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.RoleService;
import eu.openanalytics.rdepot.service.SubmissionService;
import eu.openanalytics.rdepot.service.UserEventService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.validation.PackageMaintainerValidator;
import eu.openanalytics.rdepot.validation.PackageValidator;
import eu.openanalytics.rdepot.validation.RepositoryMaintainerValidator;
import eu.openanalytics.rdepot.validation.RepositoryValidator;
import eu.openanalytics.rdepot.validation.UserValidator;

public abstract class ApiV2ControllerUnitTest {
	
	
	@MockBean
	protected UserService userService;
	
	@MockBean
	protected RoleService roleService;
	
	@MockBean
	protected PackageService packageService;
	
	@MockBean
	protected RepositoryService repositoryService;
	
	@MockBean
	protected RepositoryValidator repositoryValidator;
	
	@MockBean
	protected SubmissionService submissionService;
	
	@MockBean
	protected PackageMaintainerService packageMaintainerService;
	
	@MockBean
	protected RepositoryMaintainerService repositoryMaintainerService;
	
	@MockBean
	protected PackageMaintainerValidator packageMaintainerValidator;
	
	@MockBean
	protected RepositoryMaintainerValidator repositoryMaintainerValidator;
	
	@MockBean
	protected UserValidator userValidator;
	
	@MockBean
	protected UserEventService userEventService;
	
	@MockBean
	protected PackageEventService packageEventService;
	
	@MockBean
	protected RepositoryEventService repositoryEventService;
	
	@MockBean
	protected PackageMaintainerEventService packageMaintainerEventService;
	
	@MockBean
	protected RepositoryMaintainerEventService repositoryMaintainerEventService;
	
	@MockBean
	protected PackageValidator packageValidator;
	
	@MockBean
	protected MirrorService mirrorService;
	
	@BeforeEach
	public void clearContext() {
		SecurityContextHolder.clearContext();
	}
	
	public static final String JSON_PATH_COMMON = ClassLoader.getSystemClassLoader().getResource("unit/jsonscommon").getPath();
	
	public static final String ERROR_NOT_AUTHENTICATED_PATH = JSON_PATH_COMMON + "/error_not_authenticated.json";
	public static final String ERROR_NOT_AUTHORIZED_PATH = JSON_PATH_COMMON + "/error_not_authorized.json";
	public static final String ERROR_CREATE_PATH = JSON_PATH_COMMON + "/error_create.json";
	public static final String EXAMPLE_DELETED_PATH = JSON_PATH_COMMON + "/example_deleted.json";
	public static final String ERROR_DELETE_PATH = JSON_PATH_COMMON + "/error_delete.json";
	public static final String ERROR_PATCH_PATH = JSON_PATH_COMMON + "/error_patch.json";
	public static final String EXAMPLE_USERS_PATH = JSON_PATH_COMMON + "/example_users.json";
	public static final String EXAMPLE_USER_PATH = JSON_PATH_COMMON + "/example_user.json";
	public static final String ERROR_USER_NOT_FOUND_PATH = JSON_PATH_COMMON + "/error_user_notfound.json";
	public static final String EXAMPLE_USER_PATCHED_PATH = JSON_PATH_COMMON + "/example_user_patched.json";
	public static final String EXAMPLE_ROLES_PATH = JSON_PATH_COMMON + "/example_roles.json";
	public static final String EXAMPLE_TOKEN_PATH = JSON_PATH_COMMON + "/example_token.json";
	public static final String ERROR_UPDATE_NOT_ALLOWED_PATH = JSON_PATH_COMMON + "/error_update_notallowed.json";

	protected Authentication getMockAuthentication(User user) {
		Authentication authentication = mock(Authentication.class);
		
		when(authentication.getPrincipal()).thenReturn(null);
		when(authentication.getName()).thenReturn(user.getLogin());
		
		return authentication;
	}
	
	protected Principal getMockPrincipal(User user) {
		Principal mockPrincipal = mock(Principal.class);
		
		when(mockPrincipal.getName()).thenReturn(user.getLogin());
		
		return mockPrincipal;
	}
	
	protected User getAdminAndAuthenticate(UserService userService) {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userService.isAdmin(user)).thenReturn(true);

		authenticate(user);
		
		return user;
	}
	
	protected User getUserAndAuthenticate(UserService userService) {
		User user = UserTestFixture.GET_FIXTURE_USER();
		when(userService.isAdmin(user)).thenReturn(false);
		
		authenticate(user);
		
		return user;
	}
	
	protected User getRepositoryMaintainerAndAuthenticate(UserService userService) {
		User user = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		when(userService.isAdmin(user)).thenReturn(false);
		
		authenticate(user);
		
		return user;
	}
	
	protected void authenticate(User user) {
		SecurityContextHolder.getContext().setAuthentication(getMockAuthentication(user));
	}
}
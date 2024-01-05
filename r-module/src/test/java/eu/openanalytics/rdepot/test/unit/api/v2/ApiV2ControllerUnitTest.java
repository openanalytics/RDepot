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
package eu.openanalytics.rdepot.test.unit.api.v2;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2NewsfeedEventController;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.mediator.deletion.PackageMaintainerDeleter;
import eu.openanalytics.rdepot.base.mediator.deletion.RepositoryMaintainerDeleter;
import eu.openanalytics.rdepot.base.mediator.deletion.SubmissionDeleter;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.CommonPackageService;
import eu.openanalytics.rdepot.base.service.CommonRepositoryService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.RoleService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.strategy.factory.StrategyFactory;
import eu.openanalytics.rdepot.base.validation.LegacyPackageMaintainerValidator;
import eu.openanalytics.rdepot.base.validation.LegacyRepositoryMaintainerValidator;
import eu.openanalytics.rdepot.base.validation.PackageMaintainerValidator;
import eu.openanalytics.rdepot.base.validation.RepositoryMaintainerValidator;
import eu.openanalytics.rdepot.base.validation.UserValidator;
import eu.openanalytics.rdepot.r.mediator.deletion.RPackageDeleter;
import eu.openanalytics.rdepot.r.mediator.deletion.RRepositoryDeleter;
import eu.openanalytics.rdepot.r.mirroring.CranMirrorSynchronizer;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.storage.RStorage;
import eu.openanalytics.rdepot.r.strategy.factory.RStrategyFactory;
import eu.openanalytics.rdepot.r.validation.RPackageValidator;
import eu.openanalytics.rdepot.r.validation.RRepositoryValidator;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;

public abstract class ApiV2ControllerUnitTest {
	
	@MockBean
	NewsfeedEventService newsfeedEventService;
	
	@MockBean
	ApiV2NewsfeedEventController apiV2NewsfeedEventController;
	
	@MockBean
	UserService userService;
	
	@MockBean 
	SecurityMediator securityMediator;

	@MockBean(name = "packageMaintainerValidator")
	PackageMaintainerValidator packageMaintainerValidator;
	
	@MockBean
	PackageMaintainerService packageMaintainerService;
	
	@MockBean
	StrategyFactory strategyFactory;
	
	@MockBean
	PackageMaintainerDeleter packageMaintainerDeleter;
	
	@MockBean
	CommonRepositoryService commonRepositoryService;
	
	@MockBean
	CommonPackageService commonPackageService;
	
	@MockBean
	RepositoryMaintainerService repositoryMaintainerService;
	
	@MockBean(name = "repositoryMaintainerValidator")
	RepositoryMaintainerValidator repositoryMaintainerValidator;
	
	@MockBean
	RepositoryMaintainerDeleter repositoryMaintainerDeleter;
	
	@MockBean(name = "legacyPackageMaintainerValidator")
	LegacyPackageMaintainerValidator legacyPackageMaintainerValidator;
	
	@MockBean(name = "legacyRepositoryMaintainerValidator")
	LegacyRepositoryMaintainerValidator legacyRepositoryMaintainerValidator;
	
	@MockBean
	RRepositoryService rRepositoryService;
	
	@MockBean
	RoleService roleService;
	
	@MockBean
	UserValidator userValidator;
	
	@MockBean
	SubmissionService submissionService;
	
	@MockBean
	RStrategyFactory rStrategyFactory;
	
	@MockBean
	SubmissionDeleter submissionDeleter;
	
	@MockBean
	RPackageService rPackageService;
	
	@MockBean
	RPackageDeleter rPackageDeleter;
	
	@MockBean
	RStorage rStorage;
	
	@MockBean
	RRepositoryValidator rRepositoryValidator;
	
	@MockBean
	RPackageValidator rPackageValidator;
	
	@MockBean
	CranMirrorSynchronizer cranMirrorSynchronizer;
	
	@MockBean
	RRepositoryDeleter rRepositoryDeleter;

	@Mock
	protected BestMaintainerChooser bestMaintainerChooser;
	
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

	@Deprecated
	protected Authentication getMockAuthentication(User user) {
		Authentication authentication = mock(Authentication.class);
		
		when(authentication.getPrincipal()).thenReturn(null);
		when(authentication.getName()).thenReturn(user.getLogin());
		
		return authentication;
	}
	
	@Deprecated
	protected Principal getMockPrincipal(User user) {
		Principal mockPrincipal = mock(Principal.class);
		
		when(mockPrincipal.getName()).thenReturn(user.getLogin());
		
		return mockPrincipal;
	}
	
	@Deprecated
	protected Optional<User> getAdminAndAuthenticate(UserService userService) {
		Optional<User> user = UserTestFixture.GET_FIXTURE_ADMIN();
		when(userService.isAdmin(user.get())).thenReturn(true);

		authenticate(user.get());
		
		return user;
	}
	
	@Deprecated
	protected Optional<User> getUserAndAuthenticate(UserService userService) {
		User userTmp = UserTestFixture.GET_FIXTURE_USER();
		Optional<User> user = Optional.of(userTmp);
		when(userService.isAdmin(user.get())).thenReturn(false);
		
		authenticate(user.get());
		
		return user;
	}
	
	@Deprecated
	protected User getRepositoryMaintainerAndAuthenticate(UserService userService) {
		User user = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		when(userService.isAdmin(user)).thenReturn(false);
		
		authenticate(user);
		
		return user;
	}
	
	@Deprecated
	protected void authenticate(User user) {
		SecurityContextHolder.getContext().setAuthentication(getMockAuthentication(user));
	}
}
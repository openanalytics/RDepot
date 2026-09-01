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
package eu.openanalytics.rdepot.test.unit.api.v2;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.openanalytics.rdepot.base.api.v2.converters.PackageDtoConverter;
import eu.openanalytics.rdepot.base.api.v2.converters.SubmissionDtoConverter;
import eu.openanalytics.rdepot.base.api.v2.converters.UserSettingsDtoConverter;
import eu.openanalytics.rdepot.base.api.v2.validation.PageableValidator;
import eu.openanalytics.rdepot.base.config.DefaultUserConfigurationProperties;
import eu.openanalytics.rdepot.base.config.RepositoryNameValidationProperties;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.mediator.deletion.*;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.*;
import eu.openanalytics.rdepot.base.storage.LocalStorage;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.StrategyExecutor;
import eu.openanalytics.rdepot.base.strategy.factory.StrategyFactory;
import eu.openanalytics.rdepot.base.synchronization.healthcheck.ServerAddressHealthcheckService;
import eu.openanalytics.rdepot.base.utils.repositories.PackageMaintainerQueryRepository;
import eu.openanalytics.rdepot.base.validation.*;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import java.security.Principal;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.parsson.JsonProviderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class ApiV2ControllerUnitTest {

    static {
        System.setProperty("jakarta.json.provider", JsonProviderImpl.class.getCanonicalName());
    }

    @MockitoBean
    AccessTokenService accessTokenService;

    @MockitoBean
    AccessTokenPatchValidator accessTokenPatchValidator;

    @MockitoBean
    AccessTokenDeleter accessTokenDeleter;

    @MockitoBean
    NewsfeedEventService newsfeedEventService;

    @MockitoBean
    UserService userService;

    @MockitoBean
    MaintainedPackageService maintainedPackageService;

    @MockitoBean
    SecurityMediator securityMediator;

    @MockitoBean(name = "packageMaintainerValidator")
    PackageMaintainerValidator packageMaintainerValidator;

    @MockitoBean
    PackageMaintainerService packageMaintainerService;

    @MockitoBean
    StrategyFactory strategyFactory;

    @MockitoBean
    PackageMaintainerDeleter packageMaintainerDeleter;

    @MockitoBean
    RepositoryService<Repository> repositoryService;

    @MockitoBean
    CommonPackageService commonPackageService;

    @MockitoBean
    RepositoryMaintainerService repositoryMaintainerService;

    @MockitoBean(name = "repositoryMaintainerValidator")
    RepositoryMaintainerValidator repositoryMaintainerValidator;

    @MockitoBean
    RepositoryMaintainerDeleter repositoryMaintainerDeleter;

    @MockitoBean
    RoleService roleService;

    @MockitoBean
    UserSettingsService userSettingsService;

    @MockitoBean
    UserValidator userValidator;

    @MockitoBean
    SubmissionService submissionService;

    @MockitoBean
    SubmissionDeleter submissionDeleter;

    @MockitoBean
    PackageDeleter<Package, Repository> packageDeleter;

    @MockitoBean
    LocalStorage<Package> localStorage;

    @MockitoBean
    RepositoryValidator<Repository> repositoryValidator;

    @MockitoBean
    PackageValidator<Package> packageValidator;

    @MockitoBean
    RepositoryDeleter<Repository, Package> repositoryDeleter;

    @MockitoBean
    SubmissionDtoConverter submissionDtoConverter;

    @MockitoBean
    PackageDtoConverter commonPackageDtoConverter;

    @MockitoBean
    UserSettingsDtoConverter userSettingsDtoConverter;

    @MockitoBean
    UserSettingsValidator userSettingsValidator;

    @MockitoBean
    PageableValidator pageableValidator;

    @MockitoBean
    StrategyExecutor strategyExecutor;

    @MockitoBean
    ServerAddressHealthcheckService serverAddressHealthcheckService;

    @MockitoBean
    RepositoryNameValidationProperties repositoryNameValidationProperties;

    @MockitoBean
    DefaultUserConfigurationProperties defaultUserConfigurationProperties;

    @MockitoBean
    PackageMaintainerQueryRepository queryRepository;

    @Mock
    protected BestMaintainerChooser bestMaintainerChooser;

    @BeforeEach
    public void clearContext() throws Exception {
        SecurityContextHolder.clearContext();
        Mockito.doAnswer((Answer<Object>) invocationOnMock -> ((Strategy<?>) invocationOnMock.getArgument(0)).perform())
                .when(strategyExecutor)
                .execute(ArgumentMatchers.any());
    }

    public static final String JSON_PATH = Objects.requireNonNull(
                    ClassLoader.getSystemClassLoader().getResource("unit/jsons"))
            .getPath();

    public static final String ERROR_NOT_AUTHENTICATED_PATH = JSON_PATH + "/error_not_authenticated.json";
    public static final String ERROR_NOT_AUTHORIZED_PATH = JSON_PATH + "/error_not_authorized.json";

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
        Optional<User> user = Optional.of(UserTestFixture.GET_ADMIN());
        when(userService.isAdmin(user.get())).thenReturn(true);

        authenticate(user.get());

        return user;
    }

    @Deprecated
    protected Optional<User> getUserAndAuthenticate(UserService userService) {
        User userTmp = UserTestFixture.GET_REGULAR_USER();
        Optional<User> user = Optional.of(userTmp);
        when(userService.isAdmin(user.get())).thenReturn(false);

        authenticate(user.get());

        return user;
    }

    @Deprecated
    protected User getRepositoryMaintainerAndAuthenticate(UserService userService) {
        User user = UserTestFixture.GET_REPOSITORY_MAINTAINER();
        when(userService.isAdmin(user)).thenReturn(false);

        authenticate(user);

        return user;
    }

    @Deprecated
    protected void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(getMockAuthentication(user));
    }
}

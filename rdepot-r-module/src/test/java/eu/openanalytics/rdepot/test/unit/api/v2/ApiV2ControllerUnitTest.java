/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2NewsfeedEventController;
import eu.openanalytics.rdepot.base.api.v2.converters.PackageDtoConverter;
import eu.openanalytics.rdepot.base.api.v2.converters.SubmissionDtoConverter;
import eu.openanalytics.rdepot.base.api.v2.converters.UserSettingsDtoConverter;
import eu.openanalytics.rdepot.base.api.v2.validation.PageableValidator;
import eu.openanalytics.rdepot.base.config.RepositoryNameValidationProperties;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.mediator.deletion.AccessTokenDeleter;
import eu.openanalytics.rdepot.base.mediator.deletion.PackageMaintainerDeleter;
import eu.openanalytics.rdepot.base.mediator.deletion.RepositoryMaintainerDeleter;
import eu.openanalytics.rdepot.base.mediator.deletion.SubmissionDeleter;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.*;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.StrategyExecutor;
import eu.openanalytics.rdepot.base.strategy.factory.StrategyFactory;
import eu.openanalytics.rdepot.base.synchronization.healthcheck.ServerAddressHealthcheckService;
import eu.openanalytics.rdepot.base.validation.*;
import eu.openanalytics.rdepot.r.config.RBinaryProperties;
import eu.openanalytics.rdepot.r.mediator.deletion.RPackageDeleter;
import eu.openanalytics.rdepot.r.mediator.deletion.RRepositoryDeleter;
import eu.openanalytics.rdepot.r.mediator.deletion.RSubmissionDeleter;
import eu.openanalytics.rdepot.r.mirroring.CranMirrorSynchronizer;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.storage.RStorage;
import eu.openanalytics.rdepot.r.strategy.factory.RStrategyFactory;
import eu.openanalytics.rdepot.r.validation.RPackageValidator;
import eu.openanalytics.rdepot.r.validation.RRepositoryValidator;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class ApiV2ControllerUnitTest {

    /*
     JsonProvider issue fix; For some reason,
     Glassfish's JsonProvider implementation is not resolved properly in the test environment.
     Setting this property forces Spring to use this particular class as implementation.
    */
    static {
        System.setProperty("jakarta.json.provider", JsonProviderImpl.class.getCanonicalName());
    }

    @MockBean
    AccessTokenService accessTokenService;

    @MockBean
    AccessTokenPatchValidator accessTokenPatchValidator;

    @MockBean
    AccessTokenDeleter accessTokenDeleter;

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
    RepositoryService<Repository> repositoryService;

    @MockBean
    CommonPackageService commonPackageService;

    @MockBean
    RepositoryMaintainerService repositoryMaintainerService;

    @MockBean(name = "repositoryMaintainerValidator")
    RepositoryMaintainerValidator repositoryMaintainerValidator;

    @MockBean
    RepositoryMaintainerDeleter repositoryMaintainerDeleter;

    @MockBean
    RRepositoryService rRepositoryService;

    @MockBean
    RoleService roleService;

    @MockBean
    UserSettingsService userSettingsService;

    @MockBean
    UserValidator userValidator;

    @MockBean
    SubmissionService submissionService;

    @MockBean
    RStrategyFactory rStrategyFactory;

    @MockBean
    SubmissionDeleter submissionDeleter;

    @MockBean
    RSubmissionDeleter rSubmissionDeleter;

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

    @MockBean
    SubmissionDtoConverter submissionDtoConverter;

    @MockBean
    PackageDtoConverter commonPackageDtoConverter;

    @MockBean
    UserSettingsDtoConverter userSettingsDtoConverter;

    @MockBean
    UserSettingsValidator userSettingsValidator;

    @MockBean
    PageableValidator pageableValidator;

    @MockBean
    StrategyExecutor strategyExecutor;

    @MockBean
    RBinaryProperties rBinaryProperties;

    @MockBean
    ServerAddressHealthcheckService serverAddressHealthcheckService;

    @MockBean
    RepositoryNameValidationProperties repositoryNameValidationProperties;

    @Mock
    protected BestMaintainerChooser bestMaintainerChooser;

    @BeforeEach
    public void clearContext() throws Exception {
        SecurityContextHolder.clearContext();
        Mockito.doAnswer((Answer<Object>) invocationOnMock -> ((Strategy<?>) invocationOnMock.getArgument(0)).perform())
                .when(strategyExecutor)
                .execute(ArgumentMatchers.any());
    }

    public static final String JSON_PATH_COMMON = Objects.requireNonNull(
                    ClassLoader.getSystemClassLoader().getResource("unit/jsonscommon"))
            .getPath();

    public static final String ERROR_NOT_AUTHENTICATED_PATH = JSON_PATH_COMMON + "/error_not_authenticated.json";
    public static final String ERROR_NOT_AUTHORIZED_PATH = JSON_PATH_COMMON + "/error_not_authorized.json";

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

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

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2NewsfeedEventController;
import eu.openanalytics.rdepot.base.api.v2.converters.PackageDtoConverter;
import eu.openanalytics.rdepot.base.api.v2.converters.SubmissionDtoConverter;
import eu.openanalytics.rdepot.base.api.v2.converters.UserSettingsDtoConverter;
import eu.openanalytics.rdepot.base.config.DefaultUserConfigurationProperties;
import eu.openanalytics.rdepot.base.config.RepositoryNameValidationProperties;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.mediator.deletion.AccessTokenDeleter;
import eu.openanalytics.rdepot.base.mediator.deletion.PackageMaintainerDeleter;
import eu.openanalytics.rdepot.base.mediator.deletion.RepositoryMaintainerDeleter;
import eu.openanalytics.rdepot.base.mediator.deletion.SubmissionDeleter;
import eu.openanalytics.rdepot.base.mirroring.MirrorSynchronizationStatusCoordinator;
import eu.openanalytics.rdepot.base.mirroring.converters.PackageSynchronizationStatusDtoConverter;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.*;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.StrategyExecutor;
import eu.openanalytics.rdepot.base.strategy.factory.StrategyFactory;
import eu.openanalytics.rdepot.base.synchronization.healthcheck.ServerAddressHealthcheckService;
import eu.openanalytics.rdepot.base.utils.repositories.PackageMaintainerQueryRepository;
import eu.openanalytics.rdepot.base.validation.*;
import eu.openanalytics.rdepot.python.entities.PythonRepositoryAllowedFiles;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonPackageDeleter;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonRepositoryDeleter;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonSubmissionDeleter;
import eu.openanalytics.rdepot.python.mirroring.PyPiMirrorSynchronizer;
import eu.openanalytics.rdepot.python.mirroring.PythonMirrorSynchronizationCoordinator;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import eu.openanalytics.rdepot.python.services.PythonRepositoryService;
import eu.openanalytics.rdepot.python.storage.implementations.fs.PythonFSLocalStorage;
import eu.openanalytics.rdepot.python.storage.implementations.fs.PythonLocalPersistentStorage;
import eu.openanalytics.rdepot.python.storage.population.implementations.PythonFSPopulator;
import eu.openanalytics.rdepot.python.strategy.factory.PythonStrategyFactory;
import eu.openanalytics.rdepot.python.validation.PythonPackageValidator;
import eu.openanalytics.rdepot.python.validation.PythonRepositoryValidator;
import java.util.Objects;
import org.eclipse.parsson.JsonProviderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public abstract class ApiV2ControllerUnitTest {

    public static final String JSON_PATH_COMMON = Objects.requireNonNull(
                    ClassLoader.getSystemClassLoader().getResource("unit/jsonscommon"))
            .getPath();
    public static final String ERROR_NOT_AUTHENTICATED_PATH = JSON_PATH_COMMON + "/error_not_authenticated.json";
    public static final String ERROR_NOT_AUTHORIZED_PATH = JSON_PATH_COMMON + "/error_not_authorized.json";

    static {
        System.setProperty("jakarta.json.provider", JsonProviderImpl.class.getCanonicalName());
    }

    @MockitoBean
    MirrorSynchronizationStatusCoordinator mirrorSynchronizationStatusCoordinator;

    @MockitoBean
    PythonMirrorSynchronizationCoordinator pythonMirrorSynchronizationCoordinator;

    @MockitoBean
    AccessTokenService accessTokenService;

    @MockitoBean
    AccessTokenPatchValidator accessTokenPatchValidator;

    @MockitoBean
    AccessTokenDeleter accessTokenDeleter;

    @MockitoBean
    NewsfeedEventService newsfeedEventService;

    @MockitoBean
    ApiV2NewsfeedEventController apiV2NewsfeedEventController;

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
    RepositoryService<Repository> commonRepositoryService;

    @MockitoBean
    CommonPackageService commonPackageService;

    @MockitoBean
    RepositoryMaintainerService repositoryMaintainerService;

    @MockitoBean(name = "repositoryMaintainerValidator")
    RepositoryMaintainerValidator repositoryMaintainerValidator;

    @MockitoBean
    RepositoryMaintainerDeleter repositoryMaintainerDeleter;

    @MockitoBean
    PythonRepositoryService pythonRepositoryService;

    @MockitoBean
    RoleService roleService;

    @MockitoBean
    UserSettingsService userSettingsService;

    @MockitoBean
    UserValidator userValidator;

    @MockitoBean
    SubmissionService submissionService;

    @MockitoBean
    PythonStrategyFactory pythonStrategyFactory;

    @MockitoBean
    SubmissionDeleter submissionDeleter;

    @MockitoBean
    PythonSubmissionDeleter pythonSubmissionDeleter;

    @MockitoBean
    PythonPackageService pythonPackageService;

    @MockitoBean
    PythonPackageDeleter pythonPackageDeleter;

    @MockitoBean
    PythonFSPopulator pythonFsPopulator;

    @MockitoBean
    PythonFSLocalStorage pythonLocalStorage;

    @MockitoBean
    PythonRepositoryValidator pythonRepositoryValidator;

    @MockitoBean
    PythonPackageValidator pythonPackageValidator;

    @MockitoBean
    PyPiMirrorSynchronizer pypiMirrorSynchronizer;

    @MockitoBean
    PythonRepositoryDeleter pythonRepositoryDeleter;

    @MockitoBean
    SubmissionDtoConverter submissionDtoConverter;

    @MockitoBean
    PackageDtoConverter commonPackageDtoConverter;

    @MockitoBean
    UserSettingsDtoConverter userDtoConverter;

    @MockitoBean
    UserSettingsValidator userSettingsValidator;

    @MockitoBean
    SubmissionPatchValidator submissionPatchValidator;

    @MockitoBean
    StrategyExecutor strategyExecutor;

    @MockitoBean
    ServerAddressHealthcheckService serverAddressHealthcheckService;

    @MockitoBean
    RepositoryNameValidationProperties repositoryNameValidationProperties;

    @MockitoBean
    PythonRepositoryAllowedFiles repositoryAllowedFiles;

    @MockitoBean
    DefaultUserConfigurationProperties defaultUserConfigurationProperties;

    @MockitoBean
    PackageSynchronizationStatusDtoConverter packageSynchronizationStatusDtoConverter;

    @MockitoBean
    PackageMaintainerQueryRepository queryRepository;

    @MockitoBean
    PythonLocalPersistentStorage pythonLocalPersistentStorage;

    @BeforeEach
    public void clearContext() throws Exception {
        SecurityContextHolder.clearContext();
        Mockito.doAnswer((Answer<Object>) invocationOnMock -> ((Strategy<?>) invocationOnMock.getArgument(0)).perform())
                .when(strategyExecutor)
                .execute(ArgumentMatchers.any());
    }
}

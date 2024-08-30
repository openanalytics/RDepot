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
package eu.openanalytics.rdepot.test.unit.api.v2;

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2NewsfeedEventController;
import eu.openanalytics.rdepot.base.api.v2.converters.PackageDtoConverter;
import eu.openanalytics.rdepot.base.api.v2.converters.SubmissionDtoConverter;
import eu.openanalytics.rdepot.base.api.v2.converters.UserSettingsDtoConverter;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.mediator.deletion.AccessTokenDeleter;
import eu.openanalytics.rdepot.base.mediator.deletion.PackageMaintainerDeleter;
import eu.openanalytics.rdepot.base.mediator.deletion.RepositoryMaintainerDeleter;
import eu.openanalytics.rdepot.base.mediator.deletion.SubmissionDeleter;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.AccessTokenService;
import eu.openanalytics.rdepot.base.service.CommonPackageService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.service.RoleService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.UserSettingsService;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.StrategyExecutor;
import eu.openanalytics.rdepot.base.strategy.factory.StrategyFactory;
import eu.openanalytics.rdepot.base.validation.AccessTokenPatchValidator;
import eu.openanalytics.rdepot.base.validation.PackageMaintainerValidator;
import eu.openanalytics.rdepot.base.validation.RepositoryMaintainerValidator;
import eu.openanalytics.rdepot.base.validation.SubmissionPatchValidator;
import eu.openanalytics.rdepot.base.validation.UserSettingsValidator;
import eu.openanalytics.rdepot.base.validation.UserValidator;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonPackageDeleter;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonRepositoryDeleter;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonSubmissionDeleter;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import eu.openanalytics.rdepot.python.services.PythonRepositoryService;
import eu.openanalytics.rdepot.python.storage.implementations.PythonLocalStorage;
import eu.openanalytics.rdepot.python.strategy.factory.PythonStrategyFactory;
import eu.openanalytics.rdepot.python.validation.PythonPackageValidator;
import eu.openanalytics.rdepot.python.validation.PythonRepositoryValidator;
import org.eclipse.parsson.JsonProviderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class ApiV2ControllerUnitTest {

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
    RepositoryService<Repository> commonRepositoryService;

    @MockBean
    CommonPackageService commonPackageService;

    @MockBean
    RepositoryMaintainerService repositoryMaintainerService;

    @MockBean(name = "repositoryMaintainerValidator")
    RepositoryMaintainerValidator repositoryMaintainerValidator;

    @MockBean
    RepositoryMaintainerDeleter repositoryMaintainerDeleter;

    @MockBean
    PythonRepositoryService pythonRepositoryService;

    @MockBean
    RoleService roleService;

    @MockBean
    UserSettingsService userSettingsService;

    @MockBean
    UserValidator userValidator;

    @MockBean
    SubmissionService submissionService;

    @MockBean
    PythonStrategyFactory pythonStrategyFactory;

    @MockBean
    SubmissionDeleter submissionDeleter;

    @MockBean
    PythonSubmissionDeleter pythonSubmissionDeleter;

    @MockBean
    PythonPackageService pythonPackageService;

    @MockBean
    PythonPackageDeleter pythonPackageDeleter;

    @MockBean
    PythonLocalStorage pythonStorage;

    @MockBean
    PythonRepositoryValidator pythonRepositoryValidator;

    @MockBean
    PythonPackageValidator pythonPackageValidator;

    @MockBean
    PythonRepositoryDeleter pythonRepositoryDeleter;

    @MockBean
    SubmissionDtoConverter submissionDtoConverter;

    @MockBean
    PackageDtoConverter commonPackageDtoConverter;

    @MockBean
    UserSettingsDtoConverter userDtoConverter;

    @MockBean
    UserSettingsValidator userSettingsValidator;

    @MockBean
    SubmissionPatchValidator submissionPatchValidator;

    @MockBean
    StrategyExecutor strategyExecutor;

    @Mock
    protected BestMaintainerChooser bestMaintainerChooser;

    @BeforeEach
    public void clearContext() throws Exception {
        SecurityContextHolder.clearContext();
        Mockito.doAnswer(new Answer<>() {
                    @Override
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                        return ((Strategy<?>) invocationOnMock.getArgument(0)).perform();
                    }
                })
                .when(strategyExecutor)
                .execute(ArgumentMatchers.any());
    }

    public static final String JSON_PATH_COMMON =
            ClassLoader.getSystemClassLoader().getResource("unit/jsonscommon").getPath();

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
}

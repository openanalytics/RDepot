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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.validation.ValidationResult;
import eu.openanalytics.rdepot.python.api.v2.controllers.PythonPackageController;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.test.context.ApiTestConfig;
import eu.openanalytics.rdepot.test.fixture.PythonPackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.PythonRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.PythonSubmissionTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.TestUtils;
import eu.openanalytics.rdepot.test.unit.api.v2.mockstrategies.FailureStrategy;
import eu.openanalytics.rdepot.test.unit.api.v2.mockstrategies.SuccessfulStrategy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@ContextConfiguration(classes = {ApiTestConfig.class})
@WebMvcTest(PythonPackageController.class)
@ActiveProfiles("apiv2deletiondisabled")
@WebAppConfiguration
@Import({ApiTestConfig.class})
public class PythonPackageDeletingDisabledControllerTest extends ApiV2ControllerUnitTest {
    private static final String JSON_PATH = "src/test/resources/unit/jsons";
    private static final String EXAMPLE_PACKAGES_PATH = JSON_PATH + "/example_packages.json";
    private static final String EXAMPLE_DELETED_PACKAGES_PATH = JSON_PATH + "/example_deleted_packages.json";
    private static final String EXAMPLE_PACKAGE_PATH = JSON_PATH + "/example_package.json";
    private static final String EXAMPLE_PACKAGE_NOT_FOUND_PATH = JSON_PATH + "/example_package_notfound.json";
    private static final String EXAMPLE_PACKAGE_PATCHED_PATH = JSON_PATH + "/example_package_patched.json";
    private static final String ERROR_PACKAGE_MALFORMED_PATCH = JSON_PATH + "/error_package_malformed_patch.json";
    private static final String ERROR_PACKAGE_VALIDATION = JSON_PATH + "/error_package_validation.json";
    private static final String EXAMPLE_PACKAGES_REPO_PATH = JSON_PATH + "/example_packages_in_repository.json";
    private static final String ERROR_PACKAGE_DELETING_DISABLED = JSON_PATH + "/error_package_deletion_disabled.json";
    private Optional<User> user;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MessageSource messageSource;

    @Autowired
    PythonPackageController PythonPackageController;

    @BeforeEach
    public void initEach() {
        user = Optional.of(UserTestFixture.GET_ADMIN());
    }

    @Test
    public void getAllPackages_returns401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/python/packages")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    public void getPackage_returns401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/python/packages/" + 123)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser
    public void getAllDeletedPackages_returns403_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/python/packages")
                        .param("deleted", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void getAllPackages() throws Exception {
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();

        when(userService.findActiveByLogin("user")).thenReturn(user);
        when(userService.isAdmin(user.get())).thenReturn(true);
        when(pythonPackageService.findAllBySpecification(any(), any()))
                .thenReturn(PythonPackageTestFixture.GET_EXAMPLE_PACKAGES_PAGED(repository, user.get()));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/python/packages")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_PACKAGES_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void getAllPackages_WhenRepositoryIsSpecified() throws Exception {
        final PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        Page<PythonPackage> packagesPage = PythonPackageTestFixture.GET_EXAMPLE_PACKAGES_PAGED(repository, user.get());

        when(pythonRepositoryService.findByName(repository.getName()))
                .thenReturn(Optional.of(PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY()));
        when(userService.findActiveByLogin("user")).thenReturn(user);

        when(pythonPackageService.findAllBySpecification(any(), any())).thenReturn(packagesPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/python/packages")
                        .param("repositoryName", repository.getName())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_PACKAGES_REPO_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void getAllPackages_OnlyDeleted() throws Exception {
        Page<PythonPackage> packagesPage = PythonPackageTestFixture.GET_EXAMPLE_PACKAGES_PAGED_DELETED();

        when(pythonPackageService.findAllBySpecification(any(), any())).thenReturn(packagesPage);
        when(userService.findActiveByLogin("user")).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/python/packages")
                        .param("deleted", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_DELETED_PACKAGES_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void getPackageById_AsAdmin() throws Exception {
        final Optional<PythonPackage> packageBag = Optional.of(PythonPackageTestFixture.GET_EXAMPLE_PACKAGE());

        when(pythonPackageService.findById(packageBag.get().getId())).thenReturn(packageBag);
        when(userService.findActiveByLogin("user")).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/python/packages/"
                                + packageBag.get().getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_PACKAGE_PATH))));
    }

    // TODO #32882 returns all packages - even deleted, in controller this is also in todo,
    // check when controller will be updated
    @Test
    @WithMockUser(authorities = "user")
    public void getPackageById_AsUser() throws Exception {
        final PythonPackage packageBag = PythonPackageTestFixture.GET_EXAMPLE_PACKAGE();

        when(pythonPackageService.findById(packageBag.getId())).thenReturn(Optional.of(packageBag));
        when(userService.findActiveByLogin("user")).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/python/packages/" + packageBag.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_PACKAGE_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void getPackageById_returns404_WhenPackageIsNotFound() throws Exception {
        final Integer ID = 123;
        when(pythonPackageService.findById(ID)).thenReturn(Optional.ofNullable(null));
        when(userService.findActiveByLogin("user")).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/python/packages/" + ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_PACKAGE_NOT_FOUND_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void deletePackage_returns404_WhenPackageIsNotFound() throws Exception {
        final Integer ID = 123;

        when(pythonPackageService.findById(ID)).thenReturn(Optional.ofNullable(null));
        when(userService.findActiveByLogin("user")).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/python/packages/" + ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_PACKAGE_NOT_FOUND_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void deletePackage_returns403_WhenUserIsNotAdmin() throws Exception {
        final Integer ID = 123;

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/python/packages/" + ID))
                .andExpect(status().isForbidden())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void deletePackage_returns405_whenPackageDeletingIsDisabled() throws Exception {
        final PythonPackage packageBag = PythonPackageTestFixture.GET_EXAMPLE_PACKAGE();
        packageBag.setDeleted(true);

        when(pythonPackageService.findOneDeleted(packageBag.getId())).thenReturn(Optional.of(packageBag));
        when(userService.findActiveByLogin("user")).thenReturn(user);
        doNothing().when(pythonPackageDeleter).delete(packageBag);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/python/packages/" + packageBag.getId()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().json(Files.readString(Path.of(ERROR_PACKAGE_DELETING_DISABLED))));
    }

    @Test
    @WithMockUser(authorities = {"user", "packagemaintainer"})
    public void patchPackage_returns404_WhenPackageIsNotFound() throws Exception {
        final Integer ID = 123;
        final String patchJson = "[{\"op\":\"replace\",\"path\":\"/active\",\"value\":\"false\"}]";

        when(pythonPackageService.findById(ID)).thenReturn(Optional.ofNullable(null));
        when(userService.findActiveByLogin("user")).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/python/packages/" + ID)
                        .content(patchJson)
                        .contentType("application/json-patch+json"))
                .andExpect(status().isNotFound())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_PACKAGE_NOT_FOUND_PATH))));
    }

    @Test
    public void patchPackage_returns401_WhenUserIsNotAuthenticated() throws Exception {
        final String patchJson = "[{\"op\":\"replace\",\"path\":\"/active\",\"value\":\"false\"}]";

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/python/packages/" + 123)
                        .content(patchJson)
                        .contentType("application/json-patch+json"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void patchPackage_returns403_WhenUserIsNotPackageMaintainer() throws Exception {
        final Integer ID = 123;
        final String patchJson = "[{\"op\": \"replace\",\"path\":\"/active\",\"value\":\"false\"}]";

        when(pythonPackageService.findById(ID)).thenReturn(Optional.ofNullable(null));
        when(userService.findActiveByLogin("user")).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/python/packages/" + 123)
                        .content(patchJson)
                        .contentType("application/json-patch+json"))
                .andExpect(status().isForbidden())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "packagemaintainer"})
    public void patchPackage() throws Exception {
        final PythonPackage packageBag = PythonPackageTestFixture.GET_EXAMPLE_PACKAGE();
        packageBag.setActive(false);
        final String patchJson = "[{\"op\":\"replace\",\"path\":\"/active\",\"value\":\"false\"}]";
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        Submission submission = PythonSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user.get(), packageBag);
        Strategy<PythonPackage> strategy = Mockito.spy(new SuccessfulStrategy<PythonPackage>(
                packageBag, newsfeedEventService, pythonPackageService, user.get()));
        when(pythonStrategyFactory.updatePackageStrategy(any(), eq(user.get()), any()))
                .thenReturn(strategy);
        when(userService.findById(anyInt())).thenReturn(user);
        when(pythonRepositoryService.findById(anyInt())).thenReturn(Optional.of(repository));
        when(pythonPackageService.findById(packageBag.getId())).thenReturn(Optional.of(packageBag));
        when(submissionService.findById(anyInt())).thenReturn(Optional.of(submission));
        when(userService.findActiveByLogin("user")).thenReturn(user);
        doNothing().when(pythonPackageValidator).validate(any(), eq(true), any());
        when(securityMediator.isAuthorizedToEdit(packageBag, user.get())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/python/packages/" + packageBag.getId())
                        .content(patchJson)
                        .contentType("application/json-patch+json"))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_PACKAGE_PATCHED_PATH))));

        verify(strategy, times(1)).perform();
        verify(pythonPackageValidator).validate(any(), eq(true), any());
    }

    @Test
    @WithMockUser(authorities = {"user", "packagemaintainer"})
    public void patchPackage_returns405_whenPackageDeletingIsDisabled() throws Exception {
        final PythonPackage packageBag = PythonPackageTestFixture.GET_EXAMPLE_PACKAGE();
        packageBag.setActive(false);
        final String patchJson = "[{\"op\":\"replace\",\"path\":\"/deleted\",\"value\":true}]";
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        Submission submission = PythonSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user.get(), packageBag);
        Strategy<PythonPackage> strategy = Mockito.spy(new SuccessfulStrategy<PythonPackage>(
                packageBag, newsfeedEventService, pythonPackageService, user.get()));
        when(pythonStrategyFactory.updatePackageStrategy(any(), eq(user.get()), any()))
                .thenReturn(strategy);
        when(userService.findById(anyInt())).thenReturn(user);
        when(pythonRepositoryService.findById(anyInt())).thenReturn(Optional.of(repository));
        when(pythonPackageService.findById(packageBag.getId())).thenReturn(Optional.of(packageBag));
        when(submissionService.findById(anyInt())).thenReturn(Optional.of(submission));
        when(userService.findActiveByLogin("user")).thenReturn(user);
        doNothing().when(pythonPackageValidator).validate(any(), eq(true), any());
        when(securityMediator.isAuthorizedToEdit(packageBag, user.get())).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/python/packages/" + packageBag.getId())
                        .content(patchJson)
                        .contentType("application/json-patch+json"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().json(Files.readString(Path.of(ERROR_PACKAGE_DELETING_DISABLED))));

        verify(strategy, times(0)).perform();
    }

    @Test
    @WithMockUser(authorities = {"user", "packagemaintainer"})
    public void patchPackage_returns422_whenPatchIsIncorrect() throws Exception {
        final PythonPackage packageBag = PythonPackageTestFixture.GET_EXAMPLE_PACKAGE();
        final String patchJson = "[{\"op\":\"replace\",\"path\":\"/actiiiiiive\",\"value\":\"false\"}]";

        when(pythonPackageService.findById(packageBag.getId())).thenReturn(Optional.of(packageBag));
        when(userService.findActiveByLogin("user")).thenReturn(user);
        when(securityMediator.isAuthorizedToEdit(packageBag, user.get())).thenReturn(true);
        doNothing().when(pythonPackageValidator).validate(any(), eq(false), any());

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/python/packages/" + packageBag.getId())
                        .contentType("application/json-patch+json")
                        .content(patchJson))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(Files.readString(Path.of(ERROR_PACKAGE_MALFORMED_PATCH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "packagemaintainer"})
    public void patchPackage_returns422_whenValidationFails() throws Exception {
        final PythonPackage packageBag = PythonPackageTestFixture.GET_EXAMPLE_PACKAGE();
        final String patchJson = "[{\"op\": \"replace\",\"path\":\"/name\",\"value\":\"newName\"}]";

        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        Submission submission = PythonSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user.get(), packageBag);
        Strategy<PythonPackage> strategy = Mockito.spy(new SuccessfulStrategy<PythonPackage>(
                packageBag, newsfeedEventService, pythonPackageService, user.get()));

        when(pythonStrategyFactory.updatePackageStrategy(any(), any(), any())).thenReturn(strategy);
        when(pythonRepositoryService.findById(anyInt())).thenReturn(Optional.of(repository));
        when(userService.findById(anyInt())).thenReturn(user);
        when(pythonPackageService.findById(packageBag.getId())).thenReturn(Optional.of(packageBag));
        when(submissionService.findById(anyInt())).thenReturn(Optional.of(submission));
        when(userService.findActiveByLogin("user")).thenReturn(user);
        when(pythonPackageService.findById(packageBag.getId())).thenReturn(Optional.of(packageBag));
        when(userService.findActiveByLogin("user")).thenReturn(user);
        when(repositoryMaintainerValidator.supports(RepositoryMaintainer.class)).thenReturn(true);
        when(securityMediator.isAuthorizedToEdit(packageBag, user.get())).thenReturn(true);

        doAnswer(new Answer<>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        ValidationResult validationResult = invocation.getArgument(2, ValidationResult.class);
                        validationResult.error("MULTIPART-FILE", MessageCodes.INVALID_FILENAME);
                        return null;
                    }
                })
                .when(pythonPackageValidator)
                .validate(any(), eq(true), any());

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/python/packages/" + packageBag.getId())
                        .content(patchJson)
                        .contentType("application/json-patch+json"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(Files.readString(Path.of(ERROR_PACKAGE_VALIDATION))));

        verify(pythonPackageValidator).validate(any(), eq(true), any());
    }

    @Test
    @WithMockUser(authorities = {"user", "packagemaintainer"})
    public void patchPackage_returns500_whenErrorIsThrown() throws Exception {
        final PythonPackage packageBag = PythonPackageTestFixture.GET_EXAMPLE_PACKAGE();
        final String patchJson = "[{\"op\":\"replace\",\"path\":\"/active\",\"value\":\"false\"}]";

        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        Submission submission = PythonSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user.get(), packageBag);
        Strategy<PythonPackage> strategy = Mockito.spy(
                new FailureStrategy<PythonPackage>(packageBag, newsfeedEventService, pythonPackageService, user.get()));

        when(pythonStrategyFactory.updatePackageStrategy(any(), any(), any())).thenReturn(strategy);
        when(pythonRepositoryService.findById(anyInt())).thenReturn(Optional.of(repository));
        when(userService.findById(anyInt())).thenReturn(user);
        when(pythonPackageService.findById(packageBag.getId())).thenReturn(Optional.of(packageBag));
        when(submissionService.findById(anyInt())).thenReturn(Optional.of(submission));

        when(pythonPackageService.findById(packageBag.getId())).thenReturn(Optional.of(packageBag));
        when(userService.findActiveByLogin("user")).thenReturn(user);
        doNothing().when(pythonPackageValidator).validate(any(), eq(true), any());
        when(securityMediator.isAuthorizedToEdit(packageBag, user.get())).thenReturn(true);

        ResultActions result = mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/v2/manager/python/packages/" + packageBag.getId())
                                .content(patchJson)
                                .contentType("application/json-patch+json"))
                .andExpect(status().isInternalServerError());
        TestUtils.matchInternalServerErrorPatch(result);

        verify(strategy, times(1)).perform();
        verify(pythonPackageValidator).validate(any(), eq(true), any());
    }
}

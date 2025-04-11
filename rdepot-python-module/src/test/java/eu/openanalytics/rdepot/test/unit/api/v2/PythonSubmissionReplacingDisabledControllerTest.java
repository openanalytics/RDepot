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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageUploadRequest;
import eu.openanalytics.rdepot.base.api.v2.dtos.SubmissionDto;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.time.DateProvider;
import eu.openanalytics.rdepot.base.validation.ValidationResult;
import eu.openanalytics.rdepot.python.api.v2.controllers.PythonSubmissionController;
import eu.openanalytics.rdepot.python.api.v2.dtos.PythonPackageDto;
import eu.openanalytics.rdepot.python.api.v2.hateoas.PythonSubmissionModelAssembler;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.test.context.ApiTestConfig;
import eu.openanalytics.rdepot.test.fixture.PythonPackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.PythonRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.PythonSubmissionTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.TestUtils;
import eu.openanalytics.rdepot.test.unit.api.v2.mockstrategies.DuplicatePackageStrategy;
import eu.openanalytics.rdepot.test.unit.api.v2.mockstrategies.FailureStrategy;
import eu.openanalytics.rdepot.test.unit.api.v2.mockstrategies.SuccessfulStrategy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

@ContextConfiguration(classes = {ApiTestConfig.class})
@WebMvcTest(PythonSubmissionController.class)
@ActiveProfiles("apiv2replacedisabled")
@WebAppConfiguration
@Import({ApiTestConfig.class})
public class PythonSubmissionReplacingDisabledControllerTest extends ApiV2ControllerUnitTest {

    private static final String JSON_PATH = "src/test/resources/unit/jsons";
    private static final String TEST_PACKAGE_PATH =
            "src/test/resources/unit/test_packages/strategy_tests/coconutpy-2.2.1.tar.gz";
    private static final String EXAMPLE_SUBMISSION_CREATED_PATH = JSON_PATH + "/example_submission_created.json";
    private static final String ERROR_SUBMISSION_DUPLICATE_PATH = JSON_PATH + "/error_submission_duplicate.json";
    private static final String WARNING_SUBMISSION_DUPLICATE_PATH = JSON_PATH + "/warning_submission_duplicate.json";
    private static final String WARNING_PACKAGE_REPLACE_DISABLED_PATH =
            JSON_PATH + "/warning_package_replace_disabled.json";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MessageSource messageSource;

    @Autowired
    PythonSubmissionModelAssembler pythonSubmissionModelAssembler;

    Locale locale = Locale.ENGLISH;

    @Autowired
    WebApplicationContext webApplicationContext;

    private User user;

    @BeforeEach
    public void initEach() {
        user = UserTestFixture.GET_ADMIN();
        DateProvider.setTestDate(LocalDateTime.of(2024, 4, 12, 0, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    @Test
    @WithMockUser(authorities = "user")
    public void submitPackage() throws Exception {
        final PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        final byte[] packageFile = Files.readAllBytes(Path.of(TEST_PACKAGE_PATH));
        final MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "coconutpy-2.2.1.tar.gz", ContentType.MULTIPART_FORM_DATA.toString(), packageFile);
        final boolean replace = false;

        final Submission submission =
                PythonPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user).getSubmission();
        submission.setState(SubmissionState.WAITING);
        Strategy<Submission> strategy =
                Mockito.spy(new SuccessfulStrategy<>(submission, newsfeedEventService, submissionService, user));
        final PackageDto packageDto = PythonPackageTestFixture.GET_EXAMPLE_PACKAGE_DTO(submission.getPackageBag());
        final SubmissionDto submissionDto =
                PythonSubmissionTestFixture.GET_FIXTURE_SUBMISSION_DTO(submission, submission.getPackageBag());

        when(userService.findActiveByLogin("user")).thenReturn(Optional.ofNullable(user));
        when(pythonRepositoryService.findByNameAndDeleted(any(String.class), eq(false)))
                .thenReturn(Optional.of(repository));
        doNothing().when(pythonPackageValidator).validate(any(), any(ValidationResult.class));
        when(commonPackageDtoConverter.convertEntityToDto(any())).thenReturn(packageDto);
        when(submissionDtoConverter.convertEntityToDto(submission)).thenReturn(submissionDto);
        when(pythonStrategyFactory.uploadPackageStrategy(any(), any()))
                .thenAnswer((Answer<Strategy<Submission>>) invocation -> {
                    PackageUploadRequest<PythonRepository> request = invocation.getArgument(0);
                    assertEquals(packageFile, request.getFileData().getBytes());
                    assertEquals(repository.getName(), request.getRepository().getName());
                    assertEquals(replace, request.isReplace());
                    return strategy;
                });

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v2/manager/python/submissions")
                        .file(multipartFile)
                        .param("repository", repository.getName())
                        .param("replace", Boolean.toString(replace)))
                .andExpect(status().isCreated())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_SUBMISSION_CREATED_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void submitPackage_returns200_whenPackageIsDuplicate() throws Exception {
        final PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        final byte[] packageFile = Files.readAllBytes(Path.of(TEST_PACKAGE_PATH));
        final MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "coconutpy-2.2.1.tar.gz", ContentType.MULTIPART_FORM_DATA.toString(), packageFile);
        final boolean replace = false;

        final PythonPackage packageBag = PythonPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        final Submission submission = packageBag.getSubmission();
        submission.setState(SubmissionState.WAITING);
        Strategy<Submission> strategy =
                Mockito.spy(new DuplicatePackageStrategy(submission, submissionService, user, newsfeedEventService));

        final SubmissionDto submissionDto =
                PythonSubmissionTestFixture.GET_FIXTURE_SUBMISSION_DTO(submission, submission.getPackageBag());
        final PythonPackageDto packageDto = new PythonPackageDto(packageBag);

        doReturn(packageDto).when(commonPackageDtoConverter).convertEntityToDto(packageBag);
        doReturn(submissionDto).when(submissionDtoConverter).convertEntityToDto(submission);
        when(userService.findActiveByLogin("user")).thenReturn(Optional.ofNullable(user));
        when(pythonStrategyFactory.uploadPackageStrategy(any(), any())).thenReturn(strategy);
        when(pythonRepositoryService.findByNameAndDeleted(any(String.class), eq(false)))
                .thenReturn(Optional.of(repository));
        doNothing().when(pythonPackageValidator).validate(any(MultipartFile.class), any());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v2/manager/python/submissions")
                        .file(multipartFile)
                        .param("repository", repository.getName())
                        .param("replace", Boolean.toString(replace)))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(WARNING_SUBMISSION_DUPLICATE_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void submitPackage_returns200_whenReplacingPackagesIsDisabled() throws Exception {
        final PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        final byte[] packageFile = Files.readAllBytes(Path.of(TEST_PACKAGE_PATH));
        final MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "coconutpy-2.2.1.tar.gz", ContentType.MULTIPART_FORM_DATA.toString(), packageFile);
        final boolean replace = true;

        final PythonPackage packageBag = PythonPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        final Submission submission = packageBag.getSubmission();
        submission.setState(SubmissionState.WAITING);
        Strategy<Submission> strategy =
                Mockito.spy(new DuplicatePackageStrategy(submission, submissionService, user, newsfeedEventService));

        final SubmissionDto submissionDto =
                PythonSubmissionTestFixture.GET_FIXTURE_SUBMISSION_DTO(submission, submission.getPackageBag());
        final PythonPackageDto packageDto = new PythonPackageDto(packageBag);

        doReturn(packageDto).when(commonPackageDtoConverter).convertEntityToDto(packageBag);
        doReturn(submissionDto).when(submissionDtoConverter).convertEntityToDto(submission);
        when(userService.findActiveByLogin("user")).thenReturn(Optional.ofNullable(user));
        when(pythonStrategyFactory.uploadPackageStrategy(any(), any())).thenReturn(strategy);
        when(pythonRepositoryService.findByNameAndDeleted(any(String.class), eq(false)))
                .thenReturn(Optional.of(repository));
        doNothing().when(pythonPackageValidator).validate(any(MultipartFile.class), any());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v2/manager/python/submissions")
                        .file(multipartFile)
                        .param("repository", repository.getName())
                        .param("replace", Boolean.toString(replace)))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(WARNING_PACKAGE_REPLACE_DISABLED_PATH))));
    }

    @Test
    public void submitPackage_returns401_whenUserIsNotAuthenticated() throws Exception {
        final byte[] packageFile = Files.readAllBytes(Path.of(TEST_PACKAGE_PATH));
        final MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "abc_1.3.tar.gz", ContentType.MULTIPART_FORM_DATA.toString(), packageFile);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v2/manager/python/submissions")
                        .file(multipartFile)
                        .param("repository", "testttt")
                        .param("replace", "false"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void submitPackage_returns422_whenRequestIsIncorrect() throws Exception {
        final String REPOSITORY_NAME = "testtt";
        final PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        final byte[] packageFile = Files.readAllBytes(Path.of(TEST_PACKAGE_PATH));
        final MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "abc_1.3.tar.gz", ContentType.MULTIPART_FORM_DATA.toString(), packageFile);
        final boolean replace = false;
        final Submission submission =
                PythonPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user).getSubmission();
        Strategy<Submission> strategy =
                Mockito.spy(new SuccessfulStrategy<>(submission, newsfeedEventService, submissionService, user));

        when(pythonRepositoryService.findByNameAndDeleted(REPOSITORY_NAME, false))
                .thenReturn(Optional.of(repository));
        when(userService.findActiveByLogin("user")).thenReturn(Optional.ofNullable(user));
        doAnswer((Answer<Object>) invocation -> {
                    ValidationResult validationResult = invocation.getArgument(1, ValidationResult.class);
                    validationResult.error("MULTIPART-FILE", MessageCodes.INVALID_FILENAME);
                    return null;
                })
                .when(pythonPackageValidator)
                .validate(any(MultipartFile.class), any());
        when(pythonStrategyFactory.uploadPackageStrategy(any(), eq(user))).thenReturn(strategy);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v2/manager/python/submissions")
                        .file(multipartFile)
                        .param("repository", REPOSITORY_NAME)
                        .param("replace", Boolean.toString(replace)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(Files.readString(Path.of(ERROR_SUBMISSION_DUPLICATE_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void submitPackage_returns500_whenCreationFails() throws Exception {
        final PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        final byte[] packageFile = Files.readAllBytes(Path.of(TEST_PACKAGE_PATH));
        final MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "abc_1.3.tar.gz", ContentType.MULTIPART_FORM_DATA.toString(), packageFile);
        final boolean replace = false;
        final Submission submission =
                PythonPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user).getSubmission();
        Strategy<Submission> strategy =
                Mockito.spy(new FailureStrategy<>(submission, newsfeedEventService, submissionService, user));

        when(pythonStrategyFactory.uploadPackageStrategy(any(), eq(user))).thenReturn(strategy);
        when(userService.findActiveByLogin("user")).thenReturn(Optional.ofNullable(user));
        when(pythonRepositoryService.findByNameAndDeleted(any(String.class), eq(false)))
                .thenReturn(Optional.of(repository));

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v2/manager/python/submissions")
                        .file(multipartFile)
                        .param("repository", repository.getName())
                        .param("replace", Boolean.toString(replace)))
                .andExpect(status().isInternalServerError());
        TestUtils.matchInternalServerErrorCreate(result);
    }
}

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.naming.OperationNotSupportedException;

import org.apache.http.entity.ContentType;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageUploadRequest;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.messaging.RefactoredMessageCodes;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.validation.exceptions.MultipartFileValidationException;
import eu.openanalytics.rdepot.r.api.v2.controllers.RSubmissionController;
import eu.openanalytics.rdepot.r.api.v2.hateoas.RSubmissionModelAssembler;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.test.context.ApiTestConfig;
import eu.openanalytics.rdepot.test.fixture.RPackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.api.v2.mockstrategies.FailureStrategy;
import eu.openanalytics.rdepot.test.unit.api.v2.mockstrategies.SuccessfulStrategy;

@ContextConfiguration(classes = { ApiTestConfig.class })
@WebMvcTest(RSubmissionController.class)
@ActiveProfiles("apiv2")
@WebAppConfiguration
@Import({ ApiTestConfig.class })
public class RSubmissionControllerTest extends ApiV2ControllerUnitTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	MessageSource messageSource;

	@Autowired
	RSubmissionModelAssembler rSubmissionModelAssembler;

	Locale locale = Locale.ENGLISH;

	@Autowired
	WebApplicationContext webApplicationContext;

	private Optional<User> user;

	private static final String JSON_PATH = "src/test/resources/unit/jsons";
	private static final String TEST_PACKAGE_PATH = "src/test/resources/unit/test_packages/abc_1.3.tar.gz";
	private static final String EXAMPLE_SUBMISSION_CREATED_PATH = JSON_PATH + "/example_submission_created.json";
	private static final String ERROR_SUBMISSION_NOT_FOUND_PATH = JSON_PATH + "/error_submission_notfound.json";
	private static final String EXAMPLE_SUBMISSIONS_PATH = JSON_PATH + "/example_submissions.json";
	private static final String EXAMPLE_SUBMISSION_PATCHED_PATH = JSON_PATH + "/example_submission_patched.json";
	private static final String EXAMPLE_SUBMISSION_PATH = JSON_PATH + "/example_submission.json";
	private static final String ERROR_SUBMISSION_DUPLICATE_PATH = JSON_PATH + "/error_submission_duplicate.json";
	private static final String ERROR_UPDATE_NOT_ALLOWED_SUBMISSION_PATH = JSON_PATH + "/error_update_notallowed_submission.json";

	@BeforeEach
	public void initEach() {
		user = UserTestFixture.GET_FIXTURE_ADMIN();
	}

	@Test
	@WithMockUser(authorities = "user")
	public void submitPackage() throws Exception {
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final byte[] packageFile = Files.readAllBytes(Path.of(TEST_PACKAGE_PATH));
		final MockMultipartFile multipartFile = new MockMultipartFile("file", "abc_1.3.tar.gz",
				ContentType.MULTIPART_FORM_DATA.toString(), packageFile);
		final Boolean generateManuals = true;
		final Boolean replace = false;

		final Submission submission = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user.get()).getSubmission();
		submission.setState(SubmissionState.WAITING);
		Strategy<Submission> strategy = Mockito.spy(
				new SuccessfulStrategy<Submission>(submission, newsfeedEventService, submissionService, user.get()));

		when(userService.findByLogin("user")).thenReturn(user);
		when(rRepositoryService.findByNameAndDeleted(any(String.class), eq(false))).thenReturn(Optional.of(repository));
		doNothing().when(rPackageValidator).validate(any());
		when(rStrategyFactory.uploadPackageStrategy(any(), any())).thenAnswer(new Answer<Strategy<Submission>>() {

			@Override
			public Strategy<Submission> answer(InvocationOnMock invocation) throws Throwable {
				PackageUploadRequest request = invocation.getArgument(0);
				assertEquals(packageFile, request.getFileData().getBytes());
				assertEquals(repository.getName(), request.getRepository().getName());
				assertEquals(replace, request.getReplace());
				assertEquals(generateManuals, request.getGenerateManual());
				return strategy;
			}

		});

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v2/manager/r/submissions").file(multipartFile)
				.param("repository", repository.getName()).param("generateManual", generateManuals.toString())
				.param("replace", replace.toString())).andExpect(status().isCreated())
				.andExpect(content().json(Files.readString(Path.of(EXAMPLE_SUBMISSION_CREATED_PATH))));
	}

	@Test
	@WithMockUser(authorities = "user")
	public void submitPackage_returns422_whenPackageIsDuplicate() throws Exception {
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final byte[] packageFile = Files.readAllBytes(Path.of(TEST_PACKAGE_PATH));
		final MockMultipartFile multipartFile = new MockMultipartFile("file", "abc_1.3.tar.gz",
				ContentType.MULTIPART_FORM_DATA.toString(), packageFile);
		final Boolean generateManuals = true;
		final Boolean replace = false;
		final Submission submission = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user.get()).getSubmission();
		submission.setState(SubmissionState.WAITING);
		Strategy<Submission> strategy = Mockito.spy(
				new SuccessfulStrategy<Submission>(submission, newsfeedEventService, submissionService, user.get()));

		when(userService.findByLogin("user")).thenReturn(user);
		when(rStrategyFactory.uploadPackageStrategy(any(), any())).thenReturn(strategy);
		when(rRepositoryService.findByNameAndDeleted(any(String.class), eq(false))).thenReturn(Optional.of(repository));
		doThrow(new MultipartFileValidationException(RefactoredMessageCodes.INVALID_FILENAME)).when(rPackageValidator).validate(any());

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v2/manager/r/submissions").file(multipartFile)
				.param("repository", repository.getName()).param("generateManual", generateManuals.toString())
				.param("replace", replace.toString())).andExpect(status().isUnprocessableEntity())
				.andExpect(content().json(Files.readString(Path.of(ERROR_SUBMISSION_DUPLICATE_PATH))));
	}

	@Test
	public void submitPackage_returns401_whenUserIsNotAuthenticated() throws Exception {
		final byte[] packageFile = Files.readAllBytes(Path.of(TEST_PACKAGE_PATH));
		final MockMultipartFile multipartFile = new MockMultipartFile("file", "abc_1.3.tar.gz",
				ContentType.MULTIPART_FORM_DATA.toString(), packageFile);

		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v2/manager/r/submissions").file(multipartFile)
				.param("repository", "testttt").param("generateManual", "true").param("replace", "false"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}

	@Test
	@WithMockUser(authorities = "user")
	public void submitPackage_returns422_whenRequestIsIncorrect() throws Exception {
		final String REPOSITORY_NAME = "testtt";
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final byte[] packageFile = Files.readAllBytes(Path.of(TEST_PACKAGE_PATH));
		final MockMultipartFile multipartFile = new MockMultipartFile("file", "abc_1.3.tar.gz",
				ContentType.MULTIPART_FORM_DATA.toString(), packageFile);
		final Boolean generateManuals = true;
		final Boolean replace = false;
		final Submission submission = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user.get()).getSubmission();
		Strategy<Submission> strategy = Mockito.spy(
				new SuccessfulStrategy<Submission>(submission, newsfeedEventService, submissionService, user.get()));
			
		when(rRepositoryService.findByNameAndDeleted(REPOSITORY_NAME, false)).thenReturn(Optional.of(repository));
		when(userService.findByLogin("user")).thenReturn(user);
		doThrow(new MultipartFileValidationException(RefactoredMessageCodes.INVALID_FILENAME)).when(rPackageValidator).validate(any());
		when(rStrategyFactory.uploadPackageStrategy(any(), eq(user.get()))).thenReturn(strategy);
		
		mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v2/manager/r/submissions").file(multipartFile)
				.param("repository", REPOSITORY_NAME).param("generateManual", generateManuals.toString())
				.param("replace", replace.toString())).andExpect(status().isUnprocessableEntity())
				.andExpect(content().json(Files.readString(Path.of(ERROR_SUBMISSION_DUPLICATE_PATH))));
	}

	@Test
	@WithMockUser(authorities = {"user", "admin"})
	public void submitPackage_returns500_whenCreationFails() throws Exception {
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final byte[] packageFile = Files.readAllBytes(Path.of(TEST_PACKAGE_PATH));
		final MockMultipartFile multipartFile = 
				new MockMultipartFile("file", "abc_1.3.tar.gz", 
						ContentType.MULTIPART_FORM_DATA.toString(), 
						packageFile);
		final Boolean generateManuals = true;
		final Boolean replace = false;
		final Submission submission = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user.get()).getSubmission();
		Strategy<Submission> strategy = Mockito.spy(
				new FailureStrategy<Submission>(submission, newsfeedEventService, submissionService, user.get()));
		
		when(rStrategyFactory.uploadPackageStrategy(any(), eq(user.get()))).thenReturn(strategy);
		when(userService.findByLogin("user")).thenReturn(user);
		when(rRepositoryService.findByNameAndDeleted(any(String.class), eq(false))).thenReturn(Optional.of(repository));
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.multipart("/api/v2/manager/r/submissions")
					.file(multipartFile)
					.param("repository", repository.getName())
					.param("generateManual", generateManuals.toString())
					.param("replace", replace.toString()))
			.andExpect(
					status().isInternalServerError())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_CREATE_PATH))));
	}
	
	@Test
	public void getAllSubmissions_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/v2/manager/r/submissions").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}

	@Test
	public void getSubmission_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc.perform(
				MockMvcRequestBuilders.get("/api/v2/manager/r/submissions/123").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}

	@Test
	public void patchSubmission_returns401_whenUserIsNotAuthenticated() throws Exception {
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/state\",\"value\":\"ACCEPTED\"}]";

		mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/submissions/" + 123)
				.contentType("application/json-patch+json").content(patchJson)).andExpect(status().isUnauthorized())
				.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}

	@Test
	public void deleteSubmission_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/r/submissions/" + 123))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(authorities = "user")
	public void deleteSubmission_returns403_whenUserIsNotAuthorized() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/r/submissions/" + 123))
				.andExpect(status().isForbidden())
				.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}

	@Test
	@WithMockUser(authorities = { "user", "admin" })
	public void deleteSubmission_returns404_whenSubmissionIsNotFound() throws Exception {

		when(userService.findByLogin("user")).thenReturn(user);
		when(submissionService.findById(any(Integer.class))).thenReturn(Optional.ofNullable(null));

		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/r/submissions/" + 123))
				.andExpect(status().isNotFound())
				.andExpect(content().json(Files.readString(Path.of(ERROR_SUBMISSION_NOT_FOUND_PATH))));
	}

	@Test
	@WithMockUser(authorities = { "user", "admin" })
	public void getSubmission_returns404_whenSubmissionIsNotFound() throws Exception {

		when(userService.findByLogin("user")).thenReturn(user);
		when(submissionService.findById(any(Integer.class))).thenReturn(Optional.ofNullable(null));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/r/submissions/" + 123))
				.andExpect(status().isNotFound())
				.andExpect(content().json(Files.readString(Path.of(ERROR_SUBMISSION_NOT_FOUND_PATH))));
	}

	@Test
	@WithMockUser(authorities = "user")
	public void patchSubmission_returns404_whenSubmissionIsNotFound() throws Exception {
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/state\",\"value\":\"ACCEPTED\"}]";

		when(userService.findByLogin("user")).thenReturn(user);
		when(submissionService.findById(any(Integer.class))).thenReturn(Optional.ofNullable(null));

		mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/submissions/" + 123)
				.contentType("application/json-patch+json").content(patchJson)).andExpect(status().isNotFound())
				.andExpect(content().json(Files.readString(Path.of(ERROR_SUBMISSION_NOT_FOUND_PATH))));
	}

	@Test
	@WithMockUser
	public void patchSubmission_returns403_whenUserIsNotAuthorized() throws Exception {
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/state\",\"value\":\"ACCEPTED\"}]";
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final Submission submission = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user.get()).getSubmission();

		when(userService.findByLogin("user")).thenReturn(user);
		when(securityMediator.isAuthorizedToEdit(eq(submission), any(), eq(user.get()))).thenReturn(false);
		when(submissionService.findById(submission.getId())).thenReturn(Optional.of(submission));

		mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/submissions/" + submission.getId())
				.contentType("application/json-patch+json").content(patchJson)).andExpect(status().isForbidden())
				.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}

	@Test
	@WithMockUser(authorities = "user")
	public void patchSubmission_returns500_whenInternalServerErrorOccurs() throws Exception {
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/state\",\"value\":\"ACCEPTED\"}]";
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final Submission submission = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user.get()).getSubmission();
		final RPackage packageBag = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user.get());

		Strategy<Submission> strategy = Mockito.spy(
				new FailureStrategy<Submission>(submission, newsfeedEventService, submissionService, user.get()));
		
		when(userService.findByLogin("user")).thenReturn(user);
		when(userService.findById(any(Integer.class))).thenReturn(user);
		when(rPackageService.findById(any(Integer.class))).thenReturn(Optional.of(packageBag));
		when(rRepositoryService.findById(any(Integer.class))).thenReturn(Optional.of(repository));
		when(securityMediator.isAuthorizedToEdit(eq(submission), any(), eq(user.get()))).thenReturn(true);
		when(submissionService.findById(any(Integer.class))).thenReturn(Optional.of(submission));
		when(rStrategyFactory.updateSubmissionStrategy(eq(submission), any(), eq(repository), eq(user.get())))
				.thenReturn(strategy);
		
		mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/submissions/" + submission.getId())
				.content(patchJson).contentType("application/json-patch+json"))
				.andExpect(status().isInternalServerError())
				.andExpect(content().json(Files.readString(Path.of(ERROR_PATCH_PATH))));
		
		verify(strategy, times(1)).perform();
	}

	@Test
	@WithMockUser(authorities = { "user", "admin" })
	public void deleteSubmission() throws Exception {
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final Submission submission = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user.get()).getSubmission();

		when(userService.findByLogin("user")).thenReturn(user);
		when(submissionService.findById(submission.getId())).thenReturn(Optional.of(submission));
		doNothing().when(submissionDeleter).delete(submission);

		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/r/submissions/" + submission.getId()))
				.andExpect(status().isNoContent());
	}

	@Test
	@WithMockUser(authorities = {"user", "admin"})
	public void getAllSubmissions_asMaintainer() throws Exception {
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();

		List<Submission> submissions = RPackageTestFixture.GET_FIXTURE_PACKAGES(repository, user.get(), 3, 100).stream()
				.map(p -> p.getSubmission()).collect(Collectors.toList());
		Page<Submission> paged = new PageImpl<Submission>(submissions);
		submissions.get(0).setState(SubmissionState.WAITING);
		submissions.get(1).setState(SubmissionState.WAITING);
		submissions.get(2).setState(SubmissionState.WAITING);
		
		when(submissionService.findAllBySpecification(any(Specification.class), any(Pageable.class))).thenReturn(paged);
		when(userService.findByLogin("user")).thenReturn(user);
		when(userService.findById(any(Integer.class))).thenReturn(user);
		when(userService.findById(user.get().getId())).thenReturn(user);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/r/submissions").param("deleted", "false")
				.param("state", "WAITING").param("userId", Integer.toString(user.get().getId()))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().json(Files.readString(Path.of(EXAMPLE_SUBMISSIONS_PATH))));
	}

	@Test
	@WithMockUser(authorities = "user")
	public void patchSubmission() throws Exception {
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/state\",\"value\":\"ACCEPTED\"}]";
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final Submission submission = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user.get()).getSubmission();
		final RPackage rPackage = (RPackage) submission.getPackage();
		
		Strategy<Submission> strategy = new SuccessfulStrategy<Submission>(submission, newsfeedEventService,
				submissionService, user.get());

		when(userService.findByLogin("user")).thenReturn(user);
		when(userService.findById(any(Integer.class))).thenReturn(user);
		when(securityMediator.isAuthorizedToEdit(eq(submission), any(), eq(user.get()))).thenReturn(true);
		when(submissionService.findById(submission.getId())).thenReturn(Optional.of(submission));
		when(submissionService.findById(any(Integer.class))).thenReturn(Optional.of(submission));
		when(rPackageService.findById(0)).thenReturn(Optional.of(rPackage));
		when(rRepositoryService.findById(any(Integer.class))).thenReturn(Optional.of(repository));
		when(rStrategyFactory.updateSubmissionStrategy(eq(submission), any(), eq(repository),
		eq(user.get()))).thenAnswer(new Answer<Strategy<Submission>>() {

			@Override
			public Strategy<Submission> answer(InvocationOnMock invocation) throws Throwable {
				Submission entity = invocation.getArgument(0);
				assertEquals(SubmissionState.ACCEPTED, entity.getState());
				return strategy;
			}

		});

		mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/submissions/" + submission.getId())
				.content(patchJson).contentType("application/json-patch+json")).andExpect(status().isOk())
				.andExpect(content().json(Files.readString(Path.of(EXAMPLE_SUBMISSION_PATCHED_PATH))));
	}

	@Test
	@WithMockUser(authorities = "user")
	public void patchSubmission_returns500_whenTryingToAcceptedCancelledSubmission() throws Exception {
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/state\",\"value\":\"ACCEPTED\"}]";
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final Submission submission = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user.get()).getSubmission();
		final RPackage packageBag = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user.get());
		submission.setState(SubmissionState.CANCELLED);
		Strategy<Submission> strategy =Mockito.spy(new SuccessfulStrategy<Submission>(submission, newsfeedEventService,
				submissionService, user.get()));

		when(userService.findByLogin("user")).thenReturn(user);
		when(userService.findById(any(Integer.class))).thenReturn(user);
		when(securityMediator.isAuthorizedToEdit(eq(submission), any(), eq(user.get()))).thenReturn(true);
		when(submissionService.findById(any(Integer.class))).thenReturn(Optional.of(submission));
		when(rRepositoryService.findById(any(Integer.class))).thenReturn(Optional.of(repository));
		when(rStrategyFactory.updateSubmissionStrategy(any(Submission.class), any(Submission.class), eq(repository),
				eq(user.get()))).thenReturn(strategy);
		when(rPackageService.findById(any(Integer.class))).thenReturn(Optional.of(packageBag));
		
		doThrow(new StrategyFailure(new OperationNotSupportedException())).when(strategy).perform();

		mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/submissions/" + submission.getId())
				.content(patchJson).contentType("application/json-patch+json"))
				.andExpect(status().isInternalServerError())
				.andExpect(content().json(Files.readString(Path.of(ERROR_UPDATE_NOT_ALLOWED_SUBMISSION_PATH))));
	}

	@Test
	@WithMockUser
	public void getSubmission_returns403_whenUserIsNotAuthorized() throws Exception {
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final Submission submission = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user.get()).getSubmission();

		when(userService.findByLogin("user")).thenReturn(user);
		when(submissionService.findById(submission.getId())).thenReturn(Optional.of(submission));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/r/submissions/" + submission.getId())
				.contentType("application/json-patch+json")).andExpect(status().isForbidden())
				.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}

	@Test
	@WithMockUser(authorities = "user")
	public void getSubmission() throws Exception {
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final Submission submission = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user.get()).getSubmission();
		submission.setState(SubmissionState.WAITING);
		when(userService.findByLogin("user")).thenReturn(user);
		when(submissionService.findById(submission.getId())).thenReturn(Optional.of(submission));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/r/submissions/" + submission.getId())
				.contentType("application/json-patch+json")).andExpect(status().isOk())
				.andExpect(content().json(Files.readString(Path.of(EXAMPLE_SUBMISSION_PATH))));
	}
}
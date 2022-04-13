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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.api.v2.controller.RSubmissionController;
import eu.openanalytics.rdepot.api.v2.dto.RSubmissionDto;
import eu.openanalytics.rdepot.api.v2.dto.SubmissionState;
import eu.openanalytics.rdepot.exception.SubmissionCreateException;
import eu.openanalytics.rdepot.exception.SubmissionEditException;
import eu.openanalytics.rdepot.exception.UpdateNotAllowedException;
import eu.openanalytics.rdepot.exception.UploadRequestValidationException;
import eu.openanalytics.rdepot.model.PackageUploadRequest;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.test.context.TestConfig;
import eu.openanalytics.rdepot.test.fixture.RPackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.warning.UploadRequestValidationWarning;

@ContextConfiguration(classes = {TestConfig.class})
@WebMvcTest(RSubmissionController.class)
@ActiveProfiles("apiv2")
@WebAppConfiguration
@Import({TestConfig.class})
public class RSubmissionControllerTest extends ApiV2ControllerUnitTest {
	
	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	MessageSource messageSource;
	
	Locale locale = Locale.ENGLISH;

	@Autowired
	WebApplicationContext webApplicationContext;
	
	private static final String JSON_PATH = "src/test/resources/unit/jsons";
	private static final String TEST_PACKAGE_PATH = "src/test/resources/unit/test_packages/abc_1.3.tar.gz";
	private static final String EXAMPLE_SUBMISSION_CREATED_PATH = JSON_PATH + "/example_submission_created.json";
	private static final String ERROR_SUBMISSION_INVALID_PATH = JSON_PATH + "/error_submission_invalid.json";
	private static final String ERROR_SUBMISSION_NOT_FOUND_PATH = JSON_PATH + "/error_submission_notfound.json";
	private static final String EXAMPLE_SUBMISSIONS_PATH = JSON_PATH + "/example_submissions.json";
	private static final String EXAMPLE_SUBMISSION_PATCHED_PATH = JSON_PATH + "/example_submission_patched.json";
	private static final String EXAMPLE_SUBMISSION_PATH = JSON_PATH + "/example_submission.json";
	private static final String ERROR_SUBMISSION_DUPLICATE_PATH = JSON_PATH + "/error_submission_duplicate.json";
	
	@Test
	public void submitPackage() throws Exception {
		final User user = getUserAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Repository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final byte[] packageFile = Files.readAllBytes(Path.of(TEST_PACKAGE_PATH));
		final MockMultipartFile multipartFile = 
				new MockMultipartFile("file", "abc_1.3.tar.gz", 
						ContentType.MULTIPART_FORM_DATA.toString(), 
						packageFile);
		final Boolean generateManuals = true;
		final Boolean replace = false;
		
		when(userService.findByLogin(user.getLogin())).thenReturn(user);
		when(submissionService.create(any(), eq(user))).thenAnswer(new Answer<Submission>() {

			@Override
			public Submission answer(InvocationOnMock invocation) throws Throwable {
				PackageUploadRequest request = invocation.getArgument(0);
				
				assertEquals(packageFile, request.getFileData().getBytes());
				assertEquals(repository.getName(), request.getRepository());
				assertEquals(replace, request.getReplace());
				assertEquals(generateManuals, request.getGenerateManual());
				return RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user).getSubmission();
			}
			
		});
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.multipart("/api/v2/manager/r/submissions")
					.file(multipartFile)
					.principal(principal)
					.param("repository", repository.getName())
					.param("generateManual", generateManuals.toString())
					.param("replace", replace.toString()))
			.andExpect(
					status().isCreated())
			.andExpect(
					content().json(Files.readString(Path.of(EXAMPLE_SUBMISSION_CREATED_PATH))));
	}
	
	@Test
	public void submitPackage_returns422_whenPackageIsDuplicate() throws Exception {
		final User user = getUserAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Repository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final byte[] packageFile = Files.readAllBytes(Path.of(TEST_PACKAGE_PATH));
		final MockMultipartFile multipartFile = 
				new MockMultipartFile("file", "abc_1.3.tar.gz", 
						ContentType.MULTIPART_FORM_DATA.toString(), 
						packageFile);
		final Boolean generateManuals = true;
		final Boolean replace = false;
		
		when(userService.findByLogin(user.getLogin())).thenReturn(user);
		when(submissionService.create(any(), eq(user)))
			.thenThrow(new UploadRequestValidationWarning("Package duplicate."));
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.multipart("/api/v2/manager/r/submissions")
					.file(multipartFile)
					.principal(principal)
					.param("repository", repository.getName())
					.param("generateManual", generateManuals.toString())
					.param("replace", replace.toString()))
			.andExpect(
					status().isUnprocessableEntity())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_SUBMISSION_DUPLICATE_PATH))));
	}
	
	@Test
	public void submitPackage_returns401_whenUserIsNotAuthenticated() throws Exception {
		final byte[] packageFile = Files.readAllBytes(Path.of(TEST_PACKAGE_PATH));
		final MockMultipartFile multipartFile = 
				new MockMultipartFile("file", "abc_1.3.tar.gz", 
						ContentType.MULTIPART_FORM_DATA.toString(), 
						packageFile);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.multipart("/api/v2/manager/r/submissions")
					.file(multipartFile)
					.param("repository", "testttt")
					.param("generateManual", "true")
					.param("replace", "false"))
			.andExpect(
					status().isUnauthorized())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void submitPackage_returns422_whenRequestIsIncorrect() throws Exception {
		final User user = getUserAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final String REPOSITORY_NAME = "testtt";
		final byte[] packageFile = Files.readAllBytes(Path.of(TEST_PACKAGE_PATH));
		final MockMultipartFile multipartFile = 
				new MockMultipartFile("file", "abc_1.3.tar.gz", 
						ContentType.MULTIPART_FORM_DATA.toString(), 
						packageFile);
		final Boolean generateManuals = true;
		final Boolean replace = false;
		
		when(userService.findByLogin(user.getLogin())).thenReturn(user);
		when(submissionService.create(any(), eq(user)))
			.thenThrow(new UploadRequestValidationException("Repository not found."));
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.multipart("/api/v2/manager/r/submissions")
					.file(multipartFile)
					.principal(principal)
					.param("repository", REPOSITORY_NAME)
					.param("generateManual", generateManuals.toString())
					.param("replace", replace.toString()))
			.andExpect(
					status().isUnprocessableEntity())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_SUBMISSION_INVALID_PATH))));
	}
	
	@Test
	public void submitPackage_returns500_whenCreationFails() throws Exception {
		final User user = getUserAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Repository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final byte[] packageFile = Files.readAllBytes(Path.of(TEST_PACKAGE_PATH));
		final MockMultipartFile multipartFile = 
				new MockMultipartFile("file", "abc_1.3.tar.gz", 
						ContentType.MULTIPART_FORM_DATA.toString(), 
						packageFile);
		final Boolean generateManuals = true;
		final Boolean replace = false;
		
		when(userService.findByLogin(user.getLogin())).thenReturn(user);
		when(submissionService.create(any(), eq(user)))
			.thenThrow(new SubmissionCreateException(messageSource, locale));
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.multipart("/api/v2/manager/r/submissions")
					.file(multipartFile)
					.principal(principal)
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
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/submissions")
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(
					status().isUnauthorized())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void getSubmission_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/submissions/123")
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(
					status().isUnauthorized())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void patchSubmission_returns401_whenUserIsNotAuthenticated() throws Exception {
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/state\",\"value\":\"ACCEPTED\"}]";
		
		mockMvc
			.perform(MockMvcRequestBuilders
				.patch("/api/v2/manager/r/submissions/" + 123)
				.contentType("application/json-patch+json")
				.content(patchJson))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void deleteSubmission_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc
			.perform(MockMvcRequestBuilders
					.delete("/api/v2/manager/r/submissions/" + 123))
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	public void deleteSubmission_returns403_whenUserIsNotAuthorized() throws Exception {
		final User user = getUserAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.delete("/api/v2/manager/r/submissions/" + 123)
					.principal(principal))
			.andExpect(status().isForbidden())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}
	
	@Test
	public void deleteSubmission_returns404_whenSubmissionIsNotFound() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(submissionService.findById(anyInt())).thenReturn(null);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.delete("/api/v2/manager/r/submissions/" + 123)
					.principal(principal))
			.andExpect(status().isNotFound())
			.andExpect(content().json(Files.readString(Path.of(ERROR_SUBMISSION_NOT_FOUND_PATH))));
	}
	
	@Test
	public void getSubmission_returns404_whenSubmissionIsNotFound() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		
		when(submissionService.findById(anyInt())).thenReturn(null);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/submissions/" + 123)
					.principal(principal))
			.andExpect(status().isNotFound())
			.andExpect(content().json(Files.readString(Path.of(ERROR_SUBMISSION_NOT_FOUND_PATH))));
	}
	
	@Test
	public void patchSubmission_returns404_whenSubmissionIsNotFound() throws Exception {
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/state\",\"value\":\"ACCEPTED\"}]";
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		
		when(submissionService.findById(anyInt())).thenReturn(null);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/r/submissions/" + 123)
					.principal(principal)
					.contentType("application/json-patch+json")
					.content(patchJson))
			.andExpect(status().isNotFound())
			.andExpect(content().json(Files.readString(Path.of(ERROR_SUBMISSION_NOT_FOUND_PATH))));
	}
	
	@Test
	public void patchSubmission_returns403_whenUserIsNotAuthorized() throws Exception {
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/state\",\"value\":\"ACCEPTED\"}]";
		final User user = getUserAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Repository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final Submission submission = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user).getSubmission();
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(userService.isAuthorizedToEdit(submission, user)).thenReturn(false);
		when(submissionService.findById(submission.getId())).thenReturn(submission);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/r/submissions/" + submission.getId())
					.contentType("application/json-patch+json")
					.content(patchJson)
					.principal(principal))
			.andExpect(
					status().isForbidden())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}
	
	@Test
	public void patchSubmission_returns500_whenInternalServerErrorOccurs() throws Exception {
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/state\",\"value\":\"ACCEPTED\"}]";
		final User user = getRepositoryMaintainerAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Repository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final Submission submission = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user).getSubmission();
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(userService.isAuthorizedToEdit(submission, user)).thenReturn(true);
		when(submissionService.findById(submission.getId())).thenReturn(submission);
		when(submissionService.evaluateAndUpdate(any(RSubmissionDto.class), eq(user)))
			.thenThrow(new SubmissionEditException(messageSource, locale, submission));
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/r/submissions/" + submission.getId())
					.content(patchJson)
					.contentType("application/json-patch+json")
					.principal(principal))
			.andExpect(
					status().isInternalServerError())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_PATCH_PATH))));
	}
	
	@Test
	public void deleteSubmission() throws Exception {
		final Repository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Submission submission = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user).getSubmission();
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(submissionService.findById(submission.getId())).thenReturn(submission);
		doNothing().when(submissionService).shiftDelete(submission);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.delete("/api/v2/manager/r/submissions/" + submission.getId())
					.principal(principal))
			.andExpect(
					status().isNoContent());
	}
	
	@Test
	public void getAllSubmissions_asMaintainer() throws Exception {
		Repository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		User user = getRepositoryMaintainerAndAuthenticate(userService);
		Principal principal = getMockPrincipal(user);
		
		List<Submission> submissions = RPackageTestFixture.GET_FIXTURE_PACKAGES(repository, user, 3, 100)
				.stream().map(p -> p.getSubmission()).collect(Collectors.toList());
		Page<Submission> paged = new PageImpl<Submission>(submissions);
		
		when(submissionService.findAllForUserOfUserAndWithState(
				eq(user), eq(user), eq(Optional.of(SubmissionState.WAITING)), any(Pageable.class))).thenReturn(paged);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(userService.findById(user.getId())).thenReturn(user);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/submissions")
					.param("deleted", "false")
					.param("state", "WAITING")
					.param("userId", Integer.toString(user.getId()))
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(status().isOk())
			.andExpect(content().json(Files.readString(Path.of(EXAMPLE_SUBMISSIONS_PATH))));
	}
	
	@Test
	public void patchSubmission() throws Exception {
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/state\",\"value\":\"ACCEPTED\"}]";
		final User user = getRepositoryMaintainerAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Repository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final Submission submission = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user).getSubmission();
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(userService.isAuthorizedToEdit(submission, user)).thenReturn(true);
		when(submissionService.findById(submission.getId())).thenReturn(submission);
		when(submissionService.evaluateAndUpdate(any(RSubmissionDto.class), eq(user)))
			.thenAnswer(new Answer<Submission>() {

				@Override
				public Submission answer(InvocationOnMock invocation) throws Throwable {
					RSubmissionDto dto = (RSubmissionDto)invocation.getArgument(0);
					assertEquals(SubmissionState.ACCEPTED, dto.getState());
					
					submission.setAccepted(true);
					return submission;
				}
				
		});
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/r/submissions/" + submission.getId())
					.content(patchJson)
					.contentType("application/json-patch+json")
					.principal(principal))
			.andExpect(
					status().isOk())
			.andExpect(
					content().json(Files.readString(Path.of(EXAMPLE_SUBMISSION_PATCHED_PATH))));
	}
	
	@Test
	public void patchSubmission_returns422_whenTryingToAcceptedCancelledSubmission() throws Exception {
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/state\",\"value\":\"ACCEPTED\"}]";
		final User user = getRepositoryMaintainerAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Repository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final Submission submission = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user).getSubmission();
		submission.setDeleted(true);
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(userService.isAuthorizedToEdit(submission, user)).thenReturn(true);
		when(submissionService.findById(submission.getId())).thenReturn(submission);
		when(submissionService.evaluateAndUpdate(any(RSubmissionDto.class), eq(user)))
			.thenThrow(new UpdateNotAllowedException(messageSource, locale));
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/r/submissions/" + submission.getId())
					.content(patchJson)
					.contentType("application/json-patch+json")
					.principal(principal))
			.andExpect(
					status().isUnprocessableEntity())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_UPDATE_NOT_ALLOWED_PATH))));
	}
	
	@Test
	public void getSubmission_returns403_whenUserIsNotAuthorized() throws Exception {
		final User user = getUserAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Repository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final Submission submission = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user).getSubmission();
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(submissionService.findByIdEvenDeleted(submission.getId())).thenReturn(Optional.of(submission));
		when(userService.isAuthorizedToSee(submission, user)).thenReturn(false);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/submissions/" + submission.getId())
					.contentType("application/json-patch+json")
					.principal(principal))
			.andExpect(
					status().isForbidden())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}
	
	@Test
	public void getSubmission() throws Exception {
		final User user = getUserAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Repository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final Submission submission = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user).getSubmission();
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(submissionService.findByIdEvenDeleted(submission.getId())).thenReturn(Optional.of(submission));
		when(userService.isAuthorizedToSee(submission, user)).thenReturn(true);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/submissions/" + submission.getId())
					.contentType("application/json-patch+json")
					.principal(principal))
			.andExpect(
					status().isOk())
			.andExpect(
					content().json(Files.readString(Path.of(EXAMPLE_SUBMISSION_PATH))));
	}
}
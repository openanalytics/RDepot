/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.api.v2.controller.ApiV2PackageMaintainerController;
import eu.openanalytics.rdepot.api.v2.dto.RepositoryMaintainerDto;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerCreateException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.test.context.TestConfig;
import eu.openanalytics.rdepot.test.fixture.RepositoryMaintainerTestFixture;

@ContextConfiguration(classes = {TestConfig.class})
@WebMvcTest(ApiV2PackageMaintainerController.class)
@ActiveProfiles("apiv2")
@WebAppConfiguration
@Import({TestConfig.class})
public class RepositoryMaintainerControllerUnitTest extends ApiV2ControllerUnitTest {

	private static final String JSON_PATH = "src/test/resources/unit/jsonscommon";
	private static final String EXAMPLE_REPOSITORYMAINTAINERS_PATH = JSON_PATH + "/example_repositorymaintainers.json";
	private static final String ERROR_REPOSITORY_MAINTAINER_NOT_FOUND_PATH = JSON_PATH + "/error_repositorymaintainer_notfound.json";
	private static final String EXAMPLE_REPOSITORYMAINTAINER_PATH = JSON_PATH + "/example_repositorymaintainer.json";
	private static final String EXAMPLE_NEW_REPOSITORYMAINTAINER_PATH = JSON_PATH + "/example_new_repositorymaintainer.json";
	private static final String ERROR_VALIDATION_REPOSITORYMAINTAINER_PATH = JSON_PATH + "/error_validation_repositorymaintainer.json";
	private static final String EXAMPLE_REPOSITORYMAINTAINER_CREATED_PATH = JSON_PATH + "/example_repositorymaintainer_created.json";
	private static final String ERROR_REPOSITORYMAINTAINER_MALFORMED_PATCH_PATH = JSON_PATH + "/error_repositorymaintainer_malformed_patch.json";
	private static final String EXAMPLE_REPOSITORYMAINTAINER_PATCHED_PATH = JSON_PATH + "/example_repositorymaintainer_patched.json";
	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	MessageSource messageSource;
	
	@Autowired
	MappingJackson2HttpMessageConverter jsonConverter;
	
	@Autowired
	WebApplicationContext webApplicationContext;
	
	@Test
	public void getAllRepositoryMaintainers() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(repositoryMaintainerService.findAll(any()))
			.thenReturn(RepositoryMaintainerTestFixture.GET_EXAMPLE_REPOSITORY_MAINTAINERS_PAGED());
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/repository-maintainers")
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status().isOk())
			.andExpect(
					content().json(Files.readString(Path.of(EXAMPLE_REPOSITORYMAINTAINERS_PATH))));
	}
	
	@Test
	public void getAllRepositoryMaintainers_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/repository-maintainers")
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void getAllRepositoryMaintainers_returns403_whenUserIsNotAdmin() throws Exception {
		final User user = getRepositoryMaintainerAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/repository-maintainers")
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status().isForbidden())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}
	
	@Test
	public void getRepositoryMaintainer_returns404_whenRepositoryMaintainerIsNotFound() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Integer ID = 123;
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(repositoryMaintainerService.findById(ID)).thenReturn(null);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/repository-maintainers/" + ID)
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status().isNotFound())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_REPOSITORY_MAINTAINER_NOT_FOUND_PATH))));
	}
	
	@Test
	public void getRepositoryMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/repository-maintainers/" + 123)
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void getRepositoryMaintainer_returns403_whenUserIsNotAdmin() throws Exception {
		final User user = getRepositoryMaintainerAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/repository-maintainers/" + 123)
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status().isForbidden())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));		
	}
	
	@Test
	public void getRepositoryMaintainer() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final RepositoryMaintainer repositoryMaintainer = RepositoryMaintainerTestFixture
				.GET_FIXTURE_REPOSITORY_MAINTAINER();
		final Integer ID = repositoryMaintainer.getId();
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(repositoryMaintainerService.findByIdEvenDeleted(ID)).thenReturn(Optional.of(repositoryMaintainer));
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/repository-maintainers/" + ID)
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status().isOk())
			.andExpect(
					content().json(Files.readString(Path.of(EXAMPLE_REPOSITORYMAINTAINER_PATH))));		
	}
	
	@Test
	public void createRepositoryMaintainer_throws401_whenUserIsNotAuthenticated() throws Exception {
		final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_REPOSITORYMAINTAINER_PATH));
		mockMvc
			.perform(MockMvcRequestBuilders
					.post("/api/v2/manager/r/repository-maintainers")
					.contentType(MediaType.APPLICATION_JSON)
					.content(exampleJson))
			.andExpect(
					status().isUnauthorized())
			.andExpect(
					content().json(Files.readString(
							Path.of(ERROR_NOT_AUTHENTICATED_PATH))));		
	}
	
	@Test
	public void createRepositoryMaintainer_throws403_whenUserIsNotAdmin() throws Exception {
		final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_REPOSITORYMAINTAINER_PATH));
		final User user = getRepositoryMaintainerAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.post("/api/v2/manager/r/repository-maintainers")
					.contentType(MediaType.APPLICATION_JSON)
					.content(exampleJson)
					.principal(principal))
			.andExpect(
					status().isForbidden())
			.andExpect(
					content().json(Files.readString(
							Path.of(ERROR_NOT_AUTHORIZED_PATH))));		
	}
	
	@Test
	public void createRepositoryMaintainer_throws422_whenValidationFails() throws Exception {
		final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_REPOSITORYMAINTAINER_PATH));
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(repositoryMaintainerValidator.supports(RepositoryMaintainer.class)).thenReturn(true);
		doAnswer(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				BindingResult bindingResult = invocation.getArgument(1);
				bindingResult.rejectValue("repository", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
				return null;
			}
			
		}).when(repositoryMaintainerValidator).validate(any(), any());
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.post("/api/v2/manager/r/repository-maintainers")
					.contentType(MediaType.APPLICATION_JSON)
					.content(exampleJson)
					.principal(principal))
			.andExpect(
					status().isUnprocessableEntity())
			.andExpect(
					content().json(Files.readString(
							Path.of(ERROR_VALIDATION_REPOSITORYMAINTAINER_PATH))));		
	}
	
	@Test
	public void createRepositoryMaintainer() throws Exception {
		final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_REPOSITORYMAINTAINER_PATH));
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		doNothing().when(repositoryMaintainerValidator).validate(any(), any());
		when(repositoryMaintainerValidator.supports(RepositoryMaintainer.class)).thenReturn(true);
		doAnswer(new Answer<RepositoryMaintainer>() {

			@Override
			public RepositoryMaintainer answer(InvocationOnMock invocation) throws Throwable {
				final RepositoryMaintainer created = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINER();

				RepositoryMaintainer actual = invocation.getArgument(0);
				created.setRepository(actual.getRepository());
				created.setUser(actual.getUser());
				
				return created;
			}
		}).when(repositoryMaintainerService).create(any(), eq(user));
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.post("/api/v2/manager/r/repository-maintainers")
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal)
					.content(exampleJson))
			.andExpect(status().isCreated())
			.andExpect(content().json(Files.readString(Path.of(EXAMPLE_REPOSITORYMAINTAINER_CREATED_PATH))));
		
		verify(repositoryMaintainerService, times(1)).create(any(), eq(user));
	}
	
	@Test
	public void createRepositoryMaintainer_returns500_whenCreationFails() throws Exception {
		final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_REPOSITORYMAINTAINER_PATH));
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final RepositoryMaintainer created = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINER();
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		doNothing().when(repositoryMaintainerValidator).validate(any(), any());
		when(repositoryMaintainerValidator.supports(RepositoryMaintainer.class)).thenReturn(true);
		doThrow(new RepositoryMaintainerCreateException(messageSource, Locale.ENGLISH, created))
			.when(repositoryMaintainerService).create(any(), eq(user));
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.post("/api/v2/manager/r/repository-maintainers")
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal)
					.content(exampleJson))
			.andExpect(status().isInternalServerError())
			.andExpect(content().json(Files.readString(Path.of(ERROR_CREATE_PATH))));		
	}
	
	@Test
	public void deleteRepositoryMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc
			.perform(MockMvcRequestBuilders
					.delete("/api/v2/manager/r/repository-maintainers/" + 123)
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void deleteRepositoryMaintainer_returns404_whenRepositoryMaintainerIsNotFound() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Integer ID = 123;
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(repositoryMaintainerService.findByIdAndDeleted(ID, true)).thenReturn(null);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.delete("/api/v2/manager/r/repository-maintainers/" + 123)
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(status().isNotFound())
			.andExpect(content().json(
					Files.readString(Path.of(ERROR_REPOSITORY_MAINTAINER_NOT_FOUND_PATH))));
	}
	
	@Test
	public void deleteRepositoryMaintainer_returns403_whenUserIsNotAdmin() throws Exception {
		final User user = getRepositoryMaintainerAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Integer ID = 123;
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.delete("/api/v2/manager/r/repository-maintainers/" + ID)
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status().isForbidden())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}
	
	@Test
	public void deleteRepositoryMaintainer() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final RepositoryMaintainer maintainer = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINER();
		maintainer.setDeleted(true);
		final Integer ID = maintainer.getId();
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(repositoryMaintainerService.findByIdAndDeleted(ID, true)).thenReturn(maintainer);
		doNothing().when(repositoryMaintainerService).shiftDelete(maintainer);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.delete("/api/v2/manager/r/repository-maintainers/" + ID)
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status().isNoContent());
			
			verify(repositoryMaintainerService, times(1)).shiftDelete(maintainer);		
	}
	
	@Test
	public void patchRepositoryMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/repositoryId\",\"value\":1234}]";
		
		mockMvc
			.perform(MockMvcRequestBuilders
				.patch("/api/v2/manager/r/repository-maintainers/" + 123)
				.contentType("application/json-patch+json")
				.content(patchJson))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void patchRepositoryMaintainer_returns404_whenRepositoryMaintainerIsNotFound() throws Exception {
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/repositoryId\",\"value\":1234}]";
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Integer ID = 123;
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(repositoryMaintainerService.findById(ID)).thenReturn(null);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/r/repository-maintainers/" + ID)
					.contentType("application/json-patch+json")
					.content(patchJson)
					.principal(principal))
			.andExpect(status().isNotFound())
			.andExpect(content().json(
					Files.readString(Path.of(ERROR_REPOSITORY_MAINTAINER_NOT_FOUND_PATH))));
	}
	
	@Test
	public void patchRepositoryMaintainer_returns403_whenUserIsNotAdmin() throws Exception {
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/repositoryId\",\"value\":1234}]";
		final User user = getRepositoryMaintainerAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Integer ID = 123;

		when(userService.findByLogin(principal.getName())).thenReturn(user);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/r/repository-maintainers/" + ID)
					.contentType("application/json-patch+json")
					.content(patchJson)
					.principal(principal))
			.andExpect(
					status().isForbidden())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}
	
	@Test
	public void patchRepositoryMaintainer_returns422_whenPatchIsIncorrect() throws Exception {
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/repositoryyyyyyId\",\"value\":1234}]";
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final RepositoryMaintainer maintainer = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINER();
		final Integer ID = maintainer.getId();
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(repositoryMaintainerService.findByIdEvenDeleted(ID)).thenReturn(Optional.of(maintainer));

		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/r/repository-maintainers/" + ID)
					.contentType("application/json-patch+json")
					.content(patchJson)
					.principal(principal))
			.andExpect(
					status().isUnprocessableEntity())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_REPOSITORYMAINTAINER_MALFORMED_PATCH_PATH))));
	}
	
	@Test
	public void patchRepositoryMaintainer() throws Exception {
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/repositoryId\",\"value\":1234}]";
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final RepositoryMaintainer maintainer = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINER();
		final Integer ID = maintainer.getId();
				
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(repositoryMaintainerService.findByIdEvenDeleted(ID)).thenReturn(Optional.of(maintainer));	
		doNothing().when(repositoryMaintainerValidator).validate(any(), any());
		when(repositoryMaintainerValidator.supports(RepositoryMaintainer.class)).thenReturn(true);
//		when(repositoryMaintainerService.evaluateAndUpdate(any(RepositoryMaintainerDto.class), eq(user)))
//			.thenReturn(updated);
		doAnswer(new Answer<RepositoryMaintainer>() {

			@Override
			public RepositoryMaintainer answer(InvocationOnMock invocation) throws Throwable {
				final RepositoryMaintainer updated = new RepositoryMaintainer(maintainer);
				updated.setId(maintainer.getId());
				updated.getRepository().setId(((RepositoryMaintainerDto)invocation.getArgument(0)).getRepositoryId());
				return updated;
			}
		}).when(repositoryMaintainerService).evaluateAndUpdate(any(RepositoryMaintainerDto.class), eq(user));
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/r/repository-maintainers/" + ID)
					.contentType("application/json-patch+json")
					.content(patchJson)
					.principal(principal))
			.andExpect(
					status().isOk())
			.andExpect(
					content().json(Files.readString(Path.of(EXAMPLE_REPOSITORYMAINTAINER_PATCHED_PATH))));
	}
}

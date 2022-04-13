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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import org.springframework.data.domain.Pageable;
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
import eu.openanalytics.rdepot.api.v2.dto.PackageMaintainerDto;
import eu.openanalytics.rdepot.exception.PackageMaintainerCreateException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.test.context.TestConfig;
import eu.openanalytics.rdepot.test.fixture.PackageMaintainerTestFixture;

@ContextConfiguration(classes = {TestConfig.class})
@WebMvcTest(ApiV2PackageMaintainerController.class)
@ActiveProfiles("apiv2")
@WebAppConfiguration
@Import({TestConfig.class})
public class PackageMaintainerControllerUnitTest extends ApiV2ControllerUnitTest {
	
	private static final String JSON_PATH = "src/test/resources/unit/jsonscommon";
	private static final String EXAMPLE_PACKAGE_MAINTAINERS_PATH = JSON_PATH + "/example_packagemaintainers.json";
	private static final String ERROR_PACKAGE_MAINTAINER_NOT_FOUND_PATH = JSON_PATH + "/error_packagemaintainer_notfound.json";
	private static final String EXAMPLE_PACKAGEMAINTAINER_PATH = JSON_PATH + "/example_packagemaintainer.json";
	private static final String EXAMPLE_NEW_PACKAGEMAINTAINER_PATH = JSON_PATH + "/example_new_packagemaintainer.json";
	private static final String EXAMPLE_PACKAGEMAINTAINER_CREATED_PATH = JSON_PATH + "/example_packagemaintainer_created.json";
	private static final String ERROR_VALIDATION_PACKAGEMAINTAINER_PATH = JSON_PATH + "/error_validation_packagemaintainer.json";
	private static final String ERROR_PACKAGEMAINTAINER_MALFORMED_PATCH = JSON_PATH + "/error_packagemaintainer_malformed_patch.json";
	private static final String EXAMPLE_PACKAGEMAINTAINER_PATCHED_PATH = JSON_PATH + "/example_packagemaintainer_patched.json";
	
	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	MappingJackson2HttpMessageConverter jsonConverter;
	
	@Autowired
	MessageSource messageSource;
	
	@Autowired
	ApiV2PackageMaintainerController packageMaintainerController;
	
	@Autowired
	WebApplicationContext webApplicationContext;

	@Test
	public void getAllPackageMaintainers() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		
		when(packageMaintainerService.findByRequester(eq(user), any(Pageable.class)))
			.thenReturn(PackageMaintainerTestFixture
					.GET_EXAMPLE_PACKAGE_MAINTAINERS_PAGED());
		when(userService.findByLogin(principal.getName()))
			.thenReturn(user);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/package-maintainers")
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status()
					.isOk())
			.andExpect(
					content()
					.json(Files.readString(Path.of(EXAMPLE_PACKAGE_MAINTAINERS_PATH))));
	}
	
	@Test
	public void getAllPackageMaintainers_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/package-maintainers")
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void getPackageMaintainer_returns404_whenPackageMaintainerIsNotFound() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		
		when(packageMaintainerService.findById(anyInt())).thenReturn(null);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/package-maintainers/" + 123)
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(status().isNotFound())
			.andExpect(content().json(
					Files.readString(Path.of(ERROR_PACKAGE_MAINTAINER_NOT_FOUND_PATH))));
	}
	
	@Test
	public void getPackageMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/package-maintainers/" + 123)
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void getPackageMaintainer_returns403_whenUserIsNotAuthorized() throws Exception {
		final User user = getUserAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(userService.isAuthorizedToSee(maintainer, user)).thenReturn(false);
		when(packageMaintainerService.findById(id)).thenReturn(maintainer);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/package-maintainers/" + id)
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status().isForbidden())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}
	
	@Test
	public void getPackageMaintainer() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(userService.isAuthorizedToSee(maintainer, user)).thenReturn(true);
		when(packageMaintainerService.findById(id)).thenReturn(maintainer);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/package-maintainers/" + id)
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(status().isOk())
			.andExpect(content().json(Files.readString(Path.of(EXAMPLE_PACKAGEMAINTAINER_PATH))));
	}
	
	@Test
	public void createPackageMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
		final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_PACKAGEMAINTAINER_PATH));
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.post("/api/v2/manager/r/package-maintainers")
					.contentType(MediaType.APPLICATION_JSON)
					.content(exampleJson))
			.andExpect(
					status().isUnauthorized())
			.andExpect(
					content().json(Files.readString(
							Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void createPackageMaintainer_returns403_whenUserIsNotAuthorized() throws Exception {
		final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_PACKAGEMAINTAINER_PATH));
		final User user = getUserAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		
		when(userService.isAuthorizedToEdit(any(PackageMaintainer.class), eq(user))).thenReturn(false);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.post("/api/v2/manager/r/package-maintainers")
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
	public void createPackageMaintainer_returns422_whenValidationFails() throws Exception {
		final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_PACKAGEMAINTAINER_PATH));
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		
		when(userService.isAuthorizedToEdit(any(PackageMaintainer.class), eq(user))).thenReturn(true);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		doAnswer(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				BindingResult bindingResult = invocation.getArgument(1);
				bindingResult.rejectValue("package", MessageCodes.ERROR_PACKAGE_ALREADY_MAINTAINED);
				//TODO: verify that validation looks good in the actual controller
				return null;
			}
		}).when(packageMaintainerValidator).validate(any(), any());
		when(packageMaintainerValidator.supports(PackageMaintainer.class)).thenReturn(true);

		mockMvc
			.perform(MockMvcRequestBuilders
					.post("/api/v2/manager/r/package-maintainers")
					.contentType(MediaType.APPLICATION_JSON)
					.content(exampleJson)
					.principal(principal))
			.andExpect(
					status().isUnprocessableEntity())
			.andExpect(
					content().json(Files.readString(
							Path.of(ERROR_VALIDATION_PACKAGEMAINTAINER_PATH))));
	}
	
	@Test
	public void createPackageMaintainer() throws Exception {
		final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_PACKAGEMAINTAINER_PATH));
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final PackageMaintainer created = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		
		when(userService.isAuthorizedToEdit(any(PackageMaintainer.class), eq(user))).thenReturn(true);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		doNothing().when(packageMaintainerValidator).validate(any(), any());
		when(packageMaintainerValidator.supports(PackageMaintainer.class)).thenReturn(true);
		when(packageMaintainerService.create(any(), eq(user))).thenReturn(created);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.post("/api/v2/manager/r/package-maintainers")
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal)
					.content(exampleJson))
			.andExpect(status().isCreated())
			.andExpect(content().json(Files.readString(Path.of(EXAMPLE_PACKAGEMAINTAINER_CREATED_PATH))));
		
		verify(packageMaintainerService, times(1)).create(any(), eq(user));
	}
	
	@Test
	public void createPackageMaintainer_returns500_whenCreationFails() throws Exception {
		final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_PACKAGEMAINTAINER_PATH));
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final PackageMaintainer created = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		
		when(userService.isAuthorizedToEdit(any(PackageMaintainer.class), eq(user))).thenReturn(true);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		doNothing().when(packageMaintainerValidator).validate(any(), any());
		when(packageMaintainerValidator.supports(PackageMaintainer.class)).thenReturn(true);
		doThrow(new PackageMaintainerCreateException(messageSource, Locale.ENGLISH, created))
			.when(packageMaintainerService).create(any(), eq(user));
		
		mockMvc
		.perform(MockMvcRequestBuilders
				.post("/api/v2/manager/r/package-maintainers")
				.contentType(MediaType.APPLICATION_JSON)
				.principal(principal)
				.content(exampleJson))
		.andExpect(status().isInternalServerError())
		.andExpect(content().json(Files.readString(Path.of(ERROR_CREATE_PATH))));
	
		verify(packageMaintainerService, times(1)).create(any(), eq(user));
	}
	
	@Test
	public void deletePackageMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc
			.perform(MockMvcRequestBuilders
					.delete("/api/v2/manager/r/package-maintainers/" + 123)
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void deletePackageMaintainer_returns404_whenPackageMaintainerIsNotFound() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		
		when(packageMaintainerService.findById(anyInt())).thenReturn(null);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.delete("/api/v2/manager/r/package-maintainers/" + 123)
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(status().isNotFound())
			.andExpect(content().json(
					Files.readString(Path.of(ERROR_PACKAGE_MAINTAINER_NOT_FOUND_PATH))));
	}
	
	@Test
	public void deletePackageMaintainer_returns403_whenUserIsNotAuthorized() throws Exception {
		final User user = getUserAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();
				
		mockMvc
			.perform(MockMvcRequestBuilders
					.delete("/api/v2/manager/r/package-maintainers/" + id)
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status().isForbidden())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}
	
	@Test
	public void deletePackageMaintainer() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(packageMaintainerService.findByIdAndDeleted(anyInt(), eq(true))).thenReturn(maintainer);
		doNothing().when(packageMaintainerService).shiftDelete(maintainer);
		
		mockMvc
		.perform(MockMvcRequestBuilders
				.delete("/api/v2/manager/r/package-maintainers/" + id)
				.contentType(MediaType.APPLICATION_JSON)
				.principal(principal))
		.andExpect(
				status().isNoContent());
		
		verify(packageMaintainerService, times(1)).shiftDelete(maintainer);
	}
	
	@Test
	public void patchPackageMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/packageName\",\"value\":\"neeeeeew_package\"}]";

		mockMvc
			.perform(MockMvcRequestBuilders
				.patch("/api/v2/manager/r/package-maintainers/" + 123)
				.contentType("application/json-patch+json")
				.content(patchJson))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void patchPackageMaintainer_returns404_whenPackageMaintainerIsNotFound() throws Exception {
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/packageName\",\"value\":\"neeeeeew_package\"}]";
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(packageMaintainerService.findById(anyInt())).thenReturn(null);
		
		mockMvc
		.perform(MockMvcRequestBuilders
				.patch("/api/v2/manager/r/package-maintainers/" + id)
				.contentType("application/json-patch+json")
				.content(patchJson)
				.principal(principal))
		.andExpect(status().isNotFound())
		.andExpect(content().json(
				Files.readString(Path.of(ERROR_PACKAGE_MAINTAINER_NOT_FOUND_PATH))));
	}
	
	@Test
	public void patchPackageMaintainer_returns403_whenUserIsNotAuthorized() throws Exception {
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/packageName\",\"value\":\"neeeeeew_package\"}]";
		final User user = getUserAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(packageMaintainerService.findById(anyInt())).thenReturn(maintainer);
		when(userService.isAuthorizedToEdit(maintainer, user)).thenReturn(false);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/r/package-maintainers/" + id)
					.contentType("application/json-patch+json")
					.content(patchJson)
					.principal(principal))
			.andExpect(
					status().isForbidden())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}
	
	@Test
	public void patchPackageMaintainer_returns422_whenPatchIsIncorrect() throws Exception {
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/packageNaaaaaaame\",\"value\":\"neeeeeew_package\"}]";
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(packageMaintainerService.findByIdEvenDeleted(anyInt())).thenReturn(Optional.of(maintainer));
		when(userService.isAuthorizedToEdit(maintainer, user)).thenReturn(true);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/r/package-maintainers/" + id)
					.contentType("application/json-patch+json")
					.content(patchJson)
					.principal(principal))
			.andExpect(
					status().isUnprocessableEntity())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_PACKAGEMAINTAINER_MALFORMED_PATCH))));
	}
	
	@Test
	public void patchPackageMaintainer() throws Exception {
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/packageName\",\"value\":\"neeeeeew_package\"}]";
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();
		
		final PackageMaintainer updated = new PackageMaintainer(maintainer);
		updated.setId(maintainer.getId());
		updated.setPackage("neeeeeew_package");
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(packageMaintainerService.findByIdEvenDeleted(anyInt())).thenReturn(Optional.of(maintainer));
		when(userService.isAuthorizedToEdit(maintainer, user)).thenReturn(true);
		when(packageMaintainerService.evaluateAndUpdate(any(PackageMaintainerDto.class), eq(user)))
			.thenReturn(updated);
		when(packageMaintainerValidator.supports(PackageMaintainer.class)).thenReturn(true);
		doNothing().when(packageMaintainerValidator).validate(any(), any());
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/r/package-maintainers/" + id)
					.contentType("application/json-patch+json")
					.content(patchJson)
					.principal(principal))
			.andExpect(
					status().isOk())
			.andExpect(
					content().json(Files.readString(Path.of(EXAMPLE_PACKAGEMAINTAINER_PATCHED_PATH))));
	}
}

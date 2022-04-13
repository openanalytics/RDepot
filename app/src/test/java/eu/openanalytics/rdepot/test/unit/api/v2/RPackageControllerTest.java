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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.Locale;

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
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.api.v2.controller.RPackageController;
import eu.openanalytics.rdepot.api.v2.dto.RPackageDto;
import eu.openanalytics.rdepot.exception.PackageDeleteException;
import eu.openanalytics.rdepot.exception.PackageException;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.test.context.TestConfig;
import eu.openanalytics.rdepot.test.fixture.RPackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RRepositoryTestFixture;

@ContextConfiguration(classes = {TestConfig.class})
@WebMvcTest(RPackageController.class)
@ActiveProfiles("apiv2")
@WebAppConfiguration
@Import({TestConfig.class})
public class RPackageControllerTest extends ApiV2ControllerUnitTest {

	private static final String JSON_PATH = "src/test/resources/unit/jsons";
	private static final String EXAMPLE_PACKAGES_PATH = JSON_PATH + "/example_packages.json";
	private static final String EXAMPLE_DELETED_PACKAGES_PATH = JSON_PATH + "/example_deleted_packages.json";
	private static final String EXAMPLE_PACKAGE_PATH = JSON_PATH + "/example_package.json";
	private static final String EXAMPLE_PACKAGE_NOT_FOUND_PATH = JSON_PATH + "/example_package_notfound.json";
	private static final String EXAMPLE_PACKAGE_PATCHED_PATH = JSON_PATH + "/example_package_patched.json";
	private static final String ERROR_PACKAGE_MALFORMED_PATCH = JSON_PATH + "/error_package_malformed_patch.json";
	
	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	MappingJackson2HttpMessageConverter jsonConverter;
	
	@Autowired
	MessageSource messageSource;
	
	@Autowired
	RPackageController rPackageController;
	
	@Autowired
	WebApplicationContext webApplicationContext;
	
	@Test
	public void getAllPackages_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/packages")
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void getPackage_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/packages/" + 123)
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void deletePackage_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc
			.perform(MockMvcRequestBuilders
					.delete("/api/v2/manager/r/packages/" + 123))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void getAllDeletedPackages_returns403_whenUserIsNotAdmin() throws Exception {
		final User user = getUserAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/packages")
					.param("deleted", "true")
					.principal(principal)
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isForbidden())
			.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}
	
	@Test
	public void getAllPackages() throws Exception {
		User user = getAdminAndAuthenticate(userService);
		Principal principal = getMockPrincipal(user);
		
		when(packageService.findAllEvenDeleted(any())).thenReturn(RPackageTestFixture.GET_EXAMPLE_PACKAGES_PAGED());
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(userService.isAdmin(user)).thenReturn(true);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/packages")
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status()
					.isOk())
			.andExpect(
					content()
					.json(Files.readString(Path.of(EXAMPLE_PACKAGES_PATH))));
	}
	
	@Test
	public void getAllPackages_WhenRepositoryIsSpecified() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final String repositoryName = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY().getName();
		
		when(packageService.findAllByRepositoryName(eq(repositoryName), any()))
			.thenReturn(RPackageTestFixture.GET_EXAMPLE_PACKAGES_PAGED());
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(userService.isAdmin(user)).thenReturn(true);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/packages")
					.param("repositoryName", repositoryName)
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status()
					.isOk())
			.andExpect(
					content()
					.json(Files.readString(Path.of(EXAMPLE_PACKAGES_PATH))));		
	}
	
	@Test
	public void getAllPackages_OnlyDeleted() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);

		when(packageService.findAllByDeleted(eq(true), any()))
			.thenReturn(RPackageTestFixture.GET_EXAMPLE_PACKAGES_PAGED_DELETED());
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(userService.isAdmin(user)).thenReturn(true);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/packages")
					.param("deleted", "true")
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status()
					.isOk())
			.andExpect(
					content()
					.json(Files.readString(Path.of(EXAMPLE_DELETED_PACKAGES_PATH))));		
	}
	
	@Test
	public void getPackageById_AsAdmin() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Package packageBag = RPackageTestFixture.GET_EXAMPLE_PACKAGE();
		
		when(packageService.findByIdEvenDeleted(packageBag.getId()))
			.thenReturn(packageBag);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(userService.isAdmin(user)).thenReturn(true);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/packages/" + packageBag.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status()
					.isOk())
			.andExpect(
					content()
					.json(Files.readString(Path.of(EXAMPLE_PACKAGE_PATH))));		
	}
	
	@Test
	public void getPackageById_AsUser() throws Exception {
		final User user = getUserAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Package packageBag = RPackageTestFixture.GET_EXAMPLE_PACKAGE();
		
		when(packageService.findByIdEvenDeleted(packageBag.getId()))
			.thenReturn(packageBag);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(userService.isAdmin(user)).thenReturn(false);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/packages/" + packageBag.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status()
					.isOk())
			.andExpect(
					content()
					.json(Files.readString(Path.of(EXAMPLE_PACKAGE_PATH))));		
		
	}
	
	@Test
	public void getPackageById_returns404_WhenPackageIsNotFound() throws Exception {
		final User user = getUserAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Integer ID = 123;
		
		when(packageService.findById(ID)).thenReturn(null);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(userService.isAdmin(user)).thenReturn(false);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.get("/api/v2/manager/r/packages/" + ID)
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status()
					.isNotFound())
			.andExpect(
					content()
					.json(Files.readString(Path.of(EXAMPLE_PACKAGE_NOT_FOUND_PATH))));			
	}
	
	@Test
	public void deletePackage_returns404_WhenPackageIsNotFound() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Integer ID = 123;
		
		when(packageService.findById(ID)).thenReturn(null);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(userService.isAdmin(user)).thenReturn(true);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.delete("/api/v2/manager/r/packages/" + ID)
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(
					status()
					.isNotFound())
			.andExpect(
					content()
					.json(Files.readString(Path.of(EXAMPLE_PACKAGE_NOT_FOUND_PATH))));			
	}
	
	@Test
	public void deletePackage_returns403_WhenUserIsNotAdmin() throws Exception {
		final User user = getUserAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Integer ID = 123;
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.delete("/api/v2/manager/r/packages/" + ID)
					.principal(principal))
			.andExpect(
					status()
					.isForbidden())
			.andExpect(
					content()
					.json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}
	
	@Test
	public void deletePackage() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Package packageBag = RPackageTestFixture.GET_EXAMPLE_PACKAGE();
		packageBag.setDeleted(true);
		
		when(packageService.findByIdAndDeleted(packageBag.getId(), true))
			.thenReturn(packageBag);
		doNothing().when(packageService).shiftDelete(packageBag);
		
		mockMvc
		.perform(MockMvcRequestBuilders
				.delete("/api/v2/manager/r/packages/" + packageBag.getId())
				.principal(principal))
		.andExpect(
				status()
				.isNoContent());
	}
	
	@Test
	public void patchPackage_returns404_WhenPackageIsNotFound() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Integer ID = 123;
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/active\",\"value\":\"false\"}]";

		
		when(packageService.findByIdEvenDeleted(ID)).thenReturn(null);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/r/packages/" + ID)
					.content(patchJson)
					.contentType("application/json-patch+json")
					.principal(principal))
			.andExpect(
					status()
					.isNotFound())
			.andExpect(
					content()
					.json(Files.readString(Path.of(EXAMPLE_PACKAGE_NOT_FOUND_PATH))));			
	}
	
	@Test
	public void patchPackage_returns401_WhenUserIsNotAuthenticated() throws Exception {
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/active\",\"value\":\"false\"}]";

		mockMvc
		.perform(MockMvcRequestBuilders
				.patch("/api/v2/manager/r/packages/" + 123)
				.content(patchJson)
				.contentType("application/json-patch+json"))
		.andExpect(status().isUnauthorized())
		.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}
	
	@Test
	public void patchPackage_returns403_WhenUserIsNotPackageMaintainer() throws Exception {
		final User user = getUserAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Integer ID = 123;
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/active\",\"value\":\"false\"}]";

		
		when(packageService.findByIdEvenDeleted(ID)).thenReturn(null);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		
		mockMvc
		.perform(MockMvcRequestBuilders
				.patch("/api/v2/manager/r/packages/" + 123)
				.content(patchJson)
				.contentType("application/json-patch+json"))
		.andExpect(status().isForbidden())
		.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}
	
	@Test
	public void patchPackage() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Package packageBag = RPackageTestFixture.GET_EXAMPLE_PACKAGE();
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/active\",\"value\":\"false\"}]";

		when(packageService.findByIdEvenDeleted(packageBag.getId())).thenReturn(packageBag);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		doNothing().when(packageValidator).validate(any(), eq(false));
		when(userService.isAuthorizedToEdit(packageBag, user)).thenReturn(true);
		when(packageService.evaluateAndUpdate(any(), eq(user))).thenAnswer(new Answer<Package>() {

			@Override
			public Package answer(InvocationOnMock invocation) throws Throwable {
				RPackageDto updated = invocation.getArgument(0);
				assertFalse(updated.getActive());
				return updated.toEntity();
			}
		});
		
		mockMvc
		.perform(MockMvcRequestBuilders
				.patch("/api/v2/manager/r/packages/" + packageBag.getId())
				.content(patchJson)
				.principal(principal)
				.contentType("application/json-patch+json"))
		.andExpect(status().isOk())
		.andExpect(content().json(Files.readString(Path.of(EXAMPLE_PACKAGE_PATCHED_PATH))));
		
		verify(packageService).evaluateAndUpdate(any(), eq(user));
		verify(packageValidator).validate(any(), eq(false));
	}
	
	@Test
	public void patchPackage_returns422_whenPatchIsIncorrect() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Package packageBag = RPackageTestFixture.GET_EXAMPLE_PACKAGE();
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/actiiiiiive\",\"value\":\"false\"}]";

		when(packageService.findByIdEvenDeleted(packageBag.getId())).thenReturn(packageBag);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(userService.isAuthorizedToEdit(packageBag, user)).thenReturn(true);
		doNothing().when(packageValidator).validate(any(), eq(false));	
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/r/packages/" + packageBag.getId())
					.contentType("application/json-patch+json")
					.content(patchJson)
					.principal(principal))
			.andExpect(
					status().isUnprocessableEntity())
			.andExpect(
					content().json(Files.readString(Path.of(ERROR_PACKAGE_MALFORMED_PATCH))));
	}
	
	@Test
	public void patchPackage_returns422_whenValidationFails() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Package packageBag = RPackageTestFixture.GET_EXAMPLE_PACKAGE();
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/active\",\"value\":\"false\"}]";

		when(packageService.findByIdEvenDeleted(packageBag.getId())).thenReturn(packageBag);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		doNothing().when(packageValidator).validate(any(), eq(false));
		when(userService.isAuthorizedToEdit(packageBag, user)).thenReturn(true);
		when(packageService.evaluateAndUpdate(any(), eq(user))).thenAnswer(new Answer<Package>() {

			@Override
			public Package answer(InvocationOnMock invocation) throws Throwable {
				RPackageDto updated = invocation.getArgument(0);
				assertFalse(updated.getActive());
				return updated.toEntity();
			}
		});
		
		mockMvc
		.perform(MockMvcRequestBuilders
				.patch("/api/v2/manager/r/packages/" + packageBag.getId())
				.content(patchJson)
				.principal(principal)
				.contentType("application/json-patch+json"))
		.andExpect(status().isOk())
		.andExpect(content().json(Files.readString(Path.of(EXAMPLE_PACKAGE_PATCHED_PATH))));
		
		verify(packageService).evaluateAndUpdate(any(), eq(user));
		verify(packageValidator).validate(any(), eq(false));
		
	}
	
	@Test
	public void patchPackage_returns500_whenErrorIsThrown() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Package packageBag = RPackageTestFixture.GET_EXAMPLE_PACKAGE();
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/active\",\"value\":\"false\"}]";
		final PackageException exception = new PackageException(null, messageSource, null, 0);
		
		when(packageService.findByIdEvenDeleted(packageBag.getId())).thenReturn(packageBag);
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		doNothing().when(packageValidator).validate(any(), eq(false));
		when(packageService.evaluateAndUpdate(any(), eq(user))).thenThrow(exception);
		when(userService.isAuthorizedToEdit(packageBag, user)).thenReturn(true);
		
		mockMvc
		.perform(MockMvcRequestBuilders
				.patch("/api/v2/manager/r/packages/" + packageBag.getId())
				.content(patchJson)
				.principal(principal)
				.contentType("application/json-patch+json"))
		.andExpect(status().isInternalServerError())
		.andExpect(content().json(Files.readString(Path.of(ERROR_PATCH_PATH))));
		
		verify(packageService).evaluateAndUpdate(any(), eq(user));
		verify(packageValidator).validate(any(), eq(false));
	}
	
	@Test
	public void deletePackage_returns500_whenErrorIsThrown() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Package packageBag = RPackageTestFixture.GET_EXAMPLE_PACKAGE();
		final PackageDeleteException exception = new PackageDeleteException(messageSource, Locale.ENGLISH, packageBag);
		packageBag.setDeleted(true);
		
		when(packageService.findByIdAndDeleted(packageBag.getId(), true))
			.thenReturn(packageBag);
		doThrow(exception).when(packageService).shiftDelete(packageBag);
		
		mockMvc
		.perform(MockMvcRequestBuilders
				.delete("/api/v2/manager/r/packages/" + packageBag.getId())
				.principal(principal))
		.andExpect(
				status()
				.isInternalServerError())
		.andExpect(content().json(Files.readString(Path.of(ERROR_DELETE_PATH))));
	}
}

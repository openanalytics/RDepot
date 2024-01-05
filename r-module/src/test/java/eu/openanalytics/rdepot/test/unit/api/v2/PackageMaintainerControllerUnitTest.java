/**
 * R Depot
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2PackageMaintainerController;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.test.context.ApiTestConfig;
import eu.openanalytics.rdepot.test.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.api.v2.mockstrategies.FailureStrategy;
import eu.openanalytics.rdepot.test.unit.api.v2.mockstrategies.SuccessfulStrategy;

@ContextConfiguration(classes = { ApiTestConfig.class })
@WebMvcTest(ApiV2PackageMaintainerController.class)
@ActiveProfiles("apiv2")
@WebAppConfiguration
@Import({ ApiTestConfig.class })
public class PackageMaintainerControllerUnitTest extends ApiV2ControllerUnitTest {

	private static final String JSON_PATH = "src/test/resources/unit/jsonscommon";
	private static final String EXAMPLE_PACKAGE_MAINTAINERS_PATH = JSON_PATH + "/example_packagemaintainers.json";
	private static final String ERROR_PACKAGE_MAINTAINER_NOT_FOUND_PATH = JSON_PATH
			+ "/error_packagemaintainer_notfound.json";
	private static final String EXAMPLE_PACKAGEMAINTAINER_PATH = JSON_PATH + "/example_packagemaintainer.json";
	private static final String EXAMPLE_NEW_PACKAGEMAINTAINER_PATH = JSON_PATH + "/example_new_packagemaintainer.json";
	private static final String EXAMPLE_PACKAGEMAINTAINER_CREATED_PATH = JSON_PATH
			+ "/example_packagemaintainer_created.json";
	private static final String ERROR_VALIDATION_PACKAGEMAINTAINER_PATH = JSON_PATH
			+ "/error_validation_packagemaintainer.json";
	private static final String ERROR_PACKAGEMAINTAINER_MALFORMED_PATCH = JSON_PATH
			+ "/error_packagemaintainer_malformed_patch.json";
	private static final String EXAMPLE_PACKAGEMAINTAINER_PATCHED_PATH = JSON_PATH
			+ "/example_packagemaintainer_patched.json";

	private Optional<User> user;

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

	@Autowired
	StaticMessageResolver staticMessageResolver;

	@BeforeEach
	public void initEach() {
		user = UserTestFixture.GET_FIXTURE_ADMIN();
	}

	@Test
	@WithMockUser(authorities = "user")
	public void getAllPackageMaintainers() throws Exception {

		when(packageMaintainerService.findAll(any(Pageable.class)))
				.thenReturn(PackageMaintainerTestFixture.GET_EXAMPLE_PACKAGE_MAINTAINERS_PAGED());

		when(userService.findByLogin("user")).thenReturn(user);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/r/package-maintainers")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().json(Files.readString(Path.of(EXAMPLE_PACKAGE_MAINTAINERS_PATH))));
	}

	@Test
	public void getAllPackageMaintainers_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/r/package-maintainers")
				.contentType(MediaType.APPLICATION_JSON))

				.andExpect(status().isUnauthorized())
				.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}

	@Test
	@WithMockUser(authorities = "user")
	public void getPackageMaintainer_returns404_whenPackageMaintainerIsNotFound() throws Exception {
		when(packageMaintainerService.findById(any(Integer.class))).thenReturn(Optional.ofNullable(null));
		when(userService.findByLogin("user")).thenReturn(user);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/r/package-maintainers/" + 123)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
				.andExpect(content().json(Files.readString(Path.of(ERROR_PACKAGE_MAINTAINER_NOT_FOUND_PATH))));
	}

	@Test
	public void getPackageMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/r/package-maintainers/" + 123)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized())
				.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}

	@Test
	@WithMockUser
	public void getPackageMaintainer_returns403_whenUserIsNotAuthorized() throws Exception {
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();

		mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/r/package-maintainers/" + id)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden())
				.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}

	@Test
	@WithMockUser(authorities = "user")
	public void getPackageMaintainer_returns403_whenUserIsNotAuthorizedToSee() throws Exception {
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();

		when(userService.findByLogin("user")).thenReturn(user);
		when(securityMediator.isAuthorizedToSee(maintainer, user.get())).thenReturn(false);
		when(packageMaintainerService.findById(id)).thenReturn(Optional.of(maintainer));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/r/package-maintainers/" + id)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden())
				.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}

	@Test
	@WithMockUser(authorities = { "user" })
	public void getPackageMaintainer() throws Exception {
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();

		when(userService.findByLogin("user")).thenReturn(user);
		when(securityMediator.isAuthorizedToSee(maintainer, user.get())).thenReturn(true);
		when(packageMaintainerService.findById(id)).thenReturn(Optional.of(maintainer));

		mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/r/package-maintainers/" + id)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().json(Files.readString(Path.of(EXAMPLE_PACKAGEMAINTAINER_PATH))));
	}

	@Test
	public void createPackageMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
		final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_PACKAGEMAINTAINER_PATH));

		mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/r/package-maintainers")
				.contentType(MediaType.APPLICATION_JSON).content(exampleJson)).andExpect(status().isUnauthorized())
				.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}

	@Test
	@WithMockUser
	public void createPackageMaintainer_returns403_whenUserIsNotAuthorized() throws Exception {
		final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_PACKAGEMAINTAINER_PATH));

		when(securityMediator.isAuthorizedToEdit(any(PackageMaintainer.class), eq(user.get()))).thenReturn(false);
		when(userService.findByLogin("user")).thenReturn(user);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/r/package-maintainers")
				.contentType(MediaType.APPLICATION_JSON).content(exampleJson)).andExpect(status().isForbidden())
				.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}

	@Test
	@WithMockUser(authorities = { "repositorymaintainer", "user" })
	public void createPackageMaintainer_returns422_whenValidationFails() throws Exception {
		final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_PACKAGEMAINTAINER_PATH));
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();

		when(securityMediator.isAuthorizedToEdit(any(PackageMaintainer.class), eq(user.get()))).thenReturn(true);
		when(userService.findByLogin("user")).thenReturn(user);
		doReturn(Optional.of(repository)).when(commonRepositoryService).findById(123);
		when(userService.findById(111)).thenReturn(user);
		doAnswer(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				BindingResult bindingResult = invocation.getArgument(1);
				bindingResult.rejectValue("packageName", MessageCodes.ERROR_PACKAGE_ALREADY_MAINTAINED);
				// TODO: verify that validation looks good in the actual controller
				return null;
			}
		}).when(packageMaintainerValidator).validate(any(), any());
		when(packageMaintainerValidator.supports(PackageMaintainer.class)).thenReturn(true);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/r/package-maintainers")
				.contentType(MediaType.APPLICATION_JSON).content(exampleJson))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(content().json(Files.readString(Path.of(ERROR_VALIDATION_PACKAGEMAINTAINER_PATH))));
	}

	@Test
	@WithMockUser(authorities = { "repositorymaintainer", "user" })
	public void createPackageMaintainer() throws Exception {
		user.get().setId(111);
		final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_PACKAGEMAINTAINER_PATH));
		final PackageMaintainer created = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		Strategy<PackageMaintainer> strategy = Mockito.spy(new SuccessfulStrategy<PackageMaintainer>(created,
				newsfeedEventService, packageMaintainerService, user.get()));

		when(strategyFactory.createPackageMaintainerStrategy(any(), eq(user.get()))).thenReturn(strategy);
		when(securityMediator.isAuthorizedToEdit(any(PackageMaintainer.class), eq(user.get()))).thenReturn(true);
		when(userService.findByLogin("user")).thenReturn(user);
		doNothing().when(packageMaintainerValidator).validate(any(), any());
		when(packageMaintainerValidator.supports(PackageMaintainer.class)).thenReturn(true);
		when(packageMaintainerService.create(any())).thenReturn(created);
		doReturn(Optional.of(repository)).when(commonRepositoryService).findById(123);
		when(userService.findById(111)).thenReturn(user);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/r/package-maintainers")
				.contentType(MediaType.APPLICATION_JSON).content(exampleJson)).andExpect(status().isCreated())
				.andExpect(content().json(Files.readString(Path.of(EXAMPLE_PACKAGEMAINTAINER_CREATED_PATH))));

		verify(strategy, times(1)).perform();
	}

	@Test
	@WithMockUser(authorities = { "user", "repositorymaintainer" })
	public void createPackageMaidntainer_returns500_whenCreationFails() throws Exception {
		final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_PACKAGEMAINTAINER_PATH));
		final PackageMaintainer created = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		Strategy<PackageMaintainer> strategy = Mockito.spy(new FailureStrategy<PackageMaintainer>(created,
				newsfeedEventService, packageMaintainerService, user.get()));

		doReturn(Optional.of(repository)).when(commonRepositoryService).findById(123);
		when(userService.findById(111)).thenReturn(user);
		when(strategyFactory.createPackageMaintainerStrategy(any(), eq(user.get()))).thenReturn(strategy);
		when(securityMediator.isAuthorizedToEdit(any(PackageMaintainer.class), eq(user.get()))).thenReturn(true);
		when(userService.findByLogin("user")).thenReturn(user);
		doNothing().when(packageMaintainerValidator).validate(any(), any());
		when(packageMaintainerValidator.supports(PackageMaintainer.class)).thenReturn(true);
		doAnswer(invocation -> invocation.getArgument(0)).when(newsfeedEventService).create(any());

		mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/r/package-maintainers")
				.contentType(MediaType.APPLICATION_JSON).content(exampleJson))
				.andExpect(status().isInternalServerError())
				.andExpect(content().json(Files.readString(Path.of(ERROR_CREATE_PATH))));
		verify(strategy, times(1)).perform();
	}

	@Test
	public void deletePackageMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/r/package-maintainers/" + 123)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized())
				.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}

	@Test
	@WithMockUser(authorities = { "admin", "user" })
	public void deletePackageMaintainer_returns404_whenPackageMaintainerIsNotFound() throws Exception {
		when(packageMaintainerService.findById(any(Integer.class))).thenReturn(null);
		when(userService.findByLogin("user")).thenReturn(user);

		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/r/package-maintainers/" + 123)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
				.andExpect(content().json(Files.readString(Path.of(ERROR_PACKAGE_MAINTAINER_NOT_FOUND_PATH))));
	}

	@Test
	@WithMockUser
	public void deletePackageMaintainer_returns403_whenUserIsNotAuthorized() throws Exception {
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();

		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/r/package-maintainers/" + id)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden())
				.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}

	@Test
	@WithMockUser(authorities = { "admin", "user" })
	public void deletePackageMaintainer() throws Exception {
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();

		when(userService.findByLogin("user")).thenReturn(user);
		when(packageMaintainerService.findOneDeleted(any(Integer.class))).thenReturn(Optional.of(maintainer));
		doNothing().when(packageMaintainerDeleter).delete(maintainer);

		mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/r/package-maintainers/" + id)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());

		verify(packageMaintainerDeleter, times(1)).delete(maintainer);
	}

	@Test
	public void patchPackageMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/packageName\",\"value\":\"neeeeeew_package\"}]";

		mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/package-maintainers/" + 123)
				.contentType("application/json-patch+json").content(patchJson)).andExpect(status().isUnauthorized())
				.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
	}

	@Test
	@WithMockUser(authorities = { "user", "repositorymaintainer" })
	public void patchPackageMaintainer_returns404_whenPackageMaintainerIsNotFound() throws Exception {
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/packageName\",\"value\":\"neeeeeew_package\"}]";
		final Optional<User> user = UserTestFixture.GET_FIXTURE_ADMIN();
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();

		when(packageMaintainerService.findById(any(Integer.class))).thenReturn(Optional.ofNullable(null));
		when(userService.findByLogin("user")).thenReturn(user);

		mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/package-maintainers/" + id)
				.contentType("application/json-patch+json").content(patchJson)).andExpect(status().isNotFound())
				.andExpect(content().json(Files.readString(Path.of(ERROR_PACKAGE_MAINTAINER_NOT_FOUND_PATH))));
	}

	@Test
	@WithMockUser
	public void patchPackageMaintainer_returns403_whenUserIsNotAuthorized() throws Exception {
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/packageName\",\"value\":\"neeeeeew_package\"}]";
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();

		mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/package-maintainers/" + id)
				.contentType("application/json-patch+json").content(patchJson)).andExpect(status().isForbidden())
				.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}

	@Test
	@WithMockUser(authorities = { "user", "repositorymaintainer" })
	public void patchPackageMaintainer_returns403_whenUserIsNotAuthorizedToEdit() throws Exception {
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/packageName\",\"value\":\"neeeeeew_package\"}]";
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();

		when(userService.findByLogin("user")).thenReturn(user);
		when(packageMaintainerService.findById(any(Integer.class))).thenReturn(Optional.of(maintainer));
		when(securityMediator.isAuthorizedToEdit(eq(maintainer), any(User.class))).thenReturn(false);

		mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/package-maintainers/" + id)
				.contentType("application/json-patch+json").content(patchJson)).andExpect(status().isForbidden())
				.andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
	}

	@Test
	@WithMockUser(authorities = { "user", "repositorymaintainer" })
	public void patchPackageMaintainer_returns422_whenPatchIsIncorrect() throws Exception {
		String patchJson = "[{\"op\": \"replace\", \"path\":\"/packageNaaaaaaame\",\"value\":\"neeeeeew_package\"}]";
		final Optional<User> user = UserTestFixture.GET_FIXTURE_ADMIN();
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();
		Strategy<PackageMaintainer> strategy = new SuccessfulStrategy<PackageMaintainer>(maintainer,
				newsfeedEventService, packageMaintainerService, user.get());

		when(userService.findByLogin("user")).thenReturn(user);
		when(packageMaintainerService.findById(any(Integer.class))).thenReturn(Optional.of(maintainer));
		when(securityMediator.isAuthorizedToEdit(maintainer, user.get())).thenReturn(true);
		when(strategyFactory.updatePackageMaintainerStrategy(eq(maintainer), any(User.class),
				any(PackageMaintainer.class))).thenReturn(strategy);

		mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/package-maintainers/" + id)
				.contentType("application/json-patch+json").content(patchJson))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(content().json(Files.readString(Path.of(ERROR_PACKAGEMAINTAINER_MALFORMED_PATCH))));
	}

	@Test
	@WithMockUser(authorities = { "user", "repositorymaintainer" })
	public void patchPackageMaintainer_returns500_whenStrategyFailure() throws Exception {
		String patchJson = "[{\"op\": \"replace\", \"path\":\"/packageName\",\"value\":\"neeeeeew_package\"}]";
		final Optional<User> user = UserTestFixture.GET_FIXTURE_ADMIN();
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		Strategy<PackageMaintainer> strategy = Mockito.spy(new FailureStrategy<PackageMaintainer>(maintainer,
				newsfeedEventService, packageMaintainerService, user.get()));

		when(userService.findByLogin("user")).thenReturn(user);
		when(packageMaintainerService.findById(any(Integer.class))).thenReturn(Optional.of(maintainer));
		when(securityMediator.isAuthorizedToEdit(maintainer, user.get())).thenReturn(true);
		when(strategyFactory.updatePackageMaintainerStrategy(eq(maintainer), any(User.class),
				any(PackageMaintainer.class))).thenReturn(strategy);
		when(packageMaintainerValidator.supports(PackageMaintainer.class)).thenReturn(true);
		doReturn(Optional.of(repository)).when(commonRepositoryService).findById(123);
		when(userService.findById(111)).thenReturn(user);

		mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/package-maintainers/" + id)
				.contentType("application/json-patch+json").content(patchJson))
				.andExpect(status().isInternalServerError())
				.andExpect(content().json(Files.readString(Path.of(ERROR_PATCH_PATH))));

		verify(strategy, times(1)).perform();
	}

	@Test
	@WithMockUser(authorities = { "user", "repositorymaintainer" })
	public void patchPackageMaintainer() throws Exception {
		String patchJson = "[{\"op\": \"replace\",\"path\":\"/packageName\",\"value\":\"neeeeeew_package\"}]";
		final PackageMaintainer maintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER();
		final Integer id = maintainer.getId();
		final PackageMaintainer updated = new PackageMaintainer(maintainer);

		updated.setId(maintainer.getId());
		updated.setPackageName("neeeeeew_package");
		user.get().setId(111);

		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		Strategy<PackageMaintainer> strategy = Mockito.spy(new SuccessfulStrategy<PackageMaintainer>(updated,
				newsfeedEventService, packageMaintainerService, user.get()));

		when(userService.findByLogin("user")).thenReturn(user);
		when(packageMaintainerService.findById(any(Integer.class))).thenReturn(Optional.of(maintainer));
		when(securityMediator.isAuthorizedToEdit(maintainer, user.get())).thenReturn(true);
		when(strategyFactory.updatePackageMaintainerStrategy(any(), any(), any())).thenReturn(strategy);
		doNothing().when(packageMaintainerValidator).validate(any(), any());
		when(packageMaintainerValidator.supports(PackageMaintainer.class)).thenReturn(true);
		doReturn(Optional.of(repository)).when(commonRepositoryService).findById(123);
		when(userService.findById(111)).thenReturn(user);

		mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/package-maintainers/" + id)
				.contentType("application/json-patch+json").content(patchJson)).andExpect(status().isOk())
				.andExpect(content().json(Files.readString(Path.of(EXAMPLE_PACKAGEMAINTAINER_PATCHED_PATH))));

		verify(strategy, times(1)).perform();
	}
}

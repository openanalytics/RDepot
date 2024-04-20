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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.python.api.v2.controllers.PythonRepositoryController;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.test.context.ApiTestConfig;
import eu.openanalytics.rdepot.test.fixture.PythonRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;

@ContextConfiguration(classes = {ApiTestConfig.class})
@WebMvcTest(PythonRepositoryController.class)
@ActiveProfiles("apiv2declarative")
public class PythonRepositoryControllerDeclarativeTest extends ApiV2ControllerUnitTest {
	
	public static final String JSON_PATH = ClassLoader.getSystemClassLoader().getResource("unit/jsons").getPath();
	public static final String ERROR_DECLARATIVE_MODE_PATH = JSON_PATH + "/error_declarative_mode.json";
	public static final String EXAMPLE_NEW_REPOSITORY_PATH = JSON_PATH + "/example_new_repository.json";
	
	private Optional<User> user;
	
	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	WebApplicationContext webApplicationContext;
	
	@BeforeEach
	public void initEach() {
		user = UserTestFixture.GET_FIXTURE_ADMIN();
	}
	
	@Test
	@WithMockUser(authorities = {"user", "repositorymaintainer"})
	public void patchRepository_returns405_whenDeclarativeModeIsEnabled() throws Exception {
		final Integer ID = 123;
		final PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/serverAddress\",\"value\":\"127.0.0.1\"}]";
		
		when(pythonRepositoryService.findById(ID)).thenReturn(Optional.of(repository));
		when(userService.findByLogin("user")).thenReturn(user);
		when(securityMediator.isAuthorizedToEdit(any(Repository.class), eq(user.get()))).thenReturn(true);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/python/repositories/" + ID)
					.content(patchJson)
					.contentType("application/json-patch+json"))
			.andExpect(status().isMethodNotAllowed())
			.andExpect(content().json(Files.readString(Path.of(ERROR_DECLARATIVE_MODE_PATH))));
	}
	
	@Test
	@WithMockUser(authorities = {"user", "admin"})
	public void createRepository_returns405_whenDeclarativeModeIsEnabled() throws Exception {
		
		final Path path = Path.of(EXAMPLE_NEW_REPOSITORY_PATH);
		final String exampleJson = Files.readString(path);
		
		when(userService.isAdmin(user.get())).thenReturn(true);
		when(userService.findByLogin("user")).thenReturn(user);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.post("/api/v2/manager/python/repositories")
					.content(exampleJson)
					.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isMethodNotAllowed())
			.andExpect(content().json(Files.readString(Path.of(ERROR_DECLARATIVE_MODE_PATH))));
	}

}
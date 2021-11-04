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

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.api.v2.controller.RRepositoryController;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.test.context.TestConfig;
import eu.openanalytics.rdepot.test.fixture.RRepositoryTestFixture;

@ContextConfiguration(classes = {TestConfig.class})
@WebMvcTest(RRepositoryController.class)
@ActiveProfiles("apiv2declarative")
public class RRepositoryControllerDeclarativeTest extends ApiV2ControllerUnitTest {
	
	public static final String JSON_PATH = ClassLoader.getSystemClassLoader().getResource("unit/jsons").getPath();
	public static final String ERROR_DECLARATIVE_MODE_PATH = JSON_PATH + "/error_declarative_mode.json";
	public static final String EXAMPLE_NEW_REPOSITORY_PATH = JSON_PATH + "/example_new_repository.json";

	
	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	WebApplicationContext webApplicationContext;
	
	@Test
	public void patchRepository_returns405_whenDeclarativeModeIsEnabled() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		final Integer ID = 123;
		final Repository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		final String patchJson = "[{\"op\": \"replace\",\"path\":\"/serverAddress\",\"value\":\"127.0.0.1\"}]";
		
		when(repositoryService.findById(ID)).thenReturn(repository);
		when(userService.findByLoginWithRepositoryMaintainers(principal.getName())).thenReturn(user);
		when(userService.isAuthorizedToEdit(any(Repository.class), eq(user))).thenReturn(true);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.patch("/api/v2/manager/r/repositories/" + ID)
					.content(patchJson)
					.contentType("application/json-patch+json")
					.principal(principal))
			.andExpect(status().isMethodNotAllowed())
			.andExpect(content().json(Files.readString(Path.of(ERROR_DECLARATIVE_MODE_PATH))));
	}
	
	@Test
	public void createRepository_returns405_whenDeclarativeModeIsEnabled() throws Exception {
		final User user = getAdminAndAuthenticate(userService);
		final Principal principal = getMockPrincipal(user);
		
		final Path path = Path.of(EXAMPLE_NEW_REPOSITORY_PATH);
		final String exampleJson = Files.readString(path);
		
		when(userService.findByLogin(principal.getName())).thenReturn(user);
		when(userService.isAdmin(user)).thenReturn(true);
		
		mockMvc
			.perform(MockMvcRequestBuilders
					.post("/api/v2/manager/r/repositories")
					.content(exampleJson)
					.contentType(MediaType.APPLICATION_JSON)
					.principal(principal))
			.andExpect(status().isMethodNotAllowed())
			.andExpect(content().json(Files.readString(Path.of(ERROR_DECLARATIVE_MODE_PATH))));
	}

}
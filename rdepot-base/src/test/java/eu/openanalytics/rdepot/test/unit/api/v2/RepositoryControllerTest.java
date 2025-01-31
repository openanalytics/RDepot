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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2RepositoryController;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.test.context.ApiTestConfig;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

@ContextConfiguration(classes = {ApiTestConfig.class})
@WebMvcTest(ApiV2RepositoryController.class)
@ActiveProfiles("apiv2")
@WebAppConfiguration
@Import({ApiTestConfig.class})
public class RepositoryControllerTest extends ApiV2ControllerUnitTest {

    public static final String EXAMPLE_REPOSITORIES_PATH = JSON_PATH + "/example_common_repositories.json";
    public static final String EXAMPLE_REPOSITORY_PATH = JSON_PATH + "/example_common_repository.json";
    public static final String EXAMPLE_NEW_REPOSITORY_PATH = JSON_PATH + "/example_new_repository.json";
    public static final String ERROR_REPOSITORY_NOT_FOUND = JSON_PATH + "/error_repository_notfound.json";
    public static final String EXAMPLE_REPOSITORY_CREATED = JSON_PATH + "/example_repository_created.json";
    public static final String ERROR_VALIDATION_REPOSITORY_NAME_PATH =
            JSON_PATH + "/error_validation_repository_name.json";
    public static final String EXAMPLE_REPOSITORY_PATCHED_PATH = JSON_PATH + "/example_repository_patched.json";
    public static final String ERROR_REPOSITORY_NOT_FOUND_PATH = JSON_PATH + "/error_repository_notfound.json";
    public static final String ERROR_REPOSITORY_MALFORMED_PATCH = JSON_PATH + "/error_repository_malformed_patch.json";

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
    ApiV2RepositoryController repositoryController;

    @Autowired
    WebApplicationContext webApplicationContext;

    @BeforeEach
    public void initEach() {
        user = Optional.of(UserTestFixture.GET_REGULAR_USER());
    }

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void getAllRepositories() throws Exception {

        when(userService.findActiveByLogin("user")).thenReturn(user);
        when(userService.isAdmin(user.get())).thenReturn(true);
        when(commonRepositoryService.findAllBySpecification(any(), any()))
                .thenReturn(RepositoryTestFixture.GET_EXAMPLE_REPOSITORIES_PAGED());
        when(securityMediator.isAuthorizedToEdit(any(Repository.class), eq(user.get())))
                .thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/repositories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_REPOSITORIES_PATH))));
    }

    @Test
    public void getAllRepositories_returns401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/repositories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void getRepository() throws Exception {
        final Repository repository = RepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        final Integer ID = repository.getId();

        when(userService.findActiveByLogin("user")).thenReturn(user);
        when(commonRepositoryService.findById(ID)).thenReturn(Optional.of(repository));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/repositories/" + ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_REPOSITORY_PATH))));
    }

    @Test
    public void getRepository_returns401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/repositories/" + 1234)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void getRepository_returns404_whenRepositoryIsNotFound() throws Exception {

        when(commonRepositoryService.findById(any(Integer.class))).thenReturn(Optional.ofNullable(null));
        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(UserTestFixture.GET_ADMIN()));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/repositories/" + 123)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(Files.readString(Path.of(ERROR_REPOSITORY_NOT_FOUND))));
    }
}

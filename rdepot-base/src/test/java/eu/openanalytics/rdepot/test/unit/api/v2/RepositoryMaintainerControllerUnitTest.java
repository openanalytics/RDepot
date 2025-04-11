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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2RepositoryMaintainerController;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.test.context.ApiTestConfig;
import eu.openanalytics.rdepot.test.fixture.RepositoryMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.TestUtils;
import eu.openanalytics.rdepot.test.unit.api.v2.mockstrategies.FailureStrategy;
import eu.openanalytics.rdepot.test.unit.api.v2.mockstrategies.SuccessfulStrategy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.WebApplicationContext;

@ContextConfiguration(classes = {ApiTestConfig.class})
@WebMvcTest(ApiV2RepositoryMaintainerController.class)
@ActiveProfiles("apiv2")
@WebAppConfiguration
@Import({ApiTestConfig.class})
public class RepositoryMaintainerControllerUnitTest extends ApiV2ControllerUnitTest {

    private static final String EXAMPLE_REPOSITORYMAINTAINERS_PATH = JSON_PATH + "/example_repositorymaintainers.json";
    private static final String ERROR_REPOSITORY_MAINTAINER_NOT_FOUND_PATH =
            JSON_PATH + "/error_repositorymaintainer_notfound.json";
    private static final String EXAMPLE_REPOSITORYMAINTAINER_PATH = JSON_PATH + "/example_repositorymaintainer.json";
    private static final String EXAMPLE_NEW_REPOSITORYMAINTAINER_PATH =
            JSON_PATH + "/example_new_repositorymaintainer.json";
    private static final String ERROR_VALIDATION_REPOSITORYMAINTAINER_PATH =
            JSON_PATH + "/error_validation_repositorymaintainer.json";
    private static final String EXAMPLE_REPOSITORYMAINTAINER_CREATED_PATH =
            JSON_PATH + "/example_repositorymaintainer_created.json";
    private static final String ERROR_REPOSITORYMAINTAINER_MALFORMED_PATCH_PATH =
            JSON_PATH + "/error_repositorymaintainer_malformed_patch.json";
    private static final String EXAMPLE_REPOSITORYMAINTAINER_PATCHED_PATH =
            JSON_PATH + "/example_repositorymaintainer_patched.json";
    public static final String EDITING_DELETED_RESOURCE_PATH = JSON_PATH + "/editing_deleted_resource.json";

    private User user;

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

    @BeforeEach
    public void initEach() {
        user = UserTestFixture.GET_ADMIN();
    }

    @Test
    @WithMockUser(authorities = {"admin", "user"})
    public void getAllRepositoryMaintainers() throws Exception {
        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(repositoryMaintainerService.findAllBySpecification(any(), any()))
                .thenReturn(RepositoryMaintainerTestFixture.GET_EXAMPLE_REPOSITORY_MAINTAINERS_PAGED());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/repository-maintainers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_REPOSITORYMAINTAINERS_PATH))));
    }

    @Test
    public void getAllRepositoryMaintainers_returns401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/repository-maintainers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser
    public void getAllRepositoryMaintainers_returns403_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/repository-maintainers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"admin", "user"})
    public void getRepositoryMaintainer_returns404_whenRepositoryMaintainerIsNotFound() throws Exception {
        final int ID = 123;

        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(repositoryMaintainerService.findById(ID)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/repository-maintainers/" + ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(Files.readString(Path.of(ERROR_REPOSITORY_MAINTAINER_NOT_FOUND_PATH))));
    }

    @Test
    public void getRepositoryMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/repository-maintainers/" + 123)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser
    public void getRepositoryMaintainer_returns403_whenUserIsNotAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/repository-maintainers/" + 123)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"admin", "user"})
    public void getRepositoryMaintainer() throws Exception {
        final RepositoryMaintainer repositoryMaintainer =
                RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINER();
        final int ID = repositoryMaintainer.getId();

        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(userService.isAdmin(user)).thenReturn(true);
        when(repositoryMaintainerService.findById(ID)).thenReturn(Optional.of(repositoryMaintainer));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/repository-maintainers/" + ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_REPOSITORYMAINTAINER_PATH))));
    }

    @Test
    public void createRepositoryMaintainer_throws401_whenUserIsNotAuthenticated() throws Exception {
        final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_REPOSITORYMAINTAINER_PATH));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/repository-maintainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exampleJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser
    public void createRepositoryMaintainer_throws403_whenUserIsNotAdmin() throws Exception {
        final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_REPOSITORYMAINTAINER_PATH));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/repository-maintainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exampleJson))
                .andExpect(status().isForbidden())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"admin", "user"})
    public void createRepositoryMaintainer_throws422_whenValidationFails() throws Exception {
        final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_REPOSITORYMAINTAINER_PATH));
        Repository repository = RepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        doReturn(Optional.of(repository)).when(repositoryService).findById(201);
        when(userService.findById(111)).thenReturn(Optional.of(user));
        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(repositoryMaintainerValidator.supports(RepositoryMaintainer.class)).thenReturn(true);
        doAnswer((Answer<Object>) invocation -> {
                    BindingResult bindingResult = invocation.getArgument(1);
                    bindingResult.rejectValue("repository", MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
                    return null;
                })
                .when(repositoryMaintainerValidator)
                .validate(any(), any());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/repository-maintainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exampleJson))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(Files.readString(Path.of(ERROR_VALIDATION_REPOSITORYMAINTAINER_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"admin", "user"})
    public void createRepositoryMaintainer() throws Exception {
        final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_REPOSITORYMAINTAINER_PATH));
        final RepositoryMaintainer created = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINER();
        Repository repository = RepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(201);
        created.setId(100);
        created.setRepository(repository);
        user.setId(111);

        Strategy<RepositoryMaintainer> strategy =
                Mockito.spy(new SuccessfulStrategy<>(created, newsfeedEventService, repositoryMaintainerService, user));

        when(strategyFactory.createRepositoryMaintainerStrategy(any(), any())).thenReturn(strategy);
        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        doNothing().when(repositoryMaintainerValidator).validate(any(), any());
        when(repositoryMaintainerValidator.supports(RepositoryMaintainer.class)).thenReturn(true);
        doReturn(Optional.of(repository)).when(repositoryService).findById(201);
        when(userService.findById(111)).thenReturn(Optional.of(user));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/repository-maintainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exampleJson))
                .andExpect(status().isCreated())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_REPOSITORYMAINTAINER_CREATED_PATH))));

        verify(strategy, times(1)).perform();
    }

    @Test
    @WithMockUser(authorities = {"admin", "user"})
    public void createRepositoryMaintainer_returns500_whenCreationFails() throws Exception {
        final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_REPOSITORYMAINTAINER_PATH));
        final RepositoryMaintainer created = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINER();

        Repository repository = RepositoryTestFixture.GET_EXAMPLE_REPOSITORY();

        Strategy<RepositoryMaintainer> strategy =
                Mockito.spy(new FailureStrategy<>(created, newsfeedEventService, repositoryMaintainerService, user));

        when(strategyFactory.createRepositoryMaintainerStrategy(any(), any())).thenReturn(strategy);
        when(repositoryMaintainerValidator.supports(RepositoryMaintainer.class)).thenReturn(true);
        doReturn(Optional.of(repository)).when(repositoryService).findById(201);
        when(userService.findById(111)).thenReturn(Optional.of(user));
        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        doNothing().when(repositoryMaintainerValidator).validate(any(), any());
        when(repositoryMaintainerValidator.supports(RepositoryMaintainer.class)).thenReturn(true);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/repository-maintainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exampleJson))
                .andExpect(status().isInternalServerError());

        TestUtils.matchInternalServerErrorCreate(result);
    }

    @Test
    public void deleteRepositoryMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/repository-maintainers/" + 123)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"admin", "user"})
    public void deleteRepositoryMaintainer_returns404_whenRepositoryMaintainerIsNotFound() throws Exception {
        final int ID = 123;

        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(repositoryMaintainerService.findOneDeleted(ID)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/repository-maintainers/" + 123)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(Files.readString(Path.of(ERROR_REPOSITORY_MAINTAINER_NOT_FOUND_PATH))));
    }

    @Test
    @WithMockUser
    public void deleteRepositoryMaintainer_returns403_whenUserIsNotAdmin() throws Exception {
        final int ID = 123;

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/repository-maintainers/" + ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"admin", "user"})
    public void deleteRepositoryMaintainer() throws Exception {
        final RepositoryMaintainer maintainer = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINER();
        maintainer.setDeleted(true);
        final int ID = maintainer.getId();

        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(repositoryMaintainerService.findOneDeleted(ID)).thenReturn(Optional.of(maintainer));
        doNothing().when(repositoryMaintainerDeleter).delete(maintainer);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/repository-maintainers/" + ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(repositoryMaintainerDeleter, times(1)).delete(maintainer);
    }

    @Test
    public void patchRepositoryMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
        String patchJson = "[{\"op\": \"replace\",\"path\":\"/repositoryId\",\"value\":1234}]";

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/repository-maintainers/" + 123)
                        .contentType("application/json-patch+json")
                        .content(patchJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"admin", "user"})
    public void patchRepositoryMaintainer_returns404_whenRepositoryMaintainerIsNotFound() throws Exception {
        String patchJson = "[{\"op\": \"replace\",\"path\":\"/repositoryId\",\"value\":1234}]";
        final int ID = 123;

        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(repositoryMaintainerService.findById(ID)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/repository-maintainers/" + ID)
                        .contentType("application/json-patch+json")
                        .content(patchJson))
                .andExpect(status().isNotFound())
                .andExpect(content().json(Files.readString(Path.of(ERROR_REPOSITORY_MAINTAINER_NOT_FOUND_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void patchRepositoryMaintainer_returns403_whenUserIsNotAdmin() throws Exception {
        String patchJson = "[{\"op\": \"replace\",\"path\":\"/repositoryId\",\"value\":1234}]";
        final int ID = 123;

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/repository-maintainers/" + ID)
                        .contentType("application/json-patch+json")
                        .content(patchJson))
                .andExpect(status().isForbidden())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void patchRepositoryMaintainer_returns405_whenRepositoryMaintainerIsDeleted() throws Exception {
        String patchJson = "[{\"op\": \"replace\", \"path\":\"/repositoryId\",\"value\":\"1234\"}]";
        final RepositoryMaintainer maintainer = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINER();
        final int ID = maintainer.getId();
        user.setId(111);
        maintainer.setDeleted(true);

        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(repositoryMaintainerService.findById(ID)).thenReturn(Optional.of(maintainer));

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/repository-maintainers/" + ID)
                        .contentType("application/json-patch+json")
                        .content(patchJson))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().json(Files.readString(Path.of(EDITING_DELETED_RESOURCE_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void patchRepositoryMaintainer_returns422_whenPatchIsIncorrect() throws Exception {
        String patchJson = "[{\"op\": \"replace\",\"path\":\"/repositoryyyyyyId\",\"value\":1234}]";
        final RepositoryMaintainer maintainer = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINER();
        final int ID = maintainer.getId();

        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(repositoryMaintainerService.findById(ID)).thenReturn(Optional.of(maintainer));

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/repository-maintainers/" + ID)
                        .contentType("application/json-patch+json")
                        .content(patchJson))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(Files.readString(Path.of(ERROR_REPOSITORYMAINTAINER_MALFORMED_PATCH_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void patchRepositoryMaintainer() throws Exception {
        String patchJson = "[{\"op\": \"replace\",\"path\":\"/repository/id\",\"value\":1234}]";
        final RepositoryMaintainer maintainer = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINER();
        final int ID = maintainer.getId();
        user.setId(111);
        maintainer.getRepository().setId(1234);

        Repository repository = RepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        Strategy<RepositoryMaintainer> strategy = Mockito.spy(
                new SuccessfulStrategy<>(maintainer, newsfeedEventService, repositoryMaintainerService, user));

        when(strategyFactory.updateRepositoryMaintainerStrategy(any(), any(), any()))
                .thenReturn(strategy);
        when(repositoryMaintainerValidator.supports(RepositoryMaintainer.class)).thenReturn(true);
        doReturn(Optional.of(repository)).when(repositoryService).findById(1234);
        when(userService.findById(111)).thenReturn(Optional.of(user));
        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(repositoryMaintainerService.findById(ID)).thenReturn(Optional.of(maintainer));
        doNothing().when(repositoryMaintainerValidator).validate(any(), any());
        when(repositoryMaintainerValidator.supports(RepositoryMaintainer.class)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/repository-maintainers/" + ID)
                        .contentType("application/json-patch+json")
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_REPOSITORYMAINTAINER_PATCHED_PATH))));
    }
}

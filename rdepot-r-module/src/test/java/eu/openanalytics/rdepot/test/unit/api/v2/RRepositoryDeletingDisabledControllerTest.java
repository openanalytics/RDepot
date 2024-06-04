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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.r.api.v2.controllers.RRepositoryController;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.test.context.ApiTestConfig;
import eu.openanalytics.rdepot.test.fixture.RRepositoryTestFixture;
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
import org.springframework.validation.Errors;
import org.springframework.web.context.WebApplicationContext;

@ContextConfiguration(classes = {ApiTestConfig.class})
@WebMvcTest(RRepositoryController.class)
@ActiveProfiles("apiv2deletiondisabled")
@WebAppConfiguration
@Import({ApiTestConfig.class})
public class RRepositoryDeletingDisabledControllerTest extends ApiV2ControllerUnitTest {

    private static final String JSON_PATH = "src/test/resources/unit/jsons";
    private static final String EXAMPLE_REPOSITORIES_PATH = JSON_PATH + "/example_repositories.json";
    private static final String EXAMPLE_REPOSITORY_PATH = JSON_PATH + "/example_repository.json";
    private static final String EXAMPLE_REPOSITORY_AS_ADMIN_PATH = JSON_PATH + "/example_repository_as_admin.json";
    private static final String EXAMPLE_NEW_REPOSITORY_PATH = JSON_PATH + "/example_new_repository.json";
    private static final String ERROR_REPOSITORY_NOT_FOUND = JSON_PATH + "/error_repository_notfound.json";
    private static final String EXAMPLE_REPOSITORY_CREATED = JSON_PATH + "/example_repository_created.json";
    private static final String EXAMPLE_REPOSITORY_PATCHED_PATH = JSON_PATH + "/example_repository_patched.json";
    private static final String ERROR_REPOSITORY_NOT_FOUND_PATH = JSON_PATH + "/error_repository_notfound.json";
    private static final String ERROR_REPOSITORY_MALFORMED_PATCH = JSON_PATH + "/error_repository_malformed_patch.json";
    private static final String ERROR_REPOSITORY_DELETING_DISABLED =
            JSON_PATH + "/error_repository_deletion_disabled.json";

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
    RRepositoryController rRepositoryController;

    @Autowired
    WebApplicationContext webApplicationContext;

    @BeforeEach
    public void initEach() {
        user = Optional.of(UserTestFixture.GET_ADMIN());
    }

    @Test
    @WithMockUser(authorities = {"admin", "user"})
    public void getAllRepositories() throws Exception {

        when(userService.findByLogin("user")).thenReturn(user);

        when(userService.isAdmin(user.get())).thenReturn(true);
        when(securityMediator.isAuthorizedToEdit(any(RRepository.class), eq(user.get())))
                .thenReturn(true);
        when(rRepositoryService.findAllBySpecification(any(), any()))
                .thenReturn(RRepositoryTestFixture.GET_EXAMPLE_REPOSITORIES_PAGED());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/r/repositories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_REPOSITORIES_PATH))));
    }

    @Test
    public void getAllRepositories_returns401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/r/repositories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void getRepository() throws Exception {
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        final Integer ID = repository.getId();

        when(rRepositoryService.findById(ID)).thenReturn(Optional.of(repository));
        when(userService.findByLogin("user")).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/r/repositories/" + ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_REPOSITORY_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void getRepository_asAdmin() throws Exception {
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        final Integer ID = repository.getId();

        when(rRepositoryService.findById(ID)).thenReturn(Optional.of(repository));
        when(userService.findByLogin("user")).thenReturn(user);
        when(securityMediator.isAuthorizedToEdit(repository, user.get())).thenReturn(true);
        when(userService.isAdmin(user.get())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/r/repositories/" + ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_REPOSITORY_AS_ADMIN_PATH))));
    }

    @Test
    public void getRepository_returns401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/r/repositories/" + 123)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void getRepository_returns404_whenRepositoryIsNotFound() throws Exception {

        when(rRepositoryService.findById(any(Integer.class))).thenReturn(Optional.ofNullable(null));
        when(userService.findByLogin("user")).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/r/repositories/" + 123)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(Files.readString(Path.of(ERROR_REPOSITORY_NOT_FOUND))));
    }

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void createRepository() throws Exception {

        final Path path = Path.of(EXAMPLE_NEW_REPOSITORY_PATH);
        final String exampleJson = Files.readString(path);

        final RRepository createdRepository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY(100);
        Strategy<RRepository> strategy = Mockito.spy(new SuccessfulStrategy<RRepository>(
                createdRepository, newsfeedEventService, rRepositoryService, user.get()));

        when(rStrategyFactory.createRepositoryStrategy(any(), eq(user.get()))).thenReturn(strategy);
        when(userService.findById(anyInt())).thenReturn(user);
        when(userService.isAdmin(user.get())).thenReturn(true);
        when(userService.findByLogin("user")).thenReturn(user);
        when(rRepositoryValidator.supports(RRepository.class)).thenReturn(true);
        doNothing().when(rRepositoryValidator).validate(any(), any());
        when(rRepositoryValidator.supports(Repository.class)).thenReturn(true);
        when(securityMediator.isAuthorizedToEdit(any(RRepository.class), eq(user.get())))
                .thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/r/repositories")
                        .content(exampleJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_REPOSITORY_CREATED))));

        verify(strategy, times(1)).perform();
    }

    @Test
    public void createRepository_returns401_whenUserIsNotAuthenticated() throws Exception {
        final Path path = Path.of(EXAMPLE_NEW_REPOSITORY_PATH);
        final String exampleJson = Files.readString(path);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/repositories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exampleJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"user"})
    public void createRepository_returns403_whenUserIsNotAuthorized() throws Exception {
        final Path path = Path.of(EXAMPLE_NEW_REPOSITORY_PATH);
        final String exampleJson = Files.readString(path);

        when(userService.findByLogin("user")).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/r/repositories")
                        .contentType("application/json")
                        .content(exampleJson))
                .andExpect(status().isForbidden())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void createRepository_returns422_whenRepositoryValidationFails() throws Exception {

        final Path path = Path.of(EXAMPLE_NEW_REPOSITORY_PATH);
        final String exampleJson = Files.readString(path);

        when(userService.findByLogin("user")).thenReturn(user);
        when(userService.isAdmin(user.get())).thenReturn(true);
        doAnswer(invocation -> {
                    Errors errors = (Errors) invocation.getArgument(1);

                    errors.rejectValue("name", MessageCodes.ERROR_DUPLICATE_NAME);
                    return null;
                })
                .when(rRepositoryValidator)
                .validate(any(), any());
        when(rRepositoryValidator.supports(RRepository.class)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/r/repositories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exampleJson))
                .andExpect(status().isUnprocessableEntity()); // TODO: #32880 Unify validation errors approach and
        // uncomment
        //			.andExpect(
        //					content().json(Files.readString(Path.of(ERROR_VALIDATION_REPOSITORY_NAME_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void createRepository_returns500_whenCreationFails() throws Exception {

        final Path path = Path.of(EXAMPLE_NEW_REPOSITORY_PATH);
        final String exampleJson = Files.readString(path);

        final RRepository newRepository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY(0);
        Strategy<RRepository> strategy = Mockito.spy(
                new FailureStrategy<RRepository>(newRepository, newsfeedEventService, rRepositoryService, user.get()));

        when(userService.isAdmin(user.get())).thenReturn(true);
        when(userService.findByLogin("user")).thenReturn(user);
        when(rStrategyFactory.createRepositoryStrategy(any(), eq(user.get()))).thenReturn(strategy);
        doNothing().when(rRepositoryValidator).validate(any(), any());
        when(rRepositoryValidator.supports(RRepository.class)).thenReturn(true);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/r/repositories")
                        .content(exampleJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        TestUtils.matchInternalServerErrorCreate(result);

        verify(strategy, times(1)).perform();
    }

    @Test
    @WithMockUser(authorities = {"user", "repositorymaintainer"})
    public void patchRepository() throws Exception {
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        final Integer ID = repository.getId();
        Strategy<RRepository> strategy = Mockito.spy(
                new SuccessfulStrategy<RRepository>(repository, newsfeedEventService, rRepositoryService, user.get()));

        final String patchJson = "[{\"op\": \"replace\",\"path\":\"/serverAddress\",\"value\":\"127.0.0.1\"}]";
        when(rRepositoryService.findById(ID)).thenReturn(Optional.of(repository));
        when(userService.findByLogin("user")).thenReturn(user);
        when(securityMediator.isAuthorizedToEdit(any(RRepository.class), eq(user.get())))
                .thenReturn(true);
        when(rStrategyFactory.updateRepositoryStrategy(any(), eq(user.get()), any()))
                .thenReturn(strategy);
        doNothing().when(rRepositoryValidator).validate(any(), any());
        when(rRepositoryValidator.supports(RRepository.class)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/repositories/" + ID)
                        .content(patchJson)
                        .contentType("application/json-patch+json"))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_REPOSITORY_PATCHED_PATH))));

        verify(strategy, times(1)).perform();
    }

    @Test
    @WithMockUser(authorities = {"user", "repositorymaintainer"})
    public void patchRepository_returns405_whenRepositoryDeleting() throws Exception {
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        final Integer ID = repository.getId();
        Strategy<RRepository> strategy = Mockito.spy(
                new SuccessfulStrategy<RRepository>(repository, newsfeedEventService, rRepositoryService, user.get()));

        final String patchJson = "[{\"op\": \"replace\",\"path\":\"/deleted\",\"value\":true}]";
        when(rRepositoryService.findById(ID)).thenReturn(Optional.of(repository));
        when(userService.findByLogin("user")).thenReturn(user);
        when(securityMediator.isAuthorizedToEdit(any(RRepository.class), eq(user.get())))
                .thenReturn(true);
        when(rStrategyFactory.updateRepositoryStrategy(any(), eq(user.get()), any()))
                .thenReturn(strategy);
        doNothing().when(rRepositoryValidator).validate(any(), any());
        when(rRepositoryValidator.supports(RRepository.class)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/repositories/" + ID)
                        .content(patchJson)
                        .contentType("application/json-patch+json"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().json(Files.readString(Path.of(ERROR_REPOSITORY_DELETING_DISABLED))));

        verify(strategy, times(0)).perform();
    }

    @Test
    public void patchRepository_returns401_whenUserIsNotAuthenticated() throws Exception {
        String patchJson = "[{\"op\": \"replace\",\"path\":\"/serverAddress\",\"value\":\"127.0.0.1\"}]";

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/repositories/" + 123)
                        .contentType("application/json-patch+json")
                        .content(patchJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void patchRepository_returns403_whenUserIsNotAuthorized() throws Exception {

        when(userService.findByLogin("user")).thenReturn(user);
        when(securityMediator.isAuthorizedToEdit(any(Repository.class), eq(user.get())))
                .thenReturn(false);

        String patchJson = "[{\"op\": \"replace\",\"path\":\"/serverAddress\",\"value\":\"127.0.0.1\"}]";

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/repositories/" + 123)
                        .contentType("application/json-patch+json")
                        .content(patchJson))
                .andExpect(status().isForbidden())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "repositorymaintainer"})
    public void patchRepository_returns404_whenRepositoryIsNotFound() throws Exception {

        when(rRepositoryService.findById(any(Integer.class))).thenReturn(Optional.ofNullable(null));
        when(userService.findByLogin("user")).thenReturn(user);

        String patchJson = "[{\"op\": \"replace\",\"path\":\"/serverAddress\",\"value\":\"127.0.0.1\"}]";

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/repositories/" + 123)
                        .contentType("application/json-patch+json")
                        .content(patchJson))
                .andExpect(status().isNotFound())
                .andExpect(content().json(Files.readString(Path.of(ERROR_REPOSITORY_NOT_FOUND_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "repositorymaintainer"})
    public void patchRepository_returns422_whenPatchIsIncorrect() throws Exception {
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        final Integer ID = repository.getId();

        final String patchJson = "[{\"op\": \"replace\",\"path\":\"/nameeeeee\",\"value\":\"Test Repo 123\"}]";

        when(rRepositoryService.findById(ID)).thenReturn(Optional.of(repository));
        when(userService.findByLogin("user")).thenReturn(user);
        when(securityMediator.isAuthorizedToEdit(any(Repository.class), eq(user.get())))
                .thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/repositories/" + ID)
                        .contentType("application/json-patch+json")
                        .content(patchJson))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(Files.readString(Path.of(ERROR_REPOSITORY_MALFORMED_PATCH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "repositorymaintainer"})
    public void patchRepository_returns422_whenRepositoryValidationFails() throws Exception {
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        final Integer ID = repository.getId();
        final String patchJson = "[{\"op\": \"replace\",\"path\":\"/name\",\"value\":\"Test Repo 123\"}]";

        when(rRepositoryService.findById(ID)).thenReturn(Optional.of(repository));
        when(userService.findByLogin("user")).thenReturn(user);
        when(securityMediator.isAuthorizedToEdit(repository, user.get())).thenReturn(true);
        when(rRepositoryValidator.supports(RRepository.class)).thenReturn(true);
        doAnswer(invocation -> {
                    Errors errors = (Errors) invocation.getArgument(1);

                    errors.rejectValue("name", MessageCodes.ERROR_DUPLICATE_NAME);
                    return null;
                })
                .when(rRepositoryValidator)
                .validate(any(), any());
        when(rRepositoryValidator.supports(Repository.class)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/r/repositories/" + ID)
                        .contentType("application/json-patch+json")
                        .content(patchJson))
                .andExpect(status().isUnprocessableEntity()); // TODO: #32880 Unify validation errors approach and
        // uncomment
        //		.andExpect(
        //				content().json(Files.readString(Path.of(ERROR_VALIDATION_REPOSITORY_NAME_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void deleteRepository_returns405_whenRepositoryDeletingIsDisabled() throws Exception {
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        final Integer ID = repository.getId();

        repository.setDeleted(true);

        when(rRepositoryService.findById(ID)).thenReturn(Optional.of(repository));
        doNothing().when(rRepositoryDeleter).delete(repository);
        when(userService.findByLogin("user")).thenReturn(user);
        when(userService.isAdmin(user.get())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/r/repositories/" + ID))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().json(Files.readString(Path.of(ERROR_REPOSITORY_DELETING_DISABLED))));

        verify(rRepositoryDeleter, times(0)).delete(repository);
    }

    @Test
    public void deleteRepository_returns401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/r/repositories/" + 123))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "user")
    public void deleteRepository_returns403_whenUserIsNotAuthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/r/repositories/" + 123))
                .andExpect(status().isForbidden())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void deleteRepository_returns404_whenRepositoryIsNotFound() throws Exception {

        when(rRepositoryService.findById(any(Integer.class))).thenReturn(Optional.ofNullable(null));
        when(userService.findByLogin("user")).thenReturn(user);
        when(userService.isAdmin(user.get())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/r/repositories/" + 123))
                .andExpect(status().isNotFound())
                .andExpect(content().json(Files.readString(Path.of(ERROR_REPOSITORY_NOT_FOUND))));
    }
}

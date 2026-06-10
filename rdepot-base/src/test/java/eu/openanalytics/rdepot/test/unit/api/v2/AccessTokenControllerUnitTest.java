/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2AccessTokenController;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2NewsfeedEventController;
import eu.openanalytics.rdepot.base.entities.AccessToken;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.test.context.ApiTestConfig;
import eu.openanalytics.rdepot.test.fixture.AccessTokenTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.TestUtils;
import eu.openanalytics.rdepot.test.unit.api.v2.mockstrategies.FailureStrategy;
import eu.openanalytics.rdepot.test.unit.api.v2.mockstrategies.SuccessfulStrategy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

@ContextConfiguration(classes = {ApiTestConfig.class, AccessTokenControllerUnitTest.NoOpTransactionManagerConfig.class})
@WebMvcTest(ApiV2AccessTokenController.class)
@ActiveProfiles("apiv2")
@WebAppConfiguration
@Import({ApiTestConfig.class})
public class AccessTokenControllerUnitTest extends ApiV2ControllerUnitTest {

    @TestConfiguration
    static class NoOpTransactionManagerConfig {
        @Bean
        public PlatformTransactionManager transactionManager() {
            return new PlatformTransactionManager() {
                @Override
                public TransactionStatus getTransaction(TransactionDefinition definition) {
                    return new SimpleTransactionStatus();
                }

                @Override
                public void commit(@NonNull TransactionStatus status) {}

                @Override
                public void rollback(@NonNull TransactionStatus status) {}
            };
        }
    }

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ApiV2NewsfeedEventController apiV2NewsfeedEventController;

    private static final String EXAMPLE_ACCESS_TOKENS_FOR_USER_PATH = JSON_PATH + "/example_accesstokens_for_user.json";
    private static final String EXAMPLE_ACCESS_TOKENS_PATH = JSON_PATH + "/example_accesstokens.json";
    private static final String ERROR_ACCESS_TOKEN_NOT_FOUND_PATH = JSON_PATH + "/error_accesstoken_notfound.json";
    private static final String EXAMPLE_NEW_ACCESSTOKEN_PATH = JSON_PATH + "/example_new_accesstoken.json";
    private static final String EXAMPLE_ACCESS_TOKEN_CREATED_PATH = JSON_PATH + "/example_accesstoken_created.json";
    private static final String ERROR_ACCESSTOKEN_MALFORMED_PATCH = JSON_PATH + "/error_accesstoken_malformed.json";
    private static final String EXAMPLE_ACCESS_TOKEN_PATCHED_PATH = JSON_PATH + "/example_accesstoken_patched.json";

    private User user;
    private User admin;
    private AccessToken token;

    @BeforeEach
    public void initEach() {
        user = UserTestFixture.GET_REGULAR_USER();
        admin = UserTestFixture.GET_ADMIN(13);
        token = AccessTokenTestFixture.GET_FIXTURE_ACCESS_TOKEN(user);
    }

    @Test
    @WithMockUser(authorities = "user")
    public void getMyAccessTokens() throws Exception {
        when(accessTokenService.findAllBySpecification(Mockito.any(), any(Pageable.class)))
                .thenReturn(AccessTokenTestFixture.GET_EXAMPLE_ACCESS_TOKENS_FOR_USER_PAGED(user));

        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/access-tokens")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_ACCESS_TOKENS_FOR_USER_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "admin"})
    public void getAccessTokens() throws Exception {
        when(accessTokenService.findAll(any(Pageable.class)))
                .thenReturn(AccessTokenTestFixture.GET_EXAMPLE_ACCESS_TOKENS_PAGED());

        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(admin));
        when(userService.isAdmin(admin)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/access-tokens")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_ACCESS_TOKENS_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void getAccessTokens_return403_whenUserIsNotAuthorized() throws Exception {
        AccessToken token = AccessTokenTestFixture.GET_FIXTURE_ACCESS_TOKEN(admin);

        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(accessTokenService.findById(any(Integer.class))).thenReturn(Optional.of(token));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/access-tokens/" + token.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
    }

    @Test
    public void getAccessTokens_returns401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/access-tokens")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void getAccessToken_returns404_whenAccessTokenIsNotFound() throws Exception {
        when(accessTokenService.findById(any(Integer.class))).thenReturn(Optional.empty());
        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v2/manager/access-tokens/" + 123)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(Files.readString(Path.of(ERROR_ACCESS_TOKEN_NOT_FOUND_PATH))));
    }

    @Test
    public void createAccessToken_returns401_whenUserIsNotAuthenticated() throws Exception {
        final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_ACCESSTOKEN_PATH));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/access-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exampleJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void createAccessToken() throws Exception {
        int userId = user.getId();
        final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_ACCESSTOKEN_PATH));
        AccessToken token = AccessTokenTestFixture.GET_FIXTURE_ACCESS_TOKEN(user);

        Strategy<AccessToken> strategy =
                Mockito.spy(new SuccessfulStrategy<>(token, newsfeedEventService, accessTokenService, user));

        when(strategyFactory.createAccessTokenStrategy(any(), eq(user))).thenReturn(strategy);
        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(accessTokenService.create(any())).thenReturn(token);
        when(userService.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/access-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exampleJson))
                .andExpect(status().isCreated())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_ACCESS_TOKEN_CREATED_PATH))));

        verify(strategy, times(1)).perform();
    }

    @Test
    @WithMockUser(authorities = "user")
    public void createAccessToken_return500_whenCreationFails() throws Exception {
        int userId = user.getId();
        final String exampleJson = Files.readString(Path.of(EXAMPLE_NEW_ACCESSTOKEN_PATH));

        Strategy<AccessToken> strategy =
                Mockito.spy(new FailureStrategy<>(token, newsfeedEventService, accessTokenService, user));

        when(strategyFactory.createAccessTokenStrategy(any(), eq(user))).thenReturn(strategy);
        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(accessTokenService.create(any())).thenReturn(token);
        when(userService.findById(userId)).thenReturn(Optional.of(user));

        doAnswer(invocation -> invocation.getArgument(0))
                .when(newsfeedEventService)
                .create(any());

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v2/manager/access-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exampleJson))
                .andExpect(status().isInternalServerError());
        TestUtils.matchInternalServerErrorCreate(result);

        verify(strategy, times(1)).perform();
    }

    @Test
    public void deleteAccessToken_return401_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/access-tokens/" + 123)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"admin", "user"})
    public void deleteAccessToken_returns404_whenAccessTokenIsNotFound() throws Exception {
        when(accessTokenService.findById(any(Integer.class))).thenReturn(Optional.empty());
        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(userService.findById(any(Integer.class))).thenReturn(Optional.of(user));

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/access-tokens/" + 123)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json(Files.readString(Path.of(ERROR_ACCESS_TOKEN_NOT_FOUND_PATH))));
    }

    @Test
    @WithMockUser
    public void deleteAccessToken_returns403_whenUserIsNotAuthorized() throws Exception {
        final int id = token.getId();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/access-tokens/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
    }

    @Test
    @WithMockUser(authorities = {"admin", "user"})
    public void deleteAccessToken() throws Exception {
        final int id = token.getId();

        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(accessTokenService.findById(any(Integer.class))).thenReturn(Optional.of(token));
        doNothing().when(accessTokenDeleter).delete(token);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v2/manager/access-tokens/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(accessTokenDeleter, times(1)).delete(token);
    }

    @Test
    public void patchAccessToken_returns401_whenUserIsNotAuthenticated() throws Exception {
        String patchJson = "[{\"op\": \"replace\",\"path\":\"/active\",\"value\":\"false\"}]";

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/access-tokens/" + 123)
                        .contentType("application/json-patch+json")
                        .content(patchJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHENTICATED_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void patchAccessToken_returns404_whenAccessTokenIsNotFound() throws Exception {
        String patchJson = "[{\"op\": \"replace\",\"path\":\"/active\",\"value\":\"false\"}]";
        final int id = token.getId();

        when(accessTokenService.findById(any(Integer.class))).thenReturn(Optional.empty());
        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/access-tokens/" + id)
                        .contentType("application/json-patch+json")
                        .content(patchJson))
                .andExpect(status().isNotFound())
                .andExpect(content().json(Files.readString(Path.of(ERROR_ACCESS_TOKEN_NOT_FOUND_PATH))));
    }

    @Test
    @WithMockUser
    public void patchAccessToken_returns403_whenUserIsNotAuthorized() throws Exception {
        String patchJson = "[{\"op\": \"replace\",\"path\":\"/active\",\"value\":\"false\"}]";
        final int id = token.getId();

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/access-tokens/" + id)
                        .contentType("application/json-patch+json")
                        .content(patchJson))
                .andExpect(status().isForbidden())
                .andExpect(content().json(Files.readString(Path.of(ERROR_NOT_AUTHORIZED_PATH))));
    }

    @Test
    @WithMockUser(authorities = "user")
    public void patchAccessToken_returns422_whenPatchIsIncorrect() throws Exception {
        String patchJson = "[{\"op\": \"replace\", \"path\":\"/activated\",\"value\":\"false\"}]";
        final int id = token.getId();
        Strategy<AccessToken> strategy =
                new SuccessfulStrategy<>(token, newsfeedEventService, accessTokenService, user);

        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(userService.findById(any(Integer.class))).thenReturn(Optional.of(user));
        when(accessTokenService.findById(any(Integer.class))).thenReturn(Optional.of(token));
        when(strategyFactory.updateAccessTokenStrategy(eq(token), any(User.class), any(AccessToken.class)))
                .thenReturn(strategy);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/access-tokens/" + id)
                        .contentType("application/json-patch+json")
                        .content(patchJson))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().json(Files.readString(Path.of(ERROR_ACCESSTOKEN_MALFORMED_PATCH))));
    }

    @Test
    @WithMockUser(authorities = {"user", "repositorymaintainer"})
    public void patchAccessToken_returns500_whenStrategyFailure() throws Exception {
        String patchJson = "[{\"op\": \"replace\", \"path\":\"/active\",\"value\":\"false\"}]";
        final int id = token.getId();

        Strategy<AccessToken> strategy =
                Mockito.spy(new FailureStrategy<>(token, newsfeedEventService, accessTokenService, user));

        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(userService.findById(any(Integer.class))).thenReturn(Optional.of(user));
        when(accessTokenService.findById(any(Integer.class))).thenReturn(Optional.of(token));
        when(strategyFactory.updateAccessTokenStrategy(eq(token), any(User.class), any(AccessToken.class)))
                .thenReturn(strategy);
        when(userService.findById(111)).thenReturn(Optional.of(user));

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/access-tokens/" + id)
                        .contentType("application/json-patch+json")
                        .content(patchJson))
                .andExpect(status().isInternalServerError());
        TestUtils.matchInternalServerErrorPatch(result);

        verify(strategy, times(1)).perform();
    }

    @Test
    @WithMockUser(authorities = "user")
    public void patchAccessToken() throws Exception {
        String patchJson = "[{\"op\": \"replace\",\"path\":\"/active\",\"value\":\"false\"}]";
        final int id = token.getId();
        final AccessToken updated = new AccessToken(token);

        updated.setId(token.getId());
        updated.setActive(false);
        user.setId(111);

        Strategy<AccessToken> strategy =
                Mockito.spy(new SuccessfulStrategy<>(updated, newsfeedEventService, accessTokenService, user));

        when(userService.findActiveByLogin("user")).thenReturn(Optional.of(user));
        when(accessTokenService.findById(any(Integer.class))).thenReturn(Optional.of(token));
        when(strategyFactory.updateAccessTokenStrategy(any(), any(), any())).thenReturn(strategy);
        doNothing().when(accessTokenPatchValidator).validatePatch(any(), any(), any());
        when(userService.findById(111)).thenReturn(Optional.of(user));

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/v2/manager/access-tokens/" + id)
                        .contentType("application/json-patch+json")
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(content().json(Files.readString(Path.of(EXAMPLE_ACCESS_TOKEN_PATCHED_PATH))));
        verify(strategy, times(1)).perform();
    }
}

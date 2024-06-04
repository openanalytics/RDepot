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
package eu.openanalytics.rdepot.integrationtest.manager.v2.base;

import eu.openanalytics.rdepot.integrationtest.manager.v2.IntegrationTest;
import eu.openanalytics.rdepot.integrationtest.manager.v2.RequestType;
import eu.openanalytics.rdepot.integrationtest.manager.v2.TestRequestBody;
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.AccessTokenTestData;
import org.junit.jupiter.api.Test;

public class AccessTokenIntegrationTest extends IntegrationTest {

    private final AccessTokenTestData testData;

    public AccessTokenIntegrationTest() {
        super("/api/v2/manager/access-tokens");
        this.testData = AccessTokenTestData.builder()
                .search("ed")
                .tokenId("2")
                .tokenIdNotActive("3")
                .days("14")
                .userLogin("newton")
                .active(true)
                .deleted(false)
                .expired(true)
                .getEndpointNewEventsAmount(0)
                .deleteEndpointNewEventsAmount(-2)
                .changeEndpointNewEventsAmount(1)
                .build();
    }

    @Test
    public void getAllTokens_asAdmin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(ADMIN_TOKEN)
                .statusCode(200)
                .urlSuffix("?sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/access-tokens/access_token_list_as_admin.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getAllTokens_asUser() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(USER_TOKEN)
                .statusCode(200)
                .urlSuffix("?sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/access-tokens/access_token_list_as_user.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getTokensByUserLogin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(ADMIN_TOKEN)
                .statusCode(200)
                .urlSuffix("?userLogin=" + testData.getUserLogin() + "&sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/access-tokens/access_tokens_by_user_login.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getTokensByNameSearching() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(USER_TOKEN)
                .statusCode(200)
                .urlSuffix("?search=" + testData.getSearch() + "&sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/access-tokens/access_tokens_searching_as_user.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getNonActiveTokensWithSearching() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(ADMIN_TOKEN)
                .statusCode(200)
                .urlSuffix("?search=" + testData.getSearch() + "&active=" + !testData.isActive() + "&sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/access-tokens/non_active_access_tokens_with_searching.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getActiveTokens() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(USER_TOKEN)
                .statusCode(200)
                .urlSuffix("?active=" + testData.isActive() + "&sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/access-tokens/active_access_tokens.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getExpiredTokens() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(USER_TOKEN)
                .statusCode(200)
                .urlSuffix("?expired=" + testData.isExpired() + "&sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/access-tokens/expired_access_tokens.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void createToken_returns422_whenInvalidLifetime() throws Exception {
        final String body = "{\n" + "    \"lifetime\": \"-14\",\n" + "    \"name\": \"test\"\n" + "}";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST)
                .urlSuffix("/")
                .token(USER_TOKEN)
                .statusCode(422)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/access-tokens/validation_error_number_of_days.json")
                .body(body)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void createToken() throws Exception {
        final String body = "{\n" + "    \"lifetime\": \"14\",\n" + "    \"name\": \"test\"\n" + "}";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST)
                .token(USER_TOKEN)
                .statusCode(200)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/access-tokens/access_token_created.json")
                .expectedEventsJson("/v2/base/events/access-tokens/created_access_token.json")
                .body(body)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/access-tokens/access_tokens_after_creation.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchToken() throws Exception {
        final String patch = "[\n"
                + "    {\n"
                + "        \"op\" : \"replace\",\n"
                + "        \"path\" : \"/active\",\n"
                + "        \"value\" : \"false\"\n"
                + "    },\n"
                + "    {\n"
                + "        \"op\" : \"replace\",\n"
                + "        \"path\" : \"/name\",\n"
                + "        \"value\" : \"change\"\n"
                + "    }\n"
                + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getTokenId())
                .statusCode(200)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedEventsJson("/v2/base/events/access-tokens/patched_access_token.json")
                .expectedJsonPath("/v2/base/access-tokens/patched_access_token.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/2")
                .statusCode(200)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/access-tokens/access_token_after_patch.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void reactivateToken_returns422() throws Exception {
        final String patch = "[\n"
                + "    {\n"
                + "        \"op\" : \"replace\",\n"
                + "        \"path\" : \"/active\",\n"
                + "        \"value\" : \"true\"\n"
                + "    }"
                + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getTokenIdNotActive())
                .statusCode(422)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/access-tokens/malformed_patch_access_token.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void deleteToken() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE)
                .urlSuffix("/" + testData.getTokenIdNotActive())
                .statusCode(204)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getDeleteEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getTokenIdNotActive())
                .statusCode(404)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/access-tokens/access_token_not_found.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void deleteToken_returns403_whenUserNotAuthorized() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE_UNAUTHORIZED)
                .urlSuffix("/" + testData.getTokenIdNotActive())
                .statusCode(403)
                .token(PACKAGEMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/access-tokens/access_token_list_as_admin.json")
                .build();
        testEndpoint(requestBody);
    }
}

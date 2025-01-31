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
package eu.openanalytics.rdepot.integrationtest.manager.v2.base;

import eu.openanalytics.rdepot.integrationtest.manager.v2.IntegrationTest;
import eu.openanalytics.rdepot.integrationtest.manager.v2.RequestType;
import eu.openanalytics.rdepot.integrationtest.manager.v2.TestRequestBody;
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.UserSettingsTestData;
import org.junit.jupiter.api.Test;

public class UserSettingsIntegrationTest extends IntegrationTest {

    private final UserSettingsTestData testData;

    public UserSettingsIntegrationTest() {
        super("/api/v2/manager/user-settings");
        this.testData = UserSettingsTestData.builder()
                .getEndpointNewEventsAmount(0)
                .changeEndpointNewEventsAmount(0)
                .userIdAdmin(4)
                .userIdDefaultSettings(6)
                .userIdWithSettings(4)
                .build();
    }

    @Test
    public void getDefaultSettings_asAdmin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getUserIdDefaultSettings())
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/user_settings/default_settings.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getDefaultSettings_asUser() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getUserIdDefaultSettings())
                .statusCode(200)
                .token(PACKAGEMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/user_settings/default_settings.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getDefaultSettings_Returns403_whenUserIsNotAuthorized() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_UNAUTHORIZED)
                .urlSuffix("/" + testData.getUserIdDefaultSettings())
                .token(REPOSITORYMAINTAINER_TOKEN)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getUserSettings() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getUserIdWithSettings())
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/user_settings/user_settings.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchUserWithSettings() throws Exception {
        final String patch = "["
                + "{"
                + "\"op\": \"replace\","
                + "\"path\":\"/theme\","
                + "\"value\":\"dark\""
                + "},"
                + "{"
                + "\"op\": \"replace\","
                + "\"path\":\"/language\","
                + "\"value\":\"de\""
                + "},"
                + "{"
                + "\"op\": \"replace\","
                + "\"path\":\"/pageSize\","
                + "\"value\":15"
                + "}"
                + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getUserIdWithSettings())
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/user_settings/patched_user_with_settings.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchUserWithDefaultSettings() throws Exception {
        final String patch = "["
                + "{"
                + "\"op\": \"replace\","
                + "\"path\":\"/theme\","
                + "\"value\":\"dark\""
                + "},"
                + "{"
                + "\"op\": \"replace\","
                + "\"path\":\"/language\","
                + "\"value\":\"pl-PL\""
                + "}"
                + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getUserIdDefaultSettings())
                .statusCode(200)
                .token(PACKAGEMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/user_settings/patched_user_with_default_settings.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchUserSettings_Returns403_whenUserIsNotAuthorized() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/pageSize\"," + "\"value\":-1" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH_UNAUTHORIZED)
                .urlSuffix("/" + testData.getUserIdDefaultSettings())
                .token(REPOSITORYMAINTAINER_TOKEN)
                .body(patch)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchUserSettings_Returns422_whenValidationFails() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/pageSize\"," + "\"value\":-1" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getUserIdDefaultSettings())
                .statusCode(422)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/user_settings/patch_422.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);
    }
}

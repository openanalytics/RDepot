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
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.StatusTestData;
import org.junit.jupiter.api.Test;

public class StatusIntegrationTest extends IntegrationTest {

    private final StatusTestData testData;

    public StatusIntegrationTest() {
        super("/api/v2/manager");
        this.testData = StatusTestData.builder()
                .serverAddressOk("http://oa-rdepot-repo:8080/testrepo1111")
                .serverAddressPython("http://oa-rdepot-repo:8080/python/testrepo1111")
                .serverAddressR("http://oa-rdepot-repo:8080/r/testrepo1111")
                .serverAddressWrong("http://oa-rdepot-wrong-repo:8080/testrepo1")
                .build();
    }

    @Test
    public void validateServerAddress_OK() throws Exception {
        final TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST)
                .token(ADMIN_TOKEN)
                .statusCode(200)
                .urlSuffix("/check-server-address")
                .body("{\"serverAddress\": \"" + testData.getServerAddressOk() + "\"}")
                .expectedJsonPath("/v2/base/status/repo_healthcheck_ok.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void validateServerAddress_Python_OK() throws Exception {
        final TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST)
                .token(ADMIN_TOKEN)
                .statusCode(200)
                .urlSuffix("/check-server-address")
                .body("{\"serverAddress\": \"" + testData.getServerAddressPython() + "\"}")
                .expectedJsonPath("/v2/base/status/repo_healthcheck_ok.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void validateServerAddress_R_OK() throws Exception {
        final TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST)
                .token(ADMIN_TOKEN)
                .statusCode(200)
                .urlSuffix("/check-server-address")
                .body("{\"serverAddress\": \"" + testData.getServerAddressR() + "\"}")
                .expectedJsonPath("/v2/base/status/repo_healthcheck_ok.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getHealthCheck_Fail() throws Exception {
        final TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST)
                .token(ADMIN_TOKEN)
                .statusCode(422)
                .urlSuffix("/check-server-address")
                .body("{\"serverAddress\": \"" + testData.getServerAddressWrong() + "\"}")
                .expectedJsonPath("/v2/base/status/repo_healthcheck_fail.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getHealthCheck_shouldReturn403_forNonAdmins() throws Exception {
        final TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .statusCode(403)
                .urlSuffix("/check-server-address")
                .body("{\"serverAddress\": \"" + testData.getServerAddressWrong() + "\"}")
                .expectedJsonPath("/v2/403.json")
                .build();
        testEndpoint(requestBody);
    }
}

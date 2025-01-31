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
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.RepositoryMaintainerTestData;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class RepositoryMaintainerIntegrationTest extends IntegrationTest {

    private final RepositoryMaintainerTestData testData;
    private static String EVENTS_PATH = "/v2/base/events/repositorymaintainers/";

    public RepositoryMaintainerIntegrationTest() {
        super("/api/v2/manager/repository-maintainers");
        this.testData = RepositoryMaintainerTestData.builder()
                .technologies(Arrays.asList("R", "python"))
                .deleted(true)
                .search("NIKO")
                .getEndpointNewEventsAmount(0)
                .deleteEndpointNewEventsAmount(-2)
                .changeEndpointNewEventsAmount(1)
                .build();
    }

    @Test
    public void getAllMaintainers() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(ADMIN_TOKEN)
                .statusCode(200)
                .urlSuffix("?sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_list.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getAllMaintainers_returns401_whenUserIsNotAuthenticated() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_UNAUTHENTICATED)
                .urlSuffix("?sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getAllMaintainers_returns403_whenUserIsNotAdmin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_UNAUTHORIZED)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .urlSuffix("?sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getAllDeletedMaintainers() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?deleted=" + testData.isDeleted())
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/deleted_list.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getAllNonDeleted_sortedByRepositoryId() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?deleted=" + !testData.isDeleted() + "&sort=repository.id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/nondeleted_sorted_list.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getRepositoryMaintainersByTechnology() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?resourceTechnology=" + testData.getTechnologies().get(1) + "&sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/python_related_repository_maintainers.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getRepositoryMaintainersByNameAndRepositorySearching() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?search=" + testData.getSearch() + "&sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainers_searching.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getDeletedRepositoryMaintainersWithSearching() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?deleted=" + testData.isDeleted() + "&search=" + testData.getSearch() + "&sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/deleted_maintainers_with_searching.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getNonDeletedMaintainer() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/3")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_nondeleted.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getDeletedMaintainer() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/2")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_deleted.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getMaintainer_returns401_whenUnauthenticated() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_UNAUTHENTICATED)
                .urlSuffix("/3")
                .statusCode(200)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getMaintainer_returns403_whenRequesterIsNotAdmin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_UNAUTHORIZED)
                .urlSuffix("/3")
                .statusCode(200)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getMaintainer_returns404_whenMaintainerIsNotFound() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_UNAUTHENTICATED)
                .urlSuffix("/2222")
                .statusCode(404)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_notfound.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void deleteMaintainer() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE)
                .urlSuffix("/2")
                .statusCode(204)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getDeleteEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/2")
                .statusCode(404)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_notfound.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void deleteMaintainer_returns403_whenMaintainerIsNotAdmin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE_UNAUTHORIZED)
                .urlSuffix("/2")
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_list.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void deleteMaintainer_returns404_whenMaintainerIsNotFound() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE)
                .urlSuffix("/2222")
                .statusCode(404)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_notfound.json")
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_list.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void deleteMaintainer_returns404_whenMaintainerIsNotSetAsDeleted() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE)
                .urlSuffix("/3")
                .statusCode(404)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_notfound.json")
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_list.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchMaintainer() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/repository/id\"," + "\"value\":\"3\"" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/3")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedEventsJson(EVENTS_PATH + "patched_repository_maintainer.json")
                .expectedJsonPath("/v2/base/repository-maintainer/patched_maintainer.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/3")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_after_patch.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
        final String patch = "[{\n"
                + "    \"op\": \"replace\",\n"
                + "    \"path\": \"/repositoryId\",\n"
                + "    \"value\": \"3\"\n"
                + "}]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH_UNAUTHENTICATED)
                .urlSuffix("/3")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .body(patch)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_list.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchMaintainer_returns403_whenUserIsRepositoryMaintainer() throws Exception {
        final String patch = "[{\n"
                + "    \"op\": \"replace\",\n"
                + "    \"path\": \"/repositoryId\",\n"
                + "    \"value\": \"3\"\n"
                + "}]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH_UNAUTHORIZED)
                .urlSuffix("/3")
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .body(patch)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_list.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchMaintainer_returns404_whenMaintainerIsNotFound() throws Exception {
        final String patch = "[{\n"
                + "    \"op\": \"replace\",\n"
                + "    \"path\": \"/repositoryId\",\n"
                + "    \"value\": \"3\"\n"
                + "}]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/3333")
                .statusCode(404)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_notfound.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_list.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchMaintainer_returns422_whenPatchIsMalformed() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/dsdsadsadsa\"," + "\"value\":false" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/3")
                .statusCode(422)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/malformed_patch.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_list.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchMaintainer_returns422_whenValidationFails() throws Exception {
        final String patch = "[{\n"
                + "    \"op\": \"replace\",\n"
                + "    \"path\": \"/repositoryId\",\n"
                + "    \"value\": \"3333\"\n"
                + "}]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/3")
                .statusCode(422)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_validation_error.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_list.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void createMaintainer() throws Exception {
        final String body = "{\n" + "    \"user\": {\"id\": 5},\n" + "    \"repository\": { \"id\": 3}\n" + "}";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST)
                .urlSuffix("/3")
                .token(ADMIN_TOKEN)
                .statusCode(201)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_created.json")
                .expectedEventsJson(EVENTS_PATH + "created_repository_maintainer.json")
                .body(body)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainers_after_creation.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void createMaintianer_returns401_whenUserIsNotAuthenticated() throws Exception {
        final String body = "{\n" + "    \"userId\": 5,\n" + "    \"repositoryId\": 3\n" + "}";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_UNAUTHENTICATED)
                .urlSuffix("/3")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .body(body)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_list.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void createMaintainer_returns403_whenUserIsRepositoryMaintainer() throws Exception {
        final String body = "{\n" + "    \"userId\": 5,\n" + "    \"repositoryId\": 3\n" + "}";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_UNAUTHORIZED)
                .urlSuffix("/3")
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .body(body)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_list.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void createMaintainer_returns422_whenValidationFails() throws Exception {
        final String body = "{\n" + "    \"user\": {\"id\": 5},\n" + "    \"repository\": {\"id\": 3333}\n" + "}";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST)
                .urlSuffix("/3")
                .statusCode(422)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/validation_error.json")
                .body(body)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repository-maintainer/maintainer_list.json")
                .build();
        testEndpoint(requestBody);
    }
}

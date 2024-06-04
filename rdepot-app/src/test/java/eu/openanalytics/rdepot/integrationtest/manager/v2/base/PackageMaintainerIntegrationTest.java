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

/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
package eu.openanalytics.rdepot.integrationtest.manager.v2.base;

import eu.openanalytics.rdepot.integrationtest.manager.v2.IntegrationTest;
import eu.openanalytics.rdepot.integrationtest.manager.v2.RequestType;
import eu.openanalytics.rdepot.integrationtest.manager.v2.TestRequestBody;
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.PackageMaintainerTestData;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class PackageMaintainerIntegrationTest extends IntegrationTest {

    private final PackageMaintainerTestData testData;
    public static final String API_PATH = "/api/v2/manager/package-maintainers";

    public PackageMaintainerIntegrationTest() {
        super(API_PATH);
        this.testData = PackageMaintainerTestData.builder()
                .getEndpointNewEventsAmount(0)
                .packageMaintainerOnlyForAdminId("3")
                .packageMaintainerForRepositoryMaintainerId("1")
                .technologies(Arrays.asList("R", "Python"))
                .repositories(Arrays.asList("testrepo8", "testrepo1"))
                .search("ADMIN")
                .deleted(true)
                .build();
    }

    @Test
    public void getAllMaintainers_asAdmin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .token(ADMIN_TOKEN)
                .statusCode(200)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/maintainers_asadmin.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getMaintainers_asRepositoryMaintainer() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .token(REPOSITORYMAINTAINER_TOKEN)
                .statusCode(200)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/maintainers_asrepositorymaintainer.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getMaintainers_returns401_whenUserIsNotAuthenticated() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_UNAUTHENTICATED)
                .urlSuffix("?sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getMaintainers_sortedByPackageName() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=packageName,asc")
                .token(ADMIN_TOKEN)
                .statusCode(200)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/maintainers_sorted.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getDeletedMaintainers() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc&deleted=" + testData.isDeleted())
                .token(ADMIN_TOKEN)
                .statusCode(200)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/maintainers_deleted.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getDeletedMaintainers_returns403_whenUserIsNotAuthorized() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_UNAUTHORIZED)
                .urlSuffix("?deleted=" + testData.isDeleted())
                .statusCode(403)
                .token(PACKAGEMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/403.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getNonDeletedMaintainers() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc&deleted=" + !testData.isDeleted())
                .token(ADMIN_TOKEN)
                .statusCode(200)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/maintainers_nondeleted.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getPackageMaintainersByTechnology() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?technology=" + testData.getTechnologies().get(0) + "&sort=repository.id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/package_maintainers_by_technology.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getPackageMaintainers_returns400_whenTryingToSortByNonExistingField() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?technology=" + testData.getTechnologies().get(0) + "&sort=repositoryId,asc")
                .statusCode(400)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/sort_unknown_param.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getPackageMaintainersByRepository() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?repository=" + testData.getRepositories().get(0) + "&sort=repository.id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/package_maintainers_by_repository.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getMaintainer() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getPackageMaintainerOnlyForAdminId())
                .token(ADMIN_TOKEN)
                .statusCode(200)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/maintainer_onlyadmin.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getPackageMaintainersByMaintainerAndPackageNameSearching() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?search=" + testData.getSearch() + "&sort=repository.id,asc")
                .statusCode(200)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/package_maintainers_searching.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getPackageMaintainersByTechnologyWithSearching() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?technology=" + testData.getTechnologies().get(1) + "&search=" + testData.getSearch()
                        + "&sort=repository.id,asc")
                .statusCode(200)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/package_maintainers_by_technology_with_searching.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getMaintainer_returns403_whenUserIsNotAllowedToSee() throws Exception {
        testGetEndpoint(
                "/v2/403.json",
                "/" + testData.getPackageMaintainerOnlyForAdminId(),
                403,
                REPOSITORYMAINTAINER_TOKEN,
                false);
    }

    @Test
    public void getMaintainer_whenUserIsAllowed() throws Exception {
        testGetEndpoint(
                "/v2/base/package-maintainer/maintainer.json",
                "/" + testData.getPackageMaintainerForRepositoryMaintainerId(),
                200,
                REPOSITORYMAINTAINER_TOKEN,
                false);
    }

    @Test
    public void getMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_UNAUTHENTICATED)
                .urlSuffix("/1")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getMaintainer_returns404_whenMaintainerIsNotFound() throws Exception {
        testGetEndpoint("/v2/base/package-maintainer/maintainer_notfound.json", "/22222", 404, ADMIN_TOKEN, false);
    }

    @Test
    public void shiftDeleteMaintainer() throws Exception {
        testDeleteEndpoint("/4", 204, ADMIN_TOKEN);
        testGetEndpoint("/v2/base/package-maintainer/maintainer_notfound.json", "/4", 404, ADMIN_TOKEN, false);
    }

    @Test
    public void shiftDeleteMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE_UNAUTHENTICATED)
                .urlSuffix("/4")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/maintainers_asadmin.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void shiftDeleteMaintainer_returns403_whenUserIsNotAdmin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE_UNAUTHORIZED)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .urlSuffix("/4")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/maintainers_asadmin.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void shiftDeleteMaintainer_returns404_whenMaintainerDoesNotExist() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE)
                .token(ADMIN_TOKEN)
                .urlSuffix("/4444")
                .statusCode(404)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/404.json")
                .build();
        testEndpoint(requestBody);
        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/maintainers_asadmin.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void shiftDeleteMaintainer_returns404_whenMaintainerIsNotSetDeleted() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE)
                .token(ADMIN_TOKEN)
                .urlSuffix("/1")
                .statusCode(404)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/404.json")
                .build();
        testEndpoint(requestBody);
        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/maintainers_asadmin.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void createMaintainer() throws Exception {
        final String body =
                "{" + "\"user\": { \"id\": 6}," + "\"packageName\": \"A3\"," + "\"repository\": {\"id\" : 2}" + "}";

        testPostEndpoint(body, "/v2/base/package-maintainer/maintainer_created.json", 201, ADMIN_TOKEN);
        testGetEndpoint(
                "/v2/base/package-maintainer/maintainers_after_creation.json", "?sort=id,asc", 200, ADMIN_TOKEN, false);
    }

    @Test
    public void createMaintainer_whenUserIsRepositoryMaintainerInTheSameRepository() throws Exception {
        final String body =
                "{" + "\"user\": { \"id\": 6}," + "\"packageName\": \"A3\"," + "\"repository\": {\"id\" : 2}" + "}";

        testPostEndpoint(
                body,
                "/v2/base/package-maintainer/maintainer_created_by_repository_maintainer.json",
                201,
                REPOSITORYMAINTAINER_TOKEN);
        testGetEndpoint(
                "/v2/base/package-maintainer/maintainers_after_creation_by_repository_maintainer.json",
                "?sort=id,asc",
                200,
                ADMIN_TOKEN,
                false);
    }

    @Test
    public void createMaintainer_returns403_whenRepositoryMaintainerIsNotInTheSameRepository() throws Exception {
        final String body =
                "{" + "\"user\": { \"id\": 6}," + "\"packageName\": \"A3\"," + "\"repository\": {\"id\" : 4}" + "}";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_UNAUTHORIZED)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .urlSuffix("")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .body(body)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(ADMIN_TOKEN)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .body(body)
                .expectedJsonPath("/v2/base/package-maintainer/maintainers_asadmin.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void createMaintainer_returns401_whenUserIsUnauthenticated() throws Exception {
        final String body =
                "{" + "\"user\": { \"id\": 6}," + "\"packageName\": \"A3\"," + "\"repository\": {\"id\" : 2}" + "}";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_UNAUTHENTICATED)
                .urlSuffix("")
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
                .expectedJsonPath("/v2/base/package-maintainer/maintainers_asadmin.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void createMaintainer_returns403_whenUserIsNotAuthorized() throws Exception {
        final String body =
                "{" + "\"user\": { \"id\": 6}," + "\"packageName\": \"A3\"," + "\"repository\": {\"id\" : 2}" + "}";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_UNAUTHORIZED)
                .token(PACKAGEMAINTAINER_TOKEN)
                .urlSuffix("")
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
                .expectedJsonPath("/v2/base/package-maintainer/maintainers_asadmin.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void createMaintainer_returns422_whenValidationFails() throws Exception {
        final String body =
                "{" + "\"user\": { \"id\": 6}," + "\"packageName\": \"A3\"," + "\"repository\": {\"id\" : 2222}" + "}";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST)
                .token(ADMIN_TOKEN)
                .urlSuffix("")
                .statusCode(422)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/maintainer_validation_error.json")
                .body(body)
                .build();
        testEndpoint(requestBody);
        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/maintainers_asadmin.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchMaintainer() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\": \"/packageName\"," + "\"value\": \"abc\"" + "}" + "]";

        testPatchEndpoint(patch, "/v2/base/package-maintainer/maintainer_patched.json", "/1", 200, ADMIN_TOKEN);
        testGetEndpoint("/v2/base/package-maintainer/maintainer_after_patch.json", "/1", 200, ADMIN_TOKEN, false);
    }

    @Test
    public void patchMaintainer_delete() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\": \"/deleted\"," + "\"value\": true" + "}" + "]";

        testPatchEndpoint(patch, "/v2/base/package-maintainer/delete_maintainer_patched.json", "/1", 200, ADMIN_TOKEN);
        testGetEndpoint(
                "/v2/base/package-maintainer/deleted_maintainer_after_patch.json", "/1", 200, ADMIN_TOKEN, false);
    }

    @Test
    public void patchMaintainer_returns401_whenUserIsUnauthenticated() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\": \"/packageName\"," + "\"value\": \"abc\"" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH_UNAUTHENTICATED)
                .urlSuffix("/1")
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
                .expectedJsonPath("/v2/base/package-maintainer/maintainers_asadmin.json")
                .build();

        testEndpoint(requestBody);
    }

    @Test
    public void patchMaintainer_returns403_whenUserIsNotAllowed() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\": \"/packageName\"," + "\"value\": \"abc\"" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH_UNAUTHORIZED)
                .token(USER_TOKEN)
                .urlSuffix("/1")
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
                .expectedJsonPath("/v2/base/package-maintainer/maintainers_asadmin.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchMaintainer_returns422_whenPatchIsMalformed() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/dsdsadsadsa\"," + "\"value\":false" + "}" + "]";
        testPatchEndpoint(patch, "/v2/malformed_patch.json", "/1", 422, ADMIN_TOKEN);
        testGetEndpoint(
                "/v2/base/package-maintainer/maintainers_asadmin.json", "?sort=id,asc", 200, ADMIN_TOKEN, false);
    }

    @Test
    public void patchMaintainer_returns404_whenMaintainerIsNotFound() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\": \"/packageName\"," + "\"value\": \"abc\"" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .urlSuffix("/11111")
                .statusCode(404)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/maintainer_notfound.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);
        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/maintainers_asadmin.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchMaintainer_returns422_whenValidationFails() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\": \"/repositoryId\"," + "\"value\": \"2222\"" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .urlSuffix("/1")
                .statusCode(422)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/malformed_patch.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);
        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/package-maintainer/maintainers_asadmin.json")
                .build();
        testEndpoint(requestBody);
    }
}

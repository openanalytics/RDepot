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

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.openanalytics.rdepot.integrationtest.manager.v2.IntegrationTest;
import eu.openanalytics.rdepot.integrationtest.manager.v2.RequestType;
import eu.openanalytics.rdepot.integrationtest.manager.v2.TestRequestBody;
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.RepositoryTestData;
import io.restassured.http.ContentType;
import java.io.FileReader;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class RepositoryIntegrationTest extends IntegrationTest {

    private final RepositoryTestData testData;

    public RepositoryIntegrationTest() {
        super("/api/v2/manager/repositories");
        this.testData = RepositoryTestData.builder()
                .technologies(Arrays.asList("R", "python"))
                .maintainers(Arrays.asList("Nikola%20Tesla"))
                .deleted(true)
                .published(true)
                .search("repo1")
                .name("testrepo1")
                .repoIdToRead("2")
                .repoIdDeleted("6")
                .repoIdNotExists("333")
                .getEndpointNewEventsAmount(0)
                .build();
    }

    @Test
    public void getConfig() throws Exception {
        final String response = given().accept(ContentType.JSON)
                .header(AUTHORIZATION, BASIC + USER_TOKEN)
                .when()
                .get("/api/v2/manager/config")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        final FileReader reader = new FileReader(JSON_PATH + "/v2/base/repositories/public_config.json");
        final JsonObject expectedResponse = (JsonObject) JsonParser.parseReader(reader);
        final JsonObject actualResponse = (JsonObject) JsonParser.parseString(response);

        assertEquals("Incorrect public configuration returned.", expectedResponse, actualResponse);
    }

    @Test
    public void getConfig_returns401_whenUnauthenticated() throws Exception {
        given().accept(ContentType.JSON)
                .when()
                .get("/api/v2/manager/config")
                .then()
                .statusCode(401);
    }

    @Test
    public void getAllRepositories() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(USER_TOKEN)
                .statusCode(200)
                .urlSuffix("?sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repositories/all_repositories.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getAllRepositories_returns401_whenUserIsNotAuthenticated() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_UNAUTHENTICATED)
                .urlSuffix("?sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getAllDeletedRepositoriesAsAdmin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(ADMIN_TOKEN)
                .statusCode(200)
                .urlSuffix("?deleted=" + testData.isDeleted() + "&sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repositories/all_deleted_repositories.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getAllDeletedRepositories_returns403_whenUserIsNotAdmin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .statusCode(403)
                .urlSuffix("?deleted=" + testData.isDeleted() + "&sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/403.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getRepositoriesByTechnologyAndMaintainerAndPublished() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(USER_TOKEN)
                .statusCode(200)
                .urlSuffix("?technology=" + testData.getTechnologies().get(1) + "&maintainer="
                        + testData.getMaintainers().get(0) + "&sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repositories/all_repositories_by_technology_and_maintainer.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getPublishedRepositories() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(USER_TOKEN)
                .statusCode(200)
                .urlSuffix("?published=" + testData.isPublished() + "&sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repositories/all_published_repositories.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getRepositoriesByNameSearching() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(USER_TOKEN)
                .statusCode(200)
                .urlSuffix("?search=" + testData.getSearch() + "&sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repositories/all_repositories_searching.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getRepositoryByName() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(USER_TOKEN)
                .statusCode(200)
                .urlSuffix("?name=" + testData.getName())
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repositories/repository_by_name.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getRepository_returns401_whenUserIsNotAuthenticated() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_UNAUTHENTICATED)
                .urlSuffix("/" + testData.getRepoIdToRead())
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getRepositoryAsUser() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getRepoIdToRead())
                .token(USER_TOKEN)
                .statusCode(200)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repositories/repository.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getDeletedRepositoryAsAdmin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getRepoIdDeleted())
                .token(ADMIN_TOKEN)
                .statusCode(200)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repositories/repository_deleted.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getDeletedRepository_returns404_whenUserIsNotAdmin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getRepoIdDeleted())
                .token(USER_TOKEN)
                .statusCode(404)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repositories/404.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getRepository_returns404_whenRepositoryNotFound() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getRepoIdNotExists())
                .token(USER_TOKEN)
                .statusCode(404)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repositories/404.json")
                .build();
        testEndpoint(requestBody);
    }
}

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
package eu.openanalytics.rdepot.integrationtest.manager.v2.r;

import static io.restassured.RestAssured.given;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.openanalytics.rdepot.integrationtest.manager.v2.IntegrationTest;
import eu.openanalytics.rdepot.integrationtest.manager.v2.RequestType;
import eu.openanalytics.rdepot.integrationtest.manager.v2.TestRequestBody;
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.RepositoryTechnologyTestData;
import io.restassured.http.ContentType;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RRepositoryIntegrationTest extends IntegrationTest {
    private final RepositoryTechnologyTestData testData;
    private static final String REPOSITORIES_PATH = "/v2/r/repositories/";
    private static final String EVENTS_PATH = "/v2/r/events/repositories/";

    public RRepositoryIntegrationTest() {
        super("/api/v2/manager/r/repositories");
        this.testData = RepositoryTechnologyTestData.builder()
                .technology("r")
                .repoNameToCreate("testrepo7")
                .repoNameToDuplicate("testrepo1")
                .repoNameToEdit("newName")
                .repoIdToPublish("5")
                .repoIdToUnpublish("2")
                .repoIdToDelete("2")
                .repoIdToShiftDelete("6")
                .repoIdToEdit("2")
                .repoIdToRead("2")
                .repoIdToPublishWithBinaryPackage("5")
                .deletedRepoId("6")
                .getEndpointNewEventsAmount(0)
                .deleteEndpointNewEventsAmount(-35)
                .changeEndpointNewEventsAmount(1)
                .build();
    }

    @Test
    public void getConfig() throws Exception {
        final String response = given().accept(ContentType.JSON)
                .header(AUTHORIZATION, BASIC + USER_TOKEN)
                .when()
                .get("/api/v2/manager/r/config")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        final FileReader reader = new FileReader(JSON_PATH + "/v2/r/repositories/r_config.json");
        final JsonObject expectedResponse = (JsonObject) JsonParser.parseReader(reader);
        final JsonObject actualResponse = (JsonObject) JsonParser.parseString(response);

        Assertions.assertEquals(expectedResponse, actualResponse, "Incorrect public configuration returned.");
    }

    @Test
    public void getConfig_returns401_whenUnauthenticated() {
        given().accept(ContentType.JSON)
                .when()
                .get("/api/v2/manager/r/config")
                .then()
                .statusCode(401);
    }

    @Test
    public void getAllRepositories() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(REPOSITORIES_PATH + "list_of_repositories.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getAllRepositories_returns401_whenUserIsNotAuthenticated() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_UNAUTHENTICATED)
                .urlSuffix("")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getDeletedRepositories() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?deleted=true")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(REPOSITORIES_PATH + "deleted_repositories.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getDeletedRepositories_returns403_whenUserIsNotAuthorized() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_UNAUTHORIZED)
                .urlSuffix("?deleted=true")
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getRepository() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getRepoIdToRead())
                .statusCode(200)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(REPOSITORIES_PATH + "repository.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getDeletedRepository_returns404_whenUserIsNotAdmin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getDeletedRepoId())
                .statusCode(404)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repositories/404.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getRepository_returns401_whenUserIsNotAuthenticated() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_UNAUTHENTICATED)
                .urlSuffix("/" + testData.getDeletedRepoId())
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getRepository_returns404_whenRepositoryIsNotFound() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/123")
                .statusCode(404)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repositories/404.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void createRepository() throws Exception {
        final String body = "{"
                + "\"name\": \"" + testData.getRepoNameToCreate() + "\","
                + "\"publicationUri\":\"http://localhost/repo/" + testData.getRepoNameToCreate() + "\","
                + "\"serverAddress\":\"http://oa-rdepot-repo:8080/" + testData.getRepoNameToCreate() + "\""
                + "}";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST)
                .urlSuffix("/")
                .statusCode(201)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedJsonPath(REPOSITORIES_PATH + "created_repository.json")
                .expectedEventsJson(EVENTS_PATH + "created_repository_event.json")
                .body(body)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(REPOSITORIES_PATH + "list_of_repositories_with_created_one.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void createRepository_returns401_whenUserIsNotAuthenticated() throws Exception {
        final String body = "{"
                + "\"name\": \"" + testData.getRepoNameToCreate() + "\","
                + "\"publicationUri\":\"http://localhost/repo/" + testData.getRepoNameToCreate() + "\","
                + "\"serverAddress\":\"http://oa-rdepot-repo:8080/" + testData.getRepoNameToCreate() + "\""
                + "}";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_UNAUTHENTICATED)
                .urlSuffix("")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .body(body)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void createRepository_returns403_whenUserIsNotAuthorized() throws Exception {
        final String body = "{"
                + "\"name\": \"" + testData.getRepoNameToCreate() + "\","
                + "\"publicationUri\":\"http://localhost/repo/" + testData.getRepoNameToCreate() + "\","
                + "\"serverAddress\":\"http://oa-rdepot-repo:8080/" + testData.getRepoNameToCreate() + "\""
                + "}";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_UNAUTHORIZED)
                .urlSuffix("")
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .body(body)
                .build();

        testEndpoint(requestBody);
    }

    @Test
    public void createRepository_returns422_whenRepositoryValidationFails() throws Exception {
        final String body = "{"
                + "\"name\": \"" + testData.getRepoNameToDuplicate() + "\","
                + "\"publicationUri\":\"http://localhost/repo/" + testData.getRepoNameToCreate() + "\","
                + "\"serverAddress\":\"http://oa-rdepot-repo:8080/" + testData.getRepoNameToCreate() + "\""
                + "}";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST)
                .urlSuffix("/")
                .statusCode(422)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repositories/422_create.json")
                .body(body)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void createRepository_returns422_whenInvalidName() throws Exception {
        final String body = "{"
                + "\"name\": \"#testrepo\","
                + "\"publicationUri\":\"http://localhost/repo/" + testData.getRepoNameToCreate() + "\","
                + "\"serverAddress\":\"http://oa-rdepot-repo:8080/" + testData.getRepoNameToCreate() + "\""
                + "}";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST)
                .urlSuffix("/")
                .statusCode(422)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repositories/422_invalid_name.json")
                .body(body)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void deleteRepository() throws Exception {

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE)
                .urlSuffix("/" + testData.getRepoIdToShiftDelete())
                .statusCode(204)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getDeleteEndpointNewEventsAmount())
                .build();

        testEndpoint(requestBody);
    }

    @Test
    public void deleteRepository_returns401_whenUserIsNotAuthenticated() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE_UNAUTHENTICATED)
                .urlSuffix("/" + testData.getRepoIdToShiftDelete())
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void deleteRepository_returns403_whenUserIsNotAuthorized() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE_UNAUTHORIZED)
                .urlSuffix("/" + testData.getRepoIdToShiftDelete())
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void deleteRepository_returns404_whenRepositoryIsNotFound() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE)
                .urlSuffix("/123")
                .statusCode(404)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repositories/404.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchRepository_delete() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/deleted\"," + "\"value\":true" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getRepoIdToDelete())
                .statusCode(200)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedJsonPath(REPOSITORIES_PATH + "deleted_repository.json")
                .expectedEventsJson(EVENTS_PATH + "patched_deleted_repository_event.json")
                .body(patch)
                .build();

        testEndpoint(requestBody);
    }

    @Test
    public void patchRepository_updateVersion_shouldFail() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/version\"," + "\"value\":100" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getRepoIdToDelete())
                .statusCode(422)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repositories/forbidden_update.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchRepository_publish() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/published\"," + "\"value\":true" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getRepoIdToPublish())
                .statusCode(200)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedJsonPath(REPOSITORIES_PATH + "published_repository.json")
                .expectedEventsJson(EVENTS_PATH + "patched_published_repository_event.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void publishRepositoryWithBinaryPackage() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/published\"," + "\"value\":true" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getRepoIdToPublishWithBinaryPackage())
                .statusCode(200)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedJsonPath(REPOSITORIES_PATH + "published_repository_with_binary.json")
                .expectedEventsJson(EVENTS_PATH + "patched_published_repository_with_binary_event.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchRepository_notRequireAuthentication() throws Exception {
        final String patch = "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/requiresAuthentication\","
                + "\"value\":false" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getRepoIdToPublish())
                .statusCode(200)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedJsonPath(REPOSITORIES_PATH + "patch_not_require_authentication_repository.json")
                .expectedEventsJson(EVENTS_PATH + "patched_not_require_authentication_repository_event.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void sendSynchronizationRequest_shouldFailForWrongMD5Sums() throws Exception {
        final Process process = new ProcessBuilder(
                        "/bin/bash", "src/test/resources/scripts/" + "tryToPublishCorruptPackage.sh")
                .redirectErrorStream(true)
                .start();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        final StringBuilder builder = new StringBuilder();
        String msg;
        while ((msg = reader.readLine()) != null) {
            builder.append(msg);
        }
        process.waitFor();
        process.destroy();
        final String output = builder.toString();
        final String responseCode = StringUtils.right(output.split("}")[1], 3);
        int code = Integer.parseInt(responseCode);
        Assertions.assertEquals(400, code, "the request did not fail properly");
    }

    @Test
    public void patchRepository_unpublish() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/published\"," + "\"value\":false" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getRepoIdToUnpublish())
                .statusCode(200)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedJsonPath(REPOSITORIES_PATH + "unpublished_repository.json")
                .expectedEventsJson(EVENTS_PATH + "patched_unpublished_repository_event.json")
                .body(patch)
                .build();

        testEndpoint(requestBody);
    }

    @Test
    public void patchRepository_update() throws Exception {
        final String patch = "["
                + "{"
                + "\"op\": \"replace\","
                + "\"path\":\"/name\","
                + "\"value\": \"" + testData.getRepoNameToEdit() + "\""
                + "},"
                + "{"
                + "\"op\": \"replace\","
                + "\"path\":\"/serverAddress\","
                + "\"value\": \"http://oa-rdepot-repo:8080/" + testData.getRepoNameToEdit() + "\""
                + "}"
                + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getRepoIdToEdit())
                .statusCode(200)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedJsonPath(REPOSITORIES_PATH + "edited_repository.json")
                .expectedEventsJson(EVENTS_PATH + "patched_updated_repository_event.json")
                .body(patch)
                .build();

        testEndpoint(requestBody);
    }

    @Test
    public void patchRepository_updateName_shouldFail() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\": \"/name\"," + "\"value\": \"#testrepo\"" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getRepoIdToDelete())
                .statusCode(422)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/repositories/422_invalid_name.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);
    }
}

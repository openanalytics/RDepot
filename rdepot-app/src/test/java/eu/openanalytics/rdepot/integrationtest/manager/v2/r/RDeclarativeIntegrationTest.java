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
package eu.openanalytics.rdepot.integrationtest.manager.v2.r;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.openanalytics.rdepot.integrationtest.IntegrationTestContainers;
import eu.openanalytics.rdepot.integrationtest.environment.TestEnvironmentConfigurator;
import eu.openanalytics.rdepot.integrationtest.manager.v2.declarative.DeclarativeIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class RDeclarativeIntegrationTest extends DeclarativeIntegrationTest {
    private static final DockerComposeContainer<?> DOCKER_COMPOSE_CONTAINER =
            new DockerComposeContainer<>(new File("src/test/resources/docker-compose-declarative.yaml"));
    private static final String REPO_NAME_TO_EDIT = "newName";
    private static final String REPO_NAME_TO_CREATE = "testrepo7";
    private static final String API_PATH = "/api/v2/manager/r";

    public static final String JSON_PATH = "src/test/resources/JSONs/v2/r-declarative";

    public static final TestEnvironmentConfigurator testEnv = TestEnvironmentConfigurator.getDefaultInstance();
    public static DockerComposeContainer<?> container = DOCKER_COMPOSE_CONTAINER
            .withOptions("--compatibility")
            .waitingFor(
                    "proxy",
                    Wait.forHttp("/actuator/health")
                            .forStatusCode(200)
                            .withHeaders(Collections.singletonMap("Accept", "application/json"))
                            .withStartupTimeout(Duration.ofMinutes(5)));

    public RDeclarativeIntegrationTest() {
        super(API_PATH);
    }

    @BeforeAll
    public static void configureRestAssured() {
        IntegrationTestContainers.stopContainers();
        System.out.println("===Starting containers for declarative mode tests...");
        container.start();
        System.out.println("===Declarative containers started.");
        RestAssured.port = 8021;
        //		RestAssured.port = 8017;
        RestAssured.urlEncodingEnabled = false;
    }

    @AfterAll
    public static void tearDownContainer() {
        System.out.println("===Stopping containers for declarative mode...");
        container.stop();
        System.out.println("===Declarative containers stopped.");
    }

    @BeforeEach
    public final void doBackup() throws Exception {
        testEnv.backupEnvironment();
    }

    @AfterEach
    public final void cleanUp() throws Exception {
        testEnv.restoreDeclarative();
    }

    @Test
    public void shouldRetrievePublicConfig() throws Exception {
        final String response = given().accept(ContentType.JSON)
                .header(AUTHORIZATION, BEARER + USER_TOKEN)
                .when()
                .get("/api/v2/manager/config")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        final FileReader reader = new FileReader(JSON_PATH + "/public_config.json");
        final JsonObject expectedResponse = (JsonObject) JsonParser.parseReader(reader);
        final JsonObject actualResponse = (JsonObject) JsonParser.parseString(response);

        Assertions.assertEquals(expectedResponse, actualResponse, "Incorrect public configuration returned.");
    }

    @Test
    public void shouldNotRetrievePublicConfig_IfUnauthenticated() {
        given().accept(ContentType.JSON)
                .when()
                .get("/api/v2/manager/config")
                .then()
                .statusCode(401);
    }

    @Test
    public void synchronizeRRepositoryWithEmptyMirror() throws IOException {
        final String repositoryId = "16";

        given().header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post(API_PATH + "/repositories/" + repositoryId + "/synchronize-mirrors")
                .then()
                .statusCode(204);

        await().atMost(30, TimeUnit.SECONDS)
                .with()
                .pollInterval(5, TimeUnit.SECONDS)
                .until(() -> assertSynchronizationFinished(repositoryId));

        FileReader reader = new FileReader(JSON_PATH + "/synchronization_status_empty.json");
        JsonObject expectedSynchronizationStatus = (JsonObject) JsonParser.parseReader(reader);

        assertSynchronizationStatus(expectedSynchronizationStatus, repositoryId, null, null);
    }

    @Test
    public void shouldSynchronizeRRepositoryWithMirror() throws IOException {
        final String sourceRepositoryId = "17";
        final String repositoryId = "3";

        given().header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post(API_PATH + "/repositories/" + sourceRepositoryId + "/synchronize-mirrors")
                .then()
                .statusCode(204);

        await().atMost(180, TimeUnit.SECONDS)
                .with()
                .pollInterval(5, TimeUnit.SECONDS)
                .until(() -> assertSynchronizationFinished(sourceRepositoryId));

        given().header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post(API_PATH + "/repositories/" + repositoryId + "/synchronize-mirrors")
                .then()
                .statusCode(204);

        await().atMost(180, TimeUnit.SECONDS)
                .with()
                .pollInterval(5, TimeUnit.SECONDS)
                .until(() -> assertSynchronizationFinished(repositoryId));

        FileReader reader = new FileReader(JSON_PATH + "/synchronization_status_paged.json");
        JsonObject expectedSynchronizationStatus = (JsonObject) JsonParser.parseReader(reader);
        reader = new FileReader(JSON_PATH + "/repositories_after_synchronization.json");
        JsonObject expectedRepositories = (JsonObject) JsonParser.parseReader(reader);
        reader = new FileReader(JSON_PATH + "/packages_after_synchronization.json");
        JsonObject expectedPackages = (JsonObject) JsonParser.parseReader(reader);

        assertSynchronizationStatus(expectedSynchronizationStatus, repositoryId, "2", "1");
        assertRepositories(expectedRepositories);
        assertPackages(expectedPackages);
    }

    @Test
    public void synchronizationStatus_withMixedSuccess() throws IOException {
        final String repositoryId = "18";

        given().header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post(API_PATH + "/repositories/" + repositoryId + "/synchronize-mirrors")
                .then()
                .statusCode(204);

        await().atMost(180, TimeUnit.SECONDS)
                .with()
                .pollInterval(5, TimeUnit.SECONDS)
                .until(() -> assertSynchronizationFinished(repositoryId));

        FileReader reader = new FileReader(JSON_PATH + "/synchronization_status_mixed_success.json");
        JsonObject expectedSynchronizationStatus = (JsonObject) JsonParser.parseReader(reader);
        reader = new FileReader(JSON_PATH + "/repositories_after_synchronization_mixed_success.json");
        JsonObject expectedRepositories = (JsonObject) JsonParser.parseReader(reader);
        reader = new FileReader(JSON_PATH + "/packages_after_synchronization_with_mixed_success.json");
        JsonObject expectedPackages = (JsonObject) JsonParser.parseReader(reader);

        assertSynchronizationStatus(expectedSynchronizationStatus, repositoryId, null, null);
        assertRepositories(expectedRepositories);
        assertPackages(expectedPackages);
    }

    @Test
    public void shouldUploadPackageToPublishedRRepository() throws IOException {
        File packageBag = new File("src/test/resources/itestPackages/A3_0.9.1.tar.gz");

        given().header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
                .accept("application/json")
                .contentType("multipart/form-data")
                .multiPart("repository", "A")
                .multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build())
                .when()
                .post(API_PATH + "/submissions")
                .then()
                .statusCode(201);

        FileReader reader = new FileReader(JSON_PATH + "/repositories_after_uploading_package.json");
        JsonObject expectedRepositories = (JsonObject) JsonParser.parseReader(reader);
        reader = new FileReader(JSON_PATH + "/packages_after_uploading_package.json");
        JsonObject expectedPackages = (JsonObject) JsonParser.parseReader(reader);

        assertRepositories(expectedRepositories);
        assertPackages(expectedPackages);
    }

    @Test
    public void shouldUploadPackageToUnpublishedRRepository() throws IOException {
        File packageBag = new File("src/test/resources/itestPackages/A3_0.9.1.tar.gz");

        given().header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
                .accept("application/json")
                .contentType("multipart/form-data")
                .multiPart("repository", "D")
                .multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build())
                .when()
                .post(API_PATH + "/submissions")
                .then()
                .statusCode(201);

        FileReader reader =
                new FileReader(JSON_PATH + "/repositories_after_uploading_package_to_unpublished_repository.json");
        JsonObject expectedRepositories = (JsonObject) JsonParser.parseReader(reader);
        reader = new FileReader(JSON_PATH + "/packages_after_uploading_package_to_unpublished_repository.json");
        JsonObject expectedPackages = (JsonObject) JsonParser.parseReader(reader);

        assertRepositories(expectedRepositories);
        assertPackages(expectedPackages);
    }

    @Test
    public void shouldNotDeleteRRepository() {
        given().headers(AUTHORIZATION, BEARER + ADMIN_TOKEN)
                .accept(ContentType.JSON)
                .when()
                .delete(API_PATH + "/repositories/3")
                .then()
                .statusCode(405);
    }

    @Test
    public void shouldNotCreateRRepository() throws JsonProcessingException {
        Map<String, String> params = new HashMap<>();
        params.put("name", REPO_NAME_TO_CREATE);
        params.put("publicationUri", "http://localhost/repo/" + REPO_NAME_TO_CREATE);
        params.put("serverAddress", "http://oa-rdepot-repo:8080/" + REPO_NAME_TO_CREATE);

        // Map, although theoretically supported, causes trouble in RestAssured.
        String bodyJson = new ObjectMapper().writeValueAsString(params);

        given().headers(AUTHORIZATION, BEARER + ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(bodyJson)
                .when()
                .post(API_PATH + "/repositories")
                .then()
                .statusCode(405);
    }

    @Test
    public void shouldNotEditRRepository() {
        final String patch = "["
                + "{"
                + "\"op\": \"replace\","
                + "\"path\":\"/name\","
                + "\"value\": \"" + REPO_NAME_TO_EDIT + "\""
                + "},"
                + "{"
                + "\"op\": \"replace\","
                + "\"path\":\"/serverAddress\","
                + "\"value\": \"http://oa-rdepot-repo:8080/" + REPO_NAME_TO_EDIT + "\""
                + "}"
                + "]";

        given().headers(AUTHORIZATION, BEARER + ADMIN_TOKEN)
                .accept(ContentType.JSON)
                .contentType("application/json-patch+json")
                .body(patch)
                .when()
                .patch(API_PATH + "/repositories/3")
                .then()
                .statusCode(405);
    }

    @Test
    public void shouldNotSynchronizeRepositoryWithMirrorWithPackageMaintainerCredentials() {
        final String repositoryId = "3";
        given().header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .patch(API_PATH + "/repositories/" + repositoryId + "/synchronize-mirrors")
                .then()
                .statusCode(405);
    }
}

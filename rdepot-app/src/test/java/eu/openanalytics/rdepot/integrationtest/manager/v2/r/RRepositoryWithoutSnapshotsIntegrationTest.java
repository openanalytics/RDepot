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

import eu.openanalytics.rdepot.integrationtest.IntegrationTestContainers;
import eu.openanalytics.rdepot.integrationtest.environment.TestEnvironmentConfigurator;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class RRepositoryWithoutSnapshotsIntegrationTest {

    private static final DockerComposeContainer<?> DOCKER_COMPOSE_CONTAINER =
            new DockerComposeContainer<>(new File("src/test/resources/docker-compose-without-snapshots.yaml"));

    public static final String REPOSITORYMAINTAINER_TOKEN =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXNsYSIsIm5hbWUiOiJOaWtvbGEgVGVzbGEiLCJlbWFpbCI6InRlc2xhQGxkYXAuZm9ydW1zeXMuY29tIiwiYXVkIjoiUkRlcG90Iiwicm9sZXMiOlsidXNlciIsInBhY2thZ2VtYWludGFpbmVyIiwicmVwb3NpdG9yeW1haW50YWluZXIiXSwiaXNzIjoiUkRlcG90IiwiZXhwIjoyMDA3MDI3NDU3LCJpYXQiOjE2OTE2Njc0NTd9.6o7URshlNb91K9DKig79XIk9ozhomwaBmLg6im1JgbeWfJJUOP9k-gLTmWWHZkBC32MGKKFR-U11QzYY6G7zsw";

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String JSON_PATH = "src/test/resources/JSONs";
    public static final String API_PATH = "/api/v2/manager/r";

    private final String REPO_ID_TO_PUBLISH = "5";

    public static final TestEnvironmentConfigurator testEnv = TestEnvironmentConfigurator.getDefaultInstance();

    public static DockerComposeContainer<?> container = DOCKER_COMPOSE_CONTAINER
            .withLocalCompose(true)
            .withOptions("--compatibility")
            .waitingFor(
                    "proxy",
                    //					Wait.forHealthcheck().withStartupTimeout(Duration.ofMinutes(5))
                    Wait.forHttp("/actuator/health")
                            .forStatusCode(200)
                            .withHeaders(Collections.singletonMap("Accept", "application/json"))
                            .withStartupTimeout(Duration.ofMinutes(5)));

    @BeforeAll
    public static void configureRestAssured() {
        IntegrationTestContainers.stopContainers();
        System.out.println("===Starting containers for without snapshots mode tests...");
        container.start();
        System.out.println("===Without snapshots containers started.");
        RestAssured.port = 8017;
        RestAssured.urlEncodingEnabled = false;
    }

    @AfterAll
    public static void tearDownContainer() {
        System.out.println("===Stopping containers for Without snapshots mode...");
        container.stop();
        System.out.println("===Without snapshots containers stopped.");
    }

    @BeforeEach
    public final void setUp() throws Exception {
        testEnv.restoreEnvironment();
    }

    @Test
    public void patchRepository_publish() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/published\"," + "\"value\":true" + "}" + "]";

        JSONParser jsonParser = new JSONParser();

        FileReader reader = new FileReader(JSON_PATH + "/v2/r/repositories/published_repository.json");
        JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);

        String data = given().headers(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
                .accept(ContentType.JSON)
                .contentType("application/json-patch+json")
                .body(patch)
                .when()
                .patch(API_PATH + "/repositories/" + REPO_ID_TO_PUBLISH)
                .then()
                .statusCode(200)
                .extract()
                .asString();

        JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
        if (actualJSON.get("data") != null) removeField(actualJSON);
        if (expectedJSON.get("data") != null) removeField(expectedJSON);
        Assertions.assertEquals(expectedJSON, actualJSON, "Incorrect JSON output");
    }

    @Test
    public void patchRepository_publish_removesGeneratedContent_whenSnapshotsAreTurnedOff() throws Exception {
        patchRepository_publish();
        int cmdExitValue = runCommand("src/test/resources/scripts/checkIfSnapshotDirExists.sh");
        Assertions.assertEquals(0, cmdExitValue, "Snapshots dir does not exist.");
        cmdExitValue = runCommand("src/test/resources/scripts/checkIfSnapshotDirIsEmpty.sh");
        Assertions.assertEquals(0, cmdExitValue, "Snapshots are not empty.");
    }

    private int runCommand(String... args) throws IOException, InterruptedException {
        int exitValue;
        String[] cmd = ArrayUtils.addAll(new String[] {"/bin/bash"}, args);
        Process process = Runtime.getRuntime().exec(cmd);
        exitValue = process.waitFor();
        process.destroy();
        return exitValue;
    }

    private void removeField(JSONObject json) {
        try {
            JSONObject jsonData = (JSONObject) json.get("data");
            if (jsonData != null) {
                if (jsonData.get("packageBag") != null) {
                    JSONObject jsonPackage = (JSONObject) jsonData.get("packageBag");
                    jsonPackage.remove("source");
                }
                if (jsonData.get("lastLoggedInOn") != null) {
                    jsonData.remove("lastLoggedInOn");
                }
                if (jsonData.get("createdOn") != null) {
                    jsonData.remove("createdOn");
                }
                if (jsonData.get("creationDate") != null) {
                    jsonData.remove("creationDate");
                }
                if (jsonData.get("expirationDate") != null) {
                    jsonData.remove("expirationDate");
                }
                if (jsonData.get("value") != null) {
                    jsonData.remove("value");
                }
                if (jsonData.get("lastPublicationTimestamp") != null) {
                    jsonData.remove("lastPublicationTimestamp");
                }
                if (jsonData.get("lastModifiedTimestamp") != null) {
                    jsonData.remove("lastModifiedTimestamp");
                }
            }

            JSONArray expectedContent =
                    (JSONArray) Objects.requireNonNull(jsonData).get("content");
            if (expectedContent != null) {
                for (Object o : expectedContent) {
                    JSONObject el = (JSONObject) o;
                    if (el.get("packageBag") != null) {
                        JSONObject jsonPackage = (JSONObject) el.get("packageBag");
                        jsonPackage.remove("source");
                    }
                    if (el.get("lastLoggedInOn") != null) {
                        el.remove("lastLoggedInOn");
                    }
                    if (el.get("createdOn") != null) {
                        el.remove("createdOn");
                    }
                    if (el.get("creationDate") != null) {
                        el.remove("creationDate");
                    }
                    if (el.get("expirationDate") != null) {
                        el.remove("expirationDate");
                    }
                    if (el.get("value") != null) {
                        el.remove("value");
                    }
                    if (el.get("lastPublicationTimestamp") != null) {
                        jsonData.remove("lastPublicationTimestamp");
                    }
                    if (el.get("lastModifiedTimestamp") != null) {
                        jsonData.remove("lastModifiedTimestamp");
                    }
                }
            }
        } catch (ClassCastException ignored) {
        }
    }
}

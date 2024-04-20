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
package eu.openanalytics.rdepot.integrationtest.manager.v2.python;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.openanalytics.rdepot.integrationtest.IntegrationTestContainers;
import eu.openanalytics.rdepot.integrationtest.environment.TestEnvironmentConfigurator;
import eu.openanalytics.rdepot.integrationtest.manager.v2.declarative.DeclarativeIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Collections;

import static io.restassured.RestAssured.given;

public class PythonDeclarativeIntegrationTest  extends DeclarativeIntegrationTest {
    private static final String API_PATH = "/api/v2/manager/python";
    public static final TestEnvironmentConfigurator testEnv = TestEnvironmentConfigurator.getDefaultInstance();
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String ADMIN_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlaW5zdGVpbiIsIm5hbWUiOiJBbGJlcnQgRWluc3RlaW4iLCJlbWFpbCI6ImVpbnN0ZWluQGxkYXAuZm9ydW1zeXMuY29tIiwiYXVkIjoiUkRlcG90Iiwicm9sZXMiOlsidXNlciIsInBhY2thZ2VtYWludGFpbmVyIiwicmVwb3NpdG9yeW1haW50YWluZXIiLCJhZG1pbiJdLCJpc3MiOiJSRGVwb3QiLCJleHAiOjIwMDcwMjcyNDgsImlhdCI6MTY5MTY2NzI0OH0.SycsCWDmEFZfWV7cMpc05KareRXQ3iKfM9iprBa-j6M27D0hg0uKS1eGEPIuAHXEdqyUSD6yv7WMeXNY9BuYdw";
    public static final String JSON_PATH = "src/test/resources/JSONs/v2/python-declarative";
    public static final String USER_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuZXd0b24iLCJuYW1lIjoiSXNhYWMgTmV3dG9uIiwiZW1haWwiOiJuZXd0b25AbGRhcC5mb3J1bXN5cy5jb20iLCJhdWQiOiJSRGVwb3QiLCJyb2xlcyI6WyJ1c2VyIl0sImlzcyI6IlJEZXBvdCIsImV4cCI6MjAwNzAyNzUwOSwiaWF0IjoxNjkxNjY3NTA5fQ.waNTEOoLL0jkDpvihngEg_O6_W91wvIcSdtcXIBYiTeE5SbyLL60FFztYwuUwo-aEghzqnQlfVj4NATZMWgA-g";

    public static DockerComposeContainer<?> container = new DockerComposeContainer<>(
            new File("src/test/resources/docker-compose-declarative.yaml"))
            .withLocalCompose(true)
            .withOptions("--compatibility")
            .waitingFor("proxy",
                    Wait.forHttp("/actuator/health")
                            .forStatusCode(200)
                            .withHeaders(Collections.singletonMap("Accept", "application/json"))
                            .withStartupTimeout(Duration.ofMinutes(5))
            );

    public PythonDeclarativeIntegrationTest() {
        super(AUTHORIZATION, BEARER, USER_TOKEN, API_PATH);
    }

    @BeforeAll
    public static void configureRestAssured() throws IOException, InterruptedException {
        IntegrationTestContainers.stopContainers();
        System.out.println("===Starting containers for declarative mode tests...");
        container.start();
        System.out.println("===Declarative containers started.");
        RestAssured.port = 8021;
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
    public void shouldNotDeletePythonRepository() {
        given()
                .headers(AUTHORIZATION, BEARER + ADMIN_TOKEN)
                .accept(ContentType.JSON)
                .when()
                .delete(API_PATH + "/repositories/13")
                .then()
                .statusCode(405);
    }

    @Test
    public void shouldUploadPackageToPublishedPythonRepository() throws IOException, ParseException, InterruptedException {
        File packageBag = new File ("src/test/resources/itestPackages/coconutpy-2.2.1.tar.gz");

        given()
                .header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
                .accept("application/json")
                .contentType("multipart/form-data")
                .multiPart("repository", "X")
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
        assertPackages(expectedPackages, false);
    }

    @Test
    public void shouldUploadPackageToUnublishedRRepository() throws IOException, ParseException, InterruptedException {
        File packageBag = new File ("src/test/resources/itestPackages/coconutpy-2.2.1.tar.gz");

        given()
                .header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
                .accept("application/json")
                .contentType("multipart/form-data")
                .multiPart("repository", "Y")
                .multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build())
                .when()
                .post(API_PATH + "/submissions")
                .then()
                .statusCode(201);

        FileReader reader = new FileReader(JSON_PATH + "/repositories_after_uploading_package_to_unpublished_repository.json");
        JsonObject expectedRepositories = (JsonObject) JsonParser.parseReader(reader);
        reader = new FileReader(JSON_PATH + "/packages_after_uploading_package_to_unpublished_repository.json");
        JsonObject expectedPackages = (JsonObject) JsonParser.parseReader(reader);

        assertRepositories(expectedRepositories);
        assertPackages(expectedPackages, false);
    }

    @Override
    protected void updateMd5SumsAndVersion(JsonArray expectedContent) throws IOException {

    }
}

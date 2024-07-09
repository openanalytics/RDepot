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
package eu.openanalytics.rdepot.integrationtest.manager.v2.r;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

public class RDeclarativeIntegrationTest extends DeclarativeIntegrationTest {
    private static final String REPO_NAME_TO_EDIT = "newName";
    private static final String REPO_NAME_TO_CREATE = "testrepo7";
    private static final String API_PATH = "/api/v2/manager/r";
    private static final String LINKS_PATH = "src/test/resources/declarative_packages_urls.csv";

    public static final String ADMIN_TOKEN =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlaW5zdGVpbiIsIm5hbWUiOiJBbGJlcnQgRWluc3RlaW4iLCJlbWFpbCI6ImVpbnN0ZWluQGxkYXAuZm9ydW1zeXMuY29tIiwiYXVkIjoiUkRlcG90Iiwicm9sZXMiOlsidXNlciIsInBhY2thZ2VtYWludGFpbmVyIiwicmVwb3NpdG9yeW1haW50YWluZXIiLCJhZG1pbiJdLCJpc3MiOiJSRGVwb3QiLCJleHAiOjIwMDcwMjcyNDgsImlhdCI6MTY5MTY2NzI0OH0.SycsCWDmEFZfWV7cMpc05KareRXQ3iKfM9iprBa-j6M27D0hg0uKS1eGEPIuAHXEdqyUSD6yv7WMeXNY9BuYdw";
    public static final String REPOSITORYMAINTAINER_TOKEN =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXNsYSIsIm5hbWUiOiJOaWtvbGEgVGVzbGEiLCJlbWFpbCI6InRlc2xhQGxkYXAuZm9ydW1zeXMuY29tIiwiYXVkIjoiUkRlcG90Iiwicm9sZXMiOlsidXNlciIsInBhY2thZ2VtYWludGFpbmVyIiwicmVwb3NpdG9yeW1haW50YWluZXIiXSwiaXNzIjoiUkRlcG90IiwiZXhwIjoyMDA3MDI3NDU3LCJpYXQiOjE2OTE2Njc0NTd9.6o7URshlNb91K9DKig79XIk9ozhomwaBmLg6im1JgbeWfJJUOP9k-gLTmWWHZkBC32MGKKFR-U11QzYY6G7zsw";
    public static final String PACKAGEMAINTAINER_TOKEN =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnYWxpZWxlbyIsImVtYWlsIjoiZ2FsaWVsZW9AbGRhcC5mb3J1bXN5cy5jb20iLCJuYW1lIjoiR2FsaWxlbyBHYWxpbGVpIiwiYXVkIjoiUkRlcG90Iiwicm9sZXMiOlsidXNlciIsInBhY2thZ2VtYWludGFpbmVyIl0sImlzcyI6IlJEZXBvdCIsImV4cCI6MjAwNzAyNzQ5MSwiaWF0IjoxNjkxNjY3NDkxfQ.24gRyDswxCmos1mUTkRJEKkrt3L2MFfyHEXa_H5EBhi3yirIN8AT7Bn_NYaTEtGcEfVd8NUQtgzm9uck76N2SQ";
    public static final String USER_TOKEN =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuZXd0b24iLCJuYW1lIjoiSXNhYWMgTmV3dG9uIiwiZW1haWwiOiJuZXd0b25AbGRhcC5mb3J1bXN5cy5jb20iLCJhdWQiOiJSRGVwb3QiLCJyb2xlcyI6WyJ1c2VyIl0sImlzcyI6IlJEZXBvdCIsImV4cCI6MjAwNzAyNzUwOSwiaWF0IjoxNjkxNjY3NTA5fQ.waNTEOoLL0jkDpvihngEg_O6_W91wvIcSdtcXIBYiTeE5SbyLL60FFztYwuUwo-aEghzqnQlfVj4NATZMWgA-g";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String JSON_PATH = "src/test/resources/JSONs/v2/r-declarative";

    public static final TestEnvironmentConfigurator testEnv = TestEnvironmentConfigurator.getDefaultInstance();

    public static DockerComposeContainer<?> container = new DockerComposeContainer<>(
                    new File("src/test/resources/docker-compose-declarative.yaml"))
            .withLocalCompose(true)
            .withOptions("--compatibility")
            .waitingFor(
                    "proxy",
                    Wait.forHttp("/actuator/health")
                            .forStatusCode(200)
                            .withHeaders(Collections.singletonMap("Accept", "application/json"))
                            .withStartupTimeout(Duration.ofMinutes(5)));

    public RDeclarativeIntegrationTest() {
        super(AUTHORIZATION, BEARER, USER_TOKEN, API_PATH);
    }

    @BeforeAll
    public static void configureRestAssured() throws IOException, InterruptedException {
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

        assertEquals("Incorrect public configuration returned.", expectedResponse, actualResponse);
    }

    @Test
    public void shouldNotRetrievePublicConfig_IfUnauthenticated() throws Exception {
        given().accept(ContentType.JSON)
                .when()
                .get("/api/v2/manager/config")
                .then()
                .statusCode(401);
    }

    @Test
    public void shouldSynchronizeRRepositoryWithMirror() throws ParseException, IOException {
        final String repositoryId = "3";

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

        FileReader reader = new FileReader(JSON_PATH + "/repositories_after_synchronization.json");
        JsonObject expectedRepositories = (JsonObject) JsonParser.parseReader(reader);
        reader = new FileReader(JSON_PATH + "/packages_after_synchronization.json");
        JsonObject expectedPackages = (JsonObject) JsonParser.parseReader(reader);

        assertRepositories(expectedRepositories);
        assertPackages(expectedPackages, true);
    }

    @Test
    public void shouldUploadPackageToPublishedRRepository() throws IOException, ParseException, InterruptedException {
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
        assertPackages(expectedPackages, false);
    }

    @Test
    public void shouldUploadPackageToUnpublishedRRepository() throws IOException, ParseException, InterruptedException {
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
        assertPackages(expectedPackages, false);
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

    private Boolean assertSynchronizationFinished(String repositoryId) {
        String response = given().header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
                .accept(ContentType.JSON)
                .when()
                .get(API_PATH + "/repositories/" + repositoryId + "/synchronization-status")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        JsonObject actualJson = (JsonObject) JsonParser.parseString(response);

        if (actualJson
                        .get("data")
                        .getAsJsonObject()
                        .get("repositoryId")
                        .getAsString()
                        .equals(repositoryId)
                && actualJson
                        .get("data")
                        .getAsJsonObject()
                        .get("pending")
                        .getAsString()
                        .equals("false")) return true;
        return false;
    }

    @Override
    protected void updateMd5SumsAndVersion(JsonArray expactedPackages) throws IOException {
        // 1. parse file with links and names to map
        // 2. download PACKAGES file
        // 3. parse PACKAGES file
        // 4. extract name and md5sum
        // 5. replace md5sum
        Map<String, String> links = new HashMap<>();
        Set<String> withVersion = new HashSet<>();
        File file = new File(LINKS_PATH);
        Scanner reader = new Scanner(file);
        while (reader.hasNextLine()) {
            String data = reader.nextLine();
            String[] tokens = data.split(",");
            if (tokens[2].equals("latest")) links.put(tokens[0], tokens[1]);
            else withVersion.add(tokens[0]);
        }
        reader.close();

        for (JsonElement el : expactedPackages) {
            JsonObject package_ = el.getAsJsonObject();
            String packageName = package_.get("name").getAsString();
            if (!withVersion.contains(packageName)) {
                String url = links.get(packageName);

                File tempFile = File.createTempFile("name", "PACKAGES");
                tempFile.deleteOnExit();
                FileUtils.copyURLToFile(new URL(url), tempFile);

                reader = new Scanner(tempFile);
                Boolean found = false;
                while (reader.hasNextLine()) {
                    String line = reader.nextLine();
                    if (!found && line.equals("Package: " + packageName)) {
                        found = true;
                    } else if (found) {
                        if (line.startsWith("MD5sum: ")) {
                            String md5 = line.split(": ")[1];
                            package_.remove("md5sum");
                            package_.addProperty("md5sum", md5);
                        } else if (line.startsWith("Version: ")) {
                            String version = line.split(": ")[1];
                            String oldSource = package_.get("source").getAsString();
                            String newSource = oldSource.split("_")[0] + "_" + version + ".tar.gz";
                            // TODO: #32887 Properly parse version as well as we do it in PackageIntegrationTest
                            package_.remove("source");
                            package_.addProperty("source", newSource);
                        }

                        if (line.startsWith("Package: ")) break;
                    }
                }
                reader.close();
            }
        }
    }
}

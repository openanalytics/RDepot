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
package eu.openanalytics.rdepot.integrationtest;

import static io.restassured.RestAssured.given;

import eu.openanalytics.rdepot.integrationtest.manager.v2.IntegrationTest;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArchiveRdsIntegrationTest extends IntegrationTest {

    private static final String API_PACKAGES_PATH = "/api/v2/manager/r/packages";
    private static final String API_SUBMISSION_PATH = "/api/v2/manager/r/submissions";

    public ArchiveRdsIntegrationTest() {
        super("/api/manager/packages");
    }

    @Test
    public void shouldNotHaveArchiveRdsFile() throws IOException {
        //		testrepo1 does not contain archived packages initially
        given().accept(ContentType.BINARY)
                .when()
                .get(PUBLICATION_URI_PATH + "/testrepo1/src/contrib/Meta/archive.rds")
                .then()
                .statusCode(404);
    }

    @Test
    public void shouldCreateArchiveRdsFile() throws IOException {
        File packageBag = new File("src/test/resources/itestPackages/A3_0.9.1.tar.gz");

        given().header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                .accept("application/json")
                .contentType("multipart/form-data")
                .multiPart("repository", "testrepo1")
                .multiPart("replace", true)
                .multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build())
                .when()
                .post(API_SUBMISSION_PATH)
                .then()
                .statusCode(201);

        //		System.out.println("EXTRACTED: " + response);

        byte[] archiveRdsFile = given().accept(ContentType.BINARY)
                .when()
                .get(PUBLICATION_URI_PATH + "/testrepo1/src/contrib/Meta/archive.rds")
                .then()
                .statusCode(200)
                .extract()
                .asByteArray();

        int originalArchiveRdsFileLength = archiveRdsFile.length;
        Assertions.assertTrue(originalArchiveRdsFileLength > 0);

        packageBag = new File("src/test/resources/itestPackages/abc_1.0.tar.gz");

        given().header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                .accept("application/json")
                .contentType("multipart/form-data")
                .multiPart("repository", "testrepo1")
                .multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build())
                .when()
                .post(API_SUBMISSION_PATH)
                .then()
                .statusCode(201);

        archiveRdsFile = given().accept(ContentType.BINARY)
                .when()
                .get(PUBLICATION_URI_PATH + "/testrepo1/src/contrib/Meta/archive.rds")
                .then()
                .statusCode(200)
                .extract()
                .asByteArray();

        int secondArchiveRdsFileLength = archiveRdsFile.length;
        Assertions.assertTrue(originalArchiveRdsFileLength < secondArchiveRdsFileLength);
    }

    @Test
    public void shouldRemoveArchiveRdsFile() throws IOException {
        // testrepo2 contains accrued 1.2 and 1.3 in archive, with ids 8 and 5
        // we need to first upload any package (not necessarily archived package),
        // to generate the initial archives.rds file as it isn't part of the integration test resources
        File packageBag = new File("src/test/resources/itestPackages/abc_1.0.tar.gz");

        given().header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                .accept("application/json")
                .contentType("multipart/form-data")
                .multiPart("repository", "testrepo2")
                .multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build())
                .when()
                .post(API_SUBMISSION_PATH)
                .then()
                .statusCode(201);

        byte[] archiveRdsFile = given().accept(ContentType.BINARY)
                .when()
                .get(PUBLICATION_URI_PATH + "/testrepo2/src/contrib/Meta/archive.rds")
                .then()
                .statusCode(200)
                .extract()
                .asByteArray();

        int originalArchiveRdsFileLength = archiveRdsFile.length;
        Assertions.assertTrue(originalArchiveRdsFileLength > 0);

        String deleteBody = "[{" + "\"op\" : \"replace\"," + "\"path\" : \"/deleted\"," + "\"value\" : true" + "}]";

        given().header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                .accept(ContentType.JSON)
                .contentType("application/json-patch+json")
                .body(deleteBody)
                .when()
                .patch(API_PACKAGES_PATH + "/8")
                .then()
                .statusCode(200);

        archiveRdsFile = given().accept(ContentType.BINARY)
                .when()
                .get(PUBLICATION_URI_PATH + "/testrepo2/src/contrib/Meta/archive.rds")
                .then()
                .statusCode(200)
                .extract()
                .asByteArray();

        int secondArchiveRdsFileLength = archiveRdsFile.length;
        Assertions.assertTrue(secondArchiveRdsFileLength > 0);
        Assertions.assertTrue(originalArchiveRdsFileLength > secondArchiveRdsFileLength);

        given().header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                .accept(ContentType.JSON)
                .contentType("application/json-patch+json")
                .body(deleteBody)
                .when()
                .patch(API_PACKAGES_PATH + "/5")
                .then()
                .statusCode(200);

        given().accept(ContentType.BINARY)
                .when()
                .get(PUBLICATION_URI_PATH + "/testrepo2/src/contrib/Meta/archive.rds")
                .then()
                .statusCode(404);
    }
}

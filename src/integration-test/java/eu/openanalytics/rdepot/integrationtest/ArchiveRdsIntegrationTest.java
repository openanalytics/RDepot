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
package eu.openanalytics.rdepot.integrationtest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.Test;

import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class ArchiveRdsIntegrationTest extends IntegrationTest {

  public ArchiveRdsIntegrationTest() {
		super("/api/manager/packages");
	}

private static final String API_PACKAGES_PATH = "/api/manager/packages";

  @Test
  public void shouldNotHaveArchiveRdsFile() throws IOException {
    // testrepo1 does not contain archived packages initially
    Response response = given().accept(ContentType.BINARY).when()
        .get(PUBLICATION_URI_PATH + "/testrepo1/src/contrib/Meta/archive.rds");
    assertEquals(404, response.getStatusCode());
  }

  @Test
  public void shouldCreateArchiveRdsFile() throws IOException {
    File packageBag = new File("src/integration-test/resources/itestPackages/A3_0.9.1.tar.gz");

    given().header(AUTHORIZATION, BEARER + ADMIN_TOKEN).accept("application/json")
        .contentType("multipart/form-data").multiPart("repository", "testrepo1").multiPart("replace", true)
        .multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
            .fileName(packageBag.getName()).mimeType("application/gzip").controlName("file")
            .build())
        .when().post(API_PACKAGES_PATH + "/submit").then().statusCode(200).extract();

    Response response = given().accept(ContentType.BINARY).when()
        .get(PUBLICATION_URI_PATH + "/testrepo1/src/contrib/Meta/archive.rds");
    assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);

    byte[] archiveRdsFile = response.asByteArray();
    int originalArchiveRdsFileLength = archiveRdsFile.length;
    assertTrue(originalArchiveRdsFileLength > 0);
    
    packageBag = new File("src/integration-test/resources/itestPackages/abc_1.0.tar.gz");

    given().header(AUTHORIZATION, BEARER + ADMIN_TOKEN).accept("application/json")
        .contentType("multipart/form-data").multiPart("repository", "testrepo1")
        .multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
            .fileName(packageBag.getName()).mimeType("application/gzip").controlName("file")
            .build())
        .when().post(API_PACKAGES_PATH + "/submit").then().statusCode(200).extract();

    response = given().accept(ContentType.BINARY).when()
        .get(PUBLICATION_URI_PATH + "/testrepo1/src/contrib/Meta/archive.rds");
    assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);

    archiveRdsFile = response.asByteArray();
    int secondArchiveRdsFileLength = archiveRdsFile.length;
    assertTrue(originalArchiveRdsFileLength < secondArchiveRdsFileLength);
  }

  @Test
  public void shouldRemoveArchiveRdsFile() throws IOException {
    // testrepo2 contains accrued 1.2 and 1.3 in archive, with ids 8 and 5
    // we need to first upload any package (not necessarily archived package), 
    // to generate the initial archives.rds file as it isn't part of the integration test resources
    File packageBag = new File("src/integration-test/resources/itestPackages/abc_1.0.tar.gz");

    given().header(AUTHORIZATION, BEARER + ADMIN_TOKEN).accept("application/json")
        .contentType("multipart/form-data").multiPart("repository", "testrepo2")
        .multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
            .fileName(packageBag.getName()).mimeType("application/gzip").controlName("file")
            .build())
        .when().post(API_PACKAGES_PATH + "/submit").then().statusCode(200).extract();
    
    Response response = given().accept(ContentType.BINARY).when()
        .get(PUBLICATION_URI_PATH + "/testrepo2/src/contrib/Meta/archive.rds");
    assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);

    byte[] archiveRdsFile = response.asByteArray();
    int originalArchiveRdsFileLength = archiveRdsFile.length;
    assertTrue(originalArchiveRdsFileLength > 0);

    given().header(AUTHORIZATION, BEARER + ADMIN_TOKEN).accept(ContentType.JSON).when()
        .delete(API_PACKAGES_PATH + "/8/delete").then().statusCode(200)
        .body("success", equalTo("Package deleted successfully."));

    response = given().accept(ContentType.BINARY).when()
        .get(PUBLICATION_URI_PATH + "/testrepo2/src/contrib/Meta/archive.rds");
    assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);

    archiveRdsFile = response.asByteArray();
    int secondArchiveRdsFileLength = archiveRdsFile.length;
    assertTrue(secondArchiveRdsFileLength > 0);
    assertTrue(originalArchiveRdsFileLength > secondArchiveRdsFileLength);

    given().header(AUTHORIZATION, BEARER + ADMIN_TOKEN).accept(ContentType.JSON).when()
        .delete(API_PACKAGES_PATH + "/5/delete").then().statusCode(200)
        .body("success", equalTo("Package deleted successfully."));

    response = given().accept(ContentType.BINARY).when()
        .get(PUBLICATION_URI_PATH + "/testrepo2/src/contrib/Meta/archive.rds");
    assertEquals(404, response.getStatusCode());
  }

}

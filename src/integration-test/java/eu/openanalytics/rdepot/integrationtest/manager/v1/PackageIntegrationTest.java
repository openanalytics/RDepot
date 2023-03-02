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
package eu.openanalytics.rdepot.integrationtest.manager.v1;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.springframework.http.MediaType;

import eu.openanalytics.rdepot.integrationtest.IntegrationTest;
import io.restassured.http.ContentType;

public class PackageIntegrationTest extends IntegrationTest {
	
	public PackageIntegrationTest() {
		super("/api/manager/packages");
	}

	private final String PACKAGES_PATH = "src/integration-test/resources/itestPackages";
	private final String PDF_PATH = "src/integration-test/resources/itestPdf";
	
	private final String PACKAGE_NAME_TO_DOWNLOAD = "accrued";
	private final String PACKAGE_VERSION_TO_DOWNLOAD = "1.3";
	private final String PACKAGE_ID_TO_DOWNLOAD = "5";
	private final String PACKAGE_ID_WITHOUT_MANUAL = "18";
	
	private final String VIGNETTE = "/25/vignettes/usl.pdf";
	
	private final String PACKAGE_TO_DEACTIVATE_ID = "25";
	private final String PACKAGE_TO_DEACTIVATE_URI = "/testrepo1/src/contrib/usl_2.0.0.tar.gz";
	
	private final String PACKAGE_TO_DEACTIVATE_ID_ARCHIVE = "8";
	private final String PACKAGE_TO_DEACTIVATE_URI_ARCHIVE = "/testrepo2/src/contrib/Archive/accrued/accrued_1.2.tar.gz";
	
	private final String PACKAGE_OLDER_ID = "9";
	private final String PACKAGE_INACTIVE_ID = "13";
	
	private final String PUBLICATION_REPO_URI = "/repo";
	
	@Test
	public void shouldReturnPackages() throws IOException, ParseException {				
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/package/packages.json");
		JSONArray rootJSON = (JSONArray) jsonParser.parse(reader);
		List<JSONObject> expectedJSON = convert(rootJSON);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
			
		rootJSON = (JSONArray) jsonParser.parse(data);

		List<JSONObject> actualJSON = convert(rootJSON);
		
		assertEquals("Differences in packages", expectedJSON, actualJSON);
	}
	
	@Test
	public void shouldReturnPackagesFromCertainRepository() throws IOException, ParseException {				
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/package/packages_from_one_repository.json");
		JSONArray rootJSON = (JSONArray) jsonParser.parse(reader);
		List<JSONObject> expectedJSON = convert(rootJSON);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list?repositoryName=testrepo1")
		.then()
			.statusCode(200)
			.extract()
			.asString();
			
		rootJSON = (JSONArray) jsonParser.parse(data);

		List<JSONObject> actualJSON = convert(rootJSON);
		
		assertEquals("Differences in packages", expectedJSON, actualJSON);
	}
	
	@Test
	public void shouldReturnDeletedPackages() throws IOException, ParseException {		
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/package/deleted_packages.json");
		JSONArray rootJSON = (JSONArray) jsonParser.parse(reader);
		List<JSONObject> expectedJSON = convert(rootJSON);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/deleted")
		.then()
			.statusCode(200)
			.extract()
			.asString();
			
		rootJSON = (JSONArray) jsonParser.parse(data);

		List<JSONObject> actualJSON = convert(rootJSON);
		
		assertEquals("Differences in deleted packages", expectedJSON, actualJSON);
	}
	
	@Test
	public void shouldNotReturnDeletedPackages() {
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/deleted")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldDownloadPackage() throws IOException {		
		byte[] pkg = given()
					.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
					.accept(ContentType.ANY)
				.when()
	            	.get(API_PATH + "/" + PACKAGE_ID_TO_DOWNLOAD + "/download/" + PACKAGE_NAME_TO_DOWNLOAD + "_" + PACKAGE_VERSION_TO_DOWNLOAD + ".tar.gz")
	            .then()
	            	.statusCode(200)
	            	.extract()
	            	.asByteArray();

		File file = new File(PACKAGES_PATH + "/" + PACKAGE_NAME_TO_DOWNLOAD + "_" + PACKAGE_VERSION_TO_DOWNLOAD + ".tar.gz");
		
		byte[] expectedpkg = readFileToByteArray(file);
		
		assertArrayEquals("Wrong package has been downloaded", expectedpkg, pkg);
	}

	@Test
	public void shouldDownloadPdf() throws IOException {
		
		byte[] pdf = given()
				.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
				.accept(ContentType.ANY)
				
			.when()
            	.get(API_PATH + "/" + PACKAGE_ID_TO_DOWNLOAD +"/download/" + PACKAGE_NAME_TO_DOWNLOAD + ".pdf")
			.then()
				.statusCode(200)
				.extract()
				.asByteArray();
		
		String content = extractContent(pdf);
		
		File file = new File(PDF_PATH + "/" + PACKAGE_NAME_TO_DOWNLOAD + ".pdf");
		byte[] expectedpdf = readFileToByteArray(file);
	
		assertTrue(content.contains(PACKAGE_NAME_TO_DOWNLOAD));
		assertTrue(content.contains("Version " + PACKAGE_VERSION_TO_DOWNLOAD));
		assertArrayEquals("Meta-data can cause some differences", expectedpdf, pdf);
	}
	
	@Test
	public void shouldActivatePackage() throws IOException, ParseException {
		given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/" + PACKAGE_INACTIVE_ID + "/activate")
		.then()
			.statusCode(200)
			.body("success", equalTo("Package activated successfully."));
	
		JSONParser jsonParser = new JSONParser();
	
		FileReader reader = new FileReader(JSON_PATH + "/package/activated_package.json");
		JSONArray rootJSON = (JSONArray) jsonParser.parse(reader);
		List<JSONObject> expectedJSON = convert(rootJSON);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
			
		rootJSON = (JSONArray) jsonParser.parse(data);
	
		List<JSONObject> actualJSON = convert(rootJSON);
		
		assertEquals("Package hasn't been activated or package maintainer can't do it", 
				expectedJSON, actualJSON);
	}
	
	@Test
	public void nonPackageMaintainerShouldNotBeAbleToActivatePackage() {
		given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/" + PACKAGE_INACTIVE_ID + "/activate")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldDeactivatePackage() throws IOException, ParseException {
		given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/" + PACKAGE_OLDER_ID + "/deactivate")
		.then()
			.statusCode(200)
			.body("success", equalTo("Package deactivated successfully."));
	
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/package/deactivated_package.json");
		JSONArray rootJSON = (JSONArray) jsonParser.parse(reader);
		List<JSONObject> expectedJSON = convert(rootJSON);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
			
		rootJSON = (JSONArray) jsonParser.parse(data);
	
		List<JSONObject> actualJSON = convert(rootJSON);
		
		assertEquals("Package hasn't been activated or package maintainer hasn't got privileges to list of packages",
				expectedJSON, actualJSON);
	}
	
	@Test
	public void shouldRemoveInactivePackageFromPublicRepositoryArchive() {
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/" + PACKAGE_TO_DEACTIVATE_ID_ARCHIVE + "/deactivate")
		.then()
			.statusCode(200)
			.body("success", equalTo("Package deactivated successfully."));
		
		given()
			.accept(ContentType.BINARY)
		.when()
			.get(PUBLICATION_REPO_URI + PACKAGE_TO_DEACTIVATE_URI_ARCHIVE)
		.then()
			.statusCode(404)
			.extract();
	}
	
	@Test
	public void shouldRemoveInactivePackageFromPublicRepository() {
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/" + PACKAGE_TO_DEACTIVATE_ID + "/deactivate")
		.then()
			.statusCode(200)
			.body("success", equalTo("Package deactivated successfully."));

		given()
			.accept(ContentType.BINARY)
		.when()
			.get(PUBLICATION_REPO_URI + PACKAGE_TO_DEACTIVATE_URI)
		.then()
			.statusCode(404)
			.extract();
	}
	
	@Test
	public void nonPackageMaintainerShouldNotBeAbleToDeactivatePackage() {
		given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/" + PACKAGE_OLDER_ID + "/deactivate")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldDeletePackage() throws IOException, ParseException {
		given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + PACKAGE_OLDER_ID + "/delete")
		.then()
			.statusCode(200)
			.body("success", equalTo("Package deleted successfully."));
	
		JSONParser jsonParser = new JSONParser();
	
		FileReader reader = new FileReader(JSON_PATH + "/package/deleted_packages_with_new_one.json");
		JSONArray rootJSON = (JSONArray) jsonParser.parse(reader);
		List<JSONObject> expectedJSONDeleted = convert(rootJSON);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/deleted")
		.then()
			.statusCode(200)
			.extract()
			.asString();
			
		rootJSON = (JSONArray) jsonParser.parse(data);
	
		List<JSONObject> actualJSONDeleted = convert(rootJSON);
		
		reader = new FileReader(JSON_PATH + "/package/packages_without_deleted_one.json");
		rootJSON = (JSONArray) jsonParser.parse(reader);
		List<JSONObject> expectedJSON = convert(rootJSON);
		
		data = given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
			
		rootJSON = (JSONArray) jsonParser.parse(data);
	
		List<JSONObject> actualJSON = convert(rootJSON);
		
		assertEquals("Package hasn't been added to the list of deleted packages", expectedJSONDeleted, actualJSONDeleted);
		assertEquals("Package hasn't been removed from the list of packages", expectedJSON, actualJSON);
	}	
	
	@Test
	public void nonPackageMaintainerShouldNotBeAbleToDeletePackage() {
		given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + PACKAGE_OLDER_ID + "/delete")
		.then()
			.statusCode(403);
	}	
	
	//TODO
	@Test
	public void shouldAdminShiftDeletePackage() throws IOException, ParseException {
		given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + PACKAGE_OLDER_ID + "/delete")
		.then()
			.statusCode(200)
			.body("success", equalTo("Package deleted successfully."));
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + PACKAGE_OLDER_ID + "/sdelete")
		.then()
			.statusCode(200)
			.body("success", equalTo("Package deleted successfully."));
	
		JSONParser jsonParser = new JSONParser();
	
		FileReader reader = new FileReader(JSON_PATH + "/package/deleted_packages_when_one_was_shift_deleted.json");
		JSONArray rootJSON = (JSONArray) jsonParser.parse(reader);
		List<JSONObject> expectedJSONDeleted = convert(rootJSON);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/deleted")
		.then()
			.statusCode(200)
			.extract()
			.asString();
			
		rootJSON = (JSONArray) jsonParser.parse(data);
	
		List<JSONObject> actualJSONDeleted = convert(rootJSON);
		
		reader = new FileReader(JSON_PATH + "/package/packages_without_deleted_one.json");
		rootJSON = (JSONArray) jsonParser.parse(reader);
		List<JSONObject> expectedJSON = convert(rootJSON);
		
		data = given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
			
		rootJSON = (JSONArray) jsonParser.parse(data);
	
		List<JSONObject> actualJSON = convert(rootJSON);
		
		assertEquals("Package hasn't been added to the list of deleted packages", expectedJSONDeleted, actualJSONDeleted);
		assertEquals("Package hasn't been removed from the list of packages", expectedJSON, actualJSON);
	}
	
	@Test
	public void shouldDownloadVignette() throws IOException {
		byte[] actual = given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(MediaType.APPLICATION_PDF_VALUE)
		.when()
			.get(API_PATH + VIGNETTE)
		.then()
			.statusCode(200)
			.extract()
		.asByteArray();
		
		File expected = new File(PDF_PATH + "/usl.pdf");

		assertTrue(Arrays.equals(actual, FileUtils.readFileToByteArray(expected)));
	}
	
	@Test
	public void shouldReturn404IfReferenceManualIsNotAvailable() {
		given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(MediaType.APPLICATION_PDF_VALUE)
		.when()
			.get(API_PATH + "/" + PACKAGE_ID_WITHOUT_MANUAL +"/download/nonexisting.pdf")
		.then()
			.statusCode(404);
	}
	
	@Test
	public void shouldReturn404IfVignetteIsNotAvailable() {
		given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(MediaType.APPLICATION_PDF_VALUE)
		.when()
			.get(API_PATH + "/" + PACKAGE_ID_WITHOUT_MANUAL +"/vignettes/nonexisting.pdf")
		.then()
			.statusCode(404);
	}
	
	@Test
	public void nonAdminShouldNotShiftDeletePackage() {
		given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + PACKAGE_OLDER_ID + "/delete")
		.then()
			.statusCode(200)
			.body("success", equalTo("Package deleted successfully."));
		
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + PACKAGE_OLDER_ID + "/sdelete")
		.then()
			.statusCode(403);
	}
}

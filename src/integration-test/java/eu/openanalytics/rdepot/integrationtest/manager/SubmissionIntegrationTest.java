/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
package eu.openanalytics.rdepot.integrationtest.manager;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.openanalytics.rdepot.integrationtest.IntegrationTest;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;


public class SubmissionIntegrationTest extends IntegrationTest{	
	private final String API_PACKAGES_PATH = "/api/manager/packages";
	private final String API_SUBMISSIONS_PATH = "/api/manager/submissions";
	private final String API_REPOSITORIES_PATH = "/api/manager/repositories";
	
	private final String SUBMISSION_ID_TO_ACCEPT = "30";
	private final String SUBMISSION_ID_TO_REJECT = "31";
		
	@Test
	public void shouldNotPublishPackageWhenRepositoryIsUnpublished() throws IOException {
		File packageBag = new File("src/integration-test/resources/itestPackages/visdat_0.1.0.tar.gz");

		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept("application/json")
			.contentType("multipart/form-data")
			.multiPart("repository", "testrepo3")
			.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
					.fileName(packageBag.getName())
					.mimeType("application/gzip")
					.controlName("file")
					.build())
		.when()
			.post(API_PACKAGES_PATH + "/submit")
		.then()
			.statusCode(200)
			.extract();
		
		given()
			.accept(ContentType.BINARY)
		.when()
			.get(PUBLICATION_URI_PATH + "/testrepo3/src/contrib/visdat_0.1.0.tar.gz")
		.then()
			.statusCode(404)
			.extract();
	}
	
	@Test
	public void shouldPublishPackage() throws IOException {
		File packageBag = new File("src/integration-test/resources/itestPackages/visdat_0.1.0.tar.gz");
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept("application/json")
			.contentType("multipart/form-data")
			.multiPart("repository", "testrepo1")
			.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
					.fileName(packageBag.getName())
					.mimeType("application/gzip")
					.controlName("file")
					.build())
		.when()
			.post(API_PACKAGES_PATH + "/submit")
		.then()
			.statusCode(200)
			.extract();
		
		byte[] uploadedPackage = given()
				.accept(ContentType.BINARY)
			.when()
				.get(PUBLICATION_URI_PATH + "/testrepo1/src/contrib/visdat_0.1.0.tar.gz")
				.asByteArray();
		
		byte[] expectedPackage = Files.readAllBytes(packageBag.toPath());
		assertTrue(Arrays.equals(uploadedPackage, expectedPackage));
	}

	@Test
	public void shouldPublishPackageInArchive() throws IOException {
		File packageBag = new File ("src/integration-test/resources/itestPackages/A3_0.9.1.tar.gz");
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept("application/json")
			.contentType("multipart/form-data")
			.multiPart("repository", "testrepo1")
			.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
					.fileName(packageBag.getName())
					.mimeType("application/gzip")
					.controlName("file")
					.build())
		.when()
			.post(API_PACKAGES_PATH + "/submit")
		.then()
			.statusCode(200)
			.extract();
		
		byte[] uploadedPackage = given()
				.accept(ContentType.BINARY)
			.when()
				.get(PUBLICATION_URI_PATH + "/testrepo1/src/contrib/Archive/A3/A3_0.9.1.tar.gz")
				.asByteArray();
		
		byte[] expectedPackage = Files.readAllBytes(packageBag.toPath());
		assertTrue(Arrays.equals(uploadedPackage, expectedPackage));
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void shouldUploadPackage() throws ParseException, IOException {
		File packageBag = new File ("src/integration-test/resources/itestPackages/A3_0.9.1.tar.gz");
				
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept("application/json")
			.contentType("multipart/form-data")
			.multiPart("repository", "testrepo1")
			.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
					.fileName(packageBag.getName())
					.mimeType("application/gzip")
					.controlName("file")
					.build())
		.when()
			.post(API_PACKAGES_PATH + "/submit")
		.then()
			.statusCode(200)
			.extract();
		
		FileReader reader = new FileReader(JSON_PATH + "/submission/repositories_after_uploading_package.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		List<Set> expectedPackages = convertNewPackagesFromRepo(expectedJSON);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_REPOSITORIES_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);
		
		List<Set> actualPackages = convertNewPackagesFromRepo(actualJSON);

		assertEquals(expectedPackages, actualPackages);
		assertTrue(compare(expectedJSON, actualJSON));
	}
	
	@SuppressWarnings({ "rawtypes" })
	@Test
	public void shouldAddPackageToWaitingSubmissions() throws ParseException, IOException {
		File packageBag = new File ("src/integration-test/resources/itestPackages/A3_0.9.2.tar.gz");
				
		try {
			given()
				.header(AUTHORIZATION, BEARER + USER_TOKEN)
				.accept("application/json")
				.contentType("multipart/form-data")
				.multiPart("repository", "testrepo2")
				.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
						.fileName(packageBag.getName())
						.mimeType("application/gzip")
						.controlName("file")
						.build())
			.when()
				.post(API_PACKAGES_PATH + "/submit")
			.then()
				.statusCode(200)
				.extract();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		FileReader reader = new FileReader(JSON_PATH + "/submission/submissions_user_wants_to_upload_package.json");
		
		JsonArray expectedSubmissions = (JsonArray) JsonParser.parseReader(reader);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_SUBMISSIONS_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		JsonArray actualSubmissions = (JsonArray) JsonParser.parseString(data);
		
		reader = new FileReader(JSON_PATH + "/submission/submissions_repositories_unchanged.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		List<Set> expectedPackages = convertNewPackagesFromRepo(expectedJSON);
		
		data = given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_REPOSITORIES_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);
		
		List<Set> actualPackages = convertNewPackagesFromRepo(actualJSON);

		assertEquals(expectedPackages, actualPackages);
		assertTrue(compare(expectedJSON, actualJSON));		
		assertTrue(compare(expectedSubmissions, actualSubmissions));
		
	}


	
	@Test
	public void shouldReturnSubmissionsWithUserCredentials() throws ParseException, IOException {
		FileReader reader = new FileReader(JSON_PATH + "/submission/submissions_view_by_user.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_SUBMISSIONS_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);
		
		assertTrue(compare(expectedJSON, actualJSON));
	}
	
	@Test
	public void shouldReturnSubmissionsWithPackageMaintainerCredentials() throws ParseException, IOException {
		FileReader reader = new FileReader(JSON_PATH + "/submission/submissions_view_by_package_maintainer.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_SUBMISSIONS_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);
		
		assertTrue(compare(expectedJSON, actualJSON));;
	}
	
	@Test
	public void nonUserShouldNotBeAbleToViewSubmissions() {
		given()
			.auth()
			.basic("", "")
			.accept(ContentType.JSON)
		.when()
			.get(API_SUBMISSIONS_PATH + "/list")
		.then()
			.statusCode(403);
	}


	@Test
	public void shouldReturnDeletedSubmissions() throws ParseException, IOException {
		FileReader reader = new FileReader(JSON_PATH + "/submission/deleted_submissions.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_SUBMISSIONS_PATH + "/deleted")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);
		
		assertTrue(compare(expectedJSON, actualJSON));
	}
	
	@Test
	public void nonAdminShouldNotBeAbleToViewDeletedSubmissions() {
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_SUBMISSIONS_PATH + "/deleted")
		.then()
			.statusCode(403);
	}

	@Test
	public void shouldAcceptSubmissionByRepositoryMaintainer() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.put(API_SUBMISSIONS_PATH + "/" + SUBMISSION_ID_TO_ACCEPT + "/accept")
		.then()
			.statusCode(200)
			.body("success", equalTo("Submission has been accepted successfully."));
		
		FileReader reader = new FileReader(JSON_PATH + "/submission/accepted_submission_by_repository_maintainer_packages.json");		
		JSONArray rootJSON = (JSONArray) jsonParser.parse(reader);
		Set<JSONObject> expectedPackages = convert(rootJSON);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PACKAGES_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
			
		rootJSON = (JSONArray) jsonParser.parse(data);
		Set<JSONObject> actualPackages = convert(rootJSON);
		
		reader = new FileReader(JSON_PATH + "/submission/accepted_submission_by_repository_maintainer_repositories.json");
		JsonArray expectedRepositories = (JsonArray) JsonParser.parseReader(reader);
		
		data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_REPOSITORIES_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualRepositories = (JsonArray) JsonParser.parseString(data);
		
		reader = new FileReader(JSON_PATH + "/submission/accepted_submission_by_repository_maintainer_submissions_view_by_submitter.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		data = given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_SUBMISSIONS_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);
		
		assertTrue("Different submissions",compare(expectedJSON, actualJSON));
		assertTrue("Different repositories", compare(expectedRepositories, actualRepositories));
		assertEquals("Differences in packages", expectedPackages, actualPackages);
	}
	
	@Test
	public void shouldRejectSubmissionByRepositoryMaintainer() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_SUBMISSIONS_PATH + "/" + SUBMISSION_ID_TO_REJECT + "/cancel")
		.then()
			.statusCode(200)
			.body("success", equalTo("Submission has been canceled successfully."));
		
		FileReader reader = new FileReader(JSON_PATH + "/submission/rejected_submission_by_repository_maintainer_packages.json");
		JSONArray rootJSON = (JSONArray) jsonParser.parse(reader);
		Set<JSONObject> expectedPackages = convert(rootJSON);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PACKAGES_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
			
		rootJSON = (JSONArray) jsonParser.parse(data);
		Set<JSONObject> actualPackages = convert(rootJSON);
			
		reader = new FileReader(JSON_PATH + "/submission/rejected_submission_by_repository_maintainer_repositories.json");
		JsonArray expectedRepositories = (JsonArray) JsonParser.parseReader(reader);
		
		data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_REPOSITORIES_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualRepositories = (JsonArray) JsonParser.parseString(data);
		
		reader = new FileReader(JSON_PATH + "/submission/rejected_submission_by_repository_maintainer_submissions_view_by_admin.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		data = given()
				.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
				.accept(ContentType.JSON)
			.when()
				.get(API_SUBMISSIONS_PATH + "/deleted")
			.then()
				.statusCode(200)
				.extract()
				.asString();
			
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);
		
		assertTrue("Different deleted submissions", compare(expectedJSON, actualJSON));
		assertTrue("Different repositoriies", compare(expectedRepositories, actualRepositories));
		assertEquals("Differences in packages", expectedPackages, actualPackages);
	}
	
	@Test
	public void nonOwnerShouldNotBeAbleToCancelSubmission() {
		given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_SUBMISSIONS_PATH + "/" + SUBMISSION_ID_TO_REJECT + "/cancel")
		.then()
			.assertThat()
			.body("error", equalTo("You are not authorized to perform this operation."))
			.and()
			.statusCode(200);
	}
	
	

	
	private boolean compare(JsonArray expected, JsonArray actual) throws ParseException {
		
		if (expected == null || actual == null)
			return false;
		
		if (expected.size() != actual.size())
			return false;
		
		for(int i = 0; i < expected.size(); i++) {
			JsonObject expectedRepository = (JsonObject) expected.get(i);
			JsonObject actualRepository = (JsonObject) actual.get(i);

			expectedRepository.remove("packages");
			actualRepository.remove("packages");
			
			if(!expectedRepository.equals(actualRepository))
				return false;
		}
		return true;
	}
	
	 @SuppressWarnings({ "rawtypes", "unchecked" })
	 private List<Set> convertNewPackagesFromRepo(JsonArray rootJSON) throws ParseException {
		 List<Set> JSON = new ArrayList<>();
				
		 for(int i = 0; i < rootJSON.size(); i++) {
			 JsonObject repositoryJSON = (JsonObject) rootJSON.get(i);
			 JsonArray packagesJSON = (JsonArray) repositoryJSON.get("packages");
			 Set JSONSet = new HashSet<>();
			 for(int k = 0; k < packagesJSON.size(); k++) {
				 JsonObject packageJSON = (JsonObject) packagesJSON.get(k);
				 String source = packageJSON.get("source").getAsString();
				 packageJSON.remove("source");
				 packageJSON.remove("md5sum");
				 String newSource = source.replaceFirst("/[0-9]{2}[0-9]+", "");
				 packageJSON.addProperty("source", newSource);
				 JSONSet.add(packageJSON);
			}
			JSON.add(JSONSet);
		}
		return JSON;
	}
		
	@SuppressWarnings("unused")
	private Set<JsonObject> convertNewPackages(JsonArray rootJSON) throws ParseException {	
		Set<JsonObject> JSON = new HashSet<>();
	
		for(int i = 0; i < rootJSON.size(); i++) {
			JsonObject objJSON = (JsonObject) rootJSON.get(i);
			String source = objJSON.get("source").getAsString();
			objJSON.remove("source");
			objJSON.remove("md5sum");
			String newSource = source.replaceFirst("/[0-9]{2}[0-9]+", "");
			objJSON.addProperty("source", newSource);
			JSON.add(objJSON);
		}	
		return JSON;
	}

}

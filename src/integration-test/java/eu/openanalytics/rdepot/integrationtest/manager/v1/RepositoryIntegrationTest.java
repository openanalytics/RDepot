/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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

import eu.openanalytics.rdepot.integrationtest.IntegrationTest;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;

public class RepositoryIntegrationTest extends IntegrationTest {
	
	public RepositoryIntegrationTest() {
		super("/api/manager/repositories");
	}

	private final String API_PACKAGES_PATH = "/api/manager/packages";
	
	private final String REPO_NAME_TO_CREATE = "testrepo7";
	private final String REPO_NAME_TO_EDIT = "newName";
	
	private final String REPO_ID_TO_PUBLISH = "5";
	private final String REPO_ID_TO_UNPUBLISH = "2";
	private final String REPO_ID_TO_DELETE = "2";
	private final String REPO_ID_TO_DELETE_UNPUBLISHED = "4";
	private final String REPO_ID_TO_EDIT = "2";
	private final String DELETED_REPOSITORY_ID = "5";
	
	@SuppressWarnings("rawtypes")
	@Test
	public void shouldCreateRepository() throws IOException, ParseException {
		Map<String, String> params = new HashMap<>();
		params.put("name", REPO_NAME_TO_CREATE);
		params.put("publicationUri", "http://localhost/repo/" + REPO_NAME_TO_CREATE);
		params.put("serverAddress", "http://oa-rdepot-repo:8080/" + REPO_NAME_TO_CREATE);
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/create")
		.then()
			.statusCode(200);
		
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repository/created_repository.json");
		JSONArray expectedJSON = (JSONArray) jsonParser.parse(reader);
		
		List<Set> expectedPackages = convertPackages(expectedJSON);
				
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONArray actualJSON = (JSONArray) jsonParser.parse(data);
		
		List<Set> actualPackages = convertPackages(actualJSON);

		assertEquals("Repository creating caused some changes in packages", expectedPackages, actualPackages);
		assertTrue("New repository hasn't been added", compare(expectedJSON, actualJSON));
	}
	
	@Test
	public void nonAdminShouldNotBeAbleToCreateRepository() {
		Map<String, String> params = new HashMap<>();
		params.put("name", REPO_NAME_TO_CREATE);
		params.put("publicationUri", "http://localhost/repo/" + REPO_NAME_TO_CREATE);
		params.put("serverAddress", "http://oa-rdepot-repo:8080/" + REPO_NAME_TO_CREATE);
		
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/create")
		.then()
			.statusCode(403);
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void shouldEditRepository() throws IOException, ParseException {
		Map<String, String> params = new HashMap<>();
		params.put("name", REPO_NAME_TO_EDIT);
		params.put("publicationUri", "http://localhost/repo/" + REPO_NAME_TO_EDIT);
		params.put("serverAddress", "http://oa-rdepot-repo:8080/" + REPO_NAME_TO_EDIT);
		
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
			.formParams(params)
		.when()
			.post(API_PATH + "/" + REPO_ID_TO_EDIT + "/edit")
		.then()
			.statusCode(200);
		
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repository/edited_repository.json");
		JSONArray expectedJSON = (JSONArray) jsonParser.parse(reader);
		
		List<Set> expectedPackages = convertPackages(expectedJSON);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONArray actualJSON = (JSONArray) jsonParser.parse(data);
		
		List<Set> actualPackages = convertPackages(actualJSON);

		assertEquals("Repository edition caused some changes in packages", expectedPackages, actualPackages);
		assertTrue("Repository hasn't been edited", compare(expectedJSON, actualJSON));
	}
	
	@Test
	public void nonRepositoryMaintainerShouldNotBeAbleToEditRepository() {
		Map<String, String> params = new HashMap<>();
		params.put("name", REPO_NAME_TO_EDIT);
		params.put("publicationUri", "http://localhost/repo/" + REPO_NAME_TO_EDIT);
		params.put("serverAddress", "http://oa-rdepot-repo:8080/" + REPO_NAME_TO_EDIT);
		
		given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
			.formParams(params)
		.when()
			.post(API_PATH + "/" + REPO_ID_TO_EDIT + "/edit")
		.then()
			.statusCode(403);
	}
	
	@SuppressWarnings({"rawtypes"})
	@Test
	public void shouldReturnRepositories() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repository/list_repositories.json");
		JSONArray expectedJSON = (JSONArray) jsonParser.parse(reader);
		
		List<Set> expectedPackages = convertPackages(expectedJSON);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONArray actualJSON = (JSONArray) jsonParser.parse(data);
		
		List<Set> actualPackages = convertPackages(actualJSON);

		assertEquals("There are some differances in packages which admin sees", expectedPackages, actualPackages);
		assertTrue("There are some differances in repositories which admin sees", compare(expectedJSON, actualJSON));
	}
		
	@SuppressWarnings("rawtypes")
	@Test
	public void shouldPublishRepository() throws IOException, ParseException, InterruptedException {
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/" + REPO_ID_TO_PUBLISH + "/publish")
		.then()
			.statusCode(200)
			.body("success", equalTo("Repository has been published successfully."));
		
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repository/published_repository.json");
		JSONArray expectedJSON = (JSONArray) jsonParser.parse(reader);
		
		List<Set> expectedPackages = convertPackages(expectedJSON);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONArray actualJSON = (JSONArray) jsonParser.parse(data);
		
		List<Set> actualPackages = convertPackages(actualJSON);

		assertEquals("Repository publishing caused some changes in packages", expectedPackages, actualPackages);
		assertTrue("Repository hasn't been published", compare(expectedJSON, actualJSON));
		
		int exitValue = -1;
		
		String[] cmd = new String[] {"gradle", "checkIfSnapshotWasCreated", "-b","src/integration-test/resources/build.gradle"};
		Process process = Runtime.getRuntime().exec(cmd);
		exitValue = process.waitFor();
		process.destroy();
		
		assertTrue("Snapshot wasn't created", exitValue == 0);
	}
	
	@Test
	public void nonRepositoryMaintainerShouldNotBeAbleToPublishRepository() {
		given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/" + REPO_ID_TO_PUBLISH + "/publish")
		.then()
			.statusCode(403);
	}
	
	
	//Remember to create symbolic link 'current' in src/integration-test/resources/docker/app/repository/2/{date}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void shouldUnpublishRepository() throws IOException, ParseException {
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/" + REPO_ID_TO_UNPUBLISH + "/unpublish")
		.then()
			.statusCode(200)
			.body("success", equalTo("Repository has been unpublished successfully."));
		
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repository/unpublished_repository.json");
		JSONArray expectedJSON = (JSONArray) jsonParser.parse(reader);
		
		List<Set> expectedPackages = convertPackages(expectedJSON);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONArray actualJSON = (JSONArray) jsonParser.parse(data);
		
		List<Set> actualPackages = convertPackages(actualJSON);

		assertEquals("Repository unpublishing caused some changes in packages", expectedPackages, actualPackages);
		assertTrue("Repository hasn't been unpublished", compare(expectedJSON, actualJSON));
	}

	@Test
	public void nonRepositoryMaintainerShouldNotBeAbleToUnpublishRepository() {
		given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/" + REPO_ID_TO_UNPUBLISH + "/unpublish")
		.then()
			.statusCode(403);
	}	
	
	@SuppressWarnings("rawtypes")
	@Test
	public void shouldDeleteRepository() throws IOException, ParseException, InterruptedException {
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + REPO_ID_TO_DELETE + "/delete")
		.then()
			.statusCode(200)
			.body("success", equalTo("Repository has been deleted successfully."));
		
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repository/repositories_without_one.json");
		JSONArray expectedJSON = (JSONArray) jsonParser.parse(reader);
		
		List<Set> expectedPackages = convertPackages(expectedJSON);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONArray actualJSON = (JSONArray) jsonParser.parse(data);
		
		List<Set> actualPackages = convertPackages(actualJSON);

		assertEquals("Repository deletion caused some changes in packages in active repositories", expectedPackages, actualPackages);
		assertTrue("Repository hasn't been removed", compare(expectedJSON, actualJSON));
				
		int exitValue = -1;
		
		String[] cmd = new String[] {"gradle", "checkServer", "-b","src/integration-test/resources/build.gradle"};
		Process process = Runtime.getRuntime().exec(cmd);
		exitValue = process.waitFor();
		process.destroy();
		
		assertTrue("Files haven't been removed from server", exitValue == 0);
	}
	
	@Test
	public void shouldRemovePackagesWhenUnpublishedRepositoryIsDeleted() throws IOException, ParseException {
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + REPO_ID_TO_DELETE_UNPUBLISHED + "/delete")
		.then()
			.statusCode(200)
			.body("success", equalTo("Repository has been deleted successfully."));
		
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repository/packages_after_removing_unpublished_repository.json");
		JSONArray expectedJSONArray = (JSONArray) jsonParser.parse(reader);
		Set<JSONObject> expectedJSON = convert(expectedJSONArray);

		String data = given()
				.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
				.accept(ContentType.JSON)
		.when()
			.get(API_PACKAGES_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
			
		JSONArray actualJSONArray = (JSONArray) jsonParser.parse(data);
		Set<JSONObject> actualJSON = convert(actualJSONArray);

		assertEquals("Differences in packages", expectedJSON, actualJSON);
	}
	
	@Test
	public void shouldCreateRepositoryUploadPackageAndPublishRepository() throws IOException, InterruptedException {
		Map<String, String> params = new HashMap<>();
		params.put("name", REPO_NAME_TO_CREATE);
		params.put("publicationUri", "http://localhost/repo/" + REPO_NAME_TO_CREATE);
		params.put("serverAddress", "http://oa-rdepot-repo:8080/" + REPO_NAME_TO_CREATE);
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/create")
		.then()
			.statusCode(200);
				
		File packageBag = new File ("src/integration-test/resources/itestPackages/visdat_0.1.0.tar.gz");
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept("application/json")
			.contentType("multipart/form-data")
			.multiPart("repository", "testrepo7")
			.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
					.fileName(packageBag.getName())
					.mimeType("application/gzip")
					.controlName("file")
					.build())
		.when()
			.post("/api/manager/packages/submit")
		.then()
			.statusCode(200)
			.extract();
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/8/publish")
		.then()
			.statusCode(200)
			.body("success", equalTo("Repository has been published successfully."));
		
		byte[] uploadedPackage = given()
				.accept(ContentType.BINARY)
			.when()
				.get(PUBLICATION_URI_PATH + "/testrepo7/src/contrib/visdat_0.1.0.tar.gz")
				.asByteArray();
		
		byte[] expectedPackage = Files.readAllBytes(packageBag.toPath());
		assertTrue(Arrays.equals(uploadedPackage, expectedPackage));
	
		int exitValue = -1;
		
		String[] cmd = new String[] {"gradle", "checkOnServerIfRepositoryWithOnlyOnePackageDoesNotHaveArchive", "-b","src/integration-test/resources/build.gradle"};
		Process process = Runtime.getRuntime().exec(cmd);
		exitValue = process.waitFor();
		process.destroy();
		
		assertTrue("Archive directory should be empty", exitValue == 1);
		
		exitValue = -1;
		
		cmd = new String[] {"gradle", "checkIfSourceOfPackageExistsInFilesystem", "-b","src/integration-test/resources/build.gradle"};
		process = Runtime.getRuntime().exec(cmd);
		exitValue = process.waitFor();
		process.destroy();
		
		assertTrue("Source of package doesn't exist in filesystem", exitValue == 0);
		
		exitValue = -1;
		
		cmd = new String[] {"gradle", "checkIfSymbolicLinkWasCreated", "-b","src/integration-test/resources/build.gradle"};
		process = Runtime.getRuntime().exec(cmd);
		exitValue = process.waitFor();
		process.destroy();
		
		assertTrue("Symbolic link in snapshot wasn't created", exitValue == 0);
	}
	
	@Test
	public void shouldNonAdminUserNotBeAbleToDeleteRepository() {
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + DELETED_REPOSITORY_ID + "/delete")
		.then()
			.statusCode(403);
	}	
	
	@SuppressWarnings({ "rawtypes" })
	@Test
	public void shouldAdminShiftDeleteRepository() throws IOException, ParseException, InterruptedException {
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + REPO_ID_TO_DELETE + "/delete")
		.then()
			.statusCode(200)
			.body("success", equalTo("Repository has been deleted successfully."));
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + REPO_ID_TO_DELETE + "/sdelete")
		.then()
			.statusCode(200)
			.body("success", equalTo("Repository has been deleted successfully."));
		
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repository/repositories_without_one.json");
		JSONArray expectedJSON = (JSONArray) jsonParser.parse(reader);
		
		List<Set> expectedPackages = convertPackages(expectedJSON);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONArray actualJSON = (JSONArray) jsonParser.parse(data);
		
		List<Set> actualPackages = convertPackages(actualJSON);

		assertEquals("Repository deletion caused some changes in packages in active repositories", expectedPackages, actualPackages);
		assertTrue("Repository hasn't been removed", compare(expectedJSON, actualJSON));
				
		int exitValue = -1;
		
		String[] cmd = new String[] {"gradle", "checkServer", "-b","src/integration-test/resources/build.gradle"};
		Process process = Runtime.getRuntime().exec(cmd);
		exitValue = process.waitFor();
		process.destroy();
		
		assertTrue("Files haven't been removed from server", exitValue == 0);
	}
	
	@Test
	public void shouldNonAdminUserNotBeAbleToShiftDeleteRepository() {
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + REPO_ID_TO_DELETE + "/sdelete")
		.then()
			.statusCode(403);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Set> convertPackages(JSONArray rootJSON) throws ParseException {
		List<Set> JSON = new ArrayList<>();
		
		for(int i = 0; i < rootJSON.size(); i++) {
			JSONObject repositoryJSON = (JSONObject) rootJSON.get(i);
			JSONArray packagesJSON = (JSONArray) repositoryJSON.get("packages");
			Set JSONSet = new HashSet<>();
			for(int k = 0; k < packagesJSON.size(); k++) {
				JSONObject packageJSON = (JSONObject) packagesJSON.get(k);
				JSONSet.add(packageJSON);
			}
			JSON.add(JSONSet);
		}
		return JSON;
	}
	
	private boolean compare(JSONArray expected, JSONArray actual) throws ParseException {
		
		if (expected == null || actual == null)
			return false;
		
		if (expected.size() != actual.size())
			return false;
		
		for(int i = 0; i < expected.size(); i++) {
			JSONObject expectedRepository = (JSONObject) expected.get(i);
			JSONObject actualRepository = (JSONObject) actual.get(i);

			expectedRepository.remove("packages");
			actualRepository.remove("packages");
			
			if(!expectedRepository.equals(actualRepository))
				return false;
		}
		return true;
	}
}

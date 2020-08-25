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

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class RepositoryIntegrationTest {
	private static final String JSON_PATH = "src/integration-test/resources/JSONs";
	
	private static final String REPO_NAME_TO_CREATE = "testrepo7";
	private static final String REPO_NAME_TO_EDIT = "newName";
	
	private static final String REPO_ID_TO_PUBLISH = "5";
	private static final String REPO_ID_TO_UNPUBLISH = "2";
	private static final String REPO_ID_TO_DELETE = "2";
	private static final String REPO_ID_TO_EDIT = "2";
	
	private static final String API_PATH = "/api/manager/repositories";
	private static final String ORDINARY_PATH = "/manager/repositories";
	
	private static final String ADMIN_LOGIN = "einstein";
	private static final String REPOSITORYMAINTAINER_LOGIN = "tesla";
	private static final String PACKAGEMAINTAINER_LOGIN = "galieleo";
	private static final String USER_LOGIN = "newton";
	private static final String PASSWORD = "testpassword";
	
	private static final String DELETED_REPOSITORY_ID = "5";
	
	@Before
	public void setUp() throws IOException, InterruptedException {
		String[] cmd = new String[] {"gradle", "restore", "-b","src/integration-test/resources/build.gradle"};
		Process process = Runtime.getRuntime().exec(cmd);
		process.waitFor();
		process.destroy();
	}
	
	@BeforeClass
	public static void configureRestAssured() {
		RestAssured.port = 8017;
		RestAssured.urlEncodingEnabled = false;

	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void shouldCreateRepository() throws IOException, ParseException {
		Map<String, String> params = new HashMap<>();
		params.put("name", REPO_NAME_TO_CREATE);
		params.put("publicationUri", "http://localhost/repo/" + REPO_NAME_TO_CREATE);
		params.put("serverAddress", "http://oa-rdepot-repo:8080/" + REPO_NAME_TO_CREATE);
		
		given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
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
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
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
	public void shouldNonAdminUserNotBeAbleToCreateRepository() {
		Map<String, String> params = new HashMap<>();
		params.put("name", REPO_NAME_TO_CREATE);
		params.put("publicationUri", "http://localhost/repo/" + REPO_NAME_TO_CREATE);
		params.put("serverAddress", "http://oa-rdepot-repo:8080/" + REPO_NAME_TO_CREATE);
		
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.contentType(ContentType.JSON)
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
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
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
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
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
	public void shouldNonRepositoryMaintainerUserNotBeAbleToEditRepository() {
		Map<String, String> params = new HashMap<>();
		params.put("name", REPO_NAME_TO_EDIT);
		params.put("publicationUri", "http://localhost/repo/" + REPO_NAME_TO_EDIT);
		params.put("serverAddress", "http://oa-rdepot-repo:8080/" + REPO_NAME_TO_EDIT);
		
		given()
			.auth()
			.basic(PACKAGEMAINTAINER_LOGIN, PASSWORD)
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
		
		FileReader reader = new FileReader(JSON_PATH + "/repository/list_repositories_user.json");
		JSONArray expectedJSON = (JSONArray) jsonParser.parse(reader);
		
		List<Set> expectedPackages = convertPackages(expectedJSON);
		
		String data = given()
			.auth()
			.basic(USER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONArray actualJSON = (JSONArray) jsonParser.parse(data);
		
		List<Set> actualPackages = convertPackages(actualJSON);

		assertEquals(expectedPackages, actualPackages);
		assertTrue(compare(expectedJSON, actualJSON));
	}
	
	@Test
	public void shouldNotReturnRepositories() {
		String html = given()
				.accept(ContentType.JSON)
			.when()
				.get(ORDINARY_PATH + "/list")
			.then()
				.statusCode(200)
				.extract()
				.asString();
			
		assertTrue("No redirection to login page", html.contains("Please enter your user credentials."));
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void shouldPublishRepository() throws IOException, ParseException {
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.post(API_PATH + "/" + REPO_ID_TO_PUBLISH + "/publish")
		.then()
			.statusCode(200)
			.body("success", equalTo("repository.published"));
		
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repository/published_repository.json");
		JSONArray expectedJSON = (JSONArray) jsonParser.parse(reader);
		
		List<Set> expectedPackages = convertPackages(expectedJSON);
		
		String data = given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
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
	}
	
	@Test
	public void shouldNonRepositoryMaintainerUserNotBeAbleToPublishRepository() {
		given()
			.auth()
			.basic(PACKAGEMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.post(API_PATH + "/" + REPO_ID_TO_PUBLISH + "/publish")
		.then()
			.statusCode(403);
	}
	
	
	//Remember to create symbolic link 'current' in src/integration-test/resources/docker/app/repository/2/{date}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void shouldUnpublishRepository() throws IOException, ParseException {
		given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.post(API_PATH + "/" + REPO_ID_TO_UNPUBLISH + "/unpublish")
		.then()
			.statusCode(200)
			.body("success", equalTo("repository.unpublished"));
		
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repository/unpublished_repository.json");
		JSONArray expectedJSON = (JSONArray) jsonParser.parse(reader);
		
		List<Set> expectedPackages = convertPackages(expectedJSON);
		
		String data = given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
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
	public void shouldNonRepositoryMaintainerUserNotBeAbleToUnpublishRepository() {
		given()
			.auth()
			.basic(PACKAGEMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.post(API_PATH + "/" + REPO_ID_TO_UNPUBLISH + "/unpublish")
		.then()
			.statusCode(403);
	}	
	
	@SuppressWarnings("rawtypes")
	@Test
	public void shouldDeleteRepository() throws IOException, ParseException, InterruptedException {
		given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + REPO_ID_TO_DELETE + "/delete")
		.then()
			.statusCode(200)
			.body("success", equalTo("repository.deleted"));
		
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repository/repositories_without_one.json");
		JSONArray expectedJSON = (JSONArray) jsonParser.parse(reader);
		
		List<Set> expectedPackages = convertPackages(expectedJSON);
		
		String data = given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
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
		
		assertTrue("Files have been removed from server", exitValue == 0);
	}
	
	@Test
	public void shouldNonAdminUserNotBeAbleToDeleteRepository() {
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + DELETED_REPOSITORY_ID + "/delete")
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

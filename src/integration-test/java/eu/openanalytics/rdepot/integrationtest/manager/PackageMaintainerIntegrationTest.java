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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class PackageMaintainerIntegrationTest {
	private static final String JSON_PATH = "src/integration-test/resources/JSONs";
	private static final String API_PATH = "/api/manager/packages/maintainers";
	
	private static final String FIRST_REPO_ID = "2";
	private static final String SECOND_REPO_ID = "5";
	
	private static final String ADMIN_LOGIN = "einstein";
	private static final String REPOSITORYMAINTAINER_LOGIN = "tesla";
	private static final String PACKAGEMAINTAINER_LOGIN = "galieleo";
	private static final String PASSWORD = "testpassword";
	
	private static final String USER_ID_TO_CREATE = "6";
	private static final String PACKAGEMAINTAINER_ID_TO_DELETE = "2";
	private static final String PACKAGEMAINTAINER_ID_TO_EDIT = "2";
	private static final String PACKAGEMAINTAINER_ID_TO_RECREATE = "5";
	
	private static final String PACKAGE_NAME_TO_CREATE = "usl";
	private static final String PACKAGE_NAME_TO_EDIT = "abc";
	
	@Before
	public void setUp() throws IOException, InterruptedException {
		String[] cmd = new String[] {"gradle", "restore", "-b","src/integration-test/resources/build.gradle"};
		Process process = Runtime.getRuntime().exec(cmd);
		process.waitFor();
		process.destroy();
	}
	
	@BeforeClass
	public static void configureRestAssured() throws IOException, InterruptedException {
		RestAssured.port = 8017;
		RestAssured.urlEncodingEnabled = false;

	}
	
	@Test
	public void shouldReturnPackageMaintainers() throws ParseException, IOException {
		JsonParser jsonParser = new JsonParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/packageMaintainer/list_package_maintainers_view_by_repo_maintainer.json");
		JsonArray expectedJSON = (JsonArray) jsonParser.parse(reader);
		reader = new FileReader(JSON_PATH + "/packageMaintainer/list_package_maintainers_view_by_admin.json");
		JsonArray wrongJSON = (JsonArray) jsonParser.parse(reader);
		
		String data = given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) jsonParser.parse(data);

		assertTrue("Differences in JSON view by Repository Maintainer", compareArrays(expectedJSON, actualJSON));
		assertFalse("Repository Maintainer can see JSON only allowed to Admin", compareArrays(wrongJSON, actualJSON));
	}
	
	@Test
	public void shouldNonRepositoryMaintainerUserNotBeAbleToReturnPackageMaintainers() {
		given()
			.auth()
			.basic(PACKAGEMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldReturnDeletedPackageMaintainers() throws ParseException, IOException {
		JsonParser jsonParser = new JsonParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/packageMaintainer/deleted_package_maintainers.json");
		JsonArray expectedJSON = (JsonArray) jsonParser.parse(reader);
		
		String data = given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/deleted")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) jsonParser.parse(data);

		assertTrue("Differences in deleted package maintainers JSON", compareArrays(expectedJSON, actualJSON));
	}
	
	@Test
	public void shouldNonRepositoryMaintainerUserNotBeAbleToReturnDeletedPackageMaintainers() {
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/deleted")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldListPackageMaintainersWithGetCreate() throws IOException, ParseException {
		JsonParser jsonParser = new JsonParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/packageMaintainer/get_create_package_maintainers_view_by_repo_maintainer.json");
		JsonObject expectedJSON = (JsonObject) jsonParser.parse(reader);
		
		reader = new FileReader(JSON_PATH + "/packageMaintainer/get_create_package_maintainers_view_by_admin.json");
		JsonObject wrongJSON = (JsonObject) jsonParser.parse(reader);
		
		String data = given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/create")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonObject actualJSON = (JsonObject) jsonParser.parse(data);
		
		assertTrue(compareObjects(expectedJSON, actualJSON));
		assertFalse(compareObjects(wrongJSON, actualJSON));
	}
	
	@Test
	public void shouldNonRepositoryMaintainerUserNotBeAbleToCreateNewPackageMaintainerWithMethodGet() {
		given()
			.auth()
			.basic(PACKAGEMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/create")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldCreateNewPackageMaintainer() throws IOException, ParseException {
		Map<String, String> params = new HashMap<>();
		params.put("userId", USER_ID_TO_CREATE);
		params.put("repositoryId", FIRST_REPO_ID);
		params.put("packageName", PACKAGE_NAME_TO_CREATE);
		
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/create")
		.then()
			.statusCode(200);
		
		JsonParser jsonParser = new JsonParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/packageMaintainer/list_created_package_maintainer_view_by_admin.json");
		JsonArray expectedJSON = (JsonArray) jsonParser.parse(reader);
		
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
		
		JsonArray actualJSON = (JsonArray) jsonParser.parse(data);

		assertTrue(compareArrays(expectedJSON, actualJSON));
	}
	
	@Test
	public void shouldNonRepositoryMaintainerUserNotBeAbleToCreateNewPackageMaintainerWithMethodPost() {
		Map<String, String> params = new HashMap<>();
		params.put("userId", USER_ID_TO_CREATE);
		params.put("repositoryId", FIRST_REPO_ID);
		params.put("packageName", PACKAGE_NAME_TO_CREATE);
		
		given()
			.auth()
			.basic(PACKAGEMAINTAINER_LOGIN, PASSWORD)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/create")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldEditPackageMaintainer() throws IOException, ParseException {
		Map<String, String> params = new HashMap<>();
		params.put("repositoryId", SECOND_REPO_ID);
		params.put("packageName", PACKAGE_NAME_TO_EDIT);
		
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/" + PACKAGEMAINTAINER_ID_TO_EDIT + "/edit")
		.then()
			.statusCode(200);
		
		JsonParser jsonParser = new JsonParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/packageMaintainer/edited_package_maintainers_view_by_admin.json");
		JsonArray expectedJSON = (JsonArray) jsonParser.parse(reader);
		
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
			
		JsonArray actualJSON = (JsonArray) jsonParser.parse(data);

		assertTrue(compareArrays(expectedJSON, actualJSON));
	}
	
	@Test
	public void shouldNonRepositoryMaintainerUserNotBeAbleToEditPackageMaintainer() {
		Map<String, String> params = new HashMap<>();
		params.put("repositoryId", SECOND_REPO_ID);
		params.put("packageName", PACKAGE_NAME_TO_EDIT);
		
		given()
			.auth()
			.basic(PACKAGEMAINTAINER_LOGIN, PASSWORD)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/" + PACKAGEMAINTAINER_ID_TO_EDIT + "/edit")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldDeletePackageMaintainer() throws IOException, ParseException {		
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + PACKAGEMAINTAINER_ID_TO_DELETE + "/delete")
		.then()
			.statusCode(200);
		
		JsonParser jsonParser = new JsonParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/packageMaintainer/list_package_maintainers_without_one.json");
		JsonArray expectedJSON = (JsonArray) jsonParser.parse(reader);
		
		String data = given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) jsonParser.parse(data);
		
		reader = new FileReader(JSON_PATH + "/packageMaintainer/deleted_package_maintainers_with_new_one.json");
		JsonArray expectedDeleted = (JsonArray) jsonParser.parse(reader);
		
		String deleted = given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/deleted")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualDeleted = (JsonArray) jsonParser.parse(deleted);

		assertTrue(compareArrays(expectedJSON, actualJSON));
		assertTrue(compareArrays(expectedDeleted, actualDeleted));
	}
	
	@Test
	public void shouldNonRepositoryMaintainerUserNotBeAbleToDeletePackageMaintainer() {
		given()
			.auth()
			.basic(PACKAGEMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + PACKAGEMAINTAINER_ID_TO_DELETE + "/delete")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldRecreatePackageMaintainer() throws FileNotFoundException, ParseException {
		Map<String, String> params = new HashMap<>();
		params.put("userId", USER_ID_TO_CREATE);
		params.put("repositoryId", FIRST_REPO_ID);
		params.put("packageName", PACKAGE_NAME_TO_CREATE);
		
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/create")
		.then()
			.statusCode(200);
		
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + PACKAGEMAINTAINER_ID_TO_RECREATE + "/delete")
		.then()
			.statusCode(200);
		
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()	
			.post(API_PATH + "/create")
		.then()
			.statusCode(200);
		
		JsonParser jsonParser = new JsonParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/packageMaintainer/package_maintainers_recreated.json");
		JsonArray expectedJSON = (JsonArray) jsonParser.parse(reader);
		
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
		
		JsonArray actualJSON = (JsonArray) jsonParser.parse(data);
		
		reader = new FileReader(JSON_PATH + "/packageMaintainer/deleted_package_maintainers_recreated.json");
		JsonArray expectedDeleted = (JsonArray) jsonParser.parse(reader);
		
		String deleted = given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/deleted")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualDeleted = (JsonArray) jsonParser.parse(deleted);

		assertTrue(compareArrays(expectedJSON, actualJSON));
		assertTrue(compareArrays(expectedDeleted, actualDeleted));
	}
	
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	private List<Set> convertPackages(JsonArray rootJSON) throws ParseException {
//		List<Set> JSON = new ArrayList<>();
//		
//		for(int i = 0; i < rootJSON.size(); i++) {
//			JsonObject repositoryJSON = (JsonObject) rootJSON.get(i);
//			JsonArray packagesJSON = (JsonArray) repositoryJSON.get("packages");
//			Set JSONSet = new HashSet<>();
//			for(int k = 0; k < packagesJSON.size(); k++) {
//				JsonObject packageJSON = (JsonObject) packagesJSON.get(k);
//				JSONSet.add(packageJSON);
//			}
//			JSON.add(JSONSet);
//		}
//		return JSON;
//	}
	
	private boolean compareArrays(JsonArray expectedJSON, JsonArray actualJSON) throws ParseException {
		
		if (expectedJSON == null || actualJSON == null)
			return false;
		
//		if (expectedJSON.size() != actualJSON.size())
//			return false;
		
		for(int i = 0; i < expectedJSON.size(); i++) {
			JsonObject expectedRepository = (JsonObject) expectedJSON.get(i);
			JsonObject actualRepository = (JsonObject) actualJSON.get(i);

			expectedRepository.getAsJsonObject("user").remove("role");
			expectedRepository.getAsJsonObject("user").remove("lastLoggedInOn");
			expectedRepository.getAsJsonObject("repository").remove("packages");
			actualRepository.getAsJsonObject("user").remove("role");
			actualRepository.getAsJsonObject("repository").remove("packages");
			actualRepository.getAsJsonObject("user").remove("lastLoggedInOn");
			
			expectedRepository.remove("packages");
			actualRepository.remove("packages");
			
			if(!expectedRepository.equals(actualRepository))
				return false;
		}
		return true;
	}

	private boolean compareObjects(JsonObject expected, JsonObject actual) throws ParseException {
		
		if (expected == null || actual == null)
			return false;
		
		expected.remove("repositories");
		actual.remove("repositories");
		
		if(!expected.equals(actual))
			return false;
		
		return true;
	}
}

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
package eu.openanalytics.rdepot.integrationtest;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class RepositoryMaintainerIntegrationTest {
	private static final String JSON_PATH = "src/integration-test/resources/JSONs";
	private static final String API_PATH = "/api/manager/repositories/maintainers";
	
	private static final String REPOSITORYMAINTAINER_ID_TO_CREATE = "5";
	
	private static final String THIRD_REPO_ID_UNPUBLISHED = "4";
	private static final String SECOND_REPO_ID_PUBLISHED = "3";
	
	private static final String ADMIN_LOGIN = "einstein";
	private static final String REPOSITORYMAINTAINER_LOGIN = "tesla";
	private static final String PASSWORD = "testpassword";
	
	private static final String REPOSITORYMAINTAINER_ID_TO_DELETE = "1";
	private static final String REPOSITORYMAINTAINER_ID_TO_EDIT = "1";
	private static final String REPOSITORYMAINTAINER_ID_TO_RECREATE = "2";
	
	private static final String REPOSITORY_ID_TO_EDIT = "3";
	
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
		
	@SuppressWarnings({"rawtypes"})
	@Test
	public void shouldCreateNewRepositoryMaintainerWithGet() throws IOException, ParseException {
		JsonParser JsonParser = new JsonParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repositoryMaintainer/get_create_repository_maintainers.json");
		JsonObject expectedJSON = (JsonObject) JsonParser.parse(reader);
		
		List<Set> expectedPackages = convertPackages((JsonArray) expectedJSON.get("repositories"));
		
		String data = given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/create")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonObject actualJSON = (JsonObject) JsonParser.parse(data);
		
		List<Set> actualPackages = convertPackages((JsonArray) actualJSON.get("repositories"));
		
		assertEquals(expectedPackages, actualPackages);
		assertTrue(compareArrays((JsonArray) expectedJSON.get("repositories"), (JsonArray) actualJSON.get("repositories")));
		assertTrue(compareObjects(expectedJSON, actualJSON));
	}
	
	@Test
	public void shouldNonAdminUserNotBeAbleToCreateNewRepositoryMaintainerWithGet() {
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/create")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldCreateNewRepositoryMaintainerWithPostUnpublished() throws IOException, ParseException {
		Map<String, String> params = new HashMap<>();
		params.put("userId", REPOSITORYMAINTAINER_ID_TO_CREATE);
		
		params.put("repositoryId", THIRD_REPO_ID_UNPUBLISHED);
		
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
		
		JsonParser JsonParser = new JsonParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repositoryMaintainer/created_repository_maintainers_unpublished.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parse(reader);
		
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
		
		JsonArray actualJSON = (JsonArray) JsonParser.parse(data);

		assertTrue(compareMaintainers(expectedJSON, actualJSON));
	}
	
	@Test
	public void shouldCreateNewRepositoryMaintainerWithPostPublished() throws IOException, ParseException {
		Map<String, String> params = new HashMap<>();
		params.put("userId", REPOSITORYMAINTAINER_ID_TO_CREATE);
		params.put("repositoryId", SECOND_REPO_ID_PUBLISHED);
		
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
		
		JsonParser JsonParser = new JsonParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repositoryMaintainer/created_repository_maintainers_published.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parse(reader);
		
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
		
		JsonArray actualJSON = (JsonArray) JsonParser.parse(data);

		assertTrue(compareMaintainers(expectedJSON, actualJSON));
	}
	
	@Test
	public void shouldNonAdminUserNotBeAbleToCreateNewRepositoryMaintainerWithMethodPost() {
		Map<String, String> params = new HashMap<>();
		params.put("userId", REPOSITORYMAINTAINER_ID_TO_CREATE);

		params.put("repositoryId", THIRD_REPO_ID_UNPUBLISHED);
		
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/create")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldEditRepositoryMaintainer() throws IOException, ParseException {
		Map<String, String> params = new HashMap<>();
		params.put("repositoryId", REPOSITORY_ID_TO_EDIT);
		
		given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/" + REPOSITORYMAINTAINER_ID_TO_EDIT + "/edit")
		.then()
			.statusCode(200);
		
		JsonParser JsonParser = new JsonParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repositoryMaintainer/edited_repository_maintainers.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parse(reader);
		
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
			
			JsonArray actualJSON = (JsonArray) JsonParser.parse(data);

			assertTrue(compareMaintainers(expectedJSON, actualJSON));
	}
	
	@Test
	public void shouldNonAdminUserNotBeAbleToEditRepositoryMaintainer() {
		Map<String, String> params = new HashMap<>();
		params.put("repositoryId", REPOSITORY_ID_TO_EDIT);
		
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/" + REPOSITORYMAINTAINER_ID_TO_EDIT + "/edit")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldReturnRepositoryMaintainers() throws IOException, ParseException {
		JsonParser JsonParser = new JsonParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repositoryMaintainer/repository_maintainers.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parse(reader);
		
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
		
		JsonArray actualJSON = (JsonArray) JsonParser.parse(data);

		assertTrue(compareMaintainers(expectedJSON, actualJSON));
	}
	
	@Test
	public void shouldNonAdminUserNotBeAbleToReturnRepositoryMaintainers() {
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldReturnDeletedRepositoryMaintainers() throws IOException, ParseException {
		JsonParser JsonParser = new JsonParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repositoryMaintainer/deleted_repository_maintainers.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parse(reader);
			
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
		
		JsonArray actualJSON = (JsonArray) JsonParser.parse(data);

		assertTrue(compareMaintainers(expectedJSON, actualJSON));
	}
	
	@Test
	public void shouldNonAdminUserNotBeAbleToReturnDeletedRepositoryMaintainers() {
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
	public void shouldDeleteRepositoryMaintainer() throws IOException, ParseException {
		given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + REPOSITORYMAINTAINER_ID_TO_DELETE + "/delete")
		.then()
			.statusCode(200);
		
		JsonParser jsonParser = new JsonParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repositoryMaintainer/repository_maintainers_without_one.json");
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
		
		reader = new FileReader(JSON_PATH + "/repositoryMaintainer/deleted_repository_maintainers_with_new_one.json");
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
	
		assertTrue(compareMaintainers(expectedJSON, actualJSON));
		assertTrue(compareMaintainers(expectedDeleted, actualDeleted));
	}
	
	@Test
	public void shouldRecreateRepositoryMaintainer() throws FileNotFoundException, ParseException {
		Map<String, String> params = new HashMap<>();
		params.put("userId", REPOSITORYMAINTAINER_ID_TO_CREATE);
		params.put("repositoryId", THIRD_REPO_ID_UNPUBLISHED);
		
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
		
		given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + REPOSITORYMAINTAINER_ID_TO_RECREATE  +"/delete")
		.then()
			.statusCode(200);
		
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
		
		JsonParser JsonParser = new JsonParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/repositoryMaintainer/created_repository_maintainers_unpublished.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parse(reader);
		
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
		
		JsonArray actualJSON = (JsonArray) JsonParser.parse(data);
		
		System.out.println(actualJSON.toString());
		System.out.println(expectedJSON.toString());
		
		assertTrue(compareMaintainers(expectedJSON, actualJSON));
	}
	
	@Test
	public void shouldNonAdminUserNotBeAbleToDeleteRepositoryMaintainer() throws IOException, ParseException {
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + REPOSITORYMAINTAINER_ID_TO_DELETE + "/delete")
		.then()
			.statusCode(403);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Set> convertPackages(JsonArray rootJSON) throws ParseException {
		List<Set> JSON = new ArrayList<>();
		
		for(int i = 0; i < rootJSON.size(); i++) {
			JsonObject repositoryJSON = (JsonObject) rootJSON.get(i);
			JsonArray packagesJSON = (JsonArray) repositoryJSON.get("packages");
			Set JSONSet = new HashSet<>();
			for(int k = 0; k < packagesJSON.size(); k++) {
				JsonObject packageJSON = (JsonObject) packagesJSON.get(k);
				JSONSet.add(packageJSON);
			}
			JSON.add(JSONSet);
		}
		return JSON;
	}
	
	private boolean compareArrays(JsonArray expected, JsonArray actual) throws ParseException {
		
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
	
	private boolean compareMaintainers(JsonArray expected, JsonArray actual) throws ParseException {
		
		if (expected == null || actual == null)
			return false;
		
		if (expected.size() != actual.size())
			return false;
		
		for(int i = 0; i < expected.size(); i++) {
			JsonObject expectedRepository = (JsonObject) expected.get(i);
			JsonObject actualRepository = (JsonObject) actual.get(i);

			expectedRepository.getAsJsonObject("user").remove("role");
			expectedRepository.getAsJsonObject("repository").remove("packages");
			actualRepository.getAsJsonObject("user").remove("role");
			actualRepository.getAsJsonObject("repository").remove("packages");
						
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

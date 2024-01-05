/**
 * R Depot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.integrationtest.manager.v1;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.openanalytics.rdepot.integrationtest.IntegrationTest;
import io.restassured.http.ContentType;

public class RepositoryMaintainerIntegrationTest extends IntegrationTest {
	
	public RepositoryMaintainerIntegrationTest() {
		super("/api/manager/repositories/maintainers");
	}

	private final String REPOSITORYMAINTAINER_ID_TO_CREATE = "7";
	
	private final String THIRD_REPO_ID_UNPUBLISHED = "4";
	private final String SECOND_REPO_ID_PUBLISHED = "3";	
	
	private final String REPOSITORYMAINTAINER_ID_TO_DELETE = "1";
	private final String REPOSITORYMAINTAINER_ID_TO_EDIT = "1";
	private final String REPOSITORYMAINTAINER_ID_TO_RECREATE = "4";
	
	private static final String REPOSITORY_ID_TO_EDIT = "3";
		
	@SuppressWarnings({"rawtypes"})
	@Test
	public void shouldReturnUsersByGetCreateNewRepositoryMaintainer() throws IOException, ParseException {;		
		FileReader reader = new FileReader(JSON_PATH + "/repositoryMaintainer/get_create_repository_maintainers.json");
		JsonObject expectedJSON = (JsonObject) JsonParser.parseReader(reader);
		
		List<List> expectedPackages = convertPackages((JsonArray) expectedJSON.get("repositories"));
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/create")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonObject actualJSON = (JsonObject) JsonParser.parseString(data);
		
		List<List> actualPackages = convertPackages((JsonArray) actualJSON.get("repositories"));
		
		assertEquals(expectedPackages, actualPackages);
		assertTrue("repository: expected: " + expectedJSON.get("repositories") + " but was: "  + (JsonArray) actualJSON.get("repositories"),compareArrays((JsonArray) expectedJSON.get("repositories"), (JsonArray) actualJSON.get("repositories")));
		assertTrue("json: expected: " + expectedJSON + " but was: " + actualJSON,  compareListOfMaintainersFromGetMaintainers(expectedJSON, actualJSON));
	}
	
	@Test
	public void shouldNonAdminUserNotBeAbleToCreateNewRepositoryMaintainerWithGet() {
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/create")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldCreateNewRepositoryMaintainerForUnpublishedRepository() throws IOException, ParseException {
		Map<String, String> params = new HashMap<>();
		params.put("userId", REPOSITORYMAINTAINER_ID_TO_CREATE);
		
		params.put("repositoryId", THIRD_REPO_ID_UNPUBLISHED);
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/create")
		.then()
			.statusCode(200);
		
		FileReader reader = new FileReader(JSON_PATH + "/repositoryMaintainer/created_repository_maintainers_unpublished.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);

		assertTrue("expected: " + expectedJSON + " but was: " + actualJSON ,compareMaintainers(expectedJSON, actualJSON));
	}
	
	@Test
	public void shouldCreateNewRepositoryMaintainerForPublishedRepository() throws IOException, ParseException {
		Map<String, String> params = new HashMap<>();
		params.put("userId", REPOSITORYMAINTAINER_ID_TO_CREATE);
		params.put("repositoryId", SECOND_REPO_ID_PUBLISHED);
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/create")
		.then()
			.statusCode(200);
		
		FileReader reader = new FileReader(JSON_PATH + "/repositoryMaintainer/created_repository_maintainers_published.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);

		assertTrue("expected: " + expectedJSON + " but was: " + actualJSON, compareMaintainers(expectedJSON, actualJSON));
	}
	
	@Test
	public void nonAdminShouldNotBeAbleToCreateNewRepositoryMaintainerWithMethodPost() {
		Map<String, String> params = new HashMap<>();
		params.put("userId", REPOSITORYMAINTAINER_ID_TO_CREATE);

		params.put("repositoryId", THIRD_REPO_ID_UNPUBLISHED);
		
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
	
	@Test
	public void shouldEditRepositoryMaintainer() throws IOException, ParseException {
		Map<String, String> params = new HashMap<>();
		params.put("repositoryId", REPOSITORY_ID_TO_EDIT);
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/" + REPOSITORYMAINTAINER_ID_TO_EDIT + "/edit")
		.then()
			.statusCode(200);
		
		FileReader reader = new FileReader(JSON_PATH + "/repositoryMaintainer/edited_repository_maintainers.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		String data = given()
				.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
				.accept(ContentType.JSON)
			.when()
				.get(API_PATH + "/list")
			.then()
				.statusCode(200)
				.extract()
				.asString();
			
			JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);

			assertTrue("expected: " + expectedJSON + " but was: " + actualJSON, compareMaintainers(expectedJSON, actualJSON));
	}
	
	@Test
	public void nonAdminShouldNotBeAbleToEditRepositoryMaintainer() {
		Map<String, String> params = new HashMap<>();
		params.put("repositoryId", REPOSITORY_ID_TO_EDIT);
		
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
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
		FileReader reader = new FileReader(JSON_PATH + "/repositoryMaintainer/repository_maintainers.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);

		assertTrue("expected: " + expectedJSON + " but was: " + actualJSON, compareMaintainers(expectedJSON, actualJSON));
	}
	
	@Test
	public void nonAdminShouldNotBeAbleToReturnRepositoryMaintainers() {
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldReturnDeletedRepositoryMaintainers() throws IOException, ParseException {
		FileReader reader = new FileReader(JSON_PATH + "/repositoryMaintainer/deleted_repository_maintainers.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
			
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/deleted")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);

		assertTrue("expected: " + expectedJSON + " but was: " + actualJSON, compareMaintainers(expectedJSON, actualJSON));
	}
	
	@Test
	public void nonAdminShouldNotBeAbleToReturnDeletedRepositoryMaintainers() {
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/deleted")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldDeleteRepositoryMaintainer() throws IOException, ParseException {
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + REPOSITORYMAINTAINER_ID_TO_DELETE + "/delete")
		.then()
			.statusCode(200);
		
		FileReader reader = new FileReader(JSON_PATH + "/repositoryMaintainer/repository_maintainers_without_one.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);
		
		reader = new FileReader(JSON_PATH + "/repositoryMaintainer/deleted_repository_maintainers_with_new_one.json");
		JsonArray expectedDeleted = (JsonArray) JsonParser.parseReader(reader);
		
		String deleted = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/deleted")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualDeleted = (JsonArray) JsonParser.parseString(deleted);
	
		assertTrue("'Deleted' repository maintainer still exists in the list of repository maintainers, expected: " + expectedJSON + " but was: " + actualJSON, compareMaintainers(expectedJSON, actualJSON));
		assertTrue("'Deleted' repository maintainer haven't been added to the list of deleted ones, expected: " + expectedDeleted + " but was: " + actualDeleted, compareMaintainers(expectedDeleted, actualDeleted));
	}
	
	@Test
	public void nonAdminShouldNotBeAbleToDeleteRepositoryMaintainer() throws IOException, ParseException {
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + REPOSITORYMAINTAINER_ID_TO_DELETE + "/delete")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldAdminShiftDeleteRepositoryMaintainer() throws IOException, ParseException {
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + REPOSITORYMAINTAINER_ID_TO_DELETE + "/delete")
		.then()
			.statusCode(200);
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + REPOSITORYMAINTAINER_ID_TO_DELETE + "/sdelete")
		.then()
			.statusCode(200);
		
		FileReader reader = new FileReader(JSON_PATH + "/repositoryMaintainer/repository_maintainers_without_one_shift_deleted.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);
		
		reader = new FileReader(JSON_PATH + "/repositoryMaintainer/deleted_repository_maintainers_with_new_one.json");
		JsonArray expectedDeleted = (JsonArray) JsonParser.parseReader(reader);
		
		String deleted = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/deleted")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualDeleted = (JsonArray) JsonParser.parseString(deleted);
	
		assertTrue("'Shift deleted' repository maintainer still exists in the list of repository maintainers, expected: " + expectedJSON + " but was: " + actualJSON, compareMaintainers(expectedJSON, actualJSON));
		assertFalse("'Shift deleted' repository maintainer can't be added to the list of deleted ones, expected: " + expectedDeleted + " but was: " + actualDeleted, compareMaintainers(expectedDeleted, actualDeleted));
	}
	
	@Test
	public void shouldRecreateRepositoryMaintainer() throws FileNotFoundException, ParseException {
		Map<String, String> params = new HashMap<>();
		params.put("userId", REPOSITORYMAINTAINER_ID_TO_CREATE);
		params.put("repositoryId", SECOND_REPO_ID_PUBLISHED);
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/create")
		.then()
			.statusCode(200);
	
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + REPOSITORYMAINTAINER_ID_TO_RECREATE  +"/delete")
		.then()
			.statusCode(200);
		
	given()
	.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
	.contentType(ContentType.JSON)
	.accept(ContentType.JSON)
	.body(params)
.when()
	.post(API_PATH + "/create")
.then()
	.statusCode(200);
		
		FileReader reader = new FileReader(JSON_PATH + "/repositoryMaintainer/created_repository_maintainers_recreated.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);
				
		assertTrue("expected: " + expectedJSON + " but was: " + actualJSON, compareMaintainers(expectedJSON, actualJSON));
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
}

/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.openanalytics.rdepot.integrationtest.IntegrationTest;
import io.restassured.http.ContentType;

public class PackageMaintainerIntegrationTest extends IntegrationTest {	
	
	public PackageMaintainerIntegrationTest() {
		super("/api/manager/packages/maintainers");
	}

	private final String FIRST_REPO_ID = "2";
	private final String FOURTH_REPO_ID = "5";
		
	private final String USER_ID_TO_CREATE = "6";
	private final String PACKAGEMAINTAINER_ID_TO_DELETE = "2";
	private final String PACKAGEMAINTAINER_ID_TO_EDIT = "2";
	private final String PACKAGEMAINTAINER_ID_TO_RECREATE = "5";
	
	private final String PACKAGE_NAME_TO_CREATE = "usl";
	private final String PACKAGE_NAME_TO_EDIT = "abc";
		
	@Test
	public void shouldReturnPackageMaintainers() throws ParseException, IOException {		
		FileReader reader = new FileReader(JSON_PATH + "/packageMaintainer/list_package_maintainers_view_by_repo_maintainer.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		reader = new FileReader(JSON_PATH + "/packageMaintainer/list_package_maintainers_view_by_admin.json");
		JsonArray wrongJSON = (JsonArray) JsonParser.parseReader(reader);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);

		assertTrue("Differences in JSON view by Repository Maintainer", compareArrays(expectedJSON, actualJSON));
		assertFalse("Repository Maintainer can see JSON only allowed to Admin", compareArrays(wrongJSON, actualJSON));
	}
	
	@Test
	public void nonRepositoryMaintainerShouldNotBeAbleToReturnPackageMaintainers() {
		given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldReturnDeletedPackageMaintainers() throws ParseException, IOException {
		FileReader reader = new FileReader(JSON_PATH + "/packageMaintainer/deleted_package_maintainers.json");
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

		assertTrue("Differences in deleted package maintainers JSON", compareArrays(expectedJSON, actualJSON));
	}
	
	@Test
	public void nonRepositoryMaintainerShouldNotBeAbleToReturnDeletedPackageMaintainers() {
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/deleted")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldListPackageMaintainersWithGetCreate() throws IOException, ParseException {		
		FileReader reader = new FileReader(JSON_PATH + "/packageMaintainer/get_create_package_maintainers_view_by_repo_maintainer.json");
		JsonObject expectedJSON = (JsonObject) JsonParser.parseReader(reader);
		
		reader = new FileReader(JSON_PATH + "/packageMaintainer/get_create_package_maintainers_view_by_admin.json");
		JsonObject wrongJSON = (JsonObject) JsonParser.parseReader(reader);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/create")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonObject actualJSON = (JsonObject) JsonParser.parseString(data);
		
		assertTrue("There are some differencess in the package maintainer list viewed by repository maintianer", compareListOfMaintainersFromGetMaintainers(expectedJSON, actualJSON));
		assertFalse("Probably the package maintainer list is viewed by admin", compareListOfMaintainersFromGetMaintainers(wrongJSON, actualJSON));
	}
	
	@Test
	public void nonRepositoryMaintainerShouldNotBeAbleToListPackageMaintainersWithGetCreate() {
		given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
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
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/create")
		.then()
			.statusCode(200);
		
		FileReader reader = new FileReader(JSON_PATH + "/packageMaintainer/list_created_package_maintainer_view_by_admin.json");
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

		assertTrue("New package maintainer hasn't been created", compareArrays(expectedJSON, actualJSON));
	}
	
	@Test
	public void nonRepositoryMaintainerShouldNotBeAbleToCreateNewPackageMaintainer() {
		Map<String, String> params = new HashMap<>();
		params.put("userId", USER_ID_TO_CREATE);
		params.put("repositoryId", FIRST_REPO_ID);
		params.put("packageName", PACKAGE_NAME_TO_CREATE);
		
		given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
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
		params.put("repositoryId", FOURTH_REPO_ID);
		params.put("packageName", PACKAGE_NAME_TO_EDIT);
		
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/" + PACKAGEMAINTAINER_ID_TO_EDIT + "/edit")
		.then()
			.statusCode(200);
		
		FileReader reader = new FileReader(JSON_PATH + "/packageMaintainer/edited_package_maintainers_view_by_admin.json");
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

		assertTrue("Package maintainer hasn't been changed", compareArrays(expectedJSON, actualJSON));
	}
	
	@Test
	public void nonRepositoryMaintainerShouldNotBeAbleToEditPackageMaintainer() {
		Map<String, String> params = new HashMap<>();
		params.put("repositoryId", FOURTH_REPO_ID);
		params.put("packageName", PACKAGE_NAME_TO_EDIT);
		
		given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
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
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + PACKAGEMAINTAINER_ID_TO_DELETE + "/delete")
		.then()
			.statusCode(200);
		
		FileReader reader = new FileReader(JSON_PATH + "/packageMaintainer/list_package_maintainers_without_one.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);
		
		reader = new FileReader(JSON_PATH + "/packageMaintainer/deleted_package_maintainers_with_new_one.json");
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

		assertTrue("'Deleted' package maintainer still exists in the list of package maintainers", compareArrays(expectedJSON, actualJSON));
		assertTrue("'Deleted' package maintainer hasn't been added to the list of deleted ones", compareArrays(expectedDeleted, actualDeleted));
	}
	
	@Test
	public void shouldAdminShiftDeletePackageMaintainer() throws IOException, ParseException {		
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + PACKAGEMAINTAINER_ID_TO_DELETE + "/delete")
		.then()
			.statusCode(200);
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + PACKAGEMAINTAINER_ID_TO_DELETE + "/sdelete")
		.then()
			.statusCode(200);
		
		FileReader reader = new FileReader(JSON_PATH + "/packageMaintainer/list_package_maintainers_without_one.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);
		
		reader = new FileReader(JSON_PATH + "/packageMaintainer/deleted_package_maintainers.json");
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

		assertTrue("'Shift deleted' package maintainer still exists in the list of package maintainers", compareArrays(expectedJSON, actualJSON));
		assertTrue("'Shift deleted' package maintainer can't be added to the list of deleted ones", compareArrays(expectedDeleted, actualDeleted));
	}
	
	@Test
	public void nonRepositoryMaintainerShouldNotBeAbleToDeletePackageMaintainer() {
		given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
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
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/create")
		.then()
			.statusCode(200);
		
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + PACKAGEMAINTAINER_ID_TO_RECREATE + "/delete")
		.then()
			.statusCode(200);
		
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()	
			.post(API_PATH + "/create")
		.then()
			.statusCode(200);
		
		FileReader reader = new FileReader(JSON_PATH + "/packageMaintainer/package_maintainers_recreated.json");
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
		
		reader = new FileReader(JSON_PATH + "/packageMaintainer/deleted_package_maintainers_recreated.json");
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

		assertTrue("'Reacreated' package maintainer hasn't been added to the list of package maintainers", compareArrays(expectedJSON, actualJSON));
		assertTrue("'Reacreated' package maintainer still exists in the list of deleted ones", compareArrays(expectedDeleted, actualDeleted));
	}
		
	private boolean compareArrays(JsonArray expectedJSON, JsonArray actualJSON) throws ParseException {
		
		if (expectedJSON == null || actualJSON == null)
			return false;
				
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
}

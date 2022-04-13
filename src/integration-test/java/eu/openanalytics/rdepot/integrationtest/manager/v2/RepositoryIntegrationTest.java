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
package eu.openanalytics.rdepot.integrationtest.manager.v2;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import eu.openanalytics.rdepot.integrationtest.IntegrationTest;
import io.restassured.http.ContentType;

public class RepositoryIntegrationTest extends IntegrationTest {	
	public RepositoryIntegrationTest() {
		super("/api/v2/manager/r/repositories");
	}

	private final String REPO_NAME_TO_CREATE = "testrepo7";
	private final String REPO_NAME_TO_DUPLICATE = "testrepo1";
	private final String REPO_NAME_TO_EDIT = "newName";
	
	private final String REPO_ID_TO_PUBLISH = "5";
	private final String REPO_ID_TO_UNPUBLISH = "2";
	private final String REPO_ID_TO_DELETE = "2";
	private final String REPO_ID_TO_SHIFT_DELETE = "6";
	private final String REPO_ID_TO_EDIT = "2";
	private final String REPO_ID_TO_READ = "2";
	
	@Test
	public void getAllRepositories() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/repository/list_of_repositories.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH)
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("There are some differences in repositories which user sees", expectedJSON, actualJSON);
	}	
	
	@Test
	public void getAllRepositories_returns401_whenUserIsNotAuthenticated() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/401.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()			
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH)
		.then()
			.statusCode(401)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("Incorrect error json", expectedJSON, actualJSON);
	}
	
	@Test
	public void getDeletedRepositories() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/repository/deleted_repositories.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "?deleted=true")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("There are some differences in repositories which user sees", expectedJSON, actualJSON);
	}
	
	@Test
	public void getDeletedRepositories_returns403_whenUserIsNotAuthorized() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/403.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "?deleted=true")
		.then()
			.statusCode(403)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("Incorrect error json", expectedJSON, actualJSON);
	}
	
	@Test
	public void getRepository() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/repository/repository.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/" + REPO_ID_TO_READ)
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("There are some differences in repository which user sees", expectedJSON, actualJSON);
	}
	
	@Test
	public void getRepository_returns401_whenUserIsNotAuthenticated() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/401.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()			
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/" + REPO_ID_TO_READ)
		.then()
			.statusCode(401)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("Incorrect error json", expectedJSON, actualJSON);
	}
	
	@Test
	public void getRepository_returns404_whenRepositoryIsNotFound() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/repository/404.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/123")
		.then()
			.statusCode(404)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("Incorrect error json", expectedJSON, actualJSON);	
	}
	
	@Test
	public void createRepository() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		FileReader reader = new FileReader(JSON_PATH + "/v2/repository/created_repository.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		Map<String, String> params = new HashMap<>();
		params.put("name", REPO_NAME_TO_CREATE);
		params.put("publicationUri", "http://localhost/repo/" + REPO_NAME_TO_CREATE);
		params.put("serverAddress", "http://oa-rdepot-repo:8080/" + REPO_NAME_TO_CREATE);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH)
		.then()
			.statusCode(201)
			.extract()
			.asString();						
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		assertEquals("Repository has not been created", expectedJSON, actualJSON);
		
		reader = new FileReader(JSON_PATH + "/v2/repository/list_of_repositories_with_created_one.json");
		expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		data = given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH)
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		actualJSON = (JSONObject) jsonParser.parse(data);
		assertEquals("There are some differences in repositories which user sees", expectedJSON, actualJSON);
	}
	
	@Test
	public void createRepository_returns401_whenUserIsNotAuthenticated() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/401.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		Map<String, String> params = new HashMap<>();
		params.put("name", REPO_NAME_TO_CREATE);
		params.put("publicationUri", "http://localhost/repo/" + REPO_NAME_TO_CREATE);
		params.put("serverAddress", "http://oa-rdepot-repo:8080/" + REPO_NAME_TO_CREATE);
		
		String data = given()			
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH)
		.then()
			.statusCode(401)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("Incorrect error json", expectedJSON, actualJSON);
	}
	
	@Test
	public void createRepository_returns403_whenUserIsNotAuthorized() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/403.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		Map<String, String> params = new HashMap<>();
		params.put("name", REPO_NAME_TO_CREATE);
		params.put("publicationUri", "http://localhost/repo/" + REPO_NAME_TO_CREATE);
		params.put("serverAddress", "http://oa-rdepot-repo:8080/" + REPO_NAME_TO_CREATE);
		
		String data = given()			
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH)
		.then()
			.statusCode(403)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("Incorrect error json", expectedJSON, actualJSON);
	}
	
	@Test
	public void createRepository_returns422_whenRepositoryValidationFails() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/repository/422_create.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		Map<String, String> params = new HashMap<>();
		params.put("name", REPO_NAME_TO_DUPLICATE);
		params.put("publicationUri", "http://localhost/repo/" + REPO_NAME_TO_CREATE);
		params.put("serverAddress", "http://oa-rdepot-repo:8080/" + REPO_NAME_TO_CREATE);
		
		String data = given()			
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH)
		.then()
			.statusCode(422)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("Incorrect error json", expectedJSON, actualJSON);
	}
	
	@Test
	public void deleteRepository() {
		given()			
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
		.when()
			.delete(API_PATH + "/" + REPO_ID_TO_SHIFT_DELETE)
		.then()
			.statusCode(204);
	}
	
	@Test
	public void deleteRepository_returns401_whenUserIsNotAuthenticated() {
		given()			
		.when()
			.delete(API_PATH + "/" + REPO_ID_TO_SHIFT_DELETE)
		.then()
			.statusCode(401);
	}
	
	@Test
	public void deleteRepository_returns403_whenUserIsNotAuthorized() {
		given()			
		.when()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.delete(API_PATH + "/" + REPO_ID_TO_SHIFT_DELETE)
		.then()
			.statusCode(403);
	}
	
	@Test
	public void deleteRepository_returns404_whenRepositoryIsNotFound() {
		given()			
		.when()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
			.delete(API_PATH + "/123")
		.then()
			.statusCode(404);
	}
	
	@Test
	public void patchRepository_delete() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/repository/deleted_repository.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		List<Map<String, Object>> params = new ArrayList<>();
		
		Map<String, Object> delete = new HashMap<>();
		delete.put("op", "replace");
		delete.put("path", "/deleted");
		delete.put("value", true);
		
		params.add(delete);
		
		String data = given()			
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.contentType("application/json-patch+json")
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.patch(API_PATH + "/" + REPO_ID_TO_DELETE)
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("Repository has not been marked as deleted", expectedJSON, actualJSON);
	}
	
	@Test
	public void patchRepository_publish() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/repository/published_repository.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		List<Map<String, Object>> params = new ArrayList<>();
		
		Map<String, Object> arg1 = new HashMap<>();
		arg1.put("op", "replace");
		arg1.put("path", "/published");
		arg1.put("value", true);
		
		params.add(arg1);

		String data = given()			
				.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
				.contentType("application/json-patch+json")
				.accept(ContentType.JSON)
				.body(params)
		.when()
			.patch(API_PATH + "/" + REPO_ID_TO_PUBLISH)
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("Repository has not been published", expectedJSON, actualJSON);
	}
	
	@Test
	public void patchRepository_unpublish() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/repository/unpublished_repository.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		List<Map<String, Object>> params = new ArrayList<>();
		
		Map<String, Object> arg1 = new HashMap<>();
		arg1.put("op", "replace");
		arg1.put("path", "/published");
		arg1.put("value", false);
		
		params.add(arg1);
		
		String data = given()			
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.contentType("application/json-patch+json; charset=UTF-8")
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.patch(API_PATH + "/" + REPO_ID_TO_UNPUBLISH)
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("Repository has not been unpublished", expectedJSON, actualJSON);
	}
	
	@Test
	public void patchRepository_update() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/repository/edited_repository.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		List<Map<String, Object>> params = new ArrayList<>();
		
		Map<String, Object> arg1 = new HashMap<>();
		arg1.put("op", "replace");
		arg1.put("path", "/name");
		arg1.put("value", REPO_NAME_TO_EDIT);
		params.add(arg1);
		
		Map<String, Object> arg2 = new HashMap<>();
		arg2.put("op", "replace");
		arg2.put("path", "/serverAddress");
		arg2.put("value", "http://oa-rdepot-repo:8080/" + REPO_NAME_TO_EDIT);
		params.add(arg2);
		
		String data = given()			
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.contentType("application/json-patch+json")
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.patch(API_PATH + "/" + REPO_ID_TO_EDIT)
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("Repository has not been edited", expectedJSON, actualJSON);
	}
}

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

public class UserIntegrationTest extends IntegrationTest {
	
	public UserIntegrationTest() {
		super("/api/v2/manager/users");
	}

	@Test
	public void getAllUsers() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/user/users.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH)
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("There are some differences in users which admin sees", expectedJSON, actualJSON);
	}

	@Test
	public void getAllUsers_Returns403_whenUserIsNotAuthorized() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/403.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH)
		.then()
			.statusCode(403)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("User should not be authorized to perform this operation", expectedJSON, actualJSON);
	}
	
	@Test
	public void getAllUsers_Returns401_whenUserIsNotAuthenticated() throws IOException, ParseException {
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
		assertEquals("Unauthenticated user should not be able to perform this operation", expectedJSON, actualJSON);
	}
	
	@Test
	public void getOneUserAsAdmin() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/user/one_user.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/7")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("There are some differences in user which admin sees", expectedJSON, actualJSON);
	}
	
	@Test
	public void getOneUserAsThisUser() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/user/one_user.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/7")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("There are some differences when user sees itself", expectedJSON, actualJSON);
	}
	
	@Test
	public void getOneUser_Returns403_whenUserIsNotAuthorized() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/403.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/7")
		.then()
			.statusCode(403)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("Non admin should not be authorized to perform this operation", expectedJSON, actualJSON);
	}
	
	@Test
	public void getOneUser_Returns401_whenUserIsNotAuthenticated() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/401.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/7")
		.then()
			.statusCode(401)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("Unauthenticated user should not be able to perform this operation", expectedJSON, actualJSON);
	}
		
	@Test
	public void getUsersTokenAsAdmin() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/user/user_token.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/7/token")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("There are some differences in token which admin sees", expectedJSON, actualJSON);
	}
	
	@Test
	public void getUsersTokenAsThisUser() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/user/user_token.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/7/token")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("There are some differences when user sees its token", expectedJSON, actualJSON);
	}
	
	@Test
	public void getUsersToken_Returns403_whenUserIsNotAuthorized() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/403.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/7/token")
		.then()
			.statusCode(403)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("User should not be authorized to perform this operation", expectedJSON, actualJSON);
	}
	
	@Test
	public void getUsersToken_Returns401_whenUserIsNotAuthenticated() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/401.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/7/token")
		.then()
			.statusCode(401)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("Unauthenticated user should not be able to perform this operation", expectedJSON, actualJSON);
	}
	
	@Test
	public void getRoles() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/user/roles.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/roles")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("There are some differences in roles which admin sees", expectedJSON, actualJSON);
	}

	@Test
	public void getRoles_Returns403_whenUserIsNotAuthorized() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/403.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/roles")
		.then()
			.statusCode(403)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("Non admin should not be authorized to perform this operation", expectedJSON, actualJSON);
	}
	
	@Test
	public void getRoles_Returns401_whenUserIsNotAuthenticated() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/401.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/roles")
		.then()
			.statusCode(401)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("Unauthenticated user should not be able to perform this operation", expectedJSON, actualJSON);
	}
	
	@Test
	public void patchUser_activate() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/user/activated_user.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		List<Map<String, Object>> body = new ArrayList<>();
		Map<String, Object> first = new HashMap<>();
		first.put("op", "replace");
		first.put("path", "/active");
		first.put("value", true);
		
		body.add(first);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.contentType("application/json-patch+json")
			.accept(ContentType.JSON)
			.body(body)
		.when()
			.patch(API_PATH + "/9")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("User has not been activated", expectedJSON, actualJSON);
	}
	
	@Test
	public void patchUser_deactivate() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/user/deactivated_user.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		List<Map<String, Object>> body = new ArrayList<>();
		Map<String, Object> first = new HashMap<>();
		first.put("op", "replace");
		first.put("path", "/active");
		first.put("value", false);
		
		body.add(first);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.contentType("application/json-patch+json")
			.accept(ContentType.JSON)
			.body(body)
		.when()
			.patch(API_PATH + "/7")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("User has not been deactivated", expectedJSON, actualJSON);
	}
	
	@Test
	public void patchUser_changeRole() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/user/updated_role_user.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		List<Map<String, Object>> body = new ArrayList<>();
		Map<String, Object> first = new HashMap<>();
		first.put("op", "replace");
		first.put("path", "/roleId");
		first.put("value", 2);
		
		body.add(first);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.contentType("application/json-patch+json")
			.accept(ContentType.JSON)
			.body(body)
		.when()
			.patch(API_PATH + "/7")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("User has not been deactivated", expectedJSON, actualJSON);
	}
	
	@Test
	public void patchUser_shouldNotUpdateLastLoggedInOn() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/user/unchanged_user.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		

		List<Map<String, String>> body = new ArrayList<>();
		
		Map<String, String> first = new HashMap<>();
		first.put("op", "replace");
		first.put("path", "/lastLoggedInOn");
		first.put("value", "2017-12-03T00:00:00");
		
		body.add(first);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.contentType("application/json-patch+json")
			.accept(ContentType.JSON)
			.body(body)
		.when()
			.patch(API_PATH + "/9")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("User cannot be updated", expectedJSON, actualJSON);
	}
	
	@Test
	public void patchUser_multipleOperationOnTheSameField() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/user/activated_user.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		List<Map<String, Object>> body = new ArrayList<>();
		Map<String, Object> first = new HashMap<>();
		first.put("op", "replace");
		first.put("path", "/active");
		first.put("value", true);
		Map<String, Object> second = new HashMap<>();
		second.put("op", "replace");
		second.put("path", "/active");
		second.put("value", false);
		Map<String, Object> third = new HashMap<>();
		third.put("op", "replace");
		third.put("path", "/active");
		third.put("value", true);
		
		body.add(first);
		body.add(second);
		body.add(third);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.contentType("application/json-patch+json")
			.accept(ContentType.JSON)
			.body(body)
		.when()
			.patch(API_PATH + "/9")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("User has not been activated", expectedJSON, actualJSON);
	}
	
	@Test
	public void patchUser_Returns403_whenUserIsNotAuthorized() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/403.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		List<Map<String, Object>> body = new ArrayList<>();
		Map<String, Object> first = new HashMap<>();
		first.put("op", "replace");
		first.put("path", "/roleId");
		first.put("value", 2);
		
		body.add(first);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.contentType("application/json-patch+json")
			.accept(ContentType.JSON)
			.body(body)
		.when()
			.get(API_PATH + "/roles")
		.then()
			.statusCode(403)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("Non admin should not be authorized to perform this operation", expectedJSON, actualJSON);
	}
	
	@Test
	public void patchUser_Returns401_whenUserIsNotAuthenticated() throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/401.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		List<Map<String, Object>> body = new ArrayList<>();
		Map<String, Object> first = new HashMap<>();
		first.put("op", "replace");
		first.put("path", "/roleId");
		first.put("value", 2);
		
		body.add(first);
		
		String data = given()
			.contentType("application/json-patch+json")
			.accept(ContentType.JSON)
			.body(body)
		.when()
			.get(API_PATH + "/roles")
		.then()
			.statusCode(401)
			.extract()
			.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);		
		assertEquals("Unauthenticated user should not be able to perform this operation", expectedJSON, actualJSON);
	}
}

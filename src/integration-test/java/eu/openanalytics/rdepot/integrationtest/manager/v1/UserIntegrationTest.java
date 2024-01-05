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
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import eu.openanalytics.rdepot.integrationtest.IntegrationTest;
import io.restassured.http.ContentType;

public class UserIntegrationTest extends IntegrationTest {	
	
	public UserIntegrationTest() {
		super("/api/manager/users");
	}

	private final String USER_TO_ACTIVATE_LOGIN = "admin";
	private final String USER_TO_DEACTIVATE_LOGIN = "galieleo";

	private final String USER_TO_ACTIVATE_ID = "8";
	private final String USER_TO_DEACTIVATE_ID = "6";
	private final String REPOSITORYMAINTAINER_ID = "5";	
	
	
	//missing serializers for date
	@Test
	public void shouldReturnUsers() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/user/users.json");
		JSONArray rootJSON = (JSONArray) jsonParser.parse(reader);
		Set<JSONObject> expectedJSON = convertToSet(rootJSON);
		
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

		Set<JSONObject> actualJSON = convertToSet(rootJSON);

		assertEquals("Users haven't been returned", expectedJSON, actualJSON);
	}
	
	@Test
	public void nonAdminShouldNotBeAbleToGetUserList() {
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(403);
	}
		
	@Test
	public void nonAdminShouldNotBeAbleToGetUserRoles() {
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/roles")
		.then()
			.statusCode(403);
	}
		
	
	@Test
	public void shouldReturnUserInfo() {			
		given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/newton")
		.then()
			.statusCode(200)
			.body("user.name", equalTo("Isaac Newton"))
			.body("user.role.name", equalTo("user"))
			.body("user.role.description", equalTo("User"))
			.body("user.email", equalTo("newton@ldap.forumsys.com"))
			.body("user.login", equalTo("newton"));
	}
	
	@Test
	public void shouldNotReturnUserInfo() {			
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/einstein")
		.then()
			.statusCode(401)
			.body("error", equalTo("You are not authorized to perform this operation."));
	}
	
	@Test
	public void shouldChangeRoleFromRepositoryMaintainerToUser() {
		Map<String, String> params = new HashMap<>();
		params.put("name", "Nikola Tesla");
		params.put("email", "tesla@ldap.forumsys.com");
		params.put("login", "tesla");
		params.put("role", "User");
		params.put("active", "true");
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
			.formParams(params)
		.when()
			.post(API_PATH + "/" + REPOSITORYMAINTAINER_ID + "/edit")
		.then()
			.statusCode(200);
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/tesla")
		.then()
			.statusCode(200)
			.body("user.role.description", equalTo("User"))
			.body("user.login", equalTo("tesla"));
	}
	
	@Test
	public void nonAdminShouldNotBeAbleToChangeRole() {
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.post(API_PATH + "/"  + REPOSITORYMAINTAINER_ID + "/edit")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldDeactivateUser() {
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/" + USER_TO_DEACTIVATE_ID + "/deactivate")
		.then()
			.statusCode(200)
			.body("success", equalTo("User has been deactivated successfully."));
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/" + USER_TO_DEACTIVATE_LOGIN)
		.then()
			.statusCode(200)
			.body("user.active", equalTo(false))
			.body("user.login", equalTo(USER_TO_DEACTIVATE_LOGIN));
	}
	
	@Test
	public void nonAdminShouldNotBeAbleToDeactivateUser() {
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/"+ REPOSITORYMAINTAINER_ID + "/deactivate")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldActivateUser() {
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/" + USER_TO_ACTIVATE_ID + "/activate")
		.then()
			.statusCode(200)
			.body("success", equalTo("User has been activated successfully."));
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/" + USER_TO_ACTIVATE_LOGIN)
		.then()
			.statusCode(200)
			.body("user.active", equalTo(true))
			.body("user.login", equalTo(USER_TO_ACTIVATE_LOGIN));
	}
	
	@Test
	public void nonAdminShouldNotBeAbleToActivateUser() {
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/"+ REPOSITORYMAINTAINER_ID + "/activate")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldAdminGetTokenOfAnyUser() {
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
			.when()
				.get(API_PATH + "/"+ REPOSITORYMAINTAINER_ID + "/token")
			.then()
				.statusCode(200)
				.body("token", equalTo(REPOSITORYMAINTAINER_TOKEN));
	}
	
	@Test
	public void shouldRepositoryMaintainerGetHisToken() {
		given()
			.header(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
			.when()
				.get(API_PATH + "/"+ REPOSITORYMAINTAINER_ID + "/token")
			.then()
				.statusCode(200)
				.body("token", equalTo(REPOSITORYMAINTAINER_TOKEN));
	}
	
	@Test
	public void nonAdminShouldNotGetTokenOfDifferentUser() {
		given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
			.when()
				.get(API_PATH + "/"+ REPOSITORYMAINTAINER_ID + "/token")
			.then()
				.statusCode(401)
				.body("error", equalTo("You are not authorized to perform this operation."));
	}
	
	@Test
	public void nonActiveUserShouldNotBeAbleToPerformOperation() {
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/" + USER_TO_DEACTIVATE_ID + "/deactivate")
		.then()
			.statusCode(200)
			.body("success", equalTo("User has been deactivated successfully."));
		
		given()
			.header(AUTHORIZATION, BEARER + PACKAGEMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/galieleo")
		.then()
			.statusCode(401);
		
	}
	
	protected Set<JSONObject> convertToSet(JSONArray rootJSON) throws ParseException {
		Set<JSONObject> JSON = new HashSet<>();
		
		for(int i = 0; i < rootJSON.size(); i++) {
			JSONObject objJSON = (JSONObject) rootJSON.get(i);
			JSON.add(objJSON);
		}
		
		return JSON;
	}

}

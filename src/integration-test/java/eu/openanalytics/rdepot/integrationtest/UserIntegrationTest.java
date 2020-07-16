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
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import java.io.File;
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
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

public class UserIntegrationTest {
	private static final String JSON_PATH = "src/integration-test/resources/JSONs";
	
	private static final String ADMIN_LOGIN = "einstein";
	private static final String REPOSITORYMAINTAINER_LOGIN = "tesla";
	private static final String USER_LOGIN = "newton";
	private static final String USER_TO_ACTIVATE_LOGIN = "admin";
	private static final String USER_TO_DEACTIVATE_LOGIN = "galieleo";
	private static final String PASSWORD = "testpassword";
	private static final String API_PATH = "/api/manager/users";
	private static final String USER_TO_ACTIVATE_ID = "8";
	private static final String USER_TO_DEACTIVATE_ID = "6";
	private static final String REPOSITORYMAINTAINER_ID = "5";
	
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
	
	@Test
	public void shouldReturnUsers() throws ParseException, IOException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/user/users.json");
		JSONArray rootJSON = (JSONArray) jsonParser.parse(reader);
		Set<JSONObject> expectedJSON = convert(rootJSON);
		
		String data = given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH+ "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		rootJSON = (JSONArray) jsonParser.parse(data);

		Set<JSONObject> actualJSON = convert(rootJSON);

		assertEquals("Users haven't been returned", expectedJSON, actualJSON);
	}
	
	@Test
	public void shouldNonAdminUserNotBeAbleToGetUserList() {
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(403);
	}
	
	
	//TODO: this test doesn't pass
	/*Script1.groovy: 1: expecting ''', found '<EOF>' @ line 1, column 54.
	 *ct.Roles haven't been returned
     *                             ^
	 */
//	@Test
//	public void shouldReturnRoles() {
//		JsonPath expectedJson = new JsonPath(new File(JSON_PATH + "/roles.json"));
//		
//		given()
//			.auth().basic(ADMIN_LOGIN, PASSWORD)
//			.accept(ContentType.JSON)
//		.when()
//			.get("/roles")
//		.then()
//			.statusCode(200)
//			.body("Roles haven't been returned", equalTo(expectedJson.getList("")));
//	}
	
	@Test
	public void shouldNonAdminUserNotBeAbleToGetUserRoles() {
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/roles")
		.then()
			.statusCode(403);
	}
		
	
	@Test
	public void shouldReturnUserInfo() {			
		given()
			.auth()
			.basic(USER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/" + USER_LOGIN)
		.then()
			.statusCode(200)
			.body("user.name", equalTo("Isaac Newton"))
			.body("user.role.name", equalTo("user"))
			.body("user.role.description", equalTo("User"))
			.body("user.email", equalTo("newton@ldap.forumsys.com"))
			.body("user.login", equalTo(USER_LOGIN));
	}
	
	@Test
	public void shouldNotReturnUserInfo() {			
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/" + ADMIN_LOGIN)
		.then()
			.statusCode(200)
			.body("error", equalTo("You are not authorized to perform this operation"));
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
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
			.formParams(params)
		.when()
			.post(API_PATH + "/" + REPOSITORYMAINTAINER_ID + "/edit")
		.then()
			.statusCode(200);
		
		given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/" + REPOSITORYMAINTAINER_LOGIN)
		.then()
			.statusCode(200)
			.body("user.role.description", equalTo("User"))
			.body("user.login", equalTo(REPOSITORYMAINTAINER_LOGIN));
	}
	
	@Test
	public void shouldNonAdminUserNotBeAbleToChangeRole() {
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.put(API_PATH + "/"  + REPOSITORYMAINTAINER_ID + "/edit")
		.then()
			.statusCode(405);
	}
	
	@Test
	public void shouldDeactivateUser() {
		given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.put(API_PATH + "/" + USER_TO_DEACTIVATE_ID + "/deactivate")
		.then()
			.statusCode(200)
			.body("success", equalTo("user.deactivated"));
		
		given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/" + USER_TO_DEACTIVATE_LOGIN)
		.then()
			.statusCode(200)
			.body("user.active", equalTo(false))
			.body("user.login", equalTo(USER_TO_DEACTIVATE_LOGIN));
	}
	
	@Test
	public void shouldNonAdminUserNotBeAbleToDeactivateUser() {
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.put(API_PATH + "/"+ REPOSITORYMAINTAINER_ID + "/deactivate")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldActivateUser() {
		given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.put(API_PATH + "/" + USER_TO_ACTIVATE_ID + "/activate")
		.then()
			.statusCode(200)
			.body("success", equalTo("user.activated"));
		
		given()
			.auth().basic(ADMIN_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/" + USER_TO_ACTIVATE_LOGIN)
		.then()
			.statusCode(200)
			.body("user.active", equalTo(true))
			.body("user.login", equalTo(USER_TO_ACTIVATE_LOGIN));
	}
	
	@Test
	public void shouldNonAdminUserNotBeAbleToActivateUser() {
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.put(API_PATH + "/"+ REPOSITORYMAINTAINER_ID + "/activate")
		.then()
			.statusCode(403);
	}
	
	private Set<JSONObject> convert(JSONArray rootJSON) throws ParseException {
		Set<JSONObject> JSON = new HashSet<>();
		
		for(int i = 0; i < rootJSON.size(); i++) {
			JSONObject objJSON = (JSONObject) rootJSON.get(i);
			objJSON.remove("lastLoggedInOn");
			JSON.add(objJSON);
		}
		
		return JSON;
	}
}

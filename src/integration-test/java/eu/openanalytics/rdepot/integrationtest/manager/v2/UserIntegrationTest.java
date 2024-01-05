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
package eu.openanalytics.rdepot.integrationtest.manager.v2;

import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;

public class UserIntegrationTest extends IntegrationTest {
	
	private final int GET_ENDPOINT_NEW_EVENTS_AMOUNT = 0;
	private final int PATCH_ENDPOINT_NEW_EVENTS_AMOUNT = 1;
	private final int FORBIDDEN_PATCH_NEW_EVENTS_AMOUNT = 0;
	
	public UserIntegrationTest() {
		super("/api/v2/manager/users");
	}

	@BeforeClass
	public static final void configureRestAssured() throws IOException, InterruptedException {
		RestAssured.port = 8017;
		RestAssured.urlEncodingEnabled = false;
	}
	
	@Test
	public void getAllUsers() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/user/users.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}

	@Test
	public void getAllUsers_Returns403_whenUserIsNotAuthorized() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/403.json", 
				"?sort=id,asc", 403, REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllUsers_Returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET_UNAUTHENTICATED, 
				"?sort=id,asc", GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getOneUserAsAdmin() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/user/one_user.json", 
				"/7", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getOneUserAsThisUser() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/user/one_user.json", 
				"/7", 200, USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getOneUser_Returns403_whenUserIsNotAuthorized() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/403.json", 
				"/7", 403, REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);				
	}
	
	@Test
	public void getOneUser_Returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET_UNAUTHENTICATED, 
				"/7", GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getOneUser_Returns401_whenRequesterAccountIsInactive() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/active\","
				+ "\"value\":false"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH,  "/v2/user/deactivated_user.json", "/7", 200, 
				ADMIN_TOKEN, PATCH_ENDPOINT_NEW_EVENTS_AMOUNT, "/v2/events/users/deactivate_user_event.json", patch);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/401.json", "/7", 401, USER_TOKEN, 0);
		testEndpoint(requestBody);
	}
		
	@Test
	public void getUsersTokenAsAdmin() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/user/user_token.json", 
				"/7/token", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getUsersTokenAsThisUser() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/user/user_token.json", 
				"/7/token", 200, USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getUsersToken_Returns403_whenUserIsNotAuthorized() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/403.json", 
				"/7/token", 403, REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getUsersToken_Returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET_UNAUTHENTICATED, 
				"/7/token", GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getRoles() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/user/roles.json", 
				"/roles", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}

	@Test
	public void getRoles_Returns403_whenUserIsNotAuthorized() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/403.json", 
				"/roles", 403, REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getRoles_Returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET_UNAUTHENTICATED, 
				"/roles", GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void patchUser_activate() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/active\","
				+ "\"value\":true"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH,  "/v2/user/activated_user.json", "/9", 200, 
				ADMIN_TOKEN, PATCH_ENDPOINT_NEW_EVENTS_AMOUNT, "/v2/events/users/activate_user_event.json", patch);
		testEndpoint(requestBody);
	}
	
	@Test
	public void patchUser_deactivate() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/active\","
				+ "\"value\":false"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH,  "/v2/user/deactivated_user.json", "/7", 200, 
				ADMIN_TOKEN, PATCH_ENDPOINT_NEW_EVENTS_AMOUNT, "/v2/events/users/deactivate_user_event.json", patch);
		testEndpoint(requestBody);
	}
	
	@Test
	public void patchUser_changeRole() throws Exception, ParseException {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/roleId\","
				+ "\"value\":2"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH,  "/v2/user/updated_role_user.json", "/7", 200, 
				ADMIN_TOKEN, PATCH_ENDPOINT_NEW_EVENTS_AMOUNT, "/v2/events/users/change_role_user_event.json", patch);
		testEndpoint(requestBody);
	}
	
	@Test
	public void patchUser_returns422_whenValidationFails() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/lastLoggedInOn\","
				+ "\"value\":\"2017-12-03\""
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH,  "/v2/user/user_validation_error.json", "/9", 422, 
				ADMIN_TOKEN, FORBIDDEN_PATCH_NEW_EVENTS_AMOUNT, patch);
		testEndpoint(requestBody);
	}
	
	@Test
	public void patchUser_multipleOperationOnTheSameField() throws Exception {				
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\": \"/active\","
				+ "\"value\": true"
				+ "},"
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\": \"/active\","
				+ "\"value\": false"
				+ "},"
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\": \"/active\","
				+ "\"value\": true"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH,  "/v2/user/activated_user.json", "/9", 200, 
				ADMIN_TOKEN, PATCH_ENDPOINT_NEW_EVENTS_AMOUNT, "/v2/events/users/activate_user_event.json", patch);
		testEndpoint(requestBody);
	}
	
	@Test
	public void patchUser_Returns403_whenUserIsNotAuthorized() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/roleId\","
				+ "\"value\":2"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH,  "/v2/403.json", "/9", 403, 
				REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, patch);
		testEndpoint(requestBody);		
	}
	
	@Test
	public void patchUser_Returns401_whenUserIsNotAuthenticated() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/roleId\","
				+ "\"value\":2"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH_UNAUTHENTICATED,  "/v2/403.json", "/9", 401, 
				REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, patch);
		testEndpoint(requestBody);
	}		
}

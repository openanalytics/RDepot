/*
 * RDepot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.integrationtest.manager.v2.base;

import java.util.Arrays;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;

import eu.openanalytics.rdepot.integrationtest.manager.v2.IntegrationTest;
import eu.openanalytics.rdepot.integrationtest.manager.v2.RequestType;
import eu.openanalytics.rdepot.integrationtest.manager.v2.TestRequestBody;
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.UserTestData;

public class UserIntegrationTest extends IntegrationTest {
	
	private final UserTestData testData;
	
	public UserIntegrationTest() {
		super("/api/v2/manager/users");
		this.testData = UserTestData.builder()
				.getEndpointNewEventsAmount(0)
				.changeEndpointNewEventsAmount(1)
				.active(false)
				.roles(Arrays.asList("admin", "packagemaintainer", "repositorymaintainer", "user"))
				.search("in@")
				.build();
	}

	@Test
	public void getAllUsers() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("?sort=id,asc")
				.statusCode(200)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/user/users.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllUsersWithTrailingSlash() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("/?sort=id,asc")
				.statusCode(200)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/user/users_with_trailing_slash.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getUsersByNameAndLoginAndEmailSearching() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("?search=" + testData.getSearch() + "&sort=id,asc")
				.statusCode(200)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/user/users_searching.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getUsersByRoleAndActive() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("?active=" + testData.isActive() + "&role=" + testData.getRoles().get(0) + "&sort=id,asc")
				.statusCode(200)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/user/users_by_role_and_active.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getNonActiveUsersWithSearching() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("?active=" + testData.isActive() + "&search=" + testData.getSearch() + "&sort=id,asc")
				.statusCode(200)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/user/non_active_users_with_seraching.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllUsers_Returns403_whenUserIsNotAuthorized() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET_UNAUTHORIZED)
				.urlSuffix("?sort=id,asc")
				.statusCode(403)
				.token(REPOSITORYMAINTAINER_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/403.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllUsers_Returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET_UNAUTHENTICATED)
				.urlSuffix("?sort=id,asc")
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getOneUserAsAdmin() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("/7")
				.statusCode(200)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/user/one_user.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getOneUserAsThisUser() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("/7")
				.statusCode(200)
				.token(USER_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/user/one_user_as_this_user.json")
				.build();
		testEndpoint(requestBody);
	}

	@Test
	public void getOneUserAsThisUserViaMeEndpoint() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("/me")
				.statusCode(200)
				.token(USER_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/user/one_user_me.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getOneUser_Returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET_UNAUTHENTICATED)
				.urlSuffix("/7")
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.build();
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
		
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.PATCH)
				.urlSuffix("/7")
				.statusCode(200)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/user/deactivated_user.json")
				.body(patch)
				.build();				
		testEndpoint(requestBody);
		
		requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("/7")
				.statusCode(401)
				.token(USER_TOKEN)
				.howManyNewEventsShouldBeCreated(0)
				.expectedJsonPath("/v2/401.json")
				.build();				
		testEndpoint(requestBody);
	}
	
	@Test
	public void getOneUser_Returns403_whenUserIsNotAuthorized() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET_UNAUTHORIZED)
				.urlSuffix("/7")
				.statusCode(403)
				.token(REPOSITORYMAINTAINER_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/403.json")
				.build();
		testEndpoint(requestBody);				
	}		
	
	@Test
	public void getRoles() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("/roles")
				.statusCode(200)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/user/roles.json")
				.build();
		testEndpoint(requestBody);
	}

	@Test
	public void getRoles_Returns403_whenUserIsNotAuthorized() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("/roles")
				.statusCode(403)
				.token(REPOSITORYMAINTAINER_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/403.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getRoles_Returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET_UNAUTHENTICATED)
				.urlSuffix("/roles")
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.build();
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

		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.PATCH)
				.urlSuffix("/9")
				.statusCode(200)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/user/activated_user.json")
				.expectedEventsJson("/v2/base/events/users/activate_user_event.json")
				.body(patch)
				.build();
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
		
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.PATCH)
				.urlSuffix("/7")
				.statusCode(200)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/user/deactivated_user.json")
				.expectedEventsJson("/v2/base/events/users/deactivate_user_event.json")
				.body(patch)
				.build();
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
		
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.PATCH)
				.urlSuffix("/7")
				.statusCode(200)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/user/updated_role_user.json")
				.expectedEventsJson("/v2/base/events/users/change_role_user_event.json")
				.body(patch)
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void patchUser_changeRoleOfUserWithSettings() throws Exception, ParseException {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/roleId\","
				+ "\"value\":4"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.PATCH)
				.urlSuffix("/5")
				.statusCode(200)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/user/updated_role_user_with_settings.json")
				.expectedEventsJson("/v2/base/events/users/change_role_user_with_settings_event.json")
				.body(patch)
				.build();
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
		
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.PATCH)
				.urlSuffix("/9")
				.statusCode(422)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/user/user_validation_error.json")
				.body(patch)
				.build();
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
		
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.PATCH)
				.urlSuffix("/9")
				.statusCode(200)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/user/activated_user.json")
				.expectedEventsJson("/v2/base/events/users/activate_user_event.json")
				.body(patch)
				.build();
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
		
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.PATCH_UNAUTHORIZED)
				.urlSuffix("/9")
				.statusCode(403)
				.token(REPOSITORYMAINTAINER_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/403.json")
				.body(patch)
				.build();
		testEndpoint(requestBody);		
	}

	@Test
	public void patchUser_Returns403_whenTriesToDeactivateThemselves() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/active\","
				+ "\"value\":false"
				+ "}"
				+ "]";

		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.PATCH_UNAUTHORIZED)
				.urlSuffix("/4")
				.statusCode(403)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/403.json")
				.body(patch)
				.build();
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
		
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.PATCH_UNAUTHENTICATED)
				.urlSuffix("/9")
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.body(patch)
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void createUserFromToken() throws Exception {		
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET_UNAUTHORIZED)
				.urlSuffix("?sort=id,asc")
				.statusCode(403)
				.token(NEW_USER_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/403.json")
				.build();
		testEndpoint(requestBody);
		
		requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("?sort=id,asc")
				.statusCode(200)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/user/users_after_creation_of_new_user.json")
				.build();
		testEndpoint(requestBody);		
	}
}

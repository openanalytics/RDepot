/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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

import org.junit.Test;

public class PackageMaintainerIntegrationTest extends IntegrationTest {
	
	public PackageMaintainerIntegrationTest() {
		super("/api/v2/manager/r/package-maintainers");
	}

	private final int GET_ENDPOINT_NEW_EVENTS_AMOUNT = 0;
	private final String PACKAGE_MAINTAINER_ONLY_FOR_ADMIN_ID = "3";
	private final String PACKAGE_MAINTAINER_FOR_REPOSITORY_MAINTAINER_ID = "1";
	
	@Test
	public void getAllMaintainers_asAdmin() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/package-maintainer/maintainers_asadmin.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);		
	}
	
	@Test
	public void getAllMaintainers_asRepositoryMaintainer() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/package-maintainer/maintainers_asrepositorymaintainer.json", 
				"?sort=id,asc", 200, REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);		
	}
	
	@Test
	public void getAllMaintainers_returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET_UNAUTHENTICATED, 
				"?sort=id,asc", GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	//TODO: error
	@Test
	public void getAllMaintainers_sortedByPackageName() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/package-maintainer/maintainers_sorted.json", 
				"?sort=packageName,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllDeletedMaintainers() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/package-maintainer/maintainers_deleted.json", 
				"?sort=id,asc&deleted=true", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllNonDeletedMaintainers() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/package-maintainer/maintainers_nondeleted.json", 
				"?sort=id,asc&deleted=false", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getMaintainer() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/package-maintainer/maintainer_onlyadmin.json", 
				"/" + PACKAGE_MAINTAINER_ONLY_FOR_ADMIN_ID, 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);		
	}
	
	@Test
	public void getMaintainer_returns403_whenUserIsNotAllowedToSee() throws Exception {
		testGetEndpoint("/v2/403.json", 
				"/" + PACKAGE_MAINTAINER_ONLY_FOR_ADMIN_ID, 403, REPOSITORYMAINTAINER_TOKEN);
	}
	
	@Test
	public void getMaintainer_whenUserIsAllowed() throws Exception {
		testGetEndpoint("/v2/package-maintainer/maintainer.json", 
				"/" + PACKAGE_MAINTAINER_FOR_REPOSITORY_MAINTAINER_ID, 200, REPOSITORYMAINTAINER_TOKEN);
	}
	
	@Test
	public void getMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET_UNAUTHENTICATED, 
				"/1", GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getMaintainer_returns404_whenMaintainerIsNotFound() throws Exception {
		testGetEndpoint("/v2/package-maintainer/maintainer_notfound.json", "/22222", 404, ADMIN_TOKEN);
	}
	
	@Test
	public void shiftDeleteMaintainer() throws Exception {
		testDeleteEndpoint("/4", 204, ADMIN_TOKEN);
		testGetEndpoint("/v2/package-maintainer/maintainer_notfound.json", "/4", 404, ADMIN_TOKEN);
	}
	
	@Test
	public void shiftDeleteMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.DELETE_UNAUTHENTICATED, 
				"/4", GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package-maintainer/maintainers_asadmin.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void shiftDeleteMaintainer_returns403_whenUserIsNotAdmin() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.DELETE_UNAUTHORIZED, 
				"/4", REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package-maintainer/maintainers_asadmin.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void shiftDeleteMaintainer_returns404_whenMaintainerDoesNotExist() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.DELETE, "/v2/403.json", 
				"/4444", 404, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package-maintainer/maintainers_asadmin.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void shiftDeleteMaintainer_returns404_whenMaintainerIsNotSetDeleted() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.DELETE, "/v2/403.json", 
				"/1", 404, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package-maintainer/maintainers_asadmin.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void createMaintainer() throws Exception {
		final String body = "{"				
				+ "\"userId\": 6,"
				+ "\"packageName\": \"A3\","
				+ "\"repositoryId\": 2"
				+ "}";
		
		testPostEndpoint(body, "/v2/package-maintainer/maintainer_created.json", 201, ADMIN_TOKEN);
		testGetEndpoint("/v2/package-maintainer/maintainers_after_creation.json", "?sort=id,asc", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void createMaintainer_whenUserIsRepositoryMaintainerInTheSameRepository() throws Exception {
		final String body = "{"				
				+ "\"userId\": 6,"
				+ "\"packageName\": \"A3\","
				+ "\"repositoryId\": 2"
				+ "}";
		
		testPostEndpoint(body, "/v2/package-maintainer/maintainer_created_by_repository_maintainer.json", 201, REPOSITORYMAINTAINER_TOKEN);
		testGetEndpoint("/v2/package-maintainer/maintainers_after_creation_by_repository_maintainer.json", "?sort=id,asc", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void createMaintainer_returns403_whenRepositoryMaintainerIsNotInTheSameRepository() throws Exception {
		final String body = "{"
				+ "\"userId\": 6,"
				+ "\"packageName\": \"A3\","
				+ "\"repositoryId\": 4"
				+ "}";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.POST_UNAUTHORIZED, "", 
				REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, body);
		testEndpoint(requestBody);
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package-maintainer/maintainers_asadmin.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void createMaintainer_returns401_whenUserIsUnauthenticated() throws Exception {
		final String body = "{"
				+ "\"userId\": 6,"
				+ "\"packageName\": \"A3\","
				+ "\"repositoryId\": 2"
				+ "}";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.POST_UNAUTHENTICATED, "",
				GET_ENDPOINT_NEW_EVENTS_AMOUNT, body);
		testEndpoint(requestBody);
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package-maintainer/maintainers_asadmin.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void createMaintainer_returns403_whenUserIsNotAuthorized() throws Exception {
		final String body = "{"
				+ "\"userId\": 6,"
				+ "\"packageName\": \"A3\","
				+ "\"repositoryId\": 2"
				+ "}";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.POST_UNAUTHORIZED, "", 
				PACKAGEMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, body);
		testEndpoint(requestBody);
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package-maintainer/maintainers_asadmin.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void createMaintainer_returns422_whenValidationFails() throws Exception{
		final String body = "{"
				+ "\"userId\": 6,"
				+ "\"packageName\": \"A3\","
				+ "\"repositoryId\": 2222"
				+ "}";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.POST, "/v2/package-maintainer/maintainer_validation_error.json", "", 422, 
				ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, body);
		testEndpoint(requestBody);
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package-maintainer/maintainers_asadmin.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void patchMaintainer() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\": \"/packageName\","
				+ "\"value\": \"abc\""
				+ "}"
				+ "]";
		
		testPatchEndpoint(patch, "/v2/package-maintainer/maintainer_patched.json", "/1", 200, ADMIN_TOKEN);
		testGetEndpoint("/v2/package-maintainer/maintainer_after_patch.json", "/1", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void patchMaintainer_returns401_whenUserIsUnauthenticated() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\": \"/packageName\","
				+ "\"value\": \"abc\""
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH_UNAUTHENTICATED, "/1",
				GET_ENDPOINT_NEW_EVENTS_AMOUNT, patch);
		testEndpoint(requestBody);
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package-maintainer/maintainers_asadmin.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void patchMaintainer_returns403_whenUserIsNotAllowed() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\": \"/packageName\","
				+ "\"value\": \"abc\""
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.POST_UNAUTHENTICATED, "/1", 
				USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, patch);
		testEndpoint(requestBody);
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package-maintainer/maintainers_asadmin.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void patchMaintainer_returns422_whenPatchIsMalformed() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/dsdsadsadsa\","
				+ "\"value\":false"
				+ "}"
				+ "]";
		testPatchEndpoint(patch, "/v2/malformed_patch.json", "/1", 422, ADMIN_TOKEN);
		testGetEndpoint("/v2/package-maintainer/maintainers_asadmin.json", "?sort=id,asc", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void patchMaintainer_returns404_whenMaintainerIsNotFound() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\": \"/packageName\","
				+ "\"value\": \"abc\""
				+ "}"
				+ "]";
						
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH, "/v2/package-maintainer/maintainer_notfound.json", "/11111", 404, 
				REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, patch);
		testEndpoint(requestBody);
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package-maintainer/maintainers_asadmin.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);				
	}
	
	@Test
	public void patchMaintainer_returns422_whenValidationFails() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\": \"/repositoryId\","
				+ "\"value\": \"222222\""
				+ "}"
				+ "]";
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH, "/v2/package-maintainer/maintainer_validation_error.json", "/1", 422, 
				REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, patch);
		testEndpoint(requestBody);
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package-maintainer/maintainers_asadmin.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);	
	}
}

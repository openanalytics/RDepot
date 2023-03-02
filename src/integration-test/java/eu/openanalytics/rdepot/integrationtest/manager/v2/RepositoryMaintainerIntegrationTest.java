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

public class RepositoryMaintainerIntegrationTest extends IntegrationTest {
	
	private final int GET_ENDPOINT_NEW_EVENTS_AMOUNT = 0;
	private final int GET_ENDPOINT_DELETE_NEW_EVENTS_AMOUNT = -2;
	private final int GET_ENDPOINT_UPDATE_NEW_EVENTS_AMOUNT = 1;
	
	public RepositoryMaintainerIntegrationTest() {
		super("/api/v2/manager/r/repository-maintainers");
	}

	@Test
	public void getAllMaintainers() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/maintainer_list.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllMaintainers_asRepositoryMaintainer() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET_UNAUTHORIZED,  
				"?sort=id,asc", REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllMaintainers_returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET_UNAUTHENTICATED,  
				"?sort=id,asc", GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllDeletedMaintainers() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/deleted_list.json",
				"?deleted=true", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllNonDeleted_sortedByRepositoryId() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/nondeleted_sorted_list.json",
				"?deleted=false&sort=repositoryId,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getNonDeletedMaintainer() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/maintainer_nondeleted.json",
				"/3", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getDeletedMaintainer() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/maintainer_deleted.json",
				"/2", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getMaintainer_returns403_whenRequesterIsNotAdmin() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET_UNAUTHORIZED,
				"/3", REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getMaintainer_returns401_whenUnauthenticated() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET_UNAUTHENTICATED,
				"/3", GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getMaintainer_returns404_whenMaintainerIsNotFound() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/maintainer_notfound.json",
				"/2222", 404, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void deleteMaintainer_returns404_whenMaintainerIsNotFound() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.DELETE, "/v2/repository-maintainer/maintainer_notfound.json",
				"/2222", 404, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/maintainer_list.json",
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void deleteMaintainer_returns404_whenMaintainerIsNotSetAsDeleted() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.DELETE, "/v2/repository-maintainer/maintainer_notfound.json",
				"/3", 404, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/maintainer_list.json",
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void deleteMaintainer_returns403_whenMaintainerIsNotAdmin() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.DELETE_UNAUTHORIZED, 
				"/2", 404, REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/maintainer_list.json",
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void deleteMaintainer() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.DELETE, 
				"/2", 204, ADMIN_TOKEN, GET_ENDPOINT_DELETE_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/maintainer_notfound.json",
				"/2", 404, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void patchMaintainer() throws Exception {
		final String patch = "[{\n"
				+ "    \"op\": \"replace\",\n"
				+ "    \"path\": \"/repositoryId\",\n"
				+ "    \"value\": \"3\"\n"
				+ "}]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH,  "/v2/repository-maintainer/patched_maintainer.json",
				"/3", 200, ADMIN_TOKEN, GET_ENDPOINT_UPDATE_NEW_EVENTS_AMOUNT, 
				"/v2/events/repositorymaintainers/patched_repository_maintainer.json", patch);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/maintainer_after_patch.json",
				"/3", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void patchMaintainer_returns404_whenMaintainerIsNotFound() throws Exception {
		final String patch = "[{\n"
				+ "    \"op\": \"replace\",\n"
				+ "    \"path\": \"/repositoryId\",\n"
				+ "    \"value\": \"3\"\n"
				+ "}]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH,  "/v2/repository-maintainer/maintainer_notfound.json",
				"/33333", 404, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, 
				"/v2/events/repositorymaintainers/patched_repository_maintainer.json", patch);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/maintainer_list.json",
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void patchMaintainer_returns403_whenUserIsRepositoryMaintainer() throws Exception {
		final String patch = "[{\n"
				+ "    \"op\": \"replace\",\n"
				+ "    \"path\": \"/repositoryId\",\n"
				+ "    \"value\": \"3\"\n"
				+ "}]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH_UNAUTHORIZED,
				"/3", REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT,  patch);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/maintainer_list.json",
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
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH,  "/v2/repository-maintainer/malformed_patch.json",
				"/3", 422, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, patch);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/maintainer_list.json",
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void patchMaintainer_returns422_whenValidationFails() throws Exception {
		final String patch = "[{\n"
				+ "    \"op\": \"replace\",\n"
				+ "    \"path\": \"/repositoryId\",\n"
				+ "    \"value\": \"3333\"\n"
				+ "}]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH,  "/v2/repository-maintainer/maintainer_validation_error.json",
				"/3", 422, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, patch);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/maintainer_list.json",
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void patchMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
		final String patch = "[{\n"
				+ "    \"op\": \"replace\",\n"
				+ "    \"path\": \"/repositoryId\",\n"
				+ "    \"value\": \"3\"\n"
				+ "}]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH_UNAUTHENTICATED,
				"/3", GET_ENDPOINT_NEW_EVENTS_AMOUNT,  patch);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/maintainer_list.json",
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}

	@Test
	public void createMaintainer() throws Exception {
		final String body = "{\n"
				+ "    \"userId\": 5,\n"
				+ "    \"repositoryId\": 3\n"
				+ "}";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.POST, "/v2/repository-maintainer/maintainer_created.json",
				"/", 201, ADMIN_TOKEN, GET_ENDPOINT_UPDATE_NEW_EVENTS_AMOUNT, 
				"/v2/events/repositorymaintainers/created_repository_maintainer.json", body);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/maintainers_after_creation.json",
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void createMaintainer_returns403_whenUserIsRepositoryMaintainer() throws Exception {
		final String body = "{\n"
				+ "    \"userId\": 5,\n"
				+ "    \"repositoryId\": 3\n"
				+ "}";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.POST_UNAUTHORIZED, "", 
				REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT,  body);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/maintainer_list.json",
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void createMaintianer_returns401_whenUserIsNotAuthenticated() throws Exception {
		final String body = "{\n"
				+ "    \"userId\": 5,\n"
				+ "    \"repositoryId\": 3\n"
				+ "}";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.POST_UNAUTHENTICATED, "", 
				GET_ENDPOINT_NEW_EVENTS_AMOUNT,  body);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/maintainer_list.json",
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void createMaintainer_returns422_whenValidationFails() throws Exception {
		final String body = "{\n"
				+ "    \"userId\": 5,\n"
				+ "    \"repositoryId\": 3333\n"
				+ "}";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.POST, "/v2/repository-maintainer/maintainer_validation_error.json",
				"/", 422, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, body);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/repository-maintainer/maintainer_list.json",
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
}

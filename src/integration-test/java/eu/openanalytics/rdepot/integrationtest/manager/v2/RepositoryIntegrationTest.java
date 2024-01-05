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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;

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
	
	private final int GET_ENDPOINT_NEW_EVENTS_AMOUNT = 0;
	private final int GET_DELETED_REPOSITORY_NEW_EVENTS_AMOUNT = -35;
	private final int GET_CHANGED_REPOSITORY_NEW_EVENTS_AMOUNT = 1;
	
	@BeforeClass
	public static final void configureRestAssured() throws IOException, InterruptedException {
		RestAssured.port = 8017;
		RestAssured.urlEncodingEnabled = false;
	}
	
	@Test
	public void getAllRepositories() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/repository/list_of_repositories.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);	
	}	
	
	@Test
	public void getAllRepositories_returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET_UNAUTHENTICATED, 
				"",GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);	
	}
	
	@Test
	public void getDeletedRepositories() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/repository/deleted_repositories.json",
				"?deleted=true", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);	
	}
	
	@Test
	public void getDeletedRepositories_returns403_whenUserIsNotAuthorized() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET_UNAUTHORIZED,"?deleted=true", REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);	
	}
	
	@Test
	public void getRepository() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/repository/repository.json",
				"/" + REPO_ID_TO_READ, 200, USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);	
	}
	
	@Test
	public void getRepository_returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET_UNAUTHENTICATED, 
				"/" + REPO_ID_TO_READ, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);	
	}
	
	@Test
	public void getRepository_returns404_whenRepositoryIsNotFound() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/repository/404.json",
				"/123", 404, USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);	
	}
	
	@Test
	public void createRepository() throws Exception {		
		final String body = 
				 "{"
				+ "\"name\": \"" + REPO_NAME_TO_CREATE + "\","
				+ "\"publicationUri\":\"http://localhost/repo/" + REPO_NAME_TO_CREATE + "\","
				+ "\"serverAddress\":\"http://oa-rdepot-repo:8080/" + REPO_NAME_TO_CREATE + "\""
				+ "}";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.POST, "/v2/repository/created_repository.json",
				"/", 201, ADMIN_TOKEN, GET_CHANGED_REPOSITORY_NEW_EVENTS_AMOUNT, 
				"/v2/events/repositories/created_repository_event.json", body);
		testEndpoint(requestBody);	

		requestBody = new TestRequestBody(RequestType.GET, "/v2/repository/list_of_repositories_with_created_one.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);	
	}
	
	@Test
	public void createRepository_returns401_whenUserIsNotAuthenticated() throws Exception {
		final String body = 
				 "{"
				+ "\"name\": \"" + REPO_NAME_TO_CREATE + "\","
				+ "\"publicationUri\":\"http://localhost/repo/" + REPO_NAME_TO_CREATE + "\","
				+ "\"serverAddress\":\"http://oa-rdepot-repo:8080/" + REPO_NAME_TO_CREATE + "\""
				+ "}";
		TestRequestBody requestBody = new TestRequestBody(RequestType.POST_UNAUTHENTICATED, "", 
				GET_ENDPOINT_NEW_EVENTS_AMOUNT, body); 
		testEndpoint(requestBody);	
	}
	
	@Test
	public void createRepository_returns403_whenUserIsNotAuthorized() throws Exception {
		final String body = 
				 "{"
				+ "\"name\": \"" + REPO_NAME_TO_CREATE + "\","
				+ "\"publicationUri\":\"http://localhost/repo/" + REPO_NAME_TO_CREATE + "\","
				+ "\"serverAddress\":\"http://oa-rdepot-repo:8080/" + REPO_NAME_TO_CREATE + "\""
				+ "}";
		TestRequestBody requestBody = new TestRequestBody(RequestType.POST_UNAUTHORIZED, "", USER_TOKEN, 
				GET_ENDPOINT_NEW_EVENTS_AMOUNT, body); 
		testEndpoint(requestBody);	
	}
	
	@Test
	public void createRepository_returns422_whenRepositoryValidationFails() throws Exception {
		final String body = 
				 "{"
				+ "\"name\": \"" + REPO_NAME_TO_DUPLICATE + "\","
				+ "\"publicationUri\":\"http://localhost/repo/" + REPO_NAME_TO_CREATE + "\","
				+ "\"serverAddress\":\"http://oa-rdepot-repo:8080/" + REPO_NAME_TO_CREATE + "\""
				+ "}";
		TestRequestBody requestBody = new TestRequestBody(RequestType.POST, "/v2/repository/422_create.json",
				"/", 422, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, body);
		testEndpoint(requestBody);	
	}
	
	@Test
	public void deleteRepository() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.DELETE,
				"/" + REPO_ID_TO_SHIFT_DELETE, 204, ADMIN_TOKEN, GET_DELETED_REPOSITORY_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);	
	}
	
	@Test
	public void deleteRepository_returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.DELETE_UNAUTHENTICATED,
				"/" + REPO_ID_TO_SHIFT_DELETE, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void deleteRepository_returns403_whenUserIsNotAuthorized() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.DELETE_UNAUTHORIZED,
				"/" + REPO_ID_TO_SHIFT_DELETE, REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void deleteRepository_returns404_whenRepositoryIsNotFound() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/repository/404.json",
				"/123", 404, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);	
	}
	
	@Test
	public void patchRepository_delete() throws Exception {	
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/deleted\","
				+ "\"value\":true"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH, "/v2/repository/deleted_repository.json",
				"/" + REPO_ID_TO_DELETE, 200, REPOSITORYMAINTAINER_TOKEN, GET_CHANGED_REPOSITORY_NEW_EVENTS_AMOUNT,
				"/v2/events/repositories/patched_deleted_repository_event.json", patch);
		testEndpoint(requestBody);	
	}
	
	@Test
	public void patchRepository_updateVerion_shouldFail() throws Exception {
		
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/version\","
				+ "\"value\":100"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH, "/v2/repository/forbidden_update.json",
				"/" + REPO_ID_TO_DELETE, 422, REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, patch);
				testEndpoint(requestBody);	
	}
	
	@Test
	public void patchRepository_publish() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/published\","
				+ "\"value\":true"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH, "/v2/repository/published_repository.json",
				"/" + REPO_ID_TO_PUBLISH, 200, REPOSITORYMAINTAINER_TOKEN, GET_CHANGED_REPOSITORY_NEW_EVENTS_AMOUNT,
				"/v2/events/repositories/patched_published_repository_event.json", patch);
		testEndpoint(requestBody);	
	}
	
	@Test
	public void patchRepository_publish_keepsGeneratedContent_whenSnapshotsAreTurnedOn() throws Exception {
		patchRepository_publish();
		final int cmdExitValue = runCommand("gradle", "checkIfSnapshotWasCreated", "-b", "src/integration-test/resources/build.gradle");
		assertEquals("Snapshots were not created or got deleted.", 0, cmdExitValue);
	}
	
	@Test
	public void patchRepository_unpublish() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/published\","
				+ "\"value\":false"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH, "/v2/repository/unpublished_repository.json",
				"/" + REPO_ID_TO_UNPUBLISH, 200, REPOSITORYMAINTAINER_TOKEN, GET_CHANGED_REPOSITORY_NEW_EVENTS_AMOUNT,
				"/v2/events/repositories/patched_unpublished_repository_event.json", patch);
		testEndpoint(requestBody);	
	}
	
	@Test
	public void patchRepository_update() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/name\","
				+ "\"value\": \"" + REPO_NAME_TO_EDIT + "\""
				+ "},"
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/serverAddress\","
				+ "\"value\": \"http://oa-rdepot-repo:8080/" + REPO_NAME_TO_EDIT + "\""
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH, "/v2/repository/edited_repository.json",
				"/" + REPO_ID_TO_EDIT, 200, REPOSITORYMAINTAINER_TOKEN, GET_CHANGED_REPOSITORY_NEW_EVENTS_AMOUNT,
				"/v2/events/repositories/patched_updated_repository_event.json", patch);
		testEndpoint(requestBody);
	}
}

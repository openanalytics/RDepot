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

import org.junit.Test;

import eu.openanalytics.rdepot.integrationtest.IntegrationTest;

public class RepositoryMaintainerIntegrationTest extends IntegrationTest {
	
	public RepositoryMaintainerIntegrationTest() {
		super("/api/v2/manager/r/repository-maintainers");
	}

	@Test
	public void getAllMaintainers() throws Exception {
		testGetEndpoint("/v2/repository-maintainer/maintainer_list.json", "?sort=id,asc", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void getAllMaintainers_asRepositoryMaintainer() throws Exception {
		testGetEndpoint("/v2/403.json", "?sort=id,asc", 403, REPOSITORYMAINTAINER_TOKEN);
	}
	
	@Test
	public void getAllMaintainers_returns401_whenUserIsNotAuthenticated() throws Exception {
		testGetEndpoint("/v2/401.json", "?sort=id,asc", 401, "");
	}
	
	@Test
	public void getAllDeletedMaintainers() throws Exception {
		testGetEndpoint("/v2/repository-maintainer/deleted_list.json", "?deleted=true", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void getAllNonDeleted_sortedByRepositoryId() throws Exception {
		testGetEndpoint("/v2/repository-maintainer/nondeleted_sorted_list.json", "?deleted=false&sort=repositoryId,asc", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void getNonDeletedMaintainer() throws Exception {
		testGetEndpoint("/v2/repository-maintainer/maintainer_nondeleted.json", "/3", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void getDeletedMaintainer() throws Exception {
		testGetEndpoint("/v2/repository-maintainer/maintainer_deleted.json", "/2", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void getMaintainer_returns403_whenRequesterIsNotAdmin() throws Exception {
		testGetEndpoint("/v2/403.json", "/3", 403, REPOSITORYMAINTAINER_TOKEN);
	}
	
	@Test
	public void getMaintainer_returns401_whenUnauthenticated() throws Exception {
		testGetEndpointUnauthenticated("/3");
	}
	
	@Test
	public void getMaintainer_returns404_whenMaintainerIsNotFound() throws Exception {
		testGetEndpoint("/v2/repository-maintainer/maintainer_notfound.json", "/2222", 404, ADMIN_TOKEN);
	}
	
	@Test
	public void deleteMaintainer_returns404_whenMaintainerIsNotFound() throws Exception {
		testDeleteEndpoint("/2222", 404, ADMIN_TOKEN);
		testGetEndpoint("/v2/repository-maintainer/maintainer_list.json", "?sort=id,asc", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void deleteMaintainer_returns404_whenMaintainerIsNotSetAsDeleted() throws Exception {
		testDeleteEndpoint("/3", 404, ADMIN_TOKEN);
		testGetEndpoint("/v2/repository-maintainer/maintainer_list.json", "?sort=id,asc", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void deleteMaintainer_returns403_whenMaintainerIsNotAdmin() throws Exception {
		testDeleteEndpoint("/2", 403, REPOSITORYMAINTAINER_TOKEN);
		testGetEndpoint("/v2/repository-maintainer/maintainer_list.json", "?sort=id,asc", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void deleteMaintainer() throws Exception {
		testDeleteEndpoint("/2", 204, ADMIN_TOKEN);
		testGetEndpoint("/v2/repository-maintainer/maintainer_notfound.json", "/2", 404, ADMIN_TOKEN);
	}
	
	@Test
	public void patchMaintainer() throws Exception {
		final String patch = "[{\n"
				+ "    \"op\": \"replace\",\n"
				+ "    \"path\": \"/repositoryId\",\n"
				+ "    \"value\": \"3\"\n"
				+ "}]";
		
		testPatchEndpoint(patch, "/v2/repository-maintainer/patched_maintainer.json", "/3", 200, ADMIN_TOKEN);
		testGetEndpoint("/v2/repository-maintainer/maintainer_after_patch.json", "/3", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void patchMaintainer_returns404_whenMaintainerIsNotFound() throws Exception {
		final String patch = "[{\n"
				+ "    \"op\": \"replace\",\n"
				+ "    \"path\": \"/repositoryId\",\n"
				+ "    \"value\": \"3\"\n"
				+ "}]";
		
		testPatchEndpoint(patch, "/v2/repository-maintainer/maintainer_notfound.json", "/33333", 404, ADMIN_TOKEN);		
		testGetEndpoint("/v2/repository-maintainer/maintainer_list.json", "?sort=id,asc", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void patchMaintainer_returns403_whenUserIsRepositoryMaintainer() throws Exception {
		final String patch = "[{\n"
				+ "    \"op\": \"replace\",\n"
				+ "    \"path\": \"/repositoryId\",\n"
				+ "    \"value\": \"3\"\n"
				+ "}]";
		
		testPatchEndpoint(patch, "/v2/403.json", "/3", 403, REPOSITORYMAINTAINER_TOKEN);
		testGetEndpoint("/v2/repository-maintainer/maintainer_list.json", "?sort=id,asc", 200, ADMIN_TOKEN);
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
		
		testPatchEndpoint(patch, "/v2/malformed_patch.json", "/3", 422, ADMIN_TOKEN);
		testGetEndpoint("/v2/repository-maintainer/maintainer_list.json", "?sort=id,asc", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void patchMaintainer_returns422_whenValidationFails() throws Exception {
		final String patch = "[{\n"
				+ "    \"op\": \"replace\",\n"
				+ "    \"path\": \"/repositoryId\",\n"
				+ "    \"value\": \"3333\"\n"
				+ "}]";
		
		testPatchEndpoint(patch, "/v2/repository-maintainer/maintainer_validation_error.json", "/3", 422, ADMIN_TOKEN);
		testGetEndpoint("/v2/repository-maintainer/maintainer_list.json", "?sort=id,asc", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void patchMaintainer_returns401_whenUserIsNotAuthenticated() throws Exception {
		final String patch = "[{\n"
				+ "    \"op\": \"replace\",\n"
				+ "    \"path\": \"/repositoryId\",\n"
				+ "    \"value\": \"3\"\n"
				+ "}]";
		
		testPatchEndpointUnauthenticated(patch, "/3");
		testGetEndpoint("/v2/repository-maintainer/maintainer_list.json", "?sort=id,asc", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void createMaintainer() throws Exception {
		final String body = "{\n"
				+ "    \"userId\": 5,\n"
				+ "    \"repositoryId\": 3\n"
				+ "}";
		
		testPostEndpoint(body, "/v2/repository-maintainer/maintainer_created.json", 201, ADMIN_TOKEN);
		testGetEndpoint("/v2/repository-maintainer/maintainers_after_creation.json", "?sort=id,asc", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void createMaintainer_returns403_whenUserIsRepositoryMaintainer() throws Exception {
		final String body = "{\n"
				+ "    \"userId\": 5,\n"
				+ "    \"repositoryId\": 3\n"
				+ "}";
		
		testPostEndpoint(body, "/v2/403.json", 403, REPOSITORYMAINTAINER_TOKEN);
		testGetEndpoint("/v2/repository-maintainer/maintainer_list.json", "?sort=id,asc", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void createMaintianer_returns401_whenUserIsNotAuthenticated() throws Exception {
		final String body = "{\n"
				+ "    \"userId\": 5,\n"
				+ "    \"repositoryId\": 3\n"
				+ "}";
		
		testPostEndpoint_asUnauthenticated(body, "/v2/401.json", 401);
		testGetEndpoint("/v2/repository-maintainer/maintainer_list.json", "?sort=id,asc", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void createMaintainer_returns422_whenValidationFails() throws Exception {
		final String body = "{\n"
				+ "    \"userId\": 5,\n"
				+ "    \"repositoryId\": 3333\n"
				+ "}";
		
		testPostEndpoint(body, "/v2/repository-maintainer/maintainer_validation_error.json", 422, ADMIN_TOKEN);
		testGetEndpoint("/v2/repository-maintainer/maintainer_list.json", "?sort=id,asc", 200, ADMIN_TOKEN);
	}
}

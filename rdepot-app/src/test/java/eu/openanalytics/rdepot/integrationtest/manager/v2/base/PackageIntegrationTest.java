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

import org.junit.jupiter.api.Test;

import eu.openanalytics.rdepot.integrationtest.manager.v2.IntegrationTest;
import eu.openanalytics.rdepot.integrationtest.manager.v2.RequestType;
import eu.openanalytics.rdepot.integrationtest.manager.v2.TestRequestBody;
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.PackageTestData;

public class PackageIntegrationTest extends IntegrationTest {	
	
	private final PackageTestData testData;
	
	public PackageIntegrationTest() {
		super("/api/v2/manager/packages");
		this.testData = PackageTestData.builder()
				.technologies(Arrays.asList("R", "Python"))
				.examplePackageId("4")
				.deletedPackageId("6")
				.getEndpointNewEventsAmount(0)
				.repositories(Arrays.asList("testrepo4", "testrepo5"))
				.exampleNotExistsPackageId("100")
				.submissionStates(Arrays.asList("accepted", "rejected", "waiting"))
				.search("acc")
				.maintainer(Arrays.asList("Nikola%20Tesla", "Galileo%20Galilei"))
				.deleted(true)
				.build();
	}
	
	@Test
	public void getPackages_Returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET_UNAUTHENTICATED)
				.urlSuffix("?sort=id,asc")
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllPackages() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.token(USER_TOKEN)
				.statusCode(200)
				.urlSuffix("?sort=id,asc")
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/packages/all_packages.json")
				.build();
		testEndpoint(requestBody);	
	}
	
	@Test
	public void getDeletedPackagesAsAdmin() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.token(ADMIN_TOKEN)
				.statusCode(200)
				.urlSuffix("?deleted=" + testData.isDeleted() + "&sort=id,asc")
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/packages/deleted_packages.json")
				.build();
		testEndpoint(requestBody);	
	}
	
	@Test
	public void getDeletedPackagesAsUser() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.token(USER_TOKEN)
				.statusCode(403)
				.urlSuffix("?deleted=" + testData.isDeleted() + "&sort=id,asc")
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/403.json")
				.build();
		testEndpoint(requestBody);	
	}
	
	@Test
	public void getPackagesByRepositories() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.token(USER_TOKEN)
				.statusCode(200)
				.urlSuffix("?repository=" + testData.getRepositories().get(0) + "," + testData.getRepositories().get(1) + "&sort=id,asc")
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/packages/packages_by_repositories.json")
				.build();
		testEndpoint(requestBody);	
	}
	
	@Test
	public void getPackagesByTechnologyAndRepositoryThatNotCoexists() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.token(USER_TOKEN)
				.statusCode(200)
				.urlSuffix("?technology=" +testData.getTechnologies().get(1) + "&repository="+testData.getRepositories().get(0)+"&sort=id,asc")
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/packages/packages_by_technology_and_repository_name_that_not_coexists.json")
				.build();
		testEndpoint(requestBody);	
	}
	
	@Test
	public void getPackagesByMaintainers() throws Exception{
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.token(USER_TOKEN)
				.statusCode(200)
				.urlSuffix("?maintainer=" + testData.getMaintainer().get(0) + "," +testData.getMaintainer().get(1) + "&sort=id,asc")
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/packages/packages_by_maintainers.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getPackagesByNameSearching() throws Exception{
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.token(USER_TOKEN)
				.statusCode(200)
				.urlSuffix("?search=" + testData.getSearch()  + "&sort=id,asc")
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/packages/packages_searching.json")
				.build();
		testEndpoint(requestBody);
	}

	@Test
	public void getPackagesBySubmissionStatesAndTechnology() throws Exception{
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.token(USER_TOKEN)
				.statusCode(200)
				.urlSuffix("?submissionState=" + testData.getSubmissionStates().get(1) + "," + testData.getSubmissionStates().get(2) + "&technology=" + testData.getTechnologies().get(0)+ "&sort=id,asc")
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/packages/packages_by_submission_states_and_technology.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getPackage_returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET_UNAUTHENTICATED)
				.urlSuffix("/" + testData.getExamplePackageId())
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getPackage() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("/" + testData.getExamplePackageId())
				.token(USER_TOKEN)
				.statusCode(200)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath( "/v2/base/packages/package.json")
				.build();
		testEndpoint(requestBody);		
	}
	
	@Test
	public void getDeletedPackage_returns200_whenUserIsAdmin() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("/" + testData.getDeletedPackageId())
				.token(ADMIN_TOKEN)
				.statusCode(200)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath( "/v2/base/packages/package_deleted.json")
				.build();
		testEndpoint(requestBody);		
	}
	
	@Test
	public void getDeletedPackage_returns404_whenUserIsNotAdmin() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("/" + testData.getDeletedPackageId())
				.token(USER_TOKEN)
				.statusCode(404)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath( "/v2/base/packages/404.json")
				.build();
		testEndpoint(requestBody);		
	}
	
	@Test
	public void getPackage_returns404_whenRepositoryNotFound() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("/" + testData.getExampleNotExistsPackageId())
				.token(USER_TOKEN)
				.statusCode(404)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath( "/v2/base/packages/404.json")
				.build();
		testEndpoint(requestBody);		
	}
}
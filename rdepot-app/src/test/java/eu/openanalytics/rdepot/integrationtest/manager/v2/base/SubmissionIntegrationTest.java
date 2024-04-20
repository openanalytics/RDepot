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
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.SubmissionTestData;

public class SubmissionIntegrationTest extends IntegrationTest {
	private final SubmissionTestData testData;
	
	public SubmissionIntegrationTest() {
		super("/api/v2/manager/submissions");
		this.testData = SubmissionTestData.builder()
				.getEndpointNewEventsAmount(0)
				.technologies(Arrays.asList("R", "Python"))
				.repositories(Arrays.asList("testrepo4", "testrepo5"))
				.states(Arrays.asList("accepted", "rejected", "waiting"))
				.fromDate("2020-03-27")
				.toDate("2020-03-28")
				.search("Nikola")
				.build();
	}
	
	@Test
	public void getAllSubmissions() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("?sort=id,asc")
				.statusCode(200)
				.token(USER_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/submissions/all_submissions.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getSubmissionsByStateAndRepository() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("?state=" + testData.getStates().get(1) + "," + testData.getStates().get(2) + "&repository=" + testData.getRepositories().get(0) + "&sort=id,asc")
				.statusCode(200)
				.token(USER_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/submissions/submissions_by_state_and_repository.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getSubmissionsByTechnology() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("?technology=" + testData.getTechnologies().get(0) + "&sort=id,asc")
				.statusCode(200)
				.token(USER_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/submissions/submissions_by_technology.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getSubmissionsByDates() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("?fromDate=" + testData.getFromDate() + "&toDate=" + testData.getToDate() + "&sort=id,asc")
				.statusCode(200)
				.token(USER_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/submissions/submissions_by_dates.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getSubmissionsByNameAndSubmitterAndApproverSearching() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("?search=" + testData.getSearch() + "&sort=id,asc")
				.statusCode(200)
				.token(USER_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/submissions/submissions_searching.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAcceptedSubmissionsWithSearching() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("?state=" + testData.getStates().get(0) + "&search=" + testData.getSearch() + "&sort=id,asc")
				.statusCode(200)
				.token(USER_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/submissions/accepted_submissions_with_searching.json")
				.build();
		testEndpoint(requestBody);
	}
}

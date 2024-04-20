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
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.EventTestData;

public class EventsFilteringIntegrationTest extends IntegrationTest {
	
	private final EventTestData testData; 
	
	public EventsFilteringIntegrationTest() {
		super("/api/v2/manager/events");
		this.testData = EventTestData.builder()
				.getEndpointNewEventsAmount(0)
				.userName("Albert%20Einstein")
				.technologies(Arrays.asList("R", "Python"))
				.resourceTypes(Arrays.asList("submission", "repository"))
				.eventTypes(Arrays.asList("create", "update"))
				.fromDate("2022-09-09")
				.toDate("2023-07-10")
				.build();
	}
	
	@Test
	public void getAllEvents() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("")
				.statusCode(200)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/events/filtering/allEvents.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllEventsAsRepositoryMaintainer() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("")
				.statusCode(200)
				.token(REPOSITORYMAINTAINER_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/events/filtering/allEventsByRepositoryMaintainer.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllEventsAsPackageMaintainer() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("")
				.statusCode(200)
				.token(PACKAGEMAINTAINER_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/events/filtering/allEventsByPackageMaintainer.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllEventsAsUser() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("?sort=id,asc")
				.statusCode(200)
				.token(USER_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/events/filtering/allEventsByUser.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getUserNameAndResourceTypeAndEventType() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("?userName=" + testData.getUserName() + "&resourceType=" + testData.getResourceTypes().get(0)+ "&eventType=" + testData.getEventTypes().get(0) )
				.statusCode(200)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/events/filtering/userNameAndResourceTypeAndEventType.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getTechnology() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("?technology=" + testData.getTechnologies().get(0))
				.statusCode(200)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/events/filtering/technology.json")
				.build();
		testEndpoint(requestBody);
	}
	
	@Test
	public void getEventsByDates() throws Exception {
		TestRequestBody requestBody = TestRequestBody.builder()
				.requestType(RequestType.GET)
				.urlSuffix("?fromDate=" + testData.getFromDate() + "&toDate=" + testData.getToDate())
				.statusCode(200)
				.token(ADMIN_TOKEN)
				.howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
				.expectedJsonPath("/v2/base/events/filtering/byDates.json")
				.build();
		testEndpoint(requestBody);
	}
}

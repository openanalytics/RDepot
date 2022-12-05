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

public class EventsFilteringIntegrationTest extends IntegrationTest {

	private static final int GET_ENDPOINT_NEW_EVENTS_AMOUNT = 0;
	private static final int USER_ID = 5;
	private static final int RESOURCE_ID = 2;
	
	private static final String RESOURCE_TYPE = "repository";
	private static final String UPDATE_EVENT_TYPE = "update";
	
	public EventsFilteringIntegrationTest() {
		super("/api/v2/manager/events");
	}
	
	@Test
	public void getAllEvents() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET,  "/v2/events/filtering/allEvents.json", 
				"", 200, USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getUserEvents() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET,  "/v2/events/filtering/userIdEvents.json", 
				"?userId=" + USER_ID, 200, USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getRepositoryEvents() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET,  "/v2/events/filtering/repositoryEvents.json", 
				"?resourceType=" + RESOURCE_TYPE, 200, USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getUserAndRepositoryEvents() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET,  "/v2/events/filtering/userIdAndRepositoryEvents.json", 
				"?userId=" + USER_ID + "&resourceType=" + RESOURCE_TYPE, 200, USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getUpdateEvents() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET,  "/v2/events/filtering/updateEvents.json", 
				"?eventType=" + UPDATE_EVENT_TYPE, 200, USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getUserAndRepositoryAndUpdateEvents() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET,  "/v2/events/filtering/userIdAndRepoistoryAndUpdateEvents.json", 
				"?userId=" + USER_ID + "&resourceType=" + RESOURCE_TYPE + "&eventType=" + UPDATE_EVENT_TYPE, 
				200, USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getResourceIdEvents() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET,  "/v2/events/filtering/resourceIdEvents.json", 
				"?resourceId=" + RESOURCE_ID, 200, USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getUserAndRepositoryAndUpdateAndResourceIdEvents() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET,  "/v2/events/filtering/userIdAndRepositoryAndUpdatedAndResourceIdEvents.json", 
				"?userId=" + USER_ID + "&resourceType=" + RESOURCE_TYPE + "&eventType=" + UPDATE_EVENT_TYPE + "&resourceId=" + RESOURCE_ID, 
				200, USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	
}

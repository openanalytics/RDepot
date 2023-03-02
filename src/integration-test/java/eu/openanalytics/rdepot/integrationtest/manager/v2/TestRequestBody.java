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

import java.util.Optional;

public class TestRequestBody {
	private RequestType requestType;
	private String expectedJsonPath;
	private String urlSuffix;
	private int statusCode;
	private String token;
	private int howManyNewEventsShouldBeCreated;
	Optional<String> path = Optional.ofNullable(null);
	Optional<String> expectedEventsJson =Optional.ofNullable(null);
	Optional<String> body = Optional.ofNullable(null);
	Optional<SubmissionMultipartBody> submissionMultipartBody = Optional.ofNullable(null);
	
	public Optional<SubmissionMultipartBody> getSubmissionMultipartBody() {
		return submissionMultipartBody;
	}

	public void setSubmissionMultipartBody(Optional<SubmissionMultipartBody> submissionMultipartBody) {
		this.submissionMultipartBody = submissionMultipartBody;
	}

	public TestRequestBody(RequestType requestType, String expectedJson, 
			String urlSuffix, int statusCode, 
			String token, int howManyNewEventsShouldBeCreated,
			String path, String expectedEventsJson, 
			String body
			) {
		this.requestType = requestType;
		this.expectedJsonPath = expectedJson;
		this.urlSuffix = urlSuffix;
		this.statusCode = statusCode;
		this.token = token;
		this.howManyNewEventsShouldBeCreated = howManyNewEventsShouldBeCreated;
		this.path = Optional.of(path);
		this.expectedEventsJson = Optional.of(expectedEventsJson);
		this.body = Optional.of(body);
	}
	
	public TestRequestBody(RequestType requestType, 
			int statusCode, 
			String token, int howManyNewEventsShouldBeCreated,
			String expectedEventsJson, 
			SubmissionMultipartBody submissionMultipartBody
			) {
		this.requestType = requestType;
		this.statusCode = statusCode;
		this.token = token;
		this.howManyNewEventsShouldBeCreated = howManyNewEventsShouldBeCreated;
		this.expectedEventsJson = Optional.of(expectedEventsJson);
		this.submissionMultipartBody = Optional.of(submissionMultipartBody);
	}
	
	public TestRequestBody(RequestType requestType, 
			int statusCode, 
			String token, int howManyNewEventsShouldBeCreated,
			SubmissionMultipartBody submissionMultipartBody
			) {
		this.requestType = requestType;
		this.statusCode = statusCode;
		this.token = token;
		this.howManyNewEventsShouldBeCreated = howManyNewEventsShouldBeCreated;
		this.submissionMultipartBody = Optional.of(submissionMultipartBody);
	}
	
	public TestRequestBody(RequestType requestType, String expectedJson, 
			String urlSuffix, int statusCode, 
			String token, int howManyNewEventsShouldBeCreated,
			String expectedEventsJson, 
			String body
			) {
		this.requestType = requestType;
		this.expectedJsonPath = expectedJson;
		this.urlSuffix = urlSuffix;
		this.statusCode = statusCode;
		this.token = token;
		this.howManyNewEventsShouldBeCreated = howManyNewEventsShouldBeCreated;
		this.expectedEventsJson = Optional.of(expectedEventsJson);
		this.body = Optional.of(body);
	}
	
	public TestRequestBody(RequestType requestType, String expectedJson, 
			String urlSuffix, int statusCode, 
			String token, int howManyNewEventsShouldBeCreated,
			String body
			) {
		this.requestType = requestType;
		this.expectedJsonPath = expectedJson;
		this.urlSuffix = urlSuffix;
		this.statusCode = statusCode;
		this.token = token;
		this.howManyNewEventsShouldBeCreated = howManyNewEventsShouldBeCreated;
		this.body = Optional.of(body);
	}
	
	public TestRequestBody(RequestType requestType, String expectedJson, 
			String urlSuffix, int statusCode, 
			String token, int howManyNewEventsShouldBeCreated
		){
		this.requestType = requestType;
		this.expectedJsonPath = expectedJson;
		this.urlSuffix = urlSuffix;
		this.statusCode = statusCode;
		this.token = token;
		this.howManyNewEventsShouldBeCreated = howManyNewEventsShouldBeCreated;
	}
	
	public TestRequestBody(RequestType requestType,
			String urlSuffix, int statusCode, 
			String token, int howManyNewEventsShouldBeCreated
		){
		this.requestType = requestType;
		this.urlSuffix = urlSuffix;
		this.statusCode = statusCode;
		this.token = token;
		this.howManyNewEventsShouldBeCreated = howManyNewEventsShouldBeCreated;
	}
	
	
	public TestRequestBody(RequestType requestType,
			String urlSuffix, int howManyNewEventsShouldBeCreated
		){
		this.requestType = requestType;
		this.urlSuffix = urlSuffix;
		this.howManyNewEventsShouldBeCreated = howManyNewEventsShouldBeCreated;
	}
	
	public TestRequestBody(RequestType requestType,
			String urlSuffix, String token, int howManyNewEventsShouldBeCreated
		){
		this.requestType = requestType;
		this.urlSuffix = urlSuffix;
		this.token = token;
		this.howManyNewEventsShouldBeCreated = howManyNewEventsShouldBeCreated;
	}
	
	public TestRequestBody(RequestType requestType,
			String urlSuffix, int howManyNewEventsShouldBeCreated,
			String body
		){
		this.requestType = requestType;
		this.urlSuffix = urlSuffix;
		this.howManyNewEventsShouldBeCreated = howManyNewEventsShouldBeCreated;
		this.body = Optional.of(body);
	}
	
	public TestRequestBody(RequestType requestType,
			String urlSuffix, String token, int howManyNewEventsShouldBeCreated,
			String body
		){
		this.requestType = requestType;
		this.urlSuffix = urlSuffix;
		this.token = token;
		this.howManyNewEventsShouldBeCreated = howManyNewEventsShouldBeCreated;
		this.body = Optional.of(body);
	}
	
	public RequestType getRequestType() {
		return requestType;
	}

	public String getExpectedJsonPath() {
		return expectedJsonPath;
	}

	public String getUrlSuffix() {
		return urlSuffix;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getToken() {
		return token;
	}

	public int getHowManyNewEventsShouldBeCreated() {
		return howManyNewEventsShouldBeCreated;
	}

	public Optional<String> getPath() {
		return path;
	}

	public Optional<String> getExpectedEventsJson() {
		return expectedEventsJson;
	}

	public Optional<String> getBody() {
		return body;
	}
	
	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	public void setExpectedJsonPath(String expectedJsonPath) {
		this.expectedJsonPath = expectedJsonPath;
	}

	public void setUrlSuffix(String urlSuffix) {
		this.urlSuffix = urlSuffix;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setHowManyNewEventsShouldBeCreated(int howManyNewEventsShouldBeCreated) {
		this.howManyNewEventsShouldBeCreated = howManyNewEventsShouldBeCreated;
	}

	public void setPath(Optional<String> path) {
		this.path = path;
	}

	public void setExpectedEventsJson(Optional<String> expectedEventsJson) {
		this.expectedEventsJson = expectedEventsJson;
	}

	public void setBody(Optional<String> body) {
		this.body = body;
	}
}

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

import static io.restassured.RestAssured.given;

import java.io.FileReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.openanalytics.rdepot.integrationtest.manager.v2.IntegrationTest;
import io.restassured.http.ContentType;

public class ErrorIntegrationTest extends IntegrationTest {

	private static final String API_PATH = "/api/v2/manager/r/repositories";
	private static final String REPO_ID_TO_PUBLISH = "5";
	
	public ErrorIntegrationTest() {
		super("/api/v2/manager/r/repositories");
	}
	
	@Test
	public void test_400() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/published\","
				+ "\"value\":true"
				+ "]";
		
		JSONParser jsonParser = new JSONParser();
		FileReader reader = new FileReader(JSON_PATH + "/v2/400.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
				.headers(AUTHORIZATION, BASIC + REPOSITORYMAINTAINER_TOKEN)
				.accept(ContentType.JSON)
				.contentType("application/json-patch+json")
				.body(patch)
			.when()
				.patch(API_PATH + "/" + REPO_ID_TO_PUBLISH)
			.then()
				.statusCode(400)
				.extract()
				.asString();
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		Assertions.assertEquals(expectedJSON, actualJSON, "Incorrect JSON output");
	}
	
	@Test
	public void test_405() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/published\","
				+ "\"value\":true"
				+ "}"
				+ "]";
		
		JSONParser jsonParser = new JSONParser();
		FileReader reader = new FileReader(JSON_PATH + "/v2/405.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
				.headers(AUTHORIZATION, BASIC + REPOSITORYMAINTAINER_TOKEN)
				.accept(ContentType.JSON)
				.contentType("application/json-patch+json")
				.body(patch)
			.when()
				.patch(API_PATH)
			.then()
				.statusCode(405)
				.extract()
				.asString();
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		Assertions.assertEquals(expectedJSON, actualJSON, "Incorrect JSON output");
	}
	
	@Test
	public void test_406() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/published\","
				+ "\"value\":true"
				+ "}"
				+ "]";
		
		JSONParser jsonParser = new JSONParser();
		FileReader reader = new FileReader(JSON_PATH + "/v2/406.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
				.headers(AUTHORIZATION, BASIC + REPOSITORYMAINTAINER_TOKEN)
				.accept(ContentType.HTML)
				.contentType("application/json-patch+json")
				.body(patch)
			.when()
				.patch(API_PATH + "/" + REPO_ID_TO_PUBLISH)
			.then()
				.statusCode(406)
				.extract()
				.asString();
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		Assertions.assertEquals(expectedJSON, actualJSON, "Incorrect JSON output");
	}
	
	@Test
	public void test_415() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/published\","
				+ "\"value\":true"
				+ "}"
				+ "]";
		
		JSONParser jsonParser = new JSONParser();
		FileReader reader = new FileReader(JSON_PATH + "/v2/415.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);		
		
		String data = given()
				.headers(AUTHORIZATION, BASIC + REPOSITORYMAINTAINER_TOKEN)
				.accept(ContentType.JSON)
				.contentType(ContentType.JSON)
				.body(patch)
			.when()
				.patch(API_PATH + "/" + REPO_ID_TO_PUBLISH)
			.then()
				.statusCode(415)
				.extract()
				.asString();		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		Assertions.assertEquals(expectedJSON, actualJSON, "Incorrect JSON output");
	}
}

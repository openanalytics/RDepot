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
package eu.openanalytics.rdepot.integrationtest;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.BeforeClass;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public abstract class IntegrationTest {
	public static final String ADMIN_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJlaW5zdGVpbiJ9.9VweA_kotRnnLn9giSE511MhWX4iDwtx85lidw_ZT5iTQ1aOB-3ytJNDB_Mrcop2H22MNhMjbpUW_sraHdvOlw"; 
	public static final String REPOSITORYMAINTAINER_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXNsYSJ9.FEQ3KqMvTj4LQAgQx23f6Y0Z7PzKHgcO1a1UodG5iwCrzXhk6tHCR6V0T16F1tWtMMF0a3AQIShczN__d6KsFA"; 
	public static final String PACKAGEMAINTAINER_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJnYWxpZWxlbyJ9.Hp95DiIZ0L0JXyQZOvhJkzyTDzNuos81QoTWfLeVPlodWvGg7ziJTI6nJFitg5VAwrGmA4wpbWbjK9aItCKB3A"; 
	public static final String USER_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuZXd0b24ifQ.3E7UwKTwc8DchKRUSD_hdJxOcl4L6SOguwbm9WmVzWU4YDQMkIJ_wVNidpus6gNJvyT6OR6pREkfQCnWkEhEBQ";
	
	public static final String AUTHORIZATION = "Authorization";
	public static final String BEARER = "Bearer ";
	public static final String JSON_PATH = "src/integration-test/resources/JSONs";
	public static final String PUBLICATION_URI_PATH = "/repo";
	
	public final String API_PATH;
	
	public IntegrationTest(String apiPath) {
		this.API_PATH = apiPath;
	}
	
	protected void testGetEndpoint(String expectedJsonPath, String urlSuffix, 
			Integer statusCode, String token) throws Exception {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + expectedJsonPath);
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()
				.header(AUTHORIZATION, BEARER + token)
				.accept(ContentType.JSON)
			.when()
				.get(API_PATH + urlSuffix)
			.then()
				.statusCode(statusCode)
				.extract()
				.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		assertEquals("Incorrect JSON output.", expectedJSON, actualJSON);
	}
	
	protected void testGetEndpoint(String expectedJsonPath, String path, String urlSuffix, 
			Integer statusCode, String token) throws Exception {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + expectedJsonPath);
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()
				.header(AUTHORIZATION, BEARER + token)
				.accept(ContentType.JSON)
			.when()
				.get(path + urlSuffix)
			.then()
				.statusCode(statusCode)
				.extract()
				.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		assertEquals("Incorrect JSON output.", expectedJSON, actualJSON);
	}
	
	protected void testDeleteEndpoint(String urlSuffix, Integer statusCode, String token) {
			given()
				.header(AUTHORIZATION, BEARER + token)
				.accept(ContentType.JSON)
			.when()
				.delete(API_PATH + urlSuffix)
			.then()
				.statusCode(statusCode);
	}
	
	protected void testGetEndpointUnauthenticated(String urlSuffix) throws Exception {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/401.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()
				.accept(ContentType.JSON)
			.when()
				.get(API_PATH + urlSuffix)
			.then()
				.statusCode(401)
				.extract()
				.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		assertEquals("Incorrect JSON output", expectedJSON, actualJSON);
	}
	
	protected void testPatchEndpointUnauthenticated(String patch, String urlSuffix) throws Exception {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/401.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()
				.accept(ContentType.JSON)
				.contentType("application/json-patch+json")
				.body(patch)
			.when()
				.patch(API_PATH + urlSuffix)
			.then()
				.statusCode(401)
				.extract()
				.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		assertEquals("Incorrect JSON output.", expectedJSON, actualJSON);		
	}
	
	protected void testPatchEndpoint(String patch, String expectedJsonPath, String urlSuffix, 
			Integer statusCode, String token) throws Exception {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + expectedJsonPath);
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()
				.header(AUTHORIZATION, BEARER + token)
				.accept(ContentType.JSON)
				.contentType("application/json-patch+json")
				.body(patch)
			.when()
				.patch(API_PATH + urlSuffix)
			.then()
				.statusCode(statusCode)
				.extract()
				.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		assertEquals("Incorrect JSON output", expectedJSON, actualJSON);		
	}
	
	protected void testPostEndpoint(String body, String expectedJsonPath, 
			Integer statusCode, String token) throws Exception {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + expectedJsonPath);
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()
				.header(AUTHORIZATION, BEARER + token)
				.accept(ContentType.JSON)
				.contentType(ContentType.JSON)
				.body(body)
			.when()
				.post(API_PATH)
			.then()
				.statusCode(statusCode)
				.extract()
				.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		assertEquals("There are some differences in packages that user sees.", expectedJSON, actualJSON);		
	}
	
	protected void testPostEndpoint_asUnauthenticated(String body, String expectedJsonPath, 
			Integer statusCode) throws Exception {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + expectedJsonPath);
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()
				.accept(ContentType.JSON)
				.contentType(ContentType.JSON)
				.body(body)
			.when()
				.post(API_PATH)
			.then()
				.statusCode(statusCode)
				.extract()
				.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		assertEquals("There are some differences in packages that user sees.", expectedJSON, actualJSON);
	}
	
	protected void testDeleteEndpointUnauthenticated(String suffix) {
		given().accept(ContentType.JSON).when().delete(API_PATH + suffix).then().statusCode(401);
	}
	
	@BeforeClass
	public static final void configureRestAssured() throws IOException, InterruptedException {
		RestAssured.port = 8017;
		RestAssured.urlEncodingEnabled = false;
	}
	
	@Before
	public final void setUp() throws IOException, InterruptedException {
		String[] cmd = new String[] {"gradle", "restore", "-b","src/integration-test/resources/build.gradle"};
		Process process = Runtime.getRuntime().exec(cmd);
		process.waitFor();
		process.destroy();		
	}
	
	@SuppressWarnings("unchecked")
	protected Set<JSONObject> convert(JSONArray rootJSON) throws ParseException {
		Set<JSONObject> JSON = new HashSet<>();
		
		for(int i = 0; i < rootJSON.size(); i++) {
			JSONObject objJSON = (JSONObject) rootJSON.get(i);
			String source = objJSON.get("source").toString();
			objJSON.remove("lastLoggedInOn");
			objJSON.remove("source");
			String newSource = source.replaceFirst("/[0-9]{2}[0-9]+", "");
			objJSON.put("source", newSource);
			JSON.add(objJSON);
		}		
		return JSON;
	 }
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected List<Set> convertPackages(JsonArray rootJSON) throws ParseException {
		List<Set> JSON = new ArrayList<>();
			
		for(int i = 0; i < rootJSON.size(); i++) {
			JsonObject repositoryJSON = (JsonObject) rootJSON.get(i);
			JsonArray packagesJSON = (JsonArray) repositoryJSON.get("packages");
			Set JSONSet = new HashSet<>();
			for(int k = 0; k < packagesJSON.size(); k++) {
				JsonObject packageJSON = (JsonObject) packagesJSON.get(k);
				JSONSet.add(packageJSON);
			}
			JSON.add(JSONSet);
		}
		return JSON;
	 }
	
	protected boolean compareListOfMaintainersFromGetMaintainers(JsonObject expected, JsonObject actual) throws ParseException {
		if (expected == null || actual == null)
			return false;
		
		expected.remove("repositories");
		actual.remove("repositories");
		
		if(!expected.equals(actual))
			return false;
		
		return true;
	}
	
	protected String extractContent(byte[] pdf) throws IOException {
	    PDDocument document = PDDocument.load(new ByteArrayInputStream(pdf));
	    try {
	         return new PDFTextStripper().getText(document);
	     } finally {
	    	 document.close();
	     }
	}
	
	protected byte[] readFileToByteArray(File file){
		FileInputStream fis;
	    byte[] bArray = new byte[(int) file.length()];
        try{
            fis = new FileInputStream(file);
            fis.read(bArray);
            fis.close();             
        } catch(IOException ioExp){
        	ioExp.printStackTrace();
	    }
	    return bArray;
	}
}

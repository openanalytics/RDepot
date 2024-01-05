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

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.BeforeClass;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
	
	public final String apiPath;
	
	public IntegrationTest(String apiPath) {
		this.apiPath = apiPath;
	}

	public void testEndpoint(TestRequestBody requestBody) throws Exception {
		int eventsNumberBeforeOperation = getTotalEventsAmount();
		chooseEndpoint(requestBody);
		int eventsNumberAfterOperation = getTotalEventsAmount();
		int result = eventsNumberAfterOperation - eventsNumberBeforeOperation;
		if(requestBody.getExpectedEventsJson().isPresent()) {
			testIfNewestEventsAreCorret(requestBody.getHowManyNewEventsShouldBeCreated(), requestBody.getExpectedEventsJson().get());
		}
		assertTrue("there are different numbers of new events in database then expected, was: " + result + ", but expected: " + requestBody.getHowManyNewEventsShouldBeCreated(),
				result == requestBody.getHowManyNewEventsShouldBeCreated());
	}
	
	protected int runCommand(String ...args) throws IOException, InterruptedException {
		int exitValue = -1;
		Process process = Runtime.getRuntime().exec(args);
		exitValue = process.waitFor();
		process.destroy();
		
		return exitValue;
	}
	
	private void chooseEndpoint(TestRequestBody req) throws Exception {
		RequestType requestType = req.getRequestType();
			
		switch(requestType) {
			case GET:
				testGetEndpoint(req.getExpectedJsonPath(),req.getUrlSuffix(), req.getStatusCode(), req.getToken());
				break;
			case GET_WITH_NEW_PATCH:
				testGetEndpoint(req.getExpectedJsonPath(), req.getPath().get(), req.getUrlSuffix(), req.getStatusCode(), req.getToken());
				break;
			case GET_UNAUTHENTICATED:
				testGetEndpointUnauthenticated(req.getUrlSuffix());
				break;
			case GET_UNAUTHORIZED:
				testGetEndpointUnauthorized(req.getUrlSuffix(), req.getToken());
				break;
			case PATCH:
				testPatchEndpoint(req.getBody().get(), req.getExpectedJsonPath(), req.getUrlSuffix(), req.getStatusCode(), req.getToken());
				break;
			case PATCH_UNAUTHENTICATED:
				testPatchEndpointUnauthenticated(req.getBody().get(), req.getUrlSuffix());
				break;
			case PATCH_UNAUTHORIZED:
				testPatchEndpointUnauthorized(req.getBody().get(), req.getUrlSuffix(), req.getToken());
				break;
			case POST:
				testPostEndpoint(req.getBody().get(), req.getExpectedJsonPath(), req.getStatusCode(), req.getToken());
				break;
			case POST_UNAUTHENTICATED:
				testPostEndpoint_asUnauthenticated(req.getBody().get());
				break;
			case POST_UNAUTHORIZED:
				testPostEndpoint_asUnauthorized(req.getBody().get(), req.getToken());
				break;
			case POST_MULTIPART:
				testPostMultipartEndpoint(req.getSubmissionMultipartBody().get(), req.getStatusCode(), req.getToken());
				break;
			case DELETE:
				testDeleteEndpoint(req.getUrlSuffix(), req.getStatusCode(), req.getToken());
				break;
			case DELETE_UNAUTHENTICATED:
				testDeleteEndpointUnauthenticated(req.getUrlSuffix());
				break;
			case DELETE_UNAUTHORIZED:
				testDeleteEndpointUnauthorized(req.getUrlSuffix(), req.getToken());
				break;
			default:
				break;
		}
	}
	
	protected void testGetEndpoint(String expectedJsonPath, String urlSuffix, 
			int statusCode, String token) throws Exception {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + expectedJsonPath);
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()
				.header(AUTHORIZATION, BEARER + token)
				.accept(ContentType.JSON)
			.when()
				.get(apiPath + urlSuffix)
			.then()
				.statusCode(statusCode)
				.extract()
				.asString();		
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		
		assertEquals("Incorrect JSON output.", expectedJSON.remove("source"), actualJSON.remove("source"));
	}
	
	protected void testGetEndpoint(String expectedJsonPath, String path, String urlSuffix, 
			int statusCode, String token) throws Exception {
		
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
	
	protected void testDeleteEndpoint(String urlSuffix, int statusCode, String token) throws JsonMappingException, JsonProcessingException, ParseException {
			given()
				.header(AUTHORIZATION, BEARER + token)
				.accept(ContentType.JSON)
			.when()
				.delete(apiPath + urlSuffix)
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
					.get(apiPath + urlSuffix)
				.then()
					.statusCode(401)
					.extract()
					.asString();
			
			JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
			assertEquals("Incorrect JSON output", expectedJSON, actualJSON);
		}
	
	protected void testGetEndpointUnauthorized(String urlSuffix, String token) throws Exception {
		
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/403.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()
				.accept(ContentType.JSON)
				.header(AUTHORIZATION, BEARER + token)
			.when()
				.get(apiPath + urlSuffix)
			.then()
				.statusCode(403)
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
				.patch(apiPath + urlSuffix)
			.then()
				.statusCode(401)
				.extract()
				.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		assertEquals("Incorrect JSON output.", expectedJSON, actualJSON);		
	}
	
	protected void testPatchEndpointUnauthorized(String patch, String urlSuffix, String token) throws Exception {
		
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/403.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()
				.header(AUTHORIZATION, BEARER + token)
				.accept(ContentType.JSON)
				.contentType("application/json-patch+json")
				.body(patch)
			.when()
				.patch(apiPath + urlSuffix)
			.then()
				.statusCode(403)
				.extract()
				.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		assertEquals("Incorrect JSON output.", expectedJSON, actualJSON);		
	}
	
	protected void testPatchEndpoint(String patch, String expectedJsonPath, String urlSuffix, 
			int statusCode, String token) throws Exception {
		
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + expectedJsonPath);
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()
				.header(AUTHORIZATION, BEARER + token)
				.accept(ContentType.JSON)
				.contentType("application/json-patch+json")
				.body(patch)
			.when()
				.patch(apiPath + urlSuffix)
			.then()
				.statusCode(statusCode)
				.extract()
				.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		assertEquals("Incorrect JSON output", expectedJSON.remove("source"), actualJSON.remove("source"));		
	}	
	
	protected void testPostEndpoint(String body, String expectedJsonPath, 
			int statusCode, String token) throws Exception {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + expectedJsonPath);
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()
				.header(AUTHORIZATION, BEARER + token)
				.accept(ContentType.JSON)
				.contentType(ContentType.JSON)
				.body(body)
			.when()
				.post(apiPath)
			.then()
				.statusCode(statusCode)
				.extract()
				.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		assertEquals("There are some differences in packages that user sees.", expectedJSON, actualJSON);		
	}
	
	protected void testPostMultipartEndpoint(SubmissionMultipartBody body,
			int statusCode, String token) throws Exception {		
		if(body.getReplace() != null && body.getGenerateManual() != null) {
			given()
					.header(AUTHORIZATION, BEARER + token)
					.accept(ContentType.JSON)
					.contentType("multipart/form-data")
					.multiPart("repository", body.getRepository())
					.multiPart("generateManual", body.getGenerateManual())
					.multiPart("replace", body.getReplace())
					.multiPart(body.getMultipartFile())
				.when()
					.post(apiPath)
				.then()
					.statusCode(statusCode)
					.extract()
					.asString();
		}
		else {
			given()
					.header(AUTHORIZATION, BEARER + token)
					.accept(ContentType.JSON)
					.contentType("multipart/form-data")
					.multiPart("repository", body.getRepository())
					.multiPart(body.getMultipartFile())
				.when()
					.post(apiPath)
				.then()
					.statusCode(statusCode)
					.extract()
					.asString();
		}		
	}
	
	protected void testPostEndpoint_asUnauthenticated(String body) throws Exception {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/401.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()
				.accept(ContentType.JSON)
				.contentType(ContentType.JSON)
				.body(body)
			.when()
				.post(apiPath)
			.then()
				.statusCode(401)
				.extract()
				.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		assertEquals("There are some differences in packages that user sees.", expectedJSON, actualJSON);
	}
	
	protected void testPostEndpoint_asUnauthorized(String body, String token) throws Exception {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH +  "/v2/403.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()
				.header(AUTHORIZATION, BEARER + token)
				.accept(ContentType.JSON)
				.contentType(ContentType.JSON)
				.body(body)
			.when()
				.post(apiPath)
			.then()
				.statusCode(403)
				.extract()
				.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		assertEquals("There are some differences in packages that user sees.", expectedJSON, actualJSON);
	}
	
	
	protected void testDeleteEndpointUnauthenticated(String suffix) {
		given().accept(ContentType.JSON).when().delete(apiPath + suffix).then().statusCode(401);
	}
	
	protected void testDeleteEndpointUnauthorized(String suffix, String token) throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/403.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()
				.accept(ContentType.JSON)
				.header(AUTHORIZATION, BEARER + token)
			.when()
				.delete(apiPath + suffix)
			.then()
				.statusCode(403)
				.extract()
				.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		assertEquals("Incorrect JSON output", expectedJSON, actualJSON);
	}
	
//	@BeforeClass
//	public static final void configureRestAssured() throws IOException, InterruptedException {
//		RestAssured.port = 8017;
//		RestAssured.urlEncodingEnabled = false;
//	}
	
	@Before
	public void setUp() throws IOException, InterruptedException {
		String[] cmd = new String[] {"gradle", "restore", "-b","src/integration-test/resources/build.gradle"};
		Process process = Runtime.getRuntime().exec(cmd);
		process.waitFor();
		process.destroy();		
	}
	
	@SuppressWarnings("unchecked")
	protected List<JSONObject> convert(JSONArray rootJSON) throws ParseException {
		List<JSONObject> JSON = new ArrayList<JSONObject>();
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
	protected List<List> convertPackages(JsonArray rootJSON) throws ParseException {
		List<List> JSON = new ArrayList<>();
			
		for(int i = 0; i < rootJSON.size(); i++) {
			JsonObject repositoryJSON = (JsonObject) rootJSON.get(i);
			JsonArray packagesJSON = (JsonArray) repositoryJSON.get("packages");
			List JSONList = new ArrayList<>();
			for(int k = 0; k < packagesJSON.size(); k++) {
				JsonObject packageJSON = (JsonObject) packagesJSON.get(k);
				JSONList.add(packageJSON);
			}
			JSON.add(JSONList);
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
	
	private int getTotalEventsAmount() throws ParseException, JsonMappingException, JsonProcessingException {
		
		String data = given()
		.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
		.accept(ContentType.JSON)
		.body("{resourceType: ${resourceType}}")
		.when()
			.get("/api/v2/manager/events")
		.then()
			.statusCode(200)
			.extract()
			.asString();
	
		JsonNode eventsNode = new ObjectMapper().readTree(data);		
		
		JsonNode result = eventsNode.get("data")
				.get("page")
				.get("totalElements");
		
		return Integer.valueOf(result.toString());
	}
	
	private void testIfNewestEventsAreCorret(int howMany, String expectedJsonPath) throws IOException, ParseException {
		
		String data = given()
		.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
		.accept(ContentType.JSON)
		.when()
			.get("/api/v2/manager/events")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonNode expectedJSON = new ObjectMapper().readTree(new File(JSON_PATH + expectedJsonPath)).get("data").get("content");	
		JsonNode eventsNode = new ObjectMapper().readTree(data).get("data").get("content");
		List<ObjectNode> eventsNodeConverted = convertEvents(eventsNode, howMany);
		
		assertEquals("Events are not equal", convertEvents(expectedJSON, howMany), eventsNodeConverted);	
	}

	private List<ObjectNode> convertEvents(JsonNode events, int howMany) {
		List<ObjectNode> result = new ArrayList<ObjectNode>();
		for(int i = 0; i < howMany; i++) {
			ObjectNode tmpEvent = (ObjectNode) events.get(i);
			tmpEvent.remove("time");
			result.add(tmpEvent);
		}
		return result;
	}
}

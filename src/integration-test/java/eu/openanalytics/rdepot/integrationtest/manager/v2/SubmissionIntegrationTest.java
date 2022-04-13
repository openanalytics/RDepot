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

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.openanalytics.rdepot.integrationtest.IntegrationTest;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;

public class SubmissionIntegrationTest extends IntegrationTest {		
	
	private final String API_SUBMISSIONS_PATH = "/api/v2/manager/r/submissions";
	private final String API_PACKAGES_PATH = "/api/v2/manager/r/packages";
	
	private final String SUBMISSION_ID = "5";
	private final String SUBMISSION_ID_REPOSITORYMAINTAINER = "17";
	private final String SUBMISSION_ID_PACKAGEMAINTAINER = "14";
	private final String SUBMISSION_ID_USER = "19";
	private final String SUBMISSION_ID_TO_ACCEPT = "30";
	private final String SUBMISSION_ID_TO_CANCEL = "31";
	
	private final String PACKAGE_ID = "32";
	private final String PACKAGE_NAME_TO_DOWNLOAD = "Benchmarking";
	private final String PDF_PATH = "src/integration-test/resources/itestPdf";	
	
	public SubmissionIntegrationTest() {
		super("/api/v2/manager/r/submissions");
	}
	
	@Test
	public void submitPackage_createManualsByDefault() throws Exception {		
		FileReader reader = new FileReader(JSON_PATH + "/v2/submission/uploaded_package_as_admin.json");
		JsonObject expectedJSON = JsonParser.parseReader(reader).getAsJsonObject();
		
		File packageBag = new File ("src/integration-test/resources/itestPackages/Benchmarking_0.10.tar.gz");
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept("application/json")
			.contentType("multipart/form-data")
			.multiPart("repository", "testrepo2")
			.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
					.fileName(packageBag.getName())
					.mimeType("application/gzip")
					.controlName("file")
					.build())
		.when()
			.post(API_SUBMISSIONS_PATH)
		.then()
			.statusCode(201)
			.extract()
			.asString();
		
		JsonObject actualJSON = JsonParser.parseString(data).getAsJsonObject();
		assertEquals(convertSubmissionJson(expectedJSON), convertSubmissionJson(actualJSON));
		
		byte[] pdf = given()
				.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
				.accept(ContentType.ANY)
				
			.when()
            	.get(API_PACKAGES_PATH + "/" + PACKAGE_ID + "/manual")
			.then()
				.statusCode(200)
				.extract()
				.asByteArray();
		
		File file = new File(PDF_PATH + "/" + PACKAGE_NAME_TO_DOWNLOAD + ".pdf");
		byte[] expectedpdf = readFileToByteArray(file);
		assertTrue("Manual PDFs are too different", expectedpdf.length + 1000 > pdf.length);
		assertTrue("Manual PDFs are too different", expectedpdf.length - 1000 < pdf.length);
		
		reader = new FileReader(JSON_PATH + "/v2/package/uploaded_package.json");
		expectedJSON = JsonParser.parseReader(reader).getAsJsonObject();
		
		
		data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept("application/json")
		.when()
			.get(API_PACKAGES_PATH + "/" + PACKAGE_ID)
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		actualJSON = JsonParser.parseString(data).getAsJsonObject();
		assertEquals(convertPackageJson(expectedJSON), convertPackageJson(actualJSON));
	}
	
	@Test
	public void submitPackage_notCreateManual() throws IOException {				
		FileReader reader = new FileReader(JSON_PATH + "/v2/submission/uploaded_package_as_admin.json");
		JsonObject expectedJSON = JsonParser.parseReader(reader).getAsJsonObject();		

		
		File packageBag = new File ("src/integration-test/resources/itestPackages/Benchmarking_0.10.tar.gz");
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept("application/json")
			.contentType("multipart/form-data")
			.multiPart("repository", "testrepo2")
			.multiPart("generateManual", false)
			.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
					.fileName(packageBag.getName())
					.mimeType("application/gzip")
					.controlName("file")
					.build())
		.when()
			.post(API_SUBMISSIONS_PATH)
		.then()
			.statusCode(201)
			.extract()
			.asString();
		
		JsonObject actualJSON = JsonParser.parseString(data).getAsJsonObject();		
		assertEquals(convertSubmissionJson(expectedJSON), convertSubmissionJson(actualJSON));
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.ANY)		
		.when()
        	.get(API_PACKAGES_PATH + "/" + PACKAGE_ID + "/manual")
		.then()
			.statusCode(404);
	}
	
	@Test
	public void submitPackage_replace() throws IOException{
		File packageBag = new File ("src/integration-test/resources/itestPackages/A3_0.9.1.tar.gz");
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept("application/json")
			.contentType("multipart/form-data")
			.multiPart("repository", "testrepo2")
			.multiPart("generateManual", false)
			.multiPart("replace", true)
			.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
					.fileName(packageBag.getName())
					.mimeType("application/gzip")
					.controlName("file")
					.build())
		.when()
			.post(API_SUBMISSIONS_PATH)
		.then()
			.statusCode(201);
		
		packageBag = new File ("src/integration-test/resources/itestPackages/A3_0-9-1.tar.gz");
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept("application/json")
			.contentType("multipart/form-data")
			.multiPart("repository", "testrepo2")
			.multiPart("generateManual", false)
			.multiPart("replace", true)
			.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
					.fileName(packageBag.getName())
					.mimeType("application/gzip")
					.controlName("file")
					.build())
		.when()
			.post(API_SUBMISSIONS_PATH)
		.then()
			.statusCode(201);
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/package/list_of_packages_with_replaced_package.json");
		JsonObject expectedJSON = JsonParser.parseReader(reader).getAsJsonObject();
		
		String data = given()
				.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
				.accept(ContentType.JSON)
			.when()
				.get(API_PACKAGES_PATH + "?sort=name,version,asc")
			.then()
				.statusCode(200)
				.extract()
				.asString();
		
		JsonObject actualJSON = JsonParser.parseString(data).getAsJsonObject();
		assertEquals("There are some differences in packages that user sees.", convertListOfPackages(expectedJSON), convertListOfPackages(actualJSON));
	}
	
	@Test
	public void submitPackage_notReplace() throws IOException {
		FileReader reader = new FileReader(JSON_PATH + "/v2/submission/invalid_submission.json");
		JsonObject expectedJSON = JsonParser.parseReader(reader).getAsJsonObject();
		File packageBag = new File ("src/integration-test/resources/itestPackages/A3_0.9.1.tar.gz");
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept("application/json")
			.contentType("multipart/form-data")
			.multiPart("repository", "testrepo2")
			.multiPart("generateManual", false)
			.multiPart("replace", true)
			.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
					.fileName(packageBag.getName())
					.mimeType("application/gzip")
					.controlName("file")
					.build())
		.when()
			.post(API_SUBMISSIONS_PATH)
		.then()
			.statusCode(201);
		
		packageBag = new File ("src/integration-test/resources/itestPackages/A3_0-9-1.tar.gz");
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept("application/json")
			.contentType("multipart/form-data")
			.multiPart("repository", "testrepo2")
			.multiPart("generateManual", false)
			.multiPart("replace", false)
			.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
					.fileName(packageBag.getName())
					.mimeType("application/gzip")
					.controlName("file")
					.build())
		.when()
			.post(API_SUBMISSIONS_PATH)
		.then()
			.statusCode(422)
			.extract()
			.asString();
		
		JsonObject actualJSON = JsonParser.parseString(data).getAsJsonObject();		
		assertEquals("Package should not be replaced", expectedJSON, actualJSON);
	}
	
	@Test
	public void getAllSubmissions_asAdmin() throws Exception {
		testGetEndpoint("/v2/submission/all_submissions_viewed_by_admin.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN);			
	}
	
	@Test
	public void getAllSubmissions_asRepositoryMaintainer() throws Exception {
		testGetEndpoint("/v2/submission/all_submissions_viewed_by_repositorymaintainer.json", 
				"?sort=id,asc", 200, REPOSITORYMAINTAINER_TOKEN);			
	}
	
	@Test
	public void getAllSubmissions_asPackageMaintainer() throws Exception {
		testGetEndpoint("/v2/submission/all_submissions_viewed_by_packagemaintainer.json", 
				"?sort=id,asc", 200, PACKAGEMAINTAINER_TOKEN);			
	}
	
	
	@Test
	public void getOnlyCancelledSubmissions_asAdmin() throws Exception {
		testGetEndpoint("/v2/submission/cancelled_submissions_viewed_by_admin.json", 
				"?state=cancelled&sort=id,asc", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void getOnlyWaitingSubmissions_asAdmin() throws Exception {
		testGetEndpoint("/v2/submission/waiting_submissions_viewed_by_admin.json", 
				"?state=waiting&sort=id,asc", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void getAllSubmissionsOfUser_asAdmin() throws Exception {
		testGetEndpoint("/v2/submission/all_submissions_of_user_viewed_by_admin.json", 
				"?userId=7&sort=id,asc", 200, ADMIN_TOKEN);			
	}
		
	@Test
	public void getAllSubmissionsOfUser_asThisUser() throws Exception {
		testGetEndpoint("/v2/submission/all_submissions_of_user_viewed_by_this_user.json", 
				"?sort=id,asc", 200, USER_TOKEN);			
	}
	
	@Test
	public void getAllSubmissionsOfUser_whenUserIsNotAllowed() throws Exception {
		testGetEndpoint("/v2/submission/all_submissions_of_user_empty.json", 
				"?userId=5&sort=id,asc", 200, USER_TOKEN);			
	}
	
	@Test
	public void getAllSubmissions_returns401_whenUserIsNotAuthenticated() throws Exception {
		testGetEndpointUnauthenticated("/");
	}
	
	@Test
	public void getOneSubmission_asAdmin() throws Exception {	
		testGetEndpoint("/v2/submission/one_submission_viewed_by_admin.json", 
				"/" + SUBMISSION_ID, 200, ADMIN_TOKEN);
	}
	
	@Test
	public void getOneSubmission_asRepositoryMaintainer() throws Exception {	
		testGetEndpoint("/v2/submission/one_submission_viewed_by_repositorymaintainer.json", 
				"/" + SUBMISSION_ID_REPOSITORYMAINTAINER, 200, REPOSITORYMAINTAINER_TOKEN);
	}
	
	@Test
	public void getOneSubmission_asPackageMaintainer() throws Exception {	
		testGetEndpoint("/v2/submission/one_submission_viewed_by_packagemaintainer.json", 
				"/" + SUBMISSION_ID_PACKAGEMAINTAINER, 200, PACKAGEMAINTAINER_TOKEN);
	}
	
	@Test
	public void getOneSubmission_asUser() throws Exception {	
		testGetEndpoint("/v2/submission/one_submission_viewed_by_user.json", 
				"/" + SUBMISSION_ID_USER, 200, USER_TOKEN);
	}
	
	@Test
	public void getOneSubmission_returns401_whenUserIsNotAuthenticated() throws Exception {
		testGetEndpointUnauthenticated("/" + SUBMISSION_ID_USER);
	}
	
	@Test
	public void deleteSubmission() throws FileNotFoundException {
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_SUBMISSIONS_PATH + "/" + SUBMISSION_ID)
		.then()
			.statusCode(204);
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/submission/submission_not_found.json");
		JsonObject expectedJSON = JsonParser.parseReader(reader).getAsJsonObject();		
		
		String data = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_SUBMISSIONS_PATH + "/" + SUBMISSION_ID)
		.then()
			.statusCode(404)
			.extract()
			.asString();
		
		JsonObject actualJSON = JsonParser.parseString(data).getAsJsonObject();		
		assertEquals(expectedJSON, actualJSON);
	}
	
	@Test
	public void deleteSubmission_Returns401_whenUserIsNotAuthenticated() {
		testDeleteEndpointUnauthenticated(SUBMISSION_ID);
	}
	
	@Test
	public void acceptSubmission() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/state\","
				+ "\"value\":\"accepted\""
				+ "}"
				+ "]";
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/submission/accepted_submission.json");
		JsonObject expectedJSON = JsonParser.parseReader(reader).getAsJsonObject();
		
		String data = given()
				.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
				.accept(ContentType.JSON)
				.contentType("application/json-patch+json")
				.body(patch)
			.when()
				.patch(API_PATH + "/" + SUBMISSION_ID_TO_ACCEPT)
			.then()
				.statusCode(200)
				.extract()
				.asString();
		
		JsonObject actualJSON = JsonParser.parseString(data).getAsJsonObject();
		assertEquals(convertSubmissionJson(expectedJSON), convertSubmissionJson(actualJSON));
	}
	
	@Test
	public void cancelSubmission() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/state\","
				+ "\"value\":\"cancelled\""
				+ "}"
				+ "]";		
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/v2/submission/cancelled_submission.json");
		JSONObject expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		String data = given()
				.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
				.accept(ContentType.JSON)
				.contentType("application/json-patch+json")
				.body(patch)
			.when()
				.patch(API_PATH + "/" + SUBMISSION_ID_TO_CANCEL)
			.then()
				.statusCode(200)
				.extract()
				.asString();
		
		JSONObject actualJSON = (JSONObject) jsonParser.parse(data);
		fixJsonRandomValueInSinglePackage(actualJSON);
		assertEquals("Incorrect JSON output", expectedJSON, actualJSON);	
		
		reader = new FileReader(JSON_PATH + "/v2/submission/submission_after_cancelled.json");
		expectedJSON = (JSONObject) jsonParser.parse(reader);
		
		data = given()
				.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
				.accept(ContentType.JSON)
			.when()
				.get(API_PATH + "/" + SUBMISSION_ID_TO_CANCEL)
			.then()
				.statusCode(200)
				.extract()
				.asString();
		
		actualJSON = (JSONObject) jsonParser.parse(data);
		fixJsonRandomValueInSinglePackage(actualJSON);
		assertEquals("Incorrect JSON output.", expectedJSON, actualJSON);
	}
	
	@SuppressWarnings("unchecked")
	private void fixJsonRandomValueInSinglePackage(JSONObject actualJSON) {
		String source = ((JSONObject)((JSONObject)actualJSON.get("data")).get("packageBag")).get("source").toString();
		String fixedSource = fixSource(source);
		
		((JSONObject)((JSONObject)actualJSON.get("data")).get("packageBag")).put("source", fixedSource);
	}
	
	private String fixSource(String source) {
		String[] tokens = source.split("/");
		String fixedSource = "";
		
		try {
			for(int i = 0; i < tokens.length; i++) {
				if(i != 5) {
					fixedSource += tokens[i];
				} else {
					fixedSource += "RANDOM_VALUE";
				}
				
				if(i < tokens.length - 1)
					fixedSource += "/";
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			fail("Incorrect source value!");
		}
		
		return fixedSource;
	}

	@Test
	public void patchSubmission_returns422_whenPatchIsMalformed() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/dsdsadsadsa\","
				+ "\"value\":false"
				+ "}"
				+ "]";
		
		testPatchEndpoint(patch, "/v2/malformed_patch.json", "/" + SUBMISSION_ID_TO_ACCEPT, 422, ADMIN_TOKEN);
		testGetEndpoint("/v2/submission/all_submissions_viewed_by_admin.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN);
	}
	
	@Test
	public void patchSubmission_returns422_whenUserIsNotAuthenticated() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/dsdsadsadsa\","
				+ "\"value\":false"
				+ "}"
				+ "]";
		
		testPatchEndpointUnauthenticated(patch, SUBMISSION_ID);		
	}
	
	private JsonObject convertSubmissionJson(JsonObject json) {
		String newSource = "xyz";
		JsonObject data = json.get("data").getAsJsonObject();
		JsonObject packageBag = data.get("packageBag").getAsJsonObject();
		packageBag.remove("source");
		packageBag.addProperty("source", newSource);		
		return json;
	}
	
	private JsonObject convertPackageJson(JsonObject json) {
		String newSource = "xyz";
		JsonObject data = json.get("data").getAsJsonObject();
		data.remove("source");
		data.addProperty("source", newSource);		
		return json;
	}
	
	private JsonObject convertListOfPackages(JsonObject json) {
		String newSource = "xyz";
		JsonObject data = json.get("data").getAsJsonObject();
		JsonArray content = data.getAsJsonArray("content");
		content.forEach(e -> {
			JsonObject packageBag = e.getAsJsonObject();
			packageBag.remove("source");
			packageBag.addProperty("source", newSource);
		});				
		return json;
	}
}

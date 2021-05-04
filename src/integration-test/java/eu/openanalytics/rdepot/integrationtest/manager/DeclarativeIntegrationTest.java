/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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
package eu.openanalytics.rdepot.integrationtest.manager;

import static eu.openanalytics.rdepot.integrationtest.IntegrationTest.ADMIN_TOKEN;
import static eu.openanalytics.rdepot.integrationtest.IntegrationTest.AUTHORIZATION;
import static eu.openanalytics.rdepot.integrationtest.IntegrationTest.BEARER;
import static eu.openanalytics.rdepot.integrationtest.IntegrationTest.JSON_PATH;
import static eu.openanalytics.rdepot.integrationtest.IntegrationTest.REPOSITORYMAINTAINER_TOKEN;
import static eu.openanalytics.rdepot.integrationtest.IntegrationTest.USER_TOKEN;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.restassured.RestAssured;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;

public class DeclarativeIntegrationTest {
	private static final String REPO_NAME_TO_EDIT = "newName";
	private static final String REPO_NAME_TO_CREATE = "testrepo7";
	private static final String API_PATH = "/api/manager";
	
	@BeforeClass
	public static final void configureRestAssured() throws IOException, InterruptedException {
		RestAssured.port = 8021;
		RestAssured.urlEncodingEnabled = false;
	}
	
	@Before
	public final void doBackup() throws IOException, InterruptedException {
		String[] cmd = new String[] {"gradle", "backupDeclarative", "-b","src/integration-test/resources/build.gradle"};
		Process process = Runtime.getRuntime().exec(cmd);
		process.waitFor();
		process.destroy();
	}
	
	@After
	public final void cleanUp() throws IOException, InterruptedException {
		String[] cmd = new String[] {"gradle", "restoreDeclarative", "-b","src/integration-test/resources/build.gradle"};
		Process process = Runtime.getRuntime().exec(cmd);
		process.waitFor();
		process.destroy();
	}
	
	@Test
	public void shouldNotSynchronizeRepositoryWithMirrorWithPackageMaintainerCredentials() {
		final String repositoryId = "7";

		given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/repositories/" + repositoryId + "/synchronize-mirrors")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldSynchronizeRepositoryWithMirror() throws FileNotFoundException, ParseException {
		final String repositoryId = "7";
		
		String response = given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/repositories/" + repositoryId + "/synchronize-mirrors")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonObject actualJson = (JsonObject) JsonParser.parseString(response);
		String expectedString = "Repository synchronization with mirrors started.";
		
		assertEquals("Response message is incorrect", 
				expectedString, actualJson.get("success").getAsString());
		
		await()
			.atMost(180, TimeUnit.SECONDS)
		.with()
			.pollInterval(5, TimeUnit.SECONDS)
			.until(() -> assertSynchronizationFinished(repositoryId));
		
		FileReader reader = new FileReader(JSON_PATH + "/declarative/repositories_after_synchronization.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		assertRepositories(expectedJSON);
	}
	
	private Boolean assertSynchronizationFinished(String repositoryId) {
		String response = given()
				.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
			.when()
				.get(API_PATH + "/repositories/synchronization/status")
			.then()
				.statusCode(200)
				.extract()
				.asString();
				
		JsonArray actualJson = (JsonArray) JsonParser.parseString(response);
		
		assertEquals("Too many synchronizations are taking place "
				+ "or synchronization status is incorrect.", 
				1, actualJson.size());
		
		
		if(actualJson.get(0).getAsJsonObject().get("repositoryId").getAsString().equals(repositoryId) && 
				actualJson.get(0).getAsJsonObject().get("pending").getAsString().equals("false"))
			return true;
		
		return false;
	}
	
	@Test
	public void shouldUploadPackage() throws IOException, ParseException {
		File packageBag = new File ("src/integration-test/resources/itestPackages/A3_0.9.1.tar.gz");
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept("application/json")
			.contentType("multipart/form-data")
			.multiPart("repository", "A")
			.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
					.fileName(packageBag.getName())
					.mimeType("application/gzip")
					.controlName("file")
					.build())
			.when()
				.post(API_PATH + "/packages/submit")
			.then()
				.statusCode(200)
				.extract();
		
		FileReader reader = new FileReader(JSON_PATH + "/declarative/repositories_after_uploading_package.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		assertRepositories(expectedJSON);
	}
	
	private void assertRepositories(JsonArray expectedRepositoriesJson) throws ParseException {
		List<Set<JsonObject>> expectedPackages = convertPackages(expectedRepositoriesJson);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/repositories/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);
		
		List<Set<JsonObject>> actualPackages = convertPackages(actualJSON);

		assertEquals(expectedPackages, actualPackages);
		assertTrue(compareRepositories(expectedRepositoriesJson, actualJSON));
	}
	
	@Test
	public void shouldUploadPackageAndPublishRepository() throws ParseException, IOException {
		File packageBag = new File ("src/integration-test/resources/itestPackages/A3_0.9.1.tar.gz");
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept("application/json")
			.contentType("multipart/form-data")
			.multiPart("repository", "A")
			.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
				.fileName(packageBag.getName())
				.mimeType("application/gzip")
				.controlName("file")
				.build())
		.when()
			.post(API_PATH + "/packages/submit")
		.then()
			.statusCode(200)
			.extract();
		
		given()
			.headers(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/repositories/2/publish")
		.then()
			.statusCode(200)
			.body("success", equalTo("Repository has been published successfully."));
		
		FileReader reader = new FileReader(JSON_PATH + "/declarative/repositories_after_publishing.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		List<Set<JsonObject>> expectedPackages = convertPackages(expectedJSON);
		
		String data = given()
			.headers(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/repositories/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);
		//TODO: are those really packages?
		List<Set<JsonObject>> actualPackages = convertPackages(actualJSON);
	
		assertEquals("Repository publishing caused some changes in packages", expectedPackages, actualPackages);
		assertTrue("Repository hasn't been published", compareRepositories(expectedJSON, actualJSON));
	}
	
	@Test
	public void shouldNotDeleteRepository() {
		given()
			.headers(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/repositories/2/delete")
		.then()
			.statusCode(403);			
	}
	
	@Test
	public void shouldNotCreateRepository() {
		Map<String, String> params = new HashMap<>();
		params.put("name", REPO_NAME_TO_CREATE);
		params.put("publicationUri", "http://localhost/repo/" + REPO_NAME_TO_CREATE);
		params.put("serverAddress", "http://oa-rdepot-repo:8080/" + REPO_NAME_TO_CREATE);
		
		given()
			.headers(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(params)
		.when()
			.post(API_PATH + "/repositories/create")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldNotEditRepository() {
		Map<String, String> params = new HashMap<>();
		params.put("name", REPO_NAME_TO_EDIT);
		params.put("publicationUri", "http://localhost/repo/" + REPO_NAME_TO_EDIT);
		params.put("serverAddress", "http://oa-rdepot-repo:8080/" + REPO_NAME_TO_EDIT);
		
		given()
			.headers(AUTHORIZATION, BEARER + REPOSITORYMAINTAINER_TOKEN)
			.accept(ContentType.JSON)
			.formParams(params)
		.when()
			.post(API_PATH + "/repositories/2/edit")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldUploadPackageToPublishedRepository() throws IOException, ParseException {
		File packageBag = new File ("src/integration-test/resources/itestPackages/A3_0.9.1.tar.gz");
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept("application/json")
			.contentType("multipart/form-data")
			.multiPart("repository", "A")
			.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
				.fileName(packageBag.getName())
				.mimeType("application/gzip")
				.controlName("file")
				.build())
		.when()
			.post(API_PATH + "/packages/submit")
		.then()
			.statusCode(200)
			.extract();
		
		given()
			.headers(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/repositories/2/publish")
		.then()
			.statusCode(200)
			.body("success", equalTo("Repository has been published successfully."));
		
		packageBag = new File ("src/integration-test/resources/itestPackages/visdat_0.1.0.tar.gz");
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept("application/json")
			.contentType("multipart/form-data")
			.multiPart("repository", "A")
			.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
					.fileName(packageBag.getName())
					.mimeType("application/gzip")
					.controlName("file")
					.build())
			.when()
				.post(API_PATH + "/packages/submit")
			.then()
				.statusCode(200)
				.extract();
		
		FileReader reader = new FileReader(JSON_PATH + "/declarative/repositories_after_uploading_package_to_published_repo.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		List<Set<JsonObject>> expectedPackages = convertPackages(expectedJSON);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/repositories/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);
		
		List<Set<JsonObject>> actualPackages = convertPackages(actualJSON);

		assertEquals(expectedPackages, actualPackages);
		assertTrue(compareRepositories(expectedJSON, actualJSON));
	}
	
	@Test
	public void shouldUploadPackageThenPublishAndThenUnpublishRepository() throws ParseException, IOException {
		File packageBag = new File ("src/integration-test/resources/itestPackages/A3_0.9.1.tar.gz");
		
		given()
			.header(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept("application/json")
			.contentType("multipart/form-data")
			.multiPart("repository", "A")
			.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
				.fileName(packageBag.getName())
				.mimeType("application/gzip")
				.controlName("file")
				.build())
		.when()
			.post(API_PATH + "/packages/submit")
		.then()
			.statusCode(200)
			.extract();
		
		given()
			.headers(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/repositories/2/publish")
		.then()
			.statusCode(200)
			.body("success", equalTo("Repository has been published successfully."));
		
		given()
			.headers(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.patch(API_PATH + "/repositories/2/unpublish")
		.then()
			.statusCode(200)
			.body("success", equalTo("Repository has been unpublished successfully."));	
		
		FileReader reader = new FileReader(JSON_PATH + "/declarative/repositories_after_unpublishing.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		List<Set<JsonObject>> expectedPackages = convertPackages(expectedJSON);
		
		String data = given()
			.headers(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/repositories/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);
		
		List<Set<JsonObject>> actualPackages = convertPackages(actualJSON);
	
		assertEquals("Repository unpublishing caused some changes in packages", expectedPackages, actualPackages);
		assertTrue("Repository hasn't been unpublished", compareRepositories(expectedJSON, actualJSON));
	}

	private List<Set<JsonObject>> convertPackages(JsonArray rootJSON) throws ParseException {
	List<Set<JsonObject>> JSON = new ArrayList<>();
				
		for(int i = 0; i < rootJSON.size(); i++) {
			JsonObject repositoryJSON = (JsonObject) rootJSON.get(i);
			JsonArray packagesJSON = (JsonArray) repositoryJSON.get("packages");
			Set<JsonObject> JSONSet = new HashSet<>();
			
			for(int k = 0; k < packagesJSON.size(); k++) {
				JsonObject packageJSON = convertPackageJson(packagesJSON.get(k)).getAsJsonObject();
				JSONSet.add(packageJSON);
			}
			JSON.add(JSONSet);
		}
		return JSON;
	}
	
	
	private boolean compareRepositories(JsonArray expected, JsonArray actual) throws ParseException {		
		if (expected == null || actual == null)
			return false;
		
		if (expected.size() != actual.size())
			return false;
		
		for(int i = 0; i < expected.size(); i++) {
			JsonObject expectedRepository = (JsonObject) expected.get(i);
			JsonObject actualRepository = (JsonObject) actual.get(i);

			expectedRepository.remove("packages");
			actualRepository.remove("packages");
			
			if(!expectedRepository.equals(actualRepository))
				return false;
			
		}
		return true;
	}
	
	private JsonElement convertPackageJson(JsonElement packageJsonElement) {
		JsonObject packageJSON = packageJsonElement.getAsJsonObject();
		String source = packageJSON.get("source").getAsString();
		packageJSON.remove("source");
		packageJSON.remove("id");
		String newSource = source.replaceFirst("/[0-9]{2}[0-9]+", "");
		packageJSON.addProperty("source", newSource);
		
		JsonObject submissionJSON = packageJSON.get("submission").getAsJsonObject();
		packageJSON.remove("submission");
		submissionJSON.remove("id");
		packageJSON.add("submission", submissionJSON);
		
		Gson gson = new Gson();
		
		return gson.fromJson(packageJSON.toString(), JsonElement.class);
	}
}

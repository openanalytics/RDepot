/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

public class PackageIntegrationTest {
	private static final String JSON_PATH = "src/integration-test/resources/JSONs";
	private static final String PACKAGES_PATH = "src/integration-test/resources/itestPackages";
	private static final String PDF_PATH = "src/integration-test/resources/itestPdf";
	private static final String PACKAGE_NAME = "A3";
	private static final String OLDER_VERSION = "0.9.1";
	
	private static final String PACKAGE_NAME_TO_DOWNLOAD = "accrued";
	private static final String PACKAGE_VERSION_TO_DOWNLOAD = "1.3";
	private static final String PACKAGE_ID_TO_DOWNLOAD = "5";
	
	private static final String PACKAGE_OLDER_ID = "9";
	private static final String PACKAGE_INACTIVE_ID = "13";
	
	private static final String API_PATH = "/api/manager/packages";
	private static final String ORDINARY_PATH = "/manager/packages";
	
	private static final String ADMIN_LOGIN = "einstein";
	private static final String REPOSITORYMAINTAINER_LOGIN = "tesla";
	private static final String PACKAGEMAINTAINER_LOGIN = "galieleo";
	private static final String USER_LOGIN = "newton";
	private static final String PASSWORD = "testpassword";
	
	private static final Gson gson = new GsonBuilder()
			.enableComplexMapKeySerialization()
			.create();
	
	@Before
	public void setUp() throws IOException, InterruptedException {
		String[] cmd = new String[] {"gradle", "restore", "-b","src/integration-test/resources/build.gradle"};
		Process process = Runtime.getRuntime().exec(cmd);
		process.waitFor();
		process.destroy();
	}
	
	@BeforeClass
	public static void configureRestAssured() throws IOException, InterruptedException {
		RestAssured.port = 8017;
		RestAssured.urlEncodingEnabled = false;
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnPackages() throws IOException, ParseException {
		
		//FYI: only admin sees the full list of packages
		
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/package/packages.json");
		JSONArray rootJSON = (JSONArray) jsonParser.parse(reader);
		Set<JSONObject> expectedJSON = convert(rootJSON);
		
		String data = given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
			
		rootJSON = (JSONArray) jsonParser.parse(data);

		Set<JSONObject> actualJSON = convert(rootJSON);
		
		assertEquals("Differences in packages", expectedJSON, actualJSON);

	}
	
	@Test
	public void shouldNotReturnPackages() {
		String html = given()
			.accept(ContentType.JSON)
		.when()
			.get(ORDINARY_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		assertTrue("No redirection to login page", html.contains("Please enter your user credentials."));
	}
	
	@Test
	public void shouldReturnDeletedPackages() throws IOException, ParseException {		
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/package/deleted_packages.json");
		JSONArray rootJSON = (JSONArray) jsonParser.parse(reader);
		Set<JSONObject> expectedJSON = convert(rootJSON);
		
		String data = given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/deleted")
		.then()
			.statusCode(200)
			.extract()
			.asString();
			
		rootJSON = (JSONArray) jsonParser.parse(data);

		Set<JSONObject> actualJSON = convert(rootJSON);
		
		assertEquals("Differences in deleted packages", expectedJSON, actualJSON);
	}
	
	@Test
	public void shouldNotReturnDeletedPackages() {
		given()
			.auth()
			.basic(REPOSITORYMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/deleted")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldReturnPackagesEvents() {
		JsonPath expectedJson = new JsonPath(new File(JSON_PATH + "/package/events_" + PACKAGE_NAME + ".json"));
		
		given()
			.auth()
			.basic(PACKAGEMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/" + PACKAGE_OLDER_ID + "/events")
		.then()
			.statusCode(200)
			.body("", equalTo(expectedJson.getMap("")));
	}
	
	@Test
	public void shouldNotReturnPackagesEvents() {
		given()
			.auth()
			.basic(USER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/" + PACKAGE_OLDER_ID + "/events")
		.then()
			.statusCode(403);
	}
	
	@Test
	public void shouldDownloadPackage() throws IOException {		
		byte[] pkg = given()
					.auth()
					.basic(ADMIN_LOGIN, PASSWORD)
					.accept(ContentType.ANY)
				.when()
	            	.get(API_PATH + "/" + PACKAGE_ID_TO_DOWNLOAD + "/download/" + PACKAGE_NAME_TO_DOWNLOAD + "_" + PACKAGE_VERSION_TO_DOWNLOAD + ".tar.gz")
	            .then()
	            	.statusCode(200)
	            	.extract()
	            	.asByteArray();

		File file = new File(PACKAGES_PATH + "/" + PACKAGE_NAME_TO_DOWNLOAD + "_" + PACKAGE_VERSION_TO_DOWNLOAD + ".tar.gz");
		
		byte[] expectedpkg = readFileToByteArray(file);
		
		assertArrayEquals("Wrong package has been downloaded", expectedpkg, pkg);
	}

	@Test
	public void shouldDownloadPdf() throws IOException {
		
		byte[] pdf = given()
				.auth()
				.basic(ADMIN_LOGIN, PASSWORD)
				.accept(ContentType.ANY)
				
			.when()
            	.get(API_PATH + "/" + PACKAGE_ID_TO_DOWNLOAD +"/download/" + PACKAGE_NAME_TO_DOWNLOAD + ".pdf")
			.then()
				.statusCode(200)
				.extract()
				.asByteArray();
		
		String content = extractContent(pdf);
		
		File file = new File(PDF_PATH + "/" + PACKAGE_NAME_TO_DOWNLOAD + ".pdf");
		byte[] expectedpdf = readFileToByteArray(file);
	
		assertTrue(content.contains(PACKAGE_NAME_TO_DOWNLOAD));
		assertTrue(content.contains("Version " + PACKAGE_VERSION_TO_DOWNLOAD));
		assertArrayEquals("Meta-data can cause some differences", expectedpdf, pdf);
//		TODO: Meta-data?
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void shouldActivatePackage() throws IOException, ParseException {
		given()
			.auth()
			.basic(PACKAGEMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.put(API_PATH + "/" + PACKAGE_INACTIVE_ID + "/activate")
		.then()
			.statusCode(200)
			.body("success", equalTo("Package activated successfully."));
	
		JSONParser jsonParser = new JSONParser();
	
		FileReader reader = new FileReader(JSON_PATH + "/package/activated_package.json");
		JSONArray rootJSON = (JSONArray) jsonParser.parse(reader);
		Set<JSONObject> expectedJSON = convert(rootJSON);
		
		String data = given()
			.auth()
			.basic(PACKAGEMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
			
		rootJSON = (JSONArray) jsonParser.parse(data);
	
		Set<JSONObject> actualJSON = convert(rootJSON);
		
		assertEquals("Package hasn't been activated or package maintainer can't do it", 
				expectedJSON, actualJSON);
	}
	
	@Test
	public void shouldNotActivatePackage() {
		given()
			.auth()
			.basic(USER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.put(API_PATH + "/" + PACKAGE_INACTIVE_ID + "/activate")
		.then()
			.statusCode(403);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void shouldDeactivatePackage() throws IOException, ParseException {
		given()
			.auth()
			.basic(PACKAGEMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.put(API_PATH + "/" + PACKAGE_OLDER_ID + "/deactivate")
		.then()
			.statusCode(200)
			.body("success", equalTo("Package deactivated successfully."));
	
		JSONParser jsonParser = new JSONParser();
		
		FileReader reader = new FileReader(JSON_PATH + "/package/deactivated_package.json");
		JSONArray rootJSON = (JSONArray) jsonParser.parse(reader);
		Set<JSONObject> expectedJSON = convert(rootJSON);
		
		String data = given()
			.auth()
			.basic(PACKAGEMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
			
		rootJSON = (JSONArray) jsonParser.parse(data);
	
		Set<JSONObject> actualJSON = convert(rootJSON);
		
		assertEquals("Package hasn't been activated or package maintainer hasn't got privileges to list of packages",
				expectedJSON, actualJSON);
	}
	
	@Test
	public void shouldNotDeactivatePackage() {
		given()
			.auth()
			.basic(USER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.put(API_PATH + "/" + PACKAGE_OLDER_ID + "/deactivate")
		.then()
			.statusCode(403);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void shouldDeletePackage() throws IOException, ParseException {
		given()
			.auth()
			.basic(PACKAGEMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + PACKAGE_OLDER_ID + "/delete")
		.then()
			.statusCode(200)
			.body("success", equalTo("Package deleted successfully."));
	
		JSONParser jsonParser = new JSONParser();
	
		FileReader reader = new FileReader(JSON_PATH + "/package/deleted_packages_with_new_one.json");
		JSONArray rootJSON = (JSONArray) jsonParser.parse(reader);
		Set<JSONObject> expectedJSONDeleted = convert(rootJSON);
		
		String data = given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/deleted")
		.then()
			.statusCode(200)
			.extract()
			.asString();
			
		rootJSON = (JSONArray) jsonParser.parse(data);
	
		Set<JSONObject> actualJSONDeleted = convert(rootJSON);
		
		reader = new FileReader(JSON_PATH + "/package/packages_without_deleted_one.json");
		rootJSON = (JSONArray) jsonParser.parse(reader);
		Set<JSONObject> expectedJSON = convert(rootJSON);
		
		data = given()
			.auth()
			.basic(PACKAGEMAINTAINER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.get(API_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
			
		rootJSON = (JSONArray) jsonParser.parse(data);
	
		Set<JSONObject> actualJSON = convert(rootJSON);
		
		assertEquals("Package hasn't been added to the list of deleted packages", expectedJSONDeleted, actualJSONDeleted);
		assertEquals("Package hasn't been removed from the list of packages", expectedJSON, actualJSON);
	}
	
	@Test
	public void shouldNotDeletePackage() {
		given()
			.auth()
			.basic(USER_LOGIN, PASSWORD)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PATH + "/" + PACKAGE_OLDER_ID + "/delete")
		.then()
			.statusCode(403);
	}
	
	private static String extractContent(byte[] pdf) throws IOException {
	    PDDocument document = PDDocument.load(new ByteArrayInputStream(pdf));
	    try {
	         return new PDFTextStripper().getText(document);
	     } finally {
	    	 document.close();
	     }
	}
	
	private static byte[] readFileToByteArray(File file){
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
	
	private Set<JSONObject> convert(JSONArray rootJSON) throws ParseException {
		Set<JSONObject> JSON = new HashSet<>();
		
		for(int i = 0; i < rootJSON.size(); i++) {
			JSONObject objJSON = (JSONObject) rootJSON.get(i);
			objJSON.remove("lastLoggedInOn");
			JSON.add(objJSON);
		}
		
		return JSON;
	 }
}
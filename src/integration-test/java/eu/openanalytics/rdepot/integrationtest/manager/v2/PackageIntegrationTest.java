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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class PackageIntegrationTest extends IntegrationTest {
	private final String EXAMPLE_PACKAGE_ID = "17";
	private final String DELETED_PACKAGE_ID = "14";
	private final int GET_ENDPOINT_NEW_EVENTS_AMOUNT = 0;
	private final int PATCH_ENDPOINT_NEW_EVENTS_AMOUNT = 1;
	private final int DELETE_ENDPOINT_NEW_EVENTS_AMOUNT = -8;
	
	@BeforeClass
	public static final void configureRestAssured() throws IOException, InterruptedException {
		RestAssured.port = 8017;
		RestAssured.urlEncodingEnabled = false;
	}
	
	public PackageIntegrationTest() {
		super("/api/v2/manager/r/packages");
	}
	
	@Test
	public void getAllPackages() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/package/list_of_packages.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getOnlyFirstTwoPackagesFromSecondPage() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/package/list_of_two_packages.json", 
				"?page=1&size=2&sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllDeletedPackages_asAdmin() throws Exception {	
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/package/list_of_all_deleted_packages.json", 
				"?deleted=true&sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllDeletedPackages_asUser() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/package/list_of_all_deleted_packages.json", 
				"?deleted=true&sort=id,asc", 200, USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllPackages_byRepositoryName() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/package/list_of_all_packages_testrepo3.json", 
				"?repositoryName=testrepo3&sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllDeletedPackages_byRepositoryName() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/package/empty_list.json", 
				"?repositoryName=testrepo3&deleted=true", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllPackages_asUnauthenticated() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET_UNAUTHENTICATED,"", GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getPackage() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET,"/v2/package/example_package.json", 
				"/" + EXAMPLE_PACKAGE_ID, 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getPackage_returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET_UNAUTHENTICATED,"/" + EXAMPLE_PACKAGE_ID, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getPackage_returns404_whenPackageIsNotFound() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET,"/v2/package/notfound.json", "/1234567", 404, 
				ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getDeletedPackage_asAdmin() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET,"/v2/package/deleted_package.json", "/" + DELETED_PACKAGE_ID, 200, 
				ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getDeletedPackage_asUser() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET,"/v2/package/deleted_package.json", "/" + DELETED_PACKAGE_ID, 200, 
				USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void activatePackage() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/active\","
				+ "\"value\":false"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH, "/v2/package/patched_package.json",  "/" + EXAMPLE_PACKAGE_ID, 200, 
				ADMIN_TOKEN, PATCH_ENDPOINT_NEW_EVENTS_AMOUNT, "/v2/events/packages/activate_package_event.json", patch);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET,"/v2/package/package_after_patch.json", "/" + EXAMPLE_PACKAGE_ID, 200, 
				ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void activatePackage_returns404_whenPackageIsNotFound() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/active\","
				+ "\"value\":false"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH,  "/v2/package/notfound.json", "/1321321321", 404, 
				ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, patch);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET,"/v2/package/list_of_packages.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void activatePackage_returns403_whenUserIsNotAllowed() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/active\","
				+ "\"value\":false"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH,  "/v2/403.json", "/" + EXAMPLE_PACKAGE_ID, 403, 
				USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, patch);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET,"/v2/package/list_of_packages.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void activatePackage_returns403_whenMaintainerIsDeleted() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/active\","
				+ "\"value\":false"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH,  "/v2/403.json", "/" + 15, 403, 
				PACKAGEMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, patch);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET,"/v2/package/list_of_packages.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void activatePackage_returns401_whenUserIsNotAuthenticated() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/active\","
				+ "\"value\":false"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH_UNAUTHENTICATED, "/" + EXAMPLE_PACKAGE_ID, 
				GET_ENDPOINT_NEW_EVENTS_AMOUNT, patch);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET,"/v2/package/list_of_packages.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
		
	}
	
	@Test
	public void patchPackage_returns422_whenPatchIsMalformed() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/dsdsadsadsa\","
				+ "\"value\":false"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH,  "/v2/malformed_patch.json", "/" + EXAMPLE_PACKAGE_ID, 422, 
				ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, patch);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package/list_of_packages.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void patchPackage_returns422_whenPatchIsForbidden() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/name\","
				+  "\"value\":\"newName\""
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH,  "/v2/package/forbidden_update.json", "/" + EXAMPLE_PACKAGE_ID, 422, 
				ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, patch);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package/list_of_packages.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void patchPackage_returns422_whenValidationFails() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/name\","
				+ "\"value\":\"\""
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH,   "/v2/package/package_validation_error.json", "/" + EXAMPLE_PACKAGE_ID, 422, 
				ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, patch);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package/list_of_packages.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void shiftDeletePackage() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.DELETE,   "/" + DELETED_PACKAGE_ID, 204, 
				ADMIN_TOKEN, DELETE_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package/notfound.json", "/" + DELETED_PACKAGE_ID, 404, 
				ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void shiftDeletePackage_returns403_whenUserIsNotAdmin() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.DELETE,   "/" + DELETED_PACKAGE_ID, 403, 
				USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package/list_of_packages.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void shiftDeletePackage_returns404_whenPackageIsNotFound() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.DELETE,   "/" + EXAMPLE_PACKAGE_ID, 404, 
				ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package/list_of_packages.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getManual() throws Exception {
		byte[] data = given()
				.header(AUTHORIZATION, BEARER + USER_TOKEN)
				.accept(ContentType.BINARY)
			.when()
				.get(apiPath + "/17/manual")
			.then()
				.statusCode(200)
				.extract()
				.asByteArray();

		assertTrue("Returned manual is incorrect.", extractContent(data).contains("Version 0.9.2"));
	}
	
	@Test
	public void getVignette() throws Exception {
		byte[] actual = given()
				.header(AUTHORIZATION, BEARER + USER_TOKEN)
				.accept(MediaType.APPLICATION_PDF_VALUE)
			.when()
				.get(apiPath + "/25/vignettes/usl.pdf")
			.then()
				.statusCode(200)
				.extract()
			.asByteArray();
		
		byte[] expected = FileUtils.readFileToByteArray(
				new File("src/integration-test/resources/itestPdf/usl.pdf"));
		assertTrue("Returned vignette is incorrect.", Arrays.equals(actual, expected));
	}
	
	@Test
	public void getVignetteLinks() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET,  "/v2/package/vignettes.json", "/25/vignettes", 200, 
				USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
}

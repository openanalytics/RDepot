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
package eu.openanalytics.rdepot.integrationtest.manager.v2.python;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.openanalytics.rdepot.integrationtest.manager.v2.IntegrationTest;
import eu.openanalytics.rdepot.integrationtest.manager.v2.RequestType;
import eu.openanalytics.rdepot.integrationtest.manager.v2.TestRequestBody;
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.PackageTestData;
import io.restassured.http.ContentType;

public class PythonPackageIntegrationTest extends IntegrationTest {

    private final PackageTestData testData;
    private static String PACKAGES_PATH = "/v2/python/packages/";
    private static String EVENTS_PATH = "/v2/python/events/packages/";
    private static String PACKAGE_ID_TO_DOWNLOAD = "41";
    private static String PACKAGE_NAME_TO_DOWNLOAD = "pandas";
    private static String PACKAGE_VERSION_TO_DOWNLOAD = "2.0.1";

    private static final String API_PATH = "/api/v2/manager/python/packages";

    public PythonPackageIntegrationTest() {
        super(API_PATH);
        this.testData = PackageTestData.builder()
                .technologies(Arrays.asList("Python"))
                .examplePackageId("41")
                .deletedPackageId("42")
                .getEndpointNewEventsAmount(0)
                .deleteEndpointNewEventsAmount(-1)
                .changeEndpointNewEventsAmount(1)
                .toBeActivatedPackageId("38")
                .build();
    }
    
    @Test
    public void getAllPackages() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(PACKAGES_PATH + "list_of_packages.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getOnlyFirstTwoPackagesFromSecondPage() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?page=1&size=2&sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(PACKAGES_PATH + "list_of_two_packages.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getAllDeletedPackages_asAdmin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?deleted=true&sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(PACKAGES_PATH + "list_of_all_deleted_packages_as_admin.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getAllDeletedPackages_asUser() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?deleted=true&sort=id,asc")
                .statusCode(200)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(PACKAGES_PATH + "list_of_all_deleted_packages.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getAllPackages_asUnauthenticated() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_UNAUTHENTICATED)
                .urlSuffix("")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getPackage() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getExamplePackageId())
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(PACKAGES_PATH + "example_package.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getPackage_returns401_whenUserIsNotAuthenticated() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_UNAUTHENTICATED)
                .urlSuffix("/" + testData.getExamplePackageId())
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getPackage_returns404_whenPackageIsNotFound() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/1234567")
                .statusCode(404)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/packages/404.json")
                .build();
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
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getExamplePackageId())
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedEventsJson(EVENTS_PATH + "activate_package_event.json")
                .expectedJsonPath(PACKAGES_PATH + "patched_package.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getExamplePackageId())
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(PACKAGES_PATH + "package_after_patch.json")
                .build();
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

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix( "/" + testData.getToBeActivatedPackageId())
                .statusCode(403)
                .token(PACKAGEMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/403.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(PACKAGES_PATH + "list_of_packages.json")
                .build();
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

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH_UNAUTHENTICATED)
                .urlSuffix( "/" + testData.getExamplePackageId())
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .body(patch)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(PACKAGES_PATH + "list_of_packages.json")
                .build();
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

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix( "/" + testData.getExamplePackageId())
                .statusCode(403)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/403.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(PACKAGES_PATH + "list_of_packages.json")
                .build();
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

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/1321321321")
                .statusCode(404)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/packages/404.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(PACKAGES_PATH + "list_of_packages.json")
                .build();
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

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix( "/" + testData.getExamplePackageId())
                .statusCode(422)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/malformed_patch.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(PACKAGES_PATH + "list_of_packages.json")
                .build();
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

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix( "/" + testData.getExamplePackageId())
                .statusCode(422)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/packages/forbidden_update.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(PACKAGES_PATH + "list_of_packages.json")
                .build();
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

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix( "/" + testData.getExamplePackageId())
                .statusCode(422)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/packages/package_validation_error.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(PACKAGES_PATH + "list_of_packages.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void shiftDeletePackage() throws Exception {

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE)
                .urlSuffix( "/" + testData.getDeletedPackageId())
                .statusCode(204)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getDeleteEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getDeletedPackageId())
                .statusCode(404)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/packages/404.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void shiftDeletePackage_returns403_whenUserIsNotAdmin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE)
                .urlSuffix( "/" + testData.getDeletedPackageId())
                .statusCode(403)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath( PACKAGES_PATH + "list_of_packages.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void shiftDeletePackage_returns404_whenPackageIsNotFound() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE)
                .urlSuffix( "/" + testData.getExamplePackageId())
                .statusCode(404)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(PACKAGES_PATH + "list_of_packages.json")
                .build();
        testEndpoint(requestBody);
    }

//	@Test
//	public void getManual() throws Exception {
//		byte[] data = given()
//				.header(AUTHORIZATION, BEARER + USER_TOKEN)
//				.accept(ContentType.BINARY)
//			.when()
//				.get(apiPath + "/17/manual")
//			.then()
//				.statusCode(200)
//				.extract()
//				.asByteArray();
//
//		assertTrue("Returned manual is incorrect.", extractContent(data).contains("Version 0.9.2"));
//	}
//	

    @Test
	public void downloadPackage() throws Exception {		
		byte[] pkg = given()
					.header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
					.accept(ContentType.ANY)
				.when()
	            	.get(apiPath + "/" + PACKAGE_ID_TO_DOWNLOAD + "/download/" + PACKAGE_NAME_TO_DOWNLOAD + "-" + PACKAGE_VERSION_TO_DOWNLOAD + ".tar.gz")
	            .then()
	            	.statusCode(200)
	            	.extract()
	            	.asByteArray();

		File file = new File("src/test/resources/itestPackages/" + PACKAGE_NAME_TO_DOWNLOAD + "-" + PACKAGE_VERSION_TO_DOWNLOAD + ".tar.gz");
		
		byte[] expectedpkg = readFileToByteArray(file);
						
		Assertions.assertArrayEquals(expectedpkg, pkg, "Wrong package has been downloaded");
	}
    
}

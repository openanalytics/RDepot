/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
package eu.openanalytics.rdepot.integrationtest.manager.v2.r;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.openanalytics.rdepot.integrationtest.environment.BashScriptExecutor;
import eu.openanalytics.rdepot.integrationtest.manager.v2.IntegrationTest;
import eu.openanalytics.rdepot.integrationtest.manager.v2.RequestType;
import eu.openanalytics.rdepot.integrationtest.manager.v2.TestRequestBody;
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.PackageTestData;
import io.restassured.http.ContentType;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.surefire.shared.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class RPackageIntegrationTest extends IntegrationTest {

    private final PackageTestData testData;
    private static final String PACKAGES_PATH = "/v2/r/packages/";
    private static final String EVENTS_PATH = "/v2/r/events/packages/";
    private static final String PACKAGE_NAME_TO_DOWNLOAD = "accrued";
    private static final String PACKAGE_VERSION_TO_DOWNLOAD = "1.3";
    private static final String PACKAGE_ID_TO_DOWNLOAD = "5";
    private static final BashScriptExecutor bashScriptExecutor = new BashScriptExecutor();

    public RPackageIntegrationTest() {
        super("/api/v2/manager/r/packages");
        this.testData = PackageTestData.builder()
                .technologies(List.of("r"))
                .examplePackageId("17")
                .deletedPackageId("14")
                .getEndpointNewEventsAmount(0)
                .deleteEndpointNewEventsAmount(-8)
                .changeEndpointNewEventsAmount(1)
                .toBeActivatedPackageId("15")
                .submissionStates(List.of("waiting"))
                .search("bench")
                .repositories(List.of("testrepo4"))
                .maintainer(Arrays.asList("Nikola%20Tesla", "Galileo%20Galilei"))
                .binary(true)
                .build();
    }

    @Test
    public void getPackagesByNameSearching() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(USER_TOKEN)
                .statusCode(200)
                .urlSuffix("?search=" + testData.getSearch() + "&sort=id,desc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/r/packages/packages_searching.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getPackagesByMaintainers() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(USER_TOKEN)
                .statusCode(200)
                .urlSuffix("?maintainer=" + testData.getMaintainer().get(0) + ","
                        + testData.getMaintainer().get(1) + "&sort=id,desc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/r/packages/packages_by_maintainers.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getPackagesByRepositoriesAndSubmissionState() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(USER_TOKEN)
                .statusCode(200)
                .urlSuffix("?repository=" + testData.getRepositories().get(0)
                        + "&submissionState=" + testData.getSubmissionStates().get(0)
                        + "&sort=id,desc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/r/packages/packages_by_repositories_and_submission_state.json")
                .build();
        testEndpoint(requestBody);
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
    public void getBinaryPackages() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(USER_TOKEN)
                .statusCode(200)
                .urlSuffix("?binary=" + testData.isBinary() + "&sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/r/packages/binary_packages.json")
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
                .expectedJsonPath(PACKAGES_PATH + "list_of_all_deleted_packages.json")
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
                .expectedJsonPath(PACKAGES_PATH + "list_of_all_deleted_packages_as_user.json")
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
    public void getDeletedPackage_asAdmin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getDeletedPackageId())
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(PACKAGES_PATH + "deleted_package.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getDeletedPackage_asUser() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getDeletedPackageId())
                .statusCode(404)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/packages/404.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getPackagesNotMaintainedBy() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .token(USER_TOKEN)
                .statusCode(200)
                .urlSuffix("?notMaintainedBy=" + testData.getMaintainer().get(1))
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(PACKAGES_PATH + "packages_not_maintained_by.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void activatePackage() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/active\"," + "\"value\":false" + "}" + "]";
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getExamplePackageId())
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedEventsJson(EVENTS_PATH + "activate_r_package_event.json")
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
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/active\"," + "\"value\":false" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getToBeActivatedPackageId())
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
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/active\"," + "\"value\":false" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH_UNAUTHENTICATED)
                .urlSuffix("/" + testData.getExamplePackageId())
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
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/active\"," + "\"value\":false" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getExamplePackageId())
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
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/active\"," + "\"value\":false" + "}" + "]";

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
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/dsdsadsadsa\"," + "\"value\":false" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getExamplePackageId())
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
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/name\"," + "\"value\":\"newName\"" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getExamplePackageId())
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
        final String patch = "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/name\"," + "\"value\":\"\"" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getExamplePackageId())
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
                .urlSuffix("/" + testData.getDeletedPackageId())
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
                .urlSuffix("/" + testData.getDeletedPackageId())
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
                .expectedJsonPath(PACKAGES_PATH + "list_of_packages.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void shiftDeletePackage_returns404_whenPackageIsNotFound() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE)
                .urlSuffix("/" + testData.getExamplePackageId())
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

    @Test
    public void getManual() throws Exception {
        byte[] data = given().header(AUTHORIZATION, BASIC + USER_TOKEN)
                .accept(ContentType.BINARY)
                .when()
                .get(apiPath + "/17/manual")
                .then()
                .statusCode(200)
                .extract()
                .asByteArray();

        Assertions.assertTrue(extractContent(data).contains("Version 0.9.2"), "Returned manual is incorrect.");
    }

    @Test
    public void getVignette() throws Exception {
        byte[] actual = given().header(AUTHORIZATION, BASIC + USER_TOKEN)
                .accept(MediaType.APPLICATION_PDF_VALUE)
                .when()
                .get(apiPath + "/25/vignettes/usl.pdf")
                .then()
                .statusCode(200)
                .extract()
                .asByteArray();

        byte[] expected = FileUtils.readFileToByteArray(new File("src/test/resources/itestPdf/usl.pdf"));
        Assertions.assertArrayEquals(actual, expected, "Returned vignette is incorrect.");
    }

    @Test
    public void getVignetteLinks() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_ARRAY)
                .urlSuffix("/25/vignettes")
                .statusCode(200)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(PACKAGES_PATH + "vignettes.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void downloadPackage() {
        byte[] pkg = given().header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                .accept(ContentType.ANY)
                .when()
                .get(apiPath + "/" + PACKAGE_ID_TO_DOWNLOAD + "/download/" + PACKAGE_NAME_TO_DOWNLOAD + "_"
                        + PACKAGE_VERSION_TO_DOWNLOAD + ".tar.gz")
                .then()
                .statusCode(200)
                .extract()
                .asByteArray();

        File file = new File("src/test/resources/itestPackages/" + PACKAGE_NAME_TO_DOWNLOAD + "_"
                + PACKAGE_VERSION_TO_DOWNLOAD + ".tar.gz");

        byte[] expectedpkg = readFileToByteArray(file);

        Assertions.assertArrayEquals(expectedpkg, pkg, "Wrong package has been downloaded");
    }

    @Test
    public void downloadSourcePackage() throws Exception {

        String targetDirectoryName = "src/test/resources/downloading/";

        createDownloadTestFolder(targetDirectoryName);

        bashScriptExecutor.executeBashCommand(
                "curl http://localhost:8017/repo/testrepo1/src/contrib/A3_0.9.2.tar.gz  --output " + targetDirectoryName
                        + "A3_0.9.2.tar.gz");
        File targetDirectory = new File(targetDirectoryName);
        File[] files = targetDirectory.listFiles();
        assertNotNull(files);
        assertEquals(1, files.length, "There is no package in the folder");

        byte[] actual = FileUtils.readFileToByteArray(files[0]);

        byte[] expected = FileUtils.readFileToByteArray(new File("src/test/resources/itestPackages/A3_0.9.2.tar.gz"));

        assertEquals("A3_0.9.2.tar.gz", files[0].getName(), "Package A3 of version 0.9.2 should be downloaded");
        assertArrayEquals(actual, expected, "Downloaded package is incorrect.");

        cleanAfterDownloading(targetDirectoryName);
    }

    @Test
    public void downloadBinaryPackage() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/published\"," + "\"value\":\"true\"" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH_OTHER_RESOURCE)
                .path("/api/v2/manager/r/repositories")
                .urlSuffix("/5")
                .statusCode(200)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedJsonPath("/v2/r/repositories/published_repository_with_binary.json")
                .expectedEventsJson("/v2/r/events/repositories/patched_published_repository_with_binary_event.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);

        String targetDirectoryName = "src/test/resources/downloading/";

        createDownloadTestFolder(targetDirectoryName);

        bashScriptExecutor.executeBashCommand("curl -H \"User-Agent: R (4.2.0 x86_64-pc-linux-gnu x86_64 linux-gnu)\""
                + " http://localhost:8017/repo/testrepo4/linux/centos7/src/contrib/arrow_8.0.0.tar.gz  --output "
                + targetDirectoryName + "arrow_8.0.0.tar.gz");

        File targetDirectory = new File(targetDirectoryName);
        File[] files = targetDirectory.listFiles();

        assertNotNull(files);
        byte[] actual = FileUtils.readFileToByteArray(files[0]);

        byte[] expected =
                FileUtils.readFileToByteArray(new File("src/test/resources/itestPackages/arrow_8.0.0.tar.gz"));

        assertEquals(1, files.length, "There is no package in the folder");
        assertEquals("arrow_8.0.0.tar.gz", files[0].getName(), "Package arrow of version 8.0.0 should be downloaded");
        assertArrayEquals(actual, expected, "Downloaded package is incorrect.");

        cleanAfterDownloading(targetDirectoryName);
    }

    @Test
    public void downloadSourcePackage_whenBinaryIsMissing() throws Exception {
        String targetDirectoryName = "src/test/resources/downloading/";

        createDownloadTestFolder(targetDirectoryName);

        bashScriptExecutor.executeBashCommand("curl -H \"User-Agent: R (4.4.0 x86_64-pc-linux-gnu x86_64 linux-gnu)\""
                + " http://localhost:8017/repo/testrepo1/linux/jammy/src/contrib/A3_0.9.2.tar.gz  --output "
                + targetDirectoryName + "A3_0.9.2.tar.gz");

        File targetDirectory = new File(targetDirectoryName);
        File[] files = targetDirectory.listFiles();

        assertNotNull(files);
        byte[] actual = FileUtils.readFileToByteArray(files[0]);

        byte[] expected = FileUtils.readFileToByteArray(new File("src/test/resources/itestPackages/A3_0.9.2.tar.gz"));

        assertEquals(1, files.length, "There is no package in the folder");
        assertEquals("A3_0.9.2.tar.gz", files[0].getName(), "Package A3 of version 0.9.2 should be downloaded");
        assertArrayEquals(actual, expected, "Downloaded package is incorrect.");

        cleanAfterDownloading(targetDirectoryName);
    }
}

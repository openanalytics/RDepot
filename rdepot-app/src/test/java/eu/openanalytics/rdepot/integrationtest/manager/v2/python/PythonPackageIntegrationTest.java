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
package eu.openanalytics.rdepot.integrationtest.manager.v2.python;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

import eu.openanalytics.rdepot.integrationtest.environment.BashScriptExecutor;
import eu.openanalytics.rdepot.integrationtest.manager.v2.IntegrationTest;
import eu.openanalytics.rdepot.integrationtest.manager.v2.RequestType;
import eu.openanalytics.rdepot.integrationtest.manager.v2.TestRequestBody;
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.PackageTestData;
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.SubmissionMultipartBody;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.surefire.shared.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PythonPackageIntegrationTest extends IntegrationTest {

    private final PackageTestData testData;
    private static final String PACKAGES_PATH = "/v2/python/packages/";
    private static final String EVENTS_PATH = "/v2/python/events/packages/";
    private static final String PACKAGE_ID_TO_DOWNLOAD = "41";
    private static final String PACKAGE_NAME_TO_DOWNLOAD = "pandas";
    private static final String PACKAGE_VERSION_TO_DOWNLOAD = "2.0.1";
    private static final BashScriptExecutor bashScriptExecutor = new BashScriptExecutor();
    private static final String API_PATH = "/api/v2/manager/python/packages";

    public PythonPackageIntegrationTest() {
        super(API_PATH);
        this.testData = PackageTestData.builder()
                .examplePackageId("41")
                .deletedPackageId("42")
                .getEndpointNewEventsAmount(0)
                .deleteEndpointNewEventsAmount(-1)
                .changeEndpointNewEventsAmount(1)
                .toBeActivatedPackageId("38")
                .submissionStates(List.of("waiting"))
                .search("numpy")
                .repositories(List.of("testrepo8"))
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
                .expectedJsonPath("/v2/python/packages/packages_searching.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getBinaryPackages() throws Exception {
        final File packageBag = new File(
                "src/test/resources/itestPackages/tetrapolyscope-0.0.1-cp39-cp39-manylinux_2_17_x86_64.manylinux2014_x86_64.whl");
        final SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo8",
                true,
                "",
                new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/octet-stream")
                        .controlName("file")
                        .build(),
                true);

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .path("/api/v2/manager/python/submissions")
                .statusCode(201)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .submissionMultipartBody(body)
                .expectedJsonPath("/v2/python/submissions/new_binary_submission.json")
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_RESOURCE_AFTER_SUBMISSION)
                .urlSuffix("?binary=" + testData.isBinary() + "&sort=id,asc")
                .statusCode(200)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/packages/binary_packages.json")
                .ignoreSource(true)
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
                .expectedJsonPath("/v2/python/packages/packages_by_maintainers.json")
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
                .expectedJsonPath("/v2/python/packages/packages_by_repositories_and_submission_state.json")
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
    public void deactivatePackage() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/active\"," + "\"value\":false" + "}" + "]";
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getExamplePackageId())
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedEventsJson(EVENTS_PATH + "deactivate_package_event.json")
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
    public void downloadPackage() {
        byte[] pkg = given().header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                .accept(ContentType.ANY)
                .when()
                .get(apiPath + "/" + PACKAGE_ID_TO_DOWNLOAD + "/download/" + PACKAGE_NAME_TO_DOWNLOAD + "-"
                        + PACKAGE_VERSION_TO_DOWNLOAD + ".tar.gz")
                .then()
                .statusCode(200)
                .extract()
                .asByteArray();

        File file = new File("src/test/resources/itestPackages/" + PACKAGE_NAME_TO_DOWNLOAD + "-"
                + PACKAGE_VERSION_TO_DOWNLOAD + ".tar.gz");

        byte[] expectedPackage = readFileToByteArray(file);

        Assertions.assertArrayEquals(expectedPackage, pkg, "Wrong package has been downloaded");
    }

    @Test
    public void downloadSourcePackage() throws Exception {

        String targetDirectoryName = "src/test/resources/downloading/";

        createDownloadTestFolder(targetDirectoryName);
        bashScriptExecutor.executeBashCommand(
                "curl http://localhost:8017/repo/testrepo8/pandas/pandas-2.0.1.tar.gz --output " + targetDirectoryName
                        + "pandas-2.0.1.tar.gz");

        File targetDirectory = new File(targetDirectoryName);
        File[] files = targetDirectory.listFiles();

        Assertions.assertNotNull(files);
        byte[] actual = FileUtils.readFileToByteArray(files[0]);

        byte[] expected =
                FileUtils.readFileToByteArray(new File("src/test/resources/itestPackages/pandas-2.0.1.tar.gz"));

        assertEquals(1, files.length, "There is no package in the folder");
        assertEquals("pandas-2.0.1.tar.gz", files[0].getName(), "Package pandas of version 2.0.1 should be downloaded");
        assertArrayEquals(expected, actual, "Downloaded package is incorrect.");

        cleanAfterDownloading(targetDirectoryName);
    }

    @Test
    public void getPackage_whenDeletingPackagesDisabled() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getExamplePackageId())
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath(PACKAGES_PATH + "package_as_admin.json")
                .build();
        testEndpoint(requestBody);

        changeConfigAndTest(
                Paths.get("src/test/resources/docker/app/test_configs/test_simple_config.yml"),
                Paths.get(
                        "src/test/resources/docker/app/test_configs/test_simple_deleting_repos_and_packages_disabled.yml"),
                () -> {
                    TestRequestBody body = TestRequestBody.builder()
                            .requestType(RequestType.GET)
                            .urlSuffix("/" + testData.getExamplePackageId())
                            .statusCode(200)
                            .token(ADMIN_TOKEN)
                            .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                            .expectedJsonPath(PACKAGES_PATH + "package_deleting_packages_disabled.json")
                            .build();
                    try {
                        testEndpoint(body);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}

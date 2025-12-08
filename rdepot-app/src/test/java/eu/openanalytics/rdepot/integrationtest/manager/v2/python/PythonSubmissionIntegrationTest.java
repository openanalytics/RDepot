/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.openanalytics.rdepot.integrationtest.environment.ExecutionResult;
import eu.openanalytics.rdepot.integrationtest.manager.v2.IntegrationTest;
import eu.openanalytics.rdepot.integrationtest.manager.v2.RequestType;
import eu.openanalytics.rdepot.integrationtest.manager.v2.TestRequestBody;
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.SubmissionMultipartBody;
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.SubmissionTestData;
import io.restassured.builder.MultiPartSpecBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.Arrays;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

public class PythonSubmissionIntegrationTest extends IntegrationTest {

    private final SubmissionTestData testData;
    private static final String EVENTS_PATH = "/v2/python/events/submissions/";
    private static final int DOWNLOADED_FROM_PYPI_EXIT_CODE = 123;

    public PythonSubmissionIntegrationTest() {
        super("/api/v2/manager/python/submissions");
        this.testData = SubmissionTestData.builder()
                .apiPackagesPath("/api/v2/manager/python/packages")
                .submissionId("40")
                .submissionIdToAccept("45")
                .submissionIdToCancel("46")
                .submissionIdAccepted("37")
                .packageId("33")
                .states(Arrays.asList("waiting", "cancelled"))
                .getEndpointNewEventsAmount(0)
                .postEndpointNewEventsAmount(1)
                .deleteEndpointNewEventsAmount(-1)
                .changeEndpointNewEventsAmount(1)
                .build();
    }

    @Test
    public void submitPackage_notReplace() throws Exception {
        File packageBag = new File("src/test/resources/itestPackages/coconutpy-2.2.1.tar.gz");
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo9",
                false,
                true,
                "",
                new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build());

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .urlSuffix("/")
                .statusCode(201)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getPostEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/new_coconutpy_approved_submission.json")
                .expectedEventsJson(EVENTS_PATH + "new_submission_events.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);

        packageBag = new File("src/test/resources/itestPackages/coconutpy-2.2.1.tar.gz");
        SubmissionMultipartBody notReplaceBody = new SubmissionMultipartBody(
                "testrepo9",
                false,
                false,
                "",
                new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build());

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .urlSuffix("/")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .expectedJsonPath("/v2/python/submissions/submission_not_replace.json")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .submissionMultipartBody(notReplaceBody)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void submitPackage_withMismatchedVersion() throws Exception {
        final File packageBag = new File("src/test/resources/itestPackages/abo_tools-1.123.tar.gz");
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo8",
                false,
                true,
                "",
                new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build());

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .urlSuffix("/")
                .statusCode(201)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getPostEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/new_abo_tools_with_mismatched_version_submission.json")
                .expectedEventsJson(EVENTS_PATH + "new_abo_tools_with_mismatched_version_events.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void submitPackageWithDot_toPublishedRepository() throws Exception {
        final File packageBag = new File("src/test/resources/itestPackages/zest_releaser-9.6.2.tar.gz");
        final SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo8",
                false,
                true,
                "",
                new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build());

        final TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .urlSuffix("/")
                .statusCode(201)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getPostEndpointNewEventsAmount())
                .submissionMultipartBody(body)
                .expectedJsonPath("/v2/python/submissions/new_zest_releaser_approved_submission_published.json")
                .build();
        testEndpoint(requestBody);

        final ExecutionResult result = bashScriptExecutor.executeBashScript(
                "src/test/resources/scripts/checkIfPublishedPythonPackageWithDotCanBeInstalled.sh");
        assertNotEquals(
                DOWNLOADED_FROM_PYPI_EXIT_CODE,
                result.exitCode(),
                "The package was downloaded from PyPi instead of local repository.");
        assertEquals(0, result.exitCode(), "Uploaded package was not published properly.");
    }

    @Test
    public void submitPackage_toPublishedRepository() throws Exception {
        final File packageBag = new File("src/test/resources/itestPackages/coconutpy-2.2.1.tar.gz");
        final SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo8",
                false,
                true,
                "",
                new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build());

        final TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .urlSuffix("/")
                .statusCode(201)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getPostEndpointNewEventsAmount())
                .submissionMultipartBody(body)
                .expectedJsonPath("/v2/python/submissions/new_coconutpy_approved_submission_published.json")
                .build();
        testEndpoint(requestBody);

        final ExecutionResult result = bashScriptExecutor.executeBashScript(
                "src/test/resources/scripts/checkIfPublishedPythonPackageCanBeInstalled.sh");
        assertNotEquals(
                DOWNLOADED_FROM_PYPI_EXIT_CODE,
                result.exitCode(),
                "The package was downloaded from PyPi instead of local repository.");
        assertEquals(0, result.exitCode(), "Uploaded package was not published properly.");
    }

    @Test
    public void submitBinaryPackage_installPackage() throws Exception {
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

        final TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .urlSuffix("/")
                .statusCode(201)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getPostEndpointNewEventsAmount())
                .submissionMultipartBody(body)
                .expectedJsonPath("/v2/python/submissions/new_binary_submission.json")
                .build();
        testEndpoint(requestBody);

        final ExecutionResult result = bashScriptExecutor.executeBashScript(
                "src/test/resources/scripts/checkIfPublishedPythonBinaryPackageCanBeInstalled.sh");
        assertNotEquals(
                DOWNLOADED_FROM_PYPI_EXIT_CODE,
                result.exitCode(),
                "The package was downloaded from PyPi instead of local repository.");
        assertTrue(
                result.output()
                        .contains("Downloading " + "http://oa-rdepot-proxy/repo/testrepo8/tetrapolyscope/"
                                + "tetrapolyscope-0.0.1-cp39-cp39-manylinux_2_17_x86_64.manylinux2014_x86_64.whl"),
                "Package was not downloaded from RDepot.");
        assertEquals(0, result.exitCode(), "Uploaded package was not published properly.");
    }

    @Test
    public void submit_wheelPackage() throws Exception {
        File packageBag = new File("src/test/resources/itestPackages/wheel-0.42.0.tar.gz");
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo9",
                false,
                true,
                "",
                new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build());

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .urlSuffix("/")
                .statusCode(201)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getPostEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/new_wheel_submission.json")
                .expectedEventsJson(EVENTS_PATH + "new_wheel_submission_events.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_AFTER_NEW_SUBMISSION)
                .urlSuffix("/48")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/wheel_submission.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void submit_wheel_0_40_0_Package() throws Exception {
        File packageBag = new File("src/test/resources/itestPackages/wheel-0.40.0.tar.gz");
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo9",
                false,
                true,
                "",
                new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build());

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .urlSuffix("/")
                .statusCode(201)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getPostEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/new_wheel_0-40-0_submission.json")
                .expectedEventsJson(EVENTS_PATH + "new_wheel_0-40-0_submission_events.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_AFTER_NEW_SUBMISSION)
                .urlSuffix("/48")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/wheel_0-40-0_submission.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void submit_customPackage() throws Exception {
        File packageBag = new File("src/test/resources/itestPackages/wheel-0.40.0-custom.tar.gz");
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo9",
                false,
                true,
                "",
                new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build());

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .urlSuffix("/")
                .statusCode(201)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getPostEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/new_wheel_0-40-0_custom_submission.json")
                .expectedEventsJson(EVENTS_PATH + "new_wheel_0-40-0_submission_events.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_AFTER_NEW_SUBMISSION)
                .urlSuffix("/48")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/new_submission.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void submitPackage_addToWaitingList() throws Exception {
        File packageBag = new File("src/test/resources/itestPackages/coconutpy-2.2.1.tar.gz");
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo9",
                false,
                true,
                "",
                new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build());

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .urlSuffix("/")
                .statusCode(201)
                .token(PACKAGEMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getPostEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/new_coconutpy_submission.json")
                .expectedEventsJson(EVENTS_PATH + "new_submission_events_galileo.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_AFTER_NEW_SUBMISSION)
                .urlSuffix("?sort=id,desc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/all_submissions_with_new_waiting.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getAllSubmissions() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/all_submissions.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getOnlyCancelledSubmissions_asAdmin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?state=" + testData.getStates().get(1) + "&sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/cancelled_submissions.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getOnlyWaitingSubmissions_asAdmin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?state=" + testData.getStates().get(0) + "&sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/waiting_submissions.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getAllSubmissions_returns401_whenUserIsNotAuthenticated() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_UNAUTHENTICATED)
                .urlSuffix("?userId=7&sort=id,asc")
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getOneSubmission_asAdmin() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getSubmissionId())
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/one_submission.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getOneSubmission_asRepositoryMaintainer() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getSubmissionId())
                .statusCode(200)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/one_submission.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getOneSubmission_asPackageMaintainer() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getSubmissionId())
                .statusCode(200)
                .token(PACKAGEMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/one_submission.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getOneSubmission_asUser() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getSubmissionId())
                .statusCode(200)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/one_submission.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void getOneSubmission_returns401_whenUserIsNotAuthenticated() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_UNAUTHENTICATED)
                .urlSuffix("/" + testData.getSubmissionId())
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void deleteSubmission() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE)
                .urlSuffix("/" + testData.getSubmissionId())
                .statusCode(204)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getDeleteEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getSubmissionId())
                .statusCode(404)
                .token(PACKAGEMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/base/submissions/submission_not_found.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void deleteSubmission_Returns401_whenUserIsNotAuthenticated() throws Exception {
        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.DELETE_UNAUTHENTICATED)
                .urlSuffix("/" + testData.getSubmissionId())
                .token(PACKAGEMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void acceptSubmission() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/state\"," + "\"value\":\"accepted\"" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getSubmissionIdToAccept())
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/accepted_submission.json")
                .expectedEventsJson(EVENTS_PATH + "accept_submission_event.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void cancelSubmission() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/state\"," + "\"value\":\"cancelled\"" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getSubmissionIdToCancel())
                .statusCode(200)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/cancelled_submission.json")
                .expectedEventsJson(EVENTS_PATH + "cancelled_submission_events.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getSubmissionIdToCancel())
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/submission_after_cancelled.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void cancelSubmission_returns422_whenSubmissionIsNotWaiting() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/state\"," + "\"value\":\"cancelled\"" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getSubmissionIdAccepted())
                .statusCode(422)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/malformed_patch_submission.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void rejectSubmission() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/state\"," + "\"value\":\"rejected\"" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getSubmissionIdToCancel())
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/rejected_submission.json")
                .expectedEventsJson(EVENTS_PATH + "rejected_submission_events.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("/" + testData.getSubmissionIdToCancel())
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/submission_after_rejected.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void rejectSubmission_shouldFailWhenUserIsSubmitter() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/state\"," + "\"value\":\"rejected\"" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getSubmissionIdToCancel())
                .statusCode(403)
                .token(USER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/403.json")
                .expectedEventsJson("/v2/base/events/filtering/allEvents.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void cancelSubmission_shouldFailWhenUserIsNotSubmitter() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/state\"," + "\"value\":\"cancelled\"" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getSubmissionIdToCancel())
                .statusCode(403)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/403.json")
                .expectedEventsJson("/v2/base/events/filtering/allEvents.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchSubmission_returns422_whenPatchIsMalformed() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/dsdsadsadsa\"," + "\"value\":false" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH)
                .urlSuffix("/" + testData.getSubmissionIdToAccept())
                .statusCode(422)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/malformed_patch.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void patchSubmission_returns401_whenUserIsNotAuthenticated() throws Exception {
        final String patch =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/dsdsadsadsa\"," + "\"value\":false" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH_UNAUTHENTICATED)
                .urlSuffix("/" + testData.getSubmissionIdToAccept())
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/malformed_patch_submission.json")
                .body(patch)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void submitPackage_replaceWithChanges() throws Exception {
        File packageBag = new File("src/test/resources/itestPackages/coconutpy-2.2.1.tar.gz");

        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo9",
                false,
                true,
                "",
                new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build());

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .urlSuffix("/")
                .statusCode(201)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getPostEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/new_coconutpy_approved_submission.json")
                .expectedEventsJson(EVENTS_PATH + "new_submission_events.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);

        packageBag = new File("src/test/resources/itestPackages/coconutpy-2.2.1.tar.gz");

        body = new SubmissionMultipartBody(
                "testrepo9",
                false,
                true,
                "this package has been changed",
                new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build());
        requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .urlSuffix("/")
                .statusCode(201)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(0)
                .expectedJsonPath("/v2/python/submissions/new_coconutpy_with_changes.json")
                .expectedEventsJson(EVENTS_PATH + "new_submission_events.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_OTHER_RESOURCE)
                .path("/api/v2/manager/packages")
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/packages/list_of_packages_after_replace.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void submitPackage_asPackageMaintainer() throws Exception {
        final String postBody =
                "{" + "\"user\": { \"id\": 6}," + "\"packageName\": \"wheel\"," + "\"repository\": {\"id\" : 9}" + "}";

        testPostEndpoint(
                postBody,
                "/api/v2/manager/package-maintainers",
                "/v2/base/package-maintainer/maintainer_python_created.json",
                201,
                ADMIN_TOKEN);

        File packageBag = new File("src/test/resources/itestPackages/wheel-0.42.0.tar.gz");
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo9",
                false,
                true,
                "",
                new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build());

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .urlSuffix("/")
                .statusCode(201)
                .token(PACKAGEMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getPostEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/new_submission_as_package_maintainer.json")
                .expectedEventsJson(EVENTS_PATH + "new_submission_as_package_maintainer.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void submitPackage_withNoContentDescriptionType() throws Exception {
        File packageBag = new File("src/test/resources/itestPackages/PyYAML-6.0.1.tar.gz");
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo9",
                false,
                true,
                "",
                new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build());

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .urlSuffix("/")
                .statusCode(201)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getPostEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/new_submission_no_description_content_type.json")
                .expectedEventsJson(EVENTS_PATH + "new_submission_no_description_content_type.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void submitPackage_replaceLatestSamePackage() throws Exception {

        final String patchRepository =
                "[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/published\"," + "\"value\":\"true\"" + "}" + "]";

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.PATCH_OTHER_RESOURCE)
                .path("/api/v2/manager/python/repositories")
                .urlSuffix("/9")
                .statusCode(200)
                .token(REPOSITORYMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/repositories/published_repository.json")
                .expectedEventsJson("/v2/python/events/repositories/patched_published_repository_event.json")
                .body(patchRepository)
                .build();
        testEndpoint(requestBody);

        String targetDirectoryName = "src/test/resources/downloading/";
        createDownloadTestFolder(targetDirectoryName);

        String packageName = "ArmyOfEvilRobots-0.4.1dev.tar.gz";
        File packageBag = new File("src/test/resources/itestPackages/" + packageName);

        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo9",
                false,
                true,
                "",
                new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build());

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .urlSuffix("/")
                .statusCode(201)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getPostEndpointNewEventsAmount())
                .expectedEventsJson(EVENTS_PATH + "new_submission_before_replace_package_events.json")
                .expectedJsonPath("/v2/python/submissions/new_submission_before_replace_package.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_AFTER_NEW_SUBMISSION)
                .urlSuffix("/48")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/submission_before_replace_package.json")
                .build();
        testEndpoint(requestBody);

        bashScriptExecutor.executeBashCommand(
                "curl http://localhost:8017/repo/testrepo9/armyofevilrobots/ArmyOfEvilRobots-0.4.1dev.tar.gz  --output "
                        + targetDirectoryName + packageName);

        File file = new File(targetDirectoryName + packageName);

        String oldHash = DigestUtils.md5Hex(new FileInputStream(file));
        cleanAfterDownloading(targetDirectoryName + packageName);

        packageBag = new File("src/test/resources/itestPackages/ArmyOfEvilRobots-0-4-1dev.tar.gz");

        body = new SubmissionMultipartBody(
                "testrepo9",
                false,
                true,
                "",
                new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build());

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .urlSuffix("/")
                .statusCode(201)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(0)
                .expectedJsonPath("/v2/python/submissions/new_submission_after_replace_package.json")
                .expectedEventsJson(EVENTS_PATH + "new_submission_after_replace_package_events.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_AFTER_NEW_SUBMISSION)
                .urlSuffix("/49")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/python/submissions/submission_after_replace_package.json")
                .build();
        testEndpoint(requestBody);

        bashScriptExecutor.executeBashCommand("curl http://localhost:8017/repo/testrepo9/armyofevilrobots/"
                + packageName + " --output " + targetDirectoryName + packageName);

        file = new File(targetDirectoryName + packageName);

        String newHash = DigestUtils.md5Hex(new FileInputStream(file));

        assertNotEquals(oldHash, newHash, "Hashes should be different");
        cleanAfterDownloading(targetDirectoryName);
    }
}

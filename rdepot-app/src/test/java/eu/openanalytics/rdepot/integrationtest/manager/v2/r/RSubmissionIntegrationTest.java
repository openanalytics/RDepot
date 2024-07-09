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
package eu.openanalytics.rdepot.integrationtest.manager.v2.r;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.openanalytics.rdepot.integrationtest.environment.BashScriptExecutor;
import eu.openanalytics.rdepot.integrationtest.manager.v2.IntegrationTest;
import eu.openanalytics.rdepot.integrationtest.manager.v2.RequestType;
import eu.openanalytics.rdepot.integrationtest.manager.v2.TestRequestBody;
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.SubmissionMultipartBody;
import eu.openanalytics.rdepot.integrationtest.manager.v2.testData.SubmissionTestData;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.checkerframework.checker.nullness.qual.NonNull;

public class RSubmissionIntegrationTest extends IntegrationTest {

    private final SubmissionTestData testData;
    private static String EVENTS_PATH = "/v2/r/events/submissions/";
    private static final BashScriptExecutor bashScriptExecutor = new BashScriptExecutor();

    public RSubmissionIntegrationTest() {
        super("/api/v2/manager/r/submissions");
        this.testData = SubmissionTestData.builder()
                .apiPackagesPath("/api/v2/manager/r/packages")
                .submissionId("5")
                .submissionIdToAccept("30")
                .submissionIdToCancel("31")
                .packageId("47")
                .search("galileo")
                .states(Arrays.asList("waiting", "cancelled"))
                .packageNameToDownload("Benchmarking")
                .pdfPath("src/test/resources/itestPdf")
                .getEndpointNewEventsAmount(0)
                .postEndpointNewEventsAmount(1)
                .deleteEndpointNewEventsAmount(-5)
                .changeEndpointNewEventsAmount(1)
                .submissionIdAccepted("5")
                .build();
    }

    @Test
    public void submitPackage_createManualsByDefault() throws Exception {
        File packageBag = new File("src/test/resources/itestPackages/Benchmarking_0.10.tar.gz");
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo2",
                true,
                false,
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
                .expectedJsonPath("/v2/r/submission/new_submission.json")
                .expectedEventsJson(EVENTS_PATH + "new_submission_without_manual_events.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);

        byte[] pdf = given().header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                .accept(ContentType.ANY)
                .when()
                .get(testData.getApiPackagesPath() + "/" + testData.getPackageId() + "/manual")
                .then()
                .statusCode(200)
                .extract()
                .asByteArray();

        File file = new File(testData.getPdfPath() + "/" + testData.getPackageNameToDownload() + ".pdf");
        byte[] expectedpdf = readFileToByteArray(file);
        Assertions.assertTrue(expectedpdf.length + 1000 > pdf.length, "Manual PDFs are too different");
    }

    @Test
    public void submitPackage_correctlyParseRDescription_createManualsByDefault() throws Exception {

        File packageBag = new File("src/test/resources/itestPackages/Matrix_1.6-5.tar.gz");
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo2",
                true,
                false,
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
                .expectedJsonPath("/v2/r/submission/new_submission_with_non_simple_description.json")
                .expectedEventsJson(EVENTS_PATH + "new_submission_without_manual_events_matrix.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void submitPackage_generatesWarning_whenSynchronizationFails() throws Exception {
        testEnv.blockRepoContainer(() -> {
            try {
                do_submitPackage_generatesWarning_whenSynchronizationFails();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void do_submitPackage_generatesWarning_whenSynchronizationFails() throws Exception {
        File packageBag = new File("src/test/resources/itestPackages/Benchmarking_0.10.tar.gz");
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo2",
                false,
                true,
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
                .expectedJsonPath("/v2/r/submission/new_submission.json")
                .expectedEventsJson(EVENTS_PATH + "new_submission_without_manual_events.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void submitBigPackage() throws Exception {
        File packageBag = enlargePackage(new File("src/test/resources/itestPackages/Benchmarking_0.10.tar.gz"));
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo2",
                false,
                true,
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
                .expectedEventsJson(EVENTS_PATH + "new_big_submission_without_manual_events.json")
                .expectedJsonPath("/v2/r/submission/new_big_submission.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);
        removeEnlargedPackage(packageBag);
    }

    private void removeEnlargedPackage(File packageBag) throws Exception {
        FileUtils.forceDelete(packageBag);
    }

    private File enlargePackage(@NonNull File file) {
        bashScriptExecutor.executeBashScript(
                "src/test/resources/scripts/enlargePackage.sh",
                "src/test/resources/itestPackages",
                "Benchmarking_0.10.tar.gz");

        return new File("src/test/resources/itestPackages/BigBenchmarking_0.10.tar.gz");
    }

    @Test
    public void submitPackage_notCreateManual() throws Exception {
        File packageBag = new File("src/test/resources/itestPackages/Benchmarking_0.10.tar.gz");
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo2",
                false,
                true,
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
                .expectedEventsJson(EVENTS_PATH + "new_submission_without_manual_events.json")
                .expectedJsonPath("/v2/r/submission/new_submission.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_OTHER_RESOURCE)
                .path("/api/v2/manager/r/packages")
                .urlSuffix("/" + testData.getPackageId() + "/manual")
                .statusCode(404)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/r/submission/manual_not_found.json")
                .expectedEventsJson(EVENTS_PATH + "new_submission_without_manual_events.json")
                .build();
        testEndpoint(requestBody);

        final Process process = new ProcessBuilder(
                        "/bin/bash", "-c", "src/test/resources/scripts/checkIfPublishedPackageCanBeInstalled.sh")
                .redirectErrorStream(true)
                .start();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while (reader.readLine() != null) {}
        process.waitFor();
        final int exitCode = process.exitValue();
        process.destroy();
        assertEquals(0, exitCode, "Uploaded package was not published properly.");
    }

    @Test
    public void submitPackage_correctlyParseRDescription_notCreateManual() throws Exception {
        File packageBag = new File("src/test/resources/itestPackages/GGally_2.2.1.tar.gz");
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo2",
                false,
                true,
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
                .expectedEventsJson(EVENTS_PATH + "new_submission_without_manual_events_ggally.json")
                .expectedJsonPath("/v2/r/submission/submission_with_failed_synchronization.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void submitPackage_withExoticCharacters_notCreateManual() throws Exception {
        File packageBag = new File("src/test/resources/itestPackages/A3_0.9.3.tar.gz");
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo2",
                false,
                true,
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
                .expectedEventsJson("/v2/r/events/submissions/new_exotic_submission_without_manual_events.json")
                .expectedJsonPath("/v2/r/submission/new_exotic_submission.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_AFTER_NEW_SUBMISSION)
                .urlSuffix("/47")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/r/submission/exotic_submission.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void submitPackage_replace() throws Exception {
        File packageBag = new File("src/test/resources/itestPackages/A3_0.9.1.tar.gz");

        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo2",
                false,
                true,
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
                .expectedJsonPath("/v2/r/submission/new_A3_submission.json")
                .expectedEventsJson("/v2/r/events/submissions/new_submission_events.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);

        packageBag = new File("src/test/resources/itestPackages/A3_0-9-1.tar.gz");

        body = new SubmissionMultipartBody(
                "testrepo2",
                false,
                true,
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
                .expectedJsonPath("/v2/r/submission/submission_replace.json")
                .expectedEventsJson("/v2/r/events/submissions/replace_submission_events.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET)
                .urlSuffix("?sort=id,asc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/r/packages/list_of_packages_with_replaced_package.json")
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void submitPackage_notReplace() throws Exception {
        File packageBag = new File("src/test/resources/itestPackages/A3_0.9.1.tar.gz");
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo2",
                false,
                true,
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
                .expectedEventsJson(EVENTS_PATH + "new_submission_events.json")
                .expectedJsonPath("/v2/r/submission/new_A3_submission.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);

        packageBag = new File("src/test/resources/itestPackages/A3_0.9.1.tar.gz");
        body = new SubmissionMultipartBody(
                "testrepo2",
                false,
                false,
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
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/r/submission/submission_not_replace.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void submitPythonPackage_shouldFail() throws Exception {
        File packageBag = new File("src/test/resources/itestPackages/pandas_2.0.1.tar.gz");
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo2",
                false,
                true,
                new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build());

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .urlSuffix("/")
                .statusCode(422)
                .token(ADMIN_TOKEN)
                .expectedJsonPath("/v2/r/submission/submission_validation_error.json")
                .howManyNewEventsShouldBeCreated(0)
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void submitPackage_addToWaitingList() throws Exception {
        File packageBag = new File("src/test/resources/itestPackages/bipartite_2.13.tar.gz");
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo3",
                false,
                true,
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
                .expectedJsonPath("/v2/r/submission/new_bipartite_submission.json")
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
                .expectedJsonPath("/v2/r/submission/all_submissions_with_new_waiting.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);
    }

    @Test
    public void submitPackage_manualGenerationShouldFail_revertChanges() throws Exception {
        File packageBag = new File("src/test/resources/itestPackages/A3broken_0.9.1.tar.gz");
        SubmissionMultipartBody body = new SubmissionMultipartBody(
                "testrepo1",
                true,
                true,
                new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                        .fileName(packageBag.getName())
                        .mimeType("application/gzip")
                        .controlName("file")
                        .build());

        TestRequestBody requestBody = TestRequestBody.builder()
                .requestType(RequestType.POST_MULTIPART)
                .urlSuffix("/")
                .statusCode(422)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(0)
                .expectedJsonPath("/v2/r/submission/submission_failed_validation.json")
                .submissionMultipartBody(body)
                .build();
        testEndpoint(requestBody);

        requestBody = TestRequestBody.builder()
                .requestType(RequestType.GET_OTHER_RESOURCE)
                .path("/api/v2/manager/r/packages")
                .urlSuffix("?repository=testrepo1&sort=id,desc")
                .statusCode(200)
                .token(ADMIN_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getGetEndpointNewEventsAmount())
                .expectedJsonPath("/v2/r/packages/list_all_packages_testrepo1.json")
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
                .expectedJsonPath("/v2/r/submission/all_submissions.json")
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
                .expectedJsonPath("/v2/r/submission/cancelled_submissions.json")
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
                .expectedJsonPath("/v2/r/submission/waiting_submissions.json")
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
                .expectedJsonPath("/v2/r/submission/one_submission.json")
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
                .expectedJsonPath("/v2/r/submission/one_submission_viewed_by_each_user.json")
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
                .expectedJsonPath("/v2/r/submission/accepted_submission.json")
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
                .token(PACKAGEMAINTAINER_TOKEN)
                .howManyNewEventsShouldBeCreated(testData.getChangeEndpointNewEventsAmount())
                .expectedJsonPath("/v2/r/submission/cancelled_submission.json")
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
                .expectedJsonPath("/v2/r/submission/submission_after_cancelled.json")
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
                .token(ADMIN_TOKEN)
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
                .expectedJsonPath("/v2/r/submission/rejected_submission.json")
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
                .expectedJsonPath("/v2/r/submission/submission_after_rejected.json")
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
                .token(PACKAGEMAINTAINER_TOKEN)
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
                .expectedJsonPath("/v2/r/submission/malformed_patch.json")
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
}

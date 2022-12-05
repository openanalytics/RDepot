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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;

import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;

public class SubmissionIntegrationTest extends IntegrationTest {		
	
	private final String API_PACKAGES_PATH = "/api/v2/manager/r/packages";
	protected final IntegrationTestMediator integrationTestMediator = new IntegrationTestMediator();

	private final String SUBMISSION_ID = "5";
	private final String SUBMISSION_ID_REPOSITORYMAINTAINER = "17";
	private final String SUBMISSION_ID_PACKAGEMAINTAINER = "14";
	private final String SUBMISSION_ID_USER = "19";
	private final String SUBMISSION_ID_TO_ACCEPT = "30";
	private final String SUBMISSION_ID_TO_CANCEL = "31";
	
	private final String PACKAGE_ID = "32";
	private final String PACKAGE_NAME_TO_DOWNLOAD = "Benchmarking";
	private final String PDF_PATH = "src/integration-test/resources/itestPdf";	
	
	private final int GET_ENDPOINT_POST_NEW_EVENTS_AMOUNT = 1;
	private final int GET_ENDPOINT_NEW_EVENTS_AMOUNT = 0;	
	private final int GET_ENDPOINT_NEW_DELETE_EVENTS_AMOUNT = -5;	
	private final int PATCH_ENDPOINT_NEW_EVENTS_AMOUNT = 1;
	
	public SubmissionIntegrationTest() {
		super("/api/v2/manager/r/submissions");
	}
	
	@Test
	public void submitPackage_createManualsByDefault() throws Exception {	
		
		File packageBag = new File ("src/integration-test/resources/itestPackages/Benchmarking_0.10.tar.gz");
		SubmissionMultipartBody body = new SubmissionMultipartBody("testrepo2", new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
				.fileName(packageBag.getName())
				.mimeType("application/gzip")
				.controlName("file")
				.build());
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.POST_MULTIPART, 
				201, ADMIN_TOKEN, GET_ENDPOINT_POST_NEW_EVENTS_AMOUNT, "/v2/events/submissions/new_submission_without_manual_events.json", body);
		testEndpoint(requestBody);
		
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
	}
	
	@Test
	public void submitPackage_notCreateManual() throws Exception {				
		File packageBag = new File ("src/integration-test/resources/itestPackages/Benchmarking_0.10.tar.gz");
		SubmissionMultipartBody body = new SubmissionMultipartBody("testrepo2", false, true, new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
				.fileName(packageBag.getName())
				.mimeType("application/gzip")
				.controlName("file")
				.build());
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.POST_MULTIPART, 
				201, ADMIN_TOKEN, GET_ENDPOINT_POST_NEW_EVENTS_AMOUNT, "/v2/events/submissions/new_submission_without_manual_events.json", body);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/submission/manual_not_found.json", 
				"/" + PACKAGE_ID + "/manual", 404, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);			
	}
	
	@Test
	public void submitPackage_replace() throws Exception{
		File packageBag = new File ("src/integration-test/resources/itestPackages/A3_0.9.1.tar.gz");
		
		SubmissionMultipartBody body = new SubmissionMultipartBody("testrepo2", false, true, new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
				.fileName(packageBag.getName())
				.mimeType("application/gzip")
				.controlName("file")
				.build());
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.POST_MULTIPART, 
				201, ADMIN_TOKEN, GET_ENDPOINT_POST_NEW_EVENTS_AMOUNT, "/v2/events/submissions/new_submission_events.json", body);
		testEndpoint(requestBody);

		packageBag = new File ("src/integration-test/resources/itestPackages/A3_0-9-1.tar.gz");
		
		body = new SubmissionMultipartBody("testrepo2", false, true, new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
				.fileName(packageBag.getName())
				.mimeType("application/gzip")
				.controlName("file")
				.build());
		
		requestBody = new TestRequestBody(RequestType.POST_MULTIPART, 
				201, ADMIN_TOKEN, GET_ENDPOINT_POST_NEW_EVENTS_AMOUNT, "/v2/events/submissions/replace_submission_events.json", body);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/package/list_of_packages_with_replaced_package.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void submitPackage_notReplace() throws Exception {
		File packageBag = new
				File ("src/integration-test/resources/itestPackages/A3_0.9.1.tar.gz");
		SubmissionMultipartBody body = new SubmissionMultipartBody("testrepo2", false, true, new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
				.fileName(packageBag.getName())
				.mimeType("application/gzip")
				.controlName("file")
				.build());
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.POST_MULTIPART, 
				201, ADMIN_TOKEN, GET_ENDPOINT_POST_NEW_EVENTS_AMOUNT, "/v2/events/submissions/new_submission_events.json", body);
		testEndpoint(requestBody);
		
		
		packageBag = new File ("src/integration-test/resources/itestPackages/A3_0.9.1.tar.gz");
		body = new SubmissionMultipartBody("testrepo2", false, false, new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
				.fileName(packageBag.getName())
				.mimeType("application/gzip")
				.controlName("file")
				.build());
		
		requestBody = new TestRequestBody(RequestType.POST_MULTIPART, 
				200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT, body);
		testEndpoint(requestBody);
	}
	
	@Test
	public void submitPackage_addToWaitingList() throws Exception {				
		File packageBag = new File ("src/integration-test/resources/itestPackages/Benchmarking_0.10.tar.gz");
		SubmissionMultipartBody body = new SubmissionMultipartBody("testrepo3", false, true, new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
				.fileName(packageBag.getName())
				.mimeType("application/gzip")
				.controlName("file")
				.build());
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.POST_MULTIPART, 
				201, USER_TOKEN, GET_ENDPOINT_POST_NEW_EVENTS_AMOUNT, "/v2/events/submissions/new_submission_waiting.json", body);
		testEndpoint(requestBody);
		
		integrationTestMediator.getAllPackages();
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/submission/all_submissions_with_new_waiting_viewed_by_admin.json", 
				"?sort=id,desc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);			
	}
	
	@Test
	public void getAllSubmissions_asAdmin() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/submission/all_submissions_viewed_by_admin.json", 
				"?sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	//TODO he gets all submissions
	@Test
	public void getAllSubmissions_asRepositoryMaintainer() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/submission/all_submissions_viewed_by_repositorymaintainer.json", 
				"?sort=id,asc", 200, REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);		
	}
	
	//TODO he gets all submissions
	@Test
	public void getAllSubmissions_asPackageMaintainer() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/submission/all_submissions_viewed_by_packagemaintainer.json", 
				"?sort=id,asc", 200, PACKAGEMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);				
	}
	
	
	@Test
	public void getOnlyCancelledSubmissions_asAdmin() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/submission/cancelled_submissions_viewed_by_admin.json", 
				"?state=cancelled&sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);		
	}
	
	@Test
	public void getOnlyWaitingSubmissions_asAdmin() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/submission/waiting_submissions_viewed_by_admin.json", 
				"?state=waiting&sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);	
	}
	
	@Test
	public void getAllSubmissionsOfUser_asAdmin() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/submission/all_submissions_of_user_viewed_by_admin.json", 
				"?userId=7&sort=id,asc", 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
		
	@Test
	public void getAllSubmissionsOfUser_asThisUser() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET, "/v2/submission/all_submissions_of_user_viewed_by_this_user.json", 
				"?sort=id,asc", 200, USER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
	}
	
	@Test
	public void getAllSubmissions_returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET_UNAUTHENTICATED,
				"/", GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);		
	}
	
	@Test
	public void getOneSubmission_asAdmin() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET,"/v2/submission/one_submission_viewed_by_admin.json", 
				"/" + SUBMISSION_ID, 200, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);		
	}
	
	//all users can see all submissions - but it is how we want it to work
	@Test
	public void getOneSubmission_asRepositoryMaintainer() throws Exception {	
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET,"/v2/submission/one_submission_viewed_by_repositorymaintainer.json",
				"/" + SUBMISSION_ID_REPOSITORYMAINTAINER, 200, REPOSITORYMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);		
	}
	
	@Test
	public void getOneSubmission_asPackageMaintainer() throws Exception {	
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET,"/v2/submission/one_submission_viewed_by_packagemaintainer.json",
				"/" + SUBMISSION_ID_PACKAGEMAINTAINER, 200, PACKAGEMAINTAINER_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);		
	}
	
	@Test
	public void getOneSubmission_returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.GET_UNAUTHENTICATED,
				"/" + SUBMISSION_ID_USER, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);	
	}
	
	@Test
	public void deleteSubmission() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.DELETE,
				 "/" + SUBMISSION_ID, 204, ADMIN_TOKEN, GET_ENDPOINT_NEW_DELETE_EVENTS_AMOUNT);
		testEndpoint(requestBody);	
		
		requestBody = new TestRequestBody(RequestType.DELETE, "/v2/submission/notfound.json",
				 "/" + SUBMISSION_ID, 404, ADMIN_TOKEN, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);	
	}
	
	@Test
	public void deleteSubmission_Returns401_whenUserIsNotAuthenticated() throws Exception {
		TestRequestBody requestBody = new TestRequestBody(RequestType.DELETE_UNAUTHENTICATED,
				"/" + SUBMISSION_ID, GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);	
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
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH, "/v2/submission/accepted_submission.json", 
				"/" + SUBMISSION_ID_TO_ACCEPT, 200, ADMIN_TOKEN, 
				PATCH_ENDPOINT_NEW_EVENTS_AMOUNT, "/v2/events/submissions/accept_submission_event.json", patch);
		testEndpoint(requestBody);
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
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH, "/v2/submission/cancelled_submission.json", 
				"/" + SUBMISSION_ID_TO_CANCEL, 200, ADMIN_TOKEN, 
				PATCH_ENDPOINT_NEW_EVENTS_AMOUNT, "/v2/events/submissions/cancelled_submission_events.json", patch);
		testEndpoint(requestBody);
		
		requestBody = new TestRequestBody(RequestType.GET, "/v2/submission/submission_after_cancelled.json", 
				"/" + SUBMISSION_ID_TO_CANCEL, 200, ADMIN_TOKEN, 
				GET_ENDPOINT_NEW_EVENTS_AMOUNT);
		testEndpoint(requestBody);
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
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH, "/v2/malformed_patch.json", 
				"/" + SUBMISSION_ID_TO_ACCEPT, 422, ADMIN_TOKEN, 
				GET_ENDPOINT_NEW_EVENTS_AMOUNT, patch);
		testEndpoint(requestBody);
	}
	
	@Test
	public void patchSubmission_returns401_whenUserIsNotAuthenticated() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/dsdsadsadsa\","
				+ "\"value\":false"
				+ "}"
				+ "]";
		
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH_UNAUTHENTICATED,
				"/" + SUBMISSION_ID, GET_ENDPOINT_NEW_EVENTS_AMOUNT, patch);
		testEndpoint(requestBody);	
	}
}

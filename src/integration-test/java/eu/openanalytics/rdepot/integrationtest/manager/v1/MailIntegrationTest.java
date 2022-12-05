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
package eu.openanalytics.rdepot.integrationtest.manager.v1;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.awaitility.Awaitility;
import org.json.simple.parser.ParseException;
import org.junit.Rule;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetup;

import eu.openanalytics.rdepot.integrationtest.IntegrationTest;
import eu.openanalytics.rdepot.integrationtest.utils.JSONConverter;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;

public class MailIntegrationTest extends IntegrationTest {		
	
	private final String API_PACKAGES_PATH = "/api/manager/packages";
	private final String API_SUBMISSIONS_PATH = "/api/manager/submissions";
	private final String API_REPOSITORIES_PATH = "/api/manager/repositories";
	
	private static final ServerSetup serverSetup = new ServerSetup(3925, "0.0.0.0", "smtp");			
	
	public MailIntegrationTest() {
		super("");
	}
	
	@Rule
	public final GreenMailRule greenMail = new GreenMailRule(serverSetup)
		.withConfiguration(GreenMailConfiguration.aConfig().withUser("rdepot", "mysecretpassword"));

	@Test
	public void shouldAddPackageToWaitingSubmissionsAndSendAcceptEmail() throws ParseException, IOException, MessagingException {
		File packageBag = new File ("src/integration-test/resources/itestPackages/A3_0.9.2.tar.gz");
				
		try {
			given()
				.header(AUTHORIZATION, BEARER + USER_TOKEN)
				.accept("application/json")
				.contentType("multipart/form-data")
				.multiPart("repository", "testrepo2")
				.multiPart("replace", true)
				.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
						.fileName(packageBag.getName())
						.mimeType("application/gzip")
						.controlName("file")
						.build())
			.when()
				.post(API_PACKAGES_PATH + "/submit")
			.then()
				.statusCode(200)
				.extract();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		FileReader reader = new FileReader(JSON_PATH + "/submission/submissions_user_wants_to_upload_package.json");
		
		JsonArray expectedSubmissions = (JsonArray) JsonParser.parseReader(reader);
		
		String data = given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_SUBMISSIONS_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		JsonArray actualSubmissions = (JsonArray) JsonParser.parseString(data);
		
		reader = new FileReader(JSON_PATH + "/submission/submissions_repositories_unchanged.json");
		JsonArray expectedJSON = (JsonArray) JsonParser.parseReader(reader);
		
		List<Set<JsonObject>> expectedPackages = JSONConverter.convertNewPackagesFromRepo(expectedJSON);
		
		data = given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.get(API_REPOSITORIES_PATH + "/list")
		.then()
			.statusCode(200)
			.extract()
			.asString();
		
		JsonArray actualJSON = (JsonArray) JsonParser.parseString(data);		
		
		List<Set<JsonObject>> actualPackages = JSONConverter.convertNewPackagesFromRepo(actualJSON);

		assertEquals("expected packages: " + expectedPackages + " but was: " + actualPackages, expectedPackages, actualPackages);
		assertTrue("expected json: " + expectedJSON + " but was: " + actualJSON, compare(expectedJSON, actualJSON));		
		assertTrue("expected submission: " + expectedSubmissions + " but was: " + actualSubmissions, compare(expectedSubmissions, actualSubmissions));
		
		Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			final MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
		    final MimeMessage receivedMessage = receivedMessages[0];
		    assertEquals("RDepot: new submission", receivedMessage.getSubject());
		});
	}
	
	@Test
	public void shouldAddPackageToWaitingSubmissionsAndSendCancelEmail() throws ParseException, IOException, MessagingException {
		File packageBag = new File ("src/integration-test/resources/itestPackages/accrued_1.1.tar.gz");
				
		try {
			given()
				.header(AUTHORIZATION, BEARER + USER_TOKEN)
				.accept("application/json")
				.contentType("multipart/form-data")
				.multiPart("repository", "testrepo1")
				.multiPart("replace", true)
				.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
						.fileName(packageBag.getName())
						.mimeType("application/gzip")
						.controlName("file")
						.build())
			.when()
				.post(API_PACKAGES_PATH + "/submit")
			.then()
				.statusCode(200)
				.extract();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		given()
			.header(AUTHORIZATION, BEARER + USER_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_SUBMISSIONS_PATH + "/32/cancel")
		.then()
			.statusCode(200)
			.body("success", equalTo("Submission has been canceled successfully."));
		
		Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
			final MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
		    final MimeMessage receivedMessageNew = receivedMessages[0];
		    assertEquals("RDepot: new submission", receivedMessageNew.getSubject());
		    final MimeMessage receivedMessageCancelled = receivedMessages[1];
		    assertEquals("RDepot: canceled submission", receivedMessageCancelled.getSubject());
		});				
	}
	
	private boolean compare(JsonArray expected, JsonArray actual) throws ParseException {
		
		if (expected == null || actual == null)
			return false;
		
		if (expected.size() != actual.size())
			return false;
		
		for(int i = 0; i < expected.size(); i++) {
			JsonObject expectedRepository = (JsonObject) expected.get(i);
			JsonObject actualRepository = (JsonObject) actual.get(i);

			expectedRepository.remove("packages");
			actualRepository.remove("packages");
			
			expectedRepository.remove("uploadDate");
			actualRepository.remove("uploadDate");
			
			if(!expectedRepository.equals(actualRepository))
				return false;
		}
		return true;
	}
}

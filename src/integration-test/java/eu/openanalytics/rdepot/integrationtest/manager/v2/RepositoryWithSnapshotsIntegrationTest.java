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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;

public class RepositoryWithSnapshotsIntegrationTest extends IntegrationTest {	
	public RepositoryWithSnapshotsIntegrationTest() {
		super("/api/v2/manager/r/repositories");
	}

	private final String REPO_ID_TO_PUBLISH = "5";
	private final int GET_CHANGED_REPOSITORY_NEW_EVENTS_AMOUNT = 1;
	
	@BeforeClass
	public static final void configureRestAssured() throws IOException, InterruptedException {
		RestAssured.port = 8023;
		RestAssured.urlEncodingEnabled = false;
	}
	
	@Before
	@Override
	public void setUp() throws IOException, InterruptedException {
		String[] cmd = new String[] {"gradle", "restoreWithoutSnapshots", "-b","src/integration-test/resources/build.gradle"};
		Process process = Runtime.getRuntime().exec(cmd);
		int exitCode = process.waitFor();
		process.destroy();		
		assertEquals("The restore command was not executed properly", 0, exitCode);
	}
	
	@Test
	public void patchRepository_publish() throws Exception {
		final String patch = "["
				+ "{"
				+ "\"op\": \"replace\","
				+ "\"path\":\"/published\","
				+ "\"value\":true"
				+ "}"
				+ "]";
		
		TestRequestBody requestBody = new TestRequestBody(RequestType.PATCH, "/v2/repository/published_repository.json",
				"/" + REPO_ID_TO_PUBLISH, 200, REPOSITORYMAINTAINER_TOKEN, GET_CHANGED_REPOSITORY_NEW_EVENTS_AMOUNT,
				"/v2/events/repositories/patched_published_repository_event_without_snapshots.json", patch);
		testEndpoint(requestBody);	
	}
	
	@Test
	public void patchRepository_publish_removesGeneratedContent_whenSnapshotsAreTurnedOff() throws Exception {
		patchRepository_publish();
		int cmdExitValue = runCommand("gradle", "checkIfSnapshotDirExists", "-b", "src/integration-test/resources/build.gradle");
		assertEquals("Snapshots dir does not exist.", 0, cmdExitValue);
		cmdExitValue = runCommand("gradle", "checkIfSnapshotDirIsEmpty", "-b", "src/integration-test/resources/build.gradle");
		assertEquals("Snapshots is not empty.", 0, cmdExitValue);
	}
}

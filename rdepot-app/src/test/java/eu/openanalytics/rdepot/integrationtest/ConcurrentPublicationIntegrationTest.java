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
package eu.openanalytics.rdepot.integrationtest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.integrationtest.manager.v2.IntegrationTest;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;

public class ConcurrentPublicationIntegrationTest extends IntegrationTest {
	
	public ConcurrentPublicationIntegrationTest() {
		super("/api/v2/manager/r/repositories");
	}

	private static final String PACKAGE_ID = "17";
	private static final String REPOSITORY_ID = "2";
	private static final String API_PACKAGES_PATH = "/api/v2/manager/r/packages";
	private static final String API_REPOSITORIES_PATH = "/api/v2/manager/r/repositories";
	private static final String PUBLICATION_URI_PATH = "/repo";

	private static final Integer THREAD_COUNT = 20;
	private static final String REPO_NAME_TO_CREATE = "newRepository";
	
	@Test
	public void shouldPublishRepositoryInParallel() throws InterruptedException, ExecutionException {
		String body = "[{"
				+ "\"op\" : \"replace\","
				+ "\"path\" : \"/deleted\","
				+ "\"value\" : true"
				+ "}]";
		
		given()
			.headers(AUTHORIZATION, BASIC + ADMIN_TOKEN)
			.accept(ContentType.JSON)
			.contentType("application/json-patch+json")
			.body(body)
		.when()
			.patch(API_PACKAGES_PATH + "/" + PACKAGE_ID)
		.then()
			.statusCode(200);
		
		CountDownLatch latch = new CountDownLatch(1);
		ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT);

		List<Future<String>> futures = new ArrayList<>();
		
		for(int i = 0; i < THREAD_COUNT; i++) {
			futures.add(service.submit(() -> {
				try {
					latch.await();
					String body2 = "[{"
							+ "\"op\" : \"replace\","
							+ "\"path\" : \"/published\","
							+ "\"value\" : true"
							+ "}]";
					given()
						.headers(AUTHORIZATION, BASIC + ADMIN_TOKEN)
						.contentType("application/json-patch+json")
						.accept(ContentType.JSON)
						.body(body2)
					.when()
						.patch(API_REPOSITORIES_PATH + "/" + REPOSITORY_ID)
					.then()
						.statusCode(200);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return "OK";
			}));
			
		}
		
		latch.countDown();
		Integer threadsExecuted = 0;
		for(Future<String> f : futures) {
			f.get();
			threadsExecuted++;
		}
		
		assertEquals(THREAD_COUNT, threadsExecuted);
	}
	
	@Test
	public void shouldSubmitMultiplePackagesAtOnce() 
			throws IOException, InterruptedException, ExecutionException {
		
		final String repositoriesPath = "/api/v2/manager/r/repositories";
		Map<String, String> params = new HashMap<>();
		params.put("name", REPO_NAME_TO_CREATE);
		params.put("publicationUri", "http://localhost/repo/" + REPO_NAME_TO_CREATE);
		params.put("serverAddress", "http://oa-rdepot-repo:8080/" + REPO_NAME_TO_CREATE);
		String bodyJson = new ObjectMapper().writeValueAsString(params);
		
		given()
			.header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.body(bodyJson)
		.when()
			.post(repositoriesPath)
		.then()
			.statusCode(201);
		
		String[] PACKAGES = {"visdat_0.1.0.tar.gz", 
				"npordtests_1.1.tar.gz",
				"bipartite_2.13.tar.gz",
				"A3_0.9.2.tar.gz",
				"abc_1.3.tar.gz",
				"accrued_1.4.tar.gz",
				"usl_2.0.0.tar.gz"};
		
		String body = "[{"
				+ "\"op\" : \"replace\","
				+ "\"path\" : \"/published\","
				+ "\"value\" : true"
				+ "}]";
		
		given()
			.header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
			.accept(ContentType.JSON)
			.contentType("application/json-patch+json")
			.body(body)
		.when()
			.patch(repositoriesPath + "/13")
		.then()
			.statusCode(200);
		
		List<Future<String>> futures = new ArrayList<>();
		CountDownLatch latch = new CountDownLatch(1);
		ExecutorService service = Executors.newFixedThreadPool(PACKAGES.length);
		
		for(String packageName : PACKAGES) {
			futures.add(service.submit(() -> {
				try {
					latch.await();
					
					File packageFile = new File("src/test/resources/itestPackages/" + packageName);
					
					given()
						.headers(AUTHORIZATION, BASIC + ADMIN_TOKEN)
						.accept("application/json")
						.contentType("multipart/form-data")
						.multiPart("repository", REPO_NAME_TO_CREATE)
						.multiPart("generateManual", false)
						.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageFile.toPath()))
								.fileName(packageFile.getName())
								.mimeType("application/gzip")
								.controlName("file")
								.build())
					.when()
						.post("/api/v2/manager/r/submissions")
					.then()
						.statusCode(201);
					
					byte[] uploadedPackage = given()
							.accept(ContentType.BINARY)
						.when()
							.get(PUBLICATION_URI_PATH + "/" + REPO_NAME_TO_CREATE + "/src/contrib/" + packageName)
							.asByteArray();
					
					byte[] expectedPackage = Files.readAllBytes(packageFile.toPath());
					Assertions.assertTrue(Arrays.equals(uploadedPackage, expectedPackage));
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return "OK";
			}));
		}
		
		latch.countDown();
		Integer threadsExecuted = 0;
		for(Future<String> f : futures) {
			f.get();
			threadsExecuted++;
		}
		
		assertEquals(Integer.valueOf(PACKAGES.length), threadsExecuted);
	}

}

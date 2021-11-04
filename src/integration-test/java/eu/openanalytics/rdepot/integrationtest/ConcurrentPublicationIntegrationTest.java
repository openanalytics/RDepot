/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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
package eu.openanalytics.rdepot.integrationtest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;

public class ConcurrentPublicationIntegrationTest extends IntegrationTest {
	
	public ConcurrentPublicationIntegrationTest() {
		super("/api/manager/repositories");
	}

	private static final String PACKAGE_ID = "17";
	private static final String REPOSITORY_ID = "2";
	private static final String API_PACKAGES_PATH = "/api/manager/packages";
	private static final String API_REPOSITORIES_PATH = "/api/manager/repositories";
	private static final String PUBLICATION_URI_PATH = "/repo";

	private static final Integer THREAD_COUNT = 20;
		
	@Test
	public void shouldPublishRepositoryInParallel() throws InterruptedException, ExecutionException {
		given()
			.headers(AUTHORIZATION, BEARER + ADMIN_TOKEN)
			.accept(ContentType.JSON)
		.when()
			.delete(API_PACKAGES_PATH + "/" + PACKAGE_ID + "/delete")
		.then()
			.statusCode(200)
			.body("success", equalTo("Package deleted successfully."));
		
		CountDownLatch latch = new CountDownLatch(1);
		ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT);
		
		
		List<Future<String>> futures = new ArrayList<>();
		
		for(int i = 0; i < THREAD_COUNT; i++) {
			futures.add(service.submit(() -> {
				try {
					latch.await();
					
					given()
						.headers(AUTHORIZATION, BEARER + ADMIN_TOKEN)
						.accept(ContentType.JSON)
					.when()
						.patch(API_REPOSITORIES_PATH + "/" + REPOSITORY_ID + "/publish")
					.then()
						.statusCode(200)
						.body("success", equalTo("Repository has been published successfully."));
					
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
		String[] PACKAGES = {"visdat_0.1.0.tar.gz", 
				"npordtests_1.1.tar.gz",
				"bipartite_2.13.tar.gz"};
		
		List<Future<String>> futures = new ArrayList<>();
		CountDownLatch latch = new CountDownLatch(1);
		ExecutorService service = Executors.newFixedThreadPool(PACKAGES.length);
		
		for(String packageName : PACKAGES) {
			futures.add(service.submit(() -> {
				try {
					latch.await();
					
					File packageFile = new File("src/integration-test/resources/itestPackages/" + packageName);
					
					given()
						.headers(AUTHORIZATION, BEARER + ADMIN_TOKEN)
						.accept("application/json")
						.contentType("multipart/form-data")
						.multiPart("repository", "testrepo1")
						.multiPart(new MultiPartSpecBuilder(Files.readAllBytes(packageFile.toPath()))
								.fileName(packageFile.getName())
								.mimeType("application/gzip")
								.controlName("file")
								.build())
					.when()
						.post("/api/manager/packages/submit")
					.then()
						.statusCode(200)
						.extract();
					
					byte[] uploadedPackage = given()
							.accept(ContentType.BINARY)
						.when()
							.get(PUBLICATION_URI_PATH + "/testrepo1/src/contrib/" + packageName)
							.asByteArray();
					
					byte[] expectedPackage = Files.readAllBytes(packageFile.toPath());
					assertTrue(Arrays.equals(uploadedPackage, expectedPackage));
					
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

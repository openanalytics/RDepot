/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class ConcurrentPublicationIntegrationTest {
	
	private static final String PACKAGE_ID = "17";
	private static final String ADMIN_LOGIN = "einstein";
	private static final String PASSWORD = "testpassword";
	private static final String REPOSITORY_ID = "2";
	private static final String API_PACKAGES_PATH = "/api/manager/packages";
	private static final String API_REPOSITORIES_PATH = "/api/manager/repositories";
	private static final Integer THREAD_COUNT = 20;
	
	@Before
	public void setUp() throws IOException, InterruptedException {
		String[] cmd = new String[] {"gradle", "restore", "-b","src/integration-test/resources/build.gradle"};
		Process process = Runtime.getRuntime().exec(cmd);
		process.waitFor();
		process.destroy();
	}
	
	@BeforeClass
	public static void configureRestAssured() {
		RestAssured.port = 8017;
		RestAssured.urlEncodingEnabled = false;
	}
	
	@Test
	public void shouldPublishRepositoryInParallel() throws InterruptedException, ExecutionException {
		given()
			.auth()
			.basic(ADMIN_LOGIN, PASSWORD)
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
						.auth()
						.basic(ADMIN_LOGIN, PASSWORD)
						.accept(ContentType.JSON)
					.when()
						.post(API_REPOSITORIES_PATH + "/" + REPOSITORY_ID + "/publish")
					.then()
						.statusCode(200)
						.body("success", equalTo("repository.published"));
					
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
}

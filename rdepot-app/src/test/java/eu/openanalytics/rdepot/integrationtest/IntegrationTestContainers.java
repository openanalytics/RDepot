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
package eu.openanalytics.rdepot.integrationtest;

import eu.openanalytics.rdepot.integrationtest.manager.v2.IntegrationTest;
import java.io.File;
import java.time.Duration;
import java.util.Collections;
import org.reflections.Reflections;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class IntegrationTestContainers {

    private static DockerComposeContainer<?> container = new DockerComposeContainer<>(
                    new File("src/test/resources/docker-compose.yaml"))
            .withLocalCompose(true)
            .withOptions("--compatibility")
            .waitingFor(
                    "proxy",
                    Wait.forHttp("/actuator/health")
                            .forStatusCode(200)
                            .withHeaders(Collections.singletonMap("Accept", "application/json"))
                            .withStartupTimeout(Duration.ofMinutes(5)))
            .withTailChildContainers(true);
    private static boolean running = false;
    private static int testClassesCount = 0;
    private static final int TEST_CLASSES_TO_COMPLETE = getITClassesCount();

    private static int getITClassesCount() {
        Reflections reflections = new Reflections("eu.openanalytics.rdepot");
        return reflections.getSubTypesOf(IntegrationTest.class).size();
    }

    public static void startContainersIfNotRunningYet() {
        System.out.println("===Setting up containers for test class: " + String.valueOf(testClassesCount + 1) + "/"
                + TEST_CLASSES_TO_COMPLETE);
        if (!running) {
            System.out.println("===Containers were not running. Starting up...");
            container.start();
            running = true;
            System.out.println("===Containers started succesfully.");
        } else {
            System.out.println("===Containers are already running - skipping.");
        }
        testClassesCount++;
    }

    public static void stopContainersIfAllTestsCompleted() {
        System.out.println("===Stopping test containers if all tests are complete...");
        if (running && testClassesCount == TEST_CLASSES_TO_COMPLETE) {
            System.out.println("===All tests completed! Stopping...");
            container.stop();
            running = false;
            System.out.println("===All containers stopped.");
        } else {
            System.out.println("===Tests are not complete yet - the containers will keep running.");
        }
    }

    public static void stopContainers() {
        System.out.println("===Stopping test containers regardless of running tests...");
        if (running) {
            System.out.println("===Stopping...");
            container.stop();
            running = false;
            System.out.println("===All containers stopped.");
        } else {
            System.out.println("===Containers had already been down!");
        }
    }
}

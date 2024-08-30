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
package eu.openanalytics.rdepot.migrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.openanalytics.rdepot.integrationtest.IntegrationTestContainers;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class MigrationTests {

    public static DockerComposeContainer<?> container = new DockerComposeContainer<>(
                    new File("src/test/resources/docker-compose-migrations.yaml"))
            .withLocalCompose(true)
            .withOptions("--compatibility")
            .waitingFor(
                    "proxy",
                    Wait.forHttp("/actuator/health")
                            .forStatusCode(200)
                            .withHeaders(Collections.singletonMap("Accept", "application/json"))
                            .withStartupTimeout(Duration.ofMinutes(5)));

    @BeforeAll
    public static void configureRestAssured() throws IOException, InterruptedException {
        IntegrationTestContainers.stopContainers();
        System.out.println("===Starting containers for migrations tests...");
        container.start();
        System.out.println("===Migrations containers started.");
    }

    @AfterAll
    public static void tearDownContainer() {
        System.out.println("===Stopping containers for migrations...");
        container.stop();
        System.out.println("===Migrations containers stopped.");
    }

    @Test
    public void migrationModulesTest() throws IOException, InterruptedException {
        String expected = Files.readString(Path.of("src/test/resources/docker/db/sql_files/migrations/expected.sql"))
                .trim()
                .replaceAll("\\t", " ")
                .replaceAll("\\s{2,}", " ");

        String[] cmd = new String[] {"bash", "src/test/resources/scripts/checkIfModulesMigrationWasSuccessful.sh"};
        Process process = Runtime.getRuntime().exec(cmd);

        String output = new String(process.getInputStream().readAllBytes());
        String actual = output.split("TRUNCATE TABLE\n")[1]
                .split("ENDOFTESTCASE")[0]
                .trim()
                .replaceAll("\\t", " ")
                .replaceAll("\\s{2,}", " ");

        process.destroy();
        assertEquals(expected, actual, "Migration failed");
    }
}

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
package eu.openanalytics.rdepot.migrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class MigrationTests {
	
	
	@Test
	public void migrationModulesTest() throws IOException, InterruptedException {			
		String expected = Files.readString(Path.of("src/integration-test/resources/docker/db/sql_files/migrations/expected.sql")).trim();
									
		String[] cmd = new String[] {"gradle", "checkIfModulesMigrationWasSuccessful", "-b","src/integration-test/resources/build.gradle"};
		Process process = Runtime.getRuntime().exec(cmd);
		
		String output = new String(process.getInputStream().readAllBytes());
		String actual = output.split("TRUNCATE TABLE\n")[1].split("ENDOFTESTCASE")[0].trim();
		
		process.destroy();
		assertEquals(expected, actual, "Migration failed");
	}
}

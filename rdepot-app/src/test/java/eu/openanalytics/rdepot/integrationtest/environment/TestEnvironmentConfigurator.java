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
package eu.openanalytics.rdepot.integrationtest.environment;

/**
 * Creates conditions for integration tests in the build environment.
 */
public interface TestEnvironmentConfigurator {
    /**
     * Restores container's file system and database to example state.
     * Has to be executed before every test to ensure that the conditions
     * under which every test is executed are exactly the same.
     */
    void restoreEnvironment() throws Exception;

    /**
     * Creates a backup of current database state for declarative mode testing.
     */
    void backupEnvironment() throws Exception;

    /**
     * Restores a clean declarative state after a test is performed.
     */
    void restoreDeclarative() throws Exception;

    static TestEnvironmentConfigurator getDefaultInstance() {
        return BashTestEnvironmentConfigurator.getInstance();
    }

    /**
     * Blocks connection between app and repo containers
     * to test submission with republication failure warning.
     */
    void blockRepoContainer(Runnable testMethod) throws Exception;
}

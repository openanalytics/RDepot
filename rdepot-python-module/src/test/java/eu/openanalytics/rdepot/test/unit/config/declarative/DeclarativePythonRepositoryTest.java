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
package eu.openanalytics.rdepot.test.unit.config.declarative;

import eu.openanalytics.rdepot.base.config.declarative.DeclaredRepositoryDirectoriesProps;
import eu.openanalytics.rdepot.python.config.declarative.DeclarativePythonRepository;
import eu.openanalytics.rdepot.python.config.declarative.PythonYamlDeclarativeConfigurationSource;
import eu.openanalytics.rdepot.python.technology.PythonLanguage;
import eu.openanalytics.rdepot.test.unit.UnitTest;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeclarativePythonRepositoryTest extends UnitTest {

    private static final String TEST_RESOURCES_PATH = "src/test/resources/unit";
    private static final String GOOD_REPOSITORIES =
            TEST_RESOURCES_PATH + "/test_files/declarative_repository_files/good";
    private static final String MISSING_TECHNOLOGY_REPOSITORIES =
            TEST_RESOURCES_PATH + "/test_files/declarative_repository_files/missing_technology";
    private static final String TECHNOLOGY_MISMATCH_REPOSITORIES =
            TEST_RESOURCES_PATH + "/test_files/declarative_repository_files/technology_mismatch";

    @Test
    public void successfullyInitializeDeclarativePythonRepository() throws Exception {
        DeclaredRepositoryDirectoriesProps directories = new DeclaredRepositoryDirectoriesProps();
        directories.setPaths(List.of(GOOD_REPOSITORIES));
        PythonYamlDeclarativeConfigurationSource configurationSource =
                new PythonYamlDeclarativeConfigurationSource(directories);
        List<DeclarativePythonRepository> repositories = configurationSource.retrieveDeclaredRepositories();
        Assertions.assertEquals(3, repositories.size());
        for (DeclarativePythonRepository repository : repositories) {
            Assertions.assertEquals("a", repository.getName());
            Assertions.assertEquals("http://localhost/repo/Python", repository.getPublicationUri());
            Assertions.assertEquals("http://oa-rdepot-repo:8080/Python", repository.getServerAddress());
            Assertions.assertFalse(repository.getPublished());
            Assertions.assertEquals(PythonLanguage.instance, repository.getTechnology());
        }
    }

    @Test
    public void handleMissingTechnologyForDeclarativePythonRepository() throws Exception {
        DeclaredRepositoryDirectoriesProps directories = new DeclaredRepositoryDirectoriesProps();
        directories.setPaths(List.of(MISSING_TECHNOLOGY_REPOSITORIES));
        PythonYamlDeclarativeConfigurationSource configurationSource =
                new PythonYamlDeclarativeConfigurationSource(directories);
        List<DeclarativePythonRepository> repositories = configurationSource.retrieveDeclaredRepositories();
        Assertions.assertEquals(0, repositories.size());
    }

    @Test
    public void handleTechnologyMismatchForDeclarativePythonRepository() throws Exception {
        DeclaredRepositoryDirectoriesProps directories = new DeclaredRepositoryDirectoriesProps();
        directories.setPaths(List.of(TECHNOLOGY_MISMATCH_REPOSITORIES));
        PythonYamlDeclarativeConfigurationSource configurationSource =
                new PythonYamlDeclarativeConfigurationSource(directories);
        List<DeclarativePythonRepository> repositories = configurationSource.retrieveDeclaredRepositories();
        Assertions.assertEquals(0, repositories.size());
    }
}

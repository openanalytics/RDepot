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
package eu.openanalytics.rdepot.test.unit.config.declarative;

import eu.openanalytics.rdepot.base.config.declarative.DeclaredRepositoryDirectoriesProps;
import eu.openanalytics.rdepot.r.config.declarative.RYamlDeclarativeConfigurationSource;
import eu.openanalytics.rdepot.r.mirroring.pojos.MirroredRRepository;
import eu.openanalytics.rdepot.r.technology.RLanguage;
import eu.openanalytics.rdepot.r.validation.repositories.RBasicNameValidator;
import eu.openanalytics.rdepot.test.unit.UnitTest;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeclarativeRRepositoryTest extends UnitTest {
    private static final String TEST_RESOURCES_PATH = "src/test/resources/unit";
    private static final String GOOD_REPOSITORIES =
            TEST_RESOURCES_PATH + "/test_files/declarative_repository_files/good";
    private static final String TECHNOLOGY_MISMATCH_REPOSITORIES =
            TEST_RESOURCES_PATH + "/test_files/declarative_repository_files/technology_mismatch";

    @Test
    public void successfullyInitializeDeclarativeRRepository() throws Exception {
        DeclaredRepositoryDirectoriesProps directories = new DeclaredRepositoryDirectoriesProps();
        directories.setPaths(List.of(GOOD_REPOSITORIES));
        RBasicNameValidator nameValidator = new RBasicNameValidator("[A-Za-z0-9 \\-_.]+", ".+");
        RYamlDeclarativeConfigurationSource configurationSource =
                new RYamlDeclarativeConfigurationSource(directories, nameValidator);
        List<MirroredRRepository> repositories = configurationSource.retrieveDeclaredRepositories();
        Assertions.assertEquals(3, repositories.size());
        for (MirroredRRepository repository : repositories) {
            Assertions.assertEquals("a", repository.getName());
            Assertions.assertEquals("http://localhost/repo/R", repository.getPublicationUri());
            Assertions.assertEquals("http://oa-rdepot-repo:8080/R", repository.getServerAddress());
            Assertions.assertFalse(repository.getPublished());
            Assertions.assertEquals(RLanguage.instance, repository.getTechnology());
        }
    }

    @Test
    public void handleTechnologyMismatchForDeclarativeRRepository() throws Exception {
        DeclaredRepositoryDirectoriesProps directories = new DeclaredRepositoryDirectoriesProps();
        directories.setPaths(List.of(TECHNOLOGY_MISMATCH_REPOSITORIES));
        RBasicNameValidator nameValidator = new RBasicNameValidator("[A-Za-z0-9 \\-_.]+", ".+");
        RYamlDeclarativeConfigurationSource configurationSource =
                new RYamlDeclarativeConfigurationSource(directories, nameValidator);
        List<MirroredRRepository> repositories = configurationSource.retrieveDeclaredRepositories();
        Assertions.assertEquals(0, repositories.size());
    }
}

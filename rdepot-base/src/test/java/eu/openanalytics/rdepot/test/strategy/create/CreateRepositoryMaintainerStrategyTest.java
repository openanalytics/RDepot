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
package eu.openanalytics.rdepot.test.strategy.create;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.deletion.exceptions.NoSuitableMaintainerFound;
import eu.openanalytics.rdepot.base.service.CommonPackageService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.create.CreateRepositoryMaintainerStrategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.test.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.strategy.StrategyTest;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class CreateRepositoryMaintainerStrategyTest extends StrategyTest {

    @Mock
    NewsfeedEventService eventService;

    @Mock
    RepositoryMaintainerService service;

    @Mock
    CommonPackageService packageService;

    private Strategy<RepositoryMaintainer> strategy;

    private Repository repository;
    private RepositoryMaintainer resource;

    private static User user;
    private static List<Package> packagesList;

    @BeforeAll
    public static void init() {
        Repository tmpRepository = RepositoryTestFixture.GET_EXAMPLE_REPOSITORY();

        user = UserTestFixture.GET_PACKAGE_MAINTAINER();
        packagesList = PackageMaintainerTestFixture.GET_LIST_OF_PACKAGE_MAINTAINERS_FOR_REPOSITORY(
                3, tmpRepository, user, "pakckage_name");
    }

    @BeforeEach
    public void initEach() {
        repository = RepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        resource = RepositoryMaintainerTestFixture.GET_REPOSITORY_MAINTAINER_FOR_REPOSITORY(repository);
    }

    /**
     * function that prepare all conditions to run tests for creating a new package
     * maintainer
     */
    private void createRepostioryMaintainer() throws Exception {
        when(packageService.findAllByRepository(repository)).thenReturn(packagesList);

        when(service.create(any())).thenReturn(resource);

        strategy = new CreateRepositoryMaintainerStrategy(
                resource, eventService, service, user, packageService, bestMaintainerChooser);
    }

    @Test
    public void createRepositoryMaintainer_shouldCallServiceCreateMethodOnce() throws Exception {
        doAnswer(invocation -> invocation.getArgument(0)).when(eventService).create(any());
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(user);

        createRepostioryMaintainer();
        strategy.perform();

        verify(service, times(1)).create(resource);
    }

    @Test
    public void createRepositoryMaintainer_shouldCallEventServiceCreateMethodOnce() throws Exception {
        doAnswer(invocation -> invocation.getArgument(0)).when(eventService).create(any());
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(user);

        createRepostioryMaintainer();
        strategy.perform();

        verify(eventService, times(1)).create(any());
    }

    @Test
    public void createRepositoryMaintainer_shoulChooseBestMaintainerForPackagesAfterCreation() throws Exception {
        doAnswer(invocation -> invocation.getArgument(0)).when(eventService).create(any());
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(user);

        createRepostioryMaintainer();
        strategy.perform();

        verify(bestMaintainerChooser, times(3)).chooseBestPackageMaintainer(any());
    }

    @Test
    public void createRepositoryMaintainerWhenCannotChooseBestMaintainerAfterCreation() throws Exception {
        when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenThrow(NoSuitableMaintainerFound.class);

        createRepostioryMaintainer();

        assertThrows(StrategyFailure.class, () -> strategy.perform());
    }
}

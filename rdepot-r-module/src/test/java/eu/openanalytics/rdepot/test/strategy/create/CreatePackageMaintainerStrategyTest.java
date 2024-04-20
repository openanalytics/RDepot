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
package eu.openanalytics.rdepot.test.strategy.create;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.deletion.exceptions.NoSuitableMaintainerFound;
import eu.openanalytics.rdepot.base.service.CommonPackageService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.create.CreatePackageMaintainerStrategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.test.strategy.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.strategy.StrategyTest;

public class CreatePackageMaintainerStrategyTest extends StrategyTest {

	@Mock
	NewsfeedEventService eventService;

	@Mock
	PackageMaintainerService service;

	@Mock
	CommonPackageService packageService;
	
	private Strategy<PackageMaintainer> strategy;

	private RRepository repository;
	private PackageMaintainer resource;

	private static User user;
	private static List<Package> packagesList;

	@BeforeAll
	public static void init() {
		RRepository tmpRepository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();

		user = UserTestFixture.GET_PACKAGE_MAINTAINER();
		packagesList = PackageMaintainerTestFixture.GET_LIST_OF_PACKAGE_MAINTAINERS_FOR_REPOSITORY(3, tmpRepository,
				user, "test_package");
	}

	@BeforeEach
	public void initEach() {
		repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		resource = PackageMaintainerTestFixture.GET_PACKAGE_MAINTAINER_FOR_REPOSITORY(repository);
	}
	
	/**
	 * function that prepare all conditions to run tests for creating a new package maintainer
	 */

	private void createPackageMaintainer() throws Exception {
		when(packageService.findAllByRepository(repository)).thenReturn(packagesList);
		when(service.create(any())).thenReturn(resource);
		
		strategy = new CreatePackageMaintainerStrategy(resource, eventService, service,
				user, packageService, bestMaintainerChooser);
	}


	@Test
	public void createPackageMaintainer_shouldCallServiceCreateMethodOnce() throws Exception {
		doAnswer(invocation -> invocation.getArgument(0)).when(eventService).create(any());
		when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(user);
		
		createPackageMaintainer();
		strategy.perform();

		verify(service, times(1)).create(resource);
	}

	@Test
	public void createPackageMaintainer_shouldCallEventServiceCreateMethodOnce() throws Exception {
		doAnswer(invocation -> invocation.getArgument(0)).when(eventService).create(any());
		when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(user);

		createPackageMaintainer();
		strategy.perform();

		verify(eventService, times(1)).create(any());
	}

	@Test
	public void createPackageMaintainer_shoulChooseBestMaintainerForPackagesAfterCreation() throws Exception {
		doAnswer(invocation -> invocation.getArgument(0)).when(eventService).create(any());
		when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(user);
		
		createPackageMaintainer();
		strategy.perform();

		verify(bestMaintainerChooser, times(2)).chooseBestPackageMaintainer(any());
	}

	@Test
	public void createPackageMaintainerWhenCannotChooseBestMaintainerAfterCreation() throws Exception {
		when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenThrow(NoSuitableMaintainerFound.class);
		
		createPackageMaintainer();
		
		assertThrows(StrategyFailure.class, () -> strategy.perform());
	}

}
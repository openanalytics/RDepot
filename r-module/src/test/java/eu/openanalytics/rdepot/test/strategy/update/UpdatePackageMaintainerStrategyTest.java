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
package eu.openanalytics.rdepot.test.strategy.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.deletion.exceptions.NoSuitableMaintainerFound;
import eu.openanalytics.rdepot.base.service.CommonPackageService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.Service;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.update.UpdatePackageMaintainerStrategy;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.test.strategy.StrategyTest;
import eu.openanalytics.rdepot.r.test.strategy.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.UserTestFixture;

public class UpdatePackageMaintainerStrategyTest extends StrategyTest {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Mock
	NewsfeedEventService eventService;

	@Mock
	Service<PackageMaintainer> service;

	@Mock
	CommonPackageService packageService;
	
	private Strategy<PackageMaintainer> strategy;
	
	private RRepository repository;
	private PackageMaintainer resource;
	private PackageMaintainer updatedMaintainer;
	private RRepository repositoryUpdated;
	
	private static User user;
	private static List<Package<?,?>> packagesList;
	private static List<Package<?,?>> packagesListBeforeUpdate;
	private static List<Package<?,?>> packagesListAfterUpdate;
	private static int oldId;
	private static int newId;
	private static String name;
	private static String updatedName;
	
	@BeforeAll
	public static void init() {
		RRepository tmpRepository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		
		user = UserTestFixture.GET_PACKAGE_MAINTAINER();
		oldId = 123;
		newId = 124;
		updatedName = "different_package_name";
		name = "test_package";
		packagesList = PackageMaintainerTestFixture
				.GET_LIST_OF_PACKAGE_MAINTAINERS_FOR_REPOSITORY(3, tmpRepository, user, updatedName);
		packagesListBeforeUpdate = PackageMaintainerTestFixture
				.GET_LIST_OF_PACKAGE_MAINTAINERS_FOR_REPOSITORY(3, tmpRepository, user, name);
		packagesListAfterUpdate = PackageMaintainerTestFixture
				.GET_LIST_OF_PACKAGE_MAINTAINERS_FOR_REPOSITORY(2, tmpRepository, user, name);
	}
	
	@BeforeEach
	public void initEach() {
		repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repositoryUpdated = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		resource = PackageMaintainerTestFixture.GET_PACKAGE_MAINTAINER_FOR_REPOSITORY(repository);
		updatedMaintainer = new PackageMaintainer(resource);
	}
	

	/**
	 * function that prepare all conditions to run name changing tests
	 */

	private void prepareUpdatePackageName() throws Exception {
		when(packageService.findAllByRepository(repository)).thenReturn(packagesList);
		
		updatedMaintainer.setPackageName(updatedName);
		strategy = new UpdatePackageMaintainerStrategy(resource, eventService, service,
				user, updatedMaintainer, packageService, bestMaintainerChooser);
	}
	
	@Test
	public void updatePackageMaintainer_shouldChangePackageName() throws Exception {	
		doAnswer(invocation -> invocation.getArgument(0)).when(eventService).create(any());
		when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(user);
		
		prepareUpdatePackageName();
		strategy.perform();
		
		assertEquals(resource.getPackageName(), updatedMaintainer.getPackageName());
	}

	@Test
	public void updatePackageMaintainer_shouldRefreshMaintainersAfterPackageNameUpdate() throws Exception {
		doAnswer(invocation -> invocation.getArgument(0)).when(eventService).create(any());
		when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(user);
		
		prepareUpdatePackageName();
		strategy.perform();
		
		verify(bestMaintainerChooser, times(2)).chooseBestPackageMaintainer(any());
	}
	
	@Test
	public void updatePackageMaintainer_shouldFailWhenCannotChooseBestMaintainerAfterUpdatePackageName() throws Exception {
		when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenThrow(NoSuitableMaintainerFound.class);
		
		prepareUpdatePackageName();
		
		assertThrows(StrategyFailure.class, () -> strategy.perform());
	}

	/**
	 * function that prepare all conditions to run repository changing tests
	 */

	private void prepareUpdateRepository() throws Exception {
		repository.setId(oldId);
		repositoryUpdated.setId(newId);
		updatedMaintainer.setRepository(repositoryUpdated);
		
		when(packageService.findAllByRepository(repositoryUpdated)).thenReturn(packagesListAfterUpdate);
		when(packageService.findAllByRepository(repository)).thenReturn(packagesListBeforeUpdate);

		strategy = new UpdatePackageMaintainerStrategy(resource, eventService, service,
				user, updatedMaintainer, packageService, bestMaintainerChooser);
	}

	@Test
	public void updatePackageMaintainer_shouldChangePackageRepository() throws Exception {	
		doAnswer(invocation -> invocation.getArgument(0)).when(eventService).create(any());
		when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(user);
		
		prepareUpdateRepository();
		strategy.perform();
		
		assertEquals(resource.getRepository().getId(), updatedMaintainer.getRepository().getId());
	}

	@Test
	public void updatePackageMaintainer_shouldGroupPacakgesFromOldAndUpdatedRepo() throws Exception {
		doAnswer(invocation -> invocation.getArgument(0)).when(eventService).create(any());
		when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(user);
		
		prepareUpdateRepository();
		strategy.perform();
		
		verify(bestMaintainerChooser, times(3)).chooseBestPackageMaintainer(any());
	}
	
	@Test
	public void updatePackageMaintainer_shouldFailWhenCannotChooseBestMaintainerAfterUpdateRepository() throws Exception {
		when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenThrow(NoSuitableMaintainerFound.class);
		
		prepareUpdateRepository();
		
		assertThrows(StrategyFailure.class, () -> strategy.perform());
		
	}

	/**
	 * function that prepare all conditions to run soft deletion tests
	 */

	private void prepareForSoftDelete() throws Exception {
		when(packageService.findAllByNameAndRepository(updatedMaintainer.getPackageName(), repository)).thenReturn(packagesList);
		
		resource.setDeleted(false);
		updatedMaintainer.setDeleted(true);

		strategy = new UpdatePackageMaintainerStrategy(resource, eventService, service,
				user, updatedMaintainer, packageService, bestMaintainerChooser);
	}

	@Test
	public void updatePackageMaintainer_shouldSoftDeleteMaintainer() throws Exception {
		doAnswer(invocation -> invocation.getArgument(0)).when(eventService).create(any());
		when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(user);
		
		prepareForSoftDelete();
		strategy.perform();
		
		assertEquals(resource.isDeleted(), updatedMaintainer.isDeleted());
	}

	@Test
	public void updatePackageMaintainer_shouldRfreshMaintainersAfterSoftDeletion() throws Exception {
		doAnswer(invocation -> invocation.getArgument(0)).when(eventService).create(any());
		when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenReturn(user);
		
		prepareForSoftDelete();
		strategy.perform();
		
		verify(bestMaintainerChooser, times(3)).chooseBestPackageMaintainer(any());
	}

	@Test
	public void updatePackageMaintainer_shouldFailWhenCannotChooseBestMaintainerAfterSoftDelete() throws Exception {
		when(bestMaintainerChooser.chooseBestPackageMaintainer(any())).thenThrow(NoSuitableMaintainerFound.class);
		
		prepareForSoftDelete();
		
		assertThrows(StrategyFailure.class, () -> strategy.perform());
	}
}

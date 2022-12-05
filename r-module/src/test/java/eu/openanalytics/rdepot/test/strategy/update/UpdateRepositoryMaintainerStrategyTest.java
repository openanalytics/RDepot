/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.deletion.exceptions.NoSuitableMaintainerFound;
import eu.openanalytics.rdepot.base.service.CommonPackageService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.update.UpdateRepositoryMaintainerStrategy;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.test.strategy.StrategyTest;
import eu.openanalytics.rdepot.r.test.strategy.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RepositoryMaintainerTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.UserTestFixture;

public class UpdateRepositoryMaintainerStrategyTest extends StrategyTest {
	@Mock
	NewsfeedEventService eventService;

	@Mock
	RepositoryMaintainerService service;

	@Mock
	CommonPackageService packageService;

	private Strategy<RepositoryMaintainer> strategy;

	private RRepository repository;
	private RepositoryMaintainer resource;
	private RepositoryMaintainer updatedMaintainer;
	private RRepository repositoryUpdated;
	private User userUpdated;
	
	private static User user;
	private static List<Package<?, ?>> packagesListBeforeUpdate;
	private static List<Package<?, ?>> packagesListAfterUpdate;
	private static List<Package<?, ?>> packagesAll;
	private static int oldId;
	private static int newId;

	@BeforeAll
	public static void init() {
		RRepository tmpRepository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();

		user = UserTestFixture.GET_PACKAGE_MAINTAINER();
		oldId = 123;
		newId = 124;

		packagesListBeforeUpdate = PackageMaintainerTestFixture.GET_LIST_OF_PACKAGE_MAINTAINERS_FOR_REPOSITORY(3,
				tmpRepository, user, "pakckage_name");
		packagesListAfterUpdate = PackageMaintainerTestFixture.GET_LIST_OF_PACKAGE_MAINTAINERS_FOR_REPOSITORY(2,
				tmpRepository, user, "package_name");
		packagesAll = Stream.of(packagesListBeforeUpdate, packagesListAfterUpdate).flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	@BeforeEach
	public void initEach() {
		userUpdated = UserTestFixture.GET_PACKAGE_MAINTAINER();
		repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repositoryUpdated = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		resource = RepositoryMaintainerTestFixture.GET_REPOSITORY_MAINTAINER_FOR_REPOSITORY(repository);
		updatedMaintainer = new RepositoryMaintainer(resource);
	}

	/**
	 * function that prepare all conditions to run repository changing tests
	 */

	private void updateRepositoryMaintainerRepositoryId() throws Exception {
		repository.setId(oldId);
		repositoryUpdated.setId(newId);
		updatedMaintainer.setRepository(repositoryUpdated);

		when(packageService.findAllByRepository(updatedMaintainer.getRepository())).thenReturn(packagesListAfterUpdate);
		when(packageService.findAllByRepository(resource.getRepository())).thenReturn(packagesListBeforeUpdate);

		strategy = new UpdateRepositoryMaintainerStrategy(resource, eventService, service, user, updatedMaintainer,
				packageService, bestMaintainerChooser);
	}

	@Test
	public void updateResourceMaintainer_shouldChangeRepsoitory() throws Exception {
		doAnswer(invocation -> invocation.getArgument(0)).when(eventService).create(any());
		doNothing().when(bestMaintainerChooser).refreshMaintainerForPackages(packagesAll);

		updateRepositoryMaintainerRepositoryId();
		strategy.perform();

		Assertions.assertEquals(resource.getRepository(), updatedMaintainer.getRepository());
	}

	@Test
	public void refreshPackageMaintainers_shouldRefreshMaintainersAfterChangeRepository() throws Exception {
		doAnswer(invocation -> invocation.getArgument(0)).when(eventService).create(any());
		doNothing().when(bestMaintainerChooser).refreshMaintainerForPackages(packagesAll);

		updateRepositoryMaintainerRepositoryId();
		strategy.perform();

		verify(packageService, times(2)).findAllByRepository(any());
	}

	@Test
	public void updatePackageMaintainer_shouldFailWhenCannotChooseBestMaintainerAfterChangeRepository()
			throws Exception {
		doThrow(NoSuitableMaintainerFound.class).when(bestMaintainerChooser).refreshMaintainerForPackages(packagesAll);
		updateRepositoryMaintainerRepositoryId();

		assertThrows(StrategyFailure.class, () -> strategy.perform());
	}

	/**
	 * test's if change of user assigned to the repository maintainer goes correctly
	 * 
	 * @throws Exception
	 */

	@Test
	public void updateRepositoryMaintainerUserName() throws Exception {

		user.setId(oldId);
		userUpdated.setId(newId);

		resource.setUser(user);
		updatedMaintainer.setUser(userUpdated);

		strategy = new UpdateRepositoryMaintainerStrategy(resource, eventService, service, user, updatedMaintainer,
				packageService, bestMaintainerChooser);

		strategy.perform();

		assertEquals(resource.getUser(), updatedMaintainer.getUser());
	}

	/**
	 * function that prepare all conditions to run soft deletion tests
	 */

	private void prepareForSoftDelete() throws Exception {
		when(packageService.findAllByRepository(resource.getRepository())).thenReturn(packagesListBeforeUpdate);

		resource.setDeleted(false);
		updatedMaintainer.setDeleted(true);

		strategy = new UpdateRepositoryMaintainerStrategy(resource, eventService, service, user, updatedMaintainer,
				packageService, bestMaintainerChooser);
	}

	@Test
	public void updateResourceMaintainer_shouldSoftDelete() throws Exception {
		doAnswer(invocation -> invocation.getArgument(0)).when(eventService).create(any());
		doNothing().when(bestMaintainerChooser).refreshMaintainerForPackages(any());

		prepareForSoftDelete();
		strategy.perform();

		assertEquals(resource.getRepository(), updatedMaintainer.getRepository());
	}

	@Test
	public void refreshPackageMaintainers_shouldRefreshMaintainersAfterSoftDelete() throws Exception {
		doAnswer(invocation -> invocation.getArgument(0)).when(eventService).create(any());
		doNothing().when(bestMaintainerChooser).refreshMaintainerForPackages(any());

		prepareForSoftDelete();
		strategy.perform();

		verify(packageService, times(1)).findAllByRepository(any());
	}

	@Test
	public void updatePackageMaintainer_shouldFailWhenCannotChooseBestMaintainerAfterSoftDelete() throws Exception {
		doThrow(NoSuitableMaintainerFound.class).when(bestMaintainerChooser)
				.refreshMaintainerForPackages(packagesListBeforeUpdate);

		prepareForSoftDelete();

		assertThrows(StrategyFailure.class, () -> strategy.perform());
	}
}

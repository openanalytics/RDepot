/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
package eu.openanalytics.rdepot.r.test.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import eu.openanalytics.rdepot.base.entities.EventChangedVariable;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.deletion.exceptions.NoSuitableMaintainerFound;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.strategy.update.RPackageUpdateStrategy;
import eu.openanalytics.rdepot.r.test.strategy.answer.AssertEventChangedValuesAnswer;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RPackageTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.UserTestFixture;

public class RPackageStrategyTest extends StrategyTest {
	
	@Mock
	NewsfeedEventService eventService;
	
	@Mock
	RPackageService service;
	
	@Mock
	Storage<RRepository, RPackage> storage;
	
	@Test
	public void updatePackage_shouldRepublishRepository_whenRepositoryIsPublished()
			throws Exception {
		RRepository repository = RRepositoryTestFixture
				.GET_EXAMPLE_REPOSITORY();
		User user = UserTestFixture.GET_PACKAGE_MAINTAINER();
		RPackage packageBag = RPackageTestFixture
				.GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		
		packageBag.setActive(true);
		repository.setPublished(true);
		
		RPackage updatedPackageBag = new RPackage(packageBag);
		updatedPackageBag.setId(0);
		updatedPackageBag.setActive(false);
		
		when(bestMaintainerChooser.chooseBestPackageMaintainer(packageBag))
			.thenReturn(user);
		doAnswer(new Answer<NewsfeedEvent>() {

			@Override
			public NewsfeedEvent answer(InvocationOnMock invocation) 
					throws Throwable {
				NewsfeedEvent event = invocation.getArgument(0);
				return event;
			}
		}).when(eventService).create(any());	
		doNothing().when(repositorySynchronizer)
			.storeRepositoryOnRemoteServer(eq(repository), any());
		doNothing().when(eventService).attachVariables(any(), any());
		
		Strategy<RPackage> strategy = new RPackageUpdateStrategy(
				packageBag, 
				eventService, 
				service, 
				user, 
				updatedPackageBag, 
				storage, 
				bestMaintainerChooser, 
				repositorySynchronizer
			);
		
		strategy.perform();
		
		verify(repositorySynchronizer, times(1)).storeRepositoryOnRemoteServer(eq(repository), any());
	}
	
	@Test
	public void updatePackage_shouldNotRepublishRepository_whenRepositoryIsNotPublished() throws Exception {
		RRepository repository = RRepositoryTestFixture
				.GET_EXAMPLE_REPOSITORY();
		User user = UserTestFixture.GET_PACKAGE_MAINTAINER();
		RPackage packageBag = RPackageTestFixture
				.GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		
		packageBag.setActive(true);
		repository.setPublished(false);
		
		RPackage updatedPackageBag = new RPackage(packageBag);
		updatedPackageBag.setId(0);
		updatedPackageBag.setActive(false);
		
		when(bestMaintainerChooser.chooseBestPackageMaintainer(packageBag))
			.thenReturn(user);
		doAnswer(new Answer<NewsfeedEvent>() {

			@Override
			public NewsfeedEvent answer(InvocationOnMock invocation) 
					throws Throwable {
				NewsfeedEvent event = invocation.getArgument(0);
				return event;
			}
		}).when(eventService).create(any());	
		doNothing().when(eventService).attachVariables(any(), any());
		
		Strategy<RPackage> strategy = new RPackageUpdateStrategy(
				packageBag, 
				eventService, 
				service, 
				user, 
				updatedPackageBag, 
				storage, 
				bestMaintainerChooser, 
				repositorySynchronizer
			);
		
		strategy.perform();
		
		verify(repositorySynchronizer, times(0)).storeRepositoryOnRemoteServer(eq(repository), any());
	}
	
	@Test
	public void updatePackage_shouldRefreshMaintainer_whenActiveStateIsChanged() throws Exception {
		RRepository repository = RRepositoryTestFixture
				.GET_EXAMPLE_REPOSITORY();
		User user = UserTestFixture.GET_PACKAGE_MAINTAINER();
		RPackage packageBag = RPackageTestFixture
				.GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		User newMaintainer = UserTestFixture.GET_ADMIN();
		
		packageBag.setActive(true);
		repository.setPublished(true);
		
		RPackage updatedPackageBag = new RPackage(packageBag);
		updatedPackageBag.setId(0);
		updatedPackageBag.setActive(false);
		
		when(bestMaintainerChooser.chooseBestPackageMaintainer(packageBag))
			.thenReturn(newMaintainer);
		doAnswer(new Answer<NewsfeedEvent>() {

			@Override
			public NewsfeedEvent answer(InvocationOnMock invocation) 
					throws Throwable {
				NewsfeedEvent event = invocation.getArgument(0);
				return event;
			}
		}).when(eventService).create(any());	
		doNothing().when(repositorySynchronizer)
			.storeRepositoryOnRemoteServer(eq(repository), any());
		doNothing().when(eventService).attachVariables(any(), any());
		
		Strategy<RPackage> strategy = new RPackageUpdateStrategy(
				packageBag, 
				eventService, 
				service, 
				user, 
				updatedPackageBag, 
				storage, 
				bestMaintainerChooser, 
				repositorySynchronizer
			);
		
		strategy.perform();
		
		verify(bestMaintainerChooser, times(1)).chooseBestPackageMaintainer(packageBag);
		assertEquals(newMaintainer, packageBag.getUser(), "Maintainer has not been updated for the package.");
	}
	
	@Test
	public void updatePackage_shouldNotRefreshMaintainer_whenOtherPropertyIsChanged() throws Exception {
		RRepository repository = RRepositoryTestFixture
				.GET_EXAMPLE_REPOSITORY();
		User user = UserTestFixture.GET_PACKAGE_MAINTAINER();
		RPackage packageBag = RPackageTestFixture
				.GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		
		packageBag.setDeleted(false);
		repository.setPublished(true);
		
		RPackage updatedPackageBag = new RPackage(packageBag);
		updatedPackageBag.setId(0);
		updatedPackageBag.setDeleted(true);
		
		doAnswer(new Answer<NewsfeedEvent>() {

			@Override
			public NewsfeedEvent answer(InvocationOnMock invocation) 
					throws Throwable {
				NewsfeedEvent event = invocation.getArgument(0);
				return event;
			}
		}).when(eventService).create(any());	
		doNothing().when(repositorySynchronizer)
			.storeRepositoryOnRemoteServer(eq(repository), any());
		doNothing().when(eventService).attachVariables(any(), any());
		
		Strategy<RPackage> strategy = new RPackageUpdateStrategy(
				packageBag, 
				eventService, 
				service, 
				user, 
				updatedPackageBag, 
				storage, 
				bestMaintainerChooser, 
				repositorySynchronizer
			);
		
		strategy.perform();
		
		verify(bestMaintainerChooser, times(0)).chooseBestPackageMaintainer(packageBag);
	}
	
	@Test
	public void updatePackage_shouldFail_whenRepositoryPublicationFails() throws Exception {
		RRepository repository = RRepositoryTestFixture
				.GET_EXAMPLE_REPOSITORY();
		User user = UserTestFixture.GET_PACKAGE_MAINTAINER();
		RPackage packageBag = RPackageTestFixture
				.GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		
		packageBag.setActive(true);
		repository.setPublished(true);
		
		RPackage updatedPackageBag = new RPackage(packageBag);
		updatedPackageBag.setId(0);
		updatedPackageBag.setActive(false);
		
		when(bestMaintainerChooser.chooseBestPackageMaintainer(packageBag))
			.thenReturn(user);
		doAnswer(new Answer<NewsfeedEvent>() {

			@Override
			public NewsfeedEvent answer(InvocationOnMock invocation) 
					throws Throwable {
				NewsfeedEvent event = invocation.getArgument(0);
				return event;
			}
		}).when(eventService).create(any());	
		doThrow(new SynchronizeRepositoryException()).when(repositorySynchronizer)
			.storeRepositoryOnRemoteServer(eq(repository), any());
		doNothing().when(eventService).attachVariables(any(), any());
		
		Strategy<RPackage> strategy = new RPackageUpdateStrategy(
				packageBag, 
				eventService, 
				service, 
				user, 
				updatedPackageBag, 
				storage, 
				bestMaintainerChooser, 
				repositorySynchronizer
			);
		
		assertThrows(StrategyFailure.class, () -> strategy.perform());
	}
	
	@Test
	public void updatePackage_shouldFail_whenNoMaintainerCanBeFound() throws Exception {
		RRepository repository = RRepositoryTestFixture
				.GET_EXAMPLE_REPOSITORY();
		User user = UserTestFixture.GET_PACKAGE_MAINTAINER();
		RPackage packageBag = RPackageTestFixture
				.GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		
		packageBag.setActive(true);
		repository.setPublished(true);
		
		RPackage updatedPackageBag = new RPackage(packageBag);
		updatedPackageBag.setId(0);
		updatedPackageBag.setActive(false);
			
		doThrow(new NoSuitableMaintainerFound()).when(bestMaintainerChooser)
			.chooseBestPackageMaintainer(packageBag);
		
		Strategy<RPackage> strategy = new RPackageUpdateStrategy(
				packageBag, 
				eventService, 
				service, 
				user, 
				updatedPackageBag, 
				storage, 
				bestMaintainerChooser, 
				repositorySynchronizer
			);
		
		assertThrows(StrategyFailure.class, () -> strategy.perform());
	}
	
	@Test
	public void updatePackage_shouldCreateChangedVariables_whenPropertiesAreUpdated() 
			throws Exception {
		RRepository repository = RRepositoryTestFixture
				.GET_EXAMPLE_REPOSITORY();
		User user = UserTestFixture.GET_PACKAGE_MAINTAINER();
		RPackage packageBag = RPackageTestFixture
				.GET_PACKAGE_FOR_REPOSITORY_AND_USER(repository, user);
		
		packageBag.setActive(true);
		packageBag.setDeleted(false);
		repository.setPublished(false);
		
		RPackage updatedPackageBag = new RPackage(packageBag);
		updatedPackageBag.setId(0);
		updatedPackageBag.setActive(false);
		updatedPackageBag.setDeleted(true);
		
		Set<EventChangedVariable> expectedValues = new HashSet<>();
		expectedValues.add(new EventChangedVariable("active", "true", "false"));
		expectedValues.add(new EventChangedVariable("deleted", "false", "true"));
		
		when(bestMaintainerChooser.chooseBestPackageMaintainer(packageBag))
			.thenReturn(user);
		doAnswer(new Answer<NewsfeedEvent>() {

			@Override
			public NewsfeedEvent answer(InvocationOnMock invocation) 
					throws Throwable {
				NewsfeedEvent event = invocation.getArgument(0);
				return event;
			}
		}).when(eventService).create(any());	
		doAnswer(new AssertEventChangedValuesAnswer(expectedValues))
			.when(eventService).attachVariables(any(), any());
		
		Strategy<RPackage> strategy = new RPackageUpdateStrategy(
				packageBag, 
				eventService, 
				service, 
				user, 
				updatedPackageBag, 
				storage, 
				bestMaintainerChooser, 
				repositorySynchronizer
			);
		
		strategy.perform();
	}
}

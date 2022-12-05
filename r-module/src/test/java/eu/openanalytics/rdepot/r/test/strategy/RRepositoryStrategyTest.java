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
package eu.openanalytics.rdepot.r.test.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import eu.openanalytics.rdepot.base.entities.EventChangedVariable;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.strategy.create.RRepositoryCreateStrategy;
import eu.openanalytics.rdepot.r.strategy.update.RRepositoryUpdateStrategy;
import eu.openanalytics.rdepot.r.test.strategy.answer.AssertEventChangedValuesAnswer;
import eu.openanalytics.rdepot.r.test.strategy.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RPackageTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RepositoryMaintainerTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.UserTestFixture;

public class RRepositoryStrategyTest extends StrategyTest {
	
	@Mock
	protected NewsfeedEventService eventService;
	
	@Test
	public void createRepository() throws Exception {
		User requester = UserTestFixture.GET_REGULAR_USER();
		RRepository repository = RRepositoryTestFixture.GET_NEW_REPOSITORY();

		doAnswer(new Answer<RRepository>() {
			@Override
			public RRepository answer(InvocationOnMock invocation) throws Throwable {
				RRepository repository = invocation.getArgument(0);
				RRepository createdRepository = new RRepository(repository);
				createdRepository.setId(9876);
				
				return createdRepository;
			}
		}).when(service).create(repository);
		
		doAnswer(new Answer<NewsfeedEvent>() {
			@Override
			public NewsfeedEvent answer(InvocationOnMock invocation) throws Throwable {
				NewsfeedEvent event = invocation.getArgument(0);
				
				assertEquals(requester, event.getAuthor(), "Requesters do not match.");
				
				//assert related resource
//				RRepository relatedResource = event.getRelatedResource();
//				assertEquals(repository.getName(), relatedResource.getName(), 
//						"Related repositories' names do not match.");
//				assertEquals(repository.getServerAddress(), relatedResource.getServerAddress(), 
//						"Related repositories' addresses do not match.");
//				assertEquals(repository.getPublicationUri(), relatedResource.getPublicationUri(), 
//						"Related repositories' publication URIs do not match.");
				assertNotEquals(0, event.getRelatedResource().getId(), 
						"Related repositories' ID should not be 0 since "
						+ "it should have already been created prior to newsfeed event creation");
				
				return event;
			}
		}).when(eventService).create(any());
		
		Strategy<RRepository> testedStrategy = new RRepositoryCreateStrategy(
				repository, eventService, service, requester);
		
		testedStrategy.perform();

		verify(service, times(1)).create(repository);
		verify(eventService, times(1)).create(any());
	}
	
	@Test
	public void updateRepository_shouldPublish_whenRepositoryIsPublished() throws Exception {
		User requester = UserTestFixture.GET_ADMIN();
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repository.setPublished(true);
		
		RRepository updatedRepository = new RRepository(repository);
		updatedRepository.setId(0);
		updatedRepository.setPublicationUri("https://newuri");
		
		doNothing()
		.when(repositorySynchronizer)
			.storeRepositoryOnRemoteServer(eq(repository), anyString());
		
		Strategy<RRepository> strategy = new RRepositoryUpdateStrategy(
				repository,
				eventService,
				service,
				requester,
				updatedRepository,
				new RRepository(repository),
				repositorySynchronizer,
				repositoryMaintainerService,
				packageMaintainerService,
				packageService
				);
		
		strategy.perform();
		
		verify(repositorySynchronizer, times(1))
			.storeRepositoryOnRemoteServer(eq(repository), anyString());
	}
	
	@Test
	public void updateRepository_shouldThrowStrategyFailure_whenRepublicationFails() 
			throws Exception {
		User requester = UserTestFixture.GET_ADMIN();
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repository.setPublished(true);
		
		RRepository updatedRepository = new RRepository(repository);
		updatedRepository.setId(0);
		updatedRepository.setPublicationUri("https://newuri");
		
		doThrow(new SynchronizeRepositoryException())
		.when(repositorySynchronizer)
			.storeRepositoryOnRemoteServer(eq(repository), anyString());
		
		
		Strategy<RRepository> strategy = new RRepositoryUpdateStrategy(
				repository,
				eventService,
				service,
				requester,
				updatedRepository,
				new RRepository(repository),
				repositorySynchronizer,
				repositoryMaintainerService,
				packageMaintainerService,
				packageService
				);
		
		StrategyFailure exception = assertThrows(StrategyFailure.class, () -> strategy.perform());
		System.out.println(exception.getMessage()); //TODO: assert error message!
	}
	
	@Test
	public void updateRepository_shouldNotRepublish_whenRepositoryIsNotPublished() throws Exception {
		User requester = UserTestFixture.GET_ADMIN();
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repository.setPublished(false);
		
		RRepository updatedRepository = new RRepository(repository);
		updatedRepository.setId(0);
		updatedRepository.setPublicationUri("https://newuri");
		
		Strategy<RRepository> strategy = new RRepositoryUpdateStrategy(
				repository,
				eventService,
				service,
				requester,
				updatedRepository,
				new RRepository(repository),
				repositorySynchronizer,
				repositoryMaintainerService,
				packageMaintainerService,
				packageService
				);
		
		strategy.perform();
		
		verify(repositorySynchronizer, times(0))
			.storeRepositoryOnRemoteServer(eq(repository), anyString());
	}
	
	@Test
	public void updateRepository_shouldCreateNoEvents_whenNothingIsChanged() throws Exception {
		User requester = UserTestFixture.GET_ADMIN();
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repository.setPublished(false);
		
		RRepository updatedRepository = new RRepository(repository);
		updatedRepository.setId(0);
		
		Strategy<RRepository> strategy = new RRepositoryUpdateStrategy(
				repository,
				eventService,
				service,
				requester,
				updatedRepository,
				new RRepository(repository),
				repositorySynchronizer,
				repositoryMaintainerService,
				packageMaintainerService,
				packageService
				);
		
		strategy.perform();
		
		verify(eventService, times(0)).create(any());
		verify(eventService, times(0)).attachVariables(any(), any());
	}
	
	@Test
	public void updateRepository_shouldAddProperChangedVariables_whenPropertiesAreUpdated() 
			throws Exception {
		User requester = UserTestFixture.GET_ADMIN();
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repository.setPublished(true);
		
		RRepository updatedRepository = new RRepository(repository);
		updatedRepository.setId(0);
		updatedRepository.setPublicationUri("https://newuri"); //new uri
		updatedRepository.setServerAddress("192.168.1.101"); //new server address
		
		Set<EventChangedVariable> expectedValues = new HashSet<>();
		expectedValues.add(new EventChangedVariable("publicationUri", "example.com", "https://newuri"));
		expectedValues.add(new EventChangedVariable("serverAddress", "127.0.0.1", "192.168.1.101"));
		
		doNothing()
		.when(repositorySynchronizer)
			.storeRepositoryOnRemoteServer(eq(repository), anyString());
		
		doAnswer(new AssertEventChangedValuesAnswer(expectedValues))
			.when(eventService).attachVariables(any(), any());
		
		doAnswer(new Answer<NewsfeedEvent>() {

			@Override
			public NewsfeedEvent answer(InvocationOnMock invocation) 
					throws Throwable {
				NewsfeedEvent event = invocation.getArgument(0);
				return event;
			}
		}).when(eventService).create(any());	

		Strategy<RRepository> strategy = new RRepositoryUpdateStrategy(
				repository,
				eventService,
				service,
				requester,
				updatedRepository,
				new RRepository(repository),
				repositorySynchronizer,
				repositoryMaintainerService,
				packageMaintainerService,
				packageService
				);
		
		strategy.perform();
	}
	
	@Test
	public void updateRepository_shouldSoftDeletePackagesAndMaintainers_whenRepositoryIsSoftDeleted() 
			throws Exception {
		User requester = UserTestFixture.GET_ADMIN();
		RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repository.setPublished(false);
		
		List<RepositoryMaintainer> repositoryMaintainers = 
				RepositoryMaintainerTestFixture
				.GET_REPOSITORY_MAINTAINERS_FOR_REPOSITORY(3, repository);
		repositoryMaintainers.forEach(m -> m.setDeleted(false));
		when(repositoryMaintainerService.findByRepository(repository)).thenReturn(repositoryMaintainers);
		
		
		List<RPackage> packages = RPackageTestFixture
				.GET_PACKAGES_FOR_REPOSITORY_AND_USER(2, repository, requester);
		packages.forEach(p -> p.setDeleted(false));
		when(packageService.findAllByRepository(repository)).thenReturn(packages);
		
		List<PackageMaintainer> packageMaintainers = 
				PackageMaintainerTestFixture
				.GET_PACKAGE_MAINTAINERS_FOR_REPOSITORY(2, repository);
		packageMaintainers.forEach(m -> m.setDeleted(false));
		when(packageMaintainerService.findByRepositoryNonDeleted(repository)).thenReturn(packageMaintainers);
		
		RRepository updatedRepository = new RRepository(repository);
		updatedRepository.setId(0);
		updatedRepository.setDeleted(true);
		
		Strategy<RRepository> strategy = new RRepositoryUpdateStrategy(
				repository,
				eventService,
				service,
				requester,
				updatedRepository,
				new RRepository(repository),
				repositorySynchronizer,
				repositoryMaintainerService,
				packageMaintainerService,
				packageService
				);
		
		strategy.perform();
		
		repositoryMaintainers.forEach(m -> assertTrue(m.isDeleted(), "Repository Maintainers should be soft deleted as well."));
		packageMaintainers.forEach(m -> assertTrue(m.isDeleted(), "Package Maintainers should be soft deleted as well."));
		packages.forEach(p -> assertTrue(p.isDeleted(), "Packages should be soft deleted as well."));
	}
}

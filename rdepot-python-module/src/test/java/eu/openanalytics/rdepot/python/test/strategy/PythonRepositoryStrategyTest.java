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
package eu.openanalytics.rdepot.python.test.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.openanalytics.rdepot.base.entities.EventChangedVariable;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.strategy.create.PythonRepositoryCreateStrategy;
import eu.openanalytics.rdepot.python.strategy.update.PythonRepositoryUpdateStrategy;
import eu.openanalytics.rdepot.python.test.strategy.answer.AssertEventChangedValuesAnswer;
import eu.openanalytics.rdepot.test.fixture.PythonRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class PythonRepositoryStrategyTest extends StrategyTest {

    @Mock
    protected NewsfeedEventService eventService;

    @Test
    public void createRepository() throws Exception {
        User requester = UserTestFixture.GET_REGULAR_USER();
        PythonRepository repository = PythonRepositoryTestFixture.GET_NEW_REPOSITORY();

        doAnswer(new Answer<PythonRepository>() {
                    @Override
                    public PythonRepository answer(InvocationOnMock invocation) throws Throwable {
                        PythonRepository repository = invocation.getArgument(0);
                        PythonRepository createdRepository = new PythonRepository(repository);
                        createdRepository.setId(9876);

                        return createdRepository;
                    }
                })
                .when(service)
                .create(repository);

        doAnswer(new Answer<NewsfeedEvent>() {
                    @Override
                    public NewsfeedEvent answer(InvocationOnMock invocation) throws Throwable {
                        NewsfeedEvent event = invocation.getArgument(0);

                        assertEquals(requester, event.getAuthor(), "Requesters do not match.");
                        assertNotEquals(
                                0,
                                event.getRelatedResource().getId(),
                                "Related repositories' ID should not be 0 since "
                                        + "it should have already been created prior to newsfeed event creation");

                        return event;
                    }
                })
                .when(eventService)
                .create(any());

        Strategy<PythonRepository> testedStrategy =
                new PythonRepositoryCreateStrategy(repository, eventService, service, requester);

        testedStrategy.perform();

        verify(service, times(1)).create(repository);
        verify(eventService, times(1)).create(any());
    }

    @Test
    public void updateRepository_shouldPublish_whenRepositoryIsPublished() throws Exception {
        User requester = UserTestFixture.GET_REGULAR_USER();
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(true);

        PythonRepository updatedRepository = new PythonRepository(repository);
        updatedRepository.setId(0);
        updatedRepository.setPublicationUri("https://newuri");

        doNothing().when(repositorySynchronizer).storeRepositoryOnRemoteServer(eq(repository));

        Strategy<PythonRepository> strategy = new PythonRepositoryUpdateStrategy(
                repository,
                eventService,
                service,
                requester,
                updatedRepository,
                new PythonRepository(repository),
                repositorySynchronizer,
                repositoryMaintainerService,
                packageMaintainerService,
                packageService);

        strategy.perform();

        verify(repositorySynchronizer, times(1)).storeRepositoryOnRemoteServer(eq(repository));
    }

    @Test
    public void updateRepository_shouldThrowStrategyFailure_whenRepublicationFails() throws Exception {
        User requester = UserTestFixture.GET_REGULAR_USER();
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(true);

        PythonRepository updatedRepository = new PythonRepository(repository);
        updatedRepository.setId(0);
        updatedRepository.setPublicationUri("https://newuri");

        doThrow(new SynchronizeRepositoryException())
                .when(repositorySynchronizer)
                .storeRepositoryOnRemoteServer(eq(repository));

        Strategy<PythonRepository> strategy = new PythonRepositoryUpdateStrategy(
                repository,
                eventService,
                service,
                requester,
                updatedRepository,
                new PythonRepository(repository),
                repositorySynchronizer,
                repositoryMaintainerService,
                packageMaintainerService,
                packageService);

        StrategyFailure exception = assertThrows(
                StrategyFailure.class,
                () -> strategy.perform(),
                "Exception should be thrown when strategy fails to" + "update the repository.");
        String expectedMessage = MessageCodes.STRATEGY_FAILURE + ": " + MessageCodes.COULD_NOT_SYNCHRONIZE_REPOSITORY;
        assertEquals(expectedMessage, exception.getMessage(), "could.not.synchronize.repository should be thrown");
    }

    @Test
    public void updateRepository_shouldNotRepublish_whenRepositoryIsNotPublished() throws Exception {
        User requester = UserTestFixture.GET_REGULAR_USER();
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);

        PythonRepository updatedRepository = new PythonRepository(repository);
        updatedRepository.setId(0);
        updatedRepository.setPublicationUri("https://newuri");

        Strategy<PythonRepository> strategy = new PythonRepositoryUpdateStrategy(
                repository,
                eventService,
                service,
                requester,
                updatedRepository,
                new PythonRepository(repository),
                null,
                repositoryMaintainerService,
                packageMaintainerService,
                packageService);

        strategy.perform();

        verify(repositorySynchronizer, times(0)).storeRepositoryOnRemoteServer(eq(repository), anyString());
    }

    @Test
    public void updateRepository_shouldCreateNoEvents_whenNothingIsChanged() throws Exception {
        User requester = UserTestFixture.GET_REGULAR_USER();
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setPublished(false);

        PythonRepository updatedRepository = new PythonRepository(repository);
        updatedRepository.setId(0);

        Strategy<PythonRepository> strategy = new PythonRepositoryUpdateStrategy(
                repository,
                eventService,
                service,
                requester,
                updatedRepository,
                new PythonRepository(repository),
                null,
                repositoryMaintainerService,
                packageMaintainerService,
                packageService);

        strategy.perform();

        verify(eventService, times(0)).create(any());
        verify(eventService, times(0)).attachVariables(any(), any());
    }

    @Test
    public void updateRepository_shouldAddProperChangedVariables_whenPropertiesAreUpdated() throws Exception {
        User requester = UserTestFixture.GET_REGULAR_USER();
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();

        PythonRepository updatedRepository = new PythonRepository(repository);
        updatedRepository.setId(0);
        updatedRepository.setPublicationUri("https://newuri"); // new uri
        updatedRepository.setServerAddress("192.168.1.101"); // new server address

        Set<EventChangedVariable> expectedValues = new HashSet<>();
        expectedValues.add(
                new EventChangedVariable("publicationUri", "http://localhost/repo/testrepo", "https://newuri"));
        expectedValues.add(new EventChangedVariable("serverAddress", "http://192.168.1.100/testrepo", "192.168.1.101"));

        doNothing().when(repositorySynchronizer).storeRepositoryOnRemoteServer(eq(repository));

        doAnswer(new AssertEventChangedValuesAnswer(expectedValues))
                .when(eventService)
                .attachVariables(any(), any());

        doAnswer(new Answer<NewsfeedEvent>() {

                    @Override
                    public NewsfeedEvent answer(InvocationOnMock invocation) throws Throwable {
                        NewsfeedEvent event = invocation.getArgument(0);
                        return event;
                    }
                })
                .when(eventService)
                .create(any());

        Strategy<PythonRepository> strategy = new PythonRepositoryUpdateStrategy(
                repository,
                eventService,
                service,
                requester,
                updatedRepository,
                new PythonRepository(repository),
                repositorySynchronizer,
                repositoryMaintainerService,
                packageMaintainerService,
                packageService);

        strategy.perform();
    }
}

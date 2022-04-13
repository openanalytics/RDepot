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
package eu.openanalytics.rdepot.test.unit;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryEvent;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.RepositoryEventRepository;
import eu.openanalytics.rdepot.service.RepositoryEventService;
import eu.openanalytics.rdepot.service.RepositoryService;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryEventServiceTest {
	
	@InjectMocks
	RepositoryEventService repositoryEventService;
	
	@Mock
	RepositoryEventRepository repositoryEventRepository;
	
	@Mock
	RepositoryService repositoryService;
	
	@Test
	public void testCreateNewRepository() {
		Mockito.when(repositoryService.findById(123)).thenReturn(null);
		Repository newRepository = new Repository(123, "http://example.org/repo", "newrepo", "127.0.0.1", true, false);
		User user = new User(1, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);
		Event event = new Event(1, "create");
		RepositoryEvent expectedEvent = new RepositoryEvent(0, new Date(), user, newRepository, event, "created", "", "", new Date());
		
		Mockito.when(repositoryEventRepository.save(Mockito.any())).thenAnswer(new Answer<RepositoryEvent>() {
			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (RepositoryEvent) args[0];
			}
		});
		
		List<RepositoryEvent> events = repositoryEventService.create(event, user, newRepository);
		RepositoryEvent createEvent = events.get(0);
		assertEquals(expectedEvent.getId(), createEvent.getId());
		assertEquals(expectedEvent.getChangedBy(), createEvent.getChangedBy());
		assertEquals(expectedEvent.getChangedVariable(), createEvent.getChangedVariable());
		assertEquals(expectedEvent.getEvent().getValue(), createEvent.getEvent().getValue());
		assertEquals(expectedEvent.getRepository().getId(), createEvent.getRepository().getId());
		assertEquals(expectedEvent.getValueAfter(), createEvent.getValueAfter());
		assertEquals(expectedEvent.getValueBefore(), createEvent.getValueBefore());
	}
	
	@Test
	public void testUpdateRepository() {
		Repository oldRepository = new Repository(123, "http://example.org/repo", "oldrepo", "127.0.0.1", true, false);
		oldRepository.setVersion(1);
		Repository newRepository = new Repository(123, "http://example.org/repo2", "newrepo", "128.1.1.2", true, false);
		newRepository.setVersion(2);
		User user = new User(1, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);
		Event event = new Event(1, "update");
		
		RepositoryEvent versionEvent = new RepositoryEvent(0, new Date(), user, newRepository, event, "version", "" + oldRepository.getVersion(), "" + newRepository.getVersion(), new Date());
		RepositoryEvent publicationUriEvent = new RepositoryEvent(0, new Date(), user, newRepository, event, "publication URI", oldRepository.getPublicationUri(), newRepository.getPublicationUri(), new Date());
		RepositoryEvent serverAddressEvent = new RepositoryEvent(0, new Date(), user, newRepository, event, "server address", oldRepository.getServerAddress(), newRepository.getServerAddress(), new Date());
		RepositoryEvent nameEvent = new RepositoryEvent(0, new Date(), user, newRepository, event, "name", oldRepository.getName(), newRepository.getName(), new Date());
		
		List<RepositoryEvent> expectedEvents = new ArrayList<>();
		expectedEvents.add(versionEvent);
		expectedEvents.add(publicationUriEvent);
		expectedEvents.add(serverAddressEvent);
		expectedEvents.add(nameEvent);
		
		Mockito.when(repositoryEventRepository.save(Mockito.any())).thenAnswer(new Answer<RepositoryEvent>() {
			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (RepositoryEvent) args[0];
			}
		});
		
		Mockito.when(repositoryService.findById(123)).thenReturn(oldRepository);
		
		List<RepositoryEvent> events = repositoryEventService.create(event, user, newRepository);
		
		for(int i = 0; i < events.size(); i++) {
			assertEquals(expectedEvents.get(i).getId(), events.get(i).getId());
			assertEquals(expectedEvents.get(i).getChangedBy(), events.get(i).getChangedBy());
			assertEquals(expectedEvents.get(i).getChangedVariable(), events.get(i).getChangedVariable());
			assertEquals(expectedEvents.get(i).getEvent().getValue(), events.get(i).getEvent().getValue());
			assertEquals(expectedEvents.get(i).getRepository().getId(), events.get(i).getRepository().getId());
			assertEquals(expectedEvents.get(i).getValueAfter(), events.get(i).getValueAfter());
			assertEquals(expectedEvents.get(i).getValueBefore(), events.get(i).getValueBefore());
		}
	}
	
	@Test
	public void testDeleteRepository() {
		Mockito.when(repositoryService.findById(123)).thenReturn(null);
		Repository newRepository = new Repository(123, "http://example.org/repo", "newrepo", "127.0.0.1", true, false);
		User user = new User(1, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);
		Event event = new Event(1, "delete");
		RepositoryEvent expectedEvent = new RepositoryEvent(0, new Date(), user, newRepository, event, "deleted", "", "", new Date());
		
		Mockito.when(repositoryEventRepository.save(Mockito.any())).thenAnswer(new Answer<RepositoryEvent>() {
			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (RepositoryEvent) args[0];
			}
		});
		
		List<RepositoryEvent> events = repositoryEventService.create(event, user, newRepository);
		RepositoryEvent deleteEvent = events.get(0);
		assertEquals(expectedEvent.getId(), deleteEvent.getId());
		assertEquals(expectedEvent.getChangedBy(), deleteEvent.getChangedBy());
		assertEquals(expectedEvent.getChangedVariable(), deleteEvent.getChangedVariable());
		assertEquals(expectedEvent.getEvent().getValue(), deleteEvent.getEvent().getValue());
		assertEquals(expectedEvent.getRepository().getId(), deleteEvent.getRepository().getId());
		assertEquals(expectedEvent.getValueAfter(), deleteEvent.getValueAfter());
		assertEquals(expectedEvent.getValueBefore(), deleteEvent.getValueBefore());
	}
}

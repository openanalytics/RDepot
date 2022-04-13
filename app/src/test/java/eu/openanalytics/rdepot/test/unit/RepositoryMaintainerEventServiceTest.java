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
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.RepositoryMaintainerEvent;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.RepositoryMaintainerEventRepository;
import eu.openanalytics.rdepot.service.RepositoryMaintainerEventService;
import eu.openanalytics.rdepot.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.test.unit.time.TestDateRule;
import eu.openanalytics.rdepot.time.DateProvider;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryMaintainerEventServiceTest {

	@InjectMocks
	RepositoryMaintainerEventService repositoryMaintainerEventService;
	
	@Mock
	RepositoryMaintainerEventRepository repositoryMaintainerEventRepository;
	
	@Mock
	RepositoryMaintainerService repositoryMaintainerService;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Rule
	public TestDateRule testDateRule = new TestDateRule();
	
	@Test
	public void testCreateRepositoryMaintainer() {
		Event event = new Event(1, "create");
		User creator = new User(2, new Role(1, 2, "admin", "Administrator"), "tesla", "tesla@example.org", "tesla", true, false);
		
		Repository repository = new Repository(123, "http://example.org/repo", "newrepo", "127.0.0.1", true, false);
		User user = new User(1, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);
		
		RepositoryMaintainer maintainer = new RepositoryMaintainer(123, user, repository, false);
		
		Mockito.when(repositoryMaintainerService.findById(Mockito.anyInt())).thenReturn(null);
		Mockito.when(repositoryMaintainerEventRepository.save(Mockito.any())).thenAnswer(new Answer<RepositoryMaintainerEvent>() {
			@Override
			public RepositoryMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (RepositoryMaintainerEvent) args[0];
			}
		});
		
		RepositoryMaintainerEvent expectedEvent = new RepositoryMaintainerEvent(0, DateProvider.now(), creator, maintainer, event, "created", "", "", DateProvider.now());
		
		List<RepositoryMaintainerEvent> events = repositoryMaintainerEventService.create(event, creator, maintainer);
		RepositoryMaintainerEvent createEvent = events.get(0);
		
		assertEquals(expectedEvent.getChangedBy().getName(), createEvent.getChangedBy().getName());
		assertEquals(expectedEvent.getChangedVariable(), createEvent.getChangedVariable());
		assertEquals(expectedEvent.getEvent().getValue(), createEvent.getEvent().getValue());
		assertEquals(expectedEvent.getId(), createEvent.getId());
		assertEquals(expectedEvent.getRepositoryMaintainer().getId(), createEvent.getRepositoryMaintainer().getId());
		assertEquals(expectedEvent.getValueAfter(), createEvent.getValueAfter());
		assertEquals(expectedEvent.getValueBefore(), createEvent.getValueBefore());
	}
	
	@Test
	public void testUpdateRepositoryMaintainer() {
		Event event = new Event(1, "update");
		User creator = new User(2, new Role(1, 2, "admin", "Administrator"), "tesla", "tesla@example.org", "tesla", true, false);
		
		Repository oldRepository = new Repository(123, "http://example.org/repo", "newrepo", "127.0.0.1", true, false);
		User oldUser = new User(123, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);
		RepositoryMaintainer oldMaintainer = new RepositoryMaintainer(123, oldUser, oldRepository, false);

		Repository newRepository = new Repository(234, "http://example.org/repo", "newrepo", "127.0.0.1", true, false);
		User newUser = new User(234, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);
		RepositoryMaintainer newMaintainer = new RepositoryMaintainer(123, newUser, newRepository, false);
		
		Mockito.when(repositoryMaintainerService.findById(Mockito.anyInt())).thenReturn(oldMaintainer);
		Mockito.when(repositoryMaintainerEventRepository.save(Mockito.any())).thenAnswer(new Answer<RepositoryMaintainerEvent>() {
			@Override
			public RepositoryMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (RepositoryMaintainerEvent) args[0];
			}
		});
		
		RepositoryMaintainerEvent userEvent = new RepositoryMaintainerEvent(0, DateProvider.now(), creator, newMaintainer, event, "user", "" + oldMaintainer.getUser().getId(), "" + newMaintainer.getUser().getId(), DateProvider.now());
		RepositoryMaintainerEvent repositoryEvent = new RepositoryMaintainerEvent(0, DateProvider.now(), creator, newMaintainer, event, "repository", "" + oldMaintainer.getRepository().getId(), "" + newMaintainer.getRepository().getId(), DateProvider.now());
		List<RepositoryMaintainerEvent> expectedEvents = new ArrayList<>();
		expectedEvents.add(userEvent);
		expectedEvents.add(repositoryEvent);
		
		List<RepositoryMaintainerEvent> events = repositoryMaintainerEventService.create(event, creator, newMaintainer);
		
		for(int i = 0; i < events.size(); i++) {
			RepositoryMaintainerEvent expectedEvent = expectedEvents.get(i);
			RepositoryMaintainerEvent updateEvent = events.get(i);
			
			assertEquals(expectedEvent.getChangedBy().getName(), updateEvent.getChangedBy().getName());
			assertEquals(expectedEvent.getChangedVariable(), updateEvent.getChangedVariable());
			assertEquals(expectedEvent.getEvent().getValue(), updateEvent.getEvent().getValue());
			assertEquals(expectedEvent.getId(), updateEvent.getId());
			assertEquals(expectedEvent.getRepositoryMaintainer().getId(), updateEvent.getRepositoryMaintainer().getId());
			assertEquals(expectedEvent.getValueAfter(), updateEvent.getValueAfter());
			assertEquals(expectedEvent.getValueBefore(), updateEvent.getValueBefore());
		}
	}
	
	@Test
	public void testDeleteRepositoryMaintainer() {
		Event event = new Event(1, "delete");
		User creator = new User(2, new Role(1, 2, "admin", "Administrator"), "tesla", "tesla@example.org", "tesla", true, false);
		
		Repository repository = new Repository(123, "http://example.org/repo", "newrepo", "127.0.0.1", true, false);
		User user = new User(1, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);
		
		RepositoryMaintainer maintainer = new RepositoryMaintainer(123, user, repository, false);
		
		Mockito.when(repositoryMaintainerService.findById(Mockito.anyInt())).thenReturn(null);
		Mockito.when(repositoryMaintainerEventRepository.save(Mockito.any())).thenAnswer(new Answer<RepositoryMaintainerEvent>() {
			@Override
			public RepositoryMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (RepositoryMaintainerEvent) args[0];
			}
		});
		
		RepositoryMaintainerEvent expectedEvent = new RepositoryMaintainerEvent(0, DateProvider.now(), creator, maintainer, event, "deleted", "", DateProvider.nowString(), DateProvider.now());
		
		List<RepositoryMaintainerEvent> events = repositoryMaintainerEventService.create(event, creator, maintainer);
		RepositoryMaintainerEvent deleteEvent = events.get(0);
		
		assertEquals(expectedEvent.getChangedBy().getName(), deleteEvent.getChangedBy().getName());
		assertEquals(expectedEvent.getChangedVariable(), deleteEvent.getChangedVariable());
		assertEquals(expectedEvent.getEvent().getValue(), deleteEvent.getEvent().getValue());
		assertEquals(expectedEvent.getId(), deleteEvent.getId());
		assertEquals(expectedEvent.getRepositoryMaintainer().getId(), deleteEvent.getRepositoryMaintainer().getId());
		assertEquals(expectedEvent.getValueAfter(), deleteEvent.getValueAfter());
		assertEquals(expectedEvent.getValueBefore(), deleteEvent.getValueBefore());
	}
}

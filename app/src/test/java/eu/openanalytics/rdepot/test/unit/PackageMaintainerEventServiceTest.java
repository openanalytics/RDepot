/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.PackageMaintainerEvent;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.PackageMaintainerEventRepository;
import eu.openanalytics.rdepot.service.PackageMaintainerEventService;
import eu.openanalytics.rdepot.service.PackageMaintainerService;

@RunWith(MockitoJUnitRunner.class)
public class PackageMaintainerEventServiceTest {

	@InjectMocks
	PackageMaintainerEventService packageMaintainerEventService;
	
	@Mock
	PackageMaintainerService packageMaintainerService;
	
	@Mock
	PackageMaintainerEventRepository packageMaintainerEventRepository;
	
	@Test
	public void testCreatePackageMaintainer() {
		Event event = new Event(1, "create");
		User creator = new User(2, new Role(1, 2, "admin", "Administrator"), "tesla", "tesla@example.org", "tesla", true, false);

		Repository repository = new Repository(123, "http://example.org/repo", "newrepo", "127.0.0.1", true, false);
		User user = new User(1, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);

		Mockito.when(packageMaintainerService.findById(Mockito.anyInt())).thenReturn(null);
		PackageMaintainer maintainer = new PackageMaintainer(123, user, repository, "package1", false);
		
		Mockito.when(packageMaintainerEventRepository.save(Mockito.any())).thenAnswer(new Answer<PackageMaintainerEvent>() {
			@Override
			public PackageMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (PackageMaintainerEvent) args[0];
			}
		});
		
		PackageMaintainerEvent expectedEvent = new PackageMaintainerEvent(0, new Date(), creator, maintainer, event, "created", "", "", new Date());
		
		List<PackageMaintainerEvent> events = packageMaintainerEventService.create(event, creator, maintainer);
		PackageMaintainerEvent createEvent = events.get(0);
		
		assertEquals(expectedEvent.getChangedBy().getName(), createEvent.getChangedBy().getName());
		assertEquals(expectedEvent.getChangedVariable(), createEvent.getChangedVariable());
		assertEquals(expectedEvent.getEvent().getValue(), createEvent.getEvent().getValue());
		assertEquals(expectedEvent.getId(), createEvent.getId());
		assertEquals(expectedEvent.getPackageMaintainer().getId(), createEvent.getPackageMaintainer().getId());
		assertEquals(expectedEvent.getValueAfter(), createEvent.getValueAfter());
		assertEquals(expectedEvent.getValueBefore(), createEvent.getValueBefore());
	}
	
	@Test
	public void testUpdatePackageMaintainer() {
		Event event = new Event(1, "update");
		User creator = new User(2, new Role(1, 2, "admin", "Administrator"), "tesla", "tesla@example.org", "tesla", true, false);
		
		Repository oldRepository = new Repository(123, "http://example.org/repo", "newrepo", "127.0.0.1", true, false);
		User oldUser = new User(123, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);

		Repository newRepository = new Repository(234, "http://example.org/repo", "newrepo", "127.0.0.1", true, false);
		User newUser = new User(234, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);
	
		PackageMaintainer oldMaintainer = new PackageMaintainer(123, oldUser, oldRepository, "package1", false);
		PackageMaintainer newMaintainer = new PackageMaintainer(234, newUser, newRepository, "package2", false);
		
		Mockito.when(packageMaintainerService.findById(Mockito.anyInt())).thenReturn(oldMaintainer);
		Mockito.when(packageMaintainerEventRepository.save(Mockito.any())).thenAnswer(new Answer<PackageMaintainerEvent>() {
			@Override
			public PackageMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (PackageMaintainerEvent) args[0];
			}
		});
		
		PackageMaintainerEvent userEvent = new PackageMaintainerEvent(0, new Date(), creator, newMaintainer, event, "user", "" + oldMaintainer.getUser().getId(), "" + newMaintainer.getUser().getId(), new Date());
		PackageMaintainerEvent repositoryEvent = new PackageMaintainerEvent(0, new Date(), creator, newMaintainer, event, "repository", "" + oldMaintainer.getRepository().getId(), "" + newMaintainer.getRepository().getId(), new Date());
		PackageMaintainerEvent packageEvent = new PackageMaintainerEvent(0, new Date(), creator, newMaintainer, event, "package", oldMaintainer.getPackage(), newMaintainer.getPackage(), new Date());
		List<PackageMaintainerEvent> expectedEvents = new ArrayList<>();
		expectedEvents.add(userEvent);
		expectedEvents.add(repositoryEvent);
		expectedEvents.add(packageEvent);
		
		List<PackageMaintainerEvent> events = packageMaintainerEventService.create(event, creator, newMaintainer);
		for(int i = 0; i < events.size(); i++) {
			PackageMaintainerEvent updateEvent = events.get(i);
			PackageMaintainerEvent expectedEvent = expectedEvents.get(i);
			
			assertEquals(expectedEvent.getChangedBy().getName(), updateEvent.getChangedBy().getName());
			assertEquals(expectedEvent.getChangedVariable(), updateEvent.getChangedVariable());
			assertEquals(expectedEvent.getEvent().getValue(), updateEvent.getEvent().getValue());
			assertEquals(expectedEvent.getId(), updateEvent.getId());
			assertEquals(expectedEvent.getPackageMaintainer().getId(), updateEvent.getPackageMaintainer().getId());
			assertEquals(expectedEvent.getValueAfter(), updateEvent.getValueAfter());
			assertEquals(expectedEvent.getValueBefore(), updateEvent.getValueBefore());
		}
	}
}

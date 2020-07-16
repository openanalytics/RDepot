/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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

import org.junit.Before;
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
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageEvent;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.PackageEventRepository;
import eu.openanalytics.rdepot.service.EventService;
import eu.openanalytics.rdepot.service.PackageEventService;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.RepositoryEventService;

@RunWith(MockitoJUnitRunner.class)
public class PackageEventServiceTest {
	
	@InjectMocks
	PackageEventService packageEventService;
	
	@Mock
	PackageEventRepository packageEventRepository;
	
	@Mock
	PackageService packageService;
	
	@Mock
	EventService eventService;
	
	@Mock
	RepositoryEventService repositoryEventService;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
	}
	
	
	@Test
	public void testCreateNewPackage() {
		Mockito.when(packageService.findById(Mockito.anyInt())).thenReturn(null);
		//Mockito.when(repositoryEventService.create(Mockito.any())).thenReturn(null);
		
		Event event = new Event(1, "create");
		Repository repository = new Repository(123, "http://example.org/repo", "newrepo", "127.0.0.1", true, false);
		User user = new User(1, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);
		Package packageBag = new Package(1, repository, user, "example", "example package", "einstein", "some license", "some source", "example", "md5", true, false);
		
		PackageEvent expectedEvent = new PackageEvent(0, new Date(), user, packageBag, event, "created", "", "", new Date());
		
		Mockito.when(packageEventRepository.save(Mockito.any())).thenAnswer(new Answer<PackageEvent>() {
			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (PackageEvent) args[0];
			}
		});
		
		List<PackageEvent> events = packageEventService.create(event, user, packageBag);
		PackageEvent createEvent = events.get(0);
		
		assertEquals(expectedEvent.getChangedBy().getName(), createEvent.getChangedBy().getName());
		assertEquals(expectedEvent.getChangedVariable(), createEvent.getChangedVariable());
		assertEquals(expectedEvent.getEvent().getValue(), createEvent.getEvent().getValue());
		assertEquals(expectedEvent.getId(), createEvent.getId());
		assertEquals(expectedEvent.getPackage().getName(), createEvent.getPackage().getName());
		assertEquals(expectedEvent.getValueAfter(), createEvent.getValueAfter());
		assertEquals(expectedEvent.getValueBefore(), createEvent.getValueBefore());
	}
	
	@Test
	public void testUpdatePackage() {
		Mockito.when(repositoryEventService.create(Mockito.any())).thenReturn(null);
		User user = new User(3, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);

		
		Event event = new Event(1, "update");
		Repository oldRepository = new Repository(123, "http://example.org/repo", "newrepo", "127.0.0.1", true, false);
		User oldUser = new User(1, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);
		Package oldPackage = new Package(123, oldRepository, oldUser, "example", "example package", "einstein", "some license", "some source", "example", "md5", false, false);
		oldPackage.setVersion("1");
		oldPackage.setDepends("dependency1");
		oldPackage.setImports("import1, import2");
		oldPackage.setSuggests("suggestion1, suggestion2");
		oldPackage.setSystemRequirements("windows");
		oldPackage.setUrl("http://example.org/repo1");
		
		Mockito.when(packageService.findById(123)).thenReturn(oldPackage);
		
		Repository newRepository = new Repository(234, "http://example.org/repo2", "newrepo2", "127.0.0.111", true, false);
		User newUser = new User(2, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);
		Package newPackage = new Package(123, newRepository, newUser, "example2", "example package2", "tesla", "some other license", "some other source", "example2", "md5555", true, false);
		newPackage.setVersion("2");
		newPackage.setDepends("dependency1, dependency2");
		newPackage.setImports("import2, import3");
		newPackage.setSuggests("suggestion3, suggestion4");
		newPackage.setSystemRequirements("linux");
		newPackage.setUrl("http://example.org/repo2");
		
		Mockito.when(packageEventRepository.save(Mockito.any())).thenAnswer(new Answer<PackageEvent>() {
			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (PackageEvent) args[0];
			}
		});
		
		PackageEvent versionEvent = new PackageEvent(0, new Date(), user, newPackage, event, "version", oldPackage.getVersion(), newPackage.getVersion() ,new Date());
		PackageEvent repositoryEvent = new PackageEvent(0, new Date(), user, newPackage, event, "repository", "" + oldPackage.getRepository().getId(), "" + newPackage.getRepository().getId(), new Date());
		PackageEvent userEvent = new PackageEvent(0, new Date(), user, newPackage, event, "maintainer", "" + oldPackage.getUser().getId(), "" + newPackage.getUser().getId(), new Date());
		PackageEvent nameEvent = new PackageEvent(0, new Date(), user, newPackage, event, "name", oldPackage.getName(), newPackage.getName(), new Date());
		PackageEvent descriptionEvent = new PackageEvent(0, new Date(), user, newPackage, event, "description", oldPackage.getDescription(), newPackage.getDescription(), new Date());
		PackageEvent authorEvent = new PackageEvent(0, new Date(), user, newPackage, event, "author", oldPackage.getAuthor(), newPackage.getAuthor(), new Date());
		PackageEvent dependsEvent = new PackageEvent(0, new Date(), user, newPackage, event, "depends", oldPackage.getDepends(), newPackage.getDepends(), new Date());
		PackageEvent importsEvent = new PackageEvent(0, new Date(), user, newPackage, event, "imports", oldPackage.getImports(), newPackage.getImports(), new Date());
		PackageEvent suggestsEvent = new PackageEvent(0, new Date(), user, newPackage, event, "suggests", oldPackage.getSuggests(), newPackage.getSuggests(), new Date());
		PackageEvent systemRequirementsEvent = new PackageEvent(0, new Date(), user, newPackage, event, "system requirements", oldPackage.getSystemRequirements(), newPackage.getSystemRequirements(), new Date());
		PackageEvent licenseEvent = new PackageEvent(0, new Date(), user, newPackage, event, "license", oldPackage.getLicense(), newPackage.getLicense(), new Date());
		PackageEvent titleEvent = new PackageEvent(0, new Date(), user, newPackage, event, "title", oldPackage.getTitle(), newPackage.getTitle(), new Date());
		PackageEvent urlEvent = new PackageEvent(0, new Date(), user, newPackage, event, "URL", oldPackage.getUrl(), newPackage.getUrl(), new Date());
		PackageEvent sourceEvent = new PackageEvent(0, new Date(), user, newPackage, event, "source", oldPackage.getSource(), newPackage.getSource(), new Date());
		PackageEvent activeEvent = new PackageEvent(0, new Date(), user, newPackage, event, "active", "" + oldPackage.isActive(), "" + newPackage.isActive(), new Date());
		List<PackageEvent> expectedEvents = new ArrayList<>();
		expectedEvents.add(versionEvent);
		expectedEvents.add(repositoryEvent);
		expectedEvents.add(userEvent);
		expectedEvents.add(nameEvent);
		expectedEvents.add(descriptionEvent);
		expectedEvents.add(authorEvent);
		expectedEvents.add(dependsEvent);
		expectedEvents.add(importsEvent);
		expectedEvents.add(suggestsEvent);
		expectedEvents.add(systemRequirementsEvent);
		expectedEvents.add(licenseEvent);
		expectedEvents.add(titleEvent);
		expectedEvents.add(urlEvent);
		expectedEvents.add(sourceEvent);
		expectedEvents.add(activeEvent);
		
		List<PackageEvent> events = packageEventService.create(event, user, newPackage);
		
		for(int i = 0; i < events.size(); i++) {
			PackageEvent expectedEvent = expectedEvents.get(i);
			PackageEvent updateEvent = events.get(i);
			assertEquals(expectedEvent.getChangedBy().getName(), updateEvent.getChangedBy().getName());
			assertEquals(expectedEvent.getChangedVariable(), updateEvent.getChangedVariable());
			assertEquals(expectedEvent.getEvent().getValue(), updateEvent.getEvent().getValue());
			assertEquals(expectedEvent.getId(), updateEvent.getId());
			assertEquals(expectedEvent.getPackage().getName(), updateEvent.getPackage().getName());
			assertEquals(expectedEvent.getValueAfter(), updateEvent.getValueAfter());
			assertEquals(expectedEvent.getValueBefore(), updateEvent.getValueBefore());
		}
	}
	
	@Test
	public void testDeletePackage() {
		Mockito.when(packageService.findById(Mockito.anyInt())).thenReturn(null);
		Event event = new Event(1, "delete");
		Repository repository = new Repository(123, "http://example.org/repo", "newrepo", "127.0.0.1", true, false);
		User user = new User(1, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);
		Package packageBag = new Package(1, repository, user, "example", "example package", "einstein", "some license", "some source", "example", "md5", true, false);

		PackageEvent expectedEvent = new PackageEvent(0, new Date(), user, packageBag, event, "deleted", "", "", new Date());
		
		Mockito.when(packageEventRepository.save(Mockito.any())).thenAnswer(new Answer<PackageEvent>() {
			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (PackageEvent) args[0];
			}
		});
		
		Mockito.when(repositoryEventService.create(Mockito.any())).thenReturn(null);
		
		List<PackageEvent> events = packageEventService.create(event, user, packageBag);
		PackageEvent deleteEvent = events.get(0);
		
		assertEquals(expectedEvent.getChangedBy().getName(), deleteEvent.getChangedBy().getName());
		assertEquals(expectedEvent.getChangedVariable(), deleteEvent.getChangedVariable());
		assertEquals(expectedEvent.getEvent().getValue(), deleteEvent.getEvent().getValue());
		assertEquals(expectedEvent.getId(), deleteEvent.getId());
		assertEquals(expectedEvent.getPackage().getName(), deleteEvent.getPackage().getName());
		assertEquals(expectedEvent.getValueAfter(), deleteEvent.getValueAfter());
		assertEquals(expectedEvent.getValueBefore(), deleteEvent.getValueBefore());
	}
}

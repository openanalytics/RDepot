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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;

import eu.openanalytics.rdepot.exception.EventNotFound;
import eu.openanalytics.rdepot.exception.PackageEditException;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerCreateException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerNotFound;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.RepositoryMaintainerEvent;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.RepositoryMaintainerRepository;
import eu.openanalytics.rdepot.service.EventService;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.RepositoryMaintainerEventService;
import eu.openanalytics.rdepot.service.RepositoryMaintainerService;

@RunWith(MockitoJUnitRunner.class)

public class RepositoryMaintainerServiceTest {
	
	@InjectMocks
	RepositoryMaintainerService repositoryMaintainerService;
	
	@Mock
	PackageService packageService;
	
	@Mock
	EventService eventService;
	
	@Mock
	RepositoryMaintainerEventService repositoryMaintainerEventService;
	
	@Mock
	RepositoryMaintainerRepository repositoryMaintainerRepository;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testCreate() throws PackageEditException, RepositoryEditException, RepositoryMaintainerCreateException {
		Package packageBag1 = new Package();
		Package packageBag2 = new Package();
		Repository repository = new Repository();
		Set<Package> packages = new HashSet<>();
		packages.add(packageBag1);
		packages.add(packageBag2);
		repository.setPackages(packages);
		
		RepositoryMaintainer maintainer = new RepositoryMaintainer(123, new User(), repository, false);
		Event createEvent = new Event(1, "create");
//		when(eventService.findByValue("create")).thenReturn(createEvent);
		when(repositoryMaintainerRepository.save(maintainer)).thenReturn(maintainer);
//		when(packageService.update(any(), any())).thenReturn(null);
		doNothing().when(packageService).refreshMaintainer(any(), any());
		when(repositoryMaintainerEventService.create(any(), any(), any())).thenReturn(null);
		
		RepositoryMaintainer created = repositoryMaintainerService.create(maintainer, new User());
		assertEquals(maintainer.getId(), created.getId());
	}
	
	@Test
	public void testCreateWhenEventIsNotFound() throws EventNotFound {
		when(eventService.getCreateEvent()).thenThrow(new EventNotFound());
		try {
			repositoryMaintainerService.create(new RepositoryMaintainer(), new User());
		} catch(RepositoryMaintainerCreateException e) {
			assertEquals(MessageCodes.ERROR_EVENT_NOT_FOUND, e.getMessage());
		}
	}
	
	@Test
	public void testDelete() throws PackageEditException, RepositoryEditException, RepositoryMaintainerDeleteException {
		Event deleteEvent = new Event(1, "delete");
		Package packageBag1 = new Package();
		Package packageBag2 = new Package();
		Repository repository = new Repository();
		Set<Package> packages = new HashSet<>();
		packages.add(packageBag1);
		packages.add(packageBag2);
		repository.setPackages(packages);
		
		RepositoryMaintainer maintainer = new RepositoryMaintainer(123, new User(), repository, false);
		
//		when(eventService.findByValue("delete")).thenReturn(deleteEvent);
//		when(packageService.update(any(), any())).thenReturn(null);
		doNothing().when(packageService).refreshMaintainer(any(), any());
		when(repositoryMaintainerEventService.create(any(), any(), any())).thenReturn(null);
		
		RepositoryMaintainer deleted = repositoryMaintainerService.delete(maintainer, new User());
		assertTrue(deleted.isDeleted());
		assertEquals(maintainer.getId(), deleted.getId());
	}
	
//	@Test
//	public void testDeleteWhenEventIsNotFound() throws EventNotFound {
//		RepositoryMaintainer maintainer = new RepositoryMaintainer(123, new User(), new Repository(), false);
//		when(eventService.getDeleteEvent()).thenThrow(new EventNotFound());
//		
//		try {
//			repositoryMaintainerService.delete(maintainer, new User());
//		} catch(RepositoryMaintainerDeleteException e) {
//			assertEquals(MessageCodes.ERROR_EVENT_NOT_FOUND, e.getMessage());
//		}
//	}
	
	@Test
	public void testShiftDelete() throws RepositoryMaintainerDeleteException, RepositoryMaintainerNotFound {
		RepositoryMaintainer maintainer = new RepositoryMaintainer(123, new User(), new Repository(), true);
		Set<RepositoryMaintainerEvent> events = new HashSet<>();
		events.add(new RepositoryMaintainerEvent(1, new Date(), new User(), maintainer, new Event(), "", "", "", new Date()));
		events.add(new RepositoryMaintainerEvent(2, new Date(), new User(), maintainer, new Event(), "", "", "", new Date()));
		
		//doNothing().when(repositoryMaintainerEventService).delete(anyInt());
		doNothing().when(repositoryMaintainerRepository).delete(any());
		
		RepositoryMaintainer deleted = repositoryMaintainerService.shiftDelete(maintainer);
		assertEquals(maintainer.getId(), deleted.getId());
	}
	
	@Test
	public void testUpdate() {
		//TODO: Ask later for a proper way to test this method.
	}
}

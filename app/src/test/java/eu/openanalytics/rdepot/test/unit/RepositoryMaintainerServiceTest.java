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
import static org.junit.Assert.assertTrue;
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
import eu.openanalytics.rdepot.test.fixture.RepositoryMaintainerTestFixture;

@RunWith(MockitoJUnitRunner.class)

public class RepositoryMaintainerServiceTest {
	
	@InjectMocks
	RepositoryMaintainerService repositoryMaintainerService;
	
	@Mock
	PackageService packageService;
	
	@Mock
	EventService eventService;
	
	@Mock
	MessageSource messageSource;
	
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
//		Event createEvent = new Event(1, "create");
//		when(eventService.findByValue("create")).thenReturn(createEvent);
		when(repositoryMaintainerRepository.save(maintainer)).thenReturn(maintainer);
//		when(packageService.update(any(), any())).thenReturn(null);
		doNothing().when(packageService).refreshMaintainer(any(), any());
		when(repositoryMaintainerEventService.create(any(), any(), any())).thenReturn(null);
		
		RepositoryMaintainer created = repositoryMaintainerService.create(maintainer, new User());
		assertEquals(maintainer.getId(), created.getId());
	}
}

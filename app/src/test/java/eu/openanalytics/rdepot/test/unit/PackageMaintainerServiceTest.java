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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;

import eu.openanalytics.rdepot.exception.EventNotFound;
import eu.openanalytics.rdepot.exception.PackageEditException;
import eu.openanalytics.rdepot.exception.PackageMaintainerCreateException;
import eu.openanalytics.rdepot.exception.PackageMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.PackageMaintainerEditException;
import eu.openanalytics.rdepot.exception.PackageMaintainerNotFound;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.PackageMaintainerEvent;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.PackageMaintainerRepository;
import eu.openanalytics.rdepot.service.EventService;
import eu.openanalytics.rdepot.service.PackageMaintainerEventService;
import eu.openanalytics.rdepot.service.PackageMaintainerService;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.test.fixture.EventTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.time.TestDateRule;
import eu.openanalytics.rdepot.time.DateProvider;

@RunWith(MockitoJUnitRunner.class)
public class PackageMaintainerServiceTest {

	@InjectMocks
	PackageMaintainerService packageMaintainerService;
	
	@Mock
	PackageService packageService;
	
	@Mock
	PackageMaintainerEventService packageMaintainerEventService;
	
	@Mock
	PackageMaintainerRepository packageMaintainerRepository;
	
	@Mock
	EventService eventService;
	
	@Mock
	MessageSource messageSource;
	
	Locale locale = new Locale("en");
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Rule
	public TestDateRule testDateRule = new TestDateRule();
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(messageSource.getMessage(anyString(), isNull(), anyString(), any())).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return invocation.getArgument(0);
			}
			
		});
	}
	
	private class PackageMaintainerEventAssertionAnswer implements Answer<PackageMaintainerEvent> {
		
		User user;
		PackageMaintainer packageMaintainer;
		Event baseEvent;
		String changedVariable;
		String valueBefore;
		String valueAfter;
		
		public PackageMaintainerEventAssertionAnswer(PackageMaintainer packageMaintainer, User changedBy, 
				Event baseEvent, String changedVariable, String valueBefore, String valueAfter) {
			this.user = changedBy;
			this.packageMaintainer = packageMaintainer;
			this.baseEvent = baseEvent;
			this.changedVariable = changedVariable;
			this.valueBefore = valueBefore;
			this.valueAfter = valueAfter;
		}
		
		@Override
		public PackageMaintainerEvent answer(InvocationOnMock invocation) throws Throwable {
			PackageMaintainerEvent createdEvent = invocation.getArgument(0);
			
			assertPackageMaintainerEvents(createdEvent, packageMaintainer, user, baseEvent, 
					changedVariable, valueBefore, valueAfter);
			
			return createdEvent;
		}
		
	}
	
	private void assertPackageMaintainerEvents(PackageMaintainerEvent createdEvent,
			PackageMaintainer maintainer, User changedBy, Event baseEvent, String changedVariable,
			String valueBefore, String valueAfter) {
		assertEquals("Event id is not correct.", 0, createdEvent.getId());
		assertEquals("Event date is not correct.", DateProvider.now(), createdEvent.getDate());
		assertEquals("Event user is not correct.", changedBy, createdEvent.getChangedBy());
		assertEquals("Event package maintainer is not correct.", maintainer, createdEvent.getPackageMaintainer());
		assertEquals("Base event is not correct.", baseEvent, createdEvent.getEvent());
		assertEquals("Event changed variable is not correct.", changedVariable, createdEvent.getChangedVariable());
		assertEquals("Event value before is not correct.", valueBefore, createdEvent.getValueBefore());
		assertEquals("Event value after is not correct.", valueAfter, createdEvent.getValueAfter());
		assertEquals("Event time is not correct.", DateProvider.now(), createdEvent.getTime());
	}
	
	@Test
	public void create() throws EventNotFound, PackageEditException, PackageMaintainerCreateException {
		int PACKAGE_COUNT = 3;
		Event createEvent = EventTestFixture.GET_FIXTURE_EVENT("create");
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		List<Package> packages = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, user, PACKAGE_COUNT);
		packages.get(0).setName("TEST_PACKAGE");
		packages.get(1).setName("TEST_PACKAGE");
		packages.get(2).setName("ANOTHER_PACKAGE");
		
		repository.setPackages(new HashSet<Package>(packages));
		
		PackageMaintainer maintainer = 
				PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(user, repository);
		
		maintainer.setPackage(packages.get(0).getName());
		
		when(eventService.getCreateEvent()).thenReturn(createEvent);
		when(packageMaintainerRepository.save(maintainer)).thenReturn(maintainer);
		doNothing().when(packageService).refreshMaintainer(any(), eq(user));
		when(packageMaintainerEventService.create(any())).thenAnswer(
				new PackageMaintainerEventAssertionAnswer(maintainer, user, createEvent, "created", "", ""));
		
		PackageMaintainer result = packageMaintainerService.create(maintainer, user);
		
		assertEquals("Returned package maintainer is not correct", result, maintainer);
		
		verify(packageMaintainerRepository).save(maintainer);
		verify(packageService, times(2)).refreshMaintainer(any(), eq(user));
		verify(packageMaintainerEventService).create(any());
	}
	
	@Test
	public void create_ThrowsPackageMaintainerCreateException_WhenEventIsNotFound() 
			throws EventNotFound, PackageMaintainerCreateException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		PackageMaintainer packageMaintainer = 
				PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(user, repository);
		
		when(eventService.getCreateEvent()).thenThrow(new EventNotFound());
		
		expectedException.expect(PackageMaintainerCreateException.class);
		expectedException.expectMessage("Maintainer " + packageMaintainer.getUser().getName() 
				+ " of the package " + packageMaintainer.getPackage() + ": " 
				+ MessageCodes.ERROR_PACKAGEMAINTAINER_CREATE);
		
		packageMaintainerService.create(packageMaintainer, user);
	}
	
	@Test
	public void create_ThrowsPackageMaintainerCreateException_WhenPackageEditExceptionIsThrown() 
			throws PackageEditException, EventNotFound, PackageMaintainerCreateException {
		Event createEvent = EventTestFixture.GET_FIXTURE_EVENT("create");
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		List<Package> packages = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, user, 1);
		
		repository.setPackages(new HashSet<Package>(packages));
		
		PackageMaintainer packageMaintainer = 
				PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(user, repository);
		
		packageMaintainer.setPackage(packages.get(0).getName());
		
		when(eventService.getCreateEvent()).thenReturn(createEvent);
		when(packageMaintainerRepository.save(packageMaintainer)).thenReturn(packageMaintainer);
		doThrow(new PackageEditException(messageSource, locale, packages.get(0)))
			.when(packageService).refreshMaintainer(packages.get(0), user);
		
		expectedException.expect(PackageMaintainerCreateException.class);
		expectedException.expectMessage("Maintainer " + packageMaintainer.getUser().getName() 
				+ " of the package " + packageMaintainer.getPackage() + ": " + MessageCodes.ERROR_PACKAGEMAINTAINER_CREATE);
	
		packageMaintainerService.create(packageMaintainer, user);
	}
	
	@Test
	public void delete() throws EventNotFound, PackageEditException, 
			PackageMaintainerCreateException, PackageMaintainerDeleteException, 
			PackageMaintainerNotFound {
		int PACKAGE_COUNT = 3;
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		List<Package> packages = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, user, PACKAGE_COUNT);
		packages.get(0).setName("TEST_PACKAGE");
		packages.get(1).setName("TEST_PACKAGE");
		packages.get(2).setName("ANOTHER_PACKAGE");
		
		repository.setPackages(new HashSet<Package>(packages));
		
		PackageMaintainer maintainer = 
				PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(user, repository);
		
		maintainer.setDeleted(false);
		
		maintainer.setPackage(packages.get(0).getName());
		
		when(packageMaintainerRepository.findByIdAndDeleted(maintainer.getId(), false)).thenReturn(maintainer);
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		doNothing().when(packageService).refreshMaintainer(any(), eq(user));
		when(packageMaintainerEventService.create(any())).thenAnswer(
				new PackageMaintainerEventAssertionAnswer(maintainer, user, deleteEvent, "deleted", "", ""));
		
		PackageMaintainer result = packageMaintainerService.delete(maintainer.getId(), user);
		
		assertTrue("Returned package maintainer is not deleted", result.isDeleted());
		
		verify(packageService, times(2)).refreshMaintainer(any(), eq(user));
		verify(packageMaintainerEventService).create(any());
	}
	
	@Test
	public void delete_ThrowsPackageMaintainerNotFound() 
			throws PackageMaintainerDeleteException, PackageMaintainerNotFound {
		int ID = 123;
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		
		when(packageMaintainerRepository.findByIdAndDeleted(ID, false)).thenReturn(null);
		
		expectedException.expect(PackageMaintainerNotFound.class);
		expectedException.expectMessage("Package maintainer with id of " + ID + ": " 
				+ MessageCodes.ERROR_PACKAGEMAINTAINER_NOT_FOUND);
		
		packageMaintainerService.delete(ID, user);
	}
	
	@Test
	public void delete_ThrowsPackageMaintainerDeleteException_WhenEventIsNotFound() 
			throws EventNotFound, PackageMaintainerDeleteException, PackageMaintainerNotFound {
		
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		PackageMaintainer packageMaintainer = 
				PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(user, repository);
		
		when(packageMaintainerRepository.findByIdAndDeleted(packageMaintainer.getId(), false))
			.thenReturn(packageMaintainer);
		when(eventService.getDeleteEvent()).thenThrow(new EventNotFound());
		
		expectedException.expect(PackageMaintainerDeleteException.class);
		expectedException.expectMessage("Maintainer " + packageMaintainer.getUser().getName() 
				+ " of the package " + packageMaintainer.getPackage() + ": " 
				+ MessageCodes.ERROR_PACKAGEMAINTAINER_DELETE);
		
		packageMaintainerService.delete(packageMaintainer.getId(), user);
	}
	
	@Test
	public void delete_ThrowsPackageMaintainerDeleteException_WhenPackageEditExceptionIsThrown() 
			throws EventNotFound, PackageEditException, PackageMaintainerDeleteException, 
				PackageMaintainerNotFound {
		int PACKAGE_COUNT = 3;
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		List<Package> packages = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, user, PACKAGE_COUNT);
		packages.get(0).setName("TEST_PACKAGE");
		packages.get(1).setName("TEST_PACKAGE");
		packages.get(2).setName("ANOTHER_PACKAGE");
		
		repository.setPackages(new HashSet<Package>(packages));
		
		PackageMaintainer maintainer = 
				PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(user, repository);
		
		maintainer.setDeleted(false);
		
		maintainer.setPackage(packages.get(0).getName());
		
		when(packageMaintainerRepository.findByIdAndDeleted(maintainer.getId(), false)).thenReturn(maintainer);
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		doThrow(new PackageEditException(messageSource, locale, packages.get(0)))
			.when(packageService).refreshMaintainer(any(), eq(user));
		
		expectedException.expect(PackageMaintainerDeleteException.class);
		expectedException.expectMessage("Maintainer " + maintainer.getUser().getName() 
				+ " of the package " + maintainer.getPackage() + ": " 
				+ MessageCodes.ERROR_PACKAGEMAINTAINER_DELETE);
		
		packageMaintainerService.delete(maintainer.getId(), user);
	}
	
	@Test
	public void updateUser() throws EventNotFound, PackageEditException, PackageMaintainerEditException {
		User oldUser = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(0);
		User newUser = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(1);
		User requester = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		List<Package> packageList = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, requester, 3);
		Package packageToMaintain = packageList.get(0);
		Set<Package> packages = new HashSet<Package>(packageList);
		
		repository.setPackages(packages);
		PackageMaintainer packageMaintainer = 
				PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(oldUser, repository);
		packageMaintainer.setPackage(packageToMaintain.getName());
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doNothing().when(packageService).refreshMaintainer(packageToMaintain, requester);
		when(packageMaintainerEventService.create(any())).thenAnswer(
				new PackageMaintainerEventAssertionAnswer(packageMaintainer, requester, 
						updateEvent, "user", Integer.toString(oldUser.getId()), Integer.toString(newUser.getId())));
		
		packageMaintainerService.updateUser(packageMaintainer, newUser, requester);
		
		assertEquals("Package maintainer was not updated.", newUser, packageMaintainer.getUser());
		verify(packageService, times(1)).refreshMaintainer(packageToMaintain, requester);
		verify(packageMaintainerEventService, times(1)).create(any());
	}
	
	@Test
	public void updateUser_ThrowPackageMaintainerEditException_WhenEventIsNotFound() 
			throws EventNotFound, PackageMaintainerEditException {
		User requester = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(0);
		User newUser = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(1);
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		PackageMaintainer packageMaintainer = 
				PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(user, repository);
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());
		
		expectedException.expect(PackageMaintainerEditException.class);
		expectedException.expectMessage("Maintainer " + packageMaintainer.getUser().getName() 
				+ " of the package " + packageMaintainer.getPackage() + ": " 
				+ MessageCodes.ERROR_PACKAGEMAINTAINER_EDIT);
		
		packageMaintainerService.updateUser(packageMaintainer, newUser, requester);
	}
	
	@Test
	public void updateUser_ThrowsPackageMaintainerEditException_WhenPackageEditExceptionIsThrown()
			throws EventNotFound, PackageEditException, PackageMaintainerEditException {
		User oldUser = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(0);
		User newUser = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(1);
		User requester = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		List<Package> packageList = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, requester, 3);
		Package packageToMaintain = packageList.get(0);
		Set<Package> packages = new HashSet<Package>(packageList);
		
		repository.setPackages(packages);
		PackageMaintainer packageMaintainer = 
				PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(oldUser, repository);
		packageMaintainer.setPackage(packageToMaintain.getName());
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doThrow(new PackageEditException(messageSource, locale, packageToMaintain))
			.when(packageService).refreshMaintainer(packageToMaintain, requester);
		
		expectedException.expect(PackageMaintainerEditException.class);
		expectedException.expectMessage("Maintainer " + newUser.getName()
				+ " of the package " + packageMaintainer.getPackage() + ": " 
				+ MessageCodes.ERROR_PACKAGEMAINTAINER_EDIT);
		
		packageMaintainerService.updateUser(packageMaintainer, newUser, requester);
	}
	
	@Test
	public void updateRepository() throws EventNotFound, PackageEditException, PackageMaintainerEditException {
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		User requester = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		List<Repository> repositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		Repository oldRepository = repositories.get(0);
		Repository newRepository = repositories.get(1);
		
		List<Package> packageList = PackageTestFixture.GET_FIXTURE_PACKAGES(oldRepository, requester, 4);
		packageList.get(2).setRepository(newRepository);
		packageList.get(3).setRepository(newRepository);
		
		Package packageToMaintain = packageList.get(0);
		Package packageToMaintainInNewRepository = packageList.get(2);
		packageToMaintainInNewRepository.setName(packageToMaintain.getName());
		
		Set<Package> oldPackages = new HashSet<Package>();
		Set<Package> newPackages = new HashSet<Package>();
		
		oldPackages.add(packageToMaintain);
		oldPackages.add(packageList.get(1));
		
		newPackages.add(packageToMaintainInNewRepository);
		newPackages.add(packageList.get(2));
		
		
		oldRepository.setPackages(oldPackages);
		newRepository.setPackages(newPackages);
		PackageMaintainer packageMaintainer = 
				PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(user, oldRepository);
		packageMaintainer.setPackage(packageToMaintain.getName());
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doNothing().when(packageService).refreshMaintainer(packageToMaintain, requester);
		doNothing().when(packageService).refreshMaintainer(packageToMaintainInNewRepository, requester);
		when(packageMaintainerEventService.create(any())).thenAnswer(
				new PackageMaintainerEventAssertionAnswer(packageMaintainer, requester, 
						updateEvent, "repository", Integer.toString(oldRepository.getId()), 
						Integer.toString(newRepository.getId())));
		
		packageMaintainerService.updateRepository(packageMaintainer, newRepository, requester);
		
		verify(packageService).refreshMaintainer(packageToMaintain, requester);
		verify(packageService).refreshMaintainer(packageToMaintainInNewRepository, requester);
	}
	
	@Test
	public void updateRepository_ThrowsPackageMaintainerEditException_WhenEventNotFoundIsThrown() 
			throws EventNotFound, PackageMaintainerEditException {
		User requester = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		List<Repository> repositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		Repository oldRepository = repositories.get(0);
		Repository newRepository = repositories.get(1);
		Package packageToMaintain = PackageTestFixture.GET_FIXTURE_PACKAGE(oldRepository, user);
		
		PackageMaintainer packageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(user, oldRepository);
		packageMaintainer.setPackage(packageToMaintain.getName());
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());
		
		expectedException.expect(PackageMaintainerEditException.class);
		expectedException.expectMessage("Maintainer " + user.getName()
			+ " of the package " + packageMaintainer.getPackage() + ": " 
			+ MessageCodes.ERROR_PACKAGEMAINTAINER_EDIT);
		
		packageMaintainerService.updateRepository(packageMaintainer, newRepository, requester);
	}
	
	@Test
	public void updateRepository_ThrowsPackageMaintainerEditException_WhenPackageEditExceptionIsThrown()
			throws PackageEditException, EventNotFound, PackageMaintainerEditException {
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		User requester = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		List<Repository> repositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		Repository oldRepository = repositories.get(0);
		Repository newRepository = repositories.get(1);
		
		List<Package> packageList = PackageTestFixture.GET_FIXTURE_PACKAGES(oldRepository, requester, 4);
		packageList.get(2).setRepository(newRepository);
		packageList.get(3).setRepository(newRepository);
		
		Package packageToMaintain = packageList.get(0);
		Package packageToMaintainInNewRepository = packageList.get(2);
		packageToMaintainInNewRepository.setName(packageToMaintain.getName());
		
		Set<Package> oldPackages = new HashSet<Package>();
		Set<Package> newPackages = new HashSet<Package>();
		
		oldPackages.add(packageToMaintain);
		oldPackages.add(packageList.get(1));
		
		newPackages.add(packageToMaintainInNewRepository);
		newPackages.add(packageList.get(2));
		
		
		oldRepository.setPackages(oldPackages);
		newRepository.setPackages(newPackages);
		PackageMaintainer packageMaintainer = 
				PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(user, oldRepository);
		packageMaintainer.setPackage(packageToMaintain.getName());
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doThrow(new PackageEditException(messageSource, locale, packageToMaintain))
			.when(packageService).refreshMaintainer(any(), eq(requester));
		
		expectedException.expect(PackageMaintainerEditException.class);
		expectedException.expectMessage("Maintainer " + user.getName()
			+ " of the package " + packageMaintainer.getPackage() + ": " 
			+ MessageCodes.ERROR_PACKAGEMAINTAINER_EDIT);
		
		packageMaintainerService.updateRepository(packageMaintainer, newRepository, requester);
	}
	
	@Test
	public void updatePackage() throws EventNotFound, PackageEditException, PackageMaintainerEditException {
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		User user = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(0);
		User requester = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		List<Package> packageList = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, requester, 3);
		Package oldPackage = packageList.get(0);
		Package newPackage = packageList.get(1);
		repository.setPackages(new HashSet<>(packageList));
		
		PackageMaintainer packageMaintainer = 
				PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(user, repository);
		packageMaintainer.setPackage(oldPackage.getName());
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doNothing().when(packageService).refreshMaintainer(oldPackage, requester);
		doNothing().when(packageService).refreshMaintainer(newPackage, requester);
		when(packageMaintainerEventService.create(any())).thenAnswer(
				new PackageMaintainerEventAssertionAnswer(packageMaintainer, requester, 
						updateEvent, "package", oldPackage.getName(), newPackage.getName()));
		
		packageMaintainerService.updatePackage(packageMaintainer, newPackage.getName(), requester);
		
		verify(packageService).refreshMaintainer(oldPackage, requester);
		verify(packageService).refreshMaintainer(newPackage, requester);
	}
	
	@Test
	public void updatePackage_ThrowsPackageMaintainerEditException_WhenEventNotFoundIsThrown() 
			throws EventNotFound, PackageMaintainerEditException {
		User user = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(0);
		User requester = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		List<Package> packageList = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, requester, 3);
		Package oldPackage = packageList.get(0);
		Package newPackage = packageList.get(1);
		repository.setPackages(new HashSet<>(packageList));
		
		PackageMaintainer packageMaintainer = 
				PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(user, repository);
		packageMaintainer.setPackage(oldPackage.getName());
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());
		
		expectedException.expect(PackageMaintainerEditException.class);
		expectedException.expectMessage("Maintainer " + user.getName()
			+ " of the package " + packageMaintainer.getPackage() + ": " 
			+ MessageCodes.ERROR_PACKAGEMAINTAINER_EDIT);
		
		packageMaintainerService.updatePackage(packageMaintainer, newPackage.getName(), requester);
	}
	
	@Test
	public void updatePackage_ThrowsPackageMaintainerEditException_WhenPackageEditExceptionIsThrown() 
			throws PackageEditException, PackageMaintainerEditException, EventNotFound {
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		User user = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(0);
		User requester = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		List<Package> packageList = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, requester, 3);
		Package oldPackage = packageList.get(0);
		Package newPackage = packageList.get(1);
		
		repository.setPackages(new HashSet<>(packageList));
		
		PackageMaintainer packageMaintainer = 
				PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(user, repository);
		packageMaintainer.setPackage(oldPackage.getName());
		PackageEditException exception = new PackageEditException(messageSource, locale, oldPackage);
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doThrow(exception)
			.when(packageService).refreshMaintainer(oldPackage, requester);
//		doNothing().when(packageService).refreshMaintainer(newPackage, requester);
		
		expectedException.expect(PackageMaintainerEditException.class);
		expectedException.expectMessage("Maintainer " + user.getName()
			+ " of the package " + oldPackage.getName() + ": " 
			+ MessageCodes.ERROR_PACKAGEMAINTAINER_EDIT);
		
		packageMaintainerService.updatePackage(packageMaintainer, newPackage.getName(), requester);
	}
}

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
//import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;
import eu.openanalytics.rdepot.exception.AdminNotFound;
import eu.openanalytics.rdepot.exception.EventNotFound;
import eu.openanalytics.rdepot.exception.PackageActivateException;
import eu.openanalytics.rdepot.exception.PackageCreateException;
import eu.openanalytics.rdepot.exception.PackageDeactivateException;
import eu.openanalytics.rdepot.exception.PackageDeleteException;
import eu.openanalytics.rdepot.exception.PackageEditException;
import eu.openanalytics.rdepot.exception.PackageNotFound;
import eu.openanalytics.rdepot.exception.PackageStorageException;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.exception.SourceFileDeleteException;
import eu.openanalytics.rdepot.exception.SubmissionDeleteException;
import eu.openanalytics.rdepot.exception.SubmissionNotFound;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageEvent;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.PackageRepository;
import eu.openanalytics.rdepot.service.EventService;
import eu.openanalytics.rdepot.service.PackageEventService;
import eu.openanalytics.rdepot.service.PackageMaintainerService;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.RoleService;
import eu.openanalytics.rdepot.service.SubmissionService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.storage.PackageStorageLocalImpl;
import eu.openanalytics.rdepot.test.fixture.EventTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageEventTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.SubmissionTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.time.TestDateRule;
import eu.openanalytics.rdepot.time.DateProvider;
import eu.openanalytics.rdepot.warning.PackageAlreadyActivatedWarning;
import eu.openanalytics.rdepot.warning.PackageAlreadyDeactivatedWarning;
import eu.openanalytics.rdepot.warning.PackageAlreadyDeletedWarning;
import eu.openanalytics.rdepot.warning.SubmissionDeleteWarning;

@RunWith(MockitoJUnitRunner.class)
public class PackageServiceTest {
	
	@Mock
	private PackageRepository packageRepository;
	
	@Mock
	private RoleService roleService;
	
	@Mock
	private UserService userService;
	
	@Mock
	private RepositoryService repositoryService;
	
	@Mock
	private SubmissionService submissionService;
	
	@Mock
	private PackageMaintainerService packageMaintainerService;
	
	@Mock
	private RepositoryMaintainerService repositoryMaintainerService;
	
	@Mock
	private PackageEventService packageEventService;
	
	@Mock
	private EventService eventService;
	
	@Mock
	private PackageStorageLocalImpl packageStorage;
	
	@InjectMocks
	private PackageService packageService;
	
	@Rule
	public TestDateRule testDateRule = new TestDateRule();
	
	@Mock
	private MessageSource messageSource;
	
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	
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
	
	private class PackageEventAssertionAnswer implements Answer<PackageEvent> {
		User user;
		Package packageBag;
		Event baseEvent;
		String changedVariable;
		String valueBefore;
		String valueAfter;
		
		public PackageEventAssertionAnswer(User user, Package packageBag, Event baseEvent,
				String changedVariable, String valueBefore, String valueAfter) {
			super();
			this.user = user;
			this.packageBag = packageBag;
			this.baseEvent = baseEvent;
			this.changedVariable = changedVariable;
			this.valueBefore = valueBefore;
			this.valueAfter = valueAfter;
		}

		@Override
		public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
			PackageEvent createdEvent = invocation.getArgument(0);
			
			assertPackageEvents(createdEvent, user, packageBag, baseEvent, changedVariable, valueBefore, valueAfter);
			
			return createdEvent;
		}
		
	};
	
	private void assertPackageEvents(PackageEvent createdEvent, User user, Package packageBag, 
			Event baseEvent, String changedVariable, String valueBefore, String valueAfter) {
		assertEquals("Event id is not correct.", 0, createdEvent.getId());
		assertEquals("Event date is not correct.", DateProvider.now(), createdEvent.getDate());
		assertEquals("Event user is not correct.", user, createdEvent.getChangedBy());
		assertEquals("Event package is not correct.", packageBag, createdEvent.getPackage());
		assertEquals("Base event is not correct.", baseEvent, createdEvent.getEvent());
		assertEquals("Event changed variable is not correct.", changedVariable, createdEvent.getChangedVariable());
		assertEquals("Event value before is not correct.", valueBefore, createdEvent.getValueBefore());
		assertEquals("Event value after is not correct.", valueAfter, createdEvent.getValueAfter());
		assertEquals("Event time is not correct.", DateProvider.now(), createdEvent.getTime());
	}
	
	@Test
	public void create_CreatesPackage() throws EventNotFound, PackageCreateException {
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		User user = UserTestFixture.GET_FIXTURE_USER();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Event createEvent = EventTestFixture.GET_FIXTURE_EVENT("create");
		
		when(packageRepository.save(packageBag)).thenReturn(packageBag);
		when(eventService.getCreateEvent()).thenReturn(createEvent);
		when(packageEventService.create(any())).thenAnswer(new PackageEventAssertionAnswer(user, packageBag, createEvent, "created", "", ""));
		
		Package createdPackage = packageService.create(packageBag, user, false);
		assertEquals("Created package is not correct!", createdPackage, packageBag);
	}
	
	@Test
	public void create_ThrowsPackageCreateExceptionIfEventIsNotFound() 
			throws EventNotFound,PackageCreateException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package createdPackage = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		
		when(packageRepository.save(createdPackage)).thenReturn(createdPackage);
		when(eventService.getCreateEvent()).thenThrow(new EventNotFound());

		assertThrows(
		    MessageCodes.ERROR_PACKAGE_CREATE,
		    PackageCreateException.class,
            () -> {
              packageService.create(createdPackage, user, false);
        });
	}
	
	@Test
	public void create_CreatesAndReplacesPackageWhenReplaceIsTrue() throws EventNotFound, RepositoryEditException, PackageCreateException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package replacedPackage = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Package replacingPackage = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		Event createEvent = EventTestFixture.GET_FIXTURE_EVENT("create");
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, replacedPackage);

		replacedPackage.setDeleted(false);
		repository.setPublished(false);
		submission.setDeleted(false);
		replacedPackage.setSubmission(submission);
		
		when(packageRepository.findByNameAndVersionAndRepositoryAndDeleted(
				replacingPackage.getName(), replacingPackage.getVersion(), 
				replacingPackage.getRepository(), false))
			.thenReturn(replacedPackage);
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		when(eventService.getCreateEvent()).thenReturn(createEvent);
		when(packageEventService.create(any())).thenAnswer(new Answer<PackageEvent>() {
			private int count = 0;
			
			@Override
			public PackageEvent answer(InvocationOnMock invocation) throws Throwable {
				PackageEvent createdEvent = invocation.getArgument(0);
				
				if(count == 0) {
					assertPackageEvents(createdEvent, user, replacedPackage, deleteEvent, "delete", "false", "true");
				} else {
					assertPackageEvents(createdEvent, user, replacingPackage, createEvent, "created", "", "");
				}
				count++;
				return createdEvent;
			}
		});

		when(packageRepository.save(replacingPackage)).thenReturn(replacingPackage);
		
		Package createdPackage = packageService.create(replacingPackage, user, true);
		assertEquals("Returned packages is not correct.", createdPackage, replacingPackage);
		
		verify(repositoryService, times(1)).boostRepositoryVersion(repository, user);	
	}
	
	@Test
	public void delete_DeletesPackage() 
			throws SubmissionDeleteException, 
					SubmissionNotFound,
					SubmissionDeleteWarning, 
					EventNotFound,
					PackageDeleteException,
					PackageAlreadyDeletedWarning,
					PackageNotFound {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		
		packageBag.setDeleted(false);
		submission.setDeleted(false);
		packageBag.setSubmission(submission);
		
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		doNothing().when(submissionService).deleteSubmission(submission, user);
		when(packageEventService.create(any())).thenAnswer(
				new PackageEventAssertionAnswer(user, packageBag, deleteEvent, "delete", "false", "true"));
		
		packageService.delete(packageBag, user);
		
		assertTrue("Package was not deleted.", packageBag.isDeleted());
		verify(submissionService).deleteSubmission(submission, user);
	}
	
//	@Test
//	public void delete_ThrowsPackageNotFound_IfPackageIsNull() 
//			throws PackageDeleteException, PackageAlreadyDeletedWarning, PackageNotFound {
//		User user = UserTestFixture.GET_FIXTURE_ADMIN();
//		Integer ID = new Random().nextInt();
//		
//		when(packageRepository.findByIdAndDeleted(ID, false)).thenReturn(null);
//		
//		expectedException.expect(PackageNotFound.class);
//		expectedException.expectMessage(MessageCodes.ERROR_PACKAGE_NOT_FOUND);
//		
//		packageService.delete(ID, user);
//	}
	
	@Test
	public void delete_ThrowsPackageAlreadyDeletedWarning_IfPackageIsDeleted() 
			throws PackageDeleteException, PackageAlreadyDeletedWarning, PackageNotFound {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		
		packageBag.setDeleted(true);
			
		assertThrows(
            MessageCodes.WARNING_PACKAGE_ALREADY_DELETED,
            PackageAlreadyDeletedWarning.class,
            () -> {
              packageService.delete(packageBag, user);
        });
	}
	
	@Test
	public void shiftDelete_deletesPackageFromDatabaseAndFilesystem() 
			throws SourceFileDeleteException, SubmissionDeleteException,
			SubmissionNotFound, PackageDeleteException, PackageNotFound {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		List<PackageEvent> events = PackageEventTestFixture
				.GET_FIXTURE_SORTED_PACKAGE_EVENTS(user, packageBag, 2, 2);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		
		packageBag.setDeleted(true);
		packageBag.setPackageEvents(new HashSet<PackageEvent>(events));
		packageBag.setSubmission(submission);
		
		when(submissionService.shiftDelete(submission)).thenReturn(submission);
		doNothing().when(packageStorage).deleteSource(packageBag);
		doNothing().when(packageEventService).delete(anyInt());
		doNothing().when(packageRepository).delete(packageBag);
		
		packageService.shiftDelete(packageBag);
		
		assertEquals("Package source is not empty.", "", packageBag.getSource());
		
		verify(packageStorage).deleteSource(packageBag);
		verify(packageEventService, times(4)).delete(anyInt());
		verify(submissionService).shiftDelete(submission);
		verify(packageRepository).delete(packageBag);
	}
	
//	@Test
//	public void shiftDelete_ThrowsPackageNotFound_IfPackageIsNull()
//			throws PackageDeleteException, PackageNotFound {
//		Integer ID = new Random().nextInt();
//		
//		when(packageRepository.findByIdAndDeleted(ID, true)).thenReturn(null);
//		
//		expectedException.expect(PackageNotFound.class);
//		expectedException.expectMessage(MessageCodes.ERROR_PACKAGE_NOT_FOUND);
//		
//		packageService.shiftDelete(ID);
//	}
	
	@Test
	public void shiftDelete_ThrowsPackageDeleteException_IfPackageStorageExceptionIsThrown() 
			throws SubmissionDeleteException, SubmissionNotFound, SourceFileDeleteException,
					PackageDeleteException, PackageNotFound {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		List<PackageEvent> events = PackageEventTestFixture
				.GET_FIXTURE_SORTED_PACKAGE_EVENTS(user, packageBag, 2, 2);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		
		packageBag.setDeleted(true);
		packageBag.setPackageEvents(new HashSet<PackageEvent>(events));
		packageBag.setSubmission(submission);
		
		when(submissionService.shiftDelete(submission)).thenReturn(submission);
		doThrow(
				new SourceFileDeleteException( 
						messageSource, new Locale("en"), packageBag, "some details")
		).when(packageStorage).deleteSource(packageBag);
		
        assertThrows(
            MessageCodes.ERROR_PACKAGE_DELETE,
            PackageDeleteException.class,
            () -> {
              packageService.shiftDelete(packageBag);
        });
	}
	
	@Test
	public void shiftDelete_ThrowsPackageDeleteException_IfSubmissionDeleteExceptionIsThrown() 
			throws SubmissionDeleteException, 
			SubmissionNotFound,
			PackageDeleteException,
			PackageNotFound {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		List<PackageEvent> events = PackageEventTestFixture
				.GET_FIXTURE_SORTED_PACKAGE_EVENTS(user, packageBag, 2, 2);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		SubmissionDeleteException exceptionToThrow = new SubmissionDeleteException(messageSource, new Locale("en"), submission.getId());
		
		packageBag.setDeleted(true);
		packageBag.setPackageEvents(new HashSet<PackageEvent>(events));
		packageBag.setSubmission(submission);
		
		when(submissionService.shiftDelete(submission))
			.thenThrow(exceptionToThrow);

		assertThrows(
            MessageCodes.ERROR_PACKAGE_DELETE,
            PackageDeleteException.class,
            () -> {
              packageService.shiftDelete(packageBag);
        });
	}
	
//	@Test
//	public void shiftDeleteForRejectedSubmission_deletesPackageFromDatabaseAndFilesystem() 
//			throws SourceFileDeleteException, SubmissionDeleteException,
//			SubmissionNotFound, PackageDeleteException, PackageNotFound {
//		User user = UserTestFixture.GET_FIXTURE_ADMIN();
//		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
//		List<PackageEvent> events = PackageEventTestFixture
//				.GET_FIXTURE_SORTED_PACKAGE_EVENTS(user, packageBag, 2, 2);
//		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
//		
//		packageBag.setDeleted(false);
//		packageBag.setPackageEvents(new HashSet<PackageEvent>(events));
//		packageBag.setSubmission(submission);
//		
//		when(submissionService.shiftDelete(submission.getId())).thenReturn(submission);
//		doNothing().when(packageStorage).deleteSource(packageBag);
//		doNothing().when(packageEventService).delete(anyInt());
//		doNothing().when(packageRepository).delete(packageBag);
//		
//		packageService.shiftDeleteForRejectedSubmission(packageBag);
//		
//		assertEquals("Package source is not empty.", "", packageBag.getSource());
//		
//		verify(packageStorage).deleteSource(packageBag);
//		verify(packageEventService, times(4)).delete(anyInt());
//		verify(submissionService).shiftDelete(submission.getId());
//		verify(packageRepository).delete(packageBag);
//	}
//	
//	@Test
//	public void shiftDeleteForRejectedSubmission_ThrowsPackageDeleteException_IfPackageStorageExceptionIsThrown() 
//			throws SubmissionDeleteException, SubmissionNotFound, SourceFileDeleteException,
//					PackageDeleteException, PackageNotFound {
//		User user = UserTestFixture.GET_FIXTURE_ADMIN();
//		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
//		List<PackageEvent> events = PackageEventTestFixture
//				.GET_FIXTURE_SORTED_PACKAGE_EVENTS(user, packageBag, 2, 2);
//		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
//		
//		packageBag.setPackageEvents(new HashSet<PackageEvent>(events));
//		packageBag.setSubmission(submission);
//				
//		when(submissionService.shiftDelete(submission.getId())).thenReturn(submission);
//		doThrow(
//				new SourceFileDeleteException( 
//						messageSource, new Locale("en"), packageBag, "some details")
//		).when(packageStorage).deleteSource(packageBag);
//		
//		expectedException.expect(PackageDeleteException.class);
//		expectedException.expectMessage(MessageCodes.ERROR_PACKAGE_DELETE);
//		
//		packageService.shiftDeleteForRejectedSubmission(packageBag);
//	}
//	
//	@Test
//	public void shiftDeleteForRejectedSubmission_ThrowsPackageDeleteException_IfSubmissionDeleteExceptionIsThrown() 
//			throws SubmissionDeleteException, 
//			SubmissionNotFound,
//			PackageDeleteException,
//			PackageNotFound {
//		User user = UserTestFixture.GET_FIXTURE_ADMIN();
//		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
//		List<PackageEvent> events = PackageEventTestFixture
//				.GET_FIXTURE_SORTED_PACKAGE_EVENTS(user, packageBag, 2, 2);
//		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
//		SubmissionDeleteException exceptionToThrow = new SubmissionDeleteException(messageSource, new Locale("en"), submission.getId());
//		
//		packageBag.setPackageEvents(new HashSet<PackageEvent>(events));
//		packageBag.setSubmission(submission);
//		
//		when(submissionService.shiftDelete(submission.getId()))
//			.thenThrow(exceptionToThrow);
//		
//		expectedException.expect(PackageDeleteException.class);
//		expectedException.expectMessage(MessageCodes.ERROR_PACKAGE_DELETE);
//		
//		packageService.shiftDeleteForRejectedSubmission(packageBag);
//	}
	
	@Test
	public void refreshMaintainer_WhenThereIsMaintainerForPackage() throws EventNotFound, PackageEditException {
		User previousMaintainer = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(1);
		User newMaintainer = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(2);
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, previousMaintainer);
		PackageMaintainer packageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(newMaintainer, repository);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		packageMaintainer.setPackage(packageBag.getName());
		
		when(packageMaintainerService.findByPackageAndRepositoryAndNotDeleted(packageBag.getName(), repository))
			.thenReturn(packageMaintainer);
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		when(packageEventService.create(any())).thenAnswer(
				new PackageEventAssertionAnswer(user, packageBag, updateEvent, "maintainer",
				Integer.toString(previousMaintainer.getId()), 
				Integer.toString(newMaintainer.getId())));
		packageService.refreshMaintainer(packageBag, user);
		
		assertEquals("Maintainer was not updated correctly!", newMaintainer, packageBag.getUser());
	}
	
	@Test
	public void refreshMaintainer_WhenThereAreOtherAvailableMaintainers() throws PackageEditException, EventNotFound {
		User previousMaintainer = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(1);
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, previousMaintainer);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		List<User> users = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		List<RepositoryMaintainer> repositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(users, repository);
		User newMaintainer = repositoryMaintainers.get(0).getUser();
		
		when(packageMaintainerService.findByPackageAndRepositoryAndNotDeleted(packageBag.getName(), repository))
			.thenReturn(null);
		when(repositoryMaintainerService.findByRepository(repository)).thenReturn(repositoryMaintainers);
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		when(packageEventService.create(any())).thenAnswer(
				new PackageEventAssertionAnswer(user, packageBag, updateEvent, "maintainer",
				Integer.toString(previousMaintainer.getId()), Integer.toString(newMaintainer.getId())));
		
		packageService.refreshMaintainer(packageBag, user);
		
		assertEquals("Maintainer was not updated correctly!", newMaintainer, packageBag.getUser());
	}
	
	@Test
	public void refreshMaintainer_WhenThereAreNoAvailableMaintainers() 
			throws PackageEditException, AdminNotFound, EventNotFound {
		User previousMaintainer = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(1);
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, previousMaintainer);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		when(packageMaintainerService.findByPackageAndRepositoryAndNotDeleted(packageBag.getName(), repository))
			.thenReturn(null);
		when(repositoryMaintainerService.findByRepository(repository)).thenReturn(new ArrayList<RepositoryMaintainer>());
		when(userService.findFirstAdmin()).thenReturn(admin);
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		when(packageEventService.create(any())).thenAnswer(
				new PackageEventAssertionAnswer(admin, packageBag, updateEvent, "maintainer",
				Integer.toString(previousMaintainer.getId()),Integer.toString(admin.getId())));
		
		packageService.refreshMaintainer(packageBag, admin);
		
		assertEquals("Maintainer was not updated correctly!", admin, packageBag.getUser());
	}
	
	@Test
	public void refreshMaintainer_ThrowPackageEditException_WhenNoAdminIsFound()
			throws AdminNotFound, PackageEditException {
		User previousMaintainer = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(1);
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, previousMaintainer);
		
		when(packageMaintainerService.findByPackageAndRepositoryAndNotDeleted(packageBag.getName(), repository))
			.thenReturn(null);
		when(repositoryMaintainerService.findByRepository(repository)).thenReturn(new ArrayList<RepositoryMaintainer>());
		when(userService.findFirstAdmin()).thenThrow(new AdminNotFound());

		assertThrows(
		    packageBag.getName() + " " 
            + packageBag.getVersion() + ": " + MessageCodes.ERROR_PACKAGE_EDIT,
            PackageEditException.class,
            () -> {
              packageService.refreshMaintainer(packageBag, admin);
        });
	}
	
	@Test
	public void refreshMaintainer_ThrowPackageEditException_WhenNoEventIsFound() 
			throws PackageEditException, EventNotFound {
		User previousMaintainer = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(1);
		User newMaintainer = UserTestFixture.GET_FIXTURE_PACKAGEMAINTAINER(2);
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, previousMaintainer);
		PackageMaintainer packageMaintainer = PackageMaintainerTestFixture
				.GET_FIXTURE_PACKAGE_MAINTAINER(newMaintainer, repository);
		
		packageMaintainer.setPackage(packageBag.getName());
		
		when(packageMaintainerService
				.findByPackageAndRepositoryAndNotDeleted(packageBag.getName(), repository))
			.thenReturn(packageMaintainer);
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());
		
		assertThrows(
            packageBag.getName() + " " 
            + packageBag.getVersion() + ": " + MessageCodes.ERROR_PACKAGE_EDIT,
            PackageEditException.class,
            () -> {
              packageService.refreshMaintainer(packageBag, user);
        });
	}
	
	@Test
	public void activatePackage() throws EventNotFound, PackageAlreadyActivatedWarning, 
		PackageActivateException {
		User user = UserTestFixture.GET_FIXTURE_USER();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		PackageMaintainer packageMaintainer = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER(user, repository);
		
		packageBag.setActive(false);
		packageMaintainer.setPackage(packageBag.getName());
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		when(packageMaintainerService.findByPackageAndRepositoryAndNotDeleted(packageBag.getName(), repository))
		.thenReturn(packageMaintainer);
		when(packageEventService.create(any())).thenAnswer(new Answer<Package>() {
			
			int counter = 0;
			@Override
			public Package answer(InvocationOnMock invocation) throws Throwable {
				PackageEvent updatePackageEvent = invocation.getArgument(0);
				
				if(counter == 1) {
					assertPackageEvents(updatePackageEvent, user, packageBag, updateEvent, "maintainer",
							Integer.toString(user.getId()), Integer.toString(user.getId()));
				} else {
					assertPackageEvents(updatePackageEvent, user, packageBag, updateEvent, 
							"active", "false", "true");
				}
				counter++;
				return null;
			}
		});
		
		packageService.activatePackage(packageBag, user);
		
		assertTrue("Package was not activated properly.", packageBag.isActive());
	}
	
	@Test
	public void activatePackage_ThrowPackageAlreadyActivatedWarning_WhenPackageIsActive() throws PackageAlreadyActivatedWarning, PackageActivateException {
		User user = UserTestFixture.GET_FIXTURE_USER();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		
		packageBag.setActive(true);

		assertThrows(
            MessageCodes.WARNING_PACKAGE_ALREADY_ACTIVATED,
            PackageAlreadyActivatedWarning.class,
            () -> {
              packageService.activatePackage(packageBag, user);
        });
	}
	
	@Test
	public void activatePackage_ThrowPackageActivateException_WhenEventIsNotFound()
			throws EventNotFound, PackageAlreadyActivatedWarning, PackageActivateException {
		User user = UserTestFixture.GET_FIXTURE_USER();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		
		packageBag.setActive(false);
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());

		assertThrows(
		    packageBag.getName() + " " 
            + packageBag.getVersion() + ": " + MessageCodes.ERROR_PACKAGE_ACTIVATE,
            PackageActivateException.class,
            () -> {
              packageService.activatePackage(packageBag, user);
        });
	}
	
	@Test
	public void activatePackage_ThrowPackageActivateException_WhenPackageEditExceptionIsThrown()
			throws EventNotFound, AdminNotFound, PackageAlreadyActivatedWarning, PackageActivateException {
		User user = UserTestFixture.GET_FIXTURE_USER();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		packageBag.setActive(false);
		
		when(packageMaintainerService.findByPackageAndRepositoryAndNotDeleted(packageBag.getName(), repository))
		.thenReturn(null);
		when(repositoryMaintainerService.findByRepository(repository)).thenReturn(new ArrayList<RepositoryMaintainer>());
		when(userService.findFirstAdmin()).thenThrow(new AdminNotFound());
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		
		assertThrows(
            packageBag.getName() + " " 
            + packageBag.getVersion() + ": " + MessageCodes.ERROR_PACKAGE_ACTIVATE,
            PackageActivateException.class,
            () -> {
              packageService.activatePackage(packageBag, user);
        });
	}
	
	@Test
	public void deactivatePackage() throws PackageDeactivateException, PackageAlreadyDeactivatedWarning, EventNotFound {
		User user = UserTestFixture.GET_FIXTURE_USER();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		PackageMaintainer packageMaintainer = PackageMaintainerTestFixture
				.GET_FIXTURE_PACKAGE_MAINTAINER(user, repository);
		
		packageBag.setActive(true);
		packageMaintainer.setPackage(packageBag.getName());
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		when(packageMaintainerService.findByPackageAndRepositoryAndNotDeleted(packageBag.getName(), repository))
		.thenReturn(packageMaintainer);
		when(packageEventService.create(any())).thenAnswer(new Answer<Package>() {
			
			int counter = 0;
			@Override
			public Package answer(InvocationOnMock invocation) throws Throwable {
				PackageEvent updatePackageEvent = invocation.getArgument(0);
				
				if(counter == 1) {				
					assertPackageEvents(updatePackageEvent, user, packageBag, updateEvent, "maintainer",
							Integer.toString(user.getId()), Integer.toString(user.getId()));
				} else {
					assertPackageEvents(updatePackageEvent, user, packageBag, updateEvent, 
							"active", "true", "false");				}
				counter++;
				return null;
			}
		});
		
		packageService.deactivatePackage(packageBag, user);
		
		assertFalse("Package was not deactivated properly.", packageBag.isActive());
	}
	
	@Test
	public void deactivatePackage_ThrowPackageAlreadyDectivatedWarning_WhenPackageIsInactive() 
			throws PackageDeactivateException, PackageAlreadyDeactivatedWarning {
		User user = UserTestFixture.GET_FIXTURE_USER();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		
		packageBag.setActive(false);

		assertThrows(
            MessageCodes.WARNING_PACKAGE_ALREADY_DEACTIVATED,
            PackageAlreadyDeactivatedWarning.class,
            () -> {
              packageService.deactivatePackage(packageBag, user);
        });
	}
	
	@Test
	public void deactivatePackage_ThrowPackageDeactivateException_WhenEventIsNotFound()
			throws EventNotFound, PackageAlreadyDeactivatedWarning, PackageDeactivateException {
		User user = UserTestFixture.GET_FIXTURE_USER();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		
		packageBag.setActive(true);
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());

		assertThrows(
            packageBag.getName() + " " 
            + packageBag.getVersion() + ": " + MessageCodes.ERROR_PACKAGE_DEACTIVATE,
            PackageDeactivateException.class,
            () -> {
              packageService.deactivatePackage(packageBag, user);
        });
	}
	
	@Test
	public void deactivatePackage_ThrowPackageDeactivateException_WhenPackageEditExceptionIsThrown()
			throws EventNotFound, AdminNotFound, PackageAlreadyDeactivatedWarning, PackageDeactivateException {
		User user = UserTestFixture.GET_FIXTURE_USER();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		packageBag.setActive(true);
		
		when(packageMaintainerService.findByPackageAndRepositoryAndNotDeleted(packageBag.getName(), repository))
		.thenReturn(null);
		when(repositoryMaintainerService.findByRepository(repository)).thenReturn(new ArrayList<RepositoryMaintainer>());
		when(userService.findFirstAdmin()).thenThrow(new AdminNotFound());
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		
		assertThrows(
            packageBag.getName() + " " 
            + packageBag.getVersion() + ": " + MessageCodes.ERROR_PACKAGE_DEACTIVATE,
            PackageDeactivateException.class,
            () -> {
              packageService.deactivatePackage(packageBag, user);
        });
	}
	
	@Test
	public void findAll_skipsNotAcceptedSubmissions() {
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		User user = UserTestFixture.GET_FIXTURE_USER();
		List<Package> packages = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, user, 4);
		
		for(Package packageBag : packages) 
			packageBag.getSubmission().setAccepted(true);
		
		packages.get(1).getSubmission().setAccepted(false);
		
		when(packageRepository.findByDeleted(eq(false), any())).thenReturn(packages);
		
		List<Package> returnedPackages = packageService.findAll();
		
		assertEquals("Number of packages is not correct.", 3, returnedPackages.size());
		
		for(Package packageBag : returnedPackages) {
			if(packageBag.getId() == packages.get(1).getId())
				fail("Packages were not filtered properly");
		}
	}
	
	@Test
	public void updateSource() throws EventNotFound, PackageStorageException, PackageEditException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		PackageMaintainer packageMaintainer = PackageMaintainerTestFixture
				.GET_FIXTURE_PACKAGE_MAINTAINER(user, repository);
		
		String oldSource = "old/source/test";
		String newSource = "new/source/test";
		
		packageBag.setSource(oldSource);
		packageMaintainer.setPackage(packageBag.getName());
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doNothing().when(packageStorage).verifySource(packageBag, newSource);
		when(packageMaintainerService.findByPackageAndRepositoryAndNotDeleted(packageBag.getName(), repository))
		.thenReturn(packageMaintainer);
		when(packageEventService.create(any())).thenAnswer(new Answer<Package>() {
			int counter = 0;
			
			@Override
			public Package answer(InvocationOnMock invocation) throws Throwable {
				PackageEvent updatePackageEvent = invocation.getArgument(0);
				
				if(counter == 1) {
					assertPackageEvents(updatePackageEvent, user, packageBag, updateEvent, "maintainer",
							Integer.toString(user.getId()), Integer.toString(user.getId()));
				} else {
					assertPackageEvents(updatePackageEvent, user, packageBag, updateEvent, "source",
							oldSource, newSource);
				}
				
				counter++;
				return null;
			}
		});
		
		packageService.updateSource(packageBag, newSource, user);
		
		assertEquals("Source was not updated properly.", newSource, packageBag.getSource());
		
		verify(packageStorage).verifySource(packageBag, newSource);
	}
	
	@Test
	public void updateSource_ThrowsPackageEditException_WhenEventIsNotFound()
			throws EventNotFound, PackageEditException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		
		packageBag.setSource("");
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());
		
		assertThrows(
            MessageCodes.ERROR_PACKAGE_EDIT,
            PackageEditException.class,
            () -> {
              packageService.updateSource(packageBag, "", user);
        });
	}
	
	@Test
	public void updateSource_ThrowsPackageEditException_WhenPackageStorageExceptionIsThrown()
			throws EventNotFound, IOException, PackageStorageException, PackageEditException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		PackageStorageException exception = new PackageStorageException(
				MessageCodes.ERROR_PACKAGE_STORAGE_SOURCE_NOTFOUND,
				messageSource,
				new Locale("en"),
				temporaryFolder.newFile(), "");
		String newSource = "new/test/source";
		
		packageBag.setSource("");
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doThrow(exception).when(packageStorage).verifySource(packageBag, newSource);

		assertThrows(
            MessageCodes.ERROR_PACKAGE_EDIT,
            PackageEditException.class,
            () -> {
              packageService.updateSource(packageBag, newSource, user);
        });
	}
	
	@Test
	public void findByRepositoryAndActiveAndNewest() {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		List<Package> packages = 
				PackageTestFixture.GET_FIXTURE_PACKAGES_MULTIPLE_VERSIONS(repository, user, 2, 2);
		boolean active = true;
		
		when(packageRepository.findByRepositoryAndActiveAndDeleted(
				eq(repository), eq(active), eq(false), any())).thenReturn(packages);
		
		List<Package> expected = new ArrayList<>();
		
		expected.add(packages.get(1));
		expected.add(packages.get(3));
		
		List<Package> actual = packageService.findByRepositoryAndActiveAndNewest(repository, active);
		
		assertEquals("Returned packages are not correct.", expected, actual);
	}
	
	
}
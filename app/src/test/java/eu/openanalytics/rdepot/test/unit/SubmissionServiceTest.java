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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.File;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;
import eu.openanalytics.rdepot.exception.EventNotFound;
import eu.openanalytics.rdepot.exception.MovePackageSourceException;
import eu.openanalytics.rdepot.exception.PackageActivateException;
import eu.openanalytics.rdepot.exception.PackageDeleteException;
import eu.openanalytics.rdepot.exception.PackageEditException;
import eu.openanalytics.rdepot.exception.PackageSourceNotFoundException;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.exception.RepositoryPublishException;
import eu.openanalytics.rdepot.exception.SendEmailException;
import eu.openanalytics.rdepot.exception.SourceFileDeleteException;
import eu.openanalytics.rdepot.exception.SubmissionAcceptException;
import eu.openanalytics.rdepot.exception.SubmissionDeleteException;
import eu.openanalytics.rdepot.exception.SubmissionEditException;
import eu.openanalytics.rdepot.exception.SubmissionNotFound;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.SubmissionEvent;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.SubmissionRepository;
import eu.openanalytics.rdepot.service.EmailService;
import eu.openanalytics.rdepot.service.EventService;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.RepositoryEventService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.RoleService;
import eu.openanalytics.rdepot.service.SubmissionEventService;
import eu.openanalytics.rdepot.service.SubmissionService;
import eu.openanalytics.rdepot.service.UploadRequestService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.storage.PackageStorageLocalImpl;
import eu.openanalytics.rdepot.test.fixture.EventTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.SubmissionEventTestFixture;
import eu.openanalytics.rdepot.test.fixture.SubmissionTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.answer.RepositoryEventAssertionAnswer;
import eu.openanalytics.rdepot.test.unit.time.TestDateRule;
import eu.openanalytics.rdepot.time.DateProvider;
import eu.openanalytics.rdepot.warning.PackageAlreadyActivatedWarning;
import eu.openanalytics.rdepot.warning.SubmissionAlreadyAcceptedWarning;
import eu.openanalytics.rdepot.warning.SubmissionDeleteWarning;

@RunWith(MockitoJUnitRunner.class)
public class SubmissionServiceTest {
	
	@InjectMocks
	private SubmissionService submissionService;
	
	@Mock
	private SubmissionRepository submissionRepository;
	
	@Mock
	private MessageSource messageSource;
	
	@Mock
	private UserService userService;
	
	@Mock
	private PackageService packageService;
	
	@Mock
	private RoleService roleService;
	
	@Mock
	private EventService eventService;
	
	@Mock
	private SubmissionEventService submissionEventService;
	
	@Mock
	private RepositoryEventService repositoryEventService;
	
	@Mock
	private RepositoryService repositoryService;
	
	@Mock
	private PackageStorageLocalImpl packageStorage;
	
	@Mock
	private UploadRequestService uploadRequestService;
	
	@Mock
	private EmailService emailService;
	
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	@Rule
	public TestDateRule testDateRule = new TestDateRule();
	
	@Before
	public void init() {
		when(messageSource.getMessage(anyString(), isNull(), anyString(), any())).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return invocation.getArgument(0);
			}
			
		});
	}
	
	private void assertSubmissionEvents(SubmissionEvent submissionEvent, Submission submission, User updater, Event baseEvent,
				String changedVariable, String valueBefore, String valueAfter) {
		assertEquals("Event id is not correct.", 0, submissionEvent.getId());
		assertEquals("Event date is not correct.", DateProvider.now(), submissionEvent.getDate());
		assertEquals("Event user is not correct.", updater, submissionEvent.getChangedBy());
		assertEquals("Event submission is not correct.", submission, submissionEvent.getSubmission());
		assertEquals("Base event is not correct.", baseEvent, submissionEvent.getEvent());
		assertEquals("Event changed variable is not correct.", changedVariable, submissionEvent.getChangedVariable());
		assertEquals("Event value before is not correct.", valueBefore, submissionEvent.getValueBefore());
		assertEquals("Event value after is not correct.", valueAfter, submissionEvent.getValueAfter());
		assertEquals("Event time is not correct.", DateProvider.now(), submissionEvent.getTime());
	}
	
	private class SubmissionEventAssertionAnswer implements Answer<SubmissionEvent> {

		User updater;
		Submission submission;
		Event baseEvent;
		String changedVariable;
		String valueBefore;
		String valueAfter;
		
		public SubmissionEventAssertionAnswer(User updater, Submission submission, Event baseEvent,
				String changedVariable, String valueBefore, String valueAfter) {
			super();
			this.updater = updater;
			this.submission = submission;
			this.baseEvent = baseEvent;
			this.changedVariable = changedVariable;
			this.valueBefore = valueBefore;
			this.valueAfter = valueAfter;
		}
		
		@Override
		public SubmissionEvent answer(InvocationOnMock invocation) throws Throwable {
			SubmissionEvent createdEvent = invocation.getArgument(0);
			
			assertSubmissionEvents(createdEvent, submission, updater, baseEvent, changedVariable,
					valueBefore, valueAfter);
			
			return createdEvent;
		}
		
	}
	
	@Test
	public void updateUser_ThrowsSubmissionEditException_IfEventIsNotFound() throws EventNotFound, SubmissionEditException {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_USER();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());

		assertThrows(
		    "Submission " + submission.getId() + ": " 
            + MessageCodes.ERROR_SUBMISSION_EDIT,
            SubmissionEditException.class,
            () -> {
              submissionService.updateUser(user, submission, admin);
        });
	}
	
	@Test
	public void updateUser() throws EventNotFound, SubmissionEditException {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		List<User> users = UserTestFixture.GET_FIXTURE_USERS(0, 0, 2, 0);
		User currentUser = users.get(0);
		User newUser = users.get(1);
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, currentUser);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(currentUser, packageBag);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);

		doAnswer(new SubmissionEventAssertionAnswer(admin, submission, updateEvent, "user", 
				Integer.toString(currentUser.getId()), Integer.toString(newUser.getId())))
			.when(submissionEventService).create(any());
		
		submissionService.updateUser(newUser, submission, admin);
		
		assertEquals("User was not updated, id does not match", newUser.getId(), submission.getUser().getId());
		verify(submissionEventService).create(any());
	}
	
	@Test
	public void updatePackage_ThrowsSubmissionEditException_IfEventIsNotFound()
			throws EventNotFound, SubmissionEditException {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_USER();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());

		assertThrows(
            "Submission " + submission.getId() + ": " 
            + MessageCodes.ERROR_SUBMISSION_EDIT,
            SubmissionEditException.class,
            () -> {
              submissionService.updatePackage(packageBag, submission, admin);
        });
	}
	
	@Test
	public void updatePackage() throws EventNotFound, SubmissionAcceptException, SubmissionEditException {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		List<Package> packages = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, admin, 2);
		Package currentPackage = packages.get(0);
		Package newPackage = packages.get(1);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(admin, currentPackage);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doAnswer(new SubmissionEventAssertionAnswer(admin, submission, updateEvent, "package",
				Integer.toString(currentPackage.getId()), Integer.toString(newPackage.getId())))
		.when(submissionEventService).create(any());
		
		submissionService.updatePackage(newPackage, submission, admin);
		
		assertEquals("Package was not updated, id does not match", newPackage.getId(), submission.getPackage().getId());
		verify(submissionEventService).create(any());
	}
	
//	@Test
//	public void acceptSubmission() 
//			throws IOException, PackageSourceNotFoundException, 
//				DeleteFileException, MoveFileException, CreateFolderStructureException,
//				PackageEditException, PackageAlreadyActivatedWarning, PackageActivateException,
//				RepositoryEditException, RepositoryPublishException, SubmissionAlreadyAcceptedWarning,
//				SubmissionAcceptException {
//		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
//		User user = UserTestFixture.GET_FIXTURE_ADMIN();
//		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
//		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
//		File newSource = temporaryFolder.newFile();
//		
//		repository.setPublished(true);
//		submission.setAccepted(false);
//		
//		when(packageStorage.moveToMainDirectory(packageBag)).thenReturn(newSource);
//		doNothing().when(packageService).updateSource(packageBag, newSource.toString(), user);
//		doNothing().when(packageService).activatePackage(packageBag, user);
//		doNothing().when(repositoryService).boostRepositoryVersion(repository, user);
//		doNothing().when(repositoryService).publishRepository(repository, user);
//		when(submissionEventService.create(any())).thenAnswer(new Answer<SubmissionEvent>() {
//
//			@Override
//			public SubmissionEvent answer(InvocationOnMock invocation) throws Throwable {
//				SubmissionEvent createdEvent = invocation.getArgument(0);
//				assertEquals("Event id is not correct.", 0, createdEvent.getId());
//				assertEquals("Event date is not correct.", DateProvider.now(), createdEvent.getDate());
//				assertEquals("Event user is not correct.", user, createdEvent.getChangedBy());
//				assertEquals("Event submission is not correct.", submission, createdEvent.getSubmission());
//				assertEquals("Base event is not correct.", updateEvent, createdEvent.getEvent());
//				assertEquals("Event changed variable is not correct.", "accepted", createdEvent.getChangedVariable());
//				assertEquals("Event value before is not correct.", "" + false, createdEvent.getValueBefore());
//				assertEquals("Event value after is not correct.", "" + true, createdEvent.getValueAfter());
//				assertEquals("Event time is not correct.", DateProvider.now(), createdEvent.getTime());
//				
//				return null;
//			}});
//		when(repositoryEventService.create(any())).thenAnswer(new Answer<RepositoryEvent>() {
//
//			@Override
//			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
//				RepositoryEvent event = invocation.getArgument(0);
//				
//				assertEquals("Event index should be 0.", 0, event.getId());
//				assertEquals("Date object should contain current date", DateProvider.now(), event.getDate());
//				assertEquals("Updater is not correct", user, event.getChangedBy());
//				assertEquals("Repository in the event is not correct", repository, event.getRepository());
//				assertEquals("Base event is not correct", updateEvent, event.getEvent());
//				assertEquals("Changed variable is not correct", "added", event.getChangedVariable());
//				assertEquals("Current version is not correct", "", event.getValueBefore());
//				assertEquals("New version is not correct", "", event.getValueAfter());
//				assertEquals("Date object should contain current time", DateProvider.now(), event.getTime());
//				
//				return event;
//		}});
//		
//		submissionService.acceptSubmission(submission, user);
//		
//		assertTrue("Submission was not accepted.", submission.isAccepted());
//		
//		verify(packageService).updateSource(packageBag, newSource.toString(), user);
//		verify(packageService).activatePackage(packageBag, user);
//		verify(repositoryService).boostRepositoryVersion(repository, user);
//		verify(repositoryService).publishRepository(repository, user);
//	}
//	@Test
//	public void shiftDelete_ThrowsSubmissionNotFound_IfSubmissionIsNull() throws SubmissionDeleteException {
//		when(submissionRepository.findByIdAndDeleted(123, true)).thenReturn(null);
//		try {
//			submissionService.shiftDelete(123);
//			fail();
//		} catch(SubmissionNotFound e) {
//			assertEquals("Error message is not correct.", "Submission " + Integer.toString(123) + ": " + MessageCodes.ERROR_SUBMISSION_NOT_FOUND, e.getMessage());
//		}
//	}
	
	@Test
	public void acceptSubmission_AndPublishRepository() 
			throws EventNotFound, PackageSourceNotFoundException, PackageEditException, 
			IOException, PackageAlreadyActivatedWarning, PackageActivateException, 
			RepositoryEditException, RepositoryPublishException, SubmissionAlreadyAcceptedWarning, 
			SubmissionAcceptException, MovePackageSourceException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		File newPackageSource = temporaryFolder.newFile("newsource");
		
		repository.setPublished(true);
		submission.setAccepted(false);
		packageBag.setActive(false);
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		when(packageStorage.moveToMainDirectory(packageBag)).thenReturn(newPackageSource);
		doNothing().when(packageService).updateSource(packageBag, newPackageSource.toString(), user);
		doNothing().when(packageService).activatePackage(packageBag, user);
		doNothing().when(repositoryService).boostRepositoryVersion(repository, user);
		doNothing().when(repositoryService).publishRepository(repository, user);
		doAnswer(new SubmissionEventAssertionAnswer(user, submission, updateEvent, 
				"accepted", "" + false, "" + true))
			.when(submissionEventService).create(any());
		doAnswer(new RepositoryEventAssertionAnswer(user, repository, updateEvent,
				"added", "", Integer.toString(packageBag.getId())))
			.when(repositoryEventService).create(any());
		
		submissionService.acceptSubmission(submission, user);
		
		verify(packageStorage).moveToMainDirectory(packageBag);
		verify(packageService).updateSource(packageBag, newPackageSource.toString(), user);
		verify(packageService).activatePackage(packageBag, user);
		verify(repositoryService).boostRepositoryVersion(repository, user);
		verify(repositoryService).publishRepository(repository, user);
		verify(submissionEventService).create(any());
		verify(repositoryEventService).create(any());
	}
	
	@Test
	public void acceptSubmission_WithoutPublishingRepository() 
			throws EventNotFound, IOException, PackageSourceNotFoundException, 
			MovePackageSourceException, PackageEditException, PackageAlreadyActivatedWarning, 
			PackageActivateException, RepositoryEditException, SubmissionAlreadyAcceptedWarning, 
			SubmissionAcceptException, RepositoryPublishException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		File newPackageSource = temporaryFolder.newFile("newsource");
		
		repository.setPublished(false);
		submission.setAccepted(false);
		packageBag.setActive(false);
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		when(packageStorage.moveToMainDirectory(packageBag)).thenReturn(newPackageSource);
		doNothing().when(packageService).updateSource(packageBag, newPackageSource.toString(), user);
		doNothing().when(packageService).activatePackage(packageBag, user);
		doNothing().when(repositoryService).boostRepositoryVersion(repository, user);
		doAnswer(new SubmissionEventAssertionAnswer(user, submission, updateEvent, 
				"accepted", "" + false, "" + true))
			.when(submissionEventService).create(any());
		doAnswer(new RepositoryEventAssertionAnswer(user, repository, updateEvent,
				"added", "", Integer.toString(packageBag.getId())))
			.when(repositoryEventService).create(any());
		
		submissionService.acceptSubmission(submission, user);
		
		verify(packageStorage).moveToMainDirectory(packageBag);
		verify(packageService).updateSource(packageBag, newPackageSource.toString(), user);
		verify(packageService).activatePackage(packageBag, user);
		verify(repositoryService).boostRepositoryVersion(repository, user);
		verify(repositoryService, never()).publishRepository(repository, user);
		verify(submissionEventService).create(any());
		verify(repositoryEventService).create(any());
	}
	
	@Test
	public void acceptSubmission_ThrowsSubmissionAlreadyAcceptedWarning() 
			throws SubmissionAlreadyAcceptedWarning, SubmissionAcceptException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		
		submission.setAccepted(true);

		assertThrows(
            MessageCodes.WARNING_SUBMISSION_ALREADY_ACCEPTED,
            SubmissionAlreadyAcceptedWarning.class,
            () -> {
              submissionService.acceptSubmission(submission, user);
        });
	}
	
	@Test
	public void acceptSubmission_RevertsFilesystemChanges_WhenRepositoryPublishExceptionIsThrown()
			throws IOException, EventNotFound, PackageSourceNotFoundException, MovePackageSourceException,
			PackageEditException, PackageAlreadyActivatedWarning, PackageActivateException,
			RepositoryEditException, RepositoryPublishException, SubmissionAlreadyAcceptedWarning,
			SubmissionAcceptException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		File newPackageSource = temporaryFolder.newFile("newsource");
		File oldPackageSource = temporaryFolder.newFile("initialsource");
		String initialSource = oldPackageSource.getAbsolutePath();
				
		repository.setPublished(true);
		submission.setAccepted(false);
		packageBag.setActive(false);
		packageBag.setSource(initialSource);
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		when(packageStorage.moveToMainDirectory(packageBag)).thenReturn(newPackageSource);
		doNothing().when(packageService).updateSource(packageBag, newPackageSource.toString(), user);
		doNothing().when(packageService).activatePackage(packageBag, user);
		doNothing().when(repositoryService).boostRepositoryVersion(repository, user);
		doThrow(new RepositoryPublishException(messageSource, new Locale("en"), repository))
			.when(repositoryService).publishRepository(repository, user);
		doAnswer(new SubmissionEventAssertionAnswer(user, submission, updateEvent, 
				"accepted", "" + false, "" + true))
			.when(submissionEventService).create(any());
		doAnswer(new RepositoryEventAssertionAnswer(user, repository, updateEvent,
				"added", "", Integer.toString(packageBag.getId())))
			.when(repositoryEventService).create(any());
		when(packageStorage.moveSource(packageBag, initialSource)).thenReturn(oldPackageSource);
		
		assertThrows(
            MessageCodes.ERROR_SUBMISSION_ACCEPT,
            SubmissionAcceptException.class,
            () -> {
              submissionService.acceptSubmission(submission, user);
        });
		
		verify(packageStorage).moveToMainDirectory(packageBag);
		verify(packageService).updateSource(packageBag, newPackageSource.toString(), user);
		verify(packageService).activatePackage(packageBag, user);
		verify(repositoryService).boostRepositoryVersion(repository, user);
		verify(repositoryService).publishRepository(repository, user);
		verify(submissionEventService).create(any());
		verify(repositoryEventService).create(any());
		verify(packageStorage).moveSource(packageBag, initialSource);
	}
	
	@Test
	public void acceptSubmission_RevertsFilesystemChanges_WhenPackageEditExceptionIsThrown()
			throws IOException, EventNotFound, PackageSourceNotFoundException, MovePackageSourceException,
			PackageEditException, PackageAlreadyActivatedWarning, PackageActivateException,
			RepositoryEditException, RepositoryPublishException, SubmissionAlreadyAcceptedWarning,
			SubmissionAcceptException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		File newPackageSource = temporaryFolder.newFile("newsource");
		File oldPackageSource = temporaryFolder.newFile("initialsource");
		String initialSource = oldPackageSource.getAbsolutePath();
				
		repository.setPublished(true);
		submission.setAccepted(false);
		packageBag.setActive(false);
		packageBag.setSource(initialSource);
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		when(packageStorage.moveToMainDirectory(packageBag)).thenReturn(newPackageSource);
		doThrow(new PackageEditException(messageSource, new Locale("en"), packageBag))
			.when(packageService).updateSource(packageBag, newPackageSource.toString(), user);
		doNothing().when(packageService).activatePackage(packageBag, user);
		doNothing().when(repositoryService).boostRepositoryVersion(repository, user);
		doAnswer(new SubmissionEventAssertionAnswer(user, submission, updateEvent, 
				"accepted", "" + false, "" + true))
			.when(submissionEventService).create(any());
		doAnswer(new RepositoryEventAssertionAnswer(user, repository, updateEvent,
				"added", "", Integer.toString(packageBag.getId())))
			.when(repositoryEventService).create(any());
		when(packageStorage.moveSource(packageBag, initialSource)).thenReturn(oldPackageSource);
		
		assertThrows(
            MessageCodes.ERROR_SUBMISSION_ACCEPT,
            SubmissionAcceptException.class,
            () -> {
              submissionService.acceptSubmission(submission, user);
        });
		
		verify(packageStorage).moveToMainDirectory(packageBag);
		verify(packageService).activatePackage(packageBag, user);
		verify(repositoryService).boostRepositoryVersion(repository, user);
		verify(submissionEventService).create(any());
		verify(repositoryEventService).create(any());
		verify(packageStorage).moveSource(packageBag, initialSource);
	}
	
	@Test
	public void acceptSubmission_ThrowsSubmissionAcceptException_WhenEventIsNotFound()
			throws EventNotFound, SubmissionAlreadyAcceptedWarning, SubmissionAcceptException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		
		submission.setAccepted(false);
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());
		
		assertThrows(
            MessageCodes.ERROR_SUBMISSION_ACCEPT,
            SubmissionAcceptException.class,
            () -> {
              submissionService.acceptSubmission(submission, user);
        });
	}
	
	@Test
	public void acceptSubmission_ThrowsSubmissionEditException_WhenPackageActivateExceptionIsThrown() 
			throws IOException, EventNotFound, PackageAlreadyActivatedWarning, 
			PackageActivateException, RepositoryEditException, SubmissionAlreadyAcceptedWarning, 
			SubmissionAcceptException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		repository.setPublished(true);
		submission.setAccepted(false);
		packageBag.setActive(false);
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doThrow(new PackageActivateException(messageSource, new Locale("en"), packageBag))
			.when(packageService).activatePackage(packageBag, user);
		doNothing().when(repositoryService).boostRepositoryVersion(repository, user);
		doAnswer(new SubmissionEventAssertionAnswer(user, submission, updateEvent, 
				"accepted", "" + false, "" + true))
			.when(submissionEventService).create(any());
		doAnswer(new RepositoryEventAssertionAnswer(user, repository, updateEvent,
				"added", "", Integer.toString(packageBag.getId())))
			.when(repositoryEventService).create(any());
		
		assertThrows(
            MessageCodes.ERROR_SUBMISSION_ACCEPT,
            SubmissionAcceptException.class,
            () -> {
              submissionService.acceptSubmission(submission, user);
        });
		
		verify(packageService).activatePackage(packageBag, user);
		verify(repositoryService).boostRepositoryVersion(repository, user);
		verify(submissionEventService).create(any());
		verify(repositoryEventService).create(any());
	}
	
	@Test
	public void acceptSubmission_ThrowsSubmissionEditException_WhenRepositoryEditExceptionIsThrown() 
			throws EventNotFound, RepositoryEditException, SubmissionAlreadyAcceptedWarning, 
			SubmissionAcceptException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		repository.setPublished(true);
		submission.setAccepted(false);
		packageBag.setActive(false);
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doThrow(new RepositoryEditException(messageSource, new Locale("en"), repository))
			.when(repositoryService).boostRepositoryVersion(repository, user);

		assertThrows(
            MessageCodes.ERROR_SUBMISSION_ACCEPT,
            SubmissionAcceptException.class,
            () -> {
              submissionService.acceptSubmission(submission, user);
        });
		
		verify(repositoryService).boostRepositoryVersion(repository, user);
	}
	
	@Test
	public void acceptSubmission_ThrowsSubmissionAcceptException_WhenPackageSourceNotFoundException() 
			throws IOException, EventNotFound, PackageSourceNotFoundException, 
			MovePackageSourceException, PackageAlreadyActivatedWarning, PackageActivateException, 
			RepositoryEditException, SubmissionAlreadyAcceptedWarning, SubmissionAcceptException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		repository.setPublished(true);
		submission.setAccepted(false);
		packageBag.setActive(false);
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		
		PackageSourceNotFoundException exceptionToThrow = 
				new PackageSourceNotFoundException(messageSource, new Locale("en"), packageBag);
		when(packageStorage.moveToMainDirectory(packageBag))
			.thenThrow(exceptionToThrow);
		doNothing().when(packageService).activatePackage(packageBag, user);
		doNothing().when(repositoryService).boostRepositoryVersion(repository, user);
		doAnswer(new SubmissionEventAssertionAnswer(user, submission, updateEvent, 
				"accepted", "" + false, "" + true))
			.when(submissionEventService).create(any());
		doAnswer(new RepositoryEventAssertionAnswer(user, repository, updateEvent,
				"added", "", Integer.toString(packageBag.getId())))
			.when(repositoryEventService).create(any());
		
		assertThrows(
            MessageCodes.ERROR_SUBMISSION_ACCEPT,
            SubmissionAcceptException.class,
            () -> {
              submissionService.acceptSubmission(submission, user);
        });
		
		verify(packageStorage).moveToMainDirectory(packageBag);
		verify(packageService).activatePackage(packageBag, user);
		verify(repositoryService).boostRepositoryVersion(repository, user);
		verify(submissionEventService).create(any());
		verify(repositoryEventService).create(any());
	}
	
	@Test
	public void acceptSubmission_ThrowsSubmissionAcceptException_WhenMovePackageSourceException() 
			throws IOException, EventNotFound, PackageSourceNotFoundException, 
			MovePackageSourceException, PackageAlreadyActivatedWarning, PackageActivateException, 
			RepositoryEditException, SubmissionAlreadyAcceptedWarning, SubmissionAcceptException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		repository.setPublished(true);
		submission.setAccepted(false);
		packageBag.setActive(false);
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		
		MovePackageSourceException exceptionToThrow = 
				new MovePackageSourceException(messageSource, new Locale("en"), packageBag);
		when(packageStorage.moveToMainDirectory(packageBag))
			.thenThrow(exceptionToThrow);
		doNothing().when(packageService).activatePackage(packageBag, user);
		doNothing().when(repositoryService).boostRepositoryVersion(repository, user);
		doAnswer(new SubmissionEventAssertionAnswer(user, submission, updateEvent, 
				"accepted", "" + false, "" + true))
			.when(submissionEventService).create(any());
		doAnswer(new RepositoryEventAssertionAnswer(user, repository, updateEvent,
				"added", "", Integer.toString(packageBag.getId())))
			.when(repositoryEventService).create(any());
		
		assertThrows(
            MessageCodes.ERROR_SUBMISSION_ACCEPT,
            SubmissionAcceptException.class,
            () -> {
              submissionService.acceptSubmission(submission, user);
        });
		
		verify(packageStorage).moveToMainDirectory(packageBag);
		verify(packageService).activatePackage(packageBag, user);
		verify(repositoryService).boostRepositoryVersion(repository, user);
		verify(submissionEventService).create(any());
		verify(repositoryEventService).create(any());
	}
	
	@Test
	public void create() {
//		User user = UserTestFixture.GET_FIXTURE_ADMIN();
//		File packageFile = temporaryFolder.newFile("test_package.tar.gz");
//		FileInputStream fileInputStream = new FileInputStream(packageFile);
//		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
//		MockMultipartFile multipartFile = new MockMultipartFile("file", packageFile.getName(), 
//				MediaType.MULTIPART_FORM_DATA, fileInputStream);
//		String changes = "some_changes";
//		
//		PackageUploadRequest request = 
//				new PackageUploadRequest(multipartFile, repository.getName(), changes, false);
//		
//		Event createEvent = EventTestFixture.GET_FIXTURE_EVENT("create");
//		
//		packageBag.setSource(packageFile.getAbsolutePath());
//		
//		when(eventService.getCreateEvent()).thenReturn(createEvent);
//		when(uploadRequestService.createPackage(request, user)).thenReturn(packageBag);
//		when(submissionRepository.save(any())).thenAnswer(new Answer<Submission>() {
//
//			@Override
//			public Submission answer(InvocationOnMock invocation) throws Throwable {
//				Submission submission = invocation.getArgument(0);
//				
//				assertEquals("Submission user is not correct.", user, submission.getUser());
//				assertEquals("Submission package is not correct.", packageBag, submission.getPackage());
//				
//				return submission;
//			}
//		});
//		when(submissionEventService.create(eq(createEvent), eq(user), any()));
//		when(uploadRequestService.canUpload(packageBag.getName(), repository, user)).thenReturn(false);
		
	}
	
	@Test
	public void rejectSubmission_deletesSource() throws SourceFileDeleteException, SubmissionDeleteWarning, 
		SubmissionNotFound, SubmissionDeleteException, EventNotFound, SendEmailException, PackageSourceNotFoundException, MovePackageSourceException, PackageEditException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		int id = submission.getId();
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		
		submission.setDeleted(false);
		
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		when(submissionRepository.findByIdAndDeleted(id, false)).thenReturn(submission);
//		doNothing().when(packageService).deleteSource(packageBag);
		doNothing().when(packageService).moveSourceToTrashDirectory(packageBag, user);
		when(submissionEventService.create(deleteEvent, user, submission)).thenReturn(new ArrayList<>());
		doNothing().when(emailService).sendCanceledSubmissionEmail(submission);
		
		submissionService.rejectSubmission(id, user);
		
		assertTrue("Submission was not deleted correctly.", submission.isDeleted());
		verify(submissionRepository).findByIdAndDeleted(id, false);
//		verify(packageService).deleteSource(packageBag);
		verify(packageService).moveSourceToTrashDirectory(packageBag, user);
		verify(submissionEventService).create(deleteEvent, user, submission);
		verify(emailService).sendCanceledSubmissionEmail(submission);
	}
	
	@Test
	public void deleteSubmission() throws EventNotFound, SubmissionDeleteException,
		SubmissionNotFound, SubmissionDeleteWarning, SendEmailException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		
		submission.setDeleted(false);
		
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		when(submissionEventService.create(deleteEvent, user, submission)).thenReturn(new ArrayList<>());
		doNothing().when(emailService).sendCanceledSubmissionEmail(submission);
		
		submissionService.deleteSubmission(submission, user);
		
		assertTrue("Submission was not deleted correctly.", submission.isDeleted());
		verify(submissionEventService).create(deleteEvent, user, submission);
		verify(emailService).sendCanceledSubmissionEmail(submission);
	}
	
	@Test
	public void deleteSubmission_ThrowsSubmissionDeleteException_WhenEventIsNotFound() 
			throws EventNotFound, SubmissionDeleteException, SubmissionNotFound, SubmissionDeleteWarning {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		
		submission.setDeleted(false);
		
		when(eventService.getDeleteEvent()).thenThrow(new EventNotFound());

		assertThrows(
            MessageCodes.ERROR_SUBMISSION_DELETE,
            SubmissionDeleteException.class,
            () -> {
              submissionService.deleteSubmission(submission, user);
        });
	}
	
	@Test
	public void deleteSubmission_ThrowsSubmissionDeleteException_WhenSourceFileDeleteExceptionIsThrown() 
			throws EventNotFound, SourceFileDeleteException, SubmissionDeleteException, 
				SubmissionNotFound, SubmissionDeleteWarning, PackageSourceNotFoundException, MovePackageSourceException, PackageEditException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		int id = submission.getId();
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		
		submission.setDeleted(false);
		
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		when(submissionRepository.findByIdAndDeleted(id, false)).thenReturn(submission);
		doThrow(new MovePackageSourceException(messageSource, new Locale("en"), packageBag))
			.when(packageService).moveSourceToTrashDirectory(packageBag, user);

		assertThrows(
            MessageCodes.ERROR_SUBMISSION_DELETE,
            SubmissionDeleteException.class,
            () -> {
              submissionService.rejectSubmission(id, user);
        });
	}
	
	@Test
	public void deleteSubmission_ThrowsSubmissionDeleteWarning_WhenSendEmailExceptionIsThrown() 
			throws EventNotFound, SendEmailException, SubmissionDeleteException, 
					SubmissionNotFound, SubmissionDeleteWarning {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		
		submission.setDeleted(false);
		
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		when(submissionEventService.create(deleteEvent, user, submission)).thenReturn(new ArrayList<>());
		doThrow(new SendEmailException("error while sending email")).when(emailService).sendCanceledSubmissionEmail(submission);

		assertThrows(
            MessageCodes.WARNING_SUBMISSION_DELETE,
            SubmissionDeleteWarning.class,
            () -> {
              submissionService.deleteSubmission(submission, user);
        });
	}
	
	@Test
	public void shiftDelete() throws SubmissionDeleteException, SubmissionNotFound, PackageDeleteException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
		
		List<SubmissionEvent> events = SubmissionEventTestFixture.GET_SUBMISSION_EVENT_TEST_FIXTURE(3);
		submission.setSubmissionEvents(new HashSet<SubmissionEvent>(events));
		
		doNothing().when(submissionEventService).delete(any());
		doNothing().when(submissionRepository).delete(submission);
//		doNothing().when(packageService).shiftDelete(submission.getPackage());		
		submissionService.shiftDelete(submission);
		
		verify(submissionEventService, times(3)).delete(any());
		verify(submissionRepository).delete(submission);
	}


	
//	@Test
//	public void shiftDeleteSubmissionForRejectedPackage() throws PackageDeleteException, SubmissionNotFound {
//		User user = UserTestFixture.GET_FIXTURE_ADMIN();
//		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
//		Package packageBag = PackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
//		Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
//		
//		when(submissionRepository.findByIdAndDeleted(submission.getId(), true)).thenReturn(submission);
//		when(packageService.shiftDeleteForRejectedSubmission(submission.getPackage())).thenReturn(packageBag);
//		
//		submissionService.shiftDeleteSubmissionForRejectedPackage(submission.getId());
//		verify(packageService).shiftDeleteForRejectedSubmission(packageBag);
//	}
//	
//	@Test
//	public void shiftDeleteSubmissionForRejectedPackage_ThrowsSubmissionNotFoundException_WhenSubmissionIsNotFound() throws SubmissionDeleteException, SubmissionNotFound {
//		int ID = 123;
//		
//		when(submissionRepository.findByIdAndDeleted(ID, true)).thenReturn(null);
//		
//		expectedException.expect(SubmissionNotFound.class);
//		expectedException.expectMessage(MessageCodes.ERROR_SUBMISSION_NOT_FOUND);
//		
//		submissionService.shiftDelete(ID);
//	}
}

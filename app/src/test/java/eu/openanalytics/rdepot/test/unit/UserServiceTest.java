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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;
import eu.openanalytics.rdepot.exception.AdminNotFound;
import eu.openanalytics.rdepot.exception.EventNotFound;
import eu.openanalytics.rdepot.exception.NoAdminLeftException;
import eu.openanalytics.rdepot.exception.PackageEditException;
import eu.openanalytics.rdepot.exception.PackageMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.PackageMaintainerNotFound;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.SubmissionEditException;
import eu.openanalytics.rdepot.exception.UserActivateException;
import eu.openanalytics.rdepot.exception.UserCreateException;
import eu.openanalytics.rdepot.exception.UserDeactivateException;
import eu.openanalytics.rdepot.exception.UserDeleteException;
import eu.openanalytics.rdepot.exception.UserEditException;
import eu.openanalytics.rdepot.exception.UserNotFound;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageEvent;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.PackageMaintainerEvent;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryEvent;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.RepositoryMaintainerEvent;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.SubmissionEvent;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.model.UserEvent;
import eu.openanalytics.rdepot.repository.UserRepository;
import eu.openanalytics.rdepot.service.EventService;
import eu.openanalytics.rdepot.service.PackageEventService;
import eu.openanalytics.rdepot.service.PackageMaintainerEventService;
import eu.openanalytics.rdepot.service.PackageMaintainerService;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.RepositoryEventService;
import eu.openanalytics.rdepot.service.RepositoryMaintainerEventService;
import eu.openanalytics.rdepot.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.service.RoleService;
import eu.openanalytics.rdepot.service.SubmissionEventService;
import eu.openanalytics.rdepot.service.SubmissionService;
import eu.openanalytics.rdepot.service.UserEventService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.test.fixture.EventTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.RoleTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.time.TestDateRule;
import eu.openanalytics.rdepot.time.DateProvider;
import eu.openanalytics.rdepot.warning.UserAlreadyActivatedWarning;
import eu.openanalytics.rdepot.warning.UserAlreadyDeactivatedWarning;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {
	
	@Mock
	UserRepository userRepository;
	
	@Mock
	RoleService roleService;
	
	@Mock
	UserEventService userEventService;
	
	@Mock
	EventService eventService;
	
	@Mock
	SubmissionService submissionService;
	
	@Mock
	PackageService packageService;
	
	@Mock
	RepositoryMaintainerService repositoryMaintainerService;
	
	@Mock
	PackageMaintainerService packageMaintainerService;
	
	@Mock
	RepositoryEventService repositoryEventService;
	
	@Mock
	PackageEventService packageEventService;
	
	@Mock
	SubmissionEventService submissionEventService;
	
	@Mock
	PackageMaintainerEventService packageMaintainerEventService;
	
	@Mock
	RepositoryMaintainerEventService repositoryMaintainerEventService;
	
	@Mock
	MessageSource messageSource;
		
	@InjectMocks
	UserService userService;
	
	@Rule
	public TestDateRule testDateRule = new TestDateRule();
	
	private class UserEventAssertionAnswer implements Answer<UserEvent> {

		User updater;
		User user;
		Event baseEvent;
		String changedVariable;
		String valueBefore;
		String valueAfter;
		
		public UserEventAssertionAnswer(User updater, User user, Event baseEvent, 
			String changedVariable, String valueBefore, String valueAfter) {
			super();
			this.updater = updater;
			this.user = user;
			this.baseEvent = baseEvent;
			this.changedVariable = changedVariable;
			this.valueBefore = valueBefore;
			this.valueAfter = valueAfter;
		}

		@Override
		public UserEvent answer(InvocationOnMock invocation) throws Throwable {
			UserEvent createdEvent = invocation.getArgument(0);
			
			assertEquals("Id of created event should be 0", 0, createdEvent.getId());
			assertEquals("Admin object should be an updater", updater, createdEvent.getChangedBy());
			assertEquals("Incorrect user", user, createdEvent.getUser());
			assertEquals("Incorrect base event", baseEvent, createdEvent.getEvent());
			assertEquals("Incorrect changed variable", changedVariable, createdEvent.getChangedVariable());
			assertEquals("Incorrect value before", valueBefore, createdEvent.getValueBefore());
			assertEquals("Incorrect value after", valueAfter, createdEvent.getValueAfter());
			assertEquals("Event date is not correct", DateProvider.now(), createdEvent.getDate());
			assertEquals("Event time is not correct", DateProvider.now(), createdEvent.getTime());
			
			return createdEvent;
		}
		
	}
	
	@Before
	public void init() {
		when(messageSource.getMessage(anyString(), isNull(), anyString(), any())).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return invocation.getArgument(0);
			}
			
		});
	}
	
	@Test
	public void create_CreatesUserAndConsidersItAdmin_IfNoAdminIsFound() throws EventNotFound, UserCreateException, AdminNotFound {
		User user = UserTestFixture.GET_FIXTURE_USER();
		Event createEvent = EventTestFixture.GET_FIXTURE_EVENT("create");
		
		when(userRepository.save(user)).thenReturn(user);
		when(eventService.getCreateEvent()).thenReturn(createEvent);
		when(roleService.getAdminRole()).thenReturn(null);
		when(userEventService.create(createEvent, user, user)).thenReturn(null);
		
		User result = userService.create(user);
		
		assertEquals("UserService unit test: created user is not correct!", user, result);
		verify(userEventService).create(createEvent, user, user);
	}
	
	@Test
	public void create_CreatesUser_IfAdminIsFound() throws EventNotFound, UserCreateException {
		User user = UserTestFixture.GET_FIXTURE_USER();
		Event createEvent = EventTestFixture.GET_FIXTURE_EVENT("create");
		Role adminRole = RoleTestFixture.GET_FIXTURE_ROLES(1, 0, 0, 0).get(0);
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		ArrayList<User> admins = new ArrayList<>();
		admins.add(admin);
		
		when(userRepository.save(user)).thenReturn(user);
		when(eventService.getCreateEvent()).thenReturn(createEvent);
		when(roleService.getAdminRole()).thenReturn(adminRole);
		when(userRepository.findByRoleAndDeleted(adminRole, false)).thenReturn(admins);
		when(userEventService.create(createEvent, admin, user)).thenReturn(null);
		
		User result = userService.create(user);
		
		assertEquals("UserService unit test: created user is not correct!", user, result);
		verify(userEventService).create(createEvent, admin,user);
	}

	@Test
	public void create_ThrowsUserCreateException_IfNoEventIsFound() throws EventNotFound, UserCreateException {
		User user = UserTestFixture.GET_FIXTURE_USER();

		when(userRepository.save(user)).thenReturn(user);
		when(eventService.getCreateEvent()).thenThrow(new EventNotFound());

		assertThrows(
		    user.getLogin() + ": " + MessageCodes.ERROR_USER_CREATE,
		    UserCreateException.class,
            () -> {
              userService.create(user);
        });
	}
	
	@Test
	public void shiftDelete_ThrowsUserNotFoundException_IfUserIsNotFound() throws UserNotFound {
		when(userRepository.findByIdAndDeleted(0, true)).thenReturn(null);

		assertThrows(
		    "User " + Integer.toString(0) + ": " + MessageCodes.ERROR_USER_NOT_FOUND,
            UserNotFound.class,
            () -> {
              userService.shiftDelete(0);
        });
	}
	
	@Test
	public void shiftDelete_ReturnsDeletedUser() throws UserNotFound {
		final int id = 0;
		User user = UserTestFixture.GET_FIXTURE_USER();
		user.setDeleted(true);
		
		when(userRepository.findByIdAndDeleted(id,  true)).thenReturn(user);
		doNothing().when(userRepository).delete(user);
		
		User result = userService.shiftDelete(id);
		
		assertEquals("UserService unit test: deleted user is not correct!", result, user);
	}
		
	@Test
	public void deleteEvents_DeletesAllEventsOfGivenUser() {
		final int id = new Random().nextInt();
		User user = UserTestFixture.GET_FIXTURE_USER();
		
		UserEvent userEvent = new UserEvent();
		userEvent.setId(id);
		
		PackageEvent packageEvent = new PackageEvent();
		packageEvent.setId(id);
		
		RepositoryEvent repositoryEvent = new RepositoryEvent();
		repositoryEvent.setId(id);
		
		PackageMaintainerEvent packageMaintainerEvent = new PackageMaintainerEvent();
		packageMaintainerEvent.setId(id);
		
		RepositoryMaintainerEvent repositoryMaintainerEvent = new RepositoryMaintainerEvent();
		repositoryMaintainerEvent.setId(id);
		
		SubmissionEvent submissionEvent = new SubmissionEvent();
		submissionEvent.setId(id);
		
		Set<UserEvent> userEvents = new HashSet<>();
		userEvents.add(userEvent);
		user.setUserEvents(userEvents);
		user.setChangedUserEvents(userEvents);
		doNothing().when(userEventService).delete(id);
		
		Set<PackageEvent> packageEvents = new HashSet<>();
		packageEvents.add(packageEvent);
		user.setChangedPackageEvents(packageEvents);
		doNothing().when(packageEventService).delete(id);
		
		Set<RepositoryEvent> repositoryEvents = new HashSet<>();
		repositoryEvents.add(repositoryEvent);
		user.setChangedRepositoryEvents(repositoryEvents);
		doNothing().when(repositoryEventService).delete(id);
		
		Set<PackageMaintainerEvent> packageMaintainerEvents = new HashSet<>();
		packageMaintainerEvents.add(packageMaintainerEvent);
		user.setChangedPackageMaintainerEvents(packageMaintainerEvents);
		doNothing().when(packageMaintainerEventService).delete(id);
		
		Set<RepositoryMaintainerEvent> repositoryMaintainerEvents = new HashSet<>();
		repositoryMaintainerEvents.add(repositoryMaintainerEvent);
		user.setChangedRepositoryMaintainerEvents(repositoryMaintainerEvents);
		doNothing().when(repositoryMaintainerEventService).delete(id);
		
		Set<SubmissionEvent> submissionEvents = new HashSet<>();
		submissionEvents.add(submissionEvent);
		user.setChangedSubmissionEvents(submissionEvents);
		doNothing().when(submissionEventService).delete(id);
		
		userService.deleteEvents(user);
		
		verify(userEventService, times(2)).delete(id);
		verify(packageEventService).delete(id);
		verify(repositoryEventService).delete(id);
		verify(packageMaintainerEventService).delete(id);
		verify(repositoryMaintainerEventService).delete(id);
		verify(submissionEventService).delete(id);
	}
	
	@Test
	public void delete_ThrowsUserNotFound_IfUserIsNotFound() throws UserDeleteException, UserNotFound {
		final int id = new Random().nextInt();
		when(userRepository.findByIdAndDeleted(id, false)).thenReturn(null);
		
		assertThrows(
            "User " + Integer.toString(id) + ": " + MessageCodes.ERROR_USER_NOT_FOUND,
            UserNotFound.class,
            () -> {
              userService.delete(id, UserTestFixture.GET_FIXTURE_ADMIN());
        });
	}
	
	@Test
	public void delete_ThrowsUserDeleteException_IfEventIsNotFound() throws EventNotFound, UserDeleteException, UserNotFound {
		User user = UserTestFixture.GET_FIXTURE_USER();
		
		final int id = new Random().nextInt();
		when(userRepository.findByIdAndDeleted(id, false)).thenReturn(user);
		when(eventService.getDeleteEvent()).thenThrow(new EventNotFound());

		assertThrows(
		    "User " + id + ": " + MessageCodes.ERROR_USER_DELETE,
            UserDeleteException.class,
            () -> {
              userService.delete(id, UserTestFixture.GET_FIXTURE_ADMIN());
        });
	}
	
	@Test
	public void delete_ReturnsDeletedUser_IfUserHasStandardCredentials() throws EventNotFound, UserDeleteException, UserNotFound, SubmissionEditException, AdminNotFound {
		User user = UserTestFixture.GET_FIXTURE_USER();
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		Set<Submission> submissions = new HashSet<Submission>();
		user.setSubmissions(submissions);
		final int id = new Random().nextInt();
		
		user.setDeleted(false);
		
		when(userRepository.findByIdAndDeleted(id, false)).thenReturn(user);
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		doNothing().when(submissionService).fixSubmissions(user.getSubmissions(), admin);
		when(userEventService.create(deleteEvent, admin, user)).thenReturn(new ArrayList<UserEvent>());
		
		User result = userService.delete(id, admin);
		
		assertEquals("Deleted user is not correct", user, result);
		assertTrue("User was not set as deleted", user.isDeleted());
	}
	
	@Test
	public void delete_ReturnsDeletedUser_IfUserHasRepositoryMaintainerCredentials() 
			throws EventNotFound, RepositoryMaintainerDeleteException, UserDeleteException, 
			SubmissionEditException, AdminNotFound, UserNotFound {
		final int id = new Random().nextInt();
		User user = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		Set<Submission> submissions = new HashSet<Submission>();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		ArrayList<Repository> repositories = new ArrayList<Repository>();
		repositories.add(repository);
		List<RepositoryMaintainer> repositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(user, repositories);
		
		user.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(repositoryMaintainers));
		user.setSubmissions(submissions);
		user.setDeleted(false);
		user.setId(id);
		
		when(userRepository.findByIdAndDeleted(id, false)).thenReturn(user);
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		doNothing().when(submissionService).fixSubmissions(user.getSubmissions(), admin);
		when(userEventService.create(deleteEvent, admin, user)).thenReturn(new ArrayList<UserEvent>());
		when(repositoryMaintainerService.delete(repositoryMaintainers.get(0), admin)).thenReturn(null);
		when(packageService.findByRepositoryAndMaintainer(repository, user)).thenReturn(new ArrayList<Package>());
		
		User result = userService.delete(id, admin);
		
		assertEquals("Deleted user is not correct", user, result);
		assertTrue("User was not set as deleted", user.isDeleted());
		
		verify(repositoryMaintainerService).delete(repositoryMaintainers.get(0), admin);
	}
	
	@Test
	public void delete_ReturnsDeletedUser_IfUserHasPackageMaintainerCredentials() 
			throws EventNotFound, PackageMaintainerDeleteException, UserDeleteException, 
			SubmissionEditException, AdminNotFound, UserNotFound, PackageMaintainerNotFound {
		final int id = new Random().nextInt();
		User user = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		Set<Submission> submissions = new HashSet<Submission>();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		List<PackageMaintainer> packageMaintainers = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(user, repository,1);
		
		user.setPackageMaintainers(new HashSet<>(packageMaintainers));
		user.setSubmissions(submissions);
		user.setDeleted(false);
		user.setId(id);
		
		when(userRepository.findByIdAndDeleted(id, false)).thenReturn(user);
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		doNothing().when(submissionService).fixSubmissions(user.getSubmissions(), admin);
		when(userEventService.create(deleteEvent, admin, user)).thenReturn(new ArrayList<UserEvent>());
		when(packageMaintainerService.delete(packageMaintainers.get(0).getId(), admin)).thenReturn(null);
		when(packageService.findByNameAndRepository(packageMaintainers.get(0).getPackage(), repository)).thenReturn(new ArrayList<Package>());
		
		User result = userService.delete(id, admin);
		
		assertEquals("Deleted user is not correct", user, result);
		assertTrue("User was not set as deleted", user.isDeleted());
		
		verify(packageMaintainerService).delete(packageMaintainers.get(0).getId(), admin);
	}
	
	@Test
	public void delete_ReturnsDeletedUser_IfAnotherAdminIsFound() throws EventNotFound, 
		PackageEditException, UserDeleteException, UserNotFound, SubmissionEditException, AdminNotFound {
		final int id = new Random().nextInt();
		final int packageCount = new Random().nextInt(10);
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		List<Package> packages = PackageTestFixture.GET_FIXTURE_PACKAGES(new Repository(), user, packageCount);
		
		Role adminRole = RoleTestFixture.GET_FIXTURE_ROLES(1, 0, 0, 0).get(0);
		Set<User> admins = new HashSet<>();
		admins.add(admin);
		admins.add(user);
		adminRole.setUsers(admins);
		
		user.setDeleted(false);
		user.setId(id);
		
		when(userRepository.findByIdAndDeleted(id, false)).thenReturn(user);
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		doNothing().when(submissionService).fixSubmissions(eq(user.getSubmissions()), eq(admin));
		when(roleService.getAdminRole()).thenReturn(adminRole);
		when(packageService.findAll()).thenReturn(packages);
		
		User result = userService.delete(id, admin);
		
		assertEquals("Deleted user is not correct", user, result);
		assertTrue("User was not set as deleted", user.isDeleted());
		
		verify(packageService, times(packageCount)).refreshMaintainer(any(), eq(admin));
	}
	
	@Test
	public void delete_ThrowsUserDeleteException_IfOnlyAdminIsLeft() throws EventNotFound, UserDeleteException, UserNotFound {
		final int id = new Random().nextInt();
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");

		Role adminRole = RoleTestFixture.GET_FIXTURE_ROLES(1, 0, 0, 0).get(0);
		Set<User> admins = new HashSet<>();
		admins.add(admin);
		adminRole.setUsers(admins);

		admin.setDeleted(false);
		admin.setId(id);
		
		when(userRepository.findByIdAndDeleted(id, false)).thenReturn(admin);
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		when(roleService.getAdminRole()).thenReturn(adminRole);
		
		assertThrows(
            "User " + id + ": " + MessageCodes.ERROR_USER_DELETE,
            UserDeleteException.class,
            () -> {
              userService.delete(id, admin);
        });
		
		assertFalse("Admin should not be deleted if exception is thrown", admin.isDeleted());
	}
	
	@Test
	public void delete_ThrowsUserDeleteException_IfNoAdminRoleIsFound() throws EventNotFound, UserDeleteException, UserNotFound {
		final int id = new Random().nextInt();
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		
		admin.setDeleted(false);
		admin.setId(id);
		
		when(userRepository.findByIdAndDeleted(id, false)).thenReturn(admin);
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		when(roleService.getAdminRole()).thenReturn(null);
		
		assertThrows(
            "User " + id + ": " + MessageCodes.ERROR_USER_DELETE,
            UserDeleteException.class,
            () -> {
              userService.delete(id, admin);
        });
		
		assertFalse("Admin should not be deleted if exception is thrown", admin.isDeleted());
	}
	
	@Test
	public void activateUser() throws UserEditException, EventNotFound, UserActivateException, UserAlreadyActivatedWarning {
		User user = UserTestFixture.GET_FIXTURE_USER();
		final User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		user.setActive(false);
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doAnswer(new UserEventAssertionAnswer(admin, user, updateEvent, "active", "" + false, "" + true))
			.when(userEventService).create(any());
		
		userService.activateUser(user, admin);
		
		assertTrue("User should be set active", user.isActive());
		
		verify(userEventService).create(any());
	}
	
	@Test
	public void activateUser_FindsBestUpdater_IfUpdaterIsNull() throws EventNotFound, UserEditException, UserActivateException, UserAlreadyActivatedWarning {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_USER();
		Role adminRole = RoleTestFixture.GET_FIXTURE_ADMIN_ROLE();
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		List<User> admins = new ArrayList<User>();
		admins.add(admin);
		
		user.setActive(false);
		
		when(roleService.getAdminRole()).thenReturn(adminRole);
		when(userRepository.findByRoleAndDeleted(adminRole, false)).thenReturn(admins);
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doAnswer(new UserEventAssertionAnswer(admin, user, updateEvent, "active", "" + false, "" + true))
			.when(userEventService).create(any());
		
		userService.activateUser(user, null);
		
		assertTrue("User should be set active", user.isActive());
		
		verify(userEventService).create(any());
	}
	
	@Test
	public void activateUser_ThrowsUserAlreadyActivatedWarning_IfUserIsAlreadyActive() throws UserActivateException, UserAlreadyActivatedWarning {
		User user = UserTestFixture.GET_FIXTURE_USER();
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		
		user.setActive(true);

		assertThrows(
		    user.getLogin() + ": " + MessageCodes.WARNING_USER_ALREADY_ACTIVATED,
		    UserAlreadyActivatedWarning.class,
            () -> {
              userService.activateUser(user, admin);
        });
	}
	
	@Test
	public void activateUser_ThrowsUserActivateException_IfEventIsNotFound() throws EventNotFound, UserActivateException, UserAlreadyActivatedWarning {
		User user = UserTestFixture.GET_FIXTURE_USER();
		final User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		
		user.setActive(false);
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());
		
		assertThrows(
            user.getLogin() + ": " + MessageCodes.ERROR_USER_ACTIVATE,
            UserActivateException.class,
            () -> {
              userService.activateUser(user, admin);
        });
	}
	
	@Test
	public void deactivateUser() throws EventNotFound, UserDeactivateException, UserAlreadyDeactivatedWarning, NoAdminLeftException {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_USER();
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		user.setActive(true);
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doAnswer(new UserEventAssertionAnswer(admin, user, updateEvent, "active", "" + true, "" + false))
			.when(userEventService).create(any());
		
		userService.deactivateUser(user, admin);
		
		assertFalse("User should be set inactive", user.isActive());
		
		verify(userEventService).create(any());
	}
	
	@Test
	public void deactivateUser_FindsBestUpdater_IfUpdaterIsNull() throws EventNotFound, UserEditException, UserDeactivateException, UserAlreadyDeactivatedWarning, NoAdminLeftException {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_USER();
		Role adminRole = RoleTestFixture.GET_FIXTURE_ADMIN_ROLE();
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		List<User> admins = new ArrayList<User>();
		admins.add(admin);
		
		user.setActive(true);
		
		when(roleService.getAdminRole()).thenReturn(adminRole);
		when(userRepository.findByRoleAndDeleted(adminRole, false)).thenReturn(admins);
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doAnswer(new UserEventAssertionAnswer(admin, user, updateEvent, "active", "" + true, "" + false))
		.when(userEventService).create(any());
		
		userService.deactivateUser(user, null);
		
		assertFalse("User should be set active", user.isActive());
		
		verify(userEventService).create(any());
	}
	
	@Test
	public void deactivateUser_ThrowsUserAlreadyDeactivatedWarning_IfUserIsAlreadyInactive() throws UserDeactivateException, UserAlreadyDeactivatedWarning, NoAdminLeftException {
		User user = UserTestFixture.GET_FIXTURE_USER();
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		
		user.setActive(false);

		assertThrows(
            user.getLogin() + ": " + MessageCodes.WARNING_USER_ALREADY_DEACTIVATED,
            UserAlreadyDeactivatedWarning.class,
            () -> {
              userService.deactivateUser(user, admin);
        });
	}
	
	@Test
	public void deactivateUser_ThrowsUserEditException_IfEventIsNotFound() throws EventNotFound, UserDeactivateException, UserAlreadyDeactivatedWarning, NoAdminLeftException {
		User user = UserTestFixture.GET_FIXTURE_USER();
		final User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		
		user.setActive(true);
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());
		
		assertThrows(
            user.getLogin() + ": " + MessageCodes.ERROR_USER_DEACTIVATE,
            UserDeactivateException.class,
            () -> {
              userService.deactivateUser(user, admin);
        });
	}
	
	@Test
	public void updateLastLoggedInOn() throws EventNotFound, UserEditException {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_USER();
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		Date currentLastLoggedInOn = new GregorianCalendar(2017, Calendar.JULY, 2).getTime();
		Date newLastLoggedInOn = new GregorianCalendar(2017, Calendar.AUGUST, 11).getTime();
		
		user.setLastLoggedInOn(currentLastLoggedInOn);
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doAnswer(new UserEventAssertionAnswer(admin, user, updateEvent, "last logged in", "" + currentLastLoggedInOn, "" + newLastLoggedInOn))
		.when(userEventService).create(any());
		
		userService.updateLastLoggedInOn(user, admin, newLastLoggedInOn);
		
		assertTrue("LastLoggedInOn value should be updated", user.getLastLoggedInOn().equals(newLastLoggedInOn));
		
		verify(userEventService).create(any());
	}
	
	@Test
	public void updateLastLoggedInOn_FindsBestUpdater_IfUpdaterIsNull() throws EventNotFound, UserEditException {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_USER();
		Role adminRole = RoleTestFixture.GET_FIXTURE_ADMIN_ROLE();
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		List<User> admins = new ArrayList<User>();
		admins.add(admin);
		
		Date currentLastLoggedInOn = new GregorianCalendar(2017, Calendar.JULY, 2).getTime();
		Date newLastLoggedInOn = new GregorianCalendar(2017, Calendar.AUGUST, 11).getTime();
		
		user.setLastLoggedInOn(currentLastLoggedInOn);
		
		when(roleService.getAdminRole()).thenReturn(adminRole);
		when(userRepository.findByRoleAndDeleted(adminRole, false)).thenReturn(admins);
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doAnswer(new UserEventAssertionAnswer(admin, user, updateEvent, "last logged in", "" + currentLastLoggedInOn, "" + newLastLoggedInOn))
		.when(userEventService).create(any());
		
		userService.updateLastLoggedInOn(user, null, newLastLoggedInOn);
		
		assertTrue("LastLoggedInOn value should be updated", user.getLastLoggedInOn().equals(newLastLoggedInOn));
		
		verify(userEventService).create(any());
	}
	
	@Test
	public void updateLastLoggedInOn_ThrowsUserEditException_IfEventIsNotFound() throws EventNotFound, UserEditException {
		User user = UserTestFixture.GET_FIXTURE_USER();
		final User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		
		Date currentLastLoggedInOn = new GregorianCalendar(2018, Calendar.JULY, 1).getTime();
		
		user.setLastLoggedInOn(currentLastLoggedInOn);
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());

		assertThrows(
            user.getLogin() + ": " + MessageCodes.ERROR_USER_EDIT,
            UserEditException.class,
            () -> {
              userService.updateLastLoggedInOn(user, admin, new Date());
        });
	}
	
	@Test
	public void updateName_FindsBestUpdater_IfUpdaterIsNull() throws EventNotFound, UserEditException {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_USER();
		Role adminRole = RoleTestFixture.GET_FIXTURE_ADMIN_ROLE();
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		List<User> admins = new ArrayList<User>();
		admins.add(admin);
		
		String oldName = "Albert Einstein";
		String newName = "Albus Dumbledore";
		
		user.setName(oldName);
		
		when(roleService.getAdminRole()).thenReturn(adminRole);
		when(userRepository.findByRoleAndDeleted(adminRole, false)).thenReturn(admins);
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doAnswer(new UserEventAssertionAnswer(admin, user, updateEvent, "name", oldName, newName))
		.when(userEventService).create(any());
		
		userService.updateName(user, null, newName);
		
		assertTrue("Name should be updated", user.getName().equals(newName));
		
		verify(userEventService).create(any());
	}
	
	@Test
	public void updateName_ThrowsUserEditException_IfEventIsNotFound() throws EventNotFound, UserEditException {
		User user = UserTestFixture.GET_FIXTURE_USER();
		final User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		
		String oldName = "Albert Einstein";
		
		user.setName(oldName);
		String newName = "Albus Dumbledore";
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());

		assertThrows(
            user.getLogin() + ": " + MessageCodes.ERROR_USER_EDIT,
            UserEditException.class,
            () -> {
              userService.updateName(user, admin, newName);
        });
	}
	
	@Test
	public void updateEmail_FindsBestUpdater_IfUpdaterIsNull() throws EventNotFound, UserEditException {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_USER();
		Role adminRole = RoleTestFixture.GET_FIXTURE_ADMIN_ROLE();
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		List<User> admins = new ArrayList<User>();
		admins.add(admin);
		
		String oldEmail = "einstein@localhost";
		String newEmail = "einstein@gmail.com";
		
		user.setEmail(oldEmail);
		
		when(roleService.getAdminRole()).thenReturn(adminRole);
		when(userRepository.findByRoleAndDeleted(adminRole, false)).thenReturn(admins);
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doAnswer(new UserEventAssertionAnswer(admin, user, updateEvent, "email", oldEmail, newEmail))
		.when(userEventService).create(any());
		
		userService.updateEmail(user, null, newEmail);
		
		assertTrue("Email should be updated", user.getEmail().equals(newEmail));
		
		verify(userEventService).create(any());
	}
	
	@Test
	public void updateEmail_ThrowsUserEditException_IfEventIsNotFound() throws EventNotFound, UserEditException {
		User user = UserTestFixture.GET_FIXTURE_USER();
		final User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		
		String oldEmail = "einstein@localhost";
		String newEmail = "einstein@gmail.com";
		
		user.setEmail(oldEmail);
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());

		assertThrows(
            user.getLogin() + ": " + MessageCodes.ERROR_USER_EDIT,
            UserEditException.class,
            () -> {
              userService.updateEmail(user, admin, newEmail);
        });
	}
	
	@Test
	public void updateRole_FromUserToPackageMaintainer() throws EventNotFound, UserEditException {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_USER();
		Role packageMaintainerRole = RoleTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER_ROLE();
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doAnswer(new UserEventAssertionAnswer(admin, user, updateEvent, 
				"role", "" + RoleTestFixture.GET_FIXTURE_USER_ROLE().getId(),
				"" + packageMaintainerRole.getId()))
			.when(userEventService).create(any());
		
		userService.updateRole(user, admin, packageMaintainerRole);
		
		assertEquals("Updated role id is not correct", packageMaintainerRole.getId(), user.getRole().getId());
	
		verify(userEventService).create(any());
	}
	
	@Test
	public void updateRole_FromUserToRepositoryMaintainer() throws EventNotFound, UserEditException {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_USER();
		Role repositoryMaintainerRole = RoleTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINER_ROLE();
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doAnswer(new UserEventAssertionAnswer(admin, user, updateEvent, 
				"role", "" + RoleTestFixture.GET_FIXTURE_USER_ROLE().getId(),
				"" + repositoryMaintainerRole.getId()))
			.when(userEventService).create(any());
		userService.updateRole(user, admin, repositoryMaintainerRole);
		
		assertEquals("Updated role id is not correct", repositoryMaintainerRole.getId(), user.getRole().getId());
	
		verify(userEventService).create(any());
	}
	
	@Test
	public void updateRole_FromUserToAdmin() throws EventNotFound, UserEditException {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_USER();
		Role adminRole = RoleTestFixture.GET_FIXTURE_ADMIN_ROLE();
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doAnswer(new UserEventAssertionAnswer(admin, user, updateEvent, 
				"role", "" + RoleTestFixture.GET_FIXTURE_USER_ROLE().getId(),
				"" + adminRole.getId()))
			.when(userEventService).create(any());
		userService.updateRole(user, admin, adminRole);
		
		assertEquals("Updated role id is not correct", adminRole.getId(), user.getRole().getId());
	
		verify(userEventService).create(any());
	}
	
	@Test
	public void updateRole_FromAdminToUser() throws EventNotFound, UserEditException, PackageEditException {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Role userRole = RoleTestFixture.GET_FIXTURE_USER_ROLE();
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		Role adminRole = RoleTestFixture.GET_FIXTURE_ADMIN_ROLE();
		ArrayList<User> admins = new ArrayList<User>();
		admins.add(user);
		admins.add(admin);
		adminRole.setUsers(new HashSet<>(admins));
		
		List<Package> packages = PackageTestFixture.GET_FIXTURE_PACKAGES(RepositoryTestFixture.GET_FIXTURE_REPOSITORY(), UserTestFixture.GET_FIXTURE_USER(), 1);

		
		when(packageService.findAll()).thenReturn(packages);
		doNothing().when(packageService).refreshMaintainer(packages.get(0), admin);
		when(roleService.getAdminRole()).thenReturn(adminRole);
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doAnswer(new UserEventAssertionAnswer(admin, user, updateEvent, 
				"role", "" + RoleTestFixture.GET_FIXTURE_ADMIN_ROLE().getId(),
				"" + userRole.getId()))
			.when(userEventService).create(any());
		userService.updateRole(user, admin, userRole);
		
		assertEquals("Updated role id is not correct", userRole.getId(), user.getRole().getId());
	
		verify(userEventService).create(any());
		verify(packageService).refreshMaintainer(packages.get(0), admin);
	}
	
	@Test
	public void updateRole_FromPackageMaintainerToUser() throws EventNotFound, UserEditException, 
		PackageMaintainerDeleteException, PackageEditException, PackageMaintainerNotFound {
		final int packageCount = 3;
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		Role userRole = RoleTestFixture.GET_FIXTURE_USER_ROLE();
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		User packagesOwner = UserTestFixture.GET_FIXTURE_USER();
		
		List<Package> packages = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, packagesOwner, 1);
		List<PackageMaintainer> packageMaintainers = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(user, repository, packageCount);
		
		user.setPackageMaintainers(new HashSet<>(packageMaintainers));
		
		when(packageMaintainerService.delete(anyInt(), eq(admin))).thenAnswer(new Answer<PackageMaintainer>() {

			@Override
			public PackageMaintainer answer(InvocationOnMock invocation) throws Throwable {
				int id = invocation.getArgument(0);
				return packageMaintainers.get(id);
			}}
		);
		when(packageService.findByNameAndRepository(anyString(), eq(repository))).thenReturn(packages);
		doNothing().when(packageService).refreshMaintainer(packages.get(0), admin);
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		doAnswer(new UserEventAssertionAnswer(admin, user, updateEvent, 
				"role", "" + RoleTestFixture.GET_FIXTURE_PACKAGE_MAINTAINER_ROLE().getId(),
				"" + userRole.getId()))
			.when(userEventService).create(any());
		userService.updateRole(user, admin, userRole);
		
		assertEquals("Updated role id is not correct", userRole.getId(), user.getRole().getId());
	
		verify(userEventService).create(any());
		verify(packageService, times(packageCount)).refreshMaintainer(packages.get(0), admin);
		verify(packageMaintainerService, times(packageCount)).delete(anyInt(), eq(admin));
	}
	
	@Test
	public void deactivateUser_throwsNoAdminLeftException_WhenNoOtherAdminIsActive() throws UserDeactivateException, UserAlreadyDeactivatedWarning, NoAdminLeftException {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		Role adminRole = RoleTestFixture.GET_FIXTURE_ADMIN_ROLE();
		List<User> admins = new ArrayList<User>();
		admins.add(admin);
		
		when(roleService.getAdminRole()).thenReturn(adminRole);
		when(userRepository.findByRoleAndActiveAndDeleted(adminRole, true, false))
			.thenReturn(admins);

		assertThrows(
		    admin.getLogin() + ": " + MessageCodes.ERROR_NO_ADMIN_LEFT,
		    NoAdminLeftException.class,
            () -> {
              userService.deactivateUser(admin, admin);
        });
	}
	
} 
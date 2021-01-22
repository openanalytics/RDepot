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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
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
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import eu.openanalytics.rdepot.exception.CreateFolderStructureException;
import eu.openanalytics.rdepot.exception.DeleteFileException;
import eu.openanalytics.rdepot.exception.EventNotFound;
import eu.openanalytics.rdepot.exception.LinkFoldersException;
import eu.openanalytics.rdepot.exception.PackageDeleteException;
import eu.openanalytics.rdepot.exception.PackageMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.PackageMaintainerNotFound;
import eu.openanalytics.rdepot.exception.PackageNotFound;
import eu.openanalytics.rdepot.exception.RepositoryCreateException;
import eu.openanalytics.rdepot.exception.RepositoryDeleteException;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerNotFound;
import eu.openanalytics.rdepot.exception.RepositoryNotFound;
import eu.openanalytics.rdepot.exception.RepositoryPublishException;
import eu.openanalytics.rdepot.exception.RepositoryStorageException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryEvent;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.RepositoryRepository;
import eu.openanalytics.rdepot.repository.RoleRepository;
import eu.openanalytics.rdepot.service.EventService;
import eu.openanalytics.rdepot.service.PackageMaintainerService;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.RepositoryEventService;
import eu.openanalytics.rdepot.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.storage.RepositoryStorageLocalImpl;
import eu.openanalytics.rdepot.test.fixture.EventTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.PackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryEventTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryMaintainerTestFixture;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.answer.RepositoryEventAssertionAnswer;
import eu.openanalytics.rdepot.test.unit.time.TestDateRule;
import eu.openanalytics.rdepot.time.DateProvider;
import eu.openanalytics.rdepot.warning.PackageAlreadyDeletedWarning;
import eu.openanalytics.rdepot.warning.RepositoryAlreadyUnpublishedWarning;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryServiceTest {
	
	@InjectMocks
	RepositoryService repositoryService;
	
	@Mock
	private RepositoryRepository repositoryRepository;

	@Mock
	private RoleRepository roleRepository;
	
	@Mock
	private RepositoryMaintainerService repositoryMaintainerService;
	
	@Mock
	private PackageMaintainerService packageMaintainerService;
	
	@Mock
	private PackageService packageService;
	
	@Mock
	private RepositoryEventService repositoryEventService;
	
	@Mock
	private EventService eventService;
	
	@Mock
	private MessageSource messageSource;
	
	@Mock
	private RepositoryStorageLocalImpl repositoryStorage;
	
	@Rule
	public TestDateRule testDateRule = new TestDateRule();
	
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
	
	@Test
	public void create_CreatesRepository() throws EventNotFound, RepositoryCreateException {
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		User creator = UserTestFixture.GET_FIXTURE_ADMIN();
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		when(repositoryRepository.saveAndFlush(repository)).thenReturn(repository);
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		when(repositoryEventService.create(updateEvent, creator, repository)).thenReturn(null);
		
		Repository result = repositoryService.create(repository, creator);
		
		verify(repositoryRepository).saveAndFlush(repository);
		verify(repositoryEventService).create(updateEvent, creator, repository);
		
		assertEquals("Created repository is not correct.", repository, result);
	}
	
	@Test
	public void create_ThrowsRepositoryCreateExceptionIfEventIsNotFound() 
			throws EventNotFound, RepositoryCreateException {
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		User creator = UserTestFixture.GET_FIXTURE_ADMIN();
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());

		assertThrows(
            MessageCodes.ERROR_REPOSITORY_CREATE,
            RepositoryCreateException.class,
            () -> {
              repositoryService.create(repository, creator);
        });
	}
	
	@Test
	public void delete_deletesRepository() 
			throws RepositoryDeleteException, RepositoryNotFound, EventNotFound, 
			RepositoryMaintainerDeleteException, PackageMaintainerDeleteException, 
			PackageDeleteException, PackageAlreadyDeletedWarning, PackageNotFound, 
			RepositoryStorageException, DeleteFileException, PackageMaintainerNotFound {
		final int ID = 123;
		final int PACKAGE_MAINTAINER_COUNT = 2;
		final int REPOSITORY_MAINTAINER_COUNT = 2;
		Repository deletedRepository = 
				RepositoryTestFixture.GET_FIXTURE_REPOSITORY_WITH_PACKAGES_AND_REPOSITORY_AND_PACKAGE_MAINTAINERS(
						PACKAGE_MAINTAINER_COUNT, REPOSITORY_MAINTAINER_COUNT);
		
		deletedRepository.setId(ID);
		deletedRepository.setPublished(true);
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		User deleter = UserTestFixture.GET_FIXTURE_ADMIN();
		
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		when(repositoryMaintainerService.delete(any(), eq(deleter))).thenReturn(null);
		when(packageMaintainerService.delete(anyInt(), eq(deleter))).thenReturn(null);
		doNothing().when(packageService).delete(any(), eq(deleter));
		when(repositoryEventService.create(any(), eq(deleter), eq(deletedRepository))).thenReturn(null);
		doNothing().when(repositoryStorage).deleteCurrentDirectory(deletedRepository);
		
		Repository result = repositoryService.delete(deletedRepository, deleter);
		
		verify(repositoryMaintainerService, times(REPOSITORY_MAINTAINER_COUNT)).delete(any(), eq(deleter));
		verify(packageMaintainerService, times(PACKAGE_MAINTAINER_COUNT)).delete(anyInt(),  eq(deleter));
		verify(packageService, times(PACKAGE_MAINTAINER_COUNT)).delete(any(), eq(deleter));
		verify(repositoryEventService).create(deleteEvent, deleter, deletedRepository);
		verify(repositoryEventService, times(2)).create(any(RepositoryEvent.class));
		verify(repositoryStorage).deleteCurrentDirectory(deletedRepository);
		
		assertTrue("Repository is not deleted.", result.isDeleted());
		assertFalse("Repository should not be published.", result.isPublished());
	}
	
	@Test
	public void delete_ThrowsRepositoryDeleteException_IfEventIsNotFound() 
			throws EventNotFound, RepositoryDeleteException, RepositoryNotFound {
		Repository deletedRepository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		User deleter = UserTestFixture.GET_FIXTURE_ADMIN();
		
		when(eventService.getDeleteEvent()).thenThrow(new EventNotFound());
		
		assertThrows(
            MessageCodes.ERROR_REPOSITORY_DELETE,
            RepositoryDeleteException.class,
            () -> {
              repositoryService.delete(deletedRepository, deleter);
        });
	}
	
	@Test
	public void delete_ThrowsRepositoryDeleteException_WhenRepositoryMaintainerDeleteExceptionIsThrown() 
			throws EventNotFound, RepositoryMaintainerDeleteException, 
			RepositoryDeleteException, RepositoryNotFound {
		final int ID = 123;
		final int PACKAGE_MAINTAINER_COUNT = 2;
		final int REPOSITORY_MAINTAINER_COUNT = 2;
		Repository deletedRepository = 
				RepositoryTestFixture.GET_FIXTURE_REPOSITORY_WITH_PACKAGES_AND_REPOSITORY_AND_PACKAGE_MAINTAINERS(
						PACKAGE_MAINTAINER_COUNT, REPOSITORY_MAINTAINER_COUNT);
		
		deletedRepository.setId(ID);
		deletedRepository.setPublished(true);
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		User deleter = UserTestFixture.GET_FIXTURE_ADMIN();
		User user = UserTestFixture.GET_FIXTURE_USER();
		RepositoryMaintainer maintainer = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINER(user, deletedRepository);
		RepositoryMaintainerDeleteException exception = 
				new RepositoryMaintainerDeleteException(maintainer, messageSource, new Locale("en"));
		
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		when(repositoryMaintainerService.delete(any(), eq(deleter)))
			.thenThrow(exception);
		
	    assertThrows(
	        MessageCodes.ERROR_REPOSITORY_DELETE,
	        RepositoryDeleteException.class,
	        () -> {
	          repositoryService.delete(deletedRepository, deleter);
	    });
	}
	
	@Test
	public void delete_ThrowsRepositoryDeleteException_WhenPackageMaintainerDeleteExceptionIsThrown() 
			throws EventNotFound, RepositoryMaintainerDeleteException, PackageMaintainerDeleteException, 
			RepositoryDeleteException, RepositoryNotFound, PackageMaintainerNotFound {
		final int ID = 123;
		final int PACKAGE_MAINTAINER_COUNT = 2;
		final int REPOSITORY_MAINTAINER_COUNT = 2;
		Repository deletedRepository = 
				RepositoryTestFixture.GET_FIXTURE_REPOSITORY_WITH_PACKAGES_AND_REPOSITORY_AND_PACKAGE_MAINTAINERS(
						PACKAGE_MAINTAINER_COUNT, REPOSITORY_MAINTAINER_COUNT);
		
		deletedRepository.setId(ID);
		deletedRepository.setPublished(true);
		
		PackageMaintainer packageMaintainer = deletedRepository.getPackageMaintainers().iterator().next();
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		User deleter = UserTestFixture.GET_FIXTURE_ADMIN();
		
		PackageMaintainerDeleteException exception = new PackageMaintainerDeleteException(packageMaintainer, messageSource, new Locale("en"));
		
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		when(repositoryMaintainerService.delete(any(), eq(deleter))).thenReturn(null);
		when(packageMaintainerService.delete(anyInt(), eq(deleter))).thenThrow(exception);

        assertThrows(
            MessageCodes.ERROR_REPOSITORY_DELETE,
            RepositoryDeleteException.class,
            () -> {
              repositoryService.delete(deletedRepository, deleter);
        });
	}
	
	@Test
	public void delete_ThrowsRepositoryDeleteException_WhenPackageCannotBeDeleted() 
			throws EventNotFound, RepositoryMaintainerDeleteException, PackageMaintainerDeleteException, 
			PackageDeleteException, PackageAlreadyDeletedWarning, PackageNotFound, 
			RepositoryDeleteException, RepositoryNotFound, PackageMaintainerNotFound {
		final int ID = 123;
		final int PACKAGE_MAINTAINER_COUNT = 2;
		final int REPOSITORY_MAINTAINER_COUNT = 2;
		Repository deletedRepository = 
				RepositoryTestFixture.GET_FIXTURE_REPOSITORY_WITH_PACKAGES_AND_REPOSITORY_AND_PACKAGE_MAINTAINERS(
						PACKAGE_MAINTAINER_COUNT, REPOSITORY_MAINTAINER_COUNT);
		
		deletedRepository.setId(ID);
		deletedRepository.setPublished(true);
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		User deleter = UserTestFixture.GET_FIXTURE_ADMIN();
		
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		when(repositoryMaintainerService.delete(any(), eq(deleter))).thenReturn(null);
		when(packageMaintainerService.delete(anyInt(), eq(deleter))).thenReturn(null);
		doThrow(new PackageDeleteException(messageSource, new Locale("en"), 
				deletedRepository.getPackages().iterator().next())).
			when(packageService).delete(any(), eq(deleter));
		
        assertThrows(
            MessageCodes.ERROR_REPOSITORY_DELETE,
            RepositoryDeleteException.class,
            () -> {
              repositoryService.delete(deletedRepository, deleter);
        });
	}
	
	@Test
	public void delete_ThrowsRepositoryDeleteException_WhenRepositoryCannotBeUnpublished() 
			throws EventNotFound, RepositoryMaintainerDeleteException, PackageMaintainerDeleteException, 
			PackageDeleteException, PackageAlreadyDeletedWarning, PackageNotFound, 
	RepositoryDeleteException, RepositoryNotFound, PackageMaintainerNotFound {
		final int ID = 123;
		final int PACKAGE_MAINTAINER_COUNT = 2;
		final int REPOSITORY_MAINTAINER_COUNT = 2;
		Repository deletedRepository = 
				RepositoryTestFixture.GET_FIXTURE_REPOSITORY_WITH_PACKAGES_AND_REPOSITORY_AND_PACKAGE_MAINTAINERS(
						PACKAGE_MAINTAINER_COUNT, REPOSITORY_MAINTAINER_COUNT);
		
		deletedRepository.setId(ID);
		deletedRepository.setPublished(true);
		Event deleteEvent = EventTestFixture.GET_FIXTURE_EVENT("delete");
		User deleter = UserTestFixture.GET_FIXTURE_ADMIN();
		
		when(eventService.getDeleteEvent()).thenReturn(deleteEvent);
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());
		when(repositoryMaintainerService.delete(any(), eq(deleter))).thenReturn(null);
		when(packageMaintainerService.delete(anyInt(), eq(deleter))).thenReturn(null);

        assertThrows(
            MessageCodes.ERROR_REPOSITORY_DELETE,
            RepositoryDeleteException.class,
            () -> {
              repositoryService.delete(deletedRepository, deleter);
        });
	}
	
	@Test
	public void unpublishRepository() 
			throws EventNotFound, RepositoryStorageException, 
			RepositoryEditException, RepositoryAlreadyUnpublishedWarning, DeleteFileException {
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		repository.setPublished(true);
		repository.setVersion(1);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		User updater = UserTestFixture.GET_FIXTURE_ADMIN();
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		when(repositoryEventService.create(any())).thenAnswer(
				new RepositoryEventAssertionAnswer(updater, repository, updateEvent,
						"published", "true", "false")
				)
		.thenAnswer(new RepositoryEventAssertionAnswer(
				updater, repository, updateEvent, "version", "1", "2"));
		
		doNothing().when(repositoryStorage).deleteCurrentDirectory(repository);
		
		repositoryService.unpublishRepository(repository, updater);
		
		assertFalse("Repository should not be published.", repository.isPublished());
		assertEquals("Repository version should be increased.", 2, repository.getVersion());
		
		verify(repositoryEventService, times(2)).create(any(RepositoryEvent.class));
		verify(repositoryStorage).deleteCurrentDirectory(repository);
	}
	
	@Test
	public void unpublishRepository_ThrowsRepositoryAlreadyUnpublishedWarning_IfRepositoryIsAlreadyPublished()
		throws RepositoryAlreadyUnpublishedWarning, RepositoryEditException {
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		User updater = UserTestFixture.GET_FIXTURE_ADMIN();
		repository.setPublished(false);

        assertThrows(
            MessageCodes.WARNING_REPOSITORY_ALREADY_UNPUBLISHED,
            RepositoryAlreadyUnpublishedWarning.class,
            () -> {
              repositoryService.unpublishRepository(repository, updater);
        });
	}
	
	@Test
	public void unpublishRepository_ThrowsRepositoryEditException_IfEventIsNotFound() throws EventNotFound, RepositoryEditException, RepositoryAlreadyUnpublishedWarning {
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		User updater = UserTestFixture.GET_FIXTURE_ADMIN();
		repository.setPublished(true);
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());

		assertThrows(
            MessageCodes.ERROR_REPOSITORY_EDIT,
            RepositoryEditException.class,
            () -> {
              repositoryService.unpublishRepository(repository, updater);
        });
	}
	
	@Test
	public void unpublishRepository_ThrowsRepositoryEditException_IfPackageStorageExceptionIsThrown() 
			throws EventNotFound, RepositoryEditException, RepositoryAlreadyUnpublishedWarning,
			DeleteFileException {
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		repository.setPublished(true);
		repository.setVersion(1);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		User updater = UserTestFixture.GET_FIXTURE_ADMIN();
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		when(repositoryEventService.create(any())).thenReturn(null);
		doThrow(new DeleteFileException(messageSource, new Locale("en"), "target", "cause")).when(repositoryStorage).deleteCurrentDirectory(repository);
		
		assertThrows(
            MessageCodes.ERROR_REPOSITORY_EDIT,
            RepositoryEditException.class,
            () -> {
              repositoryService.unpublishRepository(repository, updater);
        });
	}
	
	@Test
	public void shiftDelete() 
			throws RepositoryMaintainerNotFound, PackageMaintainerDeleteException, 
			PackageDeleteException, PackageNotFound, RepositoryStorageException, 
			RepositoryDeleteException, RepositoryNotFound, DeleteFileException, PackageMaintainerNotFound {
		final int PACKAGE_COUNT = 2;
		final int REPOSITORY_EVENT_COUNT = 2;
		
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY_WITH_PACKAGES_AND_REPOSITORY_AND_PACKAGE_MAINTAINERS(1, 1);
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		
		List<Package> packages = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, user, PACKAGE_COUNT);
		repository.setPackages(new HashSet<Package>(packages));
		
		List<RepositoryEvent> repositoryEvents = RepositoryEventTestFixture.GET_FIXTURE_REPOSITORY_EVENTS(user, repository, REPOSITORY_EVENT_COUNT);
		repository.setRepositoryEvents(new HashSet<RepositoryEvent>(repositoryEvents));
		
		repository.setDeleted(true);
		
		when(repositoryMaintainerService.shiftDelete(any())).thenReturn(null);
		when(packageMaintainerService.shiftDelete(any())).thenReturn(null);
		doNothing().when(packageService).shiftDelete(any());
		doNothing().when(repositoryEventService).delete(anyInt());
		doNothing().when(repositoryRepository).delete(repository);
		doNothing().when(repositoryStorage).deleteRepositoryDirectory(repository);
		
		Repository result = repositoryService.shiftDelete(repository);
		
		assertEquals("Returned repository is not correct.", repository, result);
		
		verify(repositoryMaintainerService).shiftDelete(
				repository.getRepositoryMaintainers().iterator().next());
		verify(packageMaintainerService).shiftDelete(
				repository.getPackageMaintainers().iterator().next());
		verify(packageService, times(PACKAGE_COUNT)).shiftDelete(any());
		verify(repositoryEventService, times(REPOSITORY_EVENT_COUNT)).delete(anyInt());
		verify(repositoryStorage).deleteRepositoryDirectory(repository);
		verify(repositoryRepository).delete(repository);
	}
	
	@Test
	public void findMaintainedBy_WithIncludeDeletedTrue_WhenUserIsAdmin() {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		List<Repository> repositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		
		when(repositoryRepository.findAll(any(Sort.class))).thenReturn(repositories);
		
		List<Repository> result = repositoryService.findMaintainedBy(admin, true);
		
		assertEquals("Returned repository list is not correct!", repositories, result);
		verify(repositoryRepository).findAll(any(Sort.class));
	}
	
	@Test
	public void findMaintainedBy_WithIncludeDeletedFalse_WhenUserIsAdmin() {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		List<Repository> repositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		
		when(repositoryRepository.findByDeleted(eq(false), any(Sort.class))).thenReturn(repositories);
		
		List<Repository> result = repositoryService.findMaintainedBy(admin, false);
		
		assertEquals("Returned repository list is not correct!", repositories, result);
		verify(repositoryRepository).findByDeleted(eq(false), any(Sort.class));
	}
	
	@Test
	public void findMaintainedBy_WithIncludeDeletedTrue_WhenUserIsRepositoryMaintainer() {
		User repositoryMaintainerUser = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		List<Repository> repositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		repositories.get(0).setDeleted(false);
		repositories.get(1).setDeleted(true);
		
		List<RepositoryMaintainer> repositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(repositoryMaintainerUser, repositories);
		repositoryMaintainerUser.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(repositoryMaintainers));
		
		List<Repository> result = repositoryService.findMaintainedBy(repositoryMaintainerUser, true);
		
		assertTrue("The repository should be included",
				result.contains(repositories.get(0)));
		assertTrue("The repository should be included",
				result.contains(repositories.get(1)));
	}
	
	@Test
	public void findMaintainedBy_WithIncludeDeletedFalse_WhenUserIsRepositoryMaintainer() {
		User repositoryMaintainerUser = UserTestFixture.GET_FIXTURE_USER_REPOSITORYMAINTAINER();
		List<Repository> repositories = RepositoryTestFixture.GET_FIXTURE_REPOSITORIES(2);
		repositories.get(0).setDeleted(false);
		repositories.get(1).setDeleted(true);
		
		List<RepositoryMaintainer> repositoryMaintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(repositoryMaintainerUser, repositories);
		repositoryMaintainerUser.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(repositoryMaintainers));
		
		List<Repository> result = repositoryService.findMaintainedBy(repositoryMaintainerUser, false);
		
		assertTrue("The repository should be included",
				result.contains(repositories.get(0)));
		assertFalse("The repository should not be included",
				result.contains(repositories.get(1)));
	}
	
	@Test
	public void findMaintainedBy_WhenUserIsPackageMaintainer() {
		User user = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		
		List<Repository> result = repositoryService.findMaintainedBy(user, true);
		
		assertTrue("Returned list should be empty", result.isEmpty());
	}
	
	@Test
	public void findMaintainedBy_WhenUserIsStandardUser() {
		User user = UserTestFixture.GET_FIXTURE_USER();
		
		List<Repository> result = repositoryService.findMaintainedBy(user, true);
		
		assertTrue("Returned list should be empty", result.isEmpty());
	}
	
	
	@Test
	public void deleteRepositoryMaintainers() throws RepositoryMaintainerDeleteException {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		List<User> users = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();

		List<RepositoryMaintainer> maintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(users, repository);
		maintainers.get(0).setDeleted(true);
		maintainers.get(1).setDeleted(false);
		maintainers.get(2).setDeleted(false);
		
		repository.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(maintainers));
		
		when(repositoryMaintainerService.delete(any(), eq(admin))).thenReturn(null);
		
		repositoryService.deleteRepositoryMaintainers(repository, admin);
		
		verify(repositoryMaintainerService, times(2)).delete(any(), eq(admin));
	}
	
	@Test
	public void deletePackageMaintainers() throws PackageMaintainerDeleteException, PackageMaintainerNotFound {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		List<PackageMaintainer> maintainers = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(admin, repository, 3);
		maintainers.get(0).setDeleted(true);
		maintainers.get(1).setDeleted(false);
		maintainers.get(2).setDeleted(false);
		
		repository.setPackageMaintainers(new HashSet<PackageMaintainer>(maintainers));
		
		when(packageMaintainerService.delete(anyInt(), eq(admin))).thenReturn(null);
		
		repositoryService.deletePackageMaintainers(repository, admin);
		
		verify(packageMaintainerService, times(2)).delete(anyInt(), eq(admin));
	}
	
	@Test
	public void deletePackages() 
			throws PackageDeleteException,PackageAlreadyDeletedWarning, PackageNotFound {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		List<Package> packages = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, admin, 3);
		
		packages.get(0).setDeleted(true);
		packages.get(1).setDeleted(false);
		packages.get(2).setDeleted(false);
		
		repository.setPackages(new HashSet<Package>(packages));
		
		doNothing().when(packageService).delete(any(), eq(admin));
		
		repositoryService.deletePackages(repository, admin);
		
		verify(packageService, times(2)).delete(any(), eq(admin));
	}
	
	@Test
	public void shiftDeleteRepositoryMaintainers() throws RepositoryMaintainerDeleteException, RepositoryMaintainerNotFound {
		List<User> users = UserTestFixture.GET_FIXTURE_USERS(3, 0, 0);
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();

		List<RepositoryMaintainer> maintainers = RepositoryMaintainerTestFixture.GET_FIXTURE_REPOSITORY_MAINTAINERS(users, repository);
		repository.setRepositoryMaintainers(new HashSet<RepositoryMaintainer>(maintainers));
		
		when(repositoryMaintainerService.shiftDelete(any())).thenReturn(null);
		
		repositoryService.shiftDeleteRepositoryMaintainers(repository);
		
		verify(repositoryMaintainerService, times(3)).shiftDelete(any());
	}
	
	@Test
	public void shiftDeletePackageMaintainers() throws PackageMaintainerDeleteException, PackageMaintainerNotFound {
		User user = UserTestFixture.GET_FIXTURE_USER_PACKAGEMAINTAINER();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		List<PackageMaintainer> maintainers = PackageMaintainerTestFixture.GET_FIXTURE_PACKAGE_MAINTAINERS(user, repository, 3);
		repository.setPackageMaintainers(new HashSet<PackageMaintainer>(maintainers));
		
		when(packageMaintainerService.shiftDelete(any())).thenReturn(null);
		
		repositoryService.shiftDeletePackageMaintainers(repository);
		
		verify(packageMaintainerService, times(3)).shiftDelete(any());
	}
	
	@Test
	public void shiftDeletePackages() throws PackageDeleteException, PackageNotFound {
		User admin = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		List<Package> packages = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, admin, 3);
		
		repository.setPackages(new HashSet<Package>(packages));
		
		doNothing().when(packageService).shiftDelete(any());
		
		repositoryService.shiftDeletePackages(repository);
		
		verify(packageService, times(3)).shiftDelete(any());
	}
	
	@Test
	public void updateVersion() throws EventNotFound, RepositoryEditException {
		User updater = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		final int CURRENT_VERSION = new Random().nextInt(1000);
		final int NEW_VERSION = CURRENT_VERSION + 1;
		repository.setVersion(CURRENT_VERSION);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		when(repositoryEventService.create(any())).thenAnswer(
				new RepositoryEventAssertionAnswer(updater, repository, updateEvent, "version", 
						Integer.toString(CURRENT_VERSION), Integer.toString(NEW_VERSION))
		);
		
		repositoryService.updateVersion(repository, updater, NEW_VERSION);
		
		assertEquals("Version was not updated.", NEW_VERSION, repository.getVersion());
	}
	
	@Test
	public void updateVersion_ThrowsRepositoryEditException_WhenEventIsNotFound() throws EventNotFound, RepositoryEditException {
		User updater = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());

		assertThrows(
            MessageCodes.ERROR_REPOSITORY_EDIT,
            RepositoryEditException.class,
            () -> {
              repositoryService.updateVersion(repository, updater, 123);
        });
	}
	
	@Test
	public void updatePublicationUri() throws EventNotFound, RepositoryEditException {
		User updater = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		final String CURRENT_PUBLICATIONURI = "http://localhost/repo1";
		final String NEW_PUBLICATIONURI = "http://localhost/repo123";
		repository.setPublicationUri(CURRENT_PUBLICATIONURI);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		when(repositoryEventService.create(any())).thenAnswer(
				new RepositoryEventAssertionAnswer(updater, repository, updateEvent, 
						"publication URI", CURRENT_PUBLICATIONURI, NEW_PUBLICATIONURI)
				);
		
		repositoryService.updatePublicationUri(repository, updater, NEW_PUBLICATIONURI);
		
		assertEquals("Publication URI was not updated.", NEW_PUBLICATIONURI, repository.getPublicationUri());
	}
	
	@Test
	public void updatePublicationUri_ThrowsRepositoryEditException_WhenEventIsNotFound() throws EventNotFound, RepositoryEditException {
		User updater = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());

		assertThrows(
            MessageCodes.ERROR_REPOSITORY_EDIT,
            RepositoryEditException.class,
            () -> {
              repositoryService.updatePublicationUri(repository, updater, "test");
        });
	}
	
	@Test
	public void updateServerAddress() throws EventNotFound, RepositoryEditException {
		User updater = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		final String CURRENT_SERVERADDRESS = "http://oa-rdepot-repo:8080/repo1";
		final String NEW_SERVERADDRESS = "http://oa-rdepot-repo:8080/repo123";
		repository.setServerAddress(CURRENT_SERVERADDRESS);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		when(repositoryEventService.create(any())).thenAnswer(
				new RepositoryEventAssertionAnswer(updater, repository, updateEvent,
						"server address", CURRENT_SERVERADDRESS, NEW_SERVERADDRESS));
		
		repositoryService.updateServerAddress(repository, updater, NEW_SERVERADDRESS);
		
		assertEquals("Server address was not updated.", NEW_SERVERADDRESS, repository.getServerAddress());
	}
	
	@Test
	public void updateServerAddress_ThrowsRepositoryEditException_WhenEventIsNotFound() throws EventNotFound, RepositoryEditException {
		User updater = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());

		assertThrows(
            MessageCodes.ERROR_REPOSITORY_EDIT,
            RepositoryEditException.class,
            () -> {
              repositoryService.updateServerAddress(repository, updater, "test");
        });
	}
	
	@Test
	public void updateName() throws EventNotFound, RepositoryEditException {
		User updater = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		final String CURRENT_NAME = "repo1";
		final String NEW_NAME = "repo123";
		repository.setName(CURRENT_NAME);
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		when(repositoryEventService.create(any())).thenAnswer(
				new RepositoryEventAssertionAnswer(updater, repository,
						updateEvent, "name", CURRENT_NAME, NEW_NAME));
		
		repositoryService.updateName(repository, updater, NEW_NAME);
		
		assertEquals("Name was not updated.", NEW_NAME, repository.getName());
	}
	
	@Test
	public void updateName_ThrowsRepositoryEditException_WhenEventIsNotFound() 
			throws EventNotFound, RepositoryEditException {
		User updater = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());

		assertThrows(
            MessageCodes.ERROR_REPOSITORY_EDIT,
            RepositoryEditException.class,
            () -> {
              repositoryService.updateName(repository, updater, "test");
        });
	}
	
	@Test
	public void publishRepository() 
			throws RepositoryStorageException, EventNotFound, RepositoryPublishException, 
			CreateFolderStructureException, LinkFoldersException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		repository.setPublished(false);
		repository.setVersion(1);
		
		String expectedDateStamp = new SimpleDateFormat("yyyyMMdd").format(DateProvider.now());
		
		List<Package> latestPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, user, 3);
		ArrayList<Package> archivePackages = new ArrayList<Package>();
		
		for(Package package_ : latestPackages) {
			String newerVersion = "2";
			String olderVersion = "1";
			Package olderPackage = new Package(package_);
			olderPackage.setVersion(olderVersion);
			package_.setVersion(newerVersion);
			
			archivePackages.add(olderPackage);
		}
		
		List<Package> allPackages = new ArrayList<Package>();
		allPackages.addAll(archivePackages);
		
		File testFile;
		try {
			testFile = temporaryFolder.newFile();
		} catch (IOException e) {
			fail("I/O exception on temporary folder rule!");
			return;
		}
		
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		when(packageService.findByRepositoryAndActive(repository, true)).thenReturn(allPackages);
		when(packageService.findByRepositoryAndActiveAndNewest(repository, true))
			.thenReturn(latestPackages);
		doNothing().when(repositoryStorage)
			.createFolderStructureForGeneration(repository, expectedDateStamp);
		doNothing()
			.when(repositoryStorage).populateGeneratedFolder(
					allPackages, repository, expectedDateStamp);
		when(repositoryStorage.
				linkCurrentFolderToGeneratedFolder(repository, expectedDateStamp))
			.thenReturn(testFile);
		doAnswer(new Answer<File>() {

			@Override
			public File answer(InvocationOnMock invocation) throws Throwable {
				List<Package> actualArchivePackages = invocation.getArgument(1);
				
				assertEquals("Archive packages were not sorted out correctly.", archivePackages, actualArchivePackages);
				
				return testFile;
			}}).when(repositoryStorage).copyFromRepositoryToRemoteServer(eq(latestPackages), any(), eq(testFile), eq(repository));
		
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);
		when(repositoryEventService.create(any())).thenAnswer(new Answer<RepositoryEvent>() {
			private int callbackCounter = 0;

			@Override
			public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
				callbackCounter++;
				RepositoryEvent event = invocation.getArgument(0);
				
				if(callbackCounter > 1) //we skip test for boost version event since it was tested in updateVersion() test
					return event;
				
				RepositoryEventAssertionAnswer.assertRepositoryEvents(event, user, repository, updateEvent, "published", "", "");
				return event;
			}});

		repositoryService.publishRepository(repository, user);
		
		assertEquals("Version should be boosted.", 2, repository.getVersion());
		assertTrue("Repository should be set published", repository.isPublished());
		verify(repositoryStorage).createFolderStructureForGeneration(repository, expectedDateStamp);
		verify(repositoryStorage).populateGeneratedFolder(allPackages, repository, expectedDateStamp);
		verify(repositoryStorage).copyFromRepositoryToRemoteServer(eq(latestPackages), any(), eq(testFile), eq(repository));
	}
	
	@Test
	public void publishRepository_ThrowsRepositoryPublishException_WhenEventIsNotFound() 
			throws EventNotFound, RepositoryPublishException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		repository.setPublished(false);
		repository.setVersion(1);
				
		List<Package> latestPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, user, 3);
		ArrayList<Package> archivePackages = new ArrayList<Package>();
		
		for(Package package_ : latestPackages) {
			String newerVersion = "2";
			String olderVersion = "1";
			Package olderPackage = new Package(package_);
			olderPackage.setVersion(olderVersion);
			package_.setVersion(newerVersion);
			
			archivePackages.add(olderPackage);
		}
		
		List<Package> allPackages = new ArrayList<Package>();
		allPackages.addAll(archivePackages);
		
		when(packageService.findByRepositoryAndActive(repository, true)).thenReturn(allPackages);
		when(packageService.findByRepositoryAndActiveAndNewest(repository, true)).thenReturn(latestPackages);
		
		when(eventService.getUpdateEvent()).thenThrow(new EventNotFound());

		assertThrows(
            MessageCodes.ERROR_REPOSITORY_PUBLISH,
            RepositoryPublishException.class,
            () -> {
              repositoryService.publishRepository(repository, user);
        });
	}
	
	@Test
	public void publishRepository_ThrowsRepositoryPublishException_WhenCreateFolderStructureExceptionIsThrown() 
			throws RepositoryStorageException, RepositoryPublishException, EventNotFound, CreateFolderStructureException {
		User user = UserTestFixture.GET_FIXTURE_ADMIN();
		Repository repository = RepositoryTestFixture.GET_FIXTURE_REPOSITORY();
		repository.setPublished(false);
		repository.setVersion(1);
		
		String expectedDateStamp = new SimpleDateFormat("yyyyMMdd").format(DateProvider.now());
		
		List<Package> latestPackages = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, user, 3);
		ArrayList<Package> archivePackages = new ArrayList<Package>();
		
		for(Package package_ : latestPackages) {
			String newerVersion = "2";
			String olderVersion = "1";
			Package olderPackage = new Package(package_);
			olderPackage.setVersion(olderVersion);
			package_.setVersion(newerVersion);
			
			archivePackages.add(olderPackage);
		}
		
		List<Package> allPackages = new ArrayList<Package>();
		allPackages.addAll(archivePackages);
		
		Event updateEvent = EventTestFixture.GET_FIXTURE_EVENT("update");
		
		when(packageService.findByRepositoryAndActive(repository, true)).thenReturn(allPackages);
		when(packageService.findByRepositoryAndActiveAndNewest(repository, true))
			.thenReturn(latestPackages);
		doThrow(new CreateFolderStructureException(messageSource, new Locale("en"), "some details")).when(repositoryStorage) //TODO: Refactoring of RepositoryStorageException
			.createFolderStructureForGeneration(repository, expectedDateStamp);
				
		when(eventService.getUpdateEvent()).thenReturn(updateEvent);

		assertThrows(
            MessageCodes.ERROR_REPOSITORY_PUBLISH,
            RepositoryPublishException.class,
            () -> {
              repositoryService.publishRepository(repository, user);
        });
	}
}

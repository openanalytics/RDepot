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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.context.MessageSource;

import eu.openanalytics.rdepot.exception.CreateTemporaryFolderException;
import eu.openanalytics.rdepot.exception.DeleteFileException;
import eu.openanalytics.rdepot.exception.DownloadFileException;
import eu.openanalytics.rdepot.exception.DownloadPackagesFileException;
import eu.openanalytics.rdepot.exception.SubmissionCreateException;
import eu.openanalytics.rdepot.exception.SynchronizationInProgress;
import eu.openanalytics.rdepot.exception.SynchronizeMirrorException;
import eu.openanalytics.rdepot.exception.UpdatePackageException;
import eu.openanalytics.rdepot.model.Mirror;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.SynchronizationStatus;
import eu.openanalytics.rdepot.properties.RepositoriesProps;
import eu.openanalytics.rdepot.service.CranMirrorService;
import eu.openanalytics.rdepot.service.MirrorService;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.SubmissionService;
import eu.openanalytics.rdepot.service.UploadRequestService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.storage.BaseStorage;
import eu.openanalytics.rdepot.storage.RepositoryStorage;
import eu.openanalytics.rdepot.test.fixture.RepositoryTestFixture;
import eu.openanalytics.rdepot.test.unit.answer.MirrorFileAnswer;
import eu.openanalytics.rdepot.test.unit.answer.PackageAnswer;
import eu.openanalytics.rdepot.warning.SubmissionCreateWarning;
import eu.openanalytics.rdepot.warning.SubmissionNeedsToBeAcceptedWarning;

@RunWith(MockitoJUnitRunner.class)
public class CranMirrorServiceTest {

	MirrorService mirrorService;
	
	@Mock
	PackageService packageService;
	
	@Mock
	UserService userService;
	
	@Mock
	UploadRequestService uploadRequestService;
	
	@Mock
	SubmissionService submissionService;
	
	@Mock
	RepositoriesProps repositoriesProps;
	
	@Mock
	MessageSource messageSource;
	
	@Mock
	RepositoryStorage repositoryStorage;
	
	@Mock
	BaseStorage baseStorage;
	
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	Locale locale = new Locale("en");
	
//	private void assertStatusLength(List<SynchronizationStatus> status, List<Repository> repositories) {
//		assertEquals("Status was not registered for all synchronizations!", 
//				repositories.size(), status.size());
//	}
//	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		when(messageSource.getMessage(anyString(), isNull(), anyString(), any())).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return invocation.getArgument(0);
			}
			
		});
		
		mirrorService = new CranMirrorService(packageService, userService, 
				submissionService, messageSource, repositoriesProps,baseStorage);
	}
	
	@Test
	public void synchronize_DoesNotUpdate_WhenAllPackagesAreUpToDate() 
			throws DownloadFileException, IOException, DeleteFileException  {
		List<Repository> declaredRepositories = 
				RepositoryTestFixture.GET_DECLARED_REPOSITORIES_WITH_MIRRORS();

		Map<String, Map<String, Package>> mockRepositories 
			= RepositoryTestFixture.GET_REPOSITORIES_WITH_THE_LATEST_PACKAGES();
		
		when(baseStorage.downloadFile(any())).thenAnswer(new MirrorFileAnswer(temporaryFolder.newFolder()));
		doNothing().when(baseStorage).deleteFile(any());
		when(packageService.findByNameAndRepositoryAndNewest(any(), any()))
			.thenAnswer(new PackageAnswer(mockRepositories));
		when(packageService.findByNameAndVersionAndRepository(any(), any(), any()))
			.thenAnswer(new PackageAnswer(mockRepositories));
		
		Repository repository = declaredRepositories.get(1);
		
		for(Mirror mirror : repository.getMirrors()) {
			mirrorService.synchronize(repository, mirror);
		}
		
		verify(baseStorage, times(2)).deleteFile(any());
		verifyNoInteractions(submissionService);
	}
	
	@Test
	public void synchronize_downloadAllPackages_WhenThereAreNoPackagesInRepo() 
			throws DownloadFileException, IOException, 
				DeleteFileException, CreateTemporaryFolderException, SynchronizeMirrorException, SubmissionCreateWarning, SubmissionCreateException, SubmissionNeedsToBeAcceptedWarning, SynchronizationInProgress {
		List<Repository> declaredRepositories = 
				RepositoryTestFixture.GET_DECLARED_REPOSITORIES_WITH_MIRRORS();
		Map<String, Map<String, Package>> mockRepositories = 
				RepositoryTestFixture.GET_REPOSITORIES_WITH_NO_PACKAGES();
		
		when(baseStorage.downloadFile(any())).thenAnswer(new MirrorFileAnswer(temporaryFolder.newFolder()));
		when(baseStorage.downloadFile(any(), any())).thenAnswer(new MirrorFileAnswer(temporaryFolder.newFolder()));
		doNothing().when(baseStorage).deleteFile(any());
		when(packageService.findByNameAndRepositoryAndNewest(any(), any())) 
			.thenAnswer(new PackageAnswer(mockRepositories));
		when(packageService.findByNameAndVersionAndRepository(any(), any(), any()))
			.thenAnswer(new PackageAnswer(mockRepositories));
		when(baseStorage.createTemporaryFolder(any())).thenReturn(temporaryFolder.newFolder());
		when(submissionService.createInternalSubmission(any(), any(), any(), any())).thenReturn(new Submission());
		
		Repository repository = declaredRepositories.get(1);
		
		for(Mirror mirror : repository.getMirrors()) {
			mirrorService.synchronize(repository, mirror);
		}
		
		verify(baseStorage, times(2)).downloadFile(any());
		verify(baseStorage, times(2)).downloadFile(any(), any());
		verify(baseStorage, times(4)).deleteFile(any());
		verify(submissionService, times(2)).createInternalSubmission(any(), any(), any(), any());
	}
	
	@Test
	public void synchronize_UpdatePackage_WhenNoVersionIsSpecified_AndPackageIsOutOfDate() 
			throws DownloadFileException, IOException, DeleteFileException, 
			SubmissionCreateWarning, SubmissionCreateException, 
			SubmissionNeedsToBeAcceptedWarning, CreateTemporaryFolderException, 
			SynchronizeMirrorException, SynchronizationInProgress {
		List<Repository> declaredRepositories = 
				RepositoryTestFixture.GET_DECLARED_REPOSITORIES_WITH_MIRRORS();
		Map<String, Map<String, Package>> mockRepositories = 
				RepositoryTestFixture.GET_REPOSITORIES_WITH_ONE_PACKAGE_OUT_OF_DATE();
		
		when(baseStorage.downloadFile(any())).thenAnswer(new MirrorFileAnswer(temporaryFolder.newFolder()));
		when(baseStorage.downloadFile(any(), any())).thenAnswer(new MirrorFileAnswer(temporaryFolder.newFolder()));
		doNothing().when(baseStorage).deleteFile(any());
		when(packageService.findByNameAndRepositoryAndNewest(any(), any())) 
			.thenAnswer(new PackageAnswer(mockRepositories));
		when(packageService.findByNameAndVersionAndRepository(any(), any(), any()))
			.thenAnswer(new PackageAnswer(mockRepositories));
		when(baseStorage.createTemporaryFolder(any())).thenReturn(temporaryFolder.newFolder());
		when(submissionService.createInternalSubmission(any(), any(), any(), any())).thenReturn(new Submission());

		Repository repository = declaredRepositories.get(0);
		
		for(Mirror mirror : repository.getMirrors()) {
			mirrorService.synchronize(repository, mirror);
		}
		
		verify(baseStorage).downloadFile(any());
		verify(baseStorage).downloadFile(any(), any());
		verify(baseStorage, times(2)).deleteFile(any());
		verify(submissionService).createInternalSubmission(any(), any(), any(), any());
	}
	
	@Test
	public void synchronize_WhenOutOfDateVersionIsSpecified_AndPackageIsNotPresent() throws DownloadFileException, IOException, DeleteFileException, CreateTemporaryFolderException, SubmissionCreateWarning, SubmissionCreateException, SubmissionNeedsToBeAcceptedWarning, SynchronizeMirrorException, SynchronizationInProgress {
		List<Repository> declaredRepositories = 
				RepositoryTestFixture.GET_DECLARED_REPOSITORIES_WITH_MIRRORS_ONE_PACKAGE_OUT_OF_DATE();
		Map<String, Map<String, Package>> mockRepositories 
			= RepositoryTestFixture.GET_REPOSITORIES_WITH_THE_LATEST_PACKAGES();
		
		when(baseStorage.downloadFile(any())).thenAnswer(new MirrorFileAnswer(temporaryFolder.newFolder()));
		when(baseStorage.downloadFile(any(), any())).thenAnswer(new MirrorFileAnswer(temporaryFolder.newFolder()));
		doNothing().when(baseStorage).deleteFile(any());
		when(packageService.findByNameAndRepositoryAndNewest(any(), any())) 
			.thenAnswer(new PackageAnswer(mockRepositories));
		when(packageService.findByNameAndVersionAndRepository(any(), any(), any()))
			.thenAnswer(new PackageAnswer(mockRepositories));
		when(baseStorage.createTemporaryFolder(any())).thenReturn(temporaryFolder.newFolder());
		when(submissionService.createInternalSubmission(any(), any(), any(), any())).thenReturn(new Submission());
		
		Repository repository = declaredRepositories.get(0);
		
		for(Mirror mirror : repository.getMirrors()) {
			mirrorService.synchronize(repository, mirror);
		}
		
		verify(baseStorage).downloadFile(any());
		verify(baseStorage).downloadFile(any(), any());
		verify(baseStorage, times(2)).deleteFile(any());
		verify(submissionService).createInternalSubmission(any(), any(), any(), any());
	}
	
	@Test
	public void synchronize_ThrowsException_WhenPackageFileIsNotFound() throws DownloadFileException, IOException, DeleteFileException, CreateTemporaryFolderException {
		List<Repository> declaredRepositories = 
				RepositoryTestFixture.GET_DECLARED_REPOSITORIES_WITH_MIRRORS();
		Map<String, Map<String, Package>> mockRepositories = 
				RepositoryTestFixture.GET_REPOSITORIES_WITH_NO_PACKAGES();
		
		DownloadFileException exception = new DownloadFileException(messageSource, locale, "url", "dest");
		
		when(baseStorage.downloadFile(any())).thenAnswer(new MirrorFileAnswer(temporaryFolder.newFolder()));
		when(baseStorage.downloadFile(any(), any())).thenThrow(exception);
		doNothing().when(baseStorage).deleteFile(any());
		when(packageService.findByNameAndVersionAndRepository(any(), any(), any()))
			.thenAnswer(new PackageAnswer(mockRepositories));
		when(baseStorage.createTemporaryFolder(any())).thenReturn(temporaryFolder.newFolder());
		
		Repository repository = declaredRepositories.get(0);
		
		for(Mirror mirror : repository.getMirrors()) {
			mirrorService.synchronize(repository, mirror);
		}
		
		List<SynchronizationStatus> status = mirrorService.getSynchronizationStatusList();
		
		assertEquals("Synchronization status length is incorrect.", 1, status.size());
		for(SynchronizationStatus s : status) {
			assertTrue("Error was not registered.", s.getError().isPresent());
			assertEquals("Incorrect error was registered.", 
					UpdatePackageException.class, s.getError().get().getClass());
		}
		
		verify(baseStorage, times(1)).downloadFile(any());
		verify(baseStorage, times(1)).downloadFile(any(), any());
		verify(baseStorage, times(2)).deleteFile(any());
		verifyNoInteractions(submissionService);
	}
	
	@Test
	public void synchronize_ThrowsException_WhenPackagesFileIsNotFound() throws DownloadFileException, DeleteFileException {
		List<Repository> declaredRepositories = 
				RepositoryTestFixture.GET_DECLARED_REPOSITORIES_WITH_MIRRORS();

		DownloadFileException exception = new DownloadFileException(messageSource, locale, "url", "dest");
		
		when(baseStorage.downloadFile(any())).thenThrow(exception);
		
		Repository repository = declaredRepositories.get(0);
		
		for(Mirror mirror : repository.getMirrors()) {
				mirrorService.synchronize(repository, mirror);
		}
		
		List<SynchronizationStatus> status = mirrorService.getSynchronizationStatusList();
		
		assertEquals("Synchronization status length is incorrect.", 1, status.size());
		for(SynchronizationStatus s : status) {
			assertTrue("Error was not registered.", s.getError().isPresent());
			assertEquals("Incorrect error was registered.", 
					DownloadPackagesFileException.class, s.getError().get().getClass());
		}
		
		verify(baseStorage, times(1)).downloadFile(any());
		verifyNoInteractions(submissionService);
	}
	
	@Test
	public void sychronize_ThrowsException_WhenPackageCannotBeSubmitted() throws DownloadFileException, IOException, DeleteFileException, CreateTemporaryFolderException, SubmissionCreateWarning, SubmissionCreateException, SubmissionNeedsToBeAcceptedWarning, SynchronizeMirrorException {
		List<Repository> declaredRepositories = 
				RepositoryTestFixture.GET_DECLARED_REPOSITORIES_WITH_MIRRORS();
		Map<String, Map<String, Package>> mockRepositories = 
				RepositoryTestFixture.GET_REPOSITORIES_WITH_NO_PACKAGES();
		
		SubmissionCreateException exception = new SubmissionCreateException(messageSource, locale);
		
		when(baseStorage.downloadFile(any())).thenAnswer(new MirrorFileAnswer(temporaryFolder.newFolder()));
		when(baseStorage.downloadFile(any(), any())).thenAnswer(new MirrorFileAnswer(temporaryFolder.newFolder()));
		doNothing().when(baseStorage).deleteFile(any());
		when(packageService.findByNameAndRepositoryAndNewest(any(), any())) 
			.thenAnswer(new PackageAnswer(mockRepositories));
		when(baseStorage.createTemporaryFolder(any())).thenReturn(temporaryFolder.newFolder());
		when(submissionService.createInternalSubmission(any(), any(), any(), any()))
			.thenThrow(exception);
		
		Repository repository = declaredRepositories.get(1);
		
		for(Mirror mirror : repository.getMirrors()) {
			mirrorService.synchronize(repository, mirror);
		}
		
		List<SynchronizationStatus> status = mirrorService.getSynchronizationStatusList();
		
		assertEquals("Synchronization status length is incorrect.", 1, status.size());
		for(SynchronizationStatus s : status) {
			assertTrue("Error was not registered.", s.getError().isPresent());
			assertEquals("Incorrect error was registered.", 
					UpdatePackageException.class, s.getError().get().getClass());
		}
		
		verify(baseStorage, times(2)).downloadFile(any());
		verify(baseStorage, times(2)).downloadFile(any(), any());
		verify(baseStorage, times(4)).deleteFile(any());
		verify(submissionService, times(2)).createInternalSubmission(any(), any(), any(), any());
	}
}

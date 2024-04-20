/*
 * RDepot
 *
 * Copyright (C) 2012-2024 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.test.unit.synchronization;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.synchronization.RepoResponse;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.storage.RStorage;
import eu.openanalytics.rdepot.r.storage.utils.PopulatedRepositoryContent;
import eu.openanalytics.rdepot.r.synchronization.RRepositorySynchronizer;
import eu.openanalytics.rdepot.r.synchronization.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RPackageTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.r.test.strategy.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.UnitTest;

public class RRepositorySynchronizerTest extends UnitTest {
	
	@Mock
	RStorage storage;
	
	@Mock
	RPackageService packageService;
	
	@Mock
	RestTemplate rest;
	
	@InjectMocks
	RRepositorySynchronizer repositorySynchronizer;
	
	@Test
	public void storeRepositoryOnRemoteServer_withSingleChunk() throws Exception {
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repository.setServerAddress("http://127.0.0.1/testrepo1");
		
		final User requester = UserTestFixture.GET_ADMIN();
		final User user = requester;
		final String DATESTAMP = "1560188089";
		final String latestPath = "/target/generation/folder/src/contrib/latest";
		final String archivePath = "/target/generation/folder/src/contrib/Archive";
		final String TEST_PACKAGES_PATH = "src/test/resources/unit/test_packages";
		final String versionBefore = "3";
		final String versionAfter = "4";
		doReturn(new ResponseEntity<String>("[3]", HttpStatus.OK))
			.when(rest)
			.getForEntity(eq("http://127.0.0.1/testrepo1/"), eq(String.class));
		doReturn(new ResponseEntity<String>("[]", HttpStatus.OK))
			.when(rest)
			.getForEntity(eq("http://127.0.0.1/testrepo1/archive/"), eq(String.class));
		
		
		final List<RPackage> packages = RPackageTestFixture
				.GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE(repository, user);
		final List<RPackage> onlyLatest = RPackageTestFixture
				.GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE_ONLY_LATEST(repository, user);
		final List<RPackage> onlyArchive = packages.stream().filter(p -> !onlyLatest.contains(p)).collect(Collectors.toCollection(ArrayList::new));
		
		Collections.sort(onlyArchive, RRepositorySynchronizer.PACKAGE_COMPARATOR);
		Collections.sort(onlyLatest, RRepositorySynchronizer.PACKAGE_COMPARATOR);
		Collections.sort(packages, RRepositorySynchronizer.PACKAGE_COMPARATOR);
		
		final PopulatedRepositoryContent populatedContent = new PopulatedRepositoryContent(
				onlyLatest, onlyArchive, latestPath, archivePath);
		final List<File> packagesToUpload = List.of(
				new File(TEST_PACKAGES_PATH + "/abc_1.3.tar.gz"),
				new File(TEST_PACKAGES_PATH + "/accrued_1.3.tar.gz")
		);
		final List<File> packagesToUploadToArchive = List.of(
				new File(TEST_PACKAGES_PATH + "/accrued_1.2.tar.gz")
		);
		final List<String> packagesToDelete = List.of("todelete1", "todelete2");
		final List<String> packagesToDeleteFromArchive = List.of("todelete_archive1", "todelete_archive2");
		final File packagesFile = new File(TEST_PACKAGES_PATH + "/PACKAGES");
		final File packagesGzFile = new File(TEST_PACKAGES_PATH + "/PACKAGES.gz");
		final File packagesFileArchive = new File(TEST_PACKAGES_PATH + "/PACKAGES_ARCHIVE");
		final File packagesGzFileArchive = new File(TEST_PACKAGES_PATH + "/PACKAGES_ARCHIVE.gz");
		
		final SynchronizeRepositoryRequestBody requestBody = new SynchronizeRepositoryRequestBody(
				packagesToUpload,
				packagesToUploadToArchive,
				packagesToDelete,
				packagesToDeleteFromArchive,
				versionBefore,
				versionAfter,
				packagesFile,
				packagesGzFile,
				packagesFileArchive,
				packagesGzFileArchive
		);
		
		ReflectionTestUtils.setField(repositorySynchronizer, "maxRequestSize", 10);
		doReturn(packages).when(packageService).findActiveByRepository(repository);
		doReturn(new HashSet<>(onlyLatest)).when(packageService).filterLatest(anySet());
		doReturn(populatedContent).when(storage).organizePackagesInStorage(
				DATESTAMP, 
				packages,
				onlyLatest, 
				onlyArchive, 
				repository
		);
		
		doReturn(requestBody).when(storage).buildSynchronizeRequestBody(
				populatedContent, 
				new ArrayList<>(), 
				new ArrayList<>(), 
				repository, 
				versionBefore
		);
		doAnswer(
				new UploadSingleChunkRequestAssertionAnswer(
						repository, 
						"", 
						versionBefore, 
						versionAfter, 
						packagesToDelete, 
						packagesToDeleteFromArchive, 
						packagesToUpload, 
						packagesToUploadToArchive, 
						packagesFile, packagesGzFile, 
						packagesFileArchive, 
						packagesGzFileArchive
				)
		).when(rest).postForEntity(anyString(), any(Object.class), eq(RepoResponse.class));
		doNothing().when(storage).cleanUpAfterSynchronization(populatedContent);
		
		repositorySynchronizer.storeRepositoryOnRemoteServer(repository, DATESTAMP);
		
		verify(storage).cleanUpAfterSynchronization(populatedContent);
	}
	
	@Test
	public void storeRepositoryOnRemoteServer_withMultipleChunks() throws Exception {
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repository.setServerAddress("http://127.0.0.1/testrepo1");
		
		final String DATESTAMP = "1560188089";
		final String latestPath = "/target/generation/folder/src/contrib/latest";
		final String archivePath = "/target/generation/folder/src/contrib/Archive";
		final String TEST_PACKAGES_PATH = "src/test/resources/unit/test_packages/multiple_packages_test";
		final String versionBefore = "3";
		final String versionAfter = "4";
		doReturn(new ResponseEntity<String>("[3]", HttpStatus.OK))
			.when(rest)
			.getForEntity(eq("http://127.0.0.1/testrepo1/"), eq(String.class));
		doReturn(new ResponseEntity<String>("[]", HttpStatus.OK))
			.when(rest)
			.getForEntity(eq("http://127.0.0.1/testrepo1/archive/"), eq(String.class));
		
		ReflectionTestUtils.setField(repositorySynchronizer, "maxRequestSize", 4);
		
		final List<RPackage> packages = RPackageTestFixture.GET_PACKAGES_FOR_MULTICHUNK_UPLOAD(
				repository,
				TEST_PACKAGES_PATH, 
				"1_testpackage1_1.0.0",
				"2_testpackage1_1.0.1",
				"3_testpackage1_3.0.0",
				"4_testpackage2_1.0.0",
				"5_testpackage2_1.1.0",
				"6_testpackage2_2.1.0",
				"7_testpackage2_3.2.1",
				"8_testpackage3_1.0.0",
				"9_testpackage3_1.1.1",
				"10_testpackage3_2.2.2");
		
		final List<RPackage> packagesLatest = RPackageTestFixture.GET_PACKAGES_FOR_MULTICHUNK_UPLOAD(
				repository,
				TEST_PACKAGES_PATH, 
				"3_testpackage1_3.0.0",
				"7_testpackage2_3.2.1",
				"10_testpackage3_2.2.2");
		
		final List<RPackage> packagesArchive = RPackageTestFixture.GET_PACKAGES_FOR_MULTICHUNK_UPLOAD(
				repository,
				TEST_PACKAGES_PATH, 
				"1_testpackage1_1.0.0",
				"2_testpackage1_1.0.1",
				"4_testpackage2_1.0.0",
				"5_testpackage2_1.1.0",
				"6_testpackage2_2.1.0",
				"8_testpackage3_1.0.0",
				"9_testpackage3_1.1.1");
		
		final PopulatedRepositoryContent populatedContent = 
				new PopulatedRepositoryContent(packagesLatest, 
						packagesArchive, latestPath, archivePath);
		
		final List<File> packagesToUpload = packagesLatest.stream().map(p -> new File(p.getSource())).collect(Collectors.toCollection(ArrayList::new));
		final List<File> packagesToUploadToArchive = packagesArchive.stream().map(p -> new File(p.getSource())).collect(Collectors.toCollection(ArrayList::new));
		
		doReturn(packages).when(packageService).findActiveByRepository(repository);
		doReturn(new LinkedHashSet<>(packagesLatest)).when(packageService).filterLatest(new LinkedHashSet<>(packages));
		doReturn(populatedContent).when(storage).organizePackagesInStorage(
				DATESTAMP, 
				packages, 
				packagesLatest, 
				packagesArchive, 
				repository);
		doNothing().when(storage).cleanUpAfterSynchronization(populatedContent);
		
		final File packagesFile = new File(TEST_PACKAGES_PATH + "/PACKAGES");
		final File packagesGzFile = new File(TEST_PACKAGES_PATH + "/PACKAGES.gz");
		final File packagesFileArchive = new File(TEST_PACKAGES_PATH + "/PACKAGES_ARCHIVE");
		final File packagesGzFileArchive = new File(TEST_PACKAGES_PATH + "/PACKAGES_ARCHIVE.gz");
		final List<String> packagesToDelete = List.of("todelete1", "todelete2");
		final List<String> packagesToDeleteFromArchive = List.of("todelete_archive1", "todelete_archive2");
		
		final SynchronizeRepositoryRequestBody requestBody = new SynchronizeRepositoryRequestBody(
				packagesToUpload,
				packagesToUploadToArchive,
				packagesToDelete,
				packagesToDeleteFromArchive,
				versionBefore,
				versionAfter,
				packagesFile,
				packagesGzFile,
				packagesFileArchive,
				packagesGzFileArchive
		);
		doReturn(requestBody).when(storage).buildSynchronizeRequestBody(
				populatedContent,
				new ArrayList<>(),
				new ArrayList<>(),
				repository,
				versionBefore
		);
		
		List<File> expectedFirstChunkLatest = new ArrayList<>(packagesToUpload);
		expectedFirstChunkLatest.add(packagesFile);
		expectedFirstChunkLatest.add(packagesGzFile);
		List<List<File>> chunksToUploadLatest = List.of(expectedFirstChunkLatest);
		
		List<File> expectedFirstChunkArchive = new ArrayList<>(packagesToUploadToArchive.subList(0, 4));
		expectedFirstChunkArchive.add(packagesFileArchive);
		expectedFirstChunkArchive.add(packagesGzFileArchive);
		List<List<File>> chunksToUploadArchive = List.of(expectedFirstChunkArchive, packagesToUploadToArchive.subList(4, 7));
		
		doAnswer(new UploadMultipleChunksRequestAssertionAnswer(
				2, 
				repository, 
				versionBefore, 
				versionAfter, 
				packagesToDelete, 
				packagesToDeleteFromArchive, 
				chunksToUploadLatest, 
				chunksToUploadArchive
		)).when(rest).postForEntity(anyString(), any(Object.class), eq(RepoResponse.class));
		
		repositorySynchronizer.storeRepositoryOnRemoteServer(repository, DATESTAMP);
		
		verify(storage).cleanUpAfterSynchronization(populatedContent);
	}
	
	@Test
	public void storeRepositoryOnRemoteServer_shouldFail_whenRemoteSeverDoesNotReturn200() 
			throws Exception {
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repository.setServerAddress("http://127.0.0.1/testrepo1");
		
		final User requester = UserTestFixture.GET_ADMIN();
		final User user = requester;
		final String DATESTAMP = "1560188089";
		final String latestPath = "/target/generation/folder/src/contrib/latest";
		final String archivePath = "/target/generation/folder/src/contrib/Archive";
		final String TEST_PACKAGES_PATH = "src/test/resources/unit/test_packages";
		final String versionBefore = "3";
		final String versionAfter = "4";
		doReturn(new ResponseEntity<String>("[3]", HttpStatus.OK))
			.when(rest)
			.getForEntity(eq("http://127.0.0.1/testrepo1/"), eq(String.class));
		doReturn(new ResponseEntity<String>("[]", HttpStatus.OK))
			.when(rest)
			.getForEntity(eq("http://127.0.0.1/testrepo1/archive/"), eq(String.class));
		
		
		final List<RPackage> packages = RPackageTestFixture
				.GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE(repository, user);
		final List<RPackage> onlyLatest = RPackageTestFixture
				.GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE_ONLY_LATEST(repository, user);
		final List<RPackage> onlyArchive = packages.stream().filter(p -> !onlyLatest.contains(p)).collect(Collectors.toCollection(ArrayList::new));
		
		Collections.sort(onlyArchive, RRepositorySynchronizer.PACKAGE_COMPARATOR);
		Collections.sort(onlyLatest, RRepositorySynchronizer.PACKAGE_COMPARATOR);
		Collections.sort(packages, RRepositorySynchronizer.PACKAGE_COMPARATOR);
		
		final PopulatedRepositoryContent populatedContent = new PopulatedRepositoryContent(
				onlyLatest, onlyArchive, latestPath, archivePath);
		final List<File> packagesToUpload = List.of(
				new File(TEST_PACKAGES_PATH + "/abc_1.3.tar.gz"),
				new File(TEST_PACKAGES_PATH + "/accrued_1.3.tar.gz")
		);
		final List<File> packagesToUploadToArchive = List.of(
				new File(TEST_PACKAGES_PATH + "/accrued_1.2.tar.gz")
		);
		final List<String> packagesToDelete = List.of("todelete1", "todelete2");
		final List<String> packagesToDeleteFromArchive = List.of("todelete_archive1", "todelete_archive2");
		final File packagesFile = new File(TEST_PACKAGES_PATH + "/PACKAGES");
		final File packagesGzFile = new File(TEST_PACKAGES_PATH + "/PACKAGES.gz");
		final File packagesFileArchive = new File(TEST_PACKAGES_PATH + "/PACKAGES_ARCHIVE");
		final File packagesGzFileArchive = new File(TEST_PACKAGES_PATH + "/PACKAGES_ARCHIVE.gz");
		
		final SynchronizeRepositoryRequestBody requestBody = new SynchronizeRepositoryRequestBody(
				packagesToUpload,
				packagesToUploadToArchive,
				packagesToDelete,
				packagesToDeleteFromArchive,
				versionBefore,
				versionAfter,
				packagesFile,
				packagesGzFile,
				packagesFileArchive,
				packagesGzFileArchive
		);
		
		ReflectionTestUtils.setField(repositorySynchronizer, "maxRequestSize", 10);
		doReturn(packages).when(packageService).findActiveByRepository(repository);
		doReturn(new HashSet<>(onlyLatest)).when(packageService).filterLatest(anySet());
		doReturn(populatedContent).when(storage).organizePackagesInStorage(
				DATESTAMP, 
				packages,
				onlyLatest, 
				onlyArchive, 
				repository
		);
		
		doReturn(requestBody).when(storage).buildSynchronizeRequestBody(
				populatedContent, 
				new ArrayList<>(), 
				new ArrayList<>(), 
				repository, 
				versionBefore
		);
		doReturn(
				ResponseEntity.internalServerError().build()
		).when(rest).postForEntity(anyString(), any(Object.class), eq(RepoResponse.class));
		
		assertThrows(SynchronizeRepositoryException.class, 
				() -> repositorySynchronizer.storeRepositoryOnRemoteServer(repository, DATESTAMP));
	}
	
	@Test
	public void storeRepositoryOnRemoteServer_whenRemoteServerIsInSubdirectory() throws Exception {
		final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repository.setServerAddress("http://127.0.0.1/repo-server/testrepo1");
		
		final User requester = UserTestFixture.GET_ADMIN();
		final User user = requester;
		final String DATESTAMP = "1560188089";
		final String latestPath = "/target/generation/folder/src/contrib/latest";
		final String archivePath = "/target/generation/folder/src/contrib/Archive";
		final String TEST_PACKAGES_PATH = "src/test/resources/unit/test_packages";
		final String versionBefore = "3";
		final String versionAfter = "4";
		doReturn(new ResponseEntity<String>("[3]", HttpStatus.OK))
			.when(rest)
			.getForEntity(eq("http://127.0.0.1/repo-server/testrepo1/"), eq(String.class));
		doReturn(new ResponseEntity<String>("[]", HttpStatus.OK))
			.when(rest)
			.getForEntity(eq("http://127.0.0.1/repo-server/testrepo1/archive/"), eq(String.class));
		
		
		final List<RPackage> packages = RPackageTestFixture
				.GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE(repository, user);
		final List<RPackage> onlyLatest = RPackageTestFixture
				.GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE_ONLY_LATEST(repository, user);
		final List<RPackage> onlyArchive = packages.stream().filter(p -> !onlyLatest.contains(p)).collect(Collectors.toCollection(ArrayList::new));
		
		Collections.sort(onlyArchive, RRepositorySynchronizer.PACKAGE_COMPARATOR);
		Collections.sort(onlyLatest, RRepositorySynchronizer.PACKAGE_COMPARATOR);
		Collections.sort(packages, RRepositorySynchronizer.PACKAGE_COMPARATOR);
		
		final PopulatedRepositoryContent populatedContent = new PopulatedRepositoryContent(
				onlyLatest, onlyArchive, latestPath, archivePath);
		final List<File> packagesToUpload = List.of(
				new File(TEST_PACKAGES_PATH + "/abc_1.3.tar.gz"),
				new File(TEST_PACKAGES_PATH + "/accrued_1.3.tar.gz")
		);
		final List<File> packagesToUploadToArchive = List.of(
				new File(TEST_PACKAGES_PATH + "/accrued_1.2.tar.gz")
		);
		final List<String> packagesToDelete = List.of("todelete1", "todelete2");
		final List<String> packagesToDeleteFromArchive = List.of("todelete_archive1", "todelete_archive2");
		final File packagesFile = new File(TEST_PACKAGES_PATH + "/PACKAGES");
		final File packagesGzFile = new File(TEST_PACKAGES_PATH + "/PACKAGES.gz");
		final File packagesFileArchive = new File(TEST_PACKAGES_PATH + "/PACKAGES_ARCHIVE");
		final File packagesGzFileArchive = new File(TEST_PACKAGES_PATH + "/PACKAGES_ARCHIVE.gz");
		
		final SynchronizeRepositoryRequestBody requestBody = new SynchronizeRepositoryRequestBody(
				packagesToUpload,
				packagesToUploadToArchive,
				packagesToDelete,
				packagesToDeleteFromArchive,
				versionBefore,
				versionAfter,
				packagesFile,
				packagesGzFile,
				packagesFileArchive,
				packagesGzFileArchive
		);
		
		ReflectionTestUtils.setField(repositorySynchronizer, "maxRequestSize", 10);
		doReturn(packages).when(packageService).findActiveByRepository(repository);
		doReturn(new HashSet<>(onlyLatest)).when(packageService).filterLatest(anySet());
		doReturn(populatedContent).when(storage).organizePackagesInStorage(
				DATESTAMP, 
				packages,
				onlyLatest, 
				onlyArchive, 
				repository
		);
		
		doReturn(requestBody).when(storage).buildSynchronizeRequestBody(
				populatedContent, 
				new ArrayList<>(), 
				new ArrayList<>(), 
				repository, 
				versionBefore
		);
		doAnswer(
				new UploadSingleChunkRequestAssertionAnswer(
						repository, 
						"", 
						versionBefore, 
						versionAfter, 
						packagesToDelete, 
						packagesToDeleteFromArchive, 
						packagesToUpload, 
						packagesToUploadToArchive, 
						packagesFile, packagesGzFile, 
						packagesFileArchive, 
						packagesGzFileArchive
				)
		).when(rest).postForEntity(anyString(), any(Object.class), eq(RepoResponse.class));
		doNothing().when(storage).cleanUpAfterSynchronization(populatedContent);
		
		repositorySynchronizer.storeRepositoryOnRemoteServer(repository, DATESTAMP);
		
		verify(storage).cleanUpAfterSynchronization(populatedContent);
	}
}

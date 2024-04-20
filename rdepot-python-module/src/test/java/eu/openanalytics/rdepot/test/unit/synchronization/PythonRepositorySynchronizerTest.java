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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.synchronization.RepoResponse;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import eu.openanalytics.rdepot.python.storage.PythonStorage;
import eu.openanalytics.rdepot.python.storage.utils.PopulatedRepositoryContent;
import eu.openanalytics.rdepot.python.synchronization.PythonRepositorySynchronizer;
import eu.openanalytics.rdepot.python.synchronization.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.python.test.strategy.fixture.PythonPackageTestFixture;
import eu.openanalytics.rdepot.python.test.strategy.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.fixture.PythonRepositoryTestFixture;
import eu.openanalytics.rdepot.test.unit.UnitTest;

public class PythonRepositorySynchronizerTest extends UnitTest {

	@Mock
	PythonStorage storage;
	
	@Mock
	PythonPackageService packageService;
	
	@Mock
	RestTemplate rest;
	
	@InjectMocks
	PythonRepositorySynchronizer repositorySynchronizer;
	
	@Test
	public void storeRepositoryOnRemoteServer_withSingleChunk() throws Exception{
		final PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repository.setServerAddress("http://127.0.0.1/testrepo1");
		
		final User requester = UserTestFixture.GET_ADMIN();
		final User user = requester;
		final String DATESTAMP = "47619020";
		final String path = "/target/generation/folder/src";
		final String TEST_PACKAGES_PATH = "src/test/resources/unit/test_packages/testrepo1";
		final String EXPECTED_FILES_PATH = "src/test/resources/unit/test_packages/0";
		
		final String versionBefore = "1";
		final String versionAfter = "2";
		doReturn(new ResponseEntity<String>("[1]", HttpStatus.OK))
			.when(rest)
			.getForEntity(eq("http://127.0.0.1/python/testrepo1/"), eq(String.class));
		
		final List<PythonPackage> packages = PythonPackageTestFixture
				.GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE(repository, user);
		final List<PythonPackage> onlyLatest = PythonPackageTestFixture
				.GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE_ONLY_LATEST(repository, user);
		
		Collections.sort(packages, PythonRepositorySynchronizer.PACKAGE_COMPARATOR);
		Collections.sort(onlyLatest, PythonRepositorySynchronizer.PACKAGE_COMPARATOR);
		
		final PopulatedRepositoryContent populatedContent = new PopulatedRepositoryContent(packages, path);
		final List<File> filesToUpload = List.of(
				new File(TEST_PACKAGES_PATH + "/boto3/boto3-1.26.156.tar.gz"),
				new File(TEST_PACKAGES_PATH + "/boto3/index.html"),
				new File(TEST_PACKAGES_PATH + "/wheel/wheel-0.36.1.tar.gz"),
				new File(TEST_PACKAGES_PATH + "/wheel/index.html"),
				new File(TEST_PACKAGES_PATH + "/index.html")
				);
		
		final List<File> expectedFilesToUpload = List.of(
				new File(EXPECTED_FILES_PATH + "/boto3.tar.gz"),
				new File(EXPECTED_FILES_PATH + "/wheel.tar.gz"),
				new File(EXPECTED_FILES_PATH + "/index.tar.gz")
				);
		
		final List<String> packagesToDelete = List.of("todelete1", "todelete2");

		final SynchronizeRepositoryRequestBody requestBody =
				new SynchronizeRepositoryRequestBody(filesToUpload, packagesToDelete, versionBefore, repository.getName());
		
		ReflectionTestUtils.setField(repositorySynchronizer, "maxRequestSize", 10);
		doReturn(packages).when(packageService).findActiveByRepository(repository);
		doReturn(populatedContent).when(storage)
			.organizePackagesInStorage(DATESTAMP, packages, repository);
		doReturn(requestBody).when(storage)
			.buildSynchronizeRequestBody(populatedContent, new ArrayList<>(), repository, versionBefore);
		doAnswer(
				new UploadSingleChunkRequestAssertionAnswer(
						repository, 
						"", 
						versionBefore, 
						versionAfter, 
						packagesToDelete, 			
						expectedFilesToUpload
						)
				).when(rest).postForEntity(anyString(), any(Object.class), eq(RepoResponse.class));
		doNothing().when(storage).cleanUpAfterSynchronization(populatedContent);
		
		repositorySynchronizer.storeRepositoryOnRemoteServer(repository, DATESTAMP);
		
		verify(storage).cleanUpAfterSynchronization(populatedContent);
	}
	
	@Test
	public void storeRepositoryOnRemoteServer_withMultipleChunks() throws Exception {
		final PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
		repository.setServerAddress("http://127.0.0.1/testrepo2");
		
		final String DATESTAMP = "47619020";
		final String path = "/target/generation/folder/src";
		final String TEST_PACKAGES_PATH = "src/test/resources/unit/test_packages/multiple_packages_test";
		final String EXPECTED_FILES_PATH = "src/test/resources/unit/test_packages";
		
		final String versionBefore = "1";
		final String versionAfter = "2";
		doReturn(new ResponseEntity<String>("[1]", HttpStatus.OK))
			.when(rest)
			.getForEntity(eq("http://127.0.0.1/python/testrepo2/"), eq(String.class));
		
		ReflectionTestUtils.setField(repositorySynchronizer, "maxRequestSize", 10);
	
		final List<PythonPackage> packages = PythonPackageTestFixture.GET_PACKAGES_FOR_MULTICHUNK_UPLOAD(
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
		
		final PopulatedRepositoryContent populatedContent =
				new PopulatedRepositoryContent(packages, path);
		
		final List<File> filesToUpload =  List.of(
				new File(TEST_PACKAGES_PATH + "/testpackage1/testpackage1_1.0.0.tar.gz"),
				new File(TEST_PACKAGES_PATH + "/testpackage1/testpackage1_1.0.1.tar.gz"),
				new File(TEST_PACKAGES_PATH + "/testpackage1/testpackage1_3.0.0.tar.gz"),
				new File(TEST_PACKAGES_PATH + "/testpackage1/index.html"),
				new File(TEST_PACKAGES_PATH + "/testpackage2/testpackage2_1.0.0.tar.gz"),
				new File(TEST_PACKAGES_PATH + "/testpackage2/testpackage2_1.1.0.tar.gz"),
				new File(TEST_PACKAGES_PATH + "/testpackage2/testpackage2_2.1.0.tar.gz"),
				new File(TEST_PACKAGES_PATH + "/testpackage2/testpackage2_3.2.1.tar.gz"),
				new File(TEST_PACKAGES_PATH + "/testpackage2/index.html"),
				new File(TEST_PACKAGES_PATH + "/testpackage3/testpackage3_1.0.0.tar.gz"),
				new File(TEST_PACKAGES_PATH + "/testpackage3/testpackage3_1.1.1.tar.gz"),
				new File(TEST_PACKAGES_PATH + "/testpackage3/testpackage3_2.2.2.tar.gz"),
				new File(TEST_PACKAGES_PATH + "/testpackage3/testpackage3_2.3.3.tar.gz"),
				new File(TEST_PACKAGES_PATH + "/testpackage3/index.html"),
				new File(TEST_PACKAGES_PATH + "/index.html")
				);
		
		final List<File> expectedFirstFilesToUpload = List.of(
				new File(EXPECTED_FILES_PATH + "/0/testpackage1.tar.gz"),
				new File(EXPECTED_FILES_PATH + "/0/testpackage2.tar.gz"),
				new File(EXPECTED_FILES_PATH + "/0/testpackage3.tar.gz")
				);
		
		final List<File> expectedNextFilesToUpload = List.of(
				new File(EXPECTED_FILES_PATH + "/1/testpackage3.tar.gz"),
				new File(EXPECTED_FILES_PATH + "/1/index.tar.gz")
				);
		
		doReturn(packages).when(packageService).findActiveByRepository(repository);
		doReturn(populatedContent).when(storage)
			.organizePackagesInStorage(DATESTAMP, packages, repository);
		doNothing().when(storage).cleanUpAfterSynchronization(populatedContent);
		
		final List<String> packagesToDelete = List.of("todelete1", "todelete2");
		
		final SynchronizeRepositoryRequestBody requestBody = 
				new SynchronizeRepositoryRequestBody(filesToUpload, packagesToDelete, versionBefore, repository.getName());
		
		doReturn(requestBody).when(storage)
			.buildSynchronizeRequestBody(populatedContent, new ArrayList<>(), repository, versionBefore);
		
		
		List<File> expectedFirstChunk = new ArrayList<>(expectedFirstFilesToUpload);
		List<File> expectedSeondChunk = new ArrayList<>(expectedNextFilesToUpload);
		List<List<File>> chunksToUpload = List.of(expectedFirstChunk, expectedSeondChunk);
		
		doAnswer(new UploadMultipleChunksRequestAssertionAnswer(
				2, 
				repository, 
				versionBefore, 
				versionAfter, 
				packagesToDelete, 
				chunksToUpload))
			.when(rest).postForEntity(anyString(), any(Object.class), eq(RepoResponse.class));
		
		repositorySynchronizer.storeRepositoryOnRemoteServer(repository, DATESTAMP);
		
		verify(storage).cleanUpAfterSynchronization(populatedContent);		
	
	}
	
	
}

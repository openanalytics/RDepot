/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.synchronization.RepoResponse;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.storage.RStorage;
import eu.openanalytics.rdepot.r.storage.utils.PopulatedRepositoryContent;
import eu.openanalytics.rdepot.r.synchronization.RRepositorySynchronizer;
import eu.openanalytics.rdepot.r.synchronization.RRequestBodyPartitioner;
import eu.openanalytics.rdepot.r.synchronization.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.test.fixture.RPackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.UnitTest;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class RRepositorySynchronizerTest extends UnitTest {

    private static final String DATESTAMP = "1560188089";
    private static final String SOURCE_PATH = "/target/generation/folder/src/contrib";
    private static final String SOURCE_LATEST_PATH = SOURCE_PATH + "/latest";
    private static final String SOURCE_ARCHIVE_PATH = SOURCE_PATH + "/Archive";
    private static final String TEST_PACKAGES_FOR_SOURCES_PATH = "src/test/resources/unit/test_packages";
    private static final String BINARY_PATH = "/target/generation/folder/bin/linux/centos7/x86_64/4.2";
    private static final String BINARY_CENTOS8_4_2_PATH = "/target/generation/folder/bin/linux/centos8/x86_64/4.2";
    private static final String BINARY_HIGHER_VERSION_PATH = "/target/generation/folder/bin/linux/centos7/x86_64/4.5";
    private static final String TEST_PACKAGES_FOR_BINARIES_PATH =
            "src/test/resources/unit/test_packages/binary_package";
    private static final String TEST_PACKAGES_FOR_BINARIES_ARCHIVE_PATH =
            TEST_PACKAGES_FOR_BINARIES_PATH + "/archive_4_2";
    private static final String TEST_PACKAGES_FOR_BINARIES_LATEST_PATH =
            TEST_PACKAGES_FOR_BINARIES_PATH + "/latest_4_5";
    private static final String TEST_PACKAGES_FOR_BINARIES_CENTOS8_PATH =
            TEST_PACKAGES_FOR_BINARIES_PATH + "/centos8_4_2";
    private static final String VERSION_BEFORE = "3";
    private static final String VERSION_AFTER = "4";

    @Mock
    RStorage storage;

    @Mock
    RPackageService packageService;

    @Mock
    RestTemplate rest;

    @Spy
    RRequestBodyPartitioner rRequestBodyPartitioner = new RRequestBodyPartitioner();

    @InjectMocks
    RRepositorySynchronizer repositorySynchronizer;

    @Test
    public void storeRepositoryOnRemoteServer_withSingleChunk() throws Exception {
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setServerAddress("http://127.0.0.1/testrepo1");

        final User user = UserTestFixture.GET_ADMIN();

        doReturn(new ResponseEntity<>("[3]", HttpStatus.OK))
                .when(rest)
                .getForEntity(eq("http://127.0.0.1/r/testrepo1/"), eq(String.class));
        doReturn(new ResponseEntity<>("[]", HttpStatus.OK))
                .when(rest)
                .getForEntity(eq("http://127.0.0.1/r/testrepo1/archive/"), eq(String.class));

        final List<RPackage> packages =
                RPackageTestFixture.GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE(repository, user);
        final List<RPackage> onlyLatest =
                RPackageTestFixture.GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE_ONLY_LATEST(
                        repository, user);
        final List<RPackage> onlyArchive =
                packages.stream().filter(p -> !onlyLatest.contains(p)).collect(Collectors.toCollection(ArrayList::new));

        final List<RPackage> binaries =
                RPackageTestFixture.GET_BINARY_PACKAGES_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE(repository, user);
        final List<RPackage> latestBinaries =
                RPackageTestFixture.GET_BINARY_PACKAGES_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE_ONLY_LATEST(
                        repository, user);
        final List<RPackage> archiveBinaries =
                binaries.stream().filter(p -> !latestBinaries.contains(p)).collect(Collectors.toList());

        onlyArchive.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        onlyLatest.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        packages.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        archiveBinaries.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        latestBinaries.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        binaries.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);

        final MultiValueMap<String, RPackage> binLatestPaths = new LinkedMultiValueMap<>();
        binLatestPaths.addAll(BINARY_PATH, latestBinaries);
        final MultiValueMap<String, RPackage> binArchivePaths = new LinkedMultiValueMap<>();
        binArchivePaths.addAll(BINARY_PATH, archiveBinaries);

        final PopulatedRepositoryContent populatedContent = new PopulatedRepositoryContent(
                onlyLatest, onlyArchive,
                SOURCE_LATEST_PATH, SOURCE_ARCHIVE_PATH,
                binLatestPaths, binArchivePaths);

        final List<File> sourcePackagesToUpload = List.of(
                new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/abc_1.3.tar.gz"),
                new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/accrued_1.3.tar.gz"));
        final List<File> sourcePackagesToUploadToArchive =
                List.of(new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/accrued_1.2.tar.gz"));
        final List<String> sourcePackagesToDelete = List.of("todelete1", "todelete2");
        final List<String> sourcePackagesToDeleteFromArchive = List.of("todelete_archive1", "todelete_archive2");
        final File packagesFileForSources = new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/PACKAGES");
        final File packagesGzFileForSources = new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/PACKAGES.gz");
        final File packagesFileArchiveForSources = new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/PACKAGES_ARCHIVE");
        final File packagesGzFileArchiveForSources = new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/PACKAGES_ARCHIVE.gz");

        final MultiValueMap<String, File> binaryPackagesToUpload = new LinkedMultiValueMap<>();
        final MultiValueMap<String, File> binaryPackagesToUploadToArchive = new LinkedMultiValueMap<>();
        final MultiValueMap<String, String> binaryPackagesToDelete = new LinkedMultiValueMap<>();
        final MultiValueMap<String, String> binaryPackagesToDeleteFromArchive = new LinkedMultiValueMap<>();

        binaryPackagesToUpload.add(BINARY_PATH, new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/arrow_8.0.0.tar.gz"));
        binaryPackagesToDelete.add(BINARY_PATH, "binary_todelete1");
        final File packagesFileForBinaries = new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/PACKAGES");
        final File packagesGzFileForBinaries = new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/PACKAGES.gz");

        final Map<String, File> packagesFiles = new HashMap<>();
        final Map<String, File> packagesGzFiles = new HashMap<>();
        final Map<String, File> packagesFilesForArchive = new HashMap<>();
        final Map<String, File> packagesGzFilesForArchive = new HashMap<>();

        packagesFiles.put(SOURCE_PATH, packagesFileForSources);
        packagesFiles.put(BINARY_PATH, packagesFileForBinaries);
        packagesGzFiles.put(SOURCE_PATH, packagesGzFileForSources);
        packagesGzFiles.put(BINARY_PATH, packagesGzFileForBinaries);
        packagesFilesForArchive.put(SOURCE_PATH, packagesFileArchiveForSources);
        packagesGzFilesForArchive.put(SOURCE_PATH, packagesGzFileArchiveForSources);

        final Map<String, Map<String, String>> checksums = new HashMap<>();
        checksums.put(SOURCE_PATH, new HashMap<>() {

            @Serial
            private static final long serialVersionUID = -4974134751070035728L;

            {
                put("abc_1.3.tar.gz", "c47d18b86b331a5023dcd62b74fedbb6");
            }
        });
        checksums.get(SOURCE_PATH).put("accrued_1.2.tar.gz", "70d295115295a4718593f6a39d77add9");
        checksums.get(SOURCE_PATH).put("accrued_1.3.tar.gz", "a05e4ca44438c0d9e7d713d7e3890423");
        checksums.get(SOURCE_PATH).put("recent/PACKAGES", "639bf3e988764b375fa2fbdbf0ed0b74");
        checksums.get(SOURCE_PATH).put("archive/PACKAGES_ARCHIVE", "65e7f749cf8315c5c8255da701c5a844");
        checksums.get(SOURCE_PATH).put("archive/PACKAGES_ARCHIVE.gz", "df767a18774c0af24bb6b92f77b034e5");
        checksums.get(SOURCE_PATH).put("recent/PACKAGES.gz", "6a075ae72dcc3cc37a75f1df68816a14");
        checksums.put(BINARY_PATH, new HashMap<>() {

            @Serial
            private static final long serialVersionUID = -7946290225431849880L;

            {
                put("arrow_8.0.0.tar.gz", "b55eb6a2f5adeff68f1ef15fd35b03de");
            }
        });
        checksums.get(BINARY_PATH).put("recent/PACKAGES", "19d375fc238db3fcde9b2fbb53f4db79");
        checksums.get(BINARY_PATH).put("recent/PACKAGES.gz", "ef5a203d7f71fe78ab8ea3ba221cca16");

        final SynchronizeRepositoryRequestBody requestBody = new SynchronizeRepositoryRequestBody(
                sourcePackagesToUpload,
                sourcePackagesToUploadToArchive,
                sourcePackagesToDelete,
                sourcePackagesToDeleteFromArchive,
                SOURCE_PATH,
                VERSION_BEFORE,
                VERSION_AFTER,
                binaryPackagesToUpload,
                binaryPackagesToUploadToArchive,
                binaryPackagesToDelete,
                binaryPackagesToDeleteFromArchive,
                packagesFiles,
                packagesGzFiles,
                packagesFilesForArchive,
                packagesGzFilesForArchive,
                checksums);

        ReflectionTestUtils.setField(repositorySynchronizer, "maxRequestSize", 10);
        doReturn(packages).when(packageService).findSourcePackagesByRepository(repository);
        doReturn(binaries).when(packageService).findBinaryPackagesByRepository(repository);
        doReturn(new HashSet<>(onlyLatest)).when(packageService).filterLatest(new HashSet<>(packages));
        doReturn(new HashSet<>(latestBinaries)).when(packageService).filterLatest(new HashSet<>(binaries));
        doReturn(populatedContent)
                .when(storage)
                .organizePackagesInStorage(
                        DATESTAMP,
                        packages,
                        onlyLatest,
                        onlyArchive,
                        binaries,
                        latestBinaries,
                        archiveBinaries,
                        repository);

        doReturn(requestBody)
                .when(storage)
                .buildSynchronizeRequestBody(
                        populatedContent,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new LinkedMultiValueMap<>(),
                        new LinkedMultiValueMap<>(),
                        repository,
                        VERSION_BEFORE);

        // prepare files to assertion - add prefixes
        Map<String, String> pathsToUpload = new LinkedHashMap<>();
        List<File> packagesToUploadWithPrefixes = new ArrayList<>();

        prepareTestFilesToChunkRequestAssertion(
                sourcePackagesToUpload, packagesToUploadWithPrefixes, pathsToUpload, SOURCE_PATH);

        binaryPackagesToUpload.forEach((path, packagesList) -> prepareTestFilesToChunkRequestAssertion(
                packagesList, packagesToUploadWithPrefixes, pathsToUpload, path));

        prepareTestPackagesFilesToChunkRequestAssertion(
                packagesFiles, packagesToUploadWithPrefixes, pathsToUpload, false);
        prepareTestPackagesFilesToChunkRequestAssertion(
                packagesGzFiles, packagesToUploadWithPrefixes, pathsToUpload, false);

        Map<String, String> pathsToUploadToArchive = new LinkedHashMap<>();
        List<File> packagesToUploadToArchiveWithPrefixes = new ArrayList<>();

        prepareTestFilesToChunkRequestAssertion(
                sourcePackagesToUploadToArchive,
                packagesToUploadToArchiveWithPrefixes,
                pathsToUploadToArchive,
                SOURCE_PATH);

        prepareTestPackagesFilesToChunkRequestAssertion(
                packagesFilesForArchive, packagesToUploadToArchiveWithPrefixes, pathsToUploadToArchive, true);
        prepareTestPackagesFilesToChunkRequestAssertion(
                packagesGzFilesForArchive, packagesToUploadToArchiveWithPrefixes, pathsToUploadToArchive, true);

        List<String> filesToDelete = new ArrayList<>();
        List<String> filesToDeleteFromArchive = new ArrayList<>();
        Map<String, String> pathsToDelete = new LinkedHashMap<>();
        Map<String, String> pathsToDeleteFromArchive = new LinkedHashMap<>();

        prepareTestFilesToDeleteToChunkRequestAssertion(
                sourcePackagesToDelete, filesToDelete, pathsToDelete, SOURCE_PATH);
        prepareTestFilesToDeleteToChunkRequestAssertion(
                sourcePackagesToDeleteFromArchive, filesToDeleteFromArchive, pathsToDeleteFromArchive, SOURCE_PATH);

        binaryPackagesToDelete.forEach((path, packageList) ->
                prepareTestFilesToDeleteToChunkRequestAssertion(packageList, filesToDelete, pathsToDelete, path));
        binaryPackagesToDeleteFromArchive.forEach(
                (path, packageList) -> prepareTestFilesToDeleteToChunkRequestAssertion(
                        packageList, filesToDeleteFromArchive, pathsToDeleteFromArchive, path));

        doAnswer(new UploadSingleChunkRequestAssertionAnswer(
                        repository,
                        "",
                        VERSION_BEFORE,
                        VERSION_AFTER,
                        filesToDelete,
                        filesToDeleteFromArchive,
                        packagesToUploadWithPrefixes,
                        packagesToUploadToArchiveWithPrefixes,
                        pathsToUpload,
                        pathsToUploadToArchive,
                        pathsToDelete,
                        pathsToDeleteFromArchive))
                .when(rest)
                .postForEntity(anyString(), any(Object.class), eq(RepoResponse.class));

        doNothing().when(storage).cleanUpAfterSynchronization(populatedContent);

        cleanAfterTestPreparation(Paths.get(TEST_PACKAGES_FOR_SOURCES_PATH));
        cleanAfterTestPreparation(Paths.get(TEST_PACKAGES_FOR_BINARIES_PATH));

        repositorySynchronizer.storeRepositoryOnRemoteServer(repository, DATESTAMP);

        verify(storage).cleanUpAfterSynchronization(populatedContent);
    }

    @Test
    public void storeRepositoryOnRemoteServer_sameBinariesWithDifferentParameters() throws Exception {
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setServerAddress("http://127.0.0.1/r/testrepo1");

        final User user = UserTestFixture.GET_ADMIN();

        doReturn(new ResponseEntity<>("[3]", HttpStatus.OK))
                .when(rest)
                .getForEntity(eq("http://127.0.0.1/r/testrepo1/"), eq(String.class));
        doReturn(new ResponseEntity<>("[]", HttpStatus.OK))
                .when(rest)
                .getForEntity(eq("http://127.0.0.1/r/testrepo1/archive/"), eq(String.class));

        final List<RPackage> sourcePackages =
                RPackageTestFixture.GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE(repository, user);
        final List<RPackage> latestSources =
                RPackageTestFixture.GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE_ONLY_LATEST(
                        repository, user);
        final List<RPackage> archiveSources = sourcePackages.stream()
                .filter(p -> !latestSources.contains(p))
                .collect(Collectors.toCollection(ArrayList::new));

        final List<RPackage> binaries =
                RPackageTestFixture.GET_BINARY_PACKAGES_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE(repository, user);
        final List<RPackage> latestBinaries =
                RPackageTestFixture.GET_BINARY_PACKAGES_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE_ONLY_LATEST(
                        repository, user);
        final List<RPackage> archiveBinaries =
                binaries.stream().filter(p -> !latestBinaries.contains(p)).collect(Collectors.toList());

        archiveSources.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        latestSources.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        sourcePackages.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        archiveBinaries.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        latestBinaries.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        binaries.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);

        final MultiValueMap<String, RPackage> binLatestPaths = new LinkedMultiValueMap<>();
        binLatestPaths.addAll(BINARY_PATH, latestBinaries);
        final MultiValueMap<String, RPackage> binArchivePaths = new LinkedMultiValueMap<>();
        binArchivePaths.addAll(BINARY_PATH, archiveBinaries);

        final PopulatedRepositoryContent populatedContent = new PopulatedRepositoryContent(
                latestSources, archiveSources,
                SOURCE_LATEST_PATH, SOURCE_ARCHIVE_PATH,
                binLatestPaths, binArchivePaths);

        final MultiValueMap<String, File> binaryPackagesToUpload = new LinkedMultiValueMap<>();
        final MultiValueMap<String, File> binaryPackagesToUploadToArchive = new LinkedMultiValueMap<>();
        final MultiValueMap<String, String> binaryPackagesToDelete = new LinkedMultiValueMap<>();
        final MultiValueMap<String, String> binaryPackagesToDeleteFromArchive = new LinkedMultiValueMap<>();

        binaryPackagesToUpload.add(BINARY_PATH, new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/arrow_8.0.0.tar.gz"));
        binaryPackagesToDelete.add(BINARY_PATH, "binary_todelete1");
        binaryPackagesToDeleteFromArchive.add(BINARY_PATH, "binary_todelete2");
        final File packagesFileForBinaries = new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/PACKAGES");
        final File packagesGzFileForBinaries = new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/PACKAGES.gz");

        binaryPackagesToUpload.add(
                BINARY_CENTOS8_4_2_PATH, new File(TEST_PACKAGES_FOR_BINARIES_CENTOS8_PATH + "/arrow_8.0.0.tar.gz"));
        final File packagesFileForBinariesCentos8 = new File(TEST_PACKAGES_FOR_BINARIES_CENTOS8_PATH + "/PACKAGES");
        final File packagesGzFileForBinariesCentos8 =
                new File(TEST_PACKAGES_FOR_BINARIES_CENTOS8_PATH + "/PACKAGES.gz");

        final Map<String, File> packagesFiles = new HashMap<>();
        final Map<String, File> packagesGzFiles = new HashMap<>();
        final Map<String, File> packagesFilesForArchive = new HashMap<>();
        final Map<String, File> packagesGzFilesForArchive = new HashMap<>();

        packagesFiles.put(BINARY_PATH, packagesFileForBinaries);
        packagesFiles.put(BINARY_CENTOS8_4_2_PATH, packagesFileForBinariesCentos8);
        packagesGzFiles.put(BINARY_PATH, packagesGzFileForBinaries);
        packagesGzFiles.put(BINARY_CENTOS8_4_2_PATH, packagesGzFileForBinariesCentos8);

        final Map<String, Map<String, String>> checksums = new HashMap<>();
        checksums.put(BINARY_PATH, new HashMap<>() {

            @Serial
            private static final long serialVersionUID = -7946290225431849880L;

            {
                put("arrow_8.0.0.tar.gz", "b55eb6a2f5adeff68f1ef15fd35b03de");
            }
        });
        checksums.get(BINARY_PATH).put("recent/PACKAGES", "19d375fc238db3fcde9b2fbb53f4db79");
        checksums.get(BINARY_PATH).put("recent/PACKAGES.gz", "ef5a203d7f71fe78ab8ea3ba221cca16");
        checksums.put(BINARY_CENTOS8_4_2_PATH, new HashMap<>() {

            @Serial
            private static final long serialVersionUID = -7946290225431849880L;

            {
                put("arrow_8.0.0.tar.gz", "b55eb6a2f5adeff68f1ef15fd35b03de");
            }
        });
        checksums.get(BINARY_CENTOS8_4_2_PATH).put("recent/PACKAGES", "19d375fc238db3fcde9b2fbb53f4db79");
        checksums.get(BINARY_CENTOS8_4_2_PATH).put("recent/PACKAGES.gz", "ef5a203d7f71fe78ab8ea3ba221cca16");

        final SynchronizeRepositoryRequestBody requestBody = new SynchronizeRepositoryRequestBody(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                SOURCE_PATH,
                VERSION_BEFORE,
                VERSION_AFTER,
                binaryPackagesToUpload,
                binaryPackagesToUploadToArchive,
                binaryPackagesToDelete,
                binaryPackagesToDeleteFromArchive,
                packagesFiles,
                packagesGzFiles,
                packagesFilesForArchive,
                packagesGzFilesForArchive,
                checksums);

        ReflectionTestUtils.setField(repositorySynchronizer, "maxRequestSize", 10);
        doReturn(sourcePackages).when(packageService).findSourcePackagesByRepository(repository);
        doReturn(binaries).when(packageService).findBinaryPackagesByRepository(repository);
        doReturn(new HashSet<>(latestSources)).when(packageService).filterLatest(new HashSet<>(sourcePackages));
        doReturn(new HashSet<>(latestBinaries)).when(packageService).filterLatest(new HashSet<>(binaries));
        doReturn(populatedContent)
                .when(storage)
                .organizePackagesInStorage(
                        DATESTAMP,
                        sourcePackages,
                        latestSources,
                        archiveSources,
                        binaries,
                        latestBinaries,
                        archiveBinaries,
                        repository);

        doReturn(requestBody)
                .when(storage)
                .buildSynchronizeRequestBody(
                        populatedContent,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new LinkedMultiValueMap<>(),
                        new LinkedMultiValueMap<>(),
                        repository,
                        VERSION_BEFORE);

        // prepare files to assertion - add prefixes
        Map<String, String> pathsToUpload = new LinkedHashMap<>();
        List<File> packagesToUploadWithPrefixes = new ArrayList<>();

        binaryPackagesToUpload.forEach((path, packagesList) -> prepareTestFilesToChunkRequestAssertion(
                packagesList, packagesToUploadWithPrefixes, pathsToUpload, path));

        prepareTestPackagesFilesToChunkRequestAssertion(
                packagesFiles, packagesToUploadWithPrefixes, pathsToUpload, false);
        prepareTestPackagesFilesToChunkRequestAssertion(
                packagesGzFiles, packagesToUploadWithPrefixes, pathsToUpload, false);

        List<String> filesToDelete = new ArrayList<>();
        List<String> filesToDeleteFromArchive = new ArrayList<>();
        Map<String, String> pathsToDelete = new LinkedHashMap<>();
        Map<String, String> pathsToDeleteFromArchive = new LinkedHashMap<>();

        binaryPackagesToDelete.forEach((path, packageList) ->
                prepareTestFilesToDeleteToChunkRequestAssertion(packageList, filesToDelete, pathsToDelete, path));
        binaryPackagesToDeleteFromArchive.forEach(
                (path, packageList) -> prepareTestFilesToDeleteToChunkRequestAssertion(
                        packageList, filesToDeleteFromArchive, pathsToDeleteFromArchive, path));

        doAnswer(new UploadSingleChunkRequestAssertionAnswer(
                        repository,
                        "",
                        VERSION_BEFORE,
                        VERSION_AFTER,
                        filesToDelete,
                        filesToDeleteFromArchive,
                        packagesToUploadWithPrefixes,
                        new ArrayList<>(),
                        pathsToUpload,
                        new LinkedHashMap<>(),
                        pathsToDelete,
                        pathsToDeleteFromArchive))
                .when(rest)
                .postForEntity(anyString(), any(Object.class), eq(RepoResponse.class));

        doNothing().when(storage).cleanUpAfterSynchronization(populatedContent);

        cleanAfterTestPreparation(Paths.get(TEST_PACKAGES_FOR_BINARIES_CENTOS8_PATH));
        cleanAfterTestPreparation(Paths.get(TEST_PACKAGES_FOR_BINARIES_PATH));

        repositorySynchronizer.storeRepositoryOnRemoteServer(repository, DATESTAMP);

        verify(storage).cleanUpAfterSynchronization(populatedContent);
    }

    @Test
    public void storeRepositoryOnRemoteServer_withMultipleChunks() throws Exception {
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setServerAddress("http://127.0.0.1/testrepo1");
        final User user = UserTestFixture.GET_ADMIN();

        final String MULTIPLE_TEST_PACKAGES_PATH = TEST_PACKAGES_FOR_SOURCES_PATH + "/multiple_packages_test";

        doReturn(new ResponseEntity<>("[3]", HttpStatus.OK))
                .when(rest)
                .getForEntity(eq("http://127.0.0.1/r/testrepo1/"), eq(String.class));
        doReturn(new ResponseEntity<>("[]", HttpStatus.OK))
                .when(rest)
                .getForEntity(eq("http://127.0.0.1/r/testrepo1/archive/"), eq(String.class));

        ReflectionTestUtils.setField(repositorySynchronizer, "maxRequestSize", 4);

        final List<RPackage> packages = RPackageTestFixture.GET_PACKAGES_FOR_MULTICHUNK_UPLOAD(
                repository,
                MULTIPLE_TEST_PACKAGES_PATH,
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
                MULTIPLE_TEST_PACKAGES_PATH,
                "3_testpackage1_3.0.0",
                "7_testpackage2_3.2.1",
                "10_testpackage3_2.2.2");

        final List<RPackage> packagesArchive = RPackageTestFixture.GET_PACKAGES_FOR_MULTICHUNK_UPLOAD(
                repository,
                MULTIPLE_TEST_PACKAGES_PATH,
                "1_testpackage1_1.0.0",
                "2_testpackage1_1.0.1",
                "4_testpackage2_1.0.0",
                "5_testpackage2_1.1.0",
                "6_testpackage2_2.1.0",
                "8_testpackage3_1.0.0",
                "9_testpackage3_1.1.1");

        final List<RPackage> binaries =
                RPackageTestFixture.GET_BINARY_PACKAGES_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE(repository, user);
        final List<RPackage> latestBinaries =
                RPackageTestFixture.GET_BINARY_PACKAGES_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE_ONLY_LATEST(
                        repository, user);
        final List<RPackage> archiveBinaries = binaries.stream()
                .filter(p -> !latestBinaries.contains(p))
                .sorted(RRepositorySynchronizer.PACKAGE_COMPARATOR)
                .collect(Collectors.toList());

        latestBinaries.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        binaries.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);

        final MultiValueMap<String, RPackage> binLatestPaths = new LinkedMultiValueMap<>();
        binLatestPaths.addAll(BINARY_PATH, latestBinaries);
        final MultiValueMap<String, RPackage> binArchivePaths = new LinkedMultiValueMap<>();
        binArchivePaths.addAll(BINARY_PATH, archiveBinaries);

        final PopulatedRepositoryContent populatedContent = new PopulatedRepositoryContent(
                packagesLatest,
                packagesArchive,
                SOURCE_LATEST_PATH,
                SOURCE_ARCHIVE_PATH,
                binLatestPaths,
                binArchivePaths);

        final List<File> sourcePackagesToUpload = packagesLatest.stream()
                .map(p -> new File(p.getSource()))
                .collect(Collectors.toCollection(ArrayList::new));
        final List<File> sourcePackagesToUploadToArchive = packagesArchive.stream()
                .map(p -> new File(p.getSource()))
                .collect(Collectors.toCollection(ArrayList::new));

        final List<String> sourcePackagesToDelete = List.of("todelete1", "todelete2");
        final List<String> sourcePackagesToDeleteFromArchive = List.of("todelete_archive1", "todelete_archive2");

        final MultiValueMap<String, File> binaryPackagesToUpload = new LinkedMultiValueMap<>();
        final MultiValueMap<String, File> binaryPackagesToUploadToArchive = new LinkedMultiValueMap<>();
        final MultiValueMap<String, String> binaryPackagesToDelete = new LinkedMultiValueMap<>();
        final MultiValueMap<String, String> binaryPackagesToDeleteFromArchive = new LinkedMultiValueMap<>();

        binaryPackagesToUpload.add(BINARY_PATH, new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/arrow_8.0.0.tar.gz"));
        final File packagesFileForBinaries_LatestMinorRVersion =
                new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/PACKAGES");
        final File packagesGzFileForBinaries_LatestMinorRVersion =
                new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/PACKAGES.gz");
        binaryPackagesToUploadToArchive.add(
                BINARY_PATH, new File(TEST_PACKAGES_FOR_BINARIES_ARCHIVE_PATH + "/OpenSpecy_1.0.99.tar.gz"));
        final File packagesFileForBinaries_ArchiveMinorRVersion =
                new File(TEST_PACKAGES_FOR_BINARIES_ARCHIVE_PATH + "/PACKAGES_ARCHIVE");
        final File packagesGzFileForBinaries_ArchiveMinorRVersion =
                new File(TEST_PACKAGES_FOR_BINARIES_ARCHIVE_PATH + "/PACKAGES_ARCHIVE.gz");
        binaryPackagesToUpload.add(
                BINARY_HIGHER_VERSION_PATH,
                new File(TEST_PACKAGES_FOR_BINARIES_LATEST_PATH + "/OpenSpecy_1.1.0.tar.gz"));
        final File packagesFileForBinaries_LatestHigherRVersion =
                new File(TEST_PACKAGES_FOR_BINARIES_LATEST_PATH + "/PACKAGES");
        final File packagesGzFileForBinaries_LatestHigherRVersion =
                new File(TEST_PACKAGES_FOR_BINARIES_LATEST_PATH + "/PACKAGES.gz");
        binaryPackagesToDelete.add(BINARY_PATH, "binary_todelete1");

        doReturn(packages).when(packageService).findSourcePackagesByRepository(repository);
        doReturn(binaries).when(packageService).findBinaryPackagesByRepository(repository);
        doReturn(new LinkedHashSet<>(packagesLatest)).when(packageService).filterLatest(new LinkedHashSet<>(packages));
        doReturn(new HashSet<>(latestBinaries)).when(packageService).filterLatest(new HashSet<>(binaries));
        doReturn(populatedContent)
                .when(storage)
                .organizePackagesInStorage(
                        DATESTAMP,
                        packages,
                        packagesLatest,
                        packagesArchive,
                        binaries,
                        latestBinaries,
                        archiveBinaries,
                        repository);
        doNothing().when(storage).cleanUpAfterSynchronization(populatedContent);

        final File packagesFileForSources = new File(MULTIPLE_TEST_PACKAGES_PATH + "/PACKAGES");
        final File packagesGzFileForSources = new File(MULTIPLE_TEST_PACKAGES_PATH + "/PACKAGES.gz");
        final File packagesFileArchiveForSources = new File(MULTIPLE_TEST_PACKAGES_PATH + "/PACKAGES_ARCHIVE");
        final File packagesGzFileArchiveForSources = new File(MULTIPLE_TEST_PACKAGES_PATH + "/PACKAGES_ARCHIVE.gz");

        final Map<String, File> packagesFiles = new HashMap<>();
        final Map<String, File> packagesGzFiles = new HashMap<>();
        final Map<String, File> packagesFilesForArchive = new HashMap<>();
        final Map<String, File> packagesGzFilesForArchive = new HashMap<>();

        packagesFiles.put(SOURCE_PATH, packagesFileForSources);
        packagesFiles.put(BINARY_PATH, packagesFileForBinaries_LatestMinorRVersion);
        packagesFiles.put(BINARY_HIGHER_VERSION_PATH, packagesFileForBinaries_LatestHigherRVersion);

        packagesGzFiles.put(SOURCE_PATH, packagesGzFileForSources);
        packagesGzFiles.put(BINARY_PATH, packagesGzFileForBinaries_LatestMinorRVersion);
        packagesGzFiles.put(BINARY_HIGHER_VERSION_PATH, packagesGzFileForBinaries_LatestHigherRVersion);

        packagesFilesForArchive.put(SOURCE_PATH, packagesFileArchiveForSources);
        packagesFilesForArchive.put(BINARY_PATH, packagesFileForBinaries_ArchiveMinorRVersion);
        packagesGzFilesForArchive.put(SOURCE_PATH, packagesGzFileArchiveForSources);
        packagesGzFilesForArchive.put(BINARY_PATH, packagesGzFileForBinaries_ArchiveMinorRVersion);

        final Map<String, Map<String, String>> checksums = new HashMap<>();
        checksums.put(SOURCE_PATH, new HashMap<>() {

            @Serial
            private static final long serialVersionUID = 3001880287136804542L;

            {
                put("testpackage1_1.0.0.tar.gz", "c47d18b86b331a5023dcd62b74fedbb6");
            }
        });
        checksums.get(SOURCE_PATH).put("testpackage1_1.0.1.tar.gz", "c47d18b86b331a5023dcd62b74fedbb6");
        checksums.get(SOURCE_PATH).put("testpackage1_3.0.0.tar.gz", "c47d18b86b331a5023dcd62b74fedbb6");
        checksums.get(SOURCE_PATH).put("testpackage2_1.0.0.tar.gz", "c47d18b86b331a5023dcd62b74fedbb6");
        checksums.get(SOURCE_PATH).put("testpackage2_1.1.0.tar.gz", "c47d18b86b331a5023dcd62b74fedbb6");
        checksums.get(SOURCE_PATH).put("testpackage2_2.1.0.tar.gz", "c47d18b86b331a5023dcd62b74fedbb6");
        checksums.get(SOURCE_PATH).put("testpackage2_3.2.1.tar.gz", "c47d18b86b331a5023dcd62b74fedbb6");
        checksums.get(SOURCE_PATH).put("testpackage3_1.0.0.tar.gz", "c47d18b86b331a5023dcd62b74fedbb6");
        checksums.get(SOURCE_PATH).put("testpackage3_1.1.1.tar.gz", "c47d18b86b331a5023dcd62b74fedbb6");
        checksums.get(SOURCE_PATH).put("testpackage3_2.2.2.tar.gz", "c47d18b86b331a5023dcd62b74fedbb6");
        checksums.get(SOURCE_PATH).put("PACKAGES", "91fc181b3c9c4b356d8c9c64daeb2aa3");
        checksums.get(SOURCE_PATH).put("PACKAGES_ARCHIVE", "57d68e9cb8c17a92a5c91de506471674");
        checksums.get(SOURCE_PATH).put("PACKAGES_ARCHIVE.gz", "20d5badb99fc5f7fd9842dcc215e2eca");
        checksums.get(SOURCE_PATH).put("PACKAGES.gz", "c960f8caabf8fc6d4e214f2d7fcea83b");
        checksums.put(BINARY_PATH, new HashMap<>() {

            @Serial
            private static final long serialVersionUID = -3629374848421271559L;

            {
                put("arrow_8.0.0.tar.gz", "b55eb6a2f5adeff68f1ef15fd35b03de");
            }
        });
        checksums.get(BINARY_PATH).put("recent/PACKAGES", "19d375fc238db3fcde9b2fbb53f4db79");
        checksums.get(BINARY_PATH).put("recent/PACKAGES.gz", "ef5a203d7f71fe78ab8ea3ba221cca16");
        checksums.get(BINARY_PATH).put("OpenSpecy_1.0.99.tar.gz", "2333d8335e081ac4607495fe5e840dde");
        checksums.get(BINARY_PATH).put("archive/PACKAGES_ARCHIVE", "88ef9d48024d1175881894302abf3122");
        checksums.get(BINARY_PATH).put("archive/PACKAGES_ARCHIVE.gz", "0fa08c9ee5b2db0cc542880d0d238bcd");
        checksums.put(BINARY_HIGHER_VERSION_PATH, new HashMap<>() {

            @Serial
            private static final long serialVersionUID = 7237503829443707860L;

            {
                put("OpenSpecy_1.1.0.tar.gz", "13bda5374451f899771b8388983fe334");
            }
        });
        checksums.get(BINARY_HIGHER_VERSION_PATH).put("recent/PACKAGES", "4be06fd34e35897a1093a1500cd626b3");
        checksums.get(BINARY_HIGHER_VERSION_PATH).put("recent/PACKAGES.gz", "5394a5efae797ab7bad97043a5d7de6d");

        final SynchronizeRepositoryRequestBody requestBody = new SynchronizeRepositoryRequestBody(
                sourcePackagesToUpload,
                sourcePackagesToUploadToArchive,
                sourcePackagesToDelete,
                sourcePackagesToDeleteFromArchive,
                SOURCE_PATH,
                VERSION_BEFORE,
                VERSION_AFTER,
                binaryPackagesToUpload,
                binaryPackagesToUploadToArchive,
                binaryPackagesToDelete,
                binaryPackagesToDeleteFromArchive,
                packagesFiles,
                packagesGzFiles,
                packagesFilesForArchive,
                packagesGzFilesForArchive,
                checksums);
        doReturn(requestBody)
                .when(storage)
                .buildSynchronizeRequestBody(
                        populatedContent,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new LinkedMultiValueMap<>(),
                        new LinkedMultiValueMap<>(),
                        repository,
                        VERSION_BEFORE);

        // prepare files to assertion - add prefixes
        Map<String, String> pathsToUpload = new LinkedHashMap<>();
        List<File> packagesToUploadWithPrefixes = new ArrayList<>();

        prepareTestFilesToChunkRequestAssertion(
                sourcePackagesToUpload, packagesToUploadWithPrefixes, pathsToUpload, SOURCE_PATH);

        binaryPackagesToUpload.forEach((path, packagesList) -> prepareTestFilesToChunkRequestAssertion(
                packagesList, packagesToUploadWithPrefixes, pathsToUpload, path));

        prepareTestPackagesFilesToChunkRequestAssertion(
                packagesFiles, packagesToUploadWithPrefixes, pathsToUpload, false);
        prepareTestPackagesFilesToChunkRequestAssertion(
                packagesGzFiles, packagesToUploadWithPrefixes, pathsToUpload, false);

        Map<String, String> pathsToUploadToArchive = new LinkedHashMap<>();
        List<File> packagesToUploadToArchiveWithPrefixes = new ArrayList<>();

        prepareTestFilesToChunkRequestAssertion(
                sourcePackagesToUploadToArchive,
                packagesToUploadToArchiveWithPrefixes,
                pathsToUploadToArchive,
                SOURCE_PATH);

        binaryPackagesToUploadToArchive.forEach((path, packagesList) -> prepareTestFilesToChunkRequestAssertion(
                packagesList, packagesToUploadToArchiveWithPrefixes, pathsToUploadToArchive, path));

        prepareTestPackagesFilesToChunkRequestAssertion(
                packagesFilesForArchive, packagesToUploadToArchiveWithPrefixes, pathsToUploadToArchive, true);
        prepareTestPackagesFilesToChunkRequestAssertion(
                packagesGzFilesForArchive, packagesToUploadToArchiveWithPrefixes, pathsToUploadToArchive, true);

        List<String> filesToDelete = new ArrayList<>();
        List<String> filesToDeleteFromArchive = new ArrayList<>();
        Map<String, String> pathsToDelete = new LinkedHashMap<>();
        Map<String, String> pathsToDeleteFromArchive = new LinkedHashMap<>();

        prepareTestFilesToDeleteToChunkRequestAssertion(
                sourcePackagesToDelete, filesToDelete, pathsToDelete, SOURCE_PATH);
        prepareTestFilesToDeleteToChunkRequestAssertion(
                sourcePackagesToDeleteFromArchive, filesToDeleteFromArchive, pathsToDeleteFromArchive, SOURCE_PATH);

        binaryPackagesToDelete.forEach((path, packageList) ->
                prepareTestFilesToDeleteToChunkRequestAssertion(packageList, filesToDelete, pathsToDelete, path));
        binaryPackagesToDeleteFromArchive.forEach(
                (path, packageList) -> prepareTestFilesToDeleteToChunkRequestAssertion(
                        packageList, filesToDeleteFromArchive, pathsToDeleteFromArchive, path));

        List<File> expectedFirstChunkLatest = new ArrayList<>(packagesToUploadWithPrefixes.subList(0, 4));
        List<List<File>> chunksToUploadLatest = List.of(
                expectedFirstChunkLatest,
                packagesToUploadWithPrefixes.subList(4, 8),
                packagesToUploadWithPrefixes.subList(8, 11));
        Map<String, String> expectedFirstChunkLatestPaths = pathsToUpload.entrySet().stream()
                .limit(4)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<Map<String, String>> chunksLatestPaths = List.of(
                expectedFirstChunkLatestPaths,
                pathsToUpload.entrySet().stream()
                        .skip(4)
                        .limit(4)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                pathsToUpload.entrySet().stream()
                        .skip(8)
                        .limit(4)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        List<File> expectedFirstChunkArchive = new ArrayList<>(packagesToUploadToArchiveWithPrefixes.subList(0, 4));
        List<List<File>> chunksToUploadArchive = List.of(
                expectedFirstChunkArchive,
                packagesToUploadToArchiveWithPrefixes.subList(4, 8),
                packagesToUploadToArchiveWithPrefixes.subList(8, 12));
        Map<String, String> expectedFirstChunkArchivePaths = pathsToUploadToArchive.entrySet().stream()
                .limit(4)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<Map<String, String>> chunksArchivePaths = List.of(
                expectedFirstChunkArchivePaths,
                pathsToUploadToArchive.entrySet().stream()
                        .skip(4)
                        .limit(4)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                pathsToUploadToArchive.entrySet().stream()
                        .skip(8)
                        .limit(4)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        doAnswer(new UploadMultipleChunksRequestAssertionAnswer(
                        3,
                        repository,
                        VERSION_BEFORE,
                        VERSION_AFTER,
                        filesToDelete,
                        filesToDeleteFromArchive,
                        chunksToUploadLatest,
                        chunksToUploadArchive,
                        chunksLatestPaths,
                        chunksArchivePaths,
                        pathsToDelete,
                        pathsToDeleteFromArchive))
                .when(rest)
                .postForEntity(anyString(), any(Object.class), eq(RepoResponse.class));

        cleanAfterTestPreparation(Paths.get(MULTIPLE_TEST_PACKAGES_PATH));
        cleanAfterTestPreparation(Paths.get(TEST_PACKAGES_FOR_BINARIES_PATH));
        cleanAfterTestPreparation(Paths.get(TEST_PACKAGES_FOR_BINARIES_LATEST_PATH));
        cleanAfterTestPreparation(Paths.get(TEST_PACKAGES_FOR_BINARIES_ARCHIVE_PATH));

        repositorySynchronizer.storeRepositoryOnRemoteServer(repository, DATESTAMP);

        verify(storage).cleanUpAfterSynchronization(populatedContent);
    }

    @Test
    public void storeRepositoryOnRemoteServer_shouldFail_whenRemoteSeverDoesNotReturn200() throws Exception {
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setServerAddress("http://127.0.0.1/testrepo1");

        final User user = UserTestFixture.GET_ADMIN();

        doReturn(new ResponseEntity<>("[3]", HttpStatus.OK))
                .when(rest)
                .getForEntity(eq("http://127.0.0.1/r/testrepo1/"), eq(String.class));
        doReturn(new ResponseEntity<>("[]", HttpStatus.OK))
                .when(rest)
                .getForEntity(eq("http://127.0.0.1/r/testrepo1/archive/"), eq(String.class));

        final List<RPackage> packages =
                RPackageTestFixture.GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE(repository, user);
        final List<RPackage> onlyLatest =
                RPackageTestFixture.GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE_ONLY_LATEST(
                        repository, user);
        final List<RPackage> onlyArchive =
                packages.stream().filter(p -> !onlyLatest.contains(p)).collect(Collectors.toCollection(ArrayList::new));

        final List<RPackage> binaries =
                RPackageTestFixture.GET_BINARY_PACKAGES_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE(repository, user);
        final List<RPackage> latestBinaries =
                RPackageTestFixture.GET_BINARY_PACKAGES_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE_ONLY_LATEST(
                        repository, user);
        final List<RPackage> archiveBinaries =
                binaries.stream().filter(p -> !latestBinaries.contains(p)).collect(Collectors.toList());

        onlyArchive.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        onlyLatest.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        packages.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        archiveBinaries.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        latestBinaries.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        binaries.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);

        final MultiValueMap<String, RPackage> binLatestPaths = new LinkedMultiValueMap<>();
        binLatestPaths.addAll(BINARY_PATH, latestBinaries);
        final MultiValueMap<String, RPackage> binArchivePaths = new LinkedMultiValueMap<>();
        binArchivePaths.addAll(BINARY_PATH, archiveBinaries);

        final PopulatedRepositoryContent populatedContent = new PopulatedRepositoryContent(
                onlyLatest, onlyArchive, SOURCE_LATEST_PATH, SOURCE_ARCHIVE_PATH, binLatestPaths, binArchivePaths);

        final List<File> sourcePackagesToUpload = List.of(
                new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/abc_1.3.tar.gz"),
                new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/accrued_1.3.tar.gz"));
        final List<File> sourcePackagesToUploadToArchive =
                List.of(new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/accrued_1.2.tar.gz"));
        final List<String> sourcePackagesToDelete = List.of("todelete1", "todelete2");
        final List<String> sourcePackagesToDeleteFromArchive = List.of("todelete_archive1", "todelete_archive2");
        final File packagesFileForSources = new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/PACKAGES");
        final File packagesGzFileForSources = new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/PACKAGES.gz");
        final File packagesFileArchiveForSources = new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/PACKAGES_ARCHIVE");
        final File packagesGzFileArchiveForSources = new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/PACKAGES_ARCHIVE.gz");

        final MultiValueMap<String, File> binaryPackagesToUpload = new LinkedMultiValueMap<>();
        final MultiValueMap<String, File> binaryPackagesToUploadToArchive = new LinkedMultiValueMap<>();
        final MultiValueMap<String, String> binaryPackagesToDelete = new LinkedMultiValueMap<>();
        final MultiValueMap<String, String> binaryPackagesToDeleteFromArchive = new LinkedMultiValueMap<>();

        binaryPackagesToUpload.add(BINARY_PATH, new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/arrow_8.0.0.tar.gz"));
        binaryPackagesToDelete.add(BINARY_PATH, "binary_todelete1");
        final File packagesFileForBinaries = new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/PACKAGES");
        final File packagesGzFileForBinaries = new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/PACKAGES.gz");

        final Map<String, File> packagesFiles = new HashMap<>();
        final Map<String, File> packagesGzFiles = new HashMap<>();
        final Map<String, File> packagesFilesForArchive = new HashMap<>();
        final Map<String, File> packagesGzFilesForArchive = new HashMap<>();

        packagesFiles.put(SOURCE_PATH, packagesFileForSources);
        packagesFiles.put(BINARY_PATH, packagesFileForBinaries);
        packagesGzFiles.put(SOURCE_PATH, packagesGzFileForSources);
        packagesGzFiles.put(BINARY_PATH, packagesGzFileForBinaries);
        packagesFilesForArchive.put(SOURCE_PATH, packagesFileArchiveForSources);
        packagesGzFilesForArchive.put(SOURCE_PATH, packagesGzFileArchiveForSources);

        final Map<String, Map<String, String>> checksums = new HashMap<>();
        checksums.put(SOURCE_PATH, new HashMap<>() {

            @Serial
            private static final long serialVersionUID = 814643588296246560L;

            {
                put("abc_1.3.tar.gz", "c47d18b86b331a5023dcd62b74fedbb6");
            }
        });
        checksums.get(SOURCE_PATH).put("accrued_1.2.tar.gz", "70d295115295a4718593f6a39d77add9");
        checksums.get(SOURCE_PATH).put("accrued_1.3.tar.gz", "a05e4ca44438c0d9e7d713d7e3890423");
        checksums.get(SOURCE_PATH).put("recent/PACKAGES", "639bf3e988764b375fa2fbdbf0ed0b74");
        checksums.get(SOURCE_PATH).put("archive/PACKAGES_ARCHIVE", "65e7f749cf8315c5c8255da701c5a844");
        checksums.get(SOURCE_PATH).put("archive/PACKAGES_ARCHIVE.gz", "df767a18774c0af24bb6b92f77b034e5");
        checksums.get(SOURCE_PATH).put("recent/PACKAGES.gz", "6a075ae72dcc3cc37a75f1df68816a14");
        checksums.put(BINARY_PATH, new HashMap<>() {

            @Serial
            private static final long serialVersionUID = 3917524086949002413L;

            {
                put("arrow_8.0.0.tar.gz", "b55eb6a2f5adeff68f1ef15fd35b03de");
            }
        });
        checksums.get(BINARY_PATH).put("recent/PACKAGES", "19d375fc238db3fcde9b2fbb53f4db79");
        checksums.get(BINARY_PATH).put("recent/PACKAGES.gz", "ef5a203d7f71fe78ab8ea3ba221cca16");

        final SynchronizeRepositoryRequestBody requestBody = new SynchronizeRepositoryRequestBody(
                sourcePackagesToUpload,
                sourcePackagesToUploadToArchive,
                sourcePackagesToDelete,
                sourcePackagesToDeleteFromArchive,
                SOURCE_PATH,
                VERSION_BEFORE,
                VERSION_AFTER,
                binaryPackagesToUpload,
                binaryPackagesToUploadToArchive,
                binaryPackagesToDelete,
                binaryPackagesToDeleteFromArchive,
                packagesFiles,
                packagesGzFiles,
                packagesFilesForArchive,
                packagesGzFilesForArchive,
                checksums);

        ReflectionTestUtils.setField(repositorySynchronizer, "maxRequestSize", 10);
        doReturn(packages).when(packageService).findSourcePackagesByRepository(repository);
        doReturn(binaries).when(packageService).findBinaryPackagesByRepository(repository);
        doReturn(new HashSet<>(onlyLatest)).when(packageService).filterLatest(new HashSet<>(packages));
        doReturn(new HashSet<>(latestBinaries)).when(packageService).filterLatest(new HashSet<>(binaries));

        doReturn(populatedContent)
                .when(storage)
                .organizePackagesInStorage(
                        DATESTAMP,
                        packages,
                        onlyLatest,
                        onlyArchive,
                        binaries,
                        latestBinaries,
                        archiveBinaries,
                        repository);

        doReturn(requestBody)
                .when(storage)
                .buildSynchronizeRequestBody(
                        populatedContent,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new LinkedMultiValueMap<>(),
                        new LinkedMultiValueMap<>(),
                        repository,
                        VERSION_BEFORE);
        doReturn(ResponseEntity.internalServerError().build())
                .when(rest)
                .postForEntity(anyString(), any(Object.class), eq(RepoResponse.class));

        assertThrows(
                SynchronizeRepositoryException.class,
                () -> repositorySynchronizer.storeRepositoryOnRemoteServer(repository, DATESTAMP));

        cleanAfterTestPreparation(Paths.get(TEST_PACKAGES_FOR_SOURCES_PATH));
        cleanAfterTestPreparation(Paths.get(TEST_PACKAGES_FOR_BINARIES_PATH));
    }

    private void prepareTestPackagesFilesToChunkRequestAssertion(
            Map<String, File> packagesFilesToChange,
            List<File> listToAddPackages,
            Map<String, String> pathsToPackages,
            boolean archive) {
        packagesFilesToChange.forEach((path, file) -> {
            String latestOrArchive = archive ? "archive" : "latest";
            File withPrefix = prepareFile(file, path + latestOrArchive);
            listToAddPackages.add(withPrefix);
            pathsToPackages.put(withPrefix.getName(), path);
        });
    }

    private void prepareTestFilesToChunkRequestAssertion(
            List<File> packagesToChange,
            List<File> listToAddPackages,
            Map<String, String> pathsToPackages,
            String packagesPath) {
        packagesToChange.forEach(file -> {
            File withPrefix = prepareFile(file, packagesPath);
            listToAddPackages.add(withPrefix);
            pathsToPackages.put(withPrefix.getName(), packagesPath);
        });
    }

    private void prepareTestFilesToDeleteToChunkRequestAssertion(
            List<String> filesToDelete,
            List<String> listToAddPackages,
            Map<String, String> filesPathsToDelete,
            String path) {

        filesToDelete.forEach(packageName -> {
            String withPrefix = prepareFileName(packageName, path);
            listToAddPackages.add(withPrefix);
            filesPathsToDelete.put(withPrefix, path);
        });
    }

    private File prepareFile(File originalFile, String path) {

        String prefix = path.replaceAll("[^a-zA-Z0-9]", "");

        File withPrefixFile = new File(originalFile.getParent() + "/" + prefix + "_" + originalFile.getName());

        try {
            FileUtils.moveFile(originalFile, withPrefixFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException("Could not properly prepare files in chunks!");
        }
        return withPrefixFile;
    }

    private String prepareFileName(String fileName, String destinationPath) {
        return destinationPath.replaceAll("[^a-zA-Z0-9]", "") + "_" + fileName;
    }

    private void cleanAfterTestPreparation(Path testPackagesPath) {

        if (Files.isDirectory(testPackagesPath)) {
            File directory = new File(testPackagesPath.toString());
            File[] files = directory.listFiles();
            try {
                for (File file : Objects.requireNonNull(files)) {
                    if (!Files.isDirectory(Paths.get(file.getPath()))
                            && !file.getName().startsWith(".")) {
                        Path withoutPrefix =
                                Paths.get(file.getParent(), StringUtils.substringAfter(file.getName(), "_"));
                        if (!file.getAbsolutePath()
                                .equals(withoutPrefix.toFile().getAbsolutePath())) {
                            FileUtils.moveFile(file, withoutPrefix.toFile());
                        }
                    }
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new IllegalStateException("Could not clean after test preparation!");
            }
        }
    }

    @Test
    public void storeRepositoryOnRemoteServer_whenRemoteServerIsInSubdirectory() throws Exception {
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setServerAddress("http://127.0.0.1/repo-server/testrepo1");

        final User user = UserTestFixture.GET_ADMIN();

        doReturn(new ResponseEntity<>("[3]", HttpStatus.OK))
                .when(rest)
                .getForEntity(eq("http://127.0.0.1/repo-server/r/testrepo1/"), eq(String.class));
        doReturn(new ResponseEntity<>("[]", HttpStatus.OK))
                .when(rest)
                .getForEntity(eq("http://127.0.0.1/repo-server/r/testrepo1/archive/"), eq(String.class));

        final List<RPackage> packages =
                RPackageTestFixture.GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE(repository, user);
        final List<RPackage> onlyLatest =
                RPackageTestFixture.GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE_ONLY_LATEST(
                        repository, user);
        final List<RPackage> onlyArchive =
                packages.stream().filter(p -> !onlyLatest.contains(p)).collect(Collectors.toCollection(ArrayList::new));

        final List<RPackage> binaries =
                RPackageTestFixture.GET_BINARY_PACKAGES_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE(repository, user);
        final List<RPackage> latestBinaries =
                RPackageTestFixture.GET_BINARY_PACKAGES_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE_ONLY_LATEST(
                        repository, user);
        final List<RPackage> archiveBinaries =
                binaries.stream().filter(p -> !latestBinaries.contains(p)).collect(Collectors.toList());

        onlyArchive.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        onlyLatest.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        packages.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        archiveBinaries.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        latestBinaries.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);
        binaries.sort(RRepositorySynchronizer.PACKAGE_COMPARATOR);

        final MultiValueMap<String, RPackage> binLatestPaths = new LinkedMultiValueMap<>();
        binLatestPaths.addAll(BINARY_PATH, latestBinaries);
        final MultiValueMap<String, RPackage> binArchivePaths = new LinkedMultiValueMap<>();
        binArchivePaths.addAll(BINARY_PATH, archiveBinaries);

        final PopulatedRepositoryContent populatedContent = new PopulatedRepositoryContent(
                onlyLatest, onlyArchive, SOURCE_LATEST_PATH, SOURCE_ARCHIVE_PATH, binLatestPaths, binArchivePaths);

        final List<File> sourcePackagesToUpload = List.of(
                new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/abc_1.3.tar.gz"),
                new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/accrued_1.3.tar.gz"));
        final List<File> sourcePackagesToUploadToArchive =
                List.of(new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/accrued_1.2.tar.gz"));
        final List<String> sourcePackagesToDelete = List.of("todelete1", "todelete2");
        final List<String> sourcePackagesToDeleteFromArchive = List.of("todelete_archive1", "todelete_archive2");
        final File packagesFileForSources = new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/PACKAGES");
        final File packagesGzFileForSources = new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/PACKAGES.gz");
        final File packagesFileArchiveForSources = new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/PACKAGES_ARCHIVE");
        final File packagesGzFileArchiveForSources = new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/PACKAGES_ARCHIVE.gz");

        final MultiValueMap<String, File> binaryPackagesToUpload = new LinkedMultiValueMap<>();
        final MultiValueMap<String, File> binaryPackagesToUploadToArchive = new LinkedMultiValueMap<>();
        final MultiValueMap<String, String> binaryPackagesToDelete = new LinkedMultiValueMap<>();
        final MultiValueMap<String, String> binaryPackagesToDeleteFromArchive = new LinkedMultiValueMap<>();

        binaryPackagesToUpload.add(BINARY_PATH, new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/arrow_8.0.0.tar.gz"));
        binaryPackagesToDelete.add(BINARY_PATH, "binary_todelete1");
        final File packagesFileForBinaries = new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/PACKAGES");
        final File packagesGzFileForBinaries = new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/PACKAGES.gz");

        final Map<String, File> packagesFiles = new HashMap<>();
        final Map<String, File> packagesGzFiles = new HashMap<>();
        final Map<String, File> packagesFilesForArchive = new HashMap<>();
        final Map<String, File> packagesGzFilesForArchive = new HashMap<>();

        packagesFiles.put(SOURCE_PATH, packagesFileForSources);
        packagesFiles.put(BINARY_PATH, packagesFileForBinaries);
        packagesGzFiles.put(SOURCE_PATH, packagesGzFileForSources);
        packagesGzFiles.put(BINARY_PATH, packagesGzFileForBinaries);
        packagesFilesForArchive.put(SOURCE_PATH, packagesFileArchiveForSources);
        packagesGzFilesForArchive.put(SOURCE_PATH, packagesGzFileArchiveForSources);

        final Map<String, Map<String, String>> checksums = new HashMap<>();
        checksums.put(SOURCE_PATH, new HashMap<>() {

            @Serial
            private static final long serialVersionUID = -8815584833782011817L;

            {
                put("abc_1.3.tar.gz", "c47d18b86b331a5023dcd62b74fedbb6");
            }
        });
        checksums.get(SOURCE_PATH).put("accrued_1.2.tar.gz", "70d295115295a4718593f6a39d77add9");
        checksums.get(SOURCE_PATH).put("accrued_1.3.tar.gz", "a05e4ca44438c0d9e7d713d7e3890423");
        checksums.get(SOURCE_PATH).put("recent/PACKAGES", "639bf3e988764b375fa2fbdbf0ed0b74");
        checksums.get(SOURCE_PATH).put("archive/PACKAGES_ARCHIVE", "65e7f749cf8315c5c8255da701c5a844");
        checksums.get(SOURCE_PATH).put("archive/PACKAGES_ARCHIVE.gz", "df767a18774c0af24bb6b92f77b034e5");
        checksums.get(SOURCE_PATH).put("recent/PACKAGES.gz", "6a075ae72dcc3cc37a75f1df68816a14");
        checksums.put(BINARY_PATH, new HashMap<>() {

            @Serial
            private static final long serialVersionUID = -4887913667826809912L;

            {
                put("arrow_8.0.0.tar.gz", "b55eb6a2f5adeff68f1ef15fd35b03de");
            }
        });
        checksums.get(BINARY_PATH).put("recent/PACKAGES", "19d375fc238db3fcde9b2fbb53f4db79");
        checksums.get(BINARY_PATH).put("recent/PACKAGES.gz", "ef5a203d7f71fe78ab8ea3ba221cca16");

        final SynchronizeRepositoryRequestBody requestBody = new SynchronizeRepositoryRequestBody(
                sourcePackagesToUpload,
                sourcePackagesToUploadToArchive,
                sourcePackagesToDelete,
                sourcePackagesToDeleteFromArchive,
                SOURCE_PATH,
                VERSION_BEFORE,
                VERSION_AFTER,
                binaryPackagesToUpload,
                binaryPackagesToUploadToArchive,
                binaryPackagesToDelete,
                binaryPackagesToDeleteFromArchive,
                packagesFiles,
                packagesGzFiles,
                packagesFilesForArchive,
                packagesGzFilesForArchive,
                checksums);

        ReflectionTestUtils.setField(repositorySynchronizer, "maxRequestSize", 10);
        doReturn(packages).when(packageService).findSourcePackagesByRepository(repository);
        doReturn(binaries).when(packageService).findBinaryPackagesByRepository(repository);
        doReturn(new HashSet<>(onlyLatest)).when(packageService).filterLatest(new HashSet<>(packages));
        doReturn(new HashSet<>(latestBinaries)).when(packageService).filterLatest(new HashSet<>(binaries));
        doReturn(populatedContent)
                .when(storage)
                .organizePackagesInStorage(
                        DATESTAMP,
                        packages,
                        onlyLatest,
                        onlyArchive,
                        binaries,
                        latestBinaries,
                        archiveBinaries,
                        repository);

        doReturn(requestBody)
                .when(storage)
                .buildSynchronizeRequestBody(
                        populatedContent,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new LinkedMultiValueMap<>(),
                        new LinkedMultiValueMap<>(),
                        repository,
                        VERSION_BEFORE);

        Map<String, String> pathsToUpload = new LinkedHashMap<>();
        List<File> packagesToUploadWithPrefixes = new ArrayList<>();

        prepareTestFilesToChunkRequestAssertion(
                sourcePackagesToUpload, packagesToUploadWithPrefixes, pathsToUpload, SOURCE_PATH);

        binaryPackagesToUpload.forEach((path, packagesList) -> prepareTestFilesToChunkRequestAssertion(
                packagesList, packagesToUploadWithPrefixes, pathsToUpload, path));

        prepareTestPackagesFilesToChunkRequestAssertion(
                packagesFiles, packagesToUploadWithPrefixes, pathsToUpload, false);
        prepareTestPackagesFilesToChunkRequestAssertion(
                packagesGzFiles, packagesToUploadWithPrefixes, pathsToUpload, false);

        Map<String, String> pathsToUploadToArchive = new LinkedHashMap<>();
        List<File> packagesToUploadToArchiveWithPrefixes = new ArrayList<>();

        prepareTestFilesToChunkRequestAssertion(
                sourcePackagesToUploadToArchive,
                packagesToUploadToArchiveWithPrefixes,
                pathsToUploadToArchive,
                SOURCE_PATH);

        prepareTestPackagesFilesToChunkRequestAssertion(
                packagesFilesForArchive, packagesToUploadToArchiveWithPrefixes, pathsToUploadToArchive, true);
        prepareTestPackagesFilesToChunkRequestAssertion(
                packagesGzFilesForArchive, packagesToUploadToArchiveWithPrefixes, pathsToUploadToArchive, true);

        List<String> filesToDelete = new ArrayList<>();
        List<String> filesToDeleteFromArchive = new ArrayList<>();
        Map<String, String> pathsToDelete = new LinkedHashMap<>();
        Map<String, String> pathsToDeleteFromArchive = new LinkedHashMap<>();

        prepareTestFilesToDeleteToChunkRequestAssertion(
                sourcePackagesToDelete, filesToDelete, pathsToDelete, SOURCE_PATH);
        prepareTestFilesToDeleteToChunkRequestAssertion(
                sourcePackagesToDeleteFromArchive, filesToDeleteFromArchive, pathsToDeleteFromArchive, SOURCE_PATH);

        binaryPackagesToDelete.forEach((path, packageList) ->
                prepareTestFilesToDeleteToChunkRequestAssertion(packageList, filesToDelete, pathsToDelete, path));
        binaryPackagesToDeleteFromArchive.forEach(
                (path, packageList) -> prepareTestFilesToDeleteToChunkRequestAssertion(
                        packageList, filesToDeleteFromArchive, pathsToDeleteFromArchive, path));

        doAnswer(new UploadSingleChunkRequestAssertionAnswer(
                        repository,
                        "",
                        VERSION_BEFORE,
                        VERSION_AFTER,
                        filesToDelete,
                        filesToDeleteFromArchive,
                        packagesToUploadWithPrefixes,
                        packagesToUploadToArchiveWithPrefixes,
                        pathsToUpload,
                        pathsToUploadToArchive,
                        pathsToDelete,
                        pathsToDeleteFromArchive))
                .when(rest)
                .postForEntity(anyString(), any(Object.class), eq(RepoResponse.class));
        doNothing().when(storage).cleanUpAfterSynchronization(populatedContent);

        cleanAfterTestPreparation(Paths.get(TEST_PACKAGES_FOR_SOURCES_PATH));
        cleanAfterTestPreparation(Paths.get(TEST_PACKAGES_FOR_BINARIES_PATH));

        repositorySynchronizer.storeRepositoryOnRemoteServer(repository, DATESTAMP);

        verify(storage).cleanUpAfterSynchronization(populatedContent);
    }
}

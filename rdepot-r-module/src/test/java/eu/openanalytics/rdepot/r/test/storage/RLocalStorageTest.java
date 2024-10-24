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
package eu.openanalytics.rdepot.r.test.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import eu.openanalytics.rdepot.base.email.EmailService;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.storage.implementations.CommonLocalStorage;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.r.api.v2.dtos.RPackageUploadRequest;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.mediator.deletion.RPackageDeleter;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.storage.implementations.RLocalStorage;
import eu.openanalytics.rdepot.r.storage.utils.PopulatedRepositoryContent;
import eu.openanalytics.rdepot.r.strategy.upload.RPackageUploadStrategy;
import eu.openanalytics.rdepot.r.synchronization.RRepositorySynchronizer;
import eu.openanalytics.rdepot.r.synchronization.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.r.validation.RPackageValidator;
import eu.openanalytics.rdepot.test.fixture.RPackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.UnitTest;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

import org.apache.http.entity.ContentType;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

public class RLocalStorageTest extends UnitTest {

    private static final File testResourcesHome = new File("src/test/resources/unit/storage_tests");
    private static final File packageUploadDirectory = new File("src/test/resources/unit/storage_tests/rdepot");
    private static final File repositoryGenerationDirectory =
            new File("src/test/resources/unit/storage_tests/rdepot/generated");

    private final RLocalStorage storage = new RLocalStorage();

    @BeforeEach
    public void setUpDirectories() throws Exception {
        restore();
        ReflectionTestUtils.setField(
                storage, CommonLocalStorage.class, "packageUploadDirectory", packageUploadDirectory, File.class);
        ReflectionTestUtils.setField(
                storage,
                CommonLocalStorage.class,
                "repositoryGenerationDirectory",
                repositoryGenerationDirectory,
                File.class);
        ReflectionTestUtils.setField(
                storage, RLocalStorage.class, "packageUploadDirectory", packageUploadDirectory, File.class);
        ReflectionTestUtils.setField(
                storage,
                RLocalStorage.class,
                "repositoryGenerationDirectory",
                repositoryGenerationDirectory,
                File.class);
        ReflectionTestUtils.setField(storage, RLocalStorage.class, "snapshot", "true", String.class);
    }

    @AfterEach
    public void cleanUp() throws Exception {
        deleteTestFilesIfExist();
    }

    private void restore() throws Exception {
        deleteTestFilesIfExist();
        executeBashCommand(
                "tar",
                "-xvzf",
                testResourcesHome.getAbsolutePath() + "/rdepot.tar.gz",
                "-C",
                testResourcesHome.getAbsolutePath());
    }

    private void deleteTestFilesIfExist() throws Exception {
        if (packageUploadDirectory.exists()) {
            FileUtils.forceDelete(packageUploadDirectory);
        }
    }

    @Test
    public void buildSynchronizeRequestBody() throws Exception {
        final String datestamp = "20240111";
        final User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(2);
        repository.setPublished(true);
        repository.setVersion(5);

        final RPackage benchmarkingArchivePackage = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        benchmarkingArchivePackage.setSource(
                new File(packageUploadDirectory + "/repositories/2/71228208/Benchmarking_0.10.tar.gz")
                        .getAbsolutePath());
        benchmarkingArchivePackage.setMd5sum("9a99c2ebefa6d49422ca7893c1f4ead8");
        benchmarkingArchivePackage.setName("Benchmarking");
        benchmarkingArchivePackage.setVersion("0.10");
        benchmarkingArchivePackage.setActive(true);

        final RPackage benchmarkingPackage = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        benchmarkingPackage.setSource(
                new File(packageUploadDirectory + "/repositories/2/79878978/Benchmarking_0.31.tar.gz")
                        .getAbsolutePath());
        benchmarkingPackage.setName("Benchmarking");
        benchmarkingPackage.setVersion("0.31");
        benchmarkingPackage.setActive(true);
        benchmarkingPackage.setMd5sum("136460e58c711ed9cc1cdbdbde2e5b86");

        final RPackage beaR = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        beaR.setSource(
                new File(packageUploadDirectory + "/repositories/2/89565416/bea.R_1.0.5.tar.gz").getAbsolutePath());
        beaR.setName("bea.R");
        beaR.setVersion("1.0.5");
        beaR.setActive(true);
        beaR.setMd5sum("5e664f320c7cc884138d64467f6b0e49");

        List<RPackage> packages = List.of(benchmarkingArchivePackage, benchmarkingPackage, beaR);
        List<RPackage> archivePackages = List.of(benchmarkingArchivePackage);
        List<RPackage> latestPackages = List.of(benchmarkingPackage, beaR);

        final PopulatedRepositoryContent content =
                storage.organizePackagesInStorage(datestamp, packages, latestPackages, archivePackages, repository);

        List<String> remoteLatestPackages = List.of("Benchmarking_0.10.tar.gz");
        List<String> remoteArchivePackages = List.of();
        int versionBefore = 4;

        final SynchronizeRepositoryRequestBody requestBody = storage.buildSynchronizeRequestBody(
                content, remoteLatestPackages, remoteArchivePackages, repository, versionBefore + "");

        final File latestPackagesFile =
                new File(repositoryGenerationDirectory + "/2/current/src/contrib/latest/PACKAGES");
        final File archivePackagesFile =
                new File(repositoryGenerationDirectory + "/2/current/src/contrib/Archive/PACKAGES");

        final File packageToUpload1 =
                new File(packageUploadDirectory + "/repositories/2/79878978/Benchmarking_0.31.tar.gz");
        final File packageToUpload2 = new File(packageUploadDirectory + "/repositories/2/89565416/bea.R_1.0.5.tar.gz");
        final File packageToUploadToArchive =
                new File(packageUploadDirectory + "/repositories/2/71228208/Benchmarking_0.10.tar.gz");

        assertTrue(
                requestBody.getPackagesToDeleteFromArchive().isEmpty(),
                "There should be no packages to delete from Archive.");
        assertEquals(1, requestBody.getPackagesToDelete().size(), "There should one package to delete.");
        assertEquals(2, requestBody.getPackagesToUpload().size(), "Too many packages to upload.");
        assertEquals(1, requestBody.getPackagesToUploadToArchive().size(), "Too many packages to upload to archive.");
        assertTrue(
                containsFile(packageToUpload1, requestBody.getPackagesToUpload()),
                "There is a missing package to upload.");
        assertTrue(
                containsFile(packageToUpload2, requestBody.getPackagesToUpload()),
                "There is a missing package to upload.");
        assertTrue(
                containsFile(packageToUploadToArchive, requestBody.getPackagesToUploadToArchive()),
                "There is a missing package to upload to Archive.");
        assertEquals(
                latestPackagesFile.getAbsolutePath(),
                requestBody.getPackagesFile().getAbsolutePath(),
                "Incorrect latest PACKAGES file in the request body");
        assertEquals(
                archivePackagesFile.getAbsolutePath(),
                requestBody.getPackagesFileArchive().getAbsolutePath(),
                "Incorrect archive PACKAGES file in the request body.");
        assertTrue(
                requestBody.getPackagesToDelete().contains("Benchmarking_0.10.tar.gz"),
                "Old package should be selected for deletion.");
        assertTrue(
                requestBody.getPackagesToDeleteFromArchive().isEmpty(),
                "No packages should be selected for deletion from Archive.");
    }

    private boolean containsFile(File file, List<File> files) {
        for (File f : files) {
            if (f.getAbsolutePath().equals(file.getAbsolutePath())) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void generateInstallablePackagesFile() throws Exception {
        final String benchmarkingName = "bea.R";
        final String benchmarkingLocation = "/repositories/2/89565416/";
        final File benchmarkingFolder = new File(packageUploadDirectory + benchmarkingLocation + benchmarkingName);
        final Properties packageProperties = storage.getPropertiesFromExtractedFile(benchmarkingFolder.getAbsolutePath());
        final String benchmarkingFileName = benchmarkingName + "_1.0.5.tar.gz";
        final File benchmarkingFile = new File(packageUploadDirectory + benchmarkingLocation + benchmarkingFileName);
        final FileInputStream fis = new FileInputStream(benchmarkingFile);
        final byte[] packageBytes = fis.readAllBytes();
        fis.close();
        MultipartFile multipartFile = new MockMultipartFile(benchmarkingFileName, benchmarkingFileName, "", packageBytes);
        RPackageUploadRequest request =
                new RPackageUploadRequest(multipartFile, RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY(), false, true, false, null, null, null);
        final RPackageService rPackageService = mock(RPackageService.class);
        doAnswer(invocation -> invocation.getArgument(0)).when(rPackageService).create(any());
        final RPackageUploadStrategy rPackageUploadStrategy = new RPackageUploadStrategy(
                request,
                UserTestFixture.GET_PACKAGE_MAINTAINER(),
                mock(NewsfeedEventService.class),
                mock(SubmissionService.class),
                mock(RPackageValidator.class),
                mock(RRepositoryService.class),
                storage,
                rPackageService,
                mock(EmailService.class),
                mock(BestMaintainerChooser.class),
                mock(RRepositorySynchronizer.class),
                mock(SecurityMediator.class),
                storage,
                mock(RPackageDeleter.class),
                request
        );
        final RPackage benchmarkingPackage = ReflectionTestUtils.invokeMethod(rPackageUploadStrategy, "createPackage", benchmarkingName, benchmarkingFile, packageProperties);
        final String packageString = ReflectionTestUtils.invokeMethod(storage, "generatePackageString", benchmarkingPackage);
        assertEquals("""
                Package: bea.R
                Version: 1.0.5
                Depends: R (>= 3.2.1), data.table
                Imports: httr, DT, shiny, jsonlite, googleVis, shinydashboard, ggplot2,
                 stringr, chron, gtable, scales, htmltools, httpuv, xtable,
                 stringi, magrittr, htmlwidgets, Rcpp, munsell, colorspace,
                 plyr, yaml
                License: CC0
                MD5Sum: 5e664f320c7cc884138d64467f6b0e49
                NeedsCompilation: no
                
                """, packageString);
    }

    @Test
    public void organizePackagesInStorage() throws Exception {
        final String datestamp = "20240111";
        final User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(2);
        repository.setPublished(true);

        final RPackage benchmarkingArchivePackage = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        benchmarkingArchivePackage.setSource(
                new File(packageUploadDirectory + "/repositories/2/71228208/Benchmarking_0.10.tar.gz")
                        .getAbsolutePath());
        benchmarkingArchivePackage.setMd5sum("9a99c2ebefa6d49422ca7893c1f4ead8");
        benchmarkingArchivePackage.setName("Benchmarking");
        benchmarkingArchivePackage.setVersion("0.10");
        benchmarkingArchivePackage.setActive(true);

        final RPackage benchmarkingPackage = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        benchmarkingPackage.setSource(
                new File(packageUploadDirectory + "/repositories/2/79878978/Benchmarking_0.31.tar.gz")
                        .getAbsolutePath());
        benchmarkingPackage.setName("Benchmarking");
        benchmarkingPackage.setVersion("0.31");
        benchmarkingPackage.setActive(true);
        benchmarkingPackage.setMd5sum("136460e58c711ed9cc1cdbdbde2e5b86");

        final RPackage beaR = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        beaR.setSource(
                new File(packageUploadDirectory + "/repositories/2/89565416/bea.R_1.0.5.tar.gz").getAbsolutePath());
        beaR.setName("bea.R");
        beaR.setVersion("1.0.5");
        beaR.setActive(true);
        beaR.setMd5sum("5e664f320c7cc884138d64467f6b0e49");

        List<RPackage> packages = List.of(benchmarkingArchivePackage, benchmarkingPackage, beaR);
        List<RPackage> archivePackages = List.of(benchmarkingArchivePackage);
        List<RPackage> latestPackages = List.of(benchmarkingPackage, beaR);

        storage.organizePackagesInStorage(datestamp, packages, latestPackages, archivePackages, repository);

        final File currentDatestampGeneratedDirectory = new File(repositoryGenerationDirectory + "/2/20240111");
        final File currentGeneratedDirectory = new File(repositoryGenerationDirectory + "/2/current");
        final File actualLatestPackagesFile =
                new File(repositoryGenerationDirectory + "/2/20240111/src/contrib/PACKAGES");
        final File actualArchivePackagesFile =
                new File(repositoryGenerationDirectory + "/2/20240111/src/contrib/Archive/PACKAGES");
        final String actualLatestPackagesFileContent = Files.readString(actualLatestPackagesFile.toPath());
        final String actualArchivePackagesFileContent = Files.readString(actualArchivePackagesFile.toPath());

        final String expectedLatestPackagesFileContent = getExpectedLatestPackagesFile();
        final String expectedArchivePackagesFileContent = getExpectedArchivePackagesFile();

        verifyPackage(currentGeneratedDirectory, "Benchmarking_0.10.tar.gz", true);
        verifyPackage(currentGeneratedDirectory, "Benchmarking_0.31.tar.gz", false);
        verifyPackage(currentGeneratedDirectory, "bea.R_1.0.5.tar.gz", false);
        assertEquals(
                expectedLatestPackagesFileContent, actualLatestPackagesFileContent, "Incorrect latest PACKAGES file");
        assertEquals(
                expectedArchivePackagesFileContent,
                actualArchivePackagesFileContent,
                "Incorrect archive PACKAGES file");
        assertTrue(currentDatestampGeneratedDirectory.isDirectory(), "Directory was not generated.");
        assertTrue(Files.isSymbolicLink(currentGeneratedDirectory.toPath()), "current should be a symlink");
        assertEquals(
                currentDatestampGeneratedDirectory.getAbsolutePath(),
                currentGeneratedDirectory.toPath().toRealPath().toFile().getAbsolutePath(),
                "Symlink does not point at a right dir.");
    }

    private String getExpectedArchivePackagesFile() {
        return "Package: Benchmarking\n"
                + "Version: 0.10\n"
                + "License: Some license1\n"
                + "MD5Sum: 9a99c2ebefa6d49422ca7893c1f4ead8\n"
                + "NeedsCompilation: no\n"
                + "\n"
                + "";
    }

    private String getExpectedLatestPackagesFile() {
        return "Package: Benchmarking\n"
                + "Version: 0.10\n"
                + "License: Some license1\n"
                + "MD5Sum: 9a99c2ebefa6d49422ca7893c1f4ead8\n"
                + "NeedsCompilation: no\n"
                + "\n"
                + "Package: Benchmarking\n"
                + "Version: 0.31\n"
                + "License: Some license1\n"
                + "MD5Sum: 136460e58c711ed9cc1cdbdbde2e5b86\n"
                + "NeedsCompilation: no\n"
                + "\n"
                + "Package: bea.R\n"
                + "Version: 1.0.5\n"
                + "License: Some license1\n"
                + "MD5Sum: 5e664f320c7cc884138d64467f6b0e49\n"
                + "NeedsCompilation: no\n"
                + "\n"
                + "";
    }

    private void verifyPackage(File currentGeneratedDirectory, String filename, boolean archive) {
        final File packageFile =
                new File(currentGeneratedDirectory + "/src/contrib" + (archive ? "/Archive/" : "/") + filename);
        assertTrue(packageFile.exists(), "Package file " + packageFile.getAbsolutePath() + " does not exist.");
    }

    @Test
    public void moveToMainDirectory() throws Exception {
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(2);
        final User user = UserTestFixture.GET_PACKAGE_MAINTAINER();
        final File packageFileToMove =
                new File("src/test/resources/unit/storage_tests/rdepot/new/51328701/abc_1.0.tar.gz");
        final byte[] fileContent = Files.readAllBytes(packageFileToMove.toPath());
        final RPackage packageToMove = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        packageToMove.setSource(packageFileToMove.getAbsolutePath());
        packageToMove.setName("abc");
        packageToMove.setVersion("1.0");
        packageToMove.setActive(true);
        packageToMove.setDeleted(false);

        final String newPackagePath = storage.moveToMainDirectory(packageToMove);
        final File actualFile = new File(newPackagePath);
        String[] tokens = newPackagePath.split("/");
        int tl = tokens.length;
        String fileName = tokens[tl - 1];
        Integer randomNumber = Integer.parseInt(tokens[tl - 2]);
        String repositoryId = tokens[tl - 3];
        String repositoriesDir = tokens[tl - 4];

        assertTrue(
                newPackagePath.startsWith(packageUploadDirectory.getAbsolutePath()),
                "File was not uploaded to Package Upload Directory!");
        assertTrue(randomNumber > -1, "Incorrect random waiting room number.");
        assertEquals("repositories", repositoriesDir, "Repositories dir should be called \"repositories\".");
        assertEquals("2", repositoryId, "Incorrect repository id in path.");
        assertEquals("abc_1.0.tar.gz", fileName, "Incorrect file name.");
        assertTrue(Files.exists(actualFile.toPath()), "File does not exist in correct directory");
        assertArrayEquals(fileContent, Files.readAllBytes(actualFile.toPath()), "File content is incorrect.");
    }

    @Test
    public void removesGenerationDirectoryContent() throws Exception {
        ReflectionTestUtils.setField(storage, RLocalStorage.class, "snapshot", "false", String.class);
        final PopulatedRepositoryContent populatedContent = new PopulatedRepositoryContent(
                List.of(),
                List.of(),
                repositoryGenerationDirectory.getAbsolutePath() + "/2/20240110/src/contrib",
                repositoryGenerationDirectory.getAbsolutePath() + "/2/20240110/src/contrib/Archive");
        storage.cleanUpAfterSynchronization(populatedContent);

        assertTrue(
                repositoryGenerationDirectory.exists() && repositoryGenerationDirectory.isDirectory(),
                "Repository generation directory should not be deleted!");
        assertTrue(
                repositoryGenerationDirectory.listFiles().length == 0,
                "Generation directory should be empty when shapshots are off.");
    }

    @Test
    public void writeToWaitingRoom() throws Exception {
        final File newPackage = new File("src/test/resources/unit/test_packages/accrued_1.2.tar.gz");
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(2);
        repository.setPublished(false);
        final byte[] fileContent = Files.readAllBytes(newPackage.toPath());
        final MultipartFile multipart = new MockMultipartFile(
                "accrued_1.2.tar.gz", "accrued_1.2.tar.gz", ContentType.MULTIPART_FORM_DATA.toString(), fileContent);

        final String pathToWaitingRoom = storage.writeToWaitingRoom(multipart, repository);
        final File actualFile = new File(pathToWaitingRoom);
        final byte[] actualContent = Files.readAllBytes(actualFile.toPath());

        final String[] tokens = pathToWaitingRoom.split("/");
        final int tl = tokens.length;
        final String fileName = tokens[tl - 1];
        final Integer randomNumber = Integer.parseInt(tokens[tl - 2]);
        final String waitingRoomFolderName = tokens[tl - 3];

        assertTrue(
                pathToWaitingRoom.startsWith(packageUploadDirectory.getAbsolutePath()),
                "File was not uploaded to Package Upload Directory!");
        assertEquals("accrued_1.2.tar.gz", fileName, "Incorrect file name.");
        assertTrue(randomNumber > -1, "Incorrect random waiting room number.");
        assertEquals("new", waitingRoomFolderName, "Waiting Room should be called \"new\".");
        assertTrue(Files.exists(actualFile.toPath()), "File does not exist in correct directory");
        assertArrayEquals(fileContent, actualContent, "File content is incorrect.");
    }

    @Test
    public void moveToTrashDirectory() throws Exception {
        final File packageFile = new File(
                "src/test/resources/unit/storage_tests/rdepot" + "/repositories/2/89565416/bea.R_1.0.5.tar.gz");
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        final byte[] fileContent = Files.readAllBytes(packageFile.toPath());
        repository.setId(2);
        repository.setPublished(false);
        final User user = UserTestFixture.GET_PACKAGE_MAINTAINER();
        final RPackage packageBag = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        packageBag.setSource(packageFile.getAbsolutePath());
        packageBag.setName("bea.R");
        packageBag.setVersion("1.0.5");
        packageBag.setActive(true);
        packageBag.setDeleted(false);

        final String newPackagePath = storage.moveToTrashDirectory(packageBag);
        final File actualFile = new File(newPackagePath);
        final File extractedDir = new File(actualFile.getParentFile().getAbsoluteFile() + "/bea.R");

        System.out.println("Path: " + newPackagePath);
        String[] tokens = newPackagePath.split("/");
        int tl = tokens.length;
        String fileName = tokens[tl - 1];
        Integer randomNumber = Integer.parseInt(tokens[tl - 2]);
        String repositoryId = tokens[tl - 3];
        String trashDir = tokens[tl - 4];

        assertTrue(
                newPackagePath.startsWith(packageUploadDirectory.getAbsolutePath()),
                "File was not uploaded to Package Upload Directory!");
        assertTrue(randomNumber > -1, "Incorrect random waiting room number.");
        assertEquals("trash", trashDir, "Repositories dir should be called \"repositories\".");
        assertEquals("2", repositoryId, "Incorrect repository id in path.");
        assertEquals("bea.R_1.0.5.tar.gz", fileName, "Incorrect file name.");
        assertTrue(Files.exists(actualFile.toPath()), "File does not exist in correct directory");
        assertArrayEquals(fileContent, Files.readAllBytes(actualFile.toPath()), "File content is incorrect.");
        assertTrue(
                extractedDir.isDirectory() && extractedDir.getAbsolutePath().endsWith("bea.R"),
                "Package extracted content was not put into trash properly.");
    }

    @Test
    public void removePackageSource() throws Exception {
        final File packageFile = new File(
                "src/test/resources/unit/storage_tests/rdepot" + "/repositories/2/89565416/bea.R_1.0.5.tar.gz");
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(2);
        repository.setPublished(false);
        final User user = UserTestFixture.GET_PACKAGE_MAINTAINER();
        final RPackage packageBag = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        packageBag.setSource(packageFile.getAbsolutePath());
        packageBag.setName("bea.R");
        packageBag.setVersion("1.0.5");
        packageBag.setActive(true);
        packageBag.setDeleted(false);

        final String newPackagePath = storage.moveToTrashDirectory(packageBag);
        final File actualFile = new File(newPackagePath);
        final File extractedDir = new File(actualFile.getParentFile().getAbsoluteFile() + "/bea.R");

        storage.removePackageSource(newPackagePath);

        assertFalse(actualFile.exists(), "Package source file has not been removed");
        assertFalse(extractedDir.exists(), "Extracted source has not been removed");
    }
}

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
package eu.openanalytics.rdepot.python.test.storage;

import static org.junit.jupiter.api.Assertions.*;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.storage.implementations.CommonLocalStorage;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.storage.implementations.PythonLocalStorage;
import eu.openanalytics.rdepot.python.storage.indexes.PackageIndexGenerator;
import eu.openanalytics.rdepot.python.storage.indexes.RepositoryIndexGenerator;
import eu.openanalytics.rdepot.python.storage.utils.PopulatedRepositoryContent;
import eu.openanalytics.rdepot.python.synchronization.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.test.fixture.PythonPackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.PythonRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.UnitTest;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import org.apache.http.entity.ContentType;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

public class PythonLocalStorageTest extends UnitTest {

    private static final File testResourcesHome = new File("src/test/resources/unit/storage_tests");
    private static final File packageUploadDirectory = new File("src/test/resources/unit/storage_tests/rdepot");
    private static final File repositoryGenerationDirectory =
            new File("src/test/resources/unit/storage_tests/rdepot/generated");

    private final ResourceLoader resourceLoader = new DefaultResourceLoader();
    private final Resource resource = resourceLoader.getResource("classpath:templates/index_template.html");
    private final PythonLocalStorage storage =
            new PythonLocalStorage(new PackageIndexGenerator(resource), new RepositoryIndexGenerator(resource));

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
                storage, PythonLocalStorage.class, "packageUploadDirectory", packageUploadDirectory, File.class);
        ReflectionTestUtils.setField(
                storage,
                PythonLocalStorage.class,
                "repositoryGenerationDirectory",
                repositoryGenerationDirectory,
                File.class);
        ReflectionTestUtils.setField(storage, PythonLocalStorage.class, "snapshot", "true", String.class);
    }

    @AfterEach
    public void cleanUp() throws Exception {
        deleteTestFilesIfExist();
    }

    private void restore() throws Exception {
        deleteTestFilesIfExist();
        executeBashCommand(
                "tar",
                "-xzf",
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
    public void buildSynchronizeRequestBody_addNewPackages() throws Exception {
        final String datestamp = "20240228";
        final User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        final PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(1);
        repository.setPublished(true);
        repository.setVersion(5);

        final PythonPackage pandasRemotePackage = PythonPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        pandasRemotePackage.setSource(
                new File(packageUploadDirectory + "/repositories/1/86085553/pandas-2.0.1.tar.gz").getAbsolutePath());
        pandasRemotePackage.setName("pandas");
        pandasRemotePackage.setVersion("2.0.1");
        pandasRemotePackage.setHash("7053d7ff8c563324b9a76110fabbd227c96c11d337521a57d94973bbb5f2a7ad");
        pandasRemotePackage.setActive(true);

        final PythonPackage pandasPackage = PythonPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        pandasPackage.setSource(
                new File(packageUploadDirectory + "/repositories/1/87108158/pandas-2.0.3.tar.gz").getAbsolutePath());
        pandasPackage.setName("pandas");
        pandasPackage.setVersion("2.0.3");
        pandasPackage.setHash("c02f372a88e0d17f36d3093a644c73cfc1788e876a7c4bcb4020a77512e2043c");
        pandasPackage.setActive(true);

        final PythonPackage cryptographyPackage = PythonPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        cryptographyPackage.setSource(
                new File(packageUploadDirectory + "/repositories/1/36342644/cryptography-41.0.1.tar.gz")
                        .getAbsolutePath());
        cryptographyPackage.setName("cryptography");
        cryptographyPackage.setVersion("41.0.1");
        cryptographyPackage.setHash("4a0740db3e223fcd38a6ad062cdae927429a0894132e643d613d43b647bf488a");
        cryptographyPackage.setActive(true);

        List<PythonPackage> packages = List.of(pandasRemotePackage, pandasPackage, cryptographyPackage);

        final PopulatedRepositoryContent content = storage.organizePackagesInStorage(datestamp, packages, repository);

        List<String> remotePackages = List.of("pandas/pandas-2.0.1.tar.gz");
        int versionBefore = 1;

        final SynchronizeRepositoryRequestBody requestBody =
                storage.buildSynchronizeRequestBody(content, remotePackages, repository, versionBefore + "");

        final File packageToUpload1 =
                new File(repositoryGenerationDirectory + "/1/20240228/pandas/pandas-2.0.3.tar.gz");
        final File packageIndexFileToUpload1 =
                new File(repositoryGenerationDirectory + "/1/20240228/pandas/index.html");
        final File packageToUpload2 =
                new File(repositoryGenerationDirectory + "/1/20240228/cryptography/cryptography-41.0.1.tar.gz");
        final File packageIndexFileToUpload2 =
                new File(repositoryGenerationDirectory + "/1/20240228/cryptography/index.html");
        final File repositoryIndexFileToUpload = new File(repositoryGenerationDirectory + "/1/20240228/index.html");

        assertEquals(0, requestBody.getFilesToDelete().size(), "There should one package to delete.");
        assertEquals(5, requestBody.getFilesToUpload().size(), "Too many packages to upload.");
        assertTrue(
                containsFile(packageToUpload1, requestBody.getFilesToUpload()),
                "There is a missing package to upload (1st).");
        assertTrue(
                containsFile(packageToUpload2, requestBody.getFilesToUpload()),
                "There is a missing package to upload (2nd).");
        assertTrue(
                containsFile(packageIndexFileToUpload1, requestBody.getFilesToUpload()),
                "There is a missing package index file to upload (1st).");
        assertTrue(
                containsFile(packageIndexFileToUpload2, requestBody.getFilesToUpload()),
                "There is a missing package index file to upload (2nd).");
        assertTrue(
                containsFile(repositoryIndexFileToUpload, requestBody.getFilesToUpload()),
                "There is a missing repository index file to upload.");
    }

    @Test
    public void buildSynchronizeRequestBody_removeWholePackageFolder() throws Exception {
        final String datestamp = "20240405";
        final User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        final PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(2);
        repository.setPublished(true);
        repository.setVersion(5);

        final PythonPackage pandasPackage1 = PythonPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        pandasPackage1.setSource(
                new File(packageUploadDirectory + "/repositories/2/86085553/pandas-2.0.1.tar.gz").getAbsolutePath());
        pandasPackage1.setName("pandas");
        pandasPackage1.setVersion("2.0.1");
        pandasPackage1.setHash("7053d7ff8c563324b9a76110fabbd227c96c11d337521a57d94973bbb5f2a7ad");
        pandasPackage1.setActive(true);

        final PythonPackage pandasPackage2 = PythonPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        pandasPackage2.setSource(
                new File(packageUploadDirectory + "/repositories/2/87108158/pandas-2.0.3.tar.gz").getAbsolutePath());
        pandasPackage2.setName("pandas");
        pandasPackage2.setVersion("2.0.3");
        pandasPackage2.setHash("c02f372a88e0d17f36d3093a644c73cfc1788e876a7c4bcb4020a77512e2043c");
        pandasPackage2.setActive(true);

        List<PythonPackage> packages = List.of(pandasPackage1, pandasPackage2);

        final PopulatedRepositoryContent content = storage.organizePackagesInStorage(datestamp, packages, repository);

        List<String> remotePackages = List.of(
                "pandas/pandas-2.0.1.tar.gz", "pandas/pandas-2.0.3.tar.gz", "cryptography/cryptography-41.0.1.tar.gz");

        int versionBefore = 1;

        final SynchronizeRepositoryRequestBody requestBody =
                storage.buildSynchronizeRequestBody(content, remotePackages, repository, versionBefore + "");

        final String packageToDelete = "cryptography/cryptography-41.0.1.tar.gz";
        final File repositoryIndexFileToUpload = new File(repositoryGenerationDirectory + "/2/20240405/index.html");

        assertEquals(1, requestBody.getFilesToDelete().size(), "There should one package to delete.");
        assertEquals(1, requestBody.getFilesToUpload().size(), "Too many packages to upload.");
        assertTrue(
                containsFile(repositoryIndexFileToUpload, requestBody.getFilesToUpload()),
                "There is a missing repository index file to upload.");
        assertTrue(requestBody.getFilesToDelete().contains(packageToDelete), "There is a missing file to delete.");
    }

    @Test
    public void buildSynchronizeRequestBody_removeVersionOfPackage() throws Exception {
        final String datestamp = "20240405";
        final User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        final PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(3);
        repository.setPublished(true);
        repository.setVersion(5);

        final PythonPackage pandasPackage = PythonPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        pandasPackage.setSource(
                new File(packageUploadDirectory + "/repositories/3/87108158/pandas-2.0.3.tar.gz").getAbsolutePath());
        pandasPackage.setName("pandas");
        pandasPackage.setVersion("2.0.3");
        pandasPackage.setHash("c02f372a88e0d17f36d3093a644c73cfc1788e876a7c4bcb4020a77512e2043c");
        pandasPackage.setActive(true);

        final PythonPackage cryptographyPackage = PythonPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        cryptographyPackage.setSource(
                new File(packageUploadDirectory + "/repositories/3/36342644/cryptography-41.0.1.tar.gz")
                        .getAbsolutePath());
        cryptographyPackage.setName("cryptography");
        cryptographyPackage.setVersion("41.0.1");
        cryptographyPackage.setHash("4a0740db3e223fcd38a6ad062cdae927429a0894132e643d613d43b647bf488a");
        cryptographyPackage.setActive(true);

        List<PythonPackage> packages = List.of(pandasPackage, cryptographyPackage);

        final PopulatedRepositoryContent content = storage.organizePackagesInStorage(datestamp, packages, repository);

        List<String> remotePackages = List.of(
                "pandas/pandas-2.0.1.tar.gz", "pandas/pandas-2.0.3.tar.gz", "cryptography/cryptography-41.0.1.tar.gz");

        int versionBefore = 1;

        final SynchronizeRepositoryRequestBody requestBody =
                storage.buildSynchronizeRequestBody(content, remotePackages, repository, versionBefore + "");

        final String packageToDelete = "pandas/pandas-2.0.1.tar.gz";
        final File packageIndexFileToUpload = new File(repositoryGenerationDirectory + "/3/20240405/pandas/index.html");

        assertEquals(1, requestBody.getFilesToDelete().size(), "There should one package to delete.");
        assertEquals(1, requestBody.getFilesToUpload().size(), "Too many packages to upload.");
        assertTrue(requestBody.getFilesToDelete().contains(packageToDelete), "There is a missing file to delete.");
        assertTrue(
                containsFile(packageIndexFileToUpload, requestBody.getFilesToUpload()),
                "There is a missing package index file to upload.");
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
    public void organizePackagesInStorage() throws Exception {
        final String datestamp = "20240228";
        final User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        final PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(1);
        repository.setPublished(true);

        final PythonPackage pandasRemotePackage = PythonPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        pandasRemotePackage.setSource(
                new File(packageUploadDirectory + "/repositories/1/86085553/pandas-2.0.1.tar.gz").getAbsolutePath());
        pandasRemotePackage.setName("pandas");
        pandasRemotePackage.setVersion("2.0.1");
        pandasRemotePackage.setHash("7053d7ff8c563324b9a76110fabbd227c96c11d337521a57d94973bbb5f2a7ad");
        pandasRemotePackage.setActive(true);
        pandasRemotePackage.setRequiresPython(">=3.8");

        final PythonPackage pandasPackage = PythonPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        pandasPackage.setSource(
                new File(packageUploadDirectory + "/repositories/1/87108158/pandas-2.0.3.tar.gz").getAbsolutePath());
        pandasPackage.setName("pandas");
        pandasPackage.setVersion("2.0.3");
        pandasPackage.setHash("c02f372a88e0d17f36d3093a644c73cfc1788e876a7c4bcb4020a77512e2043c");
        pandasPackage.setActive(true);
        pandasPackage.setRequiresPython(">=3.8");

        final PythonPackage cryptographyPackage = PythonPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        cryptographyPackage.setSource(
                new File(packageUploadDirectory + "/repositories/1/36342644/cryptography-41.0.1.tar.gz")
                        .getAbsolutePath());
        cryptographyPackage.setName("cryptography");
        cryptographyPackage.setVersion("41.0.1");
        cryptographyPackage.setHash("4a0740db3e223fcd38a6ad062cdae927429a0894132e643d613d43b647bf488a");
        cryptographyPackage.setActive(true);
        cryptographyPackage.setRequiresPython(">=3.7");

        List<PythonPackage> packages = List.of(pandasRemotePackage, pandasPackage, cryptographyPackage);

        storage.organizePackagesInStorage(datestamp, packages, repository);

        final File currentDatestampGeneratedDirectory = new File(repositoryGenerationDirectory + "/1/20240228");
        final File currentGeneratedDirectory = new File(repositoryGenerationDirectory + "/1/current");

        final File actualRepositoryIndexFile = new File(repositoryGenerationDirectory + "/1/20240228/index.html");
        final File actualPandasIndexFile = new File(repositoryGenerationDirectory + "/1/20240228/pandas/index.html");
        final File actualCryptographyIndexFile =
                new File(repositoryGenerationDirectory + "/1/20240228/cryptography/index.html");

        final String actualRepositoryIndexFileContent =
                Files.readString(actualRepositoryIndexFile.toPath()).replaceAll("\\s+", " ");
        final String actualPandasIndexFileContent =
                Files.readString(actualPandasIndexFile.toPath()).replaceAll("\\s+", " ");
        final String actualCryptographyIndexFileContent =
                Files.readString(actualCryptographyIndexFile.toPath()).replaceAll("\\s+", " ");

        final String expectedRepositoryIndexFileContent =
                getExpectedRepositoryIndexFileContent().replaceAll("\\s+", " ");
        final String expectedPandasIndexFileContent =
                getExpectedPandasIndexFileContent().replaceAll("\\s+", " ");
        final String expectedCryptographyIndexFileContent =
                getExpectedCryptographyIndexFileContent().replaceAll("\\s+", " ");

        verifyPackage(currentGeneratedDirectory, "pandas", "pandas-2.0.1.tar.gz");
        verifyPackage(currentGeneratedDirectory, "pandas", "pandas-2.0.3.tar.gz");
        verifyPackage(currentGeneratedDirectory, "cryptography", "cryptography-41.0.1.tar.gz");
        assertEquals(
                expectedRepositoryIndexFileContent,
                actualRepositoryIndexFileContent,
                "Incorrect latest repository index file");
        assertEquals(expectedPandasIndexFileContent, actualPandasIndexFileContent, "Incorrect pandas index file");
        assertEquals(
                expectedCryptographyIndexFileContent,
                actualCryptographyIndexFileContent,
                "Incorrect cryptography index file");
        assertTrue(currentDatestampGeneratedDirectory.isDirectory(), "Directory was not generated");
        assertTrue(Files.isSymbolicLink(currentGeneratedDirectory.toPath()), "current should be a symlink");
        assertEquals(
                currentDatestampGeneratedDirectory.getAbsolutePath(),
                currentGeneratedDirectory.toPath().toRealPath().toFile().getAbsolutePath(),
                "Symlink does not point at a right dir.");
    }

    private String getExpectedRepositoryIndexFileContent() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <meta name="Test Python Repository:repository-version" content="10">
                    <title>Test Python Repository</title>
                </head>
                <body>
                  <h1></h1>
                  <a href="/repo/testrepo/pandas">pandas</a>\s
                  <a href="/repo/testrepo/cryptography">cryptography</a>\s
                </body>
                </html>
                """;
    }

    private String getExpectedPandasIndexFileContent() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <meta name="Test Python Repository:repository-version" content="10">
                    <title>Links for pandas</title>
                </head>
                <body>
                  <h1>Links for pandas</h1>
                  <a href="/repo/testrepo/pandas/pandas-2.0.1.tar.gz" data-requires-python=">=3.8" data-version="2.0.1">pandas-2.0.1.tar.gz</a><br>
                  <a href="/repo/testrepo/pandas/pandas-2.0.3.tar.gz" data-requires-python=">=3.8" data-version="2.0.3">pandas-2.0.3.tar.gz</a><br>
                </body>
                </html>
                """;
    }

    private String getExpectedCryptographyIndexFileContent() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <meta name="Test Python Repository:repository-version" content="10">
                    <title>Links for cryptography</title>
                </head>
                <body>
                  <h1>Links for cryptography</h1>
                  <a href="/repo/testrepo/cryptography/cryptography-41.0.1.tar.gz" data-requires-python=">=3.7" data-version="41.0.1">cryptography-41.0.1.tar.gz</a><br>
                </body>
                </html>
                """;
    }

    private void verifyPackage(File currentGeneratedDirectory, String packageName, String filename) {
        final File packageFile = new File(currentGeneratedDirectory + "/" + packageName + "/" + filename);
        assertTrue(packageFile.exists(), "Package file " + packageFile.getAbsolutePath() + " does not exists.");
    }

    @Test
    public void moveToMainDirectory() throws Exception {
        final PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(2);
        final User user = UserTestFixture.GET_PACKAGE_MAINTAINER();
        final File packageFileToMove = new File(packageUploadDirectory + "/new/98575792/coconutpy-2.2.1.tar.gz");

        final byte[] fileContent = Files.readAllBytes(packageFileToMove.toPath());
        final PythonPackage packageToMove = PythonPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        packageToMove.setSource(packageFileToMove.getAbsolutePath());
        packageToMove.setName("coconutpy");
        packageToMove.setVersion("2.2.1");
        packageToMove.setActive(true);
        packageToMove.setDeleted(false);

        final String newPackagePath = storage.moveToMainDirectory(packageToMove);
        final File actualFile = new File(newPackagePath);
        String[] tokens = newPackagePath.split("/");
        int tl = tokens.length;
        String filename = tokens[tl - 1];
        int randomNumber = Integer.parseInt(tokens[tl - 2]);
        String repositoryId = tokens[tl - 3];
        String repositoriesDir = tokens[tl - 4];

        assertTrue(
                newPackagePath.startsWith(packageUploadDirectory.getAbsolutePath()),
                "File was not uploaded to Package Upload Directory!");
        assertTrue(randomNumber > -1, "Incorrect random waiting room number.");
        assertEquals("repositories", repositoriesDir, "Repositories dir should be called \\\"repositories\\\".");
        assertEquals("2", repositoryId, "Incorrect repository id in path.");
        assertEquals("coconutpy-2.2.1.tar.gz", filename, "Incorrect file name.");
        assertTrue(Files.exists(actualFile.toPath()), "File does not exist in correct directory");
        assertArrayEquals(fileContent, Files.readAllBytes(actualFile.toPath()), "File content is incorrect.");
    }

    @Test
    public void removesGenerationDirectoryContent() throws Exception {
        ReflectionTestUtils.setField(storage, PythonLocalStorage.class, "snapshot", "false", String.class);
        final PopulatedRepositoryContent populatedContent = new PopulatedRepositoryContent(
                List.of(), repositoryGenerationDirectory.getAbsolutePath() + "/1/20240228");
        storage.cleanUpAfterSynchronization(populatedContent);

        assertTrue(
                repositoryGenerationDirectory.exists() && repositoryGenerationDirectory.isDirectory(),
                "Repository generation directory should not be deleted!");
        assertEquals(
                0,
                Objects.requireNonNull(repositoryGenerationDirectory.listFiles()).length,
                "Generation directory should be empty when snapshots are off.");
    }

    @Test
    public void writeToWaitingRoom() throws Exception {
        final File newPackage = new File("src/test/resources/unit/test_packages/testrepo1/boto3/boto3-1.26.156.tar.gz");
        final PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(2);
        repository.setPublished(false);
        final byte[] fileContent = Files.readAllBytes(newPackage.toPath());
        final MultipartFile multipart = new MockMultipartFile(
                "boto3-1.26.156.tar.gz",
                "boto3-1.26.156.tar.gz",
                ContentType.MULTIPART_FORM_DATA.toString(),
                fileContent);

        final String pathToWaitingRoom = storage.writeToWaitingRoom(multipart, repository);
        final File actualFile = new File(pathToWaitingRoom);
        final byte[] actualContent = Files.readAllBytes(actualFile.toPath());

        final String[] tokens = pathToWaitingRoom.split("/");
        final int tl = tokens.length;
        final String fileName = tokens[tl - 1];
        final int randomNumber = Integer.parseInt(tokens[tl - 2]);
        final String waitingRoomFolderName = tokens[tl - 3];

        assertTrue(
                pathToWaitingRoom.startsWith(packageUploadDirectory.getAbsolutePath()),
                "File was not uploaded to Package Upload Directory!");
        assertEquals("boto3-1.26.156.tar.gz", fileName, "Incorrect file name");
        assertTrue(randomNumber > -1, "Incorrect random waiting room number.");
        assertEquals("new", waitingRoomFolderName, "Waiting Room should be called \"new\".");
        assertTrue(Files.exists(actualFile.toPath()), "File does not exist in correct directory");
        assertArrayEquals(fileContent, actualContent, "File content is incorrect.");
    }

    @Test
    public void moveToTrashDirectory() throws Exception {
        final File packageFile =
                new File(packageUploadDirectory + "/repositories/1/36342644/cryptography-41.0.1.tar.gz");
        final PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(2);
        repository.setPublished(false);
        final byte[] fileContent = Files.readAllBytes(packageFile.toPath());
        final User user = UserTestFixture.GET_PACKAGE_MAINTAINER();
        final PythonPackage packageBag = PythonPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        packageBag.setSource(packageFile.getAbsolutePath());
        packageBag.setName("cryptography");
        packageBag.setVersion("41.0.1");
        packageBag.setActive(true);
        packageBag.setDeleted(false);

        final String newPackagePath = storage.moveToTrashDirectory(packageBag);
        final File actualFile = new File(newPackagePath);

        System.out.println("Path: " + newPackagePath);
        String[] tokens = newPackagePath.split("/");
        int tl = tokens.length;
        String fileName = tokens[tl - 1];
        int randomNumber = Integer.parseInt(tokens[tl - 2]);
        String repositoryId = tokens[tl - 3];
        String trashDir = tokens[tl - 4];

        assertTrue(
                newPackagePath.startsWith(packageUploadDirectory.getAbsolutePath()),
                "File was not uploaded to Package Upload Directory!");
        assertTrue(randomNumber > -1, "Incorrect random waiting room number.");
        assertEquals("trash", trashDir, "Repositories dir should be called \"repositories\".");
        assertEquals("2", repositoryId, "Incorrect repository id in path.");
        assertEquals("cryptography-41.0.1.tar.gz", fileName, "Incorrect file name.");
        assertTrue(Files.exists(actualFile.toPath()), "File does not exist in correct directory");
        assertArrayEquals(fileContent, Files.readAllBytes(actualFile.toPath()), "File content is incorrect.");
    }
}

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
package eu.openanalytics.rdepot.repo.test.r.storage;

import static org.junit.jupiter.api.Assertions.*;

import eu.openanalytics.rdepot.repo.r.model.SynchronizeCranRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.r.storage.implementations.CranFileSystemStorageService;
import eu.openanalytics.rdepot.repo.storage.StorageProperties;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class CranStorageTest {

    @TempDir
    public Path tempDir;

    private static final String TEST_PACKAGES_DIR = "src/test/resources/eu/openanalytics/rdepot/repo/testpackages/";
    private static final String TEST_BINARY_PACKAGES_RECENT_DIR_LOWER_VERSION = "binary_packages/recent_4_2";
    private static final String TEST_BINARY_PACKAGES_RECENT_DIR_HIGHER_VERSION = "binary_packages/recent_4_5";
    private static final String TEST_BINARY_PACKAGES_ARCHIVE_DIR = "binary_packages/archive_4_2";
    private static final String EXPECTED_PACKAGES_DIR =
            "src/test/resources/eu/openanalytics/rdepot/repo/testpackages/expected_packages";
    private static final String SOURCE_PATH = "target/generation/folder/src/contrib";
    private static final String BINARY_PATH = "target/generation/folder/bin/linux/centos7/x86_64/4.2";
    private static final String BINARY_HIGHER_VERSION_PATH = "target/generation/folder/bin/linux/centos7/x86_64/4.5";

    private CranFileSystemStorageService storageService;

    private File testPackagesDir;

    private File expectedPackagesDir;

    private static final String TEST_REPO = "testrepo123";
    private static final String NON_EXISTING_REPO = "testrepo234";

    private MultipartFile[] getTestPackages(boolean archive, boolean expected) throws IOException {
        ArrayList<MultipartFile> files = new ArrayList<>();

        String subDir = archive ? "archive" : "recent";

        File packagesDir = expected ? expectedPackagesDir : testPackagesDir;

        for (File file : Objects.requireNonNull(
                packagesDir.toPath().resolve(subDir).toFile().listFiles())) {
            MultipartFile multipartFile =
                    new MockMultipartFile("files", file.getName(), null, Files.readAllBytes(file.toPath()));
            files.add(multipartFile);
        }

        if (archive) {
            for (File file : Objects.requireNonNull(packagesDir
                    .toPath()
                    .resolve(TEST_BINARY_PACKAGES_ARCHIVE_DIR)
                    .toFile()
                    .listFiles())) {
                MultipartFile multipartFile =
                        new MockMultipartFile("files", file.getName(), null, Files.readAllBytes(file.toPath()));
                files.add(multipartFile);
            }
        } else {
            for (File file : Objects.requireNonNull(packagesDir
                    .toPath()
                    .resolve(TEST_BINARY_PACKAGES_RECENT_DIR_LOWER_VERSION)
                    .toFile()
                    .listFiles())) {
                MultipartFile multipartFile =
                        new MockMultipartFile("files", file.getName(), null, Files.readAllBytes(file.toPath()));
                files.add(multipartFile);
            }

            for (File file : Objects.requireNonNull(packagesDir
                    .toPath()
                    .resolve(TEST_BINARY_PACKAGES_RECENT_DIR_HIGHER_VERSION)
                    .toFile()
                    .listFiles())) {
                MultipartFile multipartFile =
                        new MockMultipartFile("files", file.getName(), null, Files.readAllBytes(file.toPath()));
                files.add(multipartFile);
            }
        }

        MultipartFile[] testPackages = new MultipartFile[files.size()];
        files.toArray(testPackages);

        return testPackages;
    }

    private MultipartFile[] getRecentTestPackages() throws IOException {
        return getTestPackages(false, false);
    }

    private MultipartFile[] getArchiveTestPackages() throws IOException {
        return getTestPackages(true, false);
    }

    private MultipartFile[] getExpectedPackages(String expectedDirectory) throws IOException {
        ArrayList<MultipartFile> files = new ArrayList<>();

        for (File file : Objects.requireNonNull(
                expectedPackagesDir.toPath().resolve(expectedDirectory).toFile().listFiles())) {
            MultipartFile multipartFile =
                    new MockMultipartFile("files", file.getName(), null, Files.readAllBytes(file.toPath()));
            files.add(multipartFile);
        }

        MultipartFile[] testPackages = new MultipartFile[files.size()];
        files.toArray(testPackages);

        return testPackages;
    }

    private void assertFiles(MultipartFile[] expectedFiles, File actualDirectory) throws IOException {
        for (MultipartFile expectedFile : expectedFiles) {
            byte[] expectedBytes;
            byte[] actualBytes;

            expectedBytes = expectedFile.getBytes();

            Path actualDirectoryPath = actualDirectory.toPath();
            String fileName = expectedFile.getOriginalFilename();
            assertFalse(StringUtils.isBlank(fileName), "Filename should not be blank");
            actualDirectoryPath = actualDirectory.getName().equals("Archive") && !fileName.startsWith("PACKAGES")
                    ? actualDirectoryPath.resolve(fileName.split("_")[0])
                    : actualDirectoryPath;
            Path actual = actualDirectoryPath.resolve(fileName);
            actualBytes = Files.readAllBytes(actual);

            assertArrayEquals(actualBytes, expectedBytes, "Uploaded file is not correct");
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        assertTrue(Files.isDirectory(tempDir));
        Files.createDirectory(tempDir.resolve(TEST_REPO));

        final StorageProperties properties = new StorageProperties();
        properties.setLocation(tempDir.toString());

        this.storageService = new CranFileSystemStorageService(properties);
        this.testPackagesDir = new File(TEST_PACKAGES_DIR);
        if (!testPackagesDir.exists() || !testPackagesDir.isDirectory())
            throw new FileNotFoundException(testPackagesDir.getAbsolutePath());

        this.expectedPackagesDir = new File(EXPECTED_PACKAGES_DIR);
        if (!expectedPackagesDir.exists() || !expectedPackagesDir.isDirectory())
            throw new FileNotFoundException(expectedPackagesDir.getAbsolutePath());
    }

    @Test
    public void boostRepositoryVersion() throws Exception {
        final FileWriter fw = new FileWriter(
                tempDir.resolve(TEST_REPO).resolve("VERSION").toAbsolutePath().toString());
        fw.write("23");
        fw.close();

        storageService.boostRepositoryVersion(TEST_REPO);

        final File versionFile = tempDir.resolve(TEST_REPO).resolve("VERSION").toFile();
        final Scanner sc = new Scanner(versionFile);
        StringBuilder result = new StringBuilder();

        while (sc.hasNextLine()) {
            result.append(sc.nextLine());
        }
        sc.close();

        assertEquals("24", result.toString(), "version was not boosted properly");
    }

    @Test
    public void storePackages() throws Exception {
        String randomId = RandomStringUtils.randomAlphabetic(16);
        final MultipartFile[] recent = getRecentTestPackages();
        final MultipartFile[] archive = getArchiveTestPackages();

        final Map<String, String> pathsToUpload = new HashMap<>();
        final Map<String, String> pathsToUploadToArchive = new HashMap<>();

        pathsToUpload.put("12345_Benchmarking_0.10.tar.gz", SOURCE_PATH);
        pathsToUpload.put("12346_PACKAGES", SOURCE_PATH);
        pathsToUpload.put("12347_PACKAGES.gz", SOURCE_PATH);
        pathsToUpload.put("12348_usl_2.0.0.tar.gz", SOURCE_PATH);
        pathsToUploadToArchive.put("12349_abc_1.3.tar.gz", SOURCE_PATH);
        pathsToUploadToArchive.put("12340_accrued_1.2.tar.gz", SOURCE_PATH);
        pathsToUploadToArchive.put("23456_accrued_1.3.tar.gz", SOURCE_PATH);
        pathsToUploadToArchive.put("23457_PACKAGES", SOURCE_PATH);
        pathsToUploadToArchive.put("23458_PACKAGES.gz", SOURCE_PATH);
        pathsToUpload.put("23459_arrow_8.0.0.tar.gz", BINARY_PATH);
        pathsToUpload.put("23450_PACKAGES", BINARY_PATH);
        pathsToUpload.put("34567_PACKAGES.gz", BINARY_PATH);
        pathsToUploadToArchive.put("34568_OpenSpecy_1.0.99.tar.gz", BINARY_PATH);
        pathsToUploadToArchive.put("34569_PACKAGES", BINARY_PATH);
        pathsToUploadToArchive.put("34560_PACKAGES.gz", BINARY_PATH);
        pathsToUpload.put("45678_OpenSpecy_1.1.0.tar.gz", BINARY_HIGHER_VERSION_PATH);
        pathsToUpload.put("45679_PACKAGES", BINARY_HIGHER_VERSION_PATH);
        pathsToUpload.put("45670_PACKAGES.gz", BINARY_HIGHER_VERSION_PATH);

        final Map<String, String> checksums = getChecksumsForTestFiles();

        final SynchronizeCranRepositoryRequestBody requestBody = new SynchronizeCranRepositoryRequestBody(
                randomId,
                recent,
                archive,
                new String[0],
                new String[0],
                "1",
                "2",
                "1/1",
                TEST_REPO,
                pathsToUpload,
                pathsToUploadToArchive,
                new HashMap<>(),
                new HashMap<>(),
                checksums);
        storageService.storeAndDeleteFiles(requestBody);
        storageService.handleLastChunk(requestBody, TEST_REPO);

        final File recentSourceDir =
                tempDir.resolve(TEST_REPO).resolve(SOURCE_PATH).toFile();
        final File archiveDir = recentSourceDir.toPath().resolve("Archive").toFile();
        final File recentBinaryLowerVersionDir =
                tempDir.resolve(TEST_REPO).resolve(BINARY_PATH).toFile();
        final File archiveBinaryLowerVersionDir = tempDir.resolve(TEST_REPO)
                .resolve(BINARY_PATH)
                .resolve("Archive")
                .toFile();
        final File recentBinaryHigherVersionDir =
                tempDir.resolve(TEST_REPO).resolve(BINARY_HIGHER_VERSION_PATH).toFile();

        final MultipartFile[] expectedSourceRecent = getExpectedPackages("recent");
        final MultipartFile[] expectedSourceArchive = getExpectedPackages("archive");
        final MultipartFile[] expectedBinaryRecentLowerVersion =
                getExpectedPackages(TEST_BINARY_PACKAGES_RECENT_DIR_LOWER_VERSION);
        final MultipartFile[] expectedBinaryRecentHigherVersion =
                getExpectedPackages(TEST_BINARY_PACKAGES_RECENT_DIR_HIGHER_VERSION);
        final MultipartFile[] expectedBinaryArchive = getExpectedPackages(TEST_BINARY_PACKAGES_ARCHIVE_DIR);

        assertFiles(expectedSourceRecent, recentSourceDir);
        assertFiles(expectedSourceArchive, archiveDir);
        assertFiles(expectedBinaryRecentLowerVersion, recentBinaryLowerVersionDir);
        assertFiles(expectedBinaryRecentHigherVersion, recentBinaryHigherVersionDir);
        assertFiles(expectedBinaryArchive, archiveBinaryLowerVersionDir);

        assertTrue(Files.exists(recentSourceDir.toPath().resolve("Meta")));
        assertTrue(Files.exists(recentSourceDir.toPath().resolve("Meta").resolve("archive.rds")));
        assertTrue(recentSourceDir
                        .toPath()
                        .resolve("Meta")
                        .resolve("archive.rds")
                        .toFile()
                        .length()
                > 0);

        assertTrue(Files.exists(recentBinaryLowerVersionDir.toPath().resolve("Meta")));
        assertTrue(Files.exists(
                recentBinaryLowerVersionDir.toPath().resolve("Meta").resolve("archive.rds")));
        assertTrue(recentBinaryLowerVersionDir
                        .toPath()
                        .resolve("Meta")
                        .resolve("archive.rds")
                        .toFile()
                        .length()
                > 0);
    }

    @Test
    public void getPackages_WhenRepositoryIsEmpty() throws IOException {
        List<Path> files = storageService.getRecentPackagesFromRepository(NON_EXISTING_REPO);
        assertTrue(files.isEmpty(), "File list should be empty.");

        Map<String, List<Path>> archive = storageService.getArchiveFromRepository(NON_EXISTING_REPO);
        assertTrue(archive.isEmpty(), "Archive map should be empty.");
    }

    @Test
    public void deletePackages() throws Exception {
        final String randomId = RandomStringUtils.randomAlphabetic(16);
        final Path trash = Files.createDirectory(tempDir.resolve("TRASH_" + randomId));
        Files.createFile(trash.resolve("TRASH_DATABASE.txt"));

        final File recentTestSourcePackagesDir =
                new File(TEST_PACKAGES_DIR).toPath().resolve("recent").toFile();
        final File archiveTestSourcePackagesDir =
                new File(TEST_PACKAGES_DIR).toPath().resolve("archive").toFile();
        final File recentTestBinaryLowerVersionDir = new File(TEST_PACKAGES_DIR)
                .toPath()
                .resolve(TEST_BINARY_PACKAGES_RECENT_DIR_LOWER_VERSION)
                .toFile();
        final File archiveTestBinaryLowerVersionDir = new File(TEST_PACKAGES_DIR)
                .toPath()
                .resolve(TEST_BINARY_PACKAGES_ARCHIVE_DIR)
                .toFile();
        final File recentTestBinaryHigherVersionDir = new File(TEST_PACKAGES_DIR)
                .toPath()
                .resolve(TEST_BINARY_PACKAGES_RECENT_DIR_HIGHER_VERSION)
                .toFile();

        final File recentSourceDir = copyTestPackagesToTemporaryFolder(
                new File(EXPECTED_PACKAGES_DIR).toPath(), SOURCE_PATH, "recent", "archive");
        final File archiveSourceDir =
                recentSourceDir.toPath().resolve("Archive").toFile();
        final File recentBinaryLowerVersionDir = copyTestPackagesToTemporaryFolder(
                new File(EXPECTED_PACKAGES_DIR).toPath(),
                BINARY_PATH,
                TEST_BINARY_PACKAGES_RECENT_DIR_LOWER_VERSION,
                TEST_BINARY_PACKAGES_ARCHIVE_DIR);
        final File archiveBinaryLowerVersionDir =
                recentBinaryLowerVersionDir.toPath().resolve("Archive").toFile();
        final File recentBinaryHigherVersionDir = copyTestPackagesToTemporaryFolder(
                new File(EXPECTED_PACKAGES_DIR).toPath(),
                BINARY_HIGHER_VERSION_PATH,
                TEST_BINARY_PACKAGES_RECENT_DIR_HIGHER_VERSION,
                "");

        final List<String> recentToDeleteList =
                getPackagesToDelete(Objects.requireNonNull(recentTestSourcePackagesDir.listFiles()));
        recentToDeleteList.addAll(
                getPackagesToDelete(Objects.requireNonNull(recentTestBinaryLowerVersionDir.listFiles())));
        recentToDeleteList.addAll(
                getPackagesToDelete(Objects.requireNonNull(recentTestBinaryHigherVersionDir.listFiles())));

        final List<String> archiveToDeleteList =
                getPackagesToDelete(Objects.requireNonNull(archiveTestSourcePackagesDir.listFiles()));
        archiveToDeleteList.addAll(
                getPackagesToDelete(Objects.requireNonNull(archiveTestBinaryLowerVersionDir.listFiles())));

        final String[] recentToDelete = new String[recentToDeleteList.size()];
        recentToDeleteList.toArray(recentToDelete);

        final String[] archiveToDelete = new String[archiveToDeleteList.size()];
        archiveToDeleteList.toArray(archiveToDelete);

        final MultipartFile[] recent = new MultipartFile[0];
        final MultipartFile[] archive = new MultipartFile[0];

        final Map<String, String> pathsToDelete = new HashMap<>();
        final Map<String, String> pathsToDeleteToArchive = new HashMap<>();

        pathsToDelete.put("12345_Benchmarking_0.10.tar.gz", SOURCE_PATH);
        pathsToDelete.put("12348_usl_2.0.0.tar.gz", SOURCE_PATH);
        pathsToDeleteToArchive.put("12349_abc_1.3.tar.gz", SOURCE_PATH);
        pathsToDeleteToArchive.put("12340_accrued_1.2.tar.gz", SOURCE_PATH);
        pathsToDeleteToArchive.put("23456_accrued_1.3.tar.gz", SOURCE_PATH);
        pathsToDelete.put("23459_arrow_8.0.0.tar.gz", BINARY_PATH);
        pathsToDeleteToArchive.put("34568_OpenSpecy_1.0.99.tar.gz", BINARY_PATH);
        pathsToDelete.put("45678_OpenSpecy_1.1.0.tar.gz", BINARY_HIGHER_VERSION_PATH);

        final Map<String, String> checksums = getChecksumsForTestFiles();

        final SynchronizeCranRepositoryRequestBody requestBody = new SynchronizeCranRepositoryRequestBody(
                randomId,
                recent,
                archive,
                recentToDelete,
                archiveToDelete,
                "1",
                "2",
                "1/1",
                TEST_REPO,
                new HashMap<>(),
                new HashMap<>(),
                pathsToDelete,
                pathsToDeleteToArchive,
                checksums);

        storageService.storeAndDeleteFiles(requestBody);
        storageService.handleLastChunk(requestBody, TEST_REPO);

        assertEquals(3, Objects.requireNonNull(recentSourceDir.listFiles()).length);
        assertEquals(0, Objects.requireNonNull(archiveSourceDir.listFiles()).length);
        assertEquals(3, recentBinaryLowerVersionDir.listFiles().length);
        assertEquals(3, recentBinaryHigherVersionDir.listFiles().length);
        assertEquals(0, archiveBinaryLowerVersionDir.listFiles().length);

        assertFalse(Files.exists(recentSourceDir.toPath().resolve("Meta")));
        assertFalse(Files.exists(recentSourceDir.toPath().resolve("Meta").resolve("archive.rds")));
        assertFalse(Files.exists(recentBinaryLowerVersionDir.toPath().resolve("Meta")));
        assertFalse(Files.exists(
                recentBinaryLowerVersionDir.toPath().resolve("Meta").resolve("archive.rds")));
        assertFalse(Files.exists(recentBinaryHigherVersionDir.toPath().resolve("Meta")));
        assertFalse(Files.exists(
                recentBinaryHigherVersionDir.toPath().resolve("Meta").resolve("archive.rds")));
    }

    private List<String> getPackagesToDelete(File[] files) {
        List<String> filenameList = new ArrayList<>();

        for (File file : files) {
            if (!file.getName().contains("PACKAGES")) filenameList.add(file.getName());
        }

        return filenameList;
    }

    private File copyTestPackagesToTemporaryFolder(
            Path testPackagesDirectory,
            String folderPath,
            String testPackagesRecentSubDir,
            String testPackagesArchiveSubDir)
            throws IOException {
        assertTrue(Files.isDirectory(tempDir));
        File recentDir = tempDir.resolve(TEST_REPO).resolve(folderPath).toFile();
        File archiveDir = recentDir.toPath().resolve("Archive").toFile();

        File recentTestPackagesDir =
                testPackagesDirectory.resolve(testPackagesRecentSubDir).toFile();
        File archiveTestPackagesDir =
                testPackagesDirectory.resolve(testPackagesArchiveSubDir).toFile();

        Files.createDirectories(archiveDir.toPath());

        FileUtils.copyDirectory(recentTestPackagesDir, recentDir);

        if (testPackagesArchiveSubDir.isEmpty()) return recentDir;

        for (File file : Objects.requireNonNull(archiveTestPackagesDir.listFiles())) {
            if (!file.getName().contains("PACKAGES")) {
                String packageName = file.getName().split("_")[0];
                Path packageDedicatedDir = archiveDir.toPath().resolve(packageName);

                if (!packageDedicatedDir.toFile().exists()) {
                    Files.createDirectories(packageDedicatedDir);
                }

                Files.copy(file.toPath(), packageDedicatedDir.resolve(file.getName()));
            } else {
                Files.copy(file.toPath(), archiveDir.toPath().resolve(file.getName()));
            }
        }

        return recentDir;
    }

    private Map<String, String> getChecksumsForTestFiles() {

        final Map<String, String> checksums = new HashMap<>();
        checksums.put("12345_Benchmarking_0.10.tar.gz", "9a99c2ebefa6d49422ca7893c1f4ead8");
        checksums.put("12346_PACKAGES", "b9edcc805d4a7f435c18ba670d488d19");
        checksums.put("12347_PACKAGES.gz", "c4fd38fcf43cce22fa16f61add39960b");
        checksums.put("12348_usl_2.0.0.tar.gz", "868140a3c3c29327eef5d5a485aee5b6");
        checksums.put("12349_abc_1.3.tar.gz", "c47d18b86b331a5023dcd62b74fedbb6");
        checksums.put("12340_accrued_1.2.tar.gz", "70d295115295a4718593f6a39d77add9");
        checksums.put("23456_accrued_1.3.tar.gz", "a05e4ca44438c0d9e7d713d7e3890423");
        checksums.put("23457_PACKAGES", "11a6d192748004e42797862505409102");
        checksums.put("23458_PACKAGES.gz", "d198474d73e5cc542aa7a0348b104d72");
        checksums.put("23459_arrow_8.0.0.tar.gz", "b55eb6a2f5adeff68f1ef15fd35b03de");
        checksums.put("23450_PACKAGES", "19d375fc238db3fcde9b2fbb53f4db79");
        checksums.put("34567_PACKAGES.gz", "ef5a203d7f71fe78ab8ea3ba221cca16");
        checksums.put("34568_OpenSpecy_1.0.99.tar.gz", "2333d8335e081ac4607495fe5e840dde");
        checksums.put("34569_PACKAGES", "88ef9d48024d1175881894302abf3122");
        checksums.put("34560_PACKAGES.gz", "0fa08c9ee5b2db0cc542880d0d238bcd");
        checksums.put("45678_OpenSpecy_1.1.0.tar.gz", "13bda5374451f899771b8388983fe334");
        checksums.put("45679_PACKAGES", "4be06fd34e35897a1093a1500cd626b3");
        checksums.put("45670_PACKAGES.gz", "5394a5efae797ab7bad97043a5d7de6d");

        return checksums;
    }
}

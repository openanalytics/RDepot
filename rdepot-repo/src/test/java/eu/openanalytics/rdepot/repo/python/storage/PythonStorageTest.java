/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
package eu.openanalytics.rdepot.repo.python.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.openanalytics.rdepot.repo.hash.model.HashMethod;
import eu.openanalytics.rdepot.repo.python.model.SynchronizePythonRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.python.storage.implementations.PythonFileSystemStorageService;
import eu.openanalytics.rdepot.repo.storage.StorageProperties;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class PythonStorageTest {

    @TempDir
    public Path tempDir;

    private PythonFileSystemStorageService storageService;

    private File testPackagesDir;

    private File expectedPackagesDir;

    private static final String TEST_PACKAGES_DIR =
            "src/test/resources/eu/openanalytics/rdepot/repo/testpackages/python/";
    private static final String EXPECTED_PACKAGES_DIR =
            "src/test/resources/eu/openanalytics/rdepot/repo/testpackages/python/extracted";

    private static final String TEST_REPO = "testrepo456";
    private static final String NON_EXISTING_REPO = "testrepo567";

    @BeforeEach
    public void setUp() throws Exception {
        assertTrue(Files.isDirectory(tempDir));
        Files.createDirectory(tempDir.resolve(TEST_REPO));

        final StorageProperties properties = new StorageProperties();
        properties.setLocation(tempDir.toString());

        this.storageService = new PythonFileSystemStorageService(properties);
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
        String randomId = RandomStringUtils.secure().nextAlphabetic(16);
        final MultipartFile[] recent = getTestPackages();

        final Map<String, String> checksums = getChecksumsForTestFiles();

        final SynchronizePythonRepositoryRequestBody requestBody = new SynchronizePythonRepositoryRequestBody(
                randomId, recent, new String[0], "1", "2", "1/1", TEST_REPO, checksums, HashMethod.SHA256);
        storageService.storeAndDeleteFiles(requestBody);
        storageService.handleLastChunk(requestBody, TEST_REPO);

        final Path repoDir = tempDir.resolve(TEST_REPO);
        final File pandasDir = repoDir.resolve("pandas").toFile();
        final File setuptoolsDir = repoDir.resolve("setuptools").toFile();

        final MultipartFile[] expectedRepoIndexFile = getExpectedPackages("");
        final MultipartFile[] expectedPandas = getExpectedPackages("pandas");
        final MultipartFile[] expectedSetuptools = getExpectedPackages("setuptools");

        assertFiles(expectedRepoIndexFile, repoDir.toFile());
        assertFiles(expectedPandas, pandasDir);
        assertFiles(expectedSetuptools, setuptoolsDir);
    }

    @Test
    public void getPackages_WhenRepositoryIsEmpty() throws IOException {
        List<Path> files = storageService.getRecentPackagesFromRepository(NON_EXISTING_REPO);
        assertTrue(files.isEmpty(), "File list should be empty.");
    }

    @Test
    public void deletePackages() throws Exception {
        String randomId = RandomStringUtils.secure().nextAlphabetic(16);
        final Path trash = Files.createDirectory(tempDir.resolve("TRASH_" + randomId));
        Files.createFile(trash.resolve("TRASH_DATABASE.txt"));

        assertTrue(Files.isDirectory(tempDir));
        File recentDir = tempDir.resolve(TEST_REPO).toFile();
        File recentPandasDir = recentDir.toPath().resolve("pandas").toFile();
        File recentSetuptoolsDir = recentDir.toPath().resolve("setuptools").toFile();

        final File recentTestPackagesDir =
                new File(EXPECTED_PACKAGES_DIR).toPath().toFile();

        FileUtils.copyDirectory(recentTestPackagesDir, recentDir);

        final File recentTestPandasPackagesDir =
                new File(EXPECTED_PACKAGES_DIR).toPath().resolve("pandas").toFile();
        final File recentTestSetuptoolsPackagesDir =
                new File(EXPECTED_PACKAGES_DIR).toPath().resolve("setuptools").toFile();

        final List<String> recentToDeleteList =
                getPackagesToDelete(Objects.requireNonNull(recentTestPandasPackagesDir.listFiles()));
        recentToDeleteList.addAll(
                getPackagesToDelete(Objects.requireNonNull(recentTestSetuptoolsPackagesDir.listFiles())));

        final String[] recentToDelete = new String[recentToDeleteList.size()];
        recentToDeleteList.toArray(recentToDelete);

        File file = testPackagesDir
                .toPath()
                .resolve("index_after_deletion")
                .resolve("index.tar.gz")
                .toFile();
        MultipartFile multipartFile =
                new MockMultipartFile("files", file.getName(), null, Files.readAllBytes(file.toPath()));
        MultipartFile[] recent = {multipartFile};

        final Map<String, String> checksums = new HashMap<>();
        checksums.put("index.tar.gz", "81e21ae1fe10065944307d20c917cf78edce18b1ecb0b8ee32adbdc83f459569");

        final SynchronizePythonRepositoryRequestBody requestBody = new SynchronizePythonRepositoryRequestBody(
                randomId, recent, recentToDelete, "1", "2", "1/1", TEST_REPO, checksums, HashMethod.SHA256);

        storageService.storeAndDeleteFiles(requestBody);
        storageService.handleLastChunk(requestBody, TEST_REPO);

        assertEquals(1, Objects.requireNonNull(recentDir.listFiles()).length);

        assertFalse(Files.exists(recentPandasDir.toPath()));
        assertFalse(Files.exists(recentSetuptoolsDir.toPath()));
    }

    private MultipartFile[] getTestPackages() throws IOException {
        ArrayList<MultipartFile> files = new ArrayList<>();

        for (File file :
                Objects.requireNonNull(testPackagesDir.toPath().toFile().listFiles())) {
            if (file.isDirectory()) continue;
            MultipartFile multipartFile =
                    new MockMultipartFile("files", file.getName(), null, Files.readAllBytes(file.toPath()));
            files.add(multipartFile);
        }

        MultipartFile[] testPackages = new MultipartFile[files.size()];
        files.toArray(testPackages);

        return testPackages;
    }

    private Map<String, String> getChecksumsForTestFiles() {

        final Map<String, String> checksums = new HashMap<>();
        checksums.put("index.tar.gz", "e3352dfc15bf1b5ac5a1302dc7b48701a7a0e699b10c8b46d8fe336ba7012c9e");
        checksums.put("pandas", "070fc315c8420efc7fa12506a55882f1d1da49021bd05057be011f8bafca4ce3");
        checksums.put("setuptools.gz", "3fc1215fe72c39b3bd8e4766caef1a3c1935d5b458c3a80c6d60cdfdd6e50073");

        return checksums;
    }

    private MultipartFile[] getExpectedPackages(String expectedDirectory) throws IOException {
        ArrayList<MultipartFile> files = new ArrayList<>();

        for (File file : Objects.requireNonNull(
                expectedPackagesDir.toPath().resolve(expectedDirectory).toFile().listFiles())) {
            if (file.isDirectory()) continue;
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
            Path actual = actualDirectoryPath.resolve(fileName);
            actualBytes = Files.readAllBytes(actual);

            assertArrayEquals(actualBytes, expectedBytes, "Uploaded file is not correct");
        }
    }

    private List<String> getPackagesToDelete(File[] files) {
        List<String> filenameList = new ArrayList<>();

        for (File file : files) {
            if (!file.getName().equals("index.html")) filenameList.add(file.getName());
        }

        return filenameList.stream()
                .map(f -> StringUtils.substringBefore(f, "-").concat("/").concat(f))
                .collect(Collectors.toList());
    }
}

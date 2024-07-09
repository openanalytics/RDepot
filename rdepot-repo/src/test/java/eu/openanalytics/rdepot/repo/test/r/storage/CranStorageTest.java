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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class CranStorageTest {

    @TempDir
    public Path tempDir;

    private static final String TEST_PACKAGES_DIR = "src/test/resources/eu/openanalytics/rdepot/repo/testpackages/";

    private final String TRASH_PREFIX = "TRASH_";
    private final String TRASH_DATABASE_FILE = "TRASH_DATABASE.txt";

    private CranFileSystemStorageService storageService;

    private File testPackagesDir;

    private static final String TEST_REPO = "testrepo123";
    private static final String NON_EXISTING_REPO = "testrepo234";

    private MultipartFile[] getTestPackages(boolean archive) throws IOException {
        ArrayList<MultipartFile> files = new ArrayList<MultipartFile>();

        String subDir = archive ? "archive" : "recent";

        for (File file : Objects.requireNonNull(
                testPackagesDir.toPath().resolve(subDir).toFile().listFiles())) {
            MultipartFile multipartFile =
                    new MockMultipartFile("files", file.getName(), null, Files.readAllBytes(file.toPath()));
            ;

            files.add(multipartFile);
        }

        MultipartFile[] testPackages = new MultipartFile[files.size()];
        files.toArray(testPackages);

        return testPackages;
    }

    private MultipartFile[] getRecentTestPackages() throws IOException {
        return getTestPackages(false);
    }

    private MultipartFile[] getArchiveTestPackages() throws IOException {
        return getTestPackages(true);
    }

    private void assertFiles(MultipartFile[] expectedFiles, File actualDirectory) throws IOException {
        for (MultipartFile expectedFile : expectedFiles) {
            byte[] expectedBytes = null;
            byte[] actualBytes = null;

            expectedBytes = expectedFile.getBytes();

            Path actualDirectoryPath = actualDirectory.toPath();
            actualDirectoryPath = actualDirectory.getName().equals("Archive")
                            && !Objects.requireNonNull(expectedFile.getOriginalFilename())
                                    .startsWith("PACKAGES")
                    ? actualDirectoryPath.resolve(
                            expectedFile.getOriginalFilename().split("_")[0])
                    : actualDirectoryPath;
            Path actual = actualDirectoryPath.resolve(Objects.requireNonNull(expectedFile.getOriginalFilename()));
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

        final Map<String, String> checksums = new HashMap<>();
        checksums.put("Benchmarking_0.10.tar.gz", "9a99c2ebefa6d49422ca7893c1f4ead8");
        checksums.put("PACKAGES", "b9edcc805d4a7f435c18ba670d488d19");
        checksums.put("PACKAGES.gz", "c4fd38fcf43cce22fa16f61add39960b");
        checksums.put("usl_2.0.0.tar.gz", "868140a3c3c29327eef5d5a485aee5b6");
        checksums.put("abc_1.3.tar.gz", "c47d18b86b331a5023dcd62b74fedbb6");
        checksums.put("accrued_1.2.tar.gz", "70d295115295a4718593f6a39d77add9");
        checksums.put("accrued_1.3.tar.gz", "a05e4ca44438c0d9e7d713d7e3890423");
        checksums.put("PACKAGES_ARCHIVE", "11a6d192748004e42797862505409102");
        checksums.put("PACKAGES_ARCHIVE.gz", "d198474d73e5cc542aa7a0348b104d72");

        final SynchronizeCranRepositoryRequestBody requestBody = new SynchronizeCranRepositoryRequestBody(
                randomId, recent, archive, new String[0], new String[0], "1", "2", "1/1", TEST_REPO, checksums);
        storageService.storeAndDeleteFiles(requestBody);
        storageService.handleLastChunk(requestBody, TEST_REPO);

        final File recentDir =
                tempDir.resolve(TEST_REPO).resolve("src").resolve("contrib").toFile();
        final File archiveDir = recentDir.toPath().resolve("Archive").toFile();

        assertFiles(recent, recentDir);
        assertFiles(archive, archiveDir);

        assertTrue(Files.exists(recentDir.toPath().resolve("Meta")));
        assertTrue(Files.exists(recentDir.toPath().resolve("Meta").resolve("archive.rds")));
        assertTrue(recentDir
                        .toPath()
                        .resolve("Meta")
                        .resolve("archive.rds")
                        .toFile()
                        .length()
                > 0);
    }

    @Test
    public void getPackages_WhenRepositoryIsEmpty() {
        List<File> files = storageService.getRecentPackagesFromRepository(NON_EXISTING_REPO);
        Map<String, List<File>> archive = storageService.getArchiveFromRepository(NON_EXISTING_REPO);

        assertTrue(files.isEmpty(), "File list should be empty.");
        assertTrue(archive.isEmpty(), "Archive map should be empty.");
    }

    @Test
    public void deletePackages() throws Exception {
        final String randomId = RandomStringUtils.randomAlphabetic(16);
        final Path trash = Files.createDirectory(tempDir.resolve(TRASH_PREFIX + randomId));
        Files.createFile(trash.resolve(TRASH_DATABASE_FILE));

        final File recentTestPackagesDir =
                new File(TEST_PACKAGES_DIR).toPath().resolve("recent").toFile();
        final File archiveTestPackagesDir =
                new File(TEST_PACKAGES_DIR).toPath().resolve("archive").toFile();

        final File recentDir = copyTestPackagesToTemporaryFolder(new File(TEST_PACKAGES_DIR).toPath());
        final File archiveDir = recentDir.toPath().resolve("Archive").toFile();

        final String[] recentToDelete = getPackagesToDelete(Objects.requireNonNull(recentTestPackagesDir.listFiles()));
        final String[] archiveToDelete =
                getPackagesToDelete(Objects.requireNonNull(archiveTestPackagesDir.listFiles()));

        final MultipartFile[] recent = new MultipartFile[0];
        final MultipartFile[] archive = new MultipartFile[0];

        final Map<String, String> checksums = new HashMap<>();
        checksums.put("Benchmarking_0.10.tar.gz", "9a99c2ebefa6d49422ca7893c1f4ead8");
        checksums.put("PACKAGES", "b9edcc805d4a7f435c18ba670d488d19");
        checksums.put("PACKAGES.gz", "c4fd38fcf43cce22fa16f61add39960b");
        checksums.put("usl_2.0.0.tar.gz", "868140a3c3c29327eef5d5a485aee5b6");
        checksums.put("abc_1.3.tar.gz", "c47d18b86b331a5023dcd62b74fedbb6");
        checksums.put("accrued_1.2.tar.gz", "70d295115295a4718593f6a39d77add9");
        checksums.put("accrued_1.3.tar.gz", "a05e4ca44438c0d9e7d713d7e3890423");
        checksums.put("PACKAGES_ARCHIVE", "11a6d192748004e42797862505409102");
        checksums.put("PACKAGES_ARCHIVE.gz", "d198474d73e5cc542aa7a0348b104d72");

        final SynchronizeCranRepositoryRequestBody requestBody = new SynchronizeCranRepositoryRequestBody(
                randomId, recent, archive, recentToDelete, archiveToDelete, "1", "2", "1/1", TEST_REPO, checksums);
        storageService.storeAndDeleteFiles(requestBody);
        storageService.handleLastChunk(requestBody, TEST_REPO);

        assertEquals(3, Objects.requireNonNull(recentDir.listFiles()).length);
        assertEquals(0, Objects.requireNonNull(archiveDir.listFiles()).length);

        assertFalse(Files.exists(recentDir.toPath().resolve("Meta")));
        assertFalse(Files.exists(recentDir.toPath().resolve("Meta").resolve("archive.rds")));
    }

    private String[] getPackagesToDelete(File[] files) {
        ArrayList<String> filenameList = new ArrayList<String>();

        for (File file : files) {
            if (!file.getName().startsWith("PACKAGES")) filenameList.add(file.getName());
        }

        String[] filenameArray = new String[filenameList.size()];
        filenameList.toArray(filenameArray);

        return filenameArray;
    }

    private File copyTestPackagesToTemporaryFolder(Path testPackagesDirectory) throws IOException {
        assertTrue(Files.isDirectory(tempDir));
        File recentDir =
                tempDir.resolve(TEST_REPO).resolve("src").resolve("contrib").toFile();
        File archiveDir = recentDir.toPath().resolve("Archive").toFile();

        File recentTestPackagesDir = testPackagesDirectory.resolve("recent").toFile();
        File archiveTestPackagesDir = testPackagesDirectory.resolve("archive").toFile();

        Files.createDirectories(archiveDir.toPath());

        FileUtils.copyDirectory(recentTestPackagesDir, recentDir);
        for (File file : archiveTestPackagesDir.listFiles()) {
            if (!file.getName().startsWith("PACKAGES")) {
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
}

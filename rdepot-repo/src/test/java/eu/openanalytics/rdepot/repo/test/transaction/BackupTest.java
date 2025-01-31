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
package eu.openanalytics.rdepot.repo.test.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import eu.openanalytics.rdepot.repo.exception.GetRepositoryVersionException;
import eu.openanalytics.rdepot.repo.exception.InitTrashDirectoryException;
import eu.openanalytics.rdepot.repo.r.storage.implementations.CranFileSystemStorageService;
import eu.openanalytics.rdepot.repo.r.transaction.backup.CranRepositoryBackup;
import eu.openanalytics.rdepot.repo.r.transaction.backup.CranRepositoryBackupServiceImpl;
import eu.openanalytics.rdepot.repo.transaction.Transaction;
import eu.openanalytics.rdepot.repo.transaction.UploadTransactionManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BackupTest {

    @Mock
    CranFileSystemStorageService storageService;

    @Mock
    UploadTransactionManager transactionManager;

    @Mock
    ConcurrentMap<Transaction, CranRepositoryBackup> cranBackups;

    @InjectMocks
    CranRepositoryBackupServiceImpl cranRepositoryBackupService;

    private static final String TEST_PACKAGES_DIR = "src/test/resources/eu/openanalytics/rdepot/repo/testpackages/";
    private static final String TEST_RECENT_DIR = "recent";
    private static final String TEST_ARCHIVE_DIR = "archive";
    private static final String TEST_BINARY_PACKAGES_RECENT_DIR_LOWER_VERSION = "binary_packages/recent_4_2";
    private static final String TEST_BINARY_PACKAGES_RECENT_DIR_HIGHER_VERSION = "binary_packages/recent_4_5";
    private static final String TEST_BINARY_PACKAGES_ARCHIVE_DIR = "binary_packages/archive_4_2";
    private static final String SOURCE_PATH = "src/contrib";
    private static final String BINARY_PATH = "bin/linux/centos7/x86_64/4.2";
    private static final String BINARY_HIGHER_VERSION_PATH = "bin/linux/centos7/x86_64/4.5";

    private Path testPackagesDir;
    private static final String TEST_REPO = "testrepo123";

    private List<Path> getTestPackages(String subDir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(testPackagesDir.resolve(subDir))) {
            return StreamSupport.stream(stream.spliterator(), false).toList();
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        this.testPackagesDir = Paths.get(TEST_PACKAGES_DIR);
        if (Files.notExists(testPackagesDir) || !Files.isDirectory(testPackagesDir))
            throw new FileNotFoundException(testPackagesDir.toAbsolutePath().toString());
    }

    @Test
    public void backupForTransaction() throws Exception {
        final Transaction mockTransaction = new Transaction(TEST_REPO, "123abc", 1);
        final File testTrash = File.createTempFile("TRASH_", "430482309482309");

        doReturn(getTestPackages(TEST_RECENT_DIR)).when(storageService).getRecentPackagesFromRepository(TEST_REPO);

        Map<String, List<Path>> recentBinaryPackages = new HashMap<>();
        recentBinaryPackages.put(BINARY_PATH, getTestPackages(TEST_BINARY_PACKAGES_RECENT_DIR_LOWER_VERSION));
        recentBinaryPackages.put(
                BINARY_HIGHER_VERSION_PATH, getTestPackages(TEST_BINARY_PACKAGES_RECENT_DIR_HIGHER_VERSION));

        Map<String, List<Path>> archiveAllPackages = new HashMap<>();
        archiveAllPackages.put(SOURCE_PATH, getTestPackages(TEST_ARCHIVE_DIR));
        archiveAllPackages.put(BINARY_PATH, getTestPackages(TEST_BINARY_PACKAGES_ARCHIVE_DIR));

        doReturn(recentBinaryPackages).when(storageService).getRecentBinaryPackagesFromRepository(TEST_REPO);

        doReturn(archiveAllPackages).when(storageService).getArchiveFromRepository(TEST_REPO);

        doReturn(testTrash).when(storageService).initTrashDirectory(anyString());
        doReturn("23").when(storageService).getRepositoryVersion(TEST_REPO);
        doAnswer(invocationOnMock -> {
                    final CranRepositoryBackup backup = invocationOnMock.getArgument(1);
                    assertEquals(
                            getTestPackages(TEST_ARCHIVE_DIR).stream()
                                    .map(Path::getFileName)
                                    .map(Path::toString)
                                    .collect(Collectors.toSet()),
                            new HashSet<>(backup.getArchivePackages().get(SOURCE_PATH)),
                            "archive source packages were not backed-up");
                    assertEquals(
                            getTestPackages(TEST_RECENT_DIR).stream()
                                    .map(Path::getFileName)
                                    .map(Path::toString)
                                    .collect(Collectors.toSet()),
                            new HashSet<>(backup.getRecentPackages()),
                            "recent source packages were not backed-up");
                    assertEquals(
                            getTestPackages(TEST_BINARY_PACKAGES_RECENT_DIR_LOWER_VERSION).stream()
                                    .map(Path::getFileName)
                                    .map(Path::toString)
                                    .collect(Collectors.toSet()),
                            new HashSet<>(backup.getRecentBinaryPackages().get(BINARY_PATH)),
                            "recent binary packages for 4.2 R version were not backed-up");
                    assertEquals(
                            getTestPackages(TEST_BINARY_PACKAGES_RECENT_DIR_HIGHER_VERSION).stream()
                                    .map(Path::getFileName)
                                    .map(Path::toString)
                                    .collect(Collectors.toSet()),
                            new HashSet<>(backup.getRecentBinaryPackages().get(BINARY_HIGHER_VERSION_PATH)),
                            "recent binary packages for 4.5 R version were not backed-up");

                    assertEquals(
                            getTestPackages(TEST_BINARY_PACKAGES_ARCHIVE_DIR).stream()
                                    .map(Path::getFileName)
                                    .map(Path::toString)
                                    .collect(Collectors.toSet()),
                            new HashSet<>(backup.getArchivePackages().get(BINARY_PATH)),
                            "archive binary packages were not backed-up");
                    assertEquals(testTrash, backup.getTrashDirectory(), "trash was not created properly");
                    assertEquals("23", backup.getVersion(), "incorrect version of backed-up repo");
                    return null;
                })
                .when(cranBackups)
                .put(eq(mockTransaction), any());

        cranRepositoryBackupService.backupForTransaction(mockTransaction);

        verify(storageService).initTrashDirectory(anyString());
        verify(cranBackups).put(eq(mockTransaction), any());
    }

    @Test
    public void backupTransaction_shouldAbort_ifInitTrashDirectoryIsNotCreated() throws Exception {
        final Transaction mockTransaction = new Transaction(TEST_REPO, "123abc", 1);
        doNothing().when(transactionManager).abortTransaction(mockTransaction);
        doThrow(new InitTrashDirectoryException("")).when(storageService).initTrashDirectory(anyString());

        try {
            cranRepositoryBackupService.backupForTransaction(mockTransaction);
            fail("Exception should have been thrown");
        } catch (RuntimeException ignored) {
        }

        verify(storageService).initTrashDirectory(anyString());
        verify(transactionManager).abortTransaction(mockTransaction);
    }

    @Test
    public void backupForTransaction_abortsTransaction_ifVersionCouldNotBeRetrieved() throws Exception {
        final Transaction mockTransaction = new Transaction(TEST_REPO, "123abc", 1);
        final File testTrash = File.createTempFile("TRASH_", "430482309482309");
        doReturn(testTrash).when(storageService).initTrashDirectory(anyString());
        doThrow(new GetRepositoryVersionException("")).when(storageService).getRepositoryVersion(TEST_REPO);

        try {
            cranRepositoryBackupService.backupForTransaction(mockTransaction);
            fail("Exception was not thrown.");
        } catch (RuntimeException ignored) {
        }

        verify(storageService).initTrashDirectory(anyString());
        verify(storageService).getRepositoryVersion(TEST_REPO);
        verify(transactionManager).abortTransaction(mockTransaction);
    }

    @Test
    public void restoreForTransaction() throws Exception {
        final Transaction mockTransaction = new Transaction(TEST_REPO, "123abc", 1);
        final List<String> recentPackages = getTestPackages(TEST_RECENT_DIR).stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .toList();

        final Map<String, List<Path>> recentBinaryPackages = new HashMap<>();
        recentBinaryPackages.put(BINARY_PATH, getTestPackages(TEST_BINARY_PACKAGES_RECENT_DIR_LOWER_VERSION));
        recentBinaryPackages.put(
                BINARY_HIGHER_VERSION_PATH, getTestPackages(TEST_BINARY_PACKAGES_RECENT_DIR_HIGHER_VERSION));

        final Map<String, List<Path>> archiveAllPackages = new HashMap<>();
        archiveAllPackages.put(SOURCE_PATH, getTestPackages(TEST_ARCHIVE_DIR));
        archiveAllPackages.put(BINARY_PATH, getTestPackages(TEST_BINARY_PACKAGES_ARCHIVE_DIR));

        final Map<String, List<String>> recentBinaryPackagesStrings = recentBinaryPackages.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .map(file -> file.getFileName().toString())
                        .collect(Collectors.toList())));

        final Map<String, List<String>> archiveAllPackagesStrings = archiveAllPackages.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .map(file -> file.getFileName().toString())
                        .collect(Collectors.toList())));

        final File testTrash = File.createTempFile("TRASH_", "430482309482309");

        final CranRepositoryBackup backup = new CranRepositoryBackup(
                recentPackages, recentBinaryPackagesStrings, archiveAllPackagesStrings, testTrash, "23");

        doReturn(backup).when(cranBackups).get(mockTransaction);
        doNothing()
                .when(storageService)
                .removeNonExistingPackagesFromRepo(backup.getRecentPackages(), mockTransaction.getRepositoryName());
        doNothing()
                .when(storageService)
                .removeNonExistingArchivePackagesFromRepo(
                        backup.getArchivePackages(), mockTransaction.getRepositoryName());
        doNothing().when(storageService).restoreTrash(testTrash);
        doNothing().when(storageService).setRepositoryVersion(mockTransaction.getRepositoryName(), "23");

        doNothing().when(storageService).generateArchiveRds(mockTransaction.getRepositoryName(), SOURCE_PATH);
        doNothing().when(storageService).generateArchiveRds(mockTransaction.getRepositoryName(), BINARY_PATH);

        cranRepositoryBackupService.restoreForTransaction(mockTransaction);

        verify(storageService)
                .removeNonExistingPackagesFromRepo(backup.getRecentPackages(), mockTransaction.getRepositoryName());
        verify(storageService)
                .removeNonExistingArchivePackagesFromRepo(
                        backup.getArchivePackages(), mockTransaction.getRepositoryName());
        verify(storageService).restoreTrash(testTrash);
        verify(storageService).setRepositoryVersion(mockTransaction.getRepositoryName(), "23");
        verify(storageService).generateArchiveRds(mockTransaction.getRepositoryName(), SOURCE_PATH);
        verify(storageService).generateArchiveRds(mockTransaction.getRepositoryName(), BINARY_PATH);
    }

    @Test
    public void removeBackupAfterSuccess() throws Exception {
        final Transaction mockTransaction = new Transaction(TEST_REPO, "123abc", 1);

        final List<String> recentPackages = getTestPackages(TEST_RECENT_DIR).stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .toList();

        final Map<String, List<Path>> recentBinaryPackages = new HashMap<>();
        recentBinaryPackages.put(BINARY_PATH, getTestPackages(TEST_BINARY_PACKAGES_RECENT_DIR_LOWER_VERSION));
        recentBinaryPackages.put(
                BINARY_HIGHER_VERSION_PATH, getTestPackages(TEST_BINARY_PACKAGES_RECENT_DIR_HIGHER_VERSION));

        final Map<String, List<Path>> archiveAllPackages = new HashMap<>();
        archiveAllPackages.put(SOURCE_PATH, getTestPackages(TEST_ARCHIVE_DIR));
        archiveAllPackages.put(BINARY_PATH, getTestPackages(TEST_BINARY_PACKAGES_ARCHIVE_DIR));

        final Map<String, List<String>> recentBinaryPackagesStrings = recentBinaryPackages.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .map(file -> file.getFileName().toString())
                        .collect(Collectors.toList())));

        final Map<String, List<String>> archiveAllPackagesStrings = archiveAllPackages.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .map(file -> file.getFileName().toString())
                        .collect(Collectors.toList())));

        final File testTrash = File.createTempFile("TRASH_", "430482309482309");

        final CranRepositoryBackup backup = new CranRepositoryBackup(
                recentPackages, recentBinaryPackagesStrings, archiveAllPackagesStrings, testTrash, "23");
        doReturn(backup).when(cranBackups).remove(mockTransaction);
        doNothing().when(storageService).emptyTrash(mockTransaction.getRepositoryName(), mockTransaction.getId());

        cranRepositoryBackupService.removeBackupAfterSuccess(mockTransaction);

        verify(cranBackups).remove(mockTransaction);
        verify(storageService).emptyTrash(mockTransaction.getRepositoryName(), mockTransaction.getId());
    }
}

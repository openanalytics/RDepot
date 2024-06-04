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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
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
    private File testPackagesDir;
    private static final String TEST_REPO = "testrepo123";

    private File[] getTestPackages(boolean archive) throws IOException {
        String subDir = archive ? "archive" : "recent";
        return testPackagesDir.toPath().resolve(subDir).toFile().listFiles();
    }

    @BeforeEach
    public void setUp() throws Exception {
        this.testPackagesDir = new File(TEST_PACKAGES_DIR);
        if (!testPackagesDir.exists() || !testPackagesDir.isDirectory())
            throw new FileNotFoundException(testPackagesDir.getAbsolutePath());
    }

    @Test
    public void backupForTransaction() throws Exception {
        final Transaction mockTransaction = new Transaction(TEST_REPO, "123abc", 1);
        final File testTrash = File.createTempFile("TRASH_", "430482309482309");

        doReturn(Arrays.stream(getTestPackages(false)).toList())
                .when(storageService)
                .getRecentPackagesFromRepository(TEST_REPO);
        doReturn(Arrays.stream(getTestPackages(true)).collect(Collectors.groupingBy(File::getName)))
                .when(storageService)
                .getArchiveFromRepository(TEST_REPO);
        doReturn(testTrash).when(storageService).initTrashDirectory(anyString());
        doReturn("23").when(storageService).getRepositoryVersion(TEST_REPO);
        doAnswer(invocationOnMock -> {
                    final CranRepositoryBackup backup = invocationOnMock.getArgument(1);
                    assertEquals(
                            Arrays.stream(getTestPackages(true))
                                    .map(File::getName)
                                    .collect(Collectors.toSet()),
                            new HashSet<>(backup.getArchivePackages()),
                            "archive packages were not backed-up");
                    assertEquals(
                            Arrays.stream(getTestPackages(false))
                                    .map(File::getName)
                                    .collect(Collectors.toSet()),
                            new HashSet<>(backup.getRecentPackages()),
                            "recent packages were not backed-up");
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
        final File testTrash = File.createTempFile("TRASH_", "430482309482309");

        doReturn(Arrays.stream(getTestPackages(false)).toList())
                .when(storageService)
                .getRecentPackagesFromRepository(TEST_REPO);
        doReturn(Arrays.stream(getTestPackages(true)).collect(Collectors.groupingBy(File::getName)))
                .when(storageService)
                .getArchiveFromRepository(TEST_REPO);
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

        doReturn(Arrays.stream(getTestPackages(false)).toList())
                .when(storageService)
                .getRecentPackagesFromRepository(TEST_REPO);
        doReturn(Arrays.stream(getTestPackages(true)).collect(Collectors.groupingBy(File::getName)))
                .when(storageService)
                .getArchiveFromRepository(TEST_REPO);
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
        final List<String> recentPackages =
                Arrays.stream(getTestPackages(false)).map(File::getName).toList();
        final List<String> archivePackages =
                Arrays.stream(getTestPackages(true)).map(File::getName).toList();
        final File testTrash = File.createTempFile("TRASH_", "430482309482309");

        final CranRepositoryBackup backup = new CranRepositoryBackup(recentPackages, archivePackages, testTrash, "23");

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
        doNothing().when(storageService).generateArchiveRds(mockTransaction.getRepositoryName());

        cranRepositoryBackupService.restoreForTransaction(mockTransaction);

        verify(storageService)
                .removeNonExistingPackagesFromRepo(backup.getRecentPackages(), mockTransaction.getRepositoryName());
        verify(storageService)
                .removeNonExistingArchivePackagesFromRepo(
                        backup.getArchivePackages(), mockTransaction.getRepositoryName());
        verify(storageService).restoreTrash(testTrash);
        verify(storageService).setRepositoryVersion(mockTransaction.getRepositoryName(), "23");
        verify(storageService).generateArchiveRds(mockTransaction.getRepositoryName());
    }

    @Test
    public void removeBackupAfterSuccess() throws Exception {
        final Transaction mockTransaction = new Transaction(TEST_REPO, "123abc", 1);
        final List<String> recentPackages =
                Arrays.stream(getTestPackages(false)).map(File::getName).toList();
        final List<String> archivePackages =
                Arrays.stream(getTestPackages(true)).map(File::getName).toList();
        final File testTrash = File.createTempFile("TRASH_", "430482309482309");

        final CranRepositoryBackup backup = new CranRepositoryBackup(recentPackages, archivePackages, testTrash, "23");
        doReturn(backup).when(cranBackups).remove(mockTransaction);
        doNothing().when(storageService).emptyTrash(mockTransaction.getRepositoryName(), mockTransaction.getId());

        cranRepositoryBackupService.removeBackupAfterSuccess(mockTransaction);

        verify(cranBackups).remove(mockTransaction);
        verify(storageService).emptyTrash(mockTransaction.getRepositoryName(), mockTransaction.getId());
    }
}

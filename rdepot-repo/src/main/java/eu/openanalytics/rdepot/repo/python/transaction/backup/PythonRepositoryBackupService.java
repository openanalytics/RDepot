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
package eu.openanalytics.rdepot.repo.python.transaction.backup;

import eu.openanalytics.rdepot.repo.exception.GetRepositoryVersionException;
import eu.openanalytics.rdepot.repo.exception.InitTrashDirectoryException;
import eu.openanalytics.rdepot.repo.exception.RestoreRepositoryException;
import eu.openanalytics.rdepot.repo.python.storage.PythonFileSystemStorageService;
import eu.openanalytics.rdepot.repo.transaction.Transaction;
import eu.openanalytics.rdepot.repo.transaction.UploadTransactionManager;
import eu.openanalytics.rdepot.repo.transaction.backup.implementations.AbstractRepositoryBackupService;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PythonRepositoryBackupService extends AbstractRepositoryBackupService<PythonRepositoryBackup> {

    private final PythonFileSystemStorageService storageService;
    private final UploadTransactionManager transactionManager;

    protected PythonRepositoryBackupService(
            PythonFileSystemStorageService storageService,
            UploadTransactionManager transactionManager,
            ConcurrentMap<Transaction, PythonRepositoryBackup> pythonBackups) {
        super(storageService, pythonBackups);
        this.storageService = storageService;
        this.transactionManager = transactionManager;
    }

    @Override
    public void backupForTransaction(Transaction transaction) {
        try {
            final List<String> recentPackages =
                    storageService.getRecentPackagesFromRepository(transaction.getRepositoryName()).stream()
                            .map(Path::getFileName)
                            .map(Path::toString)
                            .toList();
            final PythonRepositoryBackup backup = new PythonRepositoryBackup(
                    recentPackages,
                    storageService.initTrashDirectory(transaction.getId()),
                    storageService.getRepositoryVersion(transaction.getRepositoryName()));
            backups.put(transaction, backup);
        } catch (InitTrashDirectoryException | GetRepositoryVersionException | IOException e) {
            transactionManager.abortTransaction(transaction);
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void restoreForTransaction(Transaction transaction) throws RestoreRepositoryException {
        final PythonRepositoryBackup backup = backups.get(transaction);
        if (backup == null) {
            log.error("Trash directory for transaction not found. Transaction: {}", transaction);
            throw new RestoreRepositoryException(transaction.getRepositoryName());
        }
        storageService.removeNonExistingPackagesFromRepo(backup.getRecentPackages(), transaction.getRepositoryName());
        storageService.restoreTrash(backup.getTrashDirectory());
        storageService.setRepositoryVersion(transaction.getRepositoryName(), backup.getVersion());
    }
}

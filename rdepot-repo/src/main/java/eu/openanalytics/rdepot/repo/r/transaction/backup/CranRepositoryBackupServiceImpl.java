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
package eu.openanalytics.rdepot.repo.r.transaction.backup;

import eu.openanalytics.rdepot.repo.exception.GetRepositoryVersionException;
import eu.openanalytics.rdepot.repo.exception.InitTrashDirectoryException;
import eu.openanalytics.rdepot.repo.exception.RestoreRepositoryException;
import eu.openanalytics.rdepot.repo.r.storage.CranStorageService;
import eu.openanalytics.rdepot.repo.repository.RepositoryService;
import eu.openanalytics.rdepot.repo.transaction.Transaction;
import eu.openanalytics.rdepot.repo.transaction.UploadTransactionManager;
import eu.openanalytics.rdepot.repo.transaction.backup.implementations.AbstractRepositoryBackupService;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CranRepositoryBackupServiceImpl extends AbstractRepositoryBackupService<CranRepositoryBackup> {

    final CranStorageService storageService;
    final UploadTransactionManager transactionManager;
    final RepositoryService repositoryService;

    public CranRepositoryBackupServiceImpl(
            CranStorageService storageService,
            UploadTransactionManager transactionManager,
            RepositoryService repositoryService,
            ConcurrentMap<Transaction, CranRepositoryBackup> cranBackups) {
        super(storageService, cranBackups);
        this.storageService = storageService;
        this.transactionManager = transactionManager;
        this.repositoryService = repositoryService;
    }

    @Override
    public void backupForTransaction(Transaction transaction) {
        final List<String> recentPackages =
                storageService.getRecentPackagesFromRepository(transaction.getRepositoryName()).stream()
                        .map(File::getName)
                        .toList();
        final List<String> archivePackages =
                storageService.getArchiveFromRepository(transaction.getRepositoryName()).values().stream()
                        .flatMap(Collection::stream)
                        .map(File::getName)
                        .toList();

        File trashDirectory;
        String repositoryVersion;
        try {
            trashDirectory = storageService.initTrashDirectory(transaction.getId());
            repositoryVersion = storageService.getRepositoryVersion(transaction.getRepositoryName());
        } catch (InitTrashDirectoryException | GetRepositoryVersionException e) {
            transactionManager.abortTransaction(transaction);
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        final CranRepositoryBackup backup =
                new CranRepositoryBackup(recentPackages, archivePackages, trashDirectory, repositoryVersion);
        backups.put(transaction, backup);
    }

    @Override
    public void restoreForTransaction(Transaction transaction) throws RestoreRepositoryException {
        final CranRepositoryBackup backup = backups.get(transaction);
        if (backup == null) {
            log.error("Trash directory for transaction not found. Transaction: {}", transaction);
            throw new RestoreRepositoryException(transaction.getRepositoryName());
        }
        storageService.removeNonExistingPackagesFromRepo(backup.getRecentPackages(), transaction.getRepositoryName());
        storageService.removeNonExistingArchivePackagesFromRepo(
                backup.getArchivePackages(), transaction.getRepositoryName());
        storageService.restoreTrash(backup.getTrashDirectory());
        storageService.setRepositoryVersion(transaction.getRepositoryName(), backup.getVersion());
        try {
            storageService.generateArchiveRds(transaction.getRepositoryName());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RestoreRepositoryException(transaction.getRepositoryName());
        }
    }
}

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
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
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
        try {
            File trashDirectory = storageService.initTrashDirectory(transaction.getId());
            String repositoryVersion = storageService.getRepositoryVersion(transaction.getRepositoryName());

            final List<String> recentSourcePackages =
                    storageService.getRecentPackagesFromRepository(transaction.getRepositoryName()).stream()
                            .map(Path::getFileName)
                            .map(Path::toString)
                            .toList();

            final Map<String, List<String>> recentBinaryPackages =
                    storageService
                            .getRecentBinaryPackagesFromRepository(transaction.getRepositoryName())
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                                    .map(path -> path.getFileName().toString())
                                    .collect(Collectors.toList())));

            final Map<String, List<String>> archivePackages =
                    storageService.getArchiveFromRepository(transaction.getRepositoryName()).entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                                    .map(path -> path.getFileName().toString())
                                    .collect(Collectors.toList())));

            final CranRepositoryBackup backup = new CranRepositoryBackup(
                    recentSourcePackages, recentBinaryPackages, archivePackages, trashDirectory, repositoryVersion);
            backups.put(transaction, backup);
        } catch (InitTrashDirectoryException | GetRepositoryVersionException | IOException e) {
            transactionManager.abortTransaction(transaction);
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void restoreForTransaction(Transaction transaction) throws RestoreRepositoryException {
        final CranRepositoryBackup backup = backups.get(transaction);
        if (backup == null) {
            log.error("Trash directory for transaction not found. Transaction: {}", transaction);
            throw new RestoreRepositoryException(transaction.getRepositoryName());
        }
        storageService.removeNonExistingPackagesFromRepo(backup.getRecentPackages(), transaction.getRepositoryName());
        storageService.removeNonExistingBinaryPackagesFromRepo(
                backup.getRecentBinaryPackages(), transaction.getRepositoryName());
        storageService.removeNonExistingArchivePackagesFromRepo(
                backup.getArchivePackages(), transaction.getRepositoryName());
        storageService.restoreTrash(backup.getTrashDirectory());
        storageService.setRepositoryVersion(transaction.getRepositoryName(), backup.getVersion());
        try {
            for (String archivePath : backup.getArchivePackages().keySet()) {
                storageService.generateArchiveRds(transaction.getRepositoryName(), archivePath);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RestoreRepositoryException(transaction.getRepositoryName());
        }
    }
}

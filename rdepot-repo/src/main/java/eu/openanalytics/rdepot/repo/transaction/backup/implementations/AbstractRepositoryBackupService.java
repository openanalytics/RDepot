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
package eu.openanalytics.rdepot.repo.transaction.backup.implementations;

import eu.openanalytics.rdepot.repo.exception.EmptyTrashException;
import eu.openanalytics.rdepot.repo.storage.StorageService;
import eu.openanalytics.rdepot.repo.transaction.Transaction;
import eu.openanalytics.rdepot.repo.transaction.backup.RepositoryBackup;
import eu.openanalytics.rdepot.repo.transaction.backup.RepositoryBackupService;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements only {@link #removeBackupAfterSuccess} method.
 */
@Slf4j
public abstract class AbstractRepositoryBackupService<T extends RepositoryBackup> implements RepositoryBackupService {

    protected final ConcurrentMap<Transaction, T> backups;
    private final StorageService<?> storageService;

    protected AbstractRepositoryBackupService(StorageService<?> storageService, ConcurrentMap<Transaction, T> backups) {
        this.backups = backups;
        this.storageService = storageService;
    }

    @Override
    public void removeBackupAfterSuccess(Transaction transaction) {
        backups.remove(transaction);
        try {
            storageService.emptyTrash(transaction.getRepositoryName(), transaction.getId());
        } catch (EmptyTrashException e) {
            log.error(e.getMessage(), e);
        }
    }
}

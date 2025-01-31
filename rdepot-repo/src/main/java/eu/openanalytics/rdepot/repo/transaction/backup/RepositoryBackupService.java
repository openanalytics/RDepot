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
package eu.openanalytics.rdepot.repo.transaction.backup;

import eu.openanalytics.rdepot.repo.exception.RestoreRepositoryException;
import eu.openanalytics.rdepot.repo.transaction.Transaction;

/**
 * Manages {@link RepositoryBackup Repository Backups}.
 */
public interface RepositoryBackupService {
    /**
     * Creates a backup object for transaction.
     * Should be called before the first chunk gets processed.
     */
    void backupForTransaction(Transaction transaction);

    /**
     * Restores transaction to the state
     * from before the first chunk was submitted.
     * Should be called in case of failure or transaction expiration.
     */
    void restoreForTransaction(Transaction transaction) throws RestoreRepositoryException;

    /**
     *  Removes the backup when transaction has been completed successfully.
     */
    void removeBackupAfterSuccess(Transaction transaction);
}

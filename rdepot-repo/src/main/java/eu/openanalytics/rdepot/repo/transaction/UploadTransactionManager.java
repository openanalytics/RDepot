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
package eu.openanalytics.rdepot.repo.transaction;

import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.repository.Repository;
import java.util.Optional;

/**
 * Manages {@link Transaction Transactions}.
 */
public interface UploadTransactionManager {
    /**
     * Finds transaction with given ID.
     */
    Optional<Transaction> findById(String id);

    /**
     * Finds an ongoing transaction for given repository.
     */
    Optional<Transaction> findByRepo(Repository repository);

    /**
     * Initializes transaction for given upload request.
     * Should be called before the first chunk gets processed.
     */
    Transaction initTransactionForRequest(SynchronizeRepositoryRequestBody request);

    /**
     *  Checks if there is an ongoing transaction for which the last chunk was submitted
     *  earlier than the timeout period specified in configuration.
     *  It should be used in combination with {@link #abortTransaction abortTransaction} method.
     */
    boolean hasTransactionExpired(Transaction transaction);

    /**
     * Should be called after the last chunk has been successfully processed.
     * If there was an error then the transaction
     * should be {@link #abortTransaction aborted} instead.
     */
    void finishTransaction(Transaction transaction);

    /**
     * Aborts transaction in case of failure.
     * Can be used either after an exception is thrown
     * or when the transaction gets expired.
     */
    void abortTransaction(Transaction transaction);
}

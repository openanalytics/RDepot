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
import eu.openanalytics.rdepot.repo.repository.RepositoryService;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link UploadTransactionManager}
 * that makes sure that no collisions occur
 * even if two requests come at the same time from two different threads.
 */
@Component
public class ThreadSafeUploadTransactionManager implements UploadTransactionManager {

    private final RepositoryService repositoryService;
    private final ConcurrentMap<String, Transaction> activeTransactionsById;
    private final ConcurrentMap<Repository, Transaction> activeTransactionsByRepo;

    @Value("${transaction.timeout}")
    private int transactionTimeout;

    public ThreadSafeUploadTransactionManager(
            RepositoryService repositoryService,
            ConcurrentMap<String, Transaction> activeTransactionsById,
            ConcurrentMap<Repository, Transaction> activeTransactionsByRepo) {
        this.repositoryService = repositoryService;
        this.activeTransactionsById = activeTransactionsById;
        this.activeTransactionsByRepo = activeTransactionsByRepo;
    }

    @Override
    public Optional<Transaction> findById(String id) {
        return Optional.ofNullable(activeTransactionsById.get(id));
    }

    @Override
    public Optional<Transaction> findByRepo(Repository repository) {
        return Optional.ofNullable(activeTransactionsByRepo.get(repository));
    }

    private String generateId() {
        String id;
        do {
            id = UUID.randomUUID().toString();
        } while (activeTransactionsById.containsKey(id));
        return id;
    }

    @Override
    public Transaction initTransactionForRequest(SynchronizeRepositoryRequestBody request) {
        Repository repository = repositoryService
                .findByNameOrCreate(request.getRepository(), request.getTechnology())
                .orElseThrow(IllegalStateException::new);
        final String id = generateId();
        final Transaction transaction = new Transaction(
                request.getRepository(), id, Integer.parseInt(request.getPage().split("/")[1]));

        activeTransactionsById.put(id, transaction);
        activeTransactionsByRepo.put(repository, transaction);

        return transaction;
    }

    @Override
    public boolean hasTransactionExpired(Transaction transaction) {
        return Duration.between(transaction.getLastChunkArrivalTime(), Instant.now())
                        .getSeconds()
                >= transactionTimeout;
    }

    @Override
    public void finishTransaction(Transaction transaction) {
        transaction.getFinished().complete(null);
        removeTransactionFromList(transaction);
    }

    private void removeTransactionFromList(Transaction transaction) {
        activeTransactionsById.remove(transaction.getId());
        activeTransactionsByRepo.values().removeAll(Collections.singleton(transaction));
    }

    @Override
    public void abortTransaction(Transaction transaction) {
        transaction.getFinished().cancel(true);
        removeTransactionFromList(transaction);
    }
}

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

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;

/**
 * Transaction is an object which links a series of request chunks with repository.
 * An upload request is divided into smaller chunks
 * for optimization and more efficient error handling.
 * However, a correct order of chunks has to be preserved.
 * It also makes sure that no two unrelated chunks will modify the same repository,
 * leaving it in inconsistent state.
 * Transactions are managed by {@link UploadTransactionManager}.
 */
@Getter
public class Transaction {

    private final CompletableFuture<Void> finished = new CompletableFuture<>();

    private final String repositoryName;

    private final String id;

    private final int chunkCount;

    private int numberOfLastCompletedChunk = 0;
    private Instant lastChunkArrivalTime = Instant.now();

    public Transaction(String repositoryName, String id, int chunkCount) {
        this.repositoryName = repositoryName;
        this.id = id;
        this.chunkCount = chunkCount;
    }

    public void incrementChunkNumber() {
        numberOfLastCompletedChunk++;
        lastChunkArrivalTime = Instant.now();
    }
}

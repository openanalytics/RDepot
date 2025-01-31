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
package eu.openanalytics.rdepot.repo.chunks.processing;

import eu.openanalytics.rdepot.repo.chunks.processing.exceptions.RequestProcessingException;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.transaction.Transaction;

/**
 * Performs business logic for specific kinds of chunks.
 */
public interface UploadRequestChunkProcessor<REQ extends SynchronizeRepositoryRequestBody> {
    /**
     * It is called for chunks that are not the last ones for given transaction.
     */
    void processNotLastChunk(REQ requestBody, Transaction transaction) throws RequestProcessingException;

    /**
     * It is called for the last chunk for given transaction.
     */
    void processLastChunk(REQ requestBody, Transaction transaction) throws RequestProcessingException;
}

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
package eu.openanalytics.rdepot.repo.validation;

import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.transaction.Transaction;

/**
 * Validates chunk.
 */
public interface ChunkValidator<T extends SynchronizeRepositoryRequestBody> {

    /**
     * Checks if chunk is valid in the context of given transaction.
     * It will make sure that it belongs to an ongoing transaction
     * and is next in order.
     */
    boolean isValidConsecutiveChunk(T chunk, Transaction transaction);
}

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
package eu.openanalytics.rdepot.repo.validation;

import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.transaction.Transaction;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;

/**
 * Default implementation of {@link ChunkValidator}.
 */
@AllArgsConstructor
public abstract class AbstractChunkValidator<T extends SynchronizeRepositoryRequestBody> implements ChunkValidator<T> {

    private final IntegrityValidator<T> integrityValidator;

    @Override
    public boolean isValidConsecutiveChunk(T chunk, Transaction transaction) {
        final String pageStr = chunk.getPage();
        if (StringUtils.isBlank(pageStr)) return false;

        final String[] tokens = pageStr.split("/");
        if (tokens.length != 2) return false;

        try {
            final int currentPage = Integer.parseInt(tokens[0]);
            final int pageCount = Integer.parseInt(tokens[1]);

            return pageCount == transaction.getChunkCount()
                    && currentPage == transaction.getNumberOfLastCompletedChunk() + 1
                    && integrityValidator.areHashesValid(chunk);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

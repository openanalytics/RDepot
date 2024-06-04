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
package eu.openanalytics.rdepot.repo.chunks.processing.implementations;

import eu.openanalytics.rdepot.repo.chunks.processing.UploadRequestChunkProcessor;
import eu.openanalytics.rdepot.repo.chunks.processing.exceptions.RequestProcessingException;
import eu.openanalytics.rdepot.repo.exception.GetRepositoryVersionException;
import eu.openanalytics.rdepot.repo.exception.RepositoryVersionMismatchException;
import eu.openanalytics.rdepot.repo.exception.StorageException;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.storage.StorageService;
import eu.openanalytics.rdepot.repo.transaction.Transaction;
import java.io.FileNotFoundException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation for {@link UploadRequestChunkProcessor}.
 */
@Slf4j
public abstract class UploadRequestChunkProcessorImpl<REQ extends SynchronizeRepositoryRequestBody>
        implements UploadRequestChunkProcessor<REQ> {

    private final StorageService<REQ> storageService;

    public UploadRequestChunkProcessorImpl(StorageService<REQ> storageService) {
        this.storageService = storageService;
    }

    @Override
    public void processNotLastChunk(REQ requestBody, Transaction transaction) throws RequestProcessingException {
        final String repositoryName = requestBody.getRepository();
        try {
            final String actualVersion = storageService.getRepositoryVersion(repositoryName);
            final String expectedVersion = requestBody.getVersionBefore();

            if (!Objects.equals(actualVersion, expectedVersion))
                throw new RepositoryVersionMismatchException(requestBody);

            storageService.storeAndDeleteFiles(requestBody);
            storageService.boostRepositoryVersion(repositoryName);
        } catch (GetRepositoryVersionException
                | RepositoryVersionMismatchException
                | StorageException
                | FileNotFoundException e) {
            log.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
            log.debug("Trying to restore repository...");
            throw new RequestProcessingException();
        }
    }

    @Override
    public void processLastChunk(REQ requestBody, Transaction transaction) throws RequestProcessingException {
        processNotLastChunk(requestBody, transaction);
        storageService.handleLastChunk(requestBody, transaction.getRepositoryName());
    }
}

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
package eu.openanalytics.rdepot.repo.chunks.coordination;

import eu.openanalytics.rdepot.repo.chunks.processing.UploadRequestChunkProcessor;
import eu.openanalytics.rdepot.repo.chunks.processing.UploadRequestProcessor;
import eu.openanalytics.rdepot.repo.chunks.processing.exceptions.RequestProcessingException;
import eu.openanalytics.rdepot.repo.exception.InvalidRequestPageNumberException;
import eu.openanalytics.rdepot.repo.exception.RestoreRepositoryException;
import eu.openanalytics.rdepot.repo.model.RequestProcessingResult;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.repository.Repository;
import eu.openanalytics.rdepot.repo.repository.RepositoryService;
import eu.openanalytics.rdepot.repo.transaction.Transaction;
import eu.openanalytics.rdepot.repo.transaction.UploadTransactionManager;
import eu.openanalytics.rdepot.repo.transaction.backup.RepositoryBackupService;
import eu.openanalytics.rdepot.repo.validation.ChunkValidator;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

/**
 * This class serves as an entrypoint to the business logic of publication,
 * should be called from the controller method.
 * Dispatches chunks to proper services based on their content.
 * It ensures that transactions are well-coordinated so that they do not collide
 * in case of multiple requests made at once to the same repository.
 * It will also perform basic validation on the chunk.
 * @param <REQ> should be extended by technology-specific request body DTO
 */
@Slf4j
public abstract class UploadRequestChunkCoordinator<REQ extends SynchronizeRepositoryRequestBody>
        implements UploadRequestProcessor<REQ> {

    private final UploadTransactionManager transactionManager;
    private final ChunkValidator<REQ> chunkValidator;
    private final RepositoryService repositoryService;
    private final UploadRequestChunkProcessor<REQ> uploadRequestChunkProcessor;
    private final RepositoryBackupService repositoryBackupService;

    @Value("${transaction.timeout}")
    private int transactionTimeout;

    protected UploadRequestChunkCoordinator(
            UploadTransactionManager transactionManager,
            ChunkValidator<REQ> chunkValidator,
            RepositoryService repositoryService,
            UploadRequestChunkProcessor<REQ> uploadRequestChunkProcessor,
            RepositoryBackupService backupService) {
        this.transactionManager = transactionManager;
        this.chunkValidator = chunkValidator;
        this.repositoryService = repositoryService;
        this.uploadRequestChunkProcessor = uploadRequestChunkProcessor;
        this.repositoryBackupService = backupService;
    }

    private RequestProcessingResult processChunkIfConsecutive(REQ requestBody, Transaction transaction) {
        if (chunkValidator.isValidConsecutiveChunk(requestBody, transaction)) {
            try {
                processRequest(requestBody, transaction);
            } catch (RequestProcessingException e) {
                log.error(e.getMessage(), e);
                handleFailure(transaction);
                return RequestProcessingResult.SERVER_ERROR;
            }
            return RequestProcessingResult.SUCCESS;
        }
        handleFailure(transaction);
        return RequestProcessingResult.CLIENT_ERROR;
    }

    @Override
    public RequestProcessingResult submit(REQ requestBody) {
        final Optional<Transaction> currentTransactionOpt = transactionManager.findById(requestBody.getId());
        if (currentTransactionOpt.isPresent()) { // This chunk is a part of an ongoing transaction
            final Transaction transaction = currentTransactionOpt.get();

            if (transactionManager.hasTransactionExpired(transaction)) {
                handleFailure(transaction);
            } else {
                return processChunkIfConsecutive(requestBody, transaction);
            }
        }

        // This chunk is not a part of any ongoing transaction thus it should be first
        try {
            if (!requestBody.isFirstChunk()) {
                log.error("Attempted to submit chunk for non-existing transaction.");
            }
        } catch (InvalidRequestPageNumberException e) {
            log.debug("Invalid request page number.");
            return RequestProcessingResult.CLIENT_ERROR;
        }

        return processFirstChunk(requestBody);
    }

    protected RequestProcessingResult processFirstChunk(REQ requestBody) {
        final Optional<Repository> repositoryOpt =
                repositoryService.findByNameOrCreate(requestBody.getRepository(), requestBody.getTechnology());
        if (repositoryOpt.isEmpty()) {

            return RequestProcessingResult.CLIENT_ERROR;
        }

        final Repository repository = repositoryOpt.get();
        Transaction transaction;
        synchronized (repository) {
            final Optional<Transaction> currentTransactionSameRepoOpt = transactionManager.findByRepo(repository);

            if (currentTransactionSameRepoOpt.isPresent()) {
                final Transaction currentTransactionSameRepo = currentTransactionSameRepoOpt.get();
                if (transactionManager.hasTransactionExpired(currentTransactionSameRepo)) {
                    handleFailure(currentTransactionSameRepo);
                } else {
                    return RequestProcessingResult.CLIENT_ERROR;
                }
            }
            transaction = initTransaction(requestBody);
            requestBody.setId(transaction.getId());
        }
        return processChunkIfConsecutive(requestBody, transaction);
    }

    protected void processRequest(REQ requestBody, Transaction transaction) throws RequestProcessingException {
        try {
            if (requestBody.isLastChunk()) {
                uploadRequestChunkProcessor.processLastChunk(requestBody, transaction);
                handleLastChunk(transaction);
            } else {
                uploadRequestChunkProcessor.processNotLastChunk(requestBody, transaction);
            }
            transaction.incrementChunkNumber();
        } catch (RequestProcessingException e) {
            handleFailure(transaction);
            throw new RequestProcessingException();
        } catch (InvalidRequestPageNumberException e) {
            throw new RuntimeException(e);
        }
    }

    protected void handleLastChunk(Transaction transaction) {
        transactionManager.finishTransaction(transaction);
        repositoryBackupService.removeBackupAfterSuccess(transaction);
    }

    protected void handleFailure(Transaction transaction) {
        transactionManager.abortTransaction(transaction);
        try {
            repositoryBackupService.restoreForTransaction(transaction);
        } catch (RestoreRepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    protected Transaction initTransaction(REQ requestBody) {
        final Transaction transaction = transactionManager.initTransactionForRequest(requestBody);
        repositoryBackupService.backupForTransaction(transaction);
        return transaction;
    }
}

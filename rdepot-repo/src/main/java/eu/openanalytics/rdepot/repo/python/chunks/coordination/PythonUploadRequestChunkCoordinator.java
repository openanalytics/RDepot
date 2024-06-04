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
package eu.openanalytics.rdepot.repo.python.chunks.coordination;

import eu.openanalytics.rdepot.repo.chunks.coordination.UploadRequestChunkCoordinator;
import eu.openanalytics.rdepot.repo.python.model.SynchronizePythonRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.python.transaction.backup.PythonRepositoryBackupService;
import eu.openanalytics.rdepot.repo.python.upload.chunks.processing.PythonUploadRequestChunkProcessor;
import eu.openanalytics.rdepot.repo.python.validation.PythonChunkValidator;
import eu.openanalytics.rdepot.repo.repository.implementations.RepositoryServiceImpl;
import eu.openanalytics.rdepot.repo.transaction.ThreadSafeUploadTransactionManager;
import org.springframework.stereotype.Component;

@Component
public class PythonUploadRequestChunkCoordinator
        extends UploadRequestChunkCoordinator<SynchronizePythonRepositoryRequestBody> {
    public PythonUploadRequestChunkCoordinator(
            ThreadSafeUploadTransactionManager transactionManager,
            PythonChunkValidator chunkValidator,
            RepositoryServiceImpl repositoryService,
            PythonUploadRequestChunkProcessor uploadRequestProcessor,
            PythonRepositoryBackupService backupService) {
        super(transactionManager, chunkValidator, repositoryService, uploadRequestProcessor, backupService);
    }
}

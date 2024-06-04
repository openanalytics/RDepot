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
package eu.openanalytics.rdepot.repo.r.upload;

import eu.openanalytics.rdepot.repo.chunks.coordination.UploadRequestChunkCoordinator;
import eu.openanalytics.rdepot.repo.r.model.SynchronizeCranRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.r.transaction.backup.CranRepositoryBackupServiceImpl;
import eu.openanalytics.rdepot.repo.r.validation.CranChunkValidator;
import eu.openanalytics.rdepot.repo.repository.implementations.RepositoryServiceImpl;
import eu.openanalytics.rdepot.repo.transaction.UploadTransactionManager;
import org.springframework.stereotype.Component;

@Component
public class CranUploadRequestChunkCoordinator
        extends UploadRequestChunkCoordinator<SynchronizeCranRepositoryRequestBody> {
    public CranUploadRequestChunkCoordinator(
            UploadTransactionManager transactionManager,
            CranChunkValidator chunkValidator,
            RepositoryServiceImpl repositoryService,
            CranUploadRequestChunkProcessor uploadRequestProcessor,
            CranRepositoryBackupServiceImpl backupService) {
        super(transactionManager, chunkValidator, repositoryService, uploadRequestProcessor, backupService);
    }
}

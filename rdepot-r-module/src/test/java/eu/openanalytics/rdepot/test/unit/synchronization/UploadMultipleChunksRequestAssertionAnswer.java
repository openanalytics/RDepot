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
package eu.openanalytics.rdepot.test.unit.synchronization;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.synchronization.RepoResponse;
import java.io.File;
import java.util.List;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public class UploadMultipleChunksRequestAssertionAnswer extends UploadChunkRequestAssertionAnswer {

    private final String expectedVersionBefore;
    private final String expectedVersionAfter;
    private final List<String> expectedToDelete;
    private final List<String> expectedToDeleteFromArchive;
    private final List<List<File>> packagesToUpload;
    private final List<List<File>> packagesToUploadToArchive;
    private String expectedId = "";

    public UploadMultipleChunksRequestAssertionAnswer(
            int chunksToSend,
            Repository repository,
            final String expectedVersionBefore,
            final String expectedVersionAfter,
            final List<String> expectedToDelete,
            final List<String> expectedToDeleteFromArchive,
            final List<List<File>> packagesToUpload,
            final List<List<File>> packagesToUploadToArchive) {
        super(chunksToSend, repository);
        this.expectedVersionAfter = expectedVersionAfter;
        this.expectedVersionBefore = expectedVersionBefore;
        this.expectedToDelete = expectedToDelete;
        this.expectedToDeleteFromArchive = expectedToDeleteFromArchive;
        this.packagesToUpload = packagesToUpload;
        this.packagesToUploadToArchive = packagesToUploadToArchive;
    }

    @Override
    public ResponseEntity<RepoResponse> answer(InvocationOnMock invocation) throws Throwable {
        final List<List<FileSystemResource>> files = packagesToUpload.stream()
                .map(l -> l.stream().map(f -> new FileSystemResource(f)).toList())
                .toList();
        final List<List<FileSystemResource>> archiveFiles = packagesToUploadToArchive.stream()
                .map(l -> l.stream().map(f -> new FileSystemResource(f)).toList())
                .toList();

        @SuppressWarnings("unchecked")
        MultiValueMap<String, Object> entity =
                ((HttpEntity<MultiValueMap<String, Object>>) invocation.getArgument(1)).getBody();

        assertChunk(
                entity,
                new UploadChunkRequestAssertion(
                        expectedId,
                        expectedVersionBefore,
                        expectedVersionAfter,
                        List.of((callCount + 1) + "/" + chunksToSend),
                        expectedToDelete,
                        expectedToDeleteFromArchive,
                        callCount < files.size() ? files.get(callCount) : null,
                        callCount < archiveFiles.size() ? archiveFiles.get(callCount) : null));

        ResponseEntity<RepoResponse> response = super.answer(invocation);
        expectedId = response.getBody().getId();
        return response;
    }
}

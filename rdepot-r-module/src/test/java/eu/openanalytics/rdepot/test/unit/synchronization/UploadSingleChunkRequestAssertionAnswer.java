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
package eu.openanalytics.rdepot.test.unit.synchronization;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.synchronization.RepoResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public class UploadSingleChunkRequestAssertionAnswer extends UploadChunkRequestAssertionAnswer {

    private final String expectedId;
    private final String expectedVersionBefore;
    private final String expectedVersionAfter;
    private final List<String> packagesToDelete;
    private final List<String> packagesToDeleteFromArchive;
    private final List<File> packagesToUpload;
    private final List<File> packagesToUploadToArchive;
    private final Map<String, String> expectedToUploadPaths;
    private final Map<String, String> expectedToUploadToArchivePaths;
    private final Map<String, String> expectedToDeletePaths;
    private final Map<String, String> expectedToDeleteFromArchivePaths;

    public UploadSingleChunkRequestAssertionAnswer(
            Repository repository,
            final String expectedId,
            final String expectedVersionBefore,
            final String expectedVersionAfter,
            final List<String> packagesToDelete,
            final List<String> packagesToDeleteFromArchive,
            final List<File> packagesToUpload,
            final List<File> packagesToUploadToArchive,
            final Map<String, String> expectedToUploadPaths,
            final Map<String, String> expectedToUploadToArchivePaths,
            final Map<String, String> expectedToDeletePaths,
            final Map<String, String> expectedToDeleteFromArchivePaths) {
        super(1, repository);
        this.expectedId = expectedId;
        this.expectedVersionBefore = expectedVersionBefore;
        this.expectedVersionAfter = expectedVersionAfter;
        this.packagesToDelete = packagesToDelete;
        this.packagesToDeleteFromArchive = packagesToDeleteFromArchive;
        this.packagesToUpload = packagesToUpload;
        this.packagesToUploadToArchive = packagesToUploadToArchive;
        this.expectedToUploadPaths = expectedToUploadPaths;
        this.expectedToUploadToArchivePaths = expectedToUploadToArchivePaths;
        this.expectedToDeletePaths = expectedToDeletePaths;
        this.expectedToDeleteFromArchivePaths = expectedToDeleteFromArchivePaths;
        ;
    }

    @Override
    public ResponseEntity<RepoResponse> answer(InvocationOnMock invocation) throws Throwable {

        List<FileSystemResource> files = new ArrayList<>();
        files.addAll(
                packagesToUpload.stream().map(f -> new FileSystemResource(f)).toList());

        List<FileSystemResource> filesArchive = new ArrayList<>();
        filesArchive.addAll(packagesToUploadToArchive.stream()
                .map(f -> new FileSystemResource(f))
                .toList());

        @SuppressWarnings("unchecked")
        MultiValueMap<String, Object> entity =
                ((HttpEntity<MultiValueMap<String, Object>>) invocation.getArgument(1)).getBody();
        assertChunk(
                entity,
                new UploadChunkRequestAssertion(
                        expectedId,
                        expectedVersionBefore,
                        expectedVersionAfter,
                        List.of("1/1"),
                        packagesToDelete,
                        packagesToDeleteFromArchive,
                        files,
                        filesArchive,
                        expectedToUploadPaths,
                        expectedToUploadToArchivePaths,
                        expectedToDeletePaths,
                        expectedToDeleteFromArchivePaths));

        return super.answer(invocation);
    }
}

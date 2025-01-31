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

import static org.junit.jupiter.api.Assertions.*;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.synchronization.RepoResponse;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

@RequiredArgsConstructor
public class UploadChunkRequestAssertionAnswer implements Answer<ResponseEntity<RepoResponse>> {

    protected final int chunksToSend;
    protected final Repository repository;
    protected int callCount = 0;

    protected void assertChunk(MultiValueMap<String, Object> entity, UploadChunkRequestAssertion assertion) {
        if (callCount == 0) {
            assertTrue(
                    List.of(assertion.expectedId()).equals(entity.get("id")),
                    "For the first chunk, the id should be empty.");
            assertEquals(
                    List.of(assertion.expectedVersionBefore()),
                    entity.get("version_before"),
                    "Incorrect version before the request.");
            assertEquals(
                    List.of(assertion.expectedVersionAfter()),
                    entity.get("version_after"),
                    "Incorrect version after the request.");
            assertEquals(assertion.expectedPages(), entity.get("page"), "Incorrect number of chunks.");
            assertEquals(assertion.expectedToDelete(), entity.get("to_delete"), "Incorrect packages to delete.");
        }

        List<File> filesFromAssertion = assertion.filesToUpload().stream()
                .map(f -> f.getFile().getAbsoluteFile())
                .collect(Collectors.toList());
        List<File> filesFromEntity = entity.get("files").stream()
                .map(f -> ((FileSystemResource) f).getFile().getAbsoluteFile())
                .collect(Collectors.toList());
        assertEquals(filesFromAssertion, filesFromEntity);
    }

    @Override
    public ResponseEntity<RepoResponse> answer(InvocationOnMock invocation) throws Throwable {
        if (callCount >= chunksToSend) fail("To many chunks have been sent.");

        callCount++;
        final RepoResponse repoResponse = new RepoResponse();
        repoResponse.setId("" + repository.getId());
        repoResponse.setMessage("OK");

        return ResponseEntity.ok(repoResponse);
    }
}

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.synchronization.RepoResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class UploadChunkRequestAssertionAnswer 
	implements Answer<ResponseEntity<RepoResponse>> {
	
	protected final int chunksToSend;
	protected final Repository repository;
	protected int callCount = 0;
	
	protected void assertChunk(MultiValueMap<String,Object> entity, 
			UploadChunkRequestAssertion assertion) {
		if(callCount == 0) {
			assertTrue(List.of(assertion.getExpectedId()).equals(entity.get("id")), "For the first chunk, the id should be empty.");
			assertEquals(List.of(assertion.getExpectedVersionBefore()), entity.get("version_before"), "Incorrect version before the request.");
			assertEquals(List.of(assertion.getExpectedVersionAfter()), entity.get("version_after"), "Incorrect version after the request.");
			assertEquals(assertion.getExpectedPages(), entity.get("page"), "Incorrect number of chunks.");
			assertEquals(assertion.getExpectedToDelete(), entity.get("to_delete"), "Incorrect packages to delete.");
			assertEquals(assertion.getExpectedToDeleteFromArchive(), 
					entity.get("to_delete_archive"), "Incorrect packages to delete from archive.");
		}
		assertEquals(assertion.getFilesToUpload(), entity.get("files"), "Incorrect files uploaded.");
		assertEquals(assertion.getFilesToUploadToArchive(), entity.get("files_archive"), "Incorrect files uploaded to archive.");
	}
	
	@Override
	public ResponseEntity<RepoResponse> answer(InvocationOnMock invocation) throws Throwable {
		if(callCount >= chunksToSend)
			fail("To many chunks have been sent.");
		
		callCount++;
		final RepoResponse repoResponse = new RepoResponse();
		repoResponse.setId("" + repository.getId());
		repoResponse.setMessage("OK");
		return ResponseEntity.ok(repoResponse);
	}
}

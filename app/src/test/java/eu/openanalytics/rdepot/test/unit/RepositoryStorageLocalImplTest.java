/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.test.unit;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.MultiValueMap;

import eu.openanalytics.rdepot.model.SynchronizeRepositoryRequestBody;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryStorageLocalImplTest {
	
	private final String TEMP_FILE_PREFIX = "testpackage";
	private final String TEMP_FILE_SUFFIX = ".tar.gz";
	private final String TEMP_PACKAGES_FILE_PREFIX = "PACKAGES";
	private final String TEMP_PACKAGESGZ_FILE_PREFIX = "PACKAGES";
	private final String TEMP_PACKAGES_FILE_SUFFIX = "";
	private final String TEMP_PACKAGESGZ_FILE_SUFFIX = ".gz";

	@Test
	public void splitRequestIntoChunks() throws IOException {
		int elementsPerChunk = 5;
		int chunkCount = 3;
		List<File> recentToUpload = new ArrayList<File>();
		List<File> archiveToUpload = new ArrayList<File>();
		List<String> recentToDelete = new ArrayList<String>();
		List<String> archiveToDelete = new ArrayList<String>();
		
		for(int i = 0; i < elementsPerChunk * chunkCount; i++) {
			recentToUpload.add(Files.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX).toFile());
			archiveToUpload.add(Files.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX).toFile());
			
			recentToDelete.add(RandomStringUtils.randomAlphabetic(10));
			archiveToDelete.add(RandomStringUtils.randomAlphabetic(10));
		}
		
		SynchronizeRepositoryRequestBody requestBody = new SynchronizeRepositoryRequestBody(
				recentToUpload, archiveToUpload, recentToDelete, archiveToDelete, "1", 
				Files.createTempFile(TEMP_PACKAGES_FILE_PREFIX, TEMP_PACKAGES_FILE_SUFFIX).toFile(),
				Files.createTempFile(TEMP_PACKAGESGZ_FILE_PREFIX, TEMP_PACKAGESGZ_FILE_SUFFIX).toFile(),
				Files.createTempFile(TEMP_PACKAGES_FILE_PREFIX, TEMP_PACKAGES_FILE_SUFFIX).toFile(),
				Files.createTempFile(TEMP_PACKAGESGZ_FILE_PREFIX, TEMP_PACKAGESGZ_FILE_SUFFIX).toFile()
		);
		
		List<MultiValueMap<String, Object>> chunks = requestBody.toChunks(elementsPerChunk);
		
		assertEquals("Chunk count is not correct!", chunkCount, chunks.size());
		
		for(int i = 0; i < chunks.size(); i++) {
			MultiValueMap<String, Object> chunk = chunks.get(i);
			
			List<Object> files = chunk.get("files");
			List<Object> filesArchive = chunk.get("files_archive");
			String versionBefore = (String)chunk.get("version_before").get(0);
			String versionAfter = (String)chunk.get("version_after").get(0);
			String page = (String)chunk.get("page").get(0);
			List<Object> toDelete = chunk.get("to_delete");
			List<Object> toDeleteArchive = chunk.get("to_delete_archive");
			
			if(i == 0) {
				assertEquals(elementsPerChunk + 2, files.size());
				assertEquals(elementsPerChunk + 2, filesArchive.size());
				assertEquals(elementsPerChunk * chunkCount, toDelete.size());
				assertEquals(elementsPerChunk * chunkCount, toDeleteArchive.size());
			} else {
				assertEquals(elementsPerChunk, files.size());
				assertEquals(elementsPerChunk, filesArchive.size());
			}
			
			String expectedPage = (i + 1) + "/" + chunkCount;
			String expectedVersionBefore = String.valueOf(i + 1);
			String expectedVersionAfter = String.valueOf(i + 2);
			
			assertEquals(expectedPage, page);
			assertEquals(expectedVersionBefore, versionBefore);
			assertEquals(expectedVersionAfter, versionAfter);
			
		}
	}
	
}
;
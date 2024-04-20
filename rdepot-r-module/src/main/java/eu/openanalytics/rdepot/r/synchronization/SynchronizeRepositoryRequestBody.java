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
package eu.openanalytics.rdepot.r.synchronization;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.collections4.ListUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class SynchronizeRepositoryRequestBody {
	
	List<File> packagesToUpload;
	List<File> packagesToUploadToArchive;
	List<String> packagesToDelete;
	List<String> packagesToDeleteFromArchive;
	String versionBefore;
	String versionAfter;
	File packagesFile;
	File packagesGzFile;
	File packagesFileArchive;
	File packagesGzFileArchive;
	
	public List<MultiValueMap<String, Object>> toChunks(int elementsPerChunk) {
		List<MultiValueMap<String, Object>> chunks = new ArrayList<>();
				
		List<List<File>> recentChunks = new ArrayList<>(ListUtils.partition(packagesToUpload, elementsPerChunk));
		List<List<File>> archiveChunks = new ArrayList<>(ListUtils.partition(packagesToUploadToArchive, elementsPerChunk));
		
		int pageCount = Integer.max(recentChunks.size(), archiveChunks.size());
		int currentPage = 1;
		
		int currentVersion = Integer.parseInt(this.versionBefore);
		
		while(!recentChunks.isEmpty() || !archiveChunks.isEmpty()) {
			MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
			
			if(!recentChunks.isEmpty()) {
				List<File> recentChunk = recentChunks.remove(0);
				recentChunk.forEach(file -> map.add("files", new FileSystemResource(file)));
			}
			
			if(!archiveChunks.isEmpty()) {
				List<File> archiveChunk = archiveChunks.remove(0);
				archiveChunk.forEach(file -> map.add("files_archive", new FileSystemResource(file)));
			}
			
			map.add("version_before", String.valueOf(currentVersion));
			
			currentVersion = currentVersion == Integer.MAX_VALUE ? 0 : currentVersion; 
			map.add("version_after", String.valueOf(++currentVersion));
			
			map.add("page", (currentPage++) + "/" + pageCount);
			chunks.add(map);
		}
		
		MultiValueMap<String, Object> firstChunk;
		if(chunks.isEmpty()) { //in case we only upload backup of PACKAGES files
			firstChunk = new LinkedMultiValueMap<>();
			firstChunk.add("version_before", String.valueOf(currentVersion));
			firstChunk.add("version_after", String.valueOf(currentVersion)); //do not alter version if restoration is performed
			firstChunk.add("page", "1/1");
			chunks.add(firstChunk);
			
		} else {
			firstChunk = chunks.get(0);
		}
		
		firstChunk.add("files", new FileSystemResource(packagesFile));
		firstChunk.add("files", new FileSystemResource(packagesGzFile));
		firstChunk.add("files_archive", new FileSystemResource(packagesFileArchive));
		firstChunk.add("files_archive", new FileSystemResource(packagesGzFileArchive));
		
		for(String packageName : packagesToDelete) {
			firstChunk.add("to_delete", packageName);
		}
		
		for(String packageName : packagesToDeleteFromArchive) {
			firstChunk.add("to_delete_archive", packageName);
		}
		
		return chunks;
	}
}

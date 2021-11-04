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
package eu.openanalytics.rdepot.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.ListUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
	
	/**
	 * @param packagesToUpload - packages Files to upload
	 * @param packagesToUploadToArchive = packages files to upload to archive
	 * @param packagesToDelete - packages to delete
	 * @param packagesToDeleteFromArchive - packages to delete from archive
	 * @param versionBefore - version before republish operation was triggered
	 * @param packagesFile - PACKAGES file
	 * @param packagesGzFile - PACKAGES.gz file
	 * @param packagesFileArchive - PACKAGES file (archive)
	 * @param packagesGzFile - PACKAGES.gz file (archive)
	 */
	public SynchronizeRepositoryRequestBody(List<File> packagesToUpload, 
			List<File> packagesToUploadToArchive, List<String> packagesToDelete, 
			List<String> packagesToDeleteFromArchive, String versionBefore, 
			File packagesFile, File packagesGzFile,
			File packagesFileArchive, File packagesGzFileArchive) {
		super();
		this.packagesToUpload = packagesToUpload;
		this.packagesToUploadToArchive = packagesToUploadToArchive;
		this.packagesToDelete = packagesToDelete;
		this.packagesToDeleteFromArchive = packagesToDeleteFromArchive;
		this.versionBefore = versionBefore;
		this.packagesFile = packagesFile;
		this.packagesGzFile = packagesGzFile;
		this.packagesFileArchive = packagesFileArchive;
		this.packagesGzFileArchive = packagesGzFileArchive;
	}
	
	public SynchronizeRepositoryRequestBody(String versionBefore, File packagesFile, 
			File packagesGzFile, File packagesFileArchive, File packagesGzFileArchive) {
		super();
		this.packagesToUpload = new ArrayList<>();
		this.packagesToUploadToArchive = new ArrayList<>();
		this.packagesToDelete = new ArrayList<>();
		this.packagesToDeleteFromArchive = new ArrayList<>();
		this.versionBefore = versionBefore;
		this.packagesFile = packagesFile;
		this.packagesGzFile = packagesGzFile;
		this.packagesFileArchive = packagesFileArchive;
		this.packagesGzFileArchive = packagesGzFileArchive;
	}
	
	public List<File> getPackagesToUpload() {
		return packagesToUpload;
	}

	public void setPackagesToUpload(List<File> packagesToUpload) {
		this.packagesToUpload = packagesToUpload;
	}

	public List<String> getPackagesToDelete() {
		return packagesToDelete;
	}

	public void setPackagesToDelete(List<String> packagesToDelete) {
		this.packagesToDelete = packagesToDelete;
	}

	public String getVersionBefore() {
		return versionBefore;
	}

	public void setVersionBefore(String versionBefore) {
		this.versionBefore = versionBefore;
	}

	public File getPackagesFile() {
		return packagesFile;
	}

	public void setPackagesFile(File packagesFile) {
		this.packagesFile = packagesFile;
	}

	public File getPackagesGzFile() {
		return packagesGzFile;
	}

	public void setPackagesGzFile(File packagesGzFile) {
		this.packagesGzFile = packagesGzFile;
	}
	
	public List<File> getPackagesToUploadToArchive() {
		return packagesToUploadToArchive;
	}

	public void setPackagesToUploadToArchive(List<File> packagesToUploadToArchive) {
		this.packagesToUploadToArchive = packagesToUploadToArchive;
	}

	public List<String> getPackagesToDeleteFromArchive() {
		return packagesToDeleteFromArchive;
	}

	public void setPackagesToDeleteFromArchive(List<String> packagesToDeleteFromArchive) {
		this.packagesToDeleteFromArchive = packagesToDeleteFromArchive;
	}

	
	public File getPackagesFileArchive() {
		return packagesFileArchive;
	}

	public void setPackagesFileArchive(File packagesFileArchive) {
		this.packagesFileArchive = packagesFileArchive;
	}

	public File getPackagesGzFileArchive() {
		return packagesGzFileArchive;
	}

	public void setPackagesGzFileArchive(File packagesGzFileArchive) {
		this.packagesGzFileArchive = packagesGzFileArchive;
	}
	
	public List<MultiValueMap<String, Object>> toChunks(int elementsPerChunk) {
		List<MultiValueMap<String, Object>> chunks = new ArrayList<>();
				
		List<List<File>> recentChunks = new ArrayList<>(ListUtils.partition(packagesToUpload, elementsPerChunk));
		List<List<File>> archiveChunks = new ArrayList<>(ListUtils.partition(packagesToUploadToArchive, elementsPerChunk));
		
		int pageCount = Integer.max(recentChunks.size(), archiveChunks.size());
		int currentPage = 1;
		
		int currentVersion = Integer.valueOf(this.versionBefore);
		
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
		
		MultiValueMap<String, Object> firstChunk = null;
		if(chunks.size() < 1) { //in case we only upload backup of PACKAGES files
			firstChunk = new LinkedMultiValueMap<String, Object>();
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

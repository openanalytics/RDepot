/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
package eu.openanalytics.rdepot.repo.model;

import java.util.Objects;

import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.repo.exception.InvalidRequestPageNumberException;

public class SynchronizeRepositoryRequestBody {
	MultipartFile[] filesToUpload;
	MultipartFile[] filesToUploadToArchive;
//	File uploadDirectory;
	String id;
	String[] filesToDelete;
	String[] filesToDeleteFromArchive;
	String versionBefore;
	String versionAfter;
	String page;
	String repository;
	
	public SynchronizeRepositoryRequestBody(String id, MultipartFile[] filesToUpload, MultipartFile[] filesToUploadToArchive, String[] filesToDelete, String[] filesToDeleteFromArchive, String versionBefore, String versionAfter,
			String page, String repository) {
		super();
		this.id = id;
		this.filesToUpload = filesToUpload;
		this.filesToUploadToArchive = filesToUploadToArchive;
		this.filesToDelete = filesToDelete;
		this.filesToDeleteFromArchive = filesToDeleteFromArchive;
		this.versionBefore = versionBefore;
		this.versionAfter = versionAfter;
		this.page = page;
		this.repository = repository;
	}
	
//	public File getUploadDirectory() {
//		return uploadDirectory;
//	}
//
//	public void setUploadDirectory(File uploadDirectory) {
//		this.uploadDirectory = uploadDirectory;
//	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public MultipartFile[] getFilesToUpload() {
		return filesToUpload;
	}

	public void setFilesToUpload(MultipartFile[] filesToUpload) {
		this.filesToUpload = filesToUpload;
	}

	public MultipartFile[] getFilesToUploadToArchive() {
		return filesToUploadToArchive;
	}

	public void setFilesToUploadToArchive(MultipartFile[] filesToUploadToArchive) {
		this.filesToUploadToArchive = filesToUploadToArchive;
	}

	public String[] getFilesToDelete() {
		return filesToDelete;
	}

	public void setFilesToDelete(String[] filesToDelete) {
		this.filesToDelete = filesToDelete;
	}

	public String[] getFilesToDeleteFromArchive() {
		return filesToDeleteFromArchive;
	}

	public void setFilesToDeleteFromArchive(String[] filesToDeleteFromArchive) {
		this.filesToDeleteFromArchive = filesToDeleteFromArchive;
	}

	public String getVersionBefore() {
		return versionBefore;
	}

	public void setVersionBefore(String versionBefore) {
		this.versionBefore = versionBefore;
	}

	public String getVersionAfter() {
		return versionAfter;
	}

	public void setVersionAfter(String versionAfter) {
		this.versionAfter = versionAfter;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}
	
	public String getRepository() {
		return repository;
	}
	
	public void setRepository(String repository) {
		this.repository = repository;
	}
	
	public boolean isFirstChunk() throws InvalidRequestPageNumberException {
    	String[] pageStr = getPage().split("/");
    	if(pageStr.length == 2) {
    		return Objects.equals(pageStr[0], "1");
    	} else {
    		throw new InvalidRequestPageNumberException(getPage());
    	}
    }
    
    public boolean isLastChunk() throws InvalidRequestPageNumberException {
    	String[] pageStr = getPage().split("/");
    	if(pageStr.length == 2) {
    		return Objects.equals(pageStr[0], pageStr[1]);
    	} else {
    		throw new InvalidRequestPageNumberException(getPage());
    	}
    	
    }
}

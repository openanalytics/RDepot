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
package eu.openanalytics.rdepot.repo.r.model;

import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;

public class SynchronizeCranRepositoryRequestBody extends SynchronizeRepositoryRequestBody {
	MultipartFile[] filesToUpload;
	MultipartFile[] filesToUploadToArchive;
	String[] filesToDelete;
	String[] filesToDeleteFromArchive;
	
	public SynchronizeCranRepositoryRequestBody(String id, MultipartFile[] filesToUpload, MultipartFile[] filesToUploadToArchive, String[] filesToDelete, String[] filesToDeleteFromArchive, String versionBefore, String versionAfter,
			String page, String repository) {
		super(page, repository, id, versionBefore, versionAfter);
		this.filesToUpload = filesToUpload;
		this.filesToUploadToArchive = filesToUploadToArchive;
		this.filesToDelete = filesToDelete;
		this.filesToDeleteFromArchive = filesToDeleteFromArchive;
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
}

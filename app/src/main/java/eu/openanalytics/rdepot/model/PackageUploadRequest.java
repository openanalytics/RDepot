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
package eu.openanalytics.rdepot.model;

import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PackageUploadRequest {
	
	@JsonProperty("fileData")
	private MultipartFile fileData;
	@JsonProperty("repository")
	private String repository;
	@JsonProperty("changes")
	private String changes;
	private boolean generateManual = true;
	private boolean replace = false;
	
	public PackageUploadRequest() {
		
	}
	
	public PackageUploadRequest(MultipartFile fileData, String repositories) {
		this.fileData = fileData;
		this.repository = repositories;
	}
	
	public PackageUploadRequest(MultipartFile fileData, String repositories, boolean generateManual, boolean replace) {
		this.fileData = fileData;
		this.repository = repositories;
		this.generateManual = generateManual;
		this.replace = replace;
	}
	
	public PackageUploadRequest(MultipartFile fileData, String repositories, String changes) {
		this.fileData = fileData;
		this.repository = repositories;
		this.changes = changes;
	}
	
	public PackageUploadRequest(MultipartFile fileData, String repositories, String changes, boolean generateManual, boolean replace) {
		this.fileData = fileData;
		this.repository = repositories;
		this.changes = changes;
		this.generateManual = generateManual;
		this.replace = replace;
	}
	
	public MultipartFile getFileData() {
		return fileData;
	}
	
	public void setFileData(MultipartFile fileData) {
		this.fileData = fileData;
	}
	
	public String getRepository() {
		return repository;
	}
	
	public void setRepository(String repository) {
		this.repository = repository;
	}

	public boolean getGenerateManual() {
		return generateManual;
	}

	public void setGenerateManual(boolean generateManual) {
		this.generateManual = generateManual;
	}
	
	public boolean getReplace() {
		return replace;
	}

	public void setReplace(boolean replace) {
		this.replace = replace;
	}
	
}

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
package eu.openanalytics.rdepot.r.legacy.api.v1.dtos;

//import java.util.ArrayList;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

import eu.openanalytics.rdepot.r.entities.RRepository;

public class UploadRequest
{
	private CommonsMultipartFile[] fileData;
	private RRepository repository;
	private String[] changes;
	private boolean replace = false;
	
	public UploadRequest()
	{
		//this.fileData = new ArrayList<CommonsMultipartFile>(1);
		//this.repository = new ArrayList<Repository>(1);
	}
	
	public UploadRequest(CommonsMultipartFile[] fileData, RRepository repository)
	{
		this.fileData = fileData;
		this.repository = repository;
	}
	
	public UploadRequest(CommonsMultipartFile[] fileData, RRepository repository, boolean replace)
	{
		this.fileData = fileData;
		this.repository = repository;
		this.setReplace(replace);
	}
	
	public UploadRequest(CommonsMultipartFile[] fileData, RRepository repository, String[] changes)
	{
		this.fileData = fileData;
		this.repository = repository;
		this.changes = changes;
	}
	
	public UploadRequest(CommonsMultipartFile[] fileData, RRepository repository, String[] changes, boolean replace)
	{
		this.fileData = fileData;
		this.repository = repository;
		this.changes = changes;
		this.setReplace(replace);
	}

	public CommonsMultipartFile[] getFileData() {
		return fileData;
	}

	public void setFileData(CommonsMultipartFile[] fileData) {
		this.fileData = fileData;
	}

	public RRepository getRepository() {
		return repository;
	}

	public void setRepository(RRepository repository) {
		this.repository = repository;
	}

	public String[] getChanges() {
		return changes;
	}

	public void setChanges(String[] changes) {
		this.changes = changes;
	}

	/**
	 * @return the replace
	 */
	public boolean getReplace() {
		return replace;
	}

	/**
	 * @param replace the replace to set
	 */
	public void setReplace(boolean replace) {
		this.replace = replace;
	}
	
}

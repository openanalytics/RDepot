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
package eu.openanalytics.rdepot.repo.model;

import java.util.Objects;

import eu.openanalytics.rdepot.repo.exception.InvalidRequestPageNumberException;

public abstract class SynchronizeRepositoryRequestBody {
	String page;
	String repository;
	String id;
	String versionBefore;
	String versionAfter;
	
	public SynchronizeRepositoryRequestBody(String page, String repository, String id,
			String versionBefore, String versionAfter) {
		this.page = page;
		this.repository = repository;
		this.id = id;
		this.versionAfter = versionAfter;
		this.versionBefore = versionBefore;
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
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getVersionAfter() {
		return versionAfter;
	}
	
	public String getVersionBefore() {
		return versionBefore;
	}
	
	public void setVersionAfter(String versionAfter) {
		this.versionAfter = versionAfter;
	}
	
	public void setVersionBefore(String versionBefore) {
		this.versionBefore = versionBefore;
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

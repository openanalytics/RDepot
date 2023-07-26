/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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

import java.util.ArrayList;
import java.util.List;

public class VersionedRepository {
	private List<Upload> uploads = new ArrayList<>();
	private String repositoryVersion = "";
	
	public void setRepositoryVersion(String repositoryVersion) {
		this.repositoryVersion = repositoryVersion;
	}
	
	public void setUploads(List<Upload> uploads) {
		this.uploads = uploads;
	}
	
	public VersionedRepository() {}
	
	public VersionedRepository(List<Upload> uploads, String repositoryVersion) {
		super();
		this.uploads = uploads;
		this.repositoryVersion = repositoryVersion;
	}
	
	public String getRepositoryVersion() {
		return repositoryVersion;
	}
	
	public List<Upload> getUploads() {
		return uploads;
	}
}

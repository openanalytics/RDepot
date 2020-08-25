/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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

import java.io.File;
import java.util.List;

public class RepositoryBackup {
	List<String> recentPackages;
	List<String> archivePackages;
	File trashDirectory;
	String version;
	
	public RepositoryBackup(List<String> recentPackages, List<String> archivePackages, 
			File trashDirectory, String version) {
		super();
		this.recentPackages = recentPackages;
		this.archivePackages = archivePackages;
		this.trashDirectory = trashDirectory;
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<String> getRecentPackages() {
		return recentPackages;
	}

	public void setRecentPackages(List<String> recentPackages) {
		this.recentPackages = recentPackages;
	}

	public List<String> getArchivePackages() {
		return archivePackages;
	}

	public void setArchivePackages(List<String> archivePackages) {
		this.archivePackages = archivePackages;
	}

	public File getTrashDirectory() {
		return trashDirectory;
	}

	public void setTrashDirectory(File trashDirectory) {
		this.trashDirectory = trashDirectory;
	}
	
	
}

/**
 * R Depot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.r.storage.utils;

import java.util.List;

import eu.openanalytics.rdepot.r.entities.RPackage;

public class PopulatedRepositoryContent {

	private final List<RPackage> latestPackages;
	private final List<RPackage> archivePackages;
	private final String latestDirectoryPath;
	private final String archiveDirectoryPath;
	
	public PopulatedRepositoryContent(List<RPackage> latestPackages, List<RPackage> archivePackages,
			String latestDirectoryPath, String archiveDirectoryPath) {
		super();
		this.latestPackages = latestPackages;
		this.archivePackages = archivePackages;
		this.latestDirectoryPath = latestDirectoryPath;
		this.archiveDirectoryPath = archiveDirectoryPath;
	}

	public List<RPackage> getLatestPackages() {
		return latestPackages;
	}

	public List<RPackage> getArchivePackages() {
		return archivePackages;
	}
	
	public String getLatestDirectoryPath() {
		return latestDirectoryPath;
	}
	
	public String getArchiveDirectoryPath() {
		return archiveDirectoryPath;
	}
}

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
package eu.openanalytics.rdepot.base.storage.utils;

import java.util.List;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.storage.Storage;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents {@link Repository} content 
 * organized into "latest" and "archive" {@link Package packages}.
 * It also points to their respective locations in {@link Storage}.
 */
@Getter
@AllArgsConstructor
public class PopulatedRepositoryContent {
	private final List<? extends Package> latestPackages;
	private final List<? extends Package> archivePackages;
	private final String latestDirectoryPath;
	private final String archiveDirectoryPath;
}

/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
package eu.openanalytics.rdepot.r.storage;

import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.storage.population.PopulatedRPackage;
import java.util.List;

/**
 * Represents a location in storage where binary R packages
 * belonging to the same platform are stored in generated directory.
 * This is necessary since a single repository for binaries
 * have multiple "sub-repositories" for each platform (os, distro, CPU architecture, etc.).
 * @param location generated binary package location e.g.
 *                 <ul>
 *                     <li><code>{generationDir}/{repositoryId}/{datestamp}/bin/{os}/{distro}/{architecture}/{rVersion}</code></li>
 *                     <li><code>{generationDir}/{repositoryId}/{datestamp}/bin/{os}/{distro}/{architecture}/{rVersion}/latest</code></li>
 *                     <li><code>{generationDir}/{repositoryId}/{datestamp}/bin/{os}/{distro}/{architecture}/{rVersion}/Archive</code></li>
 *                 </ul>
 * @param remoteLocation target remote location, e.g.
 *                       <code>bin/{os}/{distro}/{architecture}/{rVersion}</code>
 * @param packages already populated packages for the location
 * @param packagesToPopulate packages scheduled to be populated in this location
 */
public record BinLocation(
        String location, String remoteLocation, List<PopulatedRPackage> packages, List<RPackage> packagesToPopulate) {}

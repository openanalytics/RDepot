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
package eu.openanalytics.rdepot.r.storage.population;

import eu.openanalytics.rdepot.r.storage.binaries.BinLocationSet;
import eu.openanalytics.rdepot.r.storage.indexes.RIndexDescriptor;
import eu.openanalytics.rdepot.r.storage.packagesfile.PackagesFileDescriptor;
import java.util.List;
import java.util.Set;

/**
 * @param latestPackages
 * @param archivePackages
 * @param latestDirectoryPath latest directory path in local localStorage
 *                            (e.g. <code>{generationDir}/{repositoryId}/{datestamp}/src/contrib/latest</code>)
 * @param archiveDirectoryPath archive directory path in local localStorage
 *                             (e.g. <code>{generationDir}/{repositoryId}/{datestamp}/src/contrib/Archive</code>)
 * @param binLatestPackagesPaths
 * @param binArchivePackagesPaths
 * @param indexes
 * @param packagesFiles
 */
public record PopulatedRepositoryContent(
        List<PopulatedRPackage> latestPackages,
        List<PopulatedRPackage> archivePackages,
        String latestDirectoryPath,
        String archiveDirectoryPath,
        BinLocationSet binLatestPackagesPaths,
        BinLocationSet binArchivePackagesPaths,
        List<RIndexDescriptor> indexes,
        Set<PackagesFileDescriptor> packagesFiles) {}

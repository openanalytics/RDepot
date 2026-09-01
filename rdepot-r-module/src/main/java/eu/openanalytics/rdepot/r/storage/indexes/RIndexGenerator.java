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
package eu.openanalytics.rdepot.r.storage.indexes;

import eu.openanalytics.rdepot.base.storage.exceptions.ContentEditException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.storage.binaries.BinLocation;
import eu.openanalytics.rdepot.r.storage.binaries.BinLocationSet;
import eu.openanalytics.rdepot.r.storage.indexes.resolvers.RPackagePublicationURIResolver;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Generates all indexes for R repositories.
 */
@Component
public class RIndexGenerator {
    private final RRepositoryIndexGenerator rRepositoryIndexGenerator;
    private final ArchiveIndexGenerator archiveIndexGenerator;
    private final RPackageIndexGenerator rPackageIndexGenerator;
    private static final RPackagePublicationURIResolver rPackagePublicationURIResolver =
            new RPackagePublicationURIResolver();
    private static final String separator = FileSystems.getDefault().getSeparator();

    public RIndexGenerator(
            RRepositoryIndexGenerator rRepositoryIndexGenerator,
            ArchiveIndexGenerator archiveIndexGenerator,
            RPackageIndexGenerator rPackageIndexGenerator) {
        this.rRepositoryIndexGenerator = rRepositoryIndexGenerator;
        this.archiveIndexGenerator = archiveIndexGenerator;
        this.rPackageIndexGenerator = rPackageIndexGenerator;
    }

    /**
     * Generates all indexes for R repositories.
     * The generated indexes are the following:
     * <ul>
     *     <li><code>/index.html</code> - main index for latest packages
     *     in the head directory of the repository</li>
     *     <li><code>/Archive/index.html</code> - Index in the Archive directory
     *     with links to package-specific indexes in package directories</li>
     *     <li><code>/Archive/{packageName}/index.html</code> - Package-specific indexes
     *     for archival versions for each package name
     * </ul>
     */
    public List<RIndexDescriptor> createIndexes(
            RRepository repository,
            String latestSourceFolderPath,
            String archiveSourceFolderPath,
            List<RPackage> latestSourcePackages,
            List<RPackage> archiveSourcePackages,
            BinLocationSet binArchiveFoldersPaths,
            BinLocationSet binLatestFoldersPaths)
            throws ContentEditException {
        final List<RIndexDescriptor> indexes = new ArrayList<>();
        // Latest indexes
        final String latestSourceIndexPath = latestSourceFolderPath + separator + "index.html";
        final String checksum =
                rRepositoryIndexGenerator.generateIndex(repository, latestSourcePackages, latestSourceIndexPath);
        indexes.add(new RIndexDescriptor("src/contrib", latestSourceIndexPath, false, checksum));
        //
        for (BinLocation location : binLatestFoldersPaths.getAllBinLocations()) {
            final String latestBinaryRepoIndexPath = location.location() + separator + "index.html";
            final String checksumBin = rRepositoryIndexGenerator.generateIndex(
                    repository,
                    location.packages().stream().map(p -> (RPackage) p).toList(),
                    latestBinaryRepoIndexPath);
            indexes.add(new RIndexDescriptor(location.remoteLocation(), latestBinaryRepoIndexPath, false, checksumBin));
        }

        // Archive indexes
        final String archiveSourceIndexPath = archiveSourceFolderPath + separator + "index_archived.html";
        final String checksumArchive =
                archiveIndexGenerator.generateIndex(repository, archiveSourcePackages, archiveSourceIndexPath);
        indexes.add(new RIndexDescriptor("src/contrib/Archive", archiveSourceIndexPath, true, checksumArchive));
        //
        for (BinLocation location : binArchiveFoldersPaths.getAllBinLocations()) {
            final String archiveBinaryRepoIndexPath = location.location() + separator + "index_archived.html";
            final String checksumBin = archiveIndexGenerator.generateIndex(
                    repository,
                    location.packages().stream().map(p -> (RPackage) p).toList(),
                    archiveBinaryRepoIndexPath);
            indexes.add(new RIndexDescriptor(location.remoteLocation(), archiveBinaryRepoIndexPath, true, checksumBin));
        }
        //
        // Individual archived packages
        indexes.addAll(generateIndividualIndexesForArchivePackages(archiveSourcePackages, archiveSourceFolderPath));
        for (BinLocation location : binArchiveFoldersPaths.getAllBinLocations()) {
            final List<RPackage> packagesToIndex =
                    location.packages().stream().map(p -> (RPackage) p).toList();
            if (packagesToIndex.isEmpty()) {
                continue;
            }
            indexes.addAll(generateIndividualIndexesForArchivePackages(packagesToIndex, location.location()));
        }

        return indexes;
    }

    private List<RIndexDescriptor> generateIndividualIndexesForArchivePackages(
            List<RPackage> packages, String indexPathPrefix) throws ContentEditException {
        final Map<String, RIndexDescriptor> indexes = new LinkedHashMap<>();
        for (RPackage archivePackage : packages) {
            final String archiveBinaryPackageIndexPath =
                    indexPathPrefix + separator + archivePackage.getName() + "index.html";
            rPackageIndexGenerator.generateIndexIfNotExists(archivePackage, archiveBinaryPackageIndexPath);
            final String checksum =
                    rPackageIndexGenerator.addPackageToList(archivePackage, archiveBinaryPackageIndexPath);
            indexes.put(
                    archiveBinaryPackageIndexPath,
                    new RIndexDescriptor(
                            rPackagePublicationURIResolver.resolveRelativeIndexUri(archivePackage),
                            archiveBinaryPackageIndexPath,
                            true,
                            checksum));
        }
        return indexes.values().stream().toList();
    }
}

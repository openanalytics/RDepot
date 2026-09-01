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

import eu.openanalytics.rdepot.base.storage.exceptions.CleanUpAfterSynchronizationException;
import eu.openanalytics.rdepot.base.storage.exceptions.OrganizePackagesException;
import eu.openanalytics.rdepot.base.storage.population.Populator;
import eu.openanalytics.rdepot.base.synchronization.checksums.Checksums;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.synchronization.SynchronizeRepositoryRequestBody;
import java.util.List;
import org.springframework.util.MultiValueMap;

/**
 * Provides features specific for R Packages localStorage management.
 */
public interface RPopulator extends Populator<RRepository, RPackage> {
    /**
     * Builds request body with objects ready to be uploaded to the remote server.
     * @param populatedRepositoryContent content
     * @param repository repository to synchronized
     * @param versionBefore version of the repository before synchronization
     * @return request body
     */
    SynchronizeRepositoryRequestBody buildSynchronizeRequestBody(
            PopulatedRepositoryContent populatedRepositoryContent,
            List<String> remoteLatestPackages,
            List<String> remoteArchivePackages,
            MultiValueMap<String, String> remoteLatestBinaryPackages,
            MultiValueMap<String, String> remoteArchiveBinaryPackages,
            Checksums checksums,
            RRepository repository,
            String versionBefore);

    /**
     * Populates packages and generates directory structure ready for synchronization
     * @param packages all packages
     * @param latestPackages only latest packages
     * @param archivePackages only packages going to archive
     */
    PopulatedRepositoryContent organizePackagesInStorage(
            String dateStamp,
            List<RPackage> packages,
            List<RPackage> latestPackages,
            List<RPackage> archivePackages,
            List<RPackage> binaryPackages,
            List<RPackage> archiveBinaryPackages,
            List<RPackage> latestBinaryPackages,
            List<String> binaryPlatforms,
            RRepository repository)
            throws OrganizePackagesException;

    /**
     * Cleans temporary directories after synchronization.
     */
    void cleanUpAfterSynchronization(PopulatedRepositoryContent populatedRepositoryContent)
            throws CleanUpAfterSynchronizationException;
}

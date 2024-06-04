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
package eu.openanalytics.rdepot.python.storage;

import eu.openanalytics.rdepot.base.storage.exceptions.CleanUpAfterSynchronizationException;
import eu.openanalytics.rdepot.base.storage.exceptions.GenerateManualException;
import eu.openanalytics.rdepot.base.storage.exceptions.GetReferenceManualException;
import eu.openanalytics.rdepot.base.storage.exceptions.OrganizePackagesException;
import eu.openanalytics.rdepot.base.storage.exceptions.ReadPackageVignetteException;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.entities.Vignette;
import eu.openanalytics.rdepot.python.storage.utils.PopulatedRepositoryContent;
import eu.openanalytics.rdepot.python.synchronization.SynchronizeRepositoryRequestBody;
import java.util.List;

/**
 * Provides features specific for Python Packages storage management.
 */
public interface PythonStorage {
    /**
     * Builds request body with objects ready to be uploaded to the remote server.
     * @param populatedRepositoryContent
     * @param repository repository to synchronized
     * @param versionBefore version of the repository before synchronization
     * @return request body
     */
    SynchronizeRepositoryRequestBody buildSynchronizeRequestBody(
            PopulatedRepositoryContent populatedRepositoryContent,
            List<String> remotePackages,
            PythonRepository repository,
            String versionBefore);

    /**
     * Populates packages and generates directory structure ready for synchronization
     * @param packages all packages
     * @return
     */
    PopulatedRepositoryContent organizePackagesInStorage(
            String dateStamp, List<PythonPackage> packages, PythonRepository repository)
            throws OrganizePackagesException;

    /**
     * Cleans temporary directories after synchronization.
     * @param populatedRepositoryContent
     */
    void cleanUpAfterSynchronization(PopulatedRepositoryContent populatedRepositoryContent)
            throws CleanUpAfterSynchronizationException;

    /**
     * Fetches reference manual file from the storage.
     * @param packageBag
     * @return
     */
    byte[] getReferenceManual(PythonPackage packageBag) throws GetReferenceManualException;

    /**
     * Checks if a package provides reference manual.
     * @param packageBag
     * @return
     */
    boolean isReferenceManualAvailable(PythonPackage packageBag);

    /**
     * Fetches links to available vignettes for a given package.
     * @param packageBag
     * @return
     */
    List<Vignette> getAvailableVignettes(PythonPackage packageBag);

    /**
     * Reads vignette from storage.
     * @param packageBag
     * @param filename
     * @return
     */
    byte[] readVignette(PythonPackage packageBag, String filename) throws ReadPackageVignetteException;

    /**
     * Creates manual for a package and puts it in the local storage.
     * @param packageBag
     * @throws GenerateManualException
     */
    void generateManual(PythonPackage packageBag) throws GenerateManualException;
}

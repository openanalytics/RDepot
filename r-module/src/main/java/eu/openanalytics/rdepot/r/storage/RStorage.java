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
package eu.openanalytics.rdepot.r.storage;

import java.util.List;

import eu.openanalytics.rdepot.base.storage.exceptions.CleanUpAfterSynchronizationException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.entities.Vignette;
import eu.openanalytics.rdepot.r.storage.exceptions.GenerateManualException;
import eu.openanalytics.rdepot.r.storage.exceptions.GetReferenceManualException;
import eu.openanalytics.rdepot.r.storage.exceptions.OrganizePackagesException;
import eu.openanalytics.rdepot.r.storage.exceptions.ReadPackageVignetteException;
import eu.openanalytics.rdepot.r.storage.utils.PopulatedRepositoryContent;
import eu.openanalytics.rdepot.r.synchronization.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.r.synchronization.pojos.VersionedRepository;

/**
 * Provides features specific for R Packages storage management.
 */
public interface RStorage {
	/**
	 * Builds request body with objects ready to be uploaded to the remote server.
	 * @param populatedRepositoryContent
	 * @param repository repository to synchronized
	 * @param versionBefore version of the repository before synchronization
	 * @return request body
	 */
	SynchronizeRepositoryRequestBody buildSynchronizeRequestBody(
			PopulatedRepositoryContent populatedRepositoryContent, 
			VersionedRepository remoteLatestPackages, VersionedRepository remoteArchivePackages,
			RRepository repository, String versionBefore);
	
	/**
	 * Populates packages and generates directory structure ready for synchronization
	 * @param packages all packages
	 * @param latestPackages only latest packages
	 * @param archivePackages only packages going to archive
	 * @return
	 */
	PopulatedRepositoryContent organizePackagesInStorage(String dateStamp, List<RPackage> packages, 
			List<RPackage> latestPackages, List<RPackage> archivePackages, RRepository repository) throws OrganizePackagesException;
	
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
	byte[] getReferenceManual(RPackage packageBag) throws GetReferenceManualException;
	
	/**
	 * Checks if a package provides reference manual.
	 * @param packageBag
	 * @return
	 */
	boolean isReferenceManualAvailable(RPackage packageBag);
	
	/**
	 * Fetches links to available vignettes for a given package.
	 * @param packageBag
	 * @return
	 */
	List<Vignette> getAvailableVignettes(RPackage packageBag);

	/**
	 * Reads vignette from storage.
	 * @param packageBag
	 * @param filename
	 * @return
	 */
	byte[] readVignette(RPackage packageBag, String filename) throws ReadPackageVignetteException;
	
	/**
	 * Creates manual for a package and puts it in the local storage.
	 * @param packageBag
	 * @throws GenerateManualException
	 */
	void generateManual(RPackage packageBag) throws GenerateManualException;
}

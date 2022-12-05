/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
package eu.openanalytics.rdepot.base.storage;

import java.io.File;
import java.util.Properties;

import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.storage.exceptions.CheckSumCalculationException;
import eu.openanalytics.rdepot.base.storage.exceptions.DeleteFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.ExtractFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.InvalidSourceException;
import eu.openanalytics.rdepot.base.storage.exceptions.MovePackageSourceException;
import eu.openanalytics.rdepot.base.storage.exceptions.ReadPackageDescriptionException;
import eu.openanalytics.rdepot.base.storage.exceptions.RepositoryDirectoryDeleteException;
import eu.openanalytics.rdepot.base.storage.exceptions.SourceFileDeleteException;
import eu.openanalytics.rdepot.base.storage.exceptions.SourceNotFoundException;
import eu.openanalytics.rdepot.base.storage.exceptions.WriteToWaitingRoomException;

/**
 * Allows to access storage to save persistent binary data like files.
 */
public interface Storage<R extends Repository<R, ?>, P extends Package<P, ?>> {
	
	/**
	 * This method saves package in so-called "waiting room" where it stays 
	 * until it's accepted by administrator
	 * @param fileData Multipart containing uploaded file.
	 * @param repository repository where the package should be uploaded to
	 * @return package file
	 */
	File writeToWaitingRoom(MultipartFile fileData, R repository) throws WriteToWaitingRoomException;
	
	/**
	 * When submission is accepted, its source can be moved from waiting room to the main directory.
	 * @param packageBag
	 * @return new source file
	 * @throws InvalidSourceException 
	 * @throws MovePackageSourceException 
	 */
	File moveToMainDirectory(P packageBag) throws InvalidSourceException, MovePackageSourceException;
	
	/**
	 * Extracts the package
	 * @param storedFile
	 * @return
	 * @throws ExtractFileException 
	 */
	File extractTarGzPackageFile(File storedFile) throws ExtractFileException;
	
	/**
	 * Fetches properties from extracted package file.
	 * @param extractedFile
	 * @return properties
	 * @throws ReadPackageDescriptionException 
	 */
	Properties getPropertiesFromExtractedFile(File extractedFile) throws ReadPackageDescriptionException;
	
	/**
	 * Moves cancelled/rejected submission to a trash directory.
	 * @param packageBag
	 * @return
	 * @throws MovePackageSourceException 
	 */
	File moveToTrashDirectory(P packageBag) throws MovePackageSourceException;
	
//	/**
//	 * Restores packageBag from trash directory.
//	 * @param packageBag
//	 * @return
//	 * @throws MovePackageSourceException
//	 */
//	File restoreFromTrashDirectory(P packageBag) throws MovePackageSourceException;
//	
//	File deletePermamentl
	
	/**
	 * Removes package source from persistent storage.
	 * @param packageBag
	 * @throws SourceFileDeleteException 
	 */
	void removePackageSource(P packageBag) throws SourceFileDeleteException;
	
	/**
	 * Removes file from persistent storage.
	 * Can be used in case of failure during package creation.
	 * For any other purpose, it is recommended to use dedicated
	 * {@link #removePackageSource(Package)} method.
	 * @param file file
	 */
	void removeFileIfExists(File file) throws DeleteFileException;
	
	/**
	 * Removes repository directory from persistent storage.
	 * @param repository
	 * @throws RepositoryDirectoryDeleteException
	 */
	void removeRepositoryDirectory(R repository) throws RepositoryDirectoryDeleteException;

	/**
	 * Ensures that updated source path leads to a correct package.
	 * @param packageBag
	 * @param newSource
	 */
	void verifySource(P packageBag, String newSource) throws InvalidSourceException;
	
	/**
	 * Moves package source to a new location.
	 * @param packageBag
	 * @param newSource
	 * @return
	 * @throws MovePackageSourceException 
	 */
	File moveSource(P packageBag, String newSource) throws MovePackageSourceException;

	/**
	 * Reads package from storage.
	 * @param packageBag
	 * @return
	 */
	byte[] getPackageInBytes(P packageBag) throws SourceNotFoundException;
	
	/**
	 * Calculates and assigns a checksum to the package.
	 * @param packageBag
	 * @throws CheckSumCalculationException
	 */
	void calculateCheckSum(P packageBag) throws CheckSumCalculationException;
}

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
package eu.openanalytics.rdepot.base.storage;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.storage.exceptions.*;
import java.io.File;
import java.util.Properties;
import org.springframework.web.multipart.MultipartFile;

/**
 * Allows to access storage to save persistent binary data like files.
 */
public interface Storage<R extends Repository, P extends Package> {

    /**
     * This method saves package in so-called "waiting room" where it stays
     * until it's accepted by administrator
     * @param fileData Multipart containing uploaded file.
     * @param repository repository where the package should be uploaded to
     * @return package file
     */
    String writeToWaitingRoom(MultipartFile fileData, R repository) throws WriteToWaitingRoomException;

    /**
     * When submission is accepted, its source can be moved from waiting room to the main directory.
     * @return new source file
     */
    String moveToMainDirectory(P packageBag) throws InvalidSourceException, MovePackageSourceException;

    /**
     * Extracts the package
     */
    String extractTarGzPackageFile(String storedFile) throws ExtractFileException;

    // return whole package path (with getAbsolutePath)
    String generateSubmissionWaitingRoomLocation(File file);

    /**
     * Fetches properties from extracted package file.
     */
    Properties getPropertiesFromExtractedFile(String extractedFile) throws ReadPackageDescriptionException;

    /**
     * Moves cancelled/rejected submission to a trash directory.
     */
    String moveToTrashDirectory(P packageBag) throws MovePackageSourceException;

    /**
     * Removes package source from persistent storage.
     */
    void removePackageSource(String path) throws SourceFileDeleteException;

    /**
     * Removes file from persistent storage.
     * Can be used in case of failure during package creation.
     */
    void removeFileIfExists(String path) throws DeleteFileException;

    /**
     * Ensures that updated source path leads to a correct package.
     */
    void verifySource(P packageBag, String newSource) throws InvalidSourceException;

    /**
     * Moves package source to a new location.
     */
    String moveSource(P packageBag, String newSource) throws MovePackageSourceException;

    /**
     * Reads package from storage.
     */
    byte[] getPackageInBytes(P packageBag) throws SourceNotFoundException;

    /**
     * Calculates and assigns a checksum to the package.
     */
    void calculateCheckSum(P packageBag) throws CheckSumCalculationException;

    /**
     * Populates package in generated repository structure.
     * @param packageBag package to populate
     * @param folderPath population directory path (e.g. "archive" or "latest")
     */
    void populatePackage(P packageBag, String folderPath) throws PackageFolderPopulationException;
}

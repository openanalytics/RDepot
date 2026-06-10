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
package eu.openanalytics.rdepot.base.storage;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.storage.exceptions.*;
import java.util.Properties;
import org.springframework.web.multipart.MultipartFile;

/**
 * Manages logical structures in storage and populates packages.
 */
public interface Populator<R extends Repository, P extends Package> {
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
     * Fetches properties from extracted package file.
     */
    Properties getPropertiesFromExtractedFile(String extractedFile) throws ReadPackageDescriptionException;

    /**
     * Moves cancelled/rejected submission to a trash directory.
     */
    String moveToTrashDirectory(P packageBag) throws MovePackageSourceException;

    /**
     * Populates package in generated repository structure.
     * @param packageBag package to populate
     * @param folderPath population directory path (e.g. "archive" or "latest")
     * @return storage path to populated package
     */
    String populatePackage(P packageBag, String folderPath) throws PackageFolderPopulationException;
}

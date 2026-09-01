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
package eu.openanalytics.rdepot.base.storage.population;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.storage.exceptions.*;
import java.io.File;
import java.util.List;
import java.util.Properties;
import org.springframework.web.multipart.MultipartFile;

/**
 * Manages logical structures in localStorage and populates packages.
 */
public interface Populator<R extends Repository, P extends Package> {
    void removeTemporaryLocalWaitingRoomOfFile(File file) throws DeleteFileException;

    /**
     * This method saves package in so-called "waiting room" where it stays
     * until it's accepted by administrator
     * @param fileData Multipart containing uploaded file.
     * @param repository repository where the package should be uploaded to
     * @return package file
     */
    File writeToTemporaryLocalWaitingRoom(MultipartFile fileData, R repository) throws WriteToWaitingRoomException;

    /**
     * Fetches properties from extracted package file.
     */
    Properties getPropertiesFromExtractedFile(List<File> extractedFiles) throws ReadPackageDescriptionException;

    /**
     * Populates package in generated repository structure.
     * @param packageBag package to populate
     * @param folderPath population directory path (e.g. "archive" or "latest")
     * @return localStorage path to populated package
     */
    String populatePackage(P packageBag, String folderPath) throws PackageFolderPopulationException;
}

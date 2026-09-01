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
import eu.openanalytics.rdepot.base.storage.exceptions.DeleteFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.DownloadFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.StoreFileException;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Used to store package files and metadata.
 * Can be implemented by either a file system or various cloud backends (like S3).
 * If you want to process some files temporarily and losing them
 * will not have impact on the overall integrity,
 * use {@link LocalStorage local storage} instead.
 */
public interface PersistentStorage<P extends Package, R extends Repository> {

    /**
     * Stores newly uploaded package that is not yet accepted.
     * @param packageFile package archive (uploaded file)
     * @param extractedDirs directories created after unpacking the package archive
     * @param repository repository to upload package to
     * @return path of the package archive in the persistent storage
     */
    Path storeNewPackage(File packageFile, List<File> extractedDirs, R repository) throws StoreFileException;

    /**
     * Used when submission is accepted.
     * Then package is moved from "new" directory so that it will be considered for publication.
     * @return new path to the package archive
     */
    Path movePackageToAccepted(P packageBag) throws StoreFileException;

    /**
     * Used when submission is soft-deleted.
     * @return path to the recycled package archive
     */
    Path movePackageToTrash(P packageBag) throws StoreFileException;

    /**
     * Generic method that removes given path from persistent storage.
     */
    void deleteFromStorageIfExists(Path path) throws DeleteFileException;

    /**
     * Deletes everything related to package from persistent storage.
     */
    void deleteAllPackageFilesIfExist(P packageBag) throws DeleteFileException;

    /**
     * Retrieves package archive from persistent storage.
     */
    byte[] getPackageInBytes(P packageBag) throws DownloadFileException;

    /**
     * Retrieves file from persistent storage.
     */
    byte[] readFile(Path file) throws DownloadFileException;

    /**
     * Retrieves file from persistent storage.
     * @param file path to the file in persistent storage
     * @return retrieved file
     */
    File downloadFile(Path file) throws DownloadFileException;

    /**
     * Removes downloaded file when it is not useful anymore.
     */
    void recycleDownloadedFile(File file) throws DeleteFileException;
}

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
package eu.openanalytics.rdepot.repo.storage;

import eu.openanalytics.rdepot.repo.exception.*;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.model.Technology;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Storage layer interface.
 */
public interface StorageService<T extends SynchronizeRepositoryRequestBody> extends InitializableStorageService {
    String TRASH_PREFIX = "TRASH_";
    String TRASH_DATABASE_FILE = "TRASH_DATABASE.txt";

    /**
     * Returns current version of repository based on the information stored in storage.
     */
    String getRepositoryVersion(String repository) throws GetRepositoryVersionException;

    /**
     * Returns list of recent packages for given repository based on storage contents.
     */
    List<File> getRecentPackagesFromRepository(String repository);

    /**
     * Initializes trash directory for transaction backup.
     * It should be called before the first chunk gets processed.
     * @param id {@link eu.openanalytics.rdepot.repo.transaction.Transaction transaction} id.
     */
    File initTrashDirectory(String id) throws InitTrashDirectoryException;

    /**
     * Moves package file to trash directory related to
     * {@link eu.openanalytics.rdepot.repo.transaction.Transaction}
     * of given id.
     * @param id {@link eu.openanalytics.rdepot.repo.transaction.Transaction transaction} id.
     */
    void moveToTrash(String id, File packageFile) throws MoveToTrashException;

    /**
     * Removes contents of trash directory when backup is being removed.
     */
    void emptyTrash(String repository, String requestId) throws EmptyTrashException;

    /**
     * Restores packages from trash directory.
     * @param trashDirectory can be retrieved from
     * {@link eu.openanalytics.rdepot.repo.transaction.backup.RepositoryBackup Repository Backup object}.
     */
    void restoreTrash(File trashDirectory) throws RestoreRepositoryException;

    /**
     * Removes packages that have just been uploaded
     * but were not present before the transaction was initialized.
     * @param packages <b>all</b> packages present before the first chunk was processed
     * @param repository name of the repository
     */
    void removeNonExistingPackagesFromRepo(List<String> packages, String repository) throws RestoreRepositoryException;

    /**
     * Sets repository version to given value.
     * May be used to restore repository to the state from before the transaction.
     * @param repository name of repository
     * @param version version to set
     */
    void setRepositoryVersion(String repository, String version);

    /**
     * Increments repository version after a chunk was submitted.
     * @param repository name of repository
     */
    void boostRepositoryVersion(String repository) throws SetRepositoryVersionException;

    /**
     * Saves and deletes package files according to given request.
     */
    void storeAndDeleteFiles(T requestBody) throws StorageException, FileNotFoundException;

    /**
     * Should be called for the last chunk.
     * It may be useful for some safe-checks
     * or storage operations that can be executed
     * only after all chunks have been processed.
     */
    void handleLastChunk(T requestBody, String repository) throws StorageException;

    /**
     * Returns all directories stored in the root folder of storage.
     */
    List<String> getAllRepositoryDirectories();

    void createRepoDirectory(String name) throws StorageException;

    Technology getTechnology();
}

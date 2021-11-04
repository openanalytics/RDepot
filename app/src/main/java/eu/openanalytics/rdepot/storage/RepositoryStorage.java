/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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
package eu.openanalytics.rdepot.storage;

import java.io.File;
import java.io.IOException;
import java.util.List;

import eu.openanalytics.rdepot.exception.CreateFolderStructureException;
import eu.openanalytics.rdepot.exception.DeleteFileException;
import eu.openanalytics.rdepot.exception.LinkFoldersException;
import eu.openanalytics.rdepot.exception.PackageFolderPopulationException;
import eu.openanalytics.rdepot.exception.RepositoryStorageException;
import eu.openanalytics.rdepot.exception.UploadToRemoteServerException;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;

public interface RepositoryStorage {
	
	/**
	 * This method creates a directory on the local file system that will store packages associated with repositories.
	 * @param repository Repository which directory is created for
	 * @param dateStamp Current date stamp
	 * @throws CreateFolderStructureException
	 */
	public void createFolderStructureForGeneration(Repository repository, String dateStamp) 
			throws CreateFolderStructureException;
	
	
	/**
	 * This method deletes repository generation directory.
	 * @param repository
	 * @param dateStamp
	 * @throws DeleteFileException
	 */
	public void deleteGenerationDirectory(Repository repository, String dateStamp) throws DeleteFileException;
	
	/**
	 * This method links the "current" directory to the newly published directory.
	 * @param repository Repository related to the directory
	 * @param dateStamp Current date stamp
	 * @return Link to the "current" directory
	 * @throws LinkFoldersException 
	 * @throws LinkFoldersException
	 */
	public File linkCurrentFolderToGeneratedFolder(Repository repository, String dateStamp) throws LinkFoldersException;
	
	/**
	 * This method populates generated folder in the storage file system.
	 * @param packages Packages stored by the repository
	 * @param repository Repository to populate
	 * @param dateStamp Current date stamp
	 * @throws PackageFolderPopulationException 
	 */
	public void populateGeneratedFolder(List<Package> packages, Repository repository, String dateStamp) 
			throws PackageFolderPopulationException;
	
	/**
	 * This method deletes repository snapshot
	 * @param repository Repository to delete
	 * @throws IOException
	 */
	public void deleteGeneratedFolder(Repository repository) throws IOException;
	
	/**
	 * This method transfers given packages from target directory to the contrib directory of remote server.
	 * @param files Names of files to transfer from target directory.
	 * @param target Directory to transfer.
	 * @param repository Repository to transfer.
	 * @throws UploadToRemoteServerException 
	 */
	public void copyFromRepositoryToRemoteServer(List<Package> latestPackages, 
			List<Package> archivePackages, File target, Repository repository)
					throws UploadToRemoteServerException;
	
	/**
	 * This method deletes directories related to given repository.
	 * @param repository Repository to delete
	 * @throws DeleteFileException 
	 */
	public void deleteRepositoryDirectory(Repository repository) throws DeleteFileException;
	
	/**
	 * This method deletes current directory from repository generation directory.
	 * @param repository Repository whose current directory is deleted.
	 * @throws DeleteFileException 
	 * @throws RepositoryStorageException
	 */
	public void deleteCurrentDirectory(Repository repository) throws DeleteFileException;	
}

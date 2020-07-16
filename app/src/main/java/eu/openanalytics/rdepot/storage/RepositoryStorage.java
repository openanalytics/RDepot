/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartException;

import eu.openanalytics.rdepot.exception.CreateFolderStructureException;
import eu.openanalytics.rdepot.exception.DeleteFileException;
import eu.openanalytics.rdepot.exception.GzipFileException;
import eu.openanalytics.rdepot.exception.LinkFoldersException;
import eu.openanalytics.rdepot.exception.Md5MismatchException;
import eu.openanalytics.rdepot.exception.Md5SumCalculationException;
import eu.openanalytics.rdepot.exception.PackageFolderPopulationException;
import eu.openanalytics.rdepot.exception.RepositoryStorageException;
import eu.openanalytics.rdepot.exception.UploadToRemoteServerException;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;

@Component
public class RepositoryStorage {
	
	private String separator = FileSystems.getDefault().getSeparator();

    Logger logger = LoggerFactory.getLogger(PackageStorage.class);
	
	@Resource(name="repositoryGenerationDirectory")
	private File repositoryGenerationDirectory;
	
	@Resource(name="packageUploadDirectory")
	private File packageUploadDirectory;
	
	@Resource
	BaseStorage baseStorage;
	
	@Resource
	private MessageSource messageSource;
	
	private Locale locale = LocaleContextHolder.getLocale();
	
	private String generatePackageString(Package packageBag) {
		String packageString = "";
		String lineSeparator = System.getProperty("line.separator");
		
		packageString += "Package: " + packageBag.getName() + lineSeparator;
		packageString += "Version: " + packageBag.getVersion() + lineSeparator;
		if(packageBag.getDepends() != null && !packageBag.getDepends().trim().isEmpty())
			packageString += "Depends: " + packageBag.getDepends() + lineSeparator;
		if(packageBag.getImports() != null && !packageBag.getImports().trim().isEmpty())
			packageString += "Imports: " + packageBag.getImports() + lineSeparator;
		packageString += "License: " + packageBag.getLicense() + lineSeparator;
		packageString += "MD5Sum: " + packageBag.getMd5sum() + lineSeparator;
		packageString += "NeedsCompilation: no" + lineSeparator;
		packageString += lineSeparator;
		
		return packageString;
	}
	
	/**
	 * This method creates a directory on the local file system that will store packages associated with repositories.
	 * @param repository Repository which directory is created for
	 * @param dateStamp Current date stamp
	 * @throws CreateFolderStructureException
	 */
	public void createFolderStructureForGeneration(Repository repository, String dateStamp) 
			throws CreateFolderStructureException {
		
		File dateStampFolder = baseStorage.createFolderStructure(
				repositoryGenerationDirectory.getAbsolutePath()
				+ separator + Integer.toString(repository.getId())
				+ separator + dateStamp);
		
		try {
			baseStorage.createFolderStructure(
					dateStampFolder.getAbsolutePath() + separator + "src" + separator + "contrib");
		} catch(CreateFolderStructureException cfse) {
			try {
				baseStorage.deleteFile(dateStampFolder.getAbsolutePath());
			} catch (DeleteFileException dfe) {
				logger.error("Failed to remove directory " + dateStampFolder.getAbsolutePath());
			}
			
			throw cfse;
		}
	}
	
	/**
	 * This method deletes repository generation directory.
	 * @param repository
	 * @param dateStamp
	 * @throws DeleteFileException
	 */
	public void deleteGenerationDirectory(Repository repository, String dateStamp) throws DeleteFileException {
		baseStorage.deleteFile(repositoryGenerationDirectory.getAbsolutePath()
				+ separator + Integer.toString(repository.getId()) 
				+ separator + dateStamp);
	}
	
	/**
	 * This method links the "current" directory to the newly published directory.
	 * @param repository Repository related to the directory
	 * @param dateStamp Current date stamp
	 * @return Link to the "current" directory
	 * @throws LinkFoldersException 
	 * @throws LinkFoldersException
	 */
	public File linkCurrentFolderToGeneratedFolder(Repository repository, String dateStamp) throws LinkFoldersException {

		return baseStorage.linkTwoFolders(
				repositoryGenerationDirectory.getAbsolutePath() 
					+ separator + repository.getId() + separator + dateStamp,
				repositoryGenerationDirectory.getAbsolutePath() 
					+ separator + repository.getId() + separator + "current");
	}
	
	/**
	 * This method populates generated folder in the storage file system.
	 * @param packages Packages stored by the repository
	 * @param repository Repository to populate
	 * @param dateStamp Current date stamp
	 * @throws PackageFolderPopulationException 
	 */
	public void populateGeneratedFolder(List<Package> packages, Repository repository, String dateStamp) 
			throws PackageFolderPopulationException {
		String folderPath = repositoryGenerationDirectory.getAbsolutePath()
				+ separator + repository.getId()
				+ separator + dateStamp
				+ separator+ "src"
				+ separator + "contrib";
		
		populatePackageFolder(packages, folderPath);
	}
	
	private void populatePackageFolder(List<Package> packages, String folderPath) throws PackageFolderPopulationException {
		String packageString = "";
		
		for(Package packageBag : packages) {
			if(packageBag.isActive()) {
				String targetFilePath = packageBag.getSource();
				String destinationFilePath = folderPath + separator + packageBag.getName() + "_" + packageBag.getVersion() + ".tar.gz";
				
				try {
					Files.copy(new File(targetFilePath).toPath(), new File(destinationFilePath).toPath());
					if(!packageBag.getMd5sum().equals(baseStorage.calculateMd5Sum(destinationFilePath))) {
						throw new Md5MismatchException(messageSource, locale, packageBag);
					}
					
					packageString += generatePackageString(packageBag);
					
					File packagesFile = new File(folderPath + separator + "PACKAGES");
					BufferedWriter writer = new BufferedWriter(new FileWriter(packagesFile));
					writer.write(packageString);
					writer.close();
					
					baseStorage.gzipFile(packagesFile.getAbsolutePath());
				} catch(IOException |
						Md5MismatchException |
						Md5SumCalculationException |
						GzipFileException e) {
					logger.error(e.getClass() + ": " + e.getMessage());
					throw new PackageFolderPopulationException(messageSource, locale, folderPath);
				}
			}
		}
		
	}
	
	private void createTemporaryFoldersForLatestAndArchive(String path) 
			throws CreateFolderStructureException {
		baseStorage.createFolderStructure(path + separator + "latest");
		baseStorage.createFolderStructure(path + separator + "Archive");
	}
	
	private void uploadFilesToRemoteServer(Path uploadPath, String serverAddress) 
			throws UploadToRemoteServerException {
		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		ResponseEntity<String> response = null;
		
		try {
			Files.walk(uploadPath, 1)
				.filter(path -> !path.equals(uploadPath))
				.forEach(path -> map.add("files", new FileSystemResource(path.toFile())));
			
			HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);
			response = rest.postForEntity(serverAddress, request, String.class);
		} catch(IOException | RestClientException e) {
			logger.error(e.getClass() + ": " + e.getMessage());
			throw new UploadToRemoteServerException(messageSource, locale,
					uploadPath.toFile().getAbsolutePath(), serverAddress);
		}
		
		
		if(!response.getStatusCode().is2xxSuccessful() || !Objects.equals(response.getBody(), "OK"))
			throw new UploadToRemoteServerException(messageSource, locale,
					uploadPath.toFile().getAbsolutePath(), serverAddress, response);
	}
	
	private void uploadArchivePackagesToRemoteServer(String rootPath, Repository repository) throws UploadToRemoteServerException {
		Path uploadPath = new File(rootPath + separator + "Archive").toPath();		
		uploadFilesToRemoteServer(uploadPath, repository.getServerAddress() + "/archive");
	}

	private void uploadLatestPackagesToRemoteServer(String rootPath, Repository repository) throws UploadToRemoteServerException {
		Path uploadPath = new File(rootPath + separator + "latest").toPath();
		uploadFilesToRemoteServer(uploadPath, repository.getServerAddress());
	}
	
	/**
	 * This method transfers given packages from target directory to the contrib directory of remote server.
	 * @param files Names of files to transfer from target directory.
	 * @param target Directory to transfer.
	 * @param repository Repository to transfer.
	 * @throws UploadToRemoteServerException 
	 */
	public void copyFromRepositoryToRemoteServer(List<Package> latestPackages, 
			List<Package> archivePackages, File target, Repository repository)
					throws UploadToRemoteServerException {
		Path repoPath = target.toPath().resolve("src").resolve("contrib");
		String latestFolderPath = repoPath.toString() + separator + "latest";
		String archiveFolderPath = repoPath.toString() + separator + "Archive";
		
		try {
			createTemporaryFoldersForLatestAndArchive(repoPath.toString());
			
			populatePackageFolder(latestPackages, latestFolderPath);
			populatePackageFolder(archivePackages, archiveFolderPath);

			uploadLatestPackagesToRemoteServer(repoPath.toString(), repository);			
			uploadArchivePackagesToRemoteServer(repoPath.toString(), repository);
			
			baseStorage.deleteFile(latestFolderPath);
			baseStorage.deleteFile(archiveFolderPath);
		} catch (CreateFolderStructureException |
				PackageFolderPopulationException |
				DeleteFileException e) {
			logger.error(e.getClass() + ": " + e.getMessage());
			throw new UploadToRemoteServerException(messageSource, locale, repository);
		}
	}
	
	/**
	 * This method deletes directories related to given repository.
	 * @param repository Repository to delete
	 * @throws DeleteFileException 
	 */
	public void deleteRepositoryDirectory(Repository repository) throws DeleteFileException {
		baseStorage.deleteFile(packageUploadDirectory.getAbsolutePath() + separator + "repositories" + separator + Integer.toString(repository.getId()));
		baseStorage.deleteFile(repositoryGenerationDirectory.getAbsolutePath() + separator + Integer.toString(repository.getId()));
	}
	
	/**
	 * This method deletes current directory from repository generation directory.
	 * @param repository Repository whose current directory is deleted.
	 * @throws DeleteFileException 
	 * @throws RepositoryStorageException
	 */
	public void deleteCurrentDirectory(Repository repository) throws DeleteFileException {
		File parent = new File(repositoryGenerationDirectory, Integer.toString(repository.getId()));
		File current = new File(parent, "current");
		
		baseStorage.deleteFile(current.getAbsolutePath());
	}

	
}

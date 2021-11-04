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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import eu.openanalytics.rdepot.exception.CreateFolderStructureException;
import eu.openanalytics.rdepot.exception.DeleteFileException;
import eu.openanalytics.rdepot.exception.GzipFileException;
import eu.openanalytics.rdepot.exception.InvalidServerAddressException;
import eu.openanalytics.rdepot.exception.LinkFoldersException;
import eu.openanalytics.rdepot.exception.Md5MismatchException;
import eu.openanalytics.rdepot.exception.Md5SumCalculationException;
import eu.openanalytics.rdepot.exception.PackageFolderPopulationException;
import eu.openanalytics.rdepot.exception.SendSynchronizeRequestException;
import eu.openanalytics.rdepot.exception.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.exception.UploadToRemoteServerException;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.RepoResponse;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.SynchronizeRepositoryRequestBody;

public class RepositoryStorageLocalImpl implements RepositoryStorage {
	
	private String separator = FileSystems.getDefault().getSeparator();

    Logger logger = LoggerFactory.getLogger(PackageStorageLocalImpl.class);
	
	@Resource(name="repositoryGenerationDirectory")
	private File repositoryGenerationDirectory;
	
	@Resource(name="packageUploadDirectory")
	private File packageUploadDirectory;
	
	@Value("${localStorage.maxRequestSize}")
	private Integer maxRequestSize;
	
	@Resource
	BaseStorage baseStorage;
	
	@Resource
	private MessageSource messageSource;
	
	private Locale locale = LocaleContextHolder.getLocale();
	
	private String generatePackageString(Package packageBag) {
		String packageString = "";
		String lineSeparator = System.lineSeparator();
		
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

	public void deleteGenerationDirectory(Repository repository, String dateStamp) throws DeleteFileException {
		baseStorage.deleteFile(repositoryGenerationDirectory.getAbsolutePath()
				+ separator + Integer.toString(repository.getId()) 
				+ separator + dateStamp);
	}

	public File linkCurrentFolderToGeneratedFolder(Repository repository, String dateStamp) throws LinkFoldersException {

		return baseStorage.linkTwoFolders(
				repositoryGenerationDirectory.getAbsolutePath() 
					+ separator + repository.getId() + separator + dateStamp,
				repositoryGenerationDirectory.getAbsolutePath() 
					+ separator + repository.getId() + separator + "current");
	}

	public void populateGeneratedFolder(List<Package> packages, Repository repository, String dateStamp) 
			throws PackageFolderPopulationException {
		String folderPath = repositoryGenerationDirectory.getAbsolutePath()
				+ separator + repository.getId()
				+ separator + dateStamp
				+ separator+ "src"
				+ separator + "contrib";
		
		populatePackageFolder(packages, folderPath);
	}
	
	private void populatePackage(Package packageBag, String folderPath) throws PackageFolderPopulationException {
		
		if(packageBag.isActive()) {
			String targetFilePath = packageBag.getSource();
			String destinationFilePath = folderPath + separator + packageBag.getName() + "_" + packageBag.getVersion() + ".tar.gz";
			
			try {
				File packagesFile = new File(folderPath + separator + "PACKAGES");
				
				Files.copy(new File(targetFilePath).toPath(), new File(destinationFilePath).toPath());
				if(!packageBag.getMd5sum().equals(baseStorage.calculateMd5Sum(destinationFilePath))) {
					throw new Md5MismatchException(messageSource, locale, packageBag);
				}
				
				BufferedWriter writer = new BufferedWriter(new FileWriter(packagesFile, true));
				writer.append(generatePackageString(packageBag));
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
	
	private void populatePackageFolder(List<Package> packages, String folderPath)
			throws PackageFolderPopulationException {		
		for(Package packageBag : packages) {
			populatePackage(packageBag, folderPath);
		}
	}
	
	private void createTemporaryFoldersForLatestAndArchive(String path) 
			throws CreateFolderStructureException {
		File latest = baseStorage.createFolderStructure(path + separator + "latest");
		File archive = baseStorage.createFolderStructure(path + separator + "Archive");
		
		try {
			File packagesLatest = Files.createFile(latest.toPath().resolve("PACKAGES")).toFile();
			File packagesArchive = Files.createFile(archive.toPath().resolve("PACKAGES")).toFile();
			
			baseStorage.gzipFile(packagesLatest.getAbsolutePath());
			baseStorage.gzipFile(packagesArchive.getAbsolutePath());
		} catch(IOException | GzipFileException e) {
			logger.error("Could not create PACKAGES file", e);
			throw new CreateFolderStructureException(messageSource, locale, e.getMessage());
		}
		
	}
	
	private void sendSynchronizeRequest(SynchronizeRepositoryRequestBody request,
			String serverAddress, String repositoryDirectory) throws SendSynchronizeRequestException {
		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		
		List<MultiValueMap<String, Object>> chunks = request.toChunks(maxRequestSize);
		
		logger.debug("Sending chunk to repo...");
		
		try {
			String id = "";
			for(MultiValueMap<String, Object> chunk : chunks) {
				chunk.add("id", id);
				
				HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(chunk);
				ResponseEntity<RepoResponse> httpResponse = 
						rest.postForEntity(serverAddress + "/" + repositoryDirectory, entity, RepoResponse.class);
				
				if(!httpResponse.getStatusCode().is2xxSuccessful() || 
						!Objects.equals(httpResponse.getBody().getMessage(), "OK")) {
					throw new SendSynchronizeRequestException(httpResponse, request, serverAddress, repositoryDirectory);
				}
				
				id = httpResponse.getBody().getId();
			}
		} catch(RestClientException e) {
			logger.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
			throw new SendSynchronizeRequestException(request, serverAddress, repositoryDirectory);
		}
		
	}
	
	private SynchronizeRepositoryRequestBody buildRequestBody(List<Package> latestPackages,
			List<Package> archivePackages, List<String> remoteLatestPackages, 
			List<String> remoteArchivePackages,
			Repository repository, String versionBefore) {		
		List<File> latestToUpload = selectPackagesToUpload(remoteLatestPackages, latestPackages);
		
		File currentDirectory = new File(repositoryGenerationDirectory.getAbsolutePath() +
								separator + repository.getId() 
								+ separator + "current" 
								+ separator + "src" + separator + "contrib" + separator + "latest");
		
		File packagesFile = new File(currentDirectory.getAbsolutePath() + separator + "PACKAGES");
		File packagesGzFile = new File(currentDirectory.getAbsolutePath() + separator + "PACKAGES.gz");

		List<String> latestToDelete = selectPackagesToDelete(remoteLatestPackages, latestPackages);
		
		List<File> archiveToUpload = selectPackagesToUpload(remoteArchivePackages, archivePackages);
		
		File archiveDirectory = new File(repositoryGenerationDirectory.getAbsolutePath() +
				separator + repository.getId() 
				+ separator + "current" 
				+ separator + "src" + separator + "contrib" + separator + "Archive");
		
		File packagesFileFromArchive = new File(archiveDirectory.getAbsolutePath() + separator + "PACKAGES");
		File packagesGzFileFromArchive = new File(archiveDirectory.getAbsolutePath() + separator + "PACKAGES.gz");
		
		List<String> archiveToDelete = selectPackagesToDelete(remoteArchivePackages, archivePackages);
		
		return new SynchronizeRepositoryRequestBody(latestToUpload, archiveToUpload,
				latestToDelete, archiveToDelete, versionBefore, packagesFile, 
				packagesGzFile, packagesFileFromArchive, packagesGzFileFromArchive);
	}
	
	private List<File> selectPackagesToUpload(List<String> remotePackages, List<Package> localPackages) {
		List<File> toUpload = new ArrayList<File>();
		
		for(Package packageBag : localPackages) {
			if(!remotePackages.contains(packageBag.getFileName())) {
				toUpload.add(new File(packageBag.getSource()));
			}
		}
		
		return toUpload;
	}
	
	private List<String> selectPackagesToDelete(List<String> remotePackages, List<Package> localPackages) {
		List<String> toDelete = new ArrayList<String>();
		
		for(String packageName: remotePackages) {
			Boolean found = false;
			for(Package packageBag: localPackages) {
				if(packageBag.getFileName().equals(packageName)) {
					found = true;
					break;
				}
			}
			
			if(!found) {
				toDelete.add(packageName);
			}
		}
		
		return toDelete;
	}
	
	private void synchronizeRepository(List<Package> latestPackages, List<Package> archivePackages, 
			Repository repository) throws SynchronizeRepositoryException, InvalidServerAddressException {
		
		String[] serverAddressComponents = repository.getServerAddress().split("/");
		if(serverAddressComponents.length < 4) {
			throw new InvalidServerAddressException(messageSource, locale, repository, repository.getServerAddress());
		}
		
		String serverAndPort = serverAddressComponents[0] + "//" + serverAddressComponents[2];
		String repositoryDirectory = serverAddressComponents[3];
		
		Gson gson = new Gson();
		RestTemplate rest = new RestTemplate();
		
		ResponseEntity<String> response = rest.getForEntity(serverAndPort + "/" + repositoryDirectory + "/", String.class);

		List<String> remoteLatestPackages = new ArrayList<>(Arrays.asList(gson.fromJson(response.getBody(), String[].class)));
		response = rest.getForEntity(serverAndPort + "/" + repositoryDirectory + "/archive/", String.class);
		List<String> remoteArchivePackages = new ArrayList<>(Arrays.asList(gson.fromJson(response.getBody(), String[].class)));
		
		String versionBefore = remoteLatestPackages.remove(0);

		try {
						
			SynchronizeRepositoryRequestBody requestBody = buildRequestBody(
					latestPackages, archivePackages, remoteLatestPackages, 
					remoteArchivePackages, repository, versionBefore);			
			
			sendSynchronizeRequest(requestBody, serverAndPort, repositoryDirectory);
			
		} catch(SendSynchronizeRequestException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new SynchronizeRepositoryException(messageSource, locale, repository, e.getClass().getName());
		} 		
	}
	
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
		
			synchronizeRepository(latestPackages, archivePackages, repository);
			
			baseStorage.deleteFile(latestFolderPath);
			baseStorage.deleteFile(archiveFolderPath);
		} catch (DeleteFileException | 
				CreateFolderStructureException | 
				PackageFolderPopulationException | 
				SynchronizeRepositoryException | InvalidServerAddressException e) {
			logger.error(e.getClass() + ": " + e.getMessage());
			throw new UploadToRemoteServerException(messageSource, locale, repository);
		}
	}

	public void deleteRepositoryDirectory(Repository repository) throws DeleteFileException {
		baseStorage.deleteFile(packageUploadDirectory.getAbsolutePath() + separator + "repositories" + separator + Integer.toString(repository.getId()));
		baseStorage.deleteFile(repositoryGenerationDirectory.getAbsolutePath() + separator + Integer.toString(repository.getId()));
	}
	
	public void deleteCurrentDirectory(Repository repository) throws DeleteFileException {
		File parent = new File(repositoryGenerationDirectory, Integer.toString(repository.getId()));
		File current = new File(parent, "current");
		
		baseStorage.deleteFile(current.getAbsolutePath());
	}

	@Override
	public void deleteGeneratedFolder(Repository repository) throws IOException {
		String folderPath = repositoryGenerationDirectory.getAbsolutePath()
				+ separator + repository.getId();
		FileUtils.forceDelete(new File(folderPath));
		
	}
}

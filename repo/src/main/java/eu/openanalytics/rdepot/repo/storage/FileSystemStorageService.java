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
package eu.openanalytics.rdepot.repo.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.repo.collection.QueueMap;
import eu.openanalytics.rdepot.repo.exception.EmptyTrashException;
import eu.openanalytics.rdepot.repo.exception.GetRepositoryVersionException;
import eu.openanalytics.rdepot.repo.exception.InitTransactionException;
import eu.openanalytics.rdepot.repo.exception.InitTrashDirectoryException;
import eu.openanalytics.rdepot.repo.exception.InvalidRequestPageNumberException;
import eu.openanalytics.rdepot.repo.exception.MoveToTrashException;
import eu.openanalytics.rdepot.repo.exception.ProcessRequestException;
import eu.openanalytics.rdepot.repo.exception.RepositoryVersionMismatchException;
import eu.openanalytics.rdepot.repo.exception.RestoreRepositoryException;
import eu.openanalytics.rdepot.repo.exception.SetRepositoryVersionException;
import eu.openanalytics.rdepot.repo.exception.StorageException;
import eu.openanalytics.rdepot.repo.model.RepositoryBackup;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryResponseBody;

@Service
public class FileSystemStorageService implements StorageService {
	
	Logger logger = LoggerFactory.getLogger(FileSystemStorageService.class);

    private final Path rootLocation;
    
    private final Set<String> excludedFiles = 
    		new HashSet<String>(Arrays.asList("PACKAGES", "PACKAGES.gz"));
    
    private final String TRASH_PREFIX = "TRASH_";
    private final String TRASH_DATABASE_FILE = "TRASH_DATABASE.txt";
    
    QueueMap<String, SynchronizeRepositoryRequestBody> requestMap;
    
    QueueMap<String, SynchronizeRepositoryResponseBody> responseMap;
    
    HashMap<String, RepositoryBackup> backupMap;
    
    @Autowired
    public FileSystemStorageService(StorageProperties properties, 
    		QueueMap<String, SynchronizeRepositoryRequestBody> requestMap,
    		QueueMap<String, SynchronizeRepositoryResponseBody> responseMap,
    		HashMap<String, RepositoryBackup> backupMap) {
        this.rootLocation = Paths.get(properties.getLocation());
        this.requestMap = requestMap;
        this.responseMap = responseMap;
        this.backupMap = backupMap;
    }
    
    private File initTrashDirectory(String id) throws InitTrashDirectoryException {
    	Path trash = this.rootLocation.resolve(TRASH_PREFIX + id);
    	File trashDatabase = trash.resolve(TRASH_DATABASE_FILE).toFile();
    	
    	try {
	    	if(Files.exists(trash)) {
	    		FileUtils.forceDelete(trash.toFile());
	    	}
		
			Files.createDirectory(trash);
			trashDatabase.createNewFile();
			
		} catch (IOException e) {
			logger.error("Error while creating trash directory: " + e.getMessage(), e);
			throw new InitTrashDirectoryException(id);
		}
    	
    	return trash.toFile();
    }
    
    private void backupRepository(String repository, String id, 
    		File trashDirectory, String repositoryVersion) {
    	List<String> recentPackages = new ArrayList<String>();
    	List<String> archivePackages = new ArrayList<String>();
    	getRecentPackagesFromRepository(repository).forEach(f -> recentPackages.add(f.getName()));
    	getArchiveFromRepository(repository)
    		.values().forEach(l -> l.forEach(f -> archivePackages.add(f.getName())));
    	
    	RepositoryBackup backup = new RepositoryBackup(recentPackages, archivePackages, 
    			trashDirectory, repositoryVersion);
    	
    	backupMap.put(id, backup);
    }
    
    /**
     * Creates queues for communication with the Manager app and backup of current state of repository.
     * @param repository - repository name
     * @param repository version - current repository version
     */
    public synchronized String initTransaction(String repository, String repositoryVersion) 
    		throws InitTransactionException {
    	String id = RandomStringUtils.randomAlphanumeric(16);
    	
    	while(requestMap.containsKey(id) || responseMap.containsKey(id))
    		id = RandomStringUtils.randomAlphanumeric(16);
    	
    	requestMap.createQueue(id);
    	responseMap.createQueue(id);
    	
    	File trashDirectory = null;
    	try {
    		
    		Path repositoryPublicationDirectory = this.rootLocation.resolve(repository);
        	if(Files.notExists(repositoryPublicationDirectory))
        		Files.createDirectory(repositoryPublicationDirectory).toFile();
        	
        	trashDirectory = initTrashDirectory(id);
    	} catch(InitTrashDirectoryException | IOException e) {
    		throw new InitTransactionException(id);
    	}
    	
    	backupRepository(repository, id, trashDirectory, repositoryVersion);
    	
    	return id;
    }

	@Override
	public void processLastRequest() throws ProcessRequestException {
		SynchronizeRepositoryRequestBody request = null;
		try {
			request = requestMap.getLastItem();

			processRequest(request);	
		} catch (InterruptedException e) {
			logger.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
		}
		
	}
    
    public void processRequest(SynchronizeRepositoryRequestBody request) throws ProcessRequestException {
    	String repository = request.getRepository();
    	
    	try {
    		String versionBefore = request.getVersionBefore();
    		String currentVersion = getRepositoryVersion(repository);
    		if(!Objects.equals(versionBefore, currentVersion))
    			throw new RepositoryVersionMismatchException(request);

    		logger.debug("Updating repository...");
    		
    		MultipartFile[] filesToUpload = request.getFilesToUpload();
    		MultipartFile[] filesToUploadToArchive = request.getFilesToUploadToArchive();
    		String[] filesToDelete = request.getFilesToDelete();
    		String[] filesToDeleteFromArchive = request.getFilesToDeleteFromArchive();
    		
    		if(filesToUpload != null)
        		store(filesToUpload, repository);
        	if(filesToUploadToArchive != null)
        		storeInArchive(filesToUploadToArchive, repository);
        	if(filesToDelete != null)
        		delete(filesToDelete, repository, request.getId());
        	if(filesToDeleteFromArchive != null)
        		deleteFromArchive(filesToDeleteFromArchive, repository, request.getId());
        	
        	boostRepositoryVersion(repository);

        	logger.info("Repository updated successfully!");
    	} catch(RepositoryVersionMismatchException | 
    			FileNotFoundException | GetRepositoryVersionException e) {
    		logger.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
    		logger.debug("Trying to restore repository...");
    		
    		RepositoryBackup backup = backupMap.get(request.getId());
    		if(backup != null) {
        		try {
					restoreRepository(repository, 
							backup.getRecentPackages(), 
							backup.getArchivePackages(), 
							request.getVersionBefore());
				} catch (RestoreRepositoryException rre) {
					logger.error("Could not restore repository after failure!", rre);
				}
    		}
    		
    		throw new ProcessRequestException();
    	} finally {
        	try {
				if(request.isLastChunk()) {
					requestMap.remove(request.getId());
					emptyTrash(repository, request.getId());
				}
				
			} catch (InvalidRequestPageNumberException | EmptyTrashException e) {
				logger.error(e.getClass().getCanonicalName() + ": " + e.getMessage());
			}
		}
    }
    
    private void copyToDedicatedDirectory(MultipartFile file, Path rootDirectory) throws IOException {
    	Path saveLocation = rootDirectory.resolve(file.getOriginalFilename().split("_")[0]);
    	if(!Files.exists(saveLocation))
    	{
    		Files.createDirectory(saveLocation);
    	}
    	Files.copy(file.getInputStream(), saveLocation.resolve(file.getOriginalFilename()), 
    			StandardCopyOption.REPLACE_EXISTING);
    }

    private void storeInArchive(MultipartFile[] files, String repository) {
    	Path saveLocation = ((repository != null) && (!repository.trim().isEmpty())) ? this.rootLocation.resolve(repository) : this.rootLocation;
    	saveLocation = saveLocation.resolve("src").resolve("contrib").resolve("Archive");
    	System.out.println("Saving to location " + saveLocation.toString());
		try {
			if(!Files.exists(saveLocation)) {
				Files.createDirectories(saveLocation);
			}
		}
		catch (IOException e) {
            throw new StorageException("Failed to create directory " + saveLocation.toFile().getAbsolutePath(), e);
        }
    	for(MultipartFile file : files)
    	{
	        try 
	        {
	        	if(file.getOriginalFilename().equals("PACKAGES") || file.getOriginalFilename().equals("PACKAGES.gz"))
	        		Files.copy(file.getInputStream(), saveLocation.resolve(file.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
	        	else
	        		copyToDedicatedDirectory(file, saveLocation);
	        } catch (IOException e) {
	            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
	        }
    	}
    }

    private void store(MultipartFile[] files, String repository) 
    {
    	Path saveLocation = ((repository != null) && (!repository.trim().isEmpty())) ? this.rootLocation.resolve(repository) : this.rootLocation;
    	saveLocation = saveLocation.resolve("src").resolve("contrib");
    	System.out.println("Saving to location " + saveLocation.toString());
		try {
			if(!Files.exists(saveLocation)) {
				Files.createDirectories(saveLocation);
			}
		}
		catch (IOException e) {
            throw new StorageException("Failed to create directory " + saveLocation.getFileName(), e);
        }
    	for(MultipartFile file : files)
    	{
	        try 
	        {
	            Files.copy(file.getInputStream(), saveLocation.resolve(file.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
	        } catch (IOException e) {
	            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
	        }
    	}
    }
    
    public List<File> getRecentPackagesFromRepository(String repository) {
    	ArrayList<File> files = new ArrayList<>();
    	Path location = ((repository != null) && (!repository.trim().isEmpty())) ? this.rootLocation.resolve(repository) : this.rootLocation;
    	location = location.resolve("src").resolve("contrib");
    	
    	if(location.toFile().exists()) {
    		for(File file : location.toFile().listFiles()) {
    			if(!file.isDirectory() && !excludedFiles.contains(file.getName())) {
    				files.add(file);    				
    			}
    		}
    	}
		
    	
    	return files;
    }

    public Map<String, List<File>> getArchiveFromRepository(String repository) {
    	Path location = ((repository != null) && (!repository.trim().isEmpty())) ? this.rootLocation.resolve(repository) : this.rootLocation;
    	location = location.resolve("src").resolve("contrib").resolve("Archive");
    	
    	List<Path> directories = new ArrayList<>();
    	Map<String, List<File>> archive = new HashMap<>();

    	if(location.toFile().exists()) {
    		for(File file : location.toFile().listFiles()) {
    			if(!excludedFiles.contains(file.getName())) {
    				directories.add(file.toPath());
    			}
    		}
    	}
		
		
		for(Path directory : directories) {
			List<File> files = new ArrayList<>();
			
			if(directory.toFile().exists()) {
				for(File file : directory.toFile().listFiles()) {
					if(!file.isDirectory() && !excludedFiles.contains(file.getName())) {
						files.add(file);
					}
				}
				archive.put(directory.getFileName().toString(), files);
			}
		}
		
    	return archive;
    }
    
    private void moveToTrash(String id, File packageFile) throws MoveToTrashException {
    	Path trash = this.rootLocation.resolve(TRASH_PREFIX + id);
    	File trashDatabase = trash.resolve(TRASH_DATABASE_FILE).toFile();
    	
    	if(Files.notExists(trash)) {
    		logger.error("No trash directory for transaction: " + id);
    		throw new MoveToTrashException(id, packageFile);
    	}
    	
    	try {
        	FileWriter fileWriter = new FileWriter(trashDatabase);
        	fileWriter.write(packageFile.getName() + ":" + packageFile.getAbsolutePath());
        	fileWriter.close();
        	
			Files.move(packageFile.toPath(),
					trash.resolve(packageFile.getName()), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error("Error while moving file: " + e.getMessage(), e);
			throw new MoveToTrashException(id, packageFile);
		}
    }
    
    public void emptyTrash(String repository, String requestId) throws EmptyTrashException {
    	Path trash = this.rootLocation.resolve(TRASH_PREFIX + requestId);
    	File archive = this.rootLocation.resolve(repository).resolve("src")
    			.resolve("contrib").resolve("Archive").toFile();
    	try {
    		if(Files.exists(trash))
    			FileUtils.forceDelete(trash.toFile());
    		
    		if(archive.isDirectory()) {
    			for(File file : archive.listFiles()) {
    				if(file.isDirectory() && file.listFiles().length == 0) {
        				FileUtils.forceDelete(file);
        			}
    				
    				if(archive.listFiles().length == 2) {
        				for(File packagesFile : archive.listFiles()) {
        					if(packagesFile.getName().equals("PACKAGES") 
        							|| packagesFile.getName().equals("PACKAGES.gz"))
        						FileUtils.forceDelete(packagesFile);
        				}
    				}
        		}
    		}
		} catch (IOException e) {
			logger.error("Could not delete trash directory!", e);
			throw new EmptyTrashException(repository);
		}
    }
    
    private void restoreRepository(String repository, List<String> latestPackages,
    		List<String> archivePackages, String version) throws RestoreRepositoryException {
    	Path trash = this.rootLocation.resolve(TRASH_PREFIX + repository);
		File trashDatabase = trash.resolve(TRASH_DATABASE_FILE).toFile();
    	
		if(Files.exists(trashDatabase.toPath())) {
			Scanner scanner;
			try {
				scanner = new Scanner(trashDatabase);
			} catch (FileNotFoundException e) {
				logger.error("No trash database!", e);
				throw new RestoreRepositoryException(repository);
			}
			
			try {
				while(scanner.hasNextLine()) {
					String data = scanner.nextLine();
					String fileName = data.split(":")[0];
					String previousDirectory = data.split(":")[1];
					
					Files.move(trash.resolve(fileName), new File(previousDirectory).toPath(), 
							StandardCopyOption.REPLACE_EXISTING);
				}
			} catch (IOException e) {
				logger.error("Could not restore file! " + e.getMessage(), e);
				throw new RestoreRepositoryException(repository);
			} finally {
				scanner.close();
			}
		}
		
		Path latestLocation = ((repository != null) && (!repository.trim().isEmpty())) ? 
				this.rootLocation.resolve(repository) : this.rootLocation;
    	File[] latestFiles = latestLocation.resolve("src").resolve("contrib").toFile().listFiles();
    	
    	Path archiveLocation = latestLocation.resolve("Archive");
    	File[] allArchiveFiles = archiveLocation.toFile().listFiles();
    	List<File> archiveFiles = new ArrayList<>();
    	
    	if(allArchiveFiles != null) {
    		for(File file : archiveLocation.toFile().listFiles()) {
        		if(file.isDirectory()) {
        			archiveFiles.addAll(Arrays.asList(file.listFiles()));
        		} else {
        			archiveFiles.add(file);
        		}
        	}
    	} else {
			logger.error("Could not access archive folder to restore repository!");
    	}
    	
    	try {
    		if(latestFiles != null) {
    			for(File packageFile : latestFiles) {
            		if(!latestPackages.contains(packageFile.getName())) {
        				FileUtils.forceDelete(packageFile);
            		}
            	}
    		} else {
    			logger.error("Could not access latest folder to restore repository!");
    		}
    		
        	
    		for(File packageFile : archiveFiles) {
        		if(!archivePackages.contains(packageFile.getName())) {
    				FileUtils.forceDelete(packageFile);
        		}
        	}
        	
        	
        	setRepositoryVersion(repository, version);
    	} catch (IOException | SetRepositoryVersionException e) {
			logger.error("Could not remove file! " + e.getMessage(), e);
			throw new RestoreRepositoryException(repository);
		}
    	
    }
    
    private void setRepositoryVersion(String repository, String version) {
    	Path versionPath = this.rootLocation.resolve(repository).resolve("VERSION");
    	Scanner scanner = null;
    	FileWriter writer = null;
    	
    	try {
    		if(Files.notExists(versionPath)) {
        		Files.createFile(versionPath);
        	}
    		
    		writer = new FileWriter(versionPath.toFile());    		
    		
    		try {
    			Integer.valueOf(version);
    		} catch(NumberFormatException e) {
    			throw e;    			
    		}
    		
    		writer.write(version);
			writer.close();
    	} catch(IOException | NumberFormatException e) {
    		logger.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
    		throw new SetRepositoryVersionException(repository);
    	} finally {
			if(scanner != null)
				scanner.close();
		}
    }
    
    private void delete(String packageName, String repository, String requestId, Boolean fromArchive) 
    		throws FileNotFoundException, StorageException {
    	Path location = ((repository != null) && (!repository.trim().isEmpty())) ? 
    			this.rootLocation.resolve(repository) : this.rootLocation;
    	location = location.resolve("src").resolve("contrib");
    	location = fromArchive ? location.resolve("Archive") : location;
    	
    	try {
    		if((packageName == null || packageName == "") && !fromArchive) {
        		for(File packageFile : location.toFile().listFiles()) {
        			if(!Files.exists(packageFile.toPath()))
            			throw new FileNotFoundException();

        			moveToTrash(requestId, packageFile);
        		}
    		} else {
        		if(fromArchive)
        			location = location.resolve(packageName.split("_")[0]);

        		Path packageFilePath = location.resolve(packageName);
        		
        		if(!Files.exists(packageFilePath))
        			throw new FileNotFoundException(packageFilePath.toString());
        		
        		moveToTrash(requestId, packageFilePath.toFile());
        		
        	}
    	} catch(FileNotFoundException e) {
    		throw e;
    	} catch(MoveToTrashException e) {
    		throw new StorageException("Could not delete package file", e);
    	}
    	
    }
    
    private void delete(String[] packageNames, String repository, String requestId, Boolean fromArchive) 
    		throws FileNotFoundException, StorageException {
    	for(String packageName : packageNames) {
    		delete(packageName, repository, requestId, fromArchive);
    	}
    }
    
	private void delete(String[] packageNames, String repository, String requestId) throws FileNotFoundException, StorageException {
    	delete(packageNames, repository, requestId, false);
	}

	private void deleteFromArchive(String[] packageNames, String repository, String requestId)
			throws FileNotFoundException, StorageException {
    	delete(packageNames, repository, requestId, true);		
	}
	
    @Override
    public void init() {
        try {
            if(!Files.exists(rootLocation))
            {
            	Files.createDirectory(rootLocation);
            }
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
    
    @Override
    public Map<String, File> getPackagesFiles(String repository, boolean archive) {
    	Map<String, File> files = new HashMap<>();
    	
    	Path packagesFilesRoot = this.rootLocation.resolve(repository).resolve("src").resolve("contrib");
    	packagesFilesRoot = archive ? packagesFilesRoot.resolve("Archive") : packagesFilesRoot;
    	
    	File packages = packagesFilesRoot.resolve("PACKAGES").toFile();
    	File packagesGZ = packagesFilesRoot.resolve("PACKAGES.gz").toFile();
    	
    	if(packages.exists() && packagesGZ.exists()) {
    		files.put("PACKAGES", packages);
    		files.put("PACKAGES.gz", packagesGZ);
    	}
    	
    	return files;
    }
    
    @Override
    public String getRepositoryVersion(String repository) throws GetRepositoryVersionException {
    	Path repositoryDirectory = this.rootLocation.resolve(repository);
    	
    	Path versionPath = repositoryDirectory.resolve("VERSION");
    	String versionStr = "";
    	Scanner scanner = null;
    	try {
    		if(Files.notExists(repositoryDirectory))
    			Files.createDirectory(repositoryDirectory);
    		
    		if(Files.notExists(versionPath)) {
    			versionStr = "1";
    			
        		Files.createFile(versionPath);
        		
        		FileWriter writer = new FileWriter(versionPath.toFile());
        		writer.write(versionStr);
        		writer.close();
        	} else {
        		scanner = new Scanner(versionPath);
        		versionStr = scanner.nextLine();
        		Integer.valueOf(versionStr);
        	}
    	} catch(IOException | NumberFormatException e) {
    		logger.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
    		throw new GetRepositoryVersionException(repository);
    	} finally {
    		if(scanner != null)
    			scanner.close();
    	}
    	
    	return versionStr;
    }
    
    private void boostRepositoryVersion(String repository) throws SetRepositoryVersionException {
    	try {
    		String currentVersionStr = getRepositoryVersion(repository);
    		Integer currentVersion = Integer.valueOf(currentVersionStr);
        	String newVersion = String.valueOf(++currentVersion);
        	
        	setRepositoryVersion(repository, newVersion);
    	} catch(GetRepositoryVersionException | NumberFormatException e) {
    		logger.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
    		throw new SetRepositoryVersionException(repository);
    	}
    }


}

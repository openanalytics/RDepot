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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.repo.collection.QueueMap;
import eu.openanalytics.rdepot.repo.exception.EmptyTrashException;
import eu.openanalytics.rdepot.repo.exception.GetRepositoryVersionException;
import eu.openanalytics.rdepot.repo.exception.InitTransactionException;
import eu.openanalytics.rdepot.repo.exception.InitTrashDirectoryException;
import eu.openanalytics.rdepot.repo.exception.MoveToTrashException;
import eu.openanalytics.rdepot.repo.exception.ProcessRequestException;
import eu.openanalytics.rdepot.repo.exception.RepositoryVersionMismatchException;
import eu.openanalytics.rdepot.repo.exception.RestoreRepositoryException;
import eu.openanalytics.rdepot.repo.exception.SetRepositoryVersionException;
import eu.openanalytics.rdepot.repo.exception.StorageException;
import eu.openanalytics.rdepot.repo.model.RepositoryBackup;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryResponseBody;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class FileSystemStorageService<T extends SynchronizeRepositoryRequestBody, B extends RepositoryBackup> 
	implements StorageService<T> {
	
    protected final Path rootLocation;
    
    protected final String TRASH_PREFIX = "TRASH_";
    protected final String TRASH_DATABASE_FILE = "TRASH_DATABASE.txt";
    
    protected QueueMap<String, T> requestMap;
    protected QueueMap<String, SynchronizeRepositoryResponseBody> responseMap;
    protected HashMap<String, B> backupMap;
	
    public FileSystemStorageService(StorageProperties properties, 
    		QueueMap<String, T> requestMap,
    		QueueMap<String, SynchronizeRepositoryResponseBody> responseMap,
    		HashMap<String, B> backupMap) {
        this.rootLocation = Paths.get(properties.getLocation());
        this.requestMap = requestMap;
        this.responseMap = responseMap;
        this.backupMap = backupMap;
    }
    
    protected File initTrashDirectory(String id) throws InitTrashDirectoryException {
    	Path trash = this.rootLocation.resolve(TRASH_PREFIX + id);
    	File trashDatabase = trash.resolve(TRASH_DATABASE_FILE).toFile();
    	
    	try {
	    	if(Files.exists(trash)) {
	    		FileUtils.forceDelete(trash.toFile());
	    	}
		
			Files.createDirectory(trash);
			trashDatabase.createNewFile();
		} catch (IOException e) {
			log.error("Error while creating trash directory: " + e.getMessage(), e);
			throw new InitTrashDirectoryException(id);
		}
    	
    	return trash.toFile();
    }
    
    protected void moveToTrash(String id, File packageFile) throws MoveToTrashException {
    	Path trash = this.rootLocation.resolve(TRASH_PREFIX + id);
    	File trashDatabase = trash.resolve(TRASH_DATABASE_FILE).toFile();
    	
    	if(Files.notExists(trash)) {
    		log.error("No trash directory for transaction: " + id);
    		throw new MoveToTrashException(id, packageFile);
    	}
    	
    	try {
    	    UUID uuid = UUID.randomUUID();
        	FileWriter fileWriter = new FileWriter(trashDatabase, true);
        	fileWriter.append(uuid.toString() + ":" + packageFile.getAbsolutePath() + System.lineSeparator());
        	fileWriter.close();
        	
			Files.move(packageFile.toPath(), trash.resolve(uuid.toString()));
		} catch (IOException e) {
			log.error("Error while moving file: " + e.getMessage(), e);
			throw new MoveToTrashException(id, packageFile);
		}
    }
    
    public void processRequest(T request) throws ProcessRequestException {
    	final String repository = request.getRepository();
    	boolean failed = false;
    	
    	try {
    		String versionBefore = request.getVersionBefore();
    		String currentVersion = getRepositoryVersion(repository);
    		if(!Objects.equals(versionBefore, currentVersion))
    			throw new RepositoryVersionMismatchException(request);
    		
    		log.debug("Updating repository...");
    		
    		storeAndDeleteFiles(request);
    		boostRepositoryVersion(repository);
    		
    		log.info("Repository updated successfully!");
    	} catch(RepositoryVersionMismatchException | GetRepositoryVersionException | 
    			FileNotFoundException | StorageException e) {
    		log.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
    		log.debug("Trying to restore repository...");
    		failed = true;
    		B backup = backupMap.get(request.getId());
    		if(backup != null) {
        		try {
					restoreRepository(
					        request.getId(),
					        repository, 
							backup,
							request);
					
				} catch (RestoreRepositoryException rre) {
					log.error("Could not restore repository after failure!", rre);
				}
    		}
    		
    		throw new ProcessRequestException();
    	} finally {
    		try {
    			if(request.isLastChunk()) {
    				handleLastChunk(request, repository);
    			} else if(failed) {
    				handleFailure(request, repository);
    			}
    		} catch(Exception e) {
    			log.error(e.getClass().getCanonicalName() + ": " + e.getMessage());
    		}
    	}
    }
    
    protected void handleLastChunk(T request, String repository) throws Exception {
    	requestMap.remove(request.getId());
		emptyTrash(repository, request.getId());
    }
    
    protected abstract void handleFailure(T request, String repository);
    
    protected abstract void restoreRepository(String id, String repository, B backup, T request) throws RestoreRepositoryException;
    
    protected abstract void storeAndDeleteFiles(T request) throws FileNotFoundException, StorageException;
    
    protected void store(MultipartFile[] files,  Path saveLocation, String id) {
    	log.debug("Saving to location {}", saveLocation.toString());
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
    	    Path destination = saveLocation.resolve(file.getOriginalFilename());
	        try 
	        {
	            if(Files.exists(destination)) {
	              moveToTrash(id, destination.toFile());
	            }
	            Files.copy(file.getInputStream(), destination);
	        } catch (IOException | MoveToTrashException e) {
	            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
	        }
    	}
    }
    
    protected void emptyTrash(String repository, String requestId) throws EmptyTrashException {
    	Path trash = this.rootLocation.resolve(TRASH_PREFIX + requestId);
    	if(Files.exists(trash)) {
    		try {
				FileUtils.forceDelete(trash.toFile());
			} catch (IOException e) {
				log.error("Could not delete trash directory!", e);
				throw new EmptyTrashException(repository);
			}
    	}
    }
    
    protected void setRepositoryVersion(String repository, String version) {
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
    		log.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
    		throw new SetRepositoryVersionException(repository);
    	} finally {
			if(scanner != null)
				scanner.close();
		}
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
    		log.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
    		throw new GetRepositoryVersionException(repository);
    	} finally {
    		if(scanner != null)
    			scanner.close();
    	}
    	
    	return versionStr;
    }
    
    protected void boostRepositoryVersion(String repository) throws SetRepositoryVersionException {
    	try {
    		String currentVersionStr = getRepositoryVersion(repository);
    		Integer currentVersion = Integer.valueOf(currentVersionStr);
        	String newVersion = String.valueOf(++currentVersion);
        	
        	setRepositoryVersion(repository, newVersion);
    	} catch(GetRepositoryVersionException | NumberFormatException e) {
    		log.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
    		throw new SetRepositoryVersionException(repository);
    	}
    }
    
    
    /**
     * Creates a {@link RepositoryBackup} object 
     * that preserves the state of the repository before synchronization.
     * @param repository
     * @param id
     * @param trashDirectory
     * @param repositoryVersion
     */
    protected abstract B backupRepository(String repository, String id, 
    		File trashDirectory, String repositoryVersion);
    
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
    	
    	backupMap.put(id, 
    			backupRepository(repository, id, trashDirectory, repositoryVersion));
    	
    	return id;
    }
    
    @Override
	public void processLastRequest() throws ProcessRequestException {
		T request = null;
		try {
			request = requestMap.getLastItem();

			processRequest(request);	
		} catch (InterruptedException e) {
			log.error(e.getClass().getCanonicalName() + ": " + e.getMessage(), e);
		}
	}
}
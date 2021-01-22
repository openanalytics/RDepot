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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.util.Strings;
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
import eu.openanalytics.rdepot.repo.exception.MoveToTrashException;
import eu.openanalytics.rdepot.repo.exception.ProcessRequestException;
import eu.openanalytics.rdepot.repo.exception.RepositoryVersionMismatchException;
import eu.openanalytics.rdepot.repo.exception.RestoreRepositoryException;
import eu.openanalytics.rdepot.repo.exception.SetRepositoryVersionException;
import eu.openanalytics.rdepot.repo.exception.StorageException;
import eu.openanalytics.rdepot.repo.model.ArchiveIndex;
import eu.openanalytics.rdepot.repo.model.ArchiveInfo;
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
	
	private void generateArchiveRds(String repository) throws IOException {
	  Path latestLocation = ((repository != null) && (!repository.trim().isEmpty())) ? 
          this.rootLocation.resolve(repository) : this.rootLocation;
	  Path archiveLocation = latestLocation.resolve("src").resolve("contrib").resolve("Archive");
	  
	  if(Files.notExists(archiveLocation)) {
	    Path archiveRds = latestLocation.resolve("src").resolve("contrib").resolve("Meta").resolve("archive.rds");
	    Files.deleteIfExists(archiveRds);
	    return;
	  }
	  
	  Map<String, List<ArchiveInfo>> archives = new HashMap<>();
	  
	  File[] directories = archiveLocation.toFile().listFiles(new FileFilter() {
        @Override
        public boolean accept(File file) {
          return file.canRead() && file.isDirectory();
        }
	  });
	  for(File dir : directories) {
	      List<ArchiveInfo> infos = new ArrayList<>();
	      File[] packages = dir.listFiles(new FileFilter() {
	        @Override
	        public boolean accept(File file) {
	          return file.canRead() && file.isFile();
	        }
	      });
	      for(File file : packages) {
	          LocalDateTime modifiedTime = null;
	          LocalDateTime accessTime = null;
	          LocalDateTime createdTime = null;
	          try {
	            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
	            modifiedTime = LocalDateTime.ofInstant(attr.lastModifiedTime().toInstant(), ZoneId.systemDefault());
	            accessTime = LocalDateTime.ofInstant(attr.lastAccessTime().toInstant(), ZoneId.systemDefault());
	            createdTime = LocalDateTime.ofInstant(attr.creationTime().toInstant(), ZoneId.systemDefault());
	          } catch (Exception e) {
	            logger.debug(
	                "Exception when querying for basic file attributes of path " + file.toString() + ": " + e.getMessage(), e);
	          }
	          infos.add(
	            new ArchiveInfo(
	                dir.getName() + "/" + file.getName(), Long.valueOf(file.length()).intValue(),
	                436, modifiedTime, createdTime, accessTime, 1000, 1000, null, null));
	          }
	      if(!infos.isEmpty()) {
	        archives.put(dir.getName(), infos);
	      }
	  }
	  if(archives.isEmpty()) {
        Path archiveRds = latestLocation.resolve("src").resolve("contrib").resolve("Meta").resolve("archive.rds");
        Files.deleteIfExists(archiveRds);
        return;
      }
	  
	  ArchiveIndex archiveIndex = new ArchiveIndex(archives);
	  Path metaLocation = latestLocation.resolve("src").resolve("contrib").resolve("Meta");
	  if(Files.notExists(metaLocation)) {
	    Files.createDirectories(metaLocation);
	  }
	  Path archiveRds = metaLocation.resolve("archive.rds");
	  FileOutputStream archiveRdsStrean = new FileOutputStream(archiveRds.toFile());
	  BufferedOutputStream outputStream = new BufferedOutputStream(archiveRdsStrean);
	  archiveIndex.serialize(outputStream);
	}
    
    public void processRequest(SynchronizeRepositoryRequestBody request) throws ProcessRequestException {
    	String repository = request.getRepository();
    	boolean failed = false;

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
        		store(filesToUpload, repository, request.getId());
        	if(filesToUploadToArchive != null)
        		storeInArchive(filesToUploadToArchive, repository, request.getId());
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
    		failed = true;
    		RepositoryBackup backup = backupMap.get(request.getId());
    		if(backup != null) {
        		try {
					restoreRepository(
					        request.getId(),
					        repository, 
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
		            generateArchiveRds(repository);
					requestMap.remove(request.getId());
					emptyTrash(repository, request.getId());
				} else if (failed) {
				  generateArchiveRds(repository);
				}
				
			} catch (Exception e) {
				logger.error(e.getClass().getCanonicalName() + ": " + e.getMessage());
			}
		}
    }
    
    private void copyToDedicatedDirectory(MultipartFile file, Path rootDirectory, String id) throws IOException, MoveToTrashException {
    	Path saveLocation = rootDirectory.resolve(file.getOriginalFilename().split("_")[0]);
    	if(!Files.exists(saveLocation))
    	{
    		Files.createDirectory(saveLocation);
    	}
    	Path destination = saveLocation.resolve(file.getOriginalFilename());  
        if(Files.exists(destination)) {
            moveToTrash(id, destination.toFile());
        }
    	Files.copy(file.getInputStream(), saveLocation.resolve(file.getOriginalFilename()));
    }

    private void storeInArchive(MultipartFile[] files, String repository, String id) {
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
	        	if(file.getOriginalFilename().equals("PACKAGES") || file.getOriginalFilename().equals("PACKAGES.gz")) {
	        	    Path destination = saveLocation.resolve(file.getOriginalFilename());  
	        	    if(Files.exists(destination)) {
	                    moveToTrash(id, destination.toFile());
	                }
	        	    Files.copy(file.getInputStream(), destination);
	        	} else {
	        		copyToDedicatedDirectory(file, saveLocation, id);
	        	}
	        } catch (IOException | MoveToTrashException e) {
	            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
	        }
    	}
    }

    private void store(MultipartFile[] files, String repository, String id) 
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
    	    UUID uuid = UUID.randomUUID();
        	FileWriter fileWriter = new FileWriter(trashDatabase, true);
        	fileWriter.append(uuid.toString() + ":" + packageFile.getAbsolutePath() + System.lineSeparator());
        	fileWriter.close();
        	
			Files.move(packageFile.toPath(), trash.resolve(uuid.toString()));
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
    
    private void restoreRepository(String id, String repository, List<String> latestPackages,
    		List<String> archivePackages, String version) throws RestoreRepositoryException {
    	Path trash = this.rootLocation.resolve(TRASH_PREFIX + id);
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
					if(Strings.isBlank(data)) {
					  continue;
					}
					String fileName = data.split(":")[0];
					String previousDirectory = data.split(":")[1];
					
					Files.move(trash.resolve(fileName), new File(previousDirectory).toPath(), 
							StandardCopyOption.REPLACE_EXISTING);
				}
                new FileWriter(trashDatabase).close();
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
    	
    	Path archiveLocation = latestLocation.resolve("src").resolve("contrib").resolve("Archive");
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
    			    if(packageFile.getName().equals("Archive") || packageFile.getName().equals("PACKAGES") || packageFile.getName().equals("PACKAGES.gz")) {
    			      continue;
    			    } else if(!latestPackages.contains(packageFile.getName())) {
    			      FileUtils.forceDelete(packageFile);
            		}
            	}
    		} else {
    			logger.error("Could not access latest folder to restore repository!");
    		}
    		
        	
    		for(File packageFile : archiveFiles) {
    		  if(packageFile.getName().equals("PACKAGES") || packageFile.getName().equals("PACKAGES.gz")) {
                continue;
              } else if(!archivePackages.contains(packageFile.getName())) {
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

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
package eu.openanalytics.rdepot.repo.python.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.logging.log4j.util.Strings;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.repo.collection.QueueMap;
import eu.openanalytics.rdepot.repo.exception.EmptyTrashException;
import eu.openanalytics.rdepot.repo.exception.MoveToTrashException;
import eu.openanalytics.rdepot.repo.exception.RestoreRepositoryException;
import eu.openanalytics.rdepot.repo.exception.SetRepositoryVersionException;
import eu.openanalytics.rdepot.repo.exception.StorageException;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryResponseBody;
import eu.openanalytics.rdepot.repo.python.model.PythonRepositoryBackup;
import eu.openanalytics.rdepot.repo.python.model.SynchronizePythonRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.storage.FileSystemStorageService;
import eu.openanalytics.rdepot.repo.storage.StorageProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j	
@Service
public class PythonFileSystemStorageService 
	extends FileSystemStorageService<SynchronizePythonRepositoryRequestBody, PythonRepositoryBackup> 
	implements PythonStorageService {
	
	private static final String packagesIndexFileName = "index.html";
	
    private final Set<String> excludedFiles = 
    		new HashSet<String>(Arrays.asList(packagesIndexFileName));
    
    public PythonFileSystemStorageService(StorageProperties properties,
    		@Qualifier("requestPythonMap")
    		QueueMap<String, SynchronizePythonRepositoryRequestBody> requestMap,
    		@Qualifier("responsePythonMap")
    		QueueMap<String, SynchronizeRepositoryResponseBody> responseMap,
    		@Qualifier("backupPythonMap")
    		HashMap<String, PythonRepositoryBackup> backupMap) {
    	super(properties, requestMap, responseMap, backupMap);
    }
    
    @Override
    protected PythonRepositoryBackup backupRepository(String repository, String id, 
    		File trashDirectory, String repositoryVersion) {
    	List<String> recentPackages = new ArrayList<String>();
    	getRecentPackagesFromRepository(repository).forEach(f -> recentPackages.add(f.getName()));

    	return new PythonRepositoryBackup(recentPackages, 
    			trashDirectory, repositoryVersion);
    }

    private void storeFiles(MultipartFile[] files,  Path saveLocation, String id) {
    	log.debug("Saving to location {}", saveLocation.toString());
    	for(MultipartFile file: files) {
    		unpackFiles(file, saveLocation);
    	}
    }
    
    private void unpackFiles(MultipartFile file, Path saveLocation) {
    	Path packageDir = saveLocation;
    	try {
			String dirName =	file.getOriginalFilename();	
			if(!dirName.equals("index.tar.gz")) {
				packageDir = Paths.get(saveLocation.toString() + "/" + dirName.substring(0, dirName.length() -7));    				
			}
			if(!Files.exists(packageDir)) {
					Files.createDirectories(packageDir);
			}
			File fileToUnpack = new File(packageDir.toString() + "/" + file.getOriginalFilename());
			if(!fileToUnpack.exists()) {
				fileToUnpack.createNewFile();
			}
			file.transferTo(fileToUnpack);

			File unGzippedFile = null;
			unGzippedFile = unGzip(fileToUnpack, new File(packageDir.toString()));
			unTar(unGzippedFile, new File(packageDir.toString()));
		}
		catch (IOException e) {
			throw new StorageException("Failed to create directory " + saveLocation.getFileName(), e);
		} catch (ArchiveException e) {
			throw new StorageException("Failed to untar packages" + saveLocation.getFileName(), e);
		} finally {
			cleanSpace(file, packageDir);
		}
	}
    
    private void cleanSpace(MultipartFile file, Path packageDir) {
		if(packageDir.toFile().isDirectory()) {
			for (File packageFile: packageDir.toFile().listFiles()) {
				boolean isTarFile = packageFile.getName().contains(".tar") && !packageFile.getName().contains(".gz");
				if(isTarFile) {
					packageFile.delete();
				}
			}
		}
    }
    
    private File unGzip(final File inputFile, final File outputDir) throws IOException {
		log.debug(String.format("Ungzipping %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));
		
		final File outputFile = new File(outputDir, inputFile.getName().substring(0, inputFile.getName().length() - 3));

	    final GZIPInputStream in = new GZIPInputStream(new FileInputStream(inputFile));
	    final FileOutputStream out = new FileOutputStream(outputFile);

	    IOUtils.copy(in, out);

	    in.close();
	    out.close();
	    inputFile.delete();
	    return outputFile;
	}
    
    private void unTar(final File inputFile, final File outputDir) throws IOException, ArchiveException {
		log.debug(String.format("Untarring %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));
	    final InputStream is = new FileInputStream(inputFile); 
	    final TarArchiveInputStream debInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is);
	    TarArchiveEntry entry = null; 
	    
	    while ((entry = (TarArchiveEntry)debInputStream.getNextEntry()) != null) {
	        final File outputFile = new File(outputDir, entry.getName());
	        File outputFileParentDir = outputFile.getParentFile();
	        
	        if(!outputFileParentDir.exists()) {
	        	outputFileParentDir.mkdirs();
	        }
	        
	        if (entry.isDirectory()) {	            
	            if (!outputFile.exists()) {
	                if (!outputFile.mkdirs()) {
	                    throw new IllegalStateException(String.format("Couldn't create directory %s.", outputFile.getAbsolutePath()));
	                }
	            }
	        } else {
	            final OutputStream outputFileStream = new FileOutputStream(outputFile); 
	            IOUtils.copy(debInputStream, outputFileStream);
	            outputFileStream.close();
	        }
	    }
	    debInputStream.close();
	}
    @Override
    protected void storeAndDeleteFiles(SynchronizePythonRepositoryRequestBody request) throws FileNotFoundException, StorageException {
    	final String repository = request.getRepository();
    	MultipartFile[] filesToUpload = request.getFilesToUpload();
		String[] filesToDelete = request.getFilesToDelete();
		
		if(filesToUpload != null)
    		store(filesToUpload, repository, request.getId());
    	if(filesToDelete != null)
    		delete(filesToDelete, repository, request.getId());
    }

    private void store(MultipartFile[] files, String repository, String id) 
    {
    	Path saveLocation = ((repository != null) && (!repository.trim().isEmpty())) ? this.rootLocation.resolve(repository) : this.rootLocation;
    	storeFiles(files, saveLocation, id);
    }
    
    public List<File> getRecentPackagesFromRepository(String repository) {
    	ArrayList<File> files = new ArrayList<>();
    	Path location = ((repository != null) && (!repository.trim().isEmpty())) ? this.rootLocation.resolve(repository) : this.rootLocation;
    	getRecentPackagesFromRepository(files, location);
    	return files;
    }
    
    public void getRecentPackagesFromRepository(ArrayList<File> files, Path location) {
    	if(location.toFile().exists()) {
    		for(File file : location.toFile().listFiles()) {
    			if(file.isDirectory()) {
    				getRecentPackagesFromRepository(files, file.toPath());
    			} else if(!excludedFiles.contains(file.getName())) {
    				if(!file.getName().equals("index.html") && !file.getName().equals("VERSION") ) {
    					files.add(file);
    				}
    			} 
    		}
    	}
    }

    public Map<String, List<File>> getArchiveFromRepository(String repository) {
    	Map<String, List<File>> archive = new HashMap<>();
    	return archive;
    }
    
    public void emptyTrash(String repository, String requestId) throws EmptyTrashException {
    	Path trash = this.rootLocation.resolve(TRASH_PREFIX + requestId);
    	try {
    		if(Files.exists(trash))
    			FileUtils.forceDelete(trash.toFile());
		} catch (IOException e) {
			log.error("Could not delete trash directory!", e);
			throw new EmptyTrashException(repository);
		}
    }
    
    @Override
    protected void restoreRepository(String id, String repository, PythonRepositoryBackup backup, SynchronizePythonRepositoryRequestBody request) throws RestoreRepositoryException {
    	final List<String> latestPackages = backup.getRecentPackages();
    	final String version = request.getVersionBefore();
    	
    	Path trash = this.rootLocation.resolve(TRASH_PREFIX + id);
		File trashDatabase = trash.resolve(TRASH_DATABASE_FILE).toFile();
    	
		if(Files.exists(trashDatabase.toPath())) {
			Scanner scanner;
			try {
				scanner = new Scanner(trashDatabase);
			} catch (FileNotFoundException e) {
				log.error("No trash database!", e);
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
				log.error("Could not restore file! " + e.getMessage(), e);
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
			log.error("Could not access archive folder to restore repository!");
    	}
    	
    	try {
    		if(latestFiles != null) {
    			for(File packageFile : latestFiles) {
    			    if(packageFile.getName().equals("Archive") || packageFile.getName().equals(packagesIndexFileName)) {
    			      continue;
    			    } else if(!latestPackages.contains(packageFile.getName())) {
    			      FileUtils.forceDelete(packageFile);
            		}
            	}
    		} else {
    			log.error("Could not access latest folder to restore repository!");
    		}
        	setRepositoryVersion(repository, version);
    	} catch (IOException | SetRepositoryVersionException e) {
			log.error("Could not remove file! " + e.getMessage(), e);
			throw new RestoreRepositoryException(repository);
		}
    	
    }

    private void delete(String packageName, String repository, String requestId) 
    		throws FileNotFoundException, StorageException {
    	Path location = ((repository != null) && (!repository.trim().isEmpty())) ? 
    			this.rootLocation.resolve(repository) : this.rootLocation;
       	
    	try {
    		if((packageName == null || packageName == "") ) {
        		for(File packageFile : location.toFile().listFiles()) {
        			if(!Files.exists(packageFile.toPath()))
            			throw new FileNotFoundException();

        			moveToTrash(requestId, packageFile);
        		}
    		} else {
    			location = location.resolve(packageName.substring(0, packageName.indexOf('/')));
        		Path packageFilePath = location.resolve(packageName.substring(packageName.indexOf('/') + 1));
        		
        		if(!Files.exists(packageFilePath))
        			throw new FileNotFoundException(packageFilePath.toString());
        		
        		moveToTrash(requestId, packageFilePath.toFile());
        		
        		File parentDirectory = location.toFile();  
        		if(parentDirectory.isDirectory()) {
        			String[] filesInParentDirectory = parentDirectory.list();
        			if(filesInParentDirectory.length == 1 && filesInParentDirectory[0].equals("index.html")) {
        				moveToTrash(requestId, location.resolve(filesInParentDirectory[0]).toFile());
        				parentDirectory.delete();
        			}
        		}
        		
        		
        	}
    	} catch(FileNotFoundException e) {
    		throw e;
    	} catch(MoveToTrashException e) {
    		throw new StorageException("Could not delete package file", e);
    	}
    	
    }
    
    private void delete(String[] packageNames, String repository, String requestId) 
    		throws FileNotFoundException, StorageException {
    	for(String packageName : packageNames) {
    		delete(packageName, repository, requestId);
    	}
    }
    
    @Override
    public Map<String, File> getPackagesFiles(String repository, boolean archive) {
    	Map<String, File> files = new HashMap<>();
    	
    	Path packagesFilesRoot = this.rootLocation.resolve(repository).resolve("src").resolve("contrib");
    	packagesFilesRoot = archive ? packagesFilesRoot.resolve("Archive") : packagesFilesRoot;
    	
    	File packages = packagesFilesRoot.resolve(packagesIndexFileName).toFile();
    	
    	if(packages.exists()) {
    		files.put("i", packages);
    	}
    	return files;
    }

	@Override
	protected void handleFailure(SynchronizePythonRepositoryRequestBody request, String repository) {
		// TODO: #32978 
	}
}

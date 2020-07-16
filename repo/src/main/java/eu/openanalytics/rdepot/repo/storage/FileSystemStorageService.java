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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }
    
    private void copyToDedicatedDirectory(MultipartFile file, Path rootDirectory) throws IOException {
    	Path saveLocation = rootDirectory.resolve(file.getOriginalFilename().split("_")[0]);
    	if(!Files.exists(saveLocation))
    	{
    		Files.createDirectory(saveLocation);
    	}
    	Files.copy(file.getInputStream(), saveLocation.resolve(file.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
    }
    //TODO: Refactoring
    @Override
    public void storeInArchive(MultipartFile[] files, String repository) {
    	Path saveLocation = ((repository != null) && (!repository.trim().isEmpty())) ? this.rootLocation.resolve(repository) : this.rootLocation;
    	saveLocation = saveLocation.resolve("src").resolve("contrib").resolve("Archive");
    	System.out.println("Saving to location " + saveLocation.toString());
		try {
			final Path contrib = Files.createDirectories(saveLocation);
			Files.walk(contrib)
				 .filter(i -> !contrib.equals(i))
			     .map(Path::toFile)
			     .forEach(File::delete);
		}
		catch (IOException e) {
            throw new StorageException("Failed to create directory " + saveLocation.getFileName(), e);
        }
    	for(MultipartFile file : files)
    	{
	        try 
	        {
	        	copyToDedicatedDirectory(file, saveLocation);
	            //Files.copy(file.getInputStream(), saveLocation.resolve(file.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
	        } catch (IOException e) {
	            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
	        }
    	}
    }

    @Override
    public void store(MultipartFile[] files, String repository) 
    {
    	Path saveLocation = ((repository != null) && (!repository.trim().isEmpty())) ? this.rootLocation.resolve(repository) : this.rootLocation;
    	saveLocation = saveLocation.resolve("src").resolve("contrib");
    	System.out.println("Saving to location " + saveLocation.toString());
		try {
			final Path contrib = Files.createDirectories(saveLocation);
			Files.walk(contrib)
				 .filter(i -> !contrib.equals(i))
			     .map(Path::toFile)
			     .forEach(File::delete);
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
}

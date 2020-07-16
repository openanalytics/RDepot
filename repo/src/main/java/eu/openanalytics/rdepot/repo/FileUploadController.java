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
package eu.openanalytics.rdepot.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.repo.storage.StorageFileNotFoundException;
import eu.openanalytics.rdepot.repo.storage.StorageService;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }
    
    @PostMapping("/archive")
    @ResponseBody
    public ResponseEntity<String> handleArchiveFileUpload(@RequestParam("files") MultipartFile[] files) {
    	return handleArchiveFileUpload("", files);
    }
    
    @PostMapping("/{repository:.+}/archive")
    @ResponseBody
    public ResponseEntity<String> handleArchiveFileUpload(@PathVariable String repository, @RequestParam("files") MultipartFile[] files) {
    	storageService.storeInArchive(files, repository);
    	return ResponseEntity
    			.ok()
    			.body("OK");
    }
    
    @PostMapping("/")
    @ResponseBody
    public ResponseEntity<String> handleFileUpload(@RequestParam("files") MultipartFile[] files) 
    {
        return handleFileUpload("", files);
    }

    @PostMapping("/{repository:.+}")
    @ResponseBody
    public ResponseEntity<String> handleFileUpload(@PathVariable String repository, @RequestParam("files") MultipartFile[] files)
    {
        storageService.store(files, repository);
        return ResponseEntity
                .ok()
                .body("OK");
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}

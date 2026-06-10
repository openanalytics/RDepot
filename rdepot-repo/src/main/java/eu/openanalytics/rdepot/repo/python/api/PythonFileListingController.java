/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
package eu.openanalytics.rdepot.repo.python.api;

import eu.openanalytics.rdepot.repo.exception.GetRepositoryVersionException;
import eu.openanalytics.rdepot.repo.hash.HashCalculator;
import eu.openanalytics.rdepot.repo.hash.model.HashMethod;
import eu.openanalytics.rdepot.repo.python.storage.implementations.PythonFileSystemStorageService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(value = "/python")
public class PythonFileListingController {

    private final PythonFileSystemStorageService storageService;
    private final HashCalculator hashCalculator;

    @GetMapping("/{repository}/")
    public ResponseEntity<List<String>> recentUploads(
            @PathVariable("repository") String repository,
            @RequestParam(name = "checksum", defaultValue = "SHA256") HashMethod hashMethod) {
        ArrayList<String> uploads = new ArrayList<>();
        try {
            uploads.add(storageService.getRepositoryVersion(repository));
        } catch (GetRepositoryVersionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        try {
            List<Path> files = storageService.getRecentPackagesFromRepository(repository);
            for (Path file : files) {
                uploads.add(file.getParent().getFileName().toString() + "/"
                        + file.getFileName().toString()
                        + "="
                        + hashCalculator
                                .calculate(Files.newInputStream(file), hashMethod)
                                .orElseThrow());
            }
        } catch (NoSuchElementException | IOException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok(uploads);
    }
}

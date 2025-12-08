/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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
package eu.openanalytics.rdepot.repo.r.api;

import eu.openanalytics.rdepot.repo.api.FileListingController;
import eu.openanalytics.rdepot.repo.hash.HashCalculator;
import eu.openanalytics.rdepot.repo.r.storage.CranStorageService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/r")
public class CranFileListingController extends FileListingController {
    private final CranStorageService cranStorageService;

    public CranFileListingController(CranStorageService storageService, HashCalculator hashCalculator) {
        super(storageService, hashCalculator);
        this.cranStorageService = storageService;
    }

    @GetMapping("/{repository}/")
    public ResponseEntity<List<String>> recentUploads(@PathVariable("repository") String repository) {
        return super.recentUploads(repository);
    }

    @GetMapping("/{repository}/archive/")
    public ResponseEntity<List<String>> archiveUploads(@PathVariable("repository") String repository) {
        return super.archiveUploads(repository);
    }

    @GetMapping("/{repository}/platforms")
    public ResponseEntity<List<String>> platformUploads(@PathVariable("repository") String repository) {
        return ResponseEntity.ok(cranStorageService.getBinaryPlatformDirectories(repository));
    }

    @GetMapping("/{repository}/{source}/{packagesFile}")
    public void downloadPackagesFile(
            @PathVariable("repository") String repository,
            @PathVariable("packagesFile") String packagesFile,
            @PathVariable("source") String source,
            HttpServletResponse response) {
        super.downloadPackagesFile(repository, packagesFile, source, response);
    }
}

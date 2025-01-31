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
package eu.openanalytics.rdepot.python.synchronization;

import eu.openanalytics.rdepot.python.entities.enums.HashMethod;
import java.io.File;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SynchronizeRepositoryRequestBody {
    private List<File> filesToUpload;
    private List<String> filesToDelete;
    private String versionBefore;
    private String versionAfter;
    private String repository;
    private String page;
    private String id;
    private final HashMethod hashMethod;
    private final Map<String, String> checksums;

    private final String separator = FileSystems.getDefault().getSeparator();

    public SynchronizeRepositoryRequestBody(
            List<File> filesToUpload,
            List<String> filesToDelete,
            String versionBefore,
            String repositoryName,
            HashMethod hashMethod,
            Map<String, String> checksums) {
        this.filesToUpload = filesToUpload;
        this.filesToDelete = filesToDelete;
        this.versionBefore = versionBefore;
        this.repository = repositoryName;
        this.hashMethod = hashMethod;
        this.checksums = checksums;
    }
}

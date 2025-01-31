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
package eu.openanalytics.rdepot.test.unit.synchronization;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.FileSystemResource;

@AllArgsConstructor
@Getter
public class UploadChunkRequestAssertion {
    public final String expectedId;
    public final String expectedVersionBefore;
    public final String expectedVersionAfter;
    public final List<String> expectedPages;
    public final List<String> expectedToDelete;
    public final List<String> expectedToDeleteFromArchive;
    public final List<FileSystemResource> filesToUpload;
    public final List<FileSystemResource> filesToUploadToArchive;
    public final Map<String, String> expectedToUploadPaths;
    public final Map<String, String> expectedToUploadToArchivePaths;
    public final Map<String, String> expectedToDeletePaths;
    public final Map<String, String> expectedToDeleteFromArchivePaths;
}

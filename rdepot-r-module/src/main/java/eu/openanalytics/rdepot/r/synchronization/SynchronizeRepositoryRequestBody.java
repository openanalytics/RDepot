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
package eu.openanalytics.rdepot.r.synchronization;

import java.io.File;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.MultiValueMap;

@Data
@AllArgsConstructor
public class SynchronizeRepositoryRequestBody {

    List<File> sourcePackagesToUpload;
    List<File> sourcePackagesToUploadToArchive;
    List<String> sourcePackagesToDelete;
    List<String> sourcePackagesToDeleteFromArchive;
    String sourceDirectoryPath;

    String versionBefore;
    String versionAfter;

    // <binary_folder_path, binary_package>
    MultiValueMap<String, File> binaryPackagesToUpload;
    MultiValueMap<String, File> binaryPackagesToUploadToArchive;
    MultiValueMap<String, String> binaryPackagesToDelete;
    MultiValueMap<String, String> binaryPackagesToDeleteFromArchive;

    // <folder_path, PACKAGES>
    Map<String, File> packagesFiles;
    Map<String, File> packagesGzFiles;
    Map<String, File> packagesFilesForArchive;
    Map<String, File> packagesGzFilesForArchive;

    // <folder_path, <file, checksum>>
    Map<String, Map<String, String>> checksums;
}

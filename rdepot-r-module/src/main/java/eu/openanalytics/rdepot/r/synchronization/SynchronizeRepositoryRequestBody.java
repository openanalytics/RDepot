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

import eu.openanalytics.rdepot.base.synchronization.checksums.Checksums;
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

    /**
     * e.g. <code><br/>
     *     {<br/>
     *     &emsp;"bin/linux/ubuntu/x86_64/v1.0.0": [<br/>
     *     &emsp;&emsp;File(abc_1.0.0.tar.gz),<br/>
     *     &emsp;&emsp;File(A3.1.0.0.tar.gz)<br/>
     *     &emsp;],<br/>
     *     &emsp;"bin/linux/centos7/x86_64/v1.0.0": [<br/>
     *     &emsp;&emsp;File(oaColors_1.0.0.tar.gz)<br/>
     *     &emsp;]<br/>
     *     }
     * </code>
     */
    MultiValueMap<String, File> binaryPackagesToUpload;
    /**
     * e.g. <code><br/>
     *     {<br/>
     *     &emsp;"bin/linux/ubuntu/x86_64/v1.0.0": [<br/>
     *     &emsp;&emsp;File(abc_1.0.0.tar.gz),<br/>
     *     &emsp;&emsp;File(A3.1.0.0.tar.gz)<br/>
     *     &emsp;],<br/>
     *     &emsp;"bin/linux/centos7/x86_64/v1.0.0": [<br/>
     *     &emsp;&emsp;File(oaColors_1.0.0.tar.gz)<br/>
     *     &emsp;]<br/>
     *     }
     * </code>
     */
    MultiValueMap<String, File> binaryPackagesToUploadToArchive;

    MultiValueMap<String, String> binaryPackagesToDelete;
    MultiValueMap<String, String> binaryPackagesToDeleteFromArchive;

    /**
     * Key is the directory on the remote repo
     *  (e.g. <code>src/contrib</code> or <code>src/contrib/Archive</code>)
     */
    Map<String, File> packagesFiles;

    /**
     * Key is the directory on the remote repo
     *  (e.g. <code>src/contrib</code> or <code>src/contrib/Archive</code>)
     */
    Map<String, File> packagesGzFiles;

    /**
     * Key is the directory on the remote repo
     *  (e.g. <code>src/contrib</code> or <code>src/contrib/Archive</code>)
     */
    Map<String, File> packagesFilesForArchive;

    /**
     * Key is the directory on the remote repo
     *  (e.g. <code>src/contrib</code> or <code>src/contrib/Archive</code>)
     */
    Map<String, File> packagesGzFilesForArchive;

    // <folder_path, <file, checksum>>
    //    Map<String, Map<String, String>> checksums;
    // <full file path in storage, checksum>

    /**
     * <Full path in storage, checksum>
     */
    Checksums checksums;
    // <folder_path, index.html>
    Map<String, File> indexes;
    Map<String, File> indexesForArchive;
}

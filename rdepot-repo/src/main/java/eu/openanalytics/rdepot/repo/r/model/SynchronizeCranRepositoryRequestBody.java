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
package eu.openanalytics.rdepot.repo.r.model;

import eu.openanalytics.rdepot.repo.hash.model.HashMethod;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.model.Technology;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class SynchronizeCranRepositoryRequestBody extends SynchronizeRepositoryRequestBody {
    MultipartFile[] filesToUploadToArchive;
    String[] filesToDeleteFromArchive;

    /**
     * For example: binlinuxcentos7x866445_PACKAGES -> bin/linux/centos7/x86_64/4.5
     */
    Map<String, String> pathsToUpload;

    /**
     * For example: binlinuxcentos7x866445Archive_index_archived.html -> bin/linux/centos7/x86_64/4.5/Archive
     */
    Map<String, String> pathsToUploadToArchive;

    /**
     * For example: ggplot2_4.0.0.tar.gz -> bin/linux/centos7/x86_64/4.5
     */
    Map<String, String> pathsToDelete;

    /**
     * For example: ggplot2_3.5.2.tar.gz -> bin/linux/centos7/x86_64/4.5
     */
    Map<String, String> pathsToDeleteFromArchive;

    public SynchronizeCranRepositoryRequestBody(
            String id,
            MultipartFile[] filesToUpload,
            MultipartFile[] filesToUploadToArchive,
            String[] filesToDelete,
            String[] filesToDeleteFromArchive,
            String versionBefore,
            String versionAfter,
            String page,
            String repository,
            Map<String, String> paths,
            Map<String, String> pathsToUploadToArchive,
            Map<String, String> pathsToDelete,
            Map<String, String> pathsToDeleteFromArchive,
            Map<String, String> checksums) {
        super(
                page,
                repository,
                id,
                versionBefore,
                versionAfter,
                checksums,
                HashMethod.MD5,
                filesToUpload,
                filesToDelete);
        this.filesToUploadToArchive = filesToUploadToArchive == null ? new MultipartFile[0] : filesToUploadToArchive;
        this.filesToDeleteFromArchive = filesToDeleteFromArchive == null ? new String[0] : filesToDeleteFromArchive;
        this.pathsToUpload = paths;
        this.pathsToUploadToArchive = pathsToUploadToArchive;
        this.pathsToDelete = pathsToDelete == null ? new HashMap<>() : pathsToDelete;
        this.pathsToDeleteFromArchive = pathsToDeleteFromArchive == null ? new HashMap<>() : pathsToDeleteFromArchive;
    }

    @Override
    public Technology getTechnology() {
        return Technology.R;
    }
}

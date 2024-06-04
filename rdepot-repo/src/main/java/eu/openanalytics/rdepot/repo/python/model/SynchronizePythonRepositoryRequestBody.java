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
package eu.openanalytics.rdepot.repo.python.model;

import eu.openanalytics.rdepot.repo.hash.model.HashMethod;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.model.Technology;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public class SynchronizePythonRepositoryRequestBody extends SynchronizeRepositoryRequestBody {

    public SynchronizePythonRepositoryRequestBody(
            String id,
            MultipartFile[] filesToUpload,
            String[] filesToDelete,
            String versionBefore,
            String versionAfter,
            String page,
            String repository,
            Map<String, String> checksums,
            HashMethod hashMethod) {
        super(page, repository, id, versionBefore, versionAfter, checksums, hashMethod, filesToUpload, filesToDelete);
    }

    @Override
    public Technology getTechnology() {
        return Technology.PYTHON;
    }
}

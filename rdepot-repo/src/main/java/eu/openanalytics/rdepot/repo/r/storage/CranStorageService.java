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
package eu.openanalytics.rdepot.repo.r.storage;

import eu.openanalytics.rdepot.repo.exception.RestoreRepositoryException;
import eu.openanalytics.rdepot.repo.model.Technology;
import eu.openanalytics.rdepot.repo.r.model.SynchronizeCranRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.storage.StorageService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface CranStorageService extends StorageService<SynchronizeCranRepositoryRequestBody> {
    Map<String, File> getPackagesFiles(String repository, boolean archive);

    Map<String, List<Path>> getArchiveFromRepository(String repository) throws IOException;

    void removeNonExistingArchivePackagesFromRepo(List<String> packages, String repository)
            throws RestoreRepositoryException;

    void generateArchiveRds(String repository) throws IOException;

    @Override
    default Technology getTechnology() {
        return Technology.R;
    }
}

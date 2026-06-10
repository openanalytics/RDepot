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
package eu.openanalytics.rdepot.repo.r.storage;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface CranRepositoryStorageExplorer {

    /*
    Returns all head directories for binary and source packages, e.g.:
    <ul>
        <li>src/contrib</li>
        <li>bin/linux/centos7/x86_64/4.5</li>
    </ul>
     */
    default Set<String> getExistingHeadRepositoryDirectories(String repository) throws IOException {
        return Set.of("src/contrib");
    }

    /**
     * Returns all platform directories for binary packages
     * (even if currently there are no packages inside).
     * @param repository repository name
     */
    List<String> getBinaryPlatformDirectories(String repository);
}

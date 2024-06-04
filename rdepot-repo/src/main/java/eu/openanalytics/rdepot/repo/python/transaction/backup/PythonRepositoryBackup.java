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
package eu.openanalytics.rdepot.repo.python.transaction.backup;

import eu.openanalytics.rdepot.repo.transaction.backup.RepositoryBackup;
import java.io.File;
import java.util.List;

public class PythonRepositoryBackup extends RepositoryBackup {

    public PythonRepositoryBackup(List<String> recentPackages, File trashDirectory, String version) {
        super(recentPackages, trashDirectory, version);
    }

    public List<String> getRecentPackages() {
        return getPackages();
    }
}

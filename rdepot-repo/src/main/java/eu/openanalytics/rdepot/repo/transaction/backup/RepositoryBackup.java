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
package eu.openanalytics.rdepot.repo.transaction.backup;

import java.io.File;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * It is used to restore the state of repository
 * from before the transaction in case of failure.
 * It consists of the list of packages that were stored before the transaction
 * and a trash directory that stores deleted packages.
 */
@Getter
@Setter
public abstract class RepositoryBackup {
    List<String> packages;
    File trashDirectory;
    String version;

    public RepositoryBackup(List<String> packages, File trashDirectory, String version) {
        this.packages = packages;
        this.trashDirectory = trashDirectory;
        this.version = version;
    }
}

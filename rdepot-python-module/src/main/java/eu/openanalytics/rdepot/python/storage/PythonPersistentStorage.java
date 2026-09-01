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
package eu.openanalytics.rdepot.python.storage;

import eu.openanalytics.rdepot.base.storage.PersistentStorage;
import eu.openanalytics.rdepot.base.storage.exceptions.StoreFileException;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;

public interface PythonPersistentStorage extends PersistentStorage<PythonPackage, PythonRepository> {
    /**
     * Renames a Python package in the persistent storage to conform to
     * the convention specified in {@link PythonPackage#getPackageFilename()}
     */
    void renamePackageFileToBeMoreAccurate(PythonPackage packageBag) throws StoreFileException;
}

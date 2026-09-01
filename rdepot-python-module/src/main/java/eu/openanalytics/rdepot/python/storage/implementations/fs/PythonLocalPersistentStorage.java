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
package eu.openanalytics.rdepot.python.storage.implementations.fs;

import eu.openanalytics.rdepot.base.storage.exceptions.StoreFileException;
import eu.openanalytics.rdepot.base.storage.implementations.CommonFSPersistentStorage;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.storage.PythonPersistentStorage;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@ConditionalOnProperty(name = "storage.implementation", havingValue = "local", matchIfMissing = true)
@Component
public class PythonLocalPersistentStorage extends CommonFSPersistentStorage<PythonPackage, PythonRepository>
        implements PythonPersistentStorage {
    public PythonLocalPersistentStorage(@Qualifier("packageUploadDirectory") File packageUploadDirectory) {
        super(packageUploadDirectory);
    }

    @Override
    public void renamePackageFileToBeMoreAccurate(PythonPackage packageBag) throws StoreFileException {
        String newName = packageBag.getPackageFilename();

        File renamedPackage = new File(FilenameUtils.getPath(packageBag.getSource()), newName);
        try {
            FileUtils.moveFile(new File(packageBag.getSource()), renamedPackage, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new StoreFileException(renamedPackage);
        }

        packageBag.setSource(renamedPackage.getPath());
    }
}

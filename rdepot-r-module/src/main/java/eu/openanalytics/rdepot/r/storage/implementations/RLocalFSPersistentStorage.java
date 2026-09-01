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
package eu.openanalytics.rdepot.r.storage.implementations;

import eu.openanalytics.rdepot.base.storage.implementations.CommonFSPersistentStorage;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.storage.PersistentRStorage;
import java.io.File;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "storage.implementation", havingValue = "local", matchIfMissing = true)
@Component
public class RLocalFSPersistentStorage extends CommonFSPersistentStorage<RPackage, RRepository>
        implements PersistentRStorage {
    public RLocalFSPersistentStorage(@Qualifier("packageUploadDirectory") File packageUploadDirectory) {
        super(packageUploadDirectory);
    }
}

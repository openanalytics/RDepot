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
package eu.openanalytics.rdepot.r.manuals;

import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.storage.exceptions.GenerateManualException;

/**
 * It is part of {@link eu.openanalytics.rdepot.base.storage.PersistentStorage persistent storage}.
 * The implementation should put manuals in the storage backend of choice.
 */
public interface ManualGenerator {

    /**
     * Generates the manual for given package and puts it in persistent storage.
     */
    void generateManual(RPackage packageBag) throws GenerateManualException;
}

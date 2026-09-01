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
package eu.openanalytics.rdepot.r.storage.population;

import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.Vignette;
import eu.openanalytics.rdepot.r.storage.exceptions.ReadPackageVignetteException;
import java.io.File;
import java.util.List;

public interface VignetteReader {
    /**
     * Fetches links to available vignettes for a given package.
     */
    List<Vignette> getAvailableVignettes(RPackage packageBag);

    /**
     * Reads vignette from storage.
     */
    byte[] readVignette(RPackage packageBag, String filename) throws ReadPackageVignetteException;

    /**
     * Fetches all vignette files from the given directory.
     */
    List<File> findVignettesInDir(File extractedDir) throws ReadPackageVignetteException;
}

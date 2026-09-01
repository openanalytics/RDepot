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
package eu.openanalytics.rdepot.r.storage.packagesfile;

import eu.openanalytics.rdepot.base.storage.LocalStorage;
import eu.openanalytics.rdepot.base.storage.exceptions.ContentEditException;
import eu.openanalytics.rdepot.base.storage.exceptions.GzipFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.Md5SumCalculationException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.storage.exceptions.GeneratePackagesFileException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PackagesFileDescriptorCreator {

    private final LocalStorage<RPackage> localStorage;
    private static final String PACKAGES = "PACKAGES";
    private static final String PACKAGES_GZ = "PACKAGES.gz";

    public PackagesFileDescriptorCreator(LocalStorage<RPackage> localStorage) {
        this.localStorage = localStorage;
    }

    /**
     * Creates {@link PackagesFileDescriptor Descriptors} for PACKAGES files.
     *
     * @param remoteFolder e.g. <code>src/contrib</code>
     * @param localPath e.g. <code>{rdepotGeneratedDir}/{repositoryId}/{datestamp}/bin/{os}/{distro}/{architecture}/{rVersion}/latest</code>
     * @return descriptors for both <code>PACKAGES</code> and <code>PACKAGES.gz</code> files
     * @throws GeneratePackagesFileException when preparing the PACKAGES(.gz) file fails
     * @throws Md5SumCalculationException when calculating the MD5 sum of the PACKAGE(.gz) file fails
     */
    public Set<PackagesFileDescriptor> createDescriptors(String remoteFolder, String localPath)
            throws Md5SumCalculationException, GeneratePackagesFileException {
        final String packagesPath = localPath + localStorage.getSeparator() + PACKAGES;
        final String packagesGzPath = localPath + localStorage.getSeparator() + PACKAGES_GZ;
        try {
            localStorage.removeEmptyLinesFromEnd(packagesPath);
            localStorage.gzipFile(packagesPath);
        } catch (ContentEditException | GzipFileException e) {
            log.error("{}: {}", e.getClass(), e.getMessage());
            throw new GeneratePackagesFileException();
        }
        return Set.of(
                new PackagesFileDescriptor(remoteFolder, packagesPath, localStorage.calculateMd5Sum(packagesPath)),
                new PackagesFileDescriptor(remoteFolder, packagesGzPath, localStorage.calculateMd5Sum(packagesGzPath)));
    }
}

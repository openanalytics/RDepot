/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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

import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.storage.exceptions.DeleteFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.GzipFileException;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.storage.BinLocation;
import eu.openanalytics.rdepot.r.storage.BinLocationSet;
import eu.openanalytics.rdepot.r.storage.exceptions.GeneratePackagesFileException;
import java.io.IOException;
import java.nio.file.FileSystems;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Generates PACKAGES file which indexes R Packages for package managers.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class PackageStringGenerator {

    private static final String PACKAGES = "PACKAGES";
    private static final String separator = FileSystems.getDefault().getSeparator();
    private final Storage<RPackage> storage;

    private String generatePackageString(RPackage packageBag) {
        final StringBuilder packageString = new StringBuilder(500);
        final String lineSeparator = System.lineSeparator();

        packageString
                .append("Package: ")
                .append(separateLines(packageBag.getName(), lineSeparator))
                .append(lineSeparator);
        packageString
                .append("Version: ")
                .append(separateLines(packageBag.getVersion(), lineSeparator))
                .append(lineSeparator);
        if (packageBag.getDepends() != null && !packageBag.getDepends().trim().isEmpty())
            packageString
                    .append("Depends: ")
                    .append(separateLines(packageBag.getDepends(), lineSeparator))
                    .append(lineSeparator);
        if (packageBag.getImports() != null && !packageBag.getImports().trim().isEmpty())
            packageString
                    .append("Imports: ")
                    .append(separateLines(packageBag.getImports(), lineSeparator))
                    .append(lineSeparator);
        if (packageBag.getSuggests() != null && !packageBag.getSuggests().trim().isEmpty())
            packageString
                    .append("Suggests: ")
                    .append(separateLines(packageBag.getSuggests(), lineSeparator))
                    .append(lineSeparator);
        packageString
                .append("License: ")
                .append(separateLines(packageBag.getLicense(), lineSeparator))
                .append(lineSeparator);
        if (packageBag.getLinkingTo() != null && !packageBag.getLinkingTo().isEmpty())
            packageString
                    .append("LinkingTo: ")
                    .append(separateLines(packageBag.getLinkingTo(), lineSeparator))
                    .append(lineSeparator);
        if (packageBag.getEnhances() != null && !packageBag.getEnhances().isEmpty())
            packageString
                    .append("Enhances: ")
                    .append(separateLines(packageBag.getEnhances(), lineSeparator))
                    .append(lineSeparator);
        if (packageBag.getPriority() != null && !packageBag.getPriority().isEmpty())
            packageString
                    .append("Priority: ")
                    .append(separateLines(packageBag.getPriority(), lineSeparator))
                    .append(lineSeparator);
        packageString
                .append("MD5Sum: ")
                .append(separateLines(packageBag.getMd5sum(), lineSeparator))
                .append(lineSeparator);
        packageString
                .append("NeedsCompilation: ")
                .append(packageBag.isNeedsCompilation() ? "yes" : "no")
                .append(lineSeparator);
        if (packageBag.isBinary())
            packageString
                    .append("Built: ")
                    .append(separateLines(packageBag.getBuilt(), lineSeparator))
                    .append(lineSeparator);
        packageString.append(lineSeparator);

        return packageString.toString();
    }

    private String separateLines(String lines, String lineSeparator) {
        return lines.replace("\\n", lineSeparator);
    }

    public void addPackageToPackagesFile(RPackage packageBag, String path) throws GeneratePackagesFileException {
        try {
            storage.appendText(generatePackageString(packageBag), path);
            storage.gzipFile(path);
        } catch (IOException | GzipFileException e) {
            log.error("{}: {}", e.getClass(), e.getMessage());
            throw new GeneratePackagesFileException();
        }
    }

    public void generatePackagesFiles(BinLocationSet packages) throws GeneratePackagesFileException {

        for (BinLocation location : packages.getAllBinLocations()) {
            final String path = location.location() + separator + PACKAGES;
            try {
                storage.removeFileIfExists(path);
            } catch (DeleteFileException e) {
                log.error(e.getMessage(), e);
                throw new GeneratePackagesFileException();
            }
            for (RPackage packageBag : location.packages()) {
                addPackageToPackagesFile(packageBag, path);
            }
        }
    }
}

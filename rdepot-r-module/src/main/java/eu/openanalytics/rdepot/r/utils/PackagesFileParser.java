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
package eu.openanalytics.rdepot.r.utils;

import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.mirroring.pojos.RemoteRPackage;
import eu.openanalytics.rdepot.r.utils.exceptions.ParsePackagesFileException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Parses PACKAGES file which indicates what file are stored in CRAN repository.
 */
@Slf4j
public class PackagesFileParser {

    private void setValue(RPackage packageBag, String key, String value) {
        switch (key) {
            case "Package" -> packageBag.setName(value);
            case "Version" -> packageBag.setVersion(value);
            case "Depends" -> packageBag.setDepends(value);
            case "Imports" -> packageBag.setImports(value);
            case "License" -> packageBag.setLicense(value);
            case "MD5sum" -> packageBag.setMd5sum(value);
            case "MD5Sum" -> {
                if (packageBag.getMd5sum() == null || packageBag.getMd5sum().isBlank()) {
                    packageBag.setMd5sum(value);
                }
            }
        }
    }

    public List<RemoteRPackage> parseToRemotePackages(File packagesFile) throws ParsePackagesFileException {
        return parse(packagesFile).stream()
                .map(p -> new RemoteRPackage(p.getName(), p.getVersion(), p.getMd5sum()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Parses PACKAGES file.
     * @param packagesFile The given PACKAGES file to parse
     * @return list of packages assumed to be in the repository
     * @throws ParsePackagesFileException when any I/O or out-of-bounds errors happen
     */
    public List<RPackage> parse(File packagesFile) throws ParsePackagesFileException {
        List<RPackage> packages = new ArrayList<>();
        String line = null;
        String key = "";
        String value = "";
        String previousLine = null;

        try (BufferedReader br = new BufferedReader(new FileReader(packagesFile))) {
            RPackage packageBag = new RPackage();
            boolean previousPackageAlreadyAdded = true;
            while ((line = br.readLine()) != null) {
                previousLine = line;
                if (line.isEmpty()) {
                    if (previousPackageAlreadyAdded) continue;
                    packages.add(packageBag);
                    packageBag = new RPackage();
                    previousPackageAlreadyAdded = true;
                } else if (line.startsWith(" ")) {
                    boolean addWhiteSpaceSeparator = !value.isBlank();
                    value += (addWhiteSpaceSeparator ? " " : "") + line.trim();
                    setValue(packageBag, key, value);
                    previousPackageAlreadyAdded = false;
                } else {
                    previousPackageAlreadyAdded = false;
                    if (!line.contains(":")) {
                        throw new ParsePackagesFileException(line);
                    }
                    String[] parsed = line.split(line.endsWith(":") ? ":" : ": ");
                    key = parsed[0];
                    if (parsed.length == 2) {
                        value = parsed[1];
                    } else {
                        value = "";
                    }
                    setValue(packageBag, key, value);
                }
            }
            if (Objects.nonNull(previousLine) && !previousLine.isBlank()) {
                packages.add(packageBag);
            }
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new ParsePackagesFileException(line);
        }

        return packages;
    }
}

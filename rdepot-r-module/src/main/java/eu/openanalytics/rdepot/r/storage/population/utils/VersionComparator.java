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
package eu.openanalytics.rdepot.r.storage.population.utils;

import eu.openanalytics.rdepot.r.storage.population.PopulatedRPackage;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public final class VersionComparator {
    private static int compareVersions(String sourceVersion, String binaryVersion) {

        String[] sourceVersionSplit = StringUtils.splitByWholeSeparator(sourceVersion, ".");
        String[] binaryVersionSplit = StringUtils.splitByWholeSeparator(binaryVersion, ".");

        int maxLength = Math.min(sourceVersionSplit.length, binaryVersionSplit.length);

        for (int i = 0; i < maxLength; i++) {
            if (Integer.valueOf(sourceVersionSplit[i]).compareTo(Integer.valueOf(binaryVersionSplit[i])) != 0) {
                return Integer.valueOf(sourceVersionSplit[i]).compareTo(Integer.valueOf(binaryVersionSplit[i]));
            }
        }

        if (sourceVersionSplit.length > binaryVersionSplit.length) {
            return 1;
        }

        return 0;
    }

    /**
     * Iterates over the provided package list and finds the one whose version is
     * higher than provided version.
     */
    public static Optional<PopulatedRPackage> findNewerPackage(
            List<PopulatedRPackage> packages, String name, String version) {
        return packages.stream()
                .filter(p -> p.getName().equals(name))
                .filter(p -> compareVersions(p.getVersion(), version) > 0)
                .findFirst();
    }
}

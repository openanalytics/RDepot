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
package eu.openanalytics.rdepot.r.storage;

import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.storage.population.PopulatedRPackage;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Collection of {@link BinLocation Bin Locations}.
 */
public class BinLocationSet {
    private final HashMap<String, BinLocation> binLocations = new HashMap<>();
    private final HashMap<String, BinLocation> binLocationsByRemoteLocation = new HashMap<>();

    public BinLocationSet() {}

    public BinLocationSet(BinLocationSet binLocationSet) {
        for (Map.Entry<String, BinLocation> binLocation : binLocationSet.binLocations.entrySet()) {
            final BinLocation oldLocation = binLocation.getValue();
            final BinLocation newLocation = copy(oldLocation);
            binLocations.put(binLocation.getKey(), newLocation);
            binLocationsByRemoteLocation.put(newLocation.remoteLocation(), newLocation);
        }
    }

    private BinLocation copy(BinLocation oldLocation) {
        return new BinLocation(
                oldLocation.location(),
                oldLocation.remoteLocation(),
                new LinkedList<>(oldLocation.packages()),
                new LinkedList<>(oldLocation.packagesToPopulate()));
    }

    public void addPackageToLocationIfExists(String location, PopulatedRPackage packageBag) {
        if (binLocations.containsKey(location)) {
            binLocations.get(location).packages().add(packageBag);
        }
    }

    private void addEmptyLocationToPopulate(String location, String remoteLocation) {
        final BinLocation newLocation =
                new BinLocation(location, remoteLocation, new LinkedList<>(), new LinkedList<>());
        binLocations.put(location, newLocation);
        binLocationsByRemoteLocation.put(newLocation.remoteLocation(), newLocation);
    }

    public void addEmptyLocationToPopulateIfNotExists(String location, String remoteLocation) {
        if (!binLocations.containsKey(location)) {
            addEmptyLocationToPopulate(location, remoteLocation);
        }
    }

    public void addPackageToLocationToPopulate(String location, String remoteLocation, RPackage packageBag) {
        if (!binLocations.containsKey(location)) {
            addEmptyLocationToPopulate(location, remoteLocation);
        }
        binLocations.get(location).packagesToPopulate().add(packageBag);
    }

    public List<PopulatedRPackage> getPackagesForRemoteLocation(String remoteLocation) {
        if (!binLocationsByRemoteLocation.containsKey(remoteLocation)) {
            return List.of();
        }
        return binLocationsByRemoteLocation.get(remoteLocation).packages();
    }

    public Collection<BinLocation> getAllBinLocations() {
        return binLocations.values();
    }

    public Collection<String> getAllBinLocationPaths() {
        return binLocations.keySet();
    }

    public Set<String> getAllRemoteLocations() {
        return binLocations.values().stream()
                .map(BinLocation::remoteLocation)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void removePackageFromLocation(String location, PopulatedRPackage rPackage) {
        final BinLocation localLocation = binLocations.get(location);
        if (localLocation == null || localLocation.packages().isEmpty()) {
            return;
        }
        localLocation.packages().remove(rPackage);
    }

    public void addLocation(BinLocation binLocation) {
        binLocations.put(binLocation.location(), binLocation);
        binLocationsByRemoteLocation.put(binLocation.remoteLocation(), binLocation);
    }

    public void addLocations(Collection<BinLocation> locations) {
        locations.forEach(this::addLocation);
    }
}

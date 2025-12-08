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
package eu.openanalytics.rdepot.r.synchronization.checksums;

import eu.openanalytics.rdepot.base.synchronization.checksums.Checksum;
import eu.openanalytics.rdepot.base.synchronization.checksums.Checksums;
import eu.openanalytics.rdepot.r.storage.BinLocation;
import eu.openanalytics.rdepot.r.storage.indexes.RIndexDescriptor;
import eu.openanalytics.rdepot.r.storage.packagesfile.PackagesFileDescriptor;
import eu.openanalytics.rdepot.r.storage.population.PopulatedRPackage;
import eu.openanalytics.rdepot.r.storage.population.PopulatedRepositoryContent;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RChecksumResolver {

    public Checksums resolveChecksumsForPopulatedContent(@NonNull PopulatedRepositoryContent content) {
        final Checksums checksums = new Checksums();
        content.latestPackages().forEach(new AddSourceChecksum(checksums));
        content.archivePackages().forEach(new AddSourceChecksum(checksums));
        content.binLatestPackagesPaths().getAllBinLocations().forEach(new AddBinChecksum(checksums));
        content.binArchivePackagesPaths().getAllBinLocations().forEach(new AddBinChecksum(checksums));
        content.indexes().forEach(index -> checksums.addChecksum(resolveIndexChecksum(index)));
        content.packagesFiles().forEach(new AddPackagesFileChecksum(checksums));
        return checksums;
    }

    private Checksum resolveIndexChecksum(@NonNull RIndexDescriptor indexDescriptor) {
        final String checksum = indexDescriptor.checksum();
        return new Checksum(indexDescriptor.indexLocalPath(), checksum);
    }

    private record AddPackagesFileChecksum(@NonNull Checksums checksums) implements Consumer<PackagesFileDescriptor> {

        @Override
        public void accept(@NonNull PackagesFileDescriptor packagesFileDescriptor) {
            final String checksum = packagesFileDescriptor.checksum();
            checksums.addChecksum(new Checksum(packagesFileDescriptor.localPath(), checksum));
        }
    }

    private record AddBinChecksum(Checksums checksums) implements Consumer<BinLocation> {

        @Override
        public void accept(@NonNull BinLocation binLocation) {
            binLocation.packages().forEach(new AddSourceChecksum(checksums));
        }
    }

    private record AddSourceChecksum(Checksums checksums) implements Consumer<PopulatedRPackage> {

        @Override
        public void accept(@NonNull PopulatedRPackage p) {
            checksums.addChecksum(new Checksum(p.getPopulatedPath(), p.getMd5sum()));
        }
    }
}

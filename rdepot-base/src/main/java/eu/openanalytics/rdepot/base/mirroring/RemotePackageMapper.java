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
package eu.openanalytics.rdepot.base.mirroring;

import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredPackage;
import eu.openanalytics.rdepot.base.mirroring.pojos.RemotePackage;
import eu.openanalytics.rdepot.base.utils.PackageVersionComparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class RemotePackageMapper<MP extends MirroredPackage, RP extends RemotePackage, M extends Mirror<MP>> {

    protected final PackageVersionComparator packageVersionComparator;
    protected final MirrorSynchronizationStatusCoordinator mirrorSynchronizationStatusCoordinator;

    protected RemotePackageMapper(MirrorSynchronizationStatusCoordinator mirrorSynchronizationStatusCoordinator) {
        this.packageVersionComparator = new PackageVersionComparator();
        this.mirrorSynchronizationStatusCoordinator = mirrorSynchronizationStatusCoordinator;
    }

    public abstract MP convertRemoteToExpected(RP remotePackage, M mirror);

    Map<MP, RP> mapExpectedToRemoteAndRegisterUnmapped(List<MP> expectedPackages, List<RP> remotePackages) {
        Map<MP, RP> expectedToRemoteMapping = new LinkedHashMap<>();

        for (MP expectedPackage : expectedPackages) {
            RP remotePackage = null;
            for (RP rp : remotePackages) {
                if (namesMatch(expectedPackage, rp)) {
                    if (expectedPackage.getVersion() != null
                                    && expectedPackage.getVersion().equals(rp.getVersion())
                            || expectedPackage.getVersion() == null && remotePackage == null
                            || expectedPackage.getVersion() == null
                                    && packageVersionComparator.compare(rp.getVersion(), remotePackage.getVersion())
                                            > 0) {
                        remotePackage = rp;
                    }
                }
            }
            if (remotePackage == null) {
                mirrorSynchronizationStatusCoordinator.registerPackageMirroringFinishedWithError(
                        expectedPackage, MessageCodes.ERROR_PACKAGE_NOT_FOUND);
            } else {
                expectedToRemoteMapping.put(expectedPackage, remotePackage);
            }
        }

        return expectedToRemoteMapping;
    }

    protected boolean namesMatch(MP expectedPackage, RP remotePackage) {
        return remotePackage.getName().equals(expectedPackage.getName());
    }
}

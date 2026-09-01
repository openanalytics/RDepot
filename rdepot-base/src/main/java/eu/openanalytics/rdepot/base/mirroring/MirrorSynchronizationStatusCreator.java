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

import eu.openanalytics.rdepot.base.entities.MirrorSynchronizationStatus;
import eu.openanalytics.rdepot.base.entities.PackageSynchronizationStatus;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositorySynchronizationStatus;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredPackage;
import java.util.Date;
import java.util.List;

class MirrorSynchronizationStatusCreator<P extends MirroredPackage, M extends Mirror<P>> {

    RepositorySynchronizationStatus createNewSynchronizationStatus(Repository repository, List<M> mirrors) {
        final RepositorySynchronizationStatus status = new RepositorySynchronizationStatus();
        status.setRepository(repository);
        status.setTimestamp(new Date());
        status.setPending(true);
        status.setTechnology(repository.getTechnology());
        status.getMirrors().addAll(createMirrorSynchronizationStatuses(mirrors, status));
        return status;
    }

    private List<MirrorSynchronizationStatus> createMirrorSynchronizationStatuses(
            List<M> mirrors, RepositorySynchronizationStatus repositorySynchronizationStatus) {
        return mirrors.stream()
                .map(m -> createMirrorSynchronizationStatus(m, repositorySynchronizationStatus))
                .toList();
    }

    private MirrorSynchronizationStatus createMirrorSynchronizationStatus(
            M mirror, RepositorySynchronizationStatus repositorySynchronizationStatus) {
        final MirrorSynchronizationStatus status =
                new MirrorSynchronizationStatus(mirror, repositorySynchronizationStatus);
        status.getPackageSynchronizationStatuses().addAll(createPackageSynchronizationStatuses(mirror, status));
        return status;
    }

    private List<PackageSynchronizationStatus> createPackageSynchronizationStatuses(
            M mirror, MirrorSynchronizationStatus mirrorSynchronizationStatus) {
        return mirror.getPackages().stream()
                .map(mp -> createPackageSynchronizationStatus(mp, mirrorSynchronizationStatus))
                .toList();
    }

    private PackageSynchronizationStatus createPackageSynchronizationStatus(
            P mirroredPackage, MirrorSynchronizationStatus mirrorSynchronizationStatus) {
        return new PackageSynchronizationStatus(mirroredPackage, mirrorSynchronizationStatus);
    }
}

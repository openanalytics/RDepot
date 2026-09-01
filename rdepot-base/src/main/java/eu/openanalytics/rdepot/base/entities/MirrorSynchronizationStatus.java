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
package eu.openanalytics.rdepot.base.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import eu.openanalytics.rdepot.base.mirroring.Mirror;
import eu.openanalytics.rdepot.base.mirroring.pojos.SynchronizationStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

public class MirrorSynchronizationStatus extends SynchronizationStatus {
    @Getter
    private final List<PackageSynchronizationStatus> packageSynchronizationStatuses = new ArrayList<>();

    @Getter
    @Setter
    private boolean pending = true;

    @Getter
    private final RepositorySynchronizationStatus repositorySynchronizationStatus;

    @Getter
    private final Mirror<?> mirror;

    public MirrorSynchronizationStatus(
            Mirror<?> mirror, RepositorySynchronizationStatus repositorySynchronizationStatus) {
        this.mirror = mirror;
        this.repositorySynchronizationStatus = repositorySynchronizationStatus;
    }

    @JsonGetter("status")
    public String getStatusForJson() {
        return this.status.getStatus();
    }

    @Override
    public Set<SynchronizationStatus> getChildren() {
        return packageSynchronizationStatuses.stream()
                .map(s -> (SynchronizationStatus) s)
                .collect(Collectors.toSet());
    }
}

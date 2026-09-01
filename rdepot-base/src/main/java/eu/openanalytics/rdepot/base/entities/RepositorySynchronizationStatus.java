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

import eu.openanalytics.rdepot.base.api.v2.dtos.RepositoryProjection;
import eu.openanalytics.rdepot.base.mirroring.pojos.SynchronizationStatus;
import eu.openanalytics.rdepot.base.technology.Technology;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Represents status of synchronization (publication) for given repository.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class RepositorySynchronizationStatus extends SynchronizationStatus {

    private RepositoryProjection repository;
    private Date timestamp;
    private final List<MirrorSynchronizationStatus> mirrors = new ArrayList<>();
    private boolean pending;
    private Technology technology;

    public void setRepository(Repository repository) {
        this.repository = new RepositoryProjection(repository);
    }

    @Override
    public Set<SynchronizationStatus> getChildren() {
        return mirrors.stream().map(s -> (SynchronizationStatus) s).collect(Collectors.toSet());
    }

    public List<PackageSynchronizationStatus> getPackages() {
        final List<PackageSynchronizationStatus> statuses = new ArrayList<>();

        for (MirrorSynchronizationStatus mirror : mirrors) {
            statuses.addAll(mirror.getPackageSynchronizationStatuses());
        }

        return statuses;
    }
}

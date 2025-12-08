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
package eu.openanalytics.rdepot.base.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import eu.openanalytics.rdepot.base.mirroring.Mirror;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredPackage;
import eu.openanalytics.rdepot.base.mirroring.pojos.SynchronizationStatus;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PackageSynchronizationStatus {

    private String name;
    private String version;
    private SynchronizationStatus status = SynchronizationStatus.PENDING;
    private String error;
    private Mirror<?> mirror;

    @JsonGetter("status")
    public String getStatusForJson() {
        return this.status.getStatus();
    }

    public PackageSynchronizationStatus(MirroredPackage packageBag, Mirror<?> mirror) {
        this.name = packageBag.getName();
        this.version = packageBag.getVersion();
        this.mirror = mirror;
    }

    public boolean equals(String packageName, String packageVersion, Mirror<?> mirror) {
        return this.getName().equals(packageName)
                && Objects.equals(this.getVersion(), packageVersion)
                && this.getMirror().getName().equals(mirror.getName())
                && this.getMirror().getType().equals(mirror.getType())
                && this.getMirror().getUri().equals(mirror.getUri());
    }
}

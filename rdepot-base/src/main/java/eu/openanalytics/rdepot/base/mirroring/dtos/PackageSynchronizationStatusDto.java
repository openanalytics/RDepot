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
package eu.openanalytics.rdepot.base.mirroring.dtos;

import eu.openanalytics.rdepot.base.api.v2.dtos.MirrorProjection;
import eu.openanalytics.rdepot.base.entities.PackageSynchronizationStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PackageSynchronizationStatusDto {
    private String name;
    private String version;
    private String status;
    private String error;
    private MirrorProjection mirror;

    public PackageSynchronizationStatusDto(PackageSynchronizationStatus packageStatus) {
        this.name = packageStatus.getName();
        this.version = packageStatus.getVersion();
        this.status = packageStatus.getStatus().getStatus();
        this.error = packageStatus.getError();
        this.mirror = new MirrorProjection(packageStatus.getMirror());
    }
}

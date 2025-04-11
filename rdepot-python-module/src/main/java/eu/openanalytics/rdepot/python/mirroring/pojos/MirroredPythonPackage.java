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
package eu.openanalytics.rdepot.python.mirroring.pojos;

import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredPackage;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MirroredPythonPackage extends MirroredPackage {

    public PythonPackage toPackageEntity() {
        PythonPackage entity = new PythonPackage();
        entity.setName(name);
        entity.setVersion(version);
        entity.setNormalizedName(name);
        return entity;
    }

    public String getNormalizedName() {
        PythonPackage entity = new PythonPackage();
        entity.setNormalizedName(name);
        return entity.getNormalizedName();
    }
}

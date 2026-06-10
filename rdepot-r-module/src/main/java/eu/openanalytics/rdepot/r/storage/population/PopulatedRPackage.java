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
package eu.openanalytics.rdepot.r.storage.population;

import eu.openanalytics.rdepot.base.storage.PopulatedPackage;
import eu.openanalytics.rdepot.r.entities.RPackage;
import java.io.Serial;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
public class PopulatedRPackage extends RPackage implements PopulatedPackage {

    @Serial
    private static final long serialVersionUID = 7767590207786897778L;

    @Setter
    private String populatedPath;

    private final RPackage notPopulated;

    public PopulatedRPackage(RPackage packageBag, @NonNull String populatedPath) {
        super(packageBag);
        this.populatedPath = populatedPath;
        this.notPopulated = packageBag;
    }
}

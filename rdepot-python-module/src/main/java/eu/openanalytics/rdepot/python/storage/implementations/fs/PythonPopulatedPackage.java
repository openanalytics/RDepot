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
package eu.openanalytics.rdepot.python.storage.implementations.fs;

import eu.openanalytics.rdepot.base.storage.PopulatedPackage;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import java.io.Serial;

public class PythonPopulatedPackage extends PythonPackage implements PopulatedPackage {

    @Serial
    private static final long serialVersionUID = -7228173609547888145L;

    private final String populatedPath;

    public PythonPopulatedPackage(PythonPackage that, String populatedPath) {
        super(that);
        this.populatedPath = populatedPath;
    }

    @Override
    public String getPopulatedPath() {
        return populatedPath;
    }
}

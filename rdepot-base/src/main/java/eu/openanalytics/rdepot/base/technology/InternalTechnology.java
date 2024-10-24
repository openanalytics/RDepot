/*
 * RDepot
 *
 * Copyright (C) 2012-2024 Open Analytics NV
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
package eu.openanalytics.rdepot.base.technology;

import java.util.Objects;

/**
 * {@link Technology} which represents internal RDepot's features and entities.
 */
public class InternalTechnology implements Technology {

    public static InternalTechnology instance;

    static {
        instance = new InternalTechnology();
    }

    @Override
    public Technology getInstance() {
        return instance;
    }

    @Override
    public String getName() {
        return "RDepot";
    }

    @Override
    public String getVersion() {
        return "2.4.1";
    }

    @Override
    public Boolean isCompatible(String version) {
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getVersion());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!obj.getClass().isAssignableFrom(InternalTechnology.class)) return false;
        InternalTechnology that = (InternalTechnology) obj;
        return this.getName().equals(that.getName()) && this.getVersion().equals(that.getVersion());
    }
}

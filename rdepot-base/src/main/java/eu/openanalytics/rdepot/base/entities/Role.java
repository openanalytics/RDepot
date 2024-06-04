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
package eu.openanalytics.rdepot.base.entities;

import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.technology.InternalTechnology;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing {@link User user's} role.
 * @see <a href="https://github.com/openanalytics/RDepot/tree/master/docs/user-stories.md">https://github.com/openanalytics/RDepot/tree/master/docs/user-stories.md</a>
 * @see <a href="https://rdepot.io/latest/documentation/roles/">https://rdepot.io/latest/documentation/roles/</a>
 */
@Entity
@Getter
@Setter
@Table(name = "role", schema = "public")
public class Role extends Resource {

    /**
     * Numeric values representing roles.
     */
    public static class VALUE {
        public static final int USER = 0;
        public static final int PACKAGEMAINTAINER = 1;
        public static final int REPOSITORYMAINTAINER = 2;
        public static final int ADMIN = 3;
    }

    @Column(name = "value", unique = true, nullable = false)
    private int value;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "description", unique = true, nullable = false)
    private String description;

    public Role() {
        super(InternalTechnology.instance, ResourceType.ROLE);
    }

    public Role(int id, int value, String name, String description) {
        this();
        this.id = id;
        this.value = value;
        this.name = name;
        this.description = description;
    }

    public boolean equals(Role that) {
        return this.name.equals(that.name);
    }

    @Override
    public String toString() {
        return switch (value) {
                    case VALUE.ADMIN -> "Admin";
                    case VALUE.REPOSITORYMAINTAINER -> "Repository Maintainer";
                    case VALUE.PACKAGEMAINTAINER -> "Package Maintainer";
                    case VALUE.USER -> "Standard User";
                    default -> "Unknown";
                } + " role";
    }
}

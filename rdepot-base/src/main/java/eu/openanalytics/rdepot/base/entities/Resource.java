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
import eu.openanalytics.rdepot.base.technology.Technology;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract class containing metadata about a resource, e.g. the technology used.
 * It defines properties common for all entities.
 */
@Getter
@MappedSuperclass
@EqualsAndHashCode
public abstract class Resource {

    @Transient
    private final Technology technology;

    @Transient
    private final ResourceType resourceType;

    /**
     * Unique id of a resource.
     * It needs to be noted however, that two different resources
     * can have the same ID if they belong to different
     * {@link #getResourceType() resource types}.
     */
    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    protected int id = 0;

    @Column(name = "deleted", nullable = false)
    protected Boolean deleted = false;

    public Resource(Resource that) {
        this.technology = that.technology;
        this.id = that.id;
        this.resourceType = that.resourceType;
        this.deleted = that.deleted;
    }

    public Resource(Technology technology, ResourceType resourceType) {
        this.technology = technology;
        this.resourceType = resourceType;
    }

    public Resource(int id, Technology technology, ResourceType resourceType) {
        this(technology, resourceType);
        this.id = id;
    }

    public abstract String toString();

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Returns {@literal true} if object is <b>soft</b>-deleted.
     * This means that it physically exists in the database
     * but should remain invisible for most users.
     */
    public Boolean isDeleted() {
        return deleted;
    }
}

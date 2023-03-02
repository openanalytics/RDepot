/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.base.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.technology.Technology;

/**
 * Abstract class containing metadata about a resource, e.g. the technology used.
 * It defines properties common for all entities.
 */
@MappedSuperclass
public abstract class Resource {
	
	@Transient
	private final Technology technology;
	
	@Transient
	private final ResourceType resourceType; //IN THE DB: Resource_Type: VARCHAR(3) PKG, REP, RPM, PKM, USR, SUB
	
	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected int id = 0;
	
	@Column(name = "deleted", nullable = false)
	protected Boolean deleted = false;

	public Resource(Resource that) {
		this.technology = that.technology;
		this.id = that.id;
		this.resourceType = that.resourceType;
	}
	
	public Resource(Technology technology, ResourceType resourceType) {
		this.technology = technology;
		this.resourceType = resourceType;
	}
	
	public Resource(int id, Technology technology, ResourceType resourceType) {
		this(technology, resourceType);
		this.id = id;
	}
	
	/**
	 * {@link Technology} that the entity is part of.
	 * In case of technology-agnostic entities (like users or maintainers)
	 * {@link InternalTechnology} is returned.
	 */
	public Technology getTechnology() {
		return technology;
	}
	
	public abstract String toString();
	
	/**
	 * Unique id of a resource.
	 * It needs to be noted however, that two different resources
	 * can have the same ID if they belong to different 
	 * {@link #getResourceType() resource types}.
	 */
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Enum representing resource type.
	 */
	public ResourceType getResourceType() {
		return resourceType;
	}
	
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	/**
	 * Returns {@value true} if object is <b>soft</b>-deleted.
	 * This means that it physically exists in the database
	 * but should remain invisible for most users.
	 */
	public Boolean isDeleted() {
		return deleted;
	}

	public abstract String getDescription();
}

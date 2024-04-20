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
package eu.openanalytics.rdepot.base.api.v2.dtos;

import org.apache.commons.lang3.builder.ToStringExclude;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simplified Data Transfer Object for {@link Repository Repositories}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepositorySimpleDto implements IDto {

	private Integer id = 0;
	private Integer version = 0;
	private String publicationUri;
	private String name;
	private String serverAddress = "127.0.0.1";
	private Boolean deleted = false;
	private Boolean published = false;
	private Boolean synchronizing = false;
	private String technology;
	@ToStringExclude 
	private Repository entity;

	public RepositorySimpleDto(Repository repository) {
		this.id = repository.getId();
		this.version = repository.getVersion();
		this.publicationUri = repository.getPublicationUri();
		this.name = repository.getName();
		this.serverAddress = repository.getServerAddress();
		this.deleted = repository.isDeleted();
		this.published = repository.getPublished();
		this.synchronizing = repository.isSynchronizing();
		this.technology = repository.getTechnology().getName();
		this.entity = repository;
	}

	public Boolean isDeleted() {
		return deleted;
	}
	
	public Boolean isPublished() {
		return published;
	}
	
	public Boolean isSynchronizing() {
		return synchronizing;
	}

	@Override
	@JsonIgnore
	public Resource getEntity() {
		return this.entity;
	}
}

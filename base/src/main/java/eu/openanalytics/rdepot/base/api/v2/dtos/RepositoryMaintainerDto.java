/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
package eu.openanalytics.rdepot.base.api.v2.dtos;

import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;

public class RepositoryMaintainerDto 
	implements IDto<RepositoryMaintainer> {
	private Integer id = 0;
	private Integer userId;
	private Integer repositoryId;
	private Boolean deleted = false;
	private RepositoryMaintainer entity;
	
	public RepositoryMaintainerDto() {
		
	}
	
	public RepositoryMaintainerDto(RepositoryMaintainer repositoryMaintainer) {
		this.entity = repositoryMaintainer;
		this.id = repositoryMaintainer.getId();
		this.userId = repositoryMaintainer.getUser().getId();
		this.repositoryId = repositoryMaintainer.getRepository().getId();
		this.deleted = repositoryMaintainer.isDeleted();
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(Integer repositoryId) {
		this.repositoryId = repositoryId;
	}

	public Boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public RepositoryMaintainer getEntity() {
		return entity;
	}
	
	@Override
	public void setEntity(RepositoryMaintainer entity) {
		this.entity = entity;
	}
}

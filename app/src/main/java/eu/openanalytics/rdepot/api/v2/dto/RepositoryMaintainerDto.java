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
package eu.openanalytics.rdepot.api.v2.dto;

import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.User;

public class RepositoryMaintainerDto extends EntityDto<RepositoryMaintainer>{
	private Integer id = 0;
	private Integer userId;
	private Integer repositoryId;
	private Boolean deleted = false;
	
	public RepositoryMaintainerDto() {
		
	}
	
	public RepositoryMaintainerDto(RepositoryMaintainer repositoryMaintainer) {
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
	public RepositoryMaintainer toEntity() {
		RepositoryMaintainer repositoryMaintainer = new RepositoryMaintainer();
		
		User user = new User();
		user.setId(userId);
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		
		repositoryMaintainer.setId(id);
		repositoryMaintainer.setDeleted(deleted);
		repositoryMaintainer.setRepository(repository);
		repositoryMaintainer.setUser(user);
		
		return repositoryMaintainer;
	}
}

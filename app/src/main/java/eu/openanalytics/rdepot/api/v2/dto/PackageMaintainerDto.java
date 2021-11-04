/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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

import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.User;

public class PackageMaintainerDto extends EntityDto<PackageMaintainer> {

	private Integer id = 0;
	private Integer userId;
	private String packageName = "";
	private Integer repositoryId;
	private Boolean deleted = false;
	
	public PackageMaintainerDto(PackageMaintainer packageMaintainer) {
		super(packageMaintainer);
		this.id = packageMaintainer.getId();
		this.userId = packageMaintainer.getUser().getId();
		this.packageName = packageMaintainer.getPackage();
		this.repositoryId = packageMaintainer.getRepository().getId();
		this.deleted = packageMaintainer.isDeleted();
	}
	
	public PackageMaintainerDto() {
		
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

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Integer getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(Integer repositoryId) {
		this.repositoryId = repositoryId;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public PackageMaintainer toEntity() {
		PackageMaintainer packageMaintainer = new PackageMaintainer();
		packageMaintainer.setId(id);
		packageMaintainer.setDeleted(deleted);
		packageMaintainer.setPackage(packageName);
		
		User user = new User();
		user.setId(userId);
		packageMaintainer.setUser(user);
		
		Repository repository = new Repository();
		repository.setId(repositoryId);
		packageMaintainer.setRepository(repository);
		
		return packageMaintainer;
	}

}

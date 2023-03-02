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

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageMaintainerDto;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.technology.InternalTechnology;

/**
 * This object represents the relationship between a {@link User user} 
 * and {@link Package packages} in a specific {@link Repository repository}
 * that have certain name.
 * It allows user to have control over a particular kind of package.
 */
@Entity
@Table(name = "package_maintainer", schema = "public")
public class PackageMaintainer extends Resource
	implements IEntity<PackageMaintainerDto> {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "repository_id", nullable = false)
	private Repository<?,?> repository;
	
	@Column(name = "package", unique = false, nullable = false)
	private String packageName;
	
	@OneToMany(cascade = CascadeType.ALL, 
			orphanRemoval = true, 
			fetch = FetchType.LAZY, 
			mappedBy = "packageMaintainer")
	private List<NewsfeedEvent> relatedEvents;

	public PackageMaintainer() {
		super(InternalTechnology.instance, ResourceType.PACKAGE_MAINTAINER);	
	}
	
	public PackageMaintainer(PackageMaintainerDto dto, Repository<?,?> repository, User user) {
		this();
		this.id = dto.getId();
		this.deleted = dto.getDeleted();
		this.packageName = dto.getPackageName();
		this.user = user;
		this.repository = repository;
	}

	public PackageMaintainer(int id, User user, Repository<?,?> repository,
			String package_, boolean deleted) {
		this();
		this.id = id;
		this.user = user;
		this.repository = repository;
		this.packageName = package_;
		this.deleted = deleted;
	}

	public PackageMaintainer(PackageMaintainer that) {
		this();
		this.id = that.id;
		this.deleted = that.deleted;
		this.packageName = that.packageName;
		this.repository = that.repository;
		this.user = that.user;
	}
	
	public List<NewsfeedEvent> getRelatedEvents() {
		return relatedEvents;
	}

	public void setRelatedEvents(List<NewsfeedEvent> relatedEvents) {
		this.relatedEvents = relatedEvents;
	}
	
	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Repository<?,?> getRepository() {
		return this.repository;
	}

	public void setRepository(Repository<?,?> repository) {
		this.repository = repository;
	}
	
	public String getPackageName() {
		return this.packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	@Override
	public PackageMaintainerDto createDto() {
		return new PackageMaintainerDto(this);
	}

	@Override
	public String toString() {
		return "Package Maintainer (id: " + id + ", user: \"" + user.getLogin() + "\", package: \"" + packageName + "\", repository: \"" + repository.getName() + "\")";
	}

	@Override
	public String getDescription() {
		return toString();
	}
}

/**
 * R Depot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.r.legacy.api.v1.dtos;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import eu.openanalytics.rdepot.base.entities.User;

public class UserV1Dto {
	private int id;
	private RoleV1Dto role;
	private String name;
	private String email;
	private String login;
	private boolean active;
	
	@JsonSerialize(using = LocalDateSerializer.class)
	private LocalDate lastLoggedInOn;
	private boolean deleted;
	private List<RepositoryMaintainerProjection> repositoryMaintainers;
	private List<PackageMaintainerProjection> packageMaintainers;
	
	public UserV1Dto(User user) {
		this.id = user.getId();
		this.role = new RoleV1Dto(user.getRole()); 
		this.name = user.getName();
		this.email = user.getEmail();
		this.login = user.getLogin();
		this.active = user.isActive();
		this.lastLoggedInOn  = user.getLastLoggedInOn();
		this.deleted = user.isDeleted();
		
		if(user.getRepositoryMaintainers() != null) {		
			
			Comparator<RepositoryMaintainerProjection> sortRepositoryMaintainersByLoginThenById
			 = Comparator.comparing(RepositoryMaintainerProjection::getLogin)
			            .thenComparingInt(RepositoryMaintainerProjection::getId);
			
			
			this.repositoryMaintainers = user.getRepositoryMaintainers()
					.stream()
					.map(RepositoryMaintainerProjection::of)
					.sorted(sortRepositoryMaintainersByLoginThenById)
					.collect(Collectors.toList());
			
		} else {
			this.repositoryMaintainers = null;
		}
		
		if(user.getPackageMaintainers() != null) {
			
			Comparator<PackageMaintainerProjection> sortPackageMaintainersByLoginThenById
			 = Comparator.comparing(PackageMaintainerProjection::getLogin)
			            .thenComparingInt(PackageMaintainerProjection::getId);
			
			this.packageMaintainers =  user.getPackageMaintainers()
			.stream()
			.map(PackageMaintainerProjection::of)
			.sorted(sortPackageMaintainersByLoginThenById)
			.collect(Collectors.toList());
			
		} else {
			this.packageMaintainers = null;
		}
	}
	
	static UserV1Dto of(User user) {
		return new UserV1Dto(user);
	}
	
	public int getId() {
		return id;
	}

	public RoleV1Dto getRole() {
		return role;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getLogin() {
		return login;
	}

	public boolean isActive() {
		return active;
	}

	public LocalDate getLastLoggedInOn() {
		return lastLoggedInOn;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public List<RepositoryMaintainerProjection> getRepositoryMaintainers() {
		return repositoryMaintainers;
	}

	public List<PackageMaintainerProjection> getPackageMaintainers() {
		return packageMaintainers;
	}
}

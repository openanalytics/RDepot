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
package eu.openanalytics.rdepot.r.legacy.api.v1.dtos;

import eu.openanalytics.rdepot.base.entities.Role;

public class RoleV1Dto {
	private int id;
	private int value;
	private String name;
	private String description;
//	private Set<UserProjection> users;
	
	public RoleV1Dto(Role role) {
		this.id = role.getId();
		this.value = role.getValue();
		this.name = role.getName();
		this.description = role.getDescription();
//		this.users = role.getUsers()
//				.stream()
//				.map(UserProjection::of)
//				.collect(Collectors.toSet());	
	}
	
	public static RoleV1Dto of(Role role) {
		return new RoleV1Dto(role);
	}
	
	public int getId() {
		return id;
	}
	public int getValue() {
		return value;
	}
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}

//	public Set<UserProjection> getUsers() {
//		return users;
//	}
	
}

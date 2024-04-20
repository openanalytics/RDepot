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

import eu.openanalytics.rdepot.base.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object for {@link Role Roles}
 */
@Data
@AllArgsConstructor
public class RoleDto implements IDto {

	private Integer id;
	private Integer value;
	private String name;
	private String description;
	@ToStringExclude 
	private Role entity;
	
	public RoleDto(Role role) {
		this.entity = role;
		id = role.getId();
		value = role.getValue();
		name = role.getName();
		description = role.getDescription();
	}

	@Override
	public Role getEntity() {
		return entity;
	}
}
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
package eu.openanalytics.rdepot.base.api.v2.dtos;

import eu.openanalytics.rdepot.base.entities.Role;

public class RoleDto implements IDto<Role> {

	private Integer id;
	private Integer value;
	private String name;
	private String description;
	private Role entity;
	
	public RoleDto(Role role) {
		this.entity = role;
		id = role.getId();
		value = role.getValue();
		name = role.getName();
		description = role.getDescription();
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

//	@Override
//	public Role toEntity() {
//		Role role = new Role();
//		role.setId(id);
//		role.setDescription(description);
//		role.setName(name);
//		role.setValue(value);
//		
//		return role;
//	}

	@Override
	public Role getEntity() {
		return entity;
	}

	@Override
	public void setEntity(Role entity) {
		this.entity = entity;
	}
}
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
package eu.openanalytics.rdepot.base.api.v2.dtos;

import eu.openanalytics.rdepot.base.entities.User;

public class UserDto implements IDto<User> {

	private Integer id;
	private String name;
	private String email;
	private String login;
	private Boolean active;
	private String lastLoggedInOn;
	private String createdOn;
	private Boolean deleted;
	private Integer roleId;
	private User entity;
	
	public UserDto() {
	}
	
	public UserDto(User user) {
		this.entity = user;		
		this.id = user.getId();
		this.name = user.getName();
		this.email = user.getEmail();
		this.login = user.getLogin();
		this.active = user.isActive();
		this.lastLoggedInOn = user.getLastLoggedInOn() != null 
				? user.getLastLoggedInOn().toString()
				: "";
		this.createdOn = user.getCreatedOn().toString();
		this.roleId = user.getRole().getId();
		this.deleted = user.isDeleted();
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getLastLoggedInOn() {
		return lastLoggedInOn;
	}

	public void setLastLoggedInOn(String lastLoggedInOn) {
		this.lastLoggedInOn = lastLoggedInOn;
	}
	
	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public Boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

//	@Override
//	public User toEntity() {
//		User user = new User();
//		user.setId(this.id);
//		user.setName(this.name);
//		user.setEmail(this.email);
//		user.setLogin(this.login);
//		user.setLastLoggedInOn(Date.from(
//				LocalDateTime.parse(this.lastLoggedInOn)
//				.atZone(ZoneId.systemDefault())
//				.toInstant()));
//		user.setActive(this.active);
//		user.setDeleted(this.deleted);
//		
//		Role role = new Role();
//		role.setId(this.roleId);
//		user.setRole(role);
//		return user;
//	}
	
	@Override
	public User getEntity() {
		return entity;
	}

	@Override
	public void setEntity(User entity) {
		this.entity = entity;
	}
}
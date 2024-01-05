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
package eu.openanalytics.rdepot.base.entities;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import eu.openanalytics.rdepot.base.api.v2.dtos.UserDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.UserDtoShort;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.technology.InternalTechnology;

@Entity
@Table(name = "user", schema = "public", uniqueConstraints = {
		@UniqueConstraint(columnNames = "login"),
		@UniqueConstraint(columnNames = "email") })
public class User extends Resource implements IEntity<UserDto> {

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "role_id", nullable = false)
	private Role role;
	
	@Column(name = "name", nullable = false)
	private String name;
	
	@Column(name = "email", unique = true, nullable = false)
	private String email;
	
	@Column(name = "login", unique = true, nullable = false)
	private String login;
	
	@Column(name = "active", nullable = false)
	private boolean active;
	
    @Column(name="last_logged_in_on")    
	private LocalDate lastLoggedInOn;
	
    @Column(name="created_on")
	private LocalDate createdOn;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	private Set<RepositoryMaintainer> repositoryMaintainers = new HashSet<RepositoryMaintainer>(0);
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	private Set<PackageMaintainer> packageMaintainers = new HashSet<PackageMaintainer>(0);
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	private Set<Package<?,?>> packages = new HashSet<Package<?,?>>(0);
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	private Set<Submission> submissions = new HashSet<Submission>(0);
	
	public User() {
		super(InternalTechnology.instance, ResourceType.USER);
	}
	
	public User(UserDto userDto, Role role) {
		this();
		this.id = userDto.getId();
		this.name = userDto.getName();
		this.email = userDto.getEmail();
		this.login = userDto.getLogin();
		this.active = userDto.isActive();
		this.deleted = userDto.isDeleted();
		this.lastLoggedInOn = 
			LocalDate.parse(userDto.getLastLoggedInOn());
		this.role = role;
		this.createdOn = LocalDate.parse(userDto.getCreatedOn());
	}

	public User(int id, Role role, String name, String email, String login, boolean active, boolean deleted) {
		this();
		this.id = id;
		this.role = role;
		this.name = name;
		this.email = email;
		this.login = login;
		this.active = active;
		this.deleted = deleted;
	}
	
	public User(int id, Role role, String name, String email, String login, 
			boolean active, boolean deleted, LocalDate lastLoggedInOn,
			LocalDate createdOn) {
		this();
		this.id = id;
		this.role = role;
		this.name = name;
		this.email = email;
		this.login = login;
		this.active = active;
		this.lastLoggedInOn = lastLoggedInOn;
		this.deleted = deleted;
		this.createdOn = createdOn;
	}

	public User(int id, Role role, String name, String email, String login,
			boolean active, boolean deleted, Set<RepositoryMaintainer> repositoryMaintainers,
			Set<Submission> submissions, Set<PackageMaintainer> packageMaintainers,
			Set<Package<?,?>> packages) {
		this();
		this.id = id;
		this.role = role;
		this.name = name;
		this.email = email;
		this.login = login;
		this.active = active;
		this.deleted = deleted;
		this.repositoryMaintainers = repositoryMaintainers;
		this.submissions = submissions;
		this.packageMaintainers = packageMaintainers;
		this.packages = packages;	
	}
	
	public User(int id, Role role, String name, String email, String login,
			boolean active, boolean deleted, LocalDate lastLoggedInOn, Set<RepositoryMaintainer> repositoryMaintainers,
			Set<Submission> submissions, Set<PackageMaintainer> packageMaintainers, 
			Set<Package<?,?>> packages) {
		this();
		this.id = id;
		this.role = role;
		this.name = name;
		this.email = email;
		this.login = login;
		this.active = active;
		this.deleted = deleted;
		this.repositoryMaintainers = repositoryMaintainers;
		this.submissions = submissions;
		this.packageMaintainers = packageMaintainers;
		this.packages = packages;
		this.lastLoggedInOn = lastLoggedInOn;
	}
	
	public User(User that) {
		this();
		this.id = that.id;
		this.role = that.role;
		this.name = that.name;
		this.email = that.email;
		this.login = that.login;
		this.active = that.active;
		this.deleted = that.deleted;
		this.repositoryMaintainers = that.repositoryMaintainers;
		this.submissions = that.submissions;
		this.packageMaintainers = that.packageMaintainers;
		this.packages = that.packages;
		this.lastLoggedInOn = that.lastLoggedInOn;
		this.createdOn = that.createdOn;
	}
	
	public Role getRole() {
		return this.role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public boolean isActive() {
		return this.active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Set<RepositoryMaintainer> getRepositoryMaintainers() {
		return this.repositoryMaintainers;
	}
	
	public void setRepositoryMaintainers(Set<RepositoryMaintainer> repositoryMaintainers) {
		this.repositoryMaintainers = repositoryMaintainers;
	}
	
	public Set<Submission> getSubmissions() {
		return this.submissions;
	}

	public void setSubmissions(Set<Submission> submissions) {
		this.submissions = submissions;
	}

	public Set<PackageMaintainer> getPackageMaintainers() {
		return this.packageMaintainers;
	}

	public void setPackageMaintainers(Set<PackageMaintainer> packageMaintainers) {
		this.packageMaintainers = packageMaintainers;
	}
	
	public Set<Package<?,?>> getPackages() {
		return this.packages;
	}

	public void setPackages(Set<Package<?,?>> packages) {
		this.packages = packages;
	}
	
	public LocalDate getLastLoggedInOn() {
		return lastLoggedInOn;
	}

	public void setLastLoggedInOn(LocalDate lastLoggedInOn) {
		this.lastLoggedInOn = lastLoggedInOn;
	}
	
	public LocalDate getCreatedOn() {
		return createdOn;
	}
	
	public void setCreatedOn(LocalDate createdOn) {
		this.createdOn = createdOn;
	}
	
	@Override
	public UserDto createDto() {
		return new UserDto(this);
	}
	
	public UserDtoShort createDtoShort() {
		return new UserDtoShort(this);
	}

	@Override
	public String toString() {
		return "User (id: " + id + ", login: \"" + login + "\", email: \"" 
				+ email + "\", role: \"" + role.getName() + "\")";
	}

	@Override
	public String getDescription() {
		return toString();
	}
	
	public void addPackage(Package<?,?> packageBag) {
		this.packages.add(packageBag);
		packageBag.setUser(this);		
	}
}
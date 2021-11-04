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
package eu.openanalytics.rdepot.model;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "role", schema = "public")
public class Role implements java.io.Serializable
{
	
	public static class VALUE {
		public static final int USER = 0;
		public static final int PACKAGEMAINTAINER = 1;
		public static final int REPOSITORYMAINTAINER = 2;
		public static final int ADMIN = 3;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -1071848133594711525L;
	private int id;
	private int value;
	private String name;
	private String description;
	private Set<User> users = new HashSet<User>(0);

	public Role()
	{
	}

	public Role(int id, int value, String name, String description)
	{
		this.id = id;
		this.value = value;
		this.name = name;
		this.description = description;
	}
	
	public Role(int id, int value, String name, String description, Set<User> users)
	{
		this.id = id;
		this.value = value;
		this.name = name;
		this.description = description;
		this.users = users;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false)
	public int getId()
	{
		return this.id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	@Column(name = "value", unique = true, nullable = false)
	public int getValue()
	{
		return this.value;
	}

	public void setValue(int value)
	{
		this.value = value;
	}

	@Column(name = "name", unique = true, nullable = false)
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Column(name = "description", unique = true, nullable = false)
	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "role")
	public Set<User> getUsers()
	{
		return this.users;
	}

	public void setUsers(Set<User> users)
	{
		this.users = users;
	}
	
	public boolean equals(Role that)
	{
		return this.name.equals(that.name);
	}

}

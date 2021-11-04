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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "api_token", schema = "public", uniqueConstraints = {
		@UniqueConstraint(columnNames = "token"),
		@UniqueConstraint(columnNames = "user_login") })
public class ApiToken implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5652721002200233501L;
	private int id;
	private String token;
	private String userLogin;
	
	public ApiToken() {
		
	}
	
	public ApiToken(String userLogin, String token) {
		this.userLogin = userLogin;
		this.token = token;
	}
	
	public ApiToken(int id, String userLogin, String token) {
		this.id = id;
		this.userLogin = userLogin;
		this.token = token;
	}
	
	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public int getId()
	{
		return this.id;
	}

	public void setId(int id)
	{
		this.id = id;
	}
	
	@Column(name = "token", unique = true, nullable = false)
	public String getToken()
	{
		return this.token;
	}

	public void setToken(String token)
	{
		this.token = token;
	}

	@Column(name = "user_login", unique = true, nullable = false)
	public String getUserLogin()
	{
		return this.userLogin;
	}
	
	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}
}

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

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * API token used in API v1.
 * @deprecated should be removed together with the old API.
 */
@Deprecated
@Entity
@Table(name = "api_token", schema = "public", uniqueConstraints = {
		@UniqueConstraint(columnNames = "token"),
		@UniqueConstraint(columnNames = "user_login") })
public class ApiToken implements Serializable {
	
	private static final long serialVersionUID = -608853603996019561L;

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	@Column(name = "token", unique = true, nullable = false)
	private String token;
	
	@Column(name = "user_login", unique = true, nullable = false)
	private String userLogin;
	
	public ApiToken() {}
	
	public ApiToken(String userLogin, String token) {
		this.userLogin = userLogin;
		this.token = token;
	}
	
	public ApiToken(int id, String userLogin, String token) {
		this.id = id;
		this.userLogin = userLogin;
		this.token = token;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getToken() {
		return this.token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUserLogin() {
		return this.userLogin;
	}
	
	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, token, userLogin);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ApiToken)) {
			return false;
		}
		ApiToken other = (ApiToken) obj;
		return id == other.id && Objects.equals(token, other.token) && Objects.equals(userLogin, other.userLogin);
	}
}

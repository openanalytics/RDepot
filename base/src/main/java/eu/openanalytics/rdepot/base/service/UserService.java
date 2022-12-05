/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
package eu.openanalytics.rdepot.base.service;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.JWT;

import eu.openanalytics.rdepot.base.daos.ApiTokenDao;
import eu.openanalytics.rdepot.base.daos.UserDao;
import eu.openanalytics.rdepot.base.entities.ApiToken;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.exception.AdminNotFound;

@org.springframework.stereotype.Service
public class UserService extends Service<User> {
	
	@Value("${api_token.secret}")
	private String SECRET;
	
	private final UserDao dao;
	private final ApiTokenDao apiTokenDao;		
	
	private final RoleService roleService;
	
	public UserService(UserDao dao, ApiTokenDao apiTokenDao, RoleService roleService) {
		super(dao);
		this.dao = dao;
		this.apiTokenDao = apiTokenDao;
		this.roleService = roleService;
	}
	
	public List<User> findByRole(Role role) {
		return dao.findByRoleAndDeletedAndActive(role, false, true); //TODO: Should "active" be prerequisite?
	}
	
	public Optional<User> findByLogin(String login) {
		return dao.findByLogin(login);
	}

	public Optional<User> findByEmail(String email) {
		return dao.findByEmail(email);
	}
	
	public boolean isAdmin(User user) {		
		return user.getRole().getValue() == Role.VALUE.ADMIN;
	}
	
	@Transactional(readOnly = false)
	public String generateToken(String login) {
		ApiToken apiToken = apiTokenDao.findByUserLogin(login);
		
		if(apiToken != null) {
			return apiToken.getToken();
		} else {
			String token = JWT.create()
					.withSubject(login)
					.sign(HMAC512(SECRET.getBytes()));
			apiToken = new ApiToken(login, token);
			apiTokenDao.save(apiToken);
			return token;
		}
	}
	
	public User findFirstAdmin() throws AdminNotFound {
		Role role = roleService.findByValue(Role.VALUE.ADMIN).get();
		if(role == null)
			throw new AdminNotFound();
		List<User> admins = findByRole(role);
		if(admins.size() < 1)
			throw new AdminNotFound();
		else
			return admins.get(0);
	}

}

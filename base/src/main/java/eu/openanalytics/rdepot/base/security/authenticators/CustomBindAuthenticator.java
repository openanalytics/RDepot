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
package eu.openanalytics.rdepot.base.security.authenticators;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;

import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.security.exceptions.AuthException;
import eu.openanalytics.rdepot.base.security.exceptions.RoleNotFoundException;
import eu.openanalytics.rdepot.base.security.exceptions.UserInactiveException;
import eu.openanalytics.rdepot.base.security.exceptions.UserSoftDeletedException;
import eu.openanalytics.rdepot.base.service.RoleService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.CreateEntityException;

public abstract class CustomBindAuthenticator {
	private final Environment environment;
	private final UserService userService;
	private final RoleService roleService;
	private final SecurityMediator securityMediator;
	private final String backendName;
	
	private final Logger logger = LoggerFactory.getLogger(CustomBindAuthenticator.class);
	
	public CustomBindAuthenticator(Environment environment, 
			UserService userService, RoleService roleService,
			SecurityMediator securityMediator, String backendName) {
		super();
		this.environment = environment;
		this.userService = userService;
		this.roleService = roleService;
		this.securityMediator = securityMediator;
		this.backendName = backendName;
	}
	
	private String login, email, name;
	
	protected Collection<? extends GrantedAuthority> authenticate(String username, String useremail, String fullname) 
			throws AuthException {
		this.login = username;	
		this.email = useremail;
		this.name = fullname;
		
		List<String> defaultAdmins = new ArrayList<>();
		
		for (int i=0;;i++) {
			String admin = environment.getProperty(String.format("app." + backendName + ".default.admins[%d]", i));
			if (admin == null) break;
			else defaultAdmins.add(admin);
		}
		
		if (defaultAdmins.isEmpty()) {
			defaultAdmins.add("admin");
		}
		if (email == null) {
			email = login + "@localhost";
		}
		
		User user;
		try {
			Optional<User> tmp;
			if((tmp = userService.findByLogin(username)).isPresent()) {
				user = tmp.get();
			} else if((tmp = userService.findByEmail(email)).isPresent()) {
				user = tmp.get();
			} else {
				user = createNewUser(login, name, email, defaultAdmins.contains(login)); 
			}
		} catch (CreateEntityException e) {
			logger.error(e.getMessage(), e);
			throw new AuthException(e.getMessageCode());
		}
		verifyAndUpdateUser(user, login, name, email, defaultAdmins.contains(login));
		
		user.setLastLoggedInOn(LocalDate.now());
		
		return securityMediator.getGrantedAuthorities(login);
	}
	
	private void verifyAndUpdateUser(User user, String login, String name, String email, boolean isAdmin) throws AuthException {
		if(user.isDeleted()) {
			throw new UserSoftDeletedException(user);
		}
		if(!user.isActive()) {
			throw new UserInactiveException(user);
		}
		if(!user.getName().equals(name)) {
			user.setName(name);
		}
		if(!user.getLogin().equals(login)) {
			user.setLogin(login);
		}
		if(!user.getEmail().equals(email)) {
			user.setEmail(email);
		}
		if(isAdmin && user.getRole().getValue() != Role.VALUE.ADMIN) {
			user.setRole(roleService.findByValue(Role.VALUE.ADMIN)
					.orElseThrow(() -> new RoleNotFoundException()));
		}
	}
	
	private User createNewUser(String login, String name, String email, boolean isAdmin) 
			throws RoleNotFoundException, CreateEntityException {
		final User user = new User();
		user.setLogin(login);
		user.setName(name);
		user.setEmail(email);
		user.setActive(true);
		user.setDeleted(false);
		user.setCreatedOn(LocalDate.now());
		
		if(isAdmin) {
			user.setRole(roleService.findByValue(Role.VALUE.ADMIN)
					.orElseThrow(() -> new RoleNotFoundException()));
		} else {
			user.setRole(roleService.findByValue(Role.VALUE.USER)
					.orElseThrow(() -> new RoleNotFoundException()));
		}
		
		return userService.create(user);
	}
}

/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
package eu.openanalytics.rdepot.authenticator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Resource;

import org.keycloak.representations.IDToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import eu.openanalytics.rdepot.exception.AuthenticationDeletedUserException;
import eu.openanalytics.rdepot.exception.AuthException;
import eu.openanalytics.rdepot.exception.AuthenticationInactiveUserException;
import eu.openanalytics.rdepot.exception.AuthenticationUserEditionException;
import eu.openanalytics.rdepot.exception.UserCreateException;
import eu.openanalytics.rdepot.exception.UserEditException;
import eu.openanalytics.rdepot.exception.UserNotFound;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.RoleService;
import eu.openanalytics.rdepot.service.UserService;

@ComponentScan("eu.openanalytics.rdepot")
@Service
@ConditionalOnProperty(value = "app.authentication", havingValue = "keycloak")
public class KeycloakCustomBindAuthenticator {
	
	@Resource
	private Environment environment;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService;
	
	private String login, email, name;
	
	public Collection<? extends GrantedAuthority> authenticate(String username, IDToken idToken) throws AuthException {
		this.login = username;
		this.email = idToken.getEmail();
		this.name = idToken.getGivenName() + " " + idToken.getFamilyName();
		
		List<String> defaultAdmins = new ArrayList<>();
		
		for (int i=0;;i++) {
			String admin = environment.getProperty(String.format("app.keycloak.default.admins[%d]", i));
			if (admin == null) break;
			else defaultAdmins.add(admin);
		}
		
		if (defaultAdmins.isEmpty())
			defaultAdmins.add("admin");
		
		Role adminRole = roleService.getAdminRole();
		
		if (email == null) 
		{
			email = login + "@localhost";
		}
		
		User user = userService.findByLoginEvenDeleted(login);
		
		if (user == null)
		{
			user = userService.findByEmailEvenDeleted(email);
			if (user == null)
			{
				user = new User();
				user.setLogin(login);
				if (defaultAdmins.contains(login))
					user.setRole(adminRole);
				else
					user.setRole(roleService.getUserRole());
				user.setName(name);
				user.setEmail(email);
				user.setActive(true);
				
				try {
					userService.create(user);
				} catch (UserCreateException e) {
//					throw new BadCredentialsException(messages.getMessage(e.getMessage(), e.getMessage()));
				}
			}
			else if(!user.isActive())
			{
				throw new AuthenticationInactiveUserException();
			}
			else if(user.isDeleted())
			{
				throw new AuthenticationDeletedUserException();
			}
			else
			{
				user.setLogin(login);
				if(!Objects.equals(name, user.getName()))
					user.setName(name);		
			}		
		}	
		else if(!user.isActive())
		{
			throw new AuthenticationInactiveUserException();
		}
		else if(user.isDeleted())
		{
			throw new AuthenticationDeletedUserException();
		}
		else
		{
			if(!Objects.equals(name, user.getName()))
				user.setName(name);
			if(!Objects.equals(email, user.getEmail()))
				user.setEmail(email);
			if (defaultAdmins.contains(login) && !user.getRole().equals(adminRole))
				user.setRole(adminRole);
		}
		user.setLastLoggedInOn(new Date());
		try 
		{
			//userService.update(user, null);
			userService.evaluateAndUpdate(user, null);
		} 
		catch (UserEditException | UserNotFound e) 
		{
			throw new AuthenticationUserEditionException();
//			throw new BadCredentialsException(messages.getMessage(e.getMessage(), e.getMessage()));
		}
		return userService.getGrantedAuthorities(login);
	}
}

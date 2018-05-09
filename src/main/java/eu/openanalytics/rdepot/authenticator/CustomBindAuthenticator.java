/**
 * R Depot
 *
 * Copyright (C) 2012-2018 Open Analytics NV
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.BindAuthenticator;

import eu.openanalytics.rdepot.config.SecurityConfig;
import eu.openanalytics.rdepot.exception.UserEditException;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.EventService;
import eu.openanalytics.rdepot.service.RoleService;
import eu.openanalytics.rdepot.service.UserEventService;
import eu.openanalytics.rdepot.service.UserService;

public class CustomBindAuthenticator extends BindAuthenticator
{
	
	@Resource
	private UserService userService;
	
	@Resource
	private RoleService roleService;
	
	@Resource
	private EventService eventService;
	
	@Resource
	private UserEventService userEventService;
	
	@Resource
	private Environment env;
	
	@Value("${ldap.loginfield}")
	private String ldapLoginField;
	
	@Value("${ldap.namefield}")
	private String ldapNameField;
	
	@Value("${ldap.emailfield}")
	private String ldapEmailField;
	
	@Value("${ldap.default.admins}")
	private String ldapDefaultAdmins;
	
    private static final Logger log = LoggerFactory.getLogger(CustomBindAuthenticator.class);
		
	public CustomBindAuthenticator(BaseLdapPathContextSource contextSource) 
	{
		super(contextSource);
	}
	
	@Override
	public DirContextOperations authenticate(Authentication authentication)
	{
		
		SecurityConfig.validateConfiguration(ldapLoginField, "ldap.loginfield");
		SecurityConfig.validateConfiguration(ldapNameField, "ldap.namefield");
		SecurityConfig.validateConfiguration(ldapEmailField, "ldap.emailfield");
		
		DirContextOperations userData;
		
		try
		{
			userData = super.authenticate(authentication);
		} 
		catch (Exception e)
		{
			log.info("Got exception: " + e.toString());
			throw e;
		}
		
		String login = userData.getStringAttribute(ldapLoginField);
		
		String[] nameFields = ldapNameField.trim().split("\\s*,\\s*");
		String name = "";
		for(String nameField : nameFields)
		{
			name += userData.getStringAttribute(nameField) + ", ";
		}
		
		name = name.substring(0, name.length() - 2);
		String email = userData.getStringAttribute(ldapEmailField);
		
		if (email == null) 
		{
			email = login + "@localhost";
		}
		
		User user = userService.findByLoginEvenDeleted(login);
		
		if (ldapDefaultAdmins == null || ldapDefaultAdmins.trim().isEmpty())
			ldapDefaultAdmins = "admin";
		
		List<String> defaultAdmins = Arrays.asList(ldapDefaultAdmins.trim().split("\\s*,\\s*"));
		Role adminRole = roleService.getAdminRole();
		
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
				userService.create(user);
			}
			else if(!user.isActive())
			{
				throw new BadCredentialsException(
	                    messages.getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "Account disabled"));
			}
			else if(user.isDeleted())
			{
				throw new BadCredentialsException(
	                    messages.getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "Account disabled"));
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
			throw new BadCredentialsException(
                    messages.getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "Account disabled"));
		}
		else if(user.isDeleted())
		{
			throw new BadCredentialsException(
                    messages.getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "Account disabled"));
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
			userService.update(user, null);
		} 
		catch (UserEditException e) 
		{
			throw new BadCredentialsException(messages.getMessage(e.getMessage(), e.getMessage()));
		}
		
		return userData;
	}
}

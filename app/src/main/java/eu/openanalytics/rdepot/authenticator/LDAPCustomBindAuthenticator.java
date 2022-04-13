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
package eu.openanalytics.rdepot.authenticator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.BindAuthenticator;

import eu.openanalytics.rdepot.exception.UserCreateException;
import eu.openanalytics.rdepot.exception.UserEditException;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.security.LDAPSecurityConfig;
import eu.openanalytics.rdepot.service.EventService;
import eu.openanalytics.rdepot.service.RoleService;
import eu.openanalytics.rdepot.service.UserEventService;
import eu.openanalytics.rdepot.service.UserService;

@ConditionalOnProperty(value = "app.authentication", havingValue = "ldap")
@Transactional
public class LDAPCustomBindAuthenticator extends BindAuthenticator
{
	
	@Resource
	private Environment environment;
	
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
	
	@Value("${app.ldap.loginfield}")
	private String ldapLoginField;
	
	@Value("${app.ldap.emailfield}")
	private String ldapEmailField;
	
    private static final Logger log = LoggerFactory.getLogger(LDAPCustomBindAuthenticator.class);
		
	public LDAPCustomBindAuthenticator(BaseLdapPathContextSource contextSource) 
	{
		super(contextSource);
	}
	
	@Override
	public DirContextOperations authenticate(Authentication authentication)
	{
		
		List<String> namefields = new ArrayList<>();
		for (int i=0;;i++) {
			String namefield = environment.getProperty(String.format("app.ldap.namefield[%d]", i));
			if (namefield == null) break;
			else namefields.add(namefield)		;
		}
		
		LDAPSecurityConfig.validateConfiguration(ldapLoginField, "ldap.loginfield");
		LDAPSecurityConfig.validateConfiguration(namefields, "ldap.namefield");
		LDAPSecurityConfig.validateConfiguration(ldapEmailField, "ldap.emailfield");
		
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
		String name = "";
		
		for(String namefield : namefields)
			name += userData.getStringAttribute(namefield) + " ";
		
		name = name.substring(0,  name.length() - 1);
		
		String email = userData.getStringAttribute(ldapEmailField);
		
		if (email == null) 
		{
			email = login + "@localhost";
		}
		
		User user = userService.findByLoginEvenDeleted(login);
		
		List<String> defaultAdmins = new ArrayList<>();
		
		for (int i=0;;i++) {
			String admin = environment.getProperty(String.format("app.ldap.default.admins[%d]", i));
			if (admin == null) break;
			else defaultAdmins.add(admin);
		}
		
		if (defaultAdmins.isEmpty())
			defaultAdmins.add("admin");
		
		Role adminRole = roleService.getAdminRole();
		try {
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
						throw new BadCredentialsException(messages.getMessage(e.getMessage(), e.getMessage()));
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
					userService.updateLogin(user, null, login);
					if(!Objects.equals(name, user.getName()))
						userService.updateName(user, null, name);
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
					userService.updateName(user, null, name);
				if(!Objects.equals(email, user.getEmail()))
					userService.updateEmail(user, null, email);
				if (defaultAdmins.contains(login) && !user.getRole().equals(adminRole))
					userService.updateRole(user, null, adminRole);
			}
			
			userService.updateLastLoggedInOn(user, null, new Date());
		} catch(UserEditException e) {
			throw new BadCredentialsException(messages.getMessage(e.getMessage(), e.getMessage()));
		}
		
		return userData;
	}
}

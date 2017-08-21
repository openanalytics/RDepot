/**
 * RDepot
 *
 * Copyright (C) 2012-2017 Open Analytics NV
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

import java.util.Date;
import java.util.Objects;

import javax.annotation.Resource;

import org.springframework.core.env.Environment;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.BindAuthenticator;

import eu.openanalytics.rdepot.exception.UserEditException;
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
	
	private static final String PROPERTY_NAME_LDAP_LOGINFIELD = "ldap.loginfield";
	private static final String PROPERTY_NAME_LDAP_NAMEFIELD = "ldap.namefield";
	private static final String PROPERTY_NAME_LDAP_EMAILFIELD = "ldap.emailfield";
	
	public CustomBindAuthenticator(BaseLdapPathContextSource contextSource) 
	{
		super(contextSource);
	}
	
	@Override
	public DirContextOperations authenticate(Authentication authentication)
	{
		DirContextOperations userData = super.authenticate(authentication);
		
		String login = userData.getStringAttribute(env.getRequiredProperty(PROPERTY_NAME_LDAP_LOGINFIELD));
		String name = userData.getStringAttribute(env.getRequiredProperty(PROPERTY_NAME_LDAP_NAMEFIELD));
		String email = userData.getStringAttribute(env.getRequiredProperty(PROPERTY_NAME_LDAP_EMAILFIELD));
		User user = userService.findByLoginEvenDeleted(login);
		
		if (user == null)
		{
			user = userService.findByEmailEvenDeleted(email);
			if (user == null)
			{
				user = new User();
				user.setLogin(login);
				user.setRole(roleService.findByName("user"));
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

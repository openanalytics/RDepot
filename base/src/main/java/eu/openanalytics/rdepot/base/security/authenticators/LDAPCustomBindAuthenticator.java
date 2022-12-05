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
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.backends.ldap.LDAPSecurityConfig;
import eu.openanalytics.rdepot.base.security.exceptions.RoleNotFoundException;
import eu.openanalytics.rdepot.base.service.RoleService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.CreateEntityException;

@ConditionalOnProperty(value = "app.authentication", havingValue = "ldap")
@Transactional
public class LDAPCustomBindAuthenticator extends BindAuthenticator {
	
	private final UserService userService;
	private final RoleService roleService;
	private final Environment env;
	
	@Value("${app.ldap.loginfield}")
	private String ldapLoginField;
	
	@Value("${app.ldap.emailfield}")
	private String ldapEmailField;
	
    private static final Logger logger = LoggerFactory.getLogger(LDAPCustomBindAuthenticator.class);
	
	public LDAPCustomBindAuthenticator(BaseLdapPathContextSource contextSource,
			UserService userService, RoleService roleService, Environment environment) {
		super(contextSource);
		this.userService = userService;
		this.roleService = roleService;
		this.env = environment;
	}
	
	@Override
	public DirContextOperations authenticate(Authentication authentication) {
		
		List<String> namefields = new ArrayList<>();
		for (int i=0;;i++) {
			final String namefield = env.getProperty(String.format("app.ldap.namefield[%d]", i));
			if (namefield == null) break;
			else namefields.add(namefield)		;
		}
		
		LDAPSecurityConfig.validateConfiguration(ldapLoginField, "ldap.loginfield");
		LDAPSecurityConfig.validateConfiguration(namefields, "ldap.namefield");
		LDAPSecurityConfig.validateConfiguration(ldapEmailField, "ldap.emailfield");
		
		DirContextOperations userData;
		
		try {
			userData = super.authenticate(authentication);
		} 
		catch (Exception e) {
			logger.error("Got exception: " + e.toString(), e);
			throw e;
		}
		
		final String login = userData.getStringAttribute(ldapLoginField);
		String name = "";
		
		for(String namefield : namefields)
			name += userData.getStringAttribute(namefield) + " ";
		
		name = name.substring(0,  name.length() - 1);
		
		final String email = userData.getStringAttribute(ldapEmailField) == null ? 
				login + "@localhost" : userData.getStringAttribute(ldapEmailField);
		
		List<String> defaultAdmins = new ArrayList<>();
		for (int i=0;;i++) {
			String admin = env.getProperty(String.format("app.ldap.default.admins[%d]", i));
			if (admin == null) break;
			else defaultAdmins.add(admin);
		}
		if (defaultAdmins.isEmpty())
			defaultAdmins.add("admin");
		User user;
		try {
			Optional<User> tmp;
			if((tmp = userService.findByLogin(login)).isPresent()) {
				user = tmp.get();
			} else if((tmp = userService.findByEmail(email)).isPresent()) {
				user = tmp.get();
			} else {
				user = createNewUser(login, name, email, defaultAdmins.contains(login)); 
			}
			verifyAndUpdateUser(user, login, name, email, defaultAdmins.contains(login));
			user.setLastLoggedInOn(LocalDate.now());
		} catch (RoleNotFoundException | CreateEntityException e) {
			throw new BadCredentialsException(
                    messages.getMessage(e.getMessage(), e.getMessage(), LocaleContextHolder.getLocale()));
		}
		return userData;
	}

	private void verifyAndUpdateUser(User user, String login, String name, String email, boolean isAdmin) throws RoleNotFoundException {
		if(user.isDeleted()) {
			throw new BadCredentialsException(
                    messages.getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "Account disabled"));
		}
		if(!user.isActive()) {
			throw new BadCredentialsException(
                    messages.getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "Account disabled"));
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

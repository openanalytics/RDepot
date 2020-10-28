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
package eu.openanalytics.rdepot.controller;

import org.keycloak.KeycloakPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.UserService; 

@ControllerAdvice
public class GlobalController {
	
	@Autowired
	UserService userService;
	
	@ModelAttribute("applicationVersion")
	public String getVersion() {
		return getClass().getPackage().getImplementationVersion();
	}

	@ModelAttribute("username")
	public String getUser() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username = null;
		
		if(principal instanceof UserDetails) {
			username = ((UserDetails)principal).getUsername();
		} else if (principal instanceof KeycloakPrincipal) {
			username = ((KeycloakPrincipal<?>)principal).getName();
		} else if (principal instanceof DefaultOidcUser) {
			username = ((DefaultOidcUser)principal).getName();
		}
		
		if(username == null) {
			username = SecurityContextHolder.getContext().getAuthentication().getName();
		}
		
		User user = userService.findByLogin(username);
		if(user != null)
			return user.getName();
		else
			return username;
	}
}

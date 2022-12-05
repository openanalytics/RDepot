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
package eu.openanalytics.rdepot.test.context;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.test.fixture.RoleFixture;
import eu.openanalytics.rdepot.test.fixture.RoleFixture.ROLE;


/**
 * Dummy authentication provider used for test purposes.
 * @author wiktor
 *
 */
public class TestAuthenticationProvider implements AuthenticationProvider {

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String login = authentication.getName();
		String password = authentication.toString();
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		
		String roleName = login.split("_")[0];
		Role role = RoleFixture.GET_BY_NAME(roleName);
		
		ROLE.ROLES.stream()
			.filter(r -> r.getValue() <= role.getValue())
			.forEach(r -> authorities.add(new SimpleGrantedAuthority(r.getName())));
		
		return new UsernamePasswordAuthenticationToken(login, password, authorities);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return true;
	}

}

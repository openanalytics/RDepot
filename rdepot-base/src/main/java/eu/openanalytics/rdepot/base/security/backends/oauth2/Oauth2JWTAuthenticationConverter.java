/*
 * RDepot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.base.security.backends.oauth2;

import java.util.Collection;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import eu.openanalytics.rdepot.base.security.authenticators.Oauth2CustomBindAuthenticator;
import eu.openanalytics.rdepot.base.security.exceptions.AuthException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Oauth2JWTAuthenticationConverter implements Converter<Jwt, RDepotAuthenticationToken> {
	
	private final Oauth2CustomBindAuthenticator authenticator;
	private final String loginField;
	
	@Override
	public RDepotAuthenticationToken convert(Jwt source) throws AuthException {
		String login = source.getClaimAsString(loginField);
		String email = source.getClaimAsString("email");
		String fullName = source.getClaimAsString("name");
		
		Collection<? extends GrantedAuthority> authorities = authenticator
				.authenticate(login, email, fullName);
		
		return new RDepotAuthenticationToken(login, authorities);
	}
}

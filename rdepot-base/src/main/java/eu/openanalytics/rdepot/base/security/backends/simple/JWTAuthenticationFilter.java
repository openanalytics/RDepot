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
package eu.openanalytics.rdepot.base.security.backends.simple;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.rdepot.base.api.v2.dtos.CredentialsDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final ApiTokenProperties apiTokenProperties;
	private final AuthenticationManager authenticationManager;
	
	private static final List<User> users = new ArrayList<>();
	
	public JWTAuthenticationFilter(Environment environment, AuthenticationManager authenticationManager, ApiTokenProperties apiTokenProperties) {
		this.authenticationManager = authenticationManager;
		this.apiTokenProperties = apiTokenProperties;
		for(int i = 0;;i++) {
			String login = environment.getProperty(String.format("app.simple.users[%d].login", i));
			if(login == null) break;
			else {				
				String email = environment.getProperty(String.format("app.simple.users[%d].email", i));
				String name = environment.getProperty(String.format("app.simple.users[%d].name", i));			
				
				users.add(new User(login, email, name));
			}
		}
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		try {
        	CredentialsDto creds = new ObjectMapper()
                    .readValue(request.getInputStream(), CredentialsDto.class);
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
            		creds.getLogin(), creds.getPassword()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException {
		String login = authResult.getPrincipal().toString();
		
		Optional<User> userOptional = users.stream()
                .filter(u -> u.match(login))
                .findFirst();

		if (userOptional.isEmpty()) {
			throw new BadCredentialsException("Authentication failed for " + login);
		}
		
		String[] roles = new String[authResult.getAuthorities().size()];
		int i = 0;
		
		for(GrantedAuthority a : authResult.getAuthorities()) {
			roles[i] = a.getAuthority();
			i++;
		}
					
		String token = JWT.create()
				.withIssuer(apiTokenProperties.getIssuer())
				.withSubject(login)
				.withAudience(apiTokenProperties.getAudience())
				.withIssuedAt(Date.from(Instant.now()))
				.withExpiresAt(Date.from(Instant.now().plus(apiTokenProperties.getLifetime(), ChronoUnit.MINUTES)))
				.withArrayClaim("roles", roles)
				.withClaim("name", userOptional.get().getName())
				.withClaim("email", userOptional.get().getEmail())
				.sign(HMAC512(apiTokenProperties.getSecret()));
		
		String body = 
				"{" +
				"\"status\": \"SUCCESS\"," +
				"\"code\": 200," +
				"\"messageCode\": null," +
				"\"message\": null," +
				"\"data\": {\"token\": \"" + token + "\"}" +
				"}";				
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(body);
        response.getWriter().flush();
        response.getWriter().close();
	}
	
	private static class User {
        private final String login;
        @Getter
		private final String email;
        @Getter
		private final String name;

        public User(String login, String email, String name) {
            this.login = login;
            this.email = email;
            this.name = name;
        }

        public boolean match(String login) {
            return this.login.equals(login);
        }

	}
}

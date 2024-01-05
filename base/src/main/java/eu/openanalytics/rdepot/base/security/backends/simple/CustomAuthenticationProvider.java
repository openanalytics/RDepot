/**
 * R Depot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.base.security.backends.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import eu.openanalytics.rdepot.base.security.authenticators.SimpleCustomBindAuthenticator;

public class CustomAuthenticationProvider implements AuthenticationProvider {

	private static List<User> users = new ArrayList<>();
		
	private SimpleCustomBindAuthenticator authenticator;
	
	public CustomAuthenticationProvider(Environment environment, SimpleCustomBindAuthenticator authenticator) {
		this .authenticator = authenticator;
		for(int i = 0;;i++) {
			String login = environment.getProperty(String.format("app.simple.users[%d].login", i));
			if(login == null) break;
			else {
				String password = environment.getProperty(String.format("app.simple.users[%d].password", i));
				String email = environment.getProperty(String.format("app.simple.users[%d].email", i));
				String name = environment.getProperty(String.format("app.simple.users[%d].name", i));			
				
				users.add(new User(login, password, email, name));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String login = authentication.getName();
        Object credentials = authentication.getCredentials();
        if (!(credentials instanceof String)) {
            return null;
        }
        String password = credentials.toString();

        Optional<User> userOptional = users.stream()
                                           .filter(u -> u.match(login, password))
                                           .findFirst();

        if (!userOptional.isPresent()) {
            throw new BadCredentialsException("Authentication failed for " + login);
        }

        List<GrantedAuthority> grantedAuthorities = (List<GrantedAuthority>) authenticator.authenticate(login, userOptional.get().getEmail(),
        																	userOptional.get().getName());
        Authentication auth = new
                UsernamePasswordAuthenticationToken(login, password, grantedAuthorities);
        return auth;
	}

	@Override
	public boolean supports(Class<?> authentication) {		
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

	private static class User {
        private String login;
        private String password;
        private String email;
        private String name;

        public User(String login, String password, String email, String name) {
            this.login = login;
            this.password = password;
            this.email = email;
            this.name = name;
        }

        public boolean match(String login, String password) {
            return this.login.equals(login) && this.password.equals(password);
        }      
        
        public String getEmail() {
        	return this.email;
        }
        
        public String getName() {
        	return this.name;
        }
    }
}

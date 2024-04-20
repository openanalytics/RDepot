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
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import eu.openanalytics.rdepot.base.security.authenticators.SimpleCustomBindAuthenticator;
import eu.openanalytics.rdepot.base.security.exceptions.AuthException;
import eu.openanalytics.rdepot.base.security.exceptions.JWTException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

public class JWTAuthorizationFilter extends OncePerRequestFilter {
	private final String TOKEN_PREFIX = "Bearer ";
	private final String HEADER_STRING = "Authorization";	
	private final ApiTokenProperties apiTokenProperties;	
	private final SimpleCustomBindAuthenticator authenticator;
	
	public JWTAuthorizationFilter(ApiTokenProperties apiTokenProperties,
                                  SimpleCustomBindAuthenticator authenticator) {
		this.apiTokenProperties = apiTokenProperties;
		this.authenticator = authenticator;
	}
	
	@Override
    protected void doFilterInternal(HttpServletRequest req,
                                    @NonNull HttpServletResponse res,
                                    @NonNull FilterChain chain) throws IOException, ServletException {
        String header = req.getHeader(HEADER_STRING);

        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(req, res);
            return;
        }

        UsernamePasswordAuthenticationToken authentication;
        try {
        	authentication = getAuthentication(req);
        } catch(AuthException e) {
        	authentication = null;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }
		
	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) throws AuthException {
        String token = request.getHeader(HEADER_STRING);
        if (token == null)
        	throw new JWTException("Null token");
    	
        token = token.replace(TOKEN_PREFIX, "");
    	        
        DecodedJWT decodedToken;
        try {
        	decodedToken = JWT.require(Algorithm.HMAC512(apiTokenProperties.getSecret().getBytes()))
        			.withAudience(apiTokenProperties.getAudience())
        			.withIssuer(apiTokenProperties.getIssuer())
                    .build()
                    .verify(token);
		} catch (JWTVerificationException e) {
			throw new JWTException(e.getMessage());
		}        

        String userLogin = decodedToken.getSubject();
        String userEmail = decodedToken.getClaim("email").asString();
        String fullName = decodedToken.getClaim("name").asString();

        if (userLogin == null)
        	throw new JWTException("Null user login");

		Collection<? extends GrantedAuthority> authorities = authenticator.authenticate(userLogin, userEmail, fullName);
		return new UsernamePasswordAuthenticationToken(userLogin, null, authorities);
    }

}

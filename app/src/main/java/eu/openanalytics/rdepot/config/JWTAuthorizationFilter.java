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
package eu.openanalytics.rdepot.config;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import eu.openanalytics.rdepot.model.ApiToken;
import eu.openanalytics.rdepot.repository.ApiTokenRepository;
import eu.openanalytics.rdepot.service.UserService;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
	private final String TOKEN_PREFIX = "Bearer ";
	private final String HEADER_STRING = "Authorization";
	private final String SECRET;
	private final String mode;
	
	private ApiTokenRepository apiTokenRepository;	
	private UserService userService;
	
	public JWTAuthorizationFilter(AuthenticationManager authenticationManager, ApiTokenRepository apiTokenRepository,
			UserService userService, String secret, String mode) {
		super(authenticationManager);
		this.apiTokenRepository = apiTokenRepository;
		this.userService = userService;
		this.SECRET = secret;
		this.mode = mode;
	}
	
	@Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
        String header = req.getHeader(HEADER_STRING);

        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(req, res);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }
	
	@SuppressWarnings("unchecked")
	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);
        if (token != null) {
        	token = token.replace(TOKEN_PREFIX, "");
        	String userLogin = "";
            // parse token
            userLogin = JWT.require(Algorithm.HMAC512(SECRET.getBytes()))
                    .build()
                    .verify(token)
                    .getSubject();
                       
            if (userLogin != null) {
            	ApiToken apiToken = apiTokenRepository.findByUserLogin(userLogin);
            	if(apiToken.getToken().equals(token)) {
            		if(mode.equals("simple")) {
            			List<GrantedAuthority> authorities = (List<GrantedAuthority>) userService.getGrantedAuthorities(userLogin);
            			return new UsernamePasswordAuthenticationToken(userLogin, null, authorities);
            		} else {	
            			Set<GrantedAuthority> authorities = (Set<GrantedAuthority>) userService.getGrantedAuthorities(userLogin);
            			return new UsernamePasswordAuthenticationToken(userLogin, null, authorities);
            		}            		
            	} else {
            		return null;
            	}
            }
            return null;
        }
        return null;
    }

}

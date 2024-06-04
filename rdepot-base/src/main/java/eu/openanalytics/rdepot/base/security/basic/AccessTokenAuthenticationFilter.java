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
package eu.openanalytics.rdepot.base.security.basic;

import eu.openanalytics.rdepot.base.security.exceptions.AuthException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.Collection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class AccessTokenAuthenticationFilter extends OncePerRequestFilter {

    private final String TOKEN_PREFIX = "Basic ";
    private final String HEADER_STRING = "Authorization";
    private final String type;
    private final AccessTokenBindAuthenticator authenticator;

    public AccessTokenAuthenticationFilter(AccessTokenBindAuthenticator authenticator, String type) {
        this.authenticator = authenticator;
        this.type = type;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HEADER_STRING);

        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication;
        try {
            authentication = getAuthentication(request);
        } catch (AuthException e) {
            authentication = null;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);

        token = token.substring(TOKEN_PREFIX.length());

        String[] decoded = new String(Base64.getDecoder().decode(token)).split(":", 2);

        if (decoded.length < 2) {
            throw new AuthException("Invalid login data");
        }

        String userLogin = decoded[0];
        if (userLogin.isEmpty()) throw new AuthException("Null user login");

        String accessToken = decoded[1];

        Collection<? extends GrantedAuthority> authorities = authenticator.authenticate(userLogin, accessToken, type);
        return new UsernamePasswordAuthenticationToken(userLogin, null, authorities);
    }
}

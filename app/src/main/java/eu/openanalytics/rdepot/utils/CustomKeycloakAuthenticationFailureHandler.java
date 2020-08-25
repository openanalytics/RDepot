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
package eu.openanalytics.rdepot.utils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationFailureHandler;
import org.springframework.security.core.AuthenticationException;


public class CustomKeycloakAuthenticationFailureHandler extends KeycloakAuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
				
		request.logout();
		response.sendRedirect("/authfailed?error=" + exception.getMessage());
		
		// Check that the response was not committed yet (this may happen when another
		// part of the Keycloak adapter sends a challenge or a redirect).
		if (!response.isCommitted()) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unable to authenticate using the Authorization header");
		} else {
			if (200 <= response.getStatus() && response.getStatus() < 300) {
				throw new RuntimeException("Success response was committed while authentication failed!", exception);
			}
		}
	}

	
}

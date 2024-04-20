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
package eu.openanalytics.rdepot.base.security.exceptions;

import org.springframework.security.core.AuthenticationException;

import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;

import java.io.Serial;

public class AuthException extends AuthenticationException {
	@Serial
	private static final long serialVersionUID = -5327755255269766824L;

	public AuthException(String userLogin, String messageCode) {
		super("User " + userLogin + ": " + StaticMessageResolver.getMessage(messageCode));
	}
	
	public AuthException(int userId, String messageCode) {
		super("User " + userId + ": " + StaticMessageResolver.getMessage(messageCode));
	}
	
	public AuthException(String messageCode) {
		super("Authentication error: " + StaticMessageResolver.getMessage(messageCode));
	}
}

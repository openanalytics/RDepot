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
package eu.openanalytics.rdepot.base.exception;

import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;

/**
 * Exception with a message translated to the language set in the context.
 */
public abstract class LocalizedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7657136302339131415L;

	private final String messageCode;
	
	public LocalizedException(String messageCode) {
		super(StaticMessageResolver.getMessage(messageCode));
		this.messageCode = messageCode;
	}
	
	public String getMessageCode() {
		return messageCode;
	}
}

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
package eu.openanalytics.rdepot.exception;

import java.util.Locale;

import org.springframework.context.MessageSource;

import eu.openanalytics.rdepot.model.Repository;

public class RepositoryException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3108861641231904667L;
	
	public RepositoryException(String messageCode, MessageSource messageSource,
			Locale locale, Repository repository) {
		super(repository.getName() + " id: " + Integer.toString(repository.getId()) + 
				messageSource.getMessage(messageCode, null, messageCode, locale));
	}
	
	public RepositoryException(String messageCode, MessageSource messageSource,
			Locale locale, int repositoryId) {
		super("Repository " + Integer.toString(repositoryId) + ": " + 
			messageSource.getMessage(messageCode, null, messageCode, locale));
	}
	
	public RepositoryException(String messageCode, MessageSource messageSource,
			Locale locale, String repositoryName) {
		super("Repository " + repositoryName + ": " + 
			messageSource.getMessage(messageCode, null, messageCode, locale));
	}
}

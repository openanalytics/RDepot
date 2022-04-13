/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
import org.springframework.http.ResponseEntity;

import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Repository;

public class UploadToRemoteServerException extends RepositoryStorageException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2831923789369445108L;
	
	public UploadToRemoteServerException(MessageSource messageSource, Locale locale,
			Repository repository) {
		super(MessageCodes.ERROR_UPLOAD_TO_REMOTE_SERVER, messageSource, locale,
				"\nRepository ID: " + Integer.toString(repository.getId()));
	}

	public UploadToRemoteServerException(MessageSource messageSource, Locale locale,
			String uploadPath, String serverAddress) {
		super(MessageCodes.ERROR_UPLOAD_TO_REMOTE_SERVER, messageSource, locale, 
				"Server address: " + serverAddress + "\nUpload path: " + uploadPath);
	}

	public UploadToRemoteServerException(MessageSource messageSource, Locale locale,
			String uploadPath, String serverAddress, ResponseEntity<String> response) {
		super(MessageCodes.ERROR_UPLOAD_TO_REMOTE_SERVER, messageSource, locale, 
				"Server address: " + serverAddress + "\nUpload path: " + uploadPath
				+ "\nResponse: " + response.getStatusCodeValue() + "; " + response.getBody());
	} 
}

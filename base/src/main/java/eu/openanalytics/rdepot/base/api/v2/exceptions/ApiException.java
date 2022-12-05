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
package eu.openanalytics.rdepot.base.api.v2.exceptions;

import java.util.Locale;
import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

public class ApiException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7207340302826282335L;
	private String messageCode;
	private HttpStatus httpStatus;
	private Optional<String> details = Optional.empty();

	public ApiException(MessageSource messageSource, 
			Locale locale, String messageCode,
			HttpStatus httpStatus) {
		super(messageSource.getMessage(
						messageCode, 
						null, 
						messageCode, 
						locale));
		
		this.messageCode = messageCode;
		this.httpStatus = httpStatus;
	}
	
	public ApiException(MessageSource messageSource, Locale locale, 
			String messageCode, HttpStatus httpStatus, String details) {
		this(messageSource, locale, messageCode, httpStatus);
		
		this.setDetails(Optional.of(details));
	}

	public String getMessageCode() {
		return messageCode;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public Optional<String> getDetails() {
		return details;
	}

	public void setDetails(Optional<String> details) {
		this.details = details;
	}
}
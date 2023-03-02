/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
package eu.openanalytics.rdepot.base.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.Status;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;

/**
 * Generates RESTful response on authorization error.
 */
public class RestAccessDeniedHandler implements AccessDeniedHandler {
	
	private MessageSource messageSource;
	private Locale locale;
	private ObjectMapper objectMapper;

	public RestAccessDeniedHandler(MessageSource messageSource, 
			Locale locale, ObjectMapper objectMapper) {
		this.messageSource = messageSource;
		this.locale = locale;
		this.objectMapper = objectMapper;
	}
	
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		
		final ResponseDto<?> responseDto = new ResponseDto<>(
				Status.ERROR, 
				HttpStatus.FORBIDDEN.value(),
				messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, 
						null, MessageCodes.ERROR_USER_NOT_AUTHORIZED, locale),
				MessageCodes.ERROR_USER_NOT_AUTHORIZED, null);
		
		final String responseBody = objectMapper.writeValueAsString(responseDto);
		
		final PrintWriter writer = response.getWriter();
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		writer.print(responseBody);
		writer.flush();
		writer.close();
	}

}
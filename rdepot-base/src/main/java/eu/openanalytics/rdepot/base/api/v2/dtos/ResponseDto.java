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
package eu.openanalytics.rdepot.base.api.v2.dtos;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

import eu.openanalytics.rdepot.base.api.v2.exceptions.ApiException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UnrecognizedQueryParameterException;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object for every RESTful response.
 * It contains a few static methods to generate different kinds of responses.
 * @param <T> type of data attached to response
 */
@Data
@AllArgsConstructor
public class ResponseDto<T> {
	
	private Status status;
	private Integer code;
	private String message;
	private String messageCode;
	private T data;
	
	public ResponseDto(Status status, Integer code, String message, String messageCode) {
		this(status, code, message, messageCode, null);
	}

	/**
	 * Generates a response with a resource attached.
	 * @param messageSource to resolve the message code
	 * @param locale to resolve the message code
	 * @param model resource DTO
	 * @param <T> resource DTO type
	 */
	public static <T> ResponseDto<T> generateSuccessBody(MessageSource messageSource, 
			Locale locale, T model) {
		String messageCode = MessageCodes.SUCCESS_REQUEST_PROCESSED;
		String message = messageSource.getMessage(messageCode, null, messageCode, locale);
		
		return new ResponseDto<>(Status.SUCCESS, HttpStatus.OK.value(), message, messageCode, model);
	}

	/**
	 * Generates a response for a newly created resource.
	 * @param messageSource to resolve the message code
	 * @param locale to resolve the message code
	 * @param model created resource DTO
	 * @param <T> created resource DTO type
	 */
	public static <T> ResponseDto<T> generateCreatedBody(MessageSource messageSource, Locale locale, T model) {
		String messageCode = MessageCodes.SUCCESS_RESOURCE_CREATED;
		String message = messageSource.getMessage(messageCode, null, messageCode, locale);
		
		return new ResponseDto<>(Status.SUCCESS, HttpStatus.CREATED.value(), message, messageCode, model);
	}

	/**
	 * Generates a standard error response for a given {@link ApiException API Exception}.
	 */
	public static ResponseDto<?> generateErrorBody(ApiException e) {
		return e.getHttpStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR) ? generateInternalServerErrorBody(e) :
				new ResponseDto<>(Status.ERROR, e.getHttpStatus().value(),
				e.getMessage(), e.getMessageCode(), e.getDetails().orElse(null));
	}

	private static ResponseDto<Map<String, String>> generateInternalServerErrorBody(ApiException e) {
		final Map<String, String> body = new HashMap<>();
		body.put("traceId", MDC.get("traceIdMDC") == null ? "" : MDC.get("traceIdMDC"));
		body.put("timestamp", Instant.now().toString());
		e.getDetails().ifPresent( d -> body.put("details", d));

		return new ResponseDto<>(Status.ERROR, e.getHttpStatus().value(),
				e.getMessage(), e.getMessageCode(), body);
	}
	
	/**
	 * Generates a standard error response for a given {@link UnrecognizedQueryParameterException API Exception}.
	 */
	public static ResponseDto<?> generateErrorBody(UnrecognizedQueryParameterException e) {
		return new ResponseDto<>(Status.ERROR, e.getHttpStatus().value(), 
				e.getMessage(), e.getMessageCode(), e.getParameters());
	}

	/**
	 * Generates an error response with a message resolved using a given message code.
	 * @param messageSource to resolve the message code
	 * @param locale to resolve the message code
	 * @param httpStatus HTTP Status
	 * @param messageCode will be attached to the response
	 *                       <b>and</b> resolved to full message (if possible)
	 * @param data additional data to attach
	 * @param <T> type of additional data to attach
	 */
	public static <T> ResponseDto<T> generateErrorBody(MessageSource messageSource, Locale locale,
			HttpStatus httpStatus, String messageCode, T data) {
		String message = messageSource.getMessage(messageCode, null, messageCode, locale);

		return new ResponseDto<>(Status.ERROR, httpStatus.value(), message, messageCode, data);
	}

	/**
	 * Generates a warning response with a message resolved using a given message code.
	 * @param messageSource to resolve the message code
	 * @param locale to resolve the message code
	 * @param httpStatus HTTP Status
	 * @param messageCode will be attached to the response
	 *                       <b>and</b> resolved to full message (if possible)
	 * @param data additional data to attach
	 * @param <T> type of additional data to attach
	 */
	public static <T> ResponseDto<T> generateWarningBody(MessageSource messageSource, Locale locale,
														 HttpStatus httpStatus, String messageCode, T data) {
		final String message = messageSource.getMessage(messageCode, null, messageCode, locale);

		return new ResponseDto<>(Status.WARNING, httpStatus.value(), message, messageCode, data);
	}

	/**
	 * Generates an error response.
	 * @param httpStatus HTTP Status
	 * @param messageCode will be attached to the response
	 *                       <b>and</b> resolved to full message (if possible)
	 * @param data additional data to attach
	 * @param <T> type of additional data to attach
	 */
	public static <T> ResponseDto<T> generateErrorBody(HttpStatus httpStatus, String message, String messageCode, T data) {
		return new ResponseDto<>(Status.ERROR, httpStatus.value(), message, messageCode, data);
	}
}
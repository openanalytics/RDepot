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
package eu.openanalytics.rdepot.api.v2.dto;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

import eu.openanalytics.rdepot.api.v2.exception.ApiException;
import eu.openanalytics.rdepot.messaging.MessageCodes;

public class ResponseDto<T> {
	
	private Status status;
	private Integer code;
	private String message;
	private String messageCode;
	private T data;
	
	public ResponseDto(Status status, Integer code, 
			String message, String messageCode, T data) {
		this.code = code;
		this.message = message;
		this.messageCode = messageCode;
		this.status = status;
		this.data = data;
	}
	
	public ResponseDto(Status status, Integer code, String message, String messageCode) {
		this(status, code, message, messageCode, null);
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setMessageCode(String messageCode) {
		this.messageCode = messageCode;
	}
	
	public String getMessageCode() {
		return messageCode;
	}

	public String getStatus() {
		return status.getStatus();
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
	
	public static <T> ResponseDto<T> generateSuccessBody(MessageSource messageSource, 
			Locale locale, T model) {
		String messageCode = MessageCodes.SUCCESS_REQUEST_PROCESSED;
		String message = messageSource.getMessage(messageCode, null, messageCode, locale);
		
		ResponseDto<T> response 
			= new ResponseDto<>(Status.SUCCESS, HttpStatus.OK.value(), message, messageCode, model);
		
		return response;
	}
	
	public static <T> ResponseDto<?> generateSuccessBody(MessageSource messageSource, Locale locale) {
		String messageCode = MessageCodes.SUCCESS_REQUEST_PROCESSED;
		String message = messageSource.getMessage(messageCode, null, messageCode, locale);
		
		ResponseDto<T> response 
			= new ResponseDto<>(Status.SUCCESS, HttpStatus.NO_CONTENT.value(), message, messageCode, null);
		
		return response;
	}
	
	public static <T> ResponseDto<T> generateCreatedBody(MessageSource messageSource, Locale locale, T model) {
		String messageCode = MessageCodes.SUCCESS_RESOURCE_CREATED;
		String message = messageSource.getMessage(messageCode, null, messageCode, locale);
		
		ResponseDto<T> response
			= new ResponseDto<>(Status.SUCCESS, HttpStatus.CREATED.value(), message, messageCode, model);
		
		return response;
	}
	
	public static ResponseDto<?> generateErrorBody(ApiException e) {
		return new ResponseDto<>(Status.ERROR, e.getHttpStatus().value(), 
				e.getMessage(), e.getMessageCode(), e.getDetails().orElse(null));
	}
	
	public static <T> ResponseDto<T> generateErrorBody(MessageSource messageSource, Locale locale,
			HttpStatus httpStatus, String messageCode, T data) {
		String message = messageSource.getMessage(messageCode, null, messageCode, locale);

		return new ResponseDto<>(Status.ERROR, httpStatus.value(), message, messageCode, data);
	}
}
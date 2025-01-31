/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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
package eu.openanalytics.rdepot.base.api.v2.controllers;

import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApiException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UnrecognizedQueryParameterException;
import eu.openanalytics.rdepot.base.config.RequestFilterMDC;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Controller responsible for handling errors thrown by controller methods.
 */
@RestControllerAdvice
@Slf4j
@AllArgsConstructor
public class ApiV2ErrorController extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;
    private static final Locale locale = LocaleContextHolder.getLocale();

    @ExceptionHandler(ApiException.class)
    public @ResponseBody ResponseEntity<ResponseDto<?>> handleException(ApiException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.generateErrorBody(e));
    }

    @ExceptionHandler(UnrecognizedQueryParameterException.class)
    public @ResponseBody ResponseEntity<ResponseDto<?>> handleQueryParameterException(
            UnrecognizedQueryParameterException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.generateErrorBody(e));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public @ResponseBody ResponseEntity<ResponseDto<?>> handleException() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.generateErrorBody(
                        HttpStatus.FORBIDDEN,
                        messageSource.getMessage(
                                MessageCodes.ERROR_USER_NOT_AUTHORIZED,
                                null,
                                MessageCodes.ERROR_USER_NOT_AUTHORIZED,
                                locale),
                        MessageCodes.ERROR_USER_NOT_AUTHORIZED,
                        null));
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ResponseDto<?>> handleException(Throwable e) {
        log.error("Runtime exception message: " + e.getMessage());
        log.error("RequestURI: " + MDC.get(RequestFilterMDC.MDC_REQUEST_URI_KEY));
        log.error("Query parameters: " + MDC.get(RequestFilterMDC.MDC_QUERY_PARAMS_KEY));
        log.error("Exception: " + ExceptionUtils.getStackTrace(e));
        Map<String, String> body = new HashMap<>();
        body.put("traceId", MDC.get("traceIdMDC"));
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.generateErrorBody(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        messageSource.getMessage(
                                MessageCodes.INTERNAL_ERROR, null, MessageCodes.INTERNAL_ERROR, locale),
                        MessageCodes.INTERNAL_ERROR,
                        body));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @NonNull HttpMessageNotReadableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.generateErrorBody(
                        HttpStatus.BAD_REQUEST,
                        messageSource.getMessage(MessageCodes.BAD_REQUEST, null, MessageCodes.BAD_REQUEST, locale),
                        MessageCodes.BAD_REQUEST,
                        null));
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            @NonNull HttpRequestMethodNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.generateErrorBody(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        messageSource.getMessage(
                                MessageCodes.METHOD_NOT_ALLOWED, null, MessageCodes.METHOD_NOT_ALLOWED, locale),
                        MessageCodes.METHOD_NOT_ALLOWED,
                        null));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
            @NonNull HttpMediaTypeNotAcceptableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.generateErrorBody(
                        HttpStatus.NOT_ACCEPTABLE,
                        messageSource.getMessage(
                                MessageCodes.NOT_ACCEPTABLE, null, MessageCodes.NOT_ACCEPTABLE, locale),
                        MessageCodes.NOT_ACCEPTABLE,
                        null));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            @NonNull HttpMediaTypeNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseDto.generateErrorBody(
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        messageSource.getMessage(
                                MessageCodes.UNSUPPORTED_MEDIA_TYPE, null, MessageCodes.UNSUPPORTED_MEDIA_TYPE, locale),
                        MessageCodes.UNSUPPORTED_MEDIA_TYPE,
                        null));
    }
}

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
package eu.openanalytics.rdepot.r.legacy.api.v1.controllers;

import static eu.openanalytics.rdepot.base.security.backends.keycloak.CustomKeycloakAuthenticationFailureHandler.SP_KEYCLOAK_ERROR_REASON;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.keycloak.adapters.OIDCAuthenticationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.NestedServletException;

import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.messaging.RefactoredMessageCodes;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;

@Controller
@RequestMapping("/error")
public class ErrorController 
	implements org.springframework.boot.web.servlet.error.ErrorController {
	
	private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);
	
	@RequestMapping(produces = "application/json")
	public ResponseEntity<?> handleErrorRest(Model model, HttpServletRequest request, HttpServletResponse response) {
		final String messageCode = RefactoredMessageCodes.UNKNOWN_ERROR;
		final String message = StaticMessageResolver.getMessage(messageCode);
		String content = null;
		try {
			final Throwable exception = ((Throwable)request.getAttribute("javax.servlet.error.exception"));
			if(exception != null) {
				logger.error(exception.getMessage(), exception);
				content = exception.getMessage();
			}
		} catch(ClassCastException e) {
			logger.error(e.getMessage(), e);
		}
		
		return ResponseEntity
				.status(response.getStatus())
				.contentType(MediaType.APPLICATION_JSON)
				.body(ResponseDto.generateErrorBody(HttpStatus.valueOf(response.getStatus()), message, messageCode, content));
	}
	@RequestMapping(produces = "text/html")
	public String handleError(Model model, HttpServletRequest request, HttpServletResponse response) throws ServletException {
		
	    Object obj = request.getSession().getAttribute(SP_KEYCLOAK_ERROR_REASON);
	    if (obj instanceof OIDCAuthenticationError.Reason) {
	    	request.getSession().removeAttribute(SP_KEYCLOAK_ERROR_REASON);
			OIDCAuthenticationError.Reason reason = (OIDCAuthenticationError.Reason) obj;
	    	if (reason == OIDCAuthenticationError.Reason.INVALID_STATE_COOKIE ||
				reason == OIDCAuthenticationError.Reason.STALE_TOKEN) {
	    		// These errors are typically caused by users using wrong bookmarks (e.g. bookmarks with states in)
				// or when some cookies got stale. However, the user is logged into the IDP, therefore it's enough to
				// send the user to the main page and they will get logged in automatically.
				return "redirect:/";
			} else {
				return "redirect:/authfailed";
			}
		}
	    
		Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception");
		if (exception == null) {
			exception = (Throwable) request.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
		}

		String[] msg = createMsgStack(exception);
		if (exception == null) {
			msg[0] = HttpStatus.valueOf(response.getStatus()).getReasonPhrase();
		}

		model.addAttribute("message", msg[0]);
//		model.addAttribute("stackTrace", msg[1]);
		model.addAttribute("status", response.getStatus());
	    
		
		return "error";
	}

//	@Override
	public String getErrorPath() {		
		return "/error";
	}

	private String[] createMsgStack(Throwable exception) {
		String message = "";
		String stackTrace = "";
		
		if (exception instanceof NestedServletException && exception.getCause() instanceof Exception) {
			exception = (Exception) exception.getCause();
		}
		if (exception != null) {
			if (exception.getMessage() != null) message = exception.getMessage();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try (PrintWriter writer = new PrintWriter(bos)) {
				exception.printStackTrace(writer);
			}
			stackTrace = bos.toString();
		}
		
		if (message == null || message.isEmpty()) message = "An unexpected server error occurred";
		if (stackTrace == null || stackTrace.isEmpty()) stackTrace = "n/a";
		
		return new String[] { message, stackTrace };
	}
}

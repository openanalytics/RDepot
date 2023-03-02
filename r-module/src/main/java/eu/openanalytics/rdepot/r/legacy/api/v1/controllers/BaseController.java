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

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.keycloak.KeycloakPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.utils.AjaxUtils;

@Controller
public class BaseController {
	
	@Value("${app.authentication}")
	private String mode;
	
	@Autowired
	private MessageSource messageSource;
	
	private static Locale locale = LocaleContextHolder.getLocale();
	
    @ExceptionHandler(Exception.class)
    public @ResponseBody String handleUncaughtException(Exception ex, WebRequest request, HttpServletResponse response) throws IOException 
    {
         if (AjaxUtils.isAjaxRequest(request)) {
            response.setHeader("Content-Type", "application/json");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "Unknown error occurred: " + ex.getMessage();
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            return null;
        }
    }
	
	@RequestMapping(value={"/", "index"}, method=RequestMethod.GET)
	public String index() {		
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	    if (principal == null || principal instanceof String)
	        if(mode.equals("keycloak")) {
	        	return "redirect:/manager";
	        } else if(mode.equals("ldap")) {
	        	return "redirect:/login";
	        }
	    if (principal instanceof UserDetails || 
	    		principal instanceof KeycloakPrincipal || 
	    		principal instanceof DefaultOidcUser
	    	)
	        return "redirect:/manager";
	    return "redirect:/login";
	}
	
	@RequestMapping("/accessdenied")
	public String handleAccessDenied() {
		return "accessdenied";
	}
	
	@RequestMapping(value = "/api/accessdenied", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> handleApiAccessDenied() {
		Map<String, String> body = new HashMap<>();
		body.put("status", "error");
		body.put("message", messageSource.getMessage(MessageCodes.ERROR_ACCESS_DENIED, null, 
				MessageCodes.ERROR_ACCESS_DENIED, locale));
		
		return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
	}
}

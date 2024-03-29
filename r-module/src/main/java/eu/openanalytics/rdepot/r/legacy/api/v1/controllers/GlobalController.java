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
package eu.openanalytics.rdepot.r.legacy.api.v1.controllers;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.method.HandlerMethod;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.PackageMaintainerNotFound;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.RepositoryDeclarativeModeException;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.RepositoryMaintainerNotFound;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.RepositoryNotFound;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.SubmissionNotFound;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.UserNotFound;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.UserUnauthorizedException; 

@ControllerAdvice
public class GlobalController {
	
	UserService userService;
	
	public GlobalController(UserService userService) {
		this.userService = userService;
	}
	
	@InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        // This code protects Spring Core from a "Remote Code Execution" attack (dubbed "Spring4Shell").
        // By applying this mitigation, you prevent the "Class Loader Manipulation" attack vector from firing.
        // For more details, see this post: https://www.lunasec.io/docs/blog/spring-rce-vulnerabilities/
        String[] denylist = new String[]{"class.*", "Class.*", "*.class.*", "*.Class.*"};
        dataBinder.setDisallowedFields(denylist);
    }
	
    Logger logger = LoggerFactory.getLogger(PackageController.class);
	
	@ModelAttribute("applicationVersion")
	public String getVersion() {
		return getClass().getPackage().getImplementationVersion();
	}

	@ModelAttribute("user_id")
	public String getUserId() {		
		User user = getUser();
		if(user != null)
			return Integer.toString(user.getId());
		else
			return "0";
	}
	
	@ModelAttribute("username")
	public String getUsername() {
		User user = getUser();
		if(user != null)
			return user.getName();
		else
			return null;
	}
	
	private User getUser() {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication(); 
		if(auth == null)
			return null;
		Object principal = auth.getPrincipal();
		
		String username = null;
		
		if(principal instanceof UserDetails) {
			username = ((UserDetails)principal).getUsername();
		} else if (principal instanceof KeycloakPrincipal) {
			username = ((KeycloakPrincipal<?>)principal).getName();
		} else if (principal instanceof DefaultOidcUser) {
			username = ((DefaultOidcUser)principal).getName();
		}
		
		if(username == null) {
			username = SecurityContextHolder.getContext().getAuthentication().getName();
		}
		
		return userService.findByLogin(username).orElse(null);
	}
	
	@ExceptionHandler({RepositoryDeclarativeModeException.class})
	public ResponseEntity<Map<String, String>> handleDisabledActionException(
			Exception exception, HandlerMethod handlerMethod) {
		return handleException(exception, handlerMethod, HttpStatus.FORBIDDEN, 
				"This action has been disabled by administrator.", false);
	}
	
	@ExceptionHandler({UserUnauthorizedException.class})
	public ResponseEntity<Map<String, String>> handleUserUnauthorizedException(
			Exception exception, HandlerMethod handlerMethod) {
		return handleException(exception, handlerMethod, HttpStatus.UNAUTHORIZED, 
				"Unauthorized user attempted to execute " + handlerMethod.getMethod().getName(), true);
	}
	
//	@ExceptionHandler({PackageActivateException.class, GetFileInBytesException.class, 
//		ReadPackageVignetteException.class, PackageDeactivateException.class,
//		PackageDeleteException.class, RepositoryCreateException.class, RepositoryPublishException.class,
//		RepositoryPublishException.class, RepositoryDeleteException.class,
//		RepositoryMaintainerEditException.class, RepositoryMaintainerDeleteException.class,
//		SubmissionDeleteException.class, SubmissionAcceptException.class, UserEditException.class, 
//		UserActivateException.class, NoAdminLeftException.class, UserDeactivateException.class,
//		SynchronizeMirrorException.class})
	public ResponseEntity<Map<String, String>> handleInternalServerError(
			Exception exception, HandlerMethod handlerMethod) {
		return handleException(exception, handlerMethod, HttpStatus.INTERNAL_SERVER_ERROR,
				"Action could not be completed due to internal server error.", true);
	}
	
	@ExceptionHandler({PackageMaintainerNotFound.class, RepositoryMaintainerNotFound.class, 
		RepositoryNotFound.class, SubmissionNotFound.class, UserNotFound.class})
    public ResponseEntity<Map<String, String>> handleObjectNotFoundException(
    		Exception exception, HandlerMethod handlerMethod) {
		return handleException(exception, handlerMethod, HttpStatus.NOT_FOUND, 
				"Requested resource was not found!", false);
    }
	
	private ResponseEntity<Map<String, String>> handleException(
			Exception exception, HandlerMethod handlerMethod, 
			HttpStatus httpStatus, String generalMessage, Boolean critical) {
		Class<?> controllerClass = handlerMethod.getMethod().getDeclaringClass();
		Logger logger = LoggerFactory.getLogger(controllerClass);
		String message = generalMessage + "\nException: " + 
				exception.getClass().getName() + ": " + exception.getMessage();
		
		if(critical)
			logger.error(message, exception);
		else
			logger.debug(message, exception);
		
		HashMap<String, String> result = new HashMap<>();
		result.put("error", exception.getMessage());
		
		return new ResponseEntity<>(result, httpStatus);
	}
}

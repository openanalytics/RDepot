/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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
package eu.openanalytics.rdepot.api.v1.controller;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.method.HandlerMethod;

import eu.openanalytics.rdepot.exception.GetFileInBytesException;
import eu.openanalytics.rdepot.exception.NoAdminLeftException;
import eu.openanalytics.rdepot.exception.PackageActivateException;
import eu.openanalytics.rdepot.exception.PackageDeactivateException;
import eu.openanalytics.rdepot.exception.PackageDeleteException;
import eu.openanalytics.rdepot.exception.PackageMaintainerNotFound;
import eu.openanalytics.rdepot.exception.PackageNotFound;
import eu.openanalytics.rdepot.exception.ReadPackageVignetteException;
import eu.openanalytics.rdepot.exception.RepositoryCreateException;
import eu.openanalytics.rdepot.exception.RepositoryDeclarativeModeException;
import eu.openanalytics.rdepot.exception.RepositoryDeleteException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerEditException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerNotFound;
import eu.openanalytics.rdepot.exception.RepositoryNotFound;
import eu.openanalytics.rdepot.exception.RepositoryPublishException;
import eu.openanalytics.rdepot.exception.SubmissionAcceptException;
import eu.openanalytics.rdepot.exception.SubmissionDeleteException;
import eu.openanalytics.rdepot.exception.SubmissionNotFound;
import eu.openanalytics.rdepot.exception.SynchronizeMirrorException;
import eu.openanalytics.rdepot.exception.UserActivateException;
import eu.openanalytics.rdepot.exception.UserDeactivateException;
import eu.openanalytics.rdepot.exception.UserEditException;
import eu.openanalytics.rdepot.exception.UserNotFound;
import eu.openanalytics.rdepot.exception.UserUnauthorizedException;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.UserService; 

@ControllerAdvice
public class GlobalController {
	
	UserService userService;
	
	@Autowired
	public GlobalController(UserService userService) {
		this.userService = userService;
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
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
		
		return userService.findByLogin(username);
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
	
	@ExceptionHandler({PackageActivateException.class, GetFileInBytesException.class, 
		ReadPackageVignetteException.class, PackageDeactivateException.class,
		PackageDeleteException.class, RepositoryCreateException.class, RepositoryPublishException.class,
		RepositoryPublishException.class, RepositoryDeleteException.class,
		RepositoryMaintainerEditException.class, RepositoryMaintainerDeleteException.class,
		SubmissionDeleteException.class, SubmissionAcceptException.class, UserEditException.class, 
		UserActivateException.class, NoAdminLeftException.class, UserDeactivateException.class,
		SynchronizeMirrorException.class})
	public ResponseEntity<Map<String, String>> handleInternalServerError(
			Exception exception, HandlerMethod handlerMethod) {
		return handleException(exception, handlerMethod, HttpStatus.INTERNAL_SERVER_ERROR,
				"Action could not be completed due to internal server error.", true);
	}
	
	@ExceptionHandler({PackageNotFound.class, RepositoryNotFound.class, PackageMaintainerNotFound.class,
		RepositoryMaintainerNotFound.class, SubmissionNotFound.class, UserNotFound.class})
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

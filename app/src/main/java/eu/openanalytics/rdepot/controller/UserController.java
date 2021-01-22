/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
package eu.openanalytics.rdepot.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import eu.openanalytics.rdepot.exception.NoAdminLeftException;
import eu.openanalytics.rdepot.exception.UserActivateException;
import eu.openanalytics.rdepot.exception.UserDeactivateException;
import eu.openanalytics.rdepot.exception.UserEditException;
import eu.openanalytics.rdepot.exception.UserNotFound;
import eu.openanalytics.rdepot.exception.UserUnauthorizedException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.RoleService;
import eu.openanalytics.rdepot.service.UserEventService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.warning.UserAlreadyActivatedWarning;
import eu.openanalytics.rdepot.warning.UserAlreadyDeactivatedWarning;

@Controller
@RequestMapping(value= {"/manager/users", "/api/manager/users"})
public class UserController {	

	Locale locale = LocaleContextHolder.getLocale();

	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private Validator userValidator;
	
	@Autowired
	private UserEventService userEventService;

	@Autowired
	private MessageSource messageSource;
		
	@InitBinder(value="user")
	private void initBinder(WebDataBinder binder) {
		binder.setValidator(userValidator);
	}
	
	private int placeholder = 9;
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(method=RequestMethod.GET)
	public String usersPage(Model model, Principal principal) {
		model.addAttribute("users", users());
		model.addAttribute("role", placeholder);

		return "users";
	}
	
	@PreAuthorize("hasAuthority('user')")

	@RequestMapping(value = "/{id}/token", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> getToken(@PathVariable Integer id, Principal principal) 
			throws UserUnauthorizedException {
		HashMap<String, String> result = new HashMap<>();
		User requester = userService.findByLogin(principal.getName());
		User expectedUser = userService.findById(id);
		
		if(requester.equals(expectedUser)) {		
			result.put("token", userService.generateToken(expectedUser.getLogin()));
		} else if(Objects.equals(requester.getRole().getName(), "admin")) {
			result.put("token", userService.generateToken(expectedUser.getLogin()));
		} else {
			throw new UserUnauthorizedException(messageSource, locale);
		}

		return result;
	}

	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/list", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<User> users() {
		return userService.findAll();
	}

	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/roles", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)	
	public @ResponseBody List<Role> getRoles() {
		return roleService.findAll();
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/{login}", method=RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<HashMap<String, Object>> userDetails(@PathVariable String login, Principal principal) 
			throws UserNotFound, UserUnauthorizedException {
		User requester = userService.findByLogin(principal.getName());
		User user = userService.findByLogin(login);
		HttpStatus httpStatus = HttpStatus.OK;

		HashMap<String, Object> result = new HashMap<String, Object>();
		if(requester == null || (!Objects.equals(requester.getRole().getName(), "admin") 
				&& !Objects.equals(requester.getLogin(), login))) {
			throw new UserUnauthorizedException(messageSource, locale);
		} else if(user == null) {
			throw new UserNotFound(messageSource, locale, login);
		} else {
			result.put("created", userEventService.getCreatedOn(user));
			result.put("lastloggedin", userEventService.getLastLoggedInOn(user));
			result.put("user", user);
		}
		
		return new ResponseEntity<>(result, httpStatus);
	}

	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/{id}/edit", method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<HashMap<String, Object>> editUser(@PathVariable Integer id, 
			@ModelAttribute(value="user") @Valid User user,
			BindingResult bindingResult, Principal principal) 
					throws UserUnauthorizedException, UserNotFound, UserEditException {
		HashMap<String, Object> result = new HashMap<>();
		HttpStatus httpStatus = HttpStatus.OK;
		User requester = userService.findByLogin(principal.getName());
		User currentUser = userService.findById(id);
		
		if(requester == null || !Objects.equals(requester.getRole().getName(), "admin")) {
			throw new UserUnauthorizedException(messageSource, locale);
		} else if(currentUser == null || user.getId() != id) {
			throw new UserNotFound(messageSource, locale, id);
		} else {
			userValidator.validate(user, bindingResult);
			if (bindingResult.hasErrors()) {
				result.put("error", messageSource.getMessage(bindingResult.getFieldError().getCode(), 
						null, bindingResult.getFieldError().getCode(), locale));
				//result.put("org.springframework.validation.BindingResult.user", bindingResult);
				httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			} else {
				userService.evaluateAndUpdate(currentUser, user, requester);
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_USER_UPDATED, null, 
						MessageCodes.SUCCESS_USER_UPDATED, locale));
			}
		} 

		
		return new ResponseEntity<>(result, httpStatus);
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/{id}/activate", method=RequestMethod.PATCH, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<HashMap<String, String>> activateUser(
			@PathVariable Integer id, Principal principal) 
					throws UserNotFound, UserUnauthorizedException, UserActivateException {
		HashMap<String, String> result = new HashMap<String, String>();
		User user = userService.findById(id);
		User requester = userService.findByLogin(principal.getName());
		HttpStatus httpStatus = HttpStatus.OK;
		
		try {
			if(requester == null || !Objects.equals(requester.getRole().getName(), "admin")) {
				throw new UserUnauthorizedException(messageSource, locale);
			} else if(user == null) {
				throw new UserNotFound(messageSource, locale, id);
			} else {
				userService.activateUser(user, requester);
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_USER_ACTIVATED, null, 
						MessageCodes.SUCCESS_USER_ACTIVATED, locale));				
			}
		} catch (UserAlreadyActivatedWarning w) {
			result.put("warning", w.getMessage());
		}			
		
		return new ResponseEntity<>(result, httpStatus);
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/{id}/deactivate", method=RequestMethod.PATCH, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<HashMap<String, String>> deactivateUser(
			@PathVariable Integer id, Principal principal) 
			throws UserUnauthorizedException, UserNotFound, UserDeactivateException, NoAdminLeftException {
		HashMap<String, String> result = new HashMap<String, String>();
		User user = userService.findById(id);
		User requester = userService.findByLogin(principal.getName());
		HttpStatus httpStatus = HttpStatus.OK;
		
		try {
			if(requester == null || !Objects.equals(requester.getRole().getName(), "admin")) {
				throw new UserUnauthorizedException(messageSource, locale);
			} else if(user == null) {
				throw new UserNotFound(messageSource, locale, id);
			} else {
				userService.deactivateUser(user, requester);
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_USER_DEACTIVATED, null, 
						MessageCodes.SUCCESS_USER_DEACTIVATED, locale));				
			}
		} catch (UserAlreadyDeactivatedWarning w) {
			result.put("warning", w.getMessage());
		}			
		
		return new ResponseEntity<>(result, httpStatus);
	}
}

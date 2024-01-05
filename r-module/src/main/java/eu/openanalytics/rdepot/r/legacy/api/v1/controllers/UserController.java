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

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.exception.NoAdminLeftException;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.RoleService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.factory.StrategyFactory;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.DTOConverter;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.RoleV1Dto;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.UserV1Dto;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.UserNotFound;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.UserUnauthorizedException;

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
	private MessageSource messageSource;
	
	@Autowired
	private StrategyFactory factory;
		
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
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		User expectedUser = userService.findById(id).orElse(null);
		
		if(requester.equals(expectedUser)) {		
			result.put("token", userService.generateToken(expectedUser.getLogin()));
		} else if(Objects.equals(requester.getRole().getName(), "admin")) {
			result.put("token", userService.generateToken(expectedUser.getLogin()));
		} else {
			throw new UserUnauthorizedException();
		}

		return result;
	}

	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/list", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<UserV1Dto> users() {
		return DTOConverter.convertUsers(
				userService.findAllSorted(Sort.by(Direction.ASC, "id")));
	}

	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/roles", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)	
	public @ResponseBody List<RoleV1Dto> getRoles() {
		return DTOConverter.convertRoles(roleService.findAll());
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/{login}", method=RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<HashMap<String, Object>> userDetails(@PathVariable String login, Principal principal) 
			throws UserNotFound, UserUnauthorizedException {
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		User user = userService.findByLogin(login).orElse(null);
		HttpStatus httpStatus = HttpStatus.OK;

		HashMap<String, Object> result = new HashMap<String, Object>();
		if(requester == null || (!Objects.equals(requester.getRole().getName(), "admin") 
				&& !Objects.equals(requester.getLogin(), login))) {
			throw new UserUnauthorizedException();
		} else if(user == null) {
			throw new UserNotFound();
		} else {
			Optional<LocalDate> lastLoggedInOn = Optional.ofNullable(user.getLastLoggedInOn());
			Optional<LocalDate> createdOn = Optional.ofNullable(user.getCreatedOn());
			if(createdOn.isPresent()) {
				result.put("created",  createdOn.get().toString());								
			}else {
				result.put("created", "never");
			}
			if(lastLoggedInOn.isPresent()) {
				result.put("lastloggedin",  lastLoggedInOn.get().toString());								
			}else {
				result.put("lastloggedin", "never");
			}
			result.put("user", DTOConverter.convertUser(user));
		}
		
		return new ResponseEntity<>(result, httpStatus);
	}

	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/{id}/edit", method=RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<HashMap<String, Object>> editUser(@PathVariable Integer id, 
			@ModelAttribute(value="user") User user,
			BindingResult bindingResult, Principal principal) 
					throws UserUnauthorizedException, UserNotFound {
		HashMap<String, Object> result = new HashMap<>();
		HttpStatus httpStatus = HttpStatus.OK;
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		User currentUser = userService.findById(id).orElse(null);
		
		if(requester == null || !Objects.equals(requester.getRole().getName(), "admin")) {
			throw new UserUnauthorizedException();
		} else if(currentUser == null || user.getId() != id) {
			throw new UserNotFound();
		} else {
			userValidator.validate(user, bindingResult);
			if (bindingResult.hasErrors()) {
				result.put("error", messageSource.getMessage(bindingResult.getFieldError().getCode(), 
						null, bindingResult.getFieldError().getCode(), locale));
				//result.put("org.springframework.validation.BindingResult.user", bindingResult);
				httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			} else {
				
				try {
					factory.updateUserStrategy(currentUser, requester, user).perform();
					result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_USER_UPDATED, null, 
							MessageCodes.SUCCESS_USER_UPDATED, locale));
				} catch (StrategyFailure e) {
					result.put("error", e.getMessage());
				}
			}
		} 

		
		return new ResponseEntity<>(result, httpStatus);
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/{id}/activate", method=RequestMethod.PATCH, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<HashMap<String, String>> activateUser(
			@PathVariable Integer id, Principal principal) 
					throws UserNotFound, UserUnauthorizedException {
		HashMap<String, String> result = new HashMap<String, String>();
		User user = userService.findById(id).orElse(null);
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		HttpStatus httpStatus = HttpStatus.OK;
		
		try {
			if(requester == null || !Objects.equals(requester.getRole().getName(), "admin")) {
				throw new UserUnauthorizedException();
			} else if(user == null) {
				throw new UserNotFound();
			} else {
				User updated = new User(user);
				updated.setActive(true);
				
				factory.updateUserStrategy(user, requester, updated).perform();
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_USER_ACTIVATED, null, 
						MessageCodes.SUCCESS_USER_ACTIVATED, locale));				
			}
		} catch (StrategyFailure e) {
			result.put("error", e.getMessage());
		}			
		
		return new ResponseEntity<>(result, httpStatus);
	}
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/{id}/deactivate", method=RequestMethod.PATCH, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<HashMap<String, String>> deactivateUser(
			@PathVariable Integer id, Principal principal) 
			throws UserUnauthorizedException, UserNotFound, NoAdminLeftException {
		HashMap<String, String> result = new HashMap<String, String>();
		User user = userService.findById(id).orElse(null);
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		HttpStatus httpStatus = HttpStatus.OK;
		
		try {
			if(requester == null || !Objects.equals(requester.getRole().getName(), "admin")) {
				throw new UserUnauthorizedException();
			} else if(user == null) {
				throw new UserNotFound();
			} else {
				User updated = new User(user);
				updated.setActive(false);
				
				factory.updateUserStrategy(user, requester, updated).perform();
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_USER_DEACTIVATED, null, 
						MessageCodes.SUCCESS_USER_DEACTIVATED, locale));				
			}
		} catch (StrategyFailure e) {
			result.put("error", e.getMessage());
		}			
		
		return new ResponseEntity<>(result, httpStatus);
	}
}

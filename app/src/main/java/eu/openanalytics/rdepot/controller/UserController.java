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
import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import com.auth0.jwt.JWT;

import eu.openanalytics.rdepot.exception.NoAdminLeftException;
import eu.openanalytics.rdepot.exception.UserActivateException;
import eu.openanalytics.rdepot.exception.UserDeactivateException;
import eu.openanalytics.rdepot.exception.UserEditException;
import eu.openanalytics.rdepot.exception.UserNotFound;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.ApiToken;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.ApiTokenRepository;
import eu.openanalytics.rdepot.service.RoleService;
import eu.openanalytics.rdepot.service.UserEventService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.warning.UserAlreadyActivatedWarning;
import eu.openanalytics.rdepot.warning.UserAlreadyDeactivatedWarning;

@Controller
@RequestMapping(value= {"/manager/users", "/api/manager/users"})
public class UserController 
{	
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
	private void initBinder(WebDataBinder binder) 
	{
		binder.setValidator(userValidator);
	}
	
	private int placeholder = 9;
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(method=RequestMethod.GET)
	public String usersPage(Model model, Principal principal) 
	{
		User requester = userService.findByLogin(principal.getName());
		model.addAttribute("users", users());
		model.addAttribute("role", placeholder);

		return "users";
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value = "/token", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> getToken(Principal principal)  
	{
		HashMap<String, String> result = new HashMap<>();
		
		result.put("token", userService.generateToken(principal.getName()));

		return result;
	}

	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/list", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<User> users() 
	{
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
	public HashMap<String, Object> userDetails(@PathVariable String login, Principal principal) {
		User requester = userService.findByLogin(principal.getName());
		User user = userService.findByLogin(login);
		Locale locale = LocaleContextHolder.getLocale();
		HashMap<String, Object> result = new HashMap<String, Object>();
		if(user == null)
			result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_FOUND, null, locale));
		else if(requester != null && !Objects.equals(requester.getRole().getName(), "admin") && !Objects.equals(requester.getLogin(), login)) {
			result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
		} else if(requester == null) {
			result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_FOUND, null, locale));
		} else {
			result.put("created", userEventService.getCreatedOn(user));
			result.put("lastloggedin", userEventService.getLastLoggedInOn(user));
			result.put("user", user);
		}
		
		
		return result;
	}

	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/{id}/edit", method=RequestMethod.POST)
	@ResponseBody
	public HashMap<String, Object> editUser(@PathVariable Integer id, @ModelAttribute(value="user") @Valid User user,
			BindingResult bindingResult, Principal principal) {
		HashMap<String, Object> result = new HashMap<>();
		User requester = userService.findByLogin(principal.getName());
		Locale locale = LocaleContextHolder.getLocale();
		try
		{
			if(requester == null || !Objects.equals(requester.getRole().getName(), "admin"))
				result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
			else if(user.getId() != id)
				result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_FOUND, null, locale));
			else
			{
				userValidator.validate(user, bindingResult);
				if (bindingResult.hasErrors())
					throw new UserEditException(messageSource, locale, user);
				userService.evaluateAndUpdate(user, requester);
//				if(requester == null || !Objects.equals(requester.getRole().getName(), "admin"))
//					result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));	
			} 
			return result;
		}
		catch (UserEditException | UserNotFound e) 
		{
			result.put("user", user);
			result.put("error", e.getMessage());
			result.put("roles", roleService.findAll());
			result.put("org.springframework.validation.BindingResult.user", bindingResult);
			return result;
		}
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/{id}/activate", method=RequestMethod.PUT, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody HashMap<String, String> activateUser(@PathVariable Integer id, Principal principal)
	{
		HashMap<String, String> result = new HashMap<String, String>();
		User user = userService.findById(id);
		Locale locale = LocaleContextHolder.getLocale();
		User requester = userService.findByLogin(principal.getName());
		try {
			if(requester == null)
				result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_FOUND, null, locale));
			else if(!Objects.equals(requester.getRole().getName(), "admin"))
				result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
			else if(user == null)
				result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_FOUND, null, locale));
			else {
				userService.activateUser(user, requester);
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_USER_ACTIVATED, null, locale));				
			}
		}
		catch (UserActivateException e) {
			result.put("error", e.getMessage());
		} catch (UserAlreadyActivatedWarning w) {
			result.put("warning", w.getMessage());
		}			
		
		return result;
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/{id}/deactivate", method=RequestMethod.PUT, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody HashMap<String, String> deactivateUser(@PathVariable Integer id, Principal principal)
	{
		HashMap<String, String> result = new HashMap<String, String>();
		User user = userService.findById(id);
		User requester = userService.findByLogin(principal.getName());
		Locale locale = LocaleContextHolder.getLocale();
		try
		{
			if(requester == null)
				result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_FOUND, null, locale));
			else if(!Objects.equals(requester.getRole().getName(), "admin"))
				result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
			else if(user == null)
				result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_FOUND, null, locale));
			else
			{
				userService.deactivateUser(user, requester);
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_USER_DEACTIVATED, null, locale));		
			} 
		}
		catch (UserDeactivateException | NoAdminLeftException e) {
			result.put("error", e.getMessage());
		} catch (UserAlreadyDeactivatedWarning w) {
			result.put("warning", w.getMessage());
		}
		
		return result;
	}
}

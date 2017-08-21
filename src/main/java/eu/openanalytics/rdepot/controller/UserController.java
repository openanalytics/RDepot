/**
 * RDepot
 *
 * Copyright (C) 2012-2017 Open Analytics NV
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.openanalytics.rdepot.exception.UserDeleteException;
import eu.openanalytics.rdepot.exception.UserEditException;
import eu.openanalytics.rdepot.exception.UserNotFound;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.model.UserEvent;
import eu.openanalytics.rdepot.service.RoleService;
import eu.openanalytics.rdepot.service.UserEventService;
import eu.openanalytics.rdepot.service.UserService;

@Controller
@RequestMapping(value="/manager/users")
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
	
	@InitBinder(value="user")
	private void initBinder(WebDataBinder binder) 
	{
		binder.setValidator(userValidator);
	}
	
	private int placeholder = 9;
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(method=RequestMethod.GET)
	public String usersPage(Model model) 
	{
		model.addAttribute("users", users());
		model.addAttribute("role", placeholder);
		return "users";
	}
	
//	@PreAuthorize("hasAuthority('admin')")
//	@RequestMapping(value="/create", method=RequestMethod.GET)
//	public String newUserPage(Model model) 
//	{
//		model.addAttribute("user", new User());
//		model.addAttribute("roles", roleService.findAll());
//		model.addAttribute("role", placeholder);
//		return "user-create";
//	}
	
//	@PreAuthorize("hasAuthority('admin')")
//	@RequestMapping(value="/create", method=RequestMethod.POST)
//	public String createNewUser(@ModelAttribute(value="user") @Valid User user,	BindingResult result,
//			RedirectAttributes redirectAttributes) 
//	{
//		userValidator.validate(user, result);
//		if (result.hasErrors())
//		{	
//			redirectAttributes.addFlashAttribute("user", user);
//			redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", result);
//			redirectAttributes.addFlashAttribute("roles", roleService.findAll());
//			return "redirect:/manager/users/create";
//		}
//		else
//		{
//			user.setHashedPassword(new BCryptPasswordEncoder().encode(user.getHashedPassword()));
//			userService.create(user);
//			redirectAttributes.addFlashAttribute("success", MessageCodes.SUCCESS_USER_CREATED);
//			return "redirect:/manager/users";
//		}
//	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/list", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<User> users() 
	{
		return userService.findAll();
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/deleted", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<User> deletedUsers() 
	{
		return userService.findByDeleted(true);
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/{id}/edit", method=RequestMethod.GET)
	public String editUserPage(@PathVariable Integer id, Principal principal, Model model, 
			RedirectAttributes redirectAttributes)
	{
		User requester = userService.findByLogin(principal.getName());
		String address = "redirect:/manager/users";
		User user = userService.findById(id);
		if(user == null)
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_FOUND);
		else if(requester == null || !Objects.equals(requester.getRole().getName(), "admin"))
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
		else
		{
			model.addAttribute("user", user);
			model.addAttribute("roles", roleService.findAll());
			model.addAttribute("role", requester.getRole().getValue());
			address = "user-edit";
		}
		return address;
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/{login}", method=RequestMethod.GET)
	public String userPage(@PathVariable String login, Principal principal, Model model, 
			RedirectAttributes redirectAttributes)
	{
		User requester = userService.findByLogin(principal.getName());
		String address = "redirect:/manager";
		User user = userService.findByLogin(login);
		if(user == null)
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_FOUND);
		else if(requester != null && !Objects.equals(requester.getRole().getName(), "admin") && !Objects.equals(requester.getLogin(), login))
			address = "redirect:/manager/users/"+requester.getLogin();
		else
		{
			HashMap<Date, TreeSet<UserEvent>> events = new HashMap<Date, TreeSet<UserEvent>>();
			List<Date> dates = userEventService.getUniqueDatesByUser(user);
			for(Date date : dates)
			{
				List<UserEvent> uEvents = userEventService.findByDateAndUser(date, user);
				TreeSet<UserEvent> sortedTime = new TreeSet<UserEvent>(new Comparator<UserEvent>()
				{
					@Override
				    public int compare(UserEvent lhs, UserEvent rhs) 
				    {
				        if (lhs.getTime().getTime() > rhs.getTime().getTime())
				            return -1;
				        else if (lhs.getTime() == rhs.getTime())
				            return 0;
				        else
				            return 1;
				    }
				});
				sortedTime.addAll(uEvents);
				events.put(date, sortedTime);
			}
			TreeMap<Date, TreeSet<UserEvent>> sorted = new TreeMap<Date, TreeSet<UserEvent>>(new Comparator<Date>() 
			{
			    @Override
			    public int compare(Date lhs, Date rhs) 
			    {
			        if (lhs.getTime() > rhs.getTime())
			            return -1;
			        else if (lhs.getTime() == rhs.getTime())
			            return 0;
			        else
			            return 1;
			    }
			});
			sorted.putAll(events);
			model.addAttribute("events", sorted);
			model.addAttribute("created", userEventService.getCreatedOn(user));
			model.addAttribute("lastloggedin", userEventService.getLastLoggedInOn(user));
			model.addAttribute("user", user);
			model.addAttribute("role", requester.getRole().getValue());
			address = "user";
		}
		return address;
	}
	
	@PreAuthorize("hasAuthority('user')")
	@RequestMapping(value="/{id}/edit", method=RequestMethod.POST)
	public String editUser(@PathVariable Integer id, @ModelAttribute(value="user") @Valid User user,
			BindingResult result, Principal principal,
			RedirectAttributes redirectAttributes)
	{
		String address = "redirect:/manager/users";
		User requester = userService.findByLogin(principal.getName());
		try
		{
			if(requester == null || !Objects.equals(requester.getRole().getName(), "admin"))
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else if(user.getId() != id)
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_FOUND);
			else
			{
				userValidator.validate(user, result);
				if (result.hasErrors())
					throw new UserEditException(result.getFieldError().getCode());
				userService.update(user, requester);
				if(requester == null || !Objects.equals(requester.getRole().getName(), "admin"))
					address = "redirect:/manager";		
			} 
			return address;
		}
		catch (UserEditException e) 
		{
			redirectAttributes.addFlashAttribute("user", user);
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			redirectAttributes.addFlashAttribute("roles", roleService.findAll());
			redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", result);
			return "user-edit";
		}
	}
	
//	@PreAuthorize("hasAuthority('user')")
//	@RequestMapping(value="/{id}/password", method=RequestMethod.POST)
//	public String changePassword(@PathVariable Integer id, Principal principal, 
//			RedirectAttributes redirectAttributes, 
//			@RequestParam("oldPassword") String oldPassword,
//			@RequestParam("newPassword") String newPassword)
//	{
//		String address = "redirect:/manager/settings";
//		User requester = userService.findByLogin(principal.getName());
//		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//		try
//		{
//			if(requester == null || requester.getId() != id)
//				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
//			else if(oldPassword == null || !encoder.matches(oldPassword, requester.getHashedPassword()))
//				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_PASSWORD_DOES_NOT_MATCH);
//			else if(newPassword == null || newPassword.isEmpty() || newPassword.trim().equals(""))
//				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_PASSWORD_INVALID);
//			else
//			{
//				requester.setHashedPassword(encoder.encode(newPassword));
//				userService.update(requester);
//				redirectAttributes.addFlashAttribute("success", MessageCodes.SUCCESS_PASSWORD_CHANGED);
//			} 
//			return address;
//		}
//		catch (UserEditException e) 
//		{
//			redirectAttributes.addFlashAttribute("error", e.getMessage());
//			return address;
//		}
//	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/{id}/activate", method=RequestMethod.PUT, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody HashMap<String, String> activateUser(@PathVariable Integer id, Principal principal)
	{
		HashMap<String, String> result = new HashMap<String, String>();
		User user = userService.findById(id);
		User requester = userService.findByLogin(principal.getName());
		try 
		{
			if(requester == null)
				result.put("error", MessageCodes.ERROR_USER_NOT_FOUND);
			else if(!Objects.equals(requester.getRole().getName(), "admin"))
				result.put("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else if(user == null)
				result.put("error", MessageCodes.ERROR_USER_NOT_FOUND);
			else if(user.isActive())
				result.put("warning", MessageCodes.WARNING_USER_ALREADY_ACTIVATED);
			else
			{
				user.setActive(true);
				userService.update(user, requester);
				result.put("success", MessageCodes.SUCCESS_USER_ACTIVATED);				
			}
			return result;
		}
		catch (UserEditException e) 
		{
			result.put("error", e.getMessage());
			return result;
		}			
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/{id}/deactivate", method=RequestMethod.PUT, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody HashMap<String, String> deactivateUser(@PathVariable Integer id, Principal principal)
	{
		HashMap<String, String> result = new HashMap<String, String>();
		User user = userService.findById(id);
		User requester = userService.findByLogin(principal.getName());
		try
		{
			if(requester == null)
				result.put("error", MessageCodes.ERROR_USER_NOT_FOUND);
			else if(!Objects.equals(requester.getRole().getName(), "admin"))
				result.put("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else if(user == null)
				result.put("error", MessageCodes.ERROR_USER_NOT_FOUND);
			else if(!user.isActive())
				result.put("warning", MessageCodes.WARNING_USER_ALREADY_DEACTIVATED);
			else
			{
				user.setActive(false);
				userService.update(user, requester);
				result.put("success", MessageCodes.SUCCESS_USER_DEACTIVATED);		
			} 
			return result;
		}
		catch (UserEditException e) 
		{
			result.put("error", e.getMessage());
			return result;
		}	
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value="/{id}/delete", method=RequestMethod.DELETE, produces="application/json")
	public @ResponseBody HashMap<String, String> deleteUser(@PathVariable Integer id, Principal principal)
	{
		HashMap<String, String> result = new HashMap<String, String>();
		User requester = userService.findByLogin(principal.getName());
		try
		{
			if(requester == null)
				result.put("error", MessageCodes.ERROR_ADMIN_NOT_FOUND);
			else if(!Objects.equals(requester.getRole().getName(), "admin"))
				result.put("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else if(requester.getId() == id)
				result.put("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else
			{
				userService.delete(id, requester);
				result.put("success", MessageCodes.SUCCESS_USER_DELETED);
			}
			return result;
		}
		catch(UserDeleteException e)
		{
			result.put("error", e.getMessage());
			return result;
		}	
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value="/{id}/sdelete", method=RequestMethod.DELETE, produces="application/json")
	public @ResponseBody HashMap<String, String> shiftDeleteUser(@PathVariable Integer id, Principal principal)
	{
		HashMap<String, String> result = new HashMap<String, String>();
		User requester = userService.findByLogin(principal.getName());
		try
		{
			if(requester == null)
				result.put("error", MessageCodes.ERROR_USER_NOT_FOUND);
			else if(requester.getId() == id)
				result.put("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else
			{
				userService.shiftDelete(id);
				result.put("success", MessageCodes.SUCCESS_USER_DELETED);
			}
			return result;
		}
		catch(UserNotFound e)
		{
			result.put("error", e.getMessage());
			return result;
		}	
	}
}

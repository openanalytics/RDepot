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
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import eu.openanalytics.rdepot.exception.RepositoryMaintainerCreateException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerEditException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerNotFound;
import eu.openanalytics.rdepot.exception.UserEditException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.CreateRepositoryMaintainerRequestBody;
import eu.openanalytics.rdepot.model.EditRepositoryMaintainerRequestBody;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.RoleService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.validation.RepositoryMaintainerValidator;

@Controller
@RequestMapping(value= {"/manager/repositories/maintainers", "/api/manager/repositories/maintainers"})
@PreAuthorize("hasAuthority('admin')")
public class RepositoryMaintainerController 
{
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private RepositoryMaintainerService repositoryMaintainerService;
	
	@Autowired
	private RepositoryMaintainerValidator repositoryMaintainerValidator;
	
	@Autowired
	private MessageSource messageSource;
	
	@InitBinder(value="repositorymaintainer")
	private void initBinder(WebDataBinder binder)
	{
		binder.setValidator(repositoryMaintainerValidator);
	}
	
	private int placeholder = 9;
	
	@RequestMapping(method=RequestMethod.GET)
	public String repositoryMaintainersPage(Model model, Principal principal) 
	{
		User user = userService.findByLogin(principal.getName());
		model.addAttribute("repositorymaintainers", repositoryMaintainers());
		model.addAttribute("role", placeholder);

		return "repositorymaintainers";
	}
	
	@RequestMapping(value="/create", method=RequestMethod.GET)
	public @ResponseBody HashMap<String, Object> newRepositoryMaintainerDialog() {
		HashMap<String, Object> result = new HashMap<>();
		result.put("repositorymaintainer", new RepositoryMaintainer());
		result.put("users", userService.findEligibleRepositoryMaintainers());
		result.put("repositories", repositoryService.findAll());
		result.put("role", placeholder);
		return result;
	}
	
	@RequestMapping(value="/create", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody HashMap<String, Object> createNewRepositoryMaintainer(
			@RequestBody CreateRepositoryMaintainerRequestBody requestBody,
			Principal principal) 
	{
		HashMap<String, Object> result = new HashMap<>();
		User user = userService.findById(requestBody.getUserId());
		Repository repository = repositoryService.findById(requestBody.getRepositoryId());
		RepositoryMaintainer repositoryMaintainer = new RepositoryMaintainer(0, user, repository, false);
		Locale locale = LocaleContextHolder.getLocale();
		BindException bindingResult = new BindException(repositoryMaintainer, "repositoryMaintainer");
		try {
			User requester = userService.findByLogin(principal.getName());
			repositoryMaintainerValidator.validate(repositoryMaintainer, bindingResult);
			if(requester == null) {
				throw new RepositoryMaintainerCreateException(messageSource.getMessage(MessageCodes.ERROR_USER_NOT_FOUND, null, locale));
			} else if(!Objects.equals(requester.getRole().getName(), "admin")) {
				throw new RepositoryMaintainerCreateException(messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
			} else if(bindingResult.hasErrors()) {
				throw new RepositoryMaintainerCreateException(bindingResult.getFieldError().getCode());
			} else {
				User maintainer = repositoryMaintainer.getUser();
				switch(maintainer.getRole().getName()) {
				case "user":
					userService.updateRole(maintainer, requester, roleService.getRepositoryMaintainerRole());
				case "repositorymaintainer":
					repositoryMaintainerService.create(repositoryMaintainer, requester);
					break;
				default:
					throw new RepositoryMaintainerCreateException(messageSource.getMessage(MessageCodes.ERROR_USER_NOT_CAPABLE, null, locale));
				}
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_CREATED, null, locale));
			}
			return result;
		} catch(RepositoryMaintainerCreateException | UserEditException e) {
			result.put("repositorymaintainer", repositoryMaintainer);
			result.put("org.springframework.validation.BindingResult.repositorymaintainer", bindingResult);
			result.put("error", e.getMessage());
			result.put("users", userService.findEligibleRepositoryMaintainers());
			result.put("repositories", repositoryService.findAll());
			return result;
		}
	}

	@RequestMapping(value="/{id}/edit", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody HashMap<String, Object> updateRepositoryMaintainer(
			@RequestBody EditRepositoryMaintainerRequestBody requestBody,
			@PathVariable Integer id,
			Principal principal) 
	{
		HashMap<String, Object> result = new HashMap<>();
		
		Repository repository = repositoryService.findById(requestBody.getRepositoryId());
		RepositoryMaintainer repositoryMaintainer = repositoryMaintainerService.findById(id);
		repositoryMaintainer.setRepository(repository);
		Locale locale = LocaleContextHolder.getLocale();
		BindException bindingResult = new BindException(repositoryMaintainer, "repositoryMaintainer");
		try
		{	
			User requester = userService.findByLogin(principal.getName());
			if(repositoryMaintainer.getId() != id)
				throw new RepositoryMaintainerEditException(messageSource.getMessage(MessageCodes.ERROR_REPOSITORYMAINTAINER_NOT_FOUND, null, locale));
			else if(requester == null)
				throw new RepositoryMaintainerEditException(messageSource.getMessage(MessageCodes.ERROR_USER_NOT_FOUND, null, locale));
			else if(!Objects.equals(requester.getRole().getName(), "admin"))
				throw new RepositoryMaintainerEditException(messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
			else
			{	
				repositoryMaintainerValidator.validate(repositoryMaintainer, bindingResult);
				if (bindingResult.hasErrors())
					throw new RepositoryMaintainerEditException(bindingResult.getFieldError().getCode());
				else
				{
					repositoryMaintainerService.update(repositoryMaintainer, requester);
					result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_UPDATED, null, locale));
				}
			}
			return result;
		}
		catch(RepositoryMaintainerEditException e)
		{
			result.put("repositorymaintainer", repositoryMaintainer);
			result.put(
					"org.springframework.validation.BindingResult.repositorymaintainer", bindingResult);
			result.put("error", e.getMessage());
			result.put("users", userService.findEligibleRepositoryMaintainers());
			result.put("repositories", repositoryService.findAll());
			return result;
		}
	}

	@RequestMapping(value="/list", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<RepositoryMaintainer> repositoryMaintainers() 
	{
		return repositoryMaintainerService.findAll();
	}
	
	@RequestMapping(value="/deleted", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<RepositoryMaintainer> deletedRepositoryMaintainers() 
	{
		return repositoryMaintainerService.findByDeleted(true);
	}
	
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value="/{id}/delete", method=RequestMethod.DELETE, produces="application/json")
	public @ResponseBody HashMap<String, String> deleteRepositoryMaintainer(@PathVariable Integer id, Principal principal)
	{	
		HashMap<String, String> result = new HashMap<String, String>();
		User requester = userService.findByLogin(principal.getName());
		Locale locale = LocaleContextHolder.getLocale();
		try
		{
			if(requester == null)
				throw new RepositoryMaintainerDeleteException(messageSource.getMessage(MessageCodes.ERROR_USER_NOT_FOUND, null, locale));
			else if(!Objects.equals(requester.getRole().getName(), "admin"))
				throw new RepositoryMaintainerDeleteException(messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
			else
			{
				repositoryMaintainerService.delete(id, requester);
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_DELETED, null, locale));
			}	
			return result;
		}
		catch(RepositoryMaintainerDeleteException e)
		{
			result.put("error", e.getMessage());
			return result;
		}
	
	}
	
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value="/{id}/sdelete", method=RequestMethod.DELETE, produces="application/json")
	public @ResponseBody HashMap<String, String> shiftDeleteRepositoryMaintainer(@PathVariable Integer id)
	{	
		Locale locale = LocaleContextHolder.getLocale();
		HashMap<String, String> result = new HashMap<String, String>();
		try
		{
			repositoryMaintainerService.shiftDelete(id);
			result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_DELETED, null, locale));	
			return result;
		}
		catch(RepositoryMaintainerNotFound e)
		{
			result.put("error", e.getMessage());
			return result;
		}
	
	}
}

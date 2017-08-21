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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eu.openanalytics.rdepot.exception.RepositoryMaintainerCreateException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerEditException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerNotFound;
import eu.openanalytics.rdepot.exception.UserEditException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.RoleService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.validation.RepositoryMaintainerValidator;

@Controller
@RequestMapping(value="/manager/repositories/maintainers")
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
	
	@InitBinder(value="repositorymaintainer")
	private void initBinder(WebDataBinder binder)
	{
		binder.setValidator(repositoryMaintainerValidator);
	}
	
	private int placeholder = 9;
	
	@RequestMapping(method=RequestMethod.GET)
	public String repositoryMaintainersPage(Model model) 
	{
		model.addAttribute("repositorymaintainers", repositoryMaintainers());
		model.addAttribute("role", placeholder);
		return "repositorymaintainers";
	}
	
	@RequestMapping(value="/create", method=RequestMethod.GET)
	public String newRepositoryMaintainerPage(Model model) 
	{
		model.addAttribute("repositorymaintainer", new RepositoryMaintainer());
		model.addAttribute("users", userService.findEligibleRepositoryMaintainers());
		model.addAttribute("repositories", repositoryService.findAll());
		model.addAttribute("role", placeholder);
		return "repositorymaintainer-create";
	}
	
	@RequestMapping(value="/create", method=RequestMethod.POST)
	public String createNewRepositoryMaintainer(
			@ModelAttribute(value="repositorymaintainer") @Valid RepositoryMaintainer repositoryMaintainer,
			BindingResult result, RedirectAttributes redirectAttributes, 
			Principal principal) 
	{
		try
		{
			User requester = userService.findByLogin(principal.getName());
			repositoryMaintainerValidator.validate(repositoryMaintainer, result);
			if(requester == null)
				throw new RepositoryMaintainerCreateException(MessageCodes.ERROR_USER_NOT_FOUND);
			else if(!Objects.equals(requester.getRole().getName(), "admin"))
				throw new RepositoryMaintainerCreateException(MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else if (result.hasErrors())
				throw new RepositoryMaintainerCreateException(result.getFieldError().getCode());
			else
			{
				User maintainer = repositoryMaintainer.getUser();
				switch(maintainer.getRole().getName())
				{
					case "user":
						maintainer.setRole(roleService.findByName("repositorymaintainer"));
						userService.update(maintainer, requester);
					case "repositorymaintainer":
						repositoryMaintainerService.create(repositoryMaintainer, requester);
						break;
					default:
						throw new RepositoryMaintainerCreateException(MessageCodes.ERROR_USER_NOT_CAPABLE);
				}				
				redirectAttributes.addFlashAttribute("success", MessageCodes.SUCCESS_REPOSITORYMAINTAINER_CREATED);
			}		
			return "redirect:/manager/repositories/maintainers";
		}
		catch(RepositoryMaintainerCreateException | UserEditException e)
		{
			redirectAttributes.addFlashAttribute("repositorymaintainer", repositoryMaintainer);
			redirectAttributes.addFlashAttribute(
					"org.springframework.validation.BindingResult.repositorymaintainer", result);
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			redirectAttributes.addFlashAttribute("users", userService.findEligibleRepositoryMaintainers());
			redirectAttributes.addFlashAttribute("repositories", repositoryService.findAll());
			return "redirect:/manager/repositories/maintainers/create";
		}				
	}
	
	@RequestMapping(value="/{id}/edit", method=RequestMethod.GET)
	public String editRepositoryMaintainerPage(@PathVariable Integer id, Model model, 
			RedirectAttributes redirectAttributes)
	{
		RepositoryMaintainer repositoryMaintainer = repositoryMaintainerService.findById(id);
		String address = "redirect:/manager/repositories/maintainers";
		if(repositoryMaintainer == null)
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_REPOSITORYMAINTAINER_NOT_FOUND);
		else
		{
			model.addAttribute("repositorymaintainer", repositoryMaintainer);
			model.addAttribute("users", userService.findEligibleRepositoryMaintainers());
			model.addAttribute("repositories", repositoryService.findAll());
			model.addAttribute("role", placeholder);
			address = "repositorymaintainer-edit";
		}
		return address;
	}
	
	@RequestMapping(value="/{id}/edit", method=RequestMethod.POST)
	public String updateRepositoryMaintainer(
			@ModelAttribute(value="repositorymaintainer") @Valid RepositoryMaintainer repositoryMaintainer,
			BindingResult result, @PathVariable Integer id,
			RedirectAttributes redirectAttributes, Principal principal) 
	{
		try
		{	
			User requester = userService.findByLogin(principal.getName());
			if(repositoryMaintainer.getId() != id)
				throw new RepositoryMaintainerEditException(MessageCodes.ERROR_REPOSITORYMAINTAINER_NOT_FOUND);
			else if(requester == null)
				throw new RepositoryMaintainerEditException(MessageCodes.ERROR_USER_NOT_FOUND);
			else if(!Objects.equals(requester.getRole().getName(), "admin"))
				throw new RepositoryMaintainerEditException(MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else
			{	
				repositoryMaintainerValidator.validate(repositoryMaintainer, result);
				if (result.hasErrors())
					throw new RepositoryMaintainerEditException(result.getFieldError().getCode());
				else
				{
					repositoryMaintainerService.update(repositoryMaintainer, requester);
					redirectAttributes.addFlashAttribute("success", MessageCodes.SUCCESS_REPOSITORYMAINTAINER_UPDATED);
				}
			}
			return "redirect:/manager/repositories/maintainers";
		}
		catch(RepositoryMaintainerEditException e)
		{
			redirectAttributes.addFlashAttribute("repositorymaintainer", repositoryMaintainer);
			redirectAttributes.addFlashAttribute(
					"org.springframework.validation.BindingResult.repositorymaintainer", result);
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			redirectAttributes.addFlashAttribute("users", userService.findEligibleRepositoryMaintainers());
			redirectAttributes.addFlashAttribute("repositories", repositoryService.findAll());
			return "redirect:/manager/repositories/maintainers/{id}/edit";
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
		try
		{
			if(requester == null)
				throw new RepositoryMaintainerDeleteException(MessageCodes.ERROR_USER_NOT_FOUND);
			else if(!Objects.equals(requester.getRole().getName(), "admin"))
				throw new RepositoryMaintainerDeleteException(MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else
			{
				repositoryMaintainerService.delete(id, requester);
				result.put("success", MessageCodes.SUCCESS_REPOSITORYMAINTAINER_DELETED);
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
		HashMap<String, String> result = new HashMap<String, String>();
		try
		{
			repositoryMaintainerService.shiftDelete(id);
			result.put("success", MessageCodes.SUCCESS_REPOSITORYMAINTAINER_DELETED);	
			return result;
		}
		catch(RepositoryMaintainerNotFound e)
		{
			result.put("error", e.getMessage());
			return result;
		}
	
	}
}

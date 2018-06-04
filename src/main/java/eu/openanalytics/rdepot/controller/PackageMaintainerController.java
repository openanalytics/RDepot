/**
 * R Depot
 *
 * Copyright (C) 2012-2018 Open Analytics NV
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

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.http.HttpStatus;

import com.google.gson.Gson;

import eu.openanalytics.rdepot.exception.PackageMaintainerCreateException;
import eu.openanalytics.rdepot.exception.PackageMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.PackageMaintainerEditException;
import eu.openanalytics.rdepot.exception.UserEditException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.PackageMaintainerService;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.RoleService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.validation.PackageMaintainerValidator;

@Controller
@RequestMapping(value="/manager/packages/maintainers")
public class PackageMaintainerController {

	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private PackageService packageService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private PackageMaintainerService packageMaintainerService;
	
	@Autowired
	private PackageMaintainerValidator packageMaintainerValidator;
	
	@Autowired
	private Gson gson;
	
	@InitBinder(value="packagemaintainer")
	private void initBinder(WebDataBinder binder)
	{
		binder.setValidator(packageMaintainerValidator);
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(method=RequestMethod.GET)
	public String packageMaintainersPage(Model model, Principal principal) 
	{
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		model.addAttribute("packagemaintainers", packageMaintainerService.findByRequester(requester));
		model.addAttribute("role", requester.getRole().getValue());
		return "packagemaintainers";
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/list", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<PackageMaintainer> packageMaintainers(Principal principal) 
	{
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		return packageMaintainerService.findByRequester(requester);
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/deleted", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<PackageMaintainer> deletedPackageMaintainers() 
	{
		return packageMaintainerService.findByDeleted(true);
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/create", method=RequestMethod.GET)
	public String newPackageMaintainerPage(Model model, Principal principal) 
	{
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		model.addAttribute("packagemaintainer", new PackageMaintainer());
		model.addAttribute("users", userService.findEligiblePackageMaintainers());
		model.addAttribute("repositories", repositoryService.findMaintainedBy(requester));
		model.addAttribute("packages", gson.toJson(packageService.findNamesPerRepository()));
		model.addAttribute("role", requester.getRole().getValue());
		return "packagemaintainer-create";
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/create", method=RequestMethod.POST)
	public String createNewPackageMaintainer(
			@ModelAttribute(value="packagemaintainer") @Valid PackageMaintainer packageMaintainer,
			BindingResult result, RedirectAttributes redirectAttributes, 
			Principal principal) 
	{
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName()); 
		try
		{
			if(!userService.isAuthorizedToEdit(packageMaintainer, requester))
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else
			{
				packageMaintainerValidator.validate(packageMaintainer, result);
				if (result.hasErrors())
					throw new PackageMaintainerCreateException(result.getFieldError().getCode());
				else
				{
					User maintainer = packageMaintainer.getUser();
					// Can a repositorymaintainer be a packagemaintainer also, in another repository then? NO!
					switch(maintainer.getRole().getName())
					{
						case "user":
							maintainer.setRole(roleService.findByName("packagemaintainer"));
							userService.update(maintainer, requester);
						case "packagemaintainer":
							packageMaintainerService.create(packageMaintainer, requester);
							break;
						default:
							throw new PackageMaintainerCreateException(MessageCodes.ERROR_USER_NOT_CAPABLE);
					}
					redirectAttributes.addFlashAttribute("success", MessageCodes.SUCCESS_PACKAGEMAINTAINER_CREATED);
				}
			}
			return "redirect:/manager/packages/maintainers";
		}
		catch(PackageMaintainerCreateException | UserEditException e)
		{
			redirectAttributes.addFlashAttribute("packagemaintainer", packageMaintainer);
			redirectAttributes.addFlashAttribute(
					"org.springframework.validation.BindingResult.packagemaintainer", result);
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			redirectAttributes.addFlashAttribute("users", userService.findEligiblePackageMaintainers());
			redirectAttributes.addFlashAttribute("repositories", repositoryService.findMaintainedBy(requester));
			redirectAttributes.addFlashAttribute("packages", gson.toJson(packageService.findNamesPerRepository()));
			return "redirect:/manager/packages/maintainers/create";
		}
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/{id}/edit", method=RequestMethod.GET)
	public String editPackageMaintainerPage(@PathVariable Integer id, Principal principal, 
			RedirectAttributes redirectAttributes, Model model)
	{
		PackageMaintainer packageMaintainer = packageMaintainerService.findById(id);
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		String address = "redirect:/manager/packages/maintainers";
		if(packageMaintainer == null)
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_PACKAGEMAINTAINER_NOT_FOUND);
		else if(!userService.isAuthorizedToEdit(packageMaintainer, requester))
			redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
		else
		{
			model.addAttribute("packagemaintainer", packageMaintainer);
			model.addAttribute("repositories", repositoryService.findMaintainedBy(requester));
			model.addAttribute("packages", gson.toJson(packageService.findNamesPerRepository()));
			model.addAttribute("role", requester.getRole().getValue());
			address = "packagemaintainer-edit";
		}
		return address;
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/{id}/edit", method=RequestMethod.POST)
	public String updatePackageMaintainer(
			@ModelAttribute(value="packagemaintainer") @Valid PackageMaintainer packageMaintainer,
			BindingResult result, RedirectAttributes redirectAttributes,
			Principal principal, @PathVariable Integer id) 
	{
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		try
		{
			if(packageMaintainer.getId() != id)
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_PACKAGEMAINTAINER_NOT_FOUND);
			else if(!userService.isAuthorizedToEdit(packageMaintainer, requester))
				redirectAttributes.addFlashAttribute("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else
			{
				packageMaintainerValidator.validate(packageMaintainer, result);
				if (result.hasErrors())
					throw new PackageMaintainerEditException(result.getFieldError().getCode());
				else
				{
					packageMaintainerService.update(packageMaintainer, requester);
					redirectAttributes.addFlashAttribute("success", MessageCodes.SUCCESS_PACKAGEMAINTAINER_UPDATED);
				}			
			}
			return "redirect:/manager/packages/maintainers";
		}
		catch(PackageMaintainerEditException e)
		{
			redirectAttributes.addFlashAttribute("packagemaintainer", packageMaintainer);
			redirectAttributes.addFlashAttribute(
					"org.springframework.validation.BindingResult.packagemaintainer", result);
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			redirectAttributes.addFlashAttribute("repositories", repositoryService.findMaintainedBy(requester));
			redirectAttributes.addFlashAttribute("packages", gson.toJson(packageService.findNamesPerRepository()));
			return "redirect:/manager/packages/maintainers/{id}/edit";
		}
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/{id}/delete", method=RequestMethod.DELETE, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody HashMap<String, String> deletePackageMaintainer(@PathVariable Integer id, Principal principal)
	{	
		HashMap<String, String> result = new HashMap<String, String>();
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		PackageMaintainer packageMaintainer = packageMaintainerService.findById(id);
		try
		{
			if(packageMaintainer == null)
				result.put("error", MessageCodes.ERROR_PACKAGEMAINTAINER_NOT_FOUND);
			else if(!userService.isAuthorizedToEdit(packageMaintainer, requester))
				result.put("error", MessageCodes.ERROR_USER_NOT_AUTHORIZED);
			else
			{
				packageMaintainerService.delete(id, requester);
				result.put("success", MessageCodes.SUCCESS_PACKAGEMAINTAINER_DELETED);	
			}	
			return result;
		}
		catch(PackageMaintainerDeleteException e)
		{
			result.put("error", e.getMessage());
			return result;
		}
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/{id}/sdelete", method=RequestMethod.DELETE, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody HashMap<String, String> deletePackageMaintainer(@PathVariable Integer id)
	{	
		HashMap<String, String> result = new HashMap<String, String>();
		try
		{
			packageMaintainerService.shiftDelete(id);
			result.put("success", MessageCodes.SUCCESS_PACKAGEMAINTAINER_DELETED);	
			return result;
		}
		catch(PackageMaintainerDeleteException e)
		{
			result.put("error", e.getMessage());
			return result;
		}
	}
}

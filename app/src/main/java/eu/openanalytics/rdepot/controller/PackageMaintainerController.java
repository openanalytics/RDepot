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

import com.google.gson.Gson;

import eu.openanalytics.rdepot.exception.PackageMaintainerCreateException;
import eu.openanalytics.rdepot.exception.PackageMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.PackageMaintainerEditException;
import eu.openanalytics.rdepot.exception.PackageMaintainerNotFound;
import eu.openanalytics.rdepot.exception.UserEditException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.CreatePackageMaintainerRequestBody;
import eu.openanalytics.rdepot.model.EditPackageMaintainerRequestBody;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.PackageMaintainerService;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.RoleService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.validation.PackageMaintainerValidator;

@Controller
@RequestMapping(value= {"/manager/packages/maintainers", "/api/manager/packages/maintainers"})
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

	@Autowired
	private MessageSource messageSource;
	
	@InitBinder(value="packagemaintainer")
	private void initBinder(WebDataBinder binder) {
		binder.setValidator(packageMaintainerValidator);
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(method=RequestMethod.GET)
	public String packageMaintainersPage(Model model, Principal principal) {
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		model.addAttribute("packagemaintainers", packageMaintainerService.findByRequester(requester));
		model.addAttribute("role", requester.getRole().getValue());

		return "packagemaintainers";
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/list", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<PackageMaintainer> packageMaintainers(Principal principal) {
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		return packageMaintainerService.findByRequester(requester);
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/deleted", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<PackageMaintainer> deletedPackageMaintainers() {
		return packageMaintainerService.findByDeleted(true);
	}

	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/create", method=RequestMethod.GET)
	public @ResponseBody HashMap<String, Object> newPackageMaintainerDialog(Principal principal) {
		HashMap<String, Object> result = new HashMap<>();
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		
		result.put("packagemaintainer", new PackageMaintainer());
		result.put("users", userService.findEligiblePackageMaintainers());
		result.put("repositories", repositoryService.findMaintainedBy(requester, false));
		//result.put("packages", gson.toJson(packageService.findNamesPerRepository()));
		result.put("packages", packageService.findNamesPerRepository());
		result.put("role", requester.getRole().getValue());
		
		return result;
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/create", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody HashMap<String, Object> createNewPackageMaintainer(
			@RequestBody CreatePackageMaintainerRequestBody requestBody,
			Principal principal) {
		Locale locale = LocaleContextHolder.getLocale();
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		String packageName = requestBody.getPackageName();
		User user = userService.findById(requestBody.getUserId());
		Repository repository = repositoryService.findById(requestBody.getRepositoryId());
		
		PackageMaintainer packageMaintainer = new PackageMaintainer(0, user, repository, packageName, false);
		BindException bindingResult = new BindException(packageMaintainer, "packageMaintainer");

		try {
			if(!userService.isAuthorizedToEdit(packageMaintainer, requester))
				result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
			else {
				packageMaintainerValidator.validate(packageMaintainer, bindingResult);
				
				if(bindingResult.hasErrors()) {
					String errorCode = bindingResult.getFieldError().getCode();
					String error = messageSource.getMessage(errorCode, null, locale);
					result.put("error", error);
				} else {
					User maintainer = packageMaintainer.getUser();
					// Can a repositorymaintainer be a packagemaintainer also, in another repository then? NO!

					switch(maintainer.getRole().getName()) {
						case "user":
							userService.updateRole(maintainer, requester, roleService.getPackageMaintainerRole());
						case "packagemaintainer":
							packageMaintainerService.create(packageMaintainer, requester);
							result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGEMAINTAINER_CREATED, null, locale));
							break;
						default:
							result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_CAPABLE, null, locale));
					}
				}
				
			}
		} catch(UserEditException | PackageMaintainerCreateException e) {
			result.put("error", e.getMessage());
			result.put(
					"org.springframework.validation.BindingResult.packagemaintainer", bindingResult);
		}
		return result;
		
	}

	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/{id}/edit", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody HashMap<String, Object> updatePackageMaintainer(
			@RequestBody EditPackageMaintainerRequestBody requestBody,
			Principal principal, @PathVariable Integer id) {
		HashMap<String, Object> result = new HashMap<>();
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		PackageMaintainer packageMaintainer = packageMaintainerService.findById(id);
		Locale locale = LocaleContextHolder.getLocale();
		String packageName = requestBody.getPackageName();
		Repository repository = repositoryService.findByIdAndDeleted(requestBody.getRepositoryId(), false);
		
		packageMaintainer.setRepository(repository);
		packageMaintainer.setPackage(packageName);
		
//		BindException bindingResult = new BindException(packageMaintainer, "packageMaintainer");
//		try
//		{
//			if(packageMaintainer.getId() != id)
//				result.put("error", messageSource.getMessage(MessageCodes.ERROR_PACKAGEMAINTAINER_NOT_FOUND, null, locale));
//			else if(!userService.isAuthorizedToEdit(packageMaintainer, requester))
//				result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
//			else
//			{
//				packageMaintainerValidator.validate(packageMaintainer, bindingResult);
//				if (bindingResult.hasErrors())
//					throw new PackageMaintainerEditException(bindingResult.getFieldError().getCode());
//				else
//				{
//					packageMaintainerService.update(packageMaintainer, requester);
//					result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGEMAINTAINER_UPDATED, null, locale));
//				}			
//			}
//			return result;
//		}
//		catch(PackageMaintainerEditException e)
//		{
//			result.put("packagemaintainer", packageMaintainer);
//			result.put(
//					"org.springframework.validation.BindingResult.packagemaintainer", bindingResult);
//			result.put("error", e.getMessage());
//			result.put("repositories", repositoryService.findMaintainedBy(requester, false));
//			result.put("packages", gson.toJson(packageService.findNamesPerRepository()));
//			return result;
//		}
		
		BindException bindingResult = new BindException(packageMaintainer, "packageMaintainer");
		try {
			if(!userService.isAuthorizedToEdit(packageMaintainer, requester)) {
				result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
			} else {
				packageMaintainerValidator.validate(packageMaintainer, bindingResult);
				
				if(bindingResult.hasErrors()) {
					result.put("error", messageSource.getMessage(bindingResult.getFieldError().getCode(), null, locale));
				} else {
					packageMaintainerService.evaluateAndUpdate(packageMaintainer, requester);
					result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGEMAINTAINER_UPDATED, null, locale));
				}
			}
		} catch(PackageMaintainerNotFound | PackageMaintainerEditException e) {
			result.put("error", e.getMessage());
			result.put(
			"org.springframework.validation.BindingResult.packagemaintainer", bindingResult);
		}
		
		return result;
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/{id}/delete", method=RequestMethod.DELETE, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody HashMap<String, String> deletePackageMaintainer(@PathVariable Integer id, Principal principal)
	{	
		HashMap<String, String> result = new HashMap<String, String>();
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		Locale locale = LocaleContextHolder.getLocale();
//		PackageMaintainer packageMaintainer = packageMaintainerService.findById(id);
//		try
//		{
//			if(packageMaintainer == null)
//				result.put("error", messageSource.getMessage(MessageCodes.ERROR_PACKAGEMAINTAINER_NOT_FOUND, null, locale));
//			else if(!userService.isAuthorizedToEdit(packageMaintainer, requester))
//				result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_AUTHORIZED, null, locale));
//			else
//			{
//				packageMaintainerService.delete(id, requester);
//				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGEMAINTAINER_DELETED, null, locale));	
//			}	
//			return result;
//		}
//		catch(PackageMaintainerDeleteException e)
//		{
//			result.put("error", e.getMessage());
//			return result;
//		}
		try {
			packageMaintainerService.delete(id, requester);
			result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGEMAINTAINER_DELETED, null, locale));
		} catch (PackageMaintainerDeleteException | PackageMaintainerNotFound e) {
			result.put("error", e.getMessage());
		}
		return result;
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/{id}/sdelete", method=RequestMethod.DELETE, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody HashMap<String, String> deletePackageMaintainer(@PathVariable Integer id)
	{	
		Locale locale = LocaleContextHolder.getLocale();
		HashMap<String, String> result = new HashMap<String, String>();
		try {
			packageMaintainerService.shiftDelete(id);
			result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGEMAINTAINER_DELETED, null, locale));	
		}
		catch(PackageMaintainerDeleteException e) {
			result.put("error", e.getMessage());
		} catch (PackageMaintainerNotFound e) {
			result.put("error", e.getMessage());
		}
		
		return result;
	}
}

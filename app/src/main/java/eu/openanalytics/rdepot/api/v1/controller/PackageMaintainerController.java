/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import eu.openanalytics.rdepot.exception.PackageMaintainerCreateException;
import eu.openanalytics.rdepot.exception.PackageMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.PackageMaintainerEditException;
import eu.openanalytics.rdepot.exception.PackageMaintainerNotFound;
import eu.openanalytics.rdepot.exception.UserEditException;
import eu.openanalytics.rdepot.exception.UserUnauthorizedException;
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

	private static final Logger logger = LoggerFactory.getLogger(PackageMaintainerController.class);
	
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
		result.put("packages", packageService.findNamesPerRepository());
		result.put("role", requester.getRole().getValue());
		
		return result;
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/create", method=RequestMethod.POST, 
		consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<HashMap<String, Object>> createNewPackageMaintainer(
			@RequestBody CreatePackageMaintainerRequestBody requestBody,
			Principal principal) throws UserUnauthorizedException {
		Locale locale = LocaleContextHolder.getLocale();
		HashMap<String, Object> result = new HashMap<String, Object>();
		HttpStatus httpStatus = HttpStatus.OK;
		
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		String packageName = requestBody.getPackageName();
		User user = userService.findById(requestBody.getUserId());
		Repository repository = repositoryService.findById(requestBody.getRepositoryId());
		
		PackageMaintainer packageMaintainer = new PackageMaintainer(0, user, repository, packageName, false);
		BindException bindingResult = new BindException(packageMaintainer, "packageMaintainer");
		
		try {
			if(requester == null || !userService.isAuthorizedToEdit(packageMaintainer, requester)) {
				throw new UserUnauthorizedException(messageSource, locale);
			} else {
				packageMaintainerValidator.validate(packageMaintainer, bindingResult);
				
				if(bindingResult.hasErrors()) {
					String errorCode = bindingResult.getFieldError().getCode();
					String error = messageSource.getMessage(errorCode, null, locale);
					result.put("error", error);
					
					httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
				} else {
					User maintainer = packageMaintainer.getUser();
					// Can a repositorymaintainer be a packagemaintainer also, in another repository then? NO!
					switch(maintainer.getRole().getName()) {
						case "user":
							userService.updateRole(maintainer, requester, roleService.getPackageMaintainerRole());
						case "packagemaintainer":
							packageMaintainerService.create(packageMaintainer, requester);												
							
							result.put("success", messageSource.getMessage(
									MessageCodes.SUCCESS_PACKAGEMAINTAINER_CREATED, null, 
									MessageCodes.SUCCESS_PACKAGEMAINTAINER_CREATED, locale));
							break;
						default:
							result.put("error", messageSource.getMessage(
									MessageCodes.ERROR_USER_NOT_CAPABLE, null, 
									MessageCodes.ERROR_USER_NOT_CAPABLE, locale));
							httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
					}
				}
				
			}
		} catch(UserEditException | PackageMaintainerCreateException e) {
			result.put("error", e.getMessage());
			result.put(
					"org.springframework.validation.BindingResult.packagemaintainer", bindingResult);
		}
		return new ResponseEntity<>(result, httpStatus);
		
	}

	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/{id}/edit", method=RequestMethod.POST, 
	consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<HashMap<String, Object>> updatePackageMaintainer(
			@RequestBody EditPackageMaintainerRequestBody requestBody,
			Principal principal, @PathVariable Integer id) throws UserUnauthorizedException, PackageMaintainerNotFound {
		HashMap<String, Object> result = new HashMap<>();
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		PackageMaintainer packageMaintainer = packageMaintainerService.findByIdAndDeleted(id, false);
		Locale locale = LocaleContextHolder.getLocale();
		String packageName = requestBody.getPackageName();
		Repository repository = repositoryService.findByIdAndDeleted(requestBody.getRepositoryId(), false);
		HttpStatus httpStatus = HttpStatus.OK;
		
		if(packageMaintainer == null) {
			throw new PackageMaintainerNotFound(id, messageSource, locale);
		}
		
		//TODO: This is a very temporary solution; Eventually I'd try to get rid of all "requestBodies"
		PackageMaintainer updatedPackageMaintainer = new PackageMaintainer();
		updatedPackageMaintainer.setPackage(packageName);
		updatedPackageMaintainer.setRepository(repository);
		updatedPackageMaintainer.setUser(packageMaintainer.getUser());
		
		BindException bindingResult = new BindException(packageMaintainer, "packageMaintainer");
		if(!userService.isAuthorizedToEdit(packageMaintainer, requester)) {
			throw new UserUnauthorizedException(messageSource, locale);
		} else {
			try {
				packageMaintainerValidator.validate(updatedPackageMaintainer, bindingResult);
				
				if(bindingResult.hasErrors()) {
					result.put("error", messageSource.getMessage(bindingResult.getFieldError().getCode(), null, 
							bindingResult.getFieldError().getCode(), locale));
					httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
				} else {
					packageMaintainerService.evaluateAndUpdate(packageMaintainer, updatedPackageMaintainer, requester);
					result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGEMAINTAINER_UPDATED, null, locale));
				}
				
			} catch(PackageMaintainerEditException e) {
				result.put("error", e.getMessage());
				result.put(
				"org.springframework.validation.BindingResult.packagemaintainer", bindingResult); //do we need binding result?
				httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			}
		}
		return new ResponseEntity<>(result, httpStatus);
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/{id}/delete", method=RequestMethod.DELETE, produces="application/json")
	public @ResponseBody ResponseEntity<HashMap<String, String>> deletePackageMaintainer(
			@PathVariable Integer id, Principal principal) 
					throws PackageMaintainerNotFound, UserUnauthorizedException, 
					PackageMaintainerDeleteException {	
		HashMap<String, String> result = new HashMap<String, String>();
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		Locale locale = LocaleContextHolder.getLocale();
		PackageMaintainer packageMaintainer = packageMaintainerService.findById(id);
		HttpStatus httpStatus = HttpStatus.OK;
		
		if(packageMaintainer == null) {
			throw new PackageMaintainerNotFound(id, messageSource, locale);
		} else if(!userService.isAuthorizedToEdit(packageMaintainer, requester)) {
			throw new UserUnauthorizedException(messageSource, locale);
		} else {
			packageMaintainerService.delete(packageMaintainer, requester);
			result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGEMAINTAINER_DELETED, null, 
					MessageCodes.SUCCESS_PACKAGEMAINTAINER_DELETED, locale));
		}
		
		return new ResponseEntity<>(result, httpStatus);
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/{id}/sdelete", method=RequestMethod.DELETE, produces="application/json")
	public @ResponseBody ResponseEntity<HashMap<String, String>> deletePackageMaintainer(
			@PathVariable Integer id) throws PackageMaintainerDeleteException, PackageMaintainerNotFound {	
		PackageMaintainer packageMaintainer = packageMaintainerService.findByIdAndDeleted(id, true);
		Locale locale = LocaleContextHolder.getLocale();
		HttpStatus httpStatus = HttpStatus.OK;
		
		HashMap<String, String> result = new HashMap<String, String>();
		
		if(packageMaintainer == null) {
			throw new PackageMaintainerNotFound(id, messageSource, locale);
		} else {
			packageMaintainerService.shiftDelete(packageMaintainer);
			result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGEMAINTAINER_DELETED, 
					null, MessageCodes.SUCCESS_PACKAGEMAINTAINER_DELETED, locale));	
		}
		return new ResponseEntity<>(result, httpStatus);
	}
}

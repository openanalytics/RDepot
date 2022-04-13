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
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import eu.openanalytics.rdepot.exception.RepositoryMaintainerCreateException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerEditException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerNotFound;
import eu.openanalytics.rdepot.exception.UserEditException;
import eu.openanalytics.rdepot.exception.UserUnauthorizedException;
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
public class RepositoryMaintainerController {
	
	private static final Logger logger = LoggerFactory.getLogger(RepositoryMaintainerController.class);
	
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
	private void initBinder(WebDataBinder binder) {
		binder.setValidator(repositoryMaintainerValidator);
	}
	
	private int placeholder = 9;
	
	@RequestMapping(method=RequestMethod.GET)
	public String repositoryMaintainersPage(Model model, Principal principal) {
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
	public @ResponseBody ResponseEntity<HashMap<String, Object>> createNewRepositoryMaintainer(
			@RequestBody CreateRepositoryMaintainerRequestBody requestBody,
			Principal principal) throws UserUnauthorizedException {
		Locale locale = LocaleContextHolder.getLocale();
		HashMap<String, Object> result = new HashMap<>();
		User requester = userService.findByLogin(principal.getName());

		User user = userService.findById(requestBody.getUserId());
		Repository repository = repositoryService.findById(requestBody.getRepositoryId());
		
		HttpStatus httpStatus = HttpStatus.OK;
		
		RepositoryMaintainer repositoryMaintainer = new RepositoryMaintainer(0, user, repository, false);
		BindException bindingResult = new BindException(repositoryMaintainer, "repositoryMaintainer");	
		
		try {
			repositoryMaintainerValidator.validate(repositoryMaintainer, bindingResult);
			if(requester == null || !Objects.equals(requester.getRole().getName(), "admin")) {
				throw new UserUnauthorizedException(messageSource, locale);
			} else if(bindingResult.hasErrors()) {
				result.put("error", bindingResult.getFieldError().getCode());
				result.put("repositorymaintainer", repositoryMaintainer);
				result.put("org.springframework.validation.BindingResult.repositorymaintainer", bindingResult);
				
				httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			} else {
				User maintainer = repositoryMaintainer.getUser();
				switch(maintainer.getRole().getName()) {
				case "user":
					userService.updateRole(maintainer, requester, roleService.getRepositoryMaintainerRole());
				case "repositorymaintainer":
					repositoryMaintainerService.create(repositoryMaintainer, requester);									
					
					result.put("success", messageSource.getMessage(
							MessageCodes.SUCCESS_REPOSITORYMAINTAINER_CREATED, null, locale));
					break;
				default:
					result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_CAPABLE, null, 
							MessageCodes.ERROR_USER_NOT_CAPABLE, locale));
					httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
				}
			}
		} catch(RepositoryMaintainerCreateException | UserEditException e) {
			result.put("error", e.getMessage());
			result.put("users", userService.findEligibleRepositoryMaintainers());
			result.put("repositories", repositoryService.findAll());
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(result, httpStatus);
	}
	
	@RequestMapping(value="/{id}/edit", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<HashMap<String, Object>> updateRepositoryMaintainer(
			@RequestBody EditRepositoryMaintainerRequestBody requestBody,
			@PathVariable Integer id,
			Principal principal) throws RepositoryMaintainerNotFound, UserUnauthorizedException, 
					RepositoryMaintainerEditException {
		HashMap<String, Object> result = new HashMap<>();
		HttpStatus httpStatus = HttpStatus.OK;
		
		Repository repository = repositoryService.findById(requestBody.getRepositoryId());
		RepositoryMaintainer repositoryMaintainer = repositoryMaintainerService.findById(id);
		repositoryMaintainer.setRepository(repository);
		Locale locale = LocaleContextHolder.getLocale();
		BindException bindingResult = new BindException(repositoryMaintainer, "repositoryMaintainer");
		User requester = userService.findByLogin(principal.getName());
		
		if(repositoryMaintainer.getId() != id || repositoryMaintainer == null) {
			throw new RepositoryMaintainerNotFound(id, messageSource, locale);
		} else if(requester == null || !Objects.equals(requester.getRole().getName(), "admin")) {
			throw new UserUnauthorizedException(messageSource, locale);
		} else {	
			repositoryMaintainerValidator.validate(repositoryMaintainer, bindingResult);
			if (bindingResult.hasErrors()) {
				result.put("error", bindingResult.getFieldError().getCode());
				result.put("repositorymaintainer", repositoryMaintainer);
				result.put(
					"org.springframework.validation.BindingResult.repositorymaintainer", bindingResult);
				result.put("users", userService.findEligibleRepositoryMaintainers());
				result.put("repositories", repositoryService.findAll());
				httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			} else {
				repositoryMaintainerService.update(repositoryMaintainer, requester);
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_UPDATED, null, 
						MessageCodes.SUCCESS_REPOSITORYMAINTAINER_UPDATED, locale));
			}
		}
		
		return new ResponseEntity<>(result, httpStatus);
	}

	@RequestMapping(value="/list", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<RepositoryMaintainer> repositoryMaintainers() {
		return repositoryMaintainerService.findAll();
	}
	
	@RequestMapping(value="/deleted", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<RepositoryMaintainer> deletedRepositoryMaintainers() {
		return repositoryMaintainerService.findByDeleted(true);
	}
	
	@RequestMapping(value="/{id}/delete", method=RequestMethod.DELETE, produces="application/json")
	public @ResponseBody ResponseEntity<HashMap<String, String>> deleteRepositoryMaintainer(
			@PathVariable Integer id, Principal principal) 
					throws RepositoryMaintainerDeleteException, RepositoryMaintainerNotFound, 
					UserUnauthorizedException {	
		HashMap<String, String> result = new HashMap<String, String>();
		User requester = userService.findByLogin(principal.getName());
		Locale locale = LocaleContextHolder.getLocale();
		RepositoryMaintainer repositoryMaintainer = repositoryMaintainerService.findByIdAndDeleted(id, false);
		HttpStatus httpStatus = HttpStatus.OK;
		
		if(requester == null || !Objects.equals(requester.getRole().getName(), "admin")) {
			throw new UserUnauthorizedException(messageSource, locale);
		} else if(repositoryMaintainer == null) {
			throw new RepositoryMaintainerNotFound(id, messageSource, locale);
		} else {
			repositoryMaintainerService.delete(repositoryMaintainer, requester);
			result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_DELETED, 
					null, MessageCodes.SUCCESS_REPOSITORYMAINTAINER_DELETED, locale));
		}	
		
		return new ResponseEntity<>(result, httpStatus);
	}
	
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value="/{id}/sdelete", method=RequestMethod.DELETE, produces="application/json")
	public @ResponseBody ResponseEntity<HashMap<String, String>> shiftDeleteRepositoryMaintainer(@PathVariable Integer id) throws RepositoryMaintainerNotFound {	
		Locale locale = LocaleContextHolder.getLocale();
		HashMap<String, String> result = new HashMap<String, String>();
		HttpStatus httpStatus = HttpStatus.OK;
		RepositoryMaintainer repositoryMaintainer = repositoryMaintainerService.findByIdAndDeleted(id, true);
		
		if(repositoryMaintainer == null) {
			throw new RepositoryMaintainerNotFound(id, messageSource, locale);
		} else {
			repositoryMaintainerService.shiftDelete(repositoryMaintainer);
			result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_DELETED, null, 
					MessageCodes.SUCCESS_REPOSITORYMAINTAINER_DELETED, locale));	
		}

		return new ResponseEntity<>(result, httpStatus);
	}
}

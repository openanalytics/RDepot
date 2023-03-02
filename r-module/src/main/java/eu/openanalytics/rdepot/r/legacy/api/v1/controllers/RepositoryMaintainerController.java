/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
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

import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.deletion.RepositoryMaintainerDeleter;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.RoleService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.factory.StrategyFactory;
import eu.openanalytics.rdepot.base.utils.specs.RepositoryMaintainerSpecs;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import eu.openanalytics.rdepot.base.validation.LegacyRepositoryMaintainerValidator;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.CreateRepositoryMaintainerRequestBody;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.DTOConverter;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.EditRepositoryMaintainerRequestBody;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.RepositoryMaintainerV1Dto;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.UserV1Dto;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.RepositoryMaintainerNotFound;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.UserUnauthorizedException;
import eu.openanalytics.rdepot.r.services.RRepositoryService;

@Controller
@RequestMapping(value= {"/manager/repositories/maintainers", "/api/manager/repositories/maintainers"})
@PreAuthorize("hasAuthority('admin')")
public class RepositoryMaintainerController {
	@Autowired
	private RRepositoryService repositoryService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private RepositoryMaintainerService repositoryMaintainerService;
	
	@Autowired
	private LegacyRepositoryMaintainerValidator legacyRepositoryMaintainerValidator;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private RepositoryMaintainerDeleter repositoryMaintainerDeleter;
	
	@Autowired
	StrategyFactory factory;
	
	@InitBinder(value="repositorymaintainer")
	private void initBinder(WebDataBinder binder) {
		binder.setValidator(legacyRepositoryMaintainerValidator);
	}
	
	private int placeholder = 9;
	
	@RequestMapping(method=RequestMethod.GET)
	public String repositoryMaintainersPage(Model model, Principal principal) {
		model.addAttribute("repositorymaintainers", DTOConverter.convertRepositoryMaintainers(repositoryMaintainerService.findByDeleted(false)));
		model.addAttribute("role", placeholder);

		return "repositorymaintainers";
	}
	
	@RequestMapping(value="/create", method=RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, Object> newRepositoryMaintainerDialog() {
		Map<String, Object> result = new HashMap<>();
		
		List<RRepository> repositoryEntities = repositoryService.findByDeleted(false);
		
		result.put("repositorymaintainer", new RepositoryMaintainerV1Dto());
		result.put("users", findEligibleRepositoryMaintainers());
		result.put("repositories", DTOConverter.convertRepositories(repositoryEntities));
		result.put("role", placeholder);
		return result;
	}
	
	private List<UserV1Dto> findEligibleRepositoryMaintainers() {
		List<User> maintainers = new ArrayList<>();
		Role userRole = roleService.findByValue(Role.VALUE.USER).orElse(null);
		Role packageMaintainerRole = roleService.findByValue(Role.VALUE.PACKAGEMAINTAINER).orElse(null);
		Role repositoryMaintainerRole = roleService.findByValue(Role.VALUE.REPOSITORYMAINTAINER).orElse(null);
		
		if(userRole == null || packageMaintainerRole == null) {
			throw new IllegalStateException("Required role not found!");
		}
		
		maintainers.addAll(userService.findByRole(userRole));
		maintainers.addAll(userService.findByRole(packageMaintainerRole));
		maintainers.addAll(userService.findByRole(repositoryMaintainerRole));
		
		return DTOConverter.convertUsers(maintainers);
	}

	@RequestMapping(value="/create", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<HashMap<String, Object>> createNewRepositoryMaintainer(
			@RequestBody CreateRepositoryMaintainerRequestBody requestBody,
			Principal principal) throws UserUnauthorizedException {
		Locale locale = LocaleContextHolder.getLocale();
		HashMap<String, Object> result = new HashMap<>();
		User requester = userService.findByLogin(principal.getName()).orElse(null);

		User user = userService.findById(requestBody.getUserId()).orElse(null);
		RRepository repository = repositoryService.findById(requestBody.getRepositoryId()).orElse(null);
		
		HttpStatus httpStatus = HttpStatus.OK;
		
		RepositoryMaintainer repositoryMaintainer = new RepositoryMaintainer(0, user, repository, false);
		BindException bindingResult = new BindException(repositoryMaintainer, "repositoryMaintainer");

		try {
			legacyRepositoryMaintainerValidator.validate(repositoryMaintainer, bindingResult);
			if(requester == null || !Objects.equals(requester.getRole().getName(), "admin")) {
				throw new UserUnauthorizedException();
			} else if(bindingResult.hasErrors()) {
				result.put("error", bindingResult.getFieldError().getCode());
				result.put("repositorymaintainer", new RepositoryMaintainerV1Dto(repositoryMaintainer));
				result.put("org.springframework.validation.BindingResult.repositorymaintainer", bindingResult);
				
				httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			} else {
				User maintainer = repositoryMaintainer.getUser();
				switch(maintainer.getRole().getName()) {
				case "user":
					User updatedUser = new User(maintainer);
					Role newRole = roleService.findByValue(Role.VALUE.REPOSITORYMAINTAINER).orElse(null);
					if(newRole == null)
						throw new IllegalStateException("Could not find repository maintainer role in the db!");
					updatedUser.setRole(newRole);
					
					factory.updateUserStrategy(maintainer, requester, updatedUser).perform();
				case "repositorymaintainer":
					factory.createRepositoryMaintainerStrategy(repositoryMaintainer, requester).perform();
					result.put("success", messageSource.getMessage(
							MessageCodes.SUCCESS_REPOSITORYMAINTAINER_CREATED, null, locale));
					break;
				default:
					result.put("error", messageSource.getMessage(MessageCodes.ERROR_USER_NOT_CAPABLE, null, 
							MessageCodes.ERROR_USER_NOT_CAPABLE, locale));
					httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
				}
			}
		} catch(StrategyFailure e) {
			List<RRepository> repositoryEntities = repositoryService.findAll();
			result.put("error", e.getMessage());
			result.put("users", findEligibleRepositoryMaintainers());
			result.put("repositories", DTOConverter.convertRepositories(repositoryEntities));
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(result, httpStatus);
	}
	
	@RequestMapping(value="/{id}/edit", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<HashMap<String, Object>> updateRepositoryMaintainer(
			@RequestBody EditRepositoryMaintainerRequestBody requestBody,
			@PathVariable Integer id,
			Principal principal) throws UserUnauthorizedException, RepositoryMaintainerNotFound {
		HashMap<String, Object> result = new HashMap<>();
		HttpStatus httpStatus = HttpStatus.OK;
		
		RRepository repository = repositoryService.findById(requestBody.getRepositoryId()).orElse(null);
		RepositoryMaintainer repositoryMaintainer = repositoryMaintainerService.findById(id).orElse(null);
		RepositoryMaintainer updatedRepositoryMaintainer = new RepositoryMaintainer(repositoryMaintainer);
		updatedRepositoryMaintainer.setRepository(repository);
		Locale locale = LocaleContextHolder.getLocale();
		BindException bindingResult = new BindException(repositoryMaintainer, "repositoryMaintainer");
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		
		if(repositoryMaintainer.getId() != id || repositoryMaintainer == null) {
			throw new RepositoryMaintainerNotFound();
		} else if(requester == null || !Objects.equals(requester.getRole().getName(), "admin")) {
			throw new UserUnauthorizedException();
		} else {	
			legacyRepositoryMaintainerValidator.validate(repositoryMaintainer, bindingResult);
			if (bindingResult.hasErrors()) {
				List<RRepository> repositoryEntities = repositoryService.findAll();
				result.put("error", bindingResult.getFieldError().getCode());
				result.put("repositorymaintainer", new RepositoryMaintainerV1Dto(repositoryMaintainer));
				result.put(
					"org.springframework.validation.BindingResult.repositorymaintainer", bindingResult);
				result.put("users", findEligibleRepositoryMaintainers());
				result.put("repositories", DTOConverter.convertRepositories(repositoryEntities));
				httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
			} else {
				try {
					factory.updateRepositoryMaintainerStrategy(repositoryMaintainer, requester, updatedRepositoryMaintainer).perform();
					result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_UPDATED, null, 
							MessageCodes.SUCCESS_REPOSITORYMAINTAINER_UPDATED, locale));
				} catch (StrategyFailure e) {
					result.put("error", e.getMessage());
					httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
				}
			}
		}
		
		return new ResponseEntity<>(result, httpStatus);
	}

	@RequestMapping(value="/list", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<RepositoryMaintainerV1Dto> repositoryMaintainers() {				
		Specification<RepositoryMaintainer> deletedComponent = RepositoryMaintainerSpecs.isDeleted(false);
		Specification<RepositoryMaintainer> specification = SpecificationUtils.andComponent(null, deletedComponent);
	        
		List<RepositoryMaintainer> entities = repositoryMaintainerService.findSortedBySpecification(specification, Sort.by(Direction.ASC, "id"));
		
		return DTOConverter.convertRepositoryMaintainers(entities);
	}
	
	@RequestMapping(value="/deleted", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<RepositoryMaintainerV1Dto> deletedRepositoryMaintainers() {
		List<RepositoryMaintainer> repositoryMaintainersEntities = repositoryMaintainerService.findAllDeleted();
		return DTOConverter.convertRepositoryMaintainers(repositoryMaintainersEntities);
	}
	
	@RequestMapping(value="/{id}/delete", method=RequestMethod.DELETE, produces="application/json")
	public @ResponseBody ResponseEntity<HashMap<String, String>> deleteRepositoryMaintainer(
			@PathVariable Integer id, Principal principal) 
					throws UserUnauthorizedException, RepositoryMaintainerNotFound {	
		HashMap<String, String> result = new HashMap<String, String>();
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		Locale locale = LocaleContextHolder.getLocale();
		RepositoryMaintainer repositoryMaintainer = repositoryMaintainerService.findById(id).orElse(null);
		HttpStatus httpStatus = HttpStatus.OK;
		
		if(requester == null || requester.isDeleted() || !Objects.equals(requester.getRole().getName(), "admin")) {
			throw new UserUnauthorizedException();
		} else if(repositoryMaintainer == null) {
			throw new RepositoryMaintainerNotFound();
		} else {
			try {
				RepositoryMaintainer updated = new RepositoryMaintainer(repositoryMaintainer);
				updated.setDeleted(true);
				factory.updateRepositoryMaintainerStrategy(repositoryMaintainer, requester, updated).perform();
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_DELETED, 
						null, MessageCodes.SUCCESS_REPOSITORYMAINTAINER_DELETED, locale));
			} catch (StrategyFailure e) {
				result.put("error", e.getMessage());
				httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			}
		}	
		
		return new ResponseEntity<>(result, httpStatus);
	}
	
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value="/{id}/sdelete", method=RequestMethod.DELETE, produces="application/json")
	public @ResponseBody ResponseEntity<HashMap<String, String>> shiftDeleteRepositoryMaintainer(@PathVariable Integer id) throws RepositoryMaintainerNotFound {	
		Locale locale = LocaleContextHolder.getLocale();
		HashMap<String, String> result = new HashMap<String, String>();
		HttpStatus httpStatus = HttpStatus.OK;
		RepositoryMaintainer repositoryMaintainer = repositoryMaintainerService.findById(id).orElse(null);
		
		if(repositoryMaintainer == null || !repositoryMaintainer.isDeleted()) {
			throw new RepositoryMaintainerNotFound();
		} else {
			try {
				repositoryMaintainerDeleter.delete(repositoryMaintainer);
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_REPOSITORYMAINTAINER_DELETED, null, 
						MessageCodes.SUCCESS_REPOSITORYMAINTAINER_DELETED, locale));	
			} catch (DeleteEntityException e) {
				result.put("error", e.getMessage());
				httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			}
			
		}

		return new ResponseEntity<>(result, httpStatus);
	}
}

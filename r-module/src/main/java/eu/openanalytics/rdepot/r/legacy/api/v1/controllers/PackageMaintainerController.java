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
package eu.openanalytics.rdepot.r.legacy.api.v1.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
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

import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.deletion.PackageMaintainerDeleter;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RoleService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.factory.StrategyFactory;
import eu.openanalytics.rdepot.base.validation.PackageMaintainerValidator;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.CreatePackageMaintainerRequestBody;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.DTOConverter;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.EditPackageMaintainerRequestBody;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.PackageMaintainerV1Dto;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.RepositoryWithPackagesProjection;
import eu.openanalytics.rdepot.r.legacy.api.v1.dtos.UserV1Dto;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.PackageMaintainerNotFound;
import eu.openanalytics.rdepot.r.legacy.api.v1.exceptions.UserUnauthorizedException;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.services.RRepositoryService;

@Controller
@RequestMapping(value= {"/manager/packages/maintainers", "/api/manager/packages/maintainers"})
public class PackageMaintainerController {

	@Autowired
	private RRepositoryService repositoryService;
	
	@Autowired
	private RPackageService packageService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private PackageMaintainerService packageMaintainerService;
	
	@Autowired
	private PackageMaintainerValidator packageMaintainerValidator;
	
	@Autowired
	private PackageMaintainerDeleter packageMaintainerDeleter;

	@Autowired
	private StrategyFactory strategyFactory;
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private SecurityMediator securityMediator;
	
	Logger logger = LoggerFactory.getLogger(PackageMaintainerController.class);
	
	@InitBinder(value="packagemaintainer")
	private void initBinder(WebDataBinder binder) {
		binder.setValidator(packageMaintainerValidator);
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(method=RequestMethod.GET)
	public String packageMaintainersPage(Model model, Principal principal) {
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		model.addAttribute("packagemaintainers", DTOConverter.convertPackageMaintainers(packageMaintainerService.findByRequester(requester)));
		model.addAttribute("role", requester.getRole().getValue());

		return "packagemaintainers";
	}
	
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/list", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<PackageMaintainerV1Dto> packageMaintainers(Principal principal) {
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		  List<PackageMaintainer> entities = packageMaintainerService.findByRequesterAndNotDeleted(requester);
		  return DTOConverter.convertPackageMaintainers(entities);
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/deleted", method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody List<PackageMaintainerV1Dto> deletedPackageMaintainers() {
		List<PackageMaintainer> entities = packageMaintainerService.findByDeleted(Pageable.unpaged(), true).toList();
		return DTOConverter.convertPackageMaintainers(entities);
	}

	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@RequestMapping(value="/create", method=RequestMethod.GET)
	public @ResponseBody Map<String, Object> newPackageMaintainerDialog(Principal principal) {
		Map<String, Object> result = new HashMap<>();
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		Map<String, List<String>> packages = new HashMap<>();		
		Comparator<RepositoryWithPackagesProjection> sortRepositoryWithPackagesProjectionById
		 = Comparator.comparing(RepositoryWithPackagesProjection::getId);
		packageService.findAllNonDeletedAndAccepted().forEach(p -> {
			List<String> repo = packages.get(p.getRepository().getName());
			
			if(repo == null && !p.getRepository().isDeleted()) {
				List<String> newPackageList = new ArrayList<>();
				packages.put(p.getRepository().getName(), newPackageList.stream().sorted().collect(Collectors.toList()));
				repo = newPackageList;
			}
			repo.add(p.getName());
		});			
		
		result.put("packagemaintainer", new PackageMaintainerV1Dto());
		result.put("users", findEligiblePackageMaintainers());
		result.put("repositories", findMaintainedRepositoriesBy(requester)
				.stream()
				.map(RepositoryWithPackagesProjection::of)
				.sorted(sortRepositoryWithPackagesProjectionById)
				.collect(Collectors.toList()));
		result.put("packages", packages);
		result.put("role", requester.getRole().getValue());
		
		return result;
	}
	
	private List<UserV1Dto> findEligiblePackageMaintainers() {
		List<User> maintainers = new ArrayList<>();
		Role userRole = roleService.findByValue(Role.VALUE.USER).orElse(null);
		Role packageMaintainerRole = roleService.findByValue(Role.VALUE.PACKAGEMAINTAINER).orElse(null);
		
		if(userRole == null || packageMaintainerRole == null) {
			throw new IllegalStateException("Required role not found!");
		}
		
		maintainers.addAll(userService.findByRole(userRole));
		maintainers.addAll(userService.findByRole(packageMaintainerRole));
		
		return DTOConverter.convertUsers(maintainers);
	}
	
	private List<RRepository> findMaintainedRepositoriesBy(User user) {
		switch(user.getRole().getName()) {
		case "admin":
			return repositoryService.findByDeleted(false);
		case "repositorymaintainer":
			List<RRepository> repositories = new ArrayList<RRepository>();
			for(RepositoryMaintainer repositoryMaintainer : user.getRepositoryMaintainers()) {
				if(!repositoryMaintainer.isDeleted() && !repositoryMaintainer.getRepository().isDeleted())
					repositories.add((RRepository) repositoryMaintainer.getRepository());
			}
			return repositories;
		}
				
		return List.of();
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
		
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		String packageName = requestBody.getPackageName();
		User user = userService.findById(requestBody.getUserId()).orElse(null);
		RRepository repository = repositoryService.findById(requestBody.getRepositoryId()).orElse(null);
		
		PackageMaintainer packageMaintainer = new PackageMaintainer(0, user, repository, packageName, false);
		BindException bindingResult = new BindException(packageMaintainer, "packageMaintainer");

		try {
			if(requester == null || !securityMediator.isAuthorizedToEdit(packageMaintainer, requester)) {
				throw new UserUnauthorizedException();
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
							Role newRole = roleService.findByValue(Role.VALUE.PACKAGEMAINTAINER).orElse(null);
							User updatedUser = new User(maintainer);
							updatedUser.setRole(newRole);
							
							strategyFactory.updateUserStrategy(maintainer, requester, updatedUser).perform();
						case "packagemaintainer":
							strategyFactory.createPackageMaintainerStrategy(packageMaintainer, requester).perform();
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
		} catch(StrategyFailure e) {
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
			Principal principal, @PathVariable Integer id) throws PackageMaintainerNotFound, UserNotAuthorized {
		HashMap<String, Object> result = new HashMap<>();
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		PackageMaintainer packageMaintainer = packageMaintainerService.findById(id).orElse(null);
		Locale locale = LocaleContextHolder.getLocale();
		String packageName = requestBody.getPackageName();
		RRepository repository = repositoryService.findById(requestBody.getRepositoryId()).orElse(null);
		
		if(repository.isDeleted())
			repository = null;
		
		HttpStatus httpStatus = HttpStatus.OK;
		
		if(packageMaintainer == null || packageMaintainer.isDeleted()) {
			throw new PackageMaintainerNotFound();
		}
		
		//TODO: This is a very temporary solution; Eventually I'd try to get rid of all "requestBodies"
		PackageMaintainer updatedPackageMaintainer = new PackageMaintainer();
		updatedPackageMaintainer.setPackageName(packageName);
		updatedPackageMaintainer.setRepository(repository);
		updatedPackageMaintainer.setUser(packageMaintainer.getUser());
		
		BindException bindingResult = new BindException(packageMaintainer, "packageMaintainer");
		if(!securityMediator.isAuthorizedToEdit(packageMaintainer, requester)) {
			throw new UserNotAuthorized(messageSource, locale);
		} else {
			try {
				packageMaintainerValidator.validate(updatedPackageMaintainer, bindingResult);
				
				if(bindingResult.hasErrors()) {
					result.put("error", messageSource.getMessage(bindingResult.getFieldError().getCode(), null, 
							bindingResult.getFieldError().getCode(), locale));
					httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
				} else {
					strategyFactory.updatePackageMaintainerStrategy(
							packageMaintainer, requester, updatedPackageMaintainer)
					.perform();
//					
					result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGEMAINTAINER_UPDATED, null, locale));
				}
				
			} catch(StrategyFailure e) {
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
					throws PackageMaintainerNotFound, UserNotAuthorized {	
		HashMap<String, String> result = new HashMap<String, String>();
		User requester = userService.findByLogin(principal.getName()).orElse(null);
		Locale locale = LocaleContextHolder.getLocale();
		PackageMaintainer packageMaintainer = packageMaintainerService.findById(id).orElse(null);
		HttpStatus httpStatus = HttpStatus.OK;
		
		if(packageMaintainer == null) {
			throw new PackageMaintainerNotFound();
		} else if(!securityMediator.isAuthorizedToEdit(packageMaintainer, requester)) {
			throw new UserNotAuthorized(messageSource, locale);
		} else {
			
			PackageMaintainer updatedMaintainer = new PackageMaintainer(packageMaintainer);
			updatedMaintainer.setDeleted(true);
			
			try {
				strategyFactory
					.updatePackageMaintainerStrategy(packageMaintainer, requester, updatedMaintainer)
					.perform();
			} catch (StrategyFailure e) {
				
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
//			packageMaintainerService.delete(packageMaintainer, requester);
			result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGEMAINTAINER_DELETED, null, 
					MessageCodes.SUCCESS_PACKAGEMAINTAINER_DELETED, locale));
		}
		
		return new ResponseEntity<>(result, httpStatus);
	}
	
	@PreAuthorize("hasAuthority('admin')")
	@RequestMapping(value="/{id}/sdelete", method=RequestMethod.DELETE, produces="application/json")
	public @ResponseBody ResponseEntity<HashMap<String, String>> deletePackageMaintainer(
			@PathVariable Integer id) throws PackageMaintainerNotFound {	
		PackageMaintainer packageMaintainer = packageMaintainerService.findById(id).orElse(null);
		Locale locale = LocaleContextHolder.getLocale();
		HttpStatus httpStatus = HttpStatus.OK;
		
		HashMap<String, String> result = new HashMap<String, String>();
		
		if(packageMaintainer == null || !packageMaintainer.isDeleted()) {
			throw new PackageMaintainerNotFound();
		} else {
			try {
				packageMaintainerDeleter.delete(packageMaintainer);
				result.put("success", messageSource.getMessage(MessageCodes.SUCCESS_PACKAGEMAINTAINER_DELETED, 
						null, MessageCodes.SUCCESS_PACKAGEMAINTAINER_DELETED, locale));	
			} catch (DeleteEntityException e) {
				logger.error(e.getMessage(), e);
				result.put("error", e.getMessage());
				httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			}
		}
		return new ResponseEntity<>(result, httpStatus);
	}
}

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
package eu.openanalytics.rdepot.api.v2.controller;

import java.security.Principal;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.api.v2.dto.PackageMaintainerDto;
import eu.openanalytics.rdepot.api.v2.dto.ResponseDto;
import eu.openanalytics.rdepot.api.v2.exception.ApiException;
import eu.openanalytics.rdepot.api.v2.exception.ApplyPatchException;
import eu.openanalytics.rdepot.api.v2.exception.CreateException;
import eu.openanalytics.rdepot.api.v2.exception.DeleteException;
import eu.openanalytics.rdepot.api.v2.exception.MalformedPatchException;
import eu.openanalytics.rdepot.api.v2.exception.PackageMaintainerNotFound;
import eu.openanalytics.rdepot.api.v2.exception.UserNotAuthorized;
import eu.openanalytics.rdepot.api.v2.hateoas.PackageMaintainerModelAssembler;
import eu.openanalytics.rdepot.exception.PackageMaintainerCreateException;
import eu.openanalytics.rdepot.exception.PackageMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.PackageMaintainerException;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.PackageMaintainerService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.validation.PackageMaintainerValidator;
import jakarta.json.JsonException;
import jakarta.json.JsonPatch;

/**
 * REST Controller implementation for Package Maintainers.
 */
@RestController
@RequestMapping(value = "/api/v2/manager/r/package-maintainers")
public class ApiV2PackageMaintainerController extends ApiV2Controller<PackageMaintainer, PackageMaintainerDto> {
	
	private final UserService userService;
	private final PackageMaintainerService packageMaintainerService;
	private final PackageMaintainerValidator packageMaintainerValidator;
	
	@Autowired
	public ApiV2PackageMaintainerController(MessageSource messageSource,
			PackageMaintainerModelAssembler modelAssembler,
			PagedResourcesAssembler<PackageMaintainer> pagedModelAssembler, 
			ObjectMapper objectMapper,
			UserService userService, PackageMaintainerService packageMaintainerService,
			PackageMaintainerValidator packageMaintainerValidator) {
		
		super(messageSource, 
				LocaleContextHolder.getLocale(), 
				modelAssembler, 
				pagedModelAssembler, 
				objectMapper, 
				PackageMaintainerDto.class, 
				LoggerFactory.getLogger(ApiV2PackageMaintainerController.class),
				packageMaintainerValidator);
		this.userService = userService;
		this.packageMaintainerService = packageMaintainerService;
		this.packageMaintainerValidator = packageMaintainerValidator;
	}

	/**
	 * Fetches all maintainers.
	 * @param principal used for authorization
	 * @param pageable
	 * @param deleted filters deleted maintainers
	 * @return DTO with maintainers
	 * @throws ApiException
	 */
	@PreAuthorize("hasAuthority('user')")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseDto<PagedModel<EntityModel<PackageMaintainerDto>>> getAllMaintainers(
			Principal principal, Pageable pageable,
			@RequestParam(name = "deleted", required = false) Optional<Boolean> deleted) 
					throws ApiException {
		User requester = userService.findByLogin(principal.getName());
		
		if(requester == null)
			throw new UserNotAuthorized(messageSource, locale);
		
		Page<PackageMaintainer> pagedMaintainers;
		
		if(deleted.isPresent()) {
			pagedMaintainers = packageMaintainerService.findByRequesterAndDeleted(requester, pageable, deleted.get());
		} else {
			pagedMaintainers = packageMaintainerService.findByRequester(requester, pageable);
		}
		
		return handleSuccessForPagedCollection(pagedMaintainers);
	}
	
	/**
	 * Fetches single maintainer of given ID.
	 * @param principal used for authorization
	 * @param id
	 * @return
	 * @throws ApiException
	 */
	@PreAuthorize("hasAuthority('user')")
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<ResponseDto<EntityModel<PackageMaintainerDto>>> getMaintainer(
			Principal principal, @PathVariable Integer id) throws ApiException {
		User requester = userService.findByLogin(principal.getName());
		
		if(requester == null)
			throw new UserNotAuthorized(messageSource, locale);
		
		PackageMaintainer maintainer = packageMaintainerService.findById(id);
		if(maintainer == null)
			maintainer = packageMaintainerService.findByIdAndDeleted(id, true);
		
		if(maintainer == null)
			throw new PackageMaintainerNotFound(messageSource, locale);
		
		if(!userService.isAuthorizedToSee(maintainer, requester))
			throw new UserNotAuthorized(messageSource, locale);
		
		return handleSuccessForSingleEntity(maintainer);
	}
	
	/**
	 * Creates a single maintainer.
	 * @param principal used for authorization
	 * @param packageMaintainerDto DTO with created maintainer
	 * @param bindingResult used for validation
	 * @return DTO with created maintainer
	 * @throws ApiException
	 */
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody ResponseEntity<?> createMaintainer(
			Principal principal, @RequestBody PackageMaintainerDto packageMaintainerDto) throws ApiException {
		User requester = userService.findByLogin(principal.getName());
		
		if(requester == null)
			throw new UserNotAuthorized(messageSource, locale);
		
		PackageMaintainer packageMaintainer = packageMaintainerDto.toEntity();
		BindingResult bindingResult = createBindingResult(packageMaintainer);
		
		packageMaintainerValidator.validate(packageMaintainer, bindingResult);
		
		if(bindingResult.hasErrors())
			return handleValidationError(bindingResult);
		
		try {
			PackageMaintainer created = packageMaintainerService.create(packageMaintainer, requester);
			return handleCreatedForSingleEntity(created);
		} catch(PackageMaintainerCreateException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new CreateException(messageSource, locale);
		}
	}
	
	/**
	 * Updates package maintainer.
	 * @param principal used for authorization
	 * @param id
	 * @param jsonPatch JSON Patch array with changed properties.
	 * @param bindingResult used for validation
	 * @return
	 * @throws ApiException
	 */
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<?> updatePackageMaintainer(
			Principal principal, @PathVariable Integer id, 
			@RequestBody JsonPatch jsonPatch) throws ApiException {
		Optional<PackageMaintainer> packageMaintainer = packageMaintainerService.findByIdEvenDeleted(id);
		
		if(packageMaintainer.isEmpty())
			throw new PackageMaintainerNotFound(messageSource, locale);
		
		User requester = userService.findByLogin(principal.getName());
		if(requester == null || !userService.isAuthorizedToEdit(packageMaintainer.get(), requester)) 
			throw new UserNotAuthorized(messageSource, locale);

		PackageMaintainer updated = null;
		try {			
			PackageMaintainerDto packageMaintainerDto = applyPatchToEntity(jsonPatch, packageMaintainer.get());
			PackageMaintainer entity = packageMaintainerDto.toEntity();
			BindingResult bindingResult = createBindingResult(entity);
			
			packageMaintainerValidator.validate(entity, bindingResult);
			
			if(bindingResult.hasErrors())
				return handleValidationError(bindingResult);
			
			updated = packageMaintainerService.evaluateAndUpdate(packageMaintainerDto, requester);
		} catch(PackageMaintainerException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new ApplyPatchException(messageSource, locale);
		} catch(JsonException | JsonProcessingException e) {
			throw new MalformedPatchException(messageSource, locale, e);
		}
		
		return handleSuccessForSingleEntity(updated);
	}
	
	/**
	 * Deletes maintainer of given ID. This is a permanent delete!
	 * @param principal used for authorization
	 * @param id
	 * @return
	 * @throws ApiException
	 */
	@PreAuthorize("hasAuthority('admin')")
	@DeleteMapping(path = "/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletePackageMaintainer(Principal principal, @PathVariable Integer id) throws ApiException {
		User requester = userService.findByLogin(principal.getName());
		
		if(requester == null || !userService.isAdmin(requester)) {
			throw new UserNotAuthorized(messageSource, locale);
		}
		
		PackageMaintainer packageMaintainer = packageMaintainerService.findByIdAndDeleted(id, true);
		if(packageMaintainer == null)
			throw new PackageMaintainerNotFound(messageSource, locale);
		
		try {
			packageMaintainerService.shiftDelete(packageMaintainer);
		} catch (PackageMaintainerDeleteException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new DeleteException(messageSource, locale);
		}
	}
}

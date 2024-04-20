/*
 * RDepot
 *
 * Copyright (C) 2012-2024 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.python.api.v2.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2Controller;
import eu.openanalytics.rdepot.base.api.v2.converters.exceptions.EntityResolutionException;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.*;
import eu.openanalytics.rdepot.base.api.v2.resolvers.DtoResolvedPageable;
import eu.openanalytics.rdepot.base.api.v2.resolvers.PageableSortResolver;
import eu.openanalytics.rdepot.base.api.v2.validation.PageableValidator;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.storage.exceptions.SourceNotFoundException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.base.utils.specs.PackageSpecs;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import eu.openanalytics.rdepot.base.validation.DataSpecificValidationResult;
import eu.openanalytics.rdepot.base.validation.ValidationResultImpl;
import eu.openanalytics.rdepot.python.api.v2.converters.PythonPackageDtoConverter;
import eu.openanalytics.rdepot.python.api.v2.dtos.PythonPackageDto;
import eu.openanalytics.rdepot.python.api.v2.hateoas.PythonPackageModelAssembler;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonPackageDeleter;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import eu.openanalytics.rdepot.python.storage.implementation.PythonLocalStorage;
import eu.openanalytics.rdepot.python.strategy.factory.PythonStrategyFactory;
import eu.openanalytics.rdepot.python.validation.PythonPackageValidator;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.json.JsonException;
import jakarta.json.JsonPatch;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller implementation for Python packages.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v2/manager/python/packages")
public class PythonPackageController extends ApiV2Controller<PythonPackage, PythonPackageDto> {
	
	private final Locale locale = LocaleContextHolder.getLocale();
	private final MessageSource messageSource;
	private final PythonPackageService packageService;
	private final UserService userService;
	private final PythonPackageValidator packageValidator;
	private final PythonStrategyFactory strategyFactory;
	private final PythonPackageDeleter deleter;
	private final SecurityMediator securityMediator;
	private final PythonLocalStorage storage;
	private final PageableValidator pageableValidator;
	private final PageableSortResolver pageableSortResolver;

	public PythonPackageController(MessageSource messageSource,
			PythonPackageService packageService,
			PythonPackageModelAssembler modelAssembler,
			PagedResourcesAssembler<PythonPackage> pagedResourcesAssembler,
			ObjectMapper objectMapper,
			UserService userService,
			PythonPackageValidator packageValidator,
			PythonStrategyFactory strategyFactory,
			PythonPackageDeleter packageDeleter,
			SecurityMediator securityMediator,
			PythonPackageDtoConverter converter,
			PageableValidator pageableValidator, PythonLocalStorage storage,
			PageableSortResolver pageableSortResolver
	) {
		super(messageSource, LocaleContextHolder.getLocale(), 
				modelAssembler, pagedResourcesAssembler, objectMapper,
				PythonPackageDto.class, Optional.empty(), converter
				);
		this.packageService = packageService;
		this.messageSource = messageSource;
		this.userService = userService;
		this.packageValidator = packageValidator;
		this.strategyFactory = strategyFactory;
		this.deleter = packageDeleter;
		this.securityMediator = securityMediator;
		this.storage = storage;
		this.pageableValidator = pageableValidator;
		this.pageableSortResolver = pageableSortResolver;
	}
	
	/**
	 * Fetches all packages available for a user.
	 * @param principal used for authorization
	 * @param pageable carries parameters required for pagination
	 * @param repositories show only packages from a given repository
	 * @param deleted show only deleted packages, requires admin privileges
	 * @return
	 * @throws ApiException 
	 */
	@PreAuthorize("hasAuthority('user')")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@Operation(operationId = "getAllPythonPackages")
	public @ResponseBody ResponseDto<?> getAllPackages(
			Principal principal, @ParameterObject Pageable pageable,
			@RequestParam(name="repository", required=false) List<String> repositories,
			@RequestParam(name="deleted", required=false) Optional<Boolean> deleted,
			@RequestParam(name="submissionState", required=false, defaultValue="ACCEPTED") List<SubmissionState> submissionStates,
			@RequestParam(name="name", required=false) Optional<String> name) 
					throws ApiException {
		final User requester = userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		
		final DtoResolvedPageable resolvedPageable = pageableSortResolver.resolvePackage(pageable);
		pageableValidator.validate(PackageDto.class, resolvedPageable);
		
		Specification<PythonPackage> specification = null;
		
		if(Objects.nonNull(repositories)) {
			specification = SpecificationUtils.andComponent(specification, PackageSpecs.ofRepository(repositories));
		}
		
		if(deleted.isPresent()) {
			Specification<PythonPackage> component = PackageSpecs.isDeleted(deleted.get());
			specification = SpecificationUtils.andComponent(specification, component);

			// TODO: #32882 This is a temporary fix for 2.0; We should think of better solution.
			if(deleted.get().equals(true) && !userService.isAdmin(requester)) {
				return emptyPage();
			}
		}
		
		if(name.isPresent()) {
			Specification<PythonPackage> component = PackageSpecs.ofName(name.get());
			specification = SpecificationUtils.andComponent(specification, component);
		}
		
		if(Objects.nonNull(submissionStates)) {
			Specification<PythonPackage> component = PackageSpecs.ofSubmissionState(submissionStates);
			specification = SpecificationUtils.andComponent(specification, component);
		}
		
		if(specification == null) {
			return handleSuccessForPagedCollection(packageService.findAll(resolvedPageable));
		} else {
			return handleSuccessForPagedCollection(
					packageService.findAllBySpecification(specification, resolvedPageable));
		}
	}
	
	/**
	 * Find a package of given id. 
	 * If user is not an admin, package marked as deleted will not be found.
	 * @param principal used for authorization
	 * @param id
	 * @return Package DTO
	 * @throws PackageNotFound
	 * @throws UserNotAuthorized
	 */
	@PreAuthorize("hasAuthority('user')")
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(operationId = "getAllPythonPackageById")
	public @ResponseBody ResponseEntity<ResponseDto<EntityModel<PythonPackageDto>>> getPackageById(
			Principal principal, @PathVariable("id") Integer id) 
			throws PackageNotFound, UserNotAuthorized {
		User requester = userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));			
		PythonPackage packageBag = packageService.findById(id)
				.orElseThrow(() -> new PackageNotFound(messageSource, locale));
		if((!userService.isAdmin(requester) && packageBag.isDeleted()))
			throw new PackageNotFound(messageSource, locale);

		return handleSuccessForSingleEntity(packageBag);
	}
	
	/**
	 * Updates a package.
	 * The method follows JSON Patch standard (see <a href="https://datatracker.ietf.org/doc/html/rfc6902">specification</a>).
	 * @param principal used for authorization
	 * @param id package id
	 * @param patch JsonPatch object
	 * @return Updated Package DTO
	 */
	@PreAuthorize("hasAuthority('packagemaintainer')")
	@PatchMapping(path = "/{id}", consumes="application/json-patch+json")
	@ResponseStatus(HttpStatus.OK)
	@Operation(operationId = "updatePythonPackage")
	public @ResponseBody ResponseEntity<?> updatePackage(
				Principal principal, 
				@PathVariable("id") Integer id, 
				@RequestBody JsonPatch patch) 
			throws ApiException {			
		PythonPackage packageBag = packageService.findById(id)
				.orElseThrow(() -> new PackageNotFound(messageSource, locale)); 
		final User requester = userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

		if(!securityMediator.isAuthorizedToEdit(packageBag, requester))
			throw new UserNotAuthorized(messageSource, locale);
		
		try {
			final PythonPackageDto packageDto = applyPatchToEntity(patch, packageBag);
			final PythonPackage updatedPackage = dtoConverter.resolveDtoToEntity(packageDto);

			final DataSpecificValidationResult<Submission> validationResult = ValidationResultImpl.createDataSpecificResult(Submission.class);
			packageValidator.validate(updatedPackage, true, validationResult);
			
			if(validationResult.hasErrors())
				return handleValidationError(validationResult);
			
			Strategy<PythonPackage> strategy = strategyFactory.updatePackageStrategy(packageBag, requester, updatedPackage);
			
			packageBag = strategy.perform();					
		} catch(EntityResolutionException e) {
			log.error(e.getClass().getName() + ": " + e.getMessage(), e);
			return handleValidationError(e.getMessage());
		} catch(JsonProcessingException | JsonException e) {
			throw new MalformedPatchException(messageSource, locale, e);
		} catch (StrategyFailure e) {
			log.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new ApplyPatchException(messageSource, locale);
		}
		
		return handleSuccessForSingleEntity(packageBag);
	}
	
	/**
	 * Erases package from database and file system. Requires admin privileges.
	 * @param principal used for authorization
	 * @param id
	 * @return
	 * @throws PackageNotFound
	 * @throws DeleteException
	 * @throws UserNotAuthorized 
	 */
	@PreAuthorize("hasAuthority('admin')")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(operationId = "deletePythonPackage")
	public void shiftDeletePackage(Principal principal, @PathVariable("id") Integer id) 
			throws PackageNotFound, DeleteException, UserNotAuthorized, SynchronizeRepositoryException {		
		final PythonPackage packageBag = packageService.findOneDeleted(id)
				.orElseThrow(() -> new PackageNotFound(messageSource, locale)); 
		userService.findByLogin(principal.getName())
			.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

		try {			
			deleter.deleteAndSynchronize(packageBag);
		} catch (DeleteEntityException e) {
			log.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new DeleteException(messageSource, locale);
		} catch (SynchronizeRepositoryException e) {
			log.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new SynchronizeRepositoryException();
		}
	}

	/**
	 * This method provides a package binary file to download.
	 * @param id package ID
	 * @param name package name
	 * @param version package version
	 */
	@GetMapping(value="/{id}/download/{name}-{version}.tar.gz")
	@Operation(operationId = "downloadPythonPackage")
	public @ResponseBody ResponseEntity<byte[]> downloadPackage(@PathVariable("id") Integer id,
																@PathVariable("name") String name, 
																@PathVariable("version") String version)
			throws ApiException {
		PythonPackage packageBag = packageService.findOneNonDeleted(id).orElseThrow(()-> new PackageNotFound(messageSource, locale));
		byte[] bytes = null;
		HttpHeaders httpHeaders = new HttpHeaders();
		HttpStatus httpStatus = HttpStatus.OK;

		try {
			bytes = storage.getPackageInBytes(packageBag);
		} catch (SourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		httpHeaders.set("Content-Type", "application/gzip");
		httpHeaders.set("Content-Disposition", "attachment; filename= \""
				+ name + "-" + version + ".tar.gz\"");

		return new ResponseEntity<>(bytes, httpHeaders, httpStatus);
	}
}
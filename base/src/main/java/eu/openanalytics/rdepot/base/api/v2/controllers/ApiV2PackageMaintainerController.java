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
package eu.openanalytics.rdepot.base.api.v2.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageMaintainerDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApiException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApplyPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.CreateException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.DeleteException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.MalformedPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.PackageMaintainerNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ResolveRelatedEntitiesException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.api.v2.hateoas.PackageMaintainerModelAssembler;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.deletion.PackageMaintainerDeleter;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.CommonRepositoryService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.factory.StrategyFactory;
import eu.openanalytics.rdepot.base.utils.specs.PackageMaintainerSpecs;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import eu.openanalytics.rdepot.base.validation.PackageMaintainerValidator;
import jakarta.json.JsonException;
import jakarta.json.JsonPatch;

/**
 * REST Controller implementation for Package Maintainers.
 */
@RestController
@RequestMapping(value = "/api/v2/manager/r/package-maintainers")
public class ApiV2PackageMaintainerController extends ApiV2Controller<PackageMaintainer, PackageMaintainerDto> 
	implements ModifiableDataController<PackageMaintainer,PackageMaintainerDto> {
	
	private final UserService userService;	
	private final PackageMaintainerValidator packageMaintainerValidator;
	
	private final PackageMaintainerService packageMaintainerService;
	private final StrategyFactory factory;
	private final CommonRepositoryService repositoryService;
	private final RepositoryMaintainerService repositoryMaintainerService;
	
	private final PackageMaintainerDeleter deleter;
	private final SecurityMediator securityMediator;
	
	public ApiV2PackageMaintainerController(MessageSource messageSource,
			PackageMaintainerModelAssembler modelAssembler,
			PagedResourcesAssembler<PackageMaintainer> pagedModelAssembler, 
			ObjectMapper objectMapper,
			UserService userService, 
			PackageMaintainerValidator packageMaintainerValidator,
			PackageMaintainerService packageMaintainerService,
			StrategyFactory strategyFactory,
			PackageMaintainerDeleter packageMaintainerDeleter,
			CommonRepositoryService commonRepositoryService,
			RepositoryMaintainerService repositoryMaintainerService,
			SecurityMediator securityMediator) {
		
		super(messageSource, 
				LocaleContextHolder.getLocale(), 
				modelAssembler, 
				pagedModelAssembler, 
				objectMapper, 
				PackageMaintainerDto.class, 
				LoggerFactory.getLogger(ApiV2PackageMaintainerController.class),
				packageMaintainerValidator);
		this.userService = userService;
		this.packageMaintainerValidator = packageMaintainerValidator;
		this.packageMaintainerService = packageMaintainerService;
		this.factory = strategyFactory;
		this.deleter = packageMaintainerDeleter;
		this.repositoryService = commonRepositoryService;
		this.repositoryMaintainerService = repositoryMaintainerService;
		this.securityMediator = securityMediator;
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
		User requester = userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		
		Specification<PackageMaintainer> specification = null;
		
		if(deleted.isPresent()) {
			specification = SpecificationUtils
					.andComponent(specification, PackageMaintainerSpecs.isDeleted(deleted.get()));
		}
		
		if(requester.getRole().getValue() == Role.VALUE.REPOSITORYMAINTAINER) {
			List<Repository<?,?>> repositories = repositoryMaintainerService
					.findByUserWithoutDeleted(requester).stream()
					.map(m -> m.getRepository())
					.collect(Collectors.toList());
			
			Specification<PackageMaintainer> allowedRepositories = null;
			for(Repository<?,?> repository : repositories) {
				Specification<PackageMaintainer> criteria = PackageMaintainerSpecs.ofRepository(repository);
				allowedRepositories = SpecificationUtils.orComponent(allowedRepositories, criteria);
			}
			
			specification = SpecificationUtils
					.andComponent(specification, allowedRepositories);
		} else if(requester.getRole().getValue() == Role.VALUE.PACKAGEMAINTAINER) {
			specification = SpecificationUtils
					.andComponent(specification, PackageMaintainerSpecs.ofUser(requester));
		}
		
		if(specification != null) {
			return handleSuccessForPagedCollection(
					packageMaintainerService.findAllBySpecification(specification, pageable));
		} else {
			return handleSuccessForPagedCollection(packageMaintainerService.findAll(pageable));
		}
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
		User requester = userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		
		PackageMaintainer maintainer = packageMaintainerService.findById(id)
				.orElseThrow(() -> new PackageMaintainerNotFound(messageSource, locale)); 		
		
		
		if(!securityMediator.isAuthorizedToSee(maintainer, requester))
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
		User requester = userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		
		PackageMaintainer packageMaintainer;
		try {
			packageMaintainer = resolveDtoToEntity(packageMaintainerDto);
		} catch (ResolveRelatedEntitiesException e) {
			return handleValidationError(e);
		}
		
		if(!securityMediator.isAuthorizedToEdit(packageMaintainer, requester))
			throw new UserNotAuthorized(messageSource, locale);
		
		BindingResult bindingResult = createBindingResult(packageMaintainer);
		
		packageMaintainerValidator.validate(packageMaintainer, bindingResult);
		
		if(bindingResult.hasErrors())
			return handleValidationError(bindingResult);
		
		Strategy<PackageMaintainer> strategy = factory.createPackageMaintainerStrategy(packageMaintainer, requester);
		
		try {			
			PackageMaintainer created = strategy.perform();
			return handleCreatedForSingleEntity(created);
		} catch (StrategyFailure e) {
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
	 * @throws JsonProcessingException 
	 */
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<?> updatePackageMaintainer(
			Principal principal, @PathVariable Integer id, 
			@RequestBody JsonPatch jsonPatch) throws ApiException, JsonProcessingException {

		User requester = userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale)
						);
		PackageMaintainer packageMaintainer = packageMaintainerService.findById(id).orElseThrow(() -> new PackageMaintainerNotFound(messageSource, locale));
		
		if(!securityMediator.isAuthorizedToEdit(packageMaintainer, requester)) 
			throw new UserNotAuthorized(messageSource, locale);

		PackageMaintainer updated = null;
		try {			
			PackageMaintainerDto packageMaintainerDto = applyPatchToEntity(jsonPatch, packageMaintainer);
			PackageMaintainer entity = resolveDtoToEntity(packageMaintainerDto);

			BindingResult bindingResult = createBindingResult(entity);
			
			packageMaintainerValidator.validate(entity, bindingResult);
			
			if(bindingResult.hasErrors())
				return handleValidationError(bindingResult);
			
			Strategy<PackageMaintainer> strategy = factory.updatePackageMaintainerStrategy(packageMaintainer, requester, entity);
			
			updated = strategy.perform();
		} catch (StrategyFailure e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new ApplyPatchException(messageSource, locale);
		} catch(JsonException | JsonProcessingException e) {
			throw new MalformedPatchException(messageSource, locale, e);
		} catch (ResolveRelatedEntitiesException e) {
			return handleValidationError(e);
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
	public void deletePackageMaintainer(Principal principal, @PathVariable Integer id) 
			throws ApiException {
		Optional<User> requester = userService.findByLogin(principal.getName());
		
		if(requester.isEmpty() || requester.get().getRole().getValue() != Role.VALUE.ADMIN) {
			throw new UserNotAuthorized(messageSource, locale);
		}
		
		PackageMaintainer packageMaintainer = packageMaintainerService.findOneDeleted(id)
				.orElseThrow(() -> new PackageMaintainerNotFound(messageSource, locale)); 						
		try {
			deleter.delete(packageMaintainer);
		} catch (DeleteEntityException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new DeleteException(messageSource, locale);
		}
	}

	@Override
	public PackageMaintainer resolveDtoToEntity(PackageMaintainerDto packageMaintainerDto) throws ResolveRelatedEntitiesException {
		Repository<?,?> repository = repositoryService
				.findById(packageMaintainerDto.getRepositoryId())
				.orElseThrow(() -> new ResolveRelatedEntitiesException("repository"));
		User user = userService.findById(packageMaintainerDto.getUserId())
				.orElseThrow(() -> new ResolveRelatedEntitiesException("user"));
		PackageMaintainer entity = new PackageMaintainer(packageMaintainerDto, repository, user);
		
		return entity;
	}
}

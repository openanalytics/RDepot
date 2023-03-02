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
import java.util.Optional;

import org.slf4j.LoggerFactory;
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

import eu.openanalytics.rdepot.base.api.v2.dtos.RepositoryMaintainerDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApiException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApplyPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.CreateException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.DeleteException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.MalformedPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.RepositoryMaintainerNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ResolveRelatedEntitiesException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.api.v2.hateoas.RepositoryMaintainerModelAssembler;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.deletion.RepositoryMaintainerDeleter;
import eu.openanalytics.rdepot.base.service.CommonRepositoryService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.factory.StrategyFactory;
import eu.openanalytics.rdepot.base.validation.RepositoryMaintainerValidator;
import jakarta.json.JsonException;
import jakarta.json.JsonPatch;

/**
 * REST Controller for Repository Maintainers. All endpoints require administrator privileges.
 */
@RestController
@RequestMapping(value = "/api/v2/manager/r/repository-maintainers")
@PreAuthorize("hasAuthority('admin')")
public class ApiV2RepositoryMaintainerController 
	extends ApiV2Controller<RepositoryMaintainer, RepositoryMaintainerDto> 
	implements ModifiableDataController<RepositoryMaintainer, RepositoryMaintainerDto> {
	
	private final RepositoryMaintainerValidator repositoryMaintainerValidator;
	private final UserService userService;
	private final CommonRepositoryService repositoryService;
	
	private final RepositoryMaintainerService repositoryMaintainerService;
	private final StrategyFactory factory;
	private final RepositoryMaintainerDeleter repositoryMaintainerDeleter;
	
	public ApiV2RepositoryMaintainerController(
			MessageSource messageSource,
			RepositoryMaintainerModelAssembler modelAssembler,
			PagedResourcesAssembler<RepositoryMaintainer> pagedModelAssembler, 
			ObjectMapper objectMapper,			
			RepositoryMaintainerValidator repositoryMaintainerValidator,
			UserService userService,
			StrategyFactory factory,
			RepositoryMaintainerService repositoryMaintainerService,
			RepositoryMaintainerDeleter repositoryMaintainerDeleter,
			CommonRepositoryService repositoryService) {
		super(messageSource, LocaleContextHolder.getLocale(), 
				modelAssembler, pagedModelAssembler, objectMapper, 
				RepositoryMaintainerDto.class, 
				LoggerFactory.getLogger(ApiV2RepositoryMaintainerController.class),
				repositoryMaintainerValidator);		
		this.repositoryMaintainerValidator = repositoryMaintainerValidator;
		this.userService = userService;
		this.factory = factory;
		this.repositoryMaintainerService = repositoryMaintainerService;
		this.repositoryMaintainerDeleter = repositoryMaintainerDeleter;
		this.repositoryService = repositoryService;
	}

	/**
	 * Fetches all maintainers.
	 * @param pageable used for pagination
	 * @param deleted filters deleted maintainers
	 * @return
	 */
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseDto<PagedModel<EntityModel<RepositoryMaintainerDto>>> 
		getAllRepositoryMaintainers(Pageable pageable,
				@RequestParam(name = "deleted", required = false) Optional<Boolean> deleted) {
		Page<RepositoryMaintainer> maintainers = null;
		
		if(deleted.isPresent()) {
			maintainers = repositoryMaintainerService.findByDeleted(pageable, deleted.get());
		} else {
			maintainers = repositoryMaintainerService.findAll(pageable);
		}
		
		return handleSuccessForPagedCollection(maintainers);
	}
	
	/**
	 * Fetches single maintainer of given ID.
	 * @param id
	 * @return
	 * @throws ApiException
	 */
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<ResponseDto<EntityModel<RepositoryMaintainerDto>>> getMaintainer(
			@PathVariable Integer id) throws ApiException {
		RepositoryMaintainer maintainer = repositoryMaintainerService.findById(id).orElseThrow(() -> new RepositoryMaintainerNotFound(messageSource, locale));				
		
		return handleSuccessForSingleEntity(maintainer);
	}
	
	/**
	 * Creates maintainer.
	 * @param principal used for authorization
	 * @param repositoryMaintainerDto DTO containing the maintainer
	 * @param bindingResult used for validation
	 * @return
	 * @throws ApiException
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody ResponseEntity<?> createMaintainer(Principal principal, 
			@RequestBody RepositoryMaintainerDto repositoryMaintainerDto) throws ApiException {
		User requester = userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		
		RepositoryMaintainer repositoryMaintainer = null;
		try {
			repositoryMaintainer = resolveDtoToEntity(repositoryMaintainerDto);
		} catch (ResolveRelatedEntitiesException e) {
			return handleValidationError(e);
		}
		
		BindingResult bindingResult = createBindingResult(repositoryMaintainer);
		repositoryMaintainerValidator.validate(repositoryMaintainer, bindingResult);
		
		if(bindingResult.hasErrors())
			return handleValidationError(bindingResult);
		
		try {
			Strategy<RepositoryMaintainer> strategy = factory.createRepositoryMaintainerStrategy(repositoryMaintainer, requester);
			RepositoryMaintainer created = strategy.perform(); 
			return handleCreatedForSingleEntity(created);
		} catch(StrategyFailure e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new CreateException(messageSource, locale);
		}
	}
	
	/**
	 * Updates maintainer
	 * @param principal used for authorization
	 * @param id
	 * @param jsonPatch JSON Patch array containing changed properties
	 * @param bindingResult used for validation
	 * @return
	 * @throws ApiException
	 */
	@PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<?> updateRepositoryMaintainer(Principal principal, 
			@PathVariable Integer id, @RequestBody JsonPatch jsonPatch) throws ApiException {
		
		User requester = userService.findByLogin(principal.getName()).orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		
		RepositoryMaintainer repositoryMaintainer = repositoryMaintainerService.findById(id).orElseThrow(() -> new RepositoryMaintainerNotFound(messageSource, locale));
		RepositoryMaintainer updated = null;		
		
		try {
			RepositoryMaintainerDto repositoryMaintainerDto = applyPatchToEntity(jsonPatch, repositoryMaintainer);
			RepositoryMaintainer entity = resolveDtoToEntity(repositoryMaintainerDto);
			BindingResult bindingResult = createBindingResult(entity);
			
			repositoryMaintainerValidator.validate(entity, bindingResult);
			
			if(bindingResult.hasErrors()) {
				return handleValidationError(bindingResult);
			}
			
			Strategy<RepositoryMaintainer> strategy = factory.updateRepositoryMaintainerStrategy(repositoryMaintainer, requester, entity);
			
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
	 * Deletes maintainer of given ID. This is a permanent delete.
	 * @param id
	 * @return
	 * @throws ApiException
	 */
	@DeleteMapping(path = "/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteRepositoryMaintainer(@PathVariable Integer id, Principal principal) throws ApiException {
		Optional<User> requester = userService.findByLogin(principal.getName());
		if(requester == null)
			throw new UserNotAuthorized(messageSource, locale);
		
		RepositoryMaintainer maintainer = repositoryMaintainerService.findOneDeleted(id)
				.orElseThrow(() -> new RepositoryMaintainerNotFound(messageSource, locale));			
		try {
			repositoryMaintainerDeleter.delete(maintainer);
		} catch (DeleteEntityException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new DeleteException(messageSource, locale);
		}
	}

	@Override
	public RepositoryMaintainer resolveDtoToEntity(RepositoryMaintainerDto dto) throws ResolveRelatedEntitiesException {
		User user = userService.findById(dto.getUserId())
				.orElseThrow(() -> new ResolveRelatedEntitiesException("user"));
		Repository<?,?> repository = repositoryService.findById(
				dto.getRepositoryId())
				.orElseThrow(() -> new ResolveRelatedEntitiesException("repository"));
		
		return new RepositoryMaintainer(dto, repository, user);
	}
}

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
package eu.openanalytics.rdepot.r.api.v2.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2Controller;
import eu.openanalytics.rdepot.base.api.v2.controllers.ModifiableDataController;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApiException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApplyPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.CreateException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.DeleteException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.MalformedPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.NotAllowedInDeclarativeMode;
import eu.openanalytics.rdepot.base.api.v2.exceptions.RepositoryNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ResolveRelatedEntitiesException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.SynchronizationError;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.SynchronizationStatus;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.utils.specs.RepositorySpecs;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import eu.openanalytics.rdepot.r.api.v2.dtos.RRepositoryDto;
import eu.openanalytics.rdepot.r.api.v2.hateoas.RRepositoryModelAssembler;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.mediator.deletion.RRepositoryDeleter;
import eu.openanalytics.rdepot.r.mirroring.CranMirrorSynchronizer;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.strategy.factory.RStrategyFactory;
import eu.openanalytics.rdepot.r.validation.RRepositoryValidator;
import jakarta.json.JsonException;
import jakarta.json.JsonPatch;

/**
 * REST controller implementation for R repositories.
 */
@RestController
@RequestMapping(value = "/api/v2/manager/r/repositories")
public class RRepositoryController extends ApiV2Controller<RRepository, RRepositoryDto>
	implements ModifiableDataController<RRepository, RRepositoryDto> {
	
	private final RRepositoryService repositoryService;
	private final UserService userService;
	private final RRepositoryValidator repositoryValidator;
	private final CranMirrorSynchronizer mirrorSynchronizer;
	private final SecurityMediator securityMediator;
	
	private final RStrategyFactory factory;
	private final RRepositoryDeleter deleter;
	
	@Value("${declarative}")
	private String declarative;
	
	public RRepositoryController(MessageSource messageSource,
			RRepositoryModelAssembler modelAssembler,
			PagedResourcesAssembler<RRepository> pagedModelAssembler, 
			ObjectMapper objectMapper,			
			RRepositoryService repositoryService,
			UserService userService,
			RRepositoryValidator repositoryValidator,
			CranMirrorSynchronizer mirrorSynchronizer,
			RStrategyFactory factory,
			RRepositoryDeleter rRepositoryDeleter,
			SecurityMediator securityMediator) {
		super(messageSource, LocaleContextHolder.getLocale(), modelAssembler, 
				pagedModelAssembler, objectMapper, 
				RRepositoryDto.class, 
				LoggerFactory.getLogger(RRepositoryController.class), repositoryValidator);
		this.repositoryService = repositoryService;		
		this.userService = userService;
		this.repositoryValidator = repositoryValidator;
		this.mirrorSynchronizer = mirrorSynchronizer;
		this.factory = factory;
		this.deleter = rRepositoryDeleter;
		this.securityMediator = securityMediator;
	}
	
	/**
	 * Fetches all repositories.
	 * @param principal used for authorization
	 * @param pageable carries parameters required for pagination
	 * @param deleted show only deleted repositories, requires admin privileges
	 * @return
	 * @throws UserNotAuthorized
	 */
	@PreAuthorize("hasAuthority('user')")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseDto<PagedModel<EntityModel<RRepositoryDto>>> getAllRepositories(
			Principal principal, Pageable pageable,
			@RequestParam(name = "deleted", required = false, defaultValue = "false") Boolean deleted,
			@RequestParam(name = "name", required = false) Optional<String> name) throws UserNotAuthorized {
		
		User requester = userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		if((!userService.isAdmin(requester) && deleted))
			throw new UserNotAuthorized(messageSource, locale);
		
		Specification<RRepository> specs = null;
				 		
		specs = SpecificationUtils.andComponent(specs, RepositorySpecs.isDeleted(deleted));
		if(name.isPresent())
			specs = SpecificationUtils.andComponent(specs, RepositorySpecs.ofName(name.get()));
		return handleSuccessForPagedCollection(repositoryService.findAllBySpecification(specs, pageable));
	}
	
	/**
	 * Find a repository of given id.
	 * @param principal used for authorization
	 * @param id
	 * @return Repository DTO
	 * @throws UserNotAuthorized
	 * @throws RepositoryNotFound
	 */
	@PreAuthorize("hasAuthority('user')")
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<ResponseDto<EntityModel<RRepositoryDto>>> getRepositoryById(
			Principal principal, @PathVariable Integer id) 
					throws UserNotAuthorized, RepositoryNotFound {
		userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		
		RRepository repository = repositoryService.findById(id)
				.orElseThrow(() -> new RepositoryNotFound(messageSource, locale));
		
		return handleSuccessForSingleEntity(repository);
	}
	
	/**
	 * Create repository.
	 * @param principal used for authorization
	 * @param repositoryDto object representing created repository
	 * @param bindingResult carries validation result
	 * @return response with created repository
	 * @throws NotAllowedInDeclarativeMode if declarative mode is turned on, 
	 * it should be impossible to create a repository.
	 * @throws UserNotAuthorized
	 * @throws CreateException
	 */
	@PreAuthorize("hasAuthority('admin')")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody ResponseEntity<?> createRepository(
			Principal principal, @RequestBody RRepositoryDto repositoryDto) 
					throws NotAllowedInDeclarativeMode, UserNotAuthorized, CreateException {
		User requester = userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		
		if(!userService.isAdmin(requester))				
			throw new UserNotAuthorized(messageSource, locale);
		
		if(Boolean.valueOf(declarative))
			throw new NotAllowedInDeclarativeMode(messageSource, locale);
		
		try {
			RRepository repositoryEntity = resolveDtoToEntity(repositoryDto);
			BindingResult bindingResult = createBindingResult(repositoryEntity);
			
			repositoryValidator.validate(repositoryEntity, bindingResult);
			
			if(bindingResult.hasErrors())
				return handleValidationError(bindingResult);
			
			Strategy<RRepository> strategy = factory.createRepositoryStrategy(repositoryEntity, requester);
			
			RRepository repository = strategy.perform();
			return handleCreatedForSingleEntity(repository);
		} catch (StrategyFailure e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new CreateException(messageSource, locale);
		} catch (ResolveRelatedEntitiesException e) {
			return handleValidationError(e);
		}			
	}
	
	/**
	 * Updates a repository.
	 * The method follows JSON Patch standard (see {@link https://datatracker.ietf.org/doc/html/rfc6902}).
	 * @param principal used for authorization
	 * @param id
	 * @param jsonPatch JsonPatch object
	 * @return
	 * @throws ApiException
	 */
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<?> updateRepository(
			Principal principal, @PathVariable Integer id, 
			@RequestBody JsonPatch jsonPatch) 
					throws ApiException {
		RRepository repository = repositoryService.findById(id)
				.orElseThrow(() -> new RepositoryNotFound(messageSource, locale)); 			
		
		User requester = userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		if(!securityMediator.isAuthorizedToEdit(repository, requester))
			throw new UserNotAuthorized(messageSource, locale);
		
		if(Boolean.valueOf(declarative))
			throw new NotAllowedInDeclarativeMode(messageSource, locale);
		
		try {
			RRepositoryDto repositoryDto = applyPatchToEntity(jsonPatch, repository);
			RRepository updated = resolveDtoToEntity(repositoryDto);
			BindingResult bindingResult = createBindingResult(updated);
			
			repositoryValidator.validate(updated, bindingResult);
			
			if(bindingResult.hasErrors())
				return handleValidationError(bindingResult);
			
			Strategy<RRepository> strategy = factory.updateRepositoryStrategy(repository, requester, updated);
			
			repository = strategy.perform();								
		} catch (JsonException | JsonProcessingException e) {
			throw new MalformedPatchException(messageSource, locale, e);
		} catch (StrategyFailure e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new ApplyPatchException(messageSource, locale);
		} catch (ResolveRelatedEntitiesException e) {
			return handleValidationError(e);
		}
		
		return handleSuccessForSingleEntity(repository);
	}
	
	/**
	 * Erases repository from database and file system. 
	 * Requires admin privileges and the repository to be previously set "deleted".
	 * @param principal used for authorization
	 * @param id
	 * @return
	 * @throws RepositoryNotFound
	 * @throws UserNotAuthorized
	 * @throws DeleteException
	 */
	@PreAuthorize("hasAuthority('admin')")
	@DeleteMapping(path = "/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteRepository(Principal principal, @PathVariable Integer id) 
					throws RepositoryNotFound, UserNotAuthorized, DeleteException {
		Optional<User> requester = userService.findByLogin(principal.getName());
		
		if(requester.isEmpty() || requester.get().getRole().getValue() != Role.VALUE.ADMIN)
			throw new UserNotAuthorized(messageSource, locale);
		
		RRepository repository = repositoryService.findById(id)
				.orElseThrow(() -> new RepositoryNotFound(messageSource, locale));			
		
		try {
			deleter.delete(repository);
		} catch (DeleteEntityException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new DeleteException(messageSource, locale);
		}
	}
	
	/**
	 * Synchronizes repository with mirror.
	 * @param id repository ID
	 * @param principal used for authorization
	 * @throws ApiException
	 */
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PostMapping(value = "/{id}/synchronize-mirrors")
	public void synchronizeWithMirrors(@PathVariable Integer id, Principal principal) throws ApiException {
		userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		RRepository repository = repositoryService.findById(id)
				.orElseThrow(() -> new RepositoryNotFound(messageSource, locale));
		
		mirrorSynchronizer.findByRepository(repository)
			.forEach(m -> mirrorSynchronizer.synchronize(repository, m));
	}
	
	/**
	 * Fetches synchronization status for the given repository.
	 * @param id repository ID
	 * @param principal used for authorization
	 * @return
	 * @throws ApiException
	 */
	@PreAuthorize("hasAuthority('repositorymaintainer')")
	@ResponseStatus(HttpStatus.OK)
	@GetMapping(value = "/{id}/synchronization-status")
	public ResponseDto<?> getSynchronizationStatus(@PathVariable Integer id, Principal principal) 
			throws ApiException {
		User requester = userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		RRepository repository = repositoryService.findById(id)
				.orElseThrow(() -> new RepositoryNotFound(messageSource, locale));			
		
		if(!securityMediator.isAuthorizedToEdit(repository, requester))
			throw new UserNotAuthorized(messageSource, locale);
		
		List<SynchronizationStatus> status = mirrorSynchronizer.getSynchronizationStatusList(); //TODO: it would be better to fetch it for specific repository
	
		for(SynchronizationStatus s : status) {
			if(s.getRepositoryId() == repository.getId())
				return ResponseDto.generateSuccessBody(messageSource, locale, s);
		}
		
		throw new SynchronizationError(messageSource, locale);
	}
	
	@Override
	public RRepository resolveDtoToEntity(RRepositoryDto dto) throws ResolveRelatedEntitiesException {
		return new RRepository(dto);
	}
}
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
import java.util.List;
import java.util.Objects;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import eu.openanalytics.rdepot.api.v2.dto.RRepositoryDto;
import eu.openanalytics.rdepot.api.v2.dto.ResponseDto;
import eu.openanalytics.rdepot.api.v2.exception.ApiException;
import eu.openanalytics.rdepot.api.v2.exception.ApplyPatchException;
import eu.openanalytics.rdepot.api.v2.exception.CreateException;
import eu.openanalytics.rdepot.api.v2.exception.DeleteException;
import eu.openanalytics.rdepot.api.v2.exception.MalformedPatchException;
import eu.openanalytics.rdepot.api.v2.exception.NotAllowedInDeclarativeMode;
import eu.openanalytics.rdepot.api.v2.exception.RepositoryNotFound;
import eu.openanalytics.rdepot.api.v2.exception.SynchronizationError;
import eu.openanalytics.rdepot.api.v2.exception.UserNotAuthorized;
import eu.openanalytics.rdepot.api.v2.hateoas.RRepositoryModelAssembler;
import eu.openanalytics.rdepot.exception.RepositoryCreateException;
import eu.openanalytics.rdepot.exception.RepositoryDeleteException;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.exception.RepositoryPublishException;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.SynchronizationStatus;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.MirrorService;
import eu.openanalytics.rdepot.service.RepositoryService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.validation.RepositoryValidator;
import jakarta.json.JsonException;
import jakarta.json.JsonPatch;

/**
 * REST controller implementation for R repositories.
 */
@RestController
@RequestMapping(value = "/api/v2/manager/r/repositories")
public class RRepositoryController extends ApiV2Controller<Repository, RRepositoryDto> {

	private final RepositoryService repositoryService;
	private final UserService userService;
	private final RepositoryValidator repositoryValidator;
	private final MirrorService mirrorService;
	
	@Value("${declarative}")
	private String declarative;
	
	@Autowired
	public RRepositoryController(MessageSource messageSource,
			RRepositoryModelAssembler modelAssembler,
			PagedResourcesAssembler<Repository> pagedModelAssembler, 
			ObjectMapper objectMapper,
			RepositoryService repositoryService,
			UserService userService,
			RepositoryValidator repositoryValidator,
			MirrorService mirrorService) {
		super(messageSource, LocaleContextHolder.getLocale(), modelAssembler, 
				pagedModelAssembler, objectMapper, 
				RRepositoryDto.class, 
				LoggerFactory.getLogger(RRepositoryController.class), repositoryValidator);
		this.repositoryService = repositoryService;
		this.userService = userService;
		this.repositoryValidator = repositoryValidator;
		this.mirrorService = mirrorService;
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
			@RequestParam(name = "deleted", required = false, defaultValue = "false") Boolean deleted) throws UserNotAuthorized {
		
		User requester = userService.findByLogin(principal.getName());
		
		if(requester == null || (!userService.isAdmin(requester) && deleted))
			throw new UserNotAuthorized(messageSource, locale);
		
		Page<Repository> pagedRepositories = null;
		if(deleted)
			pagedRepositories = repositoryService.findAllOnlyDeleted(pageable);
		else
			pagedRepositories = repositoryService.findAll(pageable);		
		return handleSuccessForPagedCollection(pagedRepositories);
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
		User requester = userService.findByLogin(principal.getName());
		
		if(requester == null)
			throw new UserNotAuthorized(messageSource, locale); //Should not be "not authenticated"?
		
		Repository repository = repositoryService.findById(id);
		
		if(repository == null && userService.isAdmin(requester)) 
			repository = repositoryService.findByIdAndDeleted(id, true);
		
		if(repository == null)
			throw new RepositoryNotFound(messageSource, locale);
		
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
		User requester = userService.findByLogin(principal.getName());
		
		if(requester == null || !userService.isAdmin(requester))
			throw new UserNotAuthorized(messageSource, locale);
		
		if(Boolean.valueOf(declarative))
			throw new NotAllowedInDeclarativeMode(messageSource, locale);
		
		Repository repositoryEntity = repositoryDto.toEntity();
		if(Objects.isNull(repositoryEntity.isDeleted()))
			repositoryEntity.setDeleted(false);
		if(Objects.isNull(repositoryEntity.isPublished()))
			repositoryEntity.setPublished(false);
		
		BindingResult bindingResult = createBindingResult(repositoryEntity);
		
		repositoryValidator.validate(repositoryEntity, bindingResult);
		
		if(bindingResult.hasErrors())
			return handleValidationError(bindingResult);
		
		try {
			Repository repository = repositoryService.create(repositoryEntity, requester);
			return handleCreatedForSingleEntity(repository);
		} catch (RepositoryCreateException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new CreateException(messageSource, locale);
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
		Repository repository = repositoryService.findById(id);
		
		if(repository == null)
			throw new RepositoryNotFound(messageSource, locale);
		
		User requester = userService.findByLoginWithRepositoryMaintainers(principal.getName());
		if(requester == null || !userService.isAuthorizedToEdit(repository, requester))
			throw new UserNotAuthorized(messageSource, locale);
		
		if(Boolean.valueOf(declarative))
			throw new NotAllowedInDeclarativeMode(messageSource, locale);
		
		try {
			RRepositoryDto repositoryDto = applyPatchToEntity(jsonPatch, repository);
			Repository entity = repositoryDto.toEntity();
			BindingResult bindingResult = createBindingResult(entity);
			
			repositoryValidator.validate(entity, bindingResult);
			
			if(bindingResult.hasErrors())
				return handleValidationError(bindingResult);
			
			repository = repositoryService.evaluateAndUpdate(repositoryDto, requester);
		} catch (RepositoryEditException | RepositoryPublishException | RepositoryDeleteException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new ApplyPatchException(messageSource, locale);
		} catch (JsonException | JsonProcessingException e) {
			throw new MalformedPatchException(messageSource, locale, e);
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
		User requester = userService.findByLogin(principal.getName());
		
		if(requester == null || !userService.isAdmin(requester))
			throw new UserNotAuthorized(messageSource, locale);
		
		Repository repository = repositoryService.findByIdAndDeleted(id, true);
		
		if(repository == null)
			throw new RepositoryNotFound(messageSource, locale);
		
		try {
			repositoryService.shiftDelete(repository);
		} catch (RepositoryDeleteException e) {
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
		User requester = userService.findByLogin(principal.getName());
		if(requester == null)
			throw new UserNotAuthorized(messageSource, locale);
		
		Repository repository = repositoryService.findById(id);
		if(repository == null)
			throw new RepositoryNotFound(messageSource, locale);
		
		mirrorService.findByRepository(repository)
			.forEach(m -> mirrorService.synchronize(repository, m));
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
		User requester = userService.findByLogin(principal.getName());
		Repository repository = repositoryService.findById(id);
		
		if(repository == null)
			throw new RepositoryNotFound(messageSource, locale);
		
		if(requester == null || !userService.isAuthorizedToEdit(repository, requester))
			throw new UserNotAuthorized(messageSource, locale);
		
		List<SynchronizationStatus> status = mirrorService.getSynchronizationStatusList(); //TODO: it would be better to fetch it for specific repository
	
		for(SynchronizationStatus s : status) {
			if(s.getRepositoryId() == repository.getId())
				return ResponseDto.generateSuccessBody(messageSource, locale, s);
		}
		
		throw new SynchronizationError(messageSource, locale);
	}
	

}
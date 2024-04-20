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
package eu.openanalytics.rdepot.base.api.v2.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.validation.PatchValidator;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
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

import eu.openanalytics.rdepot.base.api.v2.converters.UserDtoConverter;
import eu.openanalytics.rdepot.base.api.v2.converters.exceptions.EntityResolutionException;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.RoleDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.UserDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApiException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApplyPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.MalformedPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotFound;
import eu.openanalytics.rdepot.base.api.v2.hateoas.RoleCollectionModelAssembler;
import eu.openanalytics.rdepot.base.api.v2.hateoas.UserModelAssembler;
import eu.openanalytics.rdepot.base.api.v2.resolvers.DtoResolvedPageable;
import eu.openanalytics.rdepot.base.api.v2.resolvers.PageableSortResolver;
import eu.openanalytics.rdepot.base.api.v2.validation.PageableValidator;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.UserSettings;
import eu.openanalytics.rdepot.base.service.AccessTokenService;
import eu.openanalytics.rdepot.base.service.RoleService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.UserSettingsService;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.factory.StrategyFactory;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import eu.openanalytics.rdepot.base.utils.specs.UserSpecs;
import eu.openanalytics.rdepot.base.validation.UserValidator;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.json.JsonException;
import jakarta.json.JsonPatch;

/**
 * REST Controller implementation for user management.
 */
@RestController
@RequestMapping(value = "/api/v2/manager/users")
public class ApiV2UserController extends ApiV2Controller<User, UserDto> {
	
	private final UserService userService;
	private final UserSettingsService userSettingsService;
	private final RoleService roleService;
	private final RoleCollectionModelAssembler roleCollectionModelAssembler;
	private final UserValidator userValidator;
	private final StrategyFactory factory;
	private final PageableValidator pageableValidator;
	private final PageableSortResolver pageableSortResolver;
	private final SecurityMediator securityMediator;

	public ApiV2UserController(MessageSource messageSource,
			UserModelAssembler modelAssembler,
			PagedResourcesAssembler<User> pagedModelAssembler,
			RoleCollectionModelAssembler roleCollectionModelAssembler,
			ObjectMapper objectMapper, UserService userService,
			RoleService roleService, UserValidator userValidator,
			StrategyFactory factory,
			UserDtoConverter userDtoConverter, 
			UserSettingsService userSettingsService,
			AccessTokenService accessTokenService,
			PageableValidator pageableValidator,
			PageableSortResolver pageableSortResolver,
			SecurityMediator securityMediator) {

		super(messageSource, LocaleContextHolder.getLocale(), 
				modelAssembler, pagedModelAssembler, 
				objectMapper, UserDto.class, 
				Optional.of(userValidator),
				userDtoConverter
				);
		this.roleService = roleService;
		this.userService = userService;
		this.roleCollectionModelAssembler = roleCollectionModelAssembler;
		this.userValidator = userValidator;
		this.factory = factory;
		this.userSettingsService = userSettingsService;
		this.pageableValidator = pageableValidator;
		this.pageableSortResolver = pageableSortResolver;
		this.securityMediator = securityMediator;
	}
	
	/**
	 * Fetches all users. Requires admin privileges.
	 * @param pageable carries parameters required for pagination
	 */
	@GetMapping
	@PreAuthorize("hasAuthority('admin')")
	@ResponseStatus(HttpStatus.OK)
	@PageableAsQueryParam
	public @ResponseBody ResponseDto<PagedModel<EntityModel<UserDto>>> getAllUsers(
			@ParameterObject Pageable pageable, Principal principal,
			@RequestParam(name = "role", required=false) List<String> roles,
			@RequestParam(name = "active", required=false) Optional<Boolean> active,
			@RequestParam(name = "search", required = false) Optional<String> search
			) throws ApiException {
		User requester = userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		
		final DtoResolvedPageable resolvedPageable = pageableSortResolver.resolve(pageable);
		pageableValidator.validate(UserDto.class, resolvedPageable);
		
		Specification<User> specification = null;
		
		if(Objects.nonNull(roles)) {
			specification = SpecificationUtils.andComponent(specification, UserSpecs.ofRole(roles));
		}
		
		if(active.isPresent()) {
			specification = SpecificationUtils.andComponent(specification, UserSpecs.isActive(active.get()));
		}
		
		if(search.isPresent()) {
			specification = SpecificationUtils	
					.andComponent(specification, UserSpecs.byEmail(search.get())
							.or(UserSpecs.byName(search.get()))
							.or(UserSpecs.byLogin(search.get())));
		}
		
		if(specification != null) {
			return handleSuccessForPagedCollection(userService.findAllBySpecification(specification, resolvedPageable), requester);
		}
		
		return handleSuccessForPagedCollection(userService.findAll(resolvedPageable), requester);
	}
	
	/**
	 * Find a user of given id. 
	 * Requires admin privileges unless the requester is the same as requested user.
	 */
	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('user')")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<ResponseDto<EntityModel<UserDto>>> getUser(
			Principal principal, @PathVariable("id") Integer id) throws UserNotFound, UserNotAuthorized {
		Optional<User> requester = userService.findByLogin(principal.getName());
		Optional<User> user = userService.findOneNonDeleted(id);
				
		if(requester.isPresent()) {
			if(user.isEmpty() && userService.isAdmin(requester.get())) {
				throw new UserNotFound(messageSource, locale);
			} else if(user.isPresent() 
					&& (userService.isAdmin(requester.get()) || requester.get().getId() == user.get().getId())) {
				return handleSuccessForSingleEntity(user.get(), requester.get());
			}
		}
		
		throw new UserNotAuthorized(messageSource, locale);
	}
	
	/**
	 * Fetches data about the user themselves.
	 * @param principal used for authorization
	 */
	@GetMapping("/me")
	@PreAuthorize("hasAuthority('user')")
	@Operation(operationId = "getUserInfo")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<ResponseDto<EntityModel<UserDto>>> getUser(
			Principal principal) throws UserNotFound, UserNotAuthorized {
		User requester = userService.findByLogin(principal.getName()).orElseThrow(() -> new UserNotAuthorized(messageSource, locale));								
		UserSettings settings = userSettingsService.getUserSettings(requester);
		requester.setUserSettings(settings);
		return handleSuccessForSingleEntity(requester, requester);				
	}
		
	/**
	 * Fetches all available roles.
	 * Requires admin privileges.
	 */
	@GetMapping("/roles")
	@PreAuthorize("hasAuthority('admin')")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseDto<CollectionModel<RoleDto>> getRoles() {
		return ResponseDto.generateSuccessBody(messageSource, locale, 
				roleCollectionModelAssembler.toModel(roleService.findAll()	));
	}
	
	/**
	 * Updates a user. Requires admin privileges.
	 */
	@PatchMapping(value = "/{id}", consumes = "application/json-patch+json")
	@PreAuthorize("hasAuthority('admin')")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<?> patchUser(
			Principal principal,
			@PathVariable("id") Integer id, @RequestBody JsonPatch patch) 
					throws UserNotFound, ApplyPatchException, 
					UserNotAuthorized, MalformedPatchException {
		Optional<User> requester = userService.findByLogin(principal.getName());
		User user = userService.findById(id).orElseThrow(() -> new UserNotFound(messageSource, locale));
		
		if(requester.isEmpty()
				|| !userService.isAdmin(requester.get())
				|| !securityMediator
					.isAuthorizedToEditWithPatch(patch, user, requester.get()))
			throw new UserNotAuthorized(messageSource, locale);
		
		try {
			UserDto patchedDto = applyPatchToEntity(patch, user);
			User entity = dtoConverter.resolveDtoToEntity(patchedDto);
			BindingResult bindingResult = createBindingResult(entity);
			
			userValidator.validate(entity, bindingResult);
			
			if(bindingResult.hasErrors())
				return handleValidationError(bindingResult);
			
			Strategy<User> strategy = factory.updateUserStrategy(user, requester.get(), entity);
			
			strategy.perform();		
		} catch (JsonException |JsonProcessingException  | EntityResolutionException e) {
			throw new MalformedPatchException(messageSource, locale, e);
		} catch (StrategyFailure e) {
			throw new ApplyPatchException(messageSource, locale);
		}
		
		return handleSuccessForSingleEntity(user, requester.get());
	}
}
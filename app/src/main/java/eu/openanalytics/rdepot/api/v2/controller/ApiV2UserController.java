/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.api.v2.dto.ResponseDto;
import eu.openanalytics.rdepot.api.v2.dto.RoleDto;
import eu.openanalytics.rdepot.api.v2.dto.UserDto;
import eu.openanalytics.rdepot.api.v2.exception.ApplyPatchException;
import eu.openanalytics.rdepot.api.v2.exception.MalformedPatchException;
import eu.openanalytics.rdepot.api.v2.exception.UserNotAuthorized;
import eu.openanalytics.rdepot.api.v2.exception.UserNotFound;
import eu.openanalytics.rdepot.api.v2.hateoas.RoleCollectionModelAssembler;
import eu.openanalytics.rdepot.api.v2.hateoas.UserModelAssembler;
import eu.openanalytics.rdepot.exception.UserException;
import eu.openanalytics.rdepot.exception.UserUnauthorizedException;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.RoleService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.validation.UserValidator;
import jakarta.json.JsonException;
import jakarta.json.JsonPatch;

/**
 * REST Controller implementation for user management.
 */
@RestController
@RequestMapping(value = "/api/v2/manager/users")
public class ApiV2UserController extends ApiV2Controller<User, UserDto> {
	
	private final UserService userService;
	private final RoleService roleService;
	private final RoleCollectionModelAssembler roleCollectionModelAssembler;
	private final UserValidator userValidator;

	@Autowired
	public ApiV2UserController(MessageSource messageSource,
			UserModelAssembler modelAssembler,
			PagedResourcesAssembler<User> pagedModelAssembler,
			RoleCollectionModelAssembler roleCollectionModelAssembler,
			ObjectMapper objectMapper, UserService userService,
			RoleService roleService, UserValidator userValidator) {
		super(messageSource, LocaleContextHolder.getLocale(), 
				modelAssembler, pagedModelAssembler, 
				objectMapper, UserDto.class, 
				LoggerFactory.getLogger(ApiV2UserController.class), userValidator);
		this.roleService = roleService;
		this.userService = userService;
		this.roleCollectionModelAssembler = roleCollectionModelAssembler;
		this.userValidator = userValidator;
	}
	
	/**
	 * Fetches all users. Requires admin privileges.
	 * @param pageable carries parameters required for pagination
	 * @return 
	 */
	@GetMapping
	@PreAuthorize("hasAuthority('admin')")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseDto<PagedModel<EntityModel<UserDto>>> getAllUsers(Pageable pageable) {
		return handleSuccessForPagedCollection(userService.findAll(pageable));
	}
	
	/**
	 * Find a user of given id. 
	 * Requires admin privileges unless the requester is the same as requested user.
	 * @param principal used for authorization
	 * @param id
	 * @return User DTO
	 * @throws UserNotFound
	 * @throws UserNotAuthorized
	 */
	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('user')")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<ResponseDto<EntityModel<UserDto>>> getUser(
			Principal principal, @PathVariable Integer id) throws UserNotFound, UserNotAuthorized {
		User requester = userService.findByLogin(principal.getName());
		User user = userService.findById(id);
		
		if(requester != null) {
			if(user == null && userService.isAdmin(requester)) {
				throw new UserNotFound(messageSource, locale);
			} else if(user != null 
					&& (userService.isAdmin(requester) || requester.getId() == user.getId())) {
				return handleSuccessForSingleEntity(user);
			}
		}
		
		throw new UserNotAuthorized(messageSource, locale);
	}
	
	/**
	 * Fetches API token for a given user.
	 * Requires admin privileges unless the requester is the same as requested user.
	 * @param principal
	 * @param id
	 * @return JSON Object containing one key-value pair with token.
	 * @throws UserNotFound
	 * @throws UserNotAuthorized
	 */
	@GetMapping("/{id}/token")
	@PreAuthorize("hasAuthority('user')")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseDto<EntityModel<Map<String, String>>> getToken(Principal principal, 
			@PathVariable Integer id) throws UserNotFound, UserNotAuthorized {
		User requester = userService.findByLogin(principal.getName());
		User user = userService.findById(id);
		
		if(requester != null) {
			if(user == null && userService.isAdmin(requester)) {
				throw new UserNotFound(messageSource, locale);
			} else if(user != null 
					&& (userService.isAdmin(requester) || requester.getId() == user.getId())) {
				Map<String, String> tokenObj = new HashMap<>();
				tokenObj.put("token", userService.generateToken(user.getLogin()));
				
				EntityModel<Map<String, String>> content = EntityModel.of(tokenObj,
						linkTo(ApiV2UserController.class).slash(id).slash("token").withSelfRel());
				
				return ResponseDto.generateSuccessBody(messageSource, locale, content);
			}
		}
		
		throw new UserNotAuthorized(messageSource, locale);
	}
	
	/**
	 * Fetches all available roles.
	 * Requires admin privileges.
	 * @return
	 */
	@GetMapping("/roles")
	@PreAuthorize("hasAuthority('admin')")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseDto<CollectionModel<RoleDto>> getRoles() {
		return ResponseDto.generateSuccessBody(messageSource, locale, 
				roleCollectionModelAssembler.toModel(roleService.findAll()));
	}
	
	/**
	 * Updates a user. Requires admin privileges.
	 * @param principal used for authorization
	 * @param id
	 * @param patch JSON Patch object
	 * @return
	 * @throws UserNotFound
	 * @throws ApplyPatchException when some internal server error occurs
	 * @throws UserUnauthorizedException
	 * @throws MalformedPatchException when provided JSON Patch object is incorrect (e.g. alters non-existing fields)
	 */
	@PatchMapping(value = "/{id}", consumes = "application/json-patch+json")
	@PreAuthorize("hasAuthority('admin')")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<?> patchUser(
			Principal principal,
			@PathVariable Integer id, @RequestBody JsonPatch patch) 
					throws UserNotFound, ApplyPatchException, 
					UserUnauthorizedException, MalformedPatchException {
		User requester = userService.findByLogin(principal.getName());
		User user = userService.findById(id);
		
		if(requester == null || !userService.isAdmin(requester))
			throw new UserUnauthorizedException(messageSource, locale);
		
		if(user == null)
			throw new UserNotFound(messageSource, locale);
		
		try {
			UserDto patchedDto = applyPatchToEntity(patch, user);
			User entity = patchedDto.toEntity();
			BindingResult bindingResult = createBindingResult(entity);
			
			userValidator.validate(entity, bindingResult);
			
			if(bindingResult.hasErrors())
				return handleValidationError(bindingResult);
			
			user = userService.evaluateAndUpdate(patchedDto, requester);
		} catch (UserException e) {
			throw new ApplyPatchException(messageSource, locale);
		} catch (JsonException |JsonProcessingException e) {
			throw new MalformedPatchException(messageSource, locale, e);
		}
		
		return handleSuccessForSingleEntity(user);
	}

}
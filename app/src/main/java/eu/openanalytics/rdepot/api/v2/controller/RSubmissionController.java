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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.api.v2.dto.RSubmissionDto;
import eu.openanalytics.rdepot.api.v2.dto.ResponseDto;
import eu.openanalytics.rdepot.api.v2.dto.SubmissionState;
import eu.openanalytics.rdepot.api.v2.exception.ApplyPatchException;
import eu.openanalytics.rdepot.api.v2.exception.CreateException;
import eu.openanalytics.rdepot.api.v2.exception.DeleteException;
import eu.openanalytics.rdepot.api.v2.exception.InvalidSubmission;
import eu.openanalytics.rdepot.api.v2.exception.MalformedPatchException;
import eu.openanalytics.rdepot.api.v2.exception.SubmissionNotFound;
import eu.openanalytics.rdepot.api.v2.exception.UserNotAuthorized;
import eu.openanalytics.rdepot.api.v2.exception.UserNotFound;
import eu.openanalytics.rdepot.api.v2.hateoas.RSubmissionModelAssembler;
import eu.openanalytics.rdepot.exception.PackageDeleteException;
import eu.openanalytics.rdepot.exception.SubmissionCreateException;
import eu.openanalytics.rdepot.exception.SubmissionDeleteException;
import eu.openanalytics.rdepot.exception.SubmissionException;
import eu.openanalytics.rdepot.exception.UpdateNotAllowedException;
import eu.openanalytics.rdepot.exception.UploadRequestValidationException;
import eu.openanalytics.rdepot.model.PackageUploadRequest;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.SubmissionService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.warning.SubmissionCreateWarning;
import eu.openanalytics.rdepot.warning.SubmissionWarning;
import eu.openanalytics.rdepot.warning.UploadRequestValidationWarning;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonPatch;
import jakarta.json.spi.JsonProvider;

/**
 * REST controller implementation for R submissions.
 */
@RestController
@RequestMapping(value = "/api/v2/manager/r/submissions")
public class RSubmissionController extends ApiV2Controller<Submission, RSubmissionDto> {

	private SubmissionService submissionService;
	private UserService userService;
	
	@Autowired
	public RSubmissionController(MessageSource messageSource,
			RSubmissionModelAssembler modelAssembler,
			PagedResourcesAssembler<Submission> pagedModelAssembler, 
			ObjectMapper objectMapper,
			SubmissionService submissionService, UserService userService) {
		super(messageSource, LocaleContextHolder.getLocale(), 
				modelAssembler, pagedModelAssembler, 
				objectMapper, 
				RSubmissionDto.class, 
				LoggerFactory.getLogger(RSubmissionController.class), null);
		
		this.submissionService = submissionService;
		this.userService = userService;
	}
	
	/**
	 * Submits package archive and creates submission.
	 * @param multipartFile package file
	 * @param repository name of the destination repository
	 * @param generateManual specifies if manuals should be generated for the package
	 * @param replace specified if previous version should be replaced
	 * @param principal used for authorization
	 * @return DTO with created submission
	 * @throws UserNotAuthorized
	 * @throws CreateException if there was an error on the server side
	 * @throws InvalidSubmission if user provided invalid file or parameters
	 */
	@PreAuthorize("hasAuthority('user')")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody ResponseEntity<?> submitPackage(
			@RequestParam("file") MultipartFile multipartFile,
			@RequestParam("repository") String repository,
			@RequestParam(name = "generateManual", 
				defaultValue = "${generate-manuals}") Boolean generateManual,
			@RequestParam(name = "replace", defaultValue = "false") Boolean replace,
			Principal principal) throws UserNotAuthorized, CreateException, InvalidSubmission {
		
		User uploader = userService.findByLogin(principal.getName());
		
		if(uploader == null)
			throw new UserNotAuthorized(messageSource, locale);
		
		PackageUploadRequest request = new PackageUploadRequest(
				multipartFile, repository, generateManual, replace);
		
		try {
			Submission submission = submissionService.create(request, uploader);
			return handleCreatedForSingleEntity(submission);
		} catch (SubmissionCreateException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			throw new CreateException(messageSource, locale);
		} catch (SubmissionCreateWarning | UploadRequestValidationWarning | UploadRequestValidationException e) {
			logger.warn(e.getClass().getName() + ": " + e.getMessage());
			return handleValidationError(e);
		}
	}
	
	/**
	 * Fetches all submissions.
	 * @param principal used for authorization
	 * @param pageable carries parameters required for pagination
	 * @param deleted show only deleted submissions, requires admin privileges
	 * @param state show only submissions of a particular state (waiting, accepted, cancelled, rejected)
	 * @param userId show only submissions of a particular user
	 * @return collection of submissions
	 * @throws UserNotAuthorized
	 * @throws UserNotFound
	 */
	@PreAuthorize("hasAuthority('user')")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseDto<PagedModel<EntityModel<RSubmissionDto>>> getAllSubmissions(
			Principal principal, Pageable pageable,
			@RequestParam(name = "state", required = false) Optional<SubmissionState> state,
			@RequestParam(name = "userId", required = false) Optional<Integer> userId) 
					throws UserNotAuthorized, UserNotFound {
		User requester = userService.findByLogin(principal.getName());
		
		if(requester == null)
			throw new UserNotAuthorized(messageSource, locale);
		
		Optional<User> user = Optional.empty();
		if(userId.isPresent()) {
			User userEntity = userService.findById(userId.get());
			
			if(userEntity == null)
				throw new UserNotFound(messageSource, locale);
			
			user = Optional.of(userEntity);
		}
		
		return handleSuccessForPagedCollection(submissionService
				.findAllForUserOfUserAndWithState(requester, user.orElse(null), state, pageable));
	}
	
	/**
	 * Updates a submission.
	 * @param principal used for authorization
	 * @param id
	 * @param jsonPatch JsonPatch object
	 * @return
	 * @throws SubmissionNotFound
	 * @throws UserNotAuthorized
	 * @throws ApplyPatchException when some internal server errors occurs
	 * @throws MalformedPatchException when provided JSON Patch object is incorrect (e.g. alters non-existing fields)
	 */
	@PreAuthorize("hasAuthority('user')")
	@PatchMapping(value = "/{id}", consumes = "application/json-patch+json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<?> updateSubmission(Principal principal, 
			@PathVariable Integer id, @RequestBody JsonPatch jsonPatch) 
					throws SubmissionNotFound, UserNotAuthorized, ApplyPatchException, MalformedPatchException {
		Submission submission = submissionService.findById(id);
		
		jsonPatch = fixPatch(jsonPatch); //So that it doesn't complain when state is written with lower case
		
		if(submission == null)
			throw new SubmissionNotFound(messageSource, locale);
		
		User requester = userService.findByLogin(principal.getName());
		
		if(requester == null || !userService.isAuthorizedToEdit(submission, requester))
			throw new UserNotAuthorized(messageSource, locale);
		
		try {
			RSubmissionDto submissionDto = applyPatchToEntity(jsonPatch, submission);
			
			//TODO: Validation
			submission = submissionService.evaluateAndUpdate(submissionDto, requester);
		} catch (SubmissionException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new ApplyPatchException(messageSource, locale);
		} catch (JsonProcessingException | JsonException | UpdateNotAllowedException e) {
			throw new MalformedPatchException(messageSource, locale, e);
		} catch (SubmissionWarning w) {
			logger.warn(w.getClass().getName() + ": " + w.getMessage(), w);
		}
		return handleSuccessForSingleEntity(submission);
	}
	
	/**
	 * This method is supposed to make state field case insensitive.
	 * @param jsonPatch to fix
	 * @return fixed patch
	 */
	private JsonPatch fixPatch(JsonPatch jsonPatch) {
		JsonArray jsonArray = jsonPatch.toJsonArray();
		JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
		
		for(int i = 0; i < jsonArray.size(); i++) {
			JsonObject obj = jsonArray.getJsonObject(i);
			
			if(obj.containsKey("op") && obj.containsKey("path") 
					&& obj.getString("op").equals("replace") 
					&& obj.getString("path").equals("/state")
					&& obj.containsKey("value")) {
				
				
				JsonObjectBuilder builder = Json.createObjectBuilder()
						.add("value", obj.getString("value").toUpperCase());
				
				obj.entrySet().stream()
					.filter(e -> !e.getKey().equals("value"))
					.forEach(e -> builder.add(e.getKey(), e.getValue()));
				obj = builder.build();
			}
			arrBuilder.add(obj);
		}
		return JsonProvider.provider().createPatch(arrBuilder.build());
	}

	/**
	 * Find a submission of given id
	 * @param principal used for authorization
	 * @param id
	 * @return Submission DTO
	 * @throws SubmissionNotFound
	 * @throws UserNotAuthorized
	 */
	@PreAuthorize("hasAuthority('user')")
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<ResponseDto<EntityModel<RSubmissionDto>>> getSubmissionById(
			Principal principal, @PathVariable Integer id) 
					throws SubmissionNotFound, UserNotAuthorized {
		Optional<Submission> submission = submissionService.findByIdEvenDeleted(id);
		if(submission.isEmpty())
			throw new SubmissionNotFound(messageSource, locale);
		
		User requester = userService.findByLogin(principal.getName());
		if(requester == null || !userService.isAuthorizedToSee(submission.get(), requester))
			throw new UserNotAuthorized(messageSource, locale);
		
		return handleSuccessForSingleEntity(submission.get());
	}
	
	/**
	 * Erases submission from database and file system.
	 * Requires admin privileges.
	 * @param principal used for authorization
	 * @param id
	 * @return
	 * @throws SubmissionNotFound
	 * @throws UserNotAuthorized
	 * @throws DeleteException
	 */
	@PreAuthorize("hasAuthority('admin')")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteSubmission(
			Principal principal, @PathVariable Integer id) 
					throws SubmissionNotFound, UserNotAuthorized, DeleteException {
		User requester = userService.findByLogin(principal.getName());
		if(requester == null || !userService.isAdmin(requester))
			throw new UserNotAuthorized(messageSource, locale);
		
		Submission submission = submissionService.findById(id);
		if(submission == null)
			throw new SubmissionNotFound(messageSource, locale);
		
		try {
			submissionService.shiftDelete(submission);
		} catch (SubmissionDeleteException | PackageDeleteException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new DeleteException(messageSource, locale);
		}
	}
}
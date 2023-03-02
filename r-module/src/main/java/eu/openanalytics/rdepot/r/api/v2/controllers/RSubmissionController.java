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
package eu.openanalytics.rdepot.r.api.v2.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
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

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2Controller;
import eu.openanalytics.rdepot.base.api.v2.controllers.ModifiableDataController;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageUploadRequest;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.SubmissionDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApplyPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.CreateException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.DeleteException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.InvalidSubmission;
import eu.openanalytics.rdepot.base.api.v2.exceptions.MalformedPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.RepositoryNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ResolveRelatedEntitiesException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.SubmissionNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotFound;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.mediator.deletion.SubmissionDeleter;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import eu.openanalytics.rdepot.base.utils.specs.SubmissionSpecs;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.base.validation.SubmissionPatchValidator;
import eu.openanalytics.rdepot.base.validation.exceptions.MultipartFileValidationException;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageDuplicatedToSilentlyIgnore;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageValidationException;
import eu.openanalytics.rdepot.base.validation.exceptions.PatchValidationException;
import eu.openanalytics.rdepot.r.api.v2.hateoas.RSubmissionModelAssembler;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.strategy.factory.RStrategyFactory;
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
public class RSubmissionController extends ApiV2Controller<Submission, SubmissionDto>
	implements ModifiableDataController<Submission, SubmissionDto> {

	private SubmissionService submissionService;
	private UserService userService;
	private final RStrategyFactory strategyFactory;
	private final SecurityMediator securityMediator;
	private final RRepositoryService repositoryService;
	private final PackageValidator<RPackage> packageValidator;
	private final SubmissionDeleter submissionDeleter;
	private final RPackageService packageService;
	private final SubmissionPatchValidator submissionPatchValidator;
	
	public RSubmissionController(MessageSource messageSource,
			RSubmissionModelAssembler modelAssembler,
			PagedResourcesAssembler<Submission> pagedModelAssembler, 
			ObjectMapper objectMapper,
			SubmissionService submissionService, UserService userService,
			RStrategyFactory strategyFactory, SecurityMediator securityMediator,
			RRepositoryService repositoryService, PackageValidator<RPackage> packageValidator,
			SubmissionDeleter submissionDeleter, RPackageService rPackageService,
			SubmissionPatchValidator submissionPatchValidator) {
		super(messageSource, LocaleContextHolder.getLocale(), 
				modelAssembler, pagedModelAssembler, 
				objectMapper, 
				SubmissionDto.class, 
				LoggerFactory.getLogger(RSubmissionController.class), null);
		this.repositoryService = repositoryService;
		this.submissionService = submissionService;
		this.userService = userService;
		this.strategyFactory = strategyFactory;
		this.securityMediator = securityMediator;
		this.packageValidator = packageValidator;
		this.submissionDeleter = submissionDeleter;
		this.packageService = rPackageService;
		this.submissionPatchValidator = submissionPatchValidator;
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
	 * @throws RepositoryNotFound 
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
			Principal principal) throws UserNotAuthorized, CreateException, 
				InvalidSubmission, RepositoryNotFound {
		
		User uploader = userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		RRepository repositoryEntity = repositoryService.findByNameAndDeleted(repository, false)
			.orElseThrow(() -> new RepositoryNotFound(messageSource, locale));
		
		try {
			packageValidator.validate(multipartFile);
		} catch(MultipartFileValidationException e) {
			return handleValidationError(e);
		}
		
		PackageUploadRequest<RRepository> request = new PackageUploadRequest<>(
				multipartFile, repositoryEntity, generateManual, replace);
		
		Strategy<Submission> strategy = strategyFactory.uploadPackageStrategy(request, uploader);
		
		try {
			final Submission submission = strategy.perform();
			return handleCreatedForSingleEntity(submission);
		}
		catch (StrategyFailure e) {
			logger.error(e.getMessage(), e);
			if(e.getReason() instanceof PackageDuplicatedToSilentlyIgnore) {
				logger.warn("warning", Pair.of(multipartFile.getOriginalFilename(), e.getMessage()));
				return handleValidationErrorStateOK(e.getMessage());
			}
			if(e.getReason() instanceof PackageValidationException) {
				return handleValidationError(e.getReason());
			}
			
			throw new CreateException(messageSource, locale);
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
	public @ResponseBody ResponseDto<PagedModel<EntityModel<SubmissionDto>>> getAllSubmissions(
			Principal principal, Pageable pageable,
			@RequestParam(name = "state", required = false) Optional<SubmissionState> state,
			@RequestParam(name = "userId", required = false) Optional<Integer> userId,
			@RequestParam(name = "packageId", required = false) Optional<Integer> packageId) 
					throws UserNotAuthorized, UserNotFound {
		userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		
		Specification<Submission> specification = null;
		
		if(userId.isPresent()) {
			Optional<User> user = userService.findById(userId.get());
			
			if(user.isEmpty())
				return handleSuccessForPagedCollection(new PageImpl<>(List.of()));
			
			specification = SpecificationUtils
					.andComponent(specification, SubmissionSpecs.ofUser(user.get()));
		}
		if(state.isPresent()) {
			specification = SpecificationUtils
					.andComponent(specification, SubmissionSpecs.ofState(state.get()));
		}
		if(packageId.isPresent()) {
			Optional<RPackage> packageBag = packageService.findById(packageId.get());
			
			if(packageBag.isEmpty())
				return handleSuccessForPagedCollection(new PageImpl<>(List.of()));
			
			specification = SpecificationUtils
					.andComponent(specification, SubmissionSpecs.ofPackage(packageBag.get()));
		}
		
		if(specification != null) {
			return handleSuccessForPagedCollection(submissionService
					.findAllBySpecification(specification, pageable));
		} else {
			return handleSuccessForPagedCollection(submissionService.findAll(pageable));
		}
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
		Submission submission = submissionService.findById(id).orElseThrow(() -> new SubmissionNotFound(messageSource, locale));
		
		jsonPatch = fixPatch(jsonPatch); //So that it doesn't complain when state is written with lower case
		
		User requester = userService.findByLogin(principal.getName())
				.orElseThrow(() -> new SubmissionNotFound(messageSource, locale));
		RRepository repository = repositoryService.findById(submission.getPackage().getRepository().getId())
				.orElseThrow(() -> new SubmissionNotFound(messageSource, locale)); //If repository is not found for the technology, 
		//then certainly a user tries to fetch a submission for a wrong technology.
		
		try {
			SubmissionDto submissionDto = applyPatchToEntity(jsonPatch, submission);
			
			if(!securityMediator.isAuthorizedToEdit(submission, submissionDto, requester))
				throw new UserNotAuthorized(messageSource, locale);
			
			submissionPatchValidator.validatePatch(jsonPatch, submission, submissionDto);
			
			Strategy<Submission> strategy = strategyFactory
					.updateSubmissionStrategy(submission, 
							resolveDtoToEntity(submissionDto), repository, requester);
			submission = strategy.perform();
		} catch (StrategyFailure | ResolveRelatedEntitiesException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new ApplyPatchException(messageSource, locale);
		} catch (JsonProcessingException | JsonException | PatchValidationException e) {
			throw new MalformedPatchException(messageSource, locale, e);
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
	public @ResponseBody ResponseEntity<ResponseDto<EntityModel<SubmissionDto>>> getSubmissionById(
			Principal principal, @PathVariable Integer id) 
					throws SubmissionNotFound, UserNotAuthorized {
		userService.findByLogin(principal.getName())
			.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		Submission submission = submissionService.findById(id)
				.orElseThrow(() -> new SubmissionNotFound(messageSource, locale));
		return handleSuccessForSingleEntity(submission);
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
		Optional<User> requester = userService.findByLogin(principal.getName());
		
		if(requester.isEmpty() || requester.get().getRole().getValue() != Role.VALUE.ADMIN)
			throw new UserNotAuthorized(messageSource, locale);
		
		Submission submission = submissionService.findById(id).orElseThrow(() -> new SubmissionNotFound(messageSource, locale));
		
		try {
			submissionDeleter.delete(submission);
		} catch (DeleteEntityException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new DeleteException(messageSource, locale);
		}
	}
	
	@Override
	public Submission resolveDtoToEntity(SubmissionDto dto) throws ResolveRelatedEntitiesException {
		Package<?,?> packageBag = packageService.findById(dto.getEntity().getId())
				.orElseThrow(() -> new ResolveRelatedEntitiesException("package"));
		User user = userService.findById(dto.getUserId())
				.orElseThrow(() -> new ResolveRelatedEntitiesException("user"));
		return new Submission(dto, packageBag, user);
	}
}
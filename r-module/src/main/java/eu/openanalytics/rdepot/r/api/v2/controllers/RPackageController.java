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

import java.io.FileNotFoundException;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
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
import eu.openanalytics.rdepot.base.api.v2.controllers.ModifiableDataController;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApiException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApplyPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.DeleteException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.DownloadReferenceManualException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.DownloadVignetteException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.MalformedPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ManualNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.PackageNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ResolveRelatedEntitiesException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.api.v2.exceptions.VignetteNotFound;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.utils.specs.PackageSpecs;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageDuplicatedToSilentlyIgnore;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageValidationException;
import eu.openanalytics.rdepot.r.api.v2.dtos.RPackageDto;
import eu.openanalytics.rdepot.r.api.v2.hateoas.RPackageModelAssembler;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.entities.Vignette;
import eu.openanalytics.rdepot.r.mediator.deletion.RPackageDeleter;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.storage.RStorage;
import eu.openanalytics.rdepot.r.storage.exceptions.GetReferenceManualException;
import eu.openanalytics.rdepot.r.storage.exceptions.ReadPackageVignetteException;
import eu.openanalytics.rdepot.r.strategy.factory.RStrategyFactory;
import eu.openanalytics.rdepot.r.validation.RPackageValidator;
import jakarta.json.JsonException;
import jakarta.json.JsonPatch;

/**
 * REST controller implementation for R packages.
 */
@RestController
@RequestMapping(value = "/api/v2/manager/r/packages")
public class RPackageController extends ApiV2Controller<RPackage, RPackageDto> 
	implements ModifiableDataController<RPackage, RPackageDto>{
	
	private final Locale locale = LocaleContextHolder.getLocale();
	private final MessageSource messageSource;
	private final RPackageService packageService;
	private final UserService userService;
	private final RPackageValidator packageValidator;
	private final RRepositoryService repositoryService;
	private final RStrategyFactory strategyFactory;
	private final RPackageDeleter deleter;
	private final SecurityMediator securityMediator;
	private final RStorage storage;
	private final SubmissionService submissionService;
	
	public RPackageController(MessageSource messageSource,
			RPackageService packageService,
			RPackageModelAssembler packageModelAssembler,
			PagedResourcesAssembler<RPackage> pagedResourcesAssembler,
			ObjectMapper objectMapper,
			UserService userService,
			RPackageValidator packageValidator,
			RStrategyFactory strategyFactory,
			RPackageDeleter rPackageDeleter,
			RRepositoryService repositoryService,
			SecurityMediator securityMediator,
			RStorage storage, SubmissionService submissionService) {
		super(messageSource, LocaleContextHolder.getLocale(), 
				packageModelAssembler, pagedResourcesAssembler, objectMapper,
				RPackageDto.class, LoggerFactory.getLogger(RPackageController.class), packageValidator);
		this.packageService = packageService;
		this.messageSource = messageSource;
		this.userService = userService;
		this.packageValidator = packageValidator;
		this.strategyFactory = strategyFactory;
		this.deleter = rPackageDeleter;
		this.repositoryService = repositoryService;
		this.securityMediator = securityMediator;
		this.storage = storage;
		this.submissionService = submissionService;
	}
	
	/**
	 * Fetches all packages available for a user.
	 * @param principal used for authorization
	 * @param pageable carries parameters required for pagination
	 * @param repositoryName show only packages from a given repository
	 * @param deleted show only deleted packages, requires admin privileges
	 * @return
	 * @throws UserNotAuthorized 
	 */
	@PreAuthorize("hasAuthority('user')")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseDto<?> getAllPackages(
			Principal principal, Pageable pageable,
			@RequestParam(name="repositoryName", required=false) Optional<String> repositoryName,
			@RequestParam(name="deleted", required=false) Optional<Boolean> deleted,
			@RequestParam(name="submissionState", required=false, defaultValue="ACCEPTED") SubmissionState submissionState) 
					throws UserNotAuthorized {
		userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		
		Specification<RPackage> specification = null;
		
		if(repositoryName.isPresent()) {
			Optional<RRepository> repository = repositoryService.findByName(repositoryName.get());
			if(repository.isEmpty())
				return emptyPage();
			
			Specification<RPackage> component = PackageSpecs.ofRepository(repository.get());
			specification = SpecificationUtils.andComponent(specification, component);
		}
		
		if(deleted.isPresent()) {
			Specification<RPackage> component = PackageSpecs.isDeleted(deleted.get());
			specification = SpecificationUtils.andComponent(specification, component);
		}
		
		Specification<RPackage> component = PackageSpecs.ofSubmissionState(submissionState);
		specification = SpecificationUtils.andComponent(specification, component);
		
		if(specification == null) {
			return handleSuccessForPagedCollection(packageService.findAll(pageable));
		} else {
			return handleSuccessForPagedCollection(
					packageService.findAllBySpecification(specification, pageable));
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
	public @ResponseBody ResponseEntity<ResponseDto<EntityModel<RPackageDto>>> getPackageById(
			Principal principal, @PathVariable Integer id) 
			throws PackageNotFound, UserNotAuthorized {
		userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));			
		RPackage packageBag = packageService.findById(id)
				.orElseThrow(() -> new PackageNotFound(messageSource, locale));
		//TODO: If user is not an admin, package marked as deleted will not be found. 
		
		return handleSuccessForSingleEntity(packageBag);
	}
	
	/**
	 * Updates a package.
	 * The method follows JSON Patch standard (see {@link https://datatracker.ietf.org/doc/html/rfc6902}).
	 * @param principal used for authorization
	 * @param id
	 * @param patch JsonPatch object
	 * @param bindingResult used for validation
	 * @return Updated Package DTO
	 * @throws PackageNotFound
	 * @throws ApplyPatchException
	 */
	@PreAuthorize("hasAuthority('packagemaintainer')")
	@PatchMapping(path = "/{id}", consumes="application/json-patch+json")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<?> updatePackage(
				Principal principal, 
				@PathVariable Integer id, 
				@RequestBody JsonPatch patch) 
			throws ApiException {			
		RPackage packageBag = packageService.findById(id)
				.orElseThrow(() -> new PackageNotFound(messageSource, locale)); 
		final User requester = userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));				
		
		if(!securityMediator.isAuthorizedToEdit(packageBag, requester))
			throw new UserNotAuthorized(messageSource, locale);
		
		try {
			final RPackageDto packageDto = applyPatchToEntity(patch, packageBag);
			final RPackage updatedPackage = resolveDtoToEntity(packageDto);
			
			BindingResult bindingResult = createBindingResult(updatedPackage);
			packageValidator.validate(updatedPackage, true, bindingResult);
			
			if(bindingResult.hasErrors())
				return handleValidationError(bindingResult);
			
			Strategy<RPackage> strategy = strategyFactory.updatePackageStrategy(packageBag, requester, updatedPackage);
			
			packageBag = strategy.perform();					
		} catch(PackageValidationException | ResolveRelatedEntitiesException | PackageDuplicatedToSilentlyIgnore e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			return handleValidationError(e.getMessage());
		} catch(JsonProcessingException | JsonException e) {
			throw new MalformedPatchException(messageSource, locale, e);
		} catch (StrategyFailure e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
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
	public void shiftDeletePackage(Principal principal, @PathVariable Integer id) 
			throws PackageNotFound, DeleteException, UserNotAuthorized {		
		final RPackage packageBag = packageService.findOneDeleted(id)
				.orElseThrow(() -> new PackageNotFound(messageSource, locale)); 
		userService.findByLogin(principal.getName())
			.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		
		try {			
			deleter.delete(packageBag);
		} catch (DeleteEntityException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new DeleteException(messageSource, locale);
		}
	}
	
	/**
	 * Fetches links to package's Vignettes.
	 * @param id
	 * @return
	 * @throws PackageNotFound
	 */
	@GetMapping("/{id}/vignettes")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseDto<List<Vignette>> getVignetteLinks(@PathVariable Integer id) throws PackageNotFound {
		final RPackage packageBag = packageService.findOneNonDeleted(id).orElseThrow(() -> new PackageNotFound(messageSource, locale));
		
		return ResponseDto.generateSuccessBody(messageSource, locale, 
				storage.getAvailableVignettes(packageBag));
	}
	
	/**
	 * Fetches package PDF manual.
	 * @param id
	 * @return
	 * @throws PackageNotFound
	 * @throws ManualNotFound
	 * @throws DownloadReferenceManualException
	 */
	@GetMapping("/{id}/manual")
	public @ResponseBody ResponseEntity<byte[]> downloadReferenceManual(@PathVariable Integer id) 
			throws PackageNotFound, ManualNotFound, DownloadReferenceManualException {
		final RPackage packageBag = packageService.findOneNonDeleted(id).orElseThrow(() -> new PackageNotFound(messageSource, locale)); 

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.set("Content-Disposition", 
				"attachment; filename=\"" + packageBag.getName() 
				+ "_" + packageBag.getVersion() + "manual.pdf\"");
		
		try {
			byte[] manualRaw = storage.getReferenceManual(packageBag);
			
			return new ResponseEntity<>(manualRaw, headers, HttpStatus.OK);
		} catch(GetReferenceManualException e) {
			if(e.getReason() instanceof FileNotFoundException) {
				throw new ManualNotFound(messageSource, locale);
			}
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new DownloadReferenceManualException(messageSource, locale);
		}
	}
	
	/**
	 * Fetches a PDF vignette.
	 * @param id
	 * @param name vignette's name
	 * @return
	 * @throws PackageNotFound
	 * @throws VignetteNotFound
	 * @throws DownloadVignetteException
	 */
	@GetMapping("/{id}/vignettes/{name}.pdf")
	public @ResponseBody ResponseEntity<byte[]> downloadVignettePdf(@PathVariable Integer id,
			@PathVariable String name) 
					throws PackageNotFound, VignetteNotFound, DownloadVignetteException {
		return downloadVignette(id, name + ".pdf", MediaType.APPLICATION_PDF);
	}
	
	/**
	 * Fetches an HTML vignette.
	 * @param id
	 * @param name vignette's name
	 * @return
	 * @throws PackageNotFound
	 * @throws VignetteNotFound
	 * @throws DownloadVignetteException
	 */
	@GetMapping("/{id}/vignettes/{name}.html")
	public @ResponseBody ResponseEntity<byte[]> downloadVignetteHtml(@PathVariable Integer id,
			@PathVariable String name) 
					throws PackageNotFound, VignetteNotFound, DownloadVignetteException {
		return downloadVignette(id, name + ".html", MediaType.TEXT_HTML);
	}
	
	private ResponseEntity<byte[]> downloadVignette(Integer id, String filename, 
			MediaType mediaType) throws PackageNotFound, VignetteNotFound, DownloadVignetteException {
		final RPackage packageBag = packageService.findOneNonDeleted(id).orElseThrow(() -> new PackageNotFound(messageSource, locale));		
		
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(mediaType);
		headers.set("Content-Disposition", "attachment; filename= \""+filename+"\"");
		
		try {
			byte[] vignetteRaw = storage.readVignette(packageBag, filename);
			
			return new ResponseEntity<byte[]>(vignetteRaw, headers, HttpStatus.OK);
		} catch (ReadPackageVignetteException e) {
			if(e.getReason() instanceof FileNotFoundException) {
				throw new VignetteNotFound(messageSource, locale);
			}
			throw new DownloadVignetteException(messageSource, locale);
		} 
	}

	@Override
	public RPackage resolveDtoToEntity(RPackageDto dto) throws ResolveRelatedEntitiesException {
		RRepository repository = repositoryService.findById(dto.getRepositoryId())
				.orElseThrow(() -> new ResolveRelatedEntitiesException("repository"));
		User user = userService.findById(dto.getUserId())
				.orElseThrow(() -> new ResolveRelatedEntitiesException("user"));
		Submission submission = submissionService.findById(dto.getSubmissionId())
				.orElseThrow(() -> new ResolveRelatedEntitiesException("submission"));
		return new RPackage(dto, repository, submission, user);
	}
}

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

import java.io.FileNotFoundException;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import eu.openanalytics.rdepot.api.v2.dto.RPackageDto;
import eu.openanalytics.rdepot.api.v2.dto.ResponseDto;
import eu.openanalytics.rdepot.api.v2.exception.ApiException;
import eu.openanalytics.rdepot.api.v2.exception.ApplyPatchException;
import eu.openanalytics.rdepot.api.v2.exception.DeleteException;
import eu.openanalytics.rdepot.api.v2.exception.DownloadReferenceManualException;
import eu.openanalytics.rdepot.api.v2.exception.DownloadVignetteException;
import eu.openanalytics.rdepot.api.v2.exception.MalformedPatchException;
import eu.openanalytics.rdepot.api.v2.exception.ManualNotFound;
import eu.openanalytics.rdepot.api.v2.exception.PackageNotFound;
import eu.openanalytics.rdepot.api.v2.exception.UserNotAuthorized;
import eu.openanalytics.rdepot.api.v2.exception.VignetteNotFound;
import eu.openanalytics.rdepot.api.v2.hateoas.RPackageModelAssembler;
import eu.openanalytics.rdepot.exception.GetReferenceManualException;
import eu.openanalytics.rdepot.exception.PackageDeleteException;
import eu.openanalytics.rdepot.exception.PackageException;
import eu.openanalytics.rdepot.exception.PackageValidationException;
import eu.openanalytics.rdepot.exception.ReadPackageVignetteException;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.model.Vignette;
import eu.openanalytics.rdepot.service.PackageService;
import eu.openanalytics.rdepot.service.UserService;
import eu.openanalytics.rdepot.validation.PackageValidator;
import eu.openanalytics.rdepot.warning.PackageValidationWarning;
import jakarta.json.JsonException;
import jakarta.json.JsonPatch;

/**
 * REST controller implementation for R packages.
 */
@RestController
@RequestMapping("/api/v2/manager/r/packages")
public class RPackageController extends ApiV2Controller<Package, RPackageDto> {
	
	final private Locale locale = LocaleContextHolder.getLocale();
	final private MessageSource messageSource;
	final private PackageService packageService;
	final private UserService userService;
	final private PackageValidator packageValidator;
	
	@Autowired
	public RPackageController(MessageSource messageSource, 
			PackageService packageService, 
			RPackageModelAssembler packageModelAssembler,
			PagedResourcesAssembler<Package> pagedResourcesAssembler,
			ObjectMapper objectMapper,
			UserService userService,
			PackageValidator packageValidator) {
		super(messageSource, LocaleContextHolder.getLocale(), 
				packageModelAssembler, pagedResourcesAssembler, objectMapper,
				RPackageDto.class, LoggerFactory.getLogger(RPackageController.class), null);
		this.packageService = packageService;
		this.messageSource = messageSource;
		this.userService = userService;
		this.packageValidator = packageValidator;
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
	public @ResponseBody ResponseDto<PagedModel<EntityModel<RPackageDto>>> getAllPackages(
			Principal principal, 
//			@PageableDefault(page = 0, size = 20)
//		    @SortDefault.SortDefaults({
//		    	@SortDefault(sort = "repository.name", direction = Direction.ASC),
//		    	@SortDefault(sort = "package.name", direction = Direction.ASC),
//		        @SortDefault(sort = "package.version", direction = Direction.DESC)
//		    }) Pageable pageable, 
			Pageable pageable,
			@RequestParam(name="repositoryName", required=false) Optional<String> repositoryName,
			@RequestParam(name="deleted", required=false) Optional<Boolean> deleted) 
					throws UserNotAuthorized {
		
		final User requester = userService.findByLogin(principal.getName());
		
		if(requester == null) {
			throw new UserNotAuthorized(messageSource, locale);
		}
		
		Page<Package> pagedPackages = null;
		
		if(repositoryName.isEmpty() && deleted.isEmpty()) {				
			pagedPackages = packageService.findAllEvenDeleted(pageable);
		} else if(repositoryName.isEmpty() && deleted.isPresent()){
			pagedPackages = packageService.findAllByDeleted(deleted.get(), pageable);
		} else if(repositoryName.isPresent() && deleted.isEmpty()) {
			pagedPackages = packageService.findAllByRepositoryName(repositoryName.get(), pageable);
		} else {
			pagedPackages = packageService.findAllByRepositoryAndDeleted(repositoryName.get(), deleted.get(), pageable);
		}
		
		return handleSuccessForPagedCollection(pagedPackages); 
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
	public @ResponseBody ResponseEntity<ResponseDto<EntityModel<RPackageDto>>> getPackageById(Principal principal, @PathVariable Integer id) 
			throws PackageNotFound, UserNotAuthorized {
		final User requester = userService.findByLogin(principal.getName());
		
		if(requester == null)
			throw new UserNotAuthorized(messageSource, locale);			
		
		Package packageBag = packageService.findByIdEvenDeleted(id);
		
		if(packageBag == null)
			throw new PackageNotFound(messageSource, locale);
		
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
	public @ResponseBody ResponseEntity<?> updatePackage(Principal principal, 
			@PathVariable Integer id, @RequestBody JsonPatch patch) 
					throws ApiException {
		
		final User requester = userService.findByLogin(principal.getName());
		Package packageBag = packageService.findByIdEvenDeleted(id);
		
		if(packageBag == null)
			throw new PackageNotFound(messageSource, locale);
		
		if(requester == null || !userService.isAuthorizedToEdit(packageBag, requester))
			throw new UserNotAuthorized(messageSource, locale);
		
		try {
			RPackageDto packageDto = applyPatchToEntity(patch, packageBag);
			packageValidator.validate(packageDto.toEntity(), false);
			
			packageBag = packageService.evaluateAndUpdate(packageDto, requester);
			
		} catch(PackageException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new ApplyPatchException(messageSource, locale);
		} catch(PackageValidationException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			return handleValidationError(e.getMessage());
		} catch(PackageValidationWarning w) {
			logger.warn(w.getClass().getName() + ": " + w.getMessage(), w);
		} catch(JsonProcessingException | JsonException e) {
			throw new MalformedPatchException(messageSource, locale, e);
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
	 */
	@PreAuthorize("hasAuthority('admin')")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void shiftDeletePackage(Principal principal, @PathVariable Integer id) 
			throws PackageNotFound, DeleteException {
		final Package packageBag = packageService.findByIdAndDeleted(id, true);
		if(packageBag == null) {
			throw new PackageNotFound(messageSource, locale);
		}
		
		try {
			packageService.shiftDelete(packageBag);
		} catch (PackageDeleteException e) {
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
		final Package packageBag = packageService.findByIdAndDeleted(id, false);
		if(packageBag == null) {
			throw new PackageNotFound(messageSource, locale);
		}
		
		return ResponseDto.generateSuccessBody(messageSource, locale, 
				packageService.getAvailableVignettes(packageBag));
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
		final Package packageBag = packageService.findByIdAndDeleted(id, false);
		if(packageBag == null) {
			throw new PackageNotFound(messageSource, locale);
		}
		
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.set("Content-Disposition", 
				"attachment; filename=\"" + packageBag.getName() 
				+ "_" + packageBag.getVersion() + "manual.pdf\"");
		
		try {
			byte[] manualRaw = packageService.getReferenceManualInBytes(packageBag);
			
			return new ResponseEntity<>(manualRaw, headers, HttpStatus.OK);
		} catch(FileNotFoundException e) {
			throw new ManualNotFound(messageSource, locale);
		} catch(GetReferenceManualException e) {
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
		final Package packageBag = packageService.findById(id);
		
		if(packageBag == null) {
			throw new PackageNotFound(messageSource, locale);
		}
		
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(mediaType);
		headers.set("Content-Disposition", "attachment; filename= \""+filename+"\"");
		
		try {
			byte[] vignetteRaw = packageService.readVignette(packageBag, filename);
			
			return new ResponseEntity<byte[]>(vignetteRaw, headers, HttpStatus.OK);
		} catch(FileNotFoundException e) {
			throw new VignetteNotFound(messageSource, locale);
		} catch (ReadPackageVignetteException e) {
			throw new DownloadVignetteException(messageSource, locale);
		} 
	}
}

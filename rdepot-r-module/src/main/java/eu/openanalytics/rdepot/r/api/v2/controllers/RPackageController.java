/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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
package eu.openanalytics.rdepot.r.api.v2.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2Controller;
import eu.openanalytics.rdepot.base.api.v2.converters.exceptions.EntityResolutionException;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApiException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApplyPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.DeleteException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.DownloadReferenceManualException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.DownloadVignetteException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.MalformedPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ManualNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.PackageDeletionException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.PackageNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.api.v2.exceptions.VignetteNotFound;
import eu.openanalytics.rdepot.base.api.v2.resolvers.DtoResolvedPageable;
import eu.openanalytics.rdepot.base.api.v2.resolvers.PackagePageableSortResolver;
import eu.openanalytics.rdepot.base.api.v2.validation.PageableValidator;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.storage.exceptions.SourceNotFoundException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.StrategyExecutor;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.utils.specs.PackageSpecs;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import eu.openanalytics.rdepot.base.validation.DataSpecificValidationResult;
import eu.openanalytics.rdepot.base.validation.ValidationResultImpl;
import eu.openanalytics.rdepot.r.api.v2.converters.RPackageDtoConverter;
import eu.openanalytics.rdepot.r.api.v2.dtos.RPackageDto;
import eu.openanalytics.rdepot.r.api.v2.hateoas.RPackageModelAssembler;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.Vignette;
import eu.openanalytics.rdepot.r.mediator.deletion.RPackageDeleter;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.storage.RStorage;
import eu.openanalytics.rdepot.r.storage.exceptions.GetReferenceManualException;
import eu.openanalytics.rdepot.r.storage.exceptions.ReadPackageVignetteException;
import eu.openanalytics.rdepot.r.strategy.factory.RStrategyFactory;
import eu.openanalytics.rdepot.r.validation.RPackageValidator;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.json.JsonException;
import jakarta.json.JsonPatch;
import java.io.FileNotFoundException;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Value;
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

/**
 * REST controller implementation for R packages.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v2/manager/r/packages")
public class RPackageController extends ApiV2Controller<RPackage, RPackageDto> {

    @Value("${deleting.packages.enabled}")
    private Boolean packagesDeletionEnabled;

    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    private final RPackageService packageService;
    private final UserService userService;
    private final RPackageValidator packageValidator;
    private final RStrategyFactory strategyFactory;
    private final RPackageDeleter deleter;
    private final SecurityMediator securityMediator;
    private final RStorage storage;
    private final PageableValidator pageableValidator;
    private final PackagePageableSortResolver pageableSortResolver;
    private final StrategyExecutor strategyExecutor;

    public RPackageController(
            MessageSource messageSource,
            RPackageService packageService,
            RPackageModelAssembler packageModelAssembler,
            PagedResourcesAssembler<RPackage> pagedResourcesAssembler,
            ObjectMapper objectMapper,
            UserService userService,
            RPackageValidator packageValidator,
            RStrategyFactory strategyFactory,
            RPackageDeleter rPackageDeleter,
            SecurityMediator securityMediator,
            RStorage storage,
            PageableValidator pageableValidator,
            PackagePageableSortResolver pageableSortResolver,
            RPackageDtoConverter rPackageDtoConverter,
            StrategyExecutor strategyExecutor) {
        super(
                messageSource,
                LocaleContextHolder.getLocale(),
                packageModelAssembler,
                pagedResourcesAssembler,
                objectMapper,
                RPackageDto.class,
                Optional.empty(),
                rPackageDtoConverter);
        this.packageService = packageService;
        this.strategyExecutor = strategyExecutor;
        this.messageSource = messageSource;
        this.userService = userService;
        this.packageValidator = packageValidator;
        this.strategyFactory = strategyFactory;
        this.deleter = rPackageDeleter;
        this.securityMediator = securityMediator;
        this.storage = storage;
        this.pageableValidator = pageableValidator;
        this.pageableSortResolver = pageableSortResolver;
    }

    /**
     * Fetches all packages available for a user.
     * @param principal used for authorization
     * @param pageable carries parameters required for pagination
     * @param repositories show only packages from given repositories
     * @param deleted show only deleted packages, requires admin privileges
     * @param submissionStates show only submissions with one of these states
     */
    @PreAuthorize("hasAuthority('user')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PageableAsQueryParam
    @Operation(operationId = "getAllRPackages")
    public @ResponseBody ResponseDto<?> getAllPackages(
            Principal principal,
            @ParameterObject Pageable pageable,
            @RequestParam(name = "repository", required = false) List<String> repositories,
            @RequestParam(name = "deleted", required = false) Optional<Boolean> deleted,
            @RequestParam(name = "submissionState", required = false, defaultValue = "ACCEPTED")
                    List<SubmissionState> submissionStates,
            @RequestParam(name = "name", required = false) Optional<String> name)
            throws ApiException {
        final User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        final DtoResolvedPageable resolvedPageable = pageableSortResolver.resolve(pageable);
        pageableValidator.validate(PackageDto.class, resolvedPageable);

        Specification<RPackage> specification = null;

        if (Objects.nonNull(repositories)) {
            specification = SpecificationUtils.andComponent(specification, PackageSpecs.ofRepository(repositories));
        }

        if (deleted.isPresent()) {
            Specification<RPackage> component = PackageSpecs.isDeleted(deleted.get());
            specification = SpecificationUtils.andComponent(specification, component);

            // TODO: #32882 This is a temporary fix for 2.0; We should think of better solution.
            if (deleted.get().equals(true) && !securityMediator.canSeeDeleted(requester, RPackage.class)) {
                return emptyPage();
            }
        }

        if (name.isPresent()) {
            Specification<RPackage> component = PackageSpecs.ofName(name.get());
            specification = SpecificationUtils.andComponent(specification, component);
        }

        if (Objects.nonNull(submissionStates)) {
            Specification<RPackage> component = PackageSpecs.ofSubmissionState(submissionStates);
            specification = SpecificationUtils.andComponent(specification, component);
        }

        if (specification == null) {
            return handleSuccessForPagedCollection(packageService.findAll(resolvedPageable), requester);
        } else {
            return handleSuccessForPagedCollection(
                    packageService.findAllBySpecification(specification, resolvedPageable), requester);
        }
    }

    /**
     * Find a package of given id.
     * If user is not an admin, package marked as deleted will not be found.
     */
    @PreAuthorize("hasAuthority('user')")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "getRPackageById")
    public @ResponseBody ResponseEntity<ResponseDto<EntityModel<RPackageDto>>> getPackageById(
            Principal principal, @PathVariable("id") Integer id) throws PackageNotFound, UserNotAuthorized {
        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
        RPackage packageBag = packageService.findById(id).orElseThrow(() -> new PackageNotFound(messageSource, locale));
        if ((!userService.isAdmin(requester) && packageBag.isDeleted()))
            throw new PackageNotFound(messageSource, locale);

        return handleSuccessForSingleEntity(packageBag, requester);
    }

    /**
     * Updates a package.
     * The method follows JSON Patch standard
     * (see <a href="https://datatracker.ietf.org/doc/html/rfc6902">RFC 6902</a>).
     * @param principal used for authorization
     * @param id package id
     * @param patch JsonPatch object
     */
    @PreAuthorize("hasAuthority('packagemaintainer')")
    @PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<?> updatePackage(
            Principal principal, @PathVariable("id") Integer id, @RequestBody JsonPatch patch) throws ApiException {
        RPackage packageBag = packageService.findById(id).orElseThrow(() -> new PackageNotFound(messageSource, locale));
        final User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        if (!securityMediator.isAuthorizedToEdit(packageBag, requester))
            throw new UserNotAuthorized(messageSource, locale);

        try {
            final RPackageDto packageDto = applyPatchToEntity(patch, packageBag);
            final RPackage updatedPackage = dtoConverter.resolveDtoToEntity(packageDto);

            if (!packagesDeletionEnabled && updatedPackage.getDeleted() && !packageBag.getDeleted())
                throw new PackageDeletionException(messageSource, locale);

            final DataSpecificValidationResult<Submission> validationResult =
                    ValidationResultImpl.createDataSpecificResult();
            packageValidator.validate(updatedPackage, true, validationResult);

            if (validationResult.hasErrors()) return handleValidationError(validationResult);

            Strategy<RPackage> strategy = strategyFactory.updatePackageStrategy(packageBag, requester, updatedPackage);

            packageBag = strategyExecutor.execute(strategy);
        } catch (EntityResolutionException e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            return handleValidationError(e.getMessage());
        } catch (JsonProcessingException | JsonException e) {
            throw new MalformedPatchException(messageSource, locale, e);
        } catch (StrategyFailure e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw new ApplyPatchException(messageSource, locale);
        }

        return handleSuccessForSingleEntity(packageBag, requester);
    }

    /**
     * Erases package from database and file system. Requires admin privileges.
     */
    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void shiftDeletePackage(Principal principal, @PathVariable("id") Integer id) throws ApiException {
        final RPackage packageBag =
                packageService.findOneDeleted(id).orElseThrow(() -> new PackageNotFound(messageSource, locale));
        if (!userService.findActiveByLogin(principal.getName()).isPresent()) {
            throw new UserNotAuthorized(messageSource, locale);
        }

        if (!packagesDeletionEnabled) throw new PackageDeletionException(messageSource, locale);

        try {
            deleter.delete(packageBag);
        } catch (DeleteEntityException e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw new DeleteException(messageSource, locale);
        }
    }

    /**
     * Fetches links to package's Vignettes.
     */
    @GetMapping("/{id}/vignettes")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseDto<List<Vignette>> getVignetteLinks(@PathVariable("id") Integer id)
            throws PackageNotFound {
        final RPackage packageBag =
                packageService.findOneNonDeleted(id).orElseThrow(() -> new PackageNotFound(messageSource, locale));

        return ResponseDto.generateSuccessBody(messageSource, locale, storage.getAvailableVignettes(packageBag));
    }

    /**
     * This method provides a package binary file to download.
     * @param id package ID
     * @param name package name
     * @param version package version
     */
    @GetMapping(value = "/{id}/download/{name}_{version}.tar.gz")
    @Operation(operationId = "downloadRPackage")
    public @ResponseBody ResponseEntity<byte[]> downloadPackage(
            @PathVariable("id") Integer id, @PathVariable("name") String name, @PathVariable("version") String version)
            throws ApiException {
        RPackage packageBag =
                packageService.findOneNonDeleted(id).orElseThrow(() -> new PackageNotFound(messageSource, locale));
        byte[] bytes = null;
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpStatus httpStatus = HttpStatus.OK;

        try {
            bytes = storage.getPackageInBytes(packageBag);
        } catch (SourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        httpHeaders.set("Content-Type", "application/gzip");
        httpHeaders.set(CONTENT_DISPOSITION, "attachment; filename= \"" + name + "_" + version + ".tar.gz\"");

        return new ResponseEntity<>(bytes, httpHeaders, httpStatus);
    }

    /**
     * Fetches package PDF manual.
     */
    @GetMapping("/{id}/manual")
    public @ResponseBody ResponseEntity<byte[]> downloadReferenceManual(@PathVariable("id") Integer id)
            throws PackageNotFound, ManualNotFound, DownloadReferenceManualException {
        final RPackage packageBag =
                packageService.findOneNonDeleted(id).orElseThrow(() -> new PackageNotFound(messageSource, locale));

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(
                CONTENT_DISPOSITION,
                "attachment; filename=\"" + packageBag.getName() + "_" + packageBag.getVersion() + "_manual.pdf\"");

        try {
            byte[] manualRaw = storage.getReferenceManual(packageBag);

            return new ResponseEntity<>(manualRaw, headers, HttpStatus.OK);
        } catch (GetReferenceManualException e) {
            if (e.getReason() instanceof FileNotFoundException) {
                throw new ManualNotFound(messageSource, locale);
            }
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw new DownloadReferenceManualException(messageSource, locale);
        }
    }

    /**
     * Fetches a PDF vignette.
     * @param id package id
     * @param name vignette's name
     */
    @GetMapping("/{id}/vignettes/{name}.pdf")
    public @ResponseBody ResponseEntity<byte[]> downloadVignettePdf(
            @PathVariable("id") Integer id, @PathVariable("name") String name)
            throws PackageNotFound, VignetteNotFound, DownloadVignetteException {
        return downloadVignette(id, name + ".pdf", MediaType.APPLICATION_PDF);
    }

    /**
     * Fetches an HTML vignette.
     * @param id
     * @param name vignette's name
     */
    @GetMapping("/{id}/vignettes/{name}.html")
    public @ResponseBody ResponseEntity<byte[]> downloadVignetteHtml(
            @PathVariable("id") Integer id, @PathVariable("name") String name)
            throws PackageNotFound, VignetteNotFound, DownloadVignetteException {
        return downloadVignette(id, name + ".html", MediaType.TEXT_HTML);
    }

    private ResponseEntity<byte[]> downloadVignette(Integer id, String filename, MediaType mediaType)
            throws PackageNotFound, VignetteNotFound, DownloadVignetteException {
        final RPackage packageBag =
                packageService.findOneNonDeleted(id).orElseThrow(() -> new PackageNotFound(messageSource, locale));

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.set(CONTENT_DISPOSITION, "attachment; filename= \"" + filename + "\"");

        try {
            byte[] vignetteRaw = storage.readVignette(packageBag, filename);

            return new ResponseEntity<byte[]>(vignetteRaw, headers, HttpStatus.OK);
        } catch (ReadPackageVignetteException e) {
            if (e.getReason() instanceof FileNotFoundException) {
                throw new VignetteNotFound(messageSource, locale);
            }
            throw new DownloadVignetteException(messageSource, locale);
        }
    }
}

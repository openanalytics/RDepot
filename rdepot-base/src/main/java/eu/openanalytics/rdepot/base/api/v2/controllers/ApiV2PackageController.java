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

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApiException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.PackageNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.api.v2.hateoas.PackageModelAssembler;
import eu.openanalytics.rdepot.base.api.v2.resolvers.DtoResolvedPageable;
import eu.openanalytics.rdepot.base.api.v2.resolvers.PackagePageableSortResolver;
import eu.openanalytics.rdepot.base.api.v2.validation.PageableValidator;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.utils.specs.PackageSpecs;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import io.swagger.v3.oas.annotations.Operation;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only REST Controller for all {@link Package Packages},
 * regardless of their technology.
 */
@RestController
@RequestMapping(value = "/api/v2/manager/packages")
public class ApiV2PackageController extends ApiV2ReadingController<Package, PackageDto> {

    private final PackageService<Package> packageService;
    private final UserService userService;
    private final PageableValidator pageableValidator;

    private final PackagePageableSortResolver pageableSortResolver;

    public ApiV2PackageController(
            MessageSource messageSource,
            PackageModelAssembler modelAssembler,
            PagedResourcesAssembler<Package> pagedModelAssembler,
            UserService userService,
            PackageService<Package> packageService,
            PageableValidator pageableValidator,
            PackagePageableSortResolver pageableSortResolver) {
        super(messageSource, LocaleContextHolder.getLocale(), modelAssembler, pagedModelAssembler);
        this.packageService = packageService;
        this.userService = userService;
        this.pageableValidator = pageableValidator;
        this.pageableSortResolver = pageableSortResolver;
    }

    /**
     * Fetches all {@link Package Packages} based on provided filters and pagination.
     * @param principal represents authenticated user
     * @param pageable represents pagination parameters
     * @param repositories names of {@link Repository Repositories} whose package will be fetched
     * @param deleted specifies if soft-deleted packages should be included
     * @param submissionStates specifies which packages should be included based on the {@link SubmissionState state} of their {@link Submission}, defaults to "ACCEPTED"
     * @param technologies technologies that packages are related to, e.g. R or Python
     * @param search free-text phrase for fuzzy matching on package name
     * @param maintainers logins of {@link User users} whose maintained packages should be included
     * @return {@link PackageDto DTOs} wrapped with {@link ResponseDto}
     * @throws ApiException HTTP errors (both 4xx and 5xx) wrapped with {@link ResponseDto}
     */
    @PreAuthorize("hasAuthority('user')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "getAllPackages")
    @PageableAsQueryParam
    public @ResponseBody ResponseDto<PagedModel<EntityModel<PackageDto>>> getAllPackages(
            Principal principal,
            @ParameterObject Pageable pageable,
            @RequestParam(name = "repository", required = false) List<String> repositories,
            @RequestParam(name = "deleted", required = false) Optional<Boolean> deleted,
            @RequestParam(name = "submissionState", required = false, defaultValue = "ACCEPTED")
                    List<SubmissionState> submissionStates,
            @RequestParam(name = "technology", required = false) List<String> technologies,
            @RequestParam(name = "search", required = false) Optional<String> search,
            @RequestParam(name = "maintainer", required = false) List<String> maintainers)
            throws ApiException {
        User requester = userService
                .findByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
        if ((!userService.isAdmin(requester) && deleted.isPresent() && deleted.get()))
            throw new UserNotAuthorized(messageSource, locale);

        final DtoResolvedPageable resolvedPageable = pageableSortResolver.resolve(pageable);
        pageableValidator.validate(PackageDto.class, resolvedPageable);

        Specification<Package> specification = null;

        if (Objects.nonNull(repositories)) {
            specification = SpecificationUtils.component(PackageSpecs.ofRepository(repositories));
        }

        if (deleted.isPresent()) {
            specification = SpecificationUtils.andComponent(specification, PackageSpecs.isDeleted(deleted.get()));
        }

        if (search.isPresent()) {
            specification = SpecificationUtils.andComponent(specification, PackageSpecs.ofName(search.get()));
        }

        if (Objects.nonNull(technologies)) {
            specification = SpecificationUtils.andComponent(specification, PackageSpecs.ofTechnology(technologies));
        }

        if (Objects.nonNull(submissionStates)) {
            specification =
                    SpecificationUtils.andComponent(specification, PackageSpecs.ofSubmissionState(submissionStates));
        }

        if (Objects.nonNull(maintainers)) {
            specification = SpecificationUtils.andComponent(specification, PackageSpecs.ofMaintainer(maintainers));
        }

        return handleSuccessForPagedCollection(
                packageService.findAllBySpecification(specification, resolvedPageable), requester);
    }

    /**
     * Find a package of given id.
     * If user is not an admin (see: {@link Role}),
     * package marked as deleted will not be found.
     * @return {@link PackageDto DTO} wrapped with {@link ResponseDto}
     * @throws ApiException HTTP errors (both 4xx and 5xx) wrapped with {@link ResponseDto}
     */
    @PreAuthorize("hasAuthority('user')")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "getPackageById")
    public @ResponseBody ResponseEntity<ResponseDto<EntityModel<PackageDto>>> getPackageById(
            Principal principal, @PathVariable("id") Integer id) throws ApiException {

        User requester = userService
                .findByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
        Package packageBag = packageService.findById(id).orElseThrow(() -> new PackageNotFound(messageSource, locale));

        if ((!userService.isAdmin(requester) && packageBag.getDeleted()))
            throw new PackageNotFound(messageSource, locale);
        return handleSuccessForSingleEntity(packageBag, requester);
    }
}

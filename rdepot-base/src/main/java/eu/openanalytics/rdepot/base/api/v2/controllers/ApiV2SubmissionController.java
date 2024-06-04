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

import eu.openanalytics.rdepot.base.api.v2.converters.SubmissionDtoConverter;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.SubmissionDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApiException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.SubmissionNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.api.v2.hateoas.SubmissionModelAssembler;
import eu.openanalytics.rdepot.base.api.v2.resolvers.CommonPageableSortResolver;
import eu.openanalytics.rdepot.base.api.v2.resolvers.DtoResolvedPageable;
import eu.openanalytics.rdepot.base.api.v2.validation.PageableValidator;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import eu.openanalytics.rdepot.base.utils.specs.SubmissionSpecs;
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

@RestController
@RequestMapping(value = "/api/v2/manager/submissions")
public class ApiV2SubmissionController extends ApiV2ReadingController<Submission, SubmissionDto> {

    private SubmissionService submissionService;
    private UserService userService;
    private final PageableValidator pageableValidator;

    private final CommonPageableSortResolver pageableSortResolver;

    public ApiV2SubmissionController(
            MessageSource messageSource,
            SubmissionModelAssembler modelAssembler,
            PagedResourcesAssembler<Submission> pagedModelAssembler,
            SubmissionService submissionService,
            UserService userService,
            PackageService<Package> packageService,
            PackageMaintainerService packageMaintainerService,
            SubmissionDtoConverter submissionDtoConverter,
            PageableValidator pageableValidator,
            CommonPageableSortResolver pageableSortResolver) {

        super(messageSource, LocaleContextHolder.getLocale(), modelAssembler, pagedModelAssembler);
        this.submissionService = submissionService;
        this.userService = userService;
        this.pageableValidator = pageableValidator;
        this.pageableSortResolver = pageableSortResolver;
    }

    /**
     * Fetches all submissions.
     * @param principal used for authorization
     * @param pageable carries parameters required for pagination
     * @return collection of submissions
     */
    @PreAuthorize("hasAuthority('user')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PageableAsQueryParam
    @Operation(operationId = "getAllSubmissions")
    public @ResponseBody ResponseDto<PagedModel<EntityModel<SubmissionDto>>> getAllSubmissions(
            Principal principal,
            @ParameterObject Pageable pageable,
            @RequestParam(name = "state", required = false) List<SubmissionState> states,
            @RequestParam(name = "technology", required = false) List<String> technologies,
            @RequestParam(name = "repository", required = false) List<String> repositories,
            @RequestParam(name = "fromDate", required = false) Optional<String> fromDate,
            @RequestParam(name = "toDate", required = false) Optional<String> toDate,
            @RequestParam(name = "search", required = false) Optional<String> search)
            throws ApiException {

        User requester = userService
                .findByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        final DtoResolvedPageable resolvedPageable = pageableSortResolver.resolve(pageable);
        pageableValidator.validate(SubmissionDto.class, resolvedPageable);

        Specification<Submission> specification = null;

        if (Objects.nonNull(states)) {
            specification = SpecificationUtils.andComponent(specification, SubmissionSpecs.ofState(states));
        }

        if (Objects.nonNull(technologies)) {
            specification = SpecificationUtils.andComponent(specification, SubmissionSpecs.ofTechnology(technologies));
        }

        if (Objects.nonNull(repositories)) {
            specification = SpecificationUtils.andComponent(specification, SubmissionSpecs.ofRepository(repositories));
        }

        if (fromDate.isPresent()) {
            specification = SpecificationUtils.andComponent(specification, SubmissionSpecs.fromDate(fromDate.get()));
        }

        if (toDate.isPresent()) {
            specification = SpecificationUtils.andComponent(specification, SubmissionSpecs.toDate(toDate.get()));
        }

        if (search.isPresent()) {
            specification = SpecificationUtils.andComponent(
                    specification,
                    SubmissionSpecs.ofApprover(search.get())
                            .or(SubmissionSpecs.ofSubmitter(search.get()))
                            .or(SubmissionSpecs.ofPackage(search.get())));
        }

        if (specification != null) {
            return handleSuccessForPagedCollection(
                    submissionService.findAllBySpecification(specification, resolvedPageable), requester);
        } else {
            return handleSuccessForPagedCollection(submissionService.findAll(resolvedPageable), requester);
        }
    }

    /**
     * Find a submission of given id
     */
    @PreAuthorize("hasAuthority('user')")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "getSubmissionById")
    public @ResponseBody ResponseEntity<ResponseDto<EntityModel<SubmissionDto>>> getSubmissionById(
            Principal principal, @PathVariable("id") Integer id) throws SubmissionNotFound, UserNotAuthorized {

        if (!userService.findByLogin(principal.getName()).isPresent()) {
            throw new UserNotAuthorized(messageSource, locale);
        }
        Submission submission =
                submissionService.findById(id).orElseThrow(() -> new SubmissionNotFound(messageSource, locale));

        return handleSuccessForSingleEntity(submission);
    }
}

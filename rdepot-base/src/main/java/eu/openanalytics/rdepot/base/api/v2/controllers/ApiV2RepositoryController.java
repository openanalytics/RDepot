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
package eu.openanalytics.rdepot.base.api.v2.controllers;

import eu.openanalytics.rdepot.base.api.v2.dtos.RepositoryDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApiException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.RepositoryNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.api.v2.hateoas.RepositoryModelAssembler;
import eu.openanalytics.rdepot.base.api.v2.resolvers.CommonPageableSortResolver;
import eu.openanalytics.rdepot.base.api.v2.resolvers.DtoResolvedPageable;
import eu.openanalytics.rdepot.base.api.v2.validation.PageableValidator;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.utils.specs.RepositorySpecs;
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
 * REST Controller for Repositories.
 */
@RestController
@RequestMapping(value = "/api/v2/manager/repositories")
public class ApiV2RepositoryController extends ApiV2ReadingController<Repository, RepositoryDto> {

    private final RepositoryService<Repository> repositoryService;
    private final UserService userService;
    private final PageableValidator pageableValidator;
    private final CommonPageableSortResolver pageableSortResolver;

    public ApiV2RepositoryController(
            MessageSource messageSource,
            RepositoryModelAssembler modelAssembler,
            PagedResourcesAssembler<Repository> pagedModelAssembler,
            UserService userService,
            RepositoryService<Repository> repositoryService,
            PageableValidator pageableValidator,
            CommonPageableSortResolver pageableSortResolver) {
        super(messageSource, LocaleContextHolder.getLocale(), modelAssembler, pagedModelAssembler);
        this.repositoryService = repositoryService;
        this.userService = userService;
        this.pageableValidator = pageableValidator;
        this.pageableSortResolver = pageableSortResolver;
    }

    /**
     * Fetches all repositories.
     * @param principal used for authorization
     * @param pageable carries parameters required for pagination
     * @param deleted show only deleted repositories, requires admin privileges
     */
    @PreAuthorize("hasAuthority('user')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "getAllRepositories")
    @PageableAsQueryParam
    public @ResponseBody ResponseDto<PagedModel<EntityModel<RepositoryDto>>> getAllRepositories(
            Principal principal,
            @ParameterObject Pageable pageable,
            @RequestParam(name = "deleted", required = false, defaultValue = "false") Boolean deleted,
            @RequestParam(name = "technology", required = false) List<String> technologies,
            @RequestParam(name = "published", required = false) Optional<Boolean> published,
            @RequestParam(name = "maintainer", required = false) List<String> maintainers,
            @RequestParam(name = "name", required = false) Optional<String> name,
            @RequestParam(name = "search", required = false) Optional<String> search)
            throws ApiException {

        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        final DtoResolvedPageable resolvedPageable = pageableSortResolver.resolve(pageable);
        pageableValidator.validate(RepositoryDto.class, resolvedPageable);

        if ((!userService.isAdmin(requester) && deleted)) throw new UserNotAuthorized(messageSource, locale);

        Specification<Repository> specification =
                SpecificationUtils.andComponent(null, RepositorySpecs.isDeleted(deleted));

        if (Objects.nonNull(technologies)) {
            specification = SpecificationUtils.andComponent(specification, RepositorySpecs.ofTechnology(technologies));
        }

        if (published.isPresent()) {
            specification =
                    SpecificationUtils.andComponent(specification, RepositorySpecs.isPublished(published.get()));
        }

        if (Objects.nonNull(maintainers)) {
            specification = SpecificationUtils.andComponent(specification, RepositorySpecs.ofMaintainer(maintainers));
        }

        if (name.isPresent()) {
            specification = SpecificationUtils.andComponent(specification, RepositorySpecs.ofName(name.get()));
        }

        if (search.isPresent()) {
            specification =
                    SpecificationUtils.andComponent(specification, RepositorySpecs.ofNameSearching(search.get()));
        }

        return handleSuccessForPagedCollection(
                repositoryService.findAllBySpecification(specification, resolvedPageable), requester);
    }

    /**
     * Find a repository of given id.
     */
    @PreAuthorize("hasAuthority('user')")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "getRepositoryById")
    public @ResponseBody ResponseEntity<ResponseDto<EntityModel<RepositoryDto>>> getRepositoryById(
            Principal principal, @PathVariable("id") Integer id) throws UserNotAuthorized, RepositoryNotFound {
        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        Repository repository =
                repositoryService.findById(id).orElseThrow(() -> new RepositoryNotFound(messageSource, locale));

        if ((!userService.isAdmin(requester) && repository.getDeleted()))
            throw new RepositoryNotFound(messageSource, locale);

        return handleSuccessForSingleEntity(repository, requester);
    }
}

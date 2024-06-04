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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.rdepot.base.api.v2.converters.RepositoryMaintainerDtoConverter;
import eu.openanalytics.rdepot.base.api.v2.converters.exceptions.EntityResolutionException;
import eu.openanalytics.rdepot.base.api.v2.dtos.RepositoryMaintainerDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApiException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApplyPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.CreateException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.DeleteException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.MalformedPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.RepositoryMaintainerNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.api.v2.hateoas.RepositoryMaintainerModelAssembler;
import eu.openanalytics.rdepot.base.api.v2.resolvers.CommonPageableSortResolver;
import eu.openanalytics.rdepot.base.api.v2.resolvers.DtoResolvedPageable;
import eu.openanalytics.rdepot.base.api.v2.validation.PageableValidator;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.deletion.RepositoryMaintainerDeleter;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.factory.StrategyFactory;
import eu.openanalytics.rdepot.base.utils.specs.RepositoryMaintainerSpecs;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import eu.openanalytics.rdepot.base.validation.RepositoryMaintainerValidator;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.json.JsonException;
import jakarta.json.JsonPatch;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
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

/**
 * REST Controller for Repository Maintainers. All endpoints require
 * administrator privileges.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v2/manager/repository-maintainers")
@PreAuthorize("hasAuthority('admin')")
public class ApiV2RepositoryMaintainerController
        extends ApiV2Controller<RepositoryMaintainer, RepositoryMaintainerDto> {

    private final RepositoryMaintainerValidator repositoryMaintainerValidator;
    private final UserService userService;

    private final RepositoryMaintainerService repositoryMaintainerService;
    private final StrategyFactory factory;
    private final RepositoryMaintainerDeleter repositoryMaintainerDeleter;
    private final PageableValidator pageableValidator;
    private final CommonPageableSortResolver pageableSortResolver;

    public ApiV2RepositoryMaintainerController(
            MessageSource messageSource,
            RepositoryMaintainerModelAssembler modelAssembler,
            PagedResourcesAssembler<RepositoryMaintainer> pagedModelAssembler,
            ObjectMapper objectMapper,
            RepositoryMaintainerValidator repositoryMaintainerValidator,
            UserService userService,
            StrategyFactory factory,
            RepositoryMaintainerService repositoryMaintainerService,
            RepositoryMaintainerDeleter repositoryMaintainerDeleter,
            RepositoryMaintainerDtoConverter repositoryMaintainerDtoConverter,
            PageableValidator pageableValidator,
            CommonPageableSortResolver pageableSortResolver) {
        super(
                messageSource,
                LocaleContextHolder.getLocale(),
                modelAssembler,
                pagedModelAssembler,
                objectMapper,
                RepositoryMaintainerDto.class,
                Optional.of(repositoryMaintainerValidator),
                repositoryMaintainerDtoConverter);
        this.repositoryMaintainerValidator = repositoryMaintainerValidator;
        this.userService = userService;
        this.factory = factory;
        this.repositoryMaintainerService = repositoryMaintainerService;
        this.repositoryMaintainerDeleter = repositoryMaintainerDeleter;
        this.pageableValidator = pageableValidator;
        this.pageableSortResolver = pageableSortResolver;
    }

    /**
     * Fetches all maintainers.
     *
     * @param pageable used for pagination
     * @param deleted  filters deleted maintainers
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PageableAsQueryParam
    @Operation(operationId = "getAllRepositoryMaintainers")
    public @ResponseBody ResponseDto<PagedModel<EntityModel<RepositoryMaintainerDto>>> getAllRepositoryMaintainers(
            @ParameterObject Pageable pageable,
            @RequestParam(name = "deleted", required = false) Optional<Boolean> deleted,
            @RequestParam(name = "resourceTechnology", required = false) List<String> technologies,
            @RequestParam(name = "search", required = false) Optional<String> search,
            Principal principal)
            throws ApiException {
        User requester = userService
                .findByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        final DtoResolvedPageable resolvedPageable = pageableSortResolver.resolve(pageable);
        pageableValidator.validate(RepositoryMaintainerDto.class, resolvedPageable);

        Page<RepositoryMaintainer> maintainers = null;

        Specification<RepositoryMaintainer> specification = null;

        if (deleted.isPresent()) {
            specification =
                    SpecificationUtils.andComponent(specification, RepositoryMaintainerSpecs.isDeleted(deleted.get()));
        }
        if (Objects.nonNull(technologies)) {
            specification = SpecificationUtils.andComponent(
                    specification, RepositoryMaintainerSpecs.ofTechnology(technologies));
        }

        if (search.isPresent()) {
            specification = SpecificationUtils.andComponent(
                    specification,
                    RepositoryMaintainerSpecs.ofUser(search.get())
                            .or(RepositoryMaintainerSpecs.ofRepository(search.get())));
        }

        maintainers = repositoryMaintainerService.findAllBySpecification(specification, resolvedPageable);
        return handleSuccessForPagedCollection(maintainers, requester);
    }

    /**
     * Fetches single maintainer of given ID.
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "getRepositoryMaintainersById")
    public @ResponseBody ResponseEntity<ResponseDto<EntityModel<RepositoryMaintainerDto>>> getMaintainer(
            @PathVariable("id") Integer id, Principal principal) throws ApiException {
        User requester = userService
                .findByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        RepositoryMaintainer maintainer = repositoryMaintainerService
                .findById(id)
                .orElseThrow(() -> new RepositoryMaintainerNotFound(messageSource, locale));

        return handleSuccessForSingleEntity(maintainer, requester);
    }

    /**
     * Creates a maintainer.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createRepositoryMaintainer")
    public @ResponseBody ResponseEntity<?> createMaintainer(
            Principal principal, @RequestBody RepositoryMaintainerDto repositoryMaintainerDto) throws ApiException {
        User requester = userService
                .findByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        RepositoryMaintainer repositoryMaintainer = null;
        try {
            repositoryMaintainer = dtoConverter.resolveDtoToEntity(repositoryMaintainerDto);
        } catch (EntityResolutionException e) {
            return handleValidationError(e);
        }

        BindingResult bindingResult = createBindingResult(repositoryMaintainer);
        repositoryMaintainerValidator.validate(repositoryMaintainer, bindingResult);

        if (bindingResult.hasErrors()) return handleValidationError(bindingResult);

        try {
            Strategy<RepositoryMaintainer> strategy =
                    factory.createRepositoryMaintainerStrategy(repositoryMaintainer, requester);
            RepositoryMaintainer created = strategy.perform();
            return handleCreatedForSingleEntity(created, requester);
        } catch (StrategyFailure e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw new CreateException(messageSource, locale);
        }
    }

    /**
     * Updates maintainer
     */
    @PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "updateRepositoryMaintainer")
    public @ResponseBody ResponseEntity<?> updateRepositoryMaintainer(
            Principal principal, @PathVariable("id") Integer id, @RequestBody JsonPatch jsonPatch) throws ApiException {
        User requester = userService
                .findByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        RepositoryMaintainer repositoryMaintainer = repositoryMaintainerService
                .findById(id)
                .orElseThrow(() -> new RepositoryMaintainerNotFound(messageSource, locale));
        RepositoryMaintainer updated;

        try {
            RepositoryMaintainerDto repositoryMaintainerDto = applyPatchToEntity(jsonPatch, repositoryMaintainer);
            RepositoryMaintainer entity = dtoConverter.resolveDtoToEntity(repositoryMaintainerDto);
            BindingResult bindingResult = createBindingResult(entity);

            repositoryMaintainerValidator.validate(entity, bindingResult);

            if (bindingResult.hasErrors()) {
                return handleValidationError(bindingResult);
            }

            Strategy<RepositoryMaintainer> strategy =
                    factory.updateRepositoryMaintainerStrategy(repositoryMaintainer, requester, entity);

            updated = strategy.perform();
        } catch (StrategyFailure e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw new ApplyPatchException(messageSource, locale);
        } catch (JsonException | JsonProcessingException e) {
            throw new MalformedPatchException(messageSource, locale, e);
        } catch (EntityResolutionException e) {
            return handleValidationError(e);
        }

        return handleSuccessForSingleEntity(updated, requester);
    }

    /**
     * Deletes maintainer of given ID. This is a permanent delete.
     */
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteRepositoryMaintainer")
    public void deleteRepositoryMaintainer(@PathVariable("id") Integer id, Principal principal) throws ApiException {
        if (!userService.findByLogin(principal.getName()).isPresent()) {
            throw new UserNotAuthorized(messageSource, locale);
        }

        RepositoryMaintainer maintainer = repositoryMaintainerService
                .findOneDeleted(id)
                .orElseThrow(() -> new RepositoryMaintainerNotFound(messageSource, locale));
        try {
            repositoryMaintainerDeleter.delete(maintainer);
        } catch (DeleteEntityException e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw new DeleteException(messageSource, locale);
        }
    }
}

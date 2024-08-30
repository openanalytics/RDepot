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
import eu.openanalytics.rdepot.base.api.v2.converters.PackageMaintainerDtoConverter;
import eu.openanalytics.rdepot.base.api.v2.converters.exceptions.EntityResolutionException;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageMaintainerDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApiException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApplyPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.CreateException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.DeleteException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.MalformedPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.PackageMaintainerNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.api.v2.hateoas.PackageMaintainerModelAssembler;
import eu.openanalytics.rdepot.base.api.v2.resolvers.CommonPageableSortResolver;
import eu.openanalytics.rdepot.base.api.v2.resolvers.DtoResolvedPageable;
import eu.openanalytics.rdepot.base.api.v2.validation.PageableValidator;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.deletion.PackageMaintainerDeleter;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.StrategyExecutor;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.factory.StrategyFactory;
import eu.openanalytics.rdepot.base.utils.specs.PackageMaintainerSpecs;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import eu.openanalytics.rdepot.base.validation.PackageMaintainerValidator;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.json.JsonException;
import jakarta.json.JsonPatch;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
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
 * REST Controller implementation for {@link PackageMaintainer Package Maintainers}.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v2/manager/package-maintainers")
public class ApiV2PackageMaintainerController extends ApiV2Controller<PackageMaintainer, PackageMaintainerDto> {

    private final UserService userService;
    private final PackageMaintainerValidator packageMaintainerValidator;
    private final PackageMaintainerService packageMaintainerService;
    private final StrategyFactory factory;
    private final RepositoryMaintainerService repositoryMaintainerService;
    private final PackageMaintainerDeleter deleter;
    private final SecurityMediator securityMediator;
    private final PageableValidator pageableValidator;
    private final CommonPageableSortResolver pageableSortResolver;
    private final StrategyExecutor strategyExecutor;

    public ApiV2PackageMaintainerController(
            MessageSource messageSource,
            PackageMaintainerModelAssembler modelAssembler,
            PagedResourcesAssembler<PackageMaintainer> pagedModelAssembler,
            ObjectMapper objectMapper,
            UserService userService,
            PackageMaintainerValidator packageMaintainerValidator,
            PackageMaintainerService packageMaintainerService,
            StrategyFactory strategyFactory,
            PackageMaintainerDeleter packageMaintainerDeleter,
            RepositoryMaintainerService repositoryMaintainerService,
            SecurityMediator securityMediator,
            PackageMaintainerDtoConverter packageMaintainerDtoConverter,
            PageableValidator pageableValidator,
            CommonPageableSortResolver pageableSortResolver,
            StrategyExecutor strategyExecutor) {

        super(
                messageSource,
                LocaleContextHolder.getLocale(),
                modelAssembler,
                pagedModelAssembler,
                objectMapper,
                PackageMaintainerDto.class,
                Optional.of(packageMaintainerValidator),
                packageMaintainerDtoConverter);
        this.userService = userService;
        this.packageMaintainerValidator = packageMaintainerValidator;
        this.packageMaintainerService = packageMaintainerService;
        this.factory = strategyFactory;
        this.deleter = packageMaintainerDeleter;
        this.repositoryMaintainerService = repositoryMaintainerService;
        this.securityMediator = securityMediator;
        this.pageableValidator = pageableValidator;
        this.pageableSortResolver = pageableSortResolver;
        this.strategyExecutor = strategyExecutor;
    }

    /**
     * Fetches all maintainers.
     * @param principal represents authenticated user
     * @param pageable represents pagination parameters
     * @param deleted filters deleted maintainers
     * @return {@link PackageMaintainerDto DTOs} wrapped with {@link ResponseDto}
     * @throws ApiException HTTP errors (both 4xx and 5xx) wrapped with {@link ResponseDto}
     */
    @PreAuthorize("hasAuthority('repositorymaintainer')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PageableAsQueryParam
    @Operation(operationId = "getAllPackageMaintainers")
    public @ResponseBody ResponseDto<PagedModel<EntityModel<PackageMaintainerDto>>> getAllMaintainers(
            Principal principal,
            @ParameterObject Pageable pageable,
            @RequestParam(name = "deleted", required = false) Optional<Boolean> deleted,
            @RequestParam(name = "technology", required = false) List<String> technologies,
            @RequestParam(name = "repository", required = false) List<String> repositories,
            @RequestParam(name = "search", required = false) Optional<String> search)
            throws ApiException {
        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        final DtoResolvedPageable resolvedPageable = pageableSortResolver.resolve(pageable);
        pageableValidator.validate(PackageMaintainerDto.class, resolvedPageable);

        Specification<PackageMaintainer> specification = null;

        if (deleted.isPresent()) {
            specification = SpecificationUtils.component(PackageMaintainerSpecs.isDeleted(deleted.get()));
        }

        if (requester.getRole().getValue() == Role.VALUE.REPOSITORYMAINTAINER) {
            List<String> repos = repositoryMaintainerService.findByUserWithoutDeleted(requester).stream()
                    .map(m -> m.getRepository().getName())
                    .collect(Collectors.toList());

            Specification<PackageMaintainer> allowedRepositories =
                    SpecificationUtils.component(PackageMaintainerSpecs.ofRepository(repos));
            specification = SpecificationUtils.andComponent(specification, allowedRepositories);
        }

        if (Objects.nonNull(repositories)) {
            specification =
                    SpecificationUtils.andComponent(specification, PackageMaintainerSpecs.ofRepository(repositories));
        }

        if (Objects.nonNull(technologies)) {
            specification =
                    SpecificationUtils.andComponent(specification, PackageMaintainerSpecs.ofTechnology(technologies));
        }

        if (search.isPresent()) {
            specification = SpecificationUtils.andComponent(
                    specification,
                    PackageMaintainerSpecs.byMaintainer(search.get())
                            .or(PackageMaintainerSpecs.ofPackageName(search.get())));
        }

        if (specification != null) {
            return handleSuccessForPagedCollection(
                    packageMaintainerService.findAllBySpecification(specification, resolvedPageable), requester);
        } else {
            return handleSuccessForPagedCollection(packageMaintainerService.findAll(resolvedPageable), requester);
        }
    }

    /**
     * Fetches single maintainer of given ID.
     */
    @PreAuthorize("hasAuthority('repositorymaintainer')")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "getPackageMaintainerById")
    public @ResponseBody ResponseEntity<ResponseDto<EntityModel<PackageMaintainerDto>>> getMaintainer(
            Principal principal, @PathVariable("id") Integer id) throws ApiException {
        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        PackageMaintainer maintainer = packageMaintainerService
                .findById(id)
                .orElseThrow(() -> new PackageMaintainerNotFound(messageSource, locale));

        if (!securityMediator.isAuthorizedToSee(maintainer, requester))
            throw new UserNotAuthorized(messageSource, locale);

        return handleSuccessForSingleEntity(maintainer, requester);
    }

    /**
     * Creates a single maintainer.
     */
    @PreAuthorize("hasAuthority('repositorymaintainer')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createPackageMaintainer")
    public @ResponseBody ResponseEntity<?> createMaintainer(
            Principal principal, @RequestBody PackageMaintainerDto packageMaintainerDto) throws ApiException {
        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        PackageMaintainer packageMaintainer;
        try {
            packageMaintainer = dtoConverter.resolveDtoToEntity(packageMaintainerDto);
        } catch (EntityResolutionException e) {
            return handleValidationError(e);
        }

        if (!securityMediator.isAuthorizedToEdit(packageMaintainer, requester))
            throw new UserNotAuthorized(messageSource, locale);

        BindingResult bindingResult = createBindingResult(packageMaintainer);

        packageMaintainerValidator.validate(packageMaintainer, bindingResult);

        if (bindingResult.hasErrors()) return handleValidationError(bindingResult);

        Strategy<PackageMaintainer> strategy = factory.createPackageMaintainerStrategy(packageMaintainer, requester);

        try {
            PackageMaintainer created = strategyExecutor.execute(strategy);
            return handleCreatedForSingleEntity(created, requester);
        } catch (StrategyFailure e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw new CreateException(messageSource, locale);
        }
    }

    /**
     * Updates package maintainer.
     */
    @PreAuthorize("hasAuthority('repositorymaintainer')")
    @PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "updatePackageMaintainer")
    public @ResponseBody ResponseEntity<?> updatePackageMaintainer(
            Principal principal, @PathVariable("id") Integer id, @RequestBody JsonPatch jsonPatch) throws ApiException {

        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
        PackageMaintainer packageMaintainer = packageMaintainerService
                .findById(id)
                .orElseThrow(() -> new PackageMaintainerNotFound(messageSource, locale));

        if (!securityMediator.isAuthorizedToEdit(packageMaintainer, requester))
            throw new UserNotAuthorized(messageSource, locale);

        PackageMaintainer updated;
        try {
            PackageMaintainerDto packageMaintainerDto = applyPatchToEntity(jsonPatch, packageMaintainer);
            PackageMaintainer entity = dtoConverter.resolveDtoToEntity(packageMaintainerDto);

            BindingResult bindingResult = createBindingResult(entity);

            packageMaintainerValidator.validate(entity, bindingResult);

            if (bindingResult.hasErrors()) return handleValidationError(bindingResult);

            Strategy<PackageMaintainer> strategy =
                    factory.updatePackageMaintainerStrategy(packageMaintainer, requester, entity);

            updated = strategyExecutor.execute(strategy);
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
     * Deletes maintainer of given ID. This is a permanent delete!
     */
    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deletePackageMaintainer")
    public void deletePackageMaintainer(Principal principal, @PathVariable("id") Integer id) throws ApiException {
        Optional<User> requester = userService.findActiveByLogin(principal.getName());

        if (requester.isEmpty() || requester.get().getRole().getValue() != Role.VALUE.ADMIN) {
            throw new UserNotAuthorized(messageSource, locale);
        }

        PackageMaintainer packageMaintainer = packageMaintainerService
                .findOneDeleted(id)
                .orElseThrow(() -> new PackageMaintainerNotFound(messageSource, locale));
        try {
            deleter.delete(packageMaintainer);
        } catch (DeleteEntityException e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw new DeleteException(messageSource, locale);
        }
    }
}

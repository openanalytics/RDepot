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
import eu.openanalytics.rdepot.base.api.v2.dtos.RepositoryDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApiException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApplyPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.CreateException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.DeleteException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.MalformedPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.NotAllowedInDeclarativeMode;
import eu.openanalytics.rdepot.base.api.v2.exceptions.RepositoryDeletionException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.RepositoryNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.SynchronizationNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.api.v2.resolvers.CommonPageableSortResolver;
import eu.openanalytics.rdepot.base.api.v2.resolvers.DtoResolvedPageable;
import eu.openanalytics.rdepot.base.api.v2.validation.PageableValidator;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.SynchronizationStatus;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.StrategyExecutor;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.utils.specs.RepositorySpecs;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import eu.openanalytics.rdepot.r.api.v2.converters.RRepositoryDtoConverter;
import eu.openanalytics.rdepot.r.api.v2.dtos.RRepositoryDto;
import eu.openanalytics.rdepot.r.api.v2.hateoas.RRepositoryModelAssembler;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.mediator.deletion.RRepositoryDeleter;
import eu.openanalytics.rdepot.r.mirroring.CranMirrorSynchronizer;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.strategy.factory.RStrategyFactory;
import eu.openanalytics.rdepot.r.validation.RRepositoryValidator;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.json.JsonException;
import jakarta.json.JsonPatch;
import java.security.Principal;
import java.util.List;
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
 * REST controller implementation for R repositories.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v2/manager/r/repositories")
public class RRepositoryController extends ApiV2Controller<RRepository, RRepositoryDto> {

    private final RRepositoryService repositoryService;
    private final UserService userService;
    private final RRepositoryValidator repositoryValidator;
    private final CranMirrorSynchronizer mirrorSynchronizer;
    private final SecurityMediator securityMediator;

    private final RStrategyFactory factory;
    private final RRepositoryDeleter deleter;
    private final PageableValidator pageableValidator;
    private final CommonPageableSortResolver pageableSortResolver;
    private final StrategyExecutor strategyExecutor;

    @Value("${declarative}")
    private String declarative;

    @Value("${deleting.repositories.enabled}")
    private Boolean repositoriesDeletionEnabled;

    public RRepositoryController(
            MessageSource messageSource,
            RRepositoryModelAssembler modelAssembler,
            PagedResourcesAssembler<RRepository> pagedModelAssembler,
            ObjectMapper objectMapper,
            RRepositoryService repositoryService,
            UserService userService,
            RRepositoryValidator repositoryValidator,
            CranMirrorSynchronizer cranMirrorSynchronizer,
            RStrategyFactory factory,
            RRepositoryDeleter rRepositoryDeleter,
            SecurityMediator securityMediator,
            RRepositoryDtoConverter rRepositoryDtoConverter,
            PageableValidator pageableValidator,
            CommonPageableSortResolver pageableSortResolver,
            StrategyExecutor strategyExecutor) {
        super(
                messageSource,
                LocaleContextHolder.getLocale(),
                modelAssembler,
                pagedModelAssembler,
                objectMapper,
                RRepositoryDto.class,
                Optional.of(repositoryValidator),
                rRepositoryDtoConverter);
        this.repositoryService = repositoryService;
        this.userService = userService;
        this.repositoryValidator = repositoryValidator;
        this.mirrorSynchronizer = cranMirrorSynchronizer;
        this.factory = factory;
        this.deleter = rRepositoryDeleter;
        this.securityMediator = securityMediator;
        this.pageableValidator = pageableValidator;
        this.pageableSortResolver = pageableSortResolver;
        this.strategyExecutor = strategyExecutor;
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
    @PageableAsQueryParam
    @Operation(operationId = "getAllRRepositories")
    public @ResponseBody ResponseDto<PagedModel<EntityModel<RRepositoryDto>>> getAllRepositories(
            Principal principal,
            @ParameterObject Pageable pageable,
            @RequestParam(name = "deleted", required = false, defaultValue = "false") Boolean deleted,
            @RequestParam(name = "name", required = false) Optional<String> name)
            throws ApiException {

        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        if ((!userService.isAdmin(requester) && deleted)) throw new UserNotAuthorized(messageSource, locale);

        final DtoResolvedPageable resolvedPageable = pageableSortResolver.resolve(pageable);
        pageableValidator.validate(RepositoryDto.class, resolvedPageable);

        Specification<RRepository> specs = SpecificationUtils.andComponent(null, RepositorySpecs.isDeleted(deleted));
        if (name.isPresent()) specs = SpecificationUtils.andComponent(specs, RepositorySpecs.ofName(name.get()));
        return handleSuccessForPagedCollection(
                repositoryService.findAllBySpecification(specs, resolvedPageable), requester);
    }

    @GetMapping("/bad-endpoint")
    public @ResponseBody ResponseDto<PagedModel<EntityModel<RRepositoryDto>>> crash() throws ApiException {
        throw new ApplyPatchException(messageSource, locale);
    }

    /**
     * Find a repository of given id.
     */
    @PreAuthorize("hasAuthority('user')")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "getRRepositoryById")
    public @ResponseBody ResponseEntity<ResponseDto<EntityModel<RRepositoryDto>>> getRepositoryById(
            Principal principal, @PathVariable("id") Integer id) throws UserNotAuthorized, RepositoryNotFound {
        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        RRepository repository =
                repositoryService.findById(id).orElseThrow(() -> new RepositoryNotFound(messageSource, locale));

        if ((!userService.isAdmin(requester) && repository.isDeleted()))
            throw new RepositoryNotFound(messageSource, locale);

        return handleSuccessForSingleEntity(repository, requester);
    }

    /**
     * Create repository.
     * @param principal used for authorization
     * @param repositoryDto object representing created repository
     * @throws NotAllowedInDeclarativeMode if declarative mode is turned on,
     * it should be impossible to create a repository.
     */
    @PreAuthorize("hasAuthority('admin')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createRRepository")
    public @ResponseBody ResponseEntity<?> createRepository(
            Principal principal, @RequestBody RRepositoryDto repositoryDto)
            throws NotAllowedInDeclarativeMode, UserNotAuthorized, CreateException {
        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
        if (!userService.isAdmin(requester)) throw new UserNotAuthorized(messageSource, locale);

        if (Boolean.parseBoolean(declarative)) throw new NotAllowedInDeclarativeMode(messageSource, locale);

        try {
            RRepository repositoryEntity = dtoConverter.resolveDtoToEntity(repositoryDto);
            BindingResult bindingResult = createBindingResult(repositoryEntity);

            repositoryValidator.validate(repositoryEntity, bindingResult);

            if (bindingResult.hasErrors()) return handleValidationError(bindingResult);

            Strategy<RRepository> strategy = factory.createRepositoryStrategy(repositoryEntity, requester);

            RRepository repository = strategyExecutor.execute(strategy);
            return handleCreatedForSingleEntity(repository, requester);
        } catch (StrategyFailure e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new CreateException(messageSource, locale);
        } catch (EntityResolutionException e) {
            return handleValidationError(e);
        }
    }

    /**
     * Updates a repository.
     * The method follows JSON Patch standard
     * (see <a href="https://datatracker.ietf.org/doc/html/rfc6902">the specification</a>).
     * @param principal used for authorization
     * @param id repository id
     * @param jsonPatch JsonPatch object
     */
    @PreAuthorize("hasAuthority('repositorymaintainer')")
    @PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "updateRRepository")
    public @ResponseBody ResponseEntity<?> updateRepository(
            Principal principal, @PathVariable("id") Integer id, @RequestBody JsonPatch jsonPatch) throws ApiException {
        RRepository repository =
                repositoryService.findById(id).orElseThrow(() -> new RepositoryNotFound(messageSource, locale));

        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
        if (!securityMediator.isAuthorizedToEdit(repository, requester))
            throw new UserNotAuthorized(messageSource, locale);

        if (Boolean.parseBoolean(declarative)) throw new NotAllowedInDeclarativeMode(messageSource, locale);

        try {
            RRepositoryDto repositoryDto = applyPatchToEntity(jsonPatch, repository);
            RRepository updated = dtoConverter.resolveDtoToEntity(repositoryDto);
            BindingResult bindingResult = createBindingResult(updated);

            if (!repositoriesDeletionEnabled && updated.getDeleted() && !repository.getDeleted())
                throw new RepositoryDeletionException(messageSource, locale);

            repositoryValidator.validate(updated, bindingResult);

            if (bindingResult.hasErrors()) return handleValidationError(bindingResult);

            Strategy<RRepository> strategy = factory.updateRepositoryStrategy(repository, requester, updated);

            repository = strategyExecutor.execute(strategy);
        } catch (JsonException | JsonProcessingException e) {
            throw new MalformedPatchException(messageSource, locale, e);
        } catch (StrategyFailure e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new ApplyPatchException(messageSource, locale);
        } catch (EntityResolutionException e) {
            return handleValidationError(e);
        }

        return handleSuccessForSingleEntity(repository, requester);
    }

    /**
     * Erases repository from database and file system.
     * Requires admin privileges and the repository to be previously set "deleted".
     */
    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteRRepository")
    public void deleteRepository(Principal principal, @PathVariable("id") Integer id) throws ApiException {
        Optional<User> requester = userService.findActiveByLogin(principal.getName());

        if (requester.isEmpty() || requester.get().getRole().getValue() != Role.VALUE.ADMIN)
            throw new UserNotAuthorized(messageSource, locale);

        RRepository repository =
                repositoryService.findById(id).orElseThrow(() -> new RepositoryNotFound(messageSource, locale));

        if (Boolean.parseBoolean(declarative)) throw new NotAllowedInDeclarativeMode(messageSource, locale);

        if (!repositoriesDeletionEnabled) throw new RepositoryDeletionException(messageSource, locale);

        try {
            deleter.delete(repository);
        } catch (DeleteEntityException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new DeleteException(messageSource, locale);
        }
    }

    /**
     * Synchronizes repository with mirror.
     */
    @PreAuthorize("hasAuthority('repositorymaintainer')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{id}/synchronize-mirrors")
    public void synchronizeWithMirrors(@PathVariable("id") Integer id, Principal principal) throws ApiException {
        if (userService.findActiveByLogin(principal.getName()).isEmpty()) {
            throw new UserNotAuthorized(messageSource, locale);
        }
        RRepository repository =
                repositoryService.findById(id).orElseThrow(() -> new RepositoryNotFound(messageSource, locale));

        mirrorSynchronizer
                .findByRepository(repository)
                .forEach(m -> mirrorSynchronizer.synchronizeAsync(repository, m));
    }

    /**
     * Fetches synchronization status for the given repository.
     */
    @PreAuthorize("hasAuthority('repositorymaintainer')")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{id}/synchronization-status")
    public ResponseDto<?> getSynchronizationStatus(@PathVariable("id") Integer id, Principal principal)
            throws ApiException {
        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
        RRepository repository =
                repositoryService.findById(id).orElseThrow(() -> new RepositoryNotFound(messageSource, locale));

        if (!securityMediator.isAuthorizedToEdit(repository, requester))
            throw new UserNotAuthorized(messageSource, locale);

        List<SynchronizationStatus> status =
                mirrorSynchronizer
                        .getSynchronizationStatusList(); // TODO: #32977 it would be better to fetch it for specific
        // repository

        for (SynchronizationStatus s : status) {
            if (s.getRepositoryId() == repository.getId())
                return ResponseDto.generateSuccessBody(messageSource, locale, s);
        }

        throw new SynchronizationNotFound(messageSource, locale);
    }

    /**
     * Republish repository.
     * @param principal used for authorization
     * @param id ID of repository to republish
     */
    @PreAuthorize("hasAuthority('repositorymaintainer')")
    @PostMapping(value = "/{id}/republish")
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "republishRRepository")
    public @ResponseBody ResponseEntity<?> republishRepository(@PathVariable("id") Integer id, Principal principal)
            throws UserNotAuthorized, CreateException, RepositoryNotFound {

        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        RRepository repository =
                repositoryService.findById(id).orElseThrow(() -> new RepositoryNotFound(messageSource, locale));

        try {
            BindingResult bindingResult = createBindingResult(repository);

            repositoryValidator.validate(repository, bindingResult);

            if (bindingResult.hasErrors()) return handleValidationError(bindingResult);

            Strategy<RRepository> strategy;
            if (repository.getPublished()) {
                strategy = factory.republishRepositoryStrategy(repository, requester);
            } else {
                RRepository publishedRepo = new RRepository(repository);
                publishedRepo.setPublished(true);
                strategy = factory.updateRepositoryStrategy(repository, requester, publishedRepo);
            }

            repository = strategyExecutor.execute(strategy);
            return handleSuccessForSingleEntity(repository, requester);
        } catch (StrategyFailure e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new CreateException(messageSource, locale);
        }
    }
}

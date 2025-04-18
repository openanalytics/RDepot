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
package eu.openanalytics.rdepot.python.api.v2.controllers;

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
import eu.openanalytics.rdepot.base.api.v2.hateoas.RoleAwareRepresentationModelAssembler;
import eu.openanalytics.rdepot.base.api.v2.resolvers.CommonPageableSortResolver;
import eu.openanalytics.rdepot.base.api.v2.resolvers.DtoResolvedPageable;
import eu.openanalytics.rdepot.base.api.v2.validation.PageableValidator;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.SynchronizationStatus;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.StrategyExecutor;
import eu.openanalytics.rdepot.base.strategy.exceptions.EditingDeletedResourceException;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.utils.specs.RepositorySpecs;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import eu.openanalytics.rdepot.python.api.v2.converters.PythonRepositoryDtoConverter;
import eu.openanalytics.rdepot.python.api.v2.dtos.PythonRepositoryDto;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonRepositoryDeleter;
import eu.openanalytics.rdepot.python.mirroring.PypiMirrorSynchronizer;
import eu.openanalytics.rdepot.python.services.PythonRepositoryService;
import eu.openanalytics.rdepot.python.strategy.factory.PythonStrategyFactory;
import eu.openanalytics.rdepot.python.validation.PythonRepositoryValidator;
import eu.openanalytics.rdepot.python.validation.exceptions.PythonRepositoryValidationError;
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
 * REST controller implementation for Python repositories.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v2/manager/python/repositories")
public class PythonRepositoryController extends ApiV2Controller<PythonRepository, PythonRepositoryDto> {

    private final PythonStrategyFactory factory;
    private final UserService userService;
    private final PythonRepositoryService pythonRepositoryService;
    private final SecurityMediator securityMediator;
    private final PythonRepositoryValidator validator;
    private final PythonRepositoryDeleter deleter;
    private final PageableValidator pageableValidator;
    private final CommonPageableSortResolver pageableSortResolver;
    private final StrategyExecutor strategyExecutor;
    private final PypiMirrorSynchronizer mirrorSynchronizer;

    @Value("${declarative}")
    private String declarative;

    @Value("${deleting.repositories.enabled}")
    private Boolean repositoriesDeletionEnabled;

    public PythonRepositoryController(
            MessageSource messageSource,
            RoleAwareRepresentationModelAssembler<PythonRepository, EntityModel<PythonRepositoryDto>> modelAssembler,
            PagedResourcesAssembler<PythonRepository> pagedModelAssembler,
            ObjectMapper objectMapper,
            UserService userService,
            PythonRepositoryService pythonRepositoryService,
            PythonStrategyFactory factory,
            PythonRepositoryValidator validator,
            SecurityMediator securityMediator,
            PythonRepositoryDtoConverter dtoConverter,
            PythonRepositoryDeleter deleter,
            PageableValidator pageableValidator,
            CommonPageableSortResolver pageableSortResolver,
            StrategyExecutor strategyExecutor,
            PypiMirrorSynchronizer mirrorSynchronizer) {
        super(
                messageSource,
                LocaleContextHolder.getLocale(),
                modelAssembler,
                pagedModelAssembler,
                objectMapper,
                PythonRepositoryDto.class,
                Optional.of(validator),
                dtoConverter);
        this.securityMediator = securityMediator;
        this.factory = factory;
        this.pythonRepositoryService = pythonRepositoryService;
        this.validator = validator;
        this.userService = userService;
        this.deleter = deleter;
        this.pageableValidator = pageableValidator;
        this.pageableSortResolver = pageableSortResolver;
        this.strategyExecutor = strategyExecutor;
        this.mirrorSynchronizer = mirrorSynchronizer;
    }

    @PreAuthorize("hasAuthority('user')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PageableAsQueryParam
    @Operation(operationId = "getAllPythonRepositories")
    public @ResponseBody ResponseDto<PagedModel<EntityModel<PythonRepositoryDto>>> getAllRepositories(
            Principal principal,
            @ParameterObject Pageable pageable,
            @RequestParam(name = "deleted", required = false, defaultValue = "false") Boolean deleted,
            @RequestParam(name = "name", required = false) Optional<String> name)
            throws ApiException {

        User requester = getRequester(principal);
        if ((!userService.isAdmin(requester) && deleted)) throw new UserNotAuthorized(messageSource, locale);

        final DtoResolvedPageable resolvedPageable = pageableSortResolver.resolve(pageable);
        pageableValidator.validate(RepositoryDto.class, resolvedPageable);

        Specification<PythonRepository> specs =
                SpecificationUtils.andComponent(null, RepositorySpecs.isDeleted(deleted));
        if (name.isPresent()) specs = SpecificationUtils.andComponent(specs, RepositorySpecs.ofName(name.get()));
        return handleSuccessForPagedCollection(
                pythonRepositoryService.findAllBySpecification(specs, resolvedPageable), requester);
    }

    /**
     * Find a repository of given id.
     * @param principal used for authorization
     * @param id
     * @return Repository DTO
     * @throws UserNotAuthorized
     * @throws RepositoryNotFound
     */
    @PreAuthorize("hasAuthority('user')")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "getPythonRepositoryById")
    public @ResponseBody ResponseEntity<ResponseDto<EntityModel<PythonRepositoryDto>>> getRepositoryById(
            Principal principal, @PathVariable("id") Integer id) throws UserNotAuthorized, RepositoryNotFound {
        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        PythonRepository repository =
                pythonRepositoryService.findById(id).orElseThrow(() -> new RepositoryNotFound(messageSource, locale));

        if ((!userService.isAdmin(requester) && repository.isDeleted()))
            throw new RepositoryNotFound(messageSource, locale);

        return handleSuccessForSingleEntity(repository, requester);
    }

    /**
     *
     * @param principal
     * @param repositoryDto
     * @return response with status and created object / handled error
     * @throws UserNotAuthorized
     * @throws CreateException
     * @throws NotAllowedInDeclarativeMode
     */
    @PreAuthorize("hasAuthority('admin')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createPythonRepository")
    public @ResponseBody ResponseEntity<?> createRepository(
            Principal principal, @RequestBody PythonRepositoryDto repositoryDto)
            throws UserNotAuthorized, CreateException, NotAllowedInDeclarativeMode {
        User requester = getRequester(principal);
        checkIfUserIsAdmin(requester);
        checkDeclarative();
        try {
            PythonRepository repositoryEntity = dtoConverter.resolveDtoToEntity(repositoryDto);
            validate(repositoryEntity);
            Strategy<PythonRepository> strategy = factory.createRepositoryStrategy(repositoryEntity, requester);
            PythonRepository repository = strategyExecutor.execute(strategy);
            return handleCreatedForSingleEntity(repository, requester);
        } catch (PythonRepositoryValidationError e) {
            return handleValidationError(e.getBindingResult());
        } catch (StrategyFailure e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new CreateException(messageSource, LocaleContextHolder.getLocale());
        } catch (EntityResolutionException e) {
            return handleValidationError(e);
        }
    }

    @PreAuthorize("hasAuthority('repositorymaintainer')")
    @PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "updatePythonRepository")
    public @ResponseBody ResponseEntity<?> updateRepository(
            Principal principal, @PathVariable("id") Integer id, @RequestBody JsonPatch jsonPatch) throws ApiException {
        User requester = getRequester(principal);

        PythonRepository repository =
                pythonRepositoryService.findById(id).orElseThrow(() -> new RepositoryNotFound(messageSource, locale));

        if (!securityMediator.isAuthorizedToEdit(repository, requester))
            throw new UserNotAuthorized(messageSource, locale);

        checkDeclarative();

        if (repository.isDeleted())
            throw new EditingDeletedResourceException(
                    MessageCodes.EDITING_DELETED_RESOURCE_NOT_POSSIBLE, messageSource, locale);

        try {
            PythonRepositoryDto repositoryDto = applyPatchToEntity(jsonPatch, repository);
            PythonRepository updated = dtoConverter.resolveDtoToEntity(repositoryDto);

            if (!repositoriesDeletionEnabled && updated.getDeleted() && !repository.getDeleted())
                throw new RepositoryDeletionException(messageSource, locale);

            validate(updated);
            Strategy<PythonRepository> strategy = factory.updateRepositoryStrategy(repository, requester, updated);
            repository = strategyExecutor.execute(strategy);
        } catch (JsonException | JsonProcessingException e) {
            throw new MalformedPatchException(messageSource, locale, e);
        } catch (StrategyFailure e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new ApplyPatchException(messageSource, locale);
        } catch (PythonRepositoryValidationError e) {
            return handleValidationError(e.getBindingResult());
        } catch (EntityResolutionException e) {
            return handleValidationError(e);
        }

        return handleSuccessForSingleEntity(repository, requester);
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deletePythonRepository")
    public void deleteRepository(Principal principal, @PathVariable("id") Integer id) throws ApiException {
        Optional<User> requester = userService.findActiveByLogin(principal.getName());
        if (requester.isEmpty() || requester.get().getRole().getValue() != Role.VALUE.ADMIN)
            throw new UserNotAuthorized(messageSource, locale);

        PythonRepository repository =
                pythonRepositoryService.findById(id).orElseThrow(() -> new RepositoryNotFound(messageSource, locale));

        checkDeclarative();

        if (!repositoriesDeletionEnabled) throw new RepositoryDeletionException(messageSource, locale);

        try {
            deleter.delete(repository);
        } catch (DeleteEntityException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new DeleteException(messageSource, locale);
        }
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

        PythonRepository repository =
                pythonRepositoryService.findById(id).orElseThrow(() -> new RepositoryNotFound(messageSource, locale));

        try {
            BindingResult bindingResult = createBindingResult(repository);

            validator.validate(repository, bindingResult);

            if (bindingResult.hasErrors()) return handleValidationError(bindingResult);

            Strategy<PythonRepository> strategy;
            if (repository.getPublished()) {
                strategy = factory.republishRepositoryStrategy(repository, requester);
            } else {
                PythonRepository publishedRepo = new PythonRepository(repository);
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
        PythonRepository repository =
                pythonRepositoryService.findById(id).orElseThrow(() -> new RepositoryNotFound(messageSource, locale));

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
        PythonRepository repository =
                pythonRepositoryService.findById(id).orElseThrow(() -> new RepositoryNotFound(messageSource, locale));

        if (!securityMediator.isAuthorizedToEdit(repository, requester))
            throw new UserNotAuthorized(messageSource, locale);

        List<SynchronizationStatus> status = mirrorSynchronizer.getSynchronizationStatusList();

        for (SynchronizationStatus s : status) {
            if (s.getRepositoryId() == repository.getId())
                return ResponseDto.generateSuccessBody(messageSource, locale, s);
        }

        throw new SynchronizationNotFound(messageSource, locale);
    }

    private void checkDeclarative() throws NotAllowedInDeclarativeMode {
        if (Boolean.parseBoolean(declarative)) throw new NotAllowedInDeclarativeMode(messageSource, locale);
    }

    private void validate(PythonRepository entity) throws PythonRepositoryValidationError {
        BindingResult bindingResult = createBindingResult(entity);
        validator.validate(entity, bindingResult);
        if (bindingResult.hasErrors()) throw new PythonRepositoryValidationError(bindingResult);
    }

    private User getRequester(Principal principal) throws UserNotAuthorized {
        return userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, LocaleContextHolder.getLocale()));
    }

    private void checkIfUserIsAdmin(User user) throws UserNotAuthorized {
        if (!userService.isAdmin(user)) throw new UserNotAuthorized(messageSource, LocaleContextHolder.getLocale());
    }
}

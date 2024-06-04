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
package eu.openanalytics.rdepot.python.api.v2.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2Controller;
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
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.api.v2.hateoas.RoleAwareRepresentationModelAssembler;
import eu.openanalytics.rdepot.base.api.v2.resolvers.CommonPageableSortResolver;
import eu.openanalytics.rdepot.base.api.v2.resolvers.DtoResolvedPageable;
import eu.openanalytics.rdepot.base.api.v2.validation.PageableValidator;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.utils.specs.RepositorySpecs;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import eu.openanalytics.rdepot.python.api.v2.converters.PythonRepositoryDtoConverter;
import eu.openanalytics.rdepot.python.api.v2.dtos.PythonRepositoryDto;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonRepositoryDeleter;
import eu.openanalytics.rdepot.python.services.PythonRepositoryService;
import eu.openanalytics.rdepot.python.strategy.factory.PythonStrategyFactory;
import eu.openanalytics.rdepot.python.validation.PythonRepositoryValidator;
import eu.openanalytics.rdepot.python.validation.exceptions.PythonRepositoryValidationError;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.json.JsonException;
import jakarta.json.JsonPatch;
import java.security.Principal;
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
    private final PythonRepositoryService repositoryService;
    private final SecurityMediator securityMediator;
    private final PythonRepositoryValidator validator;
    private final PythonRepositoryDeleter deleter;
    private final PageableValidator pageableValidator;
    private final CommonPageableSortResolver pageableSortResolver;

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
            PythonRepositoryService repositoryService,
            PythonStrategyFactory factory,
            PythonRepositoryValidator validator,
            SecurityMediator securityMediator,
            PythonRepositoryDtoConverter dtoConverter,
            PythonRepositoryDeleter deleter,
            PageableValidator pageableValidator,
            CommonPageableSortResolver pageableSortResolver) {
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
        this.repositoryService = repositoryService;
        this.validator = validator;
        this.userService = userService;
        this.deleter = deleter;
        this.pageableValidator = pageableValidator;
        this.pageableSortResolver = pageableSortResolver;
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

        Specification<PythonRepository> specs = null;

        specs = SpecificationUtils.andComponent(specs, RepositorySpecs.isDeleted(deleted));
        if (name.isPresent()) specs = SpecificationUtils.andComponent(specs, RepositorySpecs.ofName(name.get()));
        return handleSuccessForPagedCollection(
                repositoryService.findAllBySpecification(specs, resolvedPageable), requester);
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
                .findByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        PythonRepository repository =
                repositoryService.findById(id).orElseThrow(() -> new RepositoryNotFound(messageSource, locale));

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
            PythonRepository repositoryEntity = repositoryDto.toEntity();
            validate(repositoryEntity);
            Strategy<PythonRepository> strategy = factory.createRepositoryStrategy(repositoryEntity, requester);
            PythonRepository repository = strategy.perform();
            return handleCreatedForSingleEntity(repository, requester);
        } catch (PythonRepositoryValidationError e) {
            return handleValidationError(e.getBindingResult());
        } catch (StrategyFailure e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw new CreateException(messageSource, LocaleContextHolder.getLocale());
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
                repositoryService.findById(id).orElseThrow(() -> new RepositoryNotFound(messageSource, locale));

        if (!securityMediator.isAuthorizedToEdit(repository, requester))
            throw new UserNotAuthorized(messageSource, locale);

        checkDeclarative();
        try {
            PythonRepositoryDto repositoryDto = applyPatchToEntity(jsonPatch, repository);
            PythonRepository updated = repositoryDto.toEntity();

            if (!repositoriesDeletionEnabled)
                if (updated.getDeleted() && !repository.getDeleted())
                    throw new RepositoryDeletionException(messageSource, locale);

            validate(updated);
            Strategy<PythonRepository> strategy = factory.updateRepositoryStrategy(repository, requester, updated);
            repository = strategy.perform();
        } catch (JsonException | JsonProcessingException e) {
            throw new MalformedPatchException(messageSource, locale, e);
        } catch (StrategyFailure e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw new ApplyPatchException(messageSource, locale);
        } catch (PythonRepositoryValidationError e) {
            return handleValidationError(e.getBindingResult());
        }

        return handleSuccessForSingleEntity(repository, requester);
    }

    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deletePythonRepository")
    public void deleteRepository(Principal principal, @PathVariable("id") Integer id) throws ApiException {
        Optional<User> requester = userService.findByLogin(principal.getName());
        if (requester.isEmpty() || requester.get().getRole().getValue() != Role.VALUE.ADMIN)
            throw new UserNotAuthorized(messageSource, locale);

        PythonRepository repository =
                repositoryService.findById(id).orElseThrow(() -> new RepositoryNotFound(messageSource, locale));

        checkDeclarative();

        if (!repositoriesDeletionEnabled) throw new RepositoryDeletionException(messageSource, locale);

        try {
            deleter.delete(repository);
        } catch (DeleteEntityException e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw new DeleteException(messageSource, locale);
        }
    }

    private void checkDeclarative() throws NotAllowedInDeclarativeMode {
        if (Boolean.valueOf(declarative)) throw new NotAllowedInDeclarativeMode(messageSource, locale);
    }

    private void validate(PythonRepository entity) throws PythonRepositoryValidationError {
        BindingResult bindingResult = createBindingResult(entity);
        validator.validate(entity, bindingResult);
        if (bindingResult.hasErrors()) throw new PythonRepositoryValidationError(bindingResult);
    }

    private User getRequester(Principal principal) throws UserNotAuthorized {
        User requester = userService
                .findByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, LocaleContextHolder.getLocale()));
        return requester;
    }

    private void checkIfUserIsAdmin(User user) throws UserNotAuthorized {
        if (!userService.isAdmin(user)) throw new UserNotAuthorized(messageSource, LocaleContextHolder.getLocale());
    }
}

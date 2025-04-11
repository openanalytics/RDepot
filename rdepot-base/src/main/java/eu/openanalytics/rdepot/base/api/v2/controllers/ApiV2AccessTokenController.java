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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.openanalytics.rdepot.base.api.v2.converters.AccessTokenDtoConverter;
import eu.openanalytics.rdepot.base.api.v2.converters.exceptions.EntityResolutionException;
import eu.openanalytics.rdepot.base.api.v2.dtos.AccessTokenDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.CreateAccessTokenDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.AccessTokenNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApiException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApplyPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.CreateException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.DeleteException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.MalformedPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotFound;
import eu.openanalytics.rdepot.base.api.v2.hateoas.AccessTokenModelAssembler;
import eu.openanalytics.rdepot.base.api.v2.resolvers.CommonPageableSortResolver;
import eu.openanalytics.rdepot.base.api.v2.resolvers.DtoResolvedPageable;
import eu.openanalytics.rdepot.base.api.v2.validation.PageableValidator;
import eu.openanalytics.rdepot.base.entities.AccessToken;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.deletion.AccessTokenDeleter;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.service.AccessTokenService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.StrategyExecutor;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.factory.StrategyFactory;
import eu.openanalytics.rdepot.base.utils.specs.AccessTokenSpecs;
import eu.openanalytics.rdepot.base.utils.specs.SpecificationUtils;
import eu.openanalytics.rdepot.base.validation.AccessTokenPatchValidator;
import eu.openanalytics.rdepot.base.validation.exceptions.PatchValidationException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.json.JsonPatch;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import org.springframework.transaction.annotation.Transactional;
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
 * REST Controller implementation for Access Tokens.
 */
@Slf4j
@RestController
@RequestMapping("/api/v2/manager/access-tokens")
public class ApiV2AccessTokenController extends ApiV2Controller<AccessToken, AccessTokenDto> {

    private final UserService userService;
    private final AccessTokenService accessTokenService;
    private final StrategyFactory factory;
    private final AccessTokenDeleter accessTokenDeleter;
    private final AccessTokenPatchValidator accessTokenPatchValidator;
    private final PageableValidator pageableValidator;
    private final CommonPageableSortResolver pageableSortResolver;
    private final StrategyExecutor strategyExecutor;

    public ApiV2AccessTokenController(
            MessageSource messageSource,
            AccessTokenModelAssembler modelAssembler,
            PagedResourcesAssembler<AccessToken> pagedModelAssembler,
            AccessTokenDtoConverter dtoConverter,
            ObjectMapper objectMapper,
            StrategyFactory factory,
            UserService userService,
            AccessTokenService accessTokenService,
            AccessTokenDeleter accessTokenDeleter,
            AccessTokenPatchValidator accessTokenPatchValidator,
            PageableValidator pageableValidator,
            CommonPageableSortResolver pageableSortResolver,
            StrategyExecutor strategyExecutor) {
        super(
                messageSource,
                LocaleContextHolder.getLocale(),
                modelAssembler,
                pagedModelAssembler,
                objectMapper,
                AccessTokenDto.class,
                Optional.empty(),
                dtoConverter);
        this.accessTokenService = accessTokenService;
        this.userService = userService;
        this.factory = factory;
        this.accessTokenDeleter = accessTokenDeleter;
        this.accessTokenPatchValidator = accessTokenPatchValidator;
        this.pageableValidator = pageableValidator;
        this.pageableSortResolver = pageableSortResolver;
        this.strategyExecutor = strategyExecutor;
    }

    /**
     * Fetches all access tokens.
     * @param principal used for authorization
     * @param pageable carries parameters required for pagination
     * @param active shows only active tokens
     * @param expired shows only expired tokens (expiration date < today)
     * @param userLogins shows tokens of given users (only for admins)
     * @return collection of access tokens
     */
    @PreAuthorize("hasAuthority('user')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PageableAsQueryParam
    @Operation(operationId = "getAllAccessTokens")
    public @ResponseBody ResponseDto<PagedModel<EntityModel<AccessTokenDto>>> getAllAccessTokens(
            Principal principal,
            @ParameterObject Pageable pageable,
            @RequestParam(name = "search", required = false) Optional<String> search,
            @RequestParam(name = "userLogin", required = false) List<String> userLogins,
            @RequestParam(name = "active", required = false) Optional<Boolean> active,
            @RequestParam(name = "expired", required = false) Optional<Boolean> expired)
            throws ApiException {

        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        final DtoResolvedPageable resolvedPageable = pageableSortResolver.resolve(pageable);
        pageableValidator.validate(AccessTokenDto.class, resolvedPageable);

        Specification<AccessToken> specification = null;

        if (userService.isAdmin(requester)) {
            if (Objects.nonNull(userLogins)) {
                specification = SpecificationUtils.andComponent(specification, AccessTokenSpecs.ofLogin(userLogins));
            }
        } else {
            specification = SpecificationUtils.andComponent(specification, AccessTokenSpecs.ofUser(requester));
        }

        if (search.isPresent()) {
            specification = SpecificationUtils.andComponent(specification, AccessTokenSpecs.ofName(search.get()));
        }

        if (active.isPresent()) {
            specification = SpecificationUtils.andComponent(specification, AccessTokenSpecs.isActive(active.get()));
        }

        if (expired.isPresent()) {
            specification = SpecificationUtils.andComponent(specification, AccessTokenSpecs.hasExpired(expired.get()));
        }

        if (specification != null) {
            return handleSuccessForPagedCollection(
                    accessTokenService.findAllBySpecification(specification, resolvedPageable), requester);
        } else {
            return handleSuccessForPagedCollection(accessTokenService.findAll(resolvedPageable), requester);
        }
    }

    /**
     * Find an access token of given id
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(operationId = "getAccessTokenById")
    public @ResponseBody ResponseEntity<ResponseDto<EntityModel<AccessTokenDto>>> getAccessTokenById(
            Principal principal, @PathVariable("id") Integer id) throws AccessTokenNotFound, UserNotAuthorized {
        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
        AccessToken accessToken =
                accessTokenService.findById(id).orElseThrow(() -> new AccessTokenNotFound(messageSource, locale));

        if (!requester.equals(accessToken.getUser()) && !userService.isAdmin(requester))
            throw new UserNotAuthorized(messageSource, locale);

        return handleSuccessForSingleEntity(accessToken, requester);
    }

    /**
     * Creates an access token.
     */
    @PreAuthorize("hasAuthority('user')")
    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "createAccessToken")
    public @ResponseBody ResponseEntity<?> createAccessToken(
            Principal principal,
            @RequestBody @Valid CreateAccessTokenDto createAccessTokenDto,
            BindingResult bindingResult)
            throws UserNotAuthorized, CreateException {
        User requester = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        if (bindingResult.hasErrors()) {
            return handleValidationErrorWithDefaultMessage(bindingResult);
        }

        AccessToken accessToken = accessTokenService.convertRequestBody(createAccessTokenDto, requester);

        Strategy<AccessToken> strategy = factory.createAccessTokenStrategy(accessToken, requester);

        try {
            return handleSuccessForSingleEntity(strategyExecutor.execute(strategy), requester);
        } catch (StrategyFailure e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw new CreateException(messageSource, locale);
        }
    }

    /**
     * Updates the access token
     */
    @PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
    @PreAuthorize("hasAuthority('user')")
    @Operation(operationId = "patchAccessToken")
    @Transactional
    public @ResponseBody ResponseEntity<?> patchAccessTokens(
            @PathVariable("id") Integer id, Principal principal, @RequestBody JsonPatch jsonPatch) throws ApiException {

        AccessToken accessToken =
                accessTokenService.findById(id).orElseThrow(() -> new AccessTokenNotFound(messageSource, locale));

        Optional<User> requester = userService.findActiveByLogin(principal.getName());
        User user = userService
                .findById(accessToken.getUser().getId())
                .orElseThrow(() -> new UserNotFound(messageSource, locale));

        if (requester.isEmpty() || (requester.get().getId() != user.getId()) && !userService.isAdmin(requester.get()))
            throw new UserNotAuthorized(messageSource, locale);

        AccessToken updated = null;
        try {
            AccessTokenDto accessTokenDto = applyPatchToEntity(jsonPatch, accessToken);
            updated = dtoConverter.resolveDtoToEntity(accessTokenDto);

            if (updated.isDeleted()) {
                return handleValidationError(MessageCodes.ACCESS_TOKENS_CANNOT_BE_SOFT_DELETED);
            }

            accessTokenPatchValidator.validatePatch(jsonPatch, accessToken, accessTokenDto);

            Strategy<AccessToken> strategy = factory.updateAccessTokenStrategy(accessToken, requester.get(), updated);

            updated = strategyExecutor.execute(strategy);
        } catch (StrategyFailure e) {
            throw new ApplyPatchException(messageSource, locale);
        } catch (JsonProcessingException | EntityResolutionException | PatchValidationException e) {
            throw new MalformedPatchException(messageSource, locale, e);
        }

        return handleSuccessForSingleEntity(updated, requester.get());
    }

    /**
     * Deletes the access token of given ID. This is a permanent delete.
     */
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('user')")
    @Operation(operationId = "deleteAccessToken")
    public void deleteAccessToken(@PathVariable("id") Integer id, Principal principal) throws ApiException {
        Optional<User> requester = userService.findActiveByLogin(principal.getName());
        if (requester.isEmpty()) throw new UserNotAuthorized(messageSource, locale);

        AccessToken token =
                accessTokenService.findById(id).orElseThrow(() -> new AccessTokenNotFound(messageSource, locale));

        if (!userService.isAdmin(requester.get()) && requester.get() != token.getUser())
            throw new UserNotAuthorized(messageSource, locale);

        try {
            accessTokenDeleter.delete(token);
        } catch (DeleteEntityException e) {
            log.error(e.getClass().getName() + ": " + e.getMessage(), e);
            throw new DeleteException(messageSource, locale);
        }
    }
}

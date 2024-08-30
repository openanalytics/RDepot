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
import eu.openanalytics.rdepot.base.api.v2.converters.UserSettingsDtoConverter;
import eu.openanalytics.rdepot.base.api.v2.converters.exceptions.EntityResolutionException;
import eu.openanalytics.rdepot.base.api.v2.dtos.UserSettingsDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApplyPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.MalformedPatchException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotFound;
import eu.openanalytics.rdepot.base.api.v2.hateoas.UserSettingsModelAssembler;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.UserSettings;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.UserSettingsService;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.StrategyExecutor;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.factory.StrategyFactory;
import eu.openanalytics.rdepot.base.validation.UserSettingsValidator;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.json.JsonException;
import jakarta.json.JsonPatch;
import java.security.Principal;
import java.util.Optional;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller implementation for User Settings.
 */
@RestController
@RequestMapping("/api/v2/manager/user-settings")
public class ApiV2UserSettingsController extends ApiV2Controller<UserSettings, UserSettingsDto> {

    private final UserService userService;
    private final UserSettingsService userSettingsService;
    private final StrategyFactory factory;
    private final UserSettingsValidator userSettingsValidator;
    private final StrategyExecutor strategyExecutor;

    public ApiV2UserSettingsController(
            MessageSource messageSource,
            UserSettingsModelAssembler modelAssembler,
            PagedResourcesAssembler<UserSettings> pagedModelAssembler,
            UserSettingsDtoConverter dtoConverter,
            ObjectMapper objectMapper,
            StrategyFactory factory,
            UserService userService,
            UserSettingsService userSettingsService,
            UserSettingsValidator validator,
            StrategyExecutor strategyExecutor) {
        super(
                messageSource,
                LocaleContextHolder.getLocale(),
                modelAssembler,
                pagedModelAssembler,
                objectMapper,
                UserSettingsDto.class,
                Optional.of(validator),
                dtoConverter);
        this.userService = userService;
        this.userSettingsService = userSettingsService;
        this.factory = factory;
        this.userSettingsValidator = validator;
        this.strategyExecutor = strategyExecutor;
    }

    /**
     * Fetches user settings of given user ID.
     */
    @GetMapping(value = "/{userId}")
    @PreAuthorize("hasAuthority('user')")
    @Operation(operationId = "getUserSettingsByUserId")
    public @ResponseBody ResponseEntity<?> getUserSettings(@PathVariable("userId") int userId, Principal principal)
            throws UserNotFound, UserNotAuthorized {
        Optional<User> requester = userService.findActiveByLogin(principal.getName());
        Optional<User> user = userService.findOneNonDeleted(userId);

        if (requester.isPresent()) {
            if (user.isEmpty() && userService.isAdmin(requester.get())) {
                throw new UserNotFound(messageSource, locale);
            } else if (user.isPresent()
                    && (userService.isAdmin(requester.get())
                            || requester.get().getId() == user.get().getId())) {
                return handleSuccessForSingleEntity(userSettingsService.getUserSettings(user.get()), requester.get());
            }
        }
        throw new UserNotAuthorized(messageSource, locale);
    }

    /**
     * Updates user settings of given user ID.
     */
    @PatchMapping(value = "/{userId}", consumes = "application/json-patch+json")
    @PreAuthorize("hasAuthority('user')")
    @Operation(operationId = "patchUserSettingsByUserId")
    @Transactional
    public @ResponseBody ResponseEntity<?> patchUserSettings(
            @PathVariable("userId") int userId, Principal principal, @RequestBody JsonPatch patch)
            throws UserNotFound, UserNotAuthorized, MalformedPatchException, ApplyPatchException {
        boolean toCreate = false;
        UserSettings settings;
        Optional<User> requester = userService.findActiveByLogin(principal.getName());
        User user = userService.findById(userId).orElseThrow(() -> new UserNotFound(messageSource, locale));
        Optional<UserSettings> settingsOptional = userSettingsService.findSettingsByUser(user);

        if (requester.isEmpty() || (requester.get().getId() != user.getId() && !userService.isAdmin(requester.get())))
            throw new UserNotAuthorized(messageSource, locale);

        try {
            if (settingsOptional.isEmpty()) {
                toCreate = true;
                settings = userSettingsService.getDefaultSettings();
                settings.setUser(user);
            } else {
                settings = settingsOptional.get();
            }

            UserSettingsDto patchedDto = applyPatchToEntity(patch, settings);
            UserSettings entity = dtoConverter.resolveDtoToEntity(patchedDto);
            BindingResult bindingResult = createBindingResult(entity);

            userSettingsValidator.validate(entity, bindingResult);

            if (bindingResult.hasErrors()) return handleValidationError(bindingResult);

            Strategy<UserSettings> strategy =
                    factory.updateUserSettingsStrategy(settings, requester.get(), entity, toCreate);

            strategyExecutor.execute(strategy);
        } catch (JsonException | JsonProcessingException | EntityResolutionException e) {
            throw new MalformedPatchException(messageSource, locale, e);
        } catch (StrategyFailure e) {
            throw new ApplyPatchException(messageSource, locale);
        }

        return handleSuccessForSingleEntity(settings, requester.get());
    }
}

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

import eu.openanalytics.rdepot.base.api.v2.dtos.NewsfeedEventDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApiException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.EventNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.api.v2.hateoas.EventModelAssembler;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.mediator.newsfeed.NewsfeedEventsRolesFiltration;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.time.DateParser;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only REST controller for Newsfeed Events.
 */
@RestController
@PreAuthorize("hasAuthority('user')")
@RequestMapping(value = "/api/v2/manager/events")
public class ApiV2NewsfeedEventController extends ApiV2ReadingController<NewsfeedEvent, NewsfeedEventDto> {

    private final NewsfeedEventService eventRetriever;
    private final UserService userService;
    private final SecurityMediator securityMediator;
    private final NewsfeedEventsRolesFiltration eventsRolesFiltering;

    public ApiV2NewsfeedEventController(
            MessageSource messageSource,
            EventModelAssembler modelAssembler,
            PagedResourcesAssembler<NewsfeedEvent> pagedModelAssembler,
            NewsfeedEventService eventRetriever,
            UserService userService,
            SecurityMediator securityMediator,
            NewsfeedEventsRolesFiltration eventsRolesFiltering) {
        super(messageSource, LocaleContextHolder.getLocale(), modelAssembler, pagedModelAssembler);
        this.eventRetriever = eventRetriever;
        this.userService = userService;
        this.securityMediator = securityMediator;
        this.eventsRolesFiltering = eventsRolesFiltering;
    }

    /**
     * Fetches {@link NewsfeedEvent Events} based on provided filters and pagination.
     * @param principal represents authenticated user
     * @param pageable represents pagination and sorting parameters
     * @param technologies technologies that events are related to, e.g. R or Python
     * @param userName login of the user that was responsible for event creation
     * @param eventType e.g. create, delete or update; see: {@link NewsfeedEventType}
     * @param resourceType e.g. package or repository
     * @param fromDate when the oldest event was created
     * @param toDate when the latest event was created
     * @return {@link NewsfeedEventDto DTOs} wrapped with {@link ResponseDto}
     * @throws ApiException HTTP errors (both 4xx and 5xx) wrapped with {@link ResponseDto}
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PageableAsQueryParam
    public @ResponseBody ResponseDto<PagedModel<EntityModel<NewsfeedEventDto>>> getAllEvents(
            Principal principal,
            @ParameterObject Pageable pageable,
            @RequestParam(name = "technology", required = false) List<String> technologies,
            @RequestParam(name = "userName", required = false) List<String> userName,
            @RequestParam(name = "packageName", required = false) List<String> packageName,
            @RequestParam(name = "packageVersion", required = false) List<String> packageVersion,
            @RequestParam(name = "repositoryName", required = false) List<String> repositoryName,
            @RequestParam(name = "eventType", required = false) List<String> eventType,
            @RequestParam(name = "resourceType", required = false) List<String> resourceType,
            @RequestParam(name = "fromDate", required = false) Optional<String> fromDate,
            @RequestParam(name = "toDate", required = false) Optional<String> toDate)
            throws ApiException {
        final User user = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));

        final Optional<Instant> fromDateInstant = fromDate.flatMap(DateParser::parseTimestampStart);
        final Optional<Instant> toDateInstant = toDate.flatMap(DateParser::parseTimestampEnd);

        final Specification<NewsfeedEvent> specification =
                eventsRolesFiltering.getNewsfeedEventsRolesSpecification(user);

        final Page<NewsfeedEvent> retrievedEvents = eventRetriever.findEventsByParameters(
                pageable,
                technologies,
                userName,
                packageName,
                packageVersion,
                repositoryName,
                eventType,
                resourceType,
                fromDateInstant,
                toDateInstant,
                specification);
        return handleSuccessForPagedCollection(retrievedEvents, user);
    }

    /**
     * Fetches single NewsfeedEvent by id.
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody ResponseEntity<ResponseDto<EntityModel<NewsfeedEventDto>>> getEvent(
            Principal principal, @PathVariable("id") Integer id) throws ApiException {
        User user = userService
                .findActiveByLogin(principal.getName())
                .orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
        NewsfeedEvent event = eventRetriever.findById(id).orElseThrow(() -> new EventNotFound(messageSource, locale));

        if (!securityMediator.canSeeEvent(event, user)) throw new UserNotAuthorized(messageSource, locale);

        return handleSuccessForSingleEntity(event, user);
    }
}

/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.base.api.v2.controllers;

import java.security.Principal;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.base.api.v2.dtos.NewsfeedEventDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.ResponseDto;
import eu.openanalytics.rdepot.base.api.v2.exceptions.ApiException;
import eu.openanalytics.rdepot.base.api.v2.exceptions.EventNotFound;
import eu.openanalytics.rdepot.base.api.v2.exceptions.UserNotAuthorized;
import eu.openanalytics.rdepot.base.api.v2.hateoas.EventModelAssembler;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.mediator.newsfeed.NewsfeedEventsRolesFiltering;

@RestController
@PreAuthorize("hasAuthority('user')")
@RequestMapping(value = "/api/v2/manager/events")
public class ApiV2NewsfeedEventController extends ApiV2Controller<NewsfeedEvent, NewsfeedEventDto> {

	private final NewsfeedEventService eventRetriever;
	private final UserService userService;
	private final SecurityMediator securityMediator;
	private final NewsfeedEventsRolesFiltering eventsRolesFiltering;  
	
	public ApiV2NewsfeedEventController(MessageSource messageSource,
			EventModelAssembler modelAssembler,
			PagedResourcesAssembler<NewsfeedEvent> pagedModelAssembler, ObjectMapper objectMapper,
			Validator validator, NewsfeedEventService eventRetriever, UserService userService,
			SecurityMediator securityMediator, NewsfeedEventsRolesFiltering eventsRolesFiltering) {
		super(messageSource, 
				LocaleContextHolder.getLocale(), 
				modelAssembler, 
				pagedModelAssembler, 
				objectMapper, 
				NewsfeedEventDto.class, 
				LoggerFactory.getLogger(ApiV2NewsfeedEventController.class), 
				validator);
		this.eventRetriever = eventRetriever;
		this.userService = userService;
		this.securityMediator = securityMediator;
		this.eventsRolesFiltering = eventsRolesFiltering;
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseDto<?> 
		getAllEvents(Principal principal, Pageable pageable,
				@RequestParam(name = "technology", required = false) Optional<String> technology,
				@RequestParam(name = "userId", required = false) Optional<Integer> userId,
				@RequestParam(name = "resourceId", required = false) Optional<Integer> resourceId,
				@RequestParam(name = "eventType", required = false) Optional<String> eventType,
				@RequestParam(name = "resourceType", required = false) Optional<String> resourceType)
		throws ApiException {
		Page<NewsfeedEvent> retrievedEvents = null;
		
		User user = userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		boolean isAdmin = user.getRole().getValue() == Role.VALUE.ADMIN;
		
		Optional<User> userParam = Optional.empty();
		if(userId.isPresent()) {
			userParam = userService.findById(userId.get());
			if(userParam.isEmpty())
				return emptyPage();
		}
		
		Specification<NewsfeedEvent> specification = eventsRolesFiltering.getNewsfeedEventsRolesSpecification(user);
		
		retrievedEvents = eventRetriever.findEventsByParameters(
				pageable, isAdmin, technology, userParam, resourceId, eventType, resourceType, specification);
		return handleSuccessForPagedCollection(retrievedEvents);
	}
	
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseEntity<?> getEvent(Principal principal, @PathVariable Integer id) 
			throws ApiException {
		User user = userService.findByLogin(principal.getName())
				.orElseThrow(() -> new UserNotAuthorized(messageSource, locale));
		NewsfeedEvent event = eventRetriever.findById(id)
				.orElseThrow(() -> new EventNotFound(messageSource, locale));
		
		if(!securityMediator.canSeeEvent(event, user))
			throw new UserNotAuthorized(messageSource, locale);
		
		return handleCreatedForSingleEntity(event);
	}
}

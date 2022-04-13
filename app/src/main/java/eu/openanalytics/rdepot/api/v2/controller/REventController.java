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
package eu.openanalytics.rdepot.api.v2.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.openanalytics.rdepot.api.v2.dto.EntityDto;
import eu.openanalytics.rdepot.api.v2.dto.REventDto;
import eu.openanalytics.rdepot.api.v2.dto.ResponseDto;
import eu.openanalytics.rdepot.api.v2.exception.ApiException;
import eu.openanalytics.rdepot.api.v2.exception.UserNotAuthorized;
import eu.openanalytics.rdepot.api.v2.hateoas.EventModelAssembler;
import eu.openanalytics.rdepot.model.IEventEntity;
import eu.openanalytics.rdepot.model.ResourceType;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.service.PackageEventService;
import eu.openanalytics.rdepot.service.PackageMaintainerEventService;
import eu.openanalytics.rdepot.service.RepositoryEventService;
import eu.openanalytics.rdepot.service.RepositoryMaintainerEventService;
import eu.openanalytics.rdepot.service.UserEventService;
import eu.openanalytics.rdepot.service.UserService;

@RestController
@RequestMapping("/api/v2/manager/r/events")
public class REventController extends ApiV2Controller<IEventEntity, 
	REventDto<IEventEntity, EntityDto<IEventEntity>>> {

	private final UserService userService;
	private final PackageEventService packageEventService;
	private final RepositoryEventService repositoryEventService;
	
	@Autowired
	public REventController(MessageSource messageSource, 
			EventModelAssembler modelAssembler,
			PagedResourcesAssembler<IEventEntity> pagedModelAssembler, ObjectMapper objectMapper, 
			UserService userService, UserEventService userEventService, 
			PackageEventService packageEventService, RepositoryEventService repositoryEventService, 
			PackageMaintainerEventService packageMaintainerEventService, 
			RepositoryMaintainerEventService repositoryMaintainerEventService) {
		super(messageSource, LocaleContextHolder.getLocale(), modelAssembler, 
				pagedModelAssembler, objectMapper, null, LoggerFactory.getLogger(REventController.class), null);
		this.userService = userService;
		this.packageEventService = packageEventService;
		this.repositoryEventService = repositoryEventService;
		// We pass null to parameterized value and this is fine since it's just a temporary solution.
	}
	
	@PreAuthorize("hasAuthority('user')")
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody ResponseDto<?> getEvents(Principal principal, Pageable pageable, 
			@RequestParam(required = true) ResourceType resourceType, 
//			@RequestParam(required = false) Optional<EventType> eventType, 
			@RequestParam(required = false) Optional<Integer> userId) throws ApiException {
		User requester = userService.findByLogin(principal.getName());
		
		if(requester == null)
			throw new UserNotAuthorized(messageSource, locale);
		
//		Boolean fetchAll = resourceType.isEmpty() && userId.isEmpty();
		
		//TODO: Fake paging for now?
//		List<IEventEntity> events = new ArrayList<>();
//		
//		
//		if(fetchAll || resourceType.get() == ResourceType.REPOSITORY) { //Let's now support this only
//			
//		}
		
		Page<IEventEntity> page = new PageImpl<>(new ArrayList<>());
		
		switch(resourceType) {
		case PACKAGE:
			packageEventService.findAllByUser(requester, pageable);
			break;
		case REPOSITORY:
			repositoryEventService.findAllByUser(requester, pageable);
			break;
		default:
			break;
		
		}
		
		return handleSuccessForPagedCollection(page);
	}
	
//	@PreAuthorize("hasAuthority('user')")
//	@GetMapping("/{id}")
//	@ResponseStatus(HttpStatus.OK)
//	public @ResponseBody ResponseDto<?> getEvent(Principal principal, Integer id) throws ApiException {
//		User requester = userService.findByLogin(principal.getName());
//		RepositoryEvent event = repositoryEventService.findById(id);
//		
//		if(event == null)
//			throw new EventNotFound(messageSource, locale);
//		
//		if(requester == null || !userService.isAuthorizedToSee(event, requester))
//			throw new UserNotAuthorized(messageSource, locale);
//		
//		return handleSuccessForSingleEntity((IEventEntity)event);
//	}
	
}

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
package eu.openanalytics.rdepot.base.api.v2.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import lombok.NonNull;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2NewsfeedEventController;
import eu.openanalytics.rdepot.base.api.v2.converters.DtoConverter;
import eu.openanalytics.rdepot.base.api.v2.dtos.NewsfeedEventDto;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.User;

/**
 * {@link RepresentationModelAssembler Model Assembler}
 * for {@link NewsfeedEvent Newsfeed Events}.
 */
@Component
public class EventModelAssembler implements RoleAwareRepresentationModelAssembler<NewsfeedEvent, 
	EntityModel<NewsfeedEventDto>> {

	private final DtoConverter<NewsfeedEvent, NewsfeedEventDto> dtoConverter;
	
	public EventModelAssembler(DtoConverter<NewsfeedEvent, NewsfeedEventDto> dtoConverter) {
		this.dtoConverter = dtoConverter;
	}
	
	@Override
	public @NonNull EntityModel<NewsfeedEventDto> toModel(@NonNull NewsfeedEvent entity) {
		NewsfeedEventDto dto = dtoConverter.convertEntityToDto(entity);
		
		return EntityModel.of(dto, 
				linkTo(ApiV2NewsfeedEventController.class).slash(entity.getId()).withSelfRel(),
				linkTo(ApiV2NewsfeedEventController.class).withRel("eventList"));
	}

	@Override
	public EntityModel<NewsfeedEventDto> toModel(NewsfeedEvent entity, User user) {
		return toModel(entity);
	}

	@Override
	public RepresentationModelAssembler<NewsfeedEvent, EntityModel<NewsfeedEventDto>> assemblerWithUser(User user) {
		return new EventModelAssembler(dtoConverter);
	}

}

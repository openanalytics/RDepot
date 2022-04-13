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
package eu.openanalytics.rdepot.api.v2.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.api.v2.controller.ApiV2UserController;
import eu.openanalytics.rdepot.api.v2.dto.UserDto;
import eu.openanalytics.rdepot.model.User;

@Component
public class UserModelAssembler 
	implements RepresentationModelAssembler<User, EntityModel<UserDto>> {

	@Override
	public EntityModel<UserDto> toModel(User entity) {
		UserDto dto = new UserDto(entity);
		
		return EntityModel.of(dto, 
				linkTo(ApiV2UserController.class).slash(entity.getId()).withSelfRel(),
				linkTo(ApiV2UserController.class).withRel("userList")); //TODO: Maybe we should return it only when user is privileged?
	}

}
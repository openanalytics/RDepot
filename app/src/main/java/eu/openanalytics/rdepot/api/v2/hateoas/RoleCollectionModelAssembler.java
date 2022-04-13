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

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.api.v2.controller.ApiV2UserController;
import eu.openanalytics.rdepot.api.v2.dto.RoleDto;
import eu.openanalytics.rdepot.model.Role;

@Component
public class RoleCollectionModelAssembler implements RepresentationModelAssembler
	<List<Role>, CollectionModel<RoleDto>>  {

	@Override
	public CollectionModel<RoleDto> toModel(List<Role> entities) {
		return CollectionModel.of(
				entities.stream().map(r -> new RoleDto(r)).collect(Collectors.toList()),
				linkTo(ApiV2UserController.class).slash("roles").withSelfRel());
	}

}
/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
package eu.openanalytics.rdepot.base.api.v2.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2RepositoryMaintainerController;
import eu.openanalytics.rdepot.base.api.v2.dtos.RepositoryMaintainerDto;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;

@Component
public class RepositoryMaintainerModelAssembler 
	implements RepresentationModelAssembler<RepositoryMaintainer, 
		EntityModel<RepositoryMaintainerDto>> {

	@Override
	public EntityModel<RepositoryMaintainerDto> toModel(RepositoryMaintainer entity) {
		RepositoryMaintainerDto dto = new RepositoryMaintainerDto(entity);
		
		return EntityModel.of(dto, 
				linkTo(ApiV2RepositoryMaintainerController.class).slash(entity.getId()).withSelfRel(),
				linkTo(ApiV2RepositoryMaintainerController.class).withRel("repositoryMaintainerList"));
	}
}

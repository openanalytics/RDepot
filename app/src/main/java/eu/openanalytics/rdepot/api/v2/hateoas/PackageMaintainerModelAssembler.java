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

import eu.openanalytics.rdepot.api.v2.controller.ApiV2PackageMaintainerController;
import eu.openanalytics.rdepot.api.v2.dto.PackageMaintainerDto;
import eu.openanalytics.rdepot.model.PackageMaintainer;

@Component
public class PackageMaintainerModelAssembler 
	implements RepresentationModelAssembler<PackageMaintainer, EntityModel<PackageMaintainerDto>> {

	@Override
	public EntityModel<PackageMaintainerDto> toModel(PackageMaintainer entity) {
		PackageMaintainerDto dto = new PackageMaintainerDto(entity);
		
		return EntityModel.of(dto, 
				linkTo(ApiV2PackageMaintainerController.class).slash(entity.getId()).withSelfRel(),
				linkTo(ApiV2PackageMaintainerController.class).withRel("packageMaintainerList"));
	}

}

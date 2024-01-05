/**
 * R Depot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.r.api.v2.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.r.api.v2.controllers.RPackageController;
import eu.openanalytics.rdepot.r.api.v2.dtos.RPackageDto;
import eu.openanalytics.rdepot.r.entities.RPackage;

@Component
public class RPackageModelAssembler 
	implements RepresentationModelAssembler<RPackage, EntityModel<RPackageDto>> {

	@Override
	public EntityModel<RPackageDto> toModel(RPackage packageBag) {
		RPackageDto dto = new RPackageDto(packageBag);
		return EntityModel.of(dto, 
				linkTo(RPackageController.class).slash(packageBag.getId()).withSelfRel(),
				linkTo(RPackageController.class).withRel("packageList")); //TODO: Add link to the repository!!!
	}

}

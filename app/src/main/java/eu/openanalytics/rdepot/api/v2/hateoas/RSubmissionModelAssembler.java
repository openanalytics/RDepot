/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.api.v2.dto.RSubmissionDto;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.api.v2.controller.RSubmissionController;

@Component
public class RSubmissionModelAssembler implements RepresentationModelAssembler<Submission, EntityModel<RSubmissionDto>>{

	@Autowired
	RPackageModelAssembler rPackageModelAssembler;
	
	@Override
	public EntityModel<RSubmissionDto> toModel(Submission entity) {
		RSubmissionDto dto = new RSubmissionDto(entity);
		
		dto.setPackageBag(rPackageModelAssembler.toModel(entity.getPackage()));
		
		EntityModel<RSubmissionDto> model = EntityModel.of(dto, 
				linkTo(RSubmissionController.class).slash(entity.getId()).withSelfRel(),
				linkTo(RSubmissionController.class).withRel("submissionList"));
		
		return model;
	}

}
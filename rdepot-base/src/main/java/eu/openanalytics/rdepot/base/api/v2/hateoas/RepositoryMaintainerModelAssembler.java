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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2RepositoryMaintainerController;
import eu.openanalytics.rdepot.base.api.v2.converters.DtoConverter;
import eu.openanalytics.rdepot.base.api.v2.dtos.RepositoryMaintainerDto;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.UserService;

/**
 * {@link RepresentationModelAssembler Model Assembler}
 * for {@link RepositoryMaintainer Repository Maintainers}.
 */
@Component
public class RepositoryMaintainerModelAssembler 
	extends AbstractRoleAwareModelAssembler<RepositoryMaintainer, RepositoryMaintainerDto> {

	private final UserService userService;
	
	@Autowired
	public RepositoryMaintainerModelAssembler(
			DtoConverter<RepositoryMaintainer, RepositoryMaintainerDto> dtoConverter,
			UserService userService) {
		super(dtoConverter, ApiV2RepositoryMaintainerController.class, "repositoryMaintainer", Optional.empty());
		this.userService = userService;
	}
	
	private RepositoryMaintainerModelAssembler(
			DtoConverter<RepositoryMaintainer, RepositoryMaintainerDto> dtoConverter,
			UserService userService, User user) {
		super(dtoConverter, ApiV2RepositoryMaintainerController.class, "repositoryMaintainer", Optional.of(user));
		this.userService = userService;
	}

	@Override
	protected List<Link> getLinksToMethodsWithLimitedAccess(RepositoryMaintainer entity, User user, Link baseLink) {
		List<Link> links = new ArrayList<>();
		
		if(userService.isAdmin(user)) {
			links.add(baseLink.withType(HTTP_METHODS.PATCH.getValue()));
			links.add(baseLink.withType(HTTP_METHODS.DELETE.getValue()));
		}
		
		return links;
	}

	@Override
	public RepresentationModelAssembler<RepositoryMaintainer, EntityModel<RepositoryMaintainerDto>> assemblerWithUser(
			User user) {
		return new RepositoryMaintainerModelAssembler(dtoConverter, userService, user);
	}

	@Override
	protected Class<?> getExtensionControllerClass(RepositoryMaintainer entity) {
		return ApiV2RepositoryMaintainerController.class;
	}
}

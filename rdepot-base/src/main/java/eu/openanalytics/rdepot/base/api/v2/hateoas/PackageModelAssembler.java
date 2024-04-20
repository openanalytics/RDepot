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
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2Controller;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2PackageController;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2ReadingController;
import eu.openanalytics.rdepot.base.api.v2.converters.DtoConverter;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageDto;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.technology.Technology;

/**
 * {@link RepresentationModelAssembler Model Assembler}
 * for {@link Package Packages}.
 */
@Component
public class PackageModelAssembler 
	extends AbstractRoleAwareModelAssembler<Package, PackageDto> {
	
	private final DtoConverter<Package, PackageDto> dtoConverter;
	private final SecurityMediator securityMediator;
	private final Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>> 
		packageControllerClassesByTechnology;
	
	@Autowired
	public PackageModelAssembler(DtoConverter<Package, PackageDto> dtoConverter, SecurityMediator securityMediator, 
			@Qualifier("packageControllerClassesByTechnology")
			Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>> packageControllerClassesByTechnology) {
		super(dtoConverter, ApiV2PackageController.class, "package", Optional.empty());
		this.securityMediator = securityMediator;
		this.dtoConverter = dtoConverter;
		this.packageControllerClassesByTechnology = packageControllerClassesByTechnology;
	}
	
	private PackageModelAssembler(DtoConverter<Package, PackageDto> dtoConverter, 
			SecurityMediator securityMediator, 
			Map<Technology, Class<? extends ApiV2ReadingController<?, ?>>> packageControllerClassesByTechnology, User user) {
		super(dtoConverter, ApiV2PackageController.class, "package", Optional.of(user));
		this.securityMediator = securityMediator;
		this.dtoConverter = dtoConverter;
		this.packageControllerClassesByTechnology = packageControllerClassesByTechnology;
	}
	
	public EntityModel<PackageDto> toModel(Package entity, Class<? extends ApiV2Controller<?,?>> clazz){
		PackageDto dto = dtoConverter.convertEntityToDto(entity);
		return EntityModel.of(dto, 
				generateAvailableLinksForEntity(entity, clazz));
	}
	
	public EntityModel<PackageDto> toModel(Package entity, User user){
		PackageDto dto = dtoConverter.convertEntityToDto(entity);
		return EntityModel.of(dto, 
				generateRoleBasedAvailableLinksForEntity(entity, user));
	}

	@Override
	protected List<Link> getLinksToMethodsWithLimitedAccess(Package entity, User user, Link baseLink) {
		List<Link> links = new ArrayList<>();
		
		if(securityMediator.isAuthorizedToEdit(entity, user)) {
			links.add(baseLink.withType(HTTP_METHODS.PATCH.getValue()));
			links.add(baseLink.withType(HTTP_METHODS.DELETE.getValue()));
		}
		
		return links;
	}

	@Override
	public RepresentationModelAssembler<Package, EntityModel<PackageDto>> assemblerWithUser(User user) {
		return new PackageModelAssembler(dtoConverter, securityMediator, packageControllerClassesByTechnology, user);
	}

	@Override
	protected Class<?> getExtensionControllerClass(Package entity) {
		return packageControllerClassesByTechnology.getOrDefault(
				entity.getTechnology(), ApiV2PackageController.class);
	}
}

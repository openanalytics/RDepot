/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2PackageMaintainerController;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2UserController;
import eu.openanalytics.rdepot.base.api.v2.converters.DtoConverter;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageMaintainerDto;
import eu.openanalytics.rdepot.base.entities.PackageMaintainerQueryDSLTuple;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class PackageMaintainerQueryDSLTupleModelAssembler
        extends AbstractRoleAwareModelAssembler<PackageMaintainerQueryDSLTuple, PackageMaintainerDto> {

    private final SecurityMediator securityMediator;

    @Autowired
    public PackageMaintainerQueryDSLTupleModelAssembler(
            DtoConverter<PackageMaintainerQueryDSLTuple, PackageMaintainerDto> dtoConverter,
            SecurityMediator securityMediator) {
        super(dtoConverter, ApiV2PackageMaintainerController.class, "packageMaintainer", Optional.empty());
        this.securityMediator = securityMediator;
    }

    private PackageMaintainerQueryDSLTupleModelAssembler(
            DtoConverter<PackageMaintainerQueryDSLTuple, PackageMaintainerDto> dtoConverter,
            SecurityMediator securityMediator,
            User user) {
        super(dtoConverter, ApiV2PackageMaintainerController.class, "packageMaintainer", Optional.of(user));
        this.securityMediator = securityMediator;
    }

    @Override
    protected List<Link> getLinksToMethodsWithLimitedAccess(
            PackageMaintainerQueryDSLTuple entity, User user, Link baseLink) {
        List<Link> links = new ArrayList<>();

        if (securityMediator.isAuthorizedToEdit(entity, user) && entity.getId() != 0) {
            links.add(baseLink.withType(HTTP_METHODS.PATCH.getValue()));
            links.add(baseLink.withType(HTTP_METHODS.DELETE.getValue()));
        }

        return links;
    }

    @Override
    public RepresentationModelAssembler<PackageMaintainerQueryDSLTuple, EntityModel<PackageMaintainerDto>>
            assemblerWithUser(User user) {
        return new PackageMaintainerQueryDSLTupleModelAssembler(dtoConverter, securityMediator, user);
    }

    @Override
    public EntityModel<PackageMaintainerDto> toModel(PackageMaintainerQueryDSLTuple entity, User user) {
        PackageMaintainerDto dto = dtoConverter.convertEntityToDto(entity);

        dto.setPermissions(securityMediator.getPermissions(entity, user));
        return EntityModel.of(dto, generateRoleBasedAvailableLinksForEntity(entity, user));
    }

    @Override
    protected List<Link> generateAvailableLinksForEntity(
            PackageMaintainerQueryDSLTuple entity, Class<?> extensionControllerClass) {
        if (entity.getId() == 0) {
            return List.of(
                    linkTo(ApiV2UserController.class).slash(entity.getUserId()).withRel("user"),
                    linkTo(ApiV2PackageMaintainerController.class).withRel("packageMaintainersList"));
        } else {
            return List.of(
                    linkTo(ApiV2PackageMaintainerController.class)
                            .slash(entity.getId())
                            .withSelfRel(),
                    linkTo(ApiV2UserController.class).slash(entity.getUserId()).withRel("user"),
                    linkTo(ApiV2PackageMaintainerController.class).withRel("packageMaintainersList"));
        }
    }

    @Override
    protected Class<?> getExtensionControllerClass(PackageMaintainerQueryDSLTuple entity) {
        return ApiV2PackageMaintainerController.class;
    }
}

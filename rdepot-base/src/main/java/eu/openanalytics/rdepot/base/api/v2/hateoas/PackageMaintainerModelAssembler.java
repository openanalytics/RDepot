/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2PackageMaintainerController;
import eu.openanalytics.rdepot.base.api.v2.converters.DtoConverter;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageMaintainerDto;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
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

/**
 * {@link RepresentationModelAssembler Model Assembler}
 * for {@link PackageMaintainer Package Maintainers}.
 */
@Component
public class PackageMaintainerModelAssembler
        extends AbstractRoleAwareModelAssembler<PackageMaintainer, PackageMaintainerDto> {

    private final SecurityMediator securityMediator;

    @Autowired
    public PackageMaintainerModelAssembler(
            DtoConverter<PackageMaintainer, PackageMaintainerDto> dtoConverter, SecurityMediator securityMediator) {
        super(dtoConverter, ApiV2PackageMaintainerController.class, "packageMaintainer", Optional.empty());
        this.securityMediator = securityMediator;
    }

    private PackageMaintainerModelAssembler(
            DtoConverter<PackageMaintainer, PackageMaintainerDto> dtoConverter,
            SecurityMediator securityMediator,
            User user) {
        super(dtoConverter, ApiV2PackageMaintainerController.class, "packageMaintainer", Optional.of(user));
        this.securityMediator = securityMediator;
    }

    @Override
    protected List<Link> getLinksToMethodsWithLimitedAccess(PackageMaintainer entity, User user, Link baseLink) {
        List<Link> links = new ArrayList<>();

        if (securityMediator.isAuthorizedToEdit(entity, user)) {
            links.add(baseLink.withType(HTTP_METHODS.PATCH.getValue()));
            links.add(baseLink.withType(HTTP_METHODS.DELETE.getValue()));
        }

        return links;
    }

    @Override
    public RepresentationModelAssembler<PackageMaintainer, EntityModel<PackageMaintainerDto>> assemblerWithUser(
            User user) {
        return new PackageMaintainerModelAssembler(dtoConverter, securityMediator, user);
    }

    @Override
    protected Class<?> getExtensionControllerClass(PackageMaintainer entity) {
        return ApiV2PackageMaintainerController.class;
    }
}

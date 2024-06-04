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
package eu.openanalytics.rdepot.r.api.v2.hateoas;

import eu.openanalytics.rdepot.base.api.v2.converters.DtoConverter;
import eu.openanalytics.rdepot.base.api.v2.hateoas.AbstractRoleAwareModelAssembler;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.r.api.v2.controllers.RPackageController;
import eu.openanalytics.rdepot.r.api.v2.dtos.RPackageDto;
import eu.openanalytics.rdepot.r.entities.RPackage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class RPackageModelAssembler extends AbstractRoleAwareModelAssembler<RPackage, RPackageDto> {

    private final SecurityMediator securityMediator;

    @Autowired
    public RPackageModelAssembler(DtoConverter<RPackage, RPackageDto> dtoConverter, SecurityMediator securityMediator) {
        super(dtoConverter, RPackageController.class, "package", Optional.empty());
        this.securityMediator = securityMediator;
    }

    private RPackageModelAssembler(
            DtoConverter<RPackage, RPackageDto> dtoConverter, SecurityMediator securityMediator, User user) {
        super(dtoConverter, RPackageController.class, "package", Optional.of(user));
        this.securityMediator = securityMediator;
    }

    @Override
    protected List<Link> getLinksToMethodsWithLimitedAccess(RPackage entity, User user, Link baseLink) {
        List<Link> links = new ArrayList<>();

        if (securityMediator.isAuthorizedToEdit(entity, user)) {
            links.add(baseLink.withType(HTTP_METHODS.PATCH.getValue()));
            links.add(baseLink.withType(HTTP_METHODS.DELETE.getValue()));
        }

        return links;
    }

    @Override
    public RepresentationModelAssembler<RPackage, EntityModel<RPackageDto>> assemblerWithUser(User user) {
        return new RPackageModelAssembler(dtoConverter, securityMediator, user);
    }

    @Override
    protected Class<?> getExtensionControllerClass(RPackage entity) {
        return RPackageController.class;
    }
}

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
package eu.openanalytics.rdepot.r.api.v2.hateoas;

import eu.openanalytics.rdepot.base.api.v2.converters.DtoConverter;
import eu.openanalytics.rdepot.base.api.v2.hateoas.AbstractRoleAwareModelAssembler;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.r.api.v2.controllers.RRepositoryController;
import eu.openanalytics.rdepot.r.api.v2.dtos.RRepositoryDto;
import eu.openanalytics.rdepot.r.entities.RRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class RRepositoryModelAssembler extends AbstractRoleAwareModelAssembler<RRepository, RRepositoryDto> {

    private final SecurityMediator securityMediator;

    @Value("${declarative}")
    private String declarative;

    @Autowired
    public RRepositoryModelAssembler(
            DtoConverter<RRepository, RRepositoryDto> dtoConverter, SecurityMediator securityMediator) {
        super(dtoConverter, RRepositoryController.class, "repository", Optional.empty());
        this.securityMediator = securityMediator;
    }

    private RRepositoryModelAssembler(
            DtoConverter<RRepository, RRepositoryDto> dtoConverter, SecurityMediator securityMediator, User user) {
        super(dtoConverter, RRepositoryController.class, "repository", Optional.of(user));
        this.securityMediator = securityMediator;
    }

    @Override
    protected List<Link> getLinksToMethodsWithLimitedAccess(RRepository entity, User user, Link baseLink) {
        List<Link> links = new ArrayList<>();

        if (securityMediator.isAuthorizedToEdit(entity, user) && !Boolean.valueOf(declarative)) {
            links.add(baseLink.withType(HTTP_METHODS.PATCH.getValue()));
            links.add(baseLink.withType(HTTP_METHODS.DELETE.getValue()));
        }

        return links;
    }

    @Override
    public RepresentationModelAssembler<RRepository, EntityModel<RRepositoryDto>> assemblerWithUser(User user) {
        return new RRepositoryModelAssembler(dtoConverter, securityMediator, user);
    }

    @Override
    protected Class<?> getExtensionControllerClass(RRepository entity) {
        return RRepositoryController.class;
    }
}

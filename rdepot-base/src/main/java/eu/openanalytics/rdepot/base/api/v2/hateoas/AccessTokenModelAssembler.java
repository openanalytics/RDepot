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

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2AccessTokenController;
import eu.openanalytics.rdepot.base.api.v2.converters.DtoConverter;
import eu.openanalytics.rdepot.base.api.v2.dtos.AccessTokenDto;
import eu.openanalytics.rdepot.base.entities.AccessToken;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.UserService;
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
 * for {@link AccessToken Access Tokens}.
 */
@Component
public class AccessTokenModelAssembler extends AbstractRoleAwareModelAssembler<AccessToken, AccessTokenDto> {

    private final UserService userService;

    @Autowired
    public AccessTokenModelAssembler(DtoConverter<AccessToken, AccessTokenDto> dtoConverter, UserService userService) {
        super(dtoConverter, ApiV2AccessTokenController.class, "accessToken", Optional.empty());
        this.userService = userService;
    }

    private AccessTokenModelAssembler(
            DtoConverter<AccessToken, AccessTokenDto> dtoConverter, UserService userService, User user) {
        super(dtoConverter, ApiV2AccessTokenController.class, "accessToken", Optional.of(user));
        this.userService = userService;
    }

    @Override
    public RepresentationModelAssembler<AccessToken, EntityModel<AccessTokenDto>> assemblerWithUser(User user) {
        return new AccessTokenModelAssembler(dtoConverter, userService, user);
    }

    @Override
    protected List<Link> getLinksToMethodsWithLimitedAccess(AccessToken entity, User user, Link baseLink) {
        List<Link> links = new ArrayList<>();
        if (user.getId() == entity.getUser().getId()) {
            links.add(baseLink.withType(HTTP_METHODS.PATCH.getValue()));
            links.add(baseLink.withType(HTTP_METHODS.DELETE.getValue()));
        }
        return links;
    }

    @Override
    protected Class<?> getExtensionControllerClass(AccessToken entity) {
        return ApiV2AccessTokenController.class;
    }
}

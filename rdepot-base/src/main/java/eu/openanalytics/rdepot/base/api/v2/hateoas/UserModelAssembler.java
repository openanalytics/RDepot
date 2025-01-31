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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2UserController;
import eu.openanalytics.rdepot.base.api.v2.converters.DtoConverter;
import eu.openanalytics.rdepot.base.api.v2.dtos.UserDto;
import eu.openanalytics.rdepot.base.api.v2.hateoas.linking.LinkWithModifiableProperties;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class UserModelAssembler extends AbstractRoleAwareModelAssembler<User, UserDto> {

    private final UserService userService;

    @Autowired
    public UserModelAssembler(DtoConverter<User, UserDto> dtoConverter, UserService userService) {
        super(dtoConverter, ApiV2UserController.class, "user", Optional.empty());
        this.userService = userService;
    }

    private UserModelAssembler(DtoConverter<User, UserDto> dtoConverter, UserService userService, User user) {
        super(dtoConverter, ApiV2UserController.class, "user", Optional.of(user));
        this.userService = userService;
    }

    @Override
    protected List<Link> getLinksToMethodsWithLimitedAccess(User entity, User user, Link baseLink) {
        final List<Link> links = new ArrayList<>();

        if (userService.isAdmin(user)) {
            links.add(linkTo(baseControllerClass).withRel("userList"));
            final String[] commonProperties = new String[] {"active", "roleId"};
            final String[] modifiableProperties = user.getId() == entity.getId()
                    ? commonProperties
                    : // A user cannot delete themselves
                    ArrayUtils.addFirst(commonProperties, "deleted");
            final LinkWithModifiableProperties link = new LinkWithModifiableProperties(
                    baseLink.withType(HTTP_METHODS.PATCH.getValue()), modifiableProperties);
            links.add(link);
        }

        return links;
    }

    @Override
    protected List<Link> generateAvailableLinksForEntity(User entity, Class<?> extensionControllerClass) {
        return List.of(linkTo(baseControllerClass).slash(entity.getId()).withSelfRel());
    }

    @Override
    public RepresentationModelAssembler<User, EntityModel<UserDto>> assemblerWithUser(User user) {
        return new UserModelAssembler(dtoConverter, userService, user);
    }

    @Override
    protected Class<?> getExtensionControllerClass(User entity) {
        return ApiV2UserController.class;
    }
}

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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2UserController;
import eu.openanalytics.rdepot.base.api.v2.controllers.ApiV2UserSettingsController;
import eu.openanalytics.rdepot.base.api.v2.converters.DtoConverter;
import eu.openanalytics.rdepot.base.api.v2.dtos.UserSettingsDto;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.UserSettings;
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
 * for {@link UserSettings User Settings}.
 */
@Component
public class UserSettingsModelAssembler extends AbstractRoleAwareModelAssembler<UserSettings, UserSettingsDto> {

    private final UserService userService;

    @Autowired
    public UserSettingsModelAssembler(
            DtoConverter<UserSettings, UserSettingsDto> dtoConverter, UserService userService) {
        super(dtoConverter, ApiV2UserSettingsController.class, "userSetting", Optional.empty());
        this.userService = userService;
    }

    private UserSettingsModelAssembler(
            DtoConverter<UserSettings, UserSettingsDto> dtoConverter, UserService userService, User user) {
        super(dtoConverter, ApiV2UserSettingsController.class, "userSetting", Optional.of(user));
        this.userService = userService;
    }

    @Override
    protected List<Link> getLinksToMethodsWithLimitedAccess(UserSettings entity, User user, Link baseLink) {
        List<Link> links = new ArrayList<>();

        if (user.getId() == entity.getUser().getId() || userService.isAdmin(user)) {
            links.add(linkTo(ApiV2UserSettingsController.class)
                    .slash(entity.getUser().getId())
                    .withSelfRel());
            links.add(linkTo(ApiV2UserSettingsController.class)
                    .slash(entity.getUser().getId())
                    .withSelfRel()
                    .withType(HTTP_METHODS.PATCH.getValue()));
            links.add(linkTo(ApiV2UserController.class)
                    .slash(entity.getUser().getId())
                    .withRel("user"));
        }

        return links;
    }

    @Override
    protected List<Link> generateAvailableLinksForEntity(UserSettings entity, Class<?> extensionControllerClass) {
        return List.of();
    }

    @Override
    public RepresentationModelAssembler<UserSettings, EntityModel<UserSettingsDto>> assemblerWithUser(User user) {
        return new UserSettingsModelAssembler(dtoConverter, userService, user);
    }

    @Override
    protected Class<?> getExtensionControllerClass(UserSettings entity) {
        return ApiV2UserSettingsController.class;
    }
}

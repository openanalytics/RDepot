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
package eu.openanalytics.rdepot.base.api.v2.converters;

import eu.openanalytics.rdepot.base.api.v2.converters.exceptions.EntityResolutionException;
import eu.openanalytics.rdepot.base.api.v2.dtos.UserDto;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.RoleService;
import org.springframework.stereotype.Component;

/**
 * {@link DtoConverter DTO Converter} for {@link User Users}
 */
@Component
public class UserDtoConverter implements DtoConverter<User, UserDto> {

    private final RoleService roleService;

    public UserDtoConverter(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public User resolveDtoToEntity(UserDto dto) throws EntityResolutionException {
        Role role = roleService.findById(dto.getRoleId()).orElseThrow(() -> new EntityResolutionException(dto));
        return new User(dto, role);
    }

    @Override
    public UserDto convertEntityToDto(User entity) {
        return new UserDto(entity);
    }
}

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
package eu.openanalytics.rdepot.base.api.v2.converters;

import eu.openanalytics.rdepot.base.api.v2.converters.exceptions.EntityResolutionException;
import eu.openanalytics.rdepot.base.api.v2.dtos.RepositoryMaintainerDto;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * {@link DtoConverter DTO Converter} for {@link RepositoryMaintainer Repository Maintaienrs}
 */
@Component
@AllArgsConstructor
public class RepositoryMaintainerDtoConverter implements DtoConverter<RepositoryMaintainer, RepositoryMaintainerDto> {

    private final UserService userService;
    private final RepositoryService<Repository> repositoryService;

    @Override
    public RepositoryMaintainer resolveDtoToEntity(RepositoryMaintainerDto dto) throws EntityResolutionException {
        User user = userService.findById(dto.getUser().getId()).orElseThrow(() -> new EntityResolutionException(dto));
        Repository repository = repositoryService
                .findById(dto.getRepository().getId())
                .orElseThrow(() -> new EntityResolutionException(dto));

        return new RepositoryMaintainer(dto, repository, user);
    }

    @Override
    public RepositoryMaintainerDto convertEntityToDto(RepositoryMaintainer entity) {
        return new RepositoryMaintainerDto(entity);
    }
}

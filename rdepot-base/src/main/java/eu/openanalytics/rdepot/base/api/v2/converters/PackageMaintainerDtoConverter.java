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
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageMaintainerDto;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * {@link DtoConverter DTO Converter} for {@link PackageMaintainer Package Maintainers}
 */
@Component
@AllArgsConstructor
public class PackageMaintainerDtoConverter implements DtoConverter<PackageMaintainer, PackageMaintainerDto> {

    private final RepositoryService<Repository> repositoryService;
    private final UserService userService;

    @Override
    public PackageMaintainer resolveDtoToEntity(PackageMaintainerDto dto) throws EntityResolutionException {
        Repository repository = repositoryService
                .findById(dto.getRepository().getId())
                .orElseThrow(() -> new EntityResolutionException(dto));
        User user = userService.findById(dto.getUser().getId()).orElseThrow(() -> new EntityResolutionException(dto));
        return new PackageMaintainer(dto, repository, user);
    }

    @Override
    public PackageMaintainerDto convertEntityToDto(PackageMaintainer entity) {
        return new PackageMaintainerDto(entity);
    }
}

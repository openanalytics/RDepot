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
import eu.openanalytics.rdepot.base.api.v2.dtos.RepositoryDto;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.time.DateProvider;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

/**
 * {@link DtoConverter DTO Converter} for {@link Repository Repositories}
 */
@Component
@AllArgsConstructor
public class RepositoryDtoConverter implements DtoConverter<Repository, RepositoryDto> {

    private final PackageService<Package> packageService;

    @Override
    public Repository resolveDtoToEntity(RepositoryDto dto) throws EntityResolutionException {
        throw new NotImplementedException();
    }

    @Override
    public RepositoryDto convertEntityToDto(Repository entity) {
        return new RepositoryDto(
                entity,
                packageService.countByRepository(entity),
                entity.getLastPublicationTimestamp() != null
                        ? DateProvider.instantToTimestamp(entity.getLastPublicationTimestamp())
                        : "",
                DateProvider.instantToTimestamp(entity.getLastModifiedTimestamp()),
                entity.isLastPublicationSuccessful());
    }
}

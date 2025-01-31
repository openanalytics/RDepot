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
package eu.openanalytics.rdepot.r.api.v2.converters;

import eu.openanalytics.rdepot.base.api.v2.converters.DtoConverter;
import eu.openanalytics.rdepot.base.api.v2.converters.exceptions.EntityResolutionException;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.time.DateProvider;
import eu.openanalytics.rdepot.r.api.v2.dtos.RRepositoryDto;
import eu.openanalytics.rdepot.r.entities.RRepository;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RRepositoryDtoConverter implements DtoConverter<RRepository, RRepositoryDto> {
    private final PackageService<Package> packageService;

    @Override
    public RRepository resolveDtoToEntity(RRepositoryDto dto) throws EntityResolutionException {
        try {
            final Instant lastPublicationTimestamp = dto.getLastPublicationTimestamp() != null
                            && !dto.getLastPublicationTimestamp().isEmpty()
                    ? DateProvider.timestampToInstant(dto.getLastPublicationTimestamp())
                    : null;
            final Instant lastModifiedTimestamp = dto.getLastModifiedTimestamp() != null
                            && !dto.getLastModifiedTimestamp().isEmpty()
                    ? DateProvider.timestampToInstant(dto.getLastModifiedTimestamp())
                    : DateProvider.now();
            return new RRepository(dto, lastPublicationTimestamp, lastModifiedTimestamp);
        } catch (DateTimeParseException e) {
            throw new EntityResolutionException(dto);
        }
    }

    @Override
    public RRepositoryDto convertEntityToDto(RRepository entity) {
        return new RRepositoryDto(
                entity,
                packageService.countByRepository(entity),
                entity.getLastPublicationTimestamp() != null
                        ? DateProvider.instantToTimestamp(entity.getLastPublicationTimestamp())
                        : "",
                DateProvider.instantToTimestamp(entity.getLastModifiedTimestamp()),
                entity.isLastPublicationSuccessful());
    }
}

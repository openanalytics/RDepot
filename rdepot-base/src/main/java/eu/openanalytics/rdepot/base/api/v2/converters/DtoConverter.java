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
import eu.openanalytics.rdepot.base.api.v2.dtos.IDto;
import eu.openanalytics.rdepot.base.entities.Resource;

/**
 * Converts DTOs to entities and vice-versa.
 * @param <E> entity type
 * @param <D> dto type
 */
public interface DtoConverter<E extends Resource, D extends IDto> {
    /**
     * Converts DTO to an entity (together with nested properties).
     * @throws EntityResolutionException when related entities cannot be found
     */
    E resolveDtoToEntity(D dto) throws EntityResolutionException;

    /**
     * Converts entity to DTO.
     */
    D convertEntityToDto(E entity);
}

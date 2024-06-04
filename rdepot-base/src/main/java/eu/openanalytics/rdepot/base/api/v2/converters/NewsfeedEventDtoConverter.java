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
import eu.openanalytics.rdepot.base.api.v2.dtos.NewsfeedEventDto;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Component;

/**
 * {@link DtoConverter DTO Converter} for {@link NewsfeedEvent Newsfeed Events}
 */
@Component
public class NewsfeedEventDtoConverter implements DtoConverter<NewsfeedEvent, NewsfeedEventDto> {

    @Override
    public NewsfeedEvent resolveDtoToEntity(NewsfeedEventDto dto) throws EntityResolutionException {
        throw new NotImplementedException(
                "Events are read-only " + "and only registered internally by the application.");
    }

    @Override
    public NewsfeedEventDto convertEntityToDto(NewsfeedEvent entity) {
        return new NewsfeedEventDto(entity);
    }
}

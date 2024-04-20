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

import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.api.v2.converters.exceptions.EntityResolutionException;
import eu.openanalytics.rdepot.base.api.v2.dtos.AccessTokenDto;
import eu.openanalytics.rdepot.base.entities.AccessToken;

/**
 * {@link DtoConverter DTO Converter} for {@link AccessToken Access Tokens}
 */
@Component
public class AccessTokenDtoConverter implements DtoConverter<AccessToken, AccessTokenDto>{
	
	@Override
	public AccessToken resolveDtoToEntity(AccessTokenDto dto) throws EntityResolutionException {
		return new AccessToken(dto);
	}

	@Override
	public AccessTokenDto convertEntityToDto(AccessToken entity) {
		return new AccessTokenDto(entity);
	}
	
}


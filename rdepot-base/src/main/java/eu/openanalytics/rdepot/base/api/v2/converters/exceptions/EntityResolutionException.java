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
package eu.openanalytics.rdepot.base.api.v2.converters.exceptions;

import eu.openanalytics.rdepot.base.api.v2.dtos.IDto;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;

import java.io.Serial;

/**
 * It is thrown during DTO to Entity conversion
 * when one of related entities cannot be found in the database.
 */
public class EntityResolutionException extends Exception {

	@Serial
	private static final long serialVersionUID = 5861884979279129715L;

	public EntityResolutionException(IDto dto) {
		super(StaticMessageResolver.getMessage(
				MessageCodes.RESOLVE_RELATED_ENTITIES_EXCEPTION) + ": " + dto.toString());
	}
}

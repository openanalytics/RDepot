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
package eu.openanalytics.rdepot.base.entities;

import eu.openanalytics.rdepot.base.api.v2.converters.DtoConverter;
import eu.openanalytics.rdepot.base.api.v2.dtos.IDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.NewsfeedEventDto;

/**
 * Resource that can be converted to a {@link IDto DTO} 
 * that will represent a simple overview of the object 
 * as related to another resource. 
 * For any more sophisticated representations 
 * a {@link DtoConverter} should be used 
 * as it allows to fetch additional information from the database.
 * For example, it is used for {@link NewsfeedEventDto}
 * to create a nested related resource's DTO.
 */
public interface HavingSimpleDtoRepresentation {
	
	/**
	 * Creates a simple representation of the entity.
	 * For any more sophisticated representations 
	 * (e.g. those that require fetching additional information from the database)
	 * a {@link DtoConverter} should be used.
     */
	IDto createSimpleDto();
}

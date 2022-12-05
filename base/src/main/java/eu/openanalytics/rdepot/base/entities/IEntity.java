/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.base.entities;

/**
 * Should be implemented by final entities.
 * "Final" means that they should not be further extended by any other entity.
 * It is used to create a DTO used to communicate via API v2.
 * @param <T> Class representing entity's DTO, e.g.
 * 		{@link eu.openanalytics.rdepot.r.api.v2.dtos.RPackageDto RPackageDto}
 */
public interface IEntity<T> {
	
	/**
	 * Creates a DTO.
	 * @return implementation of {@link eu.openanalytics.rdepot.api.v2.dtos.IDto}
	 */
	T createDto();
}

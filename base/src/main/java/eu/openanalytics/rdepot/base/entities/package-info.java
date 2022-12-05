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
/**
 * Java objects representing the actual data in the database.
 * They are used to access and modify data.
 * It is important to note that they should <b>not</b> be used to communicate via the API.
 * For this purpose, one should use DTOs that are much simpler and 
 * do not impact the application state in any way.
 * @see eu.openanalytics.rdepot.base.api.v2.dtos.IDto
 * @see eu.openanalytics.rdepot.base.entities.IEntity
 * @see org.hibernate.annotations.Entity
 */
package eu.openanalytics.rdepot.base.entities;
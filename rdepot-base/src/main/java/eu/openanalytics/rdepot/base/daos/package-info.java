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
/**
 * JPA Repository interfaces for the entities.
 * It is encouraged to access them only from the
 * {@link eu.openanalytics.rdepot.base.service.Service services}.
 * It is a preferred, though not only way to fetch or save data in the database.
 * Extended interfaces should support pagination.
 * For more sophisticated searching, specifications can be used.
 * @see org.springframework.data.domain.Pageable
 * @see org.springframework.data.jpa.domain.Specification
 */
package eu.openanalytics.rdepot.base.daos;

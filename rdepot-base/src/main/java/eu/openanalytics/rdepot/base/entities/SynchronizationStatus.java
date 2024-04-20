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

import java.util.Date;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.openanalytics.rdepot.base.technology.Technology;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents status of synchronization (publication) for given repository.
 */
@Data
@NoArgsConstructor
public class SynchronizationStatus {
	private Integer repositoryId;
	private Date timestamp;
	private Optional<Exception> error = Optional.empty();
	private boolean pending;

	@JsonIgnore
	private Technology technology;
}

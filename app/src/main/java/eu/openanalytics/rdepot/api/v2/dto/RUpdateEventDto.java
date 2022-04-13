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
package eu.openanalytics.rdepot.api.v2.dto;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;

public class RUpdateEventDto<E, T extends EntityDto<?>> extends REventDto<E, T> {

	private Set<UpdatedVariable> updatedVariables = new HashSet<UpdatedVariable>();
	
	public RUpdateEventDto() {
		super(EventType.UPDATE);
	}
	
	public RUpdateEventDto(E entity, Integer id, UserDto user, RRepositoryDto repository, 
			String timestamp, T resource, Set<UpdatedVariable> updatedVariables) {
		super(entity, id, user, repository, timestamp, resource, EventType.UPDATE);
		this.updatedVariables = updatedVariables;
	}

	public RUpdateEventDto(E entity, Integer id, UserDto user, RRepositoryDto repository, 
			String timestamp, T resource) {
		super(entity, id, user, repository, timestamp, resource, EventType.UPDATE);
	}
	
	public RUpdateEventDto(Set<UpdatedVariable> updatedVariables) {
		this();
		this.updatedVariables = updatedVariables;
	}

	public Set<UpdatedVariable> getUpdatedVariables() {
		return updatedVariables;
	}

	public void setUpdatedVariables(Set<UpdatedVariable> updatedVariables) {
		this.updatedVariables = updatedVariables;
	}

	@Override
	public E toEntity() {
		throw new NotImplementedException(); //events are read-only so far
	}
}

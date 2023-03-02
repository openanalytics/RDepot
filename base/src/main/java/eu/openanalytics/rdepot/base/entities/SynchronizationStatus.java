/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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

import java.util.Date;
import java.util.Optional;

import eu.openanalytics.rdepot.base.technology.Technology;

public class SynchronizationStatus implements IEntity<SynchronizationStatus> {
	
	private Integer repositoryId;
	private Date timestamp;
	private Optional<Exception> error = Optional.empty();
	private Boolean pending;
	private Technology technology;
	
	public SynchronizationStatus() {
		
	}

	public Integer getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(Integer repositoryId) {
		this.repositoryId = repositoryId;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Optional<Exception> getError() {
		return error;
	}

	public void setError(Optional<Exception> error) {
		this.error = error;
	}

	public Boolean isPending() {
		return pending;
	}

	public void setPending(Boolean pending) {
		this.pending = pending;
	}

	@Override
	public SynchronizationStatus createDto() {
		return this;
	}
	
	public Technology getTechnology() {
		return technology;
	}
	
	public void setTechnology(Technology technology) {
		this.technology = technology;
	}
}

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
package eu.openanalytics.rdepot.model;

import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;

import eu.openanalytics.rdepot.api.v2.dto.EntityDto;

public class SynchronizationStatus extends EntityDto<SynchronizationStatus> {
	
	private Integer repositoryId;
	private Date timestamp;
	private Optional<Exception> error = Optional.empty();
	private Boolean pending;
	
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

	/**
	 * Synchronization status is read-only so this should never be used.
	 * @throws NotImplementedException
	 */
	@Override
	public SynchronizationStatus toEntity() {
		throw new NotImplementedException();
	}	
}

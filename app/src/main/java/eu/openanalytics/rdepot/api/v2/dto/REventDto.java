/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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

public abstract class REventDto<E, T extends EntityDto<?>> extends EntityDto<E> {
	private Integer id = 0;
	private UserDto user;
	private RRepositoryDto repository;
	private String timestamp = "";
	private T resource;
	private EventType eventType;
	
	public REventDto(EventType eventType) {
		this.eventType = eventType;
	}

	public REventDto(E entity, Integer id, UserDto user, RRepositoryDto repository, String timestamp, T resource,
			EventType eventType) {
		super(entity);
		this.id = id;
		this.user = user;
		this.repository = repository;
		this.timestamp = timestamp;
		this.resource = resource;
		this.eventType = eventType;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public UserDto getUser() {
		return user;
	}

	public void setUser(UserDto user) {
		this.user = user;
	}

	public RRepositoryDto getRepository() {
		return repository;
	}

	public void setRepository(RRepositoryDto repository) {
		this.repository = repository;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public T getResource() {
		return resource;
	}

	public void setResource(T resource) {
		this.resource = resource;
	}

	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}
}

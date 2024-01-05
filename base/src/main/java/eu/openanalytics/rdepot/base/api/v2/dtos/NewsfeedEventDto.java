/**
 * R Depot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.base.api.v2.dtos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;

/**
 * DTO for Newsfeed Events.
 */
public class NewsfeedEventDto implements IDto<NewsfeedEvent> {

	private final int id;
	private final String technology;
	private final String time;
	private final UserDtoShort user;
	private final String eventType;
	private final String description; //TODO: As list?
	private final int resourceId; //123
	private final ResourceType resourceType;
	private final String resourceDescription; //Package oaColor v1.0.3 
	private NewsfeedEvent entity;
	private final List<ChangedVariableDto> changedProperties;

	@Override
	public NewsfeedEvent getEntity() {
		return entity;
	}

	@Override
	public void setEntity(NewsfeedEvent entity) {
		this.entity = entity;
	}
	
	public NewsfeedEventDto(NewsfeedEvent entity) {
		this.entity = entity;
		this.technology = entity.getTechnology().getName() 
				+ " version: " 
				+ entity.getTechnology().getVersion();
		LocalDateTime fullDate = LocalDateTime.of(entity.getDate(), entity.getTime().toLocalTime());
		this.time = fullDate.format(DateTimeFormatter.ISO_DATE_TIME);
		this.user = entity.getAuthor().createDtoShort();
		this.eventType = entity.getType().getValue();
		this.description = entity.getDescription();
		this.resourceId = entity.getRelatedResource().getId();
		this.id = entity.getId();
		this.resourceDescription = entity.getRelatedResource().getDescription();
		this.resourceType = entity.getRelatedResource().getResourceType();
		this.changedProperties = entity.getEventChangedVariables().stream()
				.map(ChangedVariableDto::of)
				.sorted()
				.collect(Collectors.toList());
	}

	public int getId() {
		return id;
	}

	public String getTechnology() {
		return technology;
	}

	public String getTime() {
		return time;
	}

	public UserDtoShort getUser() {
		return user;
	}

	public String getEventType() {
		return eventType;
	}

	public String getDescription() {
		return description;
	}

	public int getResourceId() {
		return resourceId;
	}

	public String getResourceDescription() {
		return resourceDescription;
	}
	
	public ResourceType getResourceType() {
		return resourceType;
	}
	
	public List<ChangedVariableDto> getChangedProperties() {
		return changedProperties;
	}
}

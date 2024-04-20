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
package eu.openanalytics.rdepot.base.api.v2.dtos;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import lombok.Getter;

/**
 * Data Transfer Object for {@link NewsfeedEvent Newsfeed Events}.
 */
@Getter
public class NewsfeedEventDto implements IDto {

	private final int id;
	private final String technology;
	private final String time;
	private final UserProjection user;
	private final String eventType;
	private final int resourceId;
	private final ResourceType resourceType;
	private final NewsfeedEvent entity;
	private final List<ChangedVariableDto> changedProperties;
	private final IDto relatedResource;

	@Override
	public NewsfeedEvent getEntity() {
		return entity;
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
		this.resourceId = entity.getRelatedResource().getId();
		this.id = entity.getId();
		this.resourceType = entity.getRelatedResource().getResourceType();
		this.changedProperties = entity.getEventChangedVariables().stream()
				.map(ChangedVariableDto::of)
				.sorted()
				.collect(Collectors.toList());
		this.relatedResource = entity.getRelatedResource().createSimpleDto();
	}
}
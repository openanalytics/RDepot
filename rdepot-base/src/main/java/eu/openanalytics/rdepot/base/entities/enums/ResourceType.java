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
package eu.openanalytics.rdepot.base.entities.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Provides information about type of given
 * {@link eu.openanalytics.rdepot.base.entities.Resource Resource}
 * without the need to use reflection or down-casting. 
 */
@Getter
@AllArgsConstructor
public enum ResourceType {
	PACKAGE("PCK"),
	REPOSITORY("REP"),
	SUBMISSION("SUB"),
	REPOSITORY_MAINTAINER("RPM"),
	PACKAGE_MAINTAINER("PKM"),
	USER("USR"),
	EVENT("EVN"),
	ROLE("ROL"),
	USER_SETTINGS("SET"),
	ACCESS_TOKEN("ACT");

	private final String value;
}

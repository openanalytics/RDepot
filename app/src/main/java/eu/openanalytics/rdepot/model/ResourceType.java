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

public enum ResourceType {
	PACKAGE("package"),
	REPOSITORY("repository"),
	SUBMISSION("submission"),
	UPLOAD_REQUEST("upload-request"),
	EVENT("event"),
	USER("user"),
	PACKAGE_MAINTAINER("package-maintainer"),
	REPOSITORY_MAINTAINER("repository-maintainer"),
	ROLE("role");
	
	private String value;
	
	ResourceType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
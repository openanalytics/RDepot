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
package eu.openanalytics.rdepot.base.entities.enums;

public enum SubmissionState {
	ACCEPTED("accepted"),//deleted = false; accepted = true; OR deleted = true; accepted = true;
	WAITING("waiting"), //deleted = false; accepted = false;
	CANCELLED("cancelled"), //deleted = true; accepted = false;
	REJECTED("rejected"); //deleted = true for future release
	
	private final String state;
	
	SubmissionState(String state) {
		this.state = state;
	}
	
	public String getValue() {
		return state;
	}
}
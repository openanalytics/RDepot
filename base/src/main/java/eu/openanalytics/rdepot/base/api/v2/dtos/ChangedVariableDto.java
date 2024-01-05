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

import eu.openanalytics.rdepot.base.entities.EventChangedVariable;

public class ChangedVariableDto implements Comparable<ChangedVariableDto> {

	private String property;
	private Object valueBefore;
	private Object valueAfter;
	
	private ChangedVariableDto(EventChangedVariable variable) {
		this.property = variable.getChangedVariable();
		this.valueBefore = variable.getValueBefore();
		this.valueAfter = variable.getValueAfter();
	}
	
	public static ChangedVariableDto of(EventChangedVariable variable) {
		return new ChangedVariableDto(variable);
	}

	public String getProperty() {
		return property;
	}

	public Object getValueBefore() {
		return valueBefore;
	}

	public Object getValueAfter() {
		return valueAfter;
	}

	@Override
	public int compareTo(ChangedVariableDto that) {
		return this.property.compareTo(that.property);
	}
	
	
}

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
package eu.openanalytics.rdepot.base.api.v2.dtos;

public class UpdatedVariable {
	String changedVariable;
	String valueBefore;
	String valueAfter;
	
	public UpdatedVariable(String changedVariable, String valueBefore, String valueAfter) {
		super();
		this.changedVariable = changedVariable;
		this.valueBefore = valueBefore;
		this.valueAfter = valueAfter;
	}

	public String getChangedVariable() {
		return changedVariable;
	}

	public void setChangedVariable(String changedVariable) {
		this.changedVariable = changedVariable;
	}

	public String getValueBefore() {
		return valueBefore;
	}

	public void setValueBefore(String valueBefore) {
		this.valueBefore = valueBefore;
	}

	public String getValueAfter() {
		return valueAfter;
	}

	public void setValueAfter(String valueAfter) {
		this.valueAfter = valueAfter;
	}
}

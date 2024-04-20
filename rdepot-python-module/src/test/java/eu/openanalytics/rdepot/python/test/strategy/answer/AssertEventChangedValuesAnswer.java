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
package eu.openanalytics.rdepot.python.test.strategy.answer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import eu.openanalytics.rdepot.base.entities.EventChangedVariable;

public class AssertEventChangedValuesAnswer implements Answer<Object> {
	
	private final Set<EventChangedVariable> expectedValues;

	public AssertEventChangedValuesAnswer(Set<EventChangedVariable> expectedValues) {
		this.expectedValues = expectedValues;
	}
	
	@Override
	public Object answer(InvocationOnMock invocation) throws Throwable {
		Set<EventChangedVariable> changedValues = invocation.getArgument(1);
		assertTrue(changedValues.containsAll(expectedValues),"Not all changed values were registered.");
		return null;
	}
}

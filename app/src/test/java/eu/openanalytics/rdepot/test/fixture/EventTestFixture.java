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
package eu.openanalytics.rdepot.test.fixture;

import java.util.ArrayList;
import java.util.List;

import eu.openanalytics.rdepot.model.Event;

public class EventTestFixture {
	
	public static final String EVENT_VALUE = "some value";
	
	public static List<Event> GET_FIXTURE_EVENTS(int eventCount) {
		List<Event> events = new ArrayList<>();
		
		for(int i = 0; i < eventCount; i++) {
			events.add(new Event(i, EVENT_VALUE));
		}
		
		return events;
	}
	
	public static Event GET_FIXTURE_EVENT() {
		return GET_FIXTURE_EVENTS(1).get(0);
	}
	
	public static Event GET_FIXTURE_EVENT(String value) {
		Event event = GET_FIXTURE_EVENT();
		event.setValue(value);
		return event;
	}
}

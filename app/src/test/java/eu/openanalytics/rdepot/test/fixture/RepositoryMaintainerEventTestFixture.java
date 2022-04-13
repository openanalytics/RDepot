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
package eu.openanalytics.rdepot.test.fixture;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.RepositoryMaintainerEvent;
import eu.openanalytics.rdepot.model.User;

public class RepositoryMaintainerEventTestFixture {
	
	public static final String VALUE_BEFORE = "1234";
	public static final String VALUE_AFTER = "5678";
	
	public static final String[] FIELDS_TO_CHANGE = {
			"version",
			"publication URI",
			"server address",
			"name"
	};
	
	public static List<RepositoryMaintainerEvent> GET_FIXTURE_REPOSITORY_MAINTAINER_EVENTS(User changedBy, RepositoryMaintainer repositoryMaintainer, int eventCount, int shift) {
		List<RepositoryMaintainerEvent> repositoryMaintainerEvents = new ArrayList<>();
		
		
		
		for(int i = shift; i < eventCount + shift; i++) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, i);
			Date date = cal.getTime();
			
			cal.add(Calendar.MINUTE, i);
			Date time = cal.getTime();
			
			Random random = new Random();
			String changedVariable = FIELDS_TO_CHANGE[random.nextInt(FIELDS_TO_CHANGE.length)];
			
			String valueBefore = VALUE_BEFORE + Integer.toString(i);
			String valueAfter = VALUE_AFTER + Integer.toString(i);
			
			Event event = new Event(i, Integer.toString(random.nextInt(12345)));
			repositoryMaintainerEvents.add(new RepositoryMaintainerEvent(i, date, changedBy, repositoryMaintainer, event, 
		    		changedVariable, valueBefore, valueAfter, time));
		}
		
		return repositoryMaintainerEvents;
	}
	
	public static List<RepositoryMaintainerEvent> GET_FIXTURE_REPOSITORY_MAINTAINER_EVENTS(User changedBy, RepositoryMaintainer repositoryMaintainer, int eventCount) {
		return GET_FIXTURE_REPOSITORY_MAINTAINER_EVENTS(changedBy, repositoryMaintainer, eventCount, 0);
	}
}

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
import eu.openanalytics.rdepot.model.PackageEvent;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.model.Package;

public class PackageEventTestFixture {
	
	public static final String VALUE_BEFORE = "1234abc";
	public static final String VALUE_AFTER = "5678def";
	
	public static final String[] FIELDS_TO_CHANGE = {
			"version",
			"repository",
			"maintainer",
			"name",
			"description",
			"author",
			"depends",
			"imports",
			"suggests",
			"system requirements",
			"license",
			"title",
			"URL",
			"source",
			"md5sum",
			"active"
	};
	
	public static List<PackageEvent> GET_FIXTURE_SORTED_PACKAGE_EVENTS(User changedBy, Package packageBag, int days, int eventsPerDay/*, int shift*/) {
		List<PackageEvent> packageEvents = new ArrayList<>();
		
		for(int i = 0; i < days * eventsPerDay; i += eventsPerDay) {	
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, i);
			Date date = cal.getTime();
			
			Random random = new Random();
			String changedVariable = FIELDS_TO_CHANGE[random.nextInt(FIELDS_TO_CHANGE.length)];
			
			String valueBefore = VALUE_BEFORE + Integer.toString(i);
			String valueAfter = VALUE_AFTER + Integer.toString(i);
			
			Event event = new Event(i, Integer.toString(random.nextInt(12345)));
			
			for(int k = 0; k < eventsPerDay; k++) {
				cal.add(Calendar.MINUTE, i+k);
				Date time = cal.getTime();
				PackageEvent packageEvent = new PackageEvent(i + k, date, changedBy, packageBag, event, changedVariable, valueBefore, valueAfter, time);
				
				packageEvents.add(packageEvent);
			}
			
		}
		
		return packageEvents;
	}
	
//	public static List<PackageEvent> GET_FIXTURE_SORTED_PACKAGE_EVENTS(User changedBy, Package packageBag, int eventCount, int eventsPerDay) {
//		return GET_FIXTURE_SORTED_PACKAGE_EVENTS(changedBy, packageBag, eventCount, eventsPerDay, 0);
//	}
}

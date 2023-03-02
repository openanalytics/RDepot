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
package eu.openanalytics.rdepot.base.event.legacy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.openanalytics.rdepot.base.entities.EventChangedVariable;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;

@Deprecated
public class LegacyEventFormatter {
	public static Map<String, List<Map<String, Object>>> formatEvents(
			Map<LocalDate, List<NewsfeedEvent>> events) {
		Map<String, List<Map<String, Object>>> response = new LinkedHashMap<>();
		DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		for(LocalDate date : events.keySet()) {
			String key = date.format(dayFormatter);
			
			List<Map<String, Object>> values = new LinkedList<>();
			
			for(NewsfeedEvent e : events.get(date)) {
				values.addAll(eventToHashMaps(e));
			}
			
			response.put(key, values);
		}
		
		return response;
	}

	private static Set<Map<String, Object>> eventToHashMaps(NewsfeedEvent e) {
		if(e.getType() == NewsfeedEventType.UPLOAD 
				|| e.getType() == NewsfeedEventType.CREATE) {
			Map<String, Object> map = eventToHashMap(e);
			
			switch(e.getType()) {
			case UPLOAD:
				map.put("changedVariable", "uploaded");
				break;
			case CREATE:
				map.put("changedVariable", "created");
				break;
			default:
				map.put("changedVariable", "");
			}
			
			map.put("valueBefore", "");
			map.put("valueAfter", "");
			
			return Set.of(map);
		} else if(e.getType() == NewsfeedEventType.UPDATE) {
			Set<Map<String, Object>> events = new HashSet<>();
			
			for(EventChangedVariable v : e.getEventChangedVariables()) {
				Map<String, Object> map = eventToHashMap(e);
				
				map.put("changedVariable", v.getChangedVariable());
				map.put("valueBefore", v.getValueBefore());
				map.put("valueAfter", v.getValueAfter());
				
				events.add(map);
			}
			
			return events;
		} else {
			return Set.of();
		}
	}
	
	private static Map<String, Object> eventToHashMap(NewsfeedEvent e) {
		Map<String, Object> map = new HashMap<>();
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh-mm");
		
		map.put("id", Integer.toString(e.getId()));
		map.put("repositoryName", e.getRelatedResource().getDescription());
		map.put("maintainer", e.getAuthor().getName());
		map.put("time", e.getTime().format(timeFormatter));
		
		return map;
	}
}

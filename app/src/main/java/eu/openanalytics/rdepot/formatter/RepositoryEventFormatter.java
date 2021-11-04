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
package eu.openanalytics.rdepot.formatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.model.RepositoryEvent;

@Component
public class RepositoryEventFormatter {

	public HashMap<String, String> eventToHashMap(RepositoryEvent repositoryEvent) {
		HashMap<String, String> map = new HashMap<String, String>();
		SimpleDateFormat timeFormatter = new SimpleDateFormat("hh-mm");
		
		map.put("valueBefore", repositoryEvent.getValueBefore());
		map.put("id", Integer.toString(repositoryEvent.getId()));
		map.put("valueAfter", repositoryEvent.getValueAfter());
		map.put("changedVariable", repositoryEvent.getChangedVariable());
		map.put("repositoryName", repositoryEvent.getRepository().getName());
		map.put("maintainer", repositoryEvent.getChangedBy().getName());
		map.put("time", timeFormatter.format(repositoryEvent.getTime()));
		
		return map;
	}

	public LinkedHashMap<String, ArrayList<HashMap<String, String>>> formatEvents(
			TreeMap<Date, ArrayList<RepositoryEvent>> events) {
		SimpleDateFormat dayFormatter = new SimpleDateFormat("yyyy-MM-dd");
		LinkedHashMap<String, ArrayList<HashMap<String, String>>> response = 
				new LinkedHashMap<String, ArrayList<HashMap<String, String>>>();
		
		for(Date date : events.keySet()) {
			String key = dayFormatter.format(date);
			ArrayList<HashMap<String,String>> values = new ArrayList<HashMap<String, String>>();
			
			for(RepositoryEvent repositoryEvent : events.get(date)) {
				HashMap<String, String> repositoryEventStr = eventToHashMap(repositoryEvent);
	    		
	    		values.add(repositoryEventStr);
			}
			
			response.put(key, values);
		}
		
		return response;
	}
}

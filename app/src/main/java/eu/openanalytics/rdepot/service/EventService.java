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
package eu.openanalytics.rdepot.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.exception.EventNotFound;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.repository.EventRepository;

@Service
@Transactional(readOnly = true)
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class EventService {
	
	@Resource
	private EventRepository eventRepository;
	
	public Event findById(int id) {
		return eventRepository.getOne(id);
	}
	
	public Event findByValue(String value) {
		return eventRepository.findByValue(value);
	}
	
	public Event getDeleteEvent() throws EventNotFound {
		Event event = findByValue("delete");
		if(event == null)
			throw new EventNotFound();
		return event;
	}
	
	public Event getCreateEvent() throws EventNotFound {
		Event event = findByValue("create");
		if(event == null)
			throw new EventNotFound();
		return event;
	}
	
	public Event getUpdateEvent() throws EventNotFound {
		Event event = findByValue("update");
		if(event == null)
			throw new EventNotFound();
		return event;
	}

	public List<Event> findAll() {
		return eventRepository.findAll();
	}

}

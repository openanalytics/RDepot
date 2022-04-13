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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.model.UserEvent;
import eu.openanalytics.rdepot.repository.UserEventRepository;

@Service
@Transactional(readOnly = true)
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class UserEventService
{	
	@Resource
	private UserEventRepository userEventRepository;
	
	@Resource
	private UserService userService;

	@Transactional(readOnly = false)
	public UserEvent create(UserEvent userEvent) 
	{
		UserEvent createdUserEvent = userEvent;
		return userEventRepository.save(createdUserEvent);
	}
	
	@Transactional(readOnly = false)
	public void delete(int id)
	{
		UserEvent event = userEventRepository.getOne(id);
		if(event != null)
			userEventRepository.delete(event);
	}
	
	@Transactional(readOnly = false)
	public List<UserEvent> create(Event event, User creator, User user) 
	{
		User oldUser = userService.findById(user.getId());
		User newUser = user;
		
		List<UserEvent> events = new ArrayList<UserEvent>();
		
		if(Objects.equals(event.getValue(), "create"))
			events.add(new UserEvent(0, new Date(), creator, user, event, "created", "", "", new Date()));
		else if(Objects.equals(event.getValue(), "update"))
		{
			if(oldUser.getRole().getId() != newUser.getRole().getId())
				events.add(new UserEvent(0, new Date(), creator, user, event, "role", "" + oldUser.getRole().getId(), "" + newUser.getRole().getId(), new Date()));
			if(!Objects.equals(oldUser.getName(), newUser.getName()))
				events.add(new UserEvent(0, new Date(), creator, user, event, "name", oldUser.getName(), newUser.getName(), new Date()));
			if(!Objects.equals(oldUser.getEmail(), newUser.getEmail()))
				events.add(new UserEvent(0, new Date(), creator, user, event, "email", oldUser.getEmail(), newUser.getEmail(), new Date()));
			if(!Objects.equals(oldUser.getLogin(), newUser.getLogin()))
				events.add(new UserEvent(0, new Date(), creator, user, event, "login", oldUser.getLogin(), newUser.getLogin(), new Date()));
			if(oldUser.isActive() != newUser.isActive())
				events.add(new UserEvent(0, new Date(), creator, user, event, "active", "" + oldUser.isActive(), "" + newUser.isActive(), new Date()));
			if(oldUser.getLastLoggedInOn() != newUser.getLastLoggedInOn())
				events.add(new UserEvent(0, new Date(), creator, user, event, "last logged in", "" + oldUser.getLastLoggedInOn(), "" + newUser.getLastLoggedInOn(), new Date()));
		}
		else if(Objects.equals(event.getValue(), "delete"))
			events.add(new UserEvent(0, new Date(), creator, user, event, "deleted", "", new Date().toString(), new Date()));
		
		for(UserEvent rEvent : events)
		{
			rEvent = userEventRepository.save(rEvent);
		}
		
		return events;
	}
	
	public UserEvent findById(int id) 
	{
		return userEventRepository.getOne(id);
	}
	
	public List<UserEvent> findAll() 
	{
		return userEventRepository.findAll();
	}

	public List<UserEvent> findByUser(User user) 
	{
		return userEventRepository.findByUserNoLogs(user);
	}
	
	public List<UserEvent> findByChangedBy(User changedBy) 
	{
		return userEventRepository.findByChangedBy(changedBy);
	}

	public List<UserEvent> findByDate(Date date) 
	{
		return userEventRepository.findByDate(date);
	}

	public List<Date> getUniqueDatesByUser(User user) 
	{
		List<UserEvent> events = findByUser(user);
		List<Date> dates = new ArrayList<Date>();
		for(UserEvent event : events)
		{
			if(!dates.contains(event.getDate()))
				dates.add(event.getDate());
		}
		return dates;
	}
	
	public UserEvent getCreatedOn(User user) 
	{
		return userEventRepository.findByUserAndEvent_Value(user, "create");
	}

	public List<UserEvent> findByDateAndUser(Date date, User user) 
	{
		return userEventRepository.findByDateAndUserNoLogs(date, user);
	}

	public UserEvent getLastLoggedInOn(User user) 
	{
		return userEventRepository.findLastByUserAndChangedVariable(user, "last logged in");
	}
}

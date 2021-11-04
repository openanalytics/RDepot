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
package eu.openanalytics.rdepot.test.unit;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.model.UserEvent;
import eu.openanalytics.rdepot.repository.UserEventRepository;
import eu.openanalytics.rdepot.service.UserEventService;
import eu.openanalytics.rdepot.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class UserEventServiceTest {
	
	@InjectMocks
	UserEventService userEventService;
	
	@Mock
	UserService userService;
	
	@Mock
	UserEventRepository userEventRepository;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testCreateUser() {
		Event event = new Event(1, "create");
		User user = new User(123, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);
		User creator = new User(2, new Role(1, 2, "admin", "Administrator"), "tesla", "tesla@example.org", "tesla", true, false);
		
		Mockito.when(userService.findById(123)).thenReturn(null);
		UserEvent expectedEvent = new UserEvent(0, new Date(), creator, user, event, "created", "", "", new Date());
		
		Mockito.when(userEventRepository.save(Mockito.any())).thenAnswer(new Answer<UserEvent>() {
			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (UserEvent) args[0];
			}
		});
		
		List<UserEvent> events = userEventService.create(event, creator, user);
		UserEvent createEvent = events.get(0);
		
		assertEquals(expectedEvent.getChangedBy().getId(), createEvent.getChangedBy().getId());
		assertEquals(expectedEvent.getChangedVariable(), createEvent.getChangedVariable());
		assertEquals(expectedEvent.getEvent().getValue(), createEvent.getEvent().getValue());
		assertEquals(expectedEvent.getId(), createEvent.getId());
		assertEquals(expectedEvent.getUser().getId(), createEvent.getUser().getId());
		assertEquals(expectedEvent.getValueAfter(), createEvent.getValueAfter());
		assertEquals(expectedEvent.getValueBefore(), createEvent.getValueBefore());
	}
	
	@Test
	public void testUpdateUser() {
		Event event = new Event(1, "update");	
		User creator = new User(2, new Role(1, 2, "admin", "Administrator"), "tesla", "tesla@example.org", "tesla", true, false);
		
		User oldUser = new User(123, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);
		Calendar myCalendar = new GregorianCalendar(2018, 7, 2);
		oldUser.setLastLoggedInOn(myCalendar.getTime());
		
		User newUser = new User(123, new Role(2, 3, "admin", "Administrator"), "newton", "newton@example.org", "newton", false, false);
		myCalendar.set(2018, 8, 31);
		newUser.setLastLoggedInOn(myCalendar.getTime());
		
		Mockito.when(userService.findById(123)).thenReturn(oldUser);
		Mockito.when(userEventRepository.save(Mockito.any())).thenAnswer(new Answer<UserEvent>() {
			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (UserEvent) args[0];
			}
		});
		
		UserEvent roleEvent = new UserEvent(0, new Date(), creator, newUser, event, "role", "" + oldUser.getRole().getId(), "" + newUser.getRole().getId(), new Date());
		UserEvent nameEvent = new UserEvent(0, new Date(), creator, newUser, event, "name", oldUser.getName(), newUser.getName(), new Date());
		UserEvent emailEvent = new UserEvent(0, new Date(), creator, newUser, event, "email", oldUser.getEmail(), newUser.getEmail(), new Date());
		UserEvent loginEvent = new UserEvent(0, new Date(), creator, newUser, event, "login", oldUser.getLogin(), newUser.getLogin(), new Date());
		UserEvent activeEvent = new UserEvent(0, new Date(), creator, newUser, event, "active", "" + oldUser.isActive(), "" + newUser.isActive(), new Date());
		UserEvent lastLoggedInOnEvent = new UserEvent(0, new Date(), creator, newUser, event, "last logged in", "" + oldUser.getLastLoggedInOn(), "" + newUser.getLastLoggedInOn(), new Date());
		List<UserEvent> expectedEvents = new ArrayList<>();
		expectedEvents.add(roleEvent);
		expectedEvents.add(nameEvent);
		expectedEvents.add(emailEvent);
		expectedEvents.add(loginEvent);
		expectedEvents.add(activeEvent);
		expectedEvents.add(lastLoggedInOnEvent);
		
		List<UserEvent> events = userEventService.create(event, creator, newUser);
		
		for(int i = 0; i < events.size(); i++) {
			UserEvent updateEvent = events.get(i);
			UserEvent expectedEvent = expectedEvents.get(i);
			
			assertEquals(expectedEvent.getChangedBy().getId(), updateEvent.getChangedBy().getId());
			assertEquals(expectedEvent.getChangedVariable(), updateEvent.getChangedVariable());
			assertEquals(expectedEvent.getEvent().getValue(), updateEvent.getEvent().getValue());
			assertEquals(expectedEvent.getId(), updateEvent.getId());
			assertEquals(expectedEvent.getUser().getId(), updateEvent.getUser().getId());
			assertEquals(expectedEvent.getValueAfter(), updateEvent.getValueAfter());
			assertEquals(expectedEvent.getValueBefore(), updateEvent.getValueBefore());
		}
	}
	
	@Test
	public void testDeleteUser() {
		Event event = new Event(1, "delete");
		User user = new User(123, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);
		User creator = new User(2, new Role(1, 2, "admin", "Administrator"), "tesla", "tesla@example.org", "tesla", true, false);
		
		Mockito.when(userService.findById(123)).thenReturn(null);
		UserEvent expectedEvent = new UserEvent(0, new Date(), creator, user, event, "deleted", "", new Date().toString(), new Date());
		
		Mockito.when(userEventRepository.save(Mockito.any())).thenAnswer(new Answer<UserEvent>() {
			@Override
			public UserEvent answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (UserEvent) args[0];
			}
		});
		
		List<UserEvent> events = userEventService.create(event, creator, user);
		UserEvent deleteEvent = events.get(0);
		
		assertEquals(expectedEvent.getChangedBy().getId(), deleteEvent.getChangedBy().getId());
		assertEquals(expectedEvent.getChangedVariable(), deleteEvent.getChangedVariable());
		assertEquals(expectedEvent.getEvent().getValue(), deleteEvent.getEvent().getValue());
		assertEquals(expectedEvent.getId(), deleteEvent.getId());
		assertEquals(expectedEvent.getUser().getId(), deleteEvent.getUser().getId());
		assertEquals(expectedEvent.getValueAfter(), deleteEvent.getValueAfter());
		assertEquals(expectedEvent.getValueBefore(), deleteEvent.getValueBefore());
	}
}

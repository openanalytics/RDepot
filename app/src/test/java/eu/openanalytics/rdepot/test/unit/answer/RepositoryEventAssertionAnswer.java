/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
package eu.openanalytics.rdepot.test.unit.answer;

import static org.junit.Assert.assertEquals;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryEvent;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.time.DateProvider;

public class RepositoryEventAssertionAnswer implements Answer<RepositoryEvent> {

	User user;
	Repository repository;
	Event baseEvent;
	String changedVariable;
	String valueBefore;
	String valueAfter;
	
	public RepositoryEventAssertionAnswer(User user, Repository repository, Event baseEvent, 
			String changedVariable, String valueBefore, String valueAfter) {
		super();
		this.user = user;
		this.repository = repository;
		this.baseEvent = baseEvent;
		this.changedVariable = changedVariable;
		this.valueBefore = valueBefore;
		this.valueAfter = valueAfter;
	}
	
	@Override
	public RepositoryEvent answer(InvocationOnMock invocation) throws Throwable {
		RepositoryEvent createdEvent = invocation.getArgument(0);
		
		assertRepositoryEvents(createdEvent, user, repository, baseEvent,
				changedVariable, valueBefore, valueAfter);
		
		return createdEvent;
	}
	
	public static void assertRepositoryEvents(RepositoryEvent createdEvent, User user, Repository repository,
			Event baseEvent, String changedVariable, String valueBefore, String valueAfter) {		
		assertEquals("Event index should be 0.", 0, createdEvent.getId());
		assertEquals("Date object should contain current date", DateProvider.now(), createdEvent.getDate());
		assertEquals("Updater is not correct", user, createdEvent.getChangedBy());
		assertEquals("Repository in the event is not correct", repository, createdEvent.getRepository());
		assertEquals("Base event is not correct", baseEvent, createdEvent.getEvent());
		assertEquals("Changed variable is not correct", changedVariable, createdEvent.getChangedVariable());
		assertEquals("Event value before is not correct", valueBefore, createdEvent.getValueBefore());
		assertEquals("Event value after is not correct", valueAfter, createdEvent.getValueAfter());
		assertEquals("Date object should contain current time", DateProvider.now(), createdEvent.getTime());
	}

}

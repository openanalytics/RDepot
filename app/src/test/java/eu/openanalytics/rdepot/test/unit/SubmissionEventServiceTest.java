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
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.Role;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.SubmissionEvent;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.SubmissionEventRepository;
import eu.openanalytics.rdepot.service.EventService;
import eu.openanalytics.rdepot.service.RepositoryEventService;
import eu.openanalytics.rdepot.service.SubmissionEventService;
import eu.openanalytics.rdepot.service.SubmissionService;

public class SubmissionEventServiceTest {
	
	@InjectMocks
	SubmissionEventService submissionEventService;
	
	@Mock
	SubmissionEventRepository submissionEventRepository;
	
	@Mock
	SubmissionService submissionService;
	
	@Mock
	EventService eventService;
	
	@Mock
	RepositoryEventService repositoryEventService;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testCreateSubmission() {
		Event event = new Event(1, "create");
		User creator = new User(1, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);
		Mockito.when(submissionService.findById(Mockito.anyInt())).thenReturn(new Submission());
		
		User user = new User(1, new Role(2, 1, "packagemaintainer", "Package Maintainer"), "newton", "newton@example.org", "newton", true, false);
		Repository repository = new Repository(123, "http://example.org/repo", "newrepo", "127.0.0.1", true, false);
		Package packageBag = new Package(1, repository, user, "example", "example package", "einstein", "some license", "some source", "example", "md5", true, false);
		Submission submission = new Submission(123, user, packageBag, true, false, "changesssss");
		
		Event updateEvent = new Event(1, "update");
		Mockito.when(eventService.findByValue("update")).thenReturn(updateEvent);
		Mockito.when(repositoryEventService.create(Mockito.any())).thenReturn(null);
		Mockito.when(submissionEventRepository.save(Mockito.any())).thenAnswer(new Answer<SubmissionEvent>() {
			@Override
			public SubmissionEvent answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (SubmissionEvent) args[0];
			}
		});
		SubmissionEvent expectedEvent = new SubmissionEvent(0, new Date(), creator, submission, event, "created", "", "", new Date());
		
		List<SubmissionEvent> events = submissionEventService.create(event, creator, submission);
		SubmissionEvent createEvent = events.get(0);
		
		assertEquals(expectedEvent.getChangedBy().getName(), createEvent.getChangedBy().getName());
		assertEquals(expectedEvent.getChangedVariable(), createEvent.getChangedVariable());
		assertEquals(expectedEvent.getEvent().getValue(), createEvent.getEvent().getValue());
		assertEquals(expectedEvent.getId(), createEvent.getId());
		assertEquals(expectedEvent.getSubmission().getId(), createEvent.getSubmission().getId());
		assertEquals(expectedEvent.getValueAfter(), createEvent.getValueAfter());
		assertEquals(expectedEvent.getValueBefore(), createEvent.getValueBefore());
	}
	
	@Test
	public void testUpdateSubmission() {
		Event event = new Event(1, "update");
		User creator = new User(1, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);

		Repository repository = new Repository(123, "http://example.org/repo", "newrepo", "127.0.0.1", true, false);

		User oldUser = new User(123, new Role(2, 1, "packagemaintainer", "Package Maintainer"), "newton", "newton@example.org", "newton", true, false);
		Package oldPackage = new Package(123, repository, oldUser, "example", "example package", "einstein", "some license", "some source", "example", "md5", true, false);
		Submission oldSubmission = new Submission(123, oldUser, oldPackage, true, false, "changesssss");
		
		User newUser = new User(234, new Role(2, 1, "packagemaintainer", "Package Maintainer"), "kopernik", "kopernik@example.org", "kopernik", true, false);
		Package newPackage = new Package(234, repository, newUser, "example2", "example package 2", "kopernik", "some other license", "some other source", "example2", "md544", true, false);
		Submission newSubmission = new Submission(123, newUser, newPackage, true, false, "changesssss2222");
		
		Mockito.when(submissionService.findById(Mockito.anyInt())).thenReturn(oldSubmission);
		
		SubmissionEvent userEvent = new SubmissionEvent(0, new Date(), creator, newSubmission, event, "user", "" + oldSubmission.getUser().getId(), "" + newSubmission.getUser().getId(), new Date());
		SubmissionEvent packageEvent = new SubmissionEvent(0, new Date(), creator, newSubmission, event, "package", "" + oldSubmission.getPackage().getId(), "" + newSubmission.getPackage().getId(), new Date());
		SubmissionEvent changesEvent = new SubmissionEvent(0, new Date(), creator, newSubmission, event, "changes", oldSubmission.getChanges(), newSubmission.getChanges(), new Date());
		SubmissionEvent activeEvent = new SubmissionEvent(0, new Date(), creator, newSubmission, event, "accepted", "" + oldSubmission.isAccepted(), "" + newSubmission.isAccepted(), new Date());
		List<SubmissionEvent> expectedEvents = new ArrayList<>();
		expectedEvents.add(userEvent);
		expectedEvents.add(packageEvent);
		expectedEvents.add(changesEvent);
		expectedEvents.add(activeEvent);
		
		Mockito.when(submissionEventRepository.save(Mockito.any())).thenAnswer(new Answer<SubmissionEvent>() {
			@Override
			public SubmissionEvent answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (SubmissionEvent) args[0];
			}
		});
		
		List<SubmissionEvent> events = submissionEventService.create(event, creator, newSubmission);
		
		for(int i = 0; i < events.size(); i++) {
			SubmissionEvent expectedEvent = expectedEvents.get(i);
			SubmissionEvent updateEvent = events.get(i);
			assertEquals(expectedEvent.getChangedBy().getName(), updateEvent.getChangedBy().getName());
			assertEquals(expectedEvent.getChangedVariable(), updateEvent.getChangedVariable());
			assertEquals(expectedEvent.getEvent().getValue(), updateEvent.getEvent().getValue());
			assertEquals(expectedEvent.getId(), updateEvent.getId());
			assertEquals(expectedEvent.getSubmission().getId(), updateEvent.getSubmission().getId());
			assertEquals(expectedEvent.getValueAfter(), updateEvent.getValueAfter());
			assertEquals(expectedEvent.getValueBefore(), updateEvent.getValueBefore());
		}
	}
	
	@Test
	public void testDeleteSubmission() {
		Event event = new Event(1, "delete");
		User creator = new User(1, new Role(1, 2, "repositorymaintainer", "Repository Maintainer"), "einstein", "einstein@example.org", "einstein", true, false);
		Mockito.when(submissionService.findById(Mockito.anyInt())).thenReturn(new Submission());
		
		User user = new User(1, new Role(2, 1, "packagemaintainer", "Package Maintainer"), "newton", "newton@example.org", "newton", true, false);
		Repository repository = new Repository(123, "http://example.org/repo", "newrepo", "127.0.0.1", true, false);
		Package packageBag = new Package(1, repository, user, "example", "example package", "einstein", "some license", "some source", "example", "md5", true, false);
		Submission submission = new Submission(123, user, packageBag, true, false, "changesssss");
		
		Mockito.when(submissionEventRepository.save(Mockito.any())).thenAnswer(new Answer<SubmissionEvent>() {
			@Override
			public SubmissionEvent answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				return (SubmissionEvent) args[0];
			}
		});
		SubmissionEvent expectedEvent = new SubmissionEvent(0, new Date(), creator, submission, event, "deleted", "", "", new Date());
		
		List<SubmissionEvent> events = submissionEventService.create(event, creator, submission);
		SubmissionEvent deleteEvent = events.get(0);
		
		assertEquals(expectedEvent.getChangedBy().getName(), deleteEvent.getChangedBy().getName());
		assertEquals(expectedEvent.getChangedVariable(), deleteEvent.getChangedVariable());
		assertEquals(expectedEvent.getEvent().getValue(), deleteEvent.getEvent().getValue());
		assertEquals(expectedEvent.getId(), deleteEvent.getId());
		assertEquals(expectedEvent.getSubmission().getId(), deleteEvent.getSubmission().getId());
		assertEquals(expectedEvent.getValueAfter(), deleteEvent.getValueAfter());
		assertEquals(expectedEvent.getValueBefore(), deleteEvent.getValueBefore());
	}
}

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
import org.springframework.transaction.support.TransactionSynchronizationManager;

import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.RepositoryEvent;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.SubmissionEvent;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.SubmissionEventRepository;

@Service
@Transactional(readOnly = true)
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class SubmissionEventService
{	
	@Resource
	private SubmissionEventRepository submissionEventRepository;
	
	@Resource
	private SubmissionService submissionService;
	
	@Resource
	private EventService eventService;
	
	@Resource
	private RepositoryEventService repositoryEventService;

	@Transactional(readOnly = false)
	public SubmissionEvent create(SubmissionEvent submissionEvent) 
	{
		SubmissionEvent createdSubmissionEvent = submissionEvent;
		return submissionEventRepository.save(createdSubmissionEvent);
	}
	
	@Transactional(readOnly = false)
	public List<SubmissionEvent> create(Event event, User user, Submission submission) 
	{
		Submission oldSubmission = submissionService.findById(submission.getId());
		Submission newSubmission = submission;
		
		List<SubmissionEvent> events = new ArrayList<SubmissionEvent>();
		
		if(Objects.equals(event.getValue(), "create"))
		{
			events.add(new SubmissionEvent(0, new Date(), user, submission, event, "created", "", "", new Date()));
			Event updateEvent = eventService.findByValue("update");
			repositoryEventService.create(new RepositoryEvent(0, new Date(), user, submission.getPackage().getRepository(), updateEvent, "submitted", "", "" + submission.getId(), new Date()));
		}
		else if(Objects.equals(event.getValue(), "update"))
		{
			if(oldSubmission.getUser().getId() != newSubmission.getUser().getId())
				events.add(new SubmissionEvent(0, new Date(), user, submission, event, "user", "" + oldSubmission.getUser().getId(), "" + newSubmission.getUser().getId(), new Date()));
			if(oldSubmission.getPackage().getId() != newSubmission.getPackage().getId())
				events.add(new SubmissionEvent(0, new Date(), user, submission, event, "package", "" + oldSubmission.getPackage().getId(), "" + newSubmission.getPackage().getId(), new Date()));
			if(!oldSubmission.getChanges().equals(newSubmission.getChanges()))
				events.add(new SubmissionEvent(0, new Date(), user, submission, event, "changes", oldSubmission.getChanges(), newSubmission.getChanges(), new Date()));
			if(oldSubmission.isAccepted() != newSubmission.isAccepted())
				events.add(new SubmissionEvent(0, new Date(), user, submission, event, "accepted", "" + oldSubmission.isAccepted(), "" + newSubmission.isAccepted(), new Date()));
		}
		else if(Objects.equals(event.getValue(), "delete"))
		{
			events.add(new SubmissionEvent(0, new Date(), user, submission, event, "deleted", "", "", new Date()));
		}

		for(SubmissionEvent sEvent : events)
		{
			sEvent = submissionEventRepository.save(sEvent);
		}
		
		return events;
	}
	
	public SubmissionEvent findById(int id) 
	{
		return submissionEventRepository.getOne(id);
	}
	
	public List<SubmissionEvent> findAll() 
	{
		return submissionEventRepository.findAll();
	}

	public List<SubmissionEvent> findBySubmission(Submission submission) 
	{
		return submissionEventRepository.findBySubmission(submission);
	}
	
	public List<SubmissionEvent> findByChangedBy(User changedBy) 
	{
		return submissionEventRepository.findByChangedBy(changedBy);
	}

	public List<SubmissionEvent> findByDate(Date date) 
	{
		return submissionEventRepository.findByDate(date);
	}
	
	@Transactional(readOnly = false)
	public void delete(int id)
	{
		SubmissionEvent event = submissionEventRepository.getOne(id);
		if(event != null)
			submissionEventRepository.delete(event);
	}
}

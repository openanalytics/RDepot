/**
 * RDepot
 *
 * Copyright (C) 2012-2017 Open Analytics NV
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
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.exception.AdminNotFound;
import eu.openanalytics.rdepot.exception.EventNotFound;
import eu.openanalytics.rdepot.exception.SubmissionDeleteException;
import eu.openanalytics.rdepot.exception.SubmissionEditException;
import eu.openanalytics.rdepot.exception.SubmissionNotFound;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryEvent;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.SubmissionEvent;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.SubmissionRepository;

@Service
@Transactional(readOnly = true)
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class SubmissionService
{	
	@Resource
	private SubmissionRepository submissionRepository;
	
	@Resource
	private UserService userService;
	
	@Resource
	private PackageService packageService;
	
	@Resource
	private RoleService roleService;
	
	@Resource
	private EventService eventService;
	
	@Resource
	private SubmissionEventService submissionEventService;
	
	@Resource
	private RepositoryEventService repositoryEventService;

	@Transactional(readOnly = false)
	public Submission create(Submission submission, User creator) 
	{
		Submission createdSubmission = submission;
		Event createEvent = eventService.findByValue("create");
		createdSubmission = submissionRepository.save(createdSubmission);
		submissionEventService.create(createEvent, creator, createdSubmission);
		if(createdSubmission.isAccepted())
		{
			Event updateEvent = eventService.findByValue("update");
			repositoryEventService.create(new RepositoryEvent(0, new Date(), creator, createdSubmission.getPackage().getRepository(), updateEvent, "added", "", "" + createdSubmission.getPackage().getId(), new Date()));			
		}
		return createdSubmission;
	}
	
	public Submission findById(int id) 
	{
		return submissionRepository.findByIdAndDeleted(id, false);
	}
	
	public Submission findByIdEvenDeleted(int id) 
	{
		return submissionRepository.findOne(id);
	}
	
	public Submission findByIdAndDeleted(int id, boolean deleted) 
	{
		return submissionRepository.findByIdAndDeleted(id, deleted);
	}
	
	@Transactional(readOnly = false, rollbackFor={SubmissionDeleteException.class})
	public Submission delete(int id, User deleter) throws SubmissionDeleteException
	{
		Submission deletedSubmission = submissionRepository.findByIdAndDeleted(id, false);
		Event deleteEvent = eventService.findByValue("delete");
		try
		{
			if (deletedSubmission == null)
				throw new SubmissionNotFound();
			if (deleteEvent == null)
				throw new EventNotFound();
			deletedSubmission.setDeleted(true);		
			submissionEventService.create(deleteEvent, deleter, deletedSubmission);
			return deletedSubmission;
		}
		catch (SubmissionNotFound | EventNotFound e) 
		{
			throw new SubmissionDeleteException(e.getMessage());
		}
	}
	
	@Transactional(readOnly=false, rollbackFor={SubmissionDeleteException.class})
	public Submission shiftDelete(int id) throws SubmissionDeleteException
	{
		Submission deletedSubmission = submissionRepository.findByIdAndDeleted(id, true);
		try
		{
			if (deletedSubmission == null)
				throw new SubmissionNotFound();
			for(SubmissionEvent event : deletedSubmission.getSubmissionEvents())
				submissionEventService.delete(event.getId());
			submissionRepository.delete(deletedSubmission);	
			return deletedSubmission;
		}
		catch (SubmissionNotFound e) 
		{
			throw new SubmissionDeleteException(e.getMessage());
		}
	}

	public List<Submission> findAll() 
	{
		return submissionRepository.findByDeleted(false, new Sort(new Order(Direction.DESC, "id")));
	}
	
	public List<Submission> findByDeleted(boolean deleted) 
	{
		return submissionRepository.findByDeleted(deleted, new Sort(new Order(Direction.DESC, "id")));
	}

	@Transactional(readOnly=false, rollbackFor=SubmissionNotFound.class)
	public Submission update(Submission submission, User updater) throws SubmissionEditException
	{
		Submission updatedSubmission = submissionRepository.findByIdAndDeleted(submission.getId(), false);
		List<SubmissionEvent> events = new ArrayList<SubmissionEvent>();
		List<RepositoryEvent> repositoryEvents = new ArrayList<RepositoryEvent>();
		Event updateEvent = eventService.findByValue("update");
		
		try
		{
			if (updatedSubmission == null)
				throw new SubmissionNotFound();
			if(updateEvent == null)
				throw new EventNotFound();
			
			if(updatedSubmission.getUser().getId() != submission.getUser().getId())
			{
				events.add(new SubmissionEvent(0, new Date(), updater, submission, updateEvent, "user", "" + updatedSubmission.getUser().getId(), "" + submission.getUser().getId(), new Date()));
				updatedSubmission.setUser(submission.getUser());
			}
			if(updatedSubmission.getPackage().getId() != submission.getPackage().getId())
			{
				events.add(new SubmissionEvent(0, new Date(), updater, submission, updateEvent, "package", "" + updatedSubmission.getPackage().getId(), "" + submission.getPackage().getId(), new Date()));
				updatedSubmission.setPackage(submission.getPackage());
			}
			if(!Objects.equals(updatedSubmission.getChanges(), submission.getChanges()))
			{
				events.add(new SubmissionEvent(0, new Date(), updater, submission, updateEvent, "changes", updatedSubmission.getChanges(), submission.getChanges(), new Date()));
				updatedSubmission.setChanges(submission.getChanges());
			}
			if(updatedSubmission.isAccepted() != submission.isAccepted())
			{
				events.add(new SubmissionEvent(0, new Date(), updater, submission, updateEvent, "accepted", "" + updatedSubmission.isAccepted(), "" + submission.isAccepted(), new Date()));
				updatedSubmission.setAccepted(submission.isAccepted());
				repositoryEvents.add(new RepositoryEvent(0, new Date(), updater, updatedSubmission.getPackage().getRepository(), updateEvent, "added", "", "" + updatedSubmission.getPackage().getId(), new Date()));
			}

			for(SubmissionEvent rEvent : events)
			{
				rEvent = submissionEventService.create(rEvent);
			}
			
			for(RepositoryEvent rEvent : repositoryEvents)
			{
				rEvent = repositoryEventService.create(rEvent);
			}
			
			return updatedSubmission;
		}
		catch (SubmissionNotFound | EventNotFound e)
		{
			throw new SubmissionEditException(e.getMessage());
		}
			
	}
	
	public Submission chooseBestSubmitter(Submission submission) throws SubmissionEditException
	{
		try
		{
			submission.setUser(userService.findFirstAdmin());
			return submission;
		}
		catch(AdminNotFound e)
		{
			throw new SubmissionEditException(e.getMessage());
		}
	}
	
	public List<Submission> findAllFor(User user)
	{
		List<Submission> submissions = new ArrayList<Submission>();
		switch(user.getRole().getName())
		{
			case "admin":
				submissions.addAll(findAll());
				break;
			case "repositorymaintainer":
				user = userService.findById(user.getId());
				for(RepositoryMaintainer repositoryMaintainer : user.getRepositoryMaintainers())
				{
					List<Submission> ss = findByRepository(repositoryMaintainer.getRepository());
					for(Submission s : ss)
					{
						if(s.getUser().getId() != user.getId())
							submissions.add(s);
					}
				}
				submissions.addAll(findBySubmitter(user));
				break;
			case "packagemaintainer":
				user = userService.findById(user.getId());
				List<Package> packages = new ArrayList<Package>();
				for(PackageMaintainer packageMaintainer : user.getPackageMaintainers())
				{
					packages.addAll(packageService.findByNameAndRepository(
							packageMaintainer.getPackage(), packageMaintainer.getRepository()));
				}
				for(Package p : packages)
				{
					Submission submission = findByPackage(p);
					if(submission.getUser().getId() != user.getId())
						submissions.add(submission);
				}
				submissions.addAll(findBySubmitter(user));
				break;
			case "user":
				submissions.addAll(findBySubmitter(user));
				break;
		}
		return submissions;
	}
	
	public List<Submission> findBySubmitter(User submitter)
	{
		return submissionRepository.findByUserAndDeleted(submitter, false, new Sort(new Order(Direction.DESC, "id")));
	}

	public List<Submission> findByRepository(Repository repository) 
	{
		return submissionRepository.findByDeletedAndPackage_Repository(false, repository);
	}

	public Submission findByPackage(Package packageBag) 
	{
		return submissionRepository.findByPackageAndDeleted(packageBag, false);
	}
}

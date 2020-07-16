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
import java.util.TreeMap;

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.comparator.DateComparator;
import eu.openanalytics.rdepot.comparator.RepositoryEventComparator;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryEvent;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.RepositoryEventRepository;

@Service
@Transactional(readOnly = true)
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class RepositoryEventService {	
	@Resource
	private RepositoryEventRepository repositoryEventRepository;
	
	@Resource
	private PackageService packageService;
	
	@Resource
	private SubmissionService submissionService;
	
	@Resource
	private RepositoryService repositoryService;

	@Transactional(readOnly = false)
	public RepositoryEvent create(RepositoryEvent repositoryEvent) {
		RepositoryEvent createdRepositoryEvent = repositoryEvent;
		return repositoryEventRepository.save(createdRepositoryEvent);
	}
	
	@Transactional(readOnly = false)
	public List<RepositoryEvent> create(Event event, User user, Repository repository) {
		Repository oldRepository = repositoryService.findById(repository.getId());
		Repository newRepository = repository;
		
		List<RepositoryEvent> events = new ArrayList<RepositoryEvent>();
		
		if(Objects.equals(event.getValue(), "create"))
			events.add(new RepositoryEvent(0, new Date(), user, repository, event, "created", "", "", new Date()));
		else if(Objects.equals(event.getValue(), "update")) {
			if(oldRepository.getVersion() != newRepository.getVersion())
				events.add(new RepositoryEvent(0, new Date(), user, repository, event, "version", "" + oldRepository.getVersion(), "" + newRepository.getVersion(), new Date()));
			if(!Objects.equals(oldRepository.getPublicationUri(), newRepository.getPublicationUri()))
				events.add(new RepositoryEvent(0, new Date(), user, repository, event, "publication URI", oldRepository.getPublicationUri(), newRepository.getPublicationUri(), new Date()));
			if(!Objects.equals(oldRepository.getServerAddress(), newRepository.getServerAddress()))
				events.add(new RepositoryEvent(0, new Date(), user, repository, event, "server address", oldRepository.getServerAddress(), newRepository.getServerAddress(), new Date()));
			if(!Objects.equals(oldRepository.getName(), newRepository.getName()))
				events.add(new RepositoryEvent(0, new Date(), user, repository, event, "name", oldRepository.getName(), newRepository.getName(), new Date()));

		}
		else if(Objects.equals(event.getValue(), "delete"))
			events.add(new RepositoryEvent(0, new Date(), user, repository, event, "deleted", "", "", new Date()));

		
		for(RepositoryEvent rEvent : events) {
			rEvent = repositoryEventRepository.save(rEvent);
		}
		
		return events;
	}
	
	public RepositoryEvent getCreatedOn(Repository repository) {
		return repositoryEventRepository.findByRepositoryAndEvent_Value(repository, "create");
	}
	
	public RepositoryEvent findById(int id) {
		return repositoryEventRepository.getOne(id);
	}
	
	public List<RepositoryEvent> findAll() {
		return repositoryEventRepository.findAll();
	}

	public List<RepositoryEvent> findByRepository(Repository repository) {
		return repositoryEventRepository.findByRepository(repository);
	}
	
	public List<RepositoryEvent> findByChangedBy(User changedBy) {
		return repositoryEventRepository.findByChangedBy(changedBy);
	}

	public List<RepositoryEvent> findByDate(Date date) {
		return repositoryEventRepository.findByDate(date);
	}
	
	public List<RepositoryEvent> findByDateAndRepository(Date date, Repository repository) {
		return repositoryEventRepository.findByDateAndRepository(date, repository);
	}
	
	@Transactional(readOnly = false)
	public void delete(int id) {
		RepositoryEvent event = repositoryEventRepository.getOne(id);
		if(event != null)
			repositoryEventRepository.delete(event);
	}

	public List<Date> getUniqueDates() {
		List<RepositoryEvent> events = findAll();
		List<Date> dates = new ArrayList<Date>();
		for(RepositoryEvent event : events) {
			if(!dates.contains(event.getDate()))
				dates.add(event.getDate());
		}
		return dates;
	}
	
	public List<Date> getUniqueDatesByRepository(Repository repository) {
		List<RepositoryEvent> events = findByRepository(repository);
		List<Date> dates = new ArrayList<Date>();
		for(RepositoryEvent event : events)
		{
			if(!dates.contains(event.getDate()))
				dates.add(event.getDate());
		}
		return dates;
	}

	public List<Date> getUniqueDatesByPackage(Package packageBag) {
		List<RepositoryEvent> events = findByPackage(packageBag);
		List<Date> dates = new ArrayList<Date>();
		for(RepositoryEvent event : events)
		{
			if(!dates.contains(event.getDate()))
				dates.add(event.getDate());
		}
		return dates;
	}

	private List<RepositoryEvent> findByPackage(Package packageBag) {
		return repositoryEventRepository.findByValueAfterAndChangedVariableNot(packageBag.getId() + "", "version");
	}

	public List<RepositoryEvent> findByDateAndPackage(Date date, Package packageBag) {
		return repositoryEventRepository.findByDateAndValueAfterAndChangedVariableNot(date, packageBag.getId() + "", "version");
	}
	
	public TreeMap<Date, ArrayList<RepositoryEvent>> findAllByUser(User requester) {
		List<Repository> repositories = repositoryService.findMaintainedBy(requester, true);
		List<Date> dates = getUniqueDates();
		TreeMap<Date, ArrayList<RepositoryEvent>> content = new TreeMap<Date, ArrayList<RepositoryEvent>>(new DateComparator());

		for(Date date : dates) {
			ArrayList<RepositoryEvent> sortedEvents = new ArrayList<RepositoryEvent>();
			
			for(Repository repository : repositories) {
				List<RepositoryEvent> events = findByDateAndRepository(date, repository);
				
				makeEventsHumanReadable(events);
				
				sortedEvents.addAll(events);
			}
			
			sortedEvents.sort(new RepositoryEventComparator());
			content.put(date, sortedEvents);
		}
		
		return content;
	}
	
	public TreeMap<Date, ArrayList<RepositoryEvent>> findLatestByUser(User requester, Date lastRefreshedDate, int lastEventId) {
		TreeMap<Date, ArrayList<RepositoryEvent>> content = findAllByUser(requester);
		
		content.entrySet().removeIf(entry -> entry.getKey().compareTo(lastRefreshedDate) < 0);
		ArrayList<RepositoryEvent> oldest = content.get(lastRefreshedDate);
		
		if(oldest != null)
			oldest.removeIf(event -> event.getDate().compareTo(lastRefreshedDate) == 0 && event.getId() <= lastEventId);
		
		return content;
	}
	
	private void makeEventsHumanReadable(List<RepositoryEvent> events) {
		for(RepositoryEvent repositoryEvent : events) {
			
            if(Objects.equals(repositoryEvent.getChangedVariable(), "added")) {
                Package added = packageService.findByIdEvenDeleted(Integer.parseInt(repositoryEvent.getValueAfter()));
                repositoryEvent.setValueAfter(added.toString());
            } else if(Objects.equals(repositoryEvent.getChangedVariable(), "submitted")) {
                Submission submitted = submissionService.findByIdEvenDeleted(Integer.parseInt(repositoryEvent.getValueAfter()));
                repositoryEvent.setValueAfter(submitted.toString());
            } else if(Objects.equals(repositoryEvent.getChangedVariable(), "removed")) {
                Package removed = packageService.findByIdEvenDeleted(Integer.parseInt(repositoryEvent.getValueAfter()));
                repositoryEvent.setValueAfter(removed.toString());
            }
//            } else if(Objects.equals(repositoryEvent.getChangedVariable(), "version")) {
//            	Repository editedRepository = repositoryService.findById(Integer.parseInt(repositoryEvent.get));
//            	repositoryEvent.setValueAfter(editedRepository.getName());
//            }
         
        }
	}
	
}

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
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.RepositoryMaintainerEvent;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.RepositoryMaintainerEventRepository;
import eu.openanalytics.rdepot.time.DateProvider;

@Service
@Transactional(readOnly = true)
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class RepositoryMaintainerEventService
{	
	@Resource
	private RepositoryMaintainerEventRepository repositoryMaintainerEventRepository;
	
	@Resource
	private RepositoryMaintainerService repositoryMaintainerService;

	@Transactional
	public RepositoryMaintainerEvent create(RepositoryMaintainerEvent repositoryMaintainerEvent) 
	{
		RepositoryMaintainerEvent createdRepositoryMaintainerEvent = repositoryMaintainerEvent;
		return repositoryMaintainerEventRepository.save(createdRepositoryMaintainerEvent);
	}
	
	@Transactional(readOnly = false)
	public List<RepositoryMaintainerEvent> create(Event event, User user, RepositoryMaintainer repositoryMaintainer) 
	{
		RepositoryMaintainer oldRepositoryMaintainer = repositoryMaintainerService.findById(repositoryMaintainer.getId());
		RepositoryMaintainer newRepositoryMaintainer = repositoryMaintainer;
		
		List<RepositoryMaintainerEvent> events = new ArrayList<RepositoryMaintainerEvent>();
		
		if(Objects.equals(event.getValue(), "create"))
			events.add(new RepositoryMaintainerEvent(0, DateProvider.now(), user, repositoryMaintainer, event, "created", "", "", DateProvider.now()));
		else if(Objects.equals(event.getValue(), "update"))
		{
			if(oldRepositoryMaintainer.getUser().getId() != newRepositoryMaintainer.getUser().getId())
				events.add(new RepositoryMaintainerEvent(0, DateProvider.now(), user, repositoryMaintainer, event, "user", "" + oldRepositoryMaintainer.getUser().getId(), "" + newRepositoryMaintainer.getUser().getId(), DateProvider.now()));
			if(oldRepositoryMaintainer.getRepository().getId() != newRepositoryMaintainer.getRepository().getId())
				events.add(new RepositoryMaintainerEvent(0, DateProvider.now(), user, repositoryMaintainer, event, "repository", "" + oldRepositoryMaintainer.getRepository().getId(), "" + newRepositoryMaintainer.getRepository().getId(), DateProvider.now()));
		}
		else if(Objects.equals(event.getValue(), "delete"))
			events.add(new RepositoryMaintainerEvent(0, DateProvider.now(), user, repositoryMaintainer, event, "deleted", "", DateProvider.nowString(), DateProvider.now()));

		
		for(RepositoryMaintainerEvent rEvent : events)
		{
			rEvent = repositoryMaintainerEventRepository.save(rEvent);
		}
		
		return events;
	}
	
	public RepositoryMaintainerEvent findById(int id) 
	{
		return repositoryMaintainerEventRepository.getOne(id);
	}
	
	public List<RepositoryMaintainerEvent> findAll() 
	{
		return repositoryMaintainerEventRepository.findAll();
	}

	public List<RepositoryMaintainerEvent> findByRepositoryMaintainer(RepositoryMaintainer repositoryMaintainer) 
	{
		return repositoryMaintainerEventRepository.findByRepositoryMaintainer(repositoryMaintainer);
	}
	
	public List<RepositoryMaintainerEvent> findByChangedBy(User changedBy) 
	{
		return repositoryMaintainerEventRepository.findByChangedBy(changedBy);
	}

	public List<RepositoryMaintainerEvent> findByDate(Date date) 
	{
		return repositoryMaintainerEventRepository.findByDate(date);
	}
	
	@Transactional(readOnly = false)
	public void delete(int id)
	{
		RepositoryMaintainerEvent event = repositoryMaintainerEventRepository.getOne(id);
		if(event != null)
			repositoryMaintainerEventRepository.delete(event);
	}
}

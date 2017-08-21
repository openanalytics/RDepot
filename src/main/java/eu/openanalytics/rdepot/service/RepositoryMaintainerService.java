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

import javax.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.exception.EventNotFound;
import eu.openanalytics.rdepot.exception.PackageEditException;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerCreateException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerEditException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerNotFound;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.RepositoryMaintainerEvent;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.RepositoryMaintainerRepository;

@Service
@Transactional(readOnly = true)
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class RepositoryMaintainerService
{	
	@Resource
	private RepositoryMaintainerRepository repositoryMaintainerRepository;
	
	@Resource
	private PackageService packageService;
	
	@Resource
	private EventService eventService;
	
	@Resource
	private RepositoryMaintainerEventService repositoryMaintainerEventService;

	@Transactional(readOnly=false, rollbackFor={RepositoryMaintainerCreateException.class})
	public RepositoryMaintainer create(RepositoryMaintainer repositoryMaintainer, User creator) throws RepositoryMaintainerCreateException 
	{
		RepositoryMaintainer createdRepositoryMaintainer = repositoryMaintainer;
		Event createEvent = eventService.findByValue("create");
		try 
		{
			if(createEvent == null)
				throw new EventNotFound();
			createdRepositoryMaintainer = repositoryMaintainerRepository.save(createdRepositoryMaintainer);
			for(Package p : createdRepositoryMaintainer.getRepository().getPackages())
				packageService.update(p, creator);
			repositoryMaintainerEventService.create(createEvent, creator, createdRepositoryMaintainer);
		} 
		catch (PackageEditException | RepositoryEditException | EventNotFound e) 
		{
			throw new RepositoryMaintainerCreateException(e.getMessage());
		}
		return createdRepositoryMaintainer;
	}
	
	public RepositoryMaintainer findById(int id) 
	{
		return repositoryMaintainerRepository.findByIdAndDeleted(id, false);
	}
	
	public RepositoryMaintainer findByIdAndDeleted(int id, boolean deleted) 
	{
		return repositoryMaintainerRepository.findByIdAndDeleted(id, deleted);
	}
	
	@Transactional(readOnly=false, rollbackFor={RepositoryMaintainerDeleteException.class})
	public RepositoryMaintainer delete(int id, User deleter) throws RepositoryMaintainerDeleteException 
	{
		RepositoryMaintainer deletedRepositoryMaintainer = repositoryMaintainerRepository.findByIdAndDeleted(id, false);
		Event deleteEvent = eventService.findByValue("delete");
		
		try
		{
			if (deletedRepositoryMaintainer == null)
				throw new RepositoryMaintainerNotFound();
					
			if (deleteEvent == null)
				throw new EventNotFound();
			
			deletedRepositoryMaintainer.setDeleted(true);
			for(Package p : deletedRepositoryMaintainer.getRepository().getPackages())
				packageService.update(p, deleter);	
			
			repositoryMaintainerEventService.create(deleteEvent, deleter, deletedRepositoryMaintainer);
			
			return deletedRepositoryMaintainer;
		}
		catch(RepositoryMaintainerNotFound | RepositoryEditException | PackageEditException | EventNotFound e)
		{
			throw new RepositoryMaintainerDeleteException(e.getMessage());
		}
	}
	
	@Transactional(readOnly=false, rollbackFor={RepositoryMaintainerNotFound.class})
	public RepositoryMaintainer shiftDelete(int id) throws RepositoryMaintainerNotFound 
	{
		RepositoryMaintainer deletedRepositoryMaintainer = repositoryMaintainerRepository.findByIdAndDeleted(id, true);

		if (deletedRepositoryMaintainer == null)
			throw new RepositoryMaintainerNotFound();
				
		for(RepositoryMaintainerEvent event : deletedRepositoryMaintainer.getRepositoryMaintainerEvents())
			repositoryMaintainerEventService.delete(event.getId());
		repositoryMaintainerRepository.delete(deletedRepositoryMaintainer);
		return deletedRepositoryMaintainer;
	}

	public List<RepositoryMaintainer> findAll() 
	{
		return repositoryMaintainerRepository.findByDeleted(false, new Sort(new Order(Direction.ASC, "user.name")));
	}
	
	public List<RepositoryMaintainer> findByDeleted(boolean deleted) 
	{
		return repositoryMaintainerRepository.findByDeleted(deleted, new Sort(new Order(Direction.ASC, "user.name")));
	}

	@Transactional(readOnly=false, rollbackFor={RepositoryMaintainerEditException.class})
	public RepositoryMaintainer update(RepositoryMaintainer repositoryMaintainer, User updater) throws RepositoryMaintainerEditException 
	{
		RepositoryMaintainer updatedRepositoryMaintainer = repositoryMaintainerRepository.findByIdAndDeleted(repositoryMaintainer.getId(), false);
		List<RepositoryMaintainerEvent> events = new ArrayList<RepositoryMaintainerEvent>();
		Event updateEvent = eventService.findByValue("update");
		
		try
		{
			if (updatedRepositoryMaintainer == null)
				throw new RepositoryMaintainerNotFound();
			
			if(updateEvent == null)
				throw new EventNotFound();
			
			Repository oldRepository = updatedRepositoryMaintainer.getRepository();
			Repository newRepository = repositoryMaintainer.getRepository();
			
			if(oldRepository.getId() != newRepository.getId())
			{
				events.add(new RepositoryMaintainerEvent(0, new Date(), updater, repositoryMaintainer, updateEvent, "repository", "" + updatedRepositoryMaintainer.getRepository().getId(), "" + repositoryMaintainer.getRepository().getId(), new Date()));
				updatedRepositoryMaintainer.setRepository(repositoryMaintainer.getRepository());
				repositoryMaintainer = updatedRepositoryMaintainer;
				for(Package p : oldRepository.getPackages())
				{
					packageService.update(p, updater);
				}
				for(Package p : newRepository.getPackages())
				{
					packageService.update(p, updater);
				}
			}		
			
			if(updatedRepositoryMaintainer.getUser().getId() != repositoryMaintainer.getUser().getId())
			{
				events.add(new RepositoryMaintainerEvent(0, new Date(), updater, repositoryMaintainer, updateEvent, "user", "" + updatedRepositoryMaintainer.getUser().getId(), "" + repositoryMaintainer.getUser().getId(), new Date()));
				updatedRepositoryMaintainer.setUser(repositoryMaintainer.getUser());
				repositoryMaintainer = updatedRepositoryMaintainer;
			}
			
			for(RepositoryMaintainerEvent rEvent : events)
			{
				rEvent = repositoryMaintainerEventService.create(rEvent);
			}
			
			return repositoryMaintainer;
		}
		catch(RepositoryMaintainerNotFound | RepositoryEditException | PackageEditException | EventNotFound e)
		{
			throw new RepositoryMaintainerEditException(e.getMessage());
		}
	}

	public List<RepositoryMaintainer> findByRepository(Repository repository) 
	{
		return repositoryMaintainerRepository.findByRepositoryAndDeleted(repository, false);
	}

	public RepositoryMaintainer findByUserAndRepository(User user, Repository repository) 
	{
		return repositoryMaintainerRepository.findByUserAndRepositoryAndDeleted(user, repository, false);
	}
}

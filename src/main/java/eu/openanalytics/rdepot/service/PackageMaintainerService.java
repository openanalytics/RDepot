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
import java.util.Collections;
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

import eu.openanalytics.rdepot.comparator.PackageMaintainerComparator;
import eu.openanalytics.rdepot.exception.EventNotFound;
import eu.openanalytics.rdepot.exception.PackageEditException;
import eu.openanalytics.rdepot.exception.PackageMaintainerCreateException;
import eu.openanalytics.rdepot.exception.PackageMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.PackageMaintainerEditException;
import eu.openanalytics.rdepot.exception.PackageMaintainerNotFound;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.PackageMaintainerEvent;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.PackageMaintainerRepository;

@Service
@Transactional(readOnly = true)
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class PackageMaintainerService
{	
	@Resource
	private PackageMaintainerRepository packageMaintainerRepository;

	@Resource
	private PackageService packageService;
	
	@Resource
	private EventService eventService;
	
	@Resource
	private PackageMaintainerEventService packageMaintainerEventService;
	
	@Transactional(readOnly=false, rollbackFor={PackageMaintainerCreateException.class})
	public PackageMaintainer create(PackageMaintainer packageMaintainer, User creator) throws PackageMaintainerCreateException
	{
		Event createEvent = eventService.findByValue("create");
		try
		{
			if(createEvent == null)
				throw new EventNotFound();
			PackageMaintainer createdPackageMaintainer = packageMaintainer;
			createdPackageMaintainer = packageMaintainerRepository.save(createdPackageMaintainer);
			for(Package p : createdPackageMaintainer.getRepository().getPackages())
			{
				// Choose best maintainer
				if(Objects.equals(p.getName(), packageMaintainer.getPackage()))
						packageService.update(p, creator);
			}
			
			packageMaintainerEventService.create(createEvent, creator, createdPackageMaintainer);
			return createdPackageMaintainer;
		}
		catch(PackageEditException | RepositoryEditException | EventNotFound e)
		{
			throw new PackageMaintainerCreateException(e.getMessage());
		}
	}
	
	public PackageMaintainer findById(int id) 
	{
		return packageMaintainerRepository.findByIdAndDeleted(id, false);
	}
	
	public PackageMaintainer findByIdAndDeleted(int id, boolean deleted) 
	{
		return packageMaintainerRepository.findByIdAndDeleted(id, deleted);
	}
	
	@Transactional(readOnly=false, rollbackFor={PackageMaintainerDeleteException.class})
	public PackageMaintainer delete(int id, User deleter) throws PackageMaintainerDeleteException 
	{
		PackageMaintainer deletedPackageMaintainer = packageMaintainerRepository.findByIdAndDeleted(id, false);
		Event deleteEvent = eventService.findByValue("delete");
		try
		{
			if (deletedPackageMaintainer == null)
				throw new PackageMaintainerNotFound();
			if (deleteEvent == null)
				throw new EventNotFound();
			
			deletedPackageMaintainer.setDeleted(true);
			for(Package p : deletedPackageMaintainer.getRepository().getPackages())
			{
				// Choose best maintainer
				if(Objects.equals(p.getName(), deletedPackageMaintainer.getPackage()) && !p.isDeleted())
						packageService.update(p, deleter);
			}
			packageMaintainerEventService.create(new PackageMaintainerEvent(0, new Date(), deleter, deletedPackageMaintainer, deleteEvent, "deleted", "", "", new Date()));
			return deletedPackageMaintainer;
		}
		catch(PackageMaintainerNotFound | RepositoryEditException | PackageEditException | EventNotFound e)
		{
			throw new PackageMaintainerDeleteException(e.getMessage());
		}
	}
	
	@Transactional(readOnly=false, rollbackFor={PackageMaintainerDeleteException.class})
	public PackageMaintainer shiftDelete(int id) throws PackageMaintainerDeleteException 
	{
		PackageMaintainer deletedPackageMaintainer = packageMaintainerRepository.findByIdAndDeleted(id, true);
		try
		{
			if (deletedPackageMaintainer == null)
				throw new PackageMaintainerNotFound();
			
			deletePackageMaintainerEvents(deletedPackageMaintainer);
			packageMaintainerRepository.delete(deletedPackageMaintainer);
			return deletedPackageMaintainer;
		}
		catch(PackageMaintainerNotFound e)
		{
			throw new PackageMaintainerDeleteException(e.getMessage());
		}
	}
	
	@Transactional(readOnly = false)
	public void deletePackageMaintainerEvents(PackageMaintainer packageMaintainer)
	{
		for(PackageMaintainerEvent packageEvent : packageMaintainer.getPackageMaintainerEvents())
		{
			if(packageEvent != null)
				packageMaintainerEventService.delete(packageEvent.getId());
		}
	}

	public List<PackageMaintainer> findAll() 
	{
		return packageMaintainerRepository.findByDeleted(false, new Sort(new Order(Direction.ASC, "user.name")));
	}
	
	public List<PackageMaintainer> findByDeleted(boolean deleted) 
	{
		return packageMaintainerRepository.findByDeleted(deleted, new Sort(new Order(Direction.ASC, "user.name")));
	}

	@Transactional(readOnly=false, rollbackFor={PackageMaintainerEditException.class})
	public PackageMaintainer update(PackageMaintainer packageMaintainer, User updater) throws PackageMaintainerEditException 
	{
		PackageMaintainer updatedPackageMaintainer = packageMaintainerRepository.findOne(packageMaintainer.getId());
		Event updateEvent = eventService.findByValue("update");
		try
		{
			if (updatedPackageMaintainer == null)
				throw new PackageMaintainerNotFound();
			if(updateEvent == null)
				throw new EventNotFound();
			
			List<PackageMaintainerEvent> events = new ArrayList<PackageMaintainerEvent>();
			
			
			String oldPackage = updatedPackageMaintainer.getPackage();
			String newPackage = packageMaintainer.getPackage();
			
			Repository oldRepository = updatedPackageMaintainer.getRepository();
			Repository newRepository = packageMaintainer.getRepository();
			
			if(updatedPackageMaintainer.getUser().getId() != packageMaintainer.getUser().getId())
			{
				events.add(new PackageMaintainerEvent(0, new Date(), updater, packageMaintainer, updateEvent, "user", "" + updatedPackageMaintainer.getUser().getId(), "" + packageMaintainer.getUser().getId(), new Date()));
				updatedPackageMaintainer.setUser(packageMaintainer.getUser());
				packageMaintainer = updatedPackageMaintainer;
				for(Package p : oldRepository.getPackages())
				{
					// Choose best maintainer
					if(Objects.equals(p.getName(), oldPackage))
							packageService.update(p, updater);
				}
			}
			
			if(!Objects.equals(oldPackage, newPackage))
			{
				events.add(new PackageMaintainerEvent(0, new Date(), updater, packageMaintainer, updateEvent, "package", updatedPackageMaintainer.getPackage(), packageMaintainer.getPackage(), new Date()));
				updatedPackageMaintainer.setPackage(packageMaintainer.getPackage());
				packageMaintainer = updatedPackageMaintainer;
				for(Package p : oldRepository.getPackages())
				{
					// Choose best maintainer
					if(Objects.equals(p.getName(), oldPackage))
							packageService.update(p, updater);
				}
				for(Package p : newRepository.getPackages())
				{
					// Choose best maintainer
					if(Objects.equals(p.getName(), newPackage))
							packageService.update(p, updater);
				}
			}
			
			if(oldRepository.getId() != newRepository.getId())
			{
				events.add(new PackageMaintainerEvent(0, new Date(), updater, packageMaintainer, updateEvent, "repository", "" + updatedPackageMaintainer.getRepository().getId(), "" + packageMaintainer.getRepository().getId(), new Date()));
				updatedPackageMaintainer.setRepository(packageMaintainer.getRepository());
				packageMaintainer = updatedPackageMaintainer;
				for(Package p : oldRepository.getPackages())
				{
					// Choose best maintainer
					if(Objects.equals(p.getName(), oldPackage))
							packageService.update(p, updater);
				}
				for(Package p : newRepository.getPackages())
				{
					// Choose best maintainer
					if(Objects.equals(p.getName(), newPackage))
							packageService.update(p, updater);
				}
			}			
			
			for(PackageMaintainerEvent rEvent : events)
			{
				rEvent = packageMaintainerEventService.create(rEvent);
			}
			
			return packageMaintainer;
		}
		catch(PackageMaintainerNotFound | PackageEditException | RepositoryEditException | EventNotFound e)
		{
			throw new PackageMaintainerEditException(e.getMessage());
		}
	}

	public PackageMaintainer findByPackageAndRepository(String package_, Repository repository) 
	{
		return packageMaintainerRepository.findByPackageAndRepository(package_, repository);
	}
	
	public List<PackageMaintainer> findByRepository(Repository repository) 
	{
		return packageMaintainerRepository.findByRepository(repository);
	}

	public List<PackageMaintainer> findByRequester(User requester) 
	{
		ArrayList<PackageMaintainer> result = new ArrayList<PackageMaintainer>();
		switch(requester.getRole().getName())
		{
			case "admin":
				result.addAll(findAll());
				break;
			case "repositorymaintainer":
				for(RepositoryMaintainer repositoryMaintainer : requester.getRepositoryMaintainers())
				{
					result.addAll(findByRepository(repositoryMaintainer.getRepository()));
				}
				break;
		}
		Collections.sort(result, new PackageMaintainerComparator());
		return result;
	}
}

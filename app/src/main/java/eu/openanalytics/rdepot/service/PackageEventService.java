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

import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageEvent;
import eu.openanalytics.rdepot.model.RepositoryEvent;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.PackageEventRepository;

@Service
@Transactional(readOnly = true)
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class PackageEventService
{	
	@Resource
	private PackageEventRepository packageEventRepository;
	
	@Resource
	private PackageService packageService;
	
	@Resource
	private EventService eventService;
	
	@Resource
	private RepositoryEventService repositoryEventService;

	@Transactional(readOnly = false)
	public PackageEvent create(PackageEvent packageEvent) 
	{
		PackageEvent createdPackageEvent = packageEvent;
		return packageEventRepository.save(createdPackageEvent);
	}
	
	@Transactional(readOnly = false)
	public List<PackageEvent> create(Event event, User user, Package packageBag) 
	{
		Package oldPackage = packageService.findById(packageBag.getId());
		Package newPackage = packageBag;
		
		List<PackageEvent> events = new ArrayList<PackageEvent>();
		List<RepositoryEvent> repositoryEvents = new ArrayList<RepositoryEvent>();
		
		if(Objects.equals(event.getValue(), "create"))
		{
			events.add(new PackageEvent(0, new Date(), user, packageBag, event, "created", "", "", new Date()));
		}
		else if(Objects.equals(event.getValue(), "update"))
		{
			if(!Objects.equals(oldPackage.getVersion(), newPackage.getVersion()))
				events.add(new PackageEvent(0, new Date(), user, packageBag, event, "version", oldPackage.getVersion(), newPackage.getVersion() ,new Date()));
			if(oldPackage.getRepository().getId() != newPackage.getRepository().getId())
			{
				events.add(new PackageEvent(0, new Date(), user, packageBag, event, "repository", "" + oldPackage.getRepository().getId(), "" + newPackage.getRepository().getId(), new Date()));
				repositoryEvents.add(new RepositoryEvent(0, new Date(), user, oldPackage.getRepository(), event, "removed", "", "" + oldPackage.getId(), new Date()));
				repositoryEvents.add(new RepositoryEvent(0, new Date(), user, newPackage.getRepository(), event, "added", "", "" + newPackage.getId(), new Date()));
			}
			if(oldPackage.getUser().getId() != newPackage.getUser().getId())
				events.add(new PackageEvent(0, new Date(), user, packageBag, event, "maintainer", "" + oldPackage.getUser().getId(), "" + newPackage.getUser().getId(), new Date()));
			if(!Objects.equals(oldPackage.getName(), newPackage.getName()))
				events.add(new PackageEvent(0, new Date(), user, packageBag, event, "name", oldPackage.getName(), newPackage.getName(), new Date()));
			if(!Objects.equals(oldPackage.getDescription(), newPackage.getDescription()))
				events.add(new PackageEvent(0, new Date(), user, packageBag, event, "description", oldPackage.getDescription(), newPackage.getDescription(), new Date()));
			if(!Objects.equals(oldPackage.getAuthor(), newPackage.getAuthor()))
				events.add(new PackageEvent(0, new Date(), user, packageBag, event, "author", oldPackage.getAuthor(), newPackage.getAuthor(), new Date()));
			if(!Objects.equals(oldPackage.getDepends(), newPackage.getDepends()))
				events.add(new PackageEvent(0, new Date(), user, packageBag, event, "depends", oldPackage.getDepends(), newPackage.getDepends(), new Date()));
			if(!Objects.equals(oldPackage.getImports(), newPackage.getImports()))
				events.add(new PackageEvent(0, new Date(), user, packageBag, event, "imports", oldPackage.getImports(), newPackage.getImports(), new Date()));
			if(!Objects.equals(oldPackage.getSuggests(), newPackage.getSuggests()))
				events.add(new PackageEvent(0, new Date(), user, packageBag, event, "suggests", oldPackage.getSuggests(), newPackage.getSuggests(), new Date()));
			if(!Objects.equals(oldPackage.getSystemRequirements(), newPackage.getSystemRequirements()))
				events.add(new PackageEvent(0, new Date(), user, packageBag, event, "system requirements", oldPackage.getSystemRequirements(), newPackage.getSystemRequirements(), new Date()));
			if(!Objects.equals(oldPackage.getLicense(), newPackage.getLicense()))
				events.add(new PackageEvent(0, new Date(), user, packageBag, event, "license", oldPackage.getLicense(), newPackage.getLicense(), new Date()));
			if(!Objects.equals(oldPackage.getTitle(), newPackage.getTitle()))
				events.add(new PackageEvent(0, new Date(), user, packageBag, event, "title", oldPackage.getTitle(), newPackage.getTitle(), new Date()));
			if(!Objects.equals(oldPackage.getUrl(), newPackage.getDescription()))
				events.add(new PackageEvent(0, new Date(), user, packageBag, event, "URL", oldPackage.getUrl(), newPackage.getUrl(), new Date()));
			if(!Objects.equals(oldPackage.getSource(), newPackage.getSource()))
				events.add(new PackageEvent(0, new Date(), user, packageBag, event, "source", oldPackage.getSource(), newPackage.getSource(), new Date()));
			if(oldPackage.isActive() != newPackage.isActive())
				events.add(new PackageEvent(0, new Date(), user, packageBag, event, "active", "" + oldPackage.isActive(), "" + newPackage.isActive(), new Date()));
		}
		else if(Objects.equals(event.getValue(), "delete"))
		{
			events.add(new PackageEvent(0, new Date(), user, packageBag, event, "deleted", "", "", new Date()));
			repositoryEvents.add(new RepositoryEvent(0, new Date(), user, packageBag.getRepository(), event, "removed", "", "" + packageBag.getId(), new Date()));
		}
		
		for(PackageEvent pEvent : events)
		{
			pEvent = packageEventRepository.save(pEvent);
		}
		for(RepositoryEvent rEvent : repositoryEvents)
		{
			rEvent = repositoryEventService.create(rEvent);
		}
		
		return events;
	}
	
	public PackageEvent findById(int id) 
	{
		return packageEventRepository.getOne(id);
	}
	
	public List<PackageEvent> findAll() 
	{
		return packageEventRepository.findAll();
	}

	public List<PackageEvent> findByPackage(Package packageBag) 
	{
		return packageEventRepository.findByPackage(packageBag);
	}
	
	public List<PackageEvent> findByChangedBy(User changedBy) 
	{
		return packageEventRepository.findByChangedBy(changedBy);
	}

	public List<PackageEvent> findByDate(Date date) 
	{
		return packageEventRepository.findByDate(date);
	}
	
	public PackageEvent getCreatedOn(Package packageBag) 
	{
		return packageEventRepository.findByPackageAndEvent_Value(packageBag, "create");
	}
	
	public List<Date> getUniqueDates() 
	{
		List<PackageEvent> events = findAll();
		List<Date> dates = new ArrayList<Date>();
		for(PackageEvent event : events)
		{
			if(!dates.contains(event.getDate()))
				dates.add(event.getDate());
		}
		return dates;
	}
	
	public List<Date> getUniqueDatesByPackage(Package packageBag) 
	{
		List<PackageEvent> events = findByPackage(packageBag);
		List<Date> dates = new ArrayList<Date>();
		for(PackageEvent event : events)
		{
			if(!dates.contains(event.getDate()))
				dates.add(event.getDate());
		}
		return dates;
	}
	
	public List<PackageEvent> findByDateAndPackage(Date date, Package packageBag) 
	{
		return packageEventRepository.findByDateAndPackage(date, packageBag);
	}
	
	@Transactional(readOnly = false)
	public void delete(int id)
	{
		PackageEvent deletedPackageEvent = packageEventRepository.getOne(id);

		if (deletedPackageEvent != null)	
			packageEventRepository.delete(deletedPackageEvent);	

	}
}

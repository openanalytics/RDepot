/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.PackageMaintainerEvent;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.PackageMaintainerEventRepository;

@Service
@Transactional(readOnly = true)
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class PackageMaintainerEventService {	
	@Resource
	private PackageMaintainerEventRepository packageMaintainerEventRepository;
	
	@Resource
	private PackageMaintainerService packageMaintainerService;

	@Transactional(readOnly = false)
	public PackageMaintainerEvent create(PackageMaintainerEvent packageMaintainerEvent) {
		PackageMaintainerEvent createdPackageMaintainerEvent = packageMaintainerEvent;
		return packageMaintainerEventRepository.save(createdPackageMaintainerEvent);
	}
	
	@Transactional(readOnly = false)
	public List<PackageMaintainerEvent> create(Event event, User user, PackageMaintainer packageMaintainer) {
		PackageMaintainer oldPackageMaintainer = packageMaintainerService.findById(packageMaintainer.getId());
		PackageMaintainer newPackageMaintainer = packageMaintainer;
		
		List<PackageMaintainerEvent> events = new ArrayList<PackageMaintainerEvent>();
		
		if(Objects.equals(event.getValue(), "create"))
			events.add(new PackageMaintainerEvent(0, new Date(), user, packageMaintainer, event, "created", "", "", new Date()));
		else if(Objects.equals(event.getValue(), "update")) {
			if(oldPackageMaintainer.getUser().getId() != newPackageMaintainer.getUser().getId())
				events.add(new PackageMaintainerEvent(0, new Date(), user, packageMaintainer, event, "user", "" + oldPackageMaintainer.getUser().getId(), "" + newPackageMaintainer.getUser().getId(), new Date()));
			if(oldPackageMaintainer.getRepository().getId() != newPackageMaintainer.getRepository().getId())
				events.add(new PackageMaintainerEvent(0, new Date(), user, packageMaintainer, event, "repository", "" + oldPackageMaintainer.getRepository().getId(), "" + newPackageMaintainer.getRepository().getId(), new Date()));
			if(!Objects.equals(oldPackageMaintainer.getPackage(), newPackageMaintainer.getPackage()))
				events.add(new PackageMaintainerEvent(0, new Date(), user, packageMaintainer, event, "package", oldPackageMaintainer.getPackage(), newPackageMaintainer.getPackage(), new Date()));
		}
		
		for(PackageMaintainerEvent pEvent : events) {
			pEvent = packageMaintainerEventRepository.save(pEvent);
		}
		
		return events;
	}
	
	public PackageMaintainerEvent findById(int id) {
		return packageMaintainerEventRepository.getOne(id);
	}
	
	public List<PackageMaintainerEvent> findAll() {
		return packageMaintainerEventRepository.findAll();
	}

	public List<PackageMaintainerEvent> findByPackageMaintainer(PackageMaintainer packageMaintainer) {
		return packageMaintainerEventRepository.findByPackageMaintainer(packageMaintainer);
	}
	
	public List<PackageMaintainerEvent> findByChangedBy(User changedBy) {
		return packageMaintainerEventRepository.findByChangedBy(changedBy);
	}

	public List<PackageMaintainerEvent> findByDate(Date date) {
		return packageMaintainerEventRepository.findByDate(date);
	}
	
	@Transactional(readOnly = false)
	public void delete(int id) {
		PackageMaintainerEvent deletedPackageMaintainerEvent = packageMaintainerEventRepository.getOne(id);
		if (deletedPackageMaintainerEvent != null)	
			packageMaintainerEventRepository.delete(deletedPackageMaintainerEvent);	
	}
}

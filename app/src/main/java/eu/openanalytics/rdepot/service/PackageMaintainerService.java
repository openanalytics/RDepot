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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.i18n.LocaleContextHolder;
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
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.PackageMaintainerEvent;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.PackageMaintainerRepository;
import eu.openanalytics.rdepot.time.DateProvider;

@Service
@Transactional(readOnly = true)
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class PackageMaintainerService {	
	
	Logger logger = LoggerFactory.getLogger(PackageMaintainer.class);
	Locale locale = LocaleContextHolder.getLocale();
	
	@Resource
	MessageSource messageSource;
	
	@Resource
	private PackageMaintainerRepository packageMaintainerRepository;

	@Resource
	private PackageService packageService;
	
	@Resource
	private EventService eventService;
	
	@Resource
	private PackageMaintainerEventService packageMaintainerEventService;
	
	@Transactional(readOnly=false, rollbackFor={PackageMaintainerCreateException.class})
	public PackageMaintainer create(PackageMaintainer packageMaintainer, User creator) 
			throws PackageMaintainerCreateException {
		
		try {
			PackageMaintainer deletedMaintainer = packageMaintainerRepository.
					findByPackageAndRepositoryAndDeleted(
							packageMaintainer.getPackage(), packageMaintainer.getRepository(), true);
			
			if(deletedMaintainer != null) {
				deletedMaintainer.setDeleted(false);
				for(Package packageBag : deletedMaintainer.getRepository().getPackages()) {
					if(Objects.equals(packageBag.getName(), deletedMaintainer.getPackage())) {
						packageService.refreshMaintainer(packageBag, creator);
					}
				}
				
				return deletedMaintainer;
			}

			Event createEvent = eventService.getCreateEvent();
			PackageMaintainer createdPackageMaintainer = packageMaintainer;
			
			createdPackageMaintainer = packageMaintainerRepository.save(createdPackageMaintainer);
			
			for(Package p : createdPackageMaintainer.getRepository().getNonDeletedPackages()) {
				// Choose best maintainer
				if(Objects.equals(p.getName(), packageMaintainer.getPackage()))
						packageService.refreshMaintainer(p, creator);
			}
			
			PackageMaintainerEvent packageMaintainerEvent = 
					new PackageMaintainerEvent(0, DateProvider.now(), creator, packageMaintainer, 
							createEvent, "created", "", "", DateProvider.now());
			
			packageMaintainerEventService.create(packageMaintainerEvent);
			
			return createdPackageMaintainer;
		}
		catch(PackageEditException | EventNotFound e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new PackageMaintainerCreateException(messageSource, locale, packageMaintainer);
		}
	}
	
	public PackageMaintainer findById(int id) {
		return packageMaintainerRepository.findByIdAndDeleted(id, false);
	}
	
	public PackageMaintainer findByIdAndDeleted(int id, boolean deleted) {
		return packageMaintainerRepository.findByIdAndDeleted(id, deleted);
	}
	
	@Transactional(readOnly=false, rollbackFor={PackageMaintainerDeleteException.class})
	public PackageMaintainer delete(int id, User deleter) throws PackageMaintainerDeleteException, PackageMaintainerNotFound {
		PackageMaintainer deletedPackageMaintainer = packageMaintainerRepository.findByIdAndDeleted(id, false);
		
		try {			
			if (deletedPackageMaintainer == null)
				throw new PackageMaintainerNotFound(id, messageSource, locale);
			
			Event deleteEvent = eventService.getDeleteEvent();
			
			deletedPackageMaintainer.setDeleted(true);
			for(Package p : deletedPackageMaintainer.getRepository().getNonDeletedPackages()) {
				// Choose best maintainer
				if(Objects.equals(p.getName(), deletedPackageMaintainer.getPackage()))
						packageService.refreshMaintainer(p, deleter);
			}
			
			packageMaintainerEventService.create(
					new PackageMaintainerEvent(0, DateProvider.now(), deleter, deletedPackageMaintainer, 
							deleteEvent, "deleted", "", "", DateProvider.now()));
			
			return deletedPackageMaintainer;
		}
		catch(PackageEditException | EventNotFound e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new PackageMaintainerDeleteException(deletedPackageMaintainer, messageSource, locale);
		}
	}
	
	@Transactional(readOnly=false, rollbackFor={PackageMaintainerDeleteException.class})
	public PackageMaintainer shiftDelete(int id) 
			throws PackageMaintainerDeleteException, PackageMaintainerNotFound {
		PackageMaintainer deletedPackageMaintainer = packageMaintainerRepository.findByIdAndDeleted(id, true);
		if (deletedPackageMaintainer == null)
			throw new PackageMaintainerNotFound(id, messageSource, locale);
		
		deletePackageMaintainerEvents(deletedPackageMaintainer);
		// TODO: shiftDelete the "deleted" packages that were still maintained by the "deleted" package maintainer
		packageMaintainerRepository.delete(deletedPackageMaintainer);
		return deletedPackageMaintainer;
		
	}
	
	@Transactional(readOnly = false)
	public void deletePackageMaintainerEvents(PackageMaintainer packageMaintainer) {
		for(PackageMaintainerEvent packageEvent : packageMaintainer.getPackageMaintainerEvents()) {
			if(packageEvent != null)
				packageMaintainerEventService.delete(packageEvent.getId());
		}
	}

	public List<PackageMaintainer> findAll() {
		return packageMaintainerRepository.findByDeleted(false, Sort.by(new Order(Direction.ASC, "user.name")));
	}
	
	public List<PackageMaintainer> findByDeleted(boolean deleted) {
		return packageMaintainerRepository.findByDeleted(deleted, Sort.by(new Order(Direction.ASC, "user.name")));
	}
	
	@Transactional(readOnly=false, rollbackFor={PackageMaintainerEditException.class})
	public void updateUser(PackageMaintainer packageMaintainer, User user, User updater) throws PackageMaintainerEditException {
		try {
			Repository repository = packageMaintainer.getRepository();
			Event updateEvent = eventService.getUpdateEvent();
			PackageMaintainerEvent packageMaintainerEvent = new PackageMaintainerEvent(0, DateProvider.now(), 
					updater, packageMaintainer, updateEvent, "user", 
					Integer.toString(packageMaintainer.getUser().getId()), 
					Integer.toString(user.getId()), DateProvider.now());
			
			packageMaintainer.setUser(user);
			
			for(Package packageBag : repository.getNonDeletedPackages()) {
				if(Objects.equals(packageBag.getName(), packageMaintainer.getPackage())) {
					packageService.refreshMaintainer(packageBag, updater);
				}
			}
			
			packageMaintainerEventService.create(packageMaintainerEvent);
			
		} catch (EventNotFound | PackageEditException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new PackageMaintainerEditException(messageSource, locale, packageMaintainer);
		}
	}
	
	@Transactional(readOnly=false, rollbackFor={PackageMaintainerEditException.class})
	public void updateRepository(PackageMaintainer packageMaintainer, Repository repository, User updater) 
			throws PackageMaintainerEditException {
		try {
			Repository oldRepository = packageMaintainer.getRepository();
			Event updateEvent = eventService.getUpdateEvent();
			PackageMaintainerEvent packageMaintainerEvent = new PackageMaintainerEvent(0, DateProvider.now(), 
					updater, packageMaintainer, updateEvent, "repository", 
					Integer.toString(oldRepository.getId()), 
					Integer.toString(repository.getId()), DateProvider.now());
			
			packageMaintainer.setRepository(repository);
			
			for(Package packageBag : oldRepository.getNonDeletedPackages()) {
				if(Objects.equals(packageBag.getName(), packageMaintainer.getPackage())) {
					packageService.refreshMaintainer(packageBag, updater);
				}
			}
			
			for(Package packageBag : repository.getNonDeletedPackages()) {
				if(Objects.equals(packageBag.getName(), packageMaintainer.getPackage())) {
					packageService.refreshMaintainer(packageBag, updater);
				}
			}
			
			packageMaintainerEventService.create(packageMaintainerEvent);
			
		} catch (EventNotFound | PackageEditException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new PackageMaintainerEditException(messageSource, locale, packageMaintainer);
		}
	}
	
	@Transactional(readOnly=false, rollbackFor={PackageMaintainerEditException.class})
	public void updatePackage(PackageMaintainer packageMaintainer, String packageName, User updater) 
			throws PackageMaintainerEditException {
		String oldPackageName = packageMaintainer.getPackage();

		try {
			Event updateEvent = eventService.getUpdateEvent();
			
			PackageMaintainerEvent packageMaintainerEvent = new PackageMaintainerEvent(0, DateProvider.now(), 
					updater, packageMaintainer, updateEvent, "package", 
					oldPackageName, packageName, DateProvider.now());
			
			packageMaintainer.setPackage(packageName);
			
			for(Package packageBag : packageMaintainer.getRepository().getNonDeletedPackages()) {
				if(Objects.equals(packageBag.getName(), packageName) || 
						Objects.equals(packageBag.getName(), oldPackageName)) {
					packageService.refreshMaintainer(packageBag, updater);
				}
			}
			
			packageMaintainerEventService.create(packageMaintainerEvent);

		} catch (EventNotFound | PackageEditException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			packageMaintainer.setPackage(oldPackageName);
			throw new PackageMaintainerEditException(messageSource, locale, packageMaintainer);
		}
	}
	
	@Transactional(readOnly=false, rollbackFor= {PackageMaintainerEditException.class})
	public void evaluateAndUpdate(PackageMaintainer packageMaintainer, User updater) 
			throws PackageMaintainerNotFound, PackageMaintainerEditException {
		PackageMaintainer updatedPackageMaintainer = packageMaintainerRepository.getOne(packageMaintainer.getId());
		
		if(updatedPackageMaintainer == null)
			throw new PackageMaintainerNotFound(packageMaintainer.getId(), messageSource, locale);
		if(updatedPackageMaintainer.getPackage().equals(packageMaintainer.getPackage()))
			updatePackage(updatedPackageMaintainer, packageMaintainer.getPackage(), updater);
		if(updatedPackageMaintainer.getRepository().getId() != packageMaintainer.getRepository().getId())
			updateRepository(updatedPackageMaintainer, packageMaintainer.getRepository(), updater);

	}

//	@Transactional(readOnly=false, rollbackFor={PackageMaintainerEditException.class})
//	public PackageMaintainer update(PackageMaintainer packageMaintainer, User updater) 
//			throws PackageMaintainerEditException {
//		PackageMaintainer updatedPackageMaintainer = packageMaintainerRepository.getOne(packageMaintainer.getId());
//		
//		try {
//			Event updateEvent = eventService.getUpdateEvent();
//			if (updatedPackageMaintainer == null)
//				throw new PackageMaintainerNotFound(id, messageSource, locale);
//			
//			List<PackageMaintainerEvent> events = new ArrayList<PackageMaintainerEvent>();
//			
//			
//			String oldPackage = updatedPackageMaintainer.getPackage();
//			String newPackage = packageMaintainer.getPackage();
//			
//			Repository oldRepository = updatedPackageMaintainer.getRepository();
//			Repository newRepository = packageMaintainer.getRepository();
//			
//			if(updatedPackageMaintainer.getUser().getId() != packageMaintainer.getUser().getId())
//			{
//				events.add(new PackageMaintainerEvent(0, new Date(), updater, packageMaintainer, updateEvent, "user", "" + updatedPackageMaintainer.getUser().getId(), "" + packageMaintainer.getUser().getId(), new Date()));
//				updatedPackageMaintainer.setUser(packageMaintainer.getUser());
//				packageMaintainer = updatedPackageMaintainer;
//				for(Package p : oldRepository.getNonDeletedPackages())
//				{
//					// Choose best maintainer
//					if(Objects.equals(p.getName(), oldPackage))
//							packageService.refreshMaintainer(p, updater);
//				}
//			}
//			
//			if(!Objects.equals(oldPackage, newPackage))
//			{
//				events.add(new PackageMaintainerEvent(0, new Date(), updater, packageMaintainer, updateEvent, "package", updatedPackageMaintainer.getPackage(), packageMaintainer.getPackage(), new Date()));
//				updatedPackageMaintainer.setPackage(packageMaintainer.getPackage());
//				packageMaintainer = updatedPackageMaintainer;
//				for(Package p : oldRepository.getNonDeletedPackages())
//				{
//					// Choose best maintainer
//					if(Objects.equals(p.getName(), oldPackage))
//						packageService.refreshMaintainer(p, updater);
//				}
//				for(Package p : newRepository.getNonDeletedPackages())
//				{
//					// Choose best maintainer
//					if(Objects.equals(p.getName(), newPackage))
//						packageService.refreshMaintainer(p, updater);
//				}
//			}
//			
//			if(oldRepository.getId() != newRepository.getId())
//			{
//				events.add(new PackageMaintainerEvent(0, new Date(), updater, packageMaintainer, updateEvent, "repository", "" + updatedPackageMaintainer.getRepository().getId(), "" + packageMaintainer.getRepository().getId(), new Date()));
//				updatedPackageMaintainer.setRepository(packageMaintainer.getRepository());
//				packageMaintainer = updatedPackageMaintainer;
//				for(Package p : oldRepository.getNonDeletedPackages())
//				{
//					// Choose best maintainer
//					if(Objects.equals(p.getName(), oldPackage))
//						packageService.refreshMaintainer(p, updater);
//				}
//				for(Package p : newRepository.getNonDeletedPackages())
//				{
//					// Choose best maintainer
//					if(Objects.equals(p.getName(), newPackage))
//						packageService.refreshMaintainer(p, updater);
//				}
//			}			
//			
//			for(PackageMaintainerEvent rEvent : events)
//			{
//				rEvent = packageMaintainerEventService.create(rEvent);
//			}
//			
//			return packageMaintainer;
//		}
//		catch(PackageMaintainerNotFound | PackageEditException | EventNotFound e)
//		{
//			throw new PackageMaintainerEditException(e.getMessage());
//		}
//	}

	public PackageMaintainer findByPackageAndRepository(String package_, Repository repository) {
		return packageMaintainerRepository.findByPackageAndRepository(package_, repository);
	}
	
	public PackageMaintainer findByPackageAndRepositoryAndNotDeleted(String package_, Repository repository) {
		return packageMaintainerRepository.findByPackageAndRepositoryAndDeleted(package_, repository, false);
	}
	
	public List<PackageMaintainer> findByRepository(Repository repository) {
		return packageMaintainerRepository.findByRepository(repository);
	}
	
	
	public List<PackageMaintainer> findByRequester(User requester) {
		ArrayList<PackageMaintainer> result = new ArrayList<PackageMaintainer>();
		switch(requester.getRole().getName()) {
			case "admin":
				result.addAll(findAll());
				break;
			case "repositorymaintainer":
				for(RepositoryMaintainer repositoryMaintainer : requester.getRepositoryMaintainers()) {
					if (!repositoryMaintainer.isDeleted())
						result.addAll(findByRepository(repositoryMaintainer.getRepository()));
				}
				break;
		}
		Collections.sort(result, new PackageMaintainerComparator());
		return result;
	}
}

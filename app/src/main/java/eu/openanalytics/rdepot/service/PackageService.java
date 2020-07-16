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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

import eu.openanalytics.rdepot.exception.AdminNotFound;
import eu.openanalytics.rdepot.exception.EventNotFound;
import eu.openanalytics.rdepot.exception.GetFileInBytesException;
import eu.openanalytics.rdepot.exception.ManualCreateException;
import eu.openanalytics.rdepot.exception.Md5SumCalculationException;
import eu.openanalytics.rdepot.exception.PackageActivateException;
import eu.openanalytics.rdepot.exception.PackageCreateException;
import eu.openanalytics.rdepot.exception.PackageDeactivateException;
import eu.openanalytics.rdepot.exception.PackageDeleteException;
import eu.openanalytics.rdepot.exception.PackageEditException;
import eu.openanalytics.rdepot.exception.PackageNotFound;
import eu.openanalytics.rdepot.exception.PackageStorageException;
import eu.openanalytics.rdepot.exception.ReadPackageVignetteException;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.exception.RepositoryPublishException;
import eu.openanalytics.rdepot.exception.SourceFileDeleteException;
import eu.openanalytics.rdepot.exception.SubmissionDeleteException;
import eu.openanalytics.rdepot.exception.SubmissionNotFound;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageEvent;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.PackageRepository;
import eu.openanalytics.rdepot.storage.PackageStorage;
import eu.openanalytics.rdepot.time.DateProvider;
import eu.openanalytics.rdepot.warning.PackageAlreadyActivatedWarning;
import eu.openanalytics.rdepot.warning.PackageAlreadyDeactivatedWarning;
import eu.openanalytics.rdepot.warning.PackageAlreadyDeletedWarning;
import eu.openanalytics.rdepot.warning.SubmissionDeleteWarning;

@Service
@Transactional(readOnly = true)
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class PackageService {
	
	Logger logger = LoggerFactory.getLogger(PackageService.class);
	
	Locale locale = LocaleContextHolder.getLocale();
	
	@Resource
	private MessageSource messageSource;
	
	@Resource
	private PackageRepository packageRepository;
	
	@Resource
	private RoleService roleService;
	
	@Resource
	private UserService userService;
	
	@Resource
	private RepositoryService repositoryService;
	
	@Resource
	private SubmissionService submissionService;
	
	@Resource
	private PackageMaintainerService packageMaintainerService;
	
	@Resource
	private RepositoryMaintainerService repositoryMaintainerService;
	
	@Resource
	private PackageEventService packageEventService;
	
	@Resource
	private EventService eventService;
	
	@Resource
	private PackageStorage packageStorage;
	
	@Transactional(readOnly = false, rollbackFor=PackageCreateException.class)
	private Package create(Package packageBag, User creator) throws PackageCreateException {
		Package createdPackage = packageBag;
		createdPackage = packageRepository.save(createdPackage);
		
		try {
			Event createEvent = eventService.getCreateEvent();
			PackageEvent packageEvent = new PackageEvent(
					0, DateProvider.now(), creator, packageBag, 
					createEvent, "created", "", "", DateProvider.now());
			
			packageEventService.create(packageEvent);
		} catch (EventNotFound e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new PackageCreateException(messageSource, locale, packageBag);
		}
		
		return createdPackage;
	}
	
	@Transactional(readOnly = false)
	public Package create(Package packageBag, User creator, boolean replace) throws PackageCreateException {
		if (replace) {
			Package checkSameVersion = findByNameAndVersionAndRepository(packageBag.getName(), 
					packageBag.getVersion(), packageBag.getRepository());
			
			if (checkSameVersion != null)
				try {
					delete(checkSameVersion.getId(), creator);
				} catch (PackageDeleteException | PackageNotFound e) {
					logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
					throw new PackageCreateException(messageSource, locale, packageBag);
				} catch (PackageAlreadyDeletedWarning e) {
					logger.warn(e.getClass().getName() + ": " + e.getMessage(), e);
				}
		}
		return create(packageBag, creator);
	}
	
	public Package findById(int id) {
		return packageRepository.findByIdAndDeleted(id, false);
	}
	
	public Package findByIdEvenDeleted(int id) {
		return packageRepository.getOne(id);
	}
	
	public List<Package> findByDeleted(boolean deleted) {
		return packageRepository.findByDeleted(deleted, Sort.by(new Order(Direction.ASC, "name")));
	}
	
	public Package findByIdAndDeleted(int id, boolean deleted) {
		return packageRepository.findByIdAndDeleted(id, deleted);
	}
	
	@Transactional(readOnly=false, rollbackFor={PackageDeleteException.class})
	public void delete(int id, User deleter) throws PackageDeleteException, PackageAlreadyDeletedWarning, PackageNotFound {
		Package deletedPackage = packageRepository.findByIdAndDeleted(id, false);
		if (deletedPackage == null)
			throw new PackageNotFound(messageSource, locale, id);
		
		try {
			if (deletedPackage.isDeleted()) {
				PackageAlreadyDeletedWarning warning = new PackageAlreadyDeletedWarning(messageSource, locale, deletedPackage);
				logger.warn(warning.getMessage(), warning);
				throw warning;
			}
			
			Event deleteEvent = eventService.getDeleteEvent();
			PackageEvent deletePackageEvent = new PackageEvent(0, DateProvider.now(), deleter, 
					deletedPackage, deleteEvent, "delete", "false", "true", DateProvider.now());
					
			deletedPackage.setDeleted(true);
			update(deletePackageEvent, deletedPackage, deleter);
			
			if(!deletedPackage.getSubmission().isDeleted())
				submissionService.deleteSubmission(deletedPackage.getSubmission().getId(), deleter);
		} 
		catch (PackageEditException | EventNotFound | SubmissionNotFound | SubmissionDeleteException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new PackageDeleteException(messageSource, locale, deletedPackage);
		} catch(SubmissionDeleteWarning w) {
			logger.warn(w.getClass().getName() + ": " + w.getMessage(), w);
		}
	}
	
	@Transactional(readOnly=false, rollbackFor=PackageDeleteException.class)
	public Package shiftDelete(int id) throws PackageDeleteException , PackageNotFound {
		Package deletedPackage = packageRepository.findByIdAndDeleted(id, true);
		if (deletedPackage == null)
			throw new PackageNotFound(messageSource, locale, id);
		
		try {
			submissionService.shiftDelete(deletedPackage.getSubmission().getId());
			deleteSource(deletedPackage);
			deletePackageEvents(deletedPackage);
			packageRepository.delete(deletedPackage);
			return deletedPackage;
		} 
		catch (PackageStorageException | SubmissionDeleteException | SubmissionNotFound e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new PackageDeleteException(messageSource, locale, deletedPackage);
		}	
	}
	
	@Transactional(readOnly = false, rollbackFor=PackageActivateException.class)
	public void activatePackage(Package packageBag, User requester) throws PackageAlreadyActivatedWarning, PackageActivateException {
		if(packageBag.isActive()) {
			PackageAlreadyActivatedWarning warning = new PackageAlreadyActivatedWarning(messageSource, locale, packageBag);
			logger.warn(warning.getMessage(), warning);
			throw warning;
		}
		
		try {
			Event updateEvent = eventService.getUpdateEvent();
			packageBag.setActive(true);
			PackageEvent packageUpdateEvent = new PackageEvent(0, DateProvider.now(), requester, packageBag, updateEvent, 
					"active", Boolean.toString(false), Boolean.toString(true), DateProvider.now());
			updateAndRefreshMaintainer(packageUpdateEvent, packageBag, requester);

		} catch (EventNotFound | PackageEditException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			throw new PackageActivateException(messageSource, locale, packageBag);
		}
		
//		String name = packageBag.getName();
//		Repository repository = packageBag.getRepository();
//		List<Package> packages = findByNameAndRepository(name, repository);
//		if(packages.size() > 0)
//		{
//			for(Package p : packages)
//			{
//				if(p.isActive() && p.getId() != packageBag.getId())
//				{
//					deactivatePackage(p, requester);
//				}
//			}
//		}
	}
	
	@Transactional(readOnly = false, rollbackFor=PackageEditException.class)
	public void deactivatePackage(Package packageBag, User requester) throws PackageDeactivateException, PackageAlreadyDeactivatedWarning {
		if(!packageBag.isActive())
			throw new PackageAlreadyDeactivatedWarning(messageSource, locale, packageBag);
			
		try {
			Event updateEvent = eventService.getUpdateEvent();
			packageBag.setActive(false);
			PackageEvent packageUpdateEvent = new PackageEvent(0, DateProvider.now(), requester, packageBag, updateEvent, 
					"active", Boolean.toString(true), Boolean.toString(packageBag.isActive()), DateProvider.now());
			updateAndRefreshMaintainer(packageUpdateEvent, packageBag, requester);
		} catch (EventNotFound | PackageEditException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new PackageDeactivateException(messageSource, locale, packageBag);
		}
		
	}
	
	public String calculateMd5Sum(Package packageBag) throws Md5SumCalculationException {
		return packageStorage.calculatePackageMd5Sum(packageBag);
	}
	
	public boolean isSameMd5Sum(Package packageBag, String md5sum) {
		if(Objects.equals(packageBag.getMd5sum(), md5sum))
			return true;
		return false;
	}
	
	public List<Package> findAll() {
		List<Package> allPackages = packageRepository.findByDeleted(false, Sort.by(new Order(Direction.ASC, "name")));
		List<Package> filtered = new ArrayList<>();
		filtered.addAll(allPackages.stream()
				.filter(packageBag -> packageBag.getSubmission().isAccepted()).collect(Collectors.toList()));
		return filtered;
	}
	
	@Transactional(readOnly = false, rollbackFor=PackageEditException.class)
	public void updateSource(Package packageBag, String source, User requester) throws PackageEditException {
		Event updateEvent;
		String currentSource = packageBag.getSource();

		try {
			updateEvent = eventService.getUpdateEvent();
			packageStorage.verifySource(packageBag, source);
		} catch (EventNotFound | PackageStorageException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new PackageEditException(messageSource, locale, packageBag);
		}
		
		packageBag.setSource(source);
		PackageEvent updatePackageEvent = new PackageEvent(0, DateProvider.now(), requester, packageBag, updateEvent, "source", currentSource, source, DateProvider.now());
		updateAndRefreshMaintainer(updatePackageEvent, packageBag, requester);
	}
	
	public byte[] readVignette(int id, String fileName) throws ReadPackageVignetteException {
		Package packageBag = findById(id);
		byte[] bytes = null;
		if(packageBag != null) {
			try {
				bytes = packageStorage.readVignette(packageBag, fileName);
			} catch (GetFileInBytesException e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
				throw new ReadPackageVignetteException(messageSource.getMessage(MessageCodes.ERROR_PACKAGE_READ_VIGNETTE, null, MessageCodes.ERROR_PACKAGE_READ_VIGNETTE, LocaleContextHolder.getLocale()));
			}
		}
		return bytes;
	}
	
//	@Transactional(readOnly = false)
//	public void shiftDeleteSubmissions(Package packageBag) throws SubmissionDeleteException, SubmissionNotFound {
//		for(Submission submission : packageBag.getSubmissions()) {
//			if(submission != null)
//				submissionService.shiftDelete(submission.getId());
//		}
//	}
	
	@Transactional(readOnly = false)
	public void deletePackageEvents(Package packageBag) {
		for(PackageEvent packageEvent : packageBag.getPackageEvents()) {
			if(packageEvent != null)
				packageEventService.delete(packageEvent.getId());
		}
	}
	
//	@Transactional(readOnly = false, rollbackFor=PackageEditException.class)
//	public void updateMaintainer() {
//		
//	}
	
	@Transactional(readOnly = false, rollbackFor={PackageEditException.class})
	public void refreshMaintainer(Package packageBag, User updater) throws PackageEditException {
		User bestMaintainer = chooseBestMaintainer(packageBag);
		if(bestMaintainer.getId() != packageBag.getUser().getId()) {
			Event updateEvent;
			try {
				updateEvent = eventService.getUpdateEvent();
			}
			catch(EventNotFound e) {
				logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
				throw new PackageEditException(messageSource, locale, packageBag);
			}
			
			PackageEvent updateMaintainerEvent = 
					new PackageEvent(0, DateProvider.now(), updater, packageBag, updateEvent, "maintainer", "" 
							+ packageBag.getUser().getId(), "" + bestMaintainer.getId(), DateProvider.now());
			packageBag.setUser(bestMaintainer);
			update(updateMaintainerEvent, packageBag, updater);
		}
	}
	
	@Transactional(readOnly = false, rollbackFor={PackageEditException.class})
	public void updateAndRefreshMaintainer(PackageEvent updatePackageEvent, Package packageBag, User updater) throws PackageEditException {
		refreshMaintainer(packageBag, updater);
		update(updatePackageEvent, packageBag, updater);
	}
	
	private void update(PackageEvent updateEvent, Package packageBag, User updater) throws PackageEditException
	{
		packageEventService.create(updateEvent);
		try {
			repositoryService.boostRepositoryVersion(packageBag.getRepository(), updater);
			if(packageBag.getRepository().isPublished())
				repositoryService.publishRepository(packageBag.getRepository(), updater);
		}
		catch(RepositoryPublishException | RepositoryEditException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			throw new PackageEditException(messageSource, locale, packageBag);
		}

	}
	
	public List<Package> findByRepository(Repository repository) {
		return packageRepository.findByRepositoryAndDeleted(repository, false);
	}
	
	public List<Package> findByRepositoryAndMaintainer(Repository repository, User maintainer) {
		return packageRepository.findByRepositoryAndUserAndDeleted(repository, maintainer, false);
	}

	public List<Package> findByRepositoryAndActive(Repository repository, boolean active) {
		return packageRepository.findByRepositoryAndActiveAndDeleted(repository, active, false, Sort.by(new Order(Direction.ASC, "name")));
	}
	
	public List<Package> findByRepositoryAndActiveAndNewest(Repository repository, boolean active) {
		List<Package> packages = new ArrayList<Package>();
		List<Package> allPackagesInRepository = findByRepositoryAndActive(repository, true);
		Map<String, Package> packagesDistinguishedByNames = new TreeMap<>();
		
		for(Package packageBag : allPackagesInRepository) {
			if(!packagesDistinguishedByNames.keySet().contains(packageBag.getName())) {
				packagesDistinguishedByNames.put(packageBag.getName(), packageBag);
			}
			else {
				if(packagesDistinguishedByNames.get(packageBag.getName()).compareTo(packageBag) < 0) {
					packagesDistinguishedByNames.replace(packageBag.getName(), packageBag);
				}
			}
		}
		
		for(Map.Entry<String, Package> entry : packagesDistinguishedByNames.entrySet()) {
			packages.add(entry.getValue());
		}
		return packages;
	}

	public List<Package> findByNameAndRepository(String name, Repository repository) {
		return packageRepository.findByNameAndRepositoryAndDeleted(name, repository, false);
	}
	
	public List<Package> findByNameAndRepositoryAndActiveByOrderByVersionDesc(String name, Repository repository) {
		List<Package> packages = packageRepository.findByNameAndRepositoryAndActive(name, repository, true);
		packages.sort(new Comparator<Package>() {
			@Override
			public int compare(Package o1, Package o2) {
				try {
					int result = o1.compareTo(o2);
					if (result < 0) return 1;
					else if (result > 0) return -1;
					else return 0;
				}
				catch (IllegalArgumentException e) {
					return 0;
				}
			}
		});
		
		return packages;
	}

	public Package findByNameAndVersionAndRepository(String name, String version, Repository repository) {
		Package packageBag = packageRepository.findByNameAndVersionAndRepositoryAndDeleted(name, version, repository, false);
		
		if(packageBag != null) {
			if(!packageBag.getSubmission().isDeleted())
				return packageBag;
		}
		return null;
//		return packageRepository.findByNameAndVersionAndRepositoryAndDeleted(name, version, repository, false);
//		if (packageBag == null) {
//			return null;
//		}
//		else if(!packageBag.getSubmission().isDeleted()) {
//			return packageBag;
//		} else {
//			return null;
//		}

	}
	
	public User chooseBestMaintainer(Package packageBag) throws PackageEditException {
		String name = packageBag.getName();
		Repository repository = packageBag.getRepository();
		
		try {
			PackageMaintainer packageMaintainer = packageMaintainerService.findByPackageAndRepositoryAndNotDeleted(name, repository);
			if(packageMaintainer != null)
				return packageMaintainer.getUser();
			else {
				List<RepositoryMaintainer> repositoryMaintainers = repositoryMaintainerService.findByRepository(repository);
				
				if(repositoryMaintainers.size() < 1)
					return userService.findFirstAdmin();
				else
					return repositoryMaintainers.get(0).getUser();
			}
		} catch(AdminNotFound e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage());
			throw new PackageEditException(messageSource, locale, packageBag);
		}
	}
	
	public Package getHighestVersion(Package packageBag) {
		for(Package p : packageBag.getRepository().getNonDeletedPackages()) {
			if(Objects.equals(p.getName(), packageBag.getName()) && p.getId() != packageBag.getId() && isHighestVersion(p))
				return p;
		}
		return null;
	}
	
	public boolean isHighestVersion(Package packageBag) {
		String name = packageBag.getName();
		Repository repository = packageBag.getRepository();
		List<Package> packages = findByNameAndRepository(name, repository);
		
		if(packages.size() > 1) {
			for(Package p : packages) {	
				if (packageBag.compareTo(p) < 0)
					return false;
			}
		}
		return true;
	}
	
	@Transactional(readOnly=false, rollbackFor=SourceFileDeleteException.class)
	public void deleteSource(Package packageBag) throws SourceFileDeleteException {
		packageStorage.deleteSource(packageBag);
		packageBag.setSource(""); //TODO: shouldn't this be handled by some dedicated method?
		//updateSource(packageBag, "", requester);
	}
	
	public HashMap<String, List<String>> findNamesPerRepository() {
		HashMap<String, List<String>> result = new HashMap<String, List<String>>();
		
		for(Repository r : repositoryService.findAll()) {	
			result.put(r.getName(), new ArrayList<String>());
			
			for(Package p : findByRepository(r)) {
				List<String> names = result.get(r.getName());
				if(!names.contains(p.getName()))
					names.add(p.getName());
			}
		}
		return result;
	}

	public void createManuals(Package packageBag) throws ManualCreateException {
		packageStorage.createManuals(packageBag);
	}
	
	public List<Package> findMaintainedBy(User user) {
		ArrayList<Package> packages = new ArrayList<Package>();
		switch(user.getRole().getName())
		{
			case "admin":
				packages.addAll(findAll());
				break;
			case "repositorymaintainer":
				user = userService.findById(user.getId());
				for(RepositoryMaintainer repositoryMaintainer : user.getRepositoryMaintainers())
				{
					
					if(!repositoryMaintainer.isDeleted())
					{
						for(Package p : repositoryMaintainer.getRepository().getNonDeletedPackages())
						{
							packages.add(p);								
						}
					}
				}
				break;
			case "packagemaintainer":
				user = userService.findById(user.getId());
				for(PackageMaintainer packageMaintainer : user.getPackageMaintainers())
				{
					if(!packageMaintainer.isDeleted())
						packages.addAll(findByNameAndRepository(packageMaintainer.getPackage(), packageMaintainer.getRepository()));
				}
				break;
		}
		return packages;
	}
}

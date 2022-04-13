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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.api.v2.dto.RPackageDto;
import eu.openanalytics.rdepot.comparator.PackageComparator;
import eu.openanalytics.rdepot.exception.AdminNotFound;
import eu.openanalytics.rdepot.exception.EventNotFound;
import eu.openanalytics.rdepot.exception.GetFileInBytesException;
import eu.openanalytics.rdepot.exception.GetReferenceManualException;
import eu.openanalytics.rdepot.exception.ManualCreateException;
import eu.openanalytics.rdepot.exception.Md5SumCalculationException;
import eu.openanalytics.rdepot.exception.MovePackageSourceException;
import eu.openanalytics.rdepot.exception.PackageActivateException;
import eu.openanalytics.rdepot.exception.PackageCreateException;
import eu.openanalytics.rdepot.exception.PackageDeactivateException;
import eu.openanalytics.rdepot.exception.PackageDeleteException;
import eu.openanalytics.rdepot.exception.PackageEditException;
import eu.openanalytics.rdepot.exception.PackageException;
import eu.openanalytics.rdepot.exception.PackageSourceNotFoundException;
import eu.openanalytics.rdepot.exception.PackageStorageException;
import eu.openanalytics.rdepot.exception.ReadPackageVignetteException;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.exception.RepositoryPublishException;
import eu.openanalytics.rdepot.exception.SourceFileDeleteException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageEvent;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.model.Vignette;
import eu.openanalytics.rdepot.repository.PackageRepository;
import eu.openanalytics.rdepot.storage.PackageStorage;
import eu.openanalytics.rdepot.time.DateProvider;
import eu.openanalytics.rdepot.warning.PackageAlreadyActivatedWarning;
import eu.openanalytics.rdepot.warning.PackageAlreadyDeactivatedWarning;
import eu.openanalytics.rdepot.warning.PackageAlreadyDeletedWarning;
import eu.openanalytics.rdepot.warning.PackageWarning;

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
					delete(checkSameVersion, creator);
				} catch (PackageException e) {
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
		return packageRepository.findById(id).orElse(null); //TODO: Soon we should move to Optional annotation for all services
	}
	
	public List<Package> findByDeleted(boolean deleted) {
		return packageRepository.findByDeleted(deleted, Sort.by(new Order(Direction.ASC, "name")));
	}
	
	public Page<Package> findByDeleted(boolean deleted, Pageable pageable) {
		return packageRepository.findByDeleted(deleted, pageable);
	}
	
	public Package findByIdAndDeleted(int id, boolean deleted) {
		return packageRepository.findByIdAndDeleted(id, deleted);
	}
	
	@Transactional(readOnly=false, rollbackFor={PackageDeleteException.class})
	public void delete(Package deletedPackage, User deleter) 
			throws PackageDeleteException, PackageAlreadyDeletedWarning {
		try {
			if (deletedPackage.isDeleted()) {
				PackageAlreadyDeletedWarning warning = new PackageAlreadyDeletedWarning(messageSource, locale, deletedPackage);
				logger.warn(warning.getMessage(), warning);
				throw warning;
			}
			
			Event deleteEvent = eventService.getDeleteEvent();
			PackageEvent deletePackageEvent = new PackageEvent(0, DateProvider.now(), deleter, 
					deletedPackage, deleteEvent, "delete", "false", "true", DateProvider.now());
			
			deletedPackage.setActive(false);
			deletedPackage.setDeleted(true);
			update(deletePackageEvent, deletedPackage, deleter);
			
//			Submission submission = deletedPackage.getSubmission(); 
			
//			if(submission != null && !submission.isDeleted())
//				submissionService.deleteSubmission(deletedPackage.getSubmission(), deleter);
		} 
		catch (PackageEditException | EventNotFound e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new PackageDeleteException(messageSource, locale, deletedPackage);
		}
	}
	
	@Transactional(readOnly=false, rollbackFor=PackageDeleteException.class)
	public void shiftDelete(Package deletedPackage) throws PackageDeleteException {
		shiftDeleteAdditionalActions(deletedPackage, true);
		packageRepository.delete(deletedPackage);
	}
	
	@Transactional(readOnly=false, rollbackFor=PackageDeleteException.class)
	public void shiftDeleteAdditionalActions(Package deletedPackage, boolean deleteSubmission) throws PackageDeleteException {
		try {
			deleteSource(deletedPackage);
			deletePackageEvents(deletedPackage);
			Submission submission = deletedPackage.getSubmission();
			
			if(submission != null && deleteSubmission) {
				submissionService.shiftDeleteAdditionalActions(submission, false);
			}
		} 
		catch (PackageStorageException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new PackageDeleteException(messageSource, locale, deletedPackage);
		}	
	}
	
//	@Transactional(readOnly=false, rollbackFor=PackageDeleteException.class)
//	public void shiftDeleteForRejectedSubmission(Package deletedPackage) throws PackageDeleteException {
//		try {
//			Submission submission = deletedPackage.getSubmission();
//			if(submission != null) {
//				submissionService.shiftDelete(deletedPackage.getSubmission());
//			}
//			deleteSource(deletedPackage);
//			deletePackageEvents(deletedPackage);
//			packageRepository.delete(deletedPackage);
//		} 
//		catch (PackageStorageException | SubmissionDeleteException e) {
//			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
//			throw new PackageDeleteException(messageSource, locale, deletedPackage);
//		}	
//	}
	
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
		//TODO: when submission is shift deleted, the package 'gets orphaned', we decided not to treat it like a correct one
		//TODO: in the future we would need a way to access such orphaned packages, maybe more advanced control panel for administrator
		List<Package> allPackages = packageRepository.findNonDeletedByAcceptedSubmission();		
		Collections.sort(allPackages, new PackageComparator());
		return allPackages;
	}
	
	public Page<Package> findAll(Pageable pageable) {
		Page<Package> allPackages = packageRepository.findNonDeletedByAcceptedSubmission(pageable);				
		return allPackages;
	}
	
	public List<Package> findAllByRepositoryName(String repositoryName) {
		if(Objects.isNull(repositoryName))
			return findAll();		
		List<Package> allPackages = packageRepository.findNonDeletedByRepositoryNameAndAcceptedSubmission(repositoryName);
		Collections.sort(allPackages, new PackageComparator());
		return allPackages;
	}
	
	public Page<Package> findAllByRepositoryName(String repositoryName, Pageable pageable) {
		if(Objects.isNull(repositoryName))
			return findAll(pageable);		
		else
			return packageRepository.findByRepositoryNameAndAcceptedSubmission(repositoryName, pageable);		
	}
	
	public Page<Package> findAllByRepositoryAndDeleted(String repositoryName, Boolean deleted, Pageable pageable) {
		if(deleted)
			return packageRepository.findDeletedByRepositoryNameAndAcceptedSubmission(repositoryName, pageable);
		else
			return findAllByRepositoryName(repositoryName, pageable);
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
	
	public byte[] readVignette(Package packageBag, String fileName) throws ReadPackageVignetteException, FileNotFoundException {
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
	
	public Package findByNameAndRepositoryAndNewest(String name, Repository repository) {
		List<Package> packages = packageRepository.findByNameAndRepositoryAndDeleted(name, repository, false);
		packages.sort(new PackageComparator());
		
		if(packages.size() > 0)
			return packages.get(0);
		else
			return null; //TODO: We should move to Optional instead of returning null
	}

	public Package findByNameAndVersionAndRepository(String name, String version, Repository repository) {
		Collection<String> versions = generateVariantsOfVersion(version);
		
		Package packageBag = packageRepository.findByNameAndRepositoryAndDeletedAndVersionIn(name, repository, false, versions);
		
		if(packageBag != null) {
			if(packageBag.getSubmission() != null && !packageBag.getSubmission().isDeleted())
				return packageBag;
		}
		return null;
	}
	
	private Collection<String> generateVariantsOfVersion(String version) {
		List<String> variants = new ArrayList<>();
		String[] splitted = version.split("-|\\.");
		int length = splitted.length;
		
		int numberOfVariations = 1 << (length - 1);
		
		//0 in schema means dot
		//1 in schema means hyphen
		for(int i = 0; i < numberOfVariations; i++) {
			String schema = String.format("%" + (length - 1) + "s", Integer.toBinaryString(i)).replace(' ', '0');
			String newVersion = "";
			for(int j = 0; j < length - 1; j++) {
				newVersion += splitted[j];
				char separator = schema.charAt(j);
				if(separator == '0') {
					newVersion += ".";
				} else {
					newVersion += "-";
				}
			}
			newVersion += splitted[length - 1];
			variants.add(newVersion);
		}
		
		return variants;
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
		packageStorage.createManual(packageBag);
	}
	
	public List<Package> findMaintainedBy(User user) {
		List<Package> packages = new ArrayList<>();
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
		
	//TODO: test it
	public byte[] getPackageInBytes(Package packageBag) throws GetFileInBytesException, FileNotFoundException {
		byte[] packageBytes = null;
		try {
			packageBytes = packageStorage.getPackageInBytes(packageBag);
		} catch (GetFileInBytesException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw e;
		}
		
		return packageBytes;
	}
	
	public byte[] getReferenceManualInBytes(Package packageBag) throws GetReferenceManualException, FileNotFoundException {
		byte[] manualBytes = null;
		
		try {
			manualBytes = packageStorage.getReferenceManualFileInBytes(packageBag);
		} catch (ManualCreateException | GetFileInBytesException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new GetReferenceManualException(messageSource, locale, packageBag);
		}
		
		return manualBytes;
	}

	public List<Vignette> getAvailableVignettes(Package packageBag) {
		List<Vignette> vignettes = new ArrayList<>();
		List<File> vignetteFiles = packageStorage.getVignetteFiles(packageBag);
		
		for(File vignetteFile : vignetteFiles) {
			if(FileNameUtils.getExtension(vignetteFile.getName()).equals("html")) {
				try {
					Document htmlDoc = Jsoup.parse(vignetteFile, "UTF-8");
					
					vignettes.add(new Vignette(
							htmlDoc.title(), 
							vignetteFile.getName()
						));
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				vignettes.add(new Vignette(
						FileNameUtils.getBaseName(vignetteFile.getName()), 
						vignetteFile.getName()
					));
			}
		}
		
		return vignettes;
	}

	public Optional<String> getReferenceManualFilename(Package packageBag) {
		return packageStorage.getReferenceManualFilename(packageBag);
	}

	@Transactional(readOnly = false, rollbackFor = PackageException.class)
	public Package evaluateAndUpdate(RPackageDto packageDto, User requester) throws PackageException {
		Package packageEntity = findById(packageDto.getEntity().getId());
		
		try {
			if(packageDto.getActive() != packageEntity.isActive()) {
				if(!packageDto.getActive()) {
					deactivatePackage(packageEntity, requester);
				} else {
					activatePackage(packageEntity, requester);
				}
			}
			if(packageDto.getDeleted() != packageEntity.isDeleted()) {
				if(packageDto.getDeleted()) {
					delete(packageEntity, requester);
				} else {
					throw new NotImplementedException();
				}
			}
			if(packageDto.getSource() != packageEntity.getSource()) {
				updateSource(packageEntity, packageDto.getSource(), requester);
			}
		} catch (PackageWarning w) {
			logger.warn(w.getClass().getName() + ": " + w.getMessage(), w);
		}
		
		return packageEntity;
	}

	public Page<Package> findAllEvenDeleted(Pageable pageable) {
		return packageRepository.findAllAcceptedSubmissions(pageable);
	}

	public Page<Package> findAllByDeleted(Boolean deleted, Pageable pageable) {
		return packageRepository.findByDeleted(deleted, pageable);
	}

	public void moveSourceToTrashDirectory(Package packageBag, User requester) 
			throws PackageSourceNotFoundException, MovePackageSourceException, PackageEditException {
		String newSource = packageStorage.moveToTrashDirectory(packageBag).getAbsolutePath();
		updateSource(packageBag, newSource, requester);
	}

}

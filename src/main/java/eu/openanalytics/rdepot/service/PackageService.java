/**
 * R Depot
 *
 * Copyright (C) 2012-2018 Open Analytics NV
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import eu.openanalytics.rdepot.exception.ManualCreateException;
import eu.openanalytics.rdepot.exception.PackageDeleteException;
import eu.openanalytics.rdepot.exception.PackageEditException;
import eu.openanalytics.rdepot.exception.PackageEventNotFound;
import eu.openanalytics.rdepot.exception.PackageNotFound;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.exception.SourceFileDeleteException;
import eu.openanalytics.rdepot.exception.SubmissionDeleteException;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageEvent;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.PackageRepository;

@Service
@Transactional(readOnly = true)
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class PackageService
{	
	@Resource
	private PackageRepository packageRepository;
	
	@Resource
	private RoleService roleService;
	
	@Resource
	private FileService fileService;
	
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

	@Transactional(readOnly = false)
	public Package create(Package packageBag, User creator) 
	{
		Package createdPackage = packageBag;
		createdPackage = packageRepository.save(createdPackage);
		
		Event createEvent = eventService.findByValue("create");
		packageEventService.create(createEvent, creator, createdPackage);
		
		return createdPackage;
	}
	
	@Transactional(readOnly = false)
	public Package create(Package packageBag, User creator, boolean replace) throws PackageDeleteException, RepositoryEditException 
	{
		if (replace)
		{
			Package checkSameVersion = findByNameAndVersionAndRepository(packageBag.getName(), packageBag.getVersion(), packageBag.getRepository());
			if (checkSameVersion != null)
			{
				delete(checkSameVersion.getId(), creator);
			}
		}
		return create(packageBag, creator);
	}
	
	public Package findById(int id) 
	{
		return packageRepository.findByIdAndDeleted(id, false);
	}
	
	public Package findByIdEvenDeleted(int id) 
	{
		return packageRepository.getOne(id);
	}
	
	public List<Package> findByDeleted(boolean deleted) 
	{
		return packageRepository.findByDeleted(deleted, Sort.by(new Order(Direction.ASC, "name")));
	}
	
	public Package findByIdAndDeleted(int id, boolean deleted) 
	{
		return packageRepository.findByIdAndDeleted(id, deleted);
	}
	
	@Transactional(readOnly=false, rollbackFor={PackageDeleteException.class})
	public Package delete(int id, User deleter) throws PackageDeleteException, RepositoryEditException
	{
		Package deletedPackage = packageRepository.findByIdAndDeleted(id, false);
		Event deleteEvent = eventService.findByValue("delete");
		try 
		{
			if (deletedPackage == null)
				throw new PackageNotFound();
			if (deleteEvent == null)
				throw new EventNotFound();
			deleteSubmissions(deletedPackage, deleter);
			deletedPackage.setDeleted(true);
			if(deletedPackage.isActive())
				deactivatePackage(deletedPackage, deleter);
			packageEventService.create(deleteEvent, deleter, deletedPackage);
			return deletedPackage;
		} 
		catch (PackageNotFound | SubmissionDeleteException | PackageEditException | EventNotFound e) 
		{
			throw new PackageDeleteException(e.getMessage());
		}	
	}
	
	@Transactional(readOnly=false, rollbackFor={PackageDeleteException.class})
	public Package shiftDelete(int id) throws PackageDeleteException
	{
		Package deletedPackage = packageRepository.findByIdAndDeleted(id, true);
		
		try 
		{
			if (deletedPackage == null)
				throw new PackageNotFound();
			shiftDeleteSubmissions(deletedPackage);
			deleteSource(deletedPackage);
			deletePackageEvents(deletedPackage);
			packageRepository.delete(deletedPackage);
			return deletedPackage;
		} 
		catch (PackageNotFound	| SourceFileDeleteException | SubmissionDeleteException | PackageEventNotFound e) 
		{
			throw new PackageDeleteException(e.getMessage());
		}	
	}
	
	@Transactional(readOnly = false)
	public void deactivatePackage(Package packageBag, User user) throws PackageEditException, RepositoryEditException
	{
		Package highest = getHighestVersion(packageBag);
		if(highest != null)
		{
			highest.setActive(true);
			update(highest, user);
		}
	}
	
	public String calculateMd5Sum(Package packageBag) throws IOException
	{
		return fileService.calculateMd5Sum(new File(packageBag.getSource()));
	}
	
	public boolean isSameMd5Sum(Package packageBag, String md5sum)
	{
		if(Objects.equals(packageBag.getMd5sum(), md5sum))
			return true;
		return false;
	}
	
	public List<Package> findAll() 
	{
		return packageRepository.findByDeleted(false, Sort.by(new Order(Direction.ASC, "name")));
	}
	
	@Transactional(readOnly = false)
	public void deleteSubmissions(Package packageBag, User deleter) throws SubmissionDeleteException
	{
		for(Submission submission : packageBag.getSubmissions())
		{
			if(submission != null && !submission.isDeleted())
					submissionService.delete(submission.getId(), deleter);
		}
	}
	
	public byte[] readVignette(int id, String fileName)
	{
		Package packageBag = findById(id);
		byte[] bytes = null;
		if(packageBag != null)
		{
			bytes = packageBag.readVignette(fileName);
		}
		return bytes;
	}
	
	@Transactional(readOnly = false)
	public void shiftDeleteSubmissions(Package packageBag) throws SubmissionDeleteException
	{
		for(Submission submission : packageBag.getSubmissions())
		{
			if(submission != null)
					submissionService.shiftDelete(submission.getId());
		}
	}
	
	@Transactional(readOnly = false)
	public void deletePackageEvents(Package packageBag) throws PackageEventNotFound
	{
		for(PackageEvent packageEvent : packageBag.getPackageEvents())
		{
			if(packageEvent != null)
				packageEventService.delete(packageEvent.getId());
		}
	}
	
	@Transactional(readOnly = false, rollbackFor={PackageEditException.class})
	public Package update(Package packageBag, User updater) throws PackageEditException, RepositoryEditException
	{
		Package updatedPackage = packageRepository.findByIdAndDeleted(packageBag.getId(), false);
		List<PackageEvent> events = new ArrayList<PackageEvent>();
		Event updateEvent = eventService.findByValue("update");
		
		try
		{
			if (updatedPackage == null)
				throw new PackageNotFound();
			
			if(updateEvent == null)
				throw new EventNotFound();
			
			packageBag = chooseBestMaintainer(packageBag);
			
			if(!Objects.equals(updatedPackage.getVersion(), packageBag.getVersion()))
			{
				events.add(new PackageEvent(0, new Date(), updater, packageBag, updateEvent, "version", updatedPackage.getVersion(), packageBag.getVersion(), new Date()));
				updatedPackage.setVersion(packageBag.getVersion());
			}
			if(updatedPackage.getRepository().getId() != packageBag.getRepository().getId())
			{
				events.add(new PackageEvent(0, new Date(), updater, packageBag, updateEvent, "repository", "" + updatedPackage.getRepository().getId(), "" + packageBag.getRepository().getId(), new Date()));
				updatedPackage.setRepository(packageBag.getRepository());
			}
			if(updatedPackage.getUser().getId() != packageBag.getUser().getId())
			{
				events.add(new PackageEvent(0, new Date(), updater, packageBag, updateEvent, "maintainer", "" + updatedPackage.getUser().getId(), "" + packageBag.getUser().getId(), new Date()));
				updatedPackage.setUser(packageBag.getUser());
			}
			if(!Objects.equals(updatedPackage.getName(), packageBag.getName()))
			{
				events.add(new PackageEvent(0, new Date(), updater, packageBag, updateEvent, "name", updatedPackage.getName(), packageBag.getName(), new Date()));
				updatedPackage.setName(packageBag.getName());
			}
			if(!Objects.equals(updatedPackage.getDescription(), packageBag.getDescription()))
			{
				events.add(new PackageEvent(0, new Date(), updater, packageBag, updateEvent, "description", updatedPackage.getDescription(), packageBag.getDescription(), new Date()));
				updatedPackage.setDescription(packageBag.getDescription());
			}
			if(!Objects.equals(updatedPackage.getAuthor(), packageBag.getAuthor()))
			{
				events.add(new PackageEvent(0, new Date(), updater, packageBag, updateEvent, "author", updatedPackage.getAuthor(), packageBag.getAuthor(), new Date()));
				updatedPackage.setAuthor(packageBag.getAuthor());
			}
			if(!Objects.equals(updatedPackage.getDepends(), packageBag.getDepends()))
			{
				events.add(new PackageEvent(0, new Date(), updater, packageBag, updateEvent, "depends", updatedPackage.getDepends(), packageBag.getDepends(), new Date()));
				updatedPackage.setDepends(packageBag.getDepends());
			}
			if(!Objects.equals(updatedPackage.getImports(), packageBag.getImports()))
			{
				events.add(new PackageEvent(0, new Date(), updater, packageBag, updateEvent, "imports", updatedPackage.getImports(), packageBag.getImports(), new Date()));
				updatedPackage.setImports(packageBag.getImports());
			}
			if(!Objects.equals(updatedPackage.getSuggests(), packageBag.getSuggests()))
			{
				events.add(new PackageEvent(0, new Date(), updater, packageBag, updateEvent, "suggests", updatedPackage.getSuggests(), packageBag.getSuggests(), new Date()));
				updatedPackage.setSuggests(packageBag.getSuggests());
			}
			if(!Objects.equals(updatedPackage.getSystemRequirements(), packageBag.getSystemRequirements()))
			{
				events.add(new PackageEvent(0, new Date(), updater, packageBag, updateEvent, "system requirements", updatedPackage.getSystemRequirements(), packageBag.getSystemRequirements(), new Date()));
				updatedPackage.setSystemRequirements(packageBag.getSystemRequirements());
			}
			if(!Objects.equals(updatedPackage.getLicense(), packageBag.getLicense()))
			{
				events.add(new PackageEvent(0, new Date(), updater, packageBag, updateEvent, "license", updatedPackage.getLicense(), packageBag.getLicense(), new Date()));
				updatedPackage.setLicense(packageBag.getLicense());
			}
			if(!Objects.equals(updatedPackage.getTitle(), packageBag.getTitle()))
			{
				events.add(new PackageEvent(0, new Date(), updater, packageBag, updateEvent, "title", updatedPackage.getTitle(), packageBag.getTitle(), new Date()));
				updatedPackage.setTitle(packageBag.getTitle());
			}
			if(!Objects.equals(updatedPackage.getUrl(), packageBag.getUrl()))
			{
				events.add(new PackageEvent(0, new Date(), updater, packageBag, updateEvent, "URL", updatedPackage.getUrl(), packageBag.getUrl(), new Date()));
				updatedPackage.setUrl(packageBag.getUrl());
			}
			if(!Objects.equals(updatedPackage.getSource(), packageBag.getSource()))
			{
				events.add(new PackageEvent(0, new Date(), updater, packageBag, updateEvent, "source", updatedPackage.getSource(), packageBag.getSource(), new Date()));
				updatedPackage.setSource(packageBag.getSource());
			}
			if(!Objects.equals(updatedPackage.getMd5sum(), packageBag.getMd5sum()))
			{
				events.add(new PackageEvent(0, new Date(), updater, packageBag, updateEvent, "md5sum", updatedPackage.getMd5sum(), packageBag.getMd5sum(), new Date()));
				updatedPackage.setMd5sum(packageBag.getMd5sum());
			}
			if(updatedPackage.isActive() != packageBag.isActive())
			{
				events.add(new PackageEvent(0, new Date(), updater, packageBag, updateEvent, "active", "" + updatedPackage.isActive(), "" + packageBag.isActive(), new Date()));
				if(packageBag.isActive())
					activatePackage(packageBag, updater);
				else
					deactivatePackage(packageBag, updater);
				updatedPackage.setActive(packageBag.isActive());
			}
			
			for(PackageEvent pEvent : events)
			{
				pEvent = packageEventService.create(pEvent);
			}
			
			repositoryService.boostRepositoryVersion(updatedPackage.getRepository(), updater);
			repositoryService.publishRepository(updatedPackage.getRepository(), updater);
			
			return updatedPackage;
		}
		catch(PackageNotFound | EventNotFound e)
		{
			throw new PackageEditException(e.getMessage());
		}
		
	}

	public List<Package> findByRepository(Repository repository) 
	{
		return packageRepository.findByRepositoryAndDeleted(repository, false);
	}
	
	public List<Package> findByRepositoryAndMaintainer(Repository repository, User maintainer) 
	{
		return packageRepository.findByRepositoryAndUserAndDeleted(repository, maintainer, false);
	}

	public List<Package> findByRepositoryAndActive(Repository repository, boolean active) 
	{
		return packageRepository.findByRepositoryAndActiveAndDeleted(repository, active, false);
	}

	public List<Package> findByNameAndRepository(String name, Repository repository) 
	{
		return packageRepository.findByNameAndRepositoryAndDeleted(name, repository, false);
	}

	public Package findByNameAndVersionAndRepository(String name, String version, Repository repository) 
	{
		return packageRepository.findByNameAndVersionAndRepositoryAndDeleted(name, version, repository, false);
	}
	
	public Package chooseBestMaintainer(Package packageBag) throws PackageEditException
	{
		String name = packageBag.getName();
		Repository repository = packageBag.getRepository();
		try
		{
			PackageMaintainer packageMaintainer = packageMaintainerService.findByPackageAndRepository(name, repository);
			if(packageMaintainer != null)
				packageBag.setUser(packageMaintainer.getUser());
			else
			{
				List<RepositoryMaintainer> repositoryMaintainers = repositoryMaintainerService.findByRepository(repository);
				if(repositoryMaintainers.size() < 1)
				{
					packageBag.setUser(userService.findFirstAdmin());
				}
				else
				{
					packageBag.setUser(repositoryMaintainers.get(0).getUser());
				}
			}
			return packageBag;
		}
		catch(AdminNotFound e)
		{
			throw new PackageEditException(e.getMessage());
		}
	}
	
	@Transactional(readOnly = false)
	public void activatePackage(Package packageBag, User user) throws PackageEditException, RepositoryEditException
	{
		String name = packageBag.getName();
		Repository repository = packageBag.getRepository();
		List<Package> packages = findByNameAndRepository(name, repository);
		if(packages.size() > 0)
		{
			for(Package p : packages)
			{
				if(p.isActive() && p.getId() != packageBag.getId())
				{
					p.setActive(false);
					update(p, user);
				}
			}
		}
	}
	
	public Package getHighestVersion(Package packageBag)
	{
		for(Package p : packageBag.getRepository().getPackages())
		{
			if(Objects.equals(p.getName(), packageBag.getName()) && p.getId() != packageBag.getId() && isHighestVersion(p) && !p.isDeleted())
				return p;
				
		}
		return null;
	}
	
	public boolean isHighestVersion(Package packageBag)
	{
		String name = packageBag.getName();
		Repository repository = packageBag.getRepository();
		String version = packageBag.getVersion();
		List<Package> packages = findByNameAndRepository(name, repository);
		if(packages.size() > 1)
		{
			int beforeDot = Integer.parseInt(version.split("\\.")[0]);
			int afterDot = Integer.parseInt(version.split("\\-")[0].split("\\.")[1]);
			int afterHyphen = Integer.parseInt(version.split("\\-")[1]);
			for(Package p : packages)
			{	
				int beforeDot2 = Integer.parseInt(p.getVersion().split("\\.")[0]);
				int afterDot2 = Integer.parseInt(p.getVersion().split("\\-")[0].split("\\.")[1]);
				int afterHyphen2 = Integer.parseInt(p.getVersion().split("\\-")[1]);
				if(beforeDot < beforeDot2)
				{
					return false;
				}
				else if(beforeDot == beforeDot2)
				{
					if(afterDot < afterDot2)
					{
						return false;
					}
					else if(afterDot == afterDot2)
					{
						if(afterHyphen < afterHyphen2)
						{
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	public void deleteSource(Package packageBag) throws SourceFileDeleteException
	{
		File targzfile = new File(packageBag.getSource());
		if(targzfile != null && targzfile.exists() && targzfile.getParentFile() != null && targzfile.getParentFile().exists())
		{
			Process p;
	        try 
	        {
				p = Runtime.getRuntime().exec("rm -rf " + targzfile.getParent());
				p.waitFor();
		        if(p.exitValue() != 0)
		        	throw new SourceFileDeleteException("file.exception.delete");
		        p.destroy();
			} 
	        catch (IOException e) 
	        {
	        	throw new SourceFileDeleteException("file.exception.command");
			}
	        catch (InterruptedException e) 
	        {
	        	throw new SourceFileDeleteException("file.exception.process");
			}
		}
	}
	
	public HashMap<String, List<String>> findNamesPerRepository()
	{
		HashMap<String, List<String>> result = new HashMap<String, List<String>>();
		for(Repository r : repositoryService.findAll())
		{	
			result.put(r.getName(), new ArrayList<String>());
			for(Package p : findByRepository(r))
			{
				List<String> names = result.get(r.getName());
				if(!names.contains(p.getName()))
					names.add(p.getName());
			}
		}
		return result;
	}

	public void createManuals(Package packageBag) throws ManualCreateException 
	{
		File targzfile = new File(packageBag.getSource());
		
		if(targzfile != null && targzfile.exists() && targzfile.getParentFile() != null && targzfile.getParentFile().exists())
		{
			String name = packageBag.getName();
			File manualPdf = new File(targzfile.getParent(), name + "/" + name + ".pdf");
			if(manualPdf != null && manualPdf.getParentFile() != null && manualPdf.getParentFile().exists() && !manualPdf.exists())
			{
				Process p;
				try 
		        {
					p = Runtime.getRuntime().exec("R CMD Rd2pdf --no-preview --title=" + name + " --output=" + name + ".pdf" + " .",
							null, manualPdf.getParentFile());
					p.waitFor();
			        if(p.exitValue() != 0)
			        	throw new ManualCreateException("manual.exception.create");
			        p.destroy();
				} 
		        catch (IOException e) 
		        {
		        	throw new ManualCreateException("manual.exception.command");
				}
		        catch (InterruptedException e) 
		        {
		        	throw new ManualCreateException("manual.exception.process");
				}
			}
	        
		}
	}
	
	public List<Package> findMaintainedBy(User user) 
	{
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
						for(Package p : repositoryMaintainer.getRepository().getPackages())
						{
							if(!p.isDeleted())
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

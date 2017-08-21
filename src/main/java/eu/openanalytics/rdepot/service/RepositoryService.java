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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import eu.openanalytics.rdepot.exception.EventNotFound;
import eu.openanalytics.rdepot.exception.PackageDeleteException;
import eu.openanalytics.rdepot.exception.PackageMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.RepositoryDeleteException;
import eu.openanalytics.rdepot.exception.RepositoryEditException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerDeleteException;
import eu.openanalytics.rdepot.exception.RepositoryMaintainerNotFound;
import eu.openanalytics.rdepot.exception.RepositoryNotFound;
import eu.openanalytics.rdepot.model.Event;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryEvent;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.repository.RepositoryRepository;
import eu.openanalytics.rdepot.repository.RoleRepository;

@Service
@Transactional(readOnly = true)
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class RepositoryService
{	
	private String separator =  FileSystems.getDefault().getSeparator();

	
	@Resource
	private RepositoryRepository repositoryRepository;
	
	@Resource(name="repositoryGenerationDirectory")
	private File repositoryGenerationDirectory;
	
	@Resource(name="packageUploadDirectory")
	private File packageUploadDirectory;
	
	@Resource
	private RoleRepository roleRepository;
	
	@Resource
	private RepositoryMaintainerService repositoryMaintainerService;
	
	@Resource
	private PackageMaintainerService packageMaintainerService;
	
	@Resource
	private PackageService packageService;
	
	@Resource
	private RepositoryEventService repositoryEventService;
	
	@Resource
	private EventService eventService;
	
	@Resource
	private FileService fileService;

	@Transactional(readOnly = false)
	public Repository create(Repository repository, User creator) 
	{
		Repository createdRepository = repository;
		createdRepository = repositoryRepository.save(createdRepository);
		Event createEvent = eventService.findByValue("create");
		repositoryEventService.create(createEvent, creator, createdRepository);
		return createdRepository;
	}
	
	public Repository findById(int id) 
	{
		return repositoryRepository.findByIdAndDeleted(id, false);
	}
	
	public Repository findByIdAndDeleted(int id, boolean deleted) 
	{
		return repositoryRepository.findByIdAndDeleted(id, deleted);
	}
	
	@Transactional(readOnly = false, rollbackFor={RepositoryDeleteException.class})
	public Repository delete(int id, User deleter) throws RepositoryDeleteException 
	{
		Repository deletedRepository = repositoryRepository.findByIdAndDeleted(id, false);
		Event deleteEvent = eventService.findByValue("delete");
		try
		{
			if (deletedRepository == null)
				throw new RepositoryNotFound();
			if (deleteEvent == null)
				throw new EventNotFound();
			
			deleteRepositoryMaintainers(deletedRepository, deleter);
			deletePackageMaintainers(deletedRepository, deleter);
			deletePackages(deletedRepository, deleter);
			deletedRepository.setDeleted(true);
			unpublishRepository(deletedRepository, deleter);
			
			repositoryEventService.create(deleteEvent, deleter, deletedRepository);
			return deletedRepository;
		}
		catch(PackageDeleteException | RepositoryNotFound | RepositoryEditException | PackageMaintainerDeleteException | RepositoryMaintainerDeleteException | EventNotFound e)
		{
			throw new RepositoryDeleteException(e.getMessage());
		}
	}
	
	@Transactional(readOnly = false)
	public void unpublishRepository(Repository repository, User updater) throws RepositoryEditException
	{
		File parent = new File(repositoryGenerationDirectory + separator + repository.getId());
		File current = new File(repositoryGenerationDirectory + separator + repository.getId() + separator + "current");
		try 
		{
			if(FileUtils.directoryContains(parent, current))
			{
				FileUtils.forceDelete(new File(repositoryGenerationDirectory + separator + repository.getId() + separator + "current"));
				repository.setPublished(false);
				update(repository, updater);
			}
		} 
		catch (IOException e) 
		{
			throw new RepositoryEditException(e.getMessage());
		}
	}
	
	@Transactional(readOnly = false, rollbackFor={RepositoryDeleteException.class})
	public Repository shiftDelete(int id) throws RepositoryDeleteException 
	{
		Repository deletedRepository = repositoryRepository.findByIdAndDeleted(id, true);
		
		try
		{
			if (deletedRepository == null)
				throw new RepositoryNotFound();
			
			shiftDeleteRepositoryMaintainers(deletedRepository);
			shiftDeletePackageMaintainers(deletedRepository);
			shiftDeletePackages(deletedRepository);
			for(RepositoryEvent event : deletedRepository.getRepositoryEvents())
				repositoryEventService.delete(event.getId());
			repositoryRepository.delete(deletedRepository);
			FileUtils.forceDelete(new File(packageUploadDirectory + separator + "repositories" + separator + id));
			FileUtils.forceDelete(new File(repositoryGenerationDirectory + separator + id));
			return deletedRepository;
		}
		catch(PackageDeleteException | RepositoryNotFound | PackageMaintainerDeleteException | RepositoryMaintainerNotFound | IOException e)
		{
			throw new RepositoryDeleteException(e.getMessage());
		}
	}

	public List<Repository> findAll() 
	{
		return repositoryRepository.findByDeleted(false, new Sort(new Order(Direction.ASC, "name")));
	}
	
	public List<Repository> findByDeleted(boolean deleted) 
	{
		return repositoryRepository.findByDeleted(deleted, new Sort(new Order(Direction.ASC, "name")));
	}
	
	public List<Repository> findMaintainedBy(User user) 
	{
		switch(user.getRole().getName())
		{
			case "admin":
				return findAll();
			case "repositorymaintainer":
				List<Repository> repositories = new ArrayList<Repository>();
				for(RepositoryMaintainer repositoryMaintainer : user.getRepositoryMaintainers())
				{
					if(!repositoryMaintainer.getRepository().isDeleted())
						repositories.add(repositoryMaintainer.getRepository());
				}
				return repositories;
		}
		return new ArrayList<Repository>();
	}
	
	@Transactional(readOnly = false)
	public void deleteRepositoryMaintainers(Repository repository, User deleter) throws RepositoryMaintainerDeleteException
	{
		for(RepositoryMaintainer repositoryMaintainer : repository.getRepositoryMaintainers())
		{
			if(!repositoryMaintainer.isDeleted())
				repositoryMaintainerService.delete(repositoryMaintainer.getId(), deleter);
		}
	}
	
	@Transactional(readOnly = false)
	public void deletePackageMaintainers(Repository repository, User deleter) throws PackageMaintainerDeleteException
	{
		for(PackageMaintainer packageMaintainer : repository.getPackageMaintainers())
		{
			if(!packageMaintainer.isDeleted())
				packageMaintainerService.delete(packageMaintainer.getId(), deleter);
		}
	}
	
	@Transactional(readOnly = false)
	public void deletePackages(Repository repository, User deleter) throws PackageDeleteException, RepositoryEditException
	{
		for(Package p : repository.getPackages())
		{
			if(!p.isDeleted())
				packageService.delete(p.getId(), deleter);
		}
	}
	
	@Transactional(readOnly = false)
	public void shiftDeleteRepositoryMaintainers(Repository repository) throws RepositoryMaintainerNotFound
	{
		for(RepositoryMaintainer repositoryMaintainer : repository.getRepositoryMaintainers())
		{
			repositoryMaintainerService.shiftDelete(repositoryMaintainer.getId());
		}
	}
	
	@Transactional(readOnly = false)
	public void shiftDeletePackageMaintainers(Repository repository) throws PackageMaintainerDeleteException
	{
		for(PackageMaintainer packageMaintainer : repository.getPackageMaintainers())
		{
			packageMaintainerService.shiftDelete(packageMaintainer.getId());
		}
	}
	
	@Transactional(readOnly = false)
	public void shiftDeletePackages(Repository repository) throws PackageDeleteException
	{
		for(Package p :  repository.getPackages())
		{
			packageService.shiftDelete(p.getId());
		}
	}

	@Transactional(readOnly=false, rollbackFor=RepositoryNotFound.class)
	public Repository update(Repository repository, User updater) throws RepositoryEditException
	{
		Repository updatedRepository = repositoryRepository.findByIdAndDeleted(repository.getId(), false);
		List<RepositoryEvent> events = new ArrayList<RepositoryEvent>();
		Event updateEvent = eventService.findByValue("update");
		
		try
		{
			if (updatedRepository == null)
				throw new RepositoryNotFound();
			
			if(updateEvent == null)
				throw new EventNotFound();
			
			if(updatedRepository.getVersion() != repository.getVersion())
			{
				events.add(new RepositoryEvent(0, new Date(), updater, repository, updateEvent, "version", "" + updatedRepository.getVersion(), "" + repository.getVersion(), new Date()));
				updatedRepository.setVersion(repository.getVersion());
			}
			if(!Objects.equals(updatedRepository.getPublicationUri(), repository.getPublicationUri()))
			{
				events.add(new RepositoryEvent(0, new Date(), updater, repository, updateEvent, "publication URI", updatedRepository.getPublicationUri(), repository.getPublicationUri(), new Date()));
				updatedRepository.setPublicationUri(repository.getPublicationUri());
			}
			if(!Objects.equals(updatedRepository.getServerAddress(), repository.getServerAddress()))
			{
				events.add(new RepositoryEvent(0, new Date(), updater, repository, updateEvent, "server address", updatedRepository.getServerAddress(), repository.getServerAddress(), new Date()));
				updatedRepository.setServerAddress(repository.getServerAddress());
			}
			if(!Objects.equals(updatedRepository.getName(), repository.getName()))
			{
				events.add(new RepositoryEvent(0, new Date(), updater, repository, updateEvent, "name", updatedRepository.getName(), repository.getName(), new Date()));
				updatedRepository.setName(repository.getName());
			}
			if(updatedRepository.isPublished() != repository.isPublished())
			{
				updatedRepository.setPublished(repository.isPublished());
				String action = "published";
				if(!updatedRepository.isPublished())
					action = "un" + action;
				events.add(new RepositoryEvent(0, new Date(), updater, repository, updateEvent, action, "", "", new Date()));
			}

			for(RepositoryEvent rEvent : events)
			{
				rEvent = repositoryEventService.create(rEvent);
			}
			
			return updatedRepository;
		}
		catch(RepositoryNotFound | EventNotFound e)
		{
			throw new RepositoryEditException(e.getMessage());
		}
	}

	public Repository findByName(String name) 
	{
		return repositoryRepository.findByNameAndDeleted(name, false);
	}

	public Repository findByPublicationUri(String publicationUri)
	{
		return repositoryRepository.findByPublicationUriAndDeleted(publicationUri, false);
	}
	
	@Transactional(readOnly=false)
	public Repository boostRepositoryVersion(Repository repository, User updater) throws RepositoryEditException
	{
		repository.setVersion(repository.getVersion() + 1);
		return update(repository, updater);
	}
	
	private void createFolderStructureForGeneration(Repository repository, String dateStamp) throws RepositoryEditException
	{
		File dateStampFolder = new File(repositoryGenerationDirectory.getAbsolutePath() + separator + repository.getId() + separator + dateStamp);
		try 
		{
			if(!dateStampFolder.exists())
				FileUtils.forceMkdir(dateStampFolder);
			else 
			{	
				FileUtils.cleanDirectory(dateStampFolder);
			} 
			File contrib = new File(dateStampFolder.getAbsolutePath() + separator + "src" + separator + "contrib");
			FileUtils.forceMkdir(contrib);
		}
		catch (IOException e) 
		{
			throw new RepositoryEditException(e.getMessage());
		}
	}
	
	private File linkCurrentFolderToGeneratedFolder(Repository repository, String dateStamp) throws RepositoryEditException
	{
		try 
		{
			File target = new File(repositoryGenerationDirectory.getAbsolutePath() + separator + repository.getId() + separator + dateStamp);
			File link = new File(repositoryGenerationDirectory.getAbsolutePath() + separator + repository.getId() + separator + "current");
			fileService.linkFileTo(target, link);
			return target;
		}
		catch (IOException | InterruptedException e) 
		{
			throw new RepositoryEditException(e.getMessage());
		}
		
	}

	@Transactional(readOnly = false)
	public void publishRepository(Repository repository, User uploader) throws RepositoryEditException 
	{
		// 1. Create the folder structure
		String dateStamp = (new SimpleDateFormat("yyyyMMdd")).format(new Date());
		createFolderStructureForGeneration(repository, dateStamp);		
		// 2. Put all of the packages inside the newly created directory structure + create the PACKAGES file (+ PACKAGES.gz)
		populateGeneratedFolder(repository, dateStamp);
		// 3. Link the "current" directory to the newly published directory
		File target = linkCurrentFolderToGeneratedFolder(repository, dateStamp);
		
		// 5. Copy to the remote server (publicationUri) over SCP? and put it in the directory mentioned in the application.properties?
		try
		{
			RestTemplate rest = new RestTemplate();
	        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
	        Path repoPath = target.toPath().resolve("src").resolve("contrib");
	        Files.walk(repoPath, 1)
            	.filter(path -> !path.equals(repoPath))
            	.forEach(path -> map.add("files", new FileSystemResource(path.toFile())));
			ResponseEntity<String> response = rest.postForEntity(repository.getServerAddress(), map, String.class);
			if(!response.getStatusCode().is2xxSuccessful() || !Objects.equals(response.getBody(), "OK"))
				throw new RepositoryEditException("repository.publish.remote.failed");
		}
		catch(Exception e)
		{
			throw new RepositoryEditException("repository.publish.remote.failed");
		}
		
		repository.setPublished(true);
		update(repository, uploader);
		
		// TODO:
		// 4. Generate the HTML pages? -> use template inside the resources folder?
	}

	private void populateGeneratedFolder(Repository repository, String dateStamp) throws RepositoryEditException 
	{
		List<Package> packages = packageService.findByRepository(repository);
		File folder = new File(repositoryGenerationDirectory.getAbsolutePath() + separator + repository.getId() + separator + dateStamp + separator+ "src" + separator + "contrib");
		String packageString = "";
		for(Package p : packages)
		{
			if(p.isActive())
			{
				File targetFile = new File(p.getSource());
				File destFile = new File(folder.getAbsolutePath() + separator + p.getName() + "_" + p.getVersion() + ".tar.gz");
				try 
				{
					FileUtils.copyFile(targetFile, destFile);
					packageString += "Package: " + p.getName() + System.getProperty("line.separator");
					packageString += "Version: " + p.getVersion() + System.getProperty("line.separator");
					if(p.getDepends() != null && !p.getDepends().trim().isEmpty())
						packageString += "Depends: " + p.getDepends() + System.getProperty("line.separator");
					if(p.getImports() != null && !p.getImports().trim().isEmpty())
						packageString += "Imports: " + p.getImports() + System.getProperty("line.separator");
					packageString += "License: " + p.getLicense() + System.getProperty("line.separator");	
					if(!packageService.isSameMd5Sum(p, fileService.calculateMd5Sum(destFile)))
					{
						throw new RepositoryEditException("md5.mismatch");
					}
					packageString += "MD5sum: " + p.getMd5sum() + System.getProperty("line.separator");
					packageString += "NeedsCompilation: no" + System.getProperty("line.separator");
					packageString += System.getProperty("line.separator");
				} 
				catch (IOException e) 
				{
					throw new RepositoryEditException(e.getMessage());
				}
			}
		}
        BufferedWriter writer = null;
        try 
        {
            File packagesFile = new File(folder.getAbsolutePath() + separator + "PACKAGES");
            writer = new BufferedWriter(new FileWriter(packagesFile));
            writer.write(packageString);
            writer.close();
            fileService.gzipFile(packagesFile);
            writer = new BufferedWriter(new FileWriter(packagesFile));
            writer.write(packageString);
        } 
        catch (Exception e) 
        {
            throw new RepositoryEditException(e.getMessage());
        } 
        finally 
        {
            try 
            {
                writer.close();
            } 
            catch (Exception e) 
            {
            	// ignore nullpointer
            }
        }
	}
}

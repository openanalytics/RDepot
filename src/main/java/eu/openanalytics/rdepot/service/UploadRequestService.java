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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import eu.openanalytics.rdepot.exception.CommonsMultipartFileValidationException;
import eu.openanalytics.rdepot.exception.ManualCreateException;
import eu.openanalytics.rdepot.exception.PackageEditException;
import eu.openanalytics.rdepot.exception.PackageValidationException;
import eu.openanalytics.rdepot.exception.PackageValidationWarning;
import eu.openanalytics.rdepot.exception.SourceFileDeleteException;
import eu.openanalytics.rdepot.exception.UploadRequestValidationException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.UploadRequest;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.validation.CommonsMultipartFileValidator;
import eu.openanalytics.rdepot.validation.PackageValidator;
import eu.openanalytics.rdepot.warning.UploadRequestValidationWarning;

@Service
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class UploadRequestService
{
	
	private String separator = FileSystems.getDefault().getSeparator();
	
	@Resource(name="packageUploadDirectory")
	private File packageUploadDirectory;
	
	@Resource
	private UserService userService;
	
	@Resource
	private PackageService packageService;
	
	@Resource
	private PackageMaintainerService packageMaintainerService;
	
	@Resource
	private RepositoryMaintainerService repositoryMaintainerService;
	
	@Resource
	private RepositoryService repositoryService;
	
	@Resource
	private RoleService roleService;
	
	@Resource
	private FileService fileService;
	
	@Resource
	private PackageValidator packageValidator;
	
	@Resource
	private CommonsMultipartFileValidator commonsMultipartFileValidator;
	
	public Package createPackage(UploadRequest uploadRequest, int index, User uploader) throws UploadRequestValidationException, UploadRequestValidationWarning
	{
		try 
		{
			commonsMultipartFileValidator.validate(uploadRequest.getFileData()[index]);
			
			String name = uploadRequest.getFileData()[index].getOriginalFilename().split("_")[0];
			Repository repository = uploadRequest.getRepository();
			if(repository == null)
				throw new UploadRequestValidationException(MessageCodes.ERROR_REPOSITORY_NOT_FOUND);
			File onDisk = writeToDisk(uploadRequest.getFileData()[index], uploadRequest.getRepository());
			File extracted = extractFile(onDisk);
			File descriptionFile = new File(extracted.getParent() + separator + name + separator + "DESCRIPTION");
			Properties properties = readDescription(descriptionFile);
			String version = properties.getProperty("Version");
			String description = properties.getProperty("Description");
			String author = properties.getProperty("Author");
			String depends = properties.getProperty("Depends");
			String imports = properties.getProperty("Imports");
			String suggests = properties.getProperty("Suggests");
			String systemRequirements = properties.getProperty("System Requirements");
			String license = properties.getProperty("License");
			String url = properties.getProperty("URL");
			String source = onDisk.getAbsolutePath();
			String title = properties.getProperty("Title");
			String md5sum = fileService.calculateMd5Sum(onDisk);
			
			Package packageBag = new Package();
			packageBag.setName(name);
			packageBag.setVersion(version);
			packageBag.setDescription(description);
			packageBag.setAuthor(author);
			packageBag.setRepository(repository);
			packageBag.setDepends(depends);
			packageBag.setImports(imports);
			packageBag.setSuggests(suggests);
			packageBag.setSystemRequirements(systemRequirements);
			packageBag.setLicense(license);
			packageBag.setUrl(url);
			packageBag.setSource(source);
			packageBag.setTitle(title);
			packageBag.setMd5sum(md5sum);
			
			Boolean active = chooseActive(packageBag, uploader);
			packageBag.setActive(active);
			
			packageService.chooseBestMaintainer(packageBag);
			packageValidator.validate(packageBag);
			packageBag = packageService.create(packageBag, uploader);
			packageService.createManuals(packageBag);
			return packageBag;
			
		} 
		catch (CommonsMultipartFileValidationException | PackageEditException | ManualCreateException | IOException e) 
		{
			throw new UploadRequestValidationException(e.getMessage());
		}
		catch (PackageValidationException pve)
		{
			if(pve.getPackage() != null)
			{
				try 
				{
					packageService.deleteSource(pve.getPackage());
				} 
				catch (SourceFileDeleteException e) 
				{
					throw new UploadRequestValidationException(e.getMessage());
				}
			}
			throw new UploadRequestValidationException(pve.getMessage());
		}	
		catch (PackageValidationWarning pvw)
		{
			if(pvw.getPackage() != null)
			{
				try 
				{
					packageService.deleteSource(pvw.getPackage());
				} 
				catch (SourceFileDeleteException e) 
				{
					throw new UploadRequestValidationException(e.getMessage());
				}
			}
			throw new UploadRequestValidationWarning(pvw.getMessage());
		} 
	}
	
	public Boolean chooseActive(Package packageBag, User maintainer) 
	{
		String name = packageBag.getName();
		Repository repository = packageBag.getRepository();
		if(canUpload(name, repository, maintainer))
		{
			if(packageService.isHighestVersion(packageBag))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean canUpload(String packageName, Repository repository, User uploader)
	{
		if(Objects.equals(uploader.getRole().getName(), "admin"))
			return true;
		else if(Objects.equals(uploader.getRole().getName(), "repositorymaintainer"))
		{
			Set<RepositoryMaintainer> repositoryMaintainers = uploader.getRepositoryMaintainers();
			if(repositoryMaintainers.size() >= 1)
			{
				for(RepositoryMaintainer repositoryMaintainer : repositoryMaintainers)
				{
					if(repositoryMaintainer.getRepository().getId() == repository.getId())
					{
						return true;
					}
				}
			}
		}
		else if(Objects.equals(uploader.getRole().getName(), "packagemaintainer"))
		{
			Set<PackageMaintainer> packageMaintainers = uploader.getPackageMaintainers();
			if(packageMaintainers.size() >= 1)
			{
				for(PackageMaintainer packageMaintainer : packageMaintainers)
				{
					if(Objects.equals(packageMaintainer.getPackage(), packageName) && packageMaintainer.getRepository().getId() == repository.getId())
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public File writeToDisk(CommonsMultipartFile multipartFile, Repository repository) throws UploadRequestValidationException
	{		
		File randomDir = new File(packageUploadDirectory.getAbsolutePath() + separator + "repositories" + separator + repository.getId() + separator + (new Random()).nextInt(1000000));
		try 
		{
			File file = new File(randomDir.getAbsolutePath() + separator + multipartFile.getOriginalFilename()); 
			while(randomDir.exists())
				randomDir = new File(randomDir.getParent() + separator + (new Random()).nextInt(1000000));
			FileUtils.forceMkdir(randomDir);
			multipartFile.transferTo(file);
			return file;				
		} 
		catch (IllegalStateException e) 
		{
			if (randomDir.exists())
				randomDir.delete();
			throw new UploadRequestValidationException("file.exception.illegalstate");
		} 
		catch (IOException e) 
		{
			if (randomDir.exists())
				randomDir.delete();
			throw new UploadRequestValidationException("file.exception.io");
		}
	}
	
	public File extractFile(File file) throws UploadRequestValidationException
	{
        Process p;
        try 
        {
			p = Runtime.getRuntime().exec("tar -zxvf " + file.getAbsolutePath() + " -C " + file.getParent());
			p.waitFor();
	        if(p.exitValue() != 0)
	        {
	        	deleteUpload(file);
	        	throw new UploadRequestValidationException("file.exception.extract");
	        }
	        p.destroy();
		} 
        catch (IOException e) 
        {
        	deleteUpload(file);
        	throw new UploadRequestValidationException("file.exception.command");
		}
        catch (InterruptedException e) 
        {
        	deleteUpload(file);
        	throw new UploadRequestValidationException("file.exception.process");
		}

        return file;
	}
	
	public void deleteUpload(File targzfile) throws UploadRequestValidationException
	{
        try 
        {
			FileUtils.forceDelete(targzfile.getParentFile());
		} 
        catch (IOException e) 
        {
        	throw new UploadRequestValidationException("file.exception.command");
		}
	}
	
	public Properties readDescription(File file) throws UploadRequestValidationException
	{
		Properties prop = new Properties();
		try 
		{
			prop.load(new FileInputStream(file));
		} 
		catch (FileNotFoundException e) 
		{
			deleteUpload(file.getParentFile());
			throw new UploadRequestValidationException("file.description.notfound");
		} 
		catch (IOException e) 
		{
			deleteUpload(file.getParentFile());
			throw new UploadRequestValidationException("file.description.io");
		}
		return prop;
	}
	
}

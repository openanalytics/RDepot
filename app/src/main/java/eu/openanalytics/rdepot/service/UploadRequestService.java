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

import java.io.File;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import eu.openanalytics.rdepot.exception.ExtractFileException;
import eu.openanalytics.rdepot.exception.ManualCreateException;
import eu.openanalytics.rdepot.exception.Md5SumCalculationException;
import eu.openanalytics.rdepot.exception.MultipartFileValidationException;
import eu.openanalytics.rdepot.exception.PackageCreateException;
import eu.openanalytics.rdepot.exception.PackageDescriptionNotFound;
import eu.openanalytics.rdepot.exception.PackageEditException;
import eu.openanalytics.rdepot.exception.PackageStorageException;
import eu.openanalytics.rdepot.exception.PackageValidationException;
import eu.openanalytics.rdepot.exception.RepositoryNotFound;
import eu.openanalytics.rdepot.exception.SourceFileDeleteException;
import eu.openanalytics.rdepot.exception.UploadRequestValidationException;
import eu.openanalytics.rdepot.exception.WriteToDiskException;
import eu.openanalytics.rdepot.exception.WriteToDiskFromMultipartException;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.PackageUploadRequest;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.RepositoryMaintainer;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.storage.PackageStorage;
import eu.openanalytics.rdepot.validation.MultipartFileValidator;
import eu.openanalytics.rdepot.validation.PackageValidator;
import eu.openanalytics.rdepot.warning.PackageValidationWarning;
import eu.openanalytics.rdepot.warning.UploadRequestValidationWarning;

@Service
@Scope( proxyMode = ScopedProxyMode.TARGET_CLASS )
public class UploadRequestService {
	
    Logger logger = LoggerFactory.getLogger(UploadRequestService.class);
	
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
	private PackageValidator packageValidator;
	
	@Resource
	private MultipartFileValidator multipartFileValidator;
	
	@Resource
	private PackageStorage packageStorage;
	
	@Resource
	private MessageSource messageSource;
	
	private Locale locale = LocaleContextHolder.getLocale();
	
	private Package createPackage(String name, File packageFile, 
			User uploader, Boolean generateManual, Repository repository, 
			Boolean replace) 
						throws UploadRequestValidationException, UploadRequestValidationWarning {
		Package packageBag = new Package();
		
		try {
			File extracted = packageStorage.extractPackageFile(packageFile);
			File descriptionFile = packageStorage.getDescriptionFile(extracted, name);
			
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
			String source = packageFile.getAbsolutePath();
			String title = properties.getProperty("Title");
			String md5sum = packageStorage.calculateFileMd5Sum(packageFile.getAbsolutePath());
			User user = packageService.chooseBestMaintainer(packageBag);
			
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
			packageBag.setUser(user);
			packageBag.setActive(false);
			
			packageValidator.validate(packageBag, replace);
			packageBag = packageService.create(packageBag, uploader, replace);
			
			if(generateManual) {
				packageService.createManuals(packageBag);
				logger.info("Manuals were generated");
			} else {
				logger.info("Manuals were not generated");
			}
			return packageBag;
		} catch(PackageEditException | 
				   Md5SumCalculationException | 
				   ManualCreateException | 
				   PackageDescriptionNotFound | 
				   ExtractFileException e) {
			logger.error(e.getClass() + ": " + e.getMessage());
			throw new UploadRequestValidationException(e.getMessage());
		} catch (PackageCreateException e) {
			if(packageBag != null && !packageBag.getSource().isEmpty()) {
				try {
					packageService.deleteSource(packageBag);
				} catch (SourceFileDeleteException sfde) {
					logger.error(sfde.getClass() + ": " + sfde.getMessage());
				}
			}
			
			throw new UploadRequestValidationException(e.getMessage());
			
		} catch(PackageValidationException pve) {
			if(pve.getPackage() != null) {
				try {
					packageService.deleteSource(pve.getPackage());
				} catch (SourceFileDeleteException sfde) {
					logger.error(sfde.getClass() + ": " + sfde.getMessage());
				}
			}
			throw new UploadRequestValidationException(pve.getMessage());
		} catch (PackageValidationWarning pvw) {
			if(pvw.getPackage() != null) {
				try {
					packageService.deleteSource(pvw.getPackage());
				} catch (SourceFileDeleteException sfde) {
					logger.error(sfde.getClass() + ": " + sfde.getMessage());
				}
			}
			throw new UploadRequestValidationWarning(pvw.getMessage());
		} catch(Exception e) {
			try {
				packageService.deleteSource(packageBag);
			} catch (SourceFileDeleteException sfde) {
				//TODO: what if both problems (db and fs related) happened at the same time? 
			}
			throw e;
		}
	}
	
	/**
	 * Creates a user-submitted package from an upload request.
	 * @param packageUploadRequest
	 * @param uploader
	 * @return
	 * @throws UploadRequestValidationException
	 * @throws UploadRequestValidationWarning
	 */
	public Package createPackage(PackageUploadRequest packageUploadRequest, User uploader)
			throws UploadRequestValidationException, UploadRequestValidationWarning {
		boolean generateManual = packageUploadRequest.getGenerateManual();
		boolean replace = packageUploadRequest.getReplace();
		try {

			multipartFileValidator.validate(packageUploadRequest.getFileData());
			String name = packageUploadRequest.getFileData().getOriginalFilename().split("_")[0];
			Repository repository = repositoryService.findByName(packageUploadRequest.getRepository());
			if(repository == null)
				throw new RepositoryNotFound(messageSource, locale, packageUploadRequest.getRepository());
			
			File onDisk = packageStorage.writeToWaitingRoom(packageUploadRequest.getFileData(), repository);

			return createPackage(name, onDisk, uploader, generateManual, repository, replace);
		} catch(MultipartFileValidationException | 
				RepositoryNotFound | 
				WriteToDiskFromMultipartException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new UploadRequestValidationException(e.getMessage());
		}		
	}
	
	/**
	 * Creates or replaces existing package. 
	 * Unlike {@link #createPackage(PackageUploadRequest, User) createPackage} method,
	 * it takes an existing .tar.gz file as a parameter so that it can be used for internal submission.
	 * @param packageFile
	 * @param uploader
	 * @param repository
	 * @param generateManuals
	 * @return
	 * @throws UploadRequestValidationException
	 * @throws UploadRequestValidationWarning
	 */
	public Package createOrReplacePackage(File packageFile, User uploader, Repository repository, 
			Boolean generateManuals) 
			throws UploadRequestValidationException, UploadRequestValidationWarning {
		String name = packageFile.getName().split("_")[0];
		
		try {
			File onDisk = packageStorage.writeToWaitingRoom(packageFile, repository);
			
			return createPackage(name, onDisk, uploader, generateManuals, repository, true);
		} catch (WriteToDiskException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new UploadRequestValidationException(e.getMessage());
		}
	}
	
	public boolean canUpload(String packageName, Repository repository, User uploader) {
		if(Objects.equals(uploader.getRole().getName(), "admin"))
			return true;
		else if(Objects.equals(uploader.getRole().getName(), "repositorymaintainer")) {
			Set<RepositoryMaintainer> repositoryMaintainers = uploader.getRepositoryMaintainers();
			
			if(repositoryMaintainers.size() >= 1) {
				for(RepositoryMaintainer repositoryMaintainer : repositoryMaintainers) {
					if(repositoryMaintainer.getRepository().getId() == repository.getId())
						return true;
				}
			}
		}
		else if(Objects.equals(uploader.getRole().getName(), "packagemaintainer")) {
			Set<PackageMaintainer> packageMaintainers = uploader.getPackageMaintainers();
			
			if(packageMaintainers.size() >= 1) {
				for(PackageMaintainer packageMaintainer : packageMaintainers) {
					if(Objects.equals(packageMaintainer.getPackage(), packageName) && packageMaintainer.getRepository().getId() == repository.getId())
						return true;
				}
			}
		}
		return false;
	}
	
	public Properties readDescription(File file) throws UploadRequestValidationException {
		try {
			return packageStorage.readPackageDescription(file);
		} catch (PackageStorageException e) {
			throw new UploadRequestValidationException(e.getMessage());
		}
	}
	
}

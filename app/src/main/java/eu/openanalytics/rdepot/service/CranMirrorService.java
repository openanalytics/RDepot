/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
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
/**
 * 
 */
package eu.openanalytics.rdepot.service;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import eu.openanalytics.rdepot.exception.AdminNotFound;
import eu.openanalytics.rdepot.exception.CreateTemporaryFolderException;
import eu.openanalytics.rdepot.exception.DeleteFileException;
import eu.openanalytics.rdepot.exception.DownloadFileException;
import eu.openanalytics.rdepot.exception.DownloadPackagesFileException;
import eu.openanalytics.rdepot.exception.NoSuchPackageException;
import eu.openanalytics.rdepot.exception.ParsePackagesFileException;
import eu.openanalytics.rdepot.exception.SubmissionCreateException;
import eu.openanalytics.rdepot.exception.UpdatePackageException;
import eu.openanalytics.rdepot.messaging.MessageCodes;
import eu.openanalytics.rdepot.model.Mirror;
import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.User;
import eu.openanalytics.rdepot.properties.RepositoriesProps;
import eu.openanalytics.rdepot.storage.BaseStorage;
import eu.openanalytics.rdepot.utils.PackagesFileParser;
import eu.openanalytics.rdepot.warning.SubmissionCreateWarning;

/**
 * Mirroring implementation for R repositories
 */
@Service
public class CranMirrorService extends MirrorService {
	
	private static final Logger logger = LoggerFactory.getLogger(CranMirrorService.class);
	private static final Locale locale = LocaleContextHolder.getLocale();
	private static final String PACKAGES_FILE_PATH = "/src/contrib/PACKAGES";
	private static final String PACKAGE_PREFIX = "/src/contrib/";
	private static final String PACKAGE_ARCHIVE_PREFIX = "/src/contrib/Archive";

	private final BaseStorage baseStorage;
	private final PackageService packageService;
	private final UserService userService;
	private final SubmissionService submissionService;
	private final MessageSource messageSource;
	
	public CranMirrorService(PackageService packageService, UserService userService,
			SubmissionService submissionService,
			MessageSource messageSource, RepositoriesProps repositoriesProps,
			BaseStorage baseStorage) {
		super(repositoriesProps);
		this.packageService = packageService;
		this.userService = userService;
		this.submissionService = submissionService;
		this.messageSource = messageSource;
		this.baseStorage = baseStorage;
	}
	
	@Override
	@Async
	public void synchronize(Repository repository, Mirror mirror) {
		if(isPendingAddNewStatusIfFinished(repository)) {
			logger.warn(
					"Cannot start synchronization because it is already pending for this repository: " 
					+ repository.getId());
			return;
		}
		
		logger.info("Synchronization started for repository: " + repository.getId());
		
		try {
			List<Package> remotePackages = 
					getPackageListFromRemoteRepository(mirror, PACKAGES_FILE_PATH);
			
			for(Package packageBag : mirror.getPackages()) {
				Package localPackage = null;
				
				if(packageBag.getVersion() == null) {
					localPackage = packageService.findByNameAndRepositoryAndNewest(
							packageBag.getName(), repository);
					//TODO: fetch the newest package from remote mirror too
				} else {
					localPackage = packageService.findByNameAndVersionAndRepository(
							packageBag.getName(), packageBag.getVersion(), repository);
				}
				
				if(localPackage == null) {
					if(packageBag.getVersion() != null) {
						updatePackage(packageBag.getName(), packageBag.getVersion(), mirror, 
								repository, isOutdated(packageBag, remotePackages),
								packageBag.getGenerateManuals());
					} else {
						updatePackage(packageBag.getName(), 
								getVersion(packageBag.getName(), remotePackages), mirror, 
								repository, false, packageBag.getGenerateManuals());
					}
				} else {
					if(packageBag.getVersion() == null 
							&& !getPackageMd5(packageBag.getName(), remotePackages)
								.equals(localPackage.getMd5sum())) {
						
						updatePackage(packageBag.getName(), 
								getVersion(packageBag.getName(), remotePackages), mirror, 
								repository, false, packageBag.getGenerateManuals());
					}
				}
			} 
			
		} catch(NoSuchPackageException | 
				ParsePackagesFileException |
				UpdatePackageException | 
				DownloadPackagesFileException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			registerSynchronizationError(repository, e);
		} finally {
			logger.info("Synchronization finished for repository: " + repository.getId());
			registerFinishedSynchronization(repository);
		}
	}
	
	private String getVersion(String name, List<Package> remotePackages) throws NoSuchPackageException {
		for(Package packageBag : remotePackages) {
			if(packageBag.getName().equals(name))
				return packageBag.getVersion();
		}
		
		throw new NoSuchPackageException(name, messageSource, locale);
	}
	
	private Boolean isOutdated(Package packageBag, List<Package> remotePackages) 
			throws NoSuchPackageException {
		for(Package remotePackage : remotePackages) {
			if(remotePackage.getName().equals(packageBag.getName())) {
				return remotePackage.compareTo(packageBag) == 1;
			}
		}
		
		throw new NoSuchPackageException(packageBag, messageSource, locale);
	}
	
	private String getPackageMd5(String name, List<Package> remotePackages) 
			throws ParsePackagesFileException, NoSuchPackageException {
		for(Package remotePackage : remotePackages) {
			if(remotePackage.getName().equals(name)) {
				return remotePackage.getMd5sum();
			}
		}
		
		throw new NoSuchPackageException(name, messageSource, locale);
	}
	
	private void updatePackage(String name, String version, Mirror mirror, 
			Repository repository, Boolean archived, Boolean generateManuals) 
			throws UpdatePackageException {
		File remotePackageDir = null;
		
		try {
			remotePackageDir = baseStorage.createTemporaryFolder(name + "_" + version);
			String filename = name + "_" + version + ".tar.gz";
			File downloadedFile = new File(remotePackageDir.toPath() + "/" + filename);
			
			String downloadURL = null;
			
			if(archived) {
				downloadURL = mirror.getUri() + PACKAGE_ARCHIVE_PREFIX + "/" + name + "/" 
						 + filename;
			} else {
				downloadURL = mirror.getUri() + PACKAGE_PREFIX + "/" + filename;
			}
			
			downloadedFile = baseStorage.downloadFile(downloadURL, downloadedFile);
			User uploader = userService.findFirstAdmin();
			
			submissionService.createInternalSubmission(downloadedFile, uploader, repository, generateManuals);
			logger.info("Package mirrored.");
		} catch (CreateTemporaryFolderException | AdminNotFound | 
				DownloadFileException | SubmissionCreateException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new UpdatePackageException(name, version, mirror, messageSource, locale);
		} catch (SubmissionCreateWarning w) {
			logger.warn(w.getClass().getName() + ": " + w.getMessage(), w);
		} finally {
			try {
				if(remotePackageDir != null)
					baseStorage.deleteFile(remotePackageDir.toString());
				
			} catch (DeleteFileException ioe) {
				logger.error(messageSource.getMessage(MessageCodes.ERROR_CLEAN_FS, null, 
						MessageCodes.ERROR_CLEAN_FS, locale)
						+ "\nLocation: " + remotePackageDir.toPath().toAbsolutePath().toString());
			}
		}
	}

	private List<Package> getPackageListFromRemoteRepository(Mirror mirror, String packagesFilePath) throws DownloadPackagesFileException {
		Path remotePackagesFilePath = null;
		List<Package> remotePackages = null;
		PackagesFileParser parser = new PackagesFileParser();

		try {
			String downloadUrl = mirror.getUri() + packagesFilePath;
			remotePackagesFilePath = baseStorage.downloadFile(downloadUrl).toPath();
			remotePackages = parser.parse(remotePackagesFilePath.toFile());
		} catch(DownloadFileException | ParsePackagesFileException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new DownloadPackagesFileException(mirror, messageSource, locale);
		} finally {
			if(remotePackagesFilePath != null) {
				try {
					baseStorage.deleteFile(remotePackagesFilePath.toString());
				} catch (DeleteFileException e) {
					logger.error(messageSource.getMessage(MessageCodes.ERROR_CLEAN_FS, null, 
							MessageCodes.ERROR_CLEAN_FS, locale) 
							+ "\nLocation: " + remotePackagesFilePath.toAbsolutePath().toString());
				}
			}
		}
		
		return remotePackages;
	}
}

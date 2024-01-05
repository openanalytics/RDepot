/**
 * R Depot
 *
 * Copyright (C) 2012-2024 Open Analytics NV
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
package eu.openanalytics.rdepot.r.mirroring;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageUploadRequest;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.exception.AdminNotFound;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.mirroring.MirrorSynchronizer;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.storage.exceptions.CreateTemporaryFolderException;
import eu.openanalytics.rdepot.base.storage.exceptions.DeleteFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.DownloadFileException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.r.config.props.RRepositoriesProps;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.mirroring.exceptions.DownloadPackagesFileException;
import eu.openanalytics.rdepot.r.mirroring.exceptions.NoSuchPackageException;
import eu.openanalytics.rdepot.r.mirroring.exceptions.UpdatePackageException;
import eu.openanalytics.rdepot.r.mirroring.pojos.MirroredRPackage;
import eu.openanalytics.rdepot.r.mirroring.pojos.MirroredRRepository;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.storage.implementations.RLocalStorage;
import eu.openanalytics.rdepot.r.strategy.factory.RStrategyFactory;
import eu.openanalytics.rdepot.r.utils.PackagesFileParser;
import eu.openanalytics.rdepot.r.utils.exceptions.ParsePackagesFileException;

/**
 * Mirroring implementation for R repositories
 */
@Component
public class CranMirrorSynchronizer extends MirrorSynchronizer<MirroredRRepository, MirroredRPackage, CranMirror> {
	
	private static final Logger logger = LoggerFactory.getLogger(CranMirrorSynchronizer.class);
	private static final Locale locale = LocaleContextHolder.getLocale();
	private static final String PACKAGES_FILE_PATH = "/src/contrib/PACKAGES";
	private static final String PACKAGE_PREFIX = "/src/contrib/";
	private static final String PACKAGE_ARCHIVE_PREFIX = "/src/contrib/Archive";

	private final RLocalStorage storage;
	private final RPackageService packageService;
	private final MessageSource messageSource;
	private final BestMaintainerChooser bestMaintainerChooser;
	private final RStrategyFactory strategyFactory;
	private final RRepositoryService repositoryService;
	
	public CranMirrorSynchronizer(RPackageService packageService, UserService userService,
			SubmissionService submissionService,
			MessageSource messageSource, RRepositoriesProps repositoriesProps,
			RLocalStorage storage, BestMaintainerChooser bestMaintainerChooser,
			RStrategyFactory strategyFactory, RRepositoryService repositoryService) {
		super(repositoriesProps);
		this.packageService = packageService;
		this.messageSource = messageSource;
		this.storage = storage;
		this.bestMaintainerChooser = bestMaintainerChooser;
		this.strategyFactory = strategyFactory;
		this.repositoryService = repositoryService;
	}
	
	@Override
	@Async
	public void synchronize(MirroredRRepository mirroredRepository, CranMirror mirror) {
		RRepository repositoryEntity = repositoryService.findByName(mirroredRepository.getName())
				.orElseThrow(() -> new IllegalStateException(
						"Cannot synchronize non-existing repository."));
		synchronize(repositoryEntity, mirror);
	}
	
	@Async
	public void synchronize(RRepository repository, CranMirror mirror) {
		if(isPendingAddNewStatusIfFinished(repository)) {
			logger.warn(
					"Cannot start synchronization "
					+ "because it is already pending for this repository: " 
					+ repository.getId());
			return;
		}
		
		logger.info("Synchronization started for repository: " + repository.getId());
		
		try {
			List<RPackage> remotePackages = 
					getPackageListFromRemoteRepository(mirror, PACKAGES_FILE_PATH);
			
			List<RPackage> packages = resolveMirroredPackagesToPackageEntities(mirror.getPackages());
			
			for(RPackage packageBag : packages) {
				Optional<RPackage> localPackage = Optional.empty();
				
				if(packageBag.getVersion() == null) {
					localPackage = packageService
							.findNonDeletedNewestByNameAndRepository(packageBag.getName(), repository);
					//TODO: fetch the newest package from remote mirror too
				} else {
					localPackage = packageService
							.findNonDeletedByNameAndVersionAndRepository(
									packageBag.getName(), packageBag.getVersion(), repository);
				}
				
				if(localPackage.isEmpty()) {
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
								.equals(localPackage.get().getMd5sum())) {
						
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

	private List<RPackage> resolveMirroredPackagesToPackageEntities(List<MirroredRPackage> packages) {
		return packages.stream().map(mp -> mp.toPackageEntity()).collect(Collectors.toList());
	}

	private String getVersion(String name, List<RPackage> remotePackages) throws NoSuchPackageException {
		for(RPackage packageBag : remotePackages) {
			if(packageBag.getName().equals(name))
				return packageBag.getVersion();
		}
		
		throw new NoSuchPackageException(name);
	}
	
	private Boolean isOutdated(RPackage packageBag, List<RPackage> remotePackages) 
			throws NoSuchPackageException {
		for(RPackage remotePackage : remotePackages) {
			if(remotePackage.getName().equals(packageBag.getName())) {
				return remotePackage.compareTo(packageBag) == 1;
			}
		}
		
		throw new NoSuchPackageException(packageBag);
	}
	
	private String getPackageMd5(String name, List<RPackage> remotePackages) 
			throws ParsePackagesFileException, NoSuchPackageException {
		for(RPackage remotePackage : remotePackages) {
			if(remotePackage.getName().equals(name)) {
				return remotePackage.getMd5sum();
			}
		}
		
		throw new NoSuchPackageException(name);
	}
	
	private void updatePackage(String name, String version, CranMirror mirror, 
			RRepository repository, Boolean archived, Boolean generateManuals) 
			throws UpdatePackageException {
		File remotePackageDir = null;
		
		try {
			remotePackageDir = storage.createTemporaryFolder(name + "_" + version);
			String filename = name + "_" + version + ".tar.gz";
			File downloadDestination = new File(remotePackageDir.toPath() + "/" + filename);
			
			String downloadURL = null;
			
			if(archived) {
				downloadURL = mirror.getUri() + PACKAGE_ARCHIVE_PREFIX + "/" + name + "/" 
						 + filename;
			} else {
				downloadURL = mirror.getUri() + PACKAGE_PREFIX + "/" + filename;
			}
			
			MultipartFile downloadedFile = storage.downloadFile(downloadURL, downloadDestination);
			User uploader = bestMaintainerChooser.findFirstAdmin();
			
			PackageUploadRequest<RRepository> request = new PackageUploadRequest<RRepository>(downloadedFile, repository, generateManuals, false);
			Strategy<Submission> strategy = strategyFactory.uploadPackageStrategy(request, uploader);
			strategy.perform();
			
			logger.info("Package mirrored.");
		} catch (CreateTemporaryFolderException | AdminNotFound | 
				DownloadFileException | StrategyFailure e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new UpdatePackageException(name, version, mirror);
		} finally {
			try {
				if(remotePackageDir != null)
					storage.removeFileIfExists(remotePackageDir.getAbsolutePath());
				
			} catch (DeleteFileException ioe) {
				logger.error(messageSource.getMessage(MessageCodes.ERROR_CLEAN_FS, null, 
						MessageCodes.ERROR_CLEAN_FS, locale)
						+ "\nLocation: " + remotePackageDir.toPath().toAbsolutePath().toString());
			}
		}
	}

	private List<RPackage> getPackageListFromRemoteRepository(CranMirror mirror, String packagesFilePath) throws DownloadPackagesFileException {
		Path remotePackagesFilePath = null;
		List<RPackage> remotePackages = null;
		PackagesFileParser parser = new PackagesFileParser();

		try {
			String downloadUrl = mirror.getUri() + packagesFilePath;
			remotePackagesFilePath = storage.downloadFile(downloadUrl).toPath();
			remotePackages = parser.parse(remotePackagesFilePath.toFile());
		} catch(DownloadFileException | ParsePackagesFileException e) {
			logger.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw new DownloadPackagesFileException(mirror);
		} finally {
			if(remotePackagesFilePath != null) {
				try {
					storage.removeFileIfExists(remotePackagesFilePath.toFile().getAbsolutePath());
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

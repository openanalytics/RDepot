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
package eu.openanalytics.rdepot.base.strategy.upload;

import java.io.File;
import java.util.Properties;

import org.springframework.web.multipart.MultipartFile;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageUploadRequest;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.mediator.deletion.PackageDeleter;
import eu.openanalytics.rdepot.base.mediator.deletion.exceptions.NoSuitableMaintainerFound;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.EmailService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.service.exceptions.CreateEntityException;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.storage.exceptions.CheckSumCalculationException;
import eu.openanalytics.rdepot.base.storage.exceptions.DeleteFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.ExtractFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.InvalidSourceException;
import eu.openanalytics.rdepot.base.storage.exceptions.MovePackageSourceException;
import eu.openanalytics.rdepot.base.storage.exceptions.ReadPackageDescriptionException;
import eu.openanalytics.rdepot.base.storage.exceptions.SourceFileDeleteException;
import eu.openanalytics.rdepot.base.storage.exceptions.WriteToWaitingRoomException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.FatalStrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.ParsePackagePropertiesException;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyReversionFailure;
import eu.openanalytics.rdepot.base.synchronization.RepositorySynchronizer;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.base.time.DateProvider;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageDuplicatedToSilentlyIgnore;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageValidationException;

/**
 * Default strategy for uploading packages.
 * It should be extended by specific technology's implementation.
 * @param <R> Repository entity
 * @param <P> Package entity
 */
public abstract class DefaultPackageUploadStrategy<R extends Repository<R, ?>, P extends Package<P, ?>> 
	extends Strategy<Submission> {
	
	protected final PackageUploadRequest<R> request;
	protected final PackageValidator<P> packageValidator;
	protected final RepositoryService<R> repositoryService;
	protected final PackageService<P> packageService;
	protected final Storage<R, P> storage;
	protected final SubmissionService submissionService;
	protected final BestMaintainerChooser bestMaintainerChooser;
	protected final EmailService emailService;
	protected final RepositorySynchronizer<R> repositorySynchronizer;
	protected final SecurityMediator securityMediator;
	protected final PackageDeleter<P> packageDeleter;
	protected Submission submission = new Submission();
	protected P packageBag = null;
	
	protected DefaultPackageUploadStrategy(
			PackageUploadRequest<R> request, 
			User requester,
			NewsfeedEventService newsfeedEventService, 
			PackageValidator<P> packageValidator,
			RepositoryService<R> repositoryService,
			Storage<R, P> storage,
			PackageService<P> packageService,
			SubmissionService submissionService,
			EmailService emailService,
			BestMaintainerChooser bestMaintainerChooser,
			RepositorySynchronizer<R> repositorySynchronizer,
			SecurityMediator securityMediator,
			PackageDeleter<P> packageDeleter) {
		super(new Submission(), submissionService, requester, newsfeedEventService);
		this.request = request;
		this.packageValidator = packageValidator;
		this.repositoryService = repositoryService;
		this.storage = storage;
		this.packageService = packageService;
		this.submissionService = submissionService;
		this.emailService = emailService;
		this.bestMaintainerChooser = bestMaintainerChooser;
		this.repositorySynchronizer = repositorySynchronizer;
		this.securityMediator = securityMediator;
		this.packageDeleter = packageDeleter;
	}

	@Override
	protected Submission actualStrategy() throws StrategyFailure {
		logger.debug("Package upload strategy started.");
		final boolean generateManual = request.getGenerateManual();
		final boolean replace = request.getReplace();
		final MultipartFile fileData = request.getFileData();
		final String name = fileData.getOriginalFilename().split("_")[0];
		final R repository = request.getRepository();
		
		File stored = null;
		File extracted = null;
		Properties packageProperties = null;
		try {
			stored = new File(storage.writeToWaitingRoom(fileData, repository));
			extracted = new File(storage.extractTarGzPackageFile(stored.getAbsolutePath()));
			packageProperties = storage.getPropertiesFromExtractedFile(extracted.getAbsolutePath());
			//TODO: What happens if it fails here, inside createPackage? Will it be reverted and storedFile removed?
			packageBag = createPackage(name, stored, packageProperties, requester, 
					generateManual, repository, replace);
			submission = createSubmission(packageBag);		
			logger.debug("Submission created.");
			repositoryService.incrementVersion(repository);
			logger.debug("Package upload strategy finished.");
			packageBag.setSubmission(submission);
			return submission;
		} catch (PackageValidationException | NoSuitableMaintainerFound | 
				ExtractFileException | WriteToWaitingRoomException | 
				ReadPackageDescriptionException | SourceFileDeleteException | 
				InvalidSourceException | CreateEntityException | 
				ParsePackagePropertiesException | CheckSumCalculationException e) {
			logger.error(e.getMessage(), e);
			
			if (stored != null && stored.exists()) {
				try {
					storage.removeFileIfExists(stored.getAbsolutePath());
				} catch (DeleteFileException dfe) {
					logger.error("Could not remove created sources!", dfe);
				}
			}
			if(extracted != null && extracted.exists()) {
				try {
					storage.removeFileIfExists(extracted.getAbsolutePath());
				} catch (DeleteFileException dfe) {
					logger.error("Could not remove extracted sources!", dfe);
				}
			}
			
			throw new FatalStrategyFailure(e);
		} catch (MovePackageSourceException | DeleteEntityException e) {
			logger.error(e.getMessage(), e);
			throw new StrategyFailure(e, false);
		} 
		catch(PackageDuplicatedToSilentlyIgnore e) {
			logger.error(e.getMessage());
			throw new FatalStrategyFailure(e);
		}
	}
	
	/**
	 * Creates submission for given package.
	 * If a requester has decent privileges, it is automatically accepted.
	 * @param packageBag
	 * @return submission
	 * @throws MovePackageSourceException 
	 * @throws InvalidSourceException 
	 * @throws CreateEntityException 
	 */
	private Submission createSubmission(P packageBag) 
			throws InvalidSourceException, MovePackageSourceException, CreateEntityException {
		logger.debug("Creating submission for package " + packageBag.toString());
		Submission submission = new Submission();
		submission.setUser(requester);
		submission.setPackage(packageBag);		
		logger.debug("Submission " + submission.toString() + " created.");

		if(securityMediator.canUpload(packageBag.getName(), packageBag.getRepository(), requester)) {
			logger.debug("Privileged requester - submission is being automatically accepted...");
			packageBag.setSource(storage.moveToMainDirectory(packageBag));
			submission.setState(SubmissionState.ACCEPTED);
			packageBag.setActive(true);
			logger.debug("Package " + packageBag.toString() + " accepted.");
		} else {
			logger.debug("Requester is not allowed to accept this package. Submission set to WAITING.");
			submission.setState(SubmissionState.WAITING);
			emailService.sendAcceptSubmissionEmail(submission);
			logger.debug("Submission email for submission " + submission.toString() + " sent.");
		}
		
		submission = submissionService.create(submission);
		return submission;
	}

	/**
	 * Creates a package object based on the stored file and additional data.
	 * @param name name of the package
	 * @param storedFile package stored in persistent storage
	 * @param requester user uploading file
	 * @param generateManual specifies if manuals for package should be generated
	 * @param repository repository where package should be uploaded to
	 * @param replace
	 * @return package object
	 * @throws NoSuitableMaintainerFound 
	 * @throws ExtractFileException 
	 * @throws ReadPackageDescriptionException 
	 * @throws SourceFileDeleteException 
	 * @throws CreateEntityException 
	 * @throws ParsePackagePropertiesException 
	 * @throws PackageDuplicatedToSilentlyIgnore 
	 */
	private P createPackage(String name, File storedFile, Properties properties, User requester, 
			boolean generateManual, R repository, boolean replace) 
					throws PackageValidationException, 
					NoSuitableMaintainerFound, 
					SourceFileDeleteException, 
					ExtractFileException, 
					ReadPackageDescriptionException, 
					CreateEntityException, 
					ParsePackagePropertiesException,
					CheckSumCalculationException, 
					PackageDuplicatedToSilentlyIgnore,
					DeleteEntityException {
		logger.debug("Creating package.");
		P packageBag = parseTechnologySpecificPackageProperties(properties);
		packageBag = parseUniversalProperties(packageBag, properties);
		
		packageBag.setRepository(repository);
		packageBag.setActive(false);
		packageBag.setDeleted(false);
		packageBag.setName(name);
		packageBag.setSource(storedFile.getAbsolutePath());
		packageBag.setUser(bestMaintainerChooser.chooseBestPackageMaintainer(packageBag));
		//TODO: Are manuals part of technology?
		try {
			storage.calculateCheckSum(packageBag);
			
			try {
				packageValidator.validateUploadPackage(packageBag, replace);
			} catch(PackageDuplicatedToSilentlyIgnore e) {
				packageDeleter.delete((P)e.getPackage());
				//TODO: Casting won't be needed when we improve the entities' structure in 1.8
			}
			
			packageBag = packageService.create(packageBag);
			logger.debug("Package " + packageBag.toString() + " created.");
		} 
		catch (PackageValidationException | 
				CheckSumCalculationException | 
				DeleteEntityException e) {
			if(e instanceof DeleteEntityException) {
				logger.error("Could not delete replaced package.");
			} else {
				logger.warn("Invalid package uploaded. Removing sources..."); //TODO: Should we log such things?
			}
			storage.removePackageSource(storedFile.getAbsolutePath());
			throw e;
		}
		return packageBag;
	}

	/**
	 * Parses package properties that are universal for all technologies.
	 * @param packageBag
	 * @param properties
	 * @return package
	 */
	private P parseUniversalProperties(P packageBag, Properties properties) {
		packageBag.setVersion(properties.getProperty("Version")); //TODO: this is questionable. What if it is "VERSION" or "version"?null);
		return packageBag;
	}

	/**
	 * Parses package properties that are technology-specific.
	 * @param properties
	 * @return
	 * @throws ParsePackagePropertiesException 
	 */
	protected abstract P parseTechnologySpecificPackageProperties(Properties properties) 
			throws ParsePackagePropertiesException;
	
	@Override
	protected void postStrategy() throws StrategyFailure {								
		if(request.getRepository().isPublished()) {
			try {
				repositorySynchronizer.storeRepositoryOnRemoteServer(request.getRepository(), 
						DateProvider.getCurrentDateStamp());		
			} catch (SynchronizeRepositoryException e) {
				logger.error(e.getMessage(), e);
				throw new StrategyFailure(e);
			}
		}
	}
	
	@Override
	public void revertChanges() throws StrategyReversionFailure {
		//TODO: Think about reverting changes in a different way!
	}
	
	@Override
	protected NewsfeedEvent generateEvent(Submission resource) {
		return new NewsfeedEvent(requester, NewsfeedEventType.UPLOAD, resource);
	}
}

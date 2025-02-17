/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.base.strategy.upload;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageUploadRequest;
import eu.openanalytics.rdepot.base.email.EmailService;
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
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
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
import eu.openanalytics.rdepot.base.storage.exceptions.WriteToWaitingRoomException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.exceptions.CreatePackageException;
import eu.openanalytics.rdepot.base.strategy.exceptions.CreateSubmissionException;
import eu.openanalytics.rdepot.base.strategy.exceptions.FatalStrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.NonFatalSubmissionStrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.PackageProcessingException;
import eu.openanalytics.rdepot.base.strategy.exceptions.ParsePackagePropertiesException;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyReversionFailure;
import eu.openanalytics.rdepot.base.synchronization.RepositorySynchronizer;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.base.validation.DataSpecificValidationResult;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.base.validation.ValidationResultImpl;
import eu.openanalytics.rdepot.base.validation.ValidationResultItem;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageDuplicateWithReplaceOff;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageValidationException;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

/**
 * Default strategy for uploading packages.
 * It must be extended by specific technology's implementation.
 * @param <R> Repository entity
 * @param <P> Package entity
 */
@Slf4j
public abstract class DefaultPackageUploadStrategy<R extends Repository, P extends Package>
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
    protected final PackageDeleter<P, R> packageDeleter;
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
            PackageDeleter<P, R> packageDeleter) {
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

    protected File extractPackageFile(File stored) throws ExtractFileException {
        return new File(storage.extractTarGzPackageFile(stored.getAbsolutePath()));
    }

    @Override
    protected Submission actualStrategy() throws StrategyFailure {
        log.debug("Package upload strategy started.");
        final MultipartFile fileData = request.getFileData();
        final String name =
                Objects.requireNonNull(fileData.getOriginalFilename()).split("_")[0];
        final R repository = request.getRepository();

        File stored = null;
        File extracted = null;
        Properties packageProperties;
        try {
            stored = new File(storage.writeToWaitingRoom(fileData, repository));
            extracted = extractPackageFile(stored);
            packageProperties = storage.getPropertiesFromExtractedFile(extracted.getAbsolutePath());
            packageBag = createPackage(name, stored, packageProperties);
            submission = createSubmission(packageBag);
            log.debug("Submission created.");
            repositoryService.incrementVersion(repository);
            log.debug("Package upload strategy finished.");
            packageBag.setSubmission(submission);
            return submission;
        } catch (PackageValidationException
                | WriteToWaitingRoomException
                | ExtractFileException
                | PackageDuplicateWithReplaceOff
                | CreatePackageException
                | CreateSubmissionException e) {
            cleanUpSource(stored, extracted);

            if (e instanceof PackageDuplicateWithReplaceOff) {
                log.debug(e.getMessage(), e);
                throw new StrategyFailure(e, false);
            } else {
                log.error(e.getMessage(), e);
                throw new FatalStrategyFailure(e);
            }
        } catch (ReadPackageDescriptionException e) {
            log.debug(e.getMessage(), e);
            cleanUpSource(stored, extracted);

            throw new FatalStrategyFailure(new PackageValidationException(e.getMessageCode()));
        } catch (Exception e) {
            cleanUpSource(stored, extracted);
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private void cleanUpSource(File stored, File extracted) {
        if (stored != null && stored.exists()) {
            try {
                storage.removeFileIfExists(stored.getAbsolutePath());
            } catch (DeleteFileException dfe) {
                log.error("Could not remove created sources!", dfe);
            }
        }
        if (extracted != null && extracted.exists()) {
            try {
                storage.removeFileIfExists(extracted.getAbsolutePath());
            } catch (DeleteFileException dfe) {
                log.error("Could not remove extracted sources!", dfe);
            }
        }
    }

    /**
     * Creates submission for given package.
     * If a requester has decent privileges, it is automatically accepted.
     * @param packageBag created package
     * @return submission created submission
     */
    private Submission createSubmission(P packageBag) throws CreateSubmissionException {
        log.debug("Creating submission for package {}", packageBag.toString());
        Submission submission = new Submission();
        submission.setSubmitter(requester);
        submission.setPackage(packageBag);
        submission.setChanges(request.getChanges());
        log.debug("Submission {} created.", submission);

        if (securityMediator.canUpload(packageBag.getName(), packageBag.getRepository(), requester)) {
            log.debug("Privileged requester - submission is being automatically accepted...");

            final String mainDirSource;
            try {
                mainDirSource = storage.moveToMainDirectory(packageBag);
            } catch (InvalidSourceException | MovePackageSourceException e) {
                log.error(e.getMessage(), e);
                throw new CreateSubmissionException();
            }

            packageBag.setSource(mainDirSource);
            packageBag.setActive(true);
            submission.setState(SubmissionState.ACCEPTED);
            submission.setApprover(requester);
            log.debug("Package {} accepted.", packageBag);
        } else {
            log.debug("Requester is not allowed to accept this package. Submission set to WAITING.");
            submission.setState(SubmissionState.WAITING);
            emailService.sendAcceptSubmissionEmail(submission);
            log.debug("Submission email for submission {} sent.", submission);
        }

        try {
            return submissionService.create(submission);
        } catch (CreateEntityException e) {
            log.error(e.getMessage(), e);
            throw new CreateSubmissionException();
        }
    }

    /**
     * Creates a package object based on the stored file and additional data.
     * @param name name of the package
     * @param storedFile package stored in persistent storage
     * @return package object
     */
    private P createPackage(String name, File storedFile, Properties properties)
            throws PackageValidationException, PackageDuplicateWithReplaceOff, CreatePackageException {
        try {
            log.debug("Creating package.");
            P packageBag = parseUniversalProperties(
                    parseTechnologySpecificPackageProperties(properties),
                    properties,
                    request.getRepository(),
                    name,
                    storedFile);

            final DataSpecificValidationResult<Submission> validationResult = validateAndProcessPackage(packageBag);

            if (validationResult.hasErrors()) throw new PackageValidationException(validationResult.getErrors());
            if (validationResult.hasWarnings()) {
                final Optional<ValidationResultItem<Submission>> replaceOffWarning =
                        validationResult.getDataSpecificWarnings().stream()
                                .filter(w -> w.messageCode().equals(MessageCodes.DUPLICATE_VERSION_REPLACE_OFF))
                                .findAny();
                if (replaceOffWarning.isPresent())
                    throw new PackageDuplicateWithReplaceOff(
                            replaceOffWarning.get().data());
            }

            packageBag = packageService.create(packageBag);
            log.debug("Package {} created.", packageBag.toString());

            return packageBag;
        } catch (PackageProcessingException
                | CreateEntityException
                | PackageValidationException
                | ParsePackagePropertiesException
                | NoSuitableMaintainerFound e) {
            log.debug("Exception was thrown - removing package sources...");

            if (e instanceof PackageValidationException) {
                log.debug("Internal (after-upload) package validation failed.");
                throw (PackageValidationException) e;
            } else {
                log.error("Critical error occurred while processing package.");
                log.error(e.getMessage(), e);
                throw new CreatePackageException();
            }
        }
    }

    private @NonNull DataSpecificValidationResult<Submission> validateAndProcessPackage(@NonNull P packageBag)
            throws PackageProcessingException {
        try {
            storage.calculateCheckSum(packageBag);

            final DataSpecificValidationResult<Submission> validationResult =
                    ValidationResultImpl.createDataSpecificResult();
            packageValidator.validateUploadPackage(packageBag, request.isReplace(), validationResult);

            try {
                handleDuplicatesIfThereWereAny(validationResult);
            } catch (DeleteEntityException e) {
                log.error("Package duplicate was found but could not be deleted.");
                throw e;
            }

            return validationResult;
        } catch (DeleteEntityException | CheckSumCalculationException e) {
            log.error(e.getMessage(), e);
            throw new PackageProcessingException();
        }
    }

    private void handleDuplicatesIfThereWereAny(final DataSpecificValidationResult<Submission> validationResult)
            throws DeleteEntityException {
        final List<ValidationResultItem<Submission>> packageDuplicateWarnings =
                validationResult.getDataSpecificWarnings().stream()
                        .filter(w -> w.messageCode().equals(MessageCodes.DUPLICATE_VERSION_REPLACE_ON))
                        .toList();

        for (ValidationResultItem<Submission> packageDuplicateWarning : packageDuplicateWarnings) {
            packageDeleter.deleteTransactional(
                    packageDuplicateWarning.data().getPackage().getId());
        }
    }

    /**
     * Parses package properties that are universal for all technologies.
     * @return package with assigned properties
     * @throws NoSuitableMaintainerFound if there are no suitable maintainer users in the system
     */
    private P parseUniversalProperties(P packageBag, Properties properties, R repository, String name, File storedFile)
            throws NoSuitableMaintainerFound {
        packageBag.setVersion(properties.getProperty("Version"));
        packageBag.setAuthor(properties.getProperty("Author"));
        packageBag.setActive(false);
        packageBag.setDeleted(false);
        assignRepositoryToPackage(repository, packageBag);
        packageBag.setSource(storedFile.getAbsolutePath());
        packageBag.setUser(bestMaintainerChooser.chooseBestPackageMaintainer(packageBag));
        if (!Objects.nonNull(packageBag.getName())) {
            packageBag.setName(name);
        }
        return packageBag;
    }

    /**
     * Parses package properties that are technology-specific.
     * @param properties parsed {@link Properties} object
     * @return newly created, technology-specific package object
     */
    protected abstract P parseTechnologySpecificPackageProperties(Properties properties)
            throws ParsePackagePropertiesException;

    /**
     * Parses binary package properties that are technology-specific.
     * @param properties parsed {@link Properties} object
     * @return newly created, technology-specific package object
     */
    protected abstract P parseTechnologySpecificBinaryPackageProperties(Properties properties)
            throws ParsePackagePropertiesException;

    /**
     * Used to link {@link Package} with {@link Repository} in a technology-specific way.
     */
    protected abstract void assignRepositoryToPackage(R repository, P packageBag);

    @Override
    public void postStrategy() throws StrategyFailure {
        if (request.getRepository().getPublished()) {
            try {
                repositorySynchronizer.storeRepositoryOnRemoteServer(request.getRepository());
            } catch (SynchronizeRepositoryException e) {
                log.error(e.getMessage(), e);
                throw new NonFatalSubmissionStrategyFailure(e, processedResource);
            }
        }
    }

    @Override
    public void revertChanges() throws StrategyReversionFailure {
        try {
            packageDeleter.delete(packageBag);
        } catch (DeleteEntityException e) {
            throw new StrategyReversionFailure(e);
        }
    }

    @Override
    protected NewsfeedEvent generateEvent(Submission resource) {
        return new NewsfeedEvent(requester, NewsfeedEventType.UPLOAD, resource);
    }
}

/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
package eu.openanalytics.rdepot.python.strategy.upload;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageUploadRequest;
import eu.openanalytics.rdepot.base.email.EmailService;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.*;
import eu.openanalytics.rdepot.base.storage.LocalStorage;
import eu.openanalytics.rdepot.base.storage.exceptions.ExtractFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.StoreFileException;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.upload.DefaultPackageUploadStrategy;
import eu.openanalytics.rdepot.base.validation.DataSpecificValidationResult;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.base.validation.ValidationResultItem;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonPackageDeleter;
import eu.openanalytics.rdepot.python.storage.PythonPersistentStorage;
import eu.openanalytics.rdepot.python.storage.population.PythonPopulator;
import eu.openanalytics.rdepot.python.synchronization.PythonRepositorySynchronizer;
import java.io.File;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Implementation of upload strategy for R packages.
 */
@Slf4j
public class PythonPackageUploadStrategy extends DefaultPackageUploadStrategy<PythonRepository, PythonPackage> {

    private final PythonPopulator pythonPopulator;
    private static final String PROP_CLASSIFIER = "Classifier";
    private static final String PROP_PROJECT_URL = "Project-URL";
    private static final String PROP_NAME = "Name";
    private static final String PROP_AUTHOR_EMAIL = "Author-email";
    private final PythonPersistentStorage pythonPersistentStorage;

    public PythonPackageUploadStrategy(
            PackageUploadRequest<PythonRepository> request,
            User requester,
            NewsfeedEventService eventService,
            SubmissionService service,
            PackageValidator<PythonPackage> packageValidator,
            RepositoryService<PythonRepository> repositoryService,
            LocalStorage<PythonPackage> localStorage,
            PackageService<PythonPackage> packageService,
            EmailService emailService,
            BestMaintainerChooser bestMaintainerChooser,
            PythonRepositorySynchronizer repositorySynchronizer,
            SecurityMediator securityMediator,
            PythonPackageDeleter packageDeleter,
            PythonPopulator pythonPopulator,
            PackageMaintainerService maintainerService,
            PythonPersistentStorage pythonPersistentStorage) {
        super(
                request,
                requester,
                eventService,
                packageValidator,
                repositoryService,
                localStorage,
                packageService,
                service,
                emailService,
                bestMaintainerChooser,
                repositorySynchronizer,
                securityMediator,
                packageDeleter,
                pythonPopulator,
                maintainerService,
                pythonPersistentStorage);
        this.pythonPopulator = pythonPopulator;
        this.pythonPersistentStorage = pythonPersistentStorage;
    }

    @Override
    protected PythonPackage parseTechnologySpecificPackageProperties(Properties properties) {
        PythonPackage packageBag;
        if (request.isBinaryPackage()) {
            packageBag = parseTechnologySpecificBinaryPackageProperties(properties);
        } else {
            packageBag = new PythonPackage();
        }

        packageBag.setDescription(properties.getProperty("Description"));
        packageBag.setAuthor(properties.getProperty("Author"));
        packageBag.setAuthorEmail(properties.getProperty(PROP_AUTHOR_EMAIL));
        packageBag.setClassifiers(properties.getProperty(PROP_CLASSIFIER));
        packageBag.setDescriptionContentType(properties.getProperty("Description-Content-Type", ""));
        packageBag.setKeywords(properties.getProperty("Keywords"));
        packageBag.setLicense(getLicense(properties));
        packageBag.setMaintainer(properties.getProperty("Maintainer"));
        packageBag.setMaintainerEmail(properties.getProperty("Maintainer-email"));
        packageBag.setPlatform(properties.getProperty("Platform"));
        packageBag.setProjectUrl(properties.getProperty(PROP_PROJECT_URL));
        packageBag.setUrl(properties.getProperty(PROP_PROJECT_URL));
        packageBag.setProvidesExtra(properties.getProperty("Provides-Extra"));
        packageBag.setRequiresDist(properties.getProperty("Requires-Dist"));
        packageBag.setRequiresExternal(properties.getProperty("Requires-External"));
        packageBag.setRequiresPython(properties.getProperty("Requires-Python"));
        packageBag.setSummary(properties.getProperty("Summary"));
        packageBag.setName(properties.getProperty(PROP_NAME));
        packageBag.setNormalizedName(properties.getProperty(PROP_NAME));
        packageBag.setHomePage(properties.getProperty("Home-page"));
        return packageBag;
    }

    private String getLicense(Properties properties) {
        String licenseExpression = properties.getProperty("License-Expression");
        if (!StringUtils.isAllEmpty(licenseExpression)) {
            return licenseExpression;
        }
        String licenseProperty = properties.getProperty("License");
        if (!StringUtils.isAllEmpty(licenseProperty)) {
            return licenseProperty;
        }
        return getLicenseFromClassifiers(properties);
    }

    private String getLicenseFromClassifiers(Properties properties) {
        String classifiers = properties.getProperty(PROP_CLASSIFIER);
        String classifierName = "License :: ";
        if (classifiers == null || !classifiers.contains(classifierName)) return "";
        String nextClassifier = ",";
        int classifierStartIndex = classifiers.indexOf(classifierName) + classifierName.length();
        String classifiersWithLicense = classifiers.substring(classifierStartIndex);
        int classifierEndIndex = classifiersWithLicense.indexOf(nextClassifier);
        if (classifierEndIndex != -1) {
            return classifiersWithLicense.substring(0, classifierEndIndex);
        } else {
            return classifiersWithLicense;
        }
    }

    @Override
    protected Submission actualStrategy() throws StrategyFailure {
        return super.actualStrategy();
    }

    @Override
    protected void assignRepositoryToPackage(PythonRepository repository, PythonPackage packageBag) {
        packageBag.setRepository(repository);
    }

    @Override
    protected PythonPackage parseTechnologySpecificBinaryPackageProperties(Properties properties) {
        PythonPackage packageBag = new PythonPackage();

        packageBag.setBinary(true);

        final String compatibilityTags = properties.getProperty("Tag");
        packageBag.setCompatibilityTags(compatibilityTags);

        Set<String> pythonTagsSet = new LinkedHashSet<>();
        Set<String> abiTagsSet = new LinkedHashSet<>();
        Set<String> platformTagsSet = new LinkedHashSet<>();

        final String[] splitTags = compatibilityTags.split(",");
        for (String tag : splitTags) {
            final String[] splitTag = tag.split("-");
            if (splitTag.length > 0) {
                pythonTagsSet.add(splitTag[0].trim());
            }
            if (splitTag.length > 1) {
                abiTagsSet.add(splitTag[1].trim());
            }
            if (splitTag.length > 2) {
                platformTagsSet.add(splitTag[2].trim());
            }
        }

        packageBag.setPythonTag(String.join(".", pythonTagsSet));
        packageBag.setAbiTag(String.join(".", abiTagsSet));
        packageBag.setPlatformTag(String.join(".", platformTagsSet));

        final String[] filenameSplit = Objects.requireNonNull(
                        request.getFileData().getOriginalFilename())
                .split("-");
        if (filenameSplit.length == 6) packageBag.setBuildTag(filenameSplit[2]);
        return packageBag;
    }

    @Override
    protected List<File> extractPackageFile(File stored) throws ExtractFileException {
        if (stored.getName().endsWith(".whl"))
            return pythonPopulator.extractWhlPackageFile(stored.getAbsolutePath()).stream()
                    .filter(File::isDirectory)
                    .toList();
        return super.extractPackageFile(stored);
    }

    @Override
    protected void renamePackageFileIfNecessary(
            PythonPackage packageBag, final DataSpecificValidationResult<Submission> validationResult) {
        final List<ValidationResultItem<Submission>> packageDuplicateWarnings =
                validationResult.getDataSpecificWarnings().stream()
                        .filter(w -> w.messageCode().equals(MessageCodes.MISMATCHED_DATA_IN_THE_FILENAME))
                        .toList();

        if (packageDuplicateWarnings.isEmpty()) return;
        try {
            pythonPersistentStorage.renamePackageFileToBeMoreAccurate(packageBag);
        } catch (StoreFileException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException("Could not properly rename package file!");
        }
    }
}

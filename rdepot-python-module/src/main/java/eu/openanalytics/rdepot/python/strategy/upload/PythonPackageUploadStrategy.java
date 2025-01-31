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
package eu.openanalytics.rdepot.python.strategy.upload;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageUploadRequest;
import eu.openanalytics.rdepot.base.email.EmailService;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.upload.DefaultPackageUploadStrategy;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonPackageDeleter;
import eu.openanalytics.rdepot.python.synchronization.PythonRepositorySynchronizer;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;

/**
 * Implementation of upload strategy for R packages.
 */
public class PythonPackageUploadStrategy extends DefaultPackageUploadStrategy<PythonRepository, PythonPackage> {

    public PythonPackageUploadStrategy(
            PackageUploadRequest<PythonRepository> request,
            User requester,
            NewsfeedEventService eventService,
            SubmissionService service,
            PackageValidator<PythonPackage> packageValidator,
            RepositoryService<PythonRepository> repositoryService,
            Storage<PythonRepository, PythonPackage> storage,
            PackageService<PythonPackage> packageService,
            EmailService emailService,
            BestMaintainerChooser bestMaintainerChooser,
            PythonRepositorySynchronizer repositorySynchronizer,
            SecurityMediator securityMediator,
            PythonPackageDeleter packageDeleter) {
        super(
                request,
                requester,
                eventService,
                packageValidator,
                repositoryService,
                storage,
                packageService,
                service,
                emailService,
                bestMaintainerChooser,
                repositorySynchronizer,
                securityMediator,
                packageDeleter);
    }

    @Override
    protected PythonPackage parseTechnologySpecificPackageProperties(Properties properties) {
        PythonPackage packageBag = new PythonPackage();

        packageBag.setDescription(properties.getProperty("Description"));
        String author = (properties.getProperty("Author") + ", " + properties.getProperty("Author-Email")).trim();
        packageBag.setAuthor(author);
        packageBag.setAuthorEmail(properties.getProperty("Author-Email"));
        packageBag.setClassifiers(properties.getProperty("Classifier"));
        packageBag.setDescriptionContentType(properties.getProperty("Description-Content-Type"));
        packageBag.setKeywords(properties.getProperty("Keywords"));
        packageBag.setLicense(properties.getProperty("License"));
        if (StringUtils.isAllEmpty(packageBag.getLicense())) {
            getLicenseFromClassifiers(packageBag);
        }
        packageBag.setMaintainer(properties.getProperty("Maintainer"));
        packageBag.setMaintainerEmail(properties.getProperty("Maintainer-Email"));
        packageBag.setPlatform(properties.getProperty("Platform"));
        packageBag.setProjectUrl(properties.getProperty("Project-URL"));
        packageBag.setUrl(properties.getProperty("Project-URL"));
        packageBag.setProvidesExtra(properties.getProperty("Provides-Extra"));
        packageBag.setRequiresDist(properties.getProperty("Requires-DIST"));
        packageBag.setRequiresExternal(properties.getProperty("Requires-External"));
        packageBag.setRequiresPython(properties.getProperty("Requires-Python"));
        packageBag.setSummary(properties.getProperty("Summary"));
        packageBag.setName(properties.getProperty("Name"));
        return packageBag;
    }

    private void getLicenseFromClassifiers(PythonPackage packageBag) {
        String classifiers = packageBag.getClassifiers();
        String classifierName = "License :: ";
        int classifierStartIndex = classifiers.indexOf(classifierName) + classifierName.length();
        if (classifiers.contains(classifierName)) {
            String nextClassifier = ",";
            String classifiersWithLicense = classifiers.substring(classifierStartIndex);
            int classifierEndIndex = classifiersWithLicense.indexOf(nextClassifier);
            if (classifierEndIndex != -1) {
                String license = classifiersWithLicense.substring(0, classifierEndIndex);
                packageBag.setLicense(license);
            } else {
                packageBag.setLicense(classifiersWithLicense);
            }
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
        throw new UnsupportedOperationException();
    }
}

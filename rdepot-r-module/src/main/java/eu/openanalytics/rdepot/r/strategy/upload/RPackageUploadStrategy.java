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
package eu.openanalytics.rdepot.r.strategy.upload;

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
import eu.openanalytics.rdepot.r.api.v2.dtos.RPackageUploadRequest;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.mediator.deletion.RPackageDeleter;
import eu.openanalytics.rdepot.r.storage.RStorage;
import eu.openanalytics.rdepot.r.storage.exceptions.GenerateManualException;
import eu.openanalytics.rdepot.r.synchronization.RRepositorySynchronizer;
import java.util.Properties;

/**
 * Implementation of upload strategy for R packages.
 */
public class RPackageUploadStrategy extends DefaultPackageUploadStrategy<RRepository, RPackage> {

    private final RStorage rStorage;
    private final RPackageUploadRequest rPackageRequest;

    public RPackageUploadStrategy(
            PackageUploadRequest<RRepository> request,
            User requester,
            NewsfeedEventService eventService,
            SubmissionService service,
            PackageValidator<RPackage> packageValidator,
            RepositoryService<RRepository> repositoryService,
            Storage<RRepository, RPackage> storage,
            PackageService<RPackage> packageService,
            EmailService emailService,
            BestMaintainerChooser bestMaintainerChooser,
            RRepositorySynchronizer repositorySynchronizer,
            SecurityMediator securityMediator,
            RStorage rStorage,
            RPackageDeleter packageDeleter,
            RPackageUploadRequest rPackageRequest) {
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
        this.rStorage = rStorage;
        this.rPackageRequest = rPackageRequest;
    }

    @Override
    protected RPackage parseTechnologySpecificPackageProperties(Properties properties) {
        RPackage packageBag = new RPackage();
        if (rPackageRequest.isBinaryPackage()) packageBag = parseTechnologySpecificBinaryPackageProperties(properties);

        packageBag.setDescription(properties.getProperty("Description"));
        packageBag.setDescriptionContentType("txt");
        packageBag.setDepends(properties.getProperty("Depends"));
        packageBag.setImports(properties.getProperty("Imports"));
        packageBag.setSuggests(properties.getProperty("Suggests"));
        packageBag.setSystemRequirements(properties.getProperty("System Requirements"));
        packageBag.setLicense(properties.getProperty("License"));
        packageBag.setUrl(properties.getProperty("URL"));
        packageBag.setTitle(properties.getProperty("Title"));
        packageBag.setEnhances(properties.getProperty("Enhances"));
        packageBag.setLinkingTo(properties.getProperty("LinkingTo"));
        packageBag.setNeedsCompilation(
                properties.getProperty("NeedsCompilation", "no").equalsIgnoreCase("yes"));
        packageBag.setPriority(properties.getProperty("Priority"));
        packageBag.setMaintainer(properties.getProperty("Maintainer"));

        return packageBag;
    }

    @Override
    protected RPackage parseTechnologySpecificBinaryPackageProperties(Properties properties) {
        RPackage packageBag = new RPackage();

        packageBag.setBinary(true);
        packageBag.setBuilt(properties.getProperty("Built"));
        packageBag.setRVersion(rPackageRequest.getRVersion());
        packageBag.setArchitecture(rPackageRequest.getArchitecture());
        packageBag.setDistribution(rPackageRequest.getDistribution());

        return packageBag;
    }

    @Override
    protected Submission actualStrategy() throws StrategyFailure {
        Submission submission = super.actualStrategy();
        try {
            if (rPackageRequest.isGenerateManual() && !rPackageRequest.isBinaryPackage()) {
                rStorage.generateManual(packageBag);
            }
        } catch (GenerateManualException e) {
            logger.error(e.getMessage(), e);
            super.revertChanges();
            throw new StrategyFailure(e);
        }
        return submission;
    }

    @Override
    protected void assignRepositoryToPackage(RRepository repository, RPackage packageBag) {
        packageBag.setRepository(repository);
    }
}

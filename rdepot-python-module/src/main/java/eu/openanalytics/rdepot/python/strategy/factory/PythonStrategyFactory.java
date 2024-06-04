/*
 * RDepot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.python.strategy.factory;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageUploadRequest;
import eu.openanalytics.rdepot.base.email.EmailService;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.update.UpdateSubmissionStrategy;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.mediator.deletion.PythonPackageDeleter;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import eu.openanalytics.rdepot.python.services.PythonRepositoryService;
import eu.openanalytics.rdepot.python.strategy.create.PythonRepositoryCreateStrategy;
import eu.openanalytics.rdepot.python.strategy.update.PythonPackageUpdateStrategy;
import eu.openanalytics.rdepot.python.strategy.update.PythonRepositoryUpdateStrategy;
import eu.openanalytics.rdepot.python.strategy.upload.PythonPackageUploadStrategy;
import eu.openanalytics.rdepot.python.synchronization.PythonRepositorySynchronizer;
import eu.openanalytics.rdepot.python.validation.PythonPackageValidator;
import eu.openanalytics.rdepot.python.validation.exceptions.PythonRepositoryValidationError;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PythonStrategyFactory {

    private final SubmissionService submissionService;
    private final PythonPackageValidator packageValidator;
    private final NewsfeedEventService newsfeedEventService;
    private final PythonRepositoryService repositoryService;
    private final RepositoryMaintainerService repositoryMaintainerService;
    private final PackageMaintainerService packageMaintainerService;
    private final PythonPackageService packageService;
    private final BestMaintainerChooser bestMaintainerChooser;
    private final PythonRepositorySynchronizer reposiotrySynchronizer;
    private final EmailService emailService;
    private final SecurityMediator securityMediator;
    private final Storage<PythonRepository, PythonPackage> storage;
    private final PythonRepositorySynchronizer repositorySynchronizer;
    private final PythonPackageDeleter packageDeleter;

    public Strategy<Submission> uploadPackageStrategy(PackageUploadRequest<PythonRepository> request, User requester) {
        PythonPackageUploadStrategy strategy = new PythonPackageUploadStrategy(
                request,
                requester,
                newsfeedEventService,
                submissionService,
                packageValidator,
                repositoryService,
                storage,
                packageService,
                emailService,
                bestMaintainerChooser,
                reposiotrySynchronizer,
                securityMediator,
                packageDeleter);
        return strategy;
    }

    public Strategy<PythonPackage> updatePackageStrategy(
            PythonPackage resource, User requester, PythonPackage updatedPackage) {
        PythonPackageUpdateStrategy strategy = new PythonPackageUpdateStrategy(
                resource,
                newsfeedEventService,
                packageService,
                requester,
                updatedPackage,
                storage,
                bestMaintainerChooser,
                repositorySynchronizer);

        return strategy;
    }

    public Strategy<Submission> updateSubmissionStrategy(
            Submission resource, Submission updatedResource, PythonRepository repository, User requester) {
        Strategy<Submission> strategy = new UpdateSubmissionStrategy<PythonPackage, PythonRepository>(
                resource,
                newsfeedEventService,
                submissionService,
                requester,
                updatedResource,
                packageService,
                storage,
                emailService,
                securityMediator,
                reposiotrySynchronizer,
                repository,
                repositoryService);

        return strategy;
    }

    public Strategy<PythonRepository> createRepositoryStrategy(PythonRepository resource, User requester)
            throws PythonRepositoryValidationError {
        Strategy<PythonRepository> strategy =
                new PythonRepositoryCreateStrategy(resource, newsfeedEventService, repositoryService, requester);
        return strategy;
    }

    public Strategy<PythonRepository> updateRepositoryStrategy(
            PythonRepository resource, User requester, PythonRepository updatedRepository) {
        Strategy<PythonRepository> strategy = new PythonRepositoryUpdateStrategy(
                resource,
                newsfeedEventService,
                repositoryService,
                requester,
                updatedRepository,
                new PythonRepository(resource),
                reposiotrySynchronizer,
                repositoryMaintainerService,
                packageMaintainerService,
                packageService);

        return strategy;
    }
}

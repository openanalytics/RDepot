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
package eu.openanalytics.rdepot.r.strategy.factory;

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
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.r.api.v2.dtos.RPackageUploadRequest;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.mediator.deletion.RPackageDeleter;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.storage.RStorage;
import eu.openanalytics.rdepot.r.strategy.create.RRepositoryCreateStrategy;
import eu.openanalytics.rdepot.r.strategy.republish.RRepositoryRepublishStrategy;
import eu.openanalytics.rdepot.r.strategy.update.RPackageUpdateStrategy;
import eu.openanalytics.rdepot.r.strategy.update.RRepositoryUpdateStrategy;
import eu.openanalytics.rdepot.r.strategy.upload.RPackageUploadStrategy;
import eu.openanalytics.rdepot.r.synchronization.RRepositorySynchronizer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RStrategyFactory {

    private final SubmissionService submissionService;
    private final PackageValidator<RPackage> packageValidator;
    private final RRepositoryService repositoryService;
    private final Storage<RRepository, RPackage> storage;
    private final RPackageService packageService;
    private final EmailService emailService;
    private final BestMaintainerChooser bestMaintainerChooser;
    private final NewsfeedEventService newsfeedEventService;
    private final RRepositorySynchronizer repositorySynchronizer;
    private final SecurityMediator securityMediator;
    private final PackageMaintainerService packageMaintainerService;
    private final RepositoryMaintainerService repositoryMaintainerService;
    private final RStorage rStorage;
    private final RPackageDeleter rPackageDeleter;

    public Strategy<Submission> uploadPackageStrategy(RPackageUploadRequest request, User requester) {
        return new RPackageUploadStrategy(
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
                repositorySynchronizer,
                securityMediator,
                rStorage,
                rPackageDeleter,
                request);
    }

    public Strategy<RPackage> updatePackageStrategy(RPackage resource, User requester, RPackage updatedPackage) {
        return new RPackageUpdateStrategy(
                resource,
                newsfeedEventService,
                packageService,
                requester,
                updatedPackage,
                storage,
                bestMaintainerChooser,
                repositorySynchronizer);
    }

    public Strategy<RRepository> createRepositoryStrategy(RRepository resource, User requester) {
        return new RRepositoryCreateStrategy(resource, newsfeedEventService, repositoryService, requester);
    }

    public Strategy<RRepository> updateRepositoryStrategy(
            RRepository resource, User requester, RRepository updatedRepository) {
        return new RRepositoryUpdateStrategy(
                resource,
                newsfeedEventService,
                repositoryService,
                requester,
                updatedRepository,
                new RRepository(resource),
                repositorySynchronizer,
                repositoryMaintainerService,
                packageMaintainerService,
                packageService);
    }

    public Strategy<Submission> updateSubmissionStrategy(
            Submission resource, Submission updatedResource, RRepository repository, User requester) {
        return new UpdateSubmissionStrategy<>(
                resource,
                newsfeedEventService,
                submissionService,
                requester,
                updatedResource,
                packageService,
                storage,
                emailService,
                securityMediator,
                repositorySynchronizer,
                repository,
                repositoryService);
    }

    public Strategy<RRepository> republishRepositoryStrategy(RRepository resource, User requUser) {
        return new RRepositoryRepublishStrategy(
                resource, newsfeedEventService, repositoryService, requUser, repositorySynchronizer);
    }
}

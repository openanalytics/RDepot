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
package eu.openanalytics.rdepot.r.strategy.factory;

import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageUploadRequest;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.EmailService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.update.UpdateSubmissionStrategy;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.mediator.deletion.RPackageDeleter;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.storage.RStorage;
import eu.openanalytics.rdepot.r.strategy.create.RRepositoryCreateStrategy;
import eu.openanalytics.rdepot.r.strategy.update.RPackageUpdateStrategy;
import eu.openanalytics.rdepot.r.strategy.update.RRepositoryUpdateStrategy;
import eu.openanalytics.rdepot.r.strategy.upload.RPackageUploadStrategy;
import eu.openanalytics.rdepot.r.synchronization.RRepositorySynchronizer;

@Component
public class RStrategyFactory {
	
	private final EmailService emailService;
	private final NewsfeedEventService newsfeedEventService;
	private final SubmissionService submissionService;
	private final RPackageService packageService;
	private final RRepositoryService repositoryService;
	private final RRepositorySynchronizer repositorySynchronizer;
	private final Storage<RRepository, RPackage> storage;
	private final PackageValidator<RPackage> packageValidator;	
	private final BestMaintainerChooser bestMaintainerChooser;
	private final SecurityMediator securityMediator;
	private final PackageMaintainerService packageMaintainerService;
	private final RepositoryMaintainerService repositoryMaintainerService;
	private final RStorage rStorage;
	private final RPackageDeleter rPackageDeleter;
	
	public RStrategyFactory(
			SubmissionService submissionService,
			PackageValidator<RPackage> packageValidator,
			RRepositoryService repositoryService, 
			Storage<RRepository, RPackage> storage,
			RPackageService packageService, 
			EmailService emailService, 
			BestMaintainerChooser bestMaintainerChooser,
			NewsfeedEventService newsfeedEventService,
			RRepositorySynchronizer repositorySynchronizer,
			SecurityMediator securityMediator,
			PackageMaintainerService packageMaintainerService,
			RepositoryMaintainerService repositoryMaintainerService,
			RStorage rStorage,
			RPackageDeleter rPackageDeleter) {		
		this.submissionService = submissionService;
		this.packageValidator = packageValidator;
		this.repositoryService = repositoryService;
		this.storage = storage;
		this.packageService = packageService;
		this.emailService = emailService;
		this.bestMaintainerChooser = bestMaintainerChooser;
		this.newsfeedEventService = newsfeedEventService;
		this.repositorySynchronizer = repositorySynchronizer;
		this.securityMediator = securityMediator;
		this.packageMaintainerService = packageMaintainerService;
		this.repositoryMaintainerService = repositoryMaintainerService;
		this.rStorage = rStorage;
		this.rPackageDeleter = rPackageDeleter;
	}

	public Strategy<Submission> uploadPackageStrategy(
			PackageUploadRequest<RRepository> request, 
			User requester) {
		RPackageUploadStrategy strategy = new RPackageUploadStrategy(
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
				rPackageDeleter
		);
		
		return strategy;
	}
	
	public Strategy<RPackage> updatePackageStrategy(RPackage resource, User requester, RPackage updatedPackage) {
		RPackageUpdateStrategy strategy = new RPackageUpdateStrategy(
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
	
	public Strategy<RRepository> createRepositoryStrategy(RRepository resource, User requester) {
		Strategy<RRepository> strategy = new RRepositoryCreateStrategy(
					resource, 
					newsfeedEventService, 
					repositoryService, 
					requester
				);
		
		return strategy;
	}
	
	public Strategy<RRepository> updateRepositoryStrategy(RRepository resource, User requester, RRepository updatedRepository) {
		Strategy<RRepository> strategy = new RRepositoryUpdateStrategy(
					resource, 
					newsfeedEventService, 
					repositoryService, 
					requester, 
					updatedRepository,
					new RRepository(resource),
					repositorySynchronizer,
					repositoryMaintainerService,
					packageMaintainerService,
					packageService
				);
		
		return strategy;
	}

	public Strategy<Submission> updateSubmissionStrategy(Submission resource, 
			Submission updatedResource, RRepository repository, User requester) {
		Strategy<Submission> strategy = new UpdateSubmissionStrategy<RPackage, RRepository>(
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
		
		return strategy;
	}
}

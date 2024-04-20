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
import eu.openanalytics.rdepot.base.storage.exceptions.GenerateManualException;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.upload.DefaultPackageUploadStrategy;
import eu.openanalytics.rdepot.base.validation.PackageValidator;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.mediator.deletion.RPackageDeleter;
import eu.openanalytics.rdepot.r.storage.RStorage;
import eu.openanalytics.rdepot.r.synchronization.RRepositorySynchronizer;

import java.util.Properties;

/**
 * Implementation of upload strategy for R packages.
 */
public class RPackageUploadStrategy extends DefaultPackageUploadStrategy<RRepository, RPackage> {

	private final RStorage rStorage;
	
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
			RPackageDeleter packageDeleter) {
		super(request, 
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
	}

	@Override
	protected RPackage parseTechnologySpecificPackageProperties(Properties properties) {
		RPackage packageBag = new RPackage();
		
		packageBag.setDescription(properties.getProperty("Description"));
		packageBag.setDepends(properties.getProperty("Depends"));
		packageBag.setImports(properties.getProperty("Imports"));
		packageBag.setSuggests(properties.getProperty("Suggests"));
		packageBag.setSystemRequirements(properties.getProperty("System Requirements"));
		packageBag.setLicense(properties.getProperty("License"));
		packageBag.setUrl(properties.getProperty("URL"));
		packageBag.setTitle(properties.getProperty("Title"));
		
		return packageBag;
	}

	@Override
	protected Submission actualStrategy() throws StrategyFailure {
		Submission submission = super.actualStrategy();
		try {
			if(request.isGenerateManual())
				rStorage.generateManual(packageBag);
		} catch (GenerateManualException e) {
			logger.error(e.getMessage(), e);
			throw new StrategyFailure(e);
		}
		return submission;
	}

	@Override
	protected void assignRepositoryToPackage(RRepository repository, RPackage packageBag) {
		packageBag.setRepository(repository);
	}

}

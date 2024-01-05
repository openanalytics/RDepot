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
package eu.openanalytics.rdepot.r.initializer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.exception.AdminNotFound;
import eu.openanalytics.rdepot.base.initializer.RepositoryDataInitializer;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.r.config.props.RRepositoriesProps;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.mediator.deletion.RRepositoryDeleter;
import eu.openanalytics.rdepot.r.mirroring.CranMirror;
import eu.openanalytics.rdepot.r.mirroring.CranMirrorSynchronizer;
import eu.openanalytics.rdepot.r.mirroring.pojos.MirroredRPackage;
import eu.openanalytics.rdepot.r.mirroring.pojos.MirroredRRepository;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.strategy.factory.RStrategyFactory;
import eu.openanalytics.rdepot.r.validation.RRepositoryValidator;

@Component
public class RRepositoryDataInitializer extends RepositoryDataInitializer<RRepository, MirroredRRepository, MirroredRPackage, CranMirror>{
	
	private final RStrategyFactory factory;
	private final UserService userService;
	
	public RRepositoryDataInitializer(RRepositoryService repositoryService,
			RRepositoryValidator repositoryValidator,
			RRepositoryDeleter repositoryDeleter, ThreadPoolTaskScheduler taskScheduler,
			CranMirrorSynchronizer cranMirrorSynchronizer, RRepositoriesProps repositoriesProps,
			RStrategyFactory factory,
			UserService userService) {
		super(repositoryService, repositoryValidator, repositoryDeleter, taskScheduler, cranMirrorSynchronizer);
		this.repositoriesProps = repositoriesProps;
		this.factory = factory;
		this.userService = userService;
	}

	private final RRepositoriesProps repositoriesProps;
	Logger logger = LoggerFactory.getLogger(RRepositoryDataInitializer.class);
	
	public void createRepositoriesFromConfig(boolean declarative) {
//		TODO: temporary
		List<MirroredRRepository> repositories = repositoriesProps.getRepositories();
    	logger.debug("Loading declared repositories...");
    	if(repositories == null) {
    		logger.info("There are no declared repositories!");
    		return;
    	}
    	
    	createRepositoriesFromConfig(repositories, declarative);
    	scheduleMirroring(repositories);
	}

	@Override
	protected RRepository declaredRepositoryToEntity(MirroredRRepository declaredRepository, boolean declarative) {
		RRepository repository = new RRepository();
		repository.setName(declaredRepository.getName());
		repository.setPublicationUri(declaredRepository.getPublicationUri());
		repository.setServerAddress(declaredRepository.getServerAddress());
		
		if(declarative) {
			boolean deleted, published;
			if(declaredRepository.isDeleted() == null)
				deleted = false;
			else
				deleted = declaredRepository.isDeleted();			
			if(declaredRepository.isPublished() == null) 
				published = true;
			else
				published = declaredRepository.isPublished();
			
			repository.setDeleted(deleted);
			repository.setPublished(published);
		}		
		
		return repository;
	}

	@Override
	protected void updateRepository(RRepository newRepository, RRepository existingRepository) {
		try {
			User requester = userService.findFirstAdmin();
			factory.updateRepositoryStrategy(existingRepository, requester, newRepository).perform();
		} catch (AdminNotFound e1) {
			logger.error("When trying to create a preconfigured repositories, we couldn't find any valid administrator");
		} catch (StrategyFailure e) {
			logger.error("We tried to update " + existingRepository.getName() + " repository from preconfigured " 
					+ "repositories but unexpected error occured");

		}
		
	}
	
}

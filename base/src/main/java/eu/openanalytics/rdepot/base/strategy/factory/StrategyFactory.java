/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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
package eu.openanalytics.rdepot.base.strategy.factory;

import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.service.CommonPackageService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.create.CreatePackageMaintainerStrategy;
import eu.openanalytics.rdepot.base.strategy.create.CreateRepositoryMaintainerStrategy;
import eu.openanalytics.rdepot.base.strategy.update.UpdatePackageMaintainerStrategy;
import eu.openanalytics.rdepot.base.strategy.update.UpdateRepositoryMaintainerStrategy;
import eu.openanalytics.rdepot.base.strategy.update.UpdateUserStrategy;

/**
 * This factory creates strategies with necessary beans supplied.
 */
@Component
public class StrategyFactory {
	
	private final PackageMaintainerService packageMaintainerService;
	private final RepositoryMaintainerService repositoryMaintainerService;
	private final UserService userService;
	private final CommonPackageService packageService;
	private final BestMaintainerChooser bestMaintainerChooser;	
	private final NewsfeedEventService newsfeedEventService;
	
	public StrategyFactory(PackageMaintainerService packageMaintainerService,
			CommonPackageService packageService, 
			BestMaintainerChooser bestMaintainerChooser, 
			RepositoryMaintainerService repositoryMaintainerService,
			UserService userService, NewsfeedEventService newsfeedEventService,
			SubmissionService submissionService) {		
		this.packageMaintainerService = packageMaintainerService;
		this.packageService = packageService;
		this.bestMaintainerChooser = bestMaintainerChooser;
		this.repositoryMaintainerService = repositoryMaintainerService;
		this.userService = userService;
		this.newsfeedEventService = newsfeedEventService;
	}

	public Strategy<PackageMaintainer> createPackageMaintainerStrategy(PackageMaintainer resource, User requester) {
	    Strategy<PackageMaintainer> strategy = new CreatePackageMaintainerStrategy(
	        resource, 
	        newsfeedEventService, 
	        packageMaintainerService, 
	        requester, 
	        packageService, 
	        bestMaintainerChooser);
	    
	    return strategy;
	}
	  
	public Strategy<PackageMaintainer> updatePackageMaintainerStrategy(
			PackageMaintainer resource, User requester, PackageMaintainer updatedResource) {
	    Strategy<PackageMaintainer> strategy = new UpdatePackageMaintainerStrategy(
	        resource, 
	        newsfeedEventService, 
	        packageMaintainerService, 
	        requester, 
	        updatedResource, 
	        packageService, 
	        bestMaintainerChooser);
	    
	    return strategy;
	}
	
//	public Strategy<PackageMaintainer> deletePackageMaintainerStrategy(PackageMaintainer resource, User requester) {
//		Strategy<PackageMaintainer> strategy = new DeletePackageMaintainerStrategy(
//				resource, 
//				packageMaintainerNewsfeedEventService, 
//				packageMaintainerService,
//				requester, 
//				packageService);
//		
//		
//		return strategy;
//	}
	
	public Strategy<RepositoryMaintainer> createRepositoryMaintainerStrategy(RepositoryMaintainer resource, User requester) {
		Strategy<RepositoryMaintainer> strategy = new CreateRepositoryMaintainerStrategy(
				resource, 
				newsfeedEventService, 
				repositoryMaintainerService, 
				requester, 
				packageService, 
				bestMaintainerChooser);
		
		return strategy;
	}
	
	public Strategy<RepositoryMaintainer> updateRepositoryMaintainerStrategy(RepositoryMaintainer resource, User requester, RepositoryMaintainer updatedResource) {
		Strategy<RepositoryMaintainer> strategy = new UpdateRepositoryMaintainerStrategy(
				resource, 
				newsfeedEventService, 
				repositoryMaintainerService, 
				requester, 
				updatedResource,				
				packageService, bestMaintainerChooser);
		
		return strategy;
	}
	
	public Strategy<User> updateUserStrategy(User resource, User requester, User updatedResource) {
		Strategy<User> strategy = new UpdateUserStrategy(
				resource, 
				newsfeedEventService, 
				userService, 
				requester, 
				updatedResource, 				
				packageService, 
				bestMaintainerChooser, 
				repositoryMaintainerService, 
				packageMaintainerService);
		
		return strategy;
	}
	
	
//	public Strategy<RepositoryMaintainer> deleteRepositoryMaintainerStrategy(RepositoryMaintainer resource, User requester) {
//		Strategy<RepositoryMaintainer> strategy = new DeleteRepositoryMaintainerStrategy(
//				resource, 
//				repositoryMaintainerNewsfeedEventService, 
//				repositoryMaintainerService, 
//				requester);
//		
//		return strategy;
//	}
}

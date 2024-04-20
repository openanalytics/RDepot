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
package eu.openanalytics.rdepot.base.strategy.factory;

import org.springframework.stereotype.Component;

import eu.openanalytics.rdepot.base.entities.AccessToken;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.entities.UserSettings;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.service.AccessTokenService;
import eu.openanalytics.rdepot.base.service.CommonPackageService;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.UserService;
import eu.openanalytics.rdepot.base.service.UserSettingsService;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.create.CreateAccessTokenStrategy;
import eu.openanalytics.rdepot.base.strategy.create.CreatePackageMaintainerStrategy;
import eu.openanalytics.rdepot.base.strategy.create.CreateRepositoryMaintainerStrategy;
import eu.openanalytics.rdepot.base.strategy.update.UpdateAccessTokenStrategy;
import eu.openanalytics.rdepot.base.strategy.update.UpdatePackageMaintainerStrategy;
import eu.openanalytics.rdepot.base.strategy.update.UpdateRepositoryMaintainerStrategy;
import eu.openanalytics.rdepot.base.strategy.update.UpdateUserSettingsStrategy;
import eu.openanalytics.rdepot.base.strategy.update.UpdateUserStrategy;
import lombok.AllArgsConstructor;

/**
 * This factory creates strategies with necessary beans supplied.
 */
@Component
@AllArgsConstructor
public class StrategyFactory {
	
	private final PackageMaintainerService packageMaintainerService;
	private final CommonPackageService packageService;
	private final BestMaintainerChooser bestMaintainerChooser;	
	private final RepositoryMaintainerService repositoryMaintainerService;
	private final UserService userService;
	private final UserSettingsService userSettingsService;
	private final NewsfeedEventService newsfeedEventService;
	private final AccessTokenService accessTokenService;
	
	public Strategy<PackageMaintainer> createPackageMaintainerStrategy(PackageMaintainer resource, User requester) {
	    return new CreatePackageMaintainerStrategy(
	        resource, 
	        newsfeedEventService, 
	        packageMaintainerService, 
	        requester, 
	        packageService, 
	        bestMaintainerChooser);
	}
	  
	public Strategy<PackageMaintainer> updatePackageMaintainerStrategy(
			PackageMaintainer resource, User requester, PackageMaintainer updatedResource) {
	    return new UpdatePackageMaintainerStrategy(
	        resource, 
	        newsfeedEventService, 
	        packageMaintainerService, 
	        requester, 
	        updatedResource, 
	        packageService, 
	        bestMaintainerChooser);
	}
	
	public Strategy<RepositoryMaintainer> createRepositoryMaintainerStrategy(RepositoryMaintainer resource, User requester) {
		return new CreateRepositoryMaintainerStrategy(
				resource, 
				newsfeedEventService, 
				repositoryMaintainerService, 
				requester, 
				packageService, 
				bestMaintainerChooser);
	}
	
	public Strategy<RepositoryMaintainer> updateRepositoryMaintainerStrategy(RepositoryMaintainer resource, User requester, RepositoryMaintainer updatedResource) {
		return new UpdateRepositoryMaintainerStrategy(
				resource, 
				newsfeedEventService, 
				repositoryMaintainerService, 
				requester, 
				updatedResource,				
				packageService, bestMaintainerChooser);
	}
	
	public Strategy<User> updateUserStrategy(User resource, User requester, User updatedResource) {
		return new UpdateUserStrategy(
				resource, 
				newsfeedEventService, 
				userService, 
				requester, 
				updatedResource, 				
				packageService, 
				bestMaintainerChooser, 
				repositoryMaintainerService, 
				packageMaintainerService);
	}
	
	public Strategy<UserSettings> updateUserSettingsStrategy(UserSettings resource, User requester,
			UserSettings updatedResource, boolean toCreate) {
		return new UpdateUserSettingsStrategy(
				resource, 				
				userSettingsService,
				newsfeedEventService,
				requester, 
				updatedResource,
				toCreate
				);
	}
	
	public Strategy<AccessToken> createAccessTokenStrategy(AccessToken resource, User requester){
		return new CreateAccessTokenStrategy(
				resource, 
				requester, 
				newsfeedEventService, 
				accessTokenService);
	}
	
	public Strategy<AccessToken> updateAccessTokenStrategy(
			AccessToken resource, User requester, AccessToken updatedResource) {
	    return new UpdateAccessTokenStrategy(
	        resource, 
	        requester,
	        updatedResource,
			newsfeedEventService, 
			accessTokenService);
	}
}

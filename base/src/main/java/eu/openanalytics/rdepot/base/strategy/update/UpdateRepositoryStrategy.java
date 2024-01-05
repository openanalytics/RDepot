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
package eu.openanalytics.rdepot.base.strategy.update;

import eu.openanalytics.rdepot.base.entities.EventChangedVariable;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyReversionFailure;
import eu.openanalytics.rdepot.base.synchronization.RepositorySynchronizer;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.base.time.DateProvider;

public abstract class UpdateRepositoryStrategy<T extends Repository<T, ?>> extends UpdateStrategy<T> {

	private final RepositorySynchronizer<T> repositorySynchronizer;
	private final RepositoryMaintainerService repositoryMaintainerService;
	private final PackageMaintainerService packageMaintainerService;
	private final PackageService<?> packageService;
	private final RepositoryService<T> repositoryService;
	
	protected UpdateRepositoryStrategy(
			T resource, 
			NewsfeedEventService eventService,
			RepositoryService<T> service, 
			User requester, 
			T updatedResource, 
			T oldResourceCopy,
			RepositorySynchronizer<T> repositorySynchronizer,
			RepositoryMaintainerService repositoryMaintainerService,
			PackageMaintainerService packageMaintainerService,
			PackageService<?> packageService) {
		super(resource, service, eventService, requester, 
				updatedResource, oldResourceCopy);
		this.repositorySynchronizer = repositorySynchronizer;
		this.repositoryMaintainerService = repositoryMaintainerService;
		this.packageMaintainerService = packageMaintainerService;
		this.packageService = packageService;
		this.repositoryService = service;
	}

	@Override
	protected T actualStrategy() throws StrategyFailure {
		//TODO: What about read-only values? like name or id
		if(!resource.getPublicationUri().equals(updatedResource.getPublicationUri())) {
			resource.setPublicationUri(updatedResource.getPublicationUri());
			changedValues.add(new EventChangedVariable("publicationUri", 
					oldResourceCopy.getPublicationUri(), resource.getPublicationUri()));
		}
		if(!resource.getServerAddress().equals(updatedResource.getServerAddress())) {
			resource.setServerAddress(updatedResource.getServerAddress());
			changedValues.add(new EventChangedVariable("serverAddress", 
					oldResourceCopy.getServerAddress(), resource.getServerAddress()));
		}
		if(resource.isDeleted() != updatedResource.isDeleted()) {
			resource.setDeleted(updatedResource.isDeleted());
			changedValues.add(new EventChangedVariable("deleted", 
					Boolean.toString(oldResourceCopy.isDeleted()), 
					Boolean.toString(resource.isDeleted())));
			softDelete(resource, updatedResource); //TODO: this is a bug! Test it
		}
		if(resource.isPublished() != updatedResource.isPublished()) {
			resource.setPublished(updatedResource.isPublished());
			changedValues.add(new EventChangedVariable("published", 
					Boolean.toString(oldResourceCopy.isPublished()), 
					Boolean.toString(resource.isPublished())));
		}
		if(!resource.getName().equals(updatedResource.getName())) {
			resource.setName(updatedResource.getName());
			changedValues.add(new EventChangedVariable("name", oldResourceCopy.getName(), 
					updatedResource.getName()));
		}
		repositoryService.incrementVersion(resource);
		return resource;
	}
	
	private void softDelete(T repository, T updatedRepository) {
		updatedResource.setPublished(false);
		softDeleteRepositoryMaintainers(repository);
		softDeletePackageMaintainers(repository);
		softDeletePackages(repository);
	}

	private void softDeletePackages(T repository) {
		for(Package<?,?> packageBag : packageService.findAllByRepository(repository)) {
			packageBag.setDeleted(true);
			packageBag.setActive(false);
		}
	}

	private void softDeletePackageMaintainers(T repository) {
		for(PackageMaintainer maintainer : 
			packageMaintainerService.findByRepositoryNonDeleted(repository)) {
			maintainer.setDeleted(true); //TODO: Should we notify about deleted maintainers?
		}
	}

	private void softDeleteRepositoryMaintainers(T repository) {
		for(RepositoryMaintainer maintainer : 
			repositoryMaintainerService.findByRepository(repository)) {
			maintainer.setDeleted(true); //TODO: Should we notify about deleted maintainers?
		}
	}

	@Override
	protected void postStrategy() throws StrategyFailure {
		if(resource.isPublished()) {
			try {
				repositorySynchronizer.storeRepositoryOnRemoteServer(resource, 
						DateProvider.getCurrentDateStamp());
			} catch (SynchronizeRepositoryException e) {
				logger.error(e.getMessage(), e);
				throw new StrategyFailure(e, false); //TODO: What about file-system issue in local container?
			}
		}
	}

	@Override
	public void revertChanges() throws StrategyReversionFailure {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected NewsfeedEvent generateEvent(T resource) {
		return new NewsfeedEvent(requester, NewsfeedEventType.UPDATE, resource);
	}
}

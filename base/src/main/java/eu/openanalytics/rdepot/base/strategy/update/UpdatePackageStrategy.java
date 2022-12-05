/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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

import org.apache.commons.lang3.NotImplementedException;

import eu.openanalytics.rdepot.base.entities.EventChangedVariable;
import eu.openanalytics.rdepot.base.entities.NewsfeedEvent;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.event.NewsfeedEventType;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.mediator.deletion.exceptions.NoSuitableMaintainerFound;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.Service;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.storage.exceptions.InvalidSourceException;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyReversionFailure;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;

public abstract class UpdatePackageStrategy
	<P extends Package<P, ?>> extends UpdateStrategy<P> {

	protected final Storage<?, P> storage;
	protected final BestMaintainerChooser bestMaintainerChooser;
	
	protected UpdatePackageStrategy(P resource, 
			NewsfeedEventService eventService, 
			Service<P> service, User requester,
			P updatedPackage, P oldResourceCopy, Storage<?, P> storage, 
			BestMaintainerChooser bestMaintainerChooser) {
		super(resource, service, eventService, requester, updatedPackage, oldResourceCopy);
		this.storage = storage;
		this.bestMaintainerChooser = bestMaintainerChooser;
	}
	
	@Override
	protected P actualStrategy() throws StrategyFailure {
		boolean shouldRefreshMaintainer = false;
		try {
			if(updatedResource.isActive() != resource.isActive()) {
				if(!updatedResource.isActive()) {
					deactivatePackage(resource);
				} else {
					activatePackage(resource);
				}
				shouldRefreshMaintainer = true;
			}
			if(updatedResource.isDeleted() != resource.isDeleted()) {
				if(updatedResource.isDeleted()) {
					delete(resource);
				} else {
					throw new NotImplementedException();
				}
			}
			if(updatedResource.getSource() != null && resource.getSource() != null) {
				if(!updatedResource.getSource().equals(resource.getSource())) {
					//TODO: Should it even be possible? If so - who should be able to do that?
					updateSource(resource, updatedResource.getSource());
				}
			}
			
			if(shouldRefreshMaintainer) {
				updateUser(resource, bestMaintainerChooser.chooseBestPackageMaintainer(resource)); //TODO: Should we add it to events?
			}
		} catch(NotImplementedException | InvalidSourceException | NoSuitableMaintainerFound e) {
			throw new StrategyFailure(e, true);
		}
		
		return resource;
	}
	
	protected void updateUser(P resource, User user) {
		changedValues.add(new EventChangedVariable("user", resource.getUser().toString(), user.toString()));
		resource.setUser(user);
	}
	
	protected void updateSource(P resource, String source) throws InvalidSourceException {
		storage.verifySource(resource, source);
		changedValues.add(new EventChangedVariable("source", resource.getSource(), source));
		resource.setSource(source);
	}

	protected void activatePackage(P resource) {
		changedValues.add(new EventChangedVariable("active", "false", "true"));
		resource.setActive(true);
	}

	protected void deactivatePackage(P packageBag) {
		changedValues.add(new EventChangedVariable("active", "true", "false"));
		resource.setActive(false);
	}
	
	protected void delete(P packageBag) {
		changedValues.add(new EventChangedVariable("deleted", "false", "true"));
		packageBag.setActive(false);
		packageBag.setDeleted(true);
	}
	
	@Override
	protected void postStrategy() throws StrategyFailure {
		try {
			if(processedResource.getRepository().isPublished())
				publishPackageRepository(processedResource);
		} catch (SynchronizeRepositoryException e) {
			logger.error(e.getMessage(), e);
			throw new StrategyFailure(e, false);
		}	
	}
	
	protected abstract void publishPackageRepository(P packageBag) throws SynchronizeRepositoryException;
	
	@Override
	public void revertChanges() throws StrategyReversionFailure {
		//TODO: Implement
	}
	
	@Override
	protected NewsfeedEvent generateEvent(P resource) {
		return new NewsfeedEvent(requester, NewsfeedEventType.UPDATE, requester);
	}
}

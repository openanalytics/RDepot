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
package eu.openanalytics.rdepot.base.strategy.update;

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
import org.apache.commons.lang3.NotImplementedException;

/**
 * Updates {@link Package}.
 * If situation requires, its maintainer will be refreshed.
 * @param <P> Technology-specific Package class
 */
public abstract class UpdatePackageStrategy<P extends Package> extends UpdateStrategy<P> {

    protected final Storage<?, P> storage;
    protected final BestMaintainerChooser bestMaintainerChooser;

    protected UpdatePackageStrategy(
            P resource,
            NewsfeedEventService eventService,
            Service<P> service,
            User requester,
            P updatedPackage,
            P oldResourceCopy,
            Storage<?, P> storage,
            BestMaintainerChooser bestMaintainerChooser) {
        super(resource, service, eventService, requester, updatedPackage, oldResourceCopy);
        this.storage = storage;
        this.bestMaintainerChooser = bestMaintainerChooser;
    }

    @Override
    protected P actualStrategy() throws StrategyFailure {
        boolean shouldRefreshMaintainer = false;
        try {
            if (updatedResource.isActive() != resource.isActive()) {
                if (!updatedResource.isActive()) {
                    deactivatePackage(resource);
                } else {
                    activatePackage(resource);
                }
                shouldRefreshMaintainer = true;
            }
            if (updatedResource.isDeleted() && !resource.isDeleted()) {
                delete(resource);
            }
            if (updatedResource.getSource() != null
                    && resource.getSource() != null
                    && !updatedResource.getSource().equals(resource.getSource())) {
                updateSource(resource, updatedResource.getSource());
            }

            if (shouldRefreshMaintainer) {
                final User oldMaintainer = resource.getUser();
                final User refreshedMaintainer = bestMaintainerChooser.chooseBestPackageMaintainer(resource);
                if (!oldMaintainer.equals(refreshedMaintainer)) updateUser(resource, refreshedMaintainer);
            }
        } catch (NotImplementedException | InvalidSourceException | NoSuitableMaintainerFound e) {
            throw new StrategyFailure(e, true);
        }

        return resource;
    }

    protected void updateUser(P resource, User user) {
        changedValues.add(new EventChangedVariable("user", resource.getUser().toString(), user.toString()));
        resource.setUser(user);
    }

    protected void updateSource(P resource, String source) throws InvalidSourceException {
        changedValues.add(new EventChangedVariable("source", resource.getSource(), source));
        resource.setSource(source);
    }

    protected void activatePackage(P resource) {
        changedValues.add(new EventChangedVariable("active", Boolean.FALSE.toString(), Boolean.TRUE.toString()));
        resource.setActive(true);
    }

    protected void deactivatePackage(P resource) {
        changedValues.add(new EventChangedVariable("active", Boolean.TRUE.toString(), Boolean.FALSE.toString()));
        resource.setActive(false);
    }

    protected void delete(P packageBag) {
        changedValues.add(new EventChangedVariable("deleted", Boolean.FALSE.toString(), Boolean.TRUE.toString()));
        packageBag.setActive(false);
        packageBag.setDeleted(true);
    }

    @Override
    public void postStrategy() throws StrategyFailure {
        try {
            if (processedResource.getRepository().getPublished()) publishPackageRepository(processedResource);
        } catch (SynchronizeRepositoryException e) {
            throw new StrategyFailure(e, false);
        }
    }

    /**
     * This method should trigger repository (re)publication.
     */
    protected abstract void publishPackageRepository(P packageBag) throws SynchronizeRepositoryException;

    @Override
    public void revertChanges() throws StrategyReversionFailure {}

    @Override
    protected NewsfeedEvent generateEvent(P resource) {
        return new NewsfeedEvent(requester, NewsfeedEventType.UPDATE, resource);
    }
}

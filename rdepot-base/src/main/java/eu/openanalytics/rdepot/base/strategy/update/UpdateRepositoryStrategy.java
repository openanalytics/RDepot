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
import java.time.Instant;
import java.util.Objects;

/**
 * Updates {@link Repository}, increments its version
 * and republishes if the corresponding property is set to <code>true</code>.
 * @param <T> Technology-specific {@link Repository} class
 */
public abstract class UpdateRepositoryStrategy<T extends Repository> extends UpdateStrategy<T> {

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
        super(resource, service, eventService, requester, updatedResource, oldResourceCopy);
        this.repositorySynchronizer = repositorySynchronizer;
        this.repositoryMaintainerService = repositoryMaintainerService;
        this.packageMaintainerService = packageMaintainerService;
        this.packageService = packageService;
        this.repositoryService = service;
    }

    @Override
    protected T actualStrategy() throws StrategyFailure {
        if (!resource.getPublicationUri().equals(updatedResource.getPublicationUri())) {
            resource.setPublicationUri(updatedResource.getPublicationUri());
            changedValues.add(new EventChangedVariable(
                    "publicationUri", oldResourceCopy.getPublicationUri(), resource.getPublicationUri()));
        }
        if (!resource.getServerAddress().equals(updatedResource.getServerAddress())) {
            resource.setServerAddress(updatedResource.getServerAddress());
            changedValues.add(new EventChangedVariable(
                    "serverAddress", oldResourceCopy.getServerAddress(), resource.getServerAddress()));
        }
        if (!resource.isDeleted() && updatedResource.isDeleted()) {
            resource.setDeleted(updatedResource.isDeleted());
            changedValues.add(new EventChangedVariable(
                    "deleted", Boolean.toString(oldResourceCopy.isDeleted()), Boolean.toString(resource.isDeleted())));
            softDelete(resource);
        }
        if (!Objects.equals(resource.getPublished(), updatedResource.getPublished())) {
            resource.setPublished(updatedResource.getPublished());
            changedValues.add(new EventChangedVariable(
                    "published",
                    Boolean.toString(oldResourceCopy.getPublished()),
                    Boolean.toString(resource.getPublished())));
        }
        if (!resource.getName().equals(updatedResource.getName())) {
            resource.setName(updatedResource.getName());
            changedValues.add(new EventChangedVariable("name", oldResourceCopy.getName(), updatedResource.getName()));
        }
        resource.setLastModifiedTimestamp(Instant.now());
        repositoryService.incrementVersion(resource);
        return resource;
    }

    private void softDelete(T repository) {
        updatedResource.setPublished(false);
        softDeleteRepositoryMaintainers(repository);
        softDeletePackageMaintainers(repository);
        softDeletePackages(repository);
    }

    private void softDeletePackages(T repository) {
        for (Package packageBag : packageService.findAllByRepository(repository)) {
            packageBag.setDeleted(true);
            packageBag.setActive(false);
        }
    }

    private void softDeletePackageMaintainers(T repository) {
        for (PackageMaintainer maintainer : packageMaintainerService.findByRepositoryNonDeleted(repository)) {
            maintainer.setDeleted(true);
        }
    }

    private void softDeleteRepositoryMaintainers(T repository) {
        for (RepositoryMaintainer maintainer : repositoryMaintainerService.findByRepository(repository)) {
            maintainer.setDeleted(true);
        }
    }

    @Override
    protected void postStrategy() throws StrategyFailure {
        if (resource.getPublished()) {
            try {
                repositorySynchronizer.storeRepositoryOnRemoteServer(resource);
            } catch (SynchronizeRepositoryException e) {
                throw new StrategyFailure(e); // TODO: #32973 What about file-system issue in local container?
            }
        }
    }

    @Override
    public void revertChanges() throws StrategyReversionFailure {}

    @Override
    protected NewsfeedEvent generateEvent(T resource) {
        return new NewsfeedEvent(requester, NewsfeedEventType.UPDATE, resource);
    }
}

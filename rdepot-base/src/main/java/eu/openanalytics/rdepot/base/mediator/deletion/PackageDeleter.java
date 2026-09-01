/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
package eu.openanalytics.rdepot.base.mediator.deletion;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.storage.LocalStorage;
import eu.openanalytics.rdepot.base.storage.PersistentStorage;
import eu.openanalytics.rdepot.base.storage.exceptions.DeleteFileException;
import eu.openanalytics.rdepot.base.storage.population.Populator;
import eu.openanalytics.rdepot.base.synchronization.RepositorySynchronizer;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.base.utils.PackageRepositoryResolver;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Permanently deletes {@link Package Packages}.
 * Should be implemented by a technology.
 * @param <P> technology-specific {@link Package}
 * @param <R> technology-specific {@link Repository}
 */
@Slf4j
public abstract class PackageDeleter<P extends Package, R extends Repository> extends ResourceDeleter<P> {

    protected final Populator<?, P> populator;
    protected final LocalStorage<P> localStorage;
    protected final SubmissionService submissionService;
    protected final RepositorySynchronizer<R> repositorySynchronizer;
    protected final PackageRepositoryResolver<R, P> packageRepositoryResolver;
    protected final PackageMaintainerService maintainerService;
    protected final PersistentStorage<P, R> persistentStorage;

    protected PackageDeleter(
            NewsfeedEventService newsfeedEventService,
            PackageService<P> resourceService,
            Populator<?, P> populator,
            LocalStorage<P> localStorage,
            SubmissionService submissionService,
            RepositorySynchronizer<R> repositorySynchronizer,
            PackageRepositoryResolver<R, P> packageRepositoryResolver,
            PackageMaintainerService maintainerService,
            PersistentStorage<P, R> persistentStorage) {
        super(newsfeedEventService, resourceService);
        this.populator = populator;
        this.localStorage = localStorage;
        this.submissionService = submissionService;
        this.repositorySynchronizer = repositorySynchronizer;
        this.packageRepositoryResolver = packageRepositoryResolver;
        this.maintainerService = maintainerService;
        this.persistentStorage = persistentStorage;
    }

    private void synchronizeRepository(P resource) throws SynchronizeRepositoryException {
        if (resource.getRepository().getPublished()) {
            repositorySynchronizer.storeRepositoryOnRemoteServer(
                    packageRepositoryResolver.getRepositoryForPackage(resource));
        }
    }

    @Transactional
    public void deleteAndSynchronize(P resource) throws DeleteEntityException, SynchronizeRepositoryException {
        delete(resource);
        synchronizeRepository(resource);
    }

    @Override
    public void delete(P packageBag) throws DeleteEntityException {
        if (packageBag.getSource() == null || packageBag.getSource().isBlank()) {
            log.warn("Deleting package with empty source property: {}", packageBag);
            deleteFromDatabase(packageBag);
            return;
        }
        try {
            deleteFromDatabase(packageBag);
            persistentStorage.deleteAllPackageFilesIfExist(packageBag);
        } catch (DeleteFileException | DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new DeleteEntityException();
        }
    }

    protected void deleteFromDatabase(P packageBag) throws DeleteEntityException {
        newsfeedEventService.deleteRelatedEvents(packageBag.getSubmission());
        newsfeedEventService.deleteRelatedEvents(packageBag);
        submissionService.delete(packageBag.getSubmission());

        List<PackageMaintainer> maintainers =
                maintainerService.findAllByPackageNameAndRepository(packageBag.getName(), packageBag.getRepository());
        maintainers.forEach(maintainer -> maintainer.getPackages().remove(packageBag));
    }

    @Transactional
    public void deleteTransactional(int id) throws DeleteEntityException {
        delete(id);
    }

    @Transactional
    public void deleteForSubmission(Submission submission) throws DeleteEntityException {
        delete(submission.getPackage().getId());
    }

    private void delete(int id) throws DeleteEntityException {
        delete(resourceService.findById(id).orElseThrow(DeleteEntityException::new));
    }
}

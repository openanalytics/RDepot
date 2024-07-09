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
package eu.openanalytics.rdepot.base.mediator.deletion;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.storage.exceptions.MovePackageSourceException;
import eu.openanalytics.rdepot.base.storage.exceptions.SourceFileDeleteException;
import eu.openanalytics.rdepot.base.synchronization.RepositorySynchronizer;
import eu.openanalytics.rdepot.base.synchronization.SynchronizeRepositoryException;
import eu.openanalytics.rdepot.base.utils.PackageRepositoryResolver;
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

    protected final Storage<?, P> storage;
    protected final SubmissionService submissionService;
    protected final RepositorySynchronizer<R> repositorySynchronizer;
    protected final PackageRepositoryResolver<R, P> packageRepositoryResolver;

    public PackageDeleter(
            NewsfeedEventService newsfeedEventService,
            PackageService<P> resourceService,
            Storage<?, P> storage,
            SubmissionService submissionService,
            RepositorySynchronizer<R> repositorySynchronizer,
            PackageRepositoryResolver<R, P> packageRepositoryResolver) {
        super(newsfeedEventService, resourceService);
        this.storage = storage;
        this.submissionService = submissionService;
        this.repositorySynchronizer = repositorySynchronizer;
        this.packageRepositoryResolver = packageRepositoryResolver;
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
            log.warn("Deleting package with empty source property: " + packageBag.toString());
            deleteFromDatabase(packageBag);
            return;
        }

        String recycledPackageSourcePath;
        final String oldPackageSourcePath = packageBag.getSource();
        try {
            recycledPackageSourcePath = storage.moveToTrashDirectory(packageBag);
            packageBag.setSource(recycledPackageSourcePath);

            deleteFromDatabase(packageBag);

            storage.removePackageSource(recycledPackageSourcePath);
        } catch (MovePackageSourceException | SourceFileDeleteException e) {
            log.error(e.getMessage(), e);
            throw new DeleteEntityException();
        } catch (DataAccessException dae) {
            log.error(dae.getMessage(), dae);
            try {
                packageBag.setSource(storage.moveSource(packageBag, oldPackageSourcePath));
            } catch (MovePackageSourceException mpse) {
                log.error("Could not restore package source after failed delete!");
                log.error(mpse.getMessage(), mpse);
            }
            throw new DeleteEntityException();
        }
    }

    protected void deleteFromDatabase(P packageBag) throws DeleteEntityException {
        newsfeedEventService.deleteRelatedEvents(packageBag.getSubmission());
        newsfeedEventService.deleteRelatedEvents(packageBag);
        submissionService.delete(packageBag.getSubmission());
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

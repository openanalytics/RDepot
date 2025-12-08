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
package eu.openanalytics.rdepot.base.mirroring;

import eu.openanalytics.rdepot.base.config.declarative.DeclarativeConfigurationSource;
import eu.openanalytics.rdepot.base.entities.PackageSynchronizationStatus;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositorySynchronizationStatus;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredPackage;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredRepository;
import eu.openanalytics.rdepot.base.mirroring.pojos.SynchronizationStatus;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import org.springframework.scheduling.annotation.Async;

/**
 * It provides mirroring of external repositories.
 * Packages specified as mirrored will be regularly updated
 * as defined in the configuration file.
 */
public abstract class MirrorSynchronizer<
        R extends MirroredRepository<P, M>, P extends MirroredPackage, M extends Mirror<P>> {

    private final DeclarativeConfigurationSource<R, P, M> declarativeConfigurationSource;

    @Getter
    private final Map<Integer, RepositorySynchronizationStatus> synchronizationStatuses;

    protected MirrorSynchronizer(DeclarativeConfigurationSource<R, P, M> declarativeConfigurationSource) {
        this.synchronizationStatuses = new HashMap<>();
        this.declarativeConfigurationSource = declarativeConfigurationSource;
    }

    public RepositorySynchronizationStatus getSynchronizationStatus(int id) {
        return synchronizationStatuses.get(id);
    }

    /**
     * Synchronizes a given repository with its external counterpart.
     * The method is asynchronous, status of synchronization can be obtained
     * with {@link #getSynchronizationStatusList()} method.
     * @param repository repository to synchronize
     */
    @Async
    public abstract void synchronizeAsync(R repository, M mirror);

    /**
     * Fetches mirrors from configuration file for a given repository
     */
    public Set<M> findByRepository(Repository repository) {
        List<R> declaredRepositories = declarativeConfigurationSource.retrieveDeclaredRepositories();
        Set<M> mirrors = null;
        for (R declaredRepository : declaredRepositories) {
            if (declaredRepository.getName().equals(repository.getName())
                    && declaredRepository.getTechnology().equals(repository.getTechnology())) {
                mirrors = declaredRepository.getMirrors();
                break;
            }
        }

        return mirrors != null ? mirrors : new HashSet<>();
    }

    /**
     * Checks if synchronization for a given repository is currently ongoing.
     * If it is not the previous status will be removed and a new ongoing one will be added.
     */
    protected Boolean isPendingAddNewStatusIfFinished(
            Repository repository, List<PackageSynchronizationStatus> packages) {
        synchronized (synchronizationStatuses) {
            Optional<RepositorySynchronizationStatus> status =
                    Optional.ofNullable(synchronizationStatuses.get(repository.getId()));

            if (status.isPresent()) {
                if (status.get().isPending()) {
                    return true;
                }

                synchronizationStatuses.remove(status.get().getRepository().getId());
            }

            RepositorySynchronizationStatus newStatus = new RepositorySynchronizationStatus();
            newStatus.setRepository(repository);
            newStatus.setTimestamp(new Date());
            newStatus.setPending(true);
            newStatus.setTechnology(repository.getTechnology());
            newStatus.setPackages(packages);

            synchronizationStatuses.put(repository.getId(), newStatus);

            return false;
        }
    }

    /**
     * Register synchronization error for a given package in a given repository.
     */
    protected void registerPackageSynchronizationStatus(
            Repository repository,
            String packageName,
            String packageVersion,
            M mirror,
            SynchronizationStatus status,
            String error) {
        synchronized (synchronizationStatuses) {
            synchronizationStatuses.get(repository.getId()).getPackages().stream()
                    .filter(p -> p.equals(packageName, packageVersion, mirror))
                    .findFirst()
                    .ifPresent(ps -> {
                        ps.setStatus(status);
                        ps.setError(error);
                    });
        }
    }

    /**
     * Register synchronization status for a given repository.
     */
    protected void registerRepositorySynchronizationStatus(Repository repository, SynchronizationStatus status) {
        synchronized (synchronizationStatuses) {
            RepositorySynchronizationStatus repoStatus = synchronizationStatuses.get(repository.getId());

            if (repoStatus.getStatus() == SynchronizationStatus.PENDING) {
                repoStatus.setStatus(status);
            } else if (!repoStatus.getStatus().equals(status)) {
                repoStatus.setStatus(SynchronizationStatus.MIXED);
            }
        }
    }

    /**
     * Registers the fact that synchronization is finished.
     * This method has to be triggered after finished synchronization.
     */
    protected void registerFinishedSynchronization(Repository repository) {
        synchronized (synchronizationStatuses) {
            if (synchronizationStatuses.get(repository.getId()) != null)
                synchronizationStatuses.get(repository.getId()).setPending(false);
        }
    }
}

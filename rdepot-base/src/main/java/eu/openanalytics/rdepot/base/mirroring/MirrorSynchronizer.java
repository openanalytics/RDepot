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
package eu.openanalytics.rdepot.base.mirroring;

import eu.openanalytics.rdepot.base.config.declarative.DeclarativeConfigurationSource;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.SynchronizationStatus;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredPackage;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
    private final List<SynchronizationStatus> synchronizationStatusList;

    public MirrorSynchronizer(DeclarativeConfigurationSource<R, P, M> declarativeConfigurationSource) {
        this.synchronizationStatusList = new ArrayList<>();
        this.declarativeConfigurationSource = declarativeConfigurationSource;
    }

    /**
     * Synchronizes a given repository with its external counterpart.
     * The method is asynchronous, status of synchronization can be obtained
     * with {@link #getSynchronizationStatusList()} method.
     * @param repository repository to synchronize
     */
    @Async
    public abstract void synchronize(R repository, M mirror);

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
    protected Boolean isPendingAddNewStatusIfFinished(Repository repository) {
        synchronized (synchronizationStatusList) {
            Optional<SynchronizationStatus> status = synchronizationStatusList.stream()
                    .filter(s -> s.getRepositoryId().equals(repository.getId())
                            && s.getTechnology().equals(repository.getTechnology()))
                    .findFirst();

            if (status.isPresent()) {
                if (status.get().isPending()) {
                    return true;
                }

                synchronizationStatusList.remove(status.get());
            }

            SynchronizationStatus newStatus = new SynchronizationStatus();
            newStatus.setRepositoryId(repository.getId());
            newStatus.setTimestamp(new Date());
            newStatus.setPending(true);
            newStatus.setTechnology(repository.getTechnology());

            synchronizationStatusList.add(newStatus);

            return false;
        }
    }

    /**
     * Register synchronization error for a given repository.
     */
    protected void registerSynchronizationError(Repository repository, Exception e) {
        synchronized (synchronizationStatusList) {
            synchronizationStatusList.stream()
                    .filter(s -> s.getRepositoryId().equals(repository.getId()))
                    .findFirst()
                    .ifPresent(s -> s.setError(Optional.of(e)));
        }
    }

    /**
     * Registers the fact that synchronization is finished.
     * This method has to be triggered after finished synchronization.
     */
    protected void registerFinishedSynchronization(Repository repository) {
        synchronized (synchronizationStatusList) {
            synchronizationStatusList.stream()
                    .filter(s -> s.getRepositoryId().equals(repository.getId()))
                    .findFirst()
                    .ifPresent(s -> s.setPending(false));
        }
    }
}

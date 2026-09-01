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
package eu.openanalytics.rdepot.base.mirroring;

import eu.openanalytics.rdepot.base.entities.MirrorSynchronizationStatus;
import eu.openanalytics.rdepot.base.entities.PackageSynchronizationStatus;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositorySynchronizationStatus;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredPackage;
import eu.openanalytics.rdepot.base.mirroring.pojos.SynchronizationStatus;
import eu.openanalytics.rdepot.base.mirroring.pojos.SynchronizationStatusEnum;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Coordinates mirroring status for different repositories, mirrors or packages.
 * Since mirroring is asynchronous, this component is used to track its status.
 * In order to avoid conflicts between threads,
 * registering or modifying statuses is synchronized.
 * This component can also be used to retrieve status for a given repo.
 */
@Slf4j
@Component
public class MirrorSynchronizationStatusCoordinator {

    private final Map<Integer, RepositorySynchronizationStatus> synchronizationStatuses = new LinkedHashMap<>();
    private final Map<MirroredPackage, PackageSynchronizationStatus> packageStatusIndex = new LinkedHashMap<>();
    private final Map<Mirror<?>, MirrorSynchronizationStatus> mirrorStatusIndex = new LinkedHashMap<>();

    /**
     * Creates new mirroring status for repository.
     * In case the status has to be recreated, the {@link #recreateStatus(Repository, List)}
     * method should be used.
     */
    public synchronized <P extends MirroredPackage, M extends Mirror<P>> void createNewStatus(
            Repository repository, List<M> mirrors) {
        log.debug("Creating new mirroring status for repository: {}", repository.getName());
        createNewStatus(repository, mirrors, false);
    }

    /**
     * Recreates an ongoing status for a given repository.
     * It is used in case a repository is going to be <b>fully</b> mirrored,
     * therefore no specific mirrors are declared in the configuration.
     * In this case, specific packages are not yet known at the moment
     * of the start of synchronization, as such remote repository's index
     * is downloaded in the process.
     * Recreating the status ensures that the client gets
     * the most precise feedback on the status for every individual package.
     */
    public synchronized <P extends MirroredPackage, M extends Mirror<P>> void recreateStatus(
            Repository repository, List<M> mirrors) {
        log.debug(
                "Recreating mirroring status for repository: {}. Probably the index has been fetched.",
                repository.getName());
        createNewStatus(repository, mirrors, true);
    }

    private <P extends MirroredPackage, M extends Mirror<P>> void createNewStatus(
            Repository repository, List<M> mirrors, boolean allowRecreate) {
        RepositorySynchronizationStatus status = synchronizationStatuses.get(repository.getId());
        if (Objects.nonNull(status)) {
            if (status.getStatus().equals(SynchronizationStatusEnum.PENDING) && !allowRecreate)
                throw new IllegalArgumentException("Trying to create status for already pending synchronization.");
            synchronizationStatuses.remove(repository.getId());
        }

        status = new MirrorSynchronizationStatusCreator<P, M>().createNewSynchronizationStatus(repository, mirrors);
        synchronizationStatuses.put(repository.getId(), status);
        indexPackagesAndMirrors(status);
    }

    private void indexPackagesAndMirrors(RepositorySynchronizationStatus status) {
        for (MirrorSynchronizationStatus mirrorStatus : status.getMirrors()) {
            mirrorStatusIndex.put(mirrorStatus.getMirror(), mirrorStatus);
            for (PackageSynchronizationStatus packageStatus : mirrorStatus.getPackageSynchronizationStatuses()) {
                packageStatusIndex.put(packageStatus.getMirroredPackage(), packageStatus);
            }
        }
    }

    /**
     * To be used immediately after the package is successfully mirrored.
     */
    public synchronized void registerPackageMirroringFinishedWithSuccess(MirroredPackage mirroredPackage) {
        log.debug(
                "Registering successful mirroring for package: {}",
                mirroredPackage.getName() + " " + mirroredPackage.getVersion());
        registerPackageMirroringFinished(mirroredPackage, null, SynchronizationStatusEnum.SUCCESS);
    }

    /**
     * To be used immediately after the package fails to be (up- or down-)loaded.
     */
    public synchronized void registerPackageMirroringFinishedWithError(MirroredPackage mirroredPackage, String error) {
        log.debug(
                "Registering failed mirroring for package: {}",
                mirroredPackage.getName() + " " + mirroredPackage.getVersion());
        registerPackageMirroringFinished(mirroredPackage, error, SynchronizationStatusEnum.ERROR);
    }

    /**
     * To be used for empty mirrors, to register finished synchronization.
     */
    public synchronized void registerEmptyMirrorMirroringFinished(Mirror<?> mirror) {
        final MirrorSynchronizationStatus status = mirrorStatusIndex.get(mirror);
        if (Objects.isNull(status)) {
            throw new IllegalArgumentException("Trying to fetch status for not registered mirror.");
        }
        if (!mirror.getPackages().isEmpty() && status.getStatus().equals(SynchronizationStatusEnum.PENDING)) {
            // If there are packages to mirror that are pending synchronization
            // then this method should never be used
            throw new IllegalArgumentException(
                    "Attempting to prematurely finish synchronization for non-empty mirror.");
        }
        if (status.getStatus().equals(SynchronizationStatusEnum.PENDING)) {
            status.setStatus(SynchronizationStatusEnum.SUCCESS);
        }
        status.setPending(false);
        updateStatusOfParentStatuses(status);
    }

    /**
     * To be used for empty mirrors, to register finished synchronization.
     */
    public synchronized void registerEmptyRepoMirroringFinished(int repositoryId) {
        RepositorySynchronizationStatus status = synchronizationStatuses.get(repositoryId);
        if (Objects.isNull(status)) {
            throw new IllegalArgumentException("Trying to fetch status for not registered mirroring.");
        }
        if (!status.getMirrors().isEmpty())
            throw new IllegalArgumentException(
                    "Attempting to prematurely finish synchronization for non-empty repository.");
        status.setStatus(SynchronizationStatusEnum.SUCCESS);
        status.setPending(false);
    }

    /**
     * To be used immediately after all packages of given mirror fail to be (up/down)loaded
     * or if fetching the index for the whole mirror fails.
     */
    public synchronized void registerMirrorMirroringFinishedWithError(Mirror<?> mirror, String error) {
        log.debug("Registering failed mirroring for the whole mirror: {}", mirror.getUri());
        final MirrorSynchronizationStatus status = mirrorStatusIndex.get(mirror);
        if (Objects.isNull(status)) {
            throw new IllegalArgumentException("Trying to fetch status for not registered mirror.");
        }
        status.setStatus(SynchronizationStatusEnum.ERROR);
        status.setPending(false);
        status.setError(error);
        status.getPackageSynchronizationStatuses().forEach(s -> {
            s.setStatus(SynchronizationStatusEnum.ERROR);
            s.setError(error);
        });

        updateStatusOfParentStatuses(status);
    }

    /**
     * To be used immediately after a package is (up/down)loaded
     * but a warning occurred (for example a duplicate has been detected).
     */
    public synchronized void registerPackageMirroringFinishedWithWarning(
            MirroredPackage mirroredPackage, String warning) {
        log.debug(
                "Registering successful mirroring (with warning) for package: {}",
                mirroredPackage.getName() + " " + mirroredPackage.getVersion());
        registerPackageMirroringFinished(mirroredPackage, warning, SynchronizationStatusEnum.WARNING);
    }

    private void registerPackageMirroringFinished(
            MirroredPackage mirroredPackage, String error, SynchronizationStatusEnum statusEnum) {
        final PackageSynchronizationStatus status = packageStatusIndex.get(mirroredPackage);
        if (Objects.isNull(status)) {
            throw new IllegalArgumentException("Trying to fetch status for not registered mirrored package.");
        }

        status.setStatus(statusEnum);
        status.setError(error);
        updateStatusOfParentStatuses(status);
    }

    private SynchronizationStatusEnum inferStatusFromChildren(SynchronizationStatus status) {
        final boolean areAnyPending =
                status.getChildren().stream().anyMatch(s -> s.getStatus().equals(SynchronizationStatusEnum.PENDING));
        if (areAnyPending) return SynchronizationStatusEnum.PENDING;

        final long successfulCount = status.getChildren().stream()
                .filter(s -> s.getStatus().equals(SynchronizationStatusEnum.SUCCESS)
                        || s.getStatus().equals(SynchronizationStatusEnum.WARNING))
                .count();
        final long errorCount = status.getChildren().stream()
                .filter(s -> s.getStatus().equals(SynchronizationStatusEnum.ERROR))
                .count();
        final long mixedCount = status.getChildren().stream()
                .filter(s -> s.getStatus().equals(SynchronizationStatusEnum.MIXED))
                .count();

        if (mixedCount == 0) {
            if (errorCount == 0) return SynchronizationStatusEnum.SUCCESS;
            if (successfulCount == 0) return SynchronizationStatusEnum.ERROR;
        }
        return SynchronizationStatusEnum.MIXED;
    }

    private void updateStatusOfParentStatuses(MirrorSynchronizationStatus status) {
        final RepositorySynchronizationStatus repoStatus = status.getRepositorySynchronizationStatus();
        final SynchronizationStatusEnum repoStatusEnum = inferStatusFromChildren(repoStatus);
        repoStatus.setPending(repoStatusEnum.equals(SynchronizationStatusEnum.PENDING));
        repoStatus.setStatus(repoStatusEnum);
    }

    private void updateStatusOfParentStatuses(PackageSynchronizationStatus status) {
        final MirrorSynchronizationStatus mirrorStatus = status.getMirrorSynchronizationStatus();
        final SynchronizationStatusEnum mirrorStatusEnum = inferStatusFromChildren(mirrorStatus);
        mirrorStatus.setStatus(mirrorStatusEnum);

        if (mirrorStatusEnum.equals(SynchronizationStatusEnum.PENDING)) {
            return;
        }

        updateStatusOfParentStatuses(mirrorStatus);
    }

    /**
     * Retrieves full synchronization status for given repository id.
     */
    public Optional<RepositorySynchronizationStatus> getSynchronizationStatus(Repository repository) {
        final RepositorySynchronizationStatus status = synchronizationStatuses.get(repository.getId());
        if (Objects.isNull(status)) {
            return Optional.empty();
        }
        status.setRepository(repository); // Update repository projection in case it changed in the meantime

        return Optional.of(synchronizationStatuses.get(repository.getId()));
    }
}

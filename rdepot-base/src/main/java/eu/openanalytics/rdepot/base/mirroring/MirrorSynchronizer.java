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

import eu.openanalytics.rdepot.base.config.declarative.DeclarativeConfigurationSource;
import eu.openanalytics.rdepot.base.entities.Hashable;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.mirroring.exceptions.MirrorIndexDownloadException;
import eu.openanalytics.rdepot.base.mirroring.exceptions.MirrorPackageErrorException;
import eu.openanalytics.rdepot.base.mirroring.exceptions.MirrorPackageWarningException;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredPackage;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredRepository;
import eu.openanalytics.rdepot.base.mirroring.pojos.RemotePackage;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.base.service.RepositoryService;
import eu.openanalytics.rdepot.base.storage.LocalStorage;
import eu.openanalytics.rdepot.base.storage.exceptions.CreateTemporaryFolderException;
import eu.openanalytics.rdepot.base.storage.exceptions.DeleteFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.DownloadFileException;
import eu.openanalytics.rdepot.base.storage.implementations.CommonFSLocalStorage;
import jakarta.transaction.Transactional;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.multipart.MultipartFile;

/**
 * Performs mirroring for given repository.
 * It also retrieves mirrors for given repository
 * based on declared configuration.
 */
@Slf4j
public abstract class MirrorSynchronizer<
        MP extends MirroredPackage, M extends Mirror<MP>, RP extends RemotePackage, R extends Repository> {

    protected final MirrorSynchronizationStatusCoordinator mirrorSynchronizationStatusCoordinator;
    private final PackageService<?> packageService;
    private final RemotePackageMapper<MP, RP, M> remotePackageMapper;
    private final DeclarativeConfigurationSource<?, MP, M> declarativeConfigurationSource;
    private final LocalStorage<?> storage;
    protected final MessageSource messageSource;
    protected static final Locale locale = LocaleContextHolder.getLocale();
    private final BestMaintainerChooser bestMaintainerChooser;
    private final RepositoryService<R> repositoryService;

    protected MirrorSynchronizer(
            MirrorSynchronizationStatusCoordinator mirrorSynchronizationStatusCoordinator,
            PackageService<?> packageService,
            RemotePackageMapper<MP, RP, M> remotePackageMapper,
            DeclarativeConfigurationSource<?, MP, M> declarativeConfigurationSource,
            CommonFSLocalStorage<?> storage,
            MessageSource messageSource,
            BestMaintainerChooser bestMaintainerChooser,
            RepositoryService<R> repositoryService) {
        this.mirrorSynchronizationStatusCoordinator = mirrorSynchronizationStatusCoordinator;
        this.packageService = packageService;
        this.remotePackageMapper = remotePackageMapper;
        this.declarativeConfigurationSource = declarativeConfigurationSource;
        this.storage = storage;
        this.messageSource = messageSource;
        this.bestMaintainerChooser = bestMaintainerChooser;
        this.repositoryService = repositoryService;
    }

    /**
     * Synchronizes repository with all given mirrors.
     * This method must *only* be used in an asynchronous context,
     * that is from a class derived from {@link MirrorSynchronizationCoordinator}.
     */
    @Transactional
    public void synchronizeWithMirrors(R repository, List<M> mirrors) {
        // This is required to reopen the hibernate session
        // as this entity's session might have been closed in another thread
        final R repositoryEntity = repositoryService
                .findById(repository.getId())
                .orElseThrow(() -> new IllegalArgumentException("Non-existing repository supplied"));
        //
        mirrorSynchronizationStatusCoordinator.createNewStatus(repositoryEntity, mirrors);
        log.debug("Mirroring started for repository: {}", repositoryEntity.getName());
        if (mirrors.isEmpty()) {
            mirrorSynchronizationStatusCoordinator.registerEmptyRepoMirroringFinished(repositoryEntity.getId());
            log.debug("Mirroring finished for repository: {}", repositoryEntity.getName());
            return;
        }
        for (M mirror : mirrors) {
            try {
                synchronizeWithMirror(repositoryEntity, mirror);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                mirrorSynchronizationStatusCoordinator.registerMirrorMirroringFinishedWithError(mirror, e.getMessage());
            }
        }
        log.debug("Mirroring finished for repository: {}", repositoryEntity.getName());
    }

    /**
     * Fetches all {@link Mirror mirrors} for given repository.
     */
    public List<M> findByRepository(R repository) {
        Optional<? extends MirroredRepository<MP, M>> declaredRepositoryOpt =
                declarativeConfigurationSource.retrieveDeclaredRepositories().stream()
                        .filter(dr -> dr.getName().equals(repository.getName())
                                && dr.getTechnology().equals(repository.getTechnology()))
                        .findFirst();
        if (declaredRepositoryOpt.isEmpty()) return List.of();

        final MirroredRepository<MP, M> declaredRepository = declaredRepositoryOpt.get();
        return declaredRepository.getMirrors().stream().toList();
    }

    protected void synchronizeWithMirror(R repository, M mirror) {
        log.debug("Mirroring started for mirror {} of repository {}", mirror.getUri(), repository.getName());
        final List<RP> remotePackages;
        final User uploader;
        try {
            uploader = bestMaintainerChooser.findFirstAdmin();
            remotePackages = getPackageListFromRemoteRepository(mirror, repository);
            if (mirror.getAllPackages()) {
                log.debug("Mirroring all packages from mirror: {}\nIndex will be retrieved first.", mirror.getUri());
                mirror.getPackages()
                        .addAll(remotePackages.stream()
                                .map(mp -> remotePackageMapper.convertRemoteToExpected(mp, mirror))
                                .toList());
                mirrorSynchronizationStatusCoordinator.recreateStatus(repository, List.of(mirror));
            }
        } catch (Exception e) {
            mirrorSynchronizationStatusCoordinator.registerMirrorMirroringFinishedWithError(mirror, e.getMessage());
            log.error(e.getMessage(), e);
            return;
        }

        List<MP> expectedPackages = mirror.getPackages();
        final Map<MP, RP> mirroredPackagesToRemoteMapping =
                remotePackageMapper.mapExpectedToRemoteAndRegisterUnmapped(expectedPackages, remotePackages);
        expectedPackages = new ArrayList<>(mirroredPackagesToRemoteMapping.keySet()); // to exclude unmapped

        if (expectedPackages.isEmpty()) {
            mirrorSynchronizationStatusCoordinator.registerEmptyMirrorMirroringFinished(mirror);
            return;
        }
        for (MP expectedPackage : expectedPackages) {
            log.debug(
                    "Mirroring package: {} (version: {}, mirror: {})",
                    expectedPackage.getName(),
                    expectedPackage.getVersion(),
                    mirror.getUri());
            final Optional<? extends Package> localPackage;

            if (expectedPackage.getVersion() == null) {
                localPackage = findNonDeletedNewestByNameAndRepository(expectedPackage.getName(), repository);
            } else {
                localPackage = findNonDeletedByNameAndVersionAndRepository(
                        expectedPackage.getName(), expectedPackage.getVersion(), repository);
            }

            try {
                final RP remotePackage = mirroredPackagesToRemoteMapping.get(expectedPackage);
                if (localPackage.isEmpty()) {
                    downloadAndUploadPackage(remotePackage, expectedPackage, mirror, repository, false, uploader);
                } else if (!areChecksumsEqual(localPackage.get(), remotePackage)) {
                    downloadAndUploadPackage(remotePackage, expectedPackage, mirror, repository, true, uploader);
                }
                log.debug(
                        "Package mirrored successfully: {} (version: {}, mirror: {})",
                        expectedPackage.getName(),
                        expectedPackage.getVersion(),
                        mirror.getUri());
                mirrorSynchronizationStatusCoordinator.registerPackageMirroringFinishedWithSuccess(expectedPackage);
            } catch (MirrorPackageWarningException w) {
                log.warn(w.getMessage(), w);

                mirrorSynchronizationStatusCoordinator.registerPackageMirroringFinishedWithWarning(
                        expectedPackage, w.getMessage());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                mirrorSynchronizationStatusCoordinator.registerPackageMirroringFinishedWithError(
                        expectedPackage, e.getMessage());
            }
        }
    }

    protected Optional<? extends Package> findNonDeletedNewestByNameAndRepository(String name, R repository) {
        return packageService.findNonDeletedNewestByNameAndRepository(name, repository);
    }

    protected Optional<? extends Package> findNonDeletedByNameAndVersionAndRepository(
            String name, String version, R repository) {
        return packageService.findNonDeletedByNameAndVersionAndRepository(name, version, repository);
    }

    /**
     * Returns all packages present in the index of remote repository.
     * It does not necessarily have to return all available packages,
     * as for example for CRAN, the archived packages are usually
     * not present in the PACKAGES file.
     */
    protected abstract List<RP> getPackageListFromRemoteRepository(M mirror, R repository)
            throws MirrorIndexDownloadException;

    /**
     * Compares checksum of remote and local package.
     * Used to determine if it is even needed to mirror a package.
     */
    protected boolean areChecksumsEqual(Hashable localPackage, Hashable remotePackage) {
        if (!localPackage.getHashMethod().equals(remotePackage.getHashMethod())) {
            log.warn(
                    "Could not compare hashes since different hash methods "
                            + "were used for local package {} than for remote package {}. "
                            + "Package will be reuploaded.",
                    localPackage,
                    remotePackage);
            return false;
        }

        return localPackage.getHash().equals(remotePackage.getHash());
    }

    /**
     * Downloads a remote package, stores it in temporary directory and attempts to upload
     * using the {@link #uploadPackage(MultipartFile, Mirror, MirroredPackage, Repository, boolean, User)}
     * method.
     * After that it removes the temporary directory.
     */
    protected void downloadAndUploadPackage(
            RP remotePackage, MP mirroredPackage, M mirror, R repository, boolean isReplaced, User uploader)
            throws MirrorPackageErrorException, MirrorPackageWarningException {
        final String name = remotePackage.getName();
        final String version = remotePackage.getVersion();

        File remotePackageDir = null;
        try {
            remotePackageDir = storage.createTemporaryFolder(name + "_" + version);
            final File downloadDestination =
                    new File(remotePackageDir.toPath() + "/" + getFilenameForRemotePackage(remotePackage));
            final String downloadUrl = getUrlForRemotePackage(mirror, remotePackage);
            log.debug("Downloading package from: {}", downloadUrl);
            MultipartFile downloadedFile = storage.downloadFile(downloadUrl, downloadDestination);

            log.debug(
                    "Uploading package {} to repository {}",
                    downloadedFile.getOriginalFilename(),
                    repository.getName());
            uploadPackage(downloadedFile, mirror, mirroredPackage, repository, isReplaced, uploader);
        } catch (CreateTemporaryFolderException | DownloadFileException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new MirrorPackageErrorException(remotePackage.getName(), remotePackage.getVersion());
        } finally {
            try {
                if (remotePackageDir != null) storage.removeFileIfExists(remotePackageDir.getAbsolutePath());
            } catch (DeleteFileException e) {
                logTempDirDeleteError(remotePackageDir.toPath().toAbsolutePath());
            }
        }
    }

    protected abstract void uploadPackage(
            MultipartFile multipartFile, M mirror, MP mirroredPackage, R repository, boolean isReplaced, User uploader)
            throws MirrorPackageErrorException, MirrorPackageWarningException;

    /**
     * Provides the URL where the package to mirror can be found
     * in the remote repository.
     * It should be a string with full URL (including the top-level domain, etc.).
     */
    protected abstract String getUrlForRemotePackage(M mirror, RP remotePackage);

    /**
     * Remote packages usually have certain naming conventions.
     * This returns the proper filename for such a package.
     */
    protected abstract String getFilenameForRemotePackage(RP remotePackage);

    protected void logTempDirDeleteError(Path path) {
        log.error(
                "{}\nLocation: {}",
                messageSource.getMessage(MessageCodes.ERROR_CLEAN_FS, null, MessageCodes.ERROR_CLEAN_FS, locale),
                path);
    }
}

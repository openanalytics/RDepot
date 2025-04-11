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
package eu.openanalytics.rdepot.r.mirroring;

import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.exception.AdminNotFound;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.mirroring.MirrorSynchronizer;
import eu.openanalytics.rdepot.base.mirroring.exceptions.UpdatePackageException;
import eu.openanalytics.rdepot.base.storage.exceptions.CreateTemporaryFolderException;
import eu.openanalytics.rdepot.base.storage.exceptions.DeleteFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.DownloadFileException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.StrategyExecutor;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.r.api.v2.dtos.RPackageUploadRequest;
import eu.openanalytics.rdepot.r.config.declarative.RYamlDeclarativeConfigurationSource;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.mirroring.exceptions.DownloadPackagesFileException;
import eu.openanalytics.rdepot.r.mirroring.exceptions.NoSuchPackageException;
import eu.openanalytics.rdepot.r.mirroring.pojos.MirroredRPackage;
import eu.openanalytics.rdepot.r.mirroring.pojos.MirroredRRepository;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.storage.implementations.RLocalStorage;
import eu.openanalytics.rdepot.r.strategy.factory.RStrategyFactory;
import eu.openanalytics.rdepot.r.utils.PackagesFileParser;
import eu.openanalytics.rdepot.r.utils.exceptions.ParsePackagesFileException;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Mirroring implementation for R repositories
 */
@Slf4j
@Component
public class CranMirrorSynchronizer extends MirrorSynchronizer<MirroredRRepository, MirroredRPackage, CranMirror> {

    private static final Locale locale = LocaleContextHolder.getLocale();
    private static final String PACKAGES_FILE_PATH = "/src/contrib/PACKAGES";
    private static final String PACKAGE_PREFIX = "/src/contrib";
    private static final String PACKAGE_ARCHIVE_PREFIX = "/src/contrib/Archive";

    private final RLocalStorage storage;
    private final RPackageService packageService;
    private final MessageSource messageSource;
    private final BestMaintainerChooser bestMaintainerChooser;
    private final RStrategyFactory strategyFactory;
    private final RRepositoryService repositoryService;
    private final StrategyExecutor strategyExecutor;

    public CranMirrorSynchronizer(
            RPackageService packageService,
            MessageSource messageSource,
            RLocalStorage storage,
            BestMaintainerChooser bestMaintainerChooser,
            RStrategyFactory strategyFactory,
            RRepositoryService repositoryService,
            RYamlDeclarativeConfigurationSource rYamlDeclarativeConfigurationSource,
            StrategyExecutor strategyExecutor) {
        super(rYamlDeclarativeConfigurationSource);
        this.packageService = packageService;
        this.messageSource = messageSource;
        this.storage = storage;
        this.bestMaintainerChooser = bestMaintainerChooser;
        this.strategyFactory = strategyFactory;
        this.repositoryService = repositoryService;
        this.strategyExecutor = strategyExecutor;
    }

    @Override
    @Async
    public void synchronizeAsync(MirroredRRepository mirroredRepository, CranMirror mirror) {
        RRepository repositoryEntity = repositoryService
                .findByName(mirroredRepository.getName())
                .orElseThrow(() -> new IllegalStateException("Cannot synchronize non-existing repository."));
        synchronize(repositoryEntity, mirror);
    }

    @Async
    public void synchronizeAsync(RRepository repository, CranMirror mirror) {
        synchronize(repository, mirror);
    }

    private void synchronize(RRepository repository, CranMirror mirror) {
        if (isPendingAddNewStatusIfFinished(repository)) {
            log.warn(
                    "Cannot start synchronization because it is already pending for this repository: {}",
                    repository.getId());
            return;
        }

        log.info("Synchronization started for repository: {}", repository.getId());

        try {
            List<RPackage> remotePackages = getPackageListFromRemoteRepository(mirror);

            List<RPackage> packages = resolveMirroredPackagesToPackageEntities(mirror.getPackages());

            for (RPackage packageBag : packages) {
                Optional<RPackage> localPackage;

                if (packageBag.getVersion() == null) {
                    localPackage =
                            packageService.findNonDeletedNewestByNameAndRepository(packageBag.getName(), repository);
                } else {
                    localPackage = packageService.findNonDeletedByNameAndVersionAndRepository(
                            packageBag.getName(), packageBag.getVersion(), repository);
                }

                if (packageBag.getVersion() == null) {
                    if (localPackage.isEmpty()
                            || !getPackageMd5(packageBag.getName(), remotePackages)
                                    .equals(localPackage.get().getMd5sum())) {
                        uploadPackage(
                                packageBag.getName(),
                                getVersion(packageBag.getName(), remotePackages),
                                mirror,
                                repository,
                                false,
                                packageBag.getGenerateManuals());
                    }
                } else if (localPackage.isEmpty()) {
                    uploadPackage(
                            packageBag.getName(),
                            packageBag.getVersion(),
                            mirror,
                            repository,
                            isOutdated(packageBag, remotePackages),
                            packageBag.getGenerateManuals());
                }
            }

        } catch (NoSuchPackageException
                | ParsePackagesFileException
                | UpdatePackageException
                | DownloadPackagesFileException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            registerSynchronizationError(repository, e);
        } finally {
            log.info("Synchronization finished for repository: {}", repository.getId());
            registerFinishedSynchronization(repository);
        }
    }

    private List<RPackage> resolveMirroredPackagesToPackageEntities(List<MirroredRPackage> packages) {
        return packages.stream().map(MirroredRPackage::toPackageEntity).collect(Collectors.toList());
    }

    private String getVersion(String name, List<RPackage> remotePackages) throws NoSuchPackageException {
        for (RPackage packageBag : remotePackages) {
            if (packageBag.getName().equals(name)) return packageBag.getVersion();
        }

        throw new NoSuchPackageException(name);
    }

    private Boolean isOutdated(RPackage packageBag, List<RPackage> remotePackages) throws NoSuchPackageException {
        for (RPackage remotePackage : remotePackages) {
            if (remotePackage.getName().equals(packageBag.getName())) {
                return remotePackage.compareTo(packageBag) > 0;
            }
        }

        throw new NoSuchPackageException(packageBag);
    }

    private String getPackageMd5(String name, List<RPackage> remotePackages)
            throws ParsePackagesFileException, NoSuchPackageException {
        for (RPackage remotePackage : remotePackages) {
            if (remotePackage.getName().equals(name)) {
                return remotePackage.getMd5sum();
            }
        }

        throw new NoSuchPackageException(name);
    }

    private void uploadPackage(
            String name,
            String version,
            CranMirror mirror,
            RRepository repository,
            Boolean archived,
            Boolean generateManuals)
            throws UpdatePackageException {
        File remotePackageDir = null;

        try {
            remotePackageDir = storage.createTemporaryFolder(name + "_" + version);
            String filename = name + "_" + version + ".tar.gz";
            File downloadDestination = new File(remotePackageDir.toPath() + "/" + filename);

            String downloadURL;

            if (archived) {
                downloadURL = mirror.getUri() + PACKAGE_ARCHIVE_PREFIX + "/" + name + "/" + filename;
            } else {
                downloadURL = mirror.getUri() + PACKAGE_PREFIX + "/" + filename;
            }

            MultipartFile downloadedFile = storage.downloadFile(downloadURL, downloadDestination);
            User uploader = bestMaintainerChooser.findFirstAdmin();

            RPackageUploadRequest request = new RPackageUploadRequest(
                    downloadedFile,
                    repository,
                    generateManuals,
                    false,
                    false,
                    null,
                    null,
                    null,
                    ""); // TODO #33470 Allow mirroring of binary R packages
            Strategy<Submission> strategy = strategyFactory.uploadPackageStrategy(request, uploader);
            strategyExecutor.execute(strategy);

            log.info("Package mirrored.");
        } catch (CreateTemporaryFolderException | AdminNotFound | DownloadFileException | StrategyFailure e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new UpdatePackageException(name, version, mirror);
        } finally {
            try {
                if (remotePackageDir != null) storage.removeFileIfExists(remotePackageDir.getAbsolutePath());

            } catch (DeleteFileException ioe) {
                log.error(
                        "{}\nLocation: {}",
                        messageSource.getMessage(
                                MessageCodes.ERROR_CLEAN_FS, null, MessageCodes.ERROR_CLEAN_FS, locale),
                        remotePackageDir.toPath().toAbsolutePath());
            }
        }
    }

    private List<RPackage> getPackageListFromRemoteRepository(CranMirror mirror) throws DownloadPackagesFileException {
        Path remotePackagesFilePath = null;
        List<RPackage> remotePackages;
        PackagesFileParser parser = new PackagesFileParser();

        try {
            String downloadUrl = mirror.getUri() + CranMirrorSynchronizer.PACKAGES_FILE_PATH;
            remotePackagesFilePath = storage.downloadFile(downloadUrl).toPath();
            remotePackages = parser.parse(remotePackagesFilePath.toFile());
        } catch (DownloadFileException | ParsePackagesFileException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new DownloadPackagesFileException(mirror);
        } finally {
            if (remotePackagesFilePath != null) {
                try {
                    storage.removeFileIfExists(remotePackagesFilePath.toFile().getAbsolutePath());
                } catch (DeleteFileException e) {
                    log.error(
                            "{}\nLocation: {}",
                            messageSource.getMessage(
                                    MessageCodes.ERROR_CLEAN_FS, null, MessageCodes.ERROR_CLEAN_FS, locale),
                            remotePackagesFilePath.toAbsolutePath());
                }
            }
        }

        return remotePackages;
    }
}

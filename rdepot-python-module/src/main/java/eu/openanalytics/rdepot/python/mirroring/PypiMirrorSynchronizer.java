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
package eu.openanalytics.rdepot.python.mirroring;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageUploadRequest;
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
import eu.openanalytics.rdepot.python.config.PythonProperties;
import eu.openanalytics.rdepot.python.config.declarative.PythonYamlDeclarativeConfigurationSource;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.mirroring.exceptions.EmptyHashException;
import eu.openanalytics.rdepot.python.mirroring.exceptions.NoSuchPackageException;
import eu.openanalytics.rdepot.python.mirroring.pojos.MirroredPythonPackage;
import eu.openanalytics.rdepot.python.mirroring.pojos.MirroredPythonRepository;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import eu.openanalytics.rdepot.python.services.PythonRepositoryService;
import eu.openanalytics.rdepot.python.storage.implementations.PythonLocalStorage;
import eu.openanalytics.rdepot.python.strategy.factory.PythonStrategyFactory;
import eu.openanalytics.rdepot.python.utils.IndexFileParser;
import eu.openanalytics.rdepot.python.utils.exceptions.ParseIndexFileException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Mirroring implementation for Python repositories
 */
@Slf4j
@Component
public class PypiMirrorSynchronizer
        extends MirrorSynchronizer<MirroredPythonRepository, MirroredPythonPackage, PypiMirror> {

    private static final Locale locale = LocaleContextHolder.getLocale();

    private final PythonLocalStorage storage;
    private final PythonPackageService packageService;
    private final MessageSource messageSource;
    private final BestMaintainerChooser bestMaintainerChooser;
    private final PythonStrategyFactory strategyFactory;
    private final PythonRepositoryService repositoryService;
    private final StrategyExecutor strategyExecutor;
    private final PythonProperties pythonProperties;

    protected PypiMirrorSynchronizer(
            PythonPackageService packageService,
            MessageSource messageSource,
            PythonLocalStorage storage,
            BestMaintainerChooser bestMaintainerChooser,
            PythonStrategyFactory strategyFactory,
            PythonRepositoryService repositoryService,
            PythonYamlDeclarativeConfigurationSource declarativeConfigurationSource,
            StrategyExecutor strategyExecutor,
            PythonProperties pythonProperties) {
        super(declarativeConfigurationSource);
        this.packageService = packageService;
        this.messageSource = messageSource;
        this.storage = storage;
        this.bestMaintainerChooser = bestMaintainerChooser;
        this.strategyFactory = strategyFactory;
        this.repositoryService = repositoryService;
        this.strategyExecutor = strategyExecutor;
        this.pythonProperties = pythonProperties;
    }

    @Override
    @Async
    public void synchronizeAsync(MirroredPythonRepository mirroredRepository, PypiMirror mirror) {
        PythonRepository repositoryEntity = repositoryService
                .findByName(mirroredRepository.getName())
                .orElseThrow(() -> new IllegalStateException("Cannot synchronize non-existing repository."));
        synchronize(repositoryEntity, mirror);
    }

    @Async
    public void synchronizeAsync(PythonRepository repository, PypiMirror mirror) {
        synchronize(repository, mirror);
    }

    private void synchronize(PythonRepository repository, PypiMirror mirror) {
        if (isPendingAddNewStatusIfFinished(repository)) {
            log.warn(
                    "Cannot start synchronization because it is already pending for this repository: {}",
                    repository.getId());
            return;
        }

        log.info("Synchronization started for repository: {}", repository.getId());

        try {
            Map<PythonPackage, String> remotePackages = getPackageListFromRemoteRepository(mirror);

            List<PythonPackage> packages = resolveMirroredPackagesToPackageEntities(mirror.getPackages());

            for (PythonPackage packageBag : packages) {
                Optional<PythonPackage> localPackage;

                if (packageBag.getVersion() == null) {
                    localPackage = packageService.findNonDeletedNewestByNormalizedNameAndRepository(
                            packageBag.getNormalizedName(), repository);
                } else {
                    localPackage = packageService.findNonDeletedByNormalizedNameAndVersionAndRepository(
                            packageBag.getNormalizedName(), packageBag.getVersion(), repository);
                }

                if (localPackage.isEmpty()
                        || !getPackageHash(packageBag.getNormalizedName(), remotePackages.keySet())
                                .equals(localPackage.get().getHash())) {

                    String downloadUrl = getDownloadUrlForPackage(
                            packageBag.getNormalizedName(), packageBag.getVersion(), remotePackages);
                    uploadPackage(
                            packageBag.getNormalizedName(), packageBag.getVersion(), downloadUrl, mirror, repository);
                }
            }

        } catch (NoSuchPackageException
                | UpdatePackageException
                | ParseIndexFileException
                | EmptyHashException
                | MalformedURLException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            registerSynchronizationError(repository, e);
        } finally {
            log.info("Synchronization finished for repository: {}", repository.getId());
            registerFinishedSynchronization(repository);
        }
    }

    private Map<PythonPackage, String> getPackageListFromRemoteRepository(PypiMirror mirror)
            throws ParseIndexFileException, MalformedURLException {

        URL repoUrl = new URL(mirror.getUriWithTrailingSlash());

        List<String> subfoldersUrls = new ArrayList<>();
        mirror.getPackages().forEach(packageBag -> {
            try {
                subfoldersUrls.add(new URL(repoUrl, packageBag.getNormalizedName()).toString());
            } catch (MalformedURLException e) {
                log.error(messageSource.getMessage(MessageCodes.ERROR_MALFORMED_URL, null, locale));
            }
        });

        return new IndexFileParser(pythonProperties).parseIndexFile(subfoldersUrls);
    }

    private void uploadPackage(
            String normalizedName, String version, String downloadURL, PypiMirror mirror, PythonRepository repository)
            throws UpdatePackageException {

        File remotePackageDir = null;
        try {
            remotePackageDir = storage.createTemporaryFolder(FilenameUtils.getName(normalizedName + "-" + version));
            String filename = normalizedName + "-" + version + ".tar.gz";
            File downloadDestination = new File(remotePackageDir.toPath() + "/" + FilenameUtils.getName(filename));

            MultipartFile downloadedFile = storage.downloadFile(downloadURL, downloadDestination);

            User uploader = bestMaintainerChooser.findFirstAdmin();

            PackageUploadRequest<PythonRepository> request =
                    new PackageUploadRequest<>(downloadedFile, repository, false, "");

            Strategy<Submission> strategy = strategyFactory.uploadPackageStrategy(request, uploader);
            strategyExecutor.execute(strategy);

        } catch (CreateTemporaryFolderException | AdminNotFound | DownloadFileException | StrategyFailure e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new UpdatePackageException(normalizedName, version, mirror);
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

    private String getDownloadUrlForPackage(
            String normalizedName, String version, Map<PythonPackage, String> remotePackages)
            throws NoSuchPackageException {
        for (PythonPackage packageBag : remotePackages.keySet()) {
            if (packageBag.getNormalizedName().equals(normalizedName)
                    && packageBag.getVersion().equals(version)) {
                return remotePackages.get(packageBag);
            }
        }

        throw new NoSuchPackageException(normalizedName);
    }

    private String getPackageHash(String normalizedName, Set<PythonPackage> remotePackages)
            throws NoSuchPackageException, EmptyHashException {
        for (PythonPackage remotePackage : remotePackages) {
            if (!remotePackage.getNormalizedName().equals(normalizedName)) continue;
            if (remotePackage.getHash() == null) throw new EmptyHashException(remotePackage);
            return remotePackage.getHash();
        }

        throw new NoSuchPackageException(normalizedName);
    }

    private List<PythonPackage> resolveMirroredPackagesToPackageEntities(List<MirroredPythonPackage> packages) {
        return packages.stream().map(MirroredPythonPackage::toPackageEntity).collect(Collectors.toList());
    }
}

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
import eu.openanalytics.rdepot.base.entities.PackageSynchronizationStatus;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.exception.AdminNotFound;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.mirroring.MirrorSynchronizer;
import eu.openanalytics.rdepot.base.mirroring.exceptions.UpdatePackageException;
import eu.openanalytics.rdepot.base.mirroring.pojos.SynchronizationStatus;
import eu.openanalytics.rdepot.base.storage.exceptions.CreateTemporaryFolderException;
import eu.openanalytics.rdepot.base.storage.exceptions.DeleteFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.DownloadFileException;
import eu.openanalytics.rdepot.base.storage.implementations.CommonLocalStorage;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.StrategyExecutor;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.python.config.PythonProperties;
import eu.openanalytics.rdepot.python.config.declarative.PythonYamlDeclarativeConfigurationSource;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.mirroring.exceptions.EmptyHashException;
import eu.openanalytics.rdepot.python.mirroring.exceptions.MissingDownloadUrlException;
import eu.openanalytics.rdepot.python.mirroring.exceptions.NoSuchPackageException;
import eu.openanalytics.rdepot.python.mirroring.pojos.MirroredPythonPackage;
import eu.openanalytics.rdepot.python.mirroring.pojos.MirroredPythonRepository;
import eu.openanalytics.rdepot.python.mirroring.pojos.ParseResult;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import eu.openanalytics.rdepot.python.services.PythonRepositoryService;
import eu.openanalytics.rdepot.python.strategy.factory.PythonStrategyFactory;
import eu.openanalytics.rdepot.python.utils.IndexFileParser;
import eu.openanalytics.rdepot.python.utils.exceptions.ParseIndexFileException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    private final CommonLocalStorage<PythonPackage> storage;
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
            BestMaintainerChooser bestMaintainerChooser,
            PythonStrategyFactory strategyFactory,
            PythonRepositoryService repositoryService,
            PythonYamlDeclarativeConfigurationSource declarativeConfigurationSource,
            StrategyExecutor strategyExecutor,
            PythonProperties pythonProperties,
            CommonLocalStorage<PythonPackage> storage) {
        super(declarativeConfigurationSource);
        this.packageService = packageService;
        this.messageSource = messageSource;
        this.bestMaintainerChooser = bestMaintainerChooser;
        this.strategyFactory = strategyFactory;
        this.repositoryService = repositoryService;
        this.strategyExecutor = strategyExecutor;
        this.pythonProperties = pythonProperties;
        this.storage = storage;
    }

    @Override
    @Async
    public void synchronizeAsync(MirroredPythonRepository mirroredRepository, PypiMirror mirror) {
        PythonRepository repositoryEntity = repositoryService
                .findByName(mirroredRepository.getName())
                .orElseThrow(() -> new IllegalStateException("Cannot synchronize non-existing repository."));
        List<PackageSynchronizationStatus> packages = new ArrayList<>();

        mirror.getPackages().forEach(p -> packages.add(new PackageSynchronizationStatus(p, mirror)));

        if (isPendingAddNewStatusIfFinished(repositoryEntity, packages)) {
            log.warn(
                    "Cannot start synchronization because it is already pending for this repository: {}",
                    repositoryEntity.getId());
            return;
        }
        log.info("Synchronization started for repository: {}", repositoryEntity.getId());
        synchronizeMirror(repositoryEntity, mirror);

        log.info("Synchronization finished for repository: {}", repositoryEntity.getId());
        registerFinishedSynchronization(repositoryEntity);
    }

    @Async
    public void synchronizeAsync(PythonRepository repository, Set<PypiMirror> mirrors) {
        synchronizeMirrors(repository, mirrors);
    }

    private void synchronizeMirrors(PythonRepository repository, Set<PypiMirror> mirrors) {
        List<PackageSynchronizationStatus> packages = new ArrayList<>();

        mirrors.forEach(m -> m.getPackages().forEach(p -> packages.add(new PackageSynchronizationStatus(p, m))));

        if (isPendingAddNewStatusIfFinished(repository, packages)) {
            log.warn(
                    "Cannot start synchronization because it is already pending for this repository: {}",
                    repository.getId());
            return;
        }

        log.info("Synchronization started for repository: {}", repository.getId());

        for (PypiMirror mirror : mirrors) {
            synchronizeMirror(repository, mirror);
        }

        log.info("Synchronization finished for repository: {}", repository.getId());
        registerFinishedSynchronization(repository);
    }

    private void synchronizeMirror(PythonRepository repository, PypiMirror mirror) {
        Map<String, ParseResult> remotePackages = getPackageListFromRemoteRepository(mirror);

        List<PythonPackage> packages = resolveMirroredPackagesToPackageEntities(mirror.getPackages());

        for (PythonPackage packageBag : packages) {
            Optional<PythonPackage> localPackage;
            String key = packageBag.getNormalizedName() + "-" + packageBag.getVersion();

            if (packageBag.getVersion() == null) {
                localPackage = packageService.findNonDeletedNewestByNormalizedNameAndRepository(
                        packageBag.getNormalizedName(), repository);
            } else {
                localPackage = packageService.findNonDeletedByNormalizedNameAndVersionAndRepository(
                        packageBag.getNormalizedName(), packageBag.getVersion(), repository);
            }

            try {
                if (localPackage.isEmpty()
                        || !getPackageHash(key, remotePackages)
                                .equals(localPackage.get().getHash())) {

                    String downloadUrl = getDownloadUrlForPackage(key, remotePackages.get(key));

                    uploadPackage(packageBag.getNormalizedName(), packageBag.getVersion(), downloadUrl, repository);
                }

                registerPackageSynchronizationStatus(
                        repository,
                        packageBag.getName(),
                        packageBag.getVersion(),
                        mirror,
                        SynchronizationStatus.SUCCESS,
                        null);
                registerRepositorySynchronizationStatus(repository, SynchronizationStatus.SUCCESS);
            } catch (NoSuchPackageException
                    | UpdatePackageException
                    | EmptyHashException
                    | ParseIndexFileException
                    | MalformedURLException
                    | MissingDownloadUrlException e) {
                registerPackageSynchronizationStatus(
                        repository,
                        packageBag.getName(),
                        packageBag.getVersion(),
                        mirror,
                        SynchronizationStatus.ERROR,
                        e.getMessage());
                registerRepositorySynchronizationStatus(repository, SynchronizationStatus.ERROR);
            }
        }
    }

    private Map<String, ParseResult> getPackageListFromRemoteRepository(PypiMirror mirror) {
        return new IndexFileParser(pythonProperties).parseIndexFile(mirror);
    }

    private void uploadPackage(String normalizedName, String version, String downloadURL, PythonRepository repository)
            throws UpdatePackageException {

        File remotePackageDir = null;
        try {
            remotePackageDir = storage.createTemporaryFolder(FilenameUtils.getName(normalizedName + "-" + version));
            String filename = normalizedName + "-" + version + ".tar.gz";
            File downloadDestination = new File(remotePackageDir.toPath() + "/" + FilenameUtils.getName(filename));

            MultipartFile downloadedFile = storage.downloadFile(downloadURL, downloadDestination);

            User uploader = bestMaintainerChooser.findFirstAdmin();

            PackageUploadRequest<PythonRepository> request =
                    new PackageUploadRequest<>(downloadedFile, repository, false, "", false);

            Strategy<Submission> strategy = strategyFactory.uploadPackageStrategy(request, uploader);
            strategyExecutor.execute(strategy);

        } catch (CreateTemporaryFolderException | AdminNotFound | DownloadFileException | StrategyFailure e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new UpdatePackageException(e.getMessage());
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

    private String getDownloadUrlForPackage(String key, ParseResult parseResult)
            throws ParseIndexFileException, NoSuchPackageException, MalformedURLException, MissingDownloadUrlException {
        if (parseResult == null) throw new NoSuchPackageException(key);

        if (parseResult.getDownloadUrl().isEmpty()) {
            switch (parseResult.getParseResult()) {
                case MALFORMED_URL:
                    throw new MalformedURLException(MessageCodes.ERROR_MALFORMED_URL);
                case PARSE_EXCEPTION:
                    throw new ParseIndexFileException(key);
                default:
                    throw new MissingDownloadUrlException();
            }
        }

        return parseResult.getDownloadUrl().get();
    }

    private String getPackageHash(String key, Map<String, ParseResult> remotePackages)
            throws NoSuchPackageException, EmptyHashException {

        if (remotePackages.get(key) == null) throw new NoSuchPackageException(key);

        if (remotePackages.get(key).getHash().isEmpty()) throw new EmptyHashException(key);

        return remotePackages.get(key).getHash().toString();
    }

    private List<PythonPackage> resolveMirroredPackagesToPackageEntities(List<MirroredPythonPackage> packages) {
        return packages.stream().map(MirroredPythonPackage::toPackageEntity).toList();
    }
}

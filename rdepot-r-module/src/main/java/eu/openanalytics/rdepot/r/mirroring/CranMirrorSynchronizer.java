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
package eu.openanalytics.rdepot.r.mirroring;

import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.mirroring.MirrorSynchronizationStatusCoordinator;
import eu.openanalytics.rdepot.base.mirroring.MirrorSynchronizer;
import eu.openanalytics.rdepot.base.mirroring.exceptions.MirrorIndexDownloadException;
import eu.openanalytics.rdepot.base.mirroring.exceptions.MirrorPackageErrorException;
import eu.openanalytics.rdepot.base.mirroring.exceptions.MirrorPackageWarningException;
import eu.openanalytics.rdepot.base.storage.LocalStorage;
import eu.openanalytics.rdepot.base.storage.exceptions.DeleteFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.DownloadFileException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.StrategyExecutor;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageDuplicateWithReplaceOff;
import eu.openanalytics.rdepot.r.api.v2.dtos.RPackageUploadRequest;
import eu.openanalytics.rdepot.r.config.declarative.RYamlDeclarativeConfigurationSource;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.mirroring.pojos.MirroredRPackage;
import eu.openanalytics.rdepot.r.mirroring.pojos.RemoteRPackage;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.storage.implementations.RFSLocalStorage;
import eu.openanalytics.rdepot.r.strategy.factory.RStrategyFactory;
import eu.openanalytics.rdepot.r.utils.PackagesFileParser;
import eu.openanalytics.rdepot.r.utils.exceptions.ParsePackagesFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class CranMirrorSynchronizer
        extends MirrorSynchronizer<MirroredRPackage, CranMirror, RemoteRPackage, RRepository> {

    private record NameAndVersion(String name, String version) {

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof NameAndVersion that)) {
                return false;
            }
            return Objects.equals(name, that.name) && Objects.equals(version, that.version);
        }
    }

    private static final PackagesFileParser packagesFileParser = new PackagesFileParser();
    private final LocalStorage<RPackage> localStorage;
    private static final String PACKAGES_FILE_PATH = "/src/contrib/PACKAGES";
    private static final String PACKAGE_PREFIX = "/src/contrib";
    private static final String PACKAGE_ARCHIVE_PREFIX = "/src/contrib/Archive";
    private final StrategyExecutor strategyExecutor;
    private final RStrategyFactory strategyFactory;

    public CranMirrorSynchronizer(
            MirrorSynchronizationStatusCoordinator mirrorSynchronizationStatusCoordinator,
            RPackageService packageService,
            CranRemotePackageMapper remotePackageMapper,
            RFSLocalStorage storage,
            MessageSource messageSource,
            BestMaintainerChooser bestMaintainerChooser,
            StrategyExecutor strategyExecutor,
            RStrategyFactory strategyFactory,
            RYamlDeclarativeConfigurationSource rYamlDeclarativeConfigurationSource,
            RFSLocalStorage rLocalStorage,
            RRepositoryService repositoryService) {
        super(
                mirrorSynchronizationStatusCoordinator,
                packageService,
                remotePackageMapper,
                rYamlDeclarativeConfigurationSource,
                rLocalStorage,
                messageSource,
                bestMaintainerChooser,
                repositoryService);
        this.localStorage = storage;
        this.strategyExecutor = strategyExecutor;
        this.strategyFactory = strategyFactory;
    }

    @Override
    protected List<RemoteRPackage> getPackageListFromRemoteRepository(CranMirror mirror, RRepository repository)
            throws MirrorIndexDownloadException {
        Path remotePackagesFilePath = null;
        final List<RemoteRPackage> remotePackages;
        final Set<NameAndVersion> latestPresentPackages = new HashSet<>();

        try {
            final String downloadUrl = mirror.getUri() + PACKAGES_FILE_PATH;
            remotePackagesFilePath = Path.of(localStorage.downloadFile(downloadUrl));
            remotePackages = packagesFileParser.parseToRemotePackages(remotePackagesFilePath.toFile());
            remotePackages.forEach(p -> latestPresentPackages.add(new NameAndVersion(p.getName(), p.getVersion())));
        } catch (DownloadFileException | ParsePackagesFileException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new MirrorIndexDownloadException();
        } finally {
            if (!Objects.isNull(remotePackagesFilePath)) {
                try {
                    localStorage.removeFileIfExists(
                            remotePackagesFilePath.toFile().getAbsolutePath());
                } catch (DeleteFileException e) {
                    logTempDirDeleteError(remotePackagesFilePath.toAbsolutePath());
                }
            }
        }

        for (MirroredRPackage mirroredPackage : mirror.getPackages()) {
            if (!latestPresentPackages.contains(
                            new NameAndVersion(mirroredPackage.getName(), mirroredPackage.getVersion()))
                    && !Strings.isBlank(mirroredPackage.getVersion())) {
                remotePackages.add(new RemoteRPackage(mirroredPackage.getName(), mirroredPackage.getVersion(), ""));
            }
        }
        return remotePackages;
    }

    @Override
    protected void uploadPackage(
            MultipartFile multipartFile,
            CranMirror mirror,
            MirroredRPackage mirroredRPackage,
            RRepository repository,
            boolean isReplaced,
            User uploader)
            throws MirrorPackageErrorException, MirrorPackageWarningException {
        try {
            final RPackageUploadRequest request = new RPackageUploadRequest(
                    multipartFile,
                    repository,
                    mirroredRPackage.getGenerateManuals(),
                    isReplaced,
                    false,
                    null,
                    null,
                    null,
                    "");
            final Strategy<Submission> strategy = strategyFactory.uploadPackageStrategy(request, uploader);
            strategyExecutor.execute(strategy);
        } catch (StrategyFailure e) {
            if (e.getReason() instanceof PackageDuplicateWithReplaceOff warning) {
                log.debug(warning.getMessage(), warning);
                throw new MirrorPackageWarningException(
                        MessageCodes.PACKAGE_WITH_THE_SAME_NAME_AND_VERSION_ALREADY_EXISTS);
            }
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new MirrorPackageErrorException(multipartFile.getOriginalFilename());
        }
    }

    @Override
    protected String getUrlForRemotePackage(CranMirror mirror, RemoteRPackage remotePackage) {
        if (remotePackage.isArchive()) {
            return mirror.getUri() + PACKAGE_ARCHIVE_PREFIX + "/" + remotePackage.getName() + "/"
                    + getFilenameForRemotePackage(remotePackage);
        }
        return mirror.getUri() + PACKAGE_PREFIX + "/" + getFilenameForRemotePackage(remotePackage);
    }

    @Override
    protected String getFilenameForRemotePackage(RemoteRPackage remotePackage) {
        return remotePackage.getName() + "_" + remotePackage.getVersion() + ".tar.gz";
    }
}

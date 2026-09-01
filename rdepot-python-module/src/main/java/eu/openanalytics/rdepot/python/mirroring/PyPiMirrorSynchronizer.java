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
package eu.openanalytics.rdepot.python.mirroring;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageUploadRequest;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.mirroring.MirrorSynchronizationStatusCoordinator;
import eu.openanalytics.rdepot.base.mirroring.MirrorSynchronizer;
import eu.openanalytics.rdepot.base.mirroring.exceptions.MirrorIndexDownloadException;
import eu.openanalytics.rdepot.base.mirroring.exceptions.MirrorPackageErrorException;
import eu.openanalytics.rdepot.base.mirroring.exceptions.MirrorPackageWarningException;
import eu.openanalytics.rdepot.base.strategy.Strategy;
import eu.openanalytics.rdepot.base.strategy.StrategyExecutor;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.validation.exceptions.PackageDuplicateWithReplaceOff;
import eu.openanalytics.rdepot.python.config.PythonProperties;
import eu.openanalytics.rdepot.python.config.declarative.PythonYamlDeclarativeConfigurationSource;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.mirroring.pojos.IndexFileParseResult;
import eu.openanalytics.rdepot.python.mirroring.pojos.MirroredPythonPackage;
import eu.openanalytics.rdepot.python.mirroring.pojos.RemotePythonPackage;
import eu.openanalytics.rdepot.python.mirroring.pojos.RemotePythonPackageParseResult;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import eu.openanalytics.rdepot.python.services.PythonRepositoryService;
import eu.openanalytics.rdepot.python.storage.implementations.fs.PythonFSLocalStorage;
import eu.openanalytics.rdepot.python.strategy.factory.PythonStrategyFactory;
import eu.openanalytics.rdepot.python.utils.exceptions.ParseRepositoryIndexFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class PyPiMirrorSynchronizer
        extends MirrorSynchronizer<MirroredPythonPackage, PypiMirror, RemotePythonPackage, PythonRepository> {

    private final PythonProperties pythonProperties;
    private final PythonPackageService pythonPackageService;
    private final PythonStrategyFactory strategyFactory;
    private final StrategyExecutor strategyExecutor;

    @Value("${python.max-index-size}")
    private int maxIndexSize;

    public PyPiMirrorSynchronizer(
            MirrorSynchronizationStatusCoordinator mirrorSynchronizationStatusCoordinator,
            PythonPackageService packageService,
            PythonRemotePackageMapper remotePackageMapper,
            PythonYamlDeclarativeConfigurationSource declarativeConfigurationSource,
            PythonProperties pythonProperties,
            PythonFSLocalStorage pythonLocalStorage,
            MessageSource messageSource,
            BestMaintainerChooser bestMaintainerChooser,
            PythonStrategyFactory strategyFactory,
            StrategyExecutor strategyExecutor,
            PythonRepositoryService repositoryService) {
        super(
                mirrorSynchronizationStatusCoordinator,
                packageService,
                remotePackageMapper,
                declarativeConfigurationSource,
                pythonLocalStorage,
                messageSource,
                bestMaintainerChooser,
                repositoryService);
        this.pythonProperties = pythonProperties;
        this.pythonPackageService = packageService;
        this.strategyFactory = strategyFactory;
        this.strategyExecutor = strategyExecutor;
    }

    @Override
    protected List<RemotePythonPackage> getPackageListFromRemoteRepository(
            PypiMirror mirror, PythonRepository repository) throws MirrorIndexDownloadException {
        final IndexFileParser indexFileParser;
        if (!mirror.getAllPackages()) {
            indexFileParser = new IndexFileParser(
                    repository.getHashMethod(),
                    mirror,
                    getHashPattern(),
                    mirror.getPackages().stream()
                            .map(MirroredPythonPackage::getNormalizedName)
                            .toList(),
                    maxIndexSize);
        } else {
            indexFileParser = new IndexFileParser(repository.getHashMethod(), mirror, getHashPattern(), maxIndexSize);
        }

        final List<RemotePythonPackageParseResult> results;
        try {
            results = indexFileParser.parseRepoIndexFile();
        } catch (ParseRepositoryIndexFileException e) {
            log.error(e.getMessage(), e);
            throw new MirrorIndexDownloadException();
        }

        final List<RemotePythonPackage> availableRemotePackages = new ArrayList<>();
        for (RemotePythonPackageParseResult result : results) {
            if (!result.getResult().equals(IndexFileParseResult.OK)
                    || result.getRemotePackage().isEmpty()) {
                registerFailureForPackages(result.getPackageName(), mirror.getPackages(), result.getError());
            } else {
                availableRemotePackages.add(result.getRemotePackage().get());
            }
        }

        return availableRemotePackages;
    }

    @Override
    protected Optional<? extends Package> findNonDeletedByNameAndVersionAndRepository(
            String name, String version, PythonRepository repository) {
        return pythonPackageService.findNonDeletedByNormalizedNameAndVersionAndRepository(name, version, repository);
    }

    @Override
    protected Optional<? extends Package> findNonDeletedNewestByNameAndRepository(
            String name, PythonRepository repository) {
        return pythonPackageService.findNonDeletedNewestByNormalizedNameAndRepository(name, repository);
    }

    private void registerFailureForPackages(
            String packageName, List<MirroredPythonPackage> mirroredPackages, String error) {
        for (MirroredPythonPackage mirroredPackage : mirroredPackages) {
            if (mirroredPackage.getName().equals(packageName)) {
                mirrorSynchronizationStatusCoordinator.registerPackageMirroringFinishedWithError(
                        mirroredPackage, error);
            }
        }
    }

    private Pattern getHashPattern() {
        final String hashFunctionsRegex = String.join("|", pythonProperties.getHashFunctions());
        return Pattern.compile(hashFunctionsRegex);
    }

    @Override
    protected void uploadPackage(
            MultipartFile multipartFile,
            PypiMirror mirror,
            MirroredPythonPackage mirroredPythonPackage,
            PythonRepository repository,
            boolean isReplaced,
            User uploader)
            throws MirrorPackageErrorException, MirrorPackageWarningException {
        final PackageUploadRequest<PythonRepository> request =
                new PackageUploadRequest<>(multipartFile, repository, false, "", false);
        Strategy<Submission> strategy = strategyFactory.uploadPackageStrategy(request, uploader);

        try {
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
    protected String getUrlForRemotePackage(PypiMirror mirror, RemotePythonPackage remotePackage) {
        return remotePackage.getDownloadUrl();
    }

    @Override
    protected String getFilenameForRemotePackage(RemotePythonPackage remotePackage) {
        return remotePackage.getName() + "-" + remotePackage.getVersion() + ".tar.gz";
    }
}

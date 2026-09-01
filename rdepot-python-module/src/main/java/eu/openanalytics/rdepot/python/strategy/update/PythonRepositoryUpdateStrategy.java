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
package eu.openanalytics.rdepot.python.strategy.update;

import eu.openanalytics.rdepot.base.entities.EventChangedVariable;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.storage.LocalStorage;
import eu.openanalytics.rdepot.base.storage.exceptions.CheckSumCalculationException;
import eu.openanalytics.rdepot.base.storage.exceptions.DeleteFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.DownloadFileException;
import eu.openanalytics.rdepot.base.strategy.exceptions.FatalStrategyFailure;
import eu.openanalytics.rdepot.base.strategy.exceptions.StrategyFailure;
import eu.openanalytics.rdepot.base.strategy.update.UpdateRepositoryStrategy;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.services.PythonPackageService;
import eu.openanalytics.rdepot.python.services.PythonRepositoryService;
import eu.openanalytics.rdepot.python.storage.PythonPersistentStorage;
import eu.openanalytics.rdepot.python.synchronization.PythonRepositorySynchronizer;
import java.io.File;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PythonRepositoryUpdateStrategy extends UpdateRepositoryStrategy<PythonRepository> {

    private final PythonPackageService packageService;
    private final LocalStorage<PythonPackage> localStorage;
    private final PythonPersistentStorage pythonPersistentStorage;

    public PythonRepositoryUpdateStrategy(
            PythonRepository resource,
            NewsfeedEventService eventService,
            PythonRepositoryService service,
            User requester,
            PythonRepository updatedResource,
            PythonRepository oldResourceCopy,
            PythonRepositorySynchronizer repositorySynchronizer,
            RepositoryMaintainerService repositoryMaintainerService,
            PackageMaintainerService packageMaintainerService,
            PythonPackageService packageService,
            LocalStorage<PythonPackage> localStorage,
            PythonPersistentStorage pythonPersistentStorage) {
        super(
                resource,
                eventService,
                service,
                requester,
                updatedResource,
                oldResourceCopy,
                repositorySynchronizer,
                repositoryMaintainerService,
                packageMaintainerService,
                packageService);
        this.packageService = packageService;
        this.localStorage = localStorage;
        this.pythonPersistentStorage = pythonPersistentStorage;
    }

    @Override
    protected PythonRepository actualStrategy() throws StrategyFailure {
        boolean recalculateHashes = false;
        if (!resource.getHashMethod().equals(updatedResource.getHashMethod())) {
            resource.setHashMethod(updatedResource.getHashMethod());
            recalculateHashes = true;
            changedValues.add(new EventChangedVariable(
                    "publicationUri",
                    oldResourceCopy.getHashMethod().getValue(),
                    resource.getHashMethod().getValue()));
        }
        final PythonRepository resource = super.actualStrategy();
        if (!recalculateHashes) {
            return resource;
        }
        try {
            for (PythonPackage packageBag : packageService.findAllByRepository(resource)) {
                final File downloaded;
                try {
                    downloaded = pythonPersistentStorage.downloadFile(Path.of(packageBag.getSource()));
                    localStorage.setCheckSum(packageBag, downloaded);
                    pythonPersistentStorage.recycleDownloadedFile(downloaded);
                } catch (DownloadFileException | DeleteFileException e) {
                    log.error(e.getMessage(), e);
                    throw new CheckSumCalculationException();
                }
            }
        } catch (CheckSumCalculationException e) {
            throw new FatalStrategyFailure(e);
        }
        return resource;
    }
}

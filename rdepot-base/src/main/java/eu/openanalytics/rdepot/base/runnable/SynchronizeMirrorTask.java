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
package eu.openanalytics.rdepot.base.runnable;

import eu.openanalytics.rdepot.base.mirroring.Mirror;
import eu.openanalytics.rdepot.base.mirroring.MirrorSynchronizer;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredPackage;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Synchronizes given {@link eu.openanalytics.rdepot.base.entities.Repository}
 * with a given {@link Mirror}.
 * It <b>downloads</b> {@link eu.openanalytics.rdepot.base.entities.Package packages}
 * from the external mirror.
 * <br/>
 * Must not be confused with
 * {@link eu.openanalytics.rdepot.base.synchronization.RepositorySynchronizer}
 * which <b>pushes</b> packages to a remote server location (e.g. CRAN server).
 * @param <R>
 * @param <P>
 * @param <M>
 */
@Slf4j
@AllArgsConstructor
public class SynchronizeMirrorTask<R extends MirroredRepository<P, M>, P extends MirroredPackage, M extends Mirror<P>>
        implements Runnable {

    MirrorSynchronizer<R, P, M> mirrorService;
    R repository;
    M mirror;

    @Override
    public void run() {
        log.info("Synchronizing repository " + repository.getName() + " with mirror " + mirror.getUri());
        mirrorService.synchronizeAsync(repository, mirror);
    }
}

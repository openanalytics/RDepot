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
package eu.openanalytics.rdepot.base.config.declarative;

import eu.openanalytics.rdepot.base.mirroring.Mirror;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredPackage;
import eu.openanalytics.rdepot.base.mirroring.pojos.MirroredRepository;
import java.util.List;

/**
 * Gathers declarations of repositories from various configuration sources.
 * @param <R> technology-specific Mirrored Repository
 * @param <P> technology-specific Mirrored Package
 * @param <M> technology-specific Mirror
 */
public interface DeclarativeConfigurationSource<
        R extends MirroredRepository<P, M>, P extends MirroredPackage, M extends Mirror<P>> {

    List<R> retrieveDeclaredRepositories();
}

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
package eu.openanalytics.rdepot.r.config.declarative;

import eu.openanalytics.rdepot.base.config.declarative.DeclaredRepositoryDirectoriesProps;
import eu.openanalytics.rdepot.base.config.declarative.YamlDeclarativeConfigurationSource;
import eu.openanalytics.rdepot.r.mirroring.CranMirror;
import eu.openanalytics.rdepot.r.mirroring.pojos.MirroredRPackage;
import eu.openanalytics.rdepot.r.mirroring.pojos.MirroredRRepository;
import eu.openanalytics.rdepot.r.validation.repositories.RBasicNameValidator;
import org.springframework.stereotype.Component;

@Component
public class RYamlDeclarativeConfigurationSource
        extends YamlDeclarativeConfigurationSource<MirroredRRepository, MirroredRPackage, CranMirror> {

    public RYamlDeclarativeConfigurationSource(
            DeclaredRepositoryDirectoriesProps declaredRepositoryDirectoriesProps, RBasicNameValidator nameValidator) {
        super(declaredRepositoryDirectoriesProps, MirroredRRepository.class, nameValidator);
    }
}

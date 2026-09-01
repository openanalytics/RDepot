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
package eu.openanalytics.rdepot.base.api.v2.converters;

import com.querydsl.core.Tuple;
import eu.openanalytics.rdepot.base.api.v2.converters.exceptions.EntityResolutionException;
import eu.openanalytics.rdepot.base.api.v2.dtos.PackageMaintainerDto;
import eu.openanalytics.rdepot.base.api.v2.dtos.RepositoryProjection;
import eu.openanalytics.rdepot.base.api.v2.dtos.UserProjection;
import eu.openanalytics.rdepot.base.entities.PackageMaintainerQueryDSLTuple;
import eu.openanalytics.rdepot.base.entities.QPackage;
import eu.openanalytics.rdepot.base.entities.QPackageMaintainer;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

@Component
public class PackageMaintainerQueryDSLTupleConverter
        implements DtoConverter<PackageMaintainerQueryDSLTuple, PackageMaintainerDto> {
    final QPackageMaintainer maintainer = QPackageMaintainer.packageMaintainer;
    final QPackage packageBag = QPackage.package$;

    @Override
    public PackageMaintainerQueryDSLTuple resolveDtoToEntity(PackageMaintainerDto dto)
            throws EntityResolutionException {
        throw new NotImplementedException();
    }

    @Override
    public PackageMaintainerDto convertEntityToDto(PackageMaintainerQueryDSLTuple entity) {
        final Tuple tuple = entity.getTuple();
        UserProjection userProjection = new UserProjection(
                entity.getUserId(),
                tuple.get(packageBag.user.name),
                tuple.get(packageBag.user.login),
                tuple.get(packageBag.user.email));
        RepositoryProjection repositoryProjection = new RepositoryProjection(
                entity.getRepositoryId(),
                tuple.get(packageBag.repositoryGeneric.name),
                tuple.get(packageBag.repositoryGeneric.publicationUri),
                ObjectUtils.getIfNull(tuple.get(packageBag.repositoryGeneric.published), false),
                ObjectUtils.getIfNull(tuple.get(packageBag.repositoryGeneric.requiresAuthentication), false),
                tuple.get(packageBag.repositoryGeneric.resourceTechnology),
                ObjectUtils.getIfNull(tuple.get(packageBag.repositoryGeneric.lastPublicationSuccessful), false));
        return new PackageMaintainerDto(
                tuple.get(maintainer.id),
                userProjection,
                tuple.get(packageBag.name),
                repositoryProjection,
                tuple.get(maintainer.deleted));
    }
}

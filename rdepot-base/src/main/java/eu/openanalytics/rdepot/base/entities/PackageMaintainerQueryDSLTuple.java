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
package eu.openanalytics.rdepot.base.entities;

import com.querydsl.core.Tuple;
import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import eu.openanalytics.rdepot.base.technology.InternalTechnology;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

@Getter
public class PackageMaintainerQueryDSLTuple extends Resource {

    private final Tuple tuple;
    private final QPackage packageBag = QPackage.package$;
    private final QPackageMaintainer maintainer = QPackageMaintainer.packageMaintainer;

    public PackageMaintainerQueryDSLTuple(Tuple tuple) {
        super(InternalTechnology.instance, ResourceType.PACKAGE_MAINTAINER_DSL_TUPLE);
        this.tuple = tuple;
    }

    @Override
    public String toString() {
        return tuple.toString();
    }

    public int getRepositoryId() {
        return ObjectUtils.getIfNull(tuple.get(packageBag.repositoryGeneric.id), 0);
    }

    public int getId() {
        return ObjectUtils.getIfNull(tuple.get(maintainer.id), 0);
    }

    public int getUserId() {
        return ObjectUtils.getIfNull(tuple.get(packageBag.user.id), 0);
    }

    @Override
    public Boolean isDeleted() {
        return ObjectUtils.getIfNull(tuple.get(maintainer.deleted), false);
    }
}

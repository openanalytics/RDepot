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
package eu.openanalytics.rdepot.base.api.v2.sorting;

import eu.openanalytics.rdepot.base.utils.BidirectionalMap;

public class DtoToEntityPropertyMappingImpl extends AbstractPropertyMapping {

    static final BidirectionalMap<String> PROPERTIES = new BidirectionalMap<>();

    static {
        PROPERTIES.put("technology", "resourceTechnology");
        PROPERTIES.put("repository", "repository.name");
        PROPERTIES.put("repository.technology", "repository.resourceTechnology");
        PROPERTIES.put("user", "user.name");
        PROPERTIES.put("approver", "approver.name");
        PROPERTIES.put("submitter", "submitter.name");
        PROPERTIES.put("packageBag", "packageBag.name");
        PROPERTIES.put("packageBag.repository", "packageBag.repositoryGeneric.name");
        PROPERTIES.put("packageBag.repository.id", "packageBag.repositoryGeneric.id");
        PROPERTIES.put("packageBag.repository.publicationUri", "packageBag.repositoryGeneric.publicationUri");
        PROPERTIES.put("packageBag.repository.technology", "packageBag.repositoryGeneric.resourceTechnology");
        PROPERTIES.put("created", "createdDate");
    }

    @Override
    protected BidirectionalMap<String> getProperties() {
        return PROPERTIES;
    }
}

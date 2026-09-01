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
package eu.openanalytics.rdepot.base.api.v2.permissions;

import eu.openanalytics.rdepot.base.entities.enums.ResourceType;
import java.util.Map;

public class Permissions {

    private static final Map<ResourceType, String> RESOURCE_TYPES = Map.of(
            ResourceType.PACKAGE_MAINTAINER, "packageMaintainer",
            ResourceType.REPOSITORY_MAINTAINER, "repositoryMaintainer",
            ResourceType.SUBMISSION, "submission",
            ResourceType.PACKAGE, "package",
            ResourceType.REPOSITORY, "repository",
            ResourceType.USER, "user",
            ResourceType.ACCESS_TOKEN, "accessToken",
            ResourceType.EVENT, "event");

    public static String getPermission(ResourceType resourceType, Action action) {
        return RESOURCE_TYPES.get(resourceType).concat(".").concat(action.getValue());
    }
}

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserPermissions {

    private static boolean generateManuals;
    private static boolean replacingPackagesEnabled;

    @Value("${generate-manuals}")
    public void setGenerateManuals(String generateManuals) {
        UserPermissions.generateManuals = Boolean.parseBoolean(generateManuals);
    }

    @Value("${replacing.packages.enabled}")
    public void setReplacingPackagesEnabled(String replacingPackagesEnabled) {
        UserPermissions.replacingPackagesEnabled = Boolean.parseBoolean(replacingPackagesEnabled);
    }

    public static List<String> getAdminPermissions() {
        List<String> permissions = getRepositoryMaintainerPermissions();
        permissions.add(Permissions.getPermission(ResourceType.ACCESS_TOKEN, Action.LIST_ALL));
        permissions.add(Permissions.getPermission(ResourceType.REPOSITORY, Action.CREATE));
        permissions.add(Permissions.getPermission(ResourceType.REPOSITORY_MAINTAINER, Action.CREATE));
        permissions.add(Permissions.getPermission(ResourceType.REPOSITORY_MAINTAINER, Action.LIST));
        permissions.add(Permissions.getPermission(ResourceType.USER, Action.LIST));

        permissions.sort(Comparator.naturalOrder());
        return permissions;
    }

    public static List<String> getRepositoryMaintainerPermissions() {
        List<String> permissions = getRegularUserPermissions();
        permissions.add(Permissions.getPermission(ResourceType.PACKAGE_MAINTAINER, Action.CREATE));
        permissions.add(Permissions.getPermission(ResourceType.PACKAGE_MAINTAINER, Action.LIST));

        permissions.sort(Comparator.naturalOrder());
        return permissions;
    }

    public static List<String> getRegularUserPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Permissions.getPermission(ResourceType.ACCESS_TOKEN, Action.CREATE));
        permissions.add(Permissions.getPermission(ResourceType.ACCESS_TOKEN, Action.LIST_MY));
        permissions.add(Permissions.getPermission(ResourceType.EVENT, Action.LIST));
        permissions.add(Permissions.getPermission(ResourceType.PACKAGE, Action.LIST));
        permissions.add(Permissions.getPermission(ResourceType.REPOSITORY, Action.LIST));
        permissions.add(Permissions.getPermission(ResourceType.SUBMISSION, Action.CREATE));
        permissions.add(Permissions.getPermission(ResourceType.SUBMISSION, Action.LIST));

        if (generateManuals) {
            permissions.add(Permissions.getPermission(ResourceType.SUBMISSION, Action.GENERATE_MANUAL));
        }

        if (replacingPackagesEnabled) {
            permissions.add(Permissions.getPermission(ResourceType.SUBMISSION, Action.REPLACE_PACKAGE));
        }

        permissions.sort(Comparator.naturalOrder());
        return permissions;
    }
}

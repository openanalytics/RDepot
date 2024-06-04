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
package eu.openanalytics.rdepot.base.mediator;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.Role;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.exception.AdminNotFound;
import eu.openanalytics.rdepot.base.mediator.deletion.exceptions.NoSuitableMaintainerFound;
import eu.openanalytics.rdepot.base.service.PackageMaintainerService;
import eu.openanalytics.rdepot.base.service.RepositoryMaintainerService;
import eu.openanalytics.rdepot.base.service.RoleService;
import eu.openanalytics.rdepot.base.service.UserService;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mediator used to choose the best maintainer for a {@link Package}.
 */
@Slf4j
@Component
@Transactional
@AllArgsConstructor
public class BestMaintainerChooser {

    private final RepositoryMaintainerService repositoryMaintainerService;
    private final PackageMaintainerService packageMaintainerService;
    private final UserService userService;
    private final RoleService roleService;

    /**
     * Finds the best package maintainer for a given package.
     */
    public User chooseBestPackageMaintainer(Package packageBag) throws NoSuitableMaintainerFound {
        final List<PackageMaintainer> packageMaintainers =
                packageMaintainerService.findAllByPackageAndRepositoryAndNonDeleted(
                        packageBag.getName(), packageBag.getRepository());

        if (!packageMaintainers.isEmpty()) {
            return packageMaintainers.get(0).getUser();
        }

        final List<RepositoryMaintainer> repositoryMaintainers =
                repositoryMaintainerService.findByRepositoryNonDeleted(packageBag.getRepository());
        if (!repositoryMaintainers.isEmpty()) {
            return repositoryMaintainers.get(0).getUser();
        } else {
            try {
                return findFirstAdmin();
            } catch (AdminNotFound e) {
                log.error(e.getMessage(), e);
            }
        }
        throw new NoSuitableMaintainerFound();
    }

    /**
     * Updates all packages in the list with the most suitable maintainer.
     */
    public void refreshMaintainerForPackages(List<Package> packages) throws NoSuitableMaintainerFound {
        for (Package packageBag : packages) {
            packageBag.setUser(chooseBestPackageMaintainer(packageBag));
        }
    }

    /**
     * Finds any administrator in the system.
     */
    public User findFirstAdmin() throws AdminNotFound {
        final List<User> admins = findAllAdmins();

        if (admins.isEmpty()) throw new AdminNotFound();

        return admins.get(0);
    }

    /**
     * Finds all administrators in the systems.
     */
    public List<User> findAllAdmins() {
        final Optional<Role> adminRole = roleService.findByValue(Role.VALUE.ADMIN);
        if (adminRole.isEmpty()) return List.of();

        return userService.findByRole(adminRole.get());
    }
}

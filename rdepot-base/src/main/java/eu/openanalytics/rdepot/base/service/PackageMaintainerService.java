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
package eu.openanalytics.rdepot.base.service;

import eu.openanalytics.rdepot.base.daos.PackageMaintainerDao;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
public class PackageMaintainerService extends Service<PackageMaintainer> {

    private final PackageMaintainerDao packageMaintainerDao;

    public PackageMaintainerService(PackageMaintainerDao packageMaintainerDao) {
        super(packageMaintainerDao);
        this.packageMaintainerDao = packageMaintainerDao;
    }

    @Transactional
    public Optional<PackageMaintainer> findByPackageAndRepositoryAndNonDeleted(
            String packageName, Repository repository) {
        return packageMaintainerDao.findByPackageNameAndRepositoryIdAndDeleted(packageName, repository.getId(), false);
    }

    public List<PackageMaintainer> findAllByPackageAndRepositoryAndNonDeleted(
            String packageName, Repository repository) {
        return packageMaintainerDao.findAllByPackageNameAndRepositoryIdAndDeleted(
                packageName, repository.getId(), false);
    }

    public Optional<PackageMaintainer> findByUserAndPackageNameAndRepositoryAndNonDeleted(
            User user, String packageName, Repository repository) {
        return packageMaintainerDao.findByUserIdAndPackageNameAndRepositoryIdAndDeleted(
                user.getId(), packageName, repository.getId(), false);
    }

    public boolean existsByUserAndPackageNameAndRepositoryAndNonDeleted(
            User user, String packageName, Repository repository) {
        return packageMaintainerDao.existsByUserIdAndPackageNameAndRepositoryIdAndDeleted(
                user.getId(), packageName, repository.getId(), false);
    }

    public Optional<PackageMaintainer> findByPackageAndRepositoryAndDeleted(String packageName, Repository repository) {
        return packageMaintainerDao.findByPackageNameAndRepositoryIdAndDeleted(packageName, repository.getId(), true);
    }

    public List<PackageMaintainer> findByUser(User user) {
        return packageMaintainerDao.findByUser(user);
    }

    public List<PackageMaintainer> findByRepository(Repository repository) {
        return packageMaintainerDao.findByRepository(repository);
    }

    public List<PackageMaintainer> findByRepositoryNonDeleted(Repository repository) {
        return packageMaintainerDao.findByRepositoryAndDeleted(repository, false);
    }

    public List<PackageMaintainer> findNonDeletedByUser(User user) {
        return packageMaintainerDao.findByUserAndDeleted(user, false);
    }
}

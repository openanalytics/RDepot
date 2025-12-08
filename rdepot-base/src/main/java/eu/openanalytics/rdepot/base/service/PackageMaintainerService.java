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
import eu.openanalytics.rdepot.base.entities.Package;
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

    public void updateWithNewPackages(PackageMaintainer maintainer, List<Package> packages) {
        maintainer.getPackages().addAll(packages);
        packageMaintainerDao.save(maintainer);
    }

    /**
     * Should be used when the package name or repository of a package maintainer entity has changed.
     * Example:
     * If P is a package maintainer for package ABC in repository R,
     * and package maintainer P is updated by changing the maintained package name from ABC to XYZ,
     * then all packages with name ABC from repository R should be removed from the list of packages
     * maintained by package maintainer P.
     * @param maintainer the package maintainer which has an updated package name or repository
     * @param oldPackageName the package name which was previously maintained by the package maintainer
     */
    public void updateRemoveOldPackages(PackageMaintainer maintainer, String oldPackageName) {
        maintainer.getPackages().removeIf(packageBag -> packageBag.getName().equals(oldPackageName));
        packageMaintainerDao.save(maintainer);
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

    public List<PackageMaintainer> findAllByPackageNameAndRepository(String packageName, Repository repository) {
        return packageMaintainerDao.findAllByPackageNameAndRepositoryId(packageName, repository.getId());
    }
}

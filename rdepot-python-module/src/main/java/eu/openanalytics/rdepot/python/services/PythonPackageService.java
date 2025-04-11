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
package eu.openanalytics.rdepot.python.services;

import eu.openanalytics.rdepot.base.entities.comparators.PackageComparator;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.python.daos.PythonPackageDao;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PythonPackageService extends PackageService<PythonPackage> {

    private final PythonPackageDao packageDao;

    public PythonPackageService(PythonPackageDao dao) {
        super(dao);
        this.packageDao = dao;
    }

    public Optional<PythonPackage> findNonDeletedNewestByNormalizedNameAndRepository(
            String normalizedName, PythonRepository repository) {
        List<PythonPackage> packages =
                packageDao.findByNormalizedNameAndRepositoryGenericAndDeleted(normalizedName, repository, false);
        if (packages.isEmpty()) return Optional.empty();

        return Optional.of(Collections.max(packages, new PackageComparator<>()));
    }

    public Optional<PythonPackage> findNonDeletedByNormalizedNameAndVersionAndRepository(
            String normalizedName, String version, PythonRepository repository) {
        List<PythonPackage> packages = packageDao.findAllByNormalizedNameAndRepositoryGenericAndDeletedAndVersionIn(
                normalizedName, repository, false, generateVariantsOfVersion(version));

        if (packages.isEmpty()) return Optional.empty();

        return Optional.of(packages.get(0));
    }
}

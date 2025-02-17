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
package eu.openanalytics.rdepot.r.services;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.service.PackageService;
import eu.openanalytics.rdepot.r.daos.RPackageDao;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@Service
public class RPackageService extends PackageService<RPackage> {

    private final RPackageDao packageDao;

    public RPackageService(RPackageDao dao) {
        super(dao);
        this.packageDao = dao;
    }

    public List<RPackage> findBinaryPackagesByRepository(RRepository repository) {
        return packageDao.findPackagesByRepositoryGeneric(repository, true, false, true);
    }

    public List<RPackage> findSourcePackagesByRepository(RRepository repository) {
        return packageDao.findPackagesByRepositoryGeneric(repository, false, false, true);
    }

    public Optional<RPackage> findByNameAndVersionAndRepositoryAndDeletedAndBinary(
            String name,
            String version,
            Repository repository,
            Boolean deleted,
            Boolean binary,
            String rVersion,
            String architecture,
            String distribution) {
        return packageDao.findByNameAndRepositoryGenericAndDeletedAndBinaryAndVersionIn(
                name,
                repository,
                deleted,
                binary,
                rVersion,
                architecture,
                distribution,
                generateVariantsOfVersion(version));
    }

    @Override
    protected List<RPackage> findSameVersions(RPackage entity) {
        return packageDao.findAllByNameAndRepositoryGenericAndDeletedAndBinaryAndVersionIn(
                entity.getName(),
                entity.getRepository(),
                false,
                entity.isBinary(),
                entity.getRVersion(),
                entity.getArchitecture(),
                entity.getDistribution(),
                generateVariantsOfVersion(entity.getVersion()));
    }

    @Override
    public Set<RPackage> filterLatest(Set<RPackage> packages) {

        MultiValueMap<String, RPackage> latestVersionsMap = new LinkedMultiValueMap<>();

        for (RPackage packageBag : packages) {

            List<RPackage> latestVersions = latestVersionsMap.get(packageBag.getName());
            if (latestVersions == null) {
                latestVersionsMap.add(packageBag.getName(), packageBag);
                continue;
            }

            new ArrayList<>(latestVersions).forEach(latestPackage -> {
                int comparison = comparePackages(packageBag, latestPackage);
                if (comparison == 1) {
                    latestVersions.remove(latestPackage);
                    latestVersions.add(packageBag);
                } else if (comparison == 2) {
                    latestVersions.add(packageBag);
                }
            });
        }

        return latestVersionsMap.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private int comparePackages(RPackage currentPackage, RPackage toComparePackage) {

        int comparisonResult = currentPackage.compareTo(toComparePackage);

        if (currentPackage.isBinary() && toComparePackage.isBinary() && comparisonResult == 0) {
            return 2;
        }

        return comparisonResult;
    }
}

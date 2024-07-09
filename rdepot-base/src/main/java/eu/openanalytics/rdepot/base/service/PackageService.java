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
package eu.openanalytics.rdepot.base.service;

import eu.openanalytics.rdepot.base.daos.PackageDao;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.comparators.PackageComparator;
import eu.openanalytics.rdepot.base.service.exceptions.CreateEntityException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PackageService<E extends Package> extends Service<E> {

    private final PackageDao<E> packageDao;

    public PackageService(PackageDao<E> packageDao) {
        super(packageDao);
        this.packageDao = packageDao;
    }

    @Override
    public E create(E entity) throws CreateEntityException {
        deleteSameVersion(entity);
        return super.create(entity);
    }

    public List<E> findAllByRepository(Repository repository) {
        return packageDao.findByRepositoryGenericAndDeleted(repository, false);
    }

    public int countByRepository(Repository repository) {
        return packageDao.countByRepositoryGenericAndDeleted(repository, false);
    }

    public List<E> findAllByRepositoryIncludeDeleted(Repository repository) {
        return packageDao.findByRepositoryGeneric(repository);
    }

    public List<E> findActiveByRepository(Repository repository) {
        return packageDao.findByRepositoryGenericAndDeletedAndActive(repository, false, true);
    }

    private void deleteSameVersion(E entity) {
        List<E> samePackages = packageDao.findAllByNameAndRepositoryGenericAndDeletedAndVersionIn(
                entity.getName(), entity.getRepository(), false, generateVariantsOfVersion(entity.getVersion()));

        if (!samePackages.isEmpty()) log.debug("Found non-deleted packages of the same name, version and repository.");

        samePackages.forEach(p -> {
            p.setDeleted(true);
            p.setActive(false);
        });
    }

    public List<E> findAllByNameAndRepository(String name, Repository repository) {
        return packageDao.findByNameAndRepositoryGenericAndDeleted(name, repository, false);
    }

    public Optional<E> findByNameAndVersionAndRepositoryAndDeleted(
            String name, String version, Repository repository, Boolean deleted) {
        return packageDao.findByNameAndRepositoryGenericAndDeletedAndVersionIn(
                name, repository, deleted, generateVariantsOfVersion(version));
    }

    public Optional<E> findNonDeletedNewestByNameAndRepository(String name, Repository repository) {
        List<E> packages = packageDao.findByNameAndRepositoryGenericAndDeleted(name, repository, false);
        if (packages.isEmpty()) return Optional.empty();

        return Optional.of(Collections.max(packages, new PackageComparator<>()));
    }

    public Optional<E> findNonDeletedByNameAndVersionAndRepository(String name, String version, Repository repository) {
        List<E> packages = packageDao.findAllByNameAndRepositoryGenericAndDeletedAndVersionIn(
                name, repository, false, generateVariantsOfVersion(version));

        if (packages.isEmpty()) return Optional.empty();

        return Optional.of(packages.get(0));
    }

    /**
     * Filters out packages meant to be archived.
     * It does not modify given set, rather returns a new one.
     */
    public Set<E> filterLatest(Set<E> packages) {
        LinkedHashMap<String, E> latestVersionMap = new LinkedHashMap<>();

        for (E packageBag : packages) {
            E latest = latestVersionMap.get(packageBag.getName());

            if (latest == null || packageBag.compareTo(latest) > 0) {
                latestVersionMap.put(packageBag.getName(), packageBag);
            }
        }

        return new LinkedHashSet<>(latestVersionMap.values());
    }

    protected Collection<String> generateVariantsOfVersion(String version) {
        List<String> variants = new ArrayList<>();
        String[] splitted = version.split("-|\\.");
        int length = splitted.length;

        int numberOfVariations = 1 << (length - 1);

        // 0 in schema means dot
        // 1 in schema means hyphen
        for (int i = 0; i < numberOfVariations; i++) {
            String schema = String.format("%" + (length - 1) + "s", Integer.toBinaryString(i))
                    .replace(' ', '0');
            String newVersion = "";
            for (int j = 0; j < length - 1; j++) {
                newVersion += splitted[j];
                char separator = schema.charAt(j);
                if (separator == '0') {
                    newVersion += ".";
                } else {
                    newVersion += "-";
                }
            }
            newVersion += splitted[length - 1];
            variants.add(newVersion);
        }

        return variants;
    }
}

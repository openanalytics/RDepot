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
package eu.openanalytics.rdepot.base.daos;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * {@link org.springframework.data.jpa.repository.JpaRepository JPA Repository}
 * for {@link Package Packages}.
 * @param <T> technology-specific Package class
 */
public interface PackageDao<T extends Package> extends Dao<T> {
    List<T> findByRepositoryGenericAndDeleted(Repository repositoryGeneric, boolean deleted);

    List<T> findByNameAndRepositoryGenericAndDeleted(String name, Repository repositoryGeneric, boolean deleted);

    Optional<T> findByNameAndRepositoryGenericAndDeletedAndVersionIn(
            String name, Repository repositoryGeneric, Boolean deleted, Collection<String> versions);

    List<T> findAllByNameAndRepositoryGenericAndDeletedAndVersionIn(
            String name, Repository repositoryGeneric, boolean deleted, Collection<String> versions);

    List<T> findByRepositoryGenericAndDeletedAndActive(Repository repositoryGeneric, boolean deleted, boolean active);

    List<T> findByRepositoryGeneric(Repository repositoryGeneric);

    int countByRepositoryGenericAndDeleted(Repository repository, boolean deleted);
}

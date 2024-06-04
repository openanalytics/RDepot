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

import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * {@link org.springframework.data.jpa.repository.JpaRepository JPA Repository}
 * for {@link PackageMaintainer Package Maintainers}.
 */
public interface PackageMaintainerDao extends Dao<PackageMaintainer> {
    List<PackageMaintainer> findByUser(User user);

    List<PackageMaintainer> findByRepository(Repository repository);

    Page<PackageMaintainer> findByRepository(Repository repository, Pageable pageable);

    List<PackageMaintainer> findByRepositoryAndDeleted(Repository repository, Boolean deleted);

    List<PackageMaintainer> findByUserAndDeleted(User user, boolean deleted);

    @Query(
            value =
                    "SELECT pm FROM PackageMaintainer pm "
                            + "WHERE pm.packageName = :packageName AND pm.repository.id = :repositoryId AND pm.deleted = :deleted")
    Optional<PackageMaintainer> findByPackageNameAndRepositoryIdAndDeleted(
            @Param("packageName") String packageName,
            @Param("repositoryId") int repositoryId,
            @Param("deleted") boolean deleted);

    @Query(
            value =
                    "SELECT pm FROM PackageMaintainer pm "
                            + "WHERE pm.packageName = :packageName AND pm.repository.id = :repositoryId AND pm.deleted = :deleted")
    List<PackageMaintainer> findAllByPackageNameAndRepositoryIdAndDeleted(
            @Param("packageName") String packageName,
            @Param("repositoryId") int repositoryId,
            @Param("deleted") boolean deleted);

    @Query(
            value =
                    "SELECT pm FROM PackageMaintainer pm "
                            + "WHERE pm.user.id = :userId AND pm.packageName = :packageName AND pm.repository.id = :repositoryId AND pm.deleted = :deleted")
    Optional<PackageMaintainer> findByUserIdAndPackageNameAndRepositoryIdAndDeleted(
            @Param("userId") int userId,
            @Param("packageName") String packageName,
            @Param("repositoryId") int repositoryId,
            @Param("deleted") boolean deleted);

    @Query(
            value =
                    "SELECT case when (count(pm.id) > 0) then true else false end FROM PackageMaintainer pm "
                            + "WHERE pm.user.id = :userId AND pm.packageName = :packageName AND pm.repository.id = :repositoryId AND pm.deleted = :deleted")
    boolean existsByUserIdAndPackageNameAndRepositoryIdAndDeleted(
            @Param("userId") int userId,
            @Param("packageName") String packageName,
            @Param("repositoryId") int repositoryId,
            @Param("deleted") boolean deleted);
}

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
package eu.openanalytics.rdepot.r.daos;

import eu.openanalytics.rdepot.base.daos.PackageDao;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RPackageDao extends PackageDao<RPackage> {

    @Query(
            value = "SELECT rp FROM RPackage rp "
                    + "WHERE rp.repository = :repositoryGeneric "
                    + "AND rp.binary = :binaryPackage "
                    + "AND rp.deleted = :deleted "
                    + "AND rp.active = :active")
    List<RPackage> findPackagesByRepositoryGeneric(
            @Param("repositoryGeneric") RRepository repositoryGeneric,
            @Param("binaryPackage") boolean binary_package,
            @Param("deleted") boolean deleted,
            @Param("active") boolean active);

    @Query(
            value = "SELECT rp FROM RPackage rp "
                    + "WHERE rp.name = :name "
                    + "AND rp.repository = :repositoryGeneric "
                    + "AND rp.binary = :binaryPackage "
                    + "AND rp.deleted = :deleted "
                    + "AND rp.rVersion = :rVersion "
                    + "AND rp.architecture = :architecture "
                    + "AND rp.distribution = :distribution "
                    + "AND rp.version IN :versions")
    Optional<RPackage> findByNameAndRepositoryGenericAndDeletedAndBinaryAndVersionIn(
            @Param("name") String name,
            @Param("repositoryGeneric") Repository repositoryGeneric,
            @Param("deleted") Boolean deleted,
            @Param("binaryPackage") Boolean binary_package,
            @Param("rVersion") String r_version,
            @Param("architecture") String architecture,
            @Param("distribution") String distribution,
            @Param("versions") Collection<String> versions);

    @Query(
            value = "SELECT rp FROM RPackage rp "
                    + "WHERE rp.name = :name "
                    + "AND rp.repository = :repositoryGeneric "
                    + "AND rp.binary = :binaryPackage "
                    + "AND rp.deleted = :deleted "
                    + "AND rp.rVersion = :rVersion "
                    + "AND rp.architecture = :architecture "
                    + "AND rp.distribution = :distribution "
                    + "AND rp.version IN :versions")
    List<RPackage> findAllByNameAndRepositoryGenericAndDeletedAndBinaryAndVersionIn(
            @Param("name") String name,
            @Param("repositoryGeneric") Repository repositoryGeneric,
            @Param("deleted") Boolean deleted,
            @Param("binaryPackage") Boolean binary_package,
            @Param("rVersion") String r_version,
            @Param("architecture") String architecture,
            @Param("distribution") String distribution,
            @Param("versions") Collection<String> versions);
}

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
package eu.openanalytics.rdepot.base.daos;

import eu.openanalytics.rdepot.base.api.v2.dtos.MaintainedPackageDto;
import eu.openanalytics.rdepot.base.entities.Package;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface MaintainedPackageDao extends Dao<Package> {

    @Query(
            value =
                    """
                SELECT DISTINCT new eu.openanalytics.rdepot.base.api.v2.dtos.MaintainedPackageDto(
                    p.name,
                    r.id,
                    r.name
                )
                FROM Package p
                JOIN p.repositoryGeneric r
                JOIN p.user u
                WHERE u.id = :userId
                AND (:name IS NULL OR p.name LIKE :name)
                AND (:repositoryId IS NULL OR r.id = :repositoryId)
                AND (:repositoryName IS NULL OR r.name LIKE :repositoryName)
            """,
            countQuery =
                    """
                SELECT COUNT(*)
                FROM (
                    SELECT DISTINCT
                        p.name AS name,
                        r.id AS repo_id,
                        r.name AS repo_name
                    FROM Package p
                    JOIN p.repositoryGeneric r
                    JOIN p.user u
                    WHERE u.id = :userId
                    AND (:name IS NULL OR p.name LIKE :name)
                    AND (:repositoryId IS NULL OR r.id = :repositoryId)
                    AND (:repositoryName IS NULL OR r.name LIKE :repositoryName)
                )""")
    Page<MaintainedPackageDto> findAllMaintainedPackages(
            int userId, String name, Integer repositoryId, String repositoryName, Pageable pageable);
}

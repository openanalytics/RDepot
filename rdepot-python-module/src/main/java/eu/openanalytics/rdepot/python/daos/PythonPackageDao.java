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
package eu.openanalytics.rdepot.python.daos;

import eu.openanalytics.rdepot.base.daos.PackageDao;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PythonPackageDao extends PackageDao<PythonPackage> {

    List<PythonPackage> findByNormalizedNameAndRepositoryGenericAndDeleted(
            String name, PythonRepository repositoryGeneric, boolean deleted);

    List<PythonPackage> findAllByNormalizedNameAndRepositoryGenericAndDeletedAndVersionIn(
            String name, PythonRepository repositoryGeneric, boolean deleted, Collection<String> versions);

    @Query(
            value = "SELECT pp FROM PythonPackage pp "
                    + "WHERE pp.normalizedName = :normalizedName "
                    + "AND pp.repository = :repository "
                    + "AND pp.binary = :binaryPackage "
                    + "AND pp.deleted = :deleted "
                    + "AND (pp.buildTag = :buildTag OR (pp.buildTag IS NULL AND :buildTag IS NULL)) "
                    + "AND (pp.pythonTag = :pythonTag OR (pp.pythonTag IS NULL AND :pythonTag IS NULL)) "
                    + "AND (pp.abiTag = :abiTag OR (pp.abiTag IS NULL AND :abiTag IS NULL)) "
                    + "AND (pp.platformTag = :platformTag OR (pp.platformTag IS NULL AND :platformTag IS NULL)) "
                    + "AND pp.version IN :versions")
    Optional<PythonPackage> findByNormalizedNameAndRepositoryGenericAndDeletedAndBinaryAndVersionIn(
            @Param("normalizedName") String normalizedName,
            @Param("repository") Repository repository,
            @Param("deleted") Boolean deleted,
            @Param("binaryPackage") Boolean binaryPackage,
            @Param("buildTag") String buildTag,
            @Param("pythonTag") String pythonTag,
            @Param("abiTag") String abiTag,
            @Param("platformTag") String platformTag,
            @Param("versions") Collection<String> versions);

    @Query(
            value = "SELECT pp FROM PythonPackage pp "
                    + "WHERE pp.name = :name "
                    + "AND pp.repository = :repository "
                    + "AND pp.binary = :binaryPackage "
                    + "AND pp.deleted = :deleted "
                    + "AND (pp.buildTag = :buildTag OR (pp.buildTag IS NULL AND :buildTag IS NULL)) "
                    + "AND (pp.pythonTag = :pythonTag OR (pp.pythonTag IS NULL AND :pythonTag IS NULL)) "
                    + "AND (pp.abiTag = :abiTag OR (pp.abiTag IS NULL AND :abiTag IS NULL)) "
                    + "AND (pp.platformTag = :platformTag OR (pp.platformTag IS NULL AND :platformTag IS NULL)) "
                    + "AND pp.version IN :versions")
    List<PythonPackage> findAllByNameAndRepositoryGenericAndDeletedAndBinaryAndVersionIn(
            @Param("name") String name,
            @Param("repository") Repository repository,
            @Param("deleted") Boolean deleted,
            @Param("binaryPackage") Boolean binaryPackage,
            @Param("buildTag") String buildTag,
            @Param("pythonTag") String pythonTag,
            @Param("abiTag") String abiTag,
            @Param("platformTag") String platformTag,
            @Param("versions") Collection<String> versions);
}

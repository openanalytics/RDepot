/**
 * R Depot
 *
 * Copyright (C) 2012-2020 Open Analytics NV
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.model.PackageMaintainer;
import eu.openanalytics.rdepot.model.Repository;

@org.springframework.stereotype.Repository
@Transactional(readOnly = true)
public interface PackageMaintainerRepository extends JpaRepository<PackageMaintainer, Integer>
{
	public PackageMaintainer findByPackageAndRepository(String package_, Repository repository);
	
	public PackageMaintainer findByPackageAndRepositoryAndDeleted(String package_, Repository repository, boolean deleted);

	public List<PackageMaintainer> findByRepository(Repository repository);

	public PackageMaintainer findByIdAndDeleted(int id, boolean deleted);

	public List<PackageMaintainer> findByDeleted(boolean deleted, Sort sort);
}

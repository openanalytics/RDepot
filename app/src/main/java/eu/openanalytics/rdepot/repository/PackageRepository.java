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

import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.User;

@org.springframework.stereotype.Repository
@Transactional(readOnly = true)
public interface PackageRepository extends JpaRepository<Package, Integer> {

	public List<Package> findByRepository(Repository repository);

	public List<Package> findByRepositoryAndActive(Repository repository, boolean active);

	public List<Package> findByNameAndRepository(String name, Repository repository);

	public Package findByNameAndVersionAndRepository(String name, String version, Repository repository);

	public List<Package> findByRepositoryAndUser(Repository repository,	User user);

	public Package findByIdAndDeleted(int id, boolean deleted);

	public List<Package> findByDeleted(boolean deleted, Sort sort);
	
	public List<Package> findByRepositoryAndDeleted(Repository repository, boolean deleted);

	public List<Package> findByRepositoryAndUserAndDeleted(Repository repository, User maintainer, boolean deleted);

	public List<Package> findByRepositoryAndActiveAndDeleted(Repository repository, boolean active, boolean deleted, Sort sort);

	public List<Package> findByNameAndRepositoryAndDeleted(String name,	Repository repository, boolean deleted);
	
	public List<Package> findByNameAndRepositoryAndActive(String name,	Repository repository, boolean active);

	public Package findByNameAndVersionAndRepositoryAndDeleted(String name,	String version, Repository repository, boolean deleted);

}

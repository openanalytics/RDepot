/**
 * R Depot
 *
 * Copyright (C) 2012-2022 Open Analytics NV
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

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

//	public Package findByNameAndVersionAndRepository(String name, String version, Repository repository);

	public List<Package> findByRepositoryAndUser(Repository repository,	User user);

	public Package findByIdAndDeleted(int id, boolean deleted);

	public List<Package> findByDeleted(boolean deleted, Sort sort);
	
	@Query(value = "select package.* from submission s "
			+ "join package package on s.package_id = package.id and package.deleted = ?1 "
			+ "join repository repository on package.repository_id = repository.id "
			+ "where s.accepted = true",
			nativeQuery = true)
	public Page<Package> findByDeleted(Boolean deleted, Pageable pageable);
	
	public List<Package> findByRepositoryAndDeleted(Repository repository, boolean deleted);

	public List<Package> findByRepositoryAndUserAndDeleted(Repository repository, User maintainer, boolean deleted);

	public List<Package> findByRepositoryAndActiveAndDeleted(Repository repository, boolean active, boolean deleted, Sort sort);

	public List<Package> findByNameAndRepositoryAndDeleted(String name,	Repository repository, boolean deleted);
	
	public List<Package> findByNameAndRepositoryAndActive(String name,	Repository repository, boolean active);
	
	public Package findByNameAndRepositoryAndDeletedAndVersionIn(String name, Repository repository, boolean deleted, Collection<String> versions);
	
	@Query(value = "select p.* from submission s "
			+ "join package p on s.package_id = p.id and p.deleted = false "
			+ "where s.accepted = true",
			nativeQuery = true)
	List<Package> findNonDeletedByAcceptedSubmission();
	
	@Query(value = "select package.* from submission s "
			+ "join package package on s.package_id = package.id and package.deleted = false "
			+ "join repository repository on package.repository_id = repository.id "
			+ "where s.accepted = true",
			nativeQuery = true)
	Page<Package> findNonDeletedByAcceptedSubmission(Pageable pageable);
	
	@Query(value = "select p.* from submission s "
			+ "join package p on s.package_id = p.id and p.deleted = false "
			+ "join repository r on p.repository_id = r.id and r.name = ?1 "
			+ "where s.accepted = true",
			nativeQuery = true)
	List<Package> findNonDeletedByRepositoryNameAndAcceptedSubmission(String repositoryName);
	
	@Query(value = "select package.* from submission s "
			+ "join package package on s.package_id = package.id and package.deleted = false "
			+ "join repository repository on package.repository_id = repository.id and repository.name = ?1 "
			+ "where s.accepted = true",
			nativeQuery = true)
	Page<Package> findNonDeletedByRepositoryNameAndAcceptedSubmission(String repositoryName, Pageable pageable);
	
	@Query(value = "select package.* from submission s "
			+ "join package package on s.package_id = package.id and package.deleted = true "
			+ "join repository repository on package.repository_id = repository.id and repository.name = ?1 "
			+ "where s.accepted = true",
			nativeQuery = true)
	Page<Package> findDeletedByRepositoryNameAndAcceptedSubmission(String repositoryName, Pageable pageable);

	@Query(value = "select package.* from submission s "
			+ "join package package on s.package_id = package.id "
			+ "join repository repository on package.repository_id = repository.id "
			+ "where s.accepted = true",
			nativeQuery = true)
	public Page<Package> findAllAcceptedSubmissions(Pageable pageable);

	@Query(value = "select package.* from submission s "
			+ "join package package on s.package_id = package.id "
			+ "join repository repository on package.repository_id = repository.id and repository.name = ?1 "
			+ "where s.accepted = true",
			nativeQuery = true)
	public Page<Package> findByRepositoryNameAndAcceptedSubmission(String repositoryName, Pageable pageable);
}

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

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import eu.openanalytics.rdepot.model.Package;
import eu.openanalytics.rdepot.model.Repository;
import eu.openanalytics.rdepot.model.Submission;
import eu.openanalytics.rdepot.model.User;

@org.springframework.stereotype.Repository
@Transactional(readOnly = true)
public interface SubmissionRepository extends JpaRepository<Submission, Integer> {

	List<Submission> findByUser(User submitter);

	List<Submission> findByUser(User submitter, Sort sort);

	List<Submission> findByPackage_Repository(Repository repository);

	Submission findByPackage(Package packageBag);

	Submission findByIdAndDeleted(int id, boolean deleted);

	List<Submission> findByDeleted(boolean deleted, Sort sort);

	List<Submission> findByUserAndDeleted(User submitter, boolean deleted, Sort sort);

	List<Submission> findByDeletedAndPackage_Repository(boolean deleted, Repository repository);

	Submission findByPackageAndDeleted(Package packageBag, boolean deleted);

	Page<Submission> findByAcceptedAndDeleted(boolean accepted, boolean deleted, Pageable pageable);
	
	Page<Submission> findByUserAndAcceptedAndDeleted(User submitter, boolean accepted, boolean deleted, Pageable pageable);

////	@Query()
//	public Page<Submission> findAllForRequester(User requester, User user, Boolean deleted, Boolean accepted,
//			Pageable pageable);
	
	@Query(value = "select s from Submission s "
			+ "where s.user = :requester "
			+ "and (:user is null or s.user = :user) "
			+ "and (:deleted is null or s.deleted = :deleted) "
			+ "and (:accepted is null or s.accepted = :accepted)")
	Page<Submission> findAllForUser(@Param("requester") User requester, @Param("user") User user, @Param("deleted") Boolean deleted, @Param("accepted") Boolean accepted, Pageable pageable);
	
	@Query(value = "select s from Submission s join PackageMaintainer m on m.package = s.package.name "
			+ "and m.repository = s.package.repository "
			+ "where (m.user = :requester "
			+ "or s.user = :requester) "
			+ "and (:user is null or s.user = :user) "
			+ "and (:deleted is null or s.deleted = :deleted) "
			+ "and (:accepted is null or s.accepted = :accepted) ")
	Page<Submission> findAllForPackageMaintainer(@Param("requester") User requester, @Param("user") User user, @Param("deleted") Boolean deleted, @Param("accepted") Boolean accepted, Pageable pageable);
	
	@Query(value = "select s from Submission s "
			+ "join RepositoryMaintainer m on s.package.repository = m.repository "
			+ "where (m.user = :requester or s.user=:requester) "
			+ "and  ((:deleted is null or s.deleted = :deleted) "
			+ "and (:accepted is null or s.accepted = :accepted))" 
			+ "and (:user is null or s.user = :user)")
	Page<Submission> findAllForRepositoryMaintainer(@Param("requester") User requester, @Param("user") User user, @Param("deleted") Boolean deleted, @Param("accepted") Boolean accepted, Pageable pageable);
	
	@Query(value = "select s from Submission s where "
			+ "(:deleted is null or s.deleted = :deleted) "
			+ "and (:accepted is null or s.accepted = :accepted) "
			+ "and (:user is null or s.user = :user)")
	Page<Submission> findAll(@Param("user") User user, @Param("deleted") Boolean deleted, @Param("accepted") Boolean accepted, Pageable pageable);
}

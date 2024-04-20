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

import java.util.List;
import java.util.Optional;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;

/**
 * {@link org.springframework.data.jpa.repository.JpaRepository JPA Repository}
 * for {@link Package Packages}.
 * @param <T> technology-specific Package class
 */
public interface PackageDao<T extends Package> extends Dao<T> {
	List<T> findByRepositoryGenericAndDeleted(Repository repositoryGeneric, boolean deleted);
	List<T> findByNameAndRepositoryGenericAndDeleted(String name, Repository repositoryGeneric, boolean deleted);
	List<T> findByNameAndVersionAndRepositoryGeneric(String name, String version, Repository repositoryGeneric);
	Optional<T> findByNameAndVersionAndRepositoryGenericAndDeleted(String name, String version, Repository repositoryGeneric, Boolean deleted);
	List<T> findAllByNameAndVersionAndRepositoryGenericAndDeleted(String name, String version, Repository repositoryGeneric,
			boolean deleted);
	List<T> findByDeletedAndSubmissionState(boolean deleted, SubmissionState state);
	List<T> findByRepositoryGenericAndDeletedAndActive(Repository repositoryGeneric, boolean deleted, boolean active);
	List<T> findByRepositoryGeneric(Repository repositoryGeneric);

	int countByRepositoryGenericAndDeleted(Repository repository, boolean deleted);
}

/**
 * R Depot
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.base.daos;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.enums.SubmissionState;

public interface PackageDao<T extends Package<?,?>> extends Dao<T> {
	List<T> findByRepositoryAndDeleted(Repository<?,?> repository, boolean deleted);
	List<T> findByNameAndRepositoryAndDeleted(String name, Repository<?,?> repository, boolean deleted);
	List<T> findByNameAndVersionAndRepository(String name, String version, Repository<?, ?> repository);
	Optional<T> findByNameAndVersionAndRepositoryAndDeleted(String name, String version, Repository<?, ?> repository, Boolean deleted);
	List<T> findByRepositoryAndActiveAndDeleted(Repository<?, ?> repository, boolean active, boolean deleted);
	List<T> findAllByNameAndVersionAndRepositoryAndDeleted(String name, String version, Repository<?, ?> repository,
			boolean deleted);
	List<T> findByRepositoryAndDeletedAndActive(Repository<?, ?> repository, boolean deleted, boolean active);
	List<T> findByDeletedAndSubmissionState(boolean deleted, SubmissionState state);
	Optional<T> findByNameAndRepositoryAndDeletedAndVersionIn(String name, Repository<?,?> repository, boolean deleted, Collection<String> versions);
}

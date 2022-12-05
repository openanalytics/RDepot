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
package eu.openanalytics.rdepot.base.daos;

import java.util.List;
import java.util.Optional;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;

public interface RepositoryMaintainerDao extends Dao<RepositoryMaintainer> {
//	List<RepositoryMaintainer> findByUser(User user);
	List<RepositoryMaintainer> findByRepository(Repository<?,?> repository);
	List<RepositoryMaintainer> findByRepositoryAndDeleted(Repository<?,?> repository, boolean deleted);
	List<RepositoryMaintainer> findByUserAndDeleted(User user, boolean deleted);
	List<RepositoryMaintainer> findByUserAndRepository(User user, Repository<?,?> repository);
	Optional<RepositoryMaintainer> findByRepositoryAndUserAndDeleted(Repository<?,?> repository, User user, boolean deleted);
}

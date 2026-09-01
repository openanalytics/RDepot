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

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

/**
 * {@link org.springframework.data.jpa.repository.JpaRepository JPA Repository}
 * for {@link RepositoryMaintainer Repository Maintainers}.
 */
public interface RepositoryMaintainerDao extends Dao<RepositoryMaintainer> {
    List<RepositoryMaintainer> findByRepository(Repository repository);

    List<RepositoryMaintainer> findByRepositoryAndDeleted(Repository repository, boolean deleted);

    List<RepositoryMaintainer> findByUserAndDeleted(User user, boolean deleted);

    List<RepositoryMaintainer> findByUserAndRepository(User user, Repository repository);

    @Query(
            value = "SELECT rm "
                    + "FROM RepositoryMaintainer rm "
                    + "WHERE rm.user.id = :userId "
                    + "AND rm.repository.id = :repoId")
    List<RepositoryMaintainer> findByUserIdAndRepositoryId(int userId, int repoId);

    Optional<RepositoryMaintainer> findByRepositoryAndUserAndDeleted(Repository repository, User user, boolean deleted);

    boolean existsByRepositoryAndUserAndDeleted(Repository repository, User user, boolean deleted);
}

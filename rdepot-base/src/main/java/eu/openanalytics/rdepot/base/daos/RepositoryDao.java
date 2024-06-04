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

import eu.openanalytics.rdepot.base.entities.Repository;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * {@link org.springframework.data.jpa.repository.JpaRepository JPA Repository}
 * for {@link Repository Repositories}.
 * @param <T> technology-specific Repository class
 */
@Primary
public interface RepositoryDao<T extends Repository> extends Dao<T> {
    Optional<T> findByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Repository r WHERE r.name = :name")
    Optional<T> findByNameAcquirePessimisticWriteLock(@Param("name") String name);

    Optional<T> findByNameAndDeleted(String name, boolean deleted);

    Optional<T> findByPublicationUri(String publicationUri);

    Optional<T> findByServerAddress(String serverAddress);
}

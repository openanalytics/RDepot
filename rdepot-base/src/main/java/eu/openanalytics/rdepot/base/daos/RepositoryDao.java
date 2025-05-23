/*
 * RDepot
 *
 * Copyright (C) 2012-2025 Open Analytics NV
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
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying
    @Query("UPDATE Repository r SET r.version = r.version + 1 WHERE r.name = :name")
    int incrementRepositoryVersion(@Param("name") String name);

    Optional<T> findByNameAndDeleted(String name, boolean deleted);

    Optional<T> findByPublicationUri(String publicationUri);

    Optional<T> findByServerAddress(String serverAddress);

    Optional<T> findByServerAddressStartsWith(String serverAddress);
}

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
package eu.openanalytics.rdepot.base.service;

import eu.openanalytics.rdepot.base.daos.Dao;
import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.base.service.exceptions.CreateEntityException;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Default service, used to access resources in the database.
 * It is used for both reading and writing and
 * also for some basic business logic related to the {@link Resource resource}.
 * For read-only access it suffices
 * to use {@link Retriever Retriever} implementation.
 * @param <E> Entity class related to the resource.
 */
@Slf4j
public abstract class Service<E extends Resource> extends SpringDataJpaCapableRetriever<E> {

    /**
     * @param dao Data Access Object to interact
     *  with external data source, e.g. a {@link JpaRepository}
     */
    protected Service(Dao<E> dao) {
        super(dao);
    }

    /**
     * Creates entity in the database.
     * @param entity created entity, its id must 0
     */
    public E create(E entity) throws CreateEntityException {
        try {
            return dao.save(entity);
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new CreateEntityException();
        }
    }

    /**
     * Deletes entity from the database.
     */
    public void delete(E entity) throws DeleteEntityException {
        try {
            dao.delete(entity);
        } catch (DataAccessException e) {
            log.error(e.getMessage(), e);
            throw new DeleteEntityException();
        }
    }

    /**
     * Deletes entity of given id.
     * If entity cannot be found, nothing happens.
     */
    public void delete(int id) {
        findById(id).ifPresent(r -> {
            try {
                delete(r);
            } catch (DeleteEntityException e) {
                log.error(e.getMessage(), e);
            }
        });
    }
}

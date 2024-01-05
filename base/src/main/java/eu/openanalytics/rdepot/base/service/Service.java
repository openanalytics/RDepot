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
package eu.openanalytics.rdepot.base.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import eu.openanalytics.rdepot.base.daos.Dao;
import eu.openanalytics.rdepot.base.entities.Resource;
import eu.openanalytics.rdepot.base.service.exceptions.CreateEntityException;
import eu.openanalytics.rdepot.base.service.exceptions.DeleteEntityException;

/**
 * Default service, used to access resources in the database.
 * @param <E> Entity class related to the resource.
 */
public abstract class Service<E extends Resource> extends SpringDataJpaCapableRetriever<E> {
	
	private static Logger logger = LoggerFactory.getLogger(Service.class);
	
	/**
	 * @param dao Data Access Object to interact with external data source.
	 */
	public Service(Dao<E> dao) {
		super(dao);
	}

	/**
	 * Creates entity in the database.
	 * @param entity created entity
	 * @throws CreateEntityException
	 * @return
	 */
	public E create(E entity) throws CreateEntityException {
		try {
			logger.debug("Creating resource: " + entity.toString());
			return dao.save(entity);
		} catch(DataAccessException e) {
			logger.error(e.getMessage(), e);
			throw new CreateEntityException();
		}
	}
	
	/**
	 * Deletes entity from the database.
	 * @param entity deleted entity
	 * @throws DeleteEntityException 
	 */
	public void delete(E entity) throws DeleteEntityException {
		try {
			logger.debug("Deleting resource: " + entity);
			dao.delete(entity);
		} catch(DataAccessException e) {
			logger.error(e.getMessage(), e);
			throw new DeleteEntityException();
		}
		
	}
	
	/**
	 * Deletes entity of given id.
	 * If entity cannot be found, it silently fails.
	 * @param id
	 */
	public void delete(int id) {
		findById(id).ifPresent(r -> {
			try {
				delete(r);
			} catch (DeleteEntityException e) {
				logger.error(e.getMessage(), e);
			}
		});
	}
}

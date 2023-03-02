/**
 * R Depot
 *
 * Copyright (C) 2012-2023 Open Analytics NV
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

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import eu.openanalytics.rdepot.base.entities.Resource;

/**
 * Used to fetch resources from the database.
 * @param <E>
 */
public interface Retriever<E extends Resource> {
	/**
	 * Fetches a single resource using given ID.
	 * @param id
	 * @return
	 */
	Optional<E> findById(Integer id);
	
	/**
	 * Fetches a single resource based on properties of given entity.
	 * @param entity entity object
	 * @return
	 */
	Optional<E> findOneByParameters(E entity);
	
	/**
	 * Fetches a single not soft-deleted resource using given ID.
	 * @param id
	 * @return
	 */
	Optional<E> findOneNonDeleted(Integer id);
	
	/**
	 * Fetches a single soft-deleted resource using given ID.
	 * @param id
	 * @return
	 */
	Optional<E> findOneDeleted(Integer id);
	
	/**
	 * Fetches a page of resources based on properties of given entity.
	 * @param pageable used for pagination
	 * @param entity entity object
	 * @return
	 */
	Page<E> findByParameters(Pageable pageable, E entity);
	
	/**
	 * Fetches a page of resources based on example of given entity.
	 * @param pageable used for pagination
	 * @param example
	 * @return
	 */
	Page<E> findAllByExample(Pageable pageable, Example<E> example);
	
	/**
	 * Fetches all elements from a database.
	 * @param pageable used for pagination
	 * @return
	 */
	Page<E> findAll(Pageable pageable);
	
	/**
	 * Fetches all paginated elements using given specification.
	 * @param specification
	 * @param pageable
	 * @return
	 */
	Page<E> findAllBySpecification(Specification<E> specification, Pageable pageable);
	
	/**
	 * Fetches all elements using given specification.
	 * @param specification
	 * @param pageable
	 * @return
	 */
	List<E> findAllBySpecification(Specification<E> specification);
	
	/**
	 * Fetches all elements based on deleted property.
	 * @param pageable used for pagination
	 * @param deleted boolean property
	 * @return
	 */
	Page<E> findByDeleted(Pageable pageable, boolean deleted);
	
	/**
	 * Fetches all elements from a database.
	 * @return
	 */
	List<E> findAll();
	
	/**
	 * Fetches all sorted elements from a database.
	 * @return
	 */
	List<E> findAllSorted(Sort sort);
	
	/**
	 * Fetches all resources based on properties of given entity.
	 * @param entity entity object
	 * @return
	 */
	List<E> findAllByExample(E entity);	
	
	/**
	 * Fetches all soft-deleted resources.
	 * @return
	 */
	List<E> findAllDeleted();
}

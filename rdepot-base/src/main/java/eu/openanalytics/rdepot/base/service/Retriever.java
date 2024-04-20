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

import eu.openanalytics.rdepot.base.entities.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * Used to fetch resources from the database.
 * @param <E>
 */
public interface Retriever<E extends Resource> {
	/**
	 * Fetches a single resource using given ID.
	 */
	Optional<E> findById(int id);
	
	/**
	 * Fetches a single not soft-deleted resource using given ID.
	 */
	Optional<E> findOneNonDeleted(int id);
	
	/**
	 * Fetches a single soft-deleted resource using given ID.
	 */
	Optional<E> findOneDeleted(int id);
	
	/**
	 * Fetches all elements from a database.
	 */
	Page<E> findAll(Pageable pageable);
	
	/**
	 * Fetches all paginated elements using given specification.
	 */
	Page<E> findAllBySpecification(Specification<E> specification, Pageable pageable);
	
	/**
	 * Fetches all elements using given specification.
	 */
	List<E> findAllBySpecification(Specification<E> specification);
	
	/**
	 * Fetches all elements from a database.
	 */
	List<E> findAll();
}

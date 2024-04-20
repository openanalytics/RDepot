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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Default service, used to fetch resources from the database.
 * It implies the usage of {@link JpaRepository}
 * @param <E> Entity class related to the resource.
 */
public abstract class SpringDataJpaCapableRetriever<E extends Resource> implements Retriever<E> {
	protected Dao<E> dao;
	
	/**
	 * @param dao Data Access Object to interact with external data source.
	 */
	public SpringDataJpaCapableRetriever(Dao<E> dao) {
		this.dao = dao;
	}
	
	@Override
	public Optional<E> findById(int id) {
		return dao.findById(id);
	}

	@Override
	public Page<E> findAll(Pageable pageable) {
		return dao.findAll(pageable);
	}

	@Override
	public Page<E> findAllBySpecification(Specification<E> specification, Pageable pageable) {
		return dao.findAll(specification, pageable);
	}

	@Override
	public List<E> findAllBySpecification(Specification<E> specification) {
		return dao.findAll(specification);
	}

	@Override
	public List<E> findAll() {
		return dao.findAll();
	}

	@Override
	public Optional<E> findOneNonDeleted(int id) {
		return dao.findByIdAndDeleted(id, false);
	}
	
	@Override
	public Optional<E> findOneDeleted(int id) {
		return dao.findByIdAndDeleted(id, true);
	}
}

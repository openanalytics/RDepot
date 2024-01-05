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

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import eu.openanalytics.rdepot.base.daos.Dao;
import eu.openanalytics.rdepot.base.entities.Resource;

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
	public Optional<E> findById(Integer id) {
		return dao.findById(id);
	}
	
	@Override
	public Optional<E> findOneByParameters(E entity) {
		return dao.findOne(Example.of(entity, ExampleMatcher.matching().withIgnoreNullValues()));
	}

	@Override
	public Page<E> findByParameters(Pageable pageable, E entity) {
		return dao.findAll(
				Example.of(entity, ExampleMatcher.matching().withIgnoreNullValues()), 
				pageable);
	}
	
	@Override
	public Page<E> findAllByExample(Pageable pageable, Example<E> example) {
		return dao.findAll(example, pageable);
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
	
	public List<E> findSortedBySpecification(Specification<E> specification, Sort sort) {
		return dao.findAll(specification, sort);
	}

	@Override
	public Page<E> findByDeleted(Pageable pageable, boolean deleted) {
		return dao.findByDeleted(deleted, pageable);
	}
	
	public List<E> findByDeleted(boolean deleted) {
		return dao.findByDeleted(deleted);
	}

	@Override
	public List<E> findAll() {
		return dao.findAll();
	}
	
	@Override
	public List<E> findAllSorted(Sort sort) {
		return dao.findAll(sort);
	}
	
	@Override
	public List<E> findAllByExample(E entity) {
		return dao.findAll(Example.of(entity));
	}
	
	@Override
	public Optional<E> findOneNonDeleted(Integer id) {
		return dao.findByIdAndDeleted(id, false);
	}
	
	@Override
	public Optional<E> findOneDeleted(Integer id) {
		return dao.findByIdAndDeleted(id, true);
	}

	@Override
	public List<E> findAllDeleted() {
		return dao.findByDeleted(true);
	}
}

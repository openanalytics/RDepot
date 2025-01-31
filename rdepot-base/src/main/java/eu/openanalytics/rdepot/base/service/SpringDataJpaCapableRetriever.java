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
package eu.openanalytics.rdepot.base.service;

import eu.openanalytics.rdepot.base.api.v2.resolvers.DtoResolvedPageable;
import eu.openanalytics.rdepot.base.daos.Dao;
import eu.openanalytics.rdepot.base.entities.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

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
    protected SpringDataJpaCapableRetriever(Dao<E> dao) {
        this.dao = dao;
    }

    @Override
    public Optional<E> findById(int id) {
        return dao.findById(id);
    }

    @Override
    public Page<E> findAll(Pageable pageable) {
        // keep the original pageable, to preserve original HATEOAS links,
        // in case user did not specify ID in their request
        final Pageable originalPageable = pageable;
        //
        pageable = fixPageable(pageable);
        final Page<E> found = dao.findAll(pageable);
        return new PageWithAnotherSort<>(found, originalPageable);
    }

    /**
     * By default, if two or more elements share a property
     * and are not sorted by any value which would make them unique against each other,
     * their order will be random.
     * This may lead to unexpected, non-reproducible results when using pagination. <br/>
     * For example, if submissions are sorted by the "state" property only,
     * then the order of all "waiting" submissions will be random for each request.
     * If we request page 1, and page 2 after that,
     * it may happen that duplicated values occur
     * since the random sorting algorithm may put the same element first on the first,
     * then on the second page.
     * To mitigate that, in case the user does not explicitly request sorting by ID,
     * it is implied that two similar elements should be sorted by ID in the ascending order
     * if all other sorting conditions are exhausted.
     *
     * @return a copy of pageable if there was no ID present, the same object reference otherwise
     */
    protected Pageable fixPageable(Pageable pageable) {
        if (pageable.getSort().getOrderFor("id") == null) {
            final List<Sort.Order> originalOrderList =
                    pageable.getSort().stream().toList();
            final List<Sort.Order> enhancedOrderList = new ArrayList<>(originalOrderList);
            enhancedOrderList.add(Sort.Order.asc("id"));
            return new DtoResolvedPageable(pageable, Sort.by(enhancedOrderList));
        }
        return pageable;
    }

    @Override
    public Page<E> findAllBySpecification(Specification<E> specification, Pageable pageable) {
        // keep the original pageable, to preserve original HATEOAS links,
        // in case user did not specify ID in their request
        final Pageable originalPageable = pageable;
        //
        pageable = fixPageable(pageable);

        final Page<E> found = dao.findAll(specification, pageable);
        return new PageWithAnotherSort<>(found, originalPageable);
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

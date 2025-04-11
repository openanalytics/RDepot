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

import eu.openanalytics.rdepot.base.daos.RepositoryDao;
import eu.openanalytics.rdepot.base.entities.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class RepositoryService<E extends Repository> extends eu.openanalytics.rdepot.base.service.Service<E> {

    private final RepositoryDao<E> repositoryDao;

    @PersistenceContext
    private EntityManager entityManager;

    public RepositoryService(RepositoryDao<E> repositoryDao) {
        super(repositoryDao);
        this.repositoryDao = repositoryDao;
    }

    public Optional<E> findByName(String name) {
        return repositoryDao.findByName(name);
    }

    public Optional<E> findByNameAndDeleted(String name, boolean deleted) {
        return repositoryDao.findByNameAndDeleted(name, deleted);
    }

    @Override
    public void delete(E entity) {
        repositoryDao.delete(entity);
    }

    public Optional<E> findByPublicationUri(String publicationUri) {
        return repositoryDao.findByPublicationUri(publicationUri);
    }

    public Optional<E> findByServerAddress(String serverAddress) {
        return repositoryDao.findByServerAddress(serverAddress);
    }

    public Optional<E> findByServerAddressStartsWith(String serverAddress) {
        return repositoryDao.findByServerAddressStartsWith(serverAddress);
    }

    @Transactional
    public void incrementVersion(E repository) {
        final String repoName = repository.getName();
        final int updated = repositoryDao.incrementRepositoryVersion(repoName);
        entityManager.refresh(entityManager.merge(repository));
        if (updated < 1) {
            throw new IllegalStateException(
                    String.format("No repository version has been updated. Repository name: %s", repoName));
        } else if (updated > 1) {
            throw new IllegalStateException(
                    String.format("Too many repositories have been updated. Repository name: %s", repoName));
        }
    }
}

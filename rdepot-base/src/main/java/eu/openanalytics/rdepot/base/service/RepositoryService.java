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

import eu.openanalytics.rdepot.base.daos.RepositoryDao;
import eu.openanalytics.rdepot.base.entities.Repository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class RepositoryService<E extends Repository> extends eu.openanalytics.rdepot.base.service.Service<E> {

    private final RepositoryDao<E> repositoryDao;

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

    @Transactional
    public void incrementVersion(E repository) {
        Optional<E> currentRepositoryOpt = Optional.empty();
        int attempts = 0;
        while (attempts < 3) {
            currentRepositoryOpt = repositoryDao.findByNameAcquirePessimisticWriteLock(repository.getName());

            if (currentRepositoryOpt.isEmpty()) {
                log.warn("Could not acquire lock on repository " + repository.getName() + "! Trying again...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Error while acquiring lock on repository.", e);
                }
                attempts++;
            } else {
                break;
            }
        }
        E currentRepository = currentRepositoryOpt.orElseThrow(
                () -> new IllegalStateException("Could not acquire lock on repository " + repository.getName()));
        log.debug("Incrementing version from " + currentRepository.getVersion());
        currentRepository.setVersion(currentRepository.getVersion() + 1);
        repositoryDao.saveAndFlush(currentRepository);
    }
}

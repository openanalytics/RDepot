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

import eu.openanalytics.rdepot.base.daos.RepositoryMaintainerDao;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class RepositoryMaintainerService extends Service<RepositoryMaintainer> {

    private final RepositoryMaintainerDao repositoryMaintainerDao;

    public RepositoryMaintainerService(RepositoryMaintainerDao repositoryMaintainerDao) {
        super(repositoryMaintainerDao);
        this.repositoryMaintainerDao = repositoryMaintainerDao;
    }

    public List<RepositoryMaintainer> findByRepository(Repository repository) {
        return repositoryMaintainerDao.findByRepository(repository);
    }

    public Optional<RepositoryMaintainer> findByRepositoryAndUserAndDeleted(
            Repository repository, User user, boolean deleted) {
        return repositoryMaintainerDao.findByRepositoryAndUserAndDeleted(repository, user, deleted);
    }

    public boolean existsByRepositoryAndUserAndDeleted(Repository repository, User user, boolean deleted) {
        return repositoryMaintainerDao.existsByRepositoryAndUserAndDeleted(repository, user, deleted);
    }

    public List<RepositoryMaintainer> findByUserWithoutDeleted(User user) {
        return repositoryMaintainerDao.findByUserAndDeleted(user, false);
    }

    public List<RepositoryMaintainer> findByUserAndRepository(User user, Repository repository) {
        return repositoryMaintainerDao.findByUserAndRepository(user, repository);
    }

    public List<RepositoryMaintainer> findByRepositoryNonDeleted(Repository repository) {
        return repositoryMaintainerDao.findByRepositoryAndDeleted(repository, false);
    }
}

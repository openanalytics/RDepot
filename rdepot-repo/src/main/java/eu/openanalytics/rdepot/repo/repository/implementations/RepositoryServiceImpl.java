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
package eu.openanalytics.rdepot.repo.repository.implementations;

import eu.openanalytics.rdepot.repo.exception.StorageException;
import eu.openanalytics.rdepot.repo.model.Technology;
import eu.openanalytics.rdepot.repo.repository.Repository;
import eu.openanalytics.rdepot.repo.repository.RepositoryService;
import eu.openanalytics.rdepot.repo.storage.StorageService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link RepositoryService}.
 * It maintains list of repositories in memory.
 * In case a repository is not indexed, a search is performed in storage.
 * If found, repository is indexed so that the same repository object
 * can efficiently be returned the next time.
 */
@Component
@Slf4j
public class RepositoryServiceImpl implements RepositoryService {

    private final Set<StorageService<?>> storageServices;
    private final Map<String, Repository> repositories = new HashMap<>();

    public RepositoryServiceImpl(Set<StorageService<?>> storageServices) {
        this.storageServices = storageServices;
    }

    @Override
    public Optional<Repository> findByNameOrCreate(String name, Technology technology) {
        final Optional<StorageService<?>> svcOpt = storageServices.stream()
                .filter(s -> s.getTechnology().equals(technology))
                .findAny();
        if (svcOpt.isEmpty()) return Optional.empty();
        final StorageService<?> svc = svcOpt.get();

        synchronized (repositories) {
            if (!svc.getAllRepositoryDirectories().contains(name)) {
                try {
                    svc.createRepoDirectory(name);
                } catch (StorageException e) {
                    log.error(e.getMessage(), e);
                    return Optional.empty();
                }
            }
            if (!repositories.containsKey(name)) {
                final Repository repoToIndex = new Repository(technology, name);
                repositories.put(name, repoToIndex);
                return Optional.of(repoToIndex);
            } else if (!repositories.get(name).getTechnology().equals(technology)) {
                return Optional.empty();
            }
        }

        return Optional.of(repositories.get(name));
    }
}

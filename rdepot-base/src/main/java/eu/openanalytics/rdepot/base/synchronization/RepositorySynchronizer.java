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
package eu.openanalytics.rdepot.base.synchronization;

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.technology.Technology;
import eu.openanalytics.rdepot.base.time.DateProvider;
import java.time.Instant;
import java.util.Arrays;
import lombok.NonNull;
import org.springframework.transaction.annotation.Transactional;

/**
 * Synchronizer publishes repository on a server
 * so that it is accessible outside the RDepot context.
 * Every technology and its package manager require different
 * publication flow and server directory structure
 * so this interface should be implemented by each extension module.
 * @param <T>
 */
public abstract class RepositorySynchronizer<T extends Repository> {
    /**
     * Stores repository on a public server.
     * @param repository to publish
     */
    @Transactional
    public void storeRepositoryOnRemoteServer(T repository) throws SynchronizeRepositoryException {
        final Instant timestamp = DateProvider.now();
        repository.setLastPublicationTimestamp(timestamp);

        try {
            storeRepositoryOnRemoteServer(repository, DateProvider.instantToDatestampWithoutHyphens(timestamp));
            repository.setLastPublicationSuccessful(true);
        } catch (SynchronizeRepositoryException e) {
            repository.setLastPublicationSuccessful(false);
            throw e;
        }
    }

    protected String attachTechnologyIfNeeded(
            @NonNull String serverAndPort, @NonNull String repositoryDirectory, Technology technology) {
        if (repositoryDirectory.startsWith("/"))
            throw new IllegalArgumentException("Repository path must not start with '/'");

        final String[] pathComponents = repositoryDirectory.split("/");
        final String expectedTechnology = technology.getName().toLowerCase();
        final StringBuilder enrichedPath = new StringBuilder();
        enrichedPath.append(serverAndPort);
        if (pathComponents.length == 1) {
            enrichedPath.append('/').append(expectedTechnology).append('/').append(pathComponents[0]);
        } else if (pathComponents.length > 1) {
            if (pathComponents[pathComponents.length - 2].equals(expectedTechnology)) {
                enrichedPath.append('/').append(repositoryDirectory);
            } else {
                final String firstPart = String.join(
                        "/",
                        Arrays.stream(pathComponents, 0, pathComponents.length - 1)
                                .toArray(String[]::new));
                final String lastPart = pathComponents[pathComponents.length - 1];
                enrichedPath
                        .append('/')
                        .append(firstPart)
                        .append('/')
                        .append(expectedTechnology)
                        .append('/')
                        .append(lastPart);
            }
        }
        enrichedPath.append("/");
        return enrichedPath.toString();
    }

    protected abstract void storeRepositoryOnRemoteServer(T repository, String datestamp)
            throws SynchronizeRepositoryException;
}

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
package eu.openanalytics.rdepot.base.synchronization;

import eu.openanalytics.rdepot.base.entities.Repository;

/**
 * Synchronizer publishes repository on a server
 * so that it is accessible outside the RDepot context.
 * Every technology and its package manager require different
 * publication flow and server directory structure
 * so this interface should be implemented by each extension module.
 * @param <T>
 */
public interface RepositorySynchronizer<T extends Repository> {
    /**
     * Stores repository on a public server.
     * @param repository to publish
     * @param dateStamp in the format "yyyyMMdd"
     */
    void storeRepositoryOnRemoteServer(T repository, String dateStamp) throws SynchronizeRepositoryException;
}

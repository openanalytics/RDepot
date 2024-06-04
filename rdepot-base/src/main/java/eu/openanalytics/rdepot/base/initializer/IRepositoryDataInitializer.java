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
package eu.openanalytics.rdepot.base.initializer;

/**
 * Initializes {@link eu.openanalytics.rdepot.base.entities.Repository Repositories}
 * based on provided configuration files.
 * Each extension should provide its own implementation of this interface.
 * It will be called by {@link CommonRepositoryDataInitializer}.
 */
public interface IRepositoryDataInitializer {

    /**
     * Creates repositories from provided configuration files (e.g. YAML),
     * usually at the start of application.<br/>
     * If declarative mode is enabled, it means that repositories themselves
     * (not their content) are read-only and the administrator is not able to
     * e.g. create one other than by editing config files.
     * Therefore, in such a case the implementation should make sure
     * that all other repositories that happened to be created in a different way,
     * are removed before the application is ready.
     * @param declarative true if declarative mode is enabled
     */
    void createRepositoriesFromConfig(boolean declarative);
}

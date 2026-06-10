/*
 * RDepot
 *
 * Copyright (C) 2012-2026 Open Analytics NV
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
package eu.openanalytics.rdepot.r.storage.indexes;

/**
 * Represents a generated R <code>index.html</code>
 * @param indexOnRemoteRepoPath destination location of the index in the remote repository
 *                              (e.g. <code>src/contrib/Archive</code>,
 *                              <code>src/contrib/Archive/abc</code>)
 * @param indexLocalPath actual location in the local storage
 * @param archive whether the index is meant for the Archive or not
 *                (in practice every index apart from the main one
 *                from the head location of the repository)
 */
public record RIndexDescriptor(String indexOnRemoteRepoPath, String indexLocalPath, boolean archive, String checksum) {}

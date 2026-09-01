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
package eu.openanalytics.rdepot.r.synchronization.partitioning.structs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.core.io.FileSystemResource;

public class RequestBodyChunk {
    final Set<FileSystemResource> files = new HashSet<>();
    final Set<FileSystemResource> filesArchive = new HashSet<>();

    /**
     * Indicates which specific binary directory packages will be located in, e.g.
     * <code>binlinuxjammyx866444_arrow_18.1.0.tar.gz=bin/linux/jammy/x86_64/4.4</code>
     */
    final Map<String, String> paths = new HashMap<>();

    /**
     * Indicates which specific Archive binary directory packages will be located in, e.g.
     * <code>binlinuxjammyx866444_arrow_18.1.0.tar.gz=bin/linux/jammy/x86_64/4.4</code>
     */
    final Map<String, String> pathsArchive = new HashMap<>();

    /**
     * Checksums for uploaded packages, e.g.:
     * <code>srccontrib_accrued_1.3.tar.gz=a05e4ca44438c0d9e7d713d7e3890423</code>
     */
    final Map<String, String> checksums = new HashMap<>();

    public void addPath(String filename, String path) {
        paths.put(filename, path);
    }

    public void addPathInArchive(String filename, String path) {
        pathsArchive.put(filename, path);
    }

    public void addChecksum(String filename, String checksum) {
        checksums.put(filename, checksum);
    }

    public void addFile(FileSystemResource file) {
        this.files.add(file);
    }

    public void addFileArchive(FileSystemResource file) {
        this.filesArchive.add(file);
    }
}

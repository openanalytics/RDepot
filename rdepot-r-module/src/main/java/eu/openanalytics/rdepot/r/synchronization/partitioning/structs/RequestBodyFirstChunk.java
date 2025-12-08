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
package eu.openanalytics.rdepot.r.synchronization.partitioning.structs;

import java.util.*;
import lombok.Getter;

@Getter
public class RequestBodyFirstChunk extends RequestBodyChunk {
    final List<String> toDelete = new LinkedList<>();
    final List<String> toDeleteArchive = new LinkedList<>();
    final Map<String, String> toDeletePaths = new LinkedHashMap<>();
    final Map<String, String> toDeletePathsArchive = new LinkedHashMap<>();

    public void addSourceFilesToDelete(Collection<String> fileNames) {
        toDelete.addAll(fileNames);
    }

    public void addSourceFilesToDeleteFromArchive(Collection<String> fileNames) {
        toDeleteArchive.addAll(fileNames);
    }

    private void addBinaryFileToDelete(String path, String fileName, Map<String, String> toDeletePaths) {
        toDeletePaths.put(fileName, path);
    }

    public void addBinaryFileToDelete(String path, String fileName) {
        addBinaryFileToDelete(path, fileName, toDeletePaths);
    }

    public void addBinaryFileToDeleteFromArchive(String path, String fileName) {
        addBinaryFileToDelete(path, fileName, toDeletePathsArchive);
    }
}

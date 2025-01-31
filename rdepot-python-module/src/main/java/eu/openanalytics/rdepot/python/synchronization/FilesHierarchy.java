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
package eu.openanalytics.rdepot.python.synchronization;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilesHierarchy {

    private static final String SEPARATOR = FileSystems.getDefault().getSeparator();

    public static boolean isRepositoryDir(File file) {
        File repositoryIndex =
                new File(file.getParentFile().getParentFile().getAbsolutePath() + SEPARATOR + "index.html");
        return !repositoryIndex.exists();
    }

    public static Path getArchiveDirectory(File file, int chunkNo) {
        File archiveDirectory;
        if (!isRepositoryDir(file)) {
            archiveDirectory = new File(
                    file.getParentFile().getParentFile().getParentFile().getAbsolutePath() + SEPARATOR + chunkNo);
        } else {
            archiveDirectory = new File(file.getParentFile().getParentFile().getAbsolutePath() + SEPARATOR + chunkNo);
        }

        if (!archiveDirectory.exists() && !archiveDirectory.mkdir()) {
            log.error("Could not create archive directory: {}", archiveDirectory);
        }
        return archiveDirectory.toPath();
    }

    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        boolean result = true;
        if (allContents != null) {
            for (File file : allContents) {
                result = result && deleteDirectory(file);
            }
        }
        return result && directoryToBeDeleted.delete();
    }
}

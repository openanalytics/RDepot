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
package eu.openanalytics.rdepot.python.synchronization;

import java.io.File;
import java.nio.file.FileSystems;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FilesHierarchy {
    private static final String separator = FileSystems.getDefault().getSeparator();

    public static boolean isRepositoryDir(File file) {
        File repositoryIndex =
                new File(file.getParentFile().getParentFile().getAbsolutePath() + separator + "index.html");
        if (repositoryIndex.exists()) {
            return false;
        }
        return true;
    }

    public static String getArchiveDirectory(File file, int chunkNo) {
        File archiveDirectory;
        if (!isRepositoryDir(file)) {
            archiveDirectory = new File(
                    file.getParentFile().getParentFile().getParentFile().getAbsolutePath() + separator + chunkNo);
        } else {
            archiveDirectory = new File(file.getParentFile().getParentFile().getAbsolutePath() + separator + chunkNo);
        }

        if (!archiveDirectory.exists()) {
            archiveDirectory.mkdir();
        }
        ;
        return archiveDirectory.getAbsolutePath();
    }

    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}

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
package eu.openanalytics.rdepot.python.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.springframework.core.io.FileSystemResource;

public interface PythonPackageArchiver {
    /**
     * Archives the given list of files into a compressed tar.gz archive and stores it in the specified directory.
     * <p>
     * This method creates a compressed .tar.gz archive from the provided list of {@code files} and saves it in the specified {@code archiveDir}.
     * The archive is named after the package, which is determined by the parent directory of the first file. However, if the file is part of a repository directory,
     * the archive will be named "index.tar.gz" (as both packages and repositories contain an index.html file).
     * </p>
     * <p>
     * The process involves:
     * <ol>
     *   <li>Creating a tar archive entry for each file in the list.</li>
     *   <li>Compressing the tar archive using GZIP compression.</li>
     *   <li>Writing the tar.gz file to the specified archive directory.</li>
     * </ol>
     * </p>
     *
     * @param files the list of files to be archived
     * @param archiveDir the directory where the tar.gz archive will be stored
     * @return a {@code FileSystemResource} representing the created tar.gz archive, or {@code null} if the file list is empty
     * @throws IOException if an I/O error occurs during the archiving process
     */
    FileSystemResource tarPackages(List<File> files, Path archiveDir) throws IOException;
}

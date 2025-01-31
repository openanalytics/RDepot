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
package eu.openanalytics.rdepot.python.storage.implementations;

import eu.openanalytics.rdepot.python.storage.PythonPackageArchiver;
import eu.openanalytics.rdepot.python.synchronization.FilesHierarchy;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
public class PythonPackageLocalArchiver implements PythonPackageArchiver {

    protected final String separator = FileSystems.getDefault().getSeparator();

    @Override
    public FileSystemResource tarPackages(List<File> files, Path archiveDir) throws IOException {
        if (files.isEmpty()) {
            return null;
        }
        String archiveName = files.get(0).getParentFile().getName();
        if (FilesHierarchy.isRepositoryDir(files.get(0))) {
            archiveName = "index";
        }

        final Path archivePath = archiveDir.resolve(archiveName + ".tar.gz");
        try (final OutputStream packagesArchive = Files.newOutputStream(archivePath);
                final BufferedOutputStream buffOut = new BufferedOutputStream(packagesArchive);
                final GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(buffOut);
                final TarArchiveOutputStream tarOut = new TarArchiveOutputStream(gzOut)) {
            for (File file : files) {
                final TarArchiveEntry tarEntry = new TarArchiveEntry(file, file.getName());
                tarOut.putArchiveEntry(tarEntry);
                Files.copy(file.toPath(), tarOut);
                tarOut.closeArchiveEntry();
            }
            tarOut.finish();
        }
        return new FileSystemResource(archivePath);
    }
}

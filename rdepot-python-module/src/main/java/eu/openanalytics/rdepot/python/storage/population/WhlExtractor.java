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
package eu.openanalytics.rdepot.python.storage.population;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.IOUtils;

/**
 * Utility class for extracting .whl Python archives
 */
@Slf4j
public class WhlExtractor {
    private static boolean isValid(final File storedFile) {
        return Objects.nonNull(storedFile)
                && storedFile.exists()
                && Objects.nonNull(storedFile.getParentFile())
                && !storedFile.isDirectory();
    }

    public static List<File> unpackWhl(final File storedFile) throws IOException, ArchiveException {
        if (!isValid(storedFile)) {
            log.error("Invalid WHL archive to unpack: {}", storedFile);
            throw new ArchiveException("Invalid WHL archive to unpack.");
        }
        log.debug("Extracting package file: {}", storedFile.getAbsolutePath());

        final File outputDir = storedFile.getParentFile();
        final Set<Path> beforeUnpacking;
        try (final ZipInputStream zis = new ZipInputStream(new FileInputStream(storedFile));
                Stream<Path> beforeUnpackingStream = Files.list(outputDir.toPath())) {
            beforeUnpacking = beforeUnpackingStream.collect(Collectors.toSet());
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                final File outputFile = new File(outputDir, entry.getName());
                final File outputFileParentDir = outputFile.getParentFile();

                if (!outputFileParentDir.exists() && !outputFileParentDir.mkdirs()) {
                    throw new ArchiveException("Could not create directory: " + outputFileParentDir.getAbsolutePath());
                }

                if (entry.isDirectory()) {
                    if (!outputFile.exists() && !outputFile.mkdirs()) {
                        throw new ArchiveException("Could not create directory: " + outputFile.getAbsolutePath());
                    }
                } else {
                    try (OutputStream outputFileStream = new FileOutputStream(outputFile)) {
                        IOUtils.copy(zis, outputFileStream);
                    }
                }
            }
        }

        try (Stream<Path> paths = Files.list(outputDir.toPath())) {
            return paths.filter(p -> !beforeUnpacking.contains(p))
                    .map(Path::toFile)
                    .toList();
        }
    }
}

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
package eu.openanalytics.rdepot.base.storage;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.storage.exceptions.*;
import java.io.File;
import java.io.IOException;

/**
 * Allows to access storage to save persistent binary data like files.
 */
public interface Storage<P extends Package> {

    /**
     * Extracts the package
     */
    String extractTarGzPackageFile(String storedFile) throws ExtractFileException;

    /**
     * Removes package source from persistent storage.
     */
    void removePackageSource(String path) throws SourceFileDeleteException;

    /**
     * Removes file from persistent storage.
     * Can be used in case of failure during package creation.
     */
    void removeFileIfExists(String path) throws DeleteFileException;

    /**
     * Moves package source to a new location.
     */
    String moveSource(P packageBag, String newSource) throws MovePackageSourceException;

    File move(File source, File destination) throws MoveFileException;

    void deleteFile(File file) throws DeleteFileException;

    File linkTwoFolders(String targetPath, String linkPath) throws LinkFoldersException;
    /**
     * Reads package from storage.
     */
    byte[] getPackageInBytes(P packageBag) throws SourceNotFoundException;

    String calculateMd5Sum(String targetPath) throws Md5SumCalculationException;
    /**
     * Calculates and assigns a checksum to the package.
     */
    void setCheckSum(P packageBag) throws CheckSumCalculationException;

    boolean exists(String path);

    void appendText(String content, String path) throws IOException;

    void removeContentFromEnd(String content, String path) throws IOException;

    File createFolderStructure(String path) throws CreateFolderStructureException;

    void gzipFile(final String source) throws GzipFileException;
}

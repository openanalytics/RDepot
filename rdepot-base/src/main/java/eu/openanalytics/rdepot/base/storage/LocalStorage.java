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
package eu.openanalytics.rdepot.base.storage;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.storage.exceptions.*;
import java.io.File;
import org.springframework.web.multipart.MultipartFile;

/**
 * Allows to access storage to save temporary binary data like files.
 * It is used to process files locally
 * before putting them in {@link PersistentStorage persistent storage}.
 * As a rule of thumb, if losing data is not going to be critical
 * for the integrity of storage (i.e. they can be considered temporary)
 * then this storage should be used.
 * Otherwise, use {@link PersistentStorage persistent storage}
 */
public interface LocalStorage<P extends Package> {

    /**
     * Extracts the package
     * @return the directory containing extracted files
     */
    String extractTarGzPackageFile(File file) throws ExtractFileException;

    /**
     * Removes file from persistent localStorage.
     * Can be used in case of failure during package creation.
     */
    void removeFileIfExists(String path) throws DeleteFileException;

    void deleteFile(String file) throws DeleteFileException;

    String linkTwoFolders(String targetPath, String linkPath) throws LinkFoldersException;

    String calculateMd5Sum(String targetPath) throws Md5SumCalculationException;
    /**
     * Calculates and assigns a checksum to the package.
     */
    void setCheckSum(P packageBag, File locallyStored) throws CheckSumCalculationException;

    boolean exists(String path);

    void appendText(String content, String path) throws ContentEditException;

    void removeContentFromEnd(String content, String path) throws ContentEditException;

    String createFolderStructure(String path) throws CreateFolderStructureException;

    void gzipFile(final String source) throws GzipFileException;

    void removeEmptyLinesFromEnd(String path) throws ContentEditException;

    String downloadFile(String url) throws DownloadFileException;

    void cleanDirectory(String directory) throws DeleteFileException;

    default String getSeparator() {
        return "/";
    }

    File createTemporaryFolder(String prefix) throws CreateTemporaryFolderException;

    MultipartFile downloadFile(String url, File destination) throws DownloadFileException;
}

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
package eu.openanalytics.rdepot.base.storage.implementations;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.storage.PersistentStorage;
import eu.openanalytics.rdepot.base.storage.exceptions.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

@Slf4j
public abstract class CommonFSPersistentStorage<P extends Package, R extends Repository>
        implements PersistentStorage<P, R> {

    private final File packageUploadDirectory;
    private static final String separator = FileSystems.getDefault().getSeparator();
    protected static final Random random = new Random();

    protected CommonFSPersistentStorage(File packageUploadDirectory) {
        this.packageUploadDirectory = packageUploadDirectory;
    }

    @Override
    public Path storeNewPackage(File packageFile, List<File> extractedDirs, R repository) throws StoreFileException {
        try {
            final File waitingRoom = generateWaitingRoom(packageUploadDirectory, repository);
            final File targetPackageFile = new File(waitingRoom.getAbsolutePath() + separator + packageFile.getName());
            Files.copy(packageFile.toPath(), targetPackageFile.toPath());
            for (File extractedDir : extractedDirs) {
                final File targetExtractedDir =
                        new File(waitingRoom.getAbsolutePath() + separator + extractedDir.getName());
                FileUtils.copyDirectory(extractedDir, targetExtractedDir);
            }

            return targetPackageFile.toPath();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new StoreFileException(packageFile);
        }
    }

    protected File generateWaitingRoom(File packageUploadDirectory, R repository) throws IOException {
        File waitingRoom = new File(
                packageUploadDirectory.getAbsolutePath() + separator + "new" + separator + random.nextInt(100000000));

        while (waitingRoom.exists()) {
            waitingRoom = new File(packageUploadDirectory.getAbsolutePath()
                    + separator + "new" + separator + repository.getId()
                    + random.nextInt(100000000));
        }

        FileUtils.forceMkdir(waitingRoom);
        return waitingRoom;
    }

    protected File generateDirWithRandomizedName(String name, int repositoryId) {
        File tempDir = new File(packageUploadDirectory.getAbsolutePath()
                + separator
                + name
                + separator
                + repositoryId
                + separator
                + random.nextInt(100000000));

        while (tempDir.exists()) {
            tempDir = new File(packageUploadDirectory.getAbsolutePath()
                    + separator
                    + name
                    + separator
                    + repositoryId
                    + separator
                    + random.nextInt(100000000));
        }
        return tempDir;
    }

    @Override
    public Path movePackageToTrash(P packageBag) throws StoreFileException {
        final File trashDir = generateDirWithRandomizedName(
                "trash", packageBag.getRepository().getId());
        final File packageFile = new File(packageBag.getSource());
        final File packageDir = packageFile.getParentFile();

        if (!packageFile.exists() || Objects.isNull(packageDir) || !packageDir.exists()) {
            log.error("Invalid package source: {}", packageFile.getAbsolutePath());
            throw new StoreFileException(packageFile);
        }

        try {
            FileUtils.moveDirectory(packageDir, trashDir);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new StoreFileException(packageDir);
        }

        return trashDir.toPath().resolve(packageFile.getName());
    }

    @Override
    public Path movePackageToAccepted(P packageBag) throws StoreFileException {
        log.debug("Moving package to the main directory...");
        final Repository repository = packageBag.getRepository();
        final File mainDir = generateDirWithRandomizedName("repositories", repository.getId());

        final File current = new File(packageBag.getSource());
        if (!current.exists()) {
            log.error("Source [{}] for package {} does not exist.", packageBag.getSource(), packageBag);
            throw new StoreFileException(current);
        }

        try {
            FileUtils.moveDirectory(current.getParentFile(), mainDir);
        } catch (IOException e) {
            if (mainDir.exists()) {
                try {
                    FileUtils.forceDelete(mainDir);
                } catch (IOException dfe) {
                    log.error(dfe.getMessage(), dfe);
                }
            }
            log.error(e.getMessage(), e);
            throw new StoreFileException(mainDir);
        }

        final String packageFilename = current.getName();
        log.debug(
                "Package moved to the following location: {}{}{}",
                mainDir.getAbsolutePath(),
                separator,
                packageFilename);
        return new File(mainDir.getAbsolutePath() + separator + packageFilename).toPath();
    }

    @Override
    public void deleteAllPackageFilesIfExist(P packageBag) throws DeleteFileException {
        deleteFromStorageIfExists(new File(packageBag.getSource()).getParentFile());
    }

    private void deleteFromStorageIfExists(File toDelete) throws DeleteFileException {
        try {
            FileUtils.forceDelete(toDelete);
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new DeleteFileException();
        }
    }

    @Override
    public void deleteFromStorageIfExists(Path path) throws DeleteFileException {
        deleteFromStorageIfExists(path.toFile());
    }

    @Override
    public byte[] getPackageInBytes(P packageBag) throws DownloadFileException {
        return readFile(Path.of(packageBag.getSource()));
    }

    @Override
    public byte[] readFile(Path file) throws DownloadFileException {
        try {
            return FileUtils.readFileToByteArray(downloadFile(file));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new DownloadFileException(file.toAbsolutePath().toString());
        }
    }

    @Override
    public File downloadFile(Path path) throws DownloadFileException {
        final File file = path.toFile();
        if (!file.exists() || file.isDirectory()) {
            log.error("Requested file not found in storage (or is a directory): {}", file.getAbsolutePath());
            throw new DownloadFileException(path.toAbsolutePath().toString());
        }
        // It does not really have to be "downloaded" if it's already in the file system
        return file;
    }

    @Override
    public void recycleDownloadedFile(File file) {
        // Since "downloading" does not really "download" anything
        // then "removing" should not "remove" the file either
    }
}

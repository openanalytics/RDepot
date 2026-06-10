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
import eu.openanalytics.rdepot.base.storage.PopulatedPackage;
import eu.openanalytics.rdepot.base.storage.Populator;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.storage.exceptions.*;
import eu.openanalytics.rdepot.base.synchronization.checksums.Checksums;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
public abstract class LocalFSPopulator<R extends Repository, P extends Package, PP extends Package & PopulatedPackage>
        implements Populator<R, P> {

    private final File packageUploadDirectory;
    private final File repositoryGenerationDirectory;

    protected final Storage<P> storage;
    private static final Random random = new Random();
    protected final String separator = FileSystems.getDefault().getSeparator();

    /**
     * Filter out packages that are already uploaded to the remote server.
     * @param remotePackages list of remote package names and versions
     * @param localPackages packages that are stored locally, to be published
     * @return list of {@link File} objects to upload to the remote server.
     */
    protected abstract List<File> selectPackagesToUpload(
            List<String> remotePackages,
            Checksums remoteChecksums,
            List<PP> localPackages,
            Map<String, String> localChecksums,
            boolean archive);

    protected File linkCurrentFolderToGeneratedFolder(Repository repository, String dateStamp)
            throws LinkFoldersException {
        return storage.linkTwoFolders(
                repositoryGenerationDirectory.getAbsolutePath()
                        + separator
                        + repository.getId()
                        + separator
                        + dateStamp,
                repositoryGenerationDirectory.getAbsolutePath() + separator + repository.getId() + separator
                        + "current");
    }

    @Override
    public String writeToWaitingRoom(MultipartFile fileData, R repository) throws WriteToWaitingRoomException {
        try {
            final File waitingRoom = generateWaitingRoom(packageUploadDirectory, repository);
            final File file = new File(waitingRoom.getAbsolutePath() + separator + fileData.getOriginalFilename());

            fileData.transferTo(file);

            return file.getAbsolutePath();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new WriteToWaitingRoomException();
        }
    }

    protected File generateWaitingRoom(final File packageUploadDirectory, final Repository repository)
            throws IOException {
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

    @Override
    public String moveToMainDirectory(P packageBag) throws InvalidSourceException, MovePackageSourceException {
        log.debug("Moving package to the main directory...");
        final Repository repository = packageBag.getRepository();
        File mainDir = new File(packageUploadDirectory.getAbsolutePath() + separator + "repositories" + separator
                + repository.getId() + separator + (random.nextInt(100000000)));

        while (mainDir.exists())
            mainDir = new File(packageUploadDirectory.getAbsolutePath() + separator + "repositories" + separator
                    + repository.getId() + separator + (random.nextInt(100000000)));

        final File current = new File(packageBag.getSource());
        if (!current.exists()) {
            log.error("Source [{}] for package {} does not exist.", packageBag.getSource(), packageBag);
            throw new InvalidSourceException();
        }

        File newDirectory;
        try {
            newDirectory = storage.move(current.getParentFile(), mainDir);
        } catch (MoveFileException e) {
            if (mainDir.exists()) {
                try {
                    storage.deleteFile(mainDir);
                } catch (DeleteFileException dfe) {
                    log.error(dfe.getMessage(), dfe);
                }
            }
            log.error(e.getMessage(), e);
            throw new MovePackageSourceException();
        }

        final String packageFilename = current.getName();
        try {
            storage.deleteFile(current);
        } catch (DeleteFileException e) {
            log.error(e.getMessage(), e);
            throw new MovePackageSourceException();
        }
        log.debug(
                "Package moved to the following location: {}{}{}",
                newDirectory.getAbsolutePath(),
                separator,
                packageFilename);
        return new File(newDirectory.getAbsolutePath() + separator + packageFilename).getAbsolutePath();
    }

    @Override
    public String moveToTrashDirectory(P packageBag) throws MovePackageSourceException {
        File trashDir = new File(packageUploadDirectory.getAbsolutePath() + separator
                + "trash" + separator + packageBag.getRepository().getId()
                + separator + random.nextInt(100000000));

        while (trashDir.exists()) {
            trashDir = new File(packageUploadDirectory.getAbsolutePath() + separator
                    + "trash" + separator + packageBag.getRepository().getId()
                    + separator + random.nextInt(100000000));
        }

        return storage.moveSource(packageBag, trashDir.getAbsolutePath());
    }
}

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
package eu.openanalytics.rdepot.repo.r.storage.implementations;

import eu.openanalytics.rdepot.repo.exception.EmptyTrashException;
import eu.openanalytics.rdepot.repo.exception.MoveToTrashException;
import eu.openanalytics.rdepot.repo.exception.RestoreRepositoryException;
import eu.openanalytics.rdepot.repo.exception.StorageException;
import eu.openanalytics.rdepot.repo.r.archive.ArchiveIndex;
import eu.openanalytics.rdepot.repo.r.archive.ArchiveInfo;
import eu.openanalytics.rdepot.repo.r.model.SynchronizeCranRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.r.storage.CranStorageService;
import eu.openanalytics.rdepot.repo.storage.StorageProperties;
import eu.openanalytics.rdepot.repo.storage.implementations.FileSystemStorageService;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CranFileSystemStorageService extends FileSystemStorageService<SynchronizeCranRepositoryRequestBody>
        implements CranStorageService {

    Logger logger = LoggerFactory.getLogger(CranFileSystemStorageService.class);

    private static final String PACKAGES = "PACKAGES";
    private static final String PACKAGES_GZ = "PACKAGES.gz";
    private static final String SRC_FOLDER = "src";
    private static final String CONTRIB_FOLDER = "contrib";
    private static final String ARCHIVE_FOLDER = "Archive";
    private static final String ARCHIVE_RDS = "archive.rds";

    private final Set<String> excludedFiles = new HashSet<>(Arrays.asList(PACKAGES, PACKAGES_GZ));

    public CranFileSystemStorageService(StorageProperties properties) {
        super(properties);
    }

    private boolean noExcludedFiles(Path path) {
        return !excludedFiles.contains(path.getFileName().toString());
    }

    private boolean noExcludedFilesAndDirectories(Path path) {
        return noExcludedFiles(path) && !Files.isDirectory(path);
    }

    private boolean onlyReadableDirectories(Path path) {
        return Files.isReadable(path) && Files.isDirectory(path);
    }

    private boolean onlyReadableFiles(Path path) {
        return Files.isReadable(path) && !Files.isDirectory(path);
    }

    private ArchiveInfo getArchiveInfoFromPath(Path path) throws IOException {
        LocalDateTime modifiedTime = null;
        LocalDateTime accessTime = null;
        LocalDateTime createdTime = null;
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            modifiedTime = LocalDateTime.ofInstant(attr.lastModifiedTime().toInstant(), ZoneId.systemDefault());
            accessTime = LocalDateTime.ofInstant(attr.lastAccessTime().toInstant(), ZoneId.systemDefault());
            createdTime = LocalDateTime.ofInstant(attr.creationTime().toInstant(), ZoneId.systemDefault());
        } catch (Exception e) {
            logger.debug("Exception when querying for basic file attributes of path {}: {}", path, e.getMessage(), e);
        }
        return new ArchiveInfo(
                path.getParent().getFileName().toString() + "/"
                        + path.getFileName().toString(),
                Long.valueOf(Files.size(path)).intValue(),
                436,
                modifiedTime,
                createdTime,
                accessTime,
                1000,
                1000,
                null,
                null);
    }

    public void generateArchiveRds(String repository) throws IOException {
        Path latestLocation = ((repository != null) && (!repository.trim().isEmpty()))
                ? this.rootLocation.resolve(repository)
                : this.rootLocation;
        Path archiveLocation =
                latestLocation.resolve(SRC_FOLDER).resolve(CONTRIB_FOLDER).resolve(ARCHIVE_FOLDER);

        if (Files.notExists(archiveLocation) || !Files.isDirectory(archiveLocation)) {
            Path archiveRds = latestLocation
                    .resolve(SRC_FOLDER)
                    .resolve(CONTRIB_FOLDER)
                    .resolve("Meta")
                    .resolve(ARCHIVE_RDS);
            Files.deleteIfExists(archiveRds);
            return;
        }

        List<Path> directories = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(archiveLocation, this::onlyReadableDirectories)) {
            stream.forEach(directories::add);
        }
        Map<String, List<ArchiveInfo>> archives = new HashMap<>();
        for (Path directory : directories) {
            List<Path> packages = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, this::onlyReadableFiles)) {
                stream.forEach(packages::add);
            }
            if (packages.isEmpty()) {
                continue;
            }
            List<ArchiveInfo> infos = new ArrayList<>();
            for (Path packageFile : packages) {
                infos.add(getArchiveInfoFromPath(packageFile));
            }
            archives.put(directory.getFileName().toString(), infos);
        }

        if (archives.isEmpty()) {
            Path archiveRds = latestLocation
                    .resolve(SRC_FOLDER)
                    .resolve(CONTRIB_FOLDER)
                    .resolve("Meta")
                    .resolve(ARCHIVE_RDS);
            Files.deleteIfExists(archiveRds);
            return;
        }

        ArchiveIndex archiveIndex = new ArchiveIndex(archives);
        Path metaLocation =
                latestLocation.resolve(SRC_FOLDER).resolve(CONTRIB_FOLDER).resolve("Meta");
        if (Files.notExists(metaLocation)) {
            Files.createDirectories(metaLocation);
        }
        Path archiveRds = metaLocation.resolve(ARCHIVE_RDS);
        FileOutputStream archiveRdsStream = new FileOutputStream(archiveRds.toFile());
        BufferedOutputStream outputStream = new BufferedOutputStream(archiveRdsStream);
        archiveIndex.serialize(outputStream);
    }

    @Override
    public void storeAndDeleteFiles(SynchronizeCranRepositoryRequestBody request) throws StorageException, IOException {
        final String repository = request.getRepository();
        MultipartFile[] filesToUpload = request.getFilesToUpload();
        MultipartFile[] filesToUploadToArchive = request.getFilesToUploadToArchive();
        String[] filesToDelete = request.getFilesToDelete();
        String[] filesToDeleteFromArchive = request.getFilesToDeleteFromArchive();

        if (filesToUpload != null) store(filesToUpload, repository, request.getId());
        if (filesToUploadToArchive != null) storeInArchive(filesToUploadToArchive, repository, request.getId());
        if (filesToDelete != null) delete(filesToDelete, repository, request.getId());
        if (filesToDeleteFromArchive != null) deleteFromArchive(filesToDeleteFromArchive, repository, request.getId());
    }

    @Override
    public void handleLastChunk(SynchronizeCranRepositoryRequestBody request, String repository)
            throws StorageException {
        try {
            generateArchiveRds(repository);
        } catch (IOException e) {
            throw new StorageException(e.getMessage(), e);
        }
        super.handleLastChunk(request, repository);
    }

    private void copyToDedicatedDirectory(MultipartFile file, Path rootDirectory, String id)
            throws IOException, MoveToTrashException {
        String fileName = file.getOriginalFilename();
        assert !StringUtils.isBlank(fileName);
        Path saveLocation = rootDirectory.resolve(fileName.split("_")[0]);
        if (!Files.exists(saveLocation)) {
            Files.createDirectory(saveLocation);
        }
        Path destination = saveLocation.resolve(fileName);
        if (Files.exists(destination)) {
            moveToTrash(id, destination);
        }
        Files.copy(file.getInputStream(), saveLocation.resolve(fileName));
    }

    private void storeInArchive(MultipartFile[] files, String repository, String id) {
        Path saveLocation = ((repository != null) && (!repository.trim().isEmpty()))
                ? this.rootLocation.resolve(repository)
                : this.rootLocation;
        saveLocation = saveLocation.resolve(SRC_FOLDER).resolve(CONTRIB_FOLDER).resolve(ARCHIVE_FOLDER);
        logger.debug("Saving to location {}", saveLocation);
        try {
            Files.createDirectories(saveLocation);
        } catch (IOException e) {
            throw new StorageException(
                    "Failed to create directory " + saveLocation.toFile().getAbsolutePath(), e);
        }
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            assert !StringUtils.isBlank(fileName);
            try {
                if (fileName.equals(PACKAGES) || fileName.equals(PACKAGES_GZ)) {
                    Path destination = saveLocation.resolve(fileName);
                    if (Files.exists(destination)) {
                        moveToTrash(id, destination);
                    }
                    Files.copy(file.getInputStream(), destination);
                } else {
                    copyToDedicatedDirectory(file, saveLocation, id);
                }
            } catch (IOException | MoveToTrashException e) {
                throw new StorageException("Failed to store file " + fileName, e);
            }
        }
    }

    private void store(MultipartFile[] files, String repository, String id) throws IOException {
        Path saveLocation = ((repository != null) && (!repository.trim().isEmpty()))
                ? this.rootLocation.resolve(repository)
                : this.rootLocation;
        saveLocation = saveLocation.resolve(SRC_FOLDER).resolve(CONTRIB_FOLDER);
        store(files, saveLocation, id);
    }

    public List<Path> getRecentPackagesFromRepository(String repository) throws IOException {
        Path location = ((repository != null) && (!repository.trim().isEmpty()))
                ? this.rootLocation.resolve(repository)
                : this.rootLocation;
        location = location.resolve(SRC_FOLDER).resolve(CONTRIB_FOLDER);
        List<Path> files = new ArrayList<>();
        if (Files.notExists(location) || !Files.isDirectory(location)) {
            return files;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(location, this::noExcludedFilesAndDirectories)) {
            stream.forEach(files::add);
        }
        return files;
    }

    public Map<String, List<Path>> getArchiveFromRepository(String repository) throws IOException {
        Path location = ((repository != null) && (!repository.trim().isEmpty()))
                ? this.rootLocation.resolve(repository)
                : this.rootLocation;
        location = location.resolve(SRC_FOLDER).resolve(CONTRIB_FOLDER).resolve(ARCHIVE_FOLDER);

        Map<String, List<Path>> archive = new HashMap<>();

        if (Files.notExists(location) || !Files.isDirectory(location)) {
            return archive;
        }

        List<Path> directories = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(location, this::onlyReadableDirectories)) {
            stream.forEach(directories::add);
        }

        for (Path directory : directories) {
            List<Path> files = new ArrayList<>();
            try (DirectoryStream<Path> stream =
                    Files.newDirectoryStream(directory, this::noExcludedFilesAndDirectories)) {
                stream.forEach(files::add);
            }
            archive.put(directory.getFileName().toString(), files);
        }

        return archive;
    }

    public void emptyTrash(String repository, String requestId) throws EmptyTrashException {
        Path trash = this.rootLocation.resolve(TRASH_PREFIX + requestId);
        try {
            if (Files.exists(trash)) FileUtils.forceDelete(trash.toFile());
            Path archive = this.rootLocation
                    .resolve(repository)
                    .resolve(SRC_FOLDER)
                    .resolve(CONTRIB_FOLDER)
                    .resolve(ARCHIVE_FOLDER);
            if (Files.notExists(archive) || !Files.isDirectory(archive)) {
                return;
            }
            try (DirectoryStream<Path> archiveFiles = Files.newDirectoryStream(archive)) {
                for (Path archiveFile : archiveFiles) {
                    if (!Files.isDirectory(archiveFile)) {
                        continue;
                    }
                    try (Stream<Path> files = Files.list(archiveFile)) {
                        if (files.findAny().isEmpty()) {
                            FileUtils.forceDelete(archiveFile.toFile());
                        }
                    }
                }
            }
            try (Stream<Path> archiveFiles = Files.list(archive)) {
                if (archiveFiles.count() == 2) {
                    if (Files.exists(archive.resolve(PACKAGES))) {
                        FileUtils.forceDelete(archive.resolve(PACKAGES).toFile());
                    }
                    if (Files.exists(archive.resolve(PACKAGES_GZ))) {
                        FileUtils.forceDelete(archive.resolve(PACKAGES_GZ).toFile());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Could not delete trash directory!", e);
            throw new EmptyTrashException(repository);
        }
    }

    @Override
    public void removeNonExistingPackagesFromRepo(List<String> packages, @NonNull String repository)
            throws RestoreRepositoryException {
        final Path latestLocation =
                !StringUtils.isBlank(repository) ? this.rootLocation.resolve(repository) : this.rootLocation;
        final Path packagesLocation = latestLocation.resolve(SRC_FOLDER).resolve(CONTRIB_FOLDER);
        removePackagesFromLocation(packages, packagesLocation, repository);
    }

    @Override
    public void removeNonExistingArchivePackagesFromRepo(List<String> archivePackages, String repository)
            throws RestoreRepositoryException {
        final Path archiveLocation =
                !StringUtils.isBlank(repository) ? this.rootLocation.resolve(repository) : this.rootLocation;
        final Path packagesLocation =
                archiveLocation.resolve(SRC_FOLDER).resolve(CONTRIB_FOLDER).resolve(ARCHIVE_FOLDER);
        removePackagesFromLocation(archivePackages, packagesLocation, repository);
    }

    private void delete(String packageName, String repository, String requestId, Boolean fromArchive)
            throws StorageException {
        Path location = ((repository != null) && (!repository.trim().isEmpty()))
                ? this.rootLocation.resolve(repository)
                : this.rootLocation;
        location = location.resolve(SRC_FOLDER).resolve(CONTRIB_FOLDER);
        location = fromArchive ? location.resolve(ARCHIVE_FOLDER) : location;

        if (Files.notExists(location) || !Files.isDirectory(location)) {
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(location)) {
            if (StringUtils.isBlank(packageName) && !fromArchive) {
                for (Path packageFile : stream) {
                    moveToTrash(requestId, packageFile);
                }
            } else {
                if (!StringUtils.isBlank(packageName) && fromArchive) {
                    location = location.resolve(packageName.split("_")[0]);
                }

                Path packageFilePath = location.resolve(packageName);

                moveToTrash(requestId, packageFilePath);
            }
        } catch (MoveToTrashException | IOException e) {
            throw new StorageException("Could not delete package file", e);
        }
    }

    @Override
    protected boolean isPackageFile(File file) {
        final String fileName = file.getName();
        return !(Objects.equals(fileName, ARCHIVE_FOLDER)
                || Objects.equals(fileName, PACKAGES)
                || Objects.equals(fileName, PACKAGES_GZ));
    }

    private void delete(String[] packageNames, String repository, String requestId, Boolean fromArchive)
            throws StorageException {
        for (String packageName : packageNames) {
            delete(packageName, repository, requestId, fromArchive);
        }
    }

    private void delete(String[] packageNames, String repository, String requestId) throws StorageException {
        delete(packageNames, repository, requestId, false);
    }

    private void deleteFromArchive(String[] packageNames, String repository, String requestId) throws StorageException {
        delete(packageNames, repository, requestId, true);
    }

    @Override
    public Map<String, File> getPackagesFiles(String repository, boolean archive) {
        Map<String, File> files = new HashMap<>();

        Path packagesFilesRoot =
                this.rootLocation.resolve(repository).resolve(SRC_FOLDER).resolve(CONTRIB_FOLDER);
        packagesFilesRoot = archive ? packagesFilesRoot.resolve(ARCHIVE_FOLDER) : packagesFilesRoot;

        File packages = packagesFilesRoot.resolve(PACKAGES).toFile();
        File packagesGZ = packagesFilesRoot.resolve(PACKAGES_GZ).toFile();

        if (packages.exists() && packagesGZ.exists()) {
            files.put(PACKAGES, packages);
            files.put(PACKAGES_GZ, packagesGZ);
        }

        return files;
    }
}

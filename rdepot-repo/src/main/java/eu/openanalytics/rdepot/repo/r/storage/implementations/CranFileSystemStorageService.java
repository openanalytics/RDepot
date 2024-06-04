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
import eu.openanalytics.rdepot.repo.r.transaction.backup.CranRepositoryBackup;
import eu.openanalytics.rdepot.repo.storage.StorageProperties;
import eu.openanalytics.rdepot.repo.storage.implementations.FileSystemStorageService;
import io.micrometer.common.util.StringUtils;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class CranFileSystemStorageService
        extends FileSystemStorageService<SynchronizeCranRepositoryRequestBody, CranRepositoryBackup>
        implements CranStorageService {

    Logger logger = LoggerFactory.getLogger(CranFileSystemStorageService.class);

    private final Set<String> excludedFiles = new HashSet<String>(Arrays.asList("PACKAGES", "PACKAGES.gz"));

    public CranFileSystemStorageService(StorageProperties properties) {
        super(properties);
    }

    public void generateArchiveRds(String repository) throws IOException {
        Path latestLocation = ((repository != null) && (!repository.trim().isEmpty()))
                ? this.rootLocation.resolve(repository)
                : this.rootLocation;
        Path archiveLocation = latestLocation.resolve("src").resolve("contrib").resolve("Archive");

        if (Files.notExists(archiveLocation)) {
            Path archiveRds = latestLocation
                    .resolve("src")
                    .resolve("contrib")
                    .resolve("Meta")
                    .resolve("archive.rds");
            Files.deleteIfExists(archiveRds);
            return;
        }

        Map<String, List<ArchiveInfo>> archives = new HashMap<>();

        File[] directories = archiveLocation.toFile().listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.canRead() && file.isDirectory();
            }
        });
        for (File dir : directories) {
            List<ArchiveInfo> infos = new ArrayList<>();
            File[] packages = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.canRead() && file.isFile();
                }
            });
            for (File file : packages) {
                LocalDateTime modifiedTime = null;
                LocalDateTime accessTime = null;
                LocalDateTime createdTime = null;
                try {
                    BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    modifiedTime =
                            LocalDateTime.ofInstant(attr.lastModifiedTime().toInstant(), ZoneId.systemDefault());
                    accessTime = LocalDateTime.ofInstant(attr.lastAccessTime().toInstant(), ZoneId.systemDefault());
                    createdTime = LocalDateTime.ofInstant(attr.creationTime().toInstant(), ZoneId.systemDefault());
                } catch (Exception e) {
                    logger.debug(
                            "Exception when querying for basic file attributes of path " + file.toString() + ": "
                                    + e.getMessage(),
                            e);
                }
                infos.add(new ArchiveInfo(
                        dir.getName() + "/" + file.getName(),
                        Long.valueOf(file.length()).intValue(),
                        436,
                        modifiedTime,
                        createdTime,
                        accessTime,
                        1000,
                        1000,
                        null,
                        null));
            }
            if (!infos.isEmpty()) {
                archives.put(dir.getName(), infos);
            }
        }
        if (archives.isEmpty()) {
            Path archiveRds = latestLocation
                    .resolve("src")
                    .resolve("contrib")
                    .resolve("Meta")
                    .resolve("archive.rds");
            Files.deleteIfExists(archiveRds);
            return;
        }

        ArchiveIndex archiveIndex = new ArchiveIndex(archives);
        Path metaLocation = latestLocation.resolve("src").resolve("contrib").resolve("Meta");
        if (Files.notExists(metaLocation)) {
            Files.createDirectories(metaLocation);
        }
        Path archiveRds = metaLocation.resolve("archive.rds");
        FileOutputStream archiveRdsStrean = new FileOutputStream(archiveRds.toFile());
        BufferedOutputStream outputStream = new BufferedOutputStream(archiveRdsStrean);
        archiveIndex.serialize(outputStream);
    }

    @Override
    public void storeAndDeleteFiles(SynchronizeCranRepositoryRequestBody request)
            throws StorageException, FileNotFoundException {
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
            logger.error(e.getMessage(), e);
            throw new StorageException(e.getMessage(), e);
        }
        super.handleLastChunk(request, repository);
    }

    private void copyToDedicatedDirectory(MultipartFile file, Path rootDirectory, String id)
            throws IOException, MoveToTrashException {
        Path saveLocation = rootDirectory.resolve(file.getOriginalFilename().split("_")[0]);
        if (!Files.exists(saveLocation)) {
            Files.createDirectory(saveLocation);
        }
        Path destination = saveLocation.resolve(file.getOriginalFilename());
        if (Files.exists(destination)) {
            moveToTrash(id, destination.toFile());
        }
        Files.copy(file.getInputStream(), saveLocation.resolve(file.getOriginalFilename()));
    }

    private void storeInArchive(MultipartFile[] files, String repository, String id) {
        Path saveLocation = ((repository != null) && (!repository.trim().isEmpty()))
                ? this.rootLocation.resolve(repository)
                : this.rootLocation;
        saveLocation = saveLocation.resolve("src").resolve("contrib").resolve("Archive");
        logger.debug("Saving to location {}", saveLocation.toString());
        try {
            if (!Files.exists(saveLocation)) {
                Files.createDirectories(saveLocation);
            }
        } catch (IOException e) {
            throw new StorageException(
                    "Failed to create directory " + saveLocation.toFile().getAbsolutePath(), e);
        }
        for (MultipartFile file : files) {
            try {
                if (Objects.equals(file.getOriginalFilename(), "PACKAGES")
                        || file.getOriginalFilename().equals("PACKAGES.gz")) {
                    Path destination = saveLocation.resolve(file.getOriginalFilename());
                    if (Files.exists(destination)) {
                        moveToTrash(id, destination.toFile());
                    }
                    Files.copy(file.getInputStream(), destination);
                } else {
                    copyToDedicatedDirectory(file, saveLocation, id);
                }
            } catch (IOException | MoveToTrashException e) {
                throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
            }
        }
    }

    private void store(MultipartFile[] files, String repository, String id) {
        Path saveLocation = ((repository != null) && (!repository.trim().isEmpty()))
                ? this.rootLocation.resolve(repository)
                : this.rootLocation;
        saveLocation = saveLocation.resolve("src").resolve("contrib");
        store(files, saveLocation, id);
    }

    public List<File> getRecentPackagesFromRepository(String repository) {
        ArrayList<File> files = new ArrayList<>();
        Path location = ((repository != null) && (!repository.trim().isEmpty()))
                ? this.rootLocation.resolve(repository)
                : this.rootLocation;
        location = location.resolve("src").resolve("contrib");

        if (location.toFile().exists()) {
            for (File file : location.toFile().listFiles()) {
                if (!file.isDirectory() && !excludedFiles.contains(file.getName())) {
                    files.add(file);
                }
            }
        }

        return files;
    }

    public Map<String, List<File>> getArchiveFromRepository(String repository) {
        Path location = ((repository != null) && (!repository.trim().isEmpty()))
                ? this.rootLocation.resolve(repository)
                : this.rootLocation;
        location = location.resolve("src").resolve("contrib").resolve("Archive");

        List<Path> directories = new ArrayList<>();
        Map<String, List<File>> archive = new HashMap<>();

        if (location.toFile().exists()) {
            for (File file : Objects.requireNonNull(location.toFile().listFiles())) {
                if (!excludedFiles.contains(file.getName())) {
                    directories.add(file.toPath());
                }
            }
        }

        for (Path directory : directories) {
            List<File> files = new ArrayList<>();

            if (directory.toFile().exists()) {
                for (File file : Objects.requireNonNull(directory.toFile().listFiles())) {
                    if (!file.isDirectory() && !excludedFiles.contains(file.getName())) {
                        files.add(file);
                    }
                }
                archive.put(directory.getFileName().toString(), files);
            }
        }

        return archive;
    }

    public void emptyTrash(String repository, String requestId) throws EmptyTrashException {
        Path trash = this.rootLocation.resolve(TRASH_PREFIX + requestId);
        File archive = this.rootLocation
                .resolve(repository)
                .resolve("src")
                .resolve("contrib")
                .resolve("Archive")
                .toFile();
        try {
            if (Files.exists(trash)) FileUtils.forceDelete(trash.toFile());

            if (archive.isDirectory()) {
                for (File file : Objects.requireNonNull(archive.listFiles())) {
                    if (file.isDirectory() && Objects.requireNonNull(file.listFiles()).length == 0) {
                        FileUtils.forceDelete(file);
                    }

                    if (Objects.requireNonNull(archive.listFiles()).length == 2) {
                        for (File packagesFile : archive.listFiles()) {
                            if (packagesFile.getName().equals("PACKAGES")
                                    || packagesFile.getName().equals("PACKAGES.gz"))
                                FileUtils.forceDelete(packagesFile);
                        }
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
        final Path packagesLocation = latestLocation.resolve("src").resolve("contrib");
        removePackagesFromLocation(packages, packagesLocation, repository);
    }

    @Override
    public void removeNonExistingArchivePackagesFromRepo(List<String> archivePackages, String repository)
            throws RestoreRepositoryException {
        final Path archiveLocation =
                !StringUtils.isBlank(repository) ? this.rootLocation.resolve(repository) : this.rootLocation;
        final Path packagesLocation =
                archiveLocation.resolve("src").resolve("contrib").resolve("Archive");
        removePackagesFromLocation(archivePackages, packagesLocation, repository);
    }

    private void delete(String packageName, String repository, String requestId, Boolean fromArchive)
            throws FileNotFoundException, StorageException {
        Path location = ((repository != null) && (!repository.trim().isEmpty()))
                ? this.rootLocation.resolve(repository)
                : this.rootLocation;
        location = location.resolve("src").resolve("contrib");
        location = fromArchive ? location.resolve("Archive") : location;

        try {
            if ((packageName == null || packageName.isBlank()) && !fromArchive) {
                for (File packageFile : Objects.requireNonNull(location.toFile().listFiles())) {
                    if (!Files.exists(packageFile.toPath())) throw new FileNotFoundException();

                    moveToTrash(requestId, packageFile);
                }
            } else {
                if (fromArchive) location = location.resolve(packageName.split("_")[0]);

                Path packageFilePath = location.resolve(packageName);

                if (!Files.exists(packageFilePath)) throw new FileNotFoundException(packageFilePath.toString());

                moveToTrash(requestId, packageFilePath.toFile());
            }
        } catch (FileNotFoundException e) {
            throw e;
        } catch (MoveToTrashException e) {
            throw new StorageException("Could not delete package file", e);
        }
    }

    @Override
    protected boolean isPackageFile(File file) {
        final String fileName = file.getName();
        return !(Objects.equals(fileName, "Archive")
                || Objects.equals(fileName, "PACKAGES")
                || Objects.equals(fileName, "PACKAGES.gz"));
    }

    private void delete(String[] packageNames, String repository, String requestId, Boolean fromArchive)
            throws FileNotFoundException, StorageException {
        for (String packageName : packageNames) {
            delete(packageName, repository, requestId, fromArchive);
        }
    }

    private void delete(String[] packageNames, String repository, String requestId)
            throws FileNotFoundException, StorageException {
        delete(packageNames, repository, requestId, false);
    }

    private void deleteFromArchive(String[] packageNames, String repository, String requestId)
            throws FileNotFoundException, StorageException {
        delete(packageNames, repository, requestId, true);
    }

    @Override
    public Map<String, File> getPackagesFiles(String repository, boolean archive) {
        Map<String, File> files = new HashMap<>();

        Path packagesFilesRoot =
                this.rootLocation.resolve(repository).resolve("src").resolve("contrib");
        packagesFilesRoot = archive ? packagesFilesRoot.resolve("Archive") : packagesFilesRoot;

        File packages = packagesFilesRoot.resolve("PACKAGES").toFile();
        File packagesGZ = packagesFilesRoot.resolve("PACKAGES.gz").toFile();

        if (packages.exists() && packagesGZ.exists()) {
            files.put("PACKAGES", packages);
            files.put("PACKAGES.gz", packagesGZ);
        }

        return files;
    }
}

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
package eu.openanalytics.rdepot.repo.storage.implementations;

import eu.openanalytics.rdepot.repo.exception.EmptyTrashException;
import eu.openanalytics.rdepot.repo.exception.GetRepositoryVersionException;
import eu.openanalytics.rdepot.repo.exception.InitTrashDirectoryException;
import eu.openanalytics.rdepot.repo.exception.MoveToTrashException;
import eu.openanalytics.rdepot.repo.exception.RestoreRepositoryException;
import eu.openanalytics.rdepot.repo.exception.SetRepositoryVersionException;
import eu.openanalytics.rdepot.repo.exception.StorageException;
import eu.openanalytics.rdepot.repo.model.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.storage.StorageProperties;
import eu.openanalytics.rdepot.repo.storage.StorageService;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;

@Slf4j
public abstract class FileSystemStorageService<T extends SynchronizeRepositoryRequestBody>
        implements StorageService<T> {

    protected final Path rootLocation;

    protected FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void createRepoDirectory(String name) throws StorageException {
        if (!this.rootLocation.resolve(name).toFile().mkdirs())
            throw new StorageException(String.format("Could not create repo directory %s", name));
    }

    public File initTrashDirectory(String id) throws InitTrashDirectoryException {
        Path trash = this.rootLocation.resolve(TRASH_PREFIX + id);
        Path trashDatabase = trash.resolve(TRASH_DATABASE_FILE);

        try {
            if (Files.exists(trash)) {
                FileUtils.forceDelete(trash.toFile());
            }
            Files.createDirectory(trash);
            Files.createFile(trashDatabase);
        } catch (FileAlreadyExistsException ignored) {
            // ignore if the trash database file already exists
        } catch (IOException e) {
            log.error("Error while creating trash directory: {}", e.getMessage(), e);
            throw new InitTrashDirectoryException(id);
        }
        return trash.toFile();
    }

    @Override
    public List<String> getAllRepositoryDirectories() {
        final File[] dirs = this.rootLocation.toFile().listFiles(File::isDirectory);
        if (dirs == null) {
            throw new IllegalStateException("Upload directory array is null.");
        }
        return Arrays.stream(dirs).map(File::getName).collect(Collectors.toList());
    }

    public void moveToTrash(String id, Path packageFile) throws MoveToTrashException {
        if (!Files.exists(packageFile)) throw new MoveToTrashException(id, packageFile.toFile());
        Path trash = this.rootLocation.resolve(TRASH_PREFIX + id);
        File trashDatabase = trash.resolve(TRASH_DATABASE_FILE).toFile();

        if (Files.notExists(trash)) {
            log.error("No trash directory for transaction: {}", id);
            throw new MoveToTrashException(id, packageFile.toFile());
        }

        try {
            UUID uuid = UUID.randomUUID();
            FileWriter fileWriter = new FileWriter(trashDatabase, true);
            fileWriter
                    .append(uuid.toString())
                    .append(":")
                    .append(String.valueOf(packageFile.toAbsolutePath()))
                    .append(System.lineSeparator());
            fileWriter.close();

            Files.move(packageFile, trash.resolve(uuid.toString()));
        } catch (IOException e) {
            log.error("Error while moving file: {}", e.getMessage(), e);
            throw new MoveToTrashException(id, packageFile.toFile());
        }
    }

    @Override
    public void handleLastChunk(T request, String repository) throws StorageException {
        try {
            emptyTrash(repository, request.getId());
        } catch (EmptyTrashException e) {
            log.error(e.getMessage(), e);
            throw new StorageException(e.getMessage(), e);
        }
    }

    public void emptyTrash(String repository, String requestId) throws EmptyTrashException {
        final Path trash = this.rootLocation.resolve(TRASH_PREFIX + requestId);
        if (Files.exists(trash)) {
            try {
                FileUtils.forceDelete(trash.toFile());
            } catch (IOException e) {
                log.error("Could not delete trash directory!", e);
                throw new EmptyTrashException(repository);
            }
        }
    }

    protected void removePackagesFromLocation(List<String> packages, Path location, String repository)
            throws RestoreRepositoryException {
        final File[] files = location.toFile().listFiles();

        if (files == null) throw new RestoreRepositoryException(repository);

        for (File packageFile : files) {
            if (!isPackageFile(packageFile)) continue;
            if (!packages.contains(packageFile.getName())) {
                try {
                    FileUtils.forceDelete(packageFile);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    throw new RestoreRepositoryException(repository);
                }
            }
        }
    }

    protected abstract boolean isPackageFile(File packageFile);

    public void setRepositoryVersion(String repository, String version) {
        Path versionPath = this.rootLocation.resolve(repository).resolve("VERSION");

        try {
            if (Files.notExists(versionPath)) {
                Files.createFile(versionPath);
            }

            try {
                Integer.valueOf(version);
            } catch (NumberFormatException e) {
                log.error(e.getMessage(), e);
            }

            try (FileWriter writer = new FileWriter(versionPath.toFile())) {
                writer.write(version);
            }

        } catch (IOException | NumberFormatException e) {
            log.error("{}: {}", e.getClass().getCanonicalName(), e.getMessage(), e);
            throw new SetRepositoryVersionException(repository);
        }
    }

    @Override
    public void init() {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectory(rootLocation);
            }
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Override
    public void restoreTrash(File trashDirectory) throws RestoreRepositoryException {
        final Path trashPath = trashDirectory.toPath();
        final File trashDatabaseFile = trashPath.resolve(TRASH_DATABASE_FILE).toFile();
        if (!trashDatabaseFile.exists()) {
            throw new RestoreRepositoryException(trashDirectory);
        }

        final Map<String, String> trashDatabase = parseTrashDatabase(trashDatabaseFile);
        for (Map.Entry<String, String> trashEntry : trashDatabase.entrySet()) {
            final String fileName = trashEntry.getKey();
            final String previousDirectory = trashEntry.getValue();
            try {
                Files.move(
                        trashPath.resolve(fileName),
                        new File(previousDirectory).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new RestoreRepositoryException(trashDirectory);
            }
        }
    }

    protected Map<String, String> parseTrashDatabase(File trashDatabase) throws RestoreRepositoryException {
        final Map<String, String> map = new LinkedHashMap<>();

        try (Scanner scanner = new Scanner(trashDatabase)) {
            while (scanner.hasNextLine()) {
                final String[] data = scanner.nextLine().split(":");
                if (data.length < 2) continue;
                final String fileName = data[0];
                final String previousDirectory = data[1];
                map.put(fileName, previousDirectory);
            }
        } catch (FileNotFoundException e) {
            throw new RestoreRepositoryException(trashDatabase);
        }
        return map;
    }

    @Override
    public String getRepositoryVersion(String repository) throws GetRepositoryVersionException {
        Path repositoryDirectory = this.rootLocation.resolve(repository);

        Path versionPath = repositoryDirectory.resolve("VERSION");
        String versionStr;
        try {
            if (Files.notExists(repositoryDirectory)) Files.createDirectory(repositoryDirectory);

            if (Files.notExists(versionPath)) {
                versionStr = "1";
                Files.createFile(versionPath);

                try (FileWriter writer = new FileWriter(versionPath.toFile())) {
                    writer.write(versionStr);
                }
            } else {
                try (Scanner scanner = new Scanner(versionPath)) {
                    versionStr = scanner.nextLine();
                    Integer.valueOf(versionStr);
                }
            }
        } catch (IOException | NumberFormatException e) {
            log.error("{}: {}", e.getClass().getCanonicalName(), e.getMessage(), e);
            throw new GetRepositoryVersionException(repository);
        }

        return versionStr;
    }

    public void boostRepositoryVersion(String repository) throws SetRepositoryVersionException {
        try {
            String currentVersionStr = getRepositoryVersion(repository);
            int currentVersion = Integer.parseInt(currentVersionStr);
            String newVersion = String.valueOf(++currentVersion);

            setRepositoryVersion(repository, newVersion);
        } catch (GetRepositoryVersionException | NumberFormatException e) {
            log.error("{}: {}", e.getClass().getCanonicalName(), e.getMessage(), e);
            throw new SetRepositoryVersionException(repository);
        }
    }
}

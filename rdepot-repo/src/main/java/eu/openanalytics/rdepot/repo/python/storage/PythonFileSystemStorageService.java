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
package eu.openanalytics.rdepot.repo.python.storage;

import eu.openanalytics.rdepot.repo.exception.MoveToTrashException;
import eu.openanalytics.rdepot.repo.exception.RestoreRepositoryException;
import eu.openanalytics.rdepot.repo.exception.StorageException;
import eu.openanalytics.rdepot.repo.python.model.SynchronizePythonRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.storage.StorageProperties;
import eu.openanalytics.rdepot.repo.storage.implementations.FileSystemStorageService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class PythonFileSystemStorageService extends FileSystemStorageService<SynchronizePythonRepositoryRequestBody>
        implements PythonStorageService {

    private static final String PACKAGES_INDEX_FILE_NAME = "index.html";

    private final Set<String> excludedFiles = new HashSet<>(List.of(PACKAGES_INDEX_FILE_NAME, "VERSION"));

    public PythonFileSystemStorageService(StorageProperties properties) {
        super(properties);
    }

    protected void store(MultipartFile[] files, Path saveLocation, String id) throws IOException {
        log.debug("Saving to location {}", saveLocation.toString());
        for (MultipartFile file : files) {
            extractFiles(file, saveLocation);
        }
    }

    private void extractFiles(MultipartFile file, Path saveLocation) throws IOException {
        Path packageDir = saveLocation;
        try {
            String fileName = file.getOriginalFilename();
            assert !StringUtils.isBlank(fileName);

            String normalizedName = normalizeName(fileName.substring(0, fileName.length() - 7));
            if (!fileName.equals("index.tar.gz")) {
                packageDir = Paths.get(saveLocation.toString(), normalizedName);
            }
            if (!Files.exists(packageDir)) {
                Files.createDirectories(packageDir);
            }
            File fileToUnpack = new File(packageDir + "/" + fileName);
            try {
                Files.createFile(fileToUnpack.toPath());
            } catch (FileAlreadyExistsException ignored) {
                // ignore if the file to transfer to already exists
            }
            file.transferTo(fileToUnpack);

            File unGzippedFile;
            unGzippedFile = unGzip(fileToUnpack, new File(packageDir.toString()));
            unTar(unGzippedFile, new File(packageDir.toString()));
        } catch (IOException e) {
            throw new StorageException("Failed to create directory " + saveLocation.getFileName(), e);
        } catch (ArchiveException e) {
            throw new StorageException("Failed to extract packages" + saveLocation.getFileName(), e);
        } finally {
            cleanSpace(packageDir);
        }
    }

    private String normalizeName(String name) {
        return name.replaceAll("[-_.]", "-").toLowerCase();
    }

    private void cleanSpace(Path packageDir) throws IOException {
        if (!Files.isDirectory(packageDir)) {
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(packageDir, "*.tar")) {
            for (Path packageFile : stream) {
                Files.delete(packageFile);
            }
        }
    }

    private File unGzip(final File inputFile, final File outputDir) throws IOException {
        log.debug("Extracting {} to dir {}.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath());

        final File outputFile = new File(
                outputDir, inputFile.getName().substring(0, inputFile.getName().length() - 3));

        final GZIPInputStream in = new GZIPInputStream(new FileInputStream(inputFile));
        final FileOutputStream out = new FileOutputStream(outputFile);

        IOUtils.copy(in, out);

        in.close();
        out.close();
        Files.delete(inputFile.toPath());
        return outputFile;
    }

    private void unTar(final File inputFile, final File outputDir) throws IOException, ArchiveException {
        log.debug("Extracting {} to dir {}.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath());
        final InputStream is = new FileInputStream(inputFile);
        final TarArchiveInputStream debInputStream = new ArchiveStreamFactory().createArchiveInputStream("tar", is);
        TarArchiveEntry entry;

        while ((entry = debInputStream.getNextEntry()) != null) {
            final Path outputFile = Paths.get(outputDir.getAbsolutePath(), entry.getName());

            Files.createDirectories(outputFile.getParent());

            if (entry.isDirectory()) {
                Files.createDirectories(outputFile);
            } else {
                final OutputStream outputFileStream = new FileOutputStream(outputFile.toFile());
                IOUtils.copy(debInputStream, outputFileStream);
                outputFileStream.close();
            }
        }
        debInputStream.close();
    }

    @Override
    public void storeAndDeleteFiles(SynchronizePythonRepositoryRequestBody request)
            throws IOException, StorageException {
        final String repository = request.getRepository();
        final MultipartFile[] filesToUpload = request.getFilesToUpload();
        final String[] filesToDelete = request.getFilesToDelete();

        if (filesToUpload != null) store(filesToUpload, repository, request.getId());
        if (filesToDelete != null) delete(filesToDelete, repository, request.getId());
    }

    private void store(MultipartFile[] files, String repository, String id) throws IOException {
        final Path saveLocation = ((repository != null) && (!repository.trim().isEmpty()))
                ? this.rootLocation.resolve(repository)
                : this.rootLocation;
        store(files, saveLocation, id);
    }

    public List<Path> getRecentPackagesFromRepository(String repository) throws IOException {
        final ArrayList<Path> files = new ArrayList<>();
        final Path location = ((repository != null) && (!repository.trim().isEmpty()))
                ? this.rootLocation.resolve(repository)
                : this.rootLocation;
        getRecentPackagesFromRepository(files, location);
        return files;
    }

    public void getRecentPackagesFromRepository(List<Path> files, Path location) throws IOException {
        if (Files.notExists(location) || !Files.isDirectory(location)) {
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(location)) {
            for (Path file : stream) {
                if (Files.isDirectory(file)) {
                    getRecentPackagesFromRepository(files, file);
                } else if (!excludedFiles.contains(file.getFileName().toString())) {
                    files.add(file);
                }
            }
        }
    }

    @Override
    public void removeNonExistingPackagesFromRepo(List<String> packages, String repository)
            throws RestoreRepositoryException {
        final Path latestLocation =
                !StringUtils.isBlank(repository) ? this.rootLocation.resolve(repository) : this.rootLocation;
        removePackagesFromLocation(packages, latestLocation, repository);
    }

    @Override
    protected boolean isPackageFile(@NonNull File packageFile) {
        if (!packageFile.isDirectory()) return false;
        final File[] packageArchives = packageFile.listFiles((dir, name) -> name.startsWith(packageFile.getName()));
        return packageArchives != null && packageArchives.length > 0;
    }

    private void delete(String packageName, String repository, String requestId) throws StorageException {
        Path location = ((repository != null) && (!repository.trim().isEmpty()))
                ? this.rootLocation.resolve(repository)
                : this.rootLocation;
        if (!Files.isDirectory(location)) {
            return;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(location)) {
            if (StringUtils.isBlank(packageName)) {
                for (Path packageFile : stream) {
                    moveToTrash(requestId, packageFile);
                }
            } else {
                location = location.resolve(packageName.substring(0, packageName.indexOf('/')));
                Path packageFilePath = location.resolve(packageName.substring(packageName.indexOf('/') + 1));
                moveToTrash(requestId, packageFilePath);
                if (!Files.isDirectory(location)) {
                    return;
                }
                List<Path> filesInParentDirectory =
                        StreamSupport.stream(stream.spliterator(), false).toList();
                if (filesInParentDirectory.size() == 1
                        && filesInParentDirectory
                                .get(0)
                                .getFileName()
                                .toString()
                                .equals(PACKAGES_INDEX_FILE_NAME)) {
                    moveToTrash(requestId, location.resolve(filesInParentDirectory.get(0)));
                    Files.delete(location);
                }
            }
        } catch (MoveToTrashException | IOException e) {
            throw new StorageException("Could not delete package file", e);
        }
    }

    private void delete(String[] packageNames, String repository, String requestId) throws StorageException {
        for (String packageName : packageNames) {
            delete(packageName, repository, requestId);
        }
    }
}

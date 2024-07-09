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
package eu.openanalytics.rdepot.repo.python.storage;

import eu.openanalytics.rdepot.repo.exception.EmptyTrashException;
import eu.openanalytics.rdepot.repo.exception.MoveToTrashException;
import eu.openanalytics.rdepot.repo.exception.RestoreRepositoryException;
import eu.openanalytics.rdepot.repo.exception.StorageException;
import eu.openanalytics.rdepot.repo.python.model.SynchronizePythonRepositoryRequestBody;
import eu.openanalytics.rdepot.repo.storage.StorageProperties;
import eu.openanalytics.rdepot.repo.storage.implementations.FileSystemStorageService;
import io.micrometer.common.util.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class PythonFileSystemStorageService extends FileSystemStorageService<SynchronizePythonRepositoryRequestBody>
        implements PythonStorageService {

    private static final String PACKAGES_INDEX_FILE_NAME = "index.html";

    private final Set<String> excludedFiles = new HashSet<String>(List.of(PACKAGES_INDEX_FILE_NAME));

    public PythonFileSystemStorageService(StorageProperties properties) {
        super(properties);
    }

    @Override
    protected void store(MultipartFile[] files, Path saveLocation, String id) {
        log.debug("Saving to location {}", saveLocation.toString());
        for (MultipartFile file : files) {
            extractFiles(file, saveLocation);
        }
    }

    private void extractFiles(MultipartFile file, Path saveLocation) {
        Path packageDir = saveLocation;
        try {
            String dirName = file.getOriginalFilename();
            if (!Objects.equals(dirName, "index.tar.gz")) {
                packageDir = Paths.get(saveLocation.toString() + "/" + dirName.substring(0, dirName.length() - 7));
            }
            if (!Files.exists(packageDir)) {
                Files.createDirectories(packageDir);
            }
            File fileToUnpack = new File(packageDir.toString() + "/" + file.getOriginalFilename());
            if (!fileToUnpack.exists()) {
                fileToUnpack.createNewFile();
            }
            file.transferTo(fileToUnpack);

            File unGzippedFile = null;
            unGzippedFile = unGzip(fileToUnpack, new File(packageDir.toString()));
            unTar(unGzippedFile, new File(packageDir.toString()));
        } catch (IOException e) {
            throw new StorageException("Failed to create directory " + saveLocation.getFileName(), e);
        } catch (ArchiveException e) {
            throw new StorageException("Failed to untar packages" + saveLocation.getFileName(), e);
        } finally {
            cleanSpace(packageDir);
        }
    }

    private void cleanSpace(Path packageDir) {
        if (packageDir.toFile().isDirectory()) {
            for (File packageFile : packageDir.toFile().listFiles()) {
                boolean isTarFile = packageFile.getName().contains(".tar")
                        && !packageFile.getName().contains(".gz");
                if (isTarFile) {
                    packageFile.delete();
                }
            }
        }
    }

    private File unGzip(final File inputFile, final File outputDir) throws IOException {
        log.debug(String.format("Ungzipping %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));

        final File outputFile = new File(
                outputDir, inputFile.getName().substring(0, inputFile.getName().length() - 3));

        final GZIPInputStream in = new GZIPInputStream(new FileInputStream(inputFile));
        final FileOutputStream out = new FileOutputStream(outputFile);

        IOUtils.copy(in, out);

        in.close();
        out.close();
        inputFile.delete();
        return outputFile;
    }

    private void unTar(final File inputFile, final File outputDir) throws IOException, ArchiveException {
        log.debug(String.format("Untarring %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));
        final InputStream is = new FileInputStream(inputFile);
        final TarArchiveInputStream debInputStream =
                (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is);
        TarArchiveEntry entry = null;

        while ((entry = (TarArchiveEntry) debInputStream.getNextEntry()) != null) {
            final File outputFile = new File(outputDir, entry.getName());
            File outputFileParentDir = outputFile.getParentFile();

            if (!outputFileParentDir.exists()) {
                outputFileParentDir.mkdirs();
            }

            if (entry.isDirectory()) {
                if (!outputFile.exists()) {
                    if (!outputFile.mkdirs()) {
                        throw new IllegalStateException(
                                String.format("Couldn't create directory %s.", outputFile.getAbsolutePath()));
                    }
                }
            } else {
                final OutputStream outputFileStream = new FileOutputStream(outputFile);
                IOUtils.copy(debInputStream, outputFileStream);
                outputFileStream.close();
            }
        }
        debInputStream.close();
    }

    @Override
    public void storeAndDeleteFiles(SynchronizePythonRepositoryRequestBody request)
            throws FileNotFoundException, StorageException {
        final String repository = request.getRepository();
        final MultipartFile[] filesToUpload = request.getFilesToUpload();
        final String[] filesToDelete = request.getFilesToDelete();

        if (filesToUpload != null) store(filesToUpload, repository, request.getId());
        if (filesToDelete != null) delete(filesToDelete, repository, request.getId());
    }

    private void store(MultipartFile[] files, String repository, String id) {
        final Path saveLocation = ((repository != null) && (!repository.trim().isEmpty()))
                ? this.rootLocation.resolve(repository)
                : this.rootLocation;
        store(files, saveLocation, id);
    }

    public List<File> getRecentPackagesFromRepository(String repository) {
        final ArrayList<File> files = new ArrayList<>();
        final Path location = ((repository != null) && (!repository.trim().isEmpty()))
                ? this.rootLocation.resolve(repository)
                : this.rootLocation;
        getRecentPackagesFromRepository(files, location);
        return files;
    }

    public void getRecentPackagesFromRepository(List<File> files, Path location) {
        if (location.toFile().exists()) {
            for (File file : location.toFile().listFiles()) {
                if (file.isDirectory()) {
                    getRecentPackagesFromRepository(files, file.toPath());
                } else if (!excludedFiles.contains(file.getName())) {
                    if (!file.getName().equals(PACKAGES_INDEX_FILE_NAME)
                            && !file.getName().equals("VERSION")) {
                        files.add(file);
                    }
                }
            }
        }
    }

    public void emptyTrash(String repository, String requestId) throws EmptyTrashException {
        final Path trash = this.rootLocation.resolve(TRASH_PREFIX + requestId);
        try {
            if (Files.exists(trash)) FileUtils.forceDelete(trash.toFile());
        } catch (IOException e) {
            log.error("Could not delete trash directory!", e);
            throw new EmptyTrashException(repository);
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

    private void delete(String packageName, String repository, String requestId)
            throws FileNotFoundException, StorageException {
        Path location = ((repository != null) && (!repository.trim().isEmpty()))
                ? this.rootLocation.resolve(repository)
                : this.rootLocation;

        try {
            if ((packageName == null || packageName.isBlank())) {
                for (File packageFile : Objects.requireNonNull(location.toFile().listFiles())) {
                    if (!Files.exists(packageFile.toPath())) throw new FileNotFoundException();

                    moveToTrash(requestId, packageFile);
                }
            } else {
                location = location.resolve(packageName.substring(0, packageName.indexOf('/')));
                Path packageFilePath = location.resolve(packageName.substring(packageName.indexOf('/') + 1));

                if (!Files.exists(packageFilePath)) throw new FileNotFoundException(packageFilePath.toString());

                moveToTrash(requestId, packageFilePath.toFile());

                File parentDirectory = location.toFile();
                if (parentDirectory.isDirectory()) {
                    String[] filesInParentDirectory = parentDirectory.list();
                    if (filesInParentDirectory != null
                            && filesInParentDirectory.length == 1
                            && filesInParentDirectory[0].equals(PACKAGES_INDEX_FILE_NAME)) {
                        moveToTrash(
                                requestId,
                                location.resolve(filesInParentDirectory[0]).toFile());
                        parentDirectory.delete();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw e;
        } catch (MoveToTrashException e) {
            throw new StorageException("Could not delete package file", e);
        }
    }

    private void delete(String[] packageNames, String repository, String requestId)
            throws FileNotFoundException, StorageException {
        for (String packageName : packageNames) {
            delete(packageName, repository, requestId);
        }
    }
}

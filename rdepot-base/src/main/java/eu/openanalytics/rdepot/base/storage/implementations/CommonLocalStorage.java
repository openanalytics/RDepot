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
package eu.openanalytics.rdepot.base.storage.implementations;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.storage.exceptions.CreateFolderStructureException;
import eu.openanalytics.rdepot.base.storage.exceptions.CreateTemporaryFolderException;
import eu.openanalytics.rdepot.base.storage.exceptions.DeleteFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.DownloadFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.ExtractFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.GzipFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.InvalidSourceException;
import eu.openanalytics.rdepot.base.storage.exceptions.LinkFoldersException;
import eu.openanalytics.rdepot.base.storage.exceptions.Md5SumCalculationException;
import eu.openanalytics.rdepot.base.storage.exceptions.MoveFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.MovePackageSourceException;
import eu.openanalytics.rdepot.base.storage.exceptions.PackageFolderPopulationException;
import eu.openanalytics.rdepot.base.storage.exceptions.SourceFileDeleteException;
import eu.openanalytics.rdepot.base.storage.exceptions.SourceNotFoundException;
import eu.openanalytics.rdepot.base.storage.exceptions.WriteToWaitingRoomException;
import jakarta.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Storage class for use with local file system.
 * It provides basic features for moving resources around
 * and processing them without parsing the contents
 * (e.g. compression, checksum calculation etc.).
 * @param <R> Technology-specific {@link Repository} class.
 * @param <P> Technology-specific {@link Package} class.
 */
@Slf4j
public abstract class CommonLocalStorage<R extends Repository, P extends Package> implements Storage<R, P> {

    protected final String separator = FileSystems.getDefault().getSeparator();
    private final Random random = new Random();

    @Resource(name = "packageUploadDirectory")
    private File packageUploadDirectory;

    @Resource(name = "repositoryGenerationDirectory")
    private File repositoryGenerationDirectory;

    @Override
    public String writeToWaitingRoom(final MultipartFile fileData, final Repository repository)
            throws WriteToWaitingRoomException {
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

    /**
     * Creates a "current" symbolic link pointing at the latest generated repository.
     */
    protected File linkCurrentFolderToGeneratedFolder(Repository repository, String dateStamp)
            throws LinkFoldersException {
        return linkTwoFolders(
                repositoryGenerationDirectory.getAbsolutePath()
                        + separator
                        + repository.getId()
                        + separator
                        + dateStamp,
                repositoryGenerationDirectory.getAbsolutePath() + separator + repository.getId() + separator
                        + "current");
    }

    private File generateWaitingRoom(final File packageUploadDirectory, final Repository repository)
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
    public String moveToMainDirectory(final Package packageBag)
            throws InvalidSourceException, MovePackageSourceException {
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
            newDirectory = move(current.getParentFile(), mainDir);
        } catch (MoveFileException e) {
            if (mainDir.exists()) {
                try {
                    deleteFile(mainDir);
                } catch (DeleteFileException dfe) {
                    log.error(dfe.getMessage(), dfe);
                }
            }
            log.error(e.getMessage(), e);
            throw new MovePackageSourceException();
        }

        final String packageFilename = current.getName();
        try {
            deleteFile(current);
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

    /**
     * Creates directories where the repository will be populated for publication.
     */
    protected void createFolderStructureForGeneration(Repository repository, String dateStamp)
            throws CreateFolderStructureException {
        File dateStampFolder = null;
        try {
            dateStampFolder = createFolderStructure(repositoryGenerationDirectory.getAbsolutePath()
                    + separator
                    + repository.getId()
                    + separator
                    + dateStamp);

            createFolderStructure(getRepositoryGeneratedPath(dateStampFolder, separator));

        } catch (CreateFolderStructureException e) {
            if (dateStampFolder != null) {
                try {
                    deleteFile(dateStampFolder);
                } catch (DeleteFileException dfe) {
                    log.error(dfe.getMessage(), dfe);
                }
            }

            throw e;
        }
    }

    /**
     * Returns subdirectory for repository content, specific for Technology.
     * @return path to the subdirectory in the file system.
     */
    protected abstract String getRepositoryGeneratedPath(File dateStampFolder, String separator);

    @Override
    public String moveToTrashDirectory(Package packageBag) throws MovePackageSourceException {
        File trashDir = new File(packageUploadDirectory.getAbsolutePath() + separator
                + "trash" + separator + packageBag.getRepository().getId()
                + separator + random.nextInt(100000000));

        while (trashDir.exists()) {
            trashDir = new File(packageUploadDirectory.getAbsolutePath() + separator
                    + "trash" + separator + packageBag.getRepository().getId()
                    + separator + random.nextInt(100000000));
        }

        return moveSource(packageBag, trashDir.getAbsolutePath());
    }

    @Override
    public String moveSource(Package packageBag, String destinationDir) throws MovePackageSourceException {
        if (packageBag.getSource().isEmpty()) {
            throw new IllegalStateException("Package source path should never be empty. " + packageBag);
        }

        File current = new File(packageBag.getSource());

        if (!current.exists()) {
            throw new IllegalStateException(
                    "Source for package " + packageBag + " [" + packageBag.getSource() + "] not found.");
        }

        final File destinationDirFile = new File(destinationDir);
        File newDirectory;

        try {
            newDirectory = move(current.getParentFile(), destinationDirFile);
        } catch (MoveFileException e) {
            if (destinationDirFile.exists()) {
                try {
                    deleteFile(destinationDirFile);
                } catch (DeleteFileException dfe) {
                    log.error(dfe.getMessage(), dfe);
                }
            }
            log.error(e.getMessage(), e);
            throw new MovePackageSourceException();
        }

        String packageFilename = current.getName();
        try {
            deleteFile(current);
        } catch (DeleteFileException e) {
            log.error(e.getMessage(), e);
            throw new MovePackageSourceException();
            // TODO: #32971 And what about what already happened? Maybe it would be a better idea to reverse it?
        }
        return newDirectory.getAbsolutePath() + separator + packageFilename;
    }

    /**
     * Filter out packages that are already uploaded to the remote server.
     * @param remotePackages list of remote package names and versions
     * @param localPackages packages that are stored locally, to be published
     * @return list of {@link File} objects to upload to the remote server.
     */
    protected List<File> selectPackagesToUpload(List<String> remotePackages, List<P> localPackages) {
        List<File> toUpload = new ArrayList<>();

        for (Package packageBag : localPackages) {
            if (!remotePackages.contains(packageBag.getFileName())) {
                toUpload.add(new File(packageBag.getSource()));
            }
        }

        return toUpload;
    }

    /**
     * Populates every package in a generated directory.
     */
    protected void populatePackageFolder(List<P> packages, String folderPath) throws PackageFolderPopulationException {
        for (P packageBag : packages) {
            populatePackage(packageBag, folderPath);
        }
    }

    @Override
    public String extractTarGzPackageFile(String storedFilePath) throws ExtractFileException {
        log.debug("Extracting package file: {}", storedFilePath);
        final File storedFile = new File(storedFilePath);
        final File outputDir = storedFile.getParentFile();
        File unGzippedFile = null;
        List<String> filesInArchive;

        try {
            unGzippedFile = unGzip(storedFile, outputDir);
            filesInArchive = unTar(unGzippedFile, outputDir);
        } catch (IOException | ArchiveException e) {
            try {
                if (storedFile.getParentFile().exists())
                    deleteFile(storedFile.getParentFile().getAbsoluteFile());
            } catch (DeleteFileException dfe) {
                log.error(dfe.getMessage(), dfe);
            }
            log.error(e.getMessage(), e);
            throw new ExtractFileException();
        } finally {
            try {
                if (unGzippedFile != null && unGzippedFile.exists()) {
                    deleteFile(unGzippedFile);
                }
            } catch (DeleteFileException e) {
                log.error(e.getMessage(), e);
            }
        }

        return Path.of(outputDir.getAbsolutePath(), StringUtils.substringBefore(filesInArchive.get(0), "/"))
                .toString();
    }

    /**
     * Forcibly removes file from the File System.
     */
    protected void deleteFile(File file) throws DeleteFileException {
        if (file.exists()) {
            try {
                FileUtils.forceDelete(file);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new DeleteFileException();
            }
        }
    }

    /**
     * Forcibly cleans the directory.
     * If it does not exist or is not a directory, nothing will happen.
     */
    protected void cleanDirectory(File directory) throws DeleteFileException {
        if (directory.exists() && directory.isDirectory()) {
            try {
                FileUtils.cleanDirectory(directory);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new DeleteFileException();
            }
        }
    }

    /** Extract an input tar-file into an output file.
     * The output file is created in the output folder, having the same name
     * as the input file, minus the '.tar' extension.
     *
     * @param inputFile     the input .tar file
     * @param outputDir     the output directory file.
     */
    private List<String> unTar(final File inputFile, final File outputDir) throws IOException, ArchiveException {
        log.debug(String.format("Extracting %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));
        final InputStream is = new FileInputStream(inputFile);
        TarArchiveEntry entry;
        List<String> filesInArchive = new ArrayList<>();

        try (final TarArchiveInputStream debInputStream =
                new ArchiveStreamFactory().createArchiveInputStream("tar", is)) {
            while ((entry = debInputStream.getNextEntry()) != null) {
                filesInArchive.add(entry.getName());
                final File outputFile = new File(outputDir, entry.getName());
                File outputFileParentDir = outputFile.getParentFile();

                if (!outputFileParentDir.exists() && !outputFileParentDir.mkdirs()) {
                    throw new IllegalStateException(
                            String.format("Couldn't create directory %s.", outputFileParentDir.getAbsolutePath()));
                }

                if (entry.isDirectory()) {
                    if (!outputFile.exists() && !outputFile.mkdirs()) {
                        throw new IllegalStateException(
                                String.format("Couldn't create directory %s.", outputFile.getAbsolutePath()));
                    }
                } else {
                    try (OutputStream outputFileStream = new FileOutputStream(outputFile)) {
                        IOUtils.copy(debInputStream, outputFileStream);
                    }
                }
            }
        }

        if (filesInArchive.isEmpty()) throw new ArchiveException(MessageCodes.EMPTY_ARCHIVE);

        return filesInArchive;
    }
    /**
     * Extract an input gzip file into an output file.
     * The output file is created in the output folder, having the same name
     * as the input file, minus the '.gz' extension. 	 *
     * @param inputFile     the input .gz file
     * @param outputDir     the output directory file.
     * @return  The {@link File} with the extracted content.
     */
    private File unGzip(final File inputFile, final File outputDir) throws IOException {
        log.debug(String.format("Extracting %s to dir %s.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath()));

        final File outputFile = new File(
                outputDir, inputFile.getName().substring(0, inputFile.getName().length() - 3));

        final GZIPInputStream in = new GZIPInputStream(new FileInputStream(inputFile));
        final FileOutputStream out = new FileOutputStream(outputFile);

        IOUtils.copy(in, out);

        in.close();
        out.close();

        return outputFile;
    }

    /**
     * Moves file or directory to destination.
     * If destination path does not exist, subdirectories are created.
     */
    protected File move(File source, File destination) throws MoveFileException {
        try {
            if (!destination.getParentFile().exists()) createFolderStructure(destination.getParent());

            log.debug("Moving from {} to {}", source.getAbsolutePath(), destination.getAbsolutePath());

            if (source.isDirectory()) {
                FileUtils.moveDirectory(source, destination);
            } else {
                FileUtils.moveFile(source, destination);
            }
        } catch (NullPointerException | IOException | CreateFolderStructureException e) {
            log.error(e.getMessage(), e);
            throw new MoveFileException();
        }

        return destination;
    }

    /**
     * This method creates a new directory in application's storage or overrides the existing one.
     * @param path Path to the directory
     * @return Created directory
     */
    protected File createFolderStructure(String path) throws CreateFolderStructureException {
        File newFolder = new File(path);
        try {
            if (!newFolder.exists()) {
                Files.createDirectories(newFolder.toPath());
            } else {
                FileUtils.cleanDirectory(newFolder);
            }

        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CreateFolderStructureException();
        }
        return newFolder;
    }

    @Override
    public void removeFileIfExists(final String sourceFilePath) throws DeleteFileException {
        deleteFile(new File(sourceFilePath));
    }

    @Override
    public void removePackageSource(String path) throws SourceFileDeleteException {
        final File targzFile = new File(path);
        log.info("Removing source file: {}", targzFile.getAbsolutePath());
        try {
            deleteFile(targzFile.getParentFile());
        } catch (DeleteFileException e) {
            log.error(e.getMessage(), e);
            throw new SourceFileDeleteException();
        }
    }

    /**
     * This method creates a compressed copy of a given file.
     * @param source Source file
     */
    protected void gzipFile(final File source) throws GzipFileException {
        File destination = new File(source.getAbsolutePath() + ".gz");
        try (GzipCompressorOutputStream compressor =
                new GzipCompressorOutputStream(new FileOutputStream(destination))) {
            try (FileInputStream inputSource = new FileInputStream(source)) {
                IOUtils.copy(inputSource, compressor);
            }
        } catch (IOException e) {
            throw new GzipFileException();
        }
    }

    /**
     * This method calculates MD5 sum of a file.
     */
    protected String calculateMd5Sum(File target) throws Md5SumCalculationException {
        try (InputStream is = new FileInputStream(target)) {
            return DigestUtils.md5DigestAsHex(is);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new Md5SumCalculationException();
        }
    }

    /**
     * This method creates a symbolic link to the given directory.
     * @param targetPath Path to the directory we are linking to
     * @param linkPath Path to the link
     * @return created link
     */
    protected File linkTwoFolders(String targetPath, String linkPath) throws LinkFoldersException {
        Path link = Paths.get(linkPath);

        try {
            if (Files.exists(link)) {
                Files.delete(link);
            }
            Files.createSymbolicLink(link, Paths.get(targetPath));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new LinkFoldersException();
        }

        return link.toFile();
    }

    /**
     * Downloads file from a given URL and saves it in a temporary directory.
     */
    public File downloadFile(String url) throws DownloadFileException {
        File tempFile;
        try {
            tempFile = Files.createTempFile(null, null).toFile();
        } catch (IOException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new DownloadFileException(url);
        }
        downloadFileToDestination(url, tempFile);
        return tempFile;
    }

    private void downloadFileToDestination(String url, File destination) throws DownloadFileException {
        try (final CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            final HttpGet httpGet = new HttpGet(url);
            final HttpResponse response = httpClient.execute(httpGet);
            final HttpEntity entity = response.getEntity();

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new DownloadFileException(url);
            }

            if (entity != null) {
                try (FileOutputStream os = new FileOutputStream(destination)) {
                    entity.writeTo(os);
                }
            }
        } catch (IOException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new DownloadFileException(url);
        }
    }

    /**
     * Downloads file from a given url.
     */
    public MultipartFile downloadFile(String url, File destination) throws DownloadFileException {
        downloadFileToDestination(url, destination);
        return new MultipartFile() {

            @Override
            public void transferTo(@NonNull File dest) throws IOException, IllegalStateException {
                FileCopyUtils.copy(getInputStream(), Files.newOutputStream(dest.toPath()));
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public long getSize() {
                try {
                    return Files.size(destination.toPath());
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    return -1;
                }
            }

            @Override
            public @NonNull String getOriginalFilename() {
                String[] tokens = url.split("/");
                if (tokens.length == 0) return "downloaded";
                return tokens[tokens.length - 1];
            }

            @Override
            public @NonNull String getName() {
                return getOriginalFilename();
            }

            @Override
            public @NonNull InputStream getInputStream() throws IOException {
                return new FileInputStream(destination);
            }

            @Override
            public String getContentType() {
                return "application/gzip";
            }

            @Override
            public byte @NonNull [] getBytes() throws IOException {
                return FileUtils.readFileToByteArray(destination);
            }
        };
    }

    /**
     * Creates temporary folder with a given prefix.
     */
    public File createTemporaryFolder(String prefix) throws CreateTemporaryFolderException {
        try {
            return Files.createTempDirectory(prefix).toFile();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CreateTemporaryFolderException(prefix);
        }
    }

    /**
     * Reads file to a byte array.
     */
    protected byte[] readFile(File file) throws IOException {
        final FileSystemResource fsResource = new FileSystemResource(file);
        if (fsResource.exists()) {
            return Files.readAllBytes(fsResource.getFile().toPath());
        } else {
            throw new FileNotFoundException();
        }
    }

    @Override
    public byte[] getPackageInBytes(P packageBag) throws SourceNotFoundException {
        try {
            return readFile(new File(packageBag.getSource()));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new SourceNotFoundException();
        }
    }
}

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
package eu.openanalytics.rdepot.base.storage.implementations;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.storage.Storage;
import eu.openanalytics.rdepot.base.storage.exceptions.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
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
import org.apache.commons.io.input.ReversedLinesFileReader;
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
 * @param <P> Technology-specific {@link Package} class.
 */
@Slf4j
public abstract class CommonLocalStorage<P extends Package> implements Storage<P> {

    protected final String separator = FileSystems.getDefault().getSeparator();

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
    public void deleteFile(File file) throws DeleteFileException {
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
    public void cleanDirectory(File directory) throws DeleteFileException {
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
        log.debug("Extracting tar file {} to dir {}.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath());
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
        log.debug("Extracting gzip file {} to dir {}.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath());

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
    @Override
    public File move(File source, File destination) throws MoveFileException {
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
    public File createFolderStructure(String path) throws CreateFolderStructureException {
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
    public void gzipFile(final String source) throws GzipFileException {
        final File sourceFile = new File(source);
        File destination = new File(sourceFile.getAbsolutePath() + ".gz");
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
    @Override
    public String calculateMd5Sum(String targetPath) throws Md5SumCalculationException {
        final File target = new File(targetPath);
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
    @Override
    public File linkTwoFolders(String targetPath, String linkPath) throws LinkFoldersException {
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

    protected void downloadFileToDestination(String url, File destination) throws DownloadFileException {
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
        return returnMultipartFile(url, destination);
    }

    public MultipartFile returnMultipartFile(String url, File destination) {
        return new MultipartFile() {

            @Override
            public void transferTo(@NonNull File destination) throws IOException, IllegalStateException {
                FileCopyUtils.copy(getInputStream(), Files.newOutputStream(destination.toPath()));
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
    public byte[] readFile(File file) throws IOException {
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

    @Override
    public abstract void setCheckSum(P packageBag) throws CheckSumCalculationException;

    @Override
    public void appendText(String content, String path) throws IOException {
        final Path resolved = Paths.get(path);
        if (Files.isDirectory(resolved)) {
            throw new IllegalArgumentException(path + " is a directory");
        }
        if (!Files.exists(resolved.getParent())) {
            Files.createDirectories(resolved.getParent());
        }
        Files.writeString(
                resolved, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    @Override
    public void removeContentFromEnd(String content, String path) throws IOException {
        content = content.trim(); // Remove whitespace from the beginning and end
        final String[] contentLines = content.split("\n"); // Split into separate lines
        int charCounter = contentLines.length - 1; // Initialize with the expected number of newline characters
        try (ReversedLinesFileReader reader = ReversedLinesFileReader.builder()
                .setFile(path)
                .setCharset(StandardCharsets.UTF_8)
                .get()) { // Open the file from the end
            int i = contentLines.length - 1; // Set the iteration at the end of expected content
            String fileLine;
            while ((fileLine = reader.readLine()) != null) {
                final String trimmedFileLine = fileLine.trim();
                final int whitespace = fileLine.length() - trimmedFileLine.length();
                if (i >= 0
                        && contentLines[i]
                                .trim()
                                .equals(fileLine.trim())) { // Remove whitespace, stop of all lines were matched
                    i--;
                    charCounter += fileLine.length() + whitespace + contentLines.length - 1 + i;
                } else break;
            }
            if (i >= 0) return; // If all searched lines matched, 'i' should be exactly -1
        }
        try (final RandomAccessFile raf = new RandomAccessFile(path, "rw")) {
            raf.setLength(raf.length() - charCounter);
        }
    }

    @Override
    public boolean exists(String path) {
        return Paths.get(path).toFile().exists();
    }
}

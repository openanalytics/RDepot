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
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.storage.LocalStorage;
import eu.openanalytics.rdepot.base.storage.exceptions.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.util.DigestUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * LocalStorage class for use with local file system.
 * It provides basic features for moving resources around
 * and processing them without parsing the contents
 * (e.g. compression, checksum calculation etc.).
 * @param <P> Technology-specific {@link Package} class.
 */
@Slf4j
public abstract class CommonFSLocalStorage<P extends Package> implements LocalStorage<P> {

    protected final String separator = FileSystems.getDefault().getSeparator();

    @Override
    public String extractTarGzPackageFile(File storedFile) throws ExtractFileException {
        log.debug("Extracting package file: {}", storedFile.toPath().toAbsolutePath());
        final File outputDir = storedFile.getParentFile();
        File unGzippedFile = null;
        List<String> filesInArchive;

        try {
            unGzippedFile = unGzip(storedFile, outputDir);
            filesInArchive = unTar(unGzippedFile, outputDir);
        } catch (IOException | ArchiveException e) {
            try {
                if (storedFile.getParentFile().exists())
                    deleteFile(storedFile.getParentFile().getAbsolutePath());
            } catch (DeleteFileException dfe) {
                log.error(dfe.getMessage(), dfe);
            }
            log.error(e.getMessage(), e);
            throw new ExtractFileException();
        } finally {
            try {
                if (unGzippedFile != null && unGzippedFile.exists()) {
                    deleteFile(unGzippedFile.getAbsolutePath());
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
    @Override
    public void deleteFile(String filePath) throws DeleteFileException {
        final File file = new File(filePath);
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
    public void cleanDirectory(String directoryPath) throws DeleteFileException {
        final File directory = new File(directoryPath);
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
     * This method creates a new directory in application's localStorage or overrides the existing one.
     * @param path Path to the directory
     * @return Created directory
     */
    public String createFolderStructure(String path) throws CreateFolderStructureException {
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
        return newFolder.getAbsolutePath();
    }

    @Override
    public void removeFileIfExists(final String sourceFilePath) throws DeleteFileException {
        deleteFile(sourceFilePath);
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
    public String linkTwoFolders(String targetPath, String linkPath) throws LinkFoldersException {
        final Path link = Paths.get(linkPath);

        try {
            if (Files.exists(link)) {
                Files.delete(link);
            }
            Files.createSymbolicLink(link, Paths.get(targetPath));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new LinkFoldersException();
        }

        return link.toAbsolutePath().toString();
    }

    /**
     * Downloads file from a given URL and saves it in a temporary directory.
     */
    public String downloadFile(String url) throws DownloadFileException {
        final File tempFile;
        try {
            tempFile = Files.createTempFile(null, null).toFile();
        } catch (IOException e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage(), e);
            throw new DownloadFileException(url);
        }
        downloadFileToDestination(url, tempFile);
        return tempFile.getAbsolutePath();
    }

    protected void downloadFileToDestination(String url, File destination) throws DownloadFileException {
        try (final CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            final URIBuilder uriBuilder = new URIBuilder(url);
            final HttpGet httpGet = new HttpGet(uriBuilder.build());
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
        } catch (IOException | URISyntaxException e) {
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

    @Override
    public abstract void setCheckSum(P packageBag, File packageFile) throws CheckSumCalculationException;

    @Override
    public void appendText(String content, String path) throws ContentEditException {
        final Path resolved = Paths.get(path);
        if (Files.isDirectory(resolved)) {
            throw new IllegalArgumentException(path + " is a directory");
        }

        try {
            if (!Files.exists(resolved.getParent())) {
                Files.createDirectories(resolved.getParent());
            }

            Files.writeString(
                    resolved, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ContentEditException(resolved.toAbsolutePath().toString());
        }
    }

    /**
     * Removes the given trailing content from a file at the given file path.
     * Used for HTML templates generation to prepare for inserting
     * additional package/repository information at the end of a file,
     * when creating index HTML pages for example.
     * No changes are made to the file if the file does not end with the given content.
     * Example:
     * * A file index.html with contents "<tbody><tr>package1</tr></tbody>"
     * Then executing:
     * <pre>
     * removeContentFromEnd("</tbody>", "/index.html");
     * </pre>
     * will result in the file index.html now having contents "<tbody><tr>package1</tr>"
     * which makes it ready for appending additional packages
     * before closing again with "</tbody>".
     *
     * @param content The content to remove from the end of the given file
     * @param path Path to the file from which the trailing content should be removed
     * @throws ContentEditException When reading and writing file contents fails.
     */
    @Override
    public void removeContentFromEnd(String content, String path) throws ContentEditException {
        Path file = Path.of(path);
        try {
            String text = Files.readString(file, StandardCharsets.UTF_8);
            Optional<String> newText = removeTrailingContent(content, text);
            if (newText.isPresent()) {
                Files.writeString(file, newText.get(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ContentEditException(file.toAbsolutePath().toString());
        }
    }

    /**
     * Removes the trailing contents from a String
     * if the String actually ends with the given content.
     * Returns a new String with the given text without the trailing contents,
     * otherwise an empty Optional is returned.
     * First strips leading and trailing white space characters from the given content
     * and strips any trailing white space characters from the given text
     * before testing if the given text ends with the given content.
     * Examples:
     * <pre>
     * removeTrailingContent("</tbody>", "<tbody><tr>package1</tr></tbody>");
     * </pre>
     * returns "<tbody><tr>package1</tr>".
     * <pre>
     * removeTrailingContent("</tbody>", "<tbody><tr>package1</tr>");
     * </pre>
     * returns an empty Optional.
     *
     * @param toRemove The String to remove from the end of the given text
     * @param text String from which the trailing contents should be removed
     */
    public Optional<String> removeTrailingContent(String toRemove, String text) {
        String trailing = toRemove.strip();
        if (!text.stripTrailing().endsWith(trailing)) {
            return Optional.empty();
        }
        int index = text.lastIndexOf(trailing);
        if (index >= 0) {
            return Optional.of(text.substring(0, index));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean exists(String path) {
        return Paths.get(path).toFile().exists();
    }

    @Override
    public void removeEmptyLinesFromEnd(String path) throws ContentEditException {
        Path file = Path.of(path);
        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            content = content.replaceFirst("(\\R)+\\z", "");
            Files.writeString(file, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ContentEditException(file.toAbsolutePath().toString());
        }
    }
}

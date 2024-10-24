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
package eu.openanalytics.rdepot.r.storage.implementations;

import eu.openanalytics.rdepot.base.PropertiesParser;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.storage.exceptions.*;
import eu.openanalytics.rdepot.base.storage.implementations.CommonLocalStorage;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.entities.Vignette;
import eu.openanalytics.rdepot.r.storage.RStorage;
import eu.openanalytics.rdepot.r.storage.exceptions.GenerateManualException;
import eu.openanalytics.rdepot.r.storage.exceptions.GetReferenceManualException;
import eu.openanalytics.rdepot.r.storage.exceptions.ReadPackageVignetteException;
import eu.openanalytics.rdepot.r.storage.exceptions.ReadRPackageDescriptionException;
import eu.openanalytics.rdepot.r.storage.utils.PopulatedRepositoryContent;
import eu.openanalytics.rdepot.r.synchronization.SynchronizeRepositoryRequestBody;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Local storage implementation for R.
 */
@Slf4j
@Component
public class RLocalStorage extends CommonLocalStorage<RRepository, RPackage> implements RStorage {

    @Resource(name = "repositoryGenerationDirectory")
    private File repositoryGenerationDirectory;

    @Value("${repository-snapshots}")
    private String snapshot;

    private static final String PACKAGES = "PACKAGES";
    private static final String ARCHIVE_FOLDER = "Archive";
    private static final String LATEST_FOLDER = "latest";
    private static final String CONTRIB_FOLDER = "contrib";

    @Override
    public Properties getPropertiesFromExtractedFile(final String extractedFile)
            throws ReadPackageDescriptionException {
        try {
            return new PropertiesParser(new File(extractedFile + separator + "DESCRIPTION"));
        } catch (IOException e) {
            try {
                deleteFile(new File(extractedFile).getParentFile());
            } catch (DeleteFileException dfe) {
                log.error(dfe.getMessage(), dfe);
            }
            log.error(e.getMessage(), e);
            throw new ReadRPackageDescriptionException();
        }
    }

    public String getRepositoryGeneratedPath(File dateStampFolder, String separator) {
        return dateStampFolder.getAbsolutePath() + separator + "src" + separator + CONTRIB_FOLDER;
    }

    private void createTemporaryFoldersForLatestAndArchive(String path) throws CreateFolderStructureException {
        File latest = createFolderStructure(path + separator + LATEST_FOLDER);
        File archive = createFolderStructure(path + separator + ARCHIVE_FOLDER);

        try {
            File packagesLatest =
                    Files.createFile(latest.toPath().resolve(PACKAGES)).toFile();
            File packagesArchive =
                    Files.createFile(archive.toPath().resolve(PACKAGES)).toFile();

            gzipFile(packagesLatest);
            gzipFile(packagesArchive);
        } catch (IOException | GzipFileException e) {
            log.error("Could not create PACKAGES file", e);
            throw new CreateFolderStructureException();
        }
    }

    private void populateGeneratedFolder(List<RPackage> packages, RRepository repository, String dateStamp)
            throws PackageFolderPopulationException {
        String folderPath = repositoryGenerationDirectory.getAbsolutePath()
                + separator + repository.getId()
                + separator + dateStamp
                + separator + "src"
                + separator + CONTRIB_FOLDER;

        populatePackageFolder(packages, folderPath);
    }

    @Override
    public SynchronizeRepositoryRequestBody buildSynchronizeRequestBody(
            PopulatedRepositoryContent populatedRepositoryContent,
            List<String> remoteLatestPackages,
            List<String> remoteArchivePackages,
            RRepository repository,
            String versionBefore) {
        final List<File> latestToUpload =
                selectPackagesToUpload(remoteLatestPackages, populatedRepositoryContent.getLatestPackages());

        final File currentDirectory = new File(repositoryGenerationDirectory.getAbsolutePath() + separator
                + repository.getId()
                + separator + "current"
                + separator + "src" + separator + CONTRIB_FOLDER + separator + LATEST_FOLDER);
        final File packagesFile = new File(currentDirectory.getAbsolutePath() + separator + PACKAGES);
        final File packagesGzFile = new File(currentDirectory.getAbsolutePath() + separator + "PACKAGES.gz");

        final List<String> latestToDelete =
                selectPackagesToDelete(remoteLatestPackages, populatedRepositoryContent.getLatestPackages());
        final List<File> archiveToUpload =
                selectPackagesToUpload(remoteArchivePackages, populatedRepositoryContent.getArchivePackages());

        final File archiveDirectory = new File(repositoryGenerationDirectory.getAbsolutePath() + separator
                + repository.getId()
                + separator + "current"
                + separator + "src" + separator + CONTRIB_FOLDER + separator + ARCHIVE_FOLDER);
        final File packagesFileFromArchive = new File(archiveDirectory.getAbsolutePath() + separator + PACKAGES);
        final File packagesGzFileFromArchive = new File(archiveDirectory.getAbsolutePath() + separator + "PACKAGES.gz");

        final List<String> archiveToDelete =
                selectPackagesToDelete(remoteArchivePackages, populatedRepositoryContent.getArchivePackages());

        final Map<String, String> checksums = getChecksumsForPopulatedContent(populatedRepositoryContent);

        try {
            checksums.put("recent/PACKAGES", calculateMd5Sum(packagesFile));
            checksums.put("recent/PACKAGES.gz", calculateMd5Sum(packagesGzFile));
            checksums.put("archive/PACKAGES", calculateMd5Sum(packagesFileFromArchive));
            checksums.put("archive/PACKAGES.gz", calculateMd5Sum(packagesGzFileFromArchive));
        } catch (Md5SumCalculationException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Could not build synchronize request body due to storage malfunction.");
        }

        return new SynchronizeRepositoryRequestBody(
                latestToUpload,
                archiveToUpload,
                latestToDelete,
                archiveToDelete,
                versionBefore,
                null,
                packagesFile,
                packagesGzFile,
                packagesFileFromArchive,
                packagesGzFileFromArchive,
                checksums);
    }

    private Map<String, String> getChecksumsForPopulatedContent(@NotNull PopulatedRepositoryContent content) {
        final Map<String, String> checksums = new HashMap<>();

        content.getLatestPackages().forEach(p -> checksums.put(sourceToKey(p), p.getMd5sum()));
        content.getArchivePackages().forEach(p -> checksums.put(sourceToKey(p), p.getMd5sum()));

        return checksums;
    }

    private String sourceToKey(@NotNull RPackage packageBag) {
        final String[] tokens = packageBag.getSource().split(separator);
        if (tokens.length < 1) {
            throw new IllegalStateException("Invalid source detected for package: " + packageBag);
        }

        return tokens[tokens.length - 1];
    }

    protected List<String> selectPackagesToDelete(List<String> remotePackages, List<RPackage> localPackages) {
        List<String> toDelete = new ArrayList<>();

        for (String packageName : remotePackages) {
            boolean found = false;
            for (Package packageBag : localPackages) {
                if (packageBag.getFileName().equals(packageName)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                toDelete.add(packageName);
            }
        }

        return toDelete;
    }

    @Override
    public PopulatedRepositoryContent organizePackagesInStorage(
            String dateStamp,
            List<RPackage> packages,
            List<RPackage> latestPackages,
            List<RPackage> archivePackages,
            RRepository repository)
            throws OrganizePackagesException {
        try {
            createFolderStructureForGeneration(repository, dateStamp);
            populateGeneratedFolder(packages, repository, dateStamp);

            final File target = linkCurrentFolderToGeneratedFolder(repository, dateStamp);
            final Path repoPath = target.toPath().resolve("src").resolve(CONTRIB_FOLDER);
            final String latestFolderPath = repoPath + separator + LATEST_FOLDER;
            final String archiveFolderPath = repoPath + separator + ARCHIVE_FOLDER;

            createTemporaryFoldersForLatestAndArchive(repoPath.toString());
            populatePackageFolder(latestPackages, latestFolderPath);
            populatePackageFolder(archivePackages, archiveFolderPath);

            return new PopulatedRepositoryContent(latestPackages, archivePackages, latestFolderPath, archiveFolderPath);
        } catch (CreateFolderStructureException | PackageFolderPopulationException | LinkFoldersException e) {
            log.error(e.getMessage(), e);
            throw new OrganizePackagesException();
        }
    }

    @Override
    public void populatePackage(RPackage packageBag, String folderPath) throws PackageFolderPopulationException {
        if (packageBag.isActive()) {
            String targetFilePath = packageBag.getSource();
            String destinationFilePath =
                    folderPath + separator + packageBag.getName() + "_" + packageBag.getVersion() + ".tar.gz";

            try {
                File packagesFile = new File(folderPath + separator + PACKAGES);

                Files.copy(new File(targetFilePath).toPath(), new File(destinationFilePath).toPath());
                if (!packageBag.getMd5sum().equals(calculateMd5Sum(new File(destinationFilePath)))) {
                    throw new Md5MismatchException();
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter(packagesFile, true));
                writer.append(generatePackageString(packageBag));
                writer.close();

                gzipFile(packagesFile);
            } catch (IOException | GzipFileException | Md5MismatchException | Md5SumCalculationException e) {
                log.error("{}: {}", e.getClass(), e.getMessage());
                throw new PackageFolderPopulationException();
            }
        }
    }

    private String generatePackageString(RPackage packageBag) {
        final StringBuilder packageString = new StringBuilder(500);
        final String lineSeparator = System.lineSeparator();

        packageString.append("Package: ").append(separateLines(packageBag.getName(), lineSeparator)).append(lineSeparator);
        packageString.append("Version: ").append(separateLines(packageBag.getVersion(), lineSeparator)).append(lineSeparator);
        if (packageBag.getDepends() != null && !packageBag.getDepends().trim().isEmpty())
            packageString.append("Depends: ").append(separateLines(packageBag.getDepends(), lineSeparator)).append(lineSeparator);
        if (packageBag.getImports() != null && !packageBag.getImports().trim().isEmpty())
            packageString.append("Imports: ").append(separateLines(packageBag.getImports(), lineSeparator)).append(lineSeparator);
        if (packageBag.getSuggests() != null && !packageBag.getSuggests().trim().isEmpty())
            packageString.append("Suggests: ").append(separateLines(packageBag.getSuggests(), lineSeparator)).append(lineSeparator);
        packageString.append("License: ").append(separateLines(packageBag.getLicense(), lineSeparator)).append(lineSeparator);
        if (packageBag.getLinkingTo() != null && !packageBag.getLinkingTo().isEmpty())
            packageString.append("LinkingTo: ").append(separateLines(packageBag.getLinkingTo(), lineSeparator));
        if (packageBag.getEnhances() != null && !packageBag.getEnhances().isEmpty())
            packageString.append("Enhances: ").append(separateLines(packageBag.getEnhances(), lineSeparator));
        if (packageBag.getPriority() != null && !packageBag.getPriority().isEmpty())
            packageString.append("Priority: ").append(separateLines(packageBag.getPriority(), lineSeparator));
        packageString.append("MD5Sum: ").append(separateLines(packageBag.getMd5sum(), lineSeparator)).append(lineSeparator);
        packageString
                .append("NeedsCompilation: ")
                .append(packageBag.isNeedsCompilation() ? "yes" : "no")
                .append(lineSeparator);
        packageString.append(lineSeparator);

        return packageString.toString();
    }

    private String separateLines(String lines, String lineSeparator) {
        return lines.replace("\\n", lineSeparator);
    }

    @Override
    public void cleanUpAfterSynchronization(PopulatedRepositoryContent populatedRepositoryContent)
            throws CleanUpAfterSynchronizationException {
        try {
            deleteFile(new File(populatedRepositoryContent.getLatestDirectoryPath()));
            deleteFile(new File(populatedRepositoryContent.getArchiveDirectoryPath()));

            if (!Boolean.parseBoolean(snapshot)) {
                cleanDirectory(repositoryGenerationDirectory);
            }
        } catch (DeleteFileException e) {
            log.error(e.getMessage(), e);
            throw new CleanUpAfterSynchronizationException();
        }
    }

    @Override
    public byte[] getReferenceManual(RPackage packageBag) throws GetReferenceManualException {
        final String manualPath = new File(packageBag.getSource()).getParent()
                + separator + packageBag.getName()
                + separator + packageBag.getName() + ".pdf";
        final File manualFile = new File(manualPath);

        try {
            return readFile(manualFile);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new GetReferenceManualException(e);
        }
    }

    @Override
    public List<Vignette> getAvailableVignettes(RPackage packageBag) {
        if (packageBag == null) {
            return List.of();
        }
        final List<Vignette> vignettes = new ArrayList<>();

        File vignettesFolder = new File(
                new File(packageBag.getSource()).getParent(),
                packageBag.getName() + separator + "inst" + separator + "doc" + separator);

        File[] vignetteFiles = new File[0];
        if (vignettesFolder.exists() && vignettesFolder.isDirectory()) {
            vignetteFiles = vignettesFolder.listFiles((File dir, String name) -> (name != null
                    && (name.toLowerCase().endsWith(".html")
                            || name.toLowerCase().endsWith(".pdf"))));
        }

        for (File vignetteFile : ArrayUtils.nullToEmpty(vignetteFiles, File[].class)) {
            if (FilenameUtils.getExtension(vignetteFile.getName()).equals("html")) {
                try {
                    Document htmlDoc = Jsoup.parse(vignetteFile, "UTF-8");

                    vignettes.add(new Vignette(htmlDoc.title(), vignetteFile.getName()));
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            } else {
                vignettes.add(new Vignette(FilenameUtils.getBaseName(vignetteFile.getName()), vignetteFile.getName()));
            }
        }
        return vignettes;
    }

    @Override
    public byte[] readVignette(RPackage packageBag, String filename) throws ReadPackageVignetteException {
        final String vignetteFilename = new File(packageBag.getSource()).getParent()
                + separator + packageBag.getName()
                + separator + "inst" + separator + "doc"
                + separator + filename;
        final File file = new File(vignetteFilename);

        try {
            return readFile(file);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ReadPackageVignetteException(e);
        }
    }

    @Override
    public void calculateCheckSum(RPackage packageBag) throws CheckSumCalculationException {
        log.debug("Calculating checksum for package: {}", packageBag.toString());
        try {
            packageBag.setMd5sum(DigestUtils.md5Hex(new FileInputStream(packageBag.getSource())));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CheckSumCalculationException();
        }
    }

    @Override
    public void generateManual(RPackage packageBag) throws GenerateManualException {
        final File targzFile = new File(packageBag.getSource());

        if (!targzFile.exists()
                || targzFile.getParentFile() == null
                || !targzFile.getParentFile().exists()) {
            log.error("Invalid package source!");
            throw new GenerateManualException(packageBag);
        }
        final String packageName = packageBag.getName().replaceAll("[^a-zA-Z0-9-_]", "");
        final File manualPdf = new File(targzFile.getParent(), packageName + separator + packageName + ".pdf");

        if (manualPdf.getParentFile() != null && manualPdf.getParentFile().exists()) {
            if (manualPdf.exists()) {
                log.warn("Manual already exists!");
                return;
            }

            ProcessBuilder pb = new ProcessBuilder("R");

            pb.command().add("CMD");
            pb.command().add("Rd2pdf");
            pb.command().add("--no-preview");
            pb.command().add("--title=" + packageName);
            pb.command().add("--output=" + packageName + ".pdf");
            pb.command().add(".");
            pb.directory(manualPdf.getParentFile()).redirectErrorStream(true);

            Process process = null;
            try {
                process = pb.start();

                String outputLine;
                log.debug("Rd2pdf output: ");
                try (BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    while ((outputLine = out.readLine()) != null) log.debug(outputLine.replaceAll("[\r\n]", ""));
                }

                int exitValue = process.waitFor();
                if (exitValue != 0) {
                    log.error("Rd2pdf failed with exit code: {}", exitValue);
                    throw new GenerateManualException(packageBag);
                }
            } catch (IOException | InterruptedException e) {
                log.error(e.getMessage(), e);
                throw new GenerateManualException(packageBag);
            } finally {
                if (process != null && process.isAlive()) process.destroyForcibly();
            }
        }
    }
}

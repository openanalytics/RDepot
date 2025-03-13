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
package eu.openanalytics.rdepot.r.storage.implementations;

import eu.openanalytics.rdepot.base.PropertiesParser;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.storage.exceptions.CheckSumCalculationException;
import eu.openanalytics.rdepot.base.storage.exceptions.CleanUpAfterSynchronizationException;
import eu.openanalytics.rdepot.base.storage.exceptions.CreateFolderStructureException;
import eu.openanalytics.rdepot.base.storage.exceptions.DeleteFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.GzipFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.LinkFoldersException;
import eu.openanalytics.rdepot.base.storage.exceptions.Md5MismatchException;
import eu.openanalytics.rdepot.base.storage.exceptions.Md5SumCalculationException;
import eu.openanalytics.rdepot.base.storage.exceptions.OrganizePackagesException;
import eu.openanalytics.rdepot.base.storage.exceptions.PackageFolderPopulationException;
import eu.openanalytics.rdepot.base.storage.exceptions.ReadPackageDescriptionException;
import eu.openanalytics.rdepot.base.storage.implementations.CommonLocalStorage;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.entities.Vignette;
import eu.openanalytics.rdepot.r.storage.RStorage;
import eu.openanalytics.rdepot.r.storage.exceptions.GenerateManualException;
import eu.openanalytics.rdepot.r.storage.exceptions.GeneratePackagesFileException;
import eu.openanalytics.rdepot.r.storage.exceptions.GetReferenceManualException;
import eu.openanalytics.rdepot.r.storage.exceptions.ReadPackageVignetteException;
import eu.openanalytics.rdepot.r.storage.exceptions.ReadRPackageDescriptionException;
import eu.openanalytics.rdepot.r.storage.utils.PopulatedRepositoryContent;
import eu.openanalytics.rdepot.r.synchronization.SynchronizeRepositoryRequestBody;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
    private static final String PACKAGES_GZ = "PACKAGES.gz";
    private static final String ARCHIVE_FOLDER = "Archive";
    private static final String LATEST_FOLDER = "latest";
    private static final String CONTRIB_FOLDER = "contrib";
    private static final String SRC_FOLDER = "src";
    private static final String CURRENT_FOLDER = "current";
    private static final String BIN_FOLDER = "bin";
    private static final String LINUX_FOLDER = "linux";

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
        return dateStampFolder.getAbsolutePath() + separator + SRC_FOLDER + separator + CONTRIB_FOLDER;
    }

    public String getRepositoryForBinaryGeneratedPath(File dateStampFolder, String separator, String binPath) {
        return dateStampFolder.getAbsolutePath()
                + separator
                + BIN_FOLDER
                + separator
                + LINUX_FOLDER
                + separator
                + binPath;
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

    private void populateGeneratedSourceFolder(List<RPackage> packages, RRepository repository, String dateStamp)
            throws PackageFolderPopulationException {
        String folderPath = repositoryGenerationDirectory.getAbsolutePath()
                + separator
                + repository.getId()
                + separator
                + dateStamp
                + separator
                + SRC_FOLDER
                + separator
                + CONTRIB_FOLDER;

        populatePackageFolder(packages, folderPath);
    }

    private void populateGeneratedBinaryFolder(
            List<RPackage> packages, RRepository repository, String dateStamp, String binFolderPath)
            throws PackageFolderPopulationException {
        String folderPath = repositoryGenerationDirectory.getAbsolutePath()
                + separator
                + repository.getId()
                + separator
                + dateStamp
                + separator
                + BIN_FOLDER
                + separator
                + LINUX_FOLDER
                + separator
                + binFolderPath;

        populatePackageFolder(packages, folderPath);
    }

    @Override
    public SynchronizeRepositoryRequestBody buildSynchronizeRequestBody(
            PopulatedRepositoryContent populatedRepositoryContent,
            List<String> remoteLatestSourcePackages,
            List<String> remoteArchiveSourcePackages,
            MultiValueMap<String, String> remoteLatestBinaryPackages,
            MultiValueMap<String, String> remoteArchiveBinaryPackages,
            RRepository repository,
            String versionBefore) {

        Map<String, File> packagesFiles = new HashMap<>();
        Map<String, File> packagesGzFiles = new HashMap<>();
        Map<String, File> packagesFilesForArchive = new HashMap<>();
        Map<String, File> packagesGzFilesForArchive = new HashMap<>();

        final String sourceDirectory = StringUtils.substringBetween(
                populatedRepositoryContent.getLatestDirectoryPath(), "/current/", "/latest");

        final List<File> latestSourceToUpload =
                selectPackagesToUpload(remoteLatestSourcePackages, populatedRepositoryContent.getLatestPackages());

        final String sourceDirectoryPath = repositoryGenerationDirectory.getAbsolutePath()
                + separator
                + repository.getId()
                + separator
                + CURRENT_FOLDER
                + separator
                + SRC_FOLDER
                + separator
                + CONTRIB_FOLDER;

        final File latestSourceDirectory = new File(sourceDirectoryPath + separator + LATEST_FOLDER);

        final File packagesFileForSources = new File(latestSourceDirectory.getAbsolutePath() + separator + PACKAGES);
        final File packagesGzFileForSources =
                new File(latestSourceDirectory.getAbsolutePath() + separator + PACKAGES_GZ);

        packagesFiles.put(SRC_FOLDER + separator + CONTRIB_FOLDER, packagesFileForSources);
        packagesGzFiles.put(SRC_FOLDER + separator + CONTRIB_FOLDER, packagesGzFileForSources);

        final List<String> latestSourceToDelete =
                selectPackagesToDelete(remoteLatestSourcePackages, populatedRepositoryContent.getLatestPackages());

        final List<File> archiveSourceToUpload =
                selectPackagesToUpload(remoteArchiveSourcePackages, populatedRepositoryContent.getArchivePackages());

        final File archiveSourceDirectory = new File(sourceDirectoryPath + separator + ARCHIVE_FOLDER);
        final File packagesFileFromSourceArchive =
                new File(archiveSourceDirectory.getAbsolutePath() + separator + PACKAGES);
        final File packagesGzFileFromSourceArchive =
                new File(archiveSourceDirectory.getAbsolutePath() + separator + PACKAGES_GZ);

        packagesFilesForArchive.put(SRC_FOLDER + separator + CONTRIB_FOLDER, packagesFileFromSourceArchive);
        packagesGzFilesForArchive.put(SRC_FOLDER + separator + CONTRIB_FOLDER, packagesGzFileFromSourceArchive);

        final List<String> archiveSourceToDelete =
                selectPackagesToDelete(remoteArchiveSourcePackages, populatedRepositoryContent.getArchivePackages());

        // <path_to_folder, <file, hash>>
        final Map<String, Map<String, String>> checksums = getChecksumsForPopulatedContent(populatedRepositoryContent);

        try {
            checksums
                    .get(SRC_FOLDER + separator + CONTRIB_FOLDER)
                    .put("recent/PACKAGES", calculateMd5Sum(packagesFileForSources));
            checksums
                    .get(SRC_FOLDER + separator + CONTRIB_FOLDER)
                    .put("recent/PACKAGES.gz", calculateMd5Sum(packagesGzFileForSources));
            checksums
                    .get(SRC_FOLDER + separator + CONTRIB_FOLDER)
                    .put("archive/PACKAGES", calculateMd5Sum(packagesFileFromSourceArchive));
            checksums
                    .get(SRC_FOLDER + separator + CONTRIB_FOLDER)
                    .put("archive/PACKAGES.gz", calculateMd5Sum(packagesGzFileFromSourceArchive));
        } catch (Md5SumCalculationException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Could not build synchronize request body due to storage malfunction.");
        }

        // BINARY FILES
        MultiValueMap<String, File> binaryPackagesToUpload = new LinkedMultiValueMap<>();
        MultiValueMap<String, File> binaryPackagesToUploadToArchive = new LinkedMultiValueMap<>();
        MultiValueMap<String, String> binaryPackagesToDelete = new LinkedMultiValueMap<>();
        MultiValueMap<String, String> binaryPackagesToDeleteFromArchive = new LinkedMultiValueMap<>();

        organizeBinaryPackagesForRequestBody(
                populatedRepositoryContent.getBinLatestPackagesPaths(),
                remoteLatestBinaryPackages,
                binaryPackagesToUpload,
                binaryPackagesToDelete,
                packagesFiles,
                packagesGzFiles,
                checksums,
                repository,
                false);

        organizeBinaryPackagesForRequestBody(
                populatedRepositoryContent.getBinArchivePackagesPaths(),
                remoteArchiveBinaryPackages,
                binaryPackagesToUploadToArchive,
                binaryPackagesToDeleteFromArchive,
                packagesFilesForArchive,
                packagesGzFilesForArchive,
                checksums,
                repository,
                true);

        return new SynchronizeRepositoryRequestBody(
                latestSourceToUpload,
                archiveSourceToUpload,
                latestSourceToDelete,
                archiveSourceToDelete,
                sourceDirectory,
                versionBefore,
                null,
                binaryPackagesToUpload,
                binaryPackagesToUploadToArchive,
                binaryPackagesToDelete,
                binaryPackagesToDeleteFromArchive,
                packagesFiles,
                packagesGzFiles,
                packagesFilesForArchive,
                packagesGzFilesForArchive,
                checksums);
    }

    private void organizeBinaryPackagesForRequestBody(
            MultiValueMap<String, RPackage> binaryPackagesPaths,
            MultiValueMap<String, String> remoteBinaryPackages,
            MultiValueMap<String, File> binaryPackagesToUpload,
            MultiValueMap<String, String> binaryPackagesToDelete,
            Map<String, File> packagesFiles,
            Map<String, File> packagesGzFiles,
            Map<String, Map<String, String>> checksums,
            RRepository repository,
            boolean archive) {

        MultiValueMap<String, RPackage> reducedBinaryPaths = new LinkedMultiValueMap<>();
        binaryPackagesPaths.forEach((key, value) -> reducedBinaryPaths.addAll(
                StringUtils.substringBetween(key, "/current/", archive ? "/Archive" : "/latest"), value));

        MultiValueMap<String, String> reducedRemoteBinaryPackagesPaths = new LinkedMultiValueMap<>();
        remoteBinaryPackages.forEach((key, value) ->
                reducedRemoteBinaryPackagesPaths.addAll(StringUtils.substringBefore(key, "/Archive"), value));

        Set<String> allPaths = new HashSet<>();
        allPaths.addAll(reducedBinaryPaths.keySet());
        allPaths.addAll(reducedRemoteBinaryPackagesPaths.keySet());

        for (String path : allPaths) {
            if (!reducedBinaryPaths.containsKey(path)) {
                Objects.requireNonNull(reducedRemoteBinaryPackagesPaths.get(path))
                        .forEach(packageBag -> binaryPackagesToDelete.add(path, packageBag));
                continue;
            }
            if (reducedRemoteBinaryPackagesPaths.containsKey(path)) {
                List<File> packagesToUpload = selectPackagesToUpload(
                        reducedRemoteBinaryPackagesPaths.get(path),
                        Objects.requireNonNull(reducedBinaryPaths.get(path)).stream()
                                .toList());
                packagesToUpload.forEach(packageBag -> binaryPackagesToUpload.add(path, packageBag));

                List<String> packagesToDelete = selectPackagesToDelete(
                        Objects.requireNonNull(reducedRemoteBinaryPackagesPaths.get(path)),
                        Objects.requireNonNull(reducedBinaryPaths.get(path)).stream()
                                .toList());
                packagesToDelete.forEach(packageBag -> binaryPackagesToDelete.add(path, packageBag));
            } else {
                Objects.requireNonNull(reducedBinaryPaths.get(path))
                        .forEach(packageBag -> binaryPackagesToUpload.add(path, new File(packageBag.getSource())));
            }

            File binaryDirectory = new File(
                    repositoryGenerationDirectory.getAbsolutePath()
                            + separator
                            + repository.getId()
                            + separator
                            + CURRENT_FOLDER
                            + separator
                            + path
                            + separator,
                    archive ? ARCHIVE_FOLDER : LATEST_FOLDER);

            File packagesFile = new File(binaryDirectory.getAbsolutePath() + separator + PACKAGES);
            File packagesGzFile = new File(binaryDirectory.getAbsolutePath() + separator + PACKAGES_GZ);

            packagesFiles.put(path, packagesFile);
            packagesGzFiles.put(path, packagesGzFile);

            try {
                String recentOrArchive = archive ? "archive" : "recent";
                checksums.get(path).put(recentOrArchive + "/PACKAGES", calculateMd5Sum(packagesFile));
                checksums.get(path).put(recentOrArchive + "/PACKAGES.gz", calculateMd5Sum(packagesGzFile));
            } catch (Md5SumCalculationException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException("Could not build synchronize request body due to storage malfunction.");
            }
        }
    }

    private Map<String, Map<String, String>> getChecksumsForPopulatedContent(
            @NotNull PopulatedRepositoryContent content) {
        final Map<String, Map<String, String>> checksums = new HashMap<>();

        checksums.put(SRC_FOLDER + separator + CONTRIB_FOLDER, new HashMap<>());

        content.getLatestPackages()
                .forEach(p ->
                        checksums.get(SRC_FOLDER + separator + CONTRIB_FOLDER).put(sourceToKey(p), p.getMd5sum()));
        content.getArchivePackages()
                .forEach(p ->
                        checksums.get(SRC_FOLDER + separator + CONTRIB_FOLDER).put(sourceToKey(p), p.getMd5sum()));

        content.getBinLatestPackagesPaths()
                .keySet()
                .forEach(key ->
                        checksums.put(StringUtils.substringBetween(key, "/current/", "/latest"), new HashMap<>()));

        content.getBinArchivePackagesPaths()
                .keySet()
                .forEach(key ->
                        checksums.put(StringUtils.substringBetween(key, "/current/", "/Archive"), new HashMap<>()));

        content.getBinLatestPackagesPaths()
                .forEach((key, value) -> value.forEach(packageBag -> checksums
                        .get(StringUtils.substringBetween(key, "/current/", "/latest"))
                        .put(sourceToKey(packageBag), packageBag.getMd5sum())));
        content.getBinArchivePackagesPaths()
                .forEach((key, value) -> value.forEach(packageBag -> checksums
                        .get(StringUtils.substringBetween(key, "/current/", "/Archive"))
                        .put(sourceToKey(packageBag), packageBag.getMd5sum())));

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
            List<RPackage> binaryPackages,
            List<RPackage> latestBinaryPackages,
            List<RPackage> archiveBinaryPackages,
            RRepository repository)
            throws OrganizePackagesException {
        try {
            createFolderStructureForGeneration(repository, dateStamp);
            populateGeneratedSourceFolder(packages, repository, dateStamp);

            MultiValueMap<String, RPackage> binFoldersPaths =
                    createBinaryFolderStructureForGeneration(binaryPackages, repository, dateStamp);

            for (String folderPath : binFoldersPaths.keySet()) {
                populateGeneratedBinaryFolder(
                        binFoldersPaths.get(folderPath).stream().toList(), repository, dateStamp, folderPath);
            }

            final File target = linkCurrentFolderToGeneratedFolder(repository, dateStamp);

            final Path repoPathForSources = target.toPath().resolve(SRC_FOLDER).resolve(CONTRIB_FOLDER);
            final String latestSourceFolderPath = repoPathForSources + separator + LATEST_FOLDER;
            final String archiveSourceFolderPath = repoPathForSources + separator + ARCHIVE_FOLDER;

            createTemporaryFoldersForLatestAndArchive(repoPathForSources.toString());
            populatePackageFolder(latestPackages, latestSourceFolderPath);
            populatePackageFolder(archivePackages, archiveSourceFolderPath);

            final Path repoPathForBinaries = target.toPath().resolve(BIN_FOLDER).resolve(LINUX_FOLDER);

            MultiValueMap<String, RPackage> binLatestFoldersPaths = new LinkedMultiValueMap<>();
            MultiValueMap<String, RPackage> binArchiveFoldersPaths = new LinkedMultiValueMap<>();

            for (String folderPath : binFoldersPaths.keySet()) {
                String latestFolderPath = repoPathForBinaries + separator + folderPath + separator + LATEST_FOLDER;
                String archiveFolderPath = repoPathForBinaries + separator + folderPath + separator + ARCHIVE_FOLDER;

                createTemporaryFoldersForLatestAndArchive(repoPathForBinaries + separator + folderPath);

                extractAndPopulateBinaries(
                        latestBinaryPackages, binFoldersPaths, binLatestFoldersPaths, folderPath, latestFolderPath);
                extractAndPopulateBinaries(
                        archiveBinaryPackages, binFoldersPaths, binArchiveFoldersPaths, folderPath, archiveFolderPath);
            }

            if (repository.isRedirectToSource()) {
                Map<String, List<RPackage>> latestPackagesToPutIntoPackagesFile =
                        chooseLatestPackagesToPutIntoPackagesFile(
                                latestPackages, binLatestFoldersPaths, binArchiveFoldersPaths);
                Map<String, List<RPackage>> archivePackagesToPutIntoPackagesFile =
                        getArchivePackagesToPutIntoPackagesFile(archivePackages, binArchiveFoldersPaths);

                generatePackagesFiles(latestPackagesToPutIntoPackagesFile);
                generatePackagesFiles(archivePackagesToPutIntoPackagesFile);
            }

            return new PopulatedRepositoryContent(
                    latestPackages,
                    archivePackages,
                    latestSourceFolderPath,
                    archiveSourceFolderPath,
                    binLatestFoldersPaths,
                    binArchiveFoldersPaths);
        } catch (CreateFolderStructureException
                | PackageFolderPopulationException
                | LinkFoldersException
                | GeneratePackagesFileException e) {
            log.error(e.getMessage(), e);
            throw new OrganizePackagesException();
        }
    }

    private Map<String, List<RPackage>> chooseLatestPackagesToPutIntoPackagesFile(
            List<RPackage> sourcePackages,
            MultiValueMap<String, RPackage> binPackages,
            MultiValueMap<String, RPackage> archiveBinPackages) {

        Map<String, List<RPackage>> packagesToPutIntoPackagesFile = new HashMap<>();
        for (String path : binPackages.keySet()) {
            packagesToPutIntoPackagesFile.put(path, new ArrayList<>());

            final List<RPackage> binariesList =
                    binPackages.get(path).stream().map(RPackage::new).collect(Collectors.toList());

            for (RPackage packageBag : binariesList) {
                RPackage chosenPackage = chooseSourceOrBinaryVersionOfPackage(sourcePackages, packageBag);

                packagesToPutIntoPackagesFile.get(path).add(chosenPackage);

                if (!chosenPackage.equals(packageBag)) {
                    final File toMoveFile = new File(path, packageBag.getFileName());
                    final File newFileLocalization =
                            new File(path.replaceAll("/latest", "/Archive"), packageBag.getFileName());
                    try {
                        FileUtils.moveFile(toMoveFile, newFileLocalization, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                        throw new IllegalStateException("Could not properly move file to Archive!");
                    }
                    archiveBinPackages.add(path.replaceAll("/latest", "/Archive"), packageBag);
                    binPackages.get(path).remove(packageBag);
                }
            }

            packagesToPutIntoPackagesFile
                    .get(path)
                    .addAll(extractSourcePackagesToPutIntoPackagesFile(sourcePackages, binariesList));
        }

        return packagesToPutIntoPackagesFile;
    }

    private Map<String, List<RPackage>> getArchivePackagesToPutIntoPackagesFile(
            List<RPackage> sourcePackages, MultiValueMap<String, RPackage> binPackages) {

        Map<String, List<RPackage>> packagesToPutIntoPackagesFile = new HashMap<>();

        for (String path : binPackages.keySet()) {
            packagesToPutIntoPackagesFile.put(path, new ArrayList<>());

            packagesToPutIntoPackagesFile.get(path).addAll(binPackages.get(path));

            packagesToPutIntoPackagesFile.get(path).addAll(sourcePackages);
        }

        return packagesToPutIntoPackagesFile;
    }

    private List<RPackage> extractSourcePackagesToPutIntoPackagesFile(
            List<RPackage> sourcePackages, List<RPackage> binaryPackages) {
        List<String> binaryNames = binaryPackages.stream().map(Package::getName).toList();

        return sourcePackages.stream()
                .filter(packageBag -> !binaryNames.contains(packageBag.getName()))
                .toList();
    }

    private RPackage chooseSourceOrBinaryVersionOfPackage(List<RPackage> sourcePackages, RPackage binaryPackage) {

        for (RPackage sourcePackage : sourcePackages) {
            if (Objects.equals(sourcePackage.getName(), binaryPackage.getName())
                    && compareVersions(sourcePackage.getVersion(), binaryPackage.getVersion()) == 1) {
                return sourcePackage;
            }
        }

        return binaryPackage;
    }

    private int compareVersions(String sourceVersion, String binaryVersion) {

        String[] sourceVersionSplitted = StringUtils.splitByWholeSeparator(sourceVersion, ".");
        String[] binaryVersionSplitted = StringUtils.splitByWholeSeparator(binaryVersion, ".");

        int maxLength = Math.min(sourceVersionSplitted.length, binaryVersionSplitted.length);

        for (int i = 0; i < maxLength; i++) {
            if (Integer.valueOf(sourceVersionSplitted[i]).compareTo(Integer.valueOf(binaryVersionSplitted[i])) != 0) {
                return Integer.valueOf(sourceVersionSplitted[i]).compareTo(Integer.valueOf(binaryVersionSplitted[i]));
            }
        }

        if (sourceVersionSplitted.length > binaryVersionSplitted.length) {
            return 1;
        }

        return 0;
    }

    private void extractAndPopulateBinaries(
            List<RPackage> packagesToPopulate,
            MultiValueMap<String, RPackage> allPackagesFolderPaths,
            MultiValueMap<String, RPackage> separatedBinFoldersPaths,
            String destinationFolderPath,
            String currentFolderPath)
            throws PackageFolderPopulationException {
        final List<RPackage> packagesInBinFolder = extractPackagesForBinFolders(
                packagesToPopulate,
                allPackagesFolderPaths.get(destinationFolderPath).stream().toList());
        if (!packagesInBinFolder.isEmpty()) {
            populatePackageFolder(packagesInBinFolder, currentFolderPath);
            separatedBinFoldersPaths.addAll(currentFolderPath, packagesInBinFolder);
        }
    }

    /*
     * This method helps to extract only latest or only archive packages
     * from packages separated into individual bin folders
     */
    private List<RPackage> extractPackagesForBinFolders(
            List<RPackage> allPackages, List<RPackage> selectedForBinFolderPackages) {

        List<RPackage> extractedPackages = new ArrayList<>();
        for (RPackage packageBag : selectedForBinFolderPackages)
            if (allPackages.contains(packageBag)) extractedPackages.add(packageBag);

        return extractedPackages;
    }

    protected MultiValueMap<String, RPackage> createBinaryFolderStructureForGeneration(
            List<RPackage> packages, RRepository repository, String dateStamp) throws CreateFolderStructureException {
        File dateStampFolder = null;
        try {
            dateStampFolder = createFolderStructure(repositoryGenerationDirectory.getAbsolutePath()
                    + separator
                    + repository.getId()
                    + separator
                    + dateStamp);

            MultiValueMap<String, RPackage> packagesLocalization = new LinkedMultiValueMap<>();

            for (RPackage packageBag : packages) {
                String binFolderStructure = packageBag.getDistribution()
                        + separator
                        + packageBag.getArchitecture()
                        + separator
                        + resolveRVersionToFolderName(packageBag.getRVersion());
                packagesLocalization.add(binFolderStructure, packageBag);
                createFolderStructure(
                        getRepositoryForBinaryGeneratedPath(dateStampFolder, separator, binFolderStructure));
            }

            return packagesLocalization;
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

    /*
     * This method resolves R version of package to a folder for repository server
     * according to the pattern X.Y e.g. 4.2.1 becomes 4.2 and 4 becomes 4.0
     */
    private String resolveRVersionToFolderName(String rVersion) {

        int nonDigits = 0;

        for (int i = 0; i < rVersion.length(); i++) {
            if (!Character.isDigit(rVersion.charAt(i))) {
                nonDigits++;
                if (nonDigits == 2) return rVersion.substring(0, i);
            }
        }

        if (nonDigits == 0) return rVersion + ".0";

        return rVersion;
    }

    @Override
    public void populatePackage(RPackage packageBag, String folderPath) throws PackageFolderPopulationException {
        if (packageBag.isActive()) {
            String targetFilePath = packageBag.getSource();
            String destinationFilePath =
                    folderPath + separator + packageBag.getName() + "_" + packageBag.getVersion() + ".tar.gz";

            try {

                Files.copy(new File(targetFilePath).toPath(), new File(destinationFilePath).toPath());
                final String calculatedSum = calculateMd5Sum(new File(destinationFilePath));
                if (!packageBag.getMd5sum().equals(calculatedSum)) {
                    throw new Md5MismatchException();
                }

                if (!packageBag.isBinary() || !packageBag.getRepository().isRedirectToSource()) {
                    addPackageToPackagesFile(packageBag, folderPath);
                }

            } catch (IOException
                    | Md5MismatchException
                    | Md5SumCalculationException
                    | GeneratePackagesFileException e) {
                log.error("{}: {}", e.getClass(), e.getMessage());
                throw new PackageFolderPopulationException();
            }
        }
    }

    private void addPackageToPackagesFile(RPackage packageBag, String folderPath) throws GeneratePackagesFileException {
        File packagesFile = new File(folderPath + separator + PACKAGES);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(packagesFile, true))) {
            writer.append(generatePackageString(packageBag));
            gzipFile(packagesFile);
        } catch (IOException | GzipFileException e) {
            log.error("{}: {}", e.getClass(), e.getMessage());
            throw new GeneratePackagesFileException();
        }
    }

    // Map <folder_path, packages>
    private void generatePackagesFiles(Map<String, List<RPackage>> packages) throws GeneratePackagesFileException {
        for (String path : packages.keySet()) {

            for (RPackage packageBag : packages.get(path)) {
                addPackageToPackagesFile(packageBag, path);
            }
        }
    }

    private String generatePackageString(RPackage packageBag) {
        final StringBuilder packageString = new StringBuilder(500);
        final String lineSeparator = System.lineSeparator();

        packageString
                .append("Package: ")
                .append(separateLines(packageBag.getName(), lineSeparator))
                .append(lineSeparator);
        packageString
                .append("Version: ")
                .append(separateLines(packageBag.getVersion(), lineSeparator))
                .append(lineSeparator);
        if (packageBag.getDepends() != null && !packageBag.getDepends().trim().isEmpty())
            packageString
                    .append("Depends: ")
                    .append(separateLines(packageBag.getDepends(), lineSeparator))
                    .append(lineSeparator);
        if (packageBag.getImports() != null && !packageBag.getImports().trim().isEmpty())
            packageString
                    .append("Imports: ")
                    .append(separateLines(packageBag.getImports(), lineSeparator))
                    .append(lineSeparator);
        if (packageBag.getSuggests() != null && !packageBag.getSuggests().trim().isEmpty())
            packageString
                    .append("Suggests: ")
                    .append(separateLines(packageBag.getSuggests(), lineSeparator))
                    .append(lineSeparator);
        packageString
                .append("License: ")
                .append(separateLines(packageBag.getLicense(), lineSeparator))
                .append(lineSeparator);
        if (packageBag.getLinkingTo() != null && !packageBag.getLinkingTo().isEmpty())
            packageString
                    .append("LinkingTo: ")
                    .append(separateLines(packageBag.getLinkingTo(), lineSeparator))
                    .append(lineSeparator);
        if (packageBag.getEnhances() != null && !packageBag.getEnhances().isEmpty())
            packageString
                    .append("Enhances: ")
                    .append(separateLines(packageBag.getEnhances(), lineSeparator))
                    .append(lineSeparator);
        if (packageBag.getPriority() != null && !packageBag.getPriority().isEmpty())
            packageString
                    .append("Priority: ")
                    .append(separateLines(packageBag.getPriority(), lineSeparator))
                    .append(lineSeparator);
        packageString
                .append("MD5Sum: ")
                .append(separateLines(packageBag.getMd5sum(), lineSeparator))
                .append(lineSeparator);
        packageString
                .append("NeedsCompilation: ")
                .append(packageBag.isNeedsCompilation() ? "yes" : "no")
                .append(lineSeparator);
        if (packageBag.isBinary())
            packageString
                    .append("Built: ")
                    .append(separateLines(packageBag.getBuilt(), lineSeparator))
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

            Set<String> binaryDirPaths = new HashSet<>();
            binaryDirPaths.addAll(
                    populatedRepositoryContent.getBinArchivePackagesPaths().keySet());
            binaryDirPaths.addAll(
                    populatedRepositoryContent.getBinArchivePackagesPaths().keySet());

            for (String path : binaryDirPaths) {
                deleteFile(new File(path));
            }

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

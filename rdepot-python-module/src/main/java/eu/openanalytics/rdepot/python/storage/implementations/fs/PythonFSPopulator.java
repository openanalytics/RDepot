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
package eu.openanalytics.rdepot.python.storage.implementations.fs;

import eu.openanalytics.rdepot.base.ReadmeParser;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.messaging.MessageCodes;
import eu.openanalytics.rdepot.base.storage.exceptions.*;
import eu.openanalytics.rdepot.base.storage.implementations.LocalFSPopulator;
import eu.openanalytics.rdepot.base.synchronization.checksums.Checksums;
import eu.openanalytics.rdepot.base.utils.PackageFilteringUtils;
import eu.openanalytics.rdepot.python.PythonPropertiesParser;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.mediator.hash.HashCalculator;
import eu.openanalytics.rdepot.python.messaging.PythonMessageCodes;
import eu.openanalytics.rdepot.python.storage.PythonPopulator;
import eu.openanalytics.rdepot.python.storage.exceptions.ReadPythonPackagePropertiesFileException;
import eu.openanalytics.rdepot.python.storage.indexes.PythonPackageIndexGenerator;
import eu.openanalytics.rdepot.python.storage.indexes.PythonRepositoryIndexGenerator;
import eu.openanalytics.rdepot.python.storage.models.PopulatedRepositoryContent;
import eu.openanalytics.rdepot.python.synchronization.SynchronizeRepositoryRequestBody;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Local storage implementation for Python.
 */
@Slf4j
@Component
public class PythonFSPopulator extends LocalFSPopulator<PythonRepository, PythonPackage, PythonPopulatedPackage>
        implements PythonPopulator {

    private final String snapshot;

    private final File repositoryGenerationDirectory;

    private final PythonRepositoryIndexGenerator pythonRepositoryIndexGenerator;
    private final PythonPackageIndexGenerator pythonPackageIndexGenerator;

    private Path current;

    private final Pattern pythonPackageVersion = Pattern.compile("-([0-9]+[.][0-9]+[.][0-9])");

    private static final String INDEX_FILE = "index.html";
    private static final String DESCRIPTION = "Description";

    private final PythonLocalStorage storage;

    public PythonFSPopulator(
            @Qualifier("repositoryGenerationDirectory") File repositoryGenerationDirectory,
            @Qualifier("packageUploadDirectory") File packageUploadDirectory,
            PythonRepositoryIndexGenerator pythonRepositoryIndexGenerator,
            PythonPackageIndexGenerator pythonPackageIndexGenerator,
            PythonLocalStorage storage,
            @Value("${repository-snapshots}") String snapshot) {
        super(packageUploadDirectory, repositoryGenerationDirectory, storage);
        this.storage = storage;
        this.repositoryGenerationDirectory = repositoryGenerationDirectory;
        this.pythonRepositoryIndexGenerator = pythonRepositoryIndexGenerator;
        this.pythonPackageIndexGenerator = pythonPackageIndexGenerator;
        this.snapshot = snapshot;
    }

    private final Pattern distInfoFolder = Pattern.compile("^[^/]+\\.dist-info/.*$");

    @Override
    public Properties getPropertiesFromExtractedFile(final String extractedFile)
            throws ReadPackageDescriptionException {
        try {

            Properties properties;
            if (Files.exists(Paths.get(extractedFile + separator + "PKG-INFO"))) {
                properties = new PythonPropertiesParser(new File(extractedFile + separator + "PKG-INFO"));
            } else {
                properties = new PythonPropertiesParser(new File(extractedFile + ".dist-info/METADATA"));
                properties.load(new FileInputStream(extractedFile + ".dist-info/WHEEL"));
            }

            if (Objects.nonNull(properties.getProperty(DESCRIPTION))) return properties;

            String fileExtension = "";
            switch (properties.getProperty("Description-Content-Type", "")) {
                case "text/markdown": {
                    fileExtension = ".md";
                    break;
                }
                case "text/x-rst": {
                    fileExtension = ".rst";
                    break;
                }
                default:
                    break;
            }
            if (Files.exists(Paths.get(extractedFile + separator + "README" + fileExtension))) {
                properties.setProperty(
                        DESCRIPTION,
                        ReadmeParser.loadReadme(new File(extractedFile + separator + "README" + fileExtension)));
            } else if (Files.exists(Paths.get(extractedFile + separator + "Readme" + fileExtension))) {
                properties.setProperty(
                        DESCRIPTION,
                        ReadmeParser.loadReadme(new File(extractedFile + separator + "Readme" + fileExtension)));
            } else if (Files.exists(Paths.get(extractedFile + separator + "readme" + fileExtension))) {
                properties.setProperty(
                        DESCRIPTION,
                        ReadmeParser.loadReadme(new File(extractedFile + separator + "readme" + fileExtension)));
            }
            return properties;
        } catch (IOException e) {
            try {
                storage.deleteFile(new File(extractedFile).getParentFile());
            } catch (DeleteFileException dfe) {
                log.error(dfe.getMessage(), dfe);
            }
            log.error(e.getMessage(), e);
            throw new ReadPythonPackagePropertiesFileException();
        }
    }

    private List<PythonPopulatedPackage> populateGeneratedFolder(
            List<PythonPackage> packages, PythonRepository repository, String dateStamp)
            throws PackageFolderPopulationException {
        final List<PythonPopulatedPackage> populatedPackages = new ArrayList<>();
        String folderPath = repositoryGenerationDirectory.getAbsolutePath()
                + separator
                + repository.getId()
                + separator
                + dateStamp;

        for (PythonPackage packageBag : packages) {
            if (packageBag.isActive()) {
                populatedPackages.add(new PythonPopulatedPackage(packageBag, populatePackage(packageBag, folderPath)));
            }
        }
        return populatedPackages;
    }

    protected void createFolderStructureForGeneration(
            List<PythonPackage> packages, PythonRepository repository, String dateStamp)
            throws CreateFolderStructureException {
        File dateStampFolder = null;
        try {
            dateStampFolder = storage.createFolderStructure(repositoryGenerationDirectory.getAbsolutePath()
                    + separator
                    + repository.getId()
                    + separator
                    + dateStamp);

            storage.createFolderStructure(dateStampFolder.getAbsolutePath());
            pythonRepositoryIndexGenerator.generateEmptyIndex(
                    repository, dateStampFolder.getAbsolutePath() + separator + INDEX_FILE);

            final List<PythonPackage> distinctPackagesByName = packages.stream()
                    .filter(PackageFilteringUtils.distinctByKey(PythonPackage::getName))
                    .toList();

            for (PythonPackage packageBag : distinctPackagesByName) {
                storage.createFolderStructure(dateStampFolder + separator + packageBag.getNormalizedName());
                pythonPackageIndexGenerator.generateIndex(
                        packageBag,
                        dateStampFolder + separator + packageBag.getNormalizedName() + separator + INDEX_FILE);
                pythonRepositoryIndexGenerator.addPackageToList(packageBag, dateStampFolder + separator + INDEX_FILE);
            }
        } catch (CreateFolderStructureException e) {
            if (dateStampFolder != null) {
                try {
                    storage.deleteFile(dateStampFolder);
                } catch (DeleteFileException dfe) {
                    log.error(dfe.getMessage(), dfe);
                }
            }

            throw e;
        } catch (IOException e1) {
            log.error("Could not create index file", e1);
            throw new CreateFolderStructureException();
        }
    }

    @Override
    public SynchronizeRepositoryRequestBody buildSynchronizeRequestBody(
            PopulatedRepositoryContent populatedRepositoryContent,
            List<String> remotePackages,
            Checksums remoteChecksums,
            PythonRepository repository,
            String versionBefore) {
        final Map<String, String> checksums = new HashMap<>();

        final List<File> packagesToUpload = selectPackagesToUpload(
                remotePackages, remoteChecksums, populatedRepositoryContent.packages(), checksums);
        final List<String> packagesToDelete =
                selectPackagesToDelete(remotePackages, populatedRepositoryContent.packages(), packagesToUpload);

        return new SynchronizeRepositoryRequestBody(
                packagesToUpload,
                packagesToDelete,
                versionBefore,
                repository.getName(),
                repository.getHashMethod(),
                checksums);
    }

    private void addPackageFiles(
            PythonPopulatedPackage packageBag, List<File> toUpload, Map<String, String> checksums) {
        String packageFilePath =
                this.current + separator + packageBag.getNormalizedName() + separator + packageBag.getFileName();
        String indexFilePath = this.current + separator + packageBag.getNormalizedName() + separator + INDEX_FILE;
        final File indexFile = new File(indexFilePath);
        final File packageFile = new File(packageFilePath);
        toUpload.add(packageFile);
        if (!toUpload.contains(indexFile)) {
            toUpload.add(indexFile);
            try {
                checksums.put(
                        indexFile.getName(),
                        storage.calculateCheckSum(
                                indexFile, packageBag.getRepository().getHashMethod()));
            } catch (CheckSumCalculationException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    private void addRepositoryIndexFile(
            List<File> toUpload, List<String> remotePackages, List<PythonPopulatedPackage> localPackages) {
        Set<String> localPackagesNames =
                localPackages.stream().map(PythonPackage::getNormalizedName).collect(Collectors.toSet());

        Set<String> remotePackagesNames = remotePackages.stream()
                .map(remotePackage -> {
                    Matcher matcher = pythonPackageVersion.matcher(remotePackage);
                    if (matcher.find()) {
                        return remotePackage.substring(0, matcher.start());
                    }
                    return remotePackage;
                })
                .collect(Collectors.toSet());

        if (!remotePackagesNames.containsAll(localPackagesNames)
                || !localPackagesNames.containsAll(remotePackagesNames)) {
            String indexFilePath = this.current + separator + INDEX_FILE;
            File indexFile = new File(indexFilePath);
            if (!toUpload.contains(indexFile)) {
                toUpload.add(indexFile);
            }
        }
    }

    @Override
    protected List<File> selectPackagesToUpload(
            List<String> remotePackages,
            Checksums remoteChecksums,
            List<PythonPopulatedPackage> localPackages,
            Map<String, String> checksums) {

        List<String> remotePackagesNames = remotePackages.stream()
                .map(packageDetails -> packageDetails.substring(packageDetails.indexOf('/') + 1))
                .toList();

        List<File> toUpload = new ArrayList<>();
        for (PythonPopulatedPackage packageBag : localPackages) {
            try {
                if (!remotePackagesNames.contains(packageBag.getPackageFilename())
                        || !remoteChecksums.contains(
                                storage.calculateCheckSum(packageBag), packageBag.getPackageFilename())) {
                    addPackageFiles(packageBag, toUpload, checksums);
                }
            } catch (CheckSumCalculationException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        addRepositoryIndexFile(toUpload, remotePackagesNames, localPackages);
        return toUpload;
    }

    protected List<String> selectPackagesToDelete(
            List<String> remotePackages, List<PythonPopulatedPackage> localPackages, List<File> toUpload) {
        List<String> toDelete = new ArrayList<>();

        for (String packagePath : remotePackages) {
            boolean found = false;
            for (PythonPackage packageBag : localPackages) {
                if ((packageBag.getNormalizedName() + "/" + packageBag.getFileName()).equals(packagePath)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                toDelete.add(packagePath);
                if (!localPackages.isEmpty()) {
                    addIndexFilesWithoutDeletedPackages(localPackages, packagePath, toUpload);
                }
            }
        }
        return toDelete;
    }

    private void addIndexFilesWithoutDeletedPackages(
            List<PythonPopulatedPackage> localPackages, String packagePath, List<File> toUpload) {
        String packageName = packagePath.substring(0, packagePath.indexOf('/'));

        File packageIndexFile = new File(this.current + separator + packageName + separator + INDEX_FILE);
        File repositoryIndexFile = new File(this.current + separator + INDEX_FILE);

        if (packageIndexFile.exists()) {
            toUpload.add(packageIndexFile);
        }

        if (repositoryIndexFile.exists() && !toUpload.contains(repositoryIndexFile)) {
            boolean uploadIndexFile = true;
            for (Package packageBag : localPackages) {
                if (packageBag.getName().equals(packageName)) {
                    uploadIndexFile = false;
                    break;
                }
            }

            if (uploadIndexFile) {
                toUpload.add(repositoryIndexFile);
            }
        }
    }

    @Override
    public PopulatedRepositoryContent organizePackagesInStorage(
            String dateStamp, List<PythonPackage> packages, PythonRepository repository)
            throws OrganizePackagesException {
        try {
            createFolderStructureForGeneration(packages, repository, dateStamp);
            List<PythonPopulatedPackage> populatedPackages = populateGeneratedFolder(packages, repository, dateStamp);
            File current = linkCurrentFolderToGeneratedFolder(repository, dateStamp);
            this.current = Files.readSymbolicLink(Paths.get(current.getAbsolutePath()));
            return new PopulatedRepositoryContent(populatedPackages, current.getAbsolutePath());

        } catch (CreateFolderStructureException
                | PackageFolderPopulationException
                | LinkFoldersException
                | IOException e) {
            log.error(e.getMessage(), e);
            throw new OrganizePackagesException();
        }
    }

    @Override
    public String populatePackage(PythonPackage packageBag, String folderPath) throws PackageFolderPopulationException {
        String targetFilePath = packageBag.getSource();
        String destinationFilePath =
                folderPath + separator + packageBag.getNormalizedName() + separator + packageBag.getPackageFilename();

        try {
            checkHash(packageBag);
            pythonPackageIndexGenerator.addPackageToList(
                    packageBag, folderPath + separator + packageBag.getNormalizedName() + separator + INDEX_FILE);
            Files.copy(new File(targetFilePath).toPath(), new File(destinationFilePath).toPath());
        } catch (IOException | Md5MismatchException | CheckSumCalculationException e) {
            log.error("{}: {}", e.getClass(), e.getMessage());
            throw new PackageFolderPopulationException();
        }
        return destinationFilePath;
    }

    private void checkHash(PythonPackage packageBag) throws Md5MismatchException, CheckSumCalculationException {
        final File packageSource = new File(packageBag.getSource());
        HashCalculator hashCalculator =
                new HashCalculator(packageBag.getRepository().getHashMethod(), packageSource);
        if (!packageBag.getHash().equals(hashCalculator.calculateHash())) {
            throw new Md5MismatchException();
        }
    }

    @Override
    public void cleanUpAfterSynchronization(PopulatedRepositoryContent populatedRepositoryContent)
            throws CleanUpAfterSynchronizationException {
        try {
            if (!Boolean.parseBoolean(snapshot)) {
                storage.cleanDirectory(repositoryGenerationDirectory);
            }
        } catch (DeleteFileException e) {
            log.error(e.getMessage(), e);
            throw new CleanUpAfterSynchronizationException();
        }
    }

    @Override
    public String extractWhlPackageFile(String storedFilePath) throws ExtractFileException {
        log.debug("Extracting package file: {}", storedFilePath);
        final File storedFile = new File(storedFilePath);
        final File outputDir = storedFile.getParentFile();

        try {
            return Path.of(outputDir.getAbsolutePath(), unpackWhl(storedFile, outputDir))
                    .toString();
        } catch (IOException | ArchiveException e) {
            try {
                if (storedFile.getParentFile().exists())
                    storage.deleteFile(storedFile.getParentFile().getAbsoluteFile());
            } catch (DeleteFileException dfe) {
                log.error(dfe.getMessage(), dfe);
            }
            log.error(e.getMessage(), e);
            throw new ExtractFileException();
        }
    }

    private String unpackWhl(final File inputFile, final File outputDir) throws IOException, ArchiveException {
        log.debug("Extracting {} to dir {}.", inputFile.getAbsolutePath(), outputDir.getAbsolutePath());
        final InputStream is = new FileInputStream(inputFile);
        ZipEntry entry;
        List<String> filesInArchive = new ArrayList<>();
        String nameAndVersion = "";

        try (ZipInputStream zis = new ZipInputStream(is)) {
            while ((entry = zis.getNextEntry()) != null) {
                filesInArchive.add(entry.getName());
                if (nameAndVersion.isEmpty()
                        && distInfoFolder.matcher(entry.getName()).matches())
                    nameAndVersion = StringUtils.substringBefore(entry.getName(), ".dist-info");

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
                        IOUtils.copy(zis, outputFileStream);
                    }
                }
            }
        }

        if (filesInArchive.isEmpty()) throw new ArchiveException(MessageCodes.EMPTY_ARCHIVE);
        if (nameAndVersion.isEmpty()) throw new ArchiveException(PythonMessageCodes.COULD_NOT_FIND_DIST_INFO_FOLDER);
        return nameAndVersion;
    }
}

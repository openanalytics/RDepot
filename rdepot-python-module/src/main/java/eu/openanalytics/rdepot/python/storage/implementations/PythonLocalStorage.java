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
package eu.openanalytics.rdepot.python.storage.implementations;

import eu.openanalytics.rdepot.base.PropertiesParser;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.storage.exceptions.CheckSumCalculationException;
import eu.openanalytics.rdepot.base.storage.exceptions.CleanUpAfterSynchronizationException;
import eu.openanalytics.rdepot.base.storage.exceptions.CreateFolderStructureException;
import eu.openanalytics.rdepot.base.storage.exceptions.DeleteFileException;
import eu.openanalytics.rdepot.base.storage.exceptions.LinkFoldersException;
import eu.openanalytics.rdepot.base.storage.exceptions.Md5MismatchException;
import eu.openanalytics.rdepot.base.storage.exceptions.OrganizePackagesException;
import eu.openanalytics.rdepot.base.storage.exceptions.PackageFolderPopulationException;
import eu.openanalytics.rdepot.base.storage.exceptions.ReadPackageDescriptionException;
import eu.openanalytics.rdepot.base.storage.implementations.CommonLocalStorage;
import eu.openanalytics.rdepot.python.entities.PythonPackage;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import eu.openanalytics.rdepot.python.entities.enums.HashMethod;
import eu.openanalytics.rdepot.python.mediator.hash.HashCalculator;
import eu.openanalytics.rdepot.python.storage.PythonStorage;
import eu.openanalytics.rdepot.python.storage.exceptions.ReadPythonPackagePkgInfoException;
import eu.openanalytics.rdepot.python.storage.indexes.PackageIndexGenerator;
import eu.openanalytics.rdepot.python.storage.indexes.RepositoryIndexGenerator;
import eu.openanalytics.rdepot.python.storage.utils.PopulatedRepositoryContent;
import eu.openanalytics.rdepot.python.synchronization.SynchronizeRepositoryRequestBody;
import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Local storage implementation for Python.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PythonLocalStorage extends CommonLocalStorage<PythonRepository, PythonPackage> implements PythonStorage {

    @Resource(name = "repositoryGenerationDirectory")
    private File repositoryGenerationDirectory;

    @Value("${repository-snapshots}")
    private String snapshot;

    private final PackageIndexGenerator packageIndexGenerator;
    private final RepositoryIndexGenerator repositoryIndexGenerator;

    private Path current;

    private final Pattern pythonPackageVersion = Pattern.compile("-([0-9]+[.][0-9]+[.][0-9])");

    private static final String INDEX_FILE = "index.html";

    @Override
    public Properties getPropertiesFromExtractedFile(final String extractedFile)
            throws ReadPackageDescriptionException {
        try {
            return new PropertiesParser(new File(extractedFile + separator + "PKG-INFO"));
        } catch (IOException e) {
            try {
                deleteFile(new File(extractedFile).getParentFile());
            } catch (DeleteFileException dfe) {
                log.error(dfe.getMessage(), dfe);
            }
            log.error(e.getMessage(), e);
            throw new ReadPythonPackagePkgInfoException();
        }
    }

    public String getRepositoryGeneratedPath(File dateStampFolder, String separator) {
        return dateStampFolder.getAbsolutePath();
    }

    private void populateGeneratedFolder(List<PythonPackage> packages, PythonRepository repository, String dateStamp)
            throws PackageFolderPopulationException {
        String folderPath = repositoryGenerationDirectory.getAbsolutePath()
                + separator
                + repository.getId()
                + separator
                + dateStamp;

        for (PythonPackage packageBag : packages) {
            populatePackage(packageBag, folderPath);
        }
    }

    private void createPackageIndexTemplate(String path, PythonPackage packageBag) throws IOException {
        packageIndexGenerator.createIndexFile(packageBag, path + separator + packageBag.getName());
        repositoryIndexGenerator.addPackageToIndex(packageBag, path);
    }

    private void createFolderStructureForGeneration(
            List<PythonPackage> packages, PythonRepository repository, String dateStamp)
            throws CreateFolderStructureException {
        File dateStampFolder = null;
        try {
            dateStampFolder = createFolderStructure(repositoryGenerationDirectory.getAbsolutePath()
                    + separator
                    + repository.getId()
                    + separator
                    + dateStamp);

            createFolderStructure(dateStampFolder.getAbsolutePath());
            repositoryIndexGenerator.createIndexFile(repository, dateStampFolder.getAbsolutePath());

            for (PythonPackage packageBag : packages) {
                createFolderStructure(dateStampFolder + separator + packageBag.getName());
                createPackageIndexTemplate(dateStampFolder.toString(), packageBag);
            }

        } catch (CreateFolderStructureException e) {
            if (dateStampFolder != null) {
                try {
                    deleteFile(dateStampFolder);
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
            PythonRepository repository,
            String versionBefore) {
        final Map<String, String> checksums = new HashMap<>();

        final List<File> packagesToUpload =
                selectPackagesToUpload(remotePackages, populatedRepositoryContent.getPackages(), checksums);
        final List<String> packagesToDelete =
                selectPackagesToDelete(remotePackages, populatedRepositoryContent.getPackages(), packagesToUpload);

        return new SynchronizeRepositoryRequestBody(
                packagesToUpload,
                packagesToDelete,
                versionBefore,
                repository.getName(),
                repository.getHashMethod(),
                checksums);
    }

    private void addPackageFiles(PythonPackage packageBag, List<File> toUpload, Map<String, String> checksums) {
        String packageFilePath = this.current + separator + packageBag.getName() + separator + packageBag.getFileName();
        String indexFilePath = this.current + separator + packageBag.getName() + separator + INDEX_FILE;
        final File indexFile = new File(indexFilePath);
        final File packageFile = new File(packageFilePath);
        toUpload.add(packageFile);
        if (!toUpload.contains(indexFile)) {
            toUpload.add(indexFile);
            try {
                checksums.put(
                        indexFile.getName(),
                        calculateCheckSum(indexFile, packageBag.getRepository().getHashMethod()));
            } catch (CheckSumCalculationException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    private String calculateCheckSum(File file, HashMethod hashMethod) throws CheckSumCalculationException {
        final HashCalculator hashCalculator = new HashCalculator(hashMethod, file);
        return hashCalculator.calculateHash();
    }

    private void addRepositoryIndexFile(
            List<File> toUpload, List<String> remotePackages, List<PythonPackage> localPackages) {
        Set<String> localPackagesNames =
                localPackages.stream().map(Package::getName).collect(Collectors.toSet());

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

    protected List<File> selectPackagesToUpload(
            List<String> remotePackages, List<PythonPackage> localPackages, Map<String, String> checksums) {

        List<String> remotePackagesNames = remotePackages.stream()
                .map(packageDetails -> packageDetails.substring(packageDetails.indexOf('/') + 1))
                .collect(Collectors.toList());

        List<File> toUpload = new ArrayList<>();
        for (PythonPackage packageBag : localPackages) {
            if (!remotePackagesNames.contains(packageBag.getFileName())) {
                addPackageFiles(packageBag, toUpload, checksums);
            }
        }
        addRepositoryIndexFile(toUpload, remotePackagesNames, localPackages);
        return toUpload;
    }

    protected List<String> selectPackagesToDelete(
            List<String> remotePackages, List<PythonPackage> localPackages, List<File> toUpload) {
        List<String> toDelete = new ArrayList<>();

        for (String packagePath : remotePackages) {
            boolean found = false;
            for (Package packageBag : localPackages) {
                if ((packageBag.getName() + "/" + packageBag.getFileName()).equals(packagePath)) {
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
            List<PythonPackage> localPackages, String packagePath, List<File> toUpload) {
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
            populateGeneratedFolder(packages, repository, dateStamp);
            File current = linkCurrentFolderToGeneratedFolder(repository, dateStamp);
            this.current = Files.readSymbolicLink(Paths.get(current.getAbsolutePath()));
            return new PopulatedRepositoryContent(packages, "");

        } catch (CreateFolderStructureException
                | PackageFolderPopulationException
                | LinkFoldersException
                | IOException e) {
            log.error(e.getMessage(), e);
            throw new OrganizePackagesException();
        }
    }

    @Override
    public void populatePackage(PythonPackage packageBag, String folderPath) throws PackageFolderPopulationException {
        if (packageBag.isActive()) {
            String targetFilePath = packageBag.getSource();
            String destinationFilePath = folderPath + separator + packageBag.getName() + separator
                    + packageBag.getName() + "-" + packageBag.getVersion() + ".tar.gz";
            try {
                checkHash(packageBag);
                packageIndexGenerator.addPackageToIndex(packageBag, folderPath + separator + packageBag.getName());
                Files.copy(new File(targetFilePath).toPath(), new File(destinationFilePath).toPath());
            } catch (IOException | Md5MismatchException | CheckSumCalculationException e) {
                log.error("{}: {}", e.getClass(), e.getMessage());
                throw new PackageFolderPopulationException();
            }
        }
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
                cleanDirectory(repositoryGenerationDirectory);
            }
        } catch (DeleteFileException e) {
            log.error(e.getMessage(), e);
            throw new CleanUpAfterSynchronizationException();
        }
    }

    @Override
    public void calculateCheckSum(PythonPackage packageBag) throws CheckSumCalculationException {
        log.debug("Calculating checksum for package: {}", packageBag.toString());
        final File sourceFile = new File(packageBag.getSource());
        final HashMethod hashMethod = packageBag.getRepository().getHashMethod();
        packageBag.setHash(calculateCheckSum(sourceFile, hashMethod));
    }
}

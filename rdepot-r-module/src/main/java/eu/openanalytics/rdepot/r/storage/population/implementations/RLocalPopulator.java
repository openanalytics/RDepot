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
package eu.openanalytics.rdepot.r.storage.population.implementations;

import eu.openanalytics.rdepot.base.PropertiesParser;
import eu.openanalytics.rdepot.base.storage.exceptions.*;
import eu.openanalytics.rdepot.base.storage.implementations.CommonFSLocalStorage;
import eu.openanalytics.rdepot.base.storage.population.implementations.LocalFSPopulator;
import eu.openanalytics.rdepot.base.synchronization.checksums.Checksums;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.storage.PersistentRStorage;
import eu.openanalytics.rdepot.r.storage.binaries.BinLocation;
import eu.openanalytics.rdepot.r.storage.binaries.BinLocationSet;
import eu.openanalytics.rdepot.r.storage.exceptions.*;
import eu.openanalytics.rdepot.r.storage.implementations.RetiredBinary;
import eu.openanalytics.rdepot.r.storage.indexes.RIndexDescriptor;
import eu.openanalytics.rdepot.r.storage.indexes.RIndexGenerator;
import eu.openanalytics.rdepot.r.storage.packagesfile.PackageStringGenerator;
import eu.openanalytics.rdepot.r.storage.packagesfile.PackagesFileDescriptor;
import eu.openanalytics.rdepot.r.storage.packagesfile.PackagesFileDescriptorCreator;
import eu.openanalytics.rdepot.r.storage.population.*;
import eu.openanalytics.rdepot.r.storage.population.utils.VersionComparator;
import eu.openanalytics.rdepot.r.synchronization.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.r.synchronization.checksums.RChecksumResolver;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
@Slf4j
public class RLocalPopulator extends LocalFSPopulator<RRepository, RPackage, PopulatedRPackage> implements RPopulator {

    private static final String PACKAGES = "PACKAGES";
    private static final String ARCHIVE_FOLDER = "Archive";
    private static final String LATEST_FOLDER = "latest";
    private static final String CONTRIB_FOLDER = "contrib";
    private static final String SRC_FOLDER = "src";
    private static final String CURRENT_FOLDER = "current";
    private static final String BIN_FOLDER = "bin";
    private static final String LINUX_FOLDER = "linux";
    private final CommonFSLocalStorage<RPackage> storage;
    private final File repositoryGenerationDirectory;
    private final RIndexGenerator rIndexGenerator;
    private final String snapshot;
    private final PackageStringGenerator packageStringGenerator;
    private final PackagesFileDescriptorCreator packagesFileDescriptorCreator;
    private final RChecksumResolver checksumResolver = new RChecksumResolver();
    private final PersistentRStorage persistentStorage;

    public RLocalPopulator(
            CommonFSLocalStorage<RPackage> storage,
            @Qualifier("packageUploadDirectory") File packageUploadDirectory,
            @Qualifier("repositoryGenerationDirectory") File repositoryGenerationDirectory,
            RIndexGenerator rIndexGenerator,
            @Value("${repository-snapshots}") String snapshot,
            PackageStringGenerator packageStringGenerator,
            PackagesFileDescriptorCreator packagesFileDescriptorCreator,
            PersistentRStorage persistentStorage) {
        super(packageUploadDirectory, repositoryGenerationDirectory, storage);
        this.storage = storage;
        this.repositoryGenerationDirectory = repositoryGenerationDirectory;
        this.rIndexGenerator = rIndexGenerator;
        this.snapshot = snapshot;
        this.packageStringGenerator = packageStringGenerator;
        this.packagesFileDescriptorCreator = packagesFileDescriptorCreator;
        this.persistentStorage = persistentStorage;
    }

    public String resolveLocationForLinuxBinaryGeneratedPath(File dateStampFolder, String separator, String binPath) {
        return dateStampFolder.getAbsolutePath()
                + separator
                + BIN_FOLDER
                + separator
                + LINUX_FOLDER
                + separator
                + binPath;
    }

    private String moveToArchive(PopulatedRPackage packageBag, String location) {
        final File toMoveFile = new File(packageBag.getPopulatedPath());
        final String archivePath = location.replace(separator + LATEST_FOLDER, separator + ARCHIVE_FOLDER);
        final File newFileLocation = new File(archivePath, packageBag.getFileName());
        try {
            FileUtils.moveFile(toMoveFile, newFileLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException("Could not properly move file to Archive!");
        }
        return archivePath;
    }

    private BinLocation pickBinaries(
            BinLocation location, List<RPackage> pickedPackages, String destinationFolderPath, String remotePath)
            throws PackageFolderPopulationException {
        return new BinLocation(
                destinationFolderPath,
                remotePath,
                new LinkedList<>(),
                pickedPackages.stream()
                        .filter(p -> location.packagesToPopulate().contains(p))
                        .toList());
    }

    protected void populatePackageBinLocation(BinLocation binLocation) throws PackageFolderPopulationException {
        log.debug(
                "Populating bin location {} for {} packages...",
                binLocation.location(),
                binLocation.packages().size());
        for (RPackage rPackage : binLocation.packagesToPopulate()) {
            if (rPackage.isActive()) {
                final String populatedPath = populatePackage(rPackage, binLocation.location());
                binLocation.packages().add(new PopulatedRPackage(rPackage, populatedPath));
            }
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
    public SynchronizeRepositoryRequestBody buildSynchronizeRequestBody(
            PopulatedRepositoryContent populatedRepositoryContent,
            List<String> remoteLatestSourcePackages,
            List<String> remoteArchiveSourcePackages,
            MultiValueMap<String, String> remoteLatestBinaryPackages,
            MultiValueMap<String, String> remoteArchiveBinaryPackages,
            Checksums remoteChecksums,
            RRepository repository,
            String versionBefore) {
        final Checksums localChecksums =
                checksumResolver.resolveChecksumsForPopulatedContent(populatedRepositoryContent);

        final String sourceDirectory = StringUtils.substringBetween(
                populatedRepositoryContent.latestDirectoryPath(),
                separator + CURRENT_FOLDER + separator,
                separator + LATEST_FOLDER);

        final List<File> latestSourceToUpload = selectPackagesToUpload(
                remoteLatestSourcePackages,
                remoteChecksums,
                populatedRepositoryContent.latestPackages(),
                localChecksums.toMap(),
                false);
        final List<String> latestSourceToDelete =
                selectPackagesToDelete(remoteLatestSourcePackages, populatedRepositoryContent.latestPackages());

        final List<File> archiveSourceToUpload = selectPackagesToUpload(
                remoteArchiveSourcePackages,
                remoteChecksums,
                populatedRepositoryContent.archivePackages(),
                localChecksums.toMap(),
                true);
        final List<String> archiveSourceToDelete =
                selectPackagesToDelete(remoteArchiveSourcePackages, populatedRepositoryContent.archivePackages());

        // BINARY FILES
        final ToUploadAndDelete toUploadAndDeleteLatest = selectBinariesToUploadAndDelete(
                populatedRepositoryContent.binLatestPackagesPaths(),
                remoteChecksums,
                remoteLatestBinaryPackages,
                localChecksums,
                false);
        final ToUploadAndDelete toUploadAndDeleteArchive = selectBinariesToUploadAndDelete(
                populatedRepositoryContent.binArchivePackagesPaths(),
                remoteChecksums,
                remoteArchiveBinaryPackages,
                localChecksums,
                true);
        final Set<PackagesFileDescriptor> packagesFilesLatest = populatedRepositoryContent.packagesFiles().stream()
                .filter(pf -> !pf.remoteFolder().endsWith(ARCHIVE_FOLDER))
                .collect(Collectors.toSet());
        final Set<PackagesFileDescriptor> packagesFilesArchive = populatedRepositoryContent.packagesFiles().stream()
                .filter(pf -> !packagesFilesLatest.contains(pf))
                .collect(Collectors.toSet());
        final Set<PackagesFileDescriptor> packagesGzFiles = packagesFilesLatest.stream()
                .filter(pf -> pf.localPath().endsWith(".gz"))
                .collect(Collectors.toSet());
        final Set<PackagesFileDescriptor> packagesGzFilesForArchive = packagesFilesArchive.stream()
                .filter(pf -> pf.localPath().endsWith(".gz"))
                .collect(Collectors.toSet());
        packagesFilesLatest.removeAll(packagesGzFiles);
        packagesFilesArchive.removeAll(packagesGzFilesForArchive);

        return new SynchronizeRepositoryRequestBody(
                latestSourceToUpload,
                archiveSourceToUpload,
                latestSourceToDelete,
                archiveSourceToDelete,
                sourceDirectory,
                versionBefore,
                toUploadAndDeleteLatest.binaryPackagesToUpload(),
                toUploadAndDeleteArchive.binaryPackagesToUpload(),
                toUploadAndDeleteLatest.binaryPackagesToDelete(),
                toUploadAndDeleteArchive.binaryPackagesToDelete(),
                packagesFileSetToFileMap(packagesFilesLatest),
                packagesFileSetToFileMap(packagesGzFiles),
                packagesFileSetToFileMap(packagesFilesArchive),
                packagesFileSetToFileMap(packagesGzFilesForArchive),
                localChecksums,
                mapIndexesToFiles(populatedRepositoryContent.indexes(), false),
                mapIndexesToFiles(populatedRepositoryContent.indexes(), true));
    }

    private Map<String, File> packagesFileSetToFileMap(Set<PackagesFileDescriptor> packagesFiles) {
        return packagesFiles.stream()
                .collect(Collectors.toMap(PackagesFileDescriptor::remoteFolder, f -> new File(f.localPath())));
    }

    private Map<String, File> mapIndexesToFiles(List<RIndexDescriptor> indexes, boolean archive) {
        final Map<String, File> indexesMapped = new HashMap<>();

        for (RIndexDescriptor index : indexes) {
            if (archive != index.archive()) {
                continue;
            }
            final File file = new File(index.indexLocalPath());
            if (!file.exists() || !file.isFile()) {
                throw new IllegalStateException(
                        "Index file " + file.getAbsolutePath() + " does not exist or is a directory.");
            }
            indexesMapped.put(index.indexOnRemoteRepoPath(), file);
        }

        return indexesMapped;
    }

    private List<String> selectPackagesToDelete(List<String> remotePackages, List<PopulatedRPackage> localPackages) {
        List<String> toDelete = new ArrayList<>();

        for (String packageName : remotePackages) {
            boolean found = false;
            for (PopulatedRPackage packageBag : localPackages) {
                if (packageBag.getPackageFilename().equals(packageName)) {
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

    /**
     * Populates every package in a generated directory.
     *
     * @return PACKAGES file path in local localStorage
     */
    protected List<PopulatedRPackage> populatePackageFolder(List<RPackage> packages, String folderPath)
            throws PackageFolderPopulationException {
        log.debug("Populating package folder: {} for {} packages...", folderPath, packages.size());
        final List<PopulatedRPackage> populated = new LinkedList<>();
        for (RPackage packageBag : packages) {
            if (packageBag.isActive()) {
                final String populatedPath = populatePackage(packageBag, folderPath);
                populated.add(new PopulatedRPackage(packageBag, populatedPath));
            }
        }
        return populated;
    }

    private record ToUploadAndDelete(
            MultiValueMap<String, File> binaryPackagesToUpload, MultiValueMap<String, String> binaryPackagesToDelete) {}

    private ToUploadAndDelete selectBinariesToUploadAndDelete(
            BinLocationSet binaryPackagesPaths,
            Checksums remoteChecksums,
            MultiValueMap<String, String> remoteBinaryPackages,
            Checksums localChecksums,
            boolean archive) {

        final MultiValueMap<String, File> binaryPackagesToUpload = new LinkedMultiValueMap<>();
        final MultiValueMap<String, String> binaryPackagesToDelete = new LinkedMultiValueMap<>();
        /*
         * The code below reduces package paths so that the repository generation dir
         * and Archive/latest part are cut off. In this way, packages on the remote and
         * local end are represented by the same paths.
         *
         * If, for example, local path was:
         * /opt/rdepot/repositories/5/generated/current/bin/linux/ubuntu/x86_64/v1.0.0/
         * Archive and the remote path was: bin/linux/ubuntu/x86_64/v1.0.0/Archive
         *
         * then both become: bin/linux/ubuntu/x86_64/v1.0.0
         */
        final MultiValueMap<String, String> reducedRemoteBinaryPackagesPaths = new LinkedMultiValueMap<>();
        remoteBinaryPackages.forEach((key, value) -> reducedRemoteBinaryPackagesPaths.addAll(
                StringUtils.substringBefore(key, separator + ARCHIVE_FOLDER), value));

        final Set<String> reducedLocalBinaryPaths = binaryPackagesPaths.getAllRemoteLocations().stream()
                .map(s -> StringUtils.substringBefore(s, "/" + ARCHIVE_FOLDER))
                .collect(Collectors.toSet());
        final Set<String> allPaths = new HashSet<>();
        allPaths.addAll(reducedLocalBinaryPaths);
        allPaths.addAll(reducedRemoteBinaryPackagesPaths.keySet());

        //
        for (String path : allPaths) {
            // if a package is not present locally but is present on the remote repo
            if (!reducedLocalBinaryPaths.contains(path)) {
                Objects.requireNonNull(reducedRemoteBinaryPackagesPaths.get(path))
                        .forEach(packageBag -> binaryPackagesToDelete.add(path, packageBag));
                continue;
            }
            //

            // If there already is such an "architecture/os/r version" repository
            // then we need to select the packages to upload and those to delete
            if (reducedRemoteBinaryPackagesPaths.containsKey(path)) {
                List<File> packagesToUpload = selectPackagesToUpload(
                        Objects.requireNonNull(reducedRemoteBinaryPackagesPaths.get(path)),
                        remoteChecksums,
                        binaryPackagesPaths.getPackagesForRemoteLocation(archive ? path + "/" + ARCHIVE_FOLDER : path),
                        localChecksums.toMap(),
                        archive);
                packagesToUpload.forEach(packageBag -> binaryPackagesToUpload.add(path, packageBag));

                List<String> packagesToDelete = selectPackagesToDelete(
                        Objects.requireNonNull(reducedRemoteBinaryPackagesPaths.get(path)),
                        binaryPackagesPaths.getPackagesForRemoteLocation(archive ? path + "/" + ARCHIVE_FOLDER : path));
                packagesToDelete.forEach(packageBag -> binaryPackagesToDelete.add(path, packageBag));
            } else { // There is no such "architecture/os/r version" repository at all
                // so all packages can be uploaded
                binaryPackagesPaths
                        .getPackagesForRemoteLocation(archive ? path + "/" + ARCHIVE_FOLDER : path)
                        .forEach(packageBag ->
                                binaryPackagesToUpload.add(path, new File(packageBag.getPopulatedPath())));
            }
        }
        return new ToUploadAndDelete(binaryPackagesToUpload, binaryPackagesToDelete);
    }

    /**
     * Creates two subdirectories in the repository's generated directory:
     * - Archive/ - for packages to be archived
     * - latest/ - for the latest packages (to make it easier to iterate over them)
     * @param path repository generated dir (e.g. {generationDir}/{repositoryId}/{datestamp}/src/contrib/)
     */
    private void createTemporaryFoldersForLatestAndArchive(String path) throws CreateFolderStructureException {
        log.debug("Creating temporary directories for latest and Archive: {}", path);
        final File latest = new File(storage.createFolderStructure(path + separator + LATEST_FOLDER));
        final File archive = new File(storage.createFolderStructure(path + separator + ARCHIVE_FOLDER));

        try {
            final File packagesLatest =
                    Files.createFile(latest.toPath().resolve(PACKAGES)).toFile();
            final File packagesArchive =
                    Files.createFile(archive.toPath().resolve(PACKAGES)).toFile();

            storage.gzipFile(packagesLatest.getAbsolutePath());
            log.debug("PACKAGES file for latest packages created and gzipped: {}", packagesLatest.getAbsolutePath());
            storage.gzipFile(packagesArchive.getAbsolutePath());
            log.debug("PACKAGES file for Archive packages created and gzipped: {}", packagesArchive.getAbsolutePath());
        } catch (IOException | GzipFileException e) {
            log.error("Could not create PACKAGES file", e);
            throw new CreateFolderStructureException();
        }
    }

    /**
     * Creates a directory structure for binary packages. The structure will follow
     * the following pattern:
     * {rdepotGeneratedDir}/{repositoryId}/{datestamp}/bin/{os}/{distro}/{architecture}/{rVersion}
     * Inside there will also be an "Archive" directory with all archival versions
     * of packages. Those will be stored in a <b>flat</b> structure, meaning that
     * for example for "accrued" package it will not be stored in its dedicated
     * directory like it is in the repo app:<br/>
     * <br/>
     * Archive/<br/>
     * &emsp;PACKAGES<br/>
     * &emsp;PACKAGES.gz<br/>
     * &emsp;accrued/<br/>
     * &emsp;&emsp;accrued_1.2.tar.gz<br/>
     * &emsp;&emsp;accrued_1.3.tar.gz<br/>
     * <br/>
     * <i>but instead:</i><br/>
     * <br/>
     * Archive/<br/>
     * &emsp;PACKAGES<br/>
     * &emsp;PACKAGES.gz<br/>
     * &emsp;accrued_1.2.tar.gz<br/>
     * &emsp;accrued_1.3.tar.gz<br/>
     *
     * @param packages   all binary packages to populate
     * @param platforms  all binary platforms (including those for which there are
     *                   currently no packages)
     * @param repository repository to create the structure for
     * @param dateStamp  current datestamp for generation
     */
    protected BinLocationSet createBinaryFolderStructureForGeneration(
            List<RPackage> packages, List<String> platforms, RRepository repository, String dateStamp)
            throws CreateFolderStructureException {
        File dateStampFolder = null;
        try {
            dateStampFolder = new File(storage.createFolderStructure(repositoryGenerationDirectory.getAbsolutePath()
                    + separator
                    + repository.getId()
                    + separator
                    + dateStamp));

            log.debug("Datestamp folder created for repository {}: {}", repository, dateStampFolder.getAbsolutePath());
            final BinLocationSet packagesLocations = new BinLocationSet();
            final String binPathPrefix = BIN_FOLDER + separator + LINUX_FOLDER;

            for (String platform : platforms) {
                final String binFolderStructure = StringUtils.substringAfter(platform, binPathPrefix + separator);
                final String actualPath =
                        resolveLocationForLinuxBinaryGeneratedPath(dateStampFolder, separator, binFolderStructure);
                storage.createFolderStructure(actualPath);
                log.debug("Binary folder created for platform {}: {}", platform, actualPath);
                packagesLocations.addEmptyLocationToPopulateIfNotExists(
                        actualPath, binPathPrefix + separator + binFolderStructure);
            }

            for (RPackage packageBag : packages) {
                log.debug("Populating package {}...", packageBag);
                final String binFolderStructure = packageBag.getDistribution()
                        + separator
                        + packageBag.getArchitecture()
                        + separator
                        + resolveRVersionToFolderName(packageBag.getRVersion());
                final String actualPath =
                        resolveLocationForLinuxBinaryGeneratedPath(dateStampFolder, separator, binFolderStructure);
                storage.createFolderStructure(actualPath);
                log.debug("Binary folder created for package {}: {}", packageBag, actualPath);
                packagesLocations.addPackageToLocationToPopulate(
                        actualPath, binPathPrefix + separator + binFolderStructure, packageBag);
            }

            return packagesLocations;
        } catch (CreateFolderStructureException e) {
            if (dateStampFolder != null) {
                try {
                    storage.deleteFile(dateStampFolder.getAbsolutePath());
                } catch (DeleteFileException dfe) {
                    log.error(dfe.getMessage(), dfe);
                }
            }
            throw e;
        }
    }

    @Override
    public PopulatedRepositoryContent organizePackagesInStorage(
            String dateStamp,
            List<RPackage> allSourcePackages,
            List<RPackage> latestSourcePackages,
            List<RPackage> archiveSourcePackages,
            List<RPackage> allBinaryPackages,
            List<RPackage> latestBinaryPackages,
            List<RPackage> archiveBinaryPackages,
            List<String> binaryPlatforms,
            RRepository repository)
            throws OrganizePackagesException {
        log.debug("Packages population started for R repository {}...", repository);
        try {
            final BinLocationSet binFoldersPaths =
                    createBinaryFolderStructureForGeneration(allBinaryPackages, binaryPlatforms, repository, dateStamp);

            final File target = linkCurrentFolderToGeneratedFolder(repository, dateStamp);

            final Path repoPathForSources = target.toPath().resolve(SRC_FOLDER).resolve(CONTRIB_FOLDER);
            final String latestSourceFolderPath = repoPathForSources + separator + LATEST_FOLDER;
            final String archiveSourceFolderPath = repoPathForSources + separator + ARCHIVE_FOLDER;

            // Create Archive/ and latest/ for source packages
            createTemporaryFoldersForLatestAndArchive(repoPathForSources.toString());
            final String remoteFolderLatest = SRC_FOLDER + separator + CONTRIB_FOLDER;
            final String remoteFolderArchive = SRC_FOLDER + separator + CONTRIB_FOLDER + separator + ARCHIVE_FOLDER;

            final List<PopulatedRPackage> populatedLatestSourcePackages =
                    populatePackageFolder(latestSourcePackages, latestSourceFolderPath);
            Set<PackagesFileDescriptor> packagesFiles = new HashSet<>(
                    packagesFileDescriptorCreator.createDescriptors(remoteFolderLatest, latestSourceFolderPath));

            final List<PopulatedRPackage> populatedArchiveSourcePackages =
                    populatePackageFolder(archiveSourcePackages, archiveSourceFolderPath);
            packagesFiles.addAll(
                    packagesFileDescriptorCreator.createDescriptors(remoteFolderArchive, archiveSourceFolderPath));

            final BinLocationSet binLatestLocations = new BinLocationSet();
            final BinLocationSet binArchiveLocations = new BinLocationSet();

            for (BinLocation location : binFoldersPaths.getAllBinLocations()) {
                String folderPath = location.location();
                createTemporaryFoldersForLatestAndArchive(folderPath);

                // Created temporary folders
                final String latestFolderPath = folderPath + separator + LATEST_FOLDER;
                final String archiveFolderPath = folderPath + separator + ARCHIVE_FOLDER;
                final String remoteFolderPathLatest = location.remoteLocation();
                final String remoteFolderPathArchive = location.remoteLocation() + "/" + ARCHIVE_FOLDER;
                // Before doing this all binaries are together in one collection (both latest
                // and archive).
                // this splits them apart
                final BinLocation binariesToPopulateLatest =
                        pickBinaries(location, latestBinaryPackages, latestFolderPath, remoteFolderPathLatest);
                populatePackageBinLocation(binariesToPopulateLatest);
                binLatestLocations.addLocation(binariesToPopulateLatest);
                packagesFiles.addAll(
                        packagesFileDescriptorCreator.createDescriptors(remoteFolderPathLatest, latestFolderPath));

                final BinLocation binariesToPopulateArchive =
                        pickBinaries(location, archiveBinaryPackages, archiveFolderPath, remoteFolderPathArchive);
                populatePackageBinLocation(binariesToPopulateArchive);
                binArchiveLocations.addLocation(binariesToPopulateArchive);
                packagesFiles.addAll(
                        packagesFileDescriptorCreator.createDescriptors(remoteFolderPathArchive, archiveFolderPath));
            }

            if (repository.isRedirectToSource()) {
                packagesFiles = redirectToSource(
                        populatedLatestSourcePackages,
                        populatedArchiveSourcePackages,
                        binLatestLocations,
                        binArchiveLocations,
                        packagesFiles);
            }

            log.debug("Packages population completed for R repository {}.", repository);
            return new PopulatedRepositoryContent(
                    populatedLatestSourcePackages,
                    populatedArchiveSourcePackages,
                    latestSourceFolderPath,
                    archiveSourceFolderPath,
                    binLatestLocations,
                    binArchiveLocations,
                    rIndexGenerator.createIndexes(
                            repository,
                            latestSourceFolderPath,
                            archiveSourceFolderPath,
                            latestSourcePackages,
                            archiveSourcePackages,
                            binArchiveLocations,
                            binLatestLocations),
                    packagesFiles);
        } catch (CreateFolderStructureException
                | PackageFolderPopulationException
                | LinkFoldersException
                | GeneratePackagesFileException
                | ContentEditException
                | Md5SumCalculationException e) {
            log.error(e.getMessage(), e);
            throw new OrganizePackagesException();
        }
    }

    /**
     * "Redirect to source" means that in case there is a newer, corresponding
     * source version of a binary package, the source version will be the preferred
     * choice. The "older" latest binary package will be moved from latest to
     * archive. The latest binary PACKAGES will contain the latest source package.
     * Also, archived source packages that do not have a corresponding binary of the
     * same version, will be added to the binary archive PACKAGES file.
     *
     * <h3>Example</h3> With the following <i>source</i> packages in the
     * repository:<br/>
     * <ul>
     * <li>foo.0.9.tar.gz</li>
     * <li>foo.1.0.tar.gz</li>
     * <li>foo.1.2.tar.gz</li>
     * </ul>
     * and the following <i>binary</i> packages in the repository:<br/>
     * <ul>
     * <li>foo.0.8.tar.gz</li>
     * <li>foo.1.0.tar.gz</li>
     * <li>foo.1.1.tar.gz</li>
     * </ul>
     * <br/>
     * The resulting structure will look as follows:<br/>
     * <br/>
     * <h4>Source Packages</h4>
     * <h5>Latest</h5> <b>Packages in folder:</b><br/>
     * <ul>
     * <li>foo.1.2.tar.gz</li>
     * </ul>
     * <b>Packages in PACKAGES file:</b><br/>
     * <ul>
     * <li>foo.1.2.tar.gz</li>
     * </ul>
     * <h5>Archive Packages</h5> <b>Packages in folder:</b><br/>
     * <ul>
     * <li>foo.1.0.tar.gz</li>
     * <li>foo.0.9.tar.gz</li>
     * </ul>
     * <b>Packages in PACKAGES file:</b><br/>
     * <ul>
     * <li>foo.1.0.tar.gz</li>
     * <li>foo.0.9.tar.gz</li>
     * </ul>
     * <br/>
     * <br/>
     * <h4>Binary Packages</h4>
     * <h5>Latest</h5> <b>Packages in folder:</b><br/>
     * <br/>
     * <i>NONE</i><br/>
     * <br/>
     * <b>Packages in PACKAGES file:</b><br/>
     * <ul>
     * <li>foo.1.2.tar.gz</li>
     * </ul>
     * <h5>Archive</h5> <b>Packages in folder:</b><br/>
     * <ul>
     * <li>foo.0.8.tar.gz <i>(bin)</i></li>
     * <li>foo.1.0.tar.gz <i>(bin)</i> <- we have both source and binary version but
     * in binary folder, the binary package is preferred for older versions</li>
     * <li>foo.1.1.tar.gz <i>(bin)</i></li>
     * </ul>
     * <b>Packages in PACKAGES file:</b><br/>
     * <ul>
     * <li>foo.0.8.tar.gz <i>(bin)</i></li>
     * <li>foo.0.9.tar.gz <i>(src)<i/></li>
     * <li>foo.1.0.tar.gz <i>(bin)</i> <- we have both source and binary version but
     * in binary folder, * the binary package is preferred for older versions</li>
     * <li>foo.1.1.tar.gz <i>(bin)</i></li>
     * </ul>
     * <br/>
     *
     * @return updated PACKAGES files
     */
    private Set<PackagesFileDescriptor> redirectToSource(
            List<PopulatedRPackage> populatedLatestSourcePackages,
            List<PopulatedRPackage> populatedArchiveSourcePackages,
            BinLocationSet binLatestLocations,
            BinLocationSet binArchiveLocations,
            Set<PackagesFileDescriptor> packagesFiles)
            throws GeneratePackagesFileException, Md5SumCalculationException {
        log.debug("\"Redirect to source\" option enabled for repository.");
        final Set<RetiredBinary> retiredBinaries =
                selectBinariesToRetire(populatedLatestSourcePackages, binLatestLocations);
        final BinLocationSet binLatestLocationsForAlteredPackagesFile = new BinLocationSet(binLatestLocations);
        for (RetiredBinary binary : retiredBinaries) {
            log.debug("Retiring binary {}", binary);
            binLatestLocations.removePackageFromLocation(
                    binary.binLocationOfOutdatedPackage().location(), binary.outdatedBin());
            final String archiveLocation = moveToArchive(
                    binary.outdatedBin(), binary.binLocationOfOutdatedPackage().location());
            binary.outdatedBin()
                    .setPopulatedPath(
                            archiveLocation + "/" + binary.outdatedBin().getFileName());
            binArchiveLocations.addPackageToLocationIfExists(archiveLocation, binary.outdatedBin());
            binLatestLocationsForAlteredPackagesFile.removePackageFromLocation(
                    binary.binLocationOfOutdatedPackage().location(), binary.outdatedBin());
            binLatestLocationsForAlteredPackagesFile.addPackageToLocationIfExists(
                    binary.binLocationOfOutdatedPackage().location(), binary.latestSource());
        }

        final BinLocationSet binArchiveLocationsForAlteredPackagesFile = new BinLocationSet(binArchiveLocations);
        for (BinLocation binLocation : binArchiveLocations.getAllBinLocations()) {
            final Map<String, PopulatedRPackage> versionPackageMap = new HashMap<>();
            binLocation.packages().forEach(p -> versionPackageMap.put(p.getName() + p.getVersion(), p));

            for (PopulatedRPackage sourcePackage : populatedArchiveSourcePackages) {
                if (!versionPackageMap.containsKey(sourcePackage.getName() + sourcePackage.getVersion())) {
                    binArchiveLocationsForAlteredPackagesFile.addPackageToLocationToPopulate(
                            binLocation.location(), binLocation.remoteLocation(), sourcePackage);
                }
            }
        }
        final Set<PackagesFileDescriptor> packagesFilesToRecalculate = new HashSet<>();
        final BinLocationSet allBinLocationsForAlteredPackageFiles =
                new BinLocationSet(binLatestLocationsForAlteredPackagesFile);
        allBinLocationsForAlteredPackageFiles.addLocations(
                binArchiveLocationsForAlteredPackagesFile.getAllBinLocations());

        for (BinLocation location : allBinLocationsForAlteredPackageFiles.getAllBinLocations()) {
            selectPackagesFilesForLocation(packagesFiles, location.remoteLocation())
                    .forEach(packagesFilesToRecalculate::add);
        }

        packageStringGenerator.generatePackagesFiles(binLatestLocationsForAlteredPackagesFile);
        packageStringGenerator.generatePackagesFiles(binArchiveLocationsForAlteredPackagesFile);
        return recalculateChecksums(packagesFilesToRecalculate, packagesFiles);
    }

    private Stream<PackagesFileDescriptor> selectPackagesFilesForLocation(
            Set<PackagesFileDescriptor> packagesFiles, String location) {
        return packagesFiles.stream()
                .filter(f ->
                        f.remoteFolder().equals(location) || f.remoteFolder().equals(location + "/Archive"));
    }

    private Set<PackagesFileDescriptor> recalculateChecksums(
            Set<PackagesFileDescriptor> toRecalculate, Set<PackagesFileDescriptor> allDescriptors)
            throws Md5SumCalculationException {
        final Set<PackagesFileDescriptor> recalculated =
                allDescriptors.stream().filter(d -> !toRecalculate.contains(d)).collect(Collectors.toSet());
        for (PackagesFileDescriptor descriptor : toRecalculate) {
            final String newChecksum = storage.calculateMd5Sum(descriptor.localPath());
            recalculated.add(
                    new PackagesFileDescriptor(descriptor.remoteFolder(), descriptor.localPath(), newChecksum));
        }
        return recalculated;
    }

    private Set<RetiredBinary> selectBinariesToRetire(
            List<PopulatedRPackage> latestSourcePackages, BinLocationSet binLatestLocations) {
        final Set<RetiredBinary> retiredBinaries = new HashSet<>();

        for (BinLocation binLocation : binLatestLocations.getAllBinLocations()) {
            for (PopulatedRPackage latestBinaryPackage : binLocation.packages()) {
                VersionComparator.findNewerPackage(
                                latestSourcePackages, latestBinaryPackage.getName(), latestBinaryPackage.getVersion())
                        .map(p -> new RetiredBinary(p, latestBinaryPackage, binLocation))
                        .ifPresent(retiredBinaries::add);
            }
        }
        return retiredBinaries;
    }

    @Override
    public void cleanUpAfterSynchronization(PopulatedRepositoryContent populatedRepositoryContent)
            throws CleanUpAfterSynchronizationException {
        try {
            storage.deleteFile(populatedRepositoryContent.latestDirectoryPath());
            storage.deleteFile(populatedRepositoryContent.archiveDirectoryPath());

            Set<String> binaryDirPaths = new HashSet<>();
            binaryDirPaths.addAll(
                    populatedRepositoryContent.binArchivePackagesPaths().getAllBinLocationPaths());
            binaryDirPaths.addAll(
                    populatedRepositoryContent.binArchivePackagesPaths().getAllBinLocationPaths());

            for (String path : binaryDirPaths) {
                storage.deleteFile(path);
            }

            if (!Boolean.parseBoolean(snapshot)) {
                storage.cleanDirectory(repositoryGenerationDirectory.getAbsolutePath());
            }
        } catch (DeleteFileException e) {
            log.error(e.getMessage(), e);
            throw new CleanUpAfterSynchronizationException();
        }
    }

    @Override
    public Properties getPropertiesFromExtractedFile(final List<File> extractedFiles)
            throws ReadPackageDescriptionException {
        if (extractedFiles.size() != 1) {
            log.error("There should be only one main extracted directory for an R package.");
            throw new ReadRPackageDescriptionException();
        }
        final File extractedFile = extractedFiles.get(0);
        try {
            return new PropertiesParser(new File(extractedFile.getAbsolutePath() + separator + "DESCRIPTION"));
        } catch (IOException e) {
            try {
                storage.deleteFile(extractedFile.getParentFile().getAbsolutePath());
            } catch (DeleteFileException dfe) {
                log.error(dfe.getMessage(), dfe);
            }
            log.error(e.getMessage(), e);
            throw new ReadRPackageDescriptionException();
        }
    }

    /**
     * Copies a package from its upload directory to generated directory. If the
     * package is a source package, it also adds it to the PACKAGES file.
     *
     * @param packageBag package to populate
     * @param folderPath population directory path (e.g. "archive" or "latest")
     * @return path to populated package
     */
    @Override
    public String populatePackage(RPackage packageBag, String folderPath) throws PackageFolderPopulationException {
        log.debug("Populating package {} in directory {}...", packageBag, folderPath);
        final String originalFilePath = packageBag.getSource();
        final String[] packageFilenameTokens = originalFilePath.split(separator);
        if (packageFilenameTokens.length < 1) {
            throw new IllegalStateException("Invalid package pth: " + packageBag.getSource());
        }
        final String destinationFilePath = folderPath + separator + packageBag.getPackageFilename();

        try {
            final File populatedFile = new File(destinationFilePath);
            final File downloaded = persistentStorage.downloadFile(Path.of(originalFilePath));
            Files.copy(downloaded.toPath(), populatedFile.toPath());
            persistentStorage.recycleDownloadedFile(downloaded);
            final String calculatedSum = storage.calculateMd5Sum(destinationFilePath);
            log.debug("Calculated checksum for package {}: {}", packageBag, calculatedSum);
            if (!packageBag.getMd5sum().equals(calculatedSum)) {
                throw new Md5MismatchException();
            }

            /*
             * If package is a source package then we simply add it to the PACKAGES file. If
             * package is a binary package then if the "redirect to source" feature is
             * enabled, we do not want to add it, as the PACKAGES file will be regenerated
             * while resolving redirection later.
             */
            if (!packageBag.isBinary() || !packageBag.getRepository().isRedirectToSource()) {
                final String packagesFilePath = folderPath + separator + "PACKAGES";
                packageStringGenerator.addPackageToPackagesFile(packageBag, packagesFilePath);
            }
            return populatedFile.getAbsolutePath();
        } catch (IOException
                | Md5MismatchException
                | Md5SumCalculationException
                | GeneratePackagesFileException
                | DownloadFileException
                | DeleteFileException e) {
            log.error("{}: {}", e.getClass(), e.getMessage());
            throw new PackageFolderPopulationException();
        }
    }

    @Override
    protected List<File> selectPackagesToUpload(
            List<String> remotePackages,
            Checksums remoteChecksums,
            List<PopulatedRPackage> localPackages,
            Map<String, String> localChecksums,
            boolean archive) {

        return localPackages.stream()
                .filter(p -> !remotePackages.contains(p.getPackageFilename())
                        || !remoteChecksums.contains(
                                localChecksums.get(p.getPopulatedPath()), p.getPackageFolderPath(archive)))
                .map(p -> new File(p.getPopulatedPath()))
                .toList();
    }
}

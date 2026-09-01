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
package eu.openanalytics.rdepot.test.unit.synchronization;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import eu.openanalytics.rdepot.base.synchronization.checksums.Checksum;
import eu.openanalytics.rdepot.base.synchronization.checksums.Checksums;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.manuals.ManualGenerator;
import eu.openanalytics.rdepot.r.manuals.implementations.fs.LocalFSManualGenerator;
import eu.openanalytics.rdepot.r.storage.binaries.BinLocation;
import eu.openanalytics.rdepot.r.storage.binaries.BinLocationSet;
import eu.openanalytics.rdepot.r.storage.implementations.RFSLocalStorage;
import eu.openanalytics.rdepot.r.storage.implementations.RLocalFSPersistentStorage;
import eu.openanalytics.rdepot.r.storage.indexes.*;
import eu.openanalytics.rdepot.r.storage.packagesfile.PackageStringGenerator;
import eu.openanalytics.rdepot.r.storage.packagesfile.PackagesFileDescriptor;
import eu.openanalytics.rdepot.r.storage.packagesfile.PackagesFileDescriptorCreator;
import eu.openanalytics.rdepot.r.storage.population.PopulatedRPackage;
import eu.openanalytics.rdepot.r.storage.population.PopulatedRepositoryContent;
import eu.openanalytics.rdepot.r.storage.population.implementations.RLocalPopulator;
import eu.openanalytics.rdepot.r.synchronization.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.r.synchronization.partitioning.RRequestBodyPartitioner;
import eu.openanalytics.rdepot.r.synchronization.partitioning.structs.ChunkedRequestBody;
import eu.openanalytics.rdepot.test.fixture.RPackageTestFixture;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;

@ExtendWith(MockitoExtension.class)
public class RLocalPopulatorTest {

    private static final String EXPECTED_LATEST_SOURCE_INDEX_REDIRECT_TO_SOURCE;

    static {
        try {
            EXPECTED_LATEST_SOURCE_INDEX_REDIRECT_TO_SOURCE =
                    Files.readString(Paths.get("src/test/resources/templates/SOURCE_INDEX_REDIRECT_TO_SOURCE.html"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String EXPECTED_LATEST_BINARY_INDEX_REDIRECT_TO_SOURCE;

    static {
        try {
            EXPECTED_LATEST_BINARY_INDEX_REDIRECT_TO_SOURCE =
                    Files.readString(Paths.get("src/test/resources/templates/BINARY_INDEX_REDIRECT_TO_SOURCE.html"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String EXPECTED_ARCHIVE_BINARY_INDEX_REDIRECT_TO_SOURCE;

    static {
        try {
            EXPECTED_ARCHIVE_BINARY_INDEX_REDIRECT_TO_SOURCE = Files.readString(
                    Paths.get("src/test/resources/templates/ARCHIVE_BINARY_INDEX_REDIRECT_TO_SOURCE.html"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @InjectMocks
    private RLocalPopulator rLocalPopulator;

    @InjectMocks
    private StaticMessageResolver staticMessageResolver;

    @Spy
    private final RFSLocalStorage storage = new RFSLocalStorage();

    @Spy
    private final RLocalFSPersistentStorage persistentStorage =
            new RLocalFSPersistentStorage(new File("src/test/resources/unit/storage_tests/rdepot"));

    private final RRequestBodyPartitioner partitioner = new RRequestBodyPartitioner();

    @Mock
    private MessageSource ms;

    @Spy
    private final File repositoryGenerationDirectory = new File("/tmp/rdepot-unit-tests/generated");

    @Spy
    private RIndexGenerator rIndexGenerator = new RIndexGenerator(
            new RRepositoryIndexGenerator(
                    new FileSystemResource("src/main/resources/templates/r/index_template.html"),
                    new FileSystemResource("src/main/resources/templates/r/index_anchor_template.html"),
                    storage),
            new ArchiveIndexGenerator(
                    new FileSystemResource("src/main/resources/templates/r/archive_template.html"),
                    new FileSystemResource("src/main/resources/templates/r/archive_anchor_template.html"),
                    storage),
            new RPackageIndexGenerator(
                    new FileSystemResource("src/main/resources/templates/r/package_template.html"),
                    new FileSystemResource("src/main/resources/templates/r/package_anchor_template.html"),
                    storage));

    @Spy
    private PackageStringGenerator packageStringGenerator = new PackageStringGenerator(storage);

    @Spy
    private ManualGenerator manualGenerator = new LocalFSManualGenerator();

    @Spy
    private PackagesFileDescriptorCreator packagesFileDescriptorCreator = new PackagesFileDescriptorCreator(storage);

    public RLocalPopulatorTest() throws IOException {}

    @BeforeEach
    public void before() throws Exception {
        if (Files.exists(repositoryGenerationDirectory.toPath())) {
            FileUtils.forceDelete(repositoryGenerationDirectory);
        }
        Files.createDirectories(repositoryGenerationDirectory.toPath());
        FieldUtils.writeField(rLocalPopulator, "snapshot", "true", true);
        FieldUtils.writeField(rLocalPopulator, "repositoryGenerationDirectory", repositoryGenerationDirectory, true);

        final File exampleDir = new File("/tmp/rdepot-unit-tests/example-repository/");
        if (exampleDir.exists()) {
            FileUtils.forceDelete(exampleDir);
        }
        FileUtils.copyDirectory(new File("src/test/resources/unit/test_packages_population"), exampleDir);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        final File exampleDir = new File("/tmp/rdepot-unit-tests");
        if (exampleDir.exists()) {
            FileUtils.forceDelete(exampleDir);
        }
    }

    private static final String EXPECTED_LATEST_SOURCE_INDEX;

    static {
        try {
            EXPECTED_LATEST_SOURCE_INDEX =
                    Files.readString(Paths.get("src/test/resources/templates/EXPECTED_LATEST_SOURCE_INDEX.html"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String EXPECTED_ARCHIVE_SOURCE_INDEX;

    static {
        try {
            EXPECTED_ARCHIVE_SOURCE_INDEX =
                    Files.readString(Paths.get("src/test/resources/templates/EXPECTED_ARCHIVE_SOURCE_INDEX.html"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String EXPECTED_QSORT_ARCHIVE_SOURCE_INDEX;

    static {
        try {
            EXPECTED_QSORT_ARCHIVE_SOURCE_INDEX = Files.readString(
                    Paths.get("src/test/resources/templates/EXPECTED_QSORT_ARCHIVE_SOURCE_INDEX.html"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String EXPECTED_LATEST_BINARY_INDEX;

    static {
        try {
            EXPECTED_LATEST_BINARY_INDEX =
                    Files.readString(Paths.get("src/test/resources/templates/EXPECTED_LATEST_BINARY_INDEX.html"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String EXPECTED_ARCHIVE_BINARY_INDEX;

    static {
        try {
            EXPECTED_ARCHIVE_BINARY_INDEX =
                    Files.readString(Paths.get("src/test/resources/templates/EXPECTED_ARCHIVE_BINARY_INDEX.html"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String EXPECTED_ARCHIVE_PLYR_BINARY_INDEX;

    static {
        try {
            EXPECTED_ARCHIVE_PLYR_BINARY_INDEX =
                    Files.readString(Paths.get("src/test/resources/templates/EXPECTED_ARCHIVE_PLYR_BINARY_INDEX.html"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final List<String> PLATFORMS = List.of("bin/linux/centos8/x86_64/4.5");

    @Test
    public void generatePackagesFiles() throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        Assertions.assertNotNull(populatedContent);
        Assertions.assertEquals(8, populatedContent.packagesFiles().size(), "Invalid number of PACKAGES files");
    }

    @Test
    public void generatePackagesFiles_forLatestSourcePackages() throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        Assertions.assertNotNull(populatedContent);
        final ArrayList<PackagesFileDescriptor> packagesFiles =
                new ArrayList<>(populatedContent.packagesFiles().stream()
                        .filter(pfd -> pfd.localPath().contains("src/contrib/latest"))
                        .toList());
        Assertions.assertEquals(2, packagesFiles.size(), "Invalid number of PACKAGES files for latest source packages");
        assertLatestSourcePackagesFiles(packagesFiles);
    }

    private void assertLatestSourcePackagesFiles(ArrayList<PackagesFileDescriptor> packagesFiles) throws Exception {
        packagesFiles.sort(Comparator.comparing(PackagesFileDescriptor::getSubPath));
        final PackagesFileDescriptor packagesFile = packagesFiles.get(0);
        final PackagesFileDescriptor packagesGzFile = packagesFiles.get(1);

        Assertions.assertEquals("src/contrib", packagesFile.remoteFolder(), "Invalid remote folder for PACKAGES file.");
        Assertions.assertEquals(
                "src/contrib", packagesGzFile.remoteFolder(), "Invalid remote folder for PACKAGES.gz file.");

        final String expectedLocalPath = "/tmp/rdepot-unit-tests/generated/0/current/src/contrib/latest/PACKAGES";
        final String expectedGzLocalPath = expectedLocalPath + ".gz";
        final String expectedMd5Sum = "38bbc3386ce60554842d4e41da5cbc7a";
        final String expectedGzMd5Sum = "3d58176236841e1969581b65e8cfcad5";
        final String expectedContent =
                """
                Package: plyr
                Version: 1.8.8
                License: MIT + file LICENSE
                MD5sum: 0a22da16605ee765e7d4f1efc9f7a61f
                NeedsCompilation: no

                Package: qsort
                Version: 0.2.3
                License: GPL-3
                MD5sum: 3204109d62ec7ff8e44bd15a989fc8b1
                NeedsCompilation: no""";

        Assertions.assertEquals(expectedLocalPath, packagesFile.localPath(), "Invalid PACKAGES file local path.");
        Assertions.assertEquals(
                expectedGzLocalPath, packagesGzFile.localPath(), "Invalid PACKAGES.gz file local path.");
        Assertions.assertEquals(expectedMd5Sum, packagesFile.checksum(), "Invalid checksum for PACKAGES file");
        Assertions.assertEquals(expectedGzMd5Sum, packagesGzFile.checksum(), "Invalid checksum for PACKAGES.gz file");
        Assertions.assertEquals(
                expectedContent,
                Files.readString(Path.of(packagesFile.localPath())),
                "Invalid content of PACKAGES file.");
    }

    @Test
    public void generatePackagesFiles_forArchiveBinaryPackages() throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        Assertions.assertNotNull(populatedContent);
        final ArrayList<PackagesFileDescriptor> packagesFiles =
                new ArrayList<>(populatedContent.packagesFiles().stream()
                        .filter(pfd -> pfd.localPath().contains("bin/linux/centos8/x86_64/4.5/Archive"))
                        .toList());
        Assertions.assertEquals(2, packagesFiles.size(), "Invalid number of PACKAGES files for latest source packages");
        assertArchiveBinaryPackagesFiles(packagesFiles);
    }

    private void assertArchiveBinaryPackagesFiles(ArrayList<PackagesFileDescriptor> packagesFiles) throws Exception {
        packagesFiles.sort(Comparator.comparing(PackagesFileDescriptor::getSubPath));
        final PackagesFileDescriptor packagesFile = packagesFiles.get(0);
        final PackagesFileDescriptor packagesGzFile = packagesFiles.get(1);

        Assertions.assertEquals(
                "bin/linux/centos8/x86_64/4.5/Archive",
                packagesFile.remoteFolder(),
                "Invalid remote folder for PACKAGES file.");
        Assertions.assertEquals(
                "bin/linux/centos8/x86_64/4.5/Archive",
                packagesGzFile.remoteFolder(),
                "Invalid remote folder for PACKAGES.gz file.");

        final String expectedLocalPath =
                "/tmp/rdepot-unit-tests/generated/0/12082025/bin/linux/centos8/x86_64/4.5/Archive/PACKAGES";
        final String expectedGzLocalPath = expectedLocalPath + ".gz";
        final String expectedMd5Sum = "6ed1e715af055e781d0d94c97936e1d3";
        final String expectedGzMd5Sum = "b79124e5659bba3c9c4282afd3dc27c5";
        final String expectedContent =
                """
                Package: plyr
                Version: 1.8.6
                License: MIT + file LICENSE
                MD5sum: 6a9c2acfd924f2fb626d54168120fa08
                NeedsCompilation: no
                Built: R 4.5.1; x86_64-pc-linux-gnu; 2025-08-11 09:59:00 UTC; unix

                Package: plyr
                Version: 1.8.1
                License: MIT + file LICENSE
                MD5sum: a8b2d2284d56ab1839728040d463a360
                NeedsCompilation: no
                Built: R 4.5.1; x86_64-pc-linux-gnu; 2025-08-11 09:59:00 UTC; unix""";

        Assertions.assertEquals(expectedLocalPath, packagesFile.localPath(), "Invalid PACKAGES file local path.");
        Assertions.assertEquals(
                expectedGzLocalPath, packagesGzFile.localPath(), "Invalid PACKAGES.gz file local path.");
        Assertions.assertEquals(expectedMd5Sum, packagesFile.checksum(), "Invalid checksum for PACKAGES file");
        Assertions.assertEquals(expectedGzMd5Sum, packagesGzFile.checksum(), "Invalid checksum for PACKAGES.gz file");
        Assertions.assertEquals(
                expectedContent,
                Files.readString(Path.of(packagesFile.localPath())),
                "Invalid content of PACKAGES file.");
    }

    @Test
    public void generatePackagesFiles_forLatestBinaryPackages() throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        Assertions.assertNotNull(populatedContent);
        final ArrayList<PackagesFileDescriptor> packagesFiles =
                new ArrayList<>(populatedContent.packagesFiles().stream()
                        .filter(pfd -> pfd.localPath().contains("bin/linux/centos8/x86_64/4.5/latest"))
                        .toList());
        Assertions.assertEquals(2, packagesFiles.size(), "Invalid number of PACKAGES files for latest source packages");
        assertLatestBinaryPackagesFiles(packagesFiles);
    }

    private void assertLatestBinaryPackagesFiles(ArrayList<PackagesFileDescriptor> packagesFiles) throws Exception {
        packagesFiles.sort(Comparator.comparing(PackagesFileDescriptor::getSubPath));
        final PackagesFileDescriptor packagesFile = packagesFiles.get(0);
        final PackagesFileDescriptor packagesGzFile = packagesFiles.get(1);

        Assertions.assertEquals(
                "bin/linux/centos8/x86_64/4.5",
                packagesFile.remoteFolder(),
                "Invalid remote folder for PACKAGES file.");
        Assertions.assertEquals(
                "bin/linux/centos8/x86_64/4.5",
                packagesGzFile.remoteFolder(),
                "Invalid remote folder for PACKAGES.gz file.");

        final String expectedLocalPath =
                "/tmp/rdepot-unit-tests/generated/0/12082025/bin/linux/centos8/x86_64/4.5/latest/PACKAGES";
        final String expectedGzLocalPath = expectedLocalPath + ".gz";
        final String expectedMd5Sum = "5433198f8276ea3183607c16ac998856";
        final String expectedGzMd5Sum = "1e395a880cd0e99080d545c4d26011b0";
        final String expectedContent =
                """
                Package: plyr
                Version: 1.8.8
                License: MIT + file LICENSE
                MD5sum: 08841cfd5edbd118a512198217cf5f2e
                NeedsCompilation: no
                Built: R 4.5.1; x86_64-pc-linux-gnu; 2025-08-11 09:59:00 UTC; unix""";

        Assertions.assertEquals(expectedLocalPath, packagesFile.localPath(), "Invalid PACKAGES file local path.");
        Assertions.assertEquals(
                expectedGzLocalPath, packagesGzFile.localPath(), "Invalid PACKAGES.gz file local path.");
        Assertions.assertEquals(expectedMd5Sum, packagesFile.checksum(), "Invalid checksum for PACKAGES file");
        Assertions.assertEquals(expectedGzMd5Sum, packagesGzFile.checksum(), "Invalid checksum for PACKAGES.gz file");
        Assertions.assertEquals(
                expectedContent,
                Files.readString(Path.of(packagesFile.localPath())),
                "Invalid content of PACKAGES file.");
    }

    @Test
    public void generatePackagesFiles_forArchiveSourcePackages() throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        Assertions.assertNotNull(populatedContent);
        final ArrayList<PackagesFileDescriptor> packagesFiles =
                new ArrayList<>(populatedContent.packagesFiles().stream()
                        .filter(pfd -> pfd.localPath().contains("src/contrib/Archive"))
                        .toList());
        Assertions.assertEquals(2, packagesFiles.size(), "Invalid number of PACKAGES files for latest source packages");
        assertArchiveSourcePackagesFiles(packagesFiles);
    }

    private void assertArchiveSourcePackagesFiles(ArrayList<PackagesFileDescriptor> packagesFiles) throws Exception {
        packagesFiles.sort(Comparator.comparing(PackagesFileDescriptor::getSubPath));
        final PackagesFileDescriptor packagesFile = packagesFiles.get(0);
        final PackagesFileDescriptor packagesGzFile = packagesFiles.get(1);

        Assertions.assertEquals(
                "src/contrib/Archive", packagesFile.remoteFolder(), "Invalid remote folder for PACKAGES file.");
        Assertions.assertEquals(
                "src/contrib/Archive", packagesGzFile.remoteFolder(), "Invalid remote folder for PACKAGES.gz file.");

        final String expectedLocalPath = "/tmp/rdepot-unit-tests/generated/0/current/src/contrib/Archive/PACKAGES";
        final String expectedGzLocalPath = expectedLocalPath + ".gz";
        final String expectedMd5Sum = "1ea01b28d998cacdfa2810b8357e6a1c";
        final String expectedGzMd5Sum = "cb09a6295e29175dc8d3c82bea5d0875";
        final String expectedContent =
                """
                Package: plyr
                Version: 1.8
                License: MIT
                MD5sum: e1c1d2f0c47fd16b2cef6ec9c2e5883c
                NeedsCompilation: no

                Package: qsort
                Version: 0.2.1
                License: GPL-3
                MD5sum: 5dd316a3591a86ff3d6cda0526c67ba5
                NeedsCompilation: no

                Package: qsort
                Version: 0.2.2
                License: GPL-3
                MD5sum: 76346f1a4ef62977b0acf794c6bb0aef
                NeedsCompilation: no""";

        Assertions.assertEquals(expectedLocalPath, packagesFile.localPath(), "Invalid PACKAGES file local path.");
        Assertions.assertEquals(
                expectedGzLocalPath, packagesGzFile.localPath(), "Invalid PACKAGES.gz file local path.");
        Assertions.assertEquals(expectedMd5Sum, packagesFile.checksum(), "Invalid checksum for PACKAGES file");
        Assertions.assertEquals(expectedGzMd5Sum, packagesGzFile.checksum(), "Invalid checksum for PACKAGES.gz file");
        Assertions.assertEquals(
                expectedContent,
                Files.readString(Path.of(packagesFile.localPath())),
                "Invalid content of PACKAGES file.");
    }

    @Test
    public void organizePackagesInStorage_correctlyPopulatesArchiveBinaryPackages() throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        assertArchiveBinaryPackages(populatedContent.binArchivePackagesPaths());
    }

    @Test
    public void organizePackagesInStorage_correctlyPopulatesLatestBinaryPackages() throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        assertLatestBinaryPackages(populatedContent.binLatestPackagesPaths());
    }

    @Test
    public void organizePackagesInStorage_correctlyPopulatesArchiveSourcePackages() throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        Assertions.assertEquals(
                "/tmp/rdepot-unit-tests/generated/0/current/src/contrib/Archive",
                populatedContent.archiveDirectoryPath(),
                "Invalid Archive directory path");
        assertArchiveSourcePackages(populatedContent.archivePackages());
    }

    private PopulatedRepositoryContent getExamplePopulatedContent_almostEmpty() throws Exception {
        final List<RPackage> sourcePackages = new ArrayList<>();
        final List<RPackage> sourceLatestPackages = new ArrayList<>();
        final List<RPackage> sourceArchivePackages = new ArrayList<>();
        final List<RPackage> archiveBinaryPackages = new ArrayList<>();

        final List<RPackage> latestBinaryPackages = new ArrayList<>();
        latestBinaryPackages.add(RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES()
                .get(0));
        final List<RPackage> binaryPackages = new ArrayList<>(latestBinaryPackages);
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        return rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);
    }

    // checksums are passed to the function, so the remote and local checksums will be the same and packages will not be
    // replaced
    private PopulatedRepositoryContent getExamplePopulatedContent_withAllRemotedPackages(Checksums remoteChecksums)
            throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();
        sourceLatestPackages.forEach(
                rp -> remoteChecksums.addChecksum(new Checksum(rp.getPackageFolderPath(false), rp.getMd5sum())));
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        sourceArchivePackages.forEach(
                rp -> remoteChecksums.addChecksum(new Checksum(rp.getPackageFolderPath(true), rp.getMd5sum())));
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        archiveBinaryPackages.forEach(
                rp -> remoteChecksums.addChecksum(new Checksum(rp.getPackageFolderPath(true), rp.getMd5sum())));
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        latestBinaryPackages.forEach(
                rp -> remoteChecksums.addChecksum(new Checksum(rp.getPackageFolderPath(false), rp.getMd5sum())));
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        return rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);
    }

    private PopulatedRepositoryContent getExamplePopulatedContent_forEmptyRepository() throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();

        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();

        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();

        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();

        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        return rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);
    }

    private RPackage clonePackageWithNewName(RPackage packageBag, String name) throws IOException {
        final RPackage cloned = new RPackage(packageBag);
        cloned.setName(name);
        cloned.setTitle(cloned.getTitle().replace(packageBag.getName(), name));
        cloned.setSource(packageBag.getSource().replace(packageBag.getName(), name));
        Files.copy(new File(packageBag.getSource()).toPath(), new File(cloned.getSource()).toPath());

        return cloned;
    }

    private RPackage clonePackageWithNewVersion(RPackage packageBag, String version) throws IOException {
        final RPackage cloned = new RPackage(packageBag);
        cloned.setVersion(version);
        cloned.setSource(packageBag.getSource().replace(packageBag.getVersion(), version));
        Files.copy(new File(packageBag.getSource()).toPath(), new File(cloned.getSource()).toPath());

        return cloned;
    }

    private SynchronizeRepositoryRequestBody getBigExampleRequestBody() throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();

        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();
        RPackage latestClonePlyr1 = clonePackageWithNewName(sourcePackages.get(0), "plyyyr");
        RPackage latestClonePlyr2 = clonePackageWithNewName(sourcePackages.get(0), "plllyr");
        RPackage latestCloneQsort1 = clonePackageWithNewName(sourcePackages.get(1), "qsooort");
        RPackage latestCloneQsort2 = clonePackageWithNewName(sourcePackages.get(1), "qsssort");
        sourceLatestPackages.add(latestClonePlyr1);
        sourceLatestPackages.add(latestClonePlyr2);
        sourceLatestPackages.add(latestCloneQsort1);
        sourceLatestPackages.add(latestCloneQsort2);
        sourcePackages.add(latestCloneQsort1);
        sourcePackages.add(latestCloneQsort2);
        sourcePackages.add(latestClonePlyr1);
        sourcePackages.add(latestClonePlyr2);

        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        RPackage archivedClonePlyr1 = clonePackageWithNewVersion(sourceArchivePackages.get(0), "1.7.0");
        RPackage archivedClonePlyr2 = clonePackageWithNewVersion(sourceArchivePackages.get(0), "1.7.1");
        RPackage archivedClonePlyr3 = clonePackageWithNewVersion(sourceArchivePackages.get(0), "1.7.2");
        RPackage archivedClonePlyr4 = clonePackageWithNewVersion(sourceArchivePackages.get(0), "1.7.3");
        RPackage archivedCloneQsort1 = clonePackageWithNewVersion(sourceArchivePackages.get(1), "0.1.1");
        RPackage archivedCloneQsort2 = clonePackageWithNewVersion(sourceArchivePackages.get(1), "0.1.2");
        RPackage archivedCloneQsort3 = clonePackageWithNewVersion(sourceArchivePackages.get(1), "0.1.3");
        sourceArchivePackages.add(archivedClonePlyr1);
        sourceArchivePackages.add(archivedClonePlyr2);
        sourceArchivePackages.add(archivedClonePlyr3);
        sourceArchivePackages.add(archivedClonePlyr4);
        sourceArchivePackages.add(archivedCloneQsort1);
        sourceArchivePackages.add(archivedCloneQsort2);
        sourceArchivePackages.add(archivedCloneQsort3);
        sourcePackages.add(archivedClonePlyr1);
        sourcePackages.add(archivedClonePlyr2);
        sourcePackages.add(archivedClonePlyr3);
        sourcePackages.add(archivedClonePlyr4);
        sourcePackages.add(archivedCloneQsort1);

        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        RPackage archivedCloneBinaryPlyr1 = clonePackageWithNewVersion(archiveBinaryPackages.get(0), "1.7.0");
        RPackage archivedCloneBinaryPlyr2 = clonePackageWithNewVersion(archiveBinaryPackages.get(0), "1.7.1");
        RPackage archivedCloneBinaryPlyr3 = clonePackageWithNewVersion(archiveBinaryPackages.get(0), "1.7.2");
        RPackage archivedCloneBinaryPlyr4 = clonePackageWithNewVersion(archiveBinaryPackages.get(0), "1.7.3");
        archiveBinaryPackages.add(archivedCloneBinaryPlyr1);
        archiveBinaryPackages.add(archivedCloneBinaryPlyr2);
        archiveBinaryPackages.add(archivedCloneBinaryPlyr3);
        archiveBinaryPackages.add(archivedCloneBinaryPlyr4);
        binaryPackages.add(archivedCloneBinaryPlyr1);
        binaryPackages.add(archivedCloneBinaryPlyr2);
        binaryPackages.add(archivedCloneBinaryPlyr3);
        binaryPackages.add(archivedCloneBinaryPlyr4);

        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();

        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        return rLocalPopulator.buildSynchronizeRequestBody(
                populatedContent,
                List.of(),
                List.of(),
                new MultiValueMapAdapter<>(new HashMap<>()),
                new MultiValueMapAdapter<>(new HashMap<>()),
                new Checksums(),
                populatedContent.latestPackages().get(0).getRepository(),
                "0");
    }

    private String calculateMd5(String path) throws Exception {
        byte[] data = Files.readAllBytes(Paths.get(path));
        byte[] hash = MessageDigest.getInstance("MD5").digest(data);
        return new BigInteger(1, hash).toString(16);
    }

    private void assertFile(String expectedPath, String expectedMd5Sum, FileSystemResource fileSystemResource)
            throws Exception {
        Assertions.assertEquals(expectedMd5Sum, calculateMd5(expectedPath));
        Assertions.assertEquals(expectedPath, fileSystemResource.getPath());
    }

    @Test
    public void partitionRequestBody_firstChunk_latest() throws Exception {
        final SynchronizeRepositoryRequestBody requestBody = getBigExampleRequestBody();
        final ChunkedRequestBody chunked = partitioner.partition(requestBody, 3);
        final String id = "abc123";
        MultiValueMap<String, Object> firstChunk = chunked.firstChunkToMap();

        final List<FileSystemResource> filesInFirstChunk = new ArrayList<>(firstChunk.get("files").stream()
                .map(f -> (FileSystemResource) f)
                .toList());
        filesInFirstChunk.sort(Comparator.comparing(FileSystemResource::getFilename));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/"
                        + "latest/binlinuxcentos8x866445_PACKAGES",
                "5433198f8276ea3183607c16ac998856",
                filesInFirstChunk.get(0));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/"
                        + "latest/binlinuxcentos8x866445_PACKAGES.gz",
                "1e395a880cd0e99080d545c4d26011b0",
                filesInFirstChunk.get(1));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/"
                        + "latest/binlinuxcentos8x866445_index.html",
                "39e8995151ab231b9e75d6c02ddeb2a8",
                filesInFirstChunk.get(2));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/"
                        + "latest/binlinuxcentos8x866445_plyr_1.8.8.tar.gz",
                "8841cfd5edbd118a512198217cf5f2e",
                filesInFirstChunk.get(3));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/latest/srccontrib_PACKAGES",
                "5e4eb9b59cf19c55f963fd4781cc866f",
                filesInFirstChunk.get(4));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/latest/srccontrib_PACKAGES.gz",
                "9d323a2194c95b9cdaeb412829a867bf",
                filesInFirstChunk.get(5));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/latest/srccontrib_index.html",
                "f0ab4674239afd3a46950bd9ec3dfa73",
                filesInFirstChunk.get(6));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/latest/srccontrib_plyr_1.8.8.tar.gz",
                "a22da16605ee765e7d4f1efc9f7a61f",
                filesInFirstChunk.get(7));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/latest/srccontrib_plyyyr_1.8.8.tar.gz",
                "a22da16605ee765e7d4f1efc9f7a61f",
                filesInFirstChunk.get(8));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/latest/srccontrib_qsort_0.2.3.tar.gz",
                "3204109d62ec7ff8e44bd15a989fc8b1",
                filesInFirstChunk.get(9));
        chunked.otherChunksToMaps(id);
    }

    @Test
    public void partitionRequestBody_firstChunk_metadata() throws Exception {
        final SynchronizeRepositoryRequestBody requestBody = getBigExampleRequestBody();
        final ChunkedRequestBody chunked = partitioner.partition(requestBody, 3);
        MultiValueMap<String, Object> firstChunk = chunked.firstChunkToMap();

        Assertions.assertEquals("0", firstChunk.get("version_before").get(0), "Invalid version before");
        Assertions.assertEquals("1", firstChunk.get("version_after").get(0), "Invalid version after");
        Assertions.assertEquals("1/4", firstChunk.get("page").get(0), "Invalid page");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void partitionRequestBody_fourthChunk_checksums() throws Exception {
        final SynchronizeRepositoryRequestBody requestBody = getBigExampleRequestBody();
        final ChunkedRequestBody chunked = partitioner.partition(requestBody, 3);
        MultiValueMap<String, Object> chunk =
                chunked.otherChunksToMaps("abc123").get(2);
        Map<String, String> checksums =
                ((Map<String, String>) chunk.get("checksums").get(0));

        Assertions.assertEquals(1, checksums.size(), "Invalid number of checksums");
        Assertions.assertEquals(
                "5dd316a3591a86ff3d6cda0526c67ba5",
                checksums.get("srccontrib_qsort_0.1.3.tar.gz"),
                "Invalid checksum for file");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void partitionRequestBody_thirdChunk_checksums() throws Exception {
        final SynchronizeRepositoryRequestBody requestBody = getBigExampleRequestBody();
        final ChunkedRequestBody chunked = partitioner.partition(requestBody, 3);
        MultiValueMap<String, Object> chunk =
                chunked.otherChunksToMaps("abc123").get(1);
        Map<String, String> checksums =
                ((Map<String, String>) chunk.get("checksums").get(0));

        Assertions.assertEquals(3, checksums.size(), "Invalid number of checksums");
        Assertions.assertEquals(
                "5dd316a3591a86ff3d6cda0526c67ba5",
                checksums.get("srccontrib_qsort_0.1.1.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "5dd316a3591a86ff3d6cda0526c67ba5",
                checksums.get("srccontrib_qsort_0.1.2.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "e1c1d2f0c47fd16b2cef6ec9c2e5883c",
                checksums.get("srccontrib_plyr_1.7.3.tar.gz"),
                "Invalid checksum for file");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void partitionRequestBody_secondChunk_checksums() throws Exception {
        final SynchronizeRepositoryRequestBody requestBody = getBigExampleRequestBody();
        final ChunkedRequestBody chunked = partitioner.partition(requestBody, 3);
        MultiValueMap<String, Object> chunk =
                chunked.otherChunksToMaps("abc123").get(0);
        Map<String, String> checksums =
                ((Map<String, String>) chunk.get("checksums").get(0));

        Assertions.assertEquals(11, checksums.size(), "Invalid number of checksums");
        Assertions.assertEquals(
                "e1c1d2f0c47fd16b2cef6ec9c2e5883c",
                checksums.get("srccontrib_plyr_1.7.1.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "6a9c2acfd924f2fb626d54168120fa08",
                checksums.get("binlinuxcentos8x866445_plyr_1.7.2.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "6a9c2acfd924f2fb626d54168120fa08",
                checksums.get("binlinuxcentos8x866445_plyr_1.7.3.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "e1c1d2f0c47fd16b2cef6ec9c2e5883c",
                checksums.get("srccontrib_plyr_1.7.0.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "e1c1d2f0c47fd16b2cef6ec9c2e5883c",
                checksums.get("srccontrib_plyr_1.7.2.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "0a22da16605ee765e7d4f1efc9f7a61f",
                checksums.get("srccontrib_plllyr_1.8.8.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "e1c1d2f0c47fd16b2cef6ec9c2e5883c",
                checksums.get("srccontrib_qsooort_1.8.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "9c1b6f3d392fb9511a49cca6e9523a31",
                checksums.get("srccontribArchive_index_archived.html"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "6a9c2acfd924f2fb626d54168120fa08",
                checksums.get("binlinuxcentos8x866445_plyr_1.7.1.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "62daee935d0e25a9cd1804d3a5f4bcdf",
                checksums.get("srccontribArchiveqsort_qsortindex.html"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "e1c1d2f0c47fd16b2cef6ec9c2e5883c",
                checksums.get("srccontrib_qsssort_1.8.tar.gz"),
                "Invalid checksum for file");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void partitionRequestBody_firstChunk_checksums() throws Exception {
        final SynchronizeRepositoryRequestBody requestBody = getBigExampleRequestBody();
        final ChunkedRequestBody chunked = partitioner.partition(requestBody, 3);
        MultiValueMap<String, Object> firstChunk = chunked.firstChunkToMap();
        Map<String, String> checksums =
                ((Map<String, String>) firstChunk.get("checksums").get(0));

        Assertions.assertEquals(23, checksums.size(), "Invalid number of checksums");
        Assertions.assertEquals(
                "f8bc27c44d636319e28e093c205f3a8f",
                checksums.get("srccontribArchive_PACKAGES"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "a8b2d2284d56ab1839728040d463a360",
                checksums.get("binlinuxcentos8x866445_plyr_1.8.1.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "485b6320b1eff885ff92d61c269dccae",
                checksums.get("srccontribArchive_PACKAGES.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "08841cfd5edbd118a512198217cf5f2e",
                checksums.get("binlinuxcentos8x866445_plyr_1.8.8.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "39e8995151ab231b9e75d6c02ddeb2a8",
                checksums.get("binlinuxcentos8x866445_index.html"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "fa8d932aa12dca1e9ded1e6c6efef88b",
                checksums.get("binlinuxcentos8x866445Archiveplyr_plyrindex.html"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "0a22da16605ee765e7d4f1efc9f7a61f",
                checksums.get("srccontrib_plyr_1.8.8.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "0a22da16605ee765e7d4f1efc9f7a61f",
                checksums.get("srccontrib_plyyyr_1.8.8.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "e1c1d2f0c47fd16b2cef6ec9c2e5883c",
                checksums.get("srccontrib_plyr_1.8.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "5e4eb9b59cf19c55f963fd4781cc866f", checksums.get("srccontrib_PACKAGES"), "Invalid checksum for file");
        Assertions.assertEquals(
                "e778d1c81f9b4649c8bed5788be89b7f",
                checksums.get("binlinuxcentos8x866445Archive_index_archived.html"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "6a9c2acfd924f2fb626d54168120fa08",
                checksums.get("binlinuxcentos8x866445_plyr_1.8.6.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "c723e267a38052dcf948814da8e1cd48",
                checksums.get("binlinuxcentos8x866445Archive_PACKAGES"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "76346f1a4ef62977b0acf794c6bb0aef",
                checksums.get("srccontrib_qsort_0.2.2.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "f0ab4674239afd3a46950bd9ec3dfa73",
                checksums.get("srccontrib_index.html"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "6a9c2acfd924f2fb626d54168120fa08",
                checksums.get("binlinuxcentos8x866445_plyr_1.7.0.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "9d323a2194c95b9cdaeb412829a867bf",
                checksums.get("srccontrib_PACKAGES.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "0970b682baea08316366051a01bbcbac",
                checksums.get("binlinuxcentos8x866445Archive_PACKAGES.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "3204109d62ec7ff8e44bd15a989fc8b1",
                checksums.get("srccontrib_qsort_0.2.3.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "1e395a880cd0e99080d545c4d26011b0",
                checksums.get("binlinuxcentos8x866445_PACKAGES.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "5dd316a3591a86ff3d6cda0526c67ba5",
                checksums.get("srccontrib_qsort_0.2.1.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "270315e7e1e90f13fba4f8976a661c6b",
                checksums.get("srccontribArchiveplyr_plyrindex.html"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "5433198f8276ea3183607c16ac998856",
                checksums.get("binlinuxcentos8x866445_PACKAGES"),
                "Invalid checksum for file");
    }

    @Test
    public void partitionRequestBody_secondChunk_metadata() throws Exception {
        final SynchronizeRepositoryRequestBody requestBody = getBigExampleRequestBody();
        final ChunkedRequestBody chunked = partitioner.partition(requestBody, 3);
        final String id = "abc123";
        MultiValueMap<String, Object> chunk = chunked.otherChunksToMaps(id).get(0);

        Assertions.assertEquals("1", chunk.get("version_before").get(0), "Invalid version before");
        Assertions.assertEquals("2", chunk.get("version_after").get(0), "Invalid version after");
        Assertions.assertEquals("2/4", chunk.get("page").get(0), "Invalid page");
        Assertions.assertEquals(id, chunk.get("id").get(0), "Invalid id");
    }

    @Test
    public void partitionRequestBody_fourthChunk_metadata() throws Exception {
        final SynchronizeRepositoryRequestBody requestBody = getBigExampleRequestBody();
        final ChunkedRequestBody chunked = partitioner.partition(requestBody, 3);
        final String id = "abc123";
        MultiValueMap<String, Object> chunk = chunked.otherChunksToMaps(id).get(2);

        Assertions.assertEquals("3", chunk.get("version_before").get(0), "Invalid version before");
        Assertions.assertEquals("4", chunk.get("version_after").get(0), "Invalid version after");
        Assertions.assertEquals("4/4", chunk.get("page").get(0), "Invalid page");
        Assertions.assertEquals(id, chunk.get("id").get(0), "Invalid id");
    }

    @Test
    public void partitionRequestBody_firstChunk_archive() throws Exception {
        final SynchronizeRepositoryRequestBody requestBody = getBigExampleRequestBody();
        final ChunkedRequestBody chunked = partitioner.partition(requestBody, 3);
        final String id = "abc123";
        MultiValueMap<String, Object> firstChunk = chunked.firstChunkToMap();

        final List<FileSystemResource> filesInFirstChunk = new ArrayList<>(firstChunk.get("files_archive").stream()
                .map(f -> (FileSystemResource) f)
                .toList());
        filesInFirstChunk.sort(Comparator.comparing(FileSystemResource::getFilename));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/Archive/"
                        + "binlinuxcentos8x866445Archive_PACKAGES",
                "c723e267a38052dcf948814da8e1cd48",
                filesInFirstChunk.get(0));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/Archive/"
                        + "binlinuxcentos8x866445Archive_PACKAGES.gz",
                "970b682baea08316366051a01bbcbac",
                filesInFirstChunk.get(1));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/Archive/"
                        + "binlinuxcentos8x866445Archive_index_archived.html",
                "e778d1c81f9b4649c8bed5788be89b7f",
                filesInFirstChunk.get(2));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/Archive/"
                        + "binlinuxcentos8x866445Archiveplyr_plyrindex.html",
                "fa8d932aa12dca1e9ded1e6c6efef88b",
                filesInFirstChunk.get(3));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/Archive/"
                        + "binlinuxcentos8x866445_plyr_1.7.0.tar.gz",
                "6a9c2acfd924f2fb626d54168120fa08",
                filesInFirstChunk.get(4));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/Archive/"
                        + "binlinuxcentos8x866445_plyr_1.8.1.tar.gz",
                "a8b2d2284d56ab1839728040d463a360",
                filesInFirstChunk.get(5));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/Archive/"
                        + "binlinuxcentos8x866445_plyr_1.8.6.tar.gz",
                "6a9c2acfd924f2fb626d54168120fa08",
                filesInFirstChunk.get(6));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/Archive/" + "srccontribArchive_PACKAGES",
                "f8bc27c44d636319e28e093c205f3a8f",
                filesInFirstChunk.get(7));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/Archive/"
                        + "srccontribArchive_PACKAGES.gz",
                "485b6320b1eff885ff92d61c269dccae",
                filesInFirstChunk.get(8));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/Archive/"
                        + "srccontribArchiveplyr_plyrindex.html",
                "270315e7e1e90f13fba4f8976a661c6b",
                filesInFirstChunk.get(9));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/Archive/" + "srccontrib_plyr_1.8.tar.gz",
                "e1c1d2f0c47fd16b2cef6ec9c2e5883c",
                filesInFirstChunk.get(10));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/Archive/"
                        + "srccontrib_qsort_0.2.1.tar.gz",
                "5dd316a3591a86ff3d6cda0526c67ba5",
                filesInFirstChunk.get(11));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/Archive/"
                        + "srccontrib_qsort_0.2.2.tar.gz",
                "76346f1a4ef62977b0acf794c6bb0aef",
                filesInFirstChunk.get(12));
        chunked.otherChunksToMaps(id);
    }

    @Test
    public void partitionRequestBody_secondChunk_latest() throws Exception {
        final SynchronizeRepositoryRequestBody requestBody = getBigExampleRequestBody();
        final ChunkedRequestBody chunked = partitioner.partition(requestBody, 3);
        final String id = "abc123";
        MultiValueMap<String, Object> chunk = chunked.otherChunksToMaps(id).get(0);

        final List<FileSystemResource> filesInChunk = new ArrayList<>(
                chunk.get("files").stream().map(f -> (FileSystemResource) f).toList());
        filesInChunk.sort(Comparator.comparing(FileSystemResource::getFilename));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/"
                        + "latest/srccontrib_plllyr_1.8.8.tar.gz",
                "a22da16605ee765e7d4f1efc9f7a61f",
                filesInChunk.get(0));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/" + "latest/srccontrib_qsooort_1.8.tar.gz",
                "e1c1d2f0c47fd16b2cef6ec9c2e5883c",
                filesInChunk.get(1));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/" + "latest/srccontrib_qsssort_1.8.tar.gz",
                "e1c1d2f0c47fd16b2cef6ec9c2e5883c",
                filesInChunk.get(2));
    }

    @Test
    public void partitionRequestBody_secondChunk_archive() throws Exception {
        final SynchronizeRepositoryRequestBody requestBody = getBigExampleRequestBody();
        final ChunkedRequestBody chunked = partitioner.partition(requestBody, 3);
        final String id = "abc123";
        MultiValueMap<String, Object> chunk = chunked.otherChunksToMaps(id).get(0);

        final List<FileSystemResource> filesInChunk = new ArrayList<>(chunk.get("files_archive").stream()
                .map(f -> (FileSystemResource) f)
                .toList());
        filesInChunk.sort(Comparator.comparing(FileSystemResource::getFilename));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/Archive/"
                        + "binlinuxcentos8x866445_plyr_1.7.1.tar.gz",
                "6a9c2acfd924f2fb626d54168120fa08",
                filesInChunk.get(0));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/Archive/"
                        + "binlinuxcentos8x866445_plyr_1.7.2.tar.gz",
                "6a9c2acfd924f2fb626d54168120fa08",
                filesInChunk.get(1));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/Archive/"
                        + "binlinuxcentos8x866445_plyr_1.7.3.tar.gz",
                "6a9c2acfd924f2fb626d54168120fa08",
                filesInChunk.get(2));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/Archive/"
                        + "srccontribArchive_index_archived.html",
                "9c1b6f3d392fb9511a49cca6e9523a31",
                filesInChunk.get(3));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/Archive/"
                        + "srccontribArchiveqsort_qsortindex.html",
                "62daee935d0e25a9cd1804d3a5f4bcdf",
                filesInChunk.get(4));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/Archive/" + "srccontrib_plyr_1.7.0.tar.gz",
                "e1c1d2f0c47fd16b2cef6ec9c2e5883c",
                filesInChunk.get(5));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/Archive/" + "srccontrib_plyr_1.7.1.tar.gz",
                "e1c1d2f0c47fd16b2cef6ec9c2e5883c",
                filesInChunk.get(6));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/Archive/" + "srccontrib_plyr_1.7.2.tar.gz",
                "e1c1d2f0c47fd16b2cef6ec9c2e5883c",
                filesInChunk.get(7));
    }

    @Test
    public void partitionRequestBody_thirdChunk_archive() throws Exception {
        final SynchronizeRepositoryRequestBody requestBody = getBigExampleRequestBody();
        final ChunkedRequestBody chunked = partitioner.partition(requestBody, 3);
        final String id = "abc123";
        MultiValueMap<String, Object> chunk = chunked.otherChunksToMaps(id).get(1);

        final List<FileSystemResource> filesInChunk = new ArrayList<>(chunk.get("files_archive").stream()
                .map(f -> (FileSystemResource) f)
                .toList());
        filesInChunk.sort(Comparator.comparing(FileSystemResource::getFilename));

        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/Archive/" + "srccontrib_plyr_1.7.3.tar.gz",
                "e1c1d2f0c47fd16b2cef6ec9c2e5883c",
                filesInChunk.get(0));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/Archive/"
                        + "srccontrib_qsort_0.1.1.tar.gz",
                "5dd316a3591a86ff3d6cda0526c67ba5",
                filesInChunk.get(1));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/Archive/"
                        + "srccontrib_qsort_0.1.2.tar.gz",
                "5dd316a3591a86ff3d6cda0526c67ba5",
                filesInChunk.get(2));
    }

    @Test
    public void partitionRequestBody_fourthChunk_archive() throws Exception {
        final SynchronizeRepositoryRequestBody requestBody = getBigExampleRequestBody();
        final ChunkedRequestBody chunked = partitioner.partition(requestBody, 3);
        final String id = "abc123";
        MultiValueMap<String, Object> chunk = chunked.otherChunksToMaps(id).get(2);

        final List<FileSystemResource> filesInChunk = new ArrayList<>(chunk.get("files_archive").stream()
                .map(f -> (FileSystemResource) f)
                .toList());
        filesInChunk.sort(Comparator.comparing(FileSystemResource::getFilename));

        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/Archive/"
                        + "srccontrib_qsort_0.1.3.tar.gz",
                "5dd316a3591a86ff3d6cda0526c67ba5",
                filesInChunk.get(0));
    }

    @Test
    public void partitionRequestBody_thirdChunk_latest() throws Exception {
        final SynchronizeRepositoryRequestBody requestBody = getBigExampleRequestBody();
        final ChunkedRequestBody chunked = partitioner.partition(requestBody, 3);
        final String id = "abc123";
        MultiValueMap<String, Object> chunk = chunked.otherChunksToMaps(id).get(1);

        final List<FileSystemResource> filesInChunk = new ArrayList<>(
                chunk.get("files").stream().map(f -> (FileSystemResource) f).toList());
        Assertions.assertTrue(filesInChunk.isEmpty(), "There should be no recent files in chunk");
    }

    @Test
    public void buildSynchronizedRequestBody_forRepositoryWithAllPackages_ManyToDelete() throws Exception {
        /**
         * Remote bin:
         * - bin/linux/centos8/x86_64/4.5 -> plyr_1.8.8.tar.gz
         * - bin/linux/centos8/x86_64/4.5 -> plyr_1.8.6.tar.gz
         * - bin/linux/centos8/x86_64/4.5 -> plyr_1.8.1.tar.gz
         *
         * Local bin:
         *
         */
        final PopulatedRepositoryContent populatedContent = getExamplePopulatedContent_almostEmpty();

        final MultiValueMap<String, String> remoteLatestBinaries = new LinkedMultiValueMap<>();
        remoteLatestBinaries.add("bin/linux/centos8/x86_64/4.5", "plyr_1.8.8.tar.gz");
        final MultiValueMap<String, String> remoteArchiveBinaries = new LinkedMultiValueMap<>();
        remoteArchiveBinaries.add("bin/linux/centos8/x86_64/4.5", "plyr_1.8.6.tar.gz");
        remoteArchiveBinaries.add("bin/linux/centos8/x86_64/4.5", "plyr_1.8.1.tar.gz");

        Checksums remoteChecksums = new Checksums();
        remoteChecksums.addChecksum(
                new Checksum("bin/linux/centos8/x86_64/4.5/plyr_1.8.8.tar.gz", "08841cfd5edbd118a512198217cf5f2e"));
        remoteChecksums.addChecksum(
                new Checksum("bin/linux/centos8/x86_64/4.5/plyr_1.8.6.tar.gz", "notCheckedChecksumHere"));
        remoteChecksums.addChecksum(
                new Checksum("bin/linux/centos8/x86_64/4.5/plyr_1.8.1.tar.gz", "a8b2d2284d56ab1839728040d463a360"));

        remoteChecksums.addChecksum(new Checksum("src/contrib/plyr_1.8.8.tar.gz", "notCheckedChecksumHere"));
        remoteChecksums.addChecksum(new Checksum("src/contrib/qsort_0.2.3.tar.gz", "notCheckedChecksumHere"));
        remoteChecksums.addChecksum(new Checksum("src/contrib/plyr_1.8.tar.gz", "notCheckedChecksumHere"));
        remoteChecksums.addChecksum(new Checksum("src/contrib/qsort_0.2.2.tar.gz", "notCheckedChecksumHere"));
        remoteChecksums.addChecksum(new Checksum("src/contrib/qsort_0.2.1.tar.gz", "notCheckedChecksumHere"));

        final SynchronizeRepositoryRequestBody requestBody = rLocalPopulator.buildSynchronizeRequestBody(
                populatedContent,
                List.of("plyr_1.8.8.tar.gz", "qsort_0.2.3.tar.gz"),
                List.of("plyr_1.8.tar.gz", "qsort_0.2.2.tar.gz", "qsort_0.2.1.tar.gz"),
                remoteLatestBinaries,
                remoteArchiveBinaries,
                remoteChecksums,
                populatedContent.binLatestPackagesPaths().getAllBinLocations().stream()
                        .findFirst()
                        .orElseThrow(IllegalStateException::new)
                        .packages()
                        .get(0)
                        .getRepository(),
                "0");
        Assertions.assertEquals(
                0, requestBody.getSourcePackagesToUpload().size(), "Invalid package count in the request body.");
        Assertions.assertEquals(
                0,
                requestBody.getSourcePackagesToUploadToArchive().size(),
                "Invalid package count in the request body.");
        Assertions.assertTrue(
                requestBody.getBinaryPackagesToUpload().isEmpty(), "There should be no packages to upload.");
        Assertions.assertTrue(
                requestBody.getBinaryPackagesToUploadToArchive().isEmpty(), "There should be no packages to upload.");
        Assertions.assertEquals(2, requestBody.getPackagesFiles().size(), "Invalid number of PACKAGES files.");
        Assertions.assertEquals(
                2, requestBody.getPackagesFilesForArchive().size(), "Invalid number of PACKAGES files for archive.");
        Assertions.assertEquals(2, requestBody.getPackagesGzFiles().size(), "Invalid number of PACKAGES.gz files.");
        Assertions.assertEquals(
                2,
                requestBody.getPackagesGzFilesForArchive().size(),
                "Invalid number of PACKAGES.gz files for archive.");
        Assertions.assertEquals(
                2, requestBody.getSourcePackagesToDelete().size(), "There should be 2 packages to delete.");
        Assertions.assertEquals(
                3, requestBody.getSourcePackagesToDeleteFromArchive().size(), "There should be 3 packages to delete.");
        Assertions.assertEquals(
                0, requestBody.getBinaryPackagesToDelete().size(), "There should be no packages to delete.");
        Assertions.assertEquals(
                2,
                requestBody
                        .getBinaryPackagesToDeleteFromArchive()
                        .get("bin/linux/centos8/x86_64/4.5")
                        .size(),
                "There should be 2 packages to delete.");
        Assertions.assertEquals(2, requestBody.getIndexes().size(), "Invalid number of indexes in the request body.");
        Assertions.assertEquals(2, requestBody.getIndexesForArchive().size(), "Invalid number of indexes for archive.");
        Assertions.assertEquals(13, requestBody.getChecksums().toMap().size(), "Invalid number of checksums.");
    }

    @Test
    public void buildSynchronizedRequestBody_forRepositoryWithAllPackages() throws Exception {
        Checksums remoteChecksums = new Checksums();

        final PopulatedRepositoryContent populatedContent =
                getExamplePopulatedContent_withAllRemotedPackages(remoteChecksums);

        final MultiValueMap<String, String> remoteLatestBinaries = new LinkedMultiValueMap<>();
        remoteLatestBinaries.add("bin/linux/centos8/x86_64/4.5", "plyr_1.8.8.tar.gz");
        final MultiValueMap<String, String> remoteArchiveBinaries = new LinkedMultiValueMap<>();
        remoteArchiveBinaries.add("bin/linux/centos8/x86_64/4.5", "plyr_1.8.6.tar.gz");
        remoteArchiveBinaries.add("bin/linux/centos8/x86_64/4.5", "plyr_1.8.1.tar.gz");

        final SynchronizeRepositoryRequestBody requestBody = rLocalPopulator.buildSynchronizeRequestBody(
                populatedContent,
                List.of("plyr_1.8.8.tar.gz", "qsort_0.2.3.tar.gz"),
                List.of("plyr_1.8.tar.gz", "qsort_0.2.2.tar.gz", "qsort_0.2.1.tar.gz"),
                remoteLatestBinaries,
                remoteArchiveBinaries,
                remoteChecksums,
                populatedContent.latestPackages().get(0).getRepository(),
                "0");
        Assertions.assertEquals(
                0, requestBody.getSourcePackagesToUpload().size(), "Invalid package count in the request body.");
        Assertions.assertEquals(
                0,
                requestBody.getSourcePackagesToUploadToArchive().size(),
                "Invalid package count in the request body.");
        Assertions.assertTrue(
                requestBody.getBinaryPackagesToUpload().isEmpty(), "There should be no packages to upload.");
        Assertions.assertTrue(
                requestBody.getBinaryPackagesToUploadToArchive().isEmpty(), "There should be no packages to upload.");
        Assertions.assertEquals(2, requestBody.getPackagesFiles().size(), "Invalid number of PACKAGES files.");
        Assertions.assertEquals(
                2, requestBody.getPackagesFilesForArchive().size(), "Invalid number of PACKAGES files for archive.");
        Assertions.assertEquals(2, requestBody.getPackagesGzFiles().size(), "Invalid number of PACKAGES.gz files.");
        Assertions.assertEquals(
                2,
                requestBody.getPackagesGzFilesForArchive().size(),
                "Invalid number of PACKAGES.gz files for archive.");
        Assertions.assertTrue(
                requestBody.getSourcePackagesToDelete().isEmpty(), "There should be no packages to delete.");
        Assertions.assertTrue(
                requestBody.getSourcePackagesToDeleteFromArchive().isEmpty(), "There should be no packages to delete.");
        Assertions.assertTrue(
                requestBody.getBinaryPackagesToDelete().isEmpty(), "There should be no packages to delete.");
        Assertions.assertTrue(
                requestBody.getBinaryPackagesToDeleteFromArchive().isEmpty(), "There should be no packages to delete.");
        Assertions.assertEquals(2, requestBody.getIndexes().size(), "Invalid number of indexes in the request body.");
        Assertions.assertEquals(5, requestBody.getIndexesForArchive().size(), "Invalid number of indexes for archive.");
        Assertions.assertEquals(23, requestBody.getChecksums().toMap().size(), "Invalid number of checksums.");
    }

    @Test
    public void buildSynchronizedRequestBody_forEmptyRepository() throws Exception {
        final PopulatedRepositoryContent populatedContent = getExamplePopulatedContent_forEmptyRepository();

        final SynchronizeRepositoryRequestBody requestBody = rLocalPopulator.buildSynchronizeRequestBody(
                populatedContent,
                List.of(),
                List.of(),
                new MultiValueMapAdapter<>(new HashMap<>()),
                new MultiValueMapAdapter<>(new HashMap<>()),
                new Checksums(),
                populatedContent.latestPackages().get(0).getRepository(),
                "0");
        Assertions.assertEquals(
                2, requestBody.getSourcePackagesToUpload().size(), "Invalid package count in the request body.");
        Assertions.assertEquals(
                3,
                requestBody.getSourcePackagesToUploadToArchive().size(),
                "Invalid package count in the request body.");
        Assertions.assertEquals(
                1,
                requestBody.getBinaryPackagesToUpload().entrySet().stream()
                        .findAny()
                        .orElseThrow(IllegalStateException::new)
                        .getValue()
                        .size(),
                "Invalid package count in the request body.");
        Assertions.assertEquals(
                2,
                requestBody.getBinaryPackagesToUploadToArchive().entrySet().stream()
                        .findAny()
                        .orElseThrow(IllegalStateException::new)
                        .getValue()
                        .size(),
                "Invalid package count in the request body.");
        Assertions.assertEquals(2, requestBody.getPackagesFiles().size(), "Invalid number of PACKAGES files.");
        Assertions.assertEquals(
                2, requestBody.getPackagesFilesForArchive().size(), "Invalid number of PACKAGES files for archive.");
        Assertions.assertEquals(2, requestBody.getPackagesGzFiles().size(), "Invalid number of PACKAGES.gz files.");
        Assertions.assertEquals(
                2,
                requestBody.getPackagesGzFilesForArchive().size(),
                "Invalid number of PACKAGES.gz files for archive.");
        Assertions.assertTrue(
                requestBody.getSourcePackagesToDelete().isEmpty(), "There should be no packages to delete.");
        Assertions.assertTrue(
                requestBody.getSourcePackagesToDeleteFromArchive().isEmpty(), "There should be no packages to delete.");
        Assertions.assertTrue(
                requestBody.getBinaryPackagesToDelete().isEmpty(), "There should be no packages to delete.");
        Assertions.assertTrue(
                requestBody.getBinaryPackagesToDeleteFromArchive().isEmpty(), "There should be no packages to delete.");
        Assertions.assertEquals(2, requestBody.getIndexes().size(), "Invalid number of indexes in the request body.");
        Assertions.assertEquals(5, requestBody.getIndexesForArchive().size(), "Invalid number of indexes for archive.");
        Assertions.assertEquals(23, requestBody.getChecksums().toMap().size(), "Invalid number of checksums.");
    }

    @Test
    public void organizePackagesInStorage_correctlyPopulatesLatestSourcePackages() throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        assertLatestSourcePackages(populatedContent.latestPackages());
        Assertions.assertEquals(
                "/tmp/rdepot-unit-tests/generated/0/current/src/contrib/latest",
                populatedContent.latestDirectoryPath(),
                "Invalid latest directory path");
    }

    private void assertArchiveBinaryPackages(BinLocationSet binLocationSet) throws Exception {
        Collection<BinLocation> locations = binLocationSet.getAllBinLocations();
        Assertions.assertEquals(1, locations.size(), "There should be only one binary location.");
        final BinLocation binLocation = locations.iterator().next();

        Assertions.assertEquals(
                "bin/linux/centos8/x86_64/4.5/Archive",
                binLocation.remoteLocation(),
                "Remote binary location is incorrect.");
        Assertions.assertEquals(
                "/tmp/rdepot-unit-tests/generated/" + "0/12082025/bin/linux/centos8/x86_64/4.5/Archive",
                binLocation.location(),
                "Local binary packages location is invalid.");
        Assertions.assertEquals(
                2, binLocation.packages().size(), "Invalid number of packages in the latest binary location");
        final Set<String> packageNames = binLocation.packages().stream()
                .map(p -> p.getName() + "_" + p.getVersion())
                .collect(Collectors.toSet());
        Assertions.assertTrue(packageNames.contains("plyr_1.8.1"), "One of archive binary packages not found!");
        Assertions.assertTrue(packageNames.contains("plyr_1.8.6"), "One of archive binary packages not found!");
        final List<PopulatedRPackage> packageList =
                new ArrayList<>(binLocation.packages().stream().toList());
        packageList.sort(Package::compareTo);
        final PopulatedRPackage plyr = packageList.get(0);
        final String expectedPopulatedPath =
                "/tmp/rdepot-unit-tests/generated/0/12082025/bin/linux/centos8/x86_64/4.5/Archive/plyr_1.8.1.tar.gz";
        final String actualPopulatedPath = plyr.getPopulatedPath();
        Assertions.assertEquals(expectedPopulatedPath, actualPopulatedPath, "Invalid populated path");
        Assertions.assertTrue(
                FileUtils.contentEquals(new File(plyr.getSource()), new File(actualPopulatedPath)),
                "Populated package and the source package should be the same");
    }

    private void assertLatestBinaryPackages(BinLocationSet binLocationSet) throws Exception {
        Collection<BinLocation> locations = binLocationSet.getAllBinLocations();
        Assertions.assertEquals(1, locations.size(), "There should be only one binary location.");
        final BinLocation binLocation = locations.iterator().next();

        Assertions.assertEquals(
                "bin/linux/centos8/x86_64/4.5", binLocation.remoteLocation(), "Remote binary location is incorrect.");
        Assertions.assertEquals(
                "/tmp/rdepot-unit-tests/generated/" + "0/12082025/bin/linux/centos8/x86_64/4.5/latest",
                binLocation.location(),
                "Local binary packages location is invalid.");
        Assertions.assertEquals(
                1, binLocation.packages().size(), "Invalid number of packages in the latest binary location");

        final PopulatedRPackage plyr = binLocation.packages().iterator().next();
        Assertions.assertEquals(
                "plyr_1.8.8", plyr.getName() + "_" + plyr.getVersion(), "Invalid latest binary package.");
        final String expectedPopulatedPath =
                "/tmp/rdepot-unit-tests/generated/0/12082025/bin/linux/centos8/x86_64/4.5/latest/plyr_1.8.8.tar.gz";
        final String actualPopulatedPath = plyr.getPopulatedPath();
        Assertions.assertEquals(expectedPopulatedPath, actualPopulatedPath, "Invalid populated path");
        Assertions.assertTrue(
                FileUtils.contentEquals(new File(plyr.getSource()), new File(actualPopulatedPath)),
                "Populated package and the source package should be the same");
    }

    private void assertArchiveSourcePackages(List<PopulatedRPackage> populatedRPackages) throws Exception {
        Assertions.assertEquals(3, populatedRPackages.size(), "Incorrect number of Archive source packages.");
        final Set<String> archiveNames = populatedRPackages.stream()
                .map(p -> p.getName() + "_" + p.getVersion())
                .collect(Collectors.toSet());
        Assertions.assertTrue(
                archiveNames.contains("plyr_1.8"), "Archive package plyr 1.8 not found in archive content.");
        Assertions.assertTrue(
                archiveNames.contains("qsort_0.2.1"), "Archive package qsort 0.2.1 not found in archive content.");
        Assertions.assertTrue(
                archiveNames.contains("qsort_0.2.2"), "Archive package qsort 0.2.2 not found in archive content.");

        final PopulatedRPackage qsort021 = populatedRPackages.get(1);
        final String expectedPopulatedPath =
                "/tmp/rdepot-unit-tests/generated/0/current/src/contrib/Archive/qsort_0.2.1.tar.gz";
        final String actualPopulatedPath = qsort021.getPopulatedPath();
        Assertions.assertEquals(expectedPopulatedPath, actualPopulatedPath, "Invalid populated path for package.");

        Assertions.assertTrue(
                FileUtils.contentEquals(new File(qsort021.getSource()), new File(actualPopulatedPath)),
                "Populated package and the source package should be the same");
    }

    private void assertLatestSourcePackages(List<PopulatedRPackage> packages) throws Exception {
        final PopulatedRPackage plyr = packages.get(0);
        final PopulatedRPackage qsort = packages.get(1);

        assertLatestSourcePlyr(plyr);
        assertLatestSourceQsort(qsort);
    }

    private void assertLatestSourcePackages_redirectToSource(List<PopulatedRPackage> packages) throws Exception {
        final PopulatedRPackage plyr = packages.get(0);
        final PopulatedRPackage qsort = packages.get(1);

        assertLatestSourcePlyr_redirectToSource(plyr);
        assertLatestSourceQsort(qsort);
    }

    private void assertLatestSourceQsort(PopulatedRPackage qsort) throws Exception {
        final String expectedVersion = "0.2.3";
        Assertions.assertEquals(expectedVersion, qsort.getVersion(), "Invalid version of populated package");

        final String expectedPopulatedPath =
                "/tmp/rdepot-unit-tests/generated/0/current/src/contrib/latest/qsort_0.2.3.tar.gz";
        final String actualPopulatedPath = qsort.getPopulatedPath();
        Assertions.assertEquals(expectedPopulatedPath, actualPopulatedPath, "Invalid path of populated package");

        Assertions.assertTrue(
                FileUtils.contentEquals(new File(qsort.getSource()), new File(actualPopulatedPath)),
                "Populated package and the source package should be the same");
    }

    private void assertLatestSourcePlyr(PopulatedRPackage plyr) throws Exception {
        final String expectedVersion = "1.8.8";
        Assertions.assertEquals(expectedVersion, plyr.getVersion(), "Invalid version of populated package");

        final String expectedPopulatedPath =
                "/tmp/rdepot-unit-tests/generated/0/current/src/contrib/latest/plyr_1.8.8.tar.gz";
        final String actualPopulatedPath = plyr.getPopulatedPath();
        Assertions.assertEquals(expectedPopulatedPath, actualPopulatedPath, "Invalid path of populated package");

        Assertions.assertTrue(
                FileUtils.contentEquals(new File(plyr.getSource()), new File(actualPopulatedPath)),
                "Populated package and the source package should be the same");
    }

    private void assertLatestSourcePlyr_redirectToSource(PopulatedRPackage plyr) throws Exception {
        final String expectedVersion = "1.8.9";
        Assertions.assertEquals(expectedVersion, plyr.getVersion(), "Invalid version of populated package");

        final String expectedPopulatedPath =
                "/tmp/rdepot-unit-tests/generated/0/current/src/contrib/latest/plyr_1.8.9.tar.gz";
        final String actualPopulatedPath = plyr.getPopulatedPath();
        Assertions.assertEquals(expectedPopulatedPath, actualPopulatedPath, "Invalid path of populated package");

        Assertions.assertTrue(
                FileUtils.contentEquals(new File(plyr.getSource()), new File(actualPopulatedPath)),
                "Populated package and the source package should be the same");
    }

    @Test
    public void organizePackagesInStorage_redirectsToSource_sourcePackagesRemainIntact() throws Exception {
        final List<RPackage> sourcePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(true);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        Assertions.assertNotNull(populatedContent);
        assertLatestSourcePackages_redirectToSource(populatedContent.latestPackages());
        Assertions.assertEquals(
                "/tmp/rdepot-unit-tests/generated/0/current/src/contrib/latest",
                populatedContent.latestDirectoryPath(),
                "Invalid latest directory path");
        List<RIndexDescriptor> indexes = populatedContent.indexes();
        indexes.sort(Comparator.comparing(RIndexDescriptor::indexLocalPath));
        final RIndexDescriptor latestIndex = indexes.get(6);

        Assertions.assertEquals(
                "8d97779a2e466479cefefb3309ec239d", latestIndex.checksum(), "Invalid checksum for index.");
        Assertions.assertEquals("src/contrib", latestIndex.indexOnRemoteRepoPath(), "Invalid remote path for index.");
        Assertions.assertEquals(
                "/tmp/rdepot-unit-tests/generated/0/current/src/contrib/latest/index.html",
                latestIndex.indexLocalPath(),
                "Invalid local path for index.");
        Assertions.assertFalse(latestIndex.archive(), "This should not be an archive index.");
        Assertions.assertEquals(
                EXPECTED_LATEST_SOURCE_INDEX_REDIRECT_TO_SOURCE,
                Files.readString(Path.of(latestIndex.indexLocalPath())),
                "Invalid index.");
    }

    @Test
    public void organizePackagesInStorage_redirectsToSource_thereShouldBeNoLatestBinaryPackages() throws Exception {
        final List<RPackage> sourcePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(true);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        Assertions.assertNotNull(populatedContent);
        Assertions.assertTrue(
                populatedContent.binLatestPackagesPaths().getAllBinLocations().stream()
                        .findFirst()
                        .orElseThrow(IllegalStateException::new)
                        .packages()
                        .isEmpty(),
                "There should be no latest binary packages.");

        List<RIndexDescriptor> indexes = populatedContent.indexes();
        indexes.sort(Comparator.comparing(RIndexDescriptor::indexLocalPath));
        final RIndexDescriptor latestIndex = indexes.get(2);
        Assertions.assertEquals(
                "f6feed00af8defc9903a1b708ddb14dc", latestIndex.checksum(), "Invalid checksum for index.");
        Assertions.assertEquals(
                "bin/linux/centos8/x86_64/4.5", latestIndex.indexOnRemoteRepoPath(), "Invalid remote path for index.");
        Assertions.assertEquals(
                "/tmp/rdepot-unit-tests/generated/0/12082025/bin/linux/centos8/x86_64/4.5/latest/index.html",
                latestIndex.indexLocalPath(),
                "Invalid local path for index.");
        Assertions.assertFalse(latestIndex.archive(), "This should not be an archive index.");
        Assertions.assertEquals(
                EXPECTED_LATEST_BINARY_INDEX_REDIRECT_TO_SOURCE,
                Files.readString(Path.of(latestIndex.indexLocalPath())),
                "Invalid index.");
    }

    @Test
    public void organizePackagesInStorage_redirectsToSource_indexShouldContainArchivedPackage() throws Exception {
        final List<RPackage> sourcePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(true);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        Assertions.assertNotNull(populatedContent);

        List<RIndexDescriptor> indexes = populatedContent.indexes();
        indexes.sort(Comparator.comparing(RIndexDescriptor::indexLocalPath));
        final RIndexDescriptor archiveIndex = indexes.get(1);
        Assertions.assertEquals(
                "fab7cf15fb3138d7fc899fa98ada0d9a", archiveIndex.checksum(), "Invalid checksum for index.");
        Assertions.assertEquals(
                "bin/linux/centos8/x86_64/4.5/Archive/plyr",
                archiveIndex.indexOnRemoteRepoPath(),
                "Invalid remote path for index.");
        Assertions.assertEquals(
                "/tmp/rdepot-unit-tests/generated/0/12082025/bin/linux/centos8/x86_64/4.5/Archive/plyrindex.html",
                archiveIndex.indexLocalPath(),
                "Invalid local path for index.");
        Assertions.assertTrue(archiveIndex.archive(), "This should be an archive index.");
        Assertions.assertEquals(
                EXPECTED_ARCHIVE_BINARY_INDEX_REDIRECT_TO_SOURCE,
                Files.readString(Path.of(archiveIndex.indexLocalPath())),
                "Invalid index.");
    }

    @Test
    public void organizePackagesInStorage_redirectsToSource_latestBinShouldBeMovedToArchive() throws Exception {
        final List<RPackage> sourcePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(true);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        Assertions.assertNotNull(populatedContent);

        PopulatedRPackage latestBin = null;
        for (PopulatedRPackage packageBag : populatedContent.binArchivePackagesPaths().getAllBinLocations().stream()
                .findFirst()
                .orElseThrow(IllegalStateException::new)
                .packages()) {
            if (packageBag.getName().equals("plyr") && packageBag.getVersion().equals("1.8.8")) {
                latestBin = packageBag;
            }
        }
        Assertions.assertNotNull(latestBin, "The latest binary packages should be archived.");
        Assertions.assertEquals(
                "/tmp/rdepot-unit-tests/generated" + "/0/12082025/bin/linux/centos8/x86_64/4.5/Archive"
                        + "/plyr_1.8.8_R_x86_64-pc-linux-gnu.tar.gz",
                latestBin.getPopulatedPath(),
                "Invalid path for populated package.");
    }

    @Test
    public void organizePackagesInStorage_redirectsToSource_shouldIncludeLatestSourcePackageInBinaryPACKAGESFile()
            throws Exception {
        final List<RPackage> sourcePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(true);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        Assertions.assertNotNull(populatedContent);

        final String expectedLatestBinaryPACKAGESFile =
                """
                Package: plyr
                Version: 1.8.9
                License: MIT + file LICENSE
                MD5sum: 5a8b129534abace172059ecc5c0b5072
                NeedsCompilation: no""";
        Assertions.assertEquals(
                expectedLatestBinaryPACKAGESFile,
                Files.readString(Path.of("/tmp/rdepot-unit-tests/generated/0/12082025/"
                        + "bin/linux/centos8/x86_64/4.5/latest/PACKAGES")),
                "Invalid PACKAGES file.");
        final List<PackagesFileDescriptor> packagesFiles = populatedContent.packagesFiles().stream()
                .sorted(Comparator.comparing(PackagesFileDescriptor::localPath))
                .toList();
        Assertions.assertEquals(
                "dc0218effa94f77a68d78df8e8c13c6b", packagesFiles.get(0).checksum(), "Invalid checksum.");
        Assertions.assertEquals(
                "b576213ecbe0f0abda2d26d94b5e4b4d", packagesFiles.get(1).checksum(), "Invalid checksum.");
    }

    @Test
    public void organizePackagesInStorage_redirectsToSource_shouldIncludeArchiveSourcePackagesInBinaryPACKAGESFile()
            throws Exception {
        final List<RPackage> sourcePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES_REDIRECT_TO_SOURCE();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(true);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        Assertions.assertNotNull(populatedContent);

        final String expectedArchiveBinaryPACKAGESFile =
                """
                Package: plyr
                Version: 1.8.6
                License: MIT + file LICENSE
                MD5sum: 6a9c2acfd924f2fb626d54168120fa08
                NeedsCompilation: no
                Built: R 4.5.1; x86_64-pc-linux-gnu; 2025-08-11 09:59:00 UTC; unix

                Package: plyr
                Version: 1.8.1
                License: MIT + file LICENSE
                MD5sum: a8b2d2284d56ab1839728040d463a360
                NeedsCompilation: no
                Built: R 4.5.1; x86_64-pc-linux-gnu; 2025-08-11 09:59:00 UTC; unix

                Package: plyr
                Version: 1.8.8
                License: MIT + file LICENSE
                MD5sum: 08841cfd5edbd118a512198217cf5f2e
                NeedsCompilation: no
                Built: R 4.5.1; x86_64-pc-linux-gnu; 2025-08-11 09:59:00 UTC; unix""";
        Assertions.assertEquals(
                expectedArchiveBinaryPACKAGESFile,
                Files.readString(Path.of("/tmp/rdepot-unit-tests/generated/0/12082025/"
                        + "bin/linux/centos8/x86_64/4.5/Archive/PACKAGES")),
                "Invalid PACKAGES file.");
    }

    @Test
    public void generateIndexes_generatesLatestSourceIndex() throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        Assertions.assertNotNull(populatedContent);
        List<RIndexDescriptor> indexes = populatedContent.indexes();
        indexes.sort(Comparator.comparing(RIndexDescriptor::indexLocalPath));
        final RIndexDescriptor latestIndex = indexes.get(6);

        Assertions.assertEquals(
                "5e375cc4155f54cc4ecbda200114713a", latestIndex.checksum(), "Invalid checksum for index.");
        Assertions.assertEquals("src/contrib", latestIndex.indexOnRemoteRepoPath(), "Invalid remote path for index.");
        Assertions.assertEquals(
                "/tmp/rdepot-unit-tests/generated/0/current/src/contrib/latest/index.html",
                latestIndex.indexLocalPath(),
                "Invalid local path for index.");
        Assertions.assertFalse(latestIndex.archive(), "This should not be an archive index.");
        Assertions.assertEquals(
                EXPECTED_LATEST_SOURCE_INDEX,
                Files.readString(Path.of(latestIndex.indexLocalPath())),
                "Invalid index.");
    }

    @Test
    public void generateIndexes_generatesArchiveSourceIndex() throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        Assertions.assertNotNull(populatedContent);
        List<RIndexDescriptor> indexes = populatedContent.indexes();
        indexes.sort(Comparator.comparing(RIndexDescriptor::indexLocalPath));
        final RIndexDescriptor index = indexes.get(3);

        Assertions.assertEquals("9c1b6f3d392fb9511a49cca6e9523a31", index.checksum(), "Invalid checksum for index.");
        Assertions.assertEquals("src/contrib/Archive", index.indexOnRemoteRepoPath(), "Invalid remote path for index.");
        Assertions.assertEquals(
                "/tmp/rdepot-unit-tests/generated/0/current/src/contrib/Archive/index_archived.html",
                index.indexLocalPath(),
                "Invalid local path for index.");
        Assertions.assertTrue(index.archive(), "This should be an archive index.");
        Assertions.assertEquals(
                EXPECTED_ARCHIVE_SOURCE_INDEX, Files.readString(Path.of(index.indexLocalPath())), "Invalid index.");
    }

    @Test
    public void generateIndexes_generatesArchiveQsortSourceIndex() throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        Assertions.assertNotNull(populatedContent);
        List<RIndexDescriptor> indexes = populatedContent.indexes();
        indexes.sort(Comparator.comparing(RIndexDescriptor::indexLocalPath));
        final RIndexDescriptor index = indexes.get(5);

        Assertions.assertEquals("2b0062db047e0f2f0c34e20dcb776b5d", index.checksum(), "Invalid checksum for index.");
        Assertions.assertEquals(
                "src/contrib/Archive/qsort", index.indexOnRemoteRepoPath(), "Invalid remote path for index.");
        Assertions.assertEquals(
                "/tmp/rdepot-unit-tests/generated/0/current/src/contrib/Archive/qsortindex.html",
                index.indexLocalPath(),
                "Invalid local path for index.");
        Assertions.assertTrue(index.archive(), "This should be an archive index.");
        Assertions.assertEquals(
                EXPECTED_QSORT_ARCHIVE_SOURCE_INDEX,
                Files.readString(Path.of(index.indexLocalPath())),
                "Invalid index.");
    }

    @Test
    public void generateIndexes_generatesLatestBinaryIndex() throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        Assertions.assertNotNull(populatedContent);
        List<RIndexDescriptor> indexes = populatedContent.indexes();
        indexes.sort(Comparator.comparing(RIndexDescriptor::indexLocalPath));
        final RIndexDescriptor index = indexes.get(2);

        Assertions.assertEquals("39e8995151ab231b9e75d6c02ddeb2a8", index.checksum(), "Invalid checksum for index.");
        Assertions.assertEquals(
                "bin/linux/centos8/x86_64/4.5", index.indexOnRemoteRepoPath(), "Invalid remote path for index.");
        Assertions.assertEquals(
                "/tmp/rdepot-unit-tests/generated/0/12082025/bin/linux/centos8/x86_64/4.5/latest/index.html",
                index.indexLocalPath(),
                "Invalid local path for index.");
        Assertions.assertFalse(index.archive(), "This should not be an archive index.");
        Assertions.assertEquals(
                EXPECTED_LATEST_BINARY_INDEX, Files.readString(Path.of(index.indexLocalPath())), "Invalid index.");
    }

    @Test
    public void generateIndexes_generatesArchiveBinaryIndex() throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        Assertions.assertNotNull(populatedContent);
        List<RIndexDescriptor> indexes = populatedContent.indexes();
        indexes.sort(Comparator.comparing(RIndexDescriptor::indexLocalPath));
        final RIndexDescriptor index = indexes.get(0);

        Assertions.assertEquals("e778d1c81f9b4649c8bed5788be89b7f", index.checksum(), "Invalid checksum for index.");
        Assertions.assertEquals(
                "bin/linux/centos8/x86_64/4.5/Archive",
                index.indexOnRemoteRepoPath(),
                "Invalid remote path for index.");
        Assertions.assertEquals(
                "/tmp/rdepot-unit-tests/generated/0/12082025/bin/linux/centos8/x86_64/4.5/Archive/index_archived.html",
                index.indexLocalPath(),
                "Invalid local path for index.");
        Assertions.assertTrue(index.archive(), "This should be an archive index.");
        Assertions.assertEquals(
                EXPECTED_ARCHIVE_BINARY_INDEX, Files.readString(Path.of(index.indexLocalPath())), "Invalid index.");
    }

    @Test
    public void generateIndexes_generatesArchivePlyrBinaryIndex() throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        final PopulatedRepositoryContent populatedContent = rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);

        Assertions.assertNotNull(populatedContent);
        List<RIndexDescriptor> indexes = populatedContent.indexes();
        indexes.sort(Comparator.comparing(RIndexDescriptor::indexLocalPath));
        final RIndexDescriptor index = indexes.get(1);

        Assertions.assertEquals("6b3a1eee255f90dcf6d58894390bd0db", index.checksum(), "Invalid checksum for index.");
        Assertions.assertEquals(
                "bin/linux/centos8/x86_64/4.5/Archive/plyr",
                index.indexOnRemoteRepoPath(),
                "Invalid remote path for index.");
        Assertions.assertEquals(
                "/tmp/rdepot-unit-tests/generated/0/12082025/bin/linux/centos8/x86_64/4.5/Archive/plyrindex.html",
                index.indexLocalPath(),
                "Invalid local path for index.");
        Assertions.assertTrue(index.archive(), "This should be an archive index.");
        Assertions.assertEquals(
                EXPECTED_ARCHIVE_PLYR_BINARY_INDEX,
                Files.readString(Path.of(index.indexLocalPath())),
                "Invalid index.");
    }

    @Test
    public void buildSynchronizedRequestBody_replaceSamePackages() throws Exception {
        Checksums remoteChecksums = new Checksums();

        final PopulatedRepositoryContent populatedContent =
                getExamplePopulatedContent_forReplacingSamePackage(remoteChecksums);

        final MultiValueMap<String, String> remoteLatestBinaries = new LinkedMultiValueMap<>();
        remoteLatestBinaries.add("bin/linux/centos8/x86_64/4.5", "plyr_1.8.8.tar.gz");
        final MultiValueMap<String, String> remoteArchiveBinaries = new LinkedMultiValueMap<>();
        remoteArchiveBinaries.add("bin/linux/centos8/x86_64/4.5", "plyr_1.8.6.tar.gz");
        remoteArchiveBinaries.add("bin/linux/centos8/x86_64/4.5", "plyr_1.8.1.tar.gz");

        final SynchronizeRepositoryRequestBody requestBody = rLocalPopulator.buildSynchronizeRequestBody(
                populatedContent,
                List.of("plyr_1.8.8.tar.gz", "qsort_0.2.3.tar.gz"),
                List.of("plyr_1.8.tar.gz", "qsort_0.2.2.tar.gz", "qsort_0.2.1.tar.gz"),
                remoteLatestBinaries,
                remoteArchiveBinaries,
                remoteChecksums,
                populatedContent.latestPackages().get(0).getRepository(),
                "0");
        Assertions.assertEquals(
                0, requestBody.getSourcePackagesToUpload().size(), "Invalid package count in the request body.");
        Assertions.assertEquals(
                1,
                requestBody.getSourcePackagesToUploadToArchive().size(),
                "Invalid package count in the request body.");
        Assertions.assertFalse(
                requestBody.getBinaryPackagesToUpload().isEmpty(), "There should be packages to upload.");
        Assertions.assertTrue(
                requestBody.getBinaryPackagesToUploadToArchive().isEmpty(), "There should be no packages to upload.");
        Assertions.assertEquals(
                1,
                requestBody.getBinaryPackagesToUpload().size(),
                "Invalid number of archive binary packages to upload");

        Assertions.assertEquals(2, requestBody.getPackagesFiles().size(), "Invalid number of PACKAGES files.");
        Assertions.assertEquals(
                2, requestBody.getPackagesFilesForArchive().size(), "Invalid number of PACKAGES files for archive.");
        Assertions.assertEquals(2, requestBody.getPackagesGzFiles().size(), "Invalid number of PACKAGES.gz files.");
        Assertions.assertEquals(
                2,
                requestBody.getPackagesGzFilesForArchive().size(),
                "Invalid number of PACKAGES.gz files for archive.");
        Assertions.assertTrue(
                requestBody.getSourcePackagesToDelete().isEmpty(), "There should be no packages to delete.");
        Assertions.assertTrue(
                requestBody.getSourcePackagesToDeleteFromArchive().isEmpty(), "There should be no packages to delete.");
        Assertions.assertTrue(
                requestBody.getBinaryPackagesToDelete().isEmpty(), "There should be no packages to delete.");
        Assertions.assertTrue(
                requestBody.getBinaryPackagesToDeleteFromArchive().isEmpty(), "There should be no packages to delete.");
        Assertions.assertEquals(2, requestBody.getIndexes().size(), "Invalid number of indexes in the request body.");
        Assertions.assertEquals(5, requestBody.getIndexesForArchive().size(), "Invalid number of indexes for archive.");
        Assertions.assertEquals(23, requestBody.getChecksums().toMap().size(), "Invalid number of checksums.");
    }

    private PopulatedRepositoryContent getExamplePopulatedContent_forReplacingSamePackage(Checksums remoteChecksums)
            throws Exception {
        final List<RPackage> sourcePackages = RPackageTestFixture.RPackagePopulationFixture.GET_SOURCE_PACKAGES();
        final List<RPackage> sourceLatestPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_SOURCE_PACKAGES();
        sourceLatestPackages.forEach(
                rp -> remoteChecksums.addChecksum(new Checksum(rp.getPackageFolderPath(false), rp.getMd5sum())));

        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        remoteChecksums.addChecksum(new Checksum(
                sourceArchivePackages.get(0).getPackageFolderPath(true),
                sourceArchivePackages.get(0).getMd5sum()));
        remoteChecksums.addChecksum(new Checksum(
                sourceArchivePackages.get(1).getPackageFolderPath(true),
                sourceArchivePackages.get(1).getMd5sum()));
        remoteChecksums.addChecksum(
                new Checksum(sourceArchivePackages.get(2).getPackageFolderPath(true), "differentChecksum"));

        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        archiveBinaryPackages.forEach(
                rp -> remoteChecksums.addChecksum(new Checksum(rp.getPackageFolderPath(true), rp.getMd5sum())));

        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        remoteChecksums.addChecksum(
                new Checksum(latestBinaryPackages.get(0).getPackageFolderPath(false), "whateverChecksum"));

        final RRepository repository = RPackageTestFixture.RPackagePopulationFixture.GET_EXAMPLE_R_REPOSITORY();
        repository.setRedirectToSource(false);
        final String dateStamp = "12082025";

        return rLocalPopulator.organizePackagesInStorage(
                dateStamp,
                sourcePackages,
                sourceLatestPackages,
                sourceArchivePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                PLATFORMS,
                repository);
    }
}

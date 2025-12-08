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
package eu.openanalytics.rdepot.test.unit.synchronization;

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.messaging.StaticMessageResolver;
import eu.openanalytics.rdepot.base.synchronization.checksums.Checksum;
import eu.openanalytics.rdepot.base.synchronization.checksums.Checksums;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.manuals.ManualGenerator;
import eu.openanalytics.rdepot.r.manuals.implementations.fs.LocalFSManualGenerator;
import eu.openanalytics.rdepot.r.storage.BinLocation;
import eu.openanalytics.rdepot.r.storage.BinLocationSet;
import eu.openanalytics.rdepot.r.storage.implementations.RLocalStorage;
import eu.openanalytics.rdepot.r.storage.indexes.ArchiveIndexGenerator;
import eu.openanalytics.rdepot.r.storage.indexes.RIndexDescriptor;
import eu.openanalytics.rdepot.r.storage.indexes.RIndexGenerator;
import eu.openanalytics.rdepot.r.storage.indexes.RPackageIndexGenerator;
import eu.openanalytics.rdepot.r.storage.indexes.RRepositoryIndexGenerator;
import eu.openanalytics.rdepot.r.storage.packagesfile.PackageStringGenerator;
import eu.openanalytics.rdepot.r.storage.packagesfile.PackagesFileDescriptor;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private static final String EXPECTED_LATEST_SOURCE_INDEX_REDIRECT_TO_SOURCE =
            """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <meta name="arr:repository-version" content="0">
                        <title>arr</title>
                    </head>
                    <body>
                    \t<h1>arr</h1>
                        <a href="/Archive/index.html">Archive</a>
                    <div class="package" id="package-41">
                        <a href="/repo/arr/src/contrib/plyr_1.8.9.tar.gz">plyr</a>
                        <div class="package-version">1.8.9</div>
                        <div class="package-title">Tools for Splitting, Applying and Combining Data</div>
                        <div class="package-description">A set of tools that solves a common set of problems: you need\\n to break a big problem down into manageable pieces, operate on each\\n piece and then put all the pieces back together. For example, you\\n might want to fit a model to each spatial location or time point in\\n your study, summarise data by panels or collapse high-dimensional\\n arrays to simpler summary statistics. The development of 'plyr' has\\n been generously supported by 'Becton Dickinson'.</div>
                        <div class="package-maintainer">null</div>
                    </div>
                    <div class="package" id="package-40">
                        <a href="/repo/arr/src/contrib/qsort_0.2.3.tar.gz">qsort</a>
                        <div class="package-version">0.2.3</div>
                        <div class="package-title">Scoring Q-Sort Data</div>
                        <div class="package-description">Computes scores from Q-sort data, using criteria sorts and\\n derived scales from subsets of items.\\n The 'qsort' package includes descriptions and scoring procedures\\n for four different Q-sets commonly used in developmental psychology research:\\n Attachment Q-set (version 3.0) (Waters, 1995, <doi:10.1111/j.1540-5834.1995.tb00214.x>);\\n California Child Q-set (Block and Block, 1969, <doi:10.1037/0012-1649.21.3.508>);\\n Maternal Behaviour Q-set (version 3.1)\\n (Pederson et al., 1999, <https://ir.lib.uwo.ca/cgi/viewcontent.cgi?article=1000&context=psychologypub>);\\n Preschool Q-set (Baumrind, 1968 revised by Wanda Bronson, <doi:10.1111/j.1540-5834.1995.tb00214.x>).</div>
                        <div class="package-maintainer">null</div>
                    </div>
                    </body>
                    </html>
                    """;
    private static final String EXPECTED_LATEST_BINARY_INDEX_REDIRECT_TO_SOURCE =
            """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <meta name="arr:repository-version" content="0">
                        <title>arr</title>
                    </head>
                    <body>
                    \t<h1>arr</h1>
                        <a href="/Archive/index.html">Archive</a>
                    </body>
                    </html>
                    """;
    private static final String EXPECTED_ARCHIVE_BINARY_INDEX_REDIRECT_TO_SOURCE =
            """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <meta name="arr:repository-version" content="0">
                        <title>Links for plyr</title>
                    </head>
                    <body>
                    \t<h1>Links for plyr</h1>
                    <div class="package" id="package-31">
                        <a href="/repo/arr/bin/linux/centos8/x86_64/4.5/Archive/plyr/plyr_1.8.6.tar.gz">plyr</a>
                        <div class="package-version">1.8.6</div>
                        <div class="package-title">Tools for Splitting, Applying and Combining Data</div>
                        <div class="package-description">A set of tools that solves a common set of\\n problems: you need to break a big problem down into manageable pieces,\\n operate on each piece and then put all the pieces back together. For\\n example, you might want to fit a model to each spatial location or\\n time point in your study, summarise data by panels or collapse\\n high-dimensional arrays to simpler summary statistics. The development\\n of 'plyr' has been generously supported by 'Becton Dickinson'.</div>
                        <div class="package-maintainer">null</div>
                    </div>
                    <div class="package" id="package-33">
                        <a href="/repo/arr/bin/linux/centos8/x86_64/4.5/Archive/plyr/plyr_1.8.1.tar.gz">plyr</a>
                        <div class="package-version">1.8.1</div>
                        <div class="package-title">Tools for splitting, applying and combining data</div>
                        <div class="package-description">plyr is a set of tools that solves a common\\n set of problems: you need to break a big problem down\\n into manageable pieces, operate on each pieces and then\\n put all the pieces back together. For example, you\\n might want to fit a model to each spatial location or\\n time point in your study, summarise data by panels or\\n collapse high-dimensional arrays to simpler summary\\n statistics. The development of plyr has been generously\\n supported by BD (Becton Dickinson).</div>
                        <div class="package-maintainer">null</div>
                    </div>
                    <div class="package" id="package-36">
                        <a href="/repo/arr/bin/linux/centos8/x86_64/4.5/Archive/plyr/plyr_1.8.8.tar.gz">plyr</a>
                        <div class="package-version">1.8.8</div>
                        <div class="package-title">Tools for Splitting, Applying and Combining Data</div>
                        <div class="package-description">A set of tools that solves a common set of problems: you need\\n to break a big problem down into manageable pieces, operate on each\\n piece and then put all the pieces back together. For example, you\\n might want to fit a model to each spatial location or time point in\\n your study, summarise data by panels or collapse high-dimensional\\n arrays to simpler summary statistics. The development of 'plyr' has\\n been generously supported by 'Becton Dickinson'.</div>
                        <div class="package-maintainer">null</div>
                    </div>
                    </body>
                    </html>
                    """;

    @InjectMocks
    private RLocalPopulator rLocalPopulator;

    @InjectMocks
    private StaticMessageResolver staticMessageResolver;

    @Spy
    private final RLocalStorage storage = new RLocalStorage();

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

    private static final String EXPECTED_LATEST_SOURCE_INDEX =
            """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="arr:repository-version" content="0">
                <title>arr</title>
            </head>
            <body>
            \t<h1>arr</h1>
                <a href="/Archive/index.html">Archive</a>
            <div class="package" id="package-36">
                <a href="/repo/arr/src/contrib/plyr_1.8.8.tar.gz">plyr</a>
                <div class="package-version">1.8.8</div>
                <div class="package-title">Tools for Splitting, Applying and Combining Data</div>
                <div class="package-description">A set of tools that solves a common set of problems: you need\\n to break a big problem down into manageable pieces, operate on each\\n piece and then put all the pieces back together. For example, you\\n might want to fit a model to each spatial location or time point in\\n your study, summarise data by panels or collapse high-dimensional\\n arrays to simpler summary statistics. The development of 'plyr' has\\n been generously supported by 'Becton Dickinson'.</div>
                <div class="package-maintainer">null</div>
            </div>
            <div class="package" id="package-40">
                <a href="/repo/arr/src/contrib/qsort_0.2.3.tar.gz">qsort</a>
                <div class="package-version">0.2.3</div>
                <div class="package-title">Scoring Q-Sort Data</div>
                <div class="package-description">Computes scores from Q-sort data, using criteria sorts and\\n derived scales from subsets of items.\\n The 'qsort' package includes descriptions and scoring procedures\\n for four different Q-sets commonly used in developmental psychology research:\\n Attachment Q-set (version 3.0) (Waters, 1995, <doi:10.1111/j.1540-5834.1995.tb00214.x>);\\n California Child Q-set (Block and Block, 1969, <doi:10.1037/0012-1649.21.3.508>);\\n Maternal Behaviour Q-set (version 3.1)\\n (Pederson et al., 1999, <https://ir.lib.uwo.ca/cgi/viewcontent.cgi?article=1000&context=psychologypub>);\\n Preschool Q-set (Baumrind, 1968 revised by Wanda Bronson, <doi:10.1111/j.1540-5834.1995.tb00214.x>).</div>
                <div class="package-maintainer">null</div>
            </div>
            </body>
            </html>
            """;

    private static final String EXPECTED_ARCHIVE_SOURCE_INDEX =
            """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="arr:repository-version" content="0">
                <title>arr</title>
            </head>
            <body>
            \t<h1>arr</h1>
            <div class="package" id="package-plyr">
                <a href="/repo/arr/src/contrib/Archive/plyr">plyr</a>
            </div>
            <div class="package" id="package-qsort">
                <a href="/repo/arr/src/contrib/Archive/qsort">qsort</a>
            </div>
            </body>
            </html>
            """;

    private static final String EXPECTED_QSORT_ARCHIVE_SOURCE_INDEX =
            """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="arr:repository-version" content="0">
                <title>Links for qsort</title>
            </head>
            <body>
            \t<h1>Links for qsort</h1>
            <div class="package" id="package-35">
                <a href="/repo/arr/src/contrib/Archive/qsort/qsort_0.2.1.tar.gz">qsort</a>
                <div class="package-version">0.2.1</div>
                <div class="package-title">Scoring Q-Sort Data</div>
                <div class="package-description">Computes scores from Q-sort data, using criteria sorts and\\n derived scales from subsets of items.\\n The 'qsort' package includes descriptions and scoring procedures\\n for four different Q-sets:\\n Attachment Q-set (version 3.0) (Waters, 1995, <doi:10.1111/j.1540-5834.1995.tb00214.x>);\\n California Child Q-set (Block and Block, 1969, <doi:10.1037/0012-1649.21.3.508>);\\n Maternal Behaviour Q-set (version 3.1)\\n (Pederson et al., 1999, <https://ir.lib.uwo.ca/cgi/viewcontent.cgi?article=1000&context=psychologypub>);\\n Preschool Q-set (Baumrind, 1968 revised by Wanda Bronson, <doi:10.1111/j.1540-5834.1995.tb00214.x>).</div>
                <div class="package-maintainer">null</div>
            </div>
            <div class="package" id="package-39">
                <a href="/repo/arr/src/contrib/Archive/qsort/qsort_0.2.2.tar.gz">qsort</a>
                <div class="package-version">0.2.2</div>
                <div class="package-title">Scoring Q-Sort Data</div>
                <div class="package-description">Computes scores from Q-sort data, using criteria sorts and\\n derived scales from subsets of items.\\n The 'qsort' package includes descriptions and scoring procedures\\n for four different Q-sets:\\n Attachment Q-set (version 3.0) (Waters, 1995, <doi:10.1111/j.1540-5834.1995.tb00214.x>);\\n California Child Q-set (Block and Block, 1969, <doi:10.1037/0012-1649.21.3.508>);\\n Maternal Behaviour Q-set (version 3.1)\\n (Pederson et al., 1999, <https://ir.lib.uwo.ca/cgi/viewcontent.cgi?article=1000&context=psychologypub>);\\n Preschool Q-set (Baumrind, 1968 revised by Wanda Bronson, <doi:10.1111/j.1540-5834.1995.tb00214.x>).</div>
                <div class="package-maintainer">null</div>
            </div>
            </body>
            </html>
            """;

    private static final String EXPECTED_LATEST_BINARY_INDEX =
            """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="arr:repository-version" content="0">
                <title>arr</title>
            </head>
            <body>
            \t<h1>arr</h1>
                <a href="/Archive/index.html">Archive</a>
            <div class="package" id="package-36">
                <a href="/repo/arr/bin/linux/centos8/x86_64/4.5/plyr_1.8.8.tar.gz">plyr</a>
                <div class="package-version">1.8.8</div>
                <div class="package-title">Tools for Splitting, Applying and Combining Data</div>
                <div class="package-description">A set of tools that solves a common set of problems: you need\\n to break a big problem down into manageable pieces, operate on each\\n piece and then put all the pieces back together. For example, you\\n might want to fit a model to each spatial location or time point in\\n your study, summarise data by panels or collapse high-dimensional\\n arrays to simpler summary statistics. The development of 'plyr' has\\n been generously supported by 'Becton Dickinson'.</div>
                <div class="package-maintainer">null</div>
            </div>
            </body>
            </html>
            """;

    private static final String EXPECTED_ARCHIVE_BINARY_INDEX =
            """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="arr:repository-version" content="0">
                <title>arr</title>
            </head>
            <body>
            \t<h1>arr</h1>
            <div class="package" id="package-plyr">
                <a href="/repo/arr/bin/linux/centos8/x86_64/4.5/Archive/plyr">plyr</a>
            </div>
            </body>
            </html>
            """;

    private static final String EXPECTED_ARCHIVE_PLYR_BINARY_INDEX =
            """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="arr:repository-version" content="0">
                <title>Links for plyr</title>
            </head>
            <body>
            \t<h1>Links for plyr</h1>
            <div class="package" id="package-31">
                <a href="/repo/arr/bin/linux/centos8/x86_64/4.5/Archive/plyr/plyr_1.8.6.tar.gz">plyr</a>
                <div class="package-version">1.8.6</div>
                <div class="package-title">Tools for Splitting, Applying and Combining Data</div>
                <div class="package-description">A set of tools that solves a common set of\\n problems: you need to break a big problem down into manageable pieces,\\n operate on each piece and then put all the pieces back together. For\\n example, you might want to fit a model to each spatial location or\\n time point in your study, summarise data by panels or collapse\\n high-dimensional arrays to simpler summary statistics. The development\\n of 'plyr' has been generously supported by 'Becton Dickinson'.</div>
                <div class="package-maintainer">null</div>
            </div>
            <div class="package" id="package-33">
                <a href="/repo/arr/bin/linux/centos8/x86_64/4.5/Archive/plyr/plyr_1.8.1.tar.gz">plyr</a>
                <div class="package-version">1.8.1</div>
                <div class="package-title">Tools for splitting, applying and combining data</div>
                <div class="package-description">plyr is a set of tools that solves a common\\n set of problems: you need to break a big problem down\\n into manageable pieces, operate on each pieces and then\\n put all the pieces back together. For example, you\\n might want to fit a model to each spatial location or\\n time point in your study, summarise data by panels or\\n collapse high-dimensional arrays to simpler summary\\n statistics. The development of plyr has been generously\\n supported by BD (Becton Dickinson).</div>
                <div class="package-maintainer">null</div>
            </div>
            </body>
            </html>
            """;

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
        final String expectedMd5Sum = "46b11fe68466c613360279fbdb887782";
        final String expectedGzMd5Sum = "67b888df733317410fe417777fddbda3";
        final String expectedContent =
                """
                Package: plyr
                Version: 1.8.8
                License: MIT + file LICENSE
                MD5Sum: 0a22da16605ee765e7d4f1efc9f7a61f
                NeedsCompilation: no

                Package: qsort
                Version: 0.2.3
                License: GPL-3
                MD5Sum: 3204109d62ec7ff8e44bd15a989fc8b1
                NeedsCompilation: no

                """;

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
        final String expectedMd5Sum = "6e60a404356088a4868c338a8a71db05";
        final String expectedGzMd5Sum = "745f12f610b98a9dc611622632595a2f";
        final String expectedContent =
                """
                Package: plyr
                Version: 1.8.6
                License: MIT + file LICENSE
                MD5Sum: 6a9c2acfd924f2fb626d54168120fa08
                NeedsCompilation: no
                Built: R 4.5.1; x86_64-pc-linux-gnu; 2025-08-11 09:59:00 UTC; unix

                Package: plyr
                Version: 1.8.1
                License: MIT + file LICENSE
                MD5Sum: a8b2d2284d56ab1839728040d463a360
                NeedsCompilation: no
                Built: R 4.5.1; x86_64-pc-linux-gnu; 2025-08-11 09:59:00 UTC; unix

                """;

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
        final String expectedMd5Sum = "f6114790816cc67b302fc93ef1f9458c";
        final String expectedGzMd5Sum = "111352577d23fd5291e4d1f2306b08df";
        final String expectedContent =
                """
                Package: plyr
                Version: 1.8.8
                License: MIT + file LICENSE
                MD5Sum: 08841cfd5edbd118a512198217cf5f2e
                NeedsCompilation: no
                Built: R 4.5.1; x86_64-pc-linux-gnu; 2025-08-11 09:59:00 UTC; unix

                """;

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
        final String expectedMd5Sum = "c52287b7ce81342e423d7d0d5cef2f4b";
        final String expectedGzMd5Sum = "bed11e678efc1b6be6f0b8a04372917c";
        final String expectedContent =
                """
                Package: plyr
                Version: 1.8
                License: MIT
                MD5Sum: e1c1d2f0c47fd16b2cef6ec9c2e5883c
                NeedsCompilation: no

                Package: qsort
                Version: 0.2.1
                License: GPL-3
                MD5Sum: 5dd316a3591a86ff3d6cda0526c67ba5
                NeedsCompilation: no

                Package: qsort
                Version: 0.2.2
                License: GPL-3
                MD5Sum: 76346f1a4ef62977b0acf794c6bb0aef
                NeedsCompilation: no

                """;

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
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        archiveBinaryPackages.remove(0);

        final List<RPackage> binaryPackages = new ArrayList<>(archiveBinaryPackages);
        final List<RPackage> latestBinaryPackages = new ArrayList<>();
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
        sourceLatestPackages.forEach(rp -> {
            remoteChecksums.addChecksum(new Checksum(rp.getPackageFolderPath(), rp.getMd5sum()));
        });
        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        sourceArchivePackages.forEach(rp -> {
            remoteChecksums.addChecksum(new Checksum(rp.getPackageFolderPath(), rp.getMd5sum()));
        });
        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        archiveBinaryPackages.forEach(rp -> {
            remoteChecksums.addChecksum(new Checksum(rp.getPackageFolderPath(), rp.getMd5sum()));
        });
        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        latestBinaryPackages.forEach(rp -> {
            remoteChecksums.addChecksum(new Checksum(rp.getPackageFolderPath(), rp.getMd5sum()));
        });
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
                "f6114790816cc67b302fc93ef1f9458c",
                filesInFirstChunk.get(0));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/"
                        + "latest/binlinuxcentos8x866445_PACKAGES.gz",
                "111352577d23fd5291e4d1f2306b08df",
                filesInFirstChunk.get(1));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/"
                        + "latest/binlinuxcentos8x866445_index.html",
                "a85cf61c99ce885782d85444f0da9cce",
                filesInFirstChunk.get(2));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/"
                        + "latest/binlinuxcentos8x866445_plyr_1.8.8.tar.gz",
                "8841cfd5edbd118a512198217cf5f2e",
                filesInFirstChunk.get(3));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/latest/srccontrib_PACKAGES",
                "8b49503612d3edfe9c4411640b79b773",
                filesInFirstChunk.get(4));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/latest/srccontrib_PACKAGES.gz",
                "ff00aa637248550a363c921771701a2f",
                filesInFirstChunk.get(5));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/latest/srccontrib_index.html",
                "977665966f27c8d1e9c99694c7eabf60",
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
                "e5d155941dc63ef3cf66b489feaca96d",
                checksums.get("srccontribArchive_index_archived.html"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "6a9c2acfd924f2fb626d54168120fa08",
                checksums.get("binlinuxcentos8x866445_plyr_1.7.1.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "6a260ddacd02e52df84ddf5541448e3a",
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
                "253b785839e310b80a7ea0ef9b972d6d",
                checksums.get("srccontribArchive_PACKAGES"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "a8b2d2284d56ab1839728040d463a360",
                checksums.get("binlinuxcentos8x866445_plyr_1.8.1.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "5205085cc167bf1ff77ed91ca1427616",
                checksums.get("srccontribArchive_PACKAGES.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "08841cfd5edbd118a512198217cf5f2e",
                checksums.get("binlinuxcentos8x866445_plyr_1.8.8.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "a85cf61c99ce885782d85444f0da9cce",
                checksums.get("binlinuxcentos8x866445_index.html"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "b39ae52a3791ab332170481f7e86f5c1",
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
                "8b49503612d3edfe9c4411640b79b773", checksums.get("srccontrib_PACKAGES"), "Invalid checksum for file");
        Assertions.assertEquals(
                "790550f8e5f46a74d23bdf9eff598570",
                checksums.get("binlinuxcentos8x866445Archive_index_archived.html"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "6a9c2acfd924f2fb626d54168120fa08",
                checksums.get("binlinuxcentos8x866445_plyr_1.8.6.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "74a0143bf0e6ec522ea7e72590075c3a",
                checksums.get("binlinuxcentos8x866445Archive_PACKAGES"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "76346f1a4ef62977b0acf794c6bb0aef",
                checksums.get("srccontrib_qsort_0.2.2.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "977665966f27c8d1e9c99694c7eabf60",
                checksums.get("srccontrib_index.html"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "6a9c2acfd924f2fb626d54168120fa08",
                checksums.get("binlinuxcentos8x866445_plyr_1.7.0.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "ff00aa637248550a363c921771701a2f",
                checksums.get("srccontrib_PACKAGES.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "3ab74f0fe54414ecc73a63f58e37dcc6",
                checksums.get("binlinuxcentos8x866445Archive_PACKAGES.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "3204109d62ec7ff8e44bd15a989fc8b1",
                checksums.get("srccontrib_qsort_0.2.3.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "111352577d23fd5291e4d1f2306b08df",
                checksums.get("binlinuxcentos8x866445_PACKAGES.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "5dd316a3591a86ff3d6cda0526c67ba5",
                checksums.get("srccontrib_qsort_0.2.1.tar.gz"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "42e03df64c4a1a520806ead747571484",
                checksums.get("srccontribArchiveplyr_plyrindex.html"),
                "Invalid checksum for file");
        Assertions.assertEquals(
                "f6114790816cc67b302fc93ef1f9458c",
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
                "74a0143bf0e6ec522ea7e72590075c3a",
                filesInFirstChunk.get(0));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/Archive/"
                        + "binlinuxcentos8x866445Archive_PACKAGES.gz",
                "3ab74f0fe54414ecc73a63f58e37dcc6",
                filesInFirstChunk.get(1));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/Archive/"
                        + "binlinuxcentos8x866445Archive_index_archived.html",
                "790550f8e5f46a74d23bdf9eff598570",
                filesInFirstChunk.get(2));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/12082025/" + "bin/linux/centos8/x86_64/4.5/Archive/"
                        + "binlinuxcentos8x866445Archiveplyr_plyrindex.html",
                "b39ae52a3791ab332170481f7e86f5c1",
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
                "253b785839e310b80a7ea0ef9b972d6d",
                filesInFirstChunk.get(7));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/Archive/"
                        + "srccontribArchive_PACKAGES.gz",
                "5205085cc167bf1ff77ed91ca1427616",
                filesInFirstChunk.get(8));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/Archive/"
                        + "srccontribArchiveplyr_plyrindex.html",
                "42e03df64c4a1a520806ead747571484",
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
                "e5d155941dc63ef3cf66b489feaca96d",
                filesInChunk.get(3));
        assertFile(
                "/tmp/rdepot-unit-tests/generated/0/current/" + "src/contrib/Archive/"
                        + "srccontribArchiveqsort_qsortindex.html",
                "6a260ddacd02e52df84ddf5541448e3a",
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
        final PopulatedRepositoryContent populatedContent = getExamplePopulatedContent_almostEmpty();

        final MultiValueMap<String, String> remoteLatestBinaries = new LinkedMultiValueMap<>();
        remoteLatestBinaries.add("bin/linux/centos8/x86_64/4.5", "plyr_1.8.8.tar.gz");
        final MultiValueMap<String, String> remoteArchiveBinaries = new LinkedMultiValueMap<>();
        remoteArchiveBinaries.add("bin/linux/centos8/x86_64/4.5", "plyr_1.8.6.tar.gz");
        remoteArchiveBinaries.add("bin/linux/centos8/x86_64/4.5", "plyr_1.8.1.tar.gz");

        Checksums remoteChecksums = new Checksums();
        remoteChecksums.addChecksum(
                new Checksum("bin/linux/centos8/x86_64/4.5/plyr_1.8.8.tar.gz", "notCheckedChecksumHere"));
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
                populatedContent.binArchivePackagesPaths().getAllBinLocations().stream()
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
                1, requestBody.getBinaryPackagesToDelete().size(), "There should be one packages to delete.");
        Assertions.assertEquals(
                1, requestBody.getBinaryPackagesToDeleteFromArchive().size(), "There should be no packages to delete.");
        Assertions.assertEquals(2, requestBody.getIndexes().size(), "Invalid number of indexes in the request body.");
        Assertions.assertEquals(3, requestBody.getIndexesForArchive().size(), "Invalid number of indexes for archive.");
        Assertions.assertEquals(14, requestBody.getChecksums().toMap().size(), "Invalid number of checksums.");
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
                "d8adff68b55ed4898a6584efc236c1e7", latestIndex.checksum(), "Invalid checksum for index.");
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
                "79c8a3f83414e52561b245dd63366c67", latestIndex.checksum(), "Invalid checksum for index.");
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
                "0436cb6c22c0c327f62c8b80d0951c4c", archiveIndex.checksum(), "Invalid checksum for index.");
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
                MD5Sum: 5a8b129534abace172059ecc5c0b5072
                NeedsCompilation: no

                """;
        Assertions.assertEquals(
                expectedLatestBinaryPACKAGESFile,
                Files.readString(Path.of("/tmp/rdepot-unit-tests/generated/0/12082025/"
                        + "bin/linux/centos8/x86_64/4.5/latest/PACKAGES")),
                "Invalid PACKAGES file.");
        final List<PackagesFileDescriptor> packagesFiles = populatedContent.packagesFiles().stream()
                .sorted(Comparator.comparing(PackagesFileDescriptor::localPath))
                .toList();
        Assertions.assertEquals(
                "01803dc04cde03459b88258569fb7505", packagesFiles.get(6).checksum(), "Invalid checksum.");
        Assertions.assertEquals(
                "9c83860ea71160b554995e5ae9ef12d5", packagesFiles.get(7).checksum(), "Invalid checksum.");
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
                MD5Sum: 6a9c2acfd924f2fb626d54168120fa08
                NeedsCompilation: no
                Built: R 4.5.1; x86_64-pc-linux-gnu; 2025-08-11 09:59:00 UTC; unix

                Package: plyr
                Version: 1.8.1
                License: MIT + file LICENSE
                MD5Sum: a8b2d2284d56ab1839728040d463a360
                NeedsCompilation: no
                Built: R 4.5.1; x86_64-pc-linux-gnu; 2025-08-11 09:59:00 UTC; unix

                Package: plyr
                Version: 1.8.8
                License: MIT + file LICENSE
                MD5Sum: 08841cfd5edbd118a512198217cf5f2e
                NeedsCompilation: no
                Built: R 4.5.1; x86_64-pc-linux-gnu; 2025-08-11 09:59:00 UTC; unix

                """;
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
                "d0327cffbc919015b0a5679633562ef2", latestIndex.checksum(), "Invalid checksum for index.");
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

        Assertions.assertEquals("e5d155941dc63ef3cf66b489feaca96d", index.checksum(), "Invalid checksum for index.");
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

        Assertions.assertEquals("7cdd8e8f2cbd8bce57b3b85bb9bf8445", index.checksum(), "Invalid checksum for index.");
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

        Assertions.assertEquals("a85cf61c99ce885782d85444f0da9cce", index.checksum(), "Invalid checksum for index.");
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

        Assertions.assertEquals("790550f8e5f46a74d23bdf9eff598570", index.checksum(), "Invalid checksum for index.");
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

        Assertions.assertEquals("0710c59603e05fbd4b26e83c48f6110f", index.checksum(), "Invalid checksum for index.");
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
        sourceLatestPackages.forEach(rp -> {
            remoteChecksums.addChecksum(new Checksum(rp.getPackageFolderPath(), rp.getMd5sum()));
        });

        final List<RPackage> sourceArchivePackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_SOURCE_PACKAGES();
        remoteChecksums.addChecksum(new Checksum(
                sourceArchivePackages.get(0).getPackageFolderPath(),
                sourceArchivePackages.get(0).getMd5sum()));
        remoteChecksums.addChecksum(new Checksum(
                sourceArchivePackages.get(1).getPackageFolderPath(),
                sourceArchivePackages.get(1).getMd5sum()));
        remoteChecksums.addChecksum(
                new Checksum(sourceArchivePackages.get(2).getPackageFolderPath(), "differentChecksum"));

        final List<RPackage> binaryPackages = RPackageTestFixture.RPackagePopulationFixture.GET_BINARY_PACKAGES();
        final List<RPackage> archiveBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_ARCHIVE_BINARY_PACKAGES();
        archiveBinaryPackages.forEach(rp -> {
            remoteChecksums.addChecksum(new Checksum(rp.getPackageFolderPath(), rp.getMd5sum()));
        });

        final List<RPackage> latestBinaryPackages =
                RPackageTestFixture.RPackagePopulationFixture.GET_LATEST_BINARY_PACKAGES();
        remoteChecksums.addChecksum(
                new Checksum(latestBinaryPackages.get(0).getPackageFolderPath(), "whateverChecksum"));

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

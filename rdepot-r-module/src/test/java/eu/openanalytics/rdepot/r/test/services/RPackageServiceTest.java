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
package eu.openanalytics.rdepot.r.test.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.r.daos.RPackageDao;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.test.fixture.RPackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.UnitTest;
import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class RPackageServiceTest extends UnitTest {

    private static final String TEST_PACKAGES_FOR_SOURCES_PATH = "src/test/resources/unit/test_packages";
    private static final String TEST_PACKAGES_FOR_BINARIES_PATH =
            "src/test/resources/unit/test_packages/binary_package";
    private static final String TEST_PACKAGES_FOR_BINARIES_CENTOS7_4_2_ARCHIVE_PATH =
            TEST_PACKAGES_FOR_BINARIES_PATH + "/archive_4_2";
    private static final String TEST_PACKAGES_FOR_BINARIES_CENTOS7_4_5_LATEST_PATH =
            TEST_PACKAGES_FOR_BINARIES_PATH + "/latest_4_5";

    @Mock
    RPackageDao rPackageDao;

    private final RPackageService packageService = new RPackageService(rPackageDao);

    @Test
    public void filterLatestPackages() {
        final User user = UserTestFixture.GET_REGULAR_USER();

        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(2);
        repository.setPublished(true);
        repository.setVersion(5);

        final RPackage accruedArchivePackage = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        accruedArchivePackage.setSource(
                new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/accrued_1_2.tar.gz").getAbsolutePath());
        accruedArchivePackage.setMd5sum("70d295115295a4718593f6a39d77add9");
        accruedArchivePackage.setName("accrued");
        accruedArchivePackage.setVersion("1.2");
        accruedArchivePackage.setActive(true);

        final RPackage accruedLatestPackage = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        accruedLatestPackage.setSource(
                new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/accrued_1_3.tar.gz").getAbsolutePath());
        accruedLatestPackage.setMd5sum("a05e4ca44438c0d9e7d713d7e3890423");
        accruedLatestPackage.setName("accrued");
        accruedLatestPackage.setVersion("1.3");
        accruedLatestPackage.setActive(true);

        final RPackage abcPackage = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        abcPackage.setSource(new File(TEST_PACKAGES_FOR_SOURCES_PATH + "/abc_1.3.tar.gz").getAbsolutePath());
        abcPackage.setMd5sum("c47d18b86b331a5023dcd62b74fedbb6");
        abcPackage.setName("abc");
        abcPackage.setVersion("1.3");
        abcPackage.setActive(true);

        final RPackage openSpecyArchivePackage = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        openSpecyArchivePackage.setSource(
                new File(TEST_PACKAGES_FOR_BINARIES_CENTOS7_4_2_ARCHIVE_PATH + "/OpenSpecy_1.0.99.tar.gz")
                        .getAbsolutePath());
        openSpecyArchivePackage.setName("OpenSpecy");
        openSpecyArchivePackage.setVersion("1.0.99");
        openSpecyArchivePackage.setActive(true);
        openSpecyArchivePackage.setMd5sum("2333d8335e081ac4607495fe5e840dde");
        openSpecyArchivePackage.setRVersion("4.2.1");
        openSpecyArchivePackage.setBuilt("R 4.2.1; ; 2024-06-13 23:29:42 UTC; unix");

        final RPackage openSpecyLatest = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        openSpecyLatest.setSource(
                new File(TEST_PACKAGES_FOR_BINARIES_CENTOS7_4_5_LATEST_PATH + "/OpenSpecy_1.1.0.tar.gz")
                        .getAbsolutePath());
        openSpecyLatest.setName("OpenSpecy");
        openSpecyLatest.setVersion("1.1.0");
        openSpecyLatest.setActive(true);
        openSpecyLatest.setMd5sum("13bda5374451f899771b8388983fe334");
        openSpecyLatest.setRVersion("4.5");
        openSpecyLatest.setBuilt("R 4.5.0; ; 2024-06-13 23:29:42 UTC; unix");

        LinkedHashSet<RPackage> sourcePackages = new LinkedHashSet<>();
        sourcePackages.add(abcPackage);
        sourcePackages.add(accruedLatestPackage);
        sourcePackages.add(accruedArchivePackage);

        LinkedHashSet<RPackage> binaryPackages = new LinkedHashSet<>();
        binaryPackages.add(openSpecyLatest);
        binaryPackages.add(openSpecyArchivePackage);

        Set<RPackage> latestSourceExpected = new HashSet<>();
        latestSourceExpected.add(abcPackage);
        latestSourceExpected.add(accruedLatestPackage);

        Set<RPackage> latestBinaryExpected = new HashSet<>();
        latestBinaryExpected.add(openSpecyLatest);

        Set<RPackage> latestSourcePackageSet = packageService.filterLatest(sourcePackages);
        Set<RPackage> latestBinaryPackageSet = packageService.filterLatest(binaryPackages);

        assertEquals(latestSourceExpected, latestSourcePackageSet, "Incorrect set of latest source packages");
        assertEquals(latestBinaryExpected, latestBinaryPackageSet, "Incorrect set of latest binary packages");
    }

    @Test
    public void filterLatestPackages_sameBinariesDifferentParameters() {
        final User user = UserTestFixture.GET_REGULAR_USER();

        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(2);
        repository.setPublished(true);
        repository.setVersion(5);

        final RPackage openSpecyArchivePackage = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        openSpecyArchivePackage.setSource(
                new File(TEST_PACKAGES_FOR_BINARIES_CENTOS7_4_2_ARCHIVE_PATH + "/OpenSpecy_1.0.99.tar.gz")
                        .getAbsolutePath());
        openSpecyArchivePackage.setName("OpenSpecy");
        openSpecyArchivePackage.setVersion("1.0.99");
        openSpecyArchivePackage.setActive(true);
        openSpecyArchivePackage.setMd5sum("2333d8335e081ac4607495fe5e840dde");
        openSpecyArchivePackage.setRVersion("4.2.1");
        openSpecyArchivePackage.setBuilt("R 4.2.1; ; 2024-06-13 23:29:42 UTC; unix");

        final RPackage openSpecyLatest = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        openSpecyLatest.setSource(
                new File(TEST_PACKAGES_FOR_BINARIES_CENTOS7_4_5_LATEST_PATH + "/OpenSpecy_1.1.0.tar.gz")
                        .getAbsolutePath());
        openSpecyLatest.setName("OpenSpecy");
        openSpecyLatest.setVersion("1.1.0");
        openSpecyLatest.setActive(true);
        openSpecyLatest.setMd5sum("13bda5374451f899771b8388983fe334");
        openSpecyLatest.setRVersion("4.5");
        openSpecyLatest.setBuilt("R 4.5.0; ; 2024-06-13 23:29:42 UTC; unix");

        final RPackage openSpecyLatestCentos8 = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        openSpecyLatestCentos8.setSource(
                new File(TEST_PACKAGES_FOR_BINARIES_CENTOS7_4_5_LATEST_PATH + "/OpenSpecy_1.1.0.tar.gz")
                        .getAbsolutePath());
        openSpecyLatestCentos8.setName("OpenSpecy");
        openSpecyLatestCentos8.setVersion("1.1.0");
        openSpecyLatestCentos8.setActive(true);
        openSpecyLatestCentos8.setMd5sum("13bda5374451f899771b8388983fe334");
        openSpecyLatestCentos8.setRVersion("4.5");
        openSpecyLatestCentos8.setDistribution("centos8");
        openSpecyLatestCentos8.setBuilt("R 4.5.0; ; 2024-06-13 23:29:42 UTC; unix");

        LinkedHashSet<RPackage> binaryPackages = new LinkedHashSet<>();
        binaryPackages.add(openSpecyLatest);
        binaryPackages.add(openSpecyLatestCentos8);
        binaryPackages.add(openSpecyArchivePackage);

        Set<RPackage> latestBinaryExpected = new HashSet<>();
        latestBinaryExpected.add(openSpecyLatest);
        latestBinaryExpected.add(openSpecyLatestCentos8);

        Set<RPackage> latestBinaryPackageSet = packageService.filterLatest(binaryPackages);

        assertEquals(latestBinaryExpected, latestBinaryPackageSet, "Incorrect set of latest binary packages");
    }

    @Test
    public void filterLatestPackages_manyBinaries() {
        final User user = UserTestFixture.GET_REGULAR_USER();

        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(2);
        repository.setPublished(true);
        repository.setVersion(5);

        final RPackage openSpecyArchivePackage = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        openSpecyArchivePackage.setSource(
                new File(TEST_PACKAGES_FOR_BINARIES_CENTOS7_4_2_ARCHIVE_PATH + "/OpenSpecy_1.0.99.tar.gz")
                        .getAbsolutePath());
        openSpecyArchivePackage.setName("OpenSpecy");
        openSpecyArchivePackage.setVersion("1.0.99");
        openSpecyArchivePackage.setActive(true);
        openSpecyArchivePackage.setMd5sum("2333d8335e081ac4607495fe5e840dde");
        openSpecyArchivePackage.setRVersion("4.2.1");
        openSpecyArchivePackage.setBuilt("R 4.2.1; ; 2024-06-13 23:29:42 UTC; unix");

        final RPackage openSpecyLatest = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        openSpecyLatest.setSource(
                new File(TEST_PACKAGES_FOR_BINARIES_CENTOS7_4_5_LATEST_PATH + "/OpenSpecy_1.1.0.tar.gz")
                        .getAbsolutePath());
        openSpecyLatest.setName("OpenSpecy");
        openSpecyLatest.setVersion("1.1.0");
        openSpecyLatest.setActive(true);
        openSpecyLatest.setMd5sum("13bda5374451f899771b8388983fe334");
        openSpecyLatest.setRVersion("4.5");
        openSpecyLatest.setBuilt("R 4.5.0; ; 2024-06-13 23:29:42 UTC; unix");

        final RPackage openSpecyLatestCentos8 = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        openSpecyLatestCentos8.setSource(
                new File(TEST_PACKAGES_FOR_BINARIES_CENTOS7_4_5_LATEST_PATH + "/OpenSpecy_1.1.0.tar.gz")
                        .getAbsolutePath());
        openSpecyLatestCentos8.setName("OpenSpecy");
        openSpecyLatestCentos8.setVersion("1.1.0");
        openSpecyLatestCentos8.setActive(true);
        openSpecyLatestCentos8.setMd5sum("13bda5374451f899771b8388983fe334");
        openSpecyLatestCentos8.setRVersion("4.5");
        openSpecyLatestCentos8.setDistribution("centos8");
        openSpecyLatestCentos8.setBuilt("R 4.5.0; ; 2024-06-13 23:29:42 UTC; unix");

        final RPackage arrowCentos7 = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        arrowCentos7.setSource(new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/arrow_8.0.0.tar.gz").getAbsolutePath());
        arrowCentos7.setName("arrow");
        arrowCentos7.setVersion("8.0.0");
        arrowCentos7.setActive(true);
        arrowCentos7.setMd5sum("b55eb6a2f5adeff68f1ef15fd35b03de");
        arrowCentos7.setRVersion("4.2.0");
        arrowCentos7.setBuilt("R 4.2.0; x86_64-pc-linux-gnu; 2022-06-07 00:49:30 UTC; unix");

        final RPackage arrowCentos8 = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        arrowCentos8.setSource(new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/arrow_8.0.0.tar.gz").getAbsolutePath());
        arrowCentos8.setName("arrow");
        arrowCentos8.setVersion("8.0.0");
        arrowCentos8.setActive(true);
        arrowCentos8.setMd5sum("b55eb6a2f5adeff68f1ef15fd35b03de");
        arrowCentos8.setRVersion("4.2.0");
        arrowCentos8.setDistribution("centos8");
        arrowCentos8.setBuilt("R 4.2.0; x86_64-pc-linux-gnu; 2022-06-07 00:49:30 UTC; unix");

        final RPackage arrowArchive1 = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        arrowArchive1.setSource(new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/arrow_8.0.0.tar.gz").getAbsolutePath());
        arrowArchive1.setName("arrow");
        arrowArchive1.setVersion("7.0.0");
        arrowArchive1.setActive(true);
        arrowArchive1.setMd5sum("b55eb6a2f5adeff68f1ef15fd35b03de");
        arrowArchive1.setRVersion("4.0.5");
        arrowArchive1.setBuilt("R 4.0.5; x86_64-pc-linux-gnu; 2022-06-07 00:49:30 UTC; unix");

        final RPackage arrowArchive2 = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        arrowArchive2.setSource(new File(TEST_PACKAGES_FOR_BINARIES_PATH + "/arrow_8.0.0.tar.gz").getAbsolutePath());
        arrowArchive2.setName("arrow");
        arrowArchive2.setVersion("7.0.0");
        arrowArchive2.setActive(true);
        arrowArchive2.setMd5sum("b55eb6a2f5adeff68f1ef15fd35b03de");
        arrowArchive2.setRVersion("4.1");
        arrowArchive2.setArchitecture("x86");
        arrowArchive2.setBuilt("R 4.1; x86_64-pc-linux-gnu; 2022-06-07 00:49:30 UTC; unix");

        LinkedHashSet<RPackage> binaryPackages = new LinkedHashSet<>();
        binaryPackages.add(openSpecyLatest);
        binaryPackages.add(openSpecyLatestCentos8);
        binaryPackages.add(openSpecyArchivePackage);
        binaryPackages.add(arrowCentos7);
        binaryPackages.add(arrowCentos8);
        binaryPackages.add(arrowArchive1);
        binaryPackages.add(arrowArchive2);

        Set<RPackage> latestBinaryExpected = new HashSet<>();
        latestBinaryExpected.add(openSpecyLatest);
        latestBinaryExpected.add(openSpecyLatestCentos8);
        latestBinaryExpected.add(arrowCentos7);
        latestBinaryExpected.add(arrowCentos8);

        Set<RPackage> latestBinaryPackageSet = packageService.filterLatest(binaryPackages);

        assertEquals(latestBinaryExpected, latestBinaryPackageSet, "Incorrect set of latest binary packages");
    }
}

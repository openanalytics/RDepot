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
package eu.openanalytics.rdepot.r.test.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import eu.openanalytics.rdepot.base.email.EmailService;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.mediator.BestMaintainerChooser;
import eu.openanalytics.rdepot.base.security.authorization.SecurityMediator;
import eu.openanalytics.rdepot.base.service.NewsfeedEventService;
import eu.openanalytics.rdepot.base.service.SubmissionService;
import eu.openanalytics.rdepot.base.storage.implementations.CommonLocalStorage;
import eu.openanalytics.rdepot.r.api.v2.dtos.RPackageUploadRequest;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import eu.openanalytics.rdepot.r.mediator.deletion.RPackageDeleter;
import eu.openanalytics.rdepot.r.services.RPackageService;
import eu.openanalytics.rdepot.r.services.RRepositoryService;
import eu.openanalytics.rdepot.r.storage.implementations.RLocalStorage;
import eu.openanalytics.rdepot.r.storage.utils.PopulatedRepositoryContent;
import eu.openanalytics.rdepot.r.strategy.upload.RPackageUploadStrategy;
import eu.openanalytics.rdepot.r.synchronization.RRepositorySynchronizer;
import eu.openanalytics.rdepot.r.synchronization.SynchronizeRepositoryRequestBody;
import eu.openanalytics.rdepot.r.validation.RPackageValidator;
import eu.openanalytics.rdepot.test.fixture.RPackageTestFixture;
import eu.openanalytics.rdepot.test.fixture.RRepositoryTestFixture;
import eu.openanalytics.rdepot.test.fixture.UserTestFixture;
import eu.openanalytics.rdepot.test.unit.UnitTest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import org.apache.http.entity.ContentType;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

public class RLocalStorageTest extends UnitTest {

    private static final File testResourcesHome = new File("src/test/resources/unit/storage_tests");
    private static final File packageUploadDirectory = new File("src/test/resources/unit/storage_tests/rdepot");
    private static final File repositoryGenerationDirectory =
            new File("src/test/resources/unit/storage_tests/rdepot/generated");

    private final RLocalStorage storage = new RLocalStorage();

    @BeforeEach
    public void setUpDirectories() throws Exception {
        restore();
        ReflectionTestUtils.setField(
                storage, CommonLocalStorage.class, "packageUploadDirectory", packageUploadDirectory, File.class);
        ReflectionTestUtils.setField(
                storage,
                CommonLocalStorage.class,
                "repositoryGenerationDirectory",
                repositoryGenerationDirectory,
                File.class);
        ReflectionTestUtils.setField(
                storage, RLocalStorage.class, "packageUploadDirectory", packageUploadDirectory, File.class);
        ReflectionTestUtils.setField(
                storage,
                RLocalStorage.class,
                "repositoryGenerationDirectory",
                repositoryGenerationDirectory,
                File.class);
        ReflectionTestUtils.setField(storage, RLocalStorage.class, "snapshot", "true", String.class);
    }

    @AfterEach
    public void cleanUp() throws Exception {
        deleteTestFilesIfExist();
    }

    private void restore() throws Exception {
        deleteTestFilesIfExist();
        executeBashCommand(
                "tar",
                "-xvzf",
                testResourcesHome.getAbsolutePath() + "/rdepot.tar.gz",
                "-C",
                testResourcesHome.getAbsolutePath());
    }

    private void deleteTestFilesIfExist() throws Exception {
        if (packageUploadDirectory.exists()) {
            FileUtils.forceDelete(packageUploadDirectory);
        }
    }

    @Test
    public void buildSynchronizeRequestBody() throws Exception {
        final String datestamp = "20240111";
        final User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(2);
        repository.setPublished(true);
        repository.setVersion(5);

        final RPackage benchmarkingArchivePackage = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        benchmarkingArchivePackage.setSource(
                new File(packageUploadDirectory + "/repositories/2/71228208/Benchmarking_0.10.tar.gz")
                        .getAbsolutePath());
        benchmarkingArchivePackage.setMd5sum("9a99c2ebefa6d49422ca7893c1f4ead8");
        benchmarkingArchivePackage.setName("Benchmarking");
        benchmarkingArchivePackage.setVersion("0.10");
        benchmarkingArchivePackage.setActive(true);

        final RPackage benchmarkingPackage = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        benchmarkingPackage.setSource(
                new File(packageUploadDirectory + "/repositories/2/79878978/Benchmarking_0.31.tar.gz")
                        .getAbsolutePath());
        benchmarkingPackage.setName("Benchmarking");
        benchmarkingPackage.setVersion("0.31");
        benchmarkingPackage.setActive(true);
        benchmarkingPackage.setMd5sum("136460e58c711ed9cc1cdbdbde2e5b86");

        final RPackage beaR = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        beaR.setSource(
                new File(packageUploadDirectory + "/repositories/2/89565416/bea.R_1.0.5.tar.gz").getAbsolutePath());
        beaR.setName("bea.R");
        beaR.setVersion("1.0.5");
        beaR.setActive(true);
        beaR.setMd5sum("5e664f320c7cc884138d64467f6b0e49");

        final RPackage openSpecyLatest = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        openSpecyLatest.setSource(
                new File(packageUploadDirectory + "/repositories/2/9420570/OpenSpecy_1.1.0.tar.gz").getAbsolutePath());
        openSpecyLatest.setName("OpenSpecy");
        openSpecyLatest.setVersion("1.1.0");
        openSpecyLatest.setActive(true);
        openSpecyLatest.setMd5sum("13bda5374451f899771b8388983fe334");
        openSpecyLatest.setRVersion("4.5");
        openSpecyLatest.setBuilt("R 4.5.0; ; 2024-06-13 23:29:42 UTC; unix");

        final RPackage openSpecyArchive = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        openSpecyArchive.setSource(
                new File(packageUploadDirectory + "/repositories/2/6918282/OpenSpecy_1.0.99.tar.gz").getAbsolutePath());
        openSpecyArchive.setName("OpenSpecy");
        openSpecyArchive.setVersion("1.0.99");
        openSpecyArchive.setActive(true);
        openSpecyArchive.setMd5sum("2333d8335e081ac4607495fe5e840dde");
        openSpecyArchive.setRVersion("4.2.1");
        openSpecyArchive.setBuilt("R 4.2.1; ; 2024-06-13 23:29:42 UTC; unix");

        final RPackage arrow = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        arrow.setSource(
                new File(packageUploadDirectory + "/repositories/2/9955549/arrow_8.0.0.tar.gz").getAbsolutePath());
        arrow.setName("arrow");
        arrow.setVersion("8.0.0");
        arrow.setActive(true);
        arrow.setMd5sum("b55eb6a2f5adeff68f1ef15fd35b03de");
        arrow.setRVersion("4.2.0");
        arrow.setBuilt("R 4.2.0; x86_64-pc-linux-gnu; 2022-06-07 00:49:30 UTC; unix");

        List<RPackage> sourcePackages = List.of(benchmarkingArchivePackage, benchmarkingPackage, beaR);
        List<RPackage> archiveSourcePackages = List.of(benchmarkingArchivePackage);
        List<RPackage> latestSourcePackages = List.of(benchmarkingPackage, beaR);

        List<RPackage> binaryPackages = List.of(openSpecyLatest, openSpecyArchive, arrow);
        List<RPackage> archiveBinaryPackages = List.of(openSpecyArchive);
        List<RPackage> latestBinaryPackages = List.of(openSpecyLatest, arrow);

        final PopulatedRepositoryContent content = storage.organizePackagesInStorage(
                datestamp,
                sourcePackages,
                latestSourcePackages,
                archiveSourcePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                repository);

        List<String> remoteLatestSourcePackages = List.of("Benchmarking_0.10.tar.gz");
        List<String> remoteArchiveSourcePackages = List.of();

        MultiValueMap<String, String> remoteLatestBinaryPackages = new LinkedMultiValueMap<>();
        MultiValueMap<String, String> remoteArchiveBinaryPackages = new LinkedMultiValueMap<>();

        remoteLatestBinaryPackages.add("bin/linux/centos7/x86_64/4.2", "OpenSpecy_1.0.99.tar.gz");

        int versionBefore = 4;

        final SynchronizeRepositoryRequestBody requestBody = storage.buildSynchronizeRequestBody(
                content,
                remoteLatestSourcePackages,
                remoteArchiveSourcePackages,
                remoteLatestBinaryPackages,
                remoteArchiveBinaryPackages,
                repository,
                versionBefore + "");

        final File latestSourcePackagesFile =
                new File(repositoryGenerationDirectory + "/2/current/src/contrib/latest/PACKAGES");
        final File archiveSourcePackagesFile =
                new File(repositoryGenerationDirectory + "/2/current/src/contrib/Archive/PACKAGES");
        final File latestBinaryPackagesFileHigherRVersion =
                new File(repositoryGenerationDirectory + "/2/current/bin/linux/centos7/x86_64/4.5/latest/PACKAGES");
        final File latestBinaryPackagesFileLowerRVersion =
                new File(repositoryGenerationDirectory + "/2/current/bin/linux/centos7/x86_64/4.2/latest/PACKAGES");
        final File archiveBinaryPackagesFileLowerRVersion =
                new File(repositoryGenerationDirectory + "/2/current/bin/linux/centos7/x86_64/4.2/Archive/PACKAGES");

        final File sourcePackageToUpload1 =
                new File(packageUploadDirectory + "/repositories/2/79878978/Benchmarking_0.31.tar.gz");
        final File sourcePackageToUpload2 =
                new File(packageUploadDirectory + "/repositories/2/89565416/bea.R_1.0.5.tar.gz");
        final File sourcePackageToUploadToArchive =
                new File(packageUploadDirectory + "/repositories/2/71228208/Benchmarking_0.10.tar.gz");

        final File binaryPackageToUpload1 =
                new File(packageUploadDirectory + "/repositories/2/9420570/OpenSpecy_1.1.0.tar.gz");
        final File binaryPackageToUpload2 =
                new File(packageUploadDirectory + "/repositories/2/9955549/arrow_8.0.0.tar.gz");
        final File binaryPackageToUploadToArchive =
                new File(packageUploadDirectory + "/repositories/2/6918282/OpenSpecy_1.0.99.tar.gz");

        assertTrue(
                requestBody.getSourcePackagesToDeleteFromArchive().isEmpty(),
                "There should be no source packages to delete from Archive.");
        assertTrue(
                requestBody.getBinaryPackagesToDeleteFromArchive().isEmpty(),
                "There should be no binary packages to delete from Archive.");
        assertEquals(1, requestBody.getSourcePackagesToDelete().size(), "There should one source package to delete.");
        assertEquals(1, requestBody.getBinaryPackagesToDelete().size(), "There should one binary package to delete.");
        assertEquals(2, requestBody.getSourcePackagesToUpload().size(), "Too many source packages to upload.");
        assertEquals(2, requestBody.getBinaryPackagesToUpload().size(), "Too many binary packages to upload.");
        assertEquals(
                1,
                requestBody
                        .getBinaryPackagesToUpload()
                        .get("bin/linux/centos7/x86_64/4.5")
                        .size(),
                "Too many binary packages to upload for R version 4.5.");
        assertEquals(
                1,
                requestBody
                        .getBinaryPackagesToUpload()
                        .get("bin/linux/centos7/x86_64/4.2")
                        .size(),
                "Too many binary packages to upload for R version 4.2.");
        assertEquals(
                1,
                requestBody.getSourcePackagesToUploadToArchive().size(),
                "Too many source packages to upload to archive.");
        assertEquals(
                1,
                requestBody
                        .getBinaryPackagesToUploadToArchive()
                        .get("bin/linux/centos7/x86_64/4.2")
                        .size(),
                "Too many binary packages to upload to archive.");
        assertTrue(
                containsFile(sourcePackageToUpload1, requestBody.getSourcePackagesToUpload()),
                "There is a missing source package to upload.");
        assertTrue(
                containsFile(
                        binaryPackageToUpload1,
                        requestBody.getBinaryPackagesToUpload().get("bin/linux/centos7/x86_64/4.5")),
                "There is a missing binary package to upload.");
        assertTrue(
                containsFile(sourcePackageToUpload2, requestBody.getSourcePackagesToUpload()),
                "There is a missing source package to upload.");
        assertTrue(
                containsFile(
                        binaryPackageToUpload2,
                        requestBody.getBinaryPackagesToUpload().get("bin/linux/centos7/x86_64/4.2")),
                "There is a missing binary package to upload.");
        assertTrue(
                containsFile(sourcePackageToUploadToArchive, requestBody.getSourcePackagesToUploadToArchive()),
                "There is a missing source package to upload to Archive.");
        assertTrue(
                containsFile(
                        binaryPackageToUploadToArchive,
                        requestBody.getBinaryPackagesToUploadToArchive().get("bin/linux/centos7/x86_64/4.2")),
                "There is a missing binary package to upload to Archive.");
        assertEquals(
                latestSourcePackagesFile.getAbsolutePath(),
                requestBody.getPackagesFiles().get("src/contrib").getAbsolutePath(),
                "Incorrect source latest PACKAGES file in the request body");
        assertEquals(
                latestBinaryPackagesFileHigherRVersion.getAbsolutePath(),
                requestBody
                        .getPackagesFiles()
                        .get("bin/linux/centos7/x86_64/4.5")
                        .getAbsolutePath(),
                "Incorrect binary latest PACKAGES file in the request body");
        assertEquals(
                latestBinaryPackagesFileLowerRVersion.getAbsolutePath(),
                requestBody
                        .getPackagesFiles()
                        .get("bin/linux/centos7/x86_64/4.2")
                        .getAbsolutePath(),
                "Incorrect binary latest PACKAGES file in the request body");
        assertEquals(
                archiveSourcePackagesFile.getAbsolutePath(),
                requestBody.getPackagesFilesForArchive().get("src/contrib").getAbsolutePath(),
                "Incorrect source archive PACKAGES file in the request body");
        assertEquals(
                archiveBinaryPackagesFileLowerRVersion.getAbsolutePath(),
                requestBody
                        .getPackagesFilesForArchive()
                        .get("bin/linux/centos7/x86_64/4.2")
                        .getAbsolutePath(),
                "Incorrect binary archive PACKAGES file in the request body");
        assertTrue(
                requestBody.getSourcePackagesToDelete().contains("Benchmarking_0.10.tar.gz"),
                "Old source package should be selected for deletion.");
        assertTrue(
                requestBody
                        .getBinaryPackagesToDelete()
                        .get("bin/linux/centos7/x86_64/4.2")
                        .contains("OpenSpecy_1.0.99.tar.gz"),
                "Old binary package should be selected for deletion.");
        assertTrue(
                requestBody.getSourcePackagesToDeleteFromArchive().isEmpty(),
                "No source packages should be selected for deletion from Archive.");
        assertTrue(
                requestBody.getBinaryPackagesToDeleteFromArchive().isEmpty(),
                "No binary source packages should be selected for deletion from Archive.");
    }

    private boolean containsFile(File file, List<File> files) {
        for (File f : files) {
            if (f.getAbsolutePath().equals(file.getAbsolutePath())) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void generateInstallablePackagesFile() throws Exception {
        final String beaRName = "bea.R";
        final String beaRLocation = "/repositories/2/89565416/";
        final File beaRFolder = new File(packageUploadDirectory + beaRLocation + beaRName);
        final Properties packageProperties = storage.getPropertiesFromExtractedFile(beaRFolder.getAbsolutePath());
        final String beaRFileName = beaRName + "_1.0.5.tar.gz";
        final File beaRFile = new File(packageUploadDirectory + beaRLocation + beaRFileName);
        final FileInputStream fis = new FileInputStream(beaRFile);
        final byte[] packageBytes = fis.readAllBytes();
        fis.close();
        MultipartFile multipartFile = new MockMultipartFile(beaRFileName, beaRFileName, "", packageBytes);

        RPackageUploadRequest request = new RPackageUploadRequest(
                multipartFile,
                RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY(),
                false,
                true,
                false,
                null,
                null,
                null,
                null);
        final RPackageService rPackageService = mock(RPackageService.class);
        doAnswer(invocation -> invocation.getArgument(0)).when(rPackageService).create(any());
        final RPackageUploadStrategy rPackageUploadStrategy = new RPackageUploadStrategy(
                request,
                UserTestFixture.GET_PACKAGE_MAINTAINER(),
                mock(NewsfeedEventService.class),
                mock(SubmissionService.class),
                mock(RPackageValidator.class),
                mock(RRepositoryService.class),
                storage,
                rPackageService,
                mock(EmailService.class),
                mock(BestMaintainerChooser.class),
                mock(RRepositorySynchronizer.class),
                mock(SecurityMediator.class),
                storage,
                mock(RPackageDeleter.class),
                request);

        final RPackage beaRPackage = ReflectionTestUtils.invokeMethod(
                rPackageUploadStrategy, "createPackage", beaRName, beaRFile, packageProperties);
        final String packageString = ReflectionTestUtils.invokeMethod(storage, "generatePackageString", beaRPackage);

        final String arrowName = "arrow";
        final String arrowLocation = "/repositories/2/9955549/";
        final File arrowFolder = new File(packageUploadDirectory + arrowLocation + arrowName);
        final Properties arrowProperties = storage.getPropertiesFromExtractedFile(arrowFolder.getAbsolutePath());
        final String arrowFileName = arrowName + "_8.0.0.tar.gz";
        final File arrowFile = new File(packageUploadDirectory + arrowLocation + arrowFileName);
        final FileInputStream bfis = new FileInputStream(arrowFile);
        final byte[] binaryPackageBytes = bfis.readAllBytes();
        bfis.close();
        MultipartFile multipartBinaryFile = new MockMultipartFile(arrowFileName, arrowFileName, "", binaryPackageBytes);
        RPackageUploadRequest binaryRequest = new RPackageUploadRequest(
                multipartBinaryFile,
                RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY(),
                false,
                true,
                true,
                null,
                null,
                null,
                null);
        doAnswer(invocation -> invocation.getArgument(0)).when(rPackageService).create(any());
        final RPackageUploadStrategy rBinaryPackageUploadStrategy = new RPackageUploadStrategy(
                binaryRequest,
                UserTestFixture.GET_PACKAGE_MAINTAINER(),
                mock(NewsfeedEventService.class),
                mock(SubmissionService.class),
                mock(RPackageValidator.class),
                mock(RRepositoryService.class),
                storage,
                rPackageService,
                mock(EmailService.class),
                mock(BestMaintainerChooser.class),
                mock(RRepositorySynchronizer.class),
                mock(SecurityMediator.class),
                storage,
                mock(RPackageDeleter.class),
                binaryRequest);
        final RPackage arrowPackage = ReflectionTestUtils.invokeMethod(
                rBinaryPackageUploadStrategy, "createPackage", arrowName, arrowFile, arrowProperties);
        final String binaryPackageString =
                ReflectionTestUtils.invokeMethod(storage, "generatePackageString", arrowPackage);

        assertEquals(
                """
                Package: bea.R
                Version: 1.0.5
                Depends: R (>= 3.2.1), data.table
                Imports: httr, DT, shiny, jsonlite, googleVis, shinydashboard, ggplot2,
                 stringr, chron, gtable, scales, htmltools, httpuv, xtable,
                 stringi, magrittr, htmlwidgets, Rcpp, munsell, colorspace,
                 plyr, yaml
                License: CC0
                MD5Sum: 5e664f320c7cc884138d64467f6b0e49
                NeedsCompilation: no

                """,
                packageString);

        assertEquals(
                """
                Package: arrow
                Version: 8.0.0
                Depends: R (>= 3.4)
                Imports: assertthat, bit64 (>= 0.9-7), methods, purrr, R6, rlang,
                 stats, tidyselect (>= 1.0.0), utils, vctrs
                Suggests: DBI, dbplyr, decor, distro, dplyr, duckdb (>= 0.2.8), hms,
                 knitr, lubridate, pkgload, reticulate, rmarkdown, stringi,
                 stringr, testthat (>= 3.1.0), tibble, tzdb, withr
                License: Apache License (>= 2.0)
                LinkingTo: cpp11 (>= 0.4.2)
                MD5Sum: b55eb6a2f5adeff68f1ef15fd35b03de
                NeedsCompilation: yes
                Built: R 4.2.0; x86_64-pc-linux-gnu; 2022-06-07 00:49:30 UTC; unix

                """,
                binaryPackageString);
    }

    @Test
    public void organizePackagesInStorage() throws Exception {
        final String datestamp = "20240110";
        final User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(2);
        repository.setPublished(true);

        final RPackage benchmarkingArchivePackage = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        benchmarkingArchivePackage.setSource(
                new File(packageUploadDirectory + "/repositories/2/71228208/Benchmarking_0.10.tar.gz")
                        .getAbsolutePath());
        benchmarkingArchivePackage.setMd5sum("9a99c2ebefa6d49422ca7893c1f4ead8");
        benchmarkingArchivePackage.setName("Benchmarking");
        benchmarkingArchivePackage.setVersion("0.10");
        benchmarkingArchivePackage.setActive(true);

        final RPackage benchmarkingPackage = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        benchmarkingPackage.setSource(
                new File(packageUploadDirectory + "/repositories/2/79878978/Benchmarking_0.31.tar.gz")
                        .getAbsolutePath());
        benchmarkingPackage.setName("Benchmarking");
        benchmarkingPackage.setVersion("0.31");
        benchmarkingPackage.setActive(true);
        benchmarkingPackage.setMd5sum("136460e58c711ed9cc1cdbdbde2e5b86");

        final RPackage beaR = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        beaR.setSource(
                new File(packageUploadDirectory + "/repositories/2/89565416/bea.R_1.0.5.tar.gz").getAbsolutePath());
        beaR.setName("bea.R");
        beaR.setVersion("1.0.5");
        beaR.setActive(true);
        beaR.setMd5sum("5e664f320c7cc884138d64467f6b0e49");

        final RPackage openSpecyLatest = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        openSpecyLatest.setSource(
                new File(packageUploadDirectory + "/repositories/2/9420570/OpenSpecy_1.1.0.tar.gz").getAbsolutePath());
        openSpecyLatest.setName("OpenSpecy");
        openSpecyLatest.setVersion("1.1.0");
        openSpecyLatest.setActive(true);
        openSpecyLatest.setMd5sum("13bda5374451f899771b8388983fe334");
        openSpecyLatest.setRVersion("4.5");
        openSpecyLatest.setBuilt("R 4.5.0; ; 2024-06-13 23:29:42 UTC; unix");

        final RPackage openSpecyArchive = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        openSpecyArchive.setSource(
                new File(packageUploadDirectory + "/repositories/2/6918282/OpenSpecy_1.0.99.tar.gz").getAbsolutePath());
        openSpecyArchive.setName("OpenSpecy");
        openSpecyArchive.setVersion("1.0.99");
        openSpecyArchive.setActive(true);
        openSpecyArchive.setMd5sum("2333d8335e081ac4607495fe5e840dde");
        openSpecyArchive.setRVersion("4.2.1");
        openSpecyArchive.setBuilt("R 4.2.1; ; 2024-06-13 23:29:42 UTC; unix");

        final RPackage arrow = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        arrow.setSource(
                new File(packageUploadDirectory + "/repositories/2/9955549/arrow_8.0.0.tar.gz").getAbsolutePath());
        arrow.setName("arrow");
        arrow.setVersion("8.0.0");
        arrow.setActive(true);
        arrow.setMd5sum("b55eb6a2f5adeff68f1ef15fd35b03de");
        arrow.setRVersion("4.2.0");
        arrow.setBuilt("R 4.2.0; x86_64-pc-linux-gnu; 2022-06-07 00:49:30 UTC; unix");

        List<RPackage> sourcePackages = List.of(benchmarkingArchivePackage, benchmarkingPackage, beaR);
        List<RPackage> archiveSourcePackages = List.of(benchmarkingArchivePackage);
        List<RPackage> latestSourcePackages = List.of(benchmarkingPackage, beaR);

        List<RPackage> binaryPackages = List.of(openSpecyLatest, openSpecyArchive, arrow);
        List<RPackage> archiveBinaryPackages = List.of(openSpecyArchive);
        List<RPackage> latestBinaryPackages = List.of(openSpecyLatest, arrow);

        storage.organizePackagesInStorage(
                datestamp,
                sourcePackages,
                latestSourcePackages,
                archiveSourcePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                repository);

        final File currentDatestampGeneratedDirectory = new File(repositoryGenerationDirectory + "/2/20240110");
        final File currentGeneratedDirectory = new File(repositoryGenerationDirectory + "/2/current");

        final File actualLatestSourcePackagesFile =
                new File(repositoryGenerationDirectory + "/2/20240110/src/contrib/latest/PACKAGES");
        final File actualArchiveSourcePackagesFile =
                new File(repositoryGenerationDirectory + "/2/20240110/src/contrib/Archive/PACKAGES");
        final String actualLatestSourcePackagesFileContent = Files.readString(actualLatestSourcePackagesFile.toPath());
        final String actualArchiveSourcePackagesFileContent =
                Files.readString(actualArchiveSourcePackagesFile.toPath());

        final File actualLatestSourcePackagesGzFile =
                new File(repositoryGenerationDirectory + "/2/20240110/src/contrib/latest/PACKAGES.gz");
        final File actualArchiveSourcePackagesGzFile =
                new File(repositoryGenerationDirectory + "/2/20240110/src/contrib/Archive/PACKAGES.gz");
        final String actualLatestSourcePackagesGzFileContent = readGzFileContent(actualLatestSourcePackagesGzFile);
        final String actualArchiveSourcePackagesGzFileContent = readGzFileContent(actualArchiveSourcePackagesGzFile);

        final File actualLatestBinaryPackagesFile_HigherRVersion =
                new File(repositoryGenerationDirectory + "/2/20240110/bin/linux/centos7/x86_64/4.5/latest/PACKAGES");
        final File actualLatestBinaryPackagesFile_LowerRVersion =
                new File(repositoryGenerationDirectory + "/2/20240110/bin/linux/centos7/x86_64/4.2/latest/PACKAGES");
        final File actualArchiveBinaryPackagesFile_LowerRVersion =
                new File(repositoryGenerationDirectory + "/2/20240110/bin/linux/centos7/x86_64/4.2/Archive/PACKAGES");

        final String actualLatestBinaryPackagesFileContent_HigherRVersion =
                Files.readString(actualLatestBinaryPackagesFile_HigherRVersion.toPath());
        final String actualLatestBinaryPackagesFileContent_LowerRVersion =
                Files.readString(actualLatestBinaryPackagesFile_LowerRVersion.toPath());
        final String actualArchiveBinaryPackagesFileContent_LowerRVersion =
                Files.readString(actualArchiveBinaryPackagesFile_LowerRVersion.toPath());

        final File actualLatestBinaryPackagesGzFile_HigherRVersion =
                new File(repositoryGenerationDirectory + "/2/20240110/bin/linux/centos7/x86_64/4.5/latest/PACKAGES.gz");
        final File actualLatestBinaryPackagesGzFile_LowerRVersion =
                new File(repositoryGenerationDirectory + "/2/20240110/bin/linux/centos7/x86_64/4.2/latest/PACKAGES.gz");
        final File actualArchiveBinaryPackagesGzFile_LowerRVersion = new File(
                repositoryGenerationDirectory + "/2/20240110/bin/linux/centos7/x86_64/4.2/Archive/PACKAGES.gz");

        final String actualLatestBinaryPackagesGzFileContent_HigherRVersion =
                readGzFileContent(actualLatestBinaryPackagesGzFile_HigherRVersion);
        final String actualLatestBinaryPackagesGzFileContent_LowerRVersion =
                readGzFileContent(actualLatestBinaryPackagesGzFile_LowerRVersion);
        final String actualArchiveBinaryPackagesGzFileContent_LowerRVersion =
                readGzFileContent(actualArchiveBinaryPackagesGzFile_LowerRVersion);

        final String expectedLatestSourcePackagesFileContent = getExpectedLatestPackagesFile();
        final String expectedArchiveSourcePackagesFileContent = getExpectedArchivePackagesFile();

        final String expectedLatestBinaryPackagesFileContent_HigherRVersion =
                getExpectedBinaryLatestPackagesFileForHigherRVersion();
        final String expectedLatestBinaryPackagesFileContent_LowerRVersion =
                getExpectedBinaryLatestPackagesFileForLowerRVersion();
        final String expectedArchiveBinaryPackagesFileContent_LowerRVersion =
                getExpectedBinaryArchivePackagesFileForLowerRVersion();

        verifySourcePackage(currentGeneratedDirectory, "Benchmarking_0.10.tar.gz", true);
        verifySourcePackage(currentGeneratedDirectory, "Benchmarking_0.31.tar.gz", false);
        verifySourcePackage(currentGeneratedDirectory, "bea.R_1.0.5.tar.gz", false);

        verifyBinaryPackage(currentGeneratedDirectory, "bin/linux/centos7/x86_64/4.5", "OpenSpecy_1.1.0.tar.gz", false);
        verifyBinaryPackage(currentGeneratedDirectory, "bin/linux/centos7/x86_64/4.2", "OpenSpecy_1.0.99.tar.gz", true);
        verifyBinaryPackage(currentGeneratedDirectory, "bin/linux/centos7/x86_64/4.2", "arrow_8.0.0.tar.gz", false);

        assertEquals(
                expectedLatestSourcePackagesFileContent,
                actualLatestSourcePackagesFileContent,
                "Incorrect latest source PACKAGES file");
        assertEquals(
                expectedArchiveSourcePackagesFileContent,
                actualArchiveSourcePackagesFileContent,
                "Incorrect archive source PACKAGES file");
        assertEquals(
                expectedLatestBinaryPackagesFileContent_HigherRVersion,
                actualLatestBinaryPackagesFileContent_HigherRVersion,
                "Incorrect latest binary PACKAGES file");
        assertEquals(
                expectedLatestBinaryPackagesFileContent_LowerRVersion,
                actualLatestBinaryPackagesFileContent_LowerRVersion,
                "Incorrect latest binary PACKAGES file");
        assertEquals(
                expectedArchiveBinaryPackagesFileContent_LowerRVersion,
                actualArchiveBinaryPackagesFileContent_LowerRVersion,
                "Incorrect archive binary PACKAGES file");

        assertEquals(
                expectedLatestSourcePackagesFileContent,
                actualLatestSourcePackagesGzFileContent,
                "Incorrect latest source PACKAGES.gz file");
        assertEquals(
                expectedArchiveSourcePackagesFileContent,
                actualArchiveSourcePackagesGzFileContent,
                "Incorrect archive source PACKAGES.gz file");
        assertEquals(
                expectedLatestBinaryPackagesFileContent_HigherRVersion,
                actualLatestBinaryPackagesGzFileContent_HigherRVersion,
                "Incorrect latest binary PACKAGES.gz file");
        assertEquals(
                expectedLatestBinaryPackagesFileContent_LowerRVersion,
                actualLatestBinaryPackagesGzFileContent_LowerRVersion,
                "Incorrect latest binary PACKAGES.gz file");
        assertEquals(
                expectedArchiveBinaryPackagesFileContent_LowerRVersion,
                actualArchiveBinaryPackagesGzFileContent_LowerRVersion,
                "Incorrect archive binary PACKAGES.gz file");

        assertTrue(currentDatestampGeneratedDirectory.isDirectory(), "Directory was not generated.");
        assertTrue(Files.isSymbolicLink(currentGeneratedDirectory.toPath()), "current should be a symlink");
        assertEquals(
                currentDatestampGeneratedDirectory.getAbsolutePath(),
                currentGeneratedDirectory.toPath().toRealPath().toFile().getAbsolutePath(),
                "Symlink does not point at a right dir.");
    }

    @Test
    public void organizePackagesInStorage_withRedirectToSourceSetToTrue() throws Exception {
        final String datestamp = "20240110";
        final User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(2);
        repository.setPublished(true);
        repository.setRedirectToSource(true);

        final RPackage benchmarkingArchivePackage = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        benchmarkingArchivePackage.setSource(
                new File(packageUploadDirectory + "/repositories/2/71228208/Benchmarking_0.10.tar.gz")
                        .getAbsolutePath());
        benchmarkingArchivePackage.setMd5sum("9a99c2ebefa6d49422ca7893c1f4ead8");
        benchmarkingArchivePackage.setName("Benchmarking");
        benchmarkingArchivePackage.setVersion("0.10");
        benchmarkingArchivePackage.setActive(true);

        final RPackage benchmarkingPackage = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        benchmarkingPackage.setSource(
                new File(packageUploadDirectory + "/repositories/2/79878978/Benchmarking_0.31.tar.gz")
                        .getAbsolutePath());
        benchmarkingPackage.setName("Benchmarking");
        benchmarkingPackage.setVersion("0.31");
        benchmarkingPackage.setActive(true);
        benchmarkingPackage.setMd5sum("136460e58c711ed9cc1cdbdbde2e5b86");

        final RPackage arrowSourceVersion = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        arrowSourceVersion.setSource(
                new File(packageUploadDirectory + "/repositories/2/6897574/arrow_18.1.0.1.tar.gz").getAbsolutePath());
        arrowSourceVersion.setName("arrow");
        arrowSourceVersion.setVersion("18.1.0.1");
        arrowSourceVersion.setActive(true);
        arrowSourceVersion.setMd5sum("4fc2b28d05af4d6e458494c385082699");

        final RPackage accruedLatest = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        accruedLatest.setSource(
                new File(packageUploadDirectory + "/repositories/2/3094731/accrued_1.4.tar.gz").getAbsolutePath());
        accruedLatest.setName("accrued");
        accruedLatest.setVersion("1.4");
        accruedLatest.setActive(true);
        accruedLatest.setMd5sum("97c2930a9dd7ca9fc1409d5340c06470");

        final RPackage accruedArchive1 = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        accruedArchive1.setSource(
                new File(packageUploadDirectory + "/repositories/2/4185687/accrued_1.2.tar.gz").getAbsolutePath());
        accruedArchive1.setName("accrued");
        accruedArchive1.setVersion("1.2");
        accruedArchive1.setActive(true);
        accruedArchive1.setMd5sum("70d295115295a4718593f6a39d77add9");

        final RPackage accruedArchive2 = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        accruedArchive2.setSource(
                new File(packageUploadDirectory + "/repositories/2/3328424/accrued_1.3.tar.gz").getAbsolutePath());
        accruedArchive2.setName("accrued");
        accruedArchive2.setVersion("1.3");
        accruedArchive2.setActive(true);
        accruedArchive2.setMd5sum("a05e4ca44438c0d9e7d713d7e3890423");

        final RPackage openSpecySourceVersion = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        openSpecySourceVersion.setSource(
                new File(packageUploadDirectory + "/repositories/2/3987555/OpenSpecy_1.1.0.tar.gz").getAbsolutePath());
        openSpecySourceVersion.setName("OpenSpecy");
        openSpecySourceVersion.setVersion("1.1.0");
        openSpecySourceVersion.setActive(true);
        openSpecySourceVersion.setMd5sum("0da320752536e9659fd2f8e4e99ecb7f");

        final RPackage openSpecyLatest = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        openSpecyLatest.setSource(
                new File(packageUploadDirectory + "/repositories/2/9420570/OpenSpecy_1.1.0.tar.gz").getAbsolutePath());
        openSpecyLatest.setName("OpenSpecy");
        openSpecyLatest.setVersion("1.1.0");
        openSpecyLatest.setActive(true);
        openSpecyLatest.setMd5sum("13bda5374451f899771b8388983fe334");
        openSpecyLatest.setRVersion("4.5");
        openSpecyLatest.setBuilt("R 4.5.0; ; 2024-06-13 23:29:42 UTC; unix");

        final RPackage openSpecyArchive = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        openSpecyArchive.setSource(
                new File(packageUploadDirectory + "/repositories/2/6918282/OpenSpecy_1.0.99.tar.gz").getAbsolutePath());
        openSpecyArchive.setName("OpenSpecy");
        openSpecyArchive.setVersion("1.0.99");
        openSpecyArchive.setActive(true);
        openSpecyArchive.setMd5sum("2333d8335e081ac4607495fe5e840dde");
        openSpecyArchive.setRVersion("4.2.1");
        openSpecyArchive.setBuilt("R 4.2.1; ; 2024-06-13 23:29:42 UTC; unix");

        final RPackage arrowBinaryVersion = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        arrowBinaryVersion.setSource(
                new File(packageUploadDirectory + "/repositories/2/9955549/arrow_8.0.0.tar.gz").getAbsolutePath());
        arrowBinaryVersion.setName("arrow");
        arrowBinaryVersion.setVersion("8.0.0");
        arrowBinaryVersion.setActive(true);
        arrowBinaryVersion.setMd5sum("b55eb6a2f5adeff68f1ef15fd35b03de");
        arrowBinaryVersion.setRVersion("4.2.0");
        arrowBinaryVersion.setBuilt("R 4.2.0; x86_64-pc-linux-gnu; 2022-06-07 00:49:30 UTC; unix");

        List<RPackage> sourcePackages = List.of(
                benchmarkingArchivePackage,
                benchmarkingPackage,
                accruedArchive1,
                accruedArchive2,
                accruedLatest,
                openSpecySourceVersion,
                arrowSourceVersion);
        List<RPackage> archiveSourcePackages = List.of(benchmarkingArchivePackage, accruedArchive1, accruedArchive2);
        List<RPackage> latestSourcePackages =
                List.of(benchmarkingPackage, accruedLatest, arrowSourceVersion, openSpecySourceVersion);

        List<RPackage> binaryPackages = List.of(openSpecyLatest, openSpecyArchive, arrowBinaryVersion);
        List<RPackage> archiveBinaryPackages = List.of(openSpecyArchive);
        List<RPackage> latestBinaryPackages = List.of(openSpecyLatest, arrowBinaryVersion);

        storage.organizePackagesInStorage(
                datestamp,
                sourcePackages,
                latestSourcePackages,
                archiveSourcePackages,
                binaryPackages,
                latestBinaryPackages,
                archiveBinaryPackages,
                repository);

        final File currentDatestampGeneratedDirectory = new File(repositoryGenerationDirectory + "/2/20240110");
        final File currentGeneratedDirectory = new File(repositoryGenerationDirectory + "/2/current");

        final File actualLatestSourcePackagesFile =
                new File(repositoryGenerationDirectory + "/2/20240110/src/contrib/latest/PACKAGES");
        final File actualArchiveSourcePackagesFile =
                new File(repositoryGenerationDirectory + "/2/20240110/src/contrib/Archive/PACKAGES");
        final String actualLatestSourcePackagesFileContent = Files.readString(actualLatestSourcePackagesFile.toPath());
        final String actualArchiveSourcePackagesFileContent =
                Files.readString(actualArchiveSourcePackagesFile.toPath());

        final File actualLatestSourcePackagesGzFile =
                new File(repositoryGenerationDirectory + "/2/20240110/src/contrib/latest/PACKAGES.gz");
        final File actualArchiveSourcePackagesGzFile =
                new File(repositoryGenerationDirectory + "/2/20240110/src/contrib/Archive/PACKAGES.gz");
        final String actualLatestSourcePackagesGzFileContent = readGzFileContent(actualLatestSourcePackagesGzFile);
        final String actualArchiveSourcePackagesGzFileContent = readGzFileContent(actualArchiveSourcePackagesGzFile);

        final File actualLatestBinaryPackagesFile_HigherRVersion =
                new File(repositoryGenerationDirectory + "/2/20240110/bin/linux/centos7/x86_64/4.5/latest/PACKAGES");
        final File actualLatestBinaryPackagesFile_LowerRVersion =
                new File(repositoryGenerationDirectory + "/2/20240110/bin/linux/centos7/x86_64/4.2/latest/PACKAGES");
        final File actualArchiveBinaryPackagesFile_LowerRVersion =
                new File(repositoryGenerationDirectory + "/2/20240110/bin/linux/centos7/x86_64/4.2/Archive/PACKAGES");

        final String actualLatestBinaryPackagesFileContent_HigherRVersion =
                Files.readString(actualLatestBinaryPackagesFile_HigherRVersion.toPath());
        final String actualLatestBinaryPackagesFileContent_LowerRVersion =
                Files.readString(actualLatestBinaryPackagesFile_LowerRVersion.toPath());
        final String actualArchiveBinaryPackagesFileContent_LowerRVersion =
                Files.readString(actualArchiveBinaryPackagesFile_LowerRVersion.toPath());

        final File actualLatestBinaryPackagesGzFile_HigherRVersion =
                new File(repositoryGenerationDirectory + "/2/20240110/bin/linux/centos7/x86_64/4.5/latest/PACKAGES.gz");
        final File actualLatestBinaryPackagesGzFile_LowerRVersion =
                new File(repositoryGenerationDirectory + "/2/20240110/bin/linux/centos7/x86_64/4.2/latest/PACKAGES.gz");
        final File actualArchiveBinaryPackagesGzFile_LowerRVersion = new File(
                repositoryGenerationDirectory + "/2/20240110/bin/linux/centos7/x86_64/4.2/Archive/PACKAGES.gz");

        final String actualLatestBinaryPackagesGzFileContent_HigherRVersion =
                readGzFileContent(actualLatestBinaryPackagesGzFile_HigherRVersion);
        final String actualLatestBinaryPackagesGzFileContent_LowerRVersion =
                readGzFileContent(actualLatestBinaryPackagesGzFile_LowerRVersion);
        final String actualArchiveBinaryPackagesGzFileContent_LowerRVersion =
                readGzFileContent(actualArchiveBinaryPackagesGzFile_LowerRVersion);

        final String expectedLatestSourcePackagesFileContent = getExpectedLatestPackagesFile_forRedirectToSource();
        final String expectedArchiveSourcePackagesFileContent = getExpectedArchivePackagesFile_forRedirectToSource();

        final String expectedLatestBinaryPackagesFileContent_HigherRVersion =
                getExpectedBinaryLatestPackagesFileForHigherRVersion_forRedirectToSource();
        final String expectedLatestBinaryPackagesFileContent_LowerRVersion =
                getExpectedBinaryLatestPackagesFileForLowerRVersion_forRedirectToSource();
        final String expectedArchiveBinaryPackagesFileContent_LowerRVersion =
                getExpectedBinaryArchivePackagesFileForLowerRVersion_forRedirectToSource();

        verifySourcePackage(currentGeneratedDirectory, "Benchmarking_0.10.tar.gz", true);
        verifySourcePackage(currentGeneratedDirectory, "Benchmarking_0.31.tar.gz", false);
        verifySourcePackage(currentGeneratedDirectory, "arrow_18.1.0.1.tar.gz", false);
        verifySourcePackage(currentGeneratedDirectory, "accrued_1.2.tar.gz", true);
        verifySourcePackage(currentGeneratedDirectory, "accrued_1.3.tar.gz", true);
        verifySourcePackage(currentGeneratedDirectory, "accrued_1.4.tar.gz", false);
        verifySourcePackage(currentGeneratedDirectory, "OpenSpecy_1.1.0.tar.gz", false);

        verifyBinaryPackage(currentGeneratedDirectory, "bin/linux/centos7/x86_64/4.5", "OpenSpecy_1.1.0.tar.gz", false);
        verifyBinaryPackage(currentGeneratedDirectory, "bin/linux/centos7/x86_64/4.2", "OpenSpecy_1.0.99.tar.gz", true);
        verifyBinaryPackage(
                currentGeneratedDirectory,
                "bin/linux/centos7/x86_64/4.2",
                "arrow_8.0.0.tar.gz",
                true); // latest binary package becomes archive, due to higher version of source package

        assertEquals(
                expectedLatestSourcePackagesFileContent,
                actualLatestSourcePackagesFileContent,
                "Incorrect latest source PACKAGES file");
        assertEquals(
                expectedArchiveSourcePackagesFileContent,
                actualArchiveSourcePackagesFileContent,
                "Incorrect archive source PACKAGES file");
        assertEquals(
                expectedLatestBinaryPackagesFileContent_HigherRVersion,
                actualLatestBinaryPackagesFileContent_HigherRVersion,
                "Incorrect latest binary PACKAGES file");
        assertEquals(
                expectedLatestBinaryPackagesFileContent_LowerRVersion,
                actualLatestBinaryPackagesFileContent_LowerRVersion,
                "Incorrect latest binary PACKAGES file");
        assertEquals(
                expectedArchiveBinaryPackagesFileContent_LowerRVersion,
                actualArchiveBinaryPackagesFileContent_LowerRVersion,
                "Incorrect archive binary PACKAGES file");
        assertEquals(
                expectedLatestSourcePackagesFileContent,
                actualLatestSourcePackagesGzFileContent,
                "Incorrect latest source PACKAGES.gz file");
        assertEquals(
                expectedArchiveSourcePackagesFileContent,
                actualArchiveSourcePackagesGzFileContent,
                "Incorrect archive source PACKAGES.gz file");
        assertEquals(
                expectedLatestBinaryPackagesFileContent_HigherRVersion,
                actualLatestBinaryPackagesGzFileContent_HigherRVersion,
                "Incorrect latest binary PACKAGES.gz file");
        assertEquals(
                expectedLatestBinaryPackagesFileContent_LowerRVersion,
                actualLatestBinaryPackagesGzFileContent_LowerRVersion,
                "Incorrect latest binary PACKAGES.gz file");
        assertEquals(
                expectedArchiveBinaryPackagesFileContent_LowerRVersion,
                actualArchiveBinaryPackagesGzFileContent_LowerRVersion,
                "Incorrect archive binary PACKAGES.gz file");
        assertTrue(currentDatestampGeneratedDirectory.isDirectory(), "Directory was not generated.");
        assertTrue(Files.isSymbolicLink(currentGeneratedDirectory.toPath()), "current should be a symlink");
        assertEquals(
                currentDatestampGeneratedDirectory.getAbsolutePath(),
                currentGeneratedDirectory.toPath().toRealPath().toFile().getAbsolutePath(),
                "Symlink does not point at a right dir.");
    }

    private String readGzFileContent(File gzFile) throws IOException {

        try (GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(gzFile));
                InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            return sb.toString();
        }
    }

    private String getExpectedBinaryArchivePackagesFileForLowerRVersion_forRedirectToSource() {
        return """
				Package: OpenSpecy
				Version: 1.0.99
				License: Some license1
				MD5Sum: 2333d8335e081ac4607495fe5e840dde
				NeedsCompilation: no
				Built: R 4.2.1; ; 2024-06-13 23:29:42 UTC; unix

				Package: arrow
				Version: 8.0.0
				License: Some license1
				MD5Sum: b55eb6a2f5adeff68f1ef15fd35b03de
				NeedsCompilation: no
				Built: R 4.2.0; x86_64-pc-linux-gnu; 2022-06-07 00:49:30 UTC; unix

				Package: Benchmarking
				Version: 0.10
				License: Some license1
				MD5Sum: 9a99c2ebefa6d49422ca7893c1f4ead8
				NeedsCompilation: no

				Package: accrued
				Version: 1.2
				License: Some license1
				MD5Sum: 70d295115295a4718593f6a39d77add9
				NeedsCompilation: no

				Package: accrued
				Version: 1.3
				License: Some license1
				MD5Sum: a05e4ca44438c0d9e7d713d7e3890423
				NeedsCompilation: no

				""";
    }

    private String getExpectedBinaryLatestPackagesFileForLowerRVersion_forRedirectToSource() {
        return """
				Package: arrow
				Version: 18.1.0.1
				License: Some license1
				MD5Sum: 4fc2b28d05af4d6e458494c385082699
				NeedsCompilation: no

				Package: Benchmarking
				Version: 0.31
				License: Some license1
				MD5Sum: 136460e58c711ed9cc1cdbdbde2e5b86
				NeedsCompilation: no

				Package: accrued
				Version: 1.4
				License: Some license1
				MD5Sum: 97c2930a9dd7ca9fc1409d5340c06470
				NeedsCompilation: no

				Package: OpenSpecy
				Version: 1.1.0
				License: Some license1
				MD5Sum: 0da320752536e9659fd2f8e4e99ecb7f
				NeedsCompilation: no

				""";
    }

    private String getExpectedBinaryLatestPackagesFileForHigherRVersion_forRedirectToSource() {
        return """
				Package: OpenSpecy
				Version: 1.1.0
				License: Some license1
				MD5Sum: 13bda5374451f899771b8388983fe334
				NeedsCompilation: no
				Built: R 4.5.0; ; 2024-06-13 23:29:42 UTC; unix

				Package: Benchmarking
				Version: 0.31
				License: Some license1
				MD5Sum: 136460e58c711ed9cc1cdbdbde2e5b86
				NeedsCompilation: no

				Package: accrued
				Version: 1.4
				License: Some license1
				MD5Sum: 97c2930a9dd7ca9fc1409d5340c06470
				NeedsCompilation: no

				Package: arrow
				Version: 18.1.0.1
				License: Some license1
				MD5Sum: 4fc2b28d05af4d6e458494c385082699
				NeedsCompilation: no

				""";
    }

    private String getExpectedArchivePackagesFile_forRedirectToSource() {
        return """
				Package: Benchmarking
				Version: 0.10
				License: Some license1
				MD5Sum: 9a99c2ebefa6d49422ca7893c1f4ead8
				NeedsCompilation: no

				Package: accrued
				Version: 1.2
				License: Some license1
				MD5Sum: 70d295115295a4718593f6a39d77add9
				NeedsCompilation: no

				Package: accrued
				Version: 1.3
				License: Some license1
				MD5Sum: a05e4ca44438c0d9e7d713d7e3890423
				NeedsCompilation: no

				""";
    }

    private String getExpectedLatestPackagesFile_forRedirectToSource() {
        return """
                Package: Benchmarking
                Version: 0.31
                License: Some license1
                MD5Sum: 136460e58c711ed9cc1cdbdbde2e5b86
                NeedsCompilation: no

                Package: accrued
                Version: 1.4
                License: Some license1
                MD5Sum: 97c2930a9dd7ca9fc1409d5340c06470
                NeedsCompilation: no

                Package: arrow
                Version: 18.1.0.1
                License: Some license1
                MD5Sum: 4fc2b28d05af4d6e458494c385082699
                NeedsCompilation: no

                Package: OpenSpecy
                Version: 1.1.0
                License: Some license1
                MD5Sum: 0da320752536e9659fd2f8e4e99ecb7f
                NeedsCompilation: no

                """;
    }

    private String getExpectedArchivePackagesFile() {
        return """
                Package: Benchmarking
                Version: 0.10
                License: Some license1
                MD5Sum: 9a99c2ebefa6d49422ca7893c1f4ead8
                NeedsCompilation: no

                """;
    }

    private String getExpectedLatestPackagesFile() {
        return """
                Package: Benchmarking
                Version: 0.31
                License: Some license1
                MD5Sum: 136460e58c711ed9cc1cdbdbde2e5b86
                NeedsCompilation: no

                Package: bea.R
                Version: 1.0.5
                License: Some license1
                MD5Sum: 5e664f320c7cc884138d64467f6b0e49
                NeedsCompilation: no

                """;
    }

    private String getExpectedBinaryLatestPackagesFileForHigherRVersion() {
        return """
                Package: OpenSpecy
                Version: 1.1.0
                License: Some license1
                MD5Sum: 13bda5374451f899771b8388983fe334
                NeedsCompilation: no
                Built: R 4.5.0; ; 2024-06-13 23:29:42 UTC; unix

                """;
    }

    private String getExpectedBinaryLatestPackagesFileForLowerRVersion() {
        return """
                Package: arrow
                Version: 8.0.0
                License: Some license1
                MD5Sum: b55eb6a2f5adeff68f1ef15fd35b03de
                NeedsCompilation: no
                Built: R 4.2.0; x86_64-pc-linux-gnu; 2022-06-07 00:49:30 UTC; unix

                """;
    }

    private String getExpectedBinaryArchivePackagesFileForLowerRVersion() {
        return """
                Package: OpenSpecy
                Version: 1.0.99
                License: Some license1
                MD5Sum: 2333d8335e081ac4607495fe5e840dde
                NeedsCompilation: no
                Built: R 4.2.1; ; 2024-06-13 23:29:42 UTC; unix

                """;
    }

    private void verifySourcePackage(File currentGeneratedDirectory, String filename, boolean archive) {
        final File packageFile =
                new File(currentGeneratedDirectory + "/src/contrib" + (archive ? "/Archive/" : "/latest/") + filename);
        assertTrue(packageFile.exists(), "Package file " + packageFile.getAbsolutePath() + " does not exist.");
    }

    private void verifyBinaryPackage(
            File currentGeneratedDirectory, String binaryFolder, String filename, boolean archive) {
        final File packageFile = new File(
                currentGeneratedDirectory + "/" + binaryFolder + (archive ? "/Archive/" : "/latest/") + filename);
        assertTrue(packageFile.exists(), "Package file " + packageFile.getAbsolutePath() + " does not exist.");
    }

    @Test
    public void moveToMainDirectory() throws Exception {
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(2);
        final User user = UserTestFixture.GET_PACKAGE_MAINTAINER();
        final File packageFileToMove = new File(packageUploadDirectory + "/new/51328701/abc_1.0.tar.gz");
        final byte[] fileContent = Files.readAllBytes(packageFileToMove.toPath());
        final RPackage packageToMove = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        packageToMove.setSource(packageFileToMove.getAbsolutePath());
        packageToMove.setName("abc");
        packageToMove.setVersion("1.0");
        packageToMove.setActive(true);
        packageToMove.setDeleted(false);

        final String newPackagePath = storage.moveToMainDirectory(packageToMove);
        final File actualFile = new File(newPackagePath);
        String[] tokens = newPackagePath.split("/");
        int tl = tokens.length;
        String fileName = tokens[tl - 1];
        int randomNumber = Integer.parseInt(tokens[tl - 2]);
        String repositoryId = tokens[tl - 3];
        String repositoriesDir = tokens[tl - 4];

        final File binaryPackageFileToMove = new File(packageUploadDirectory + "/new/67826419/arrow_8.0.0.tar.gz");
        final byte[] binaryFileContent = Files.readAllBytes(binaryPackageFileToMove.toPath());
        final RPackage binaryPackage = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        binaryPackage.setSource(binaryPackageFileToMove.getAbsolutePath());
        binaryPackage.setName("arrow");
        binaryPackage.setVersion("8.0.0");
        binaryPackage.setActive(true);
        binaryPackage.setDeleted(false);

        final String newBinaryPath = storage.moveToMainDirectory(binaryPackage);
        final File actualBinaryFile = new File(newBinaryPath);

        String[] tokensBinary = newBinaryPath.split("/");
        int tbl = tokensBinary.length;
        String binaryFileName = tokensBinary[tbl - 1];
        int binaryRandomNumber = Integer.parseInt(tokensBinary[tbl - 2]);
        String repositoryIdFromBinary = tokensBinary[tbl - 3];
        String repositoriesDirFromBinary = tokensBinary[tbl - 4];

        assertTrue(
                newPackagePath.startsWith(packageUploadDirectory.getAbsolutePath()),
                "File was not uploaded to Package Upload Directory!");
        assertTrue(randomNumber > -1, "Incorrect random waiting room number.");
        assertEquals("repositories", repositoriesDir, "Repositories dir should be called \"repositories\".");
        assertEquals("2", repositoryId, "Incorrect repository id in path.");
        assertEquals("abc_1.0.tar.gz", fileName, "Incorrect file name.");
        assertTrue(Files.exists(actualFile.toPath()), "File does not exist in correct directory");
        assertArrayEquals(fileContent, Files.readAllBytes(actualFile.toPath()), "File content is incorrect.");

        assertTrue(
                newBinaryPath.startsWith(packageUploadDirectory.getAbsolutePath()),
                "Binary file was not uploaded to Package Upload Directory!");
        assertTrue(binaryRandomNumber > -1, "Incorrect random waiting room number for binary.");
        assertEquals(
                "repositories",
                repositoriesDirFromBinary,
                "Repositories dir in binary file should be called \"repositories\".");
        assertEquals("2", repositoryIdFromBinary, "Incorrect repository id in binary path.");
        assertEquals("arrow_8.0.0.tar.gz", binaryFileName, "Incorrect binary file name.");
        assertTrue(Files.exists(actualBinaryFile.toPath()), "Binary file does not exist in correct directory");
        assertArrayEquals(
                binaryFileContent, Files.readAllBytes(actualBinaryFile.toPath()), "Binary file content is incorrect.");
    }

    @Test
    public void removesGenerationDirectoryContent() throws Exception {
        ReflectionTestUtils.setField(storage, RLocalStorage.class, "snapshot", "false", String.class);
        MultiValueMap<String, RPackage> binLatestPackagesPaths = new LinkedMultiValueMap<>();
        MultiValueMap<String, RPackage> binArchivePackagesPaths = new LinkedMultiValueMap<>();
        binLatestPackagesPaths.addAll("2/20240110/bin/linux/centos7/x86_64/4.5", List.of());
        binLatestPackagesPaths.addAll("2/20240110/bin/linux/centos7/x86_64/4.2", List.of());
        binArchivePackagesPaths.addAll("2/20240110/bin/linux/centos7/x86_64/4.2/Archive", List.of());
        final PopulatedRepositoryContent populatedContent = new PopulatedRepositoryContent(
                List.of(),
                List.of(),
                repositoryGenerationDirectory.getAbsolutePath() + "/2/20240110/src/contrib",
                repositoryGenerationDirectory.getAbsolutePath() + "/2/20240110/src/contrib/Archive",
                binLatestPackagesPaths,
                binArchivePackagesPaths);
        storage.cleanUpAfterSynchronization(populatedContent);

        assertTrue(
                repositoryGenerationDirectory.exists() && repositoryGenerationDirectory.isDirectory(),
                "Repository generation directory should not be deleted!");
        assertEquals(
                0,
                Objects.requireNonNull(repositoryGenerationDirectory.listFiles()).length,
                "Generation directory should be empty when snapshots are off.");
    }

    @Test
    public void writeToWaitingRoom() throws Exception {
        final File newPackage = new File("src/test/resources/unit/test_packages/accrued_1.2.tar.gz");
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(2);
        repository.setPublished(false);
        final byte[] fileContent = Files.readAllBytes(newPackage.toPath());
        final MultipartFile multipart = new MockMultipartFile(
                "accrued_1.2.tar.gz", "accrued_1.2.tar.gz", ContentType.MULTIPART_FORM_DATA.toString(), fileContent);

        final String pathToWaitingRoom = storage.writeToWaitingRoom(multipart, repository);
        final File actualFile = new File(pathToWaitingRoom);
        final byte[] actualContent = Files.readAllBytes(actualFile.toPath());

        final String[] tokens = pathToWaitingRoom.split("/");
        final int tl = tokens.length;
        final String fileName = tokens[tl - 1];
        final int randomNumber = Integer.parseInt(tokens[tl - 2]);
        final String waitingRoomFolderName = tokens[tl - 3];

        final File binaryPackageFile =
                new File("src/test/resources/unit/test_packages/binary_package/arrow_8.0.0.tar.gz");
        final byte[] binaryFileContent = Files.readAllBytes(binaryPackageFile.toPath());
        final MultipartFile multipartBinary = new MockMultipartFile(
                "arrow_8.0.0.tar.gz",
                "arrow_8.0.0.tar.gz",
                ContentType.MULTIPART_FORM_DATA.toString(),
                binaryFileContent);

        final String binaryPathToWaitingRoom = storage.writeToWaitingRoom(multipartBinary, repository);
        final File actualBinaryFile = new File(binaryPathToWaitingRoom);
        final byte[] actualBinaryContent = Files.readAllBytes(actualBinaryFile.toPath());

        String[] tokensBinary = binaryPathToWaitingRoom.split("/");
        int tbl = tokensBinary.length;
        String binaryFileName = tokensBinary[tbl - 1];
        int binaryRandomNumber = Integer.parseInt(tokensBinary[tbl - 2]);
        String binaryWaitingRoomFolderName = tokensBinary[tbl - 3];

        assertTrue(
                pathToWaitingRoom.startsWith(packageUploadDirectory.getAbsolutePath()),
                "File was not uploaded to Package Upload Directory!");
        assertEquals("accrued_1.2.tar.gz", fileName, "Incorrect file name.");
        assertTrue(randomNumber > -1, "Incorrect random waiting room number.");
        assertEquals("new", waitingRoomFolderName, "Waiting Room should be called \"new\".");
        assertTrue(Files.exists(actualFile.toPath()), "File does not exist in correct directory");
        assertArrayEquals(fileContent, actualContent, "File content is incorrect.");

        assertTrue(
                binaryPathToWaitingRoom.startsWith(packageUploadDirectory.getAbsolutePath()),
                "Binary file was not uploaded to Package Upload Directory!");
        assertEquals("arrow_8.0.0.tar.gz", binaryFileName, "Incorrect binary file name.");
        assertTrue(binaryRandomNumber > -1, "Incorrect random waiting room number in binary.");
        assertEquals("new", binaryWaitingRoomFolderName, "Waiting Room should be called \"new\".");
        assertTrue(Files.exists(actualBinaryFile.toPath()), "File does not exist in correct directory");
        assertArrayEquals(binaryFileContent, actualBinaryContent, "File content is incorrect.");
    }

    @Test
    public void moveToTrashDirectory() throws Exception {
        final File packageFile = new File(packageUploadDirectory + "/repositories/2/89565416/bea.R_1.0.5.tar.gz");
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        final byte[] fileContent = Files.readAllBytes(packageFile.toPath());
        repository.setId(2);
        repository.setPublished(false);
        final User user = UserTestFixture.GET_PACKAGE_MAINTAINER();
        final RPackage packageBag = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        packageBag.setSource(packageFile.getAbsolutePath());
        packageBag.setName("bea.R");
        packageBag.setVersion("1.0.5");
        packageBag.setActive(true);
        packageBag.setDeleted(false);

        final String newPackagePath = storage.moveToTrashDirectory(packageBag);
        final File actualFile = new File(newPackagePath);
        final File extractedDir = new File(actualFile.getParentFile().getAbsoluteFile() + "/bea.R");

        System.out.println("Path: " + newPackagePath);
        String[] tokens = newPackagePath.split("/");
        int tl = tokens.length;
        String fileName = tokens[tl - 1];
        int randomNumber = Integer.parseInt(tokens[tl - 2]);
        String repositoryId = tokens[tl - 3];
        String trashDir = tokens[tl - 4];

        final File binaryPackageFile = new File(packageUploadDirectory + "/repositories/2/9955549/arrow_8.0.0.tar.gz");
        final byte[] binaryFileContent = Files.readAllBytes(binaryPackageFile.toPath());
        final RPackage binaryPackage = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        binaryPackage.setSource(binaryPackageFile.getAbsolutePath());
        binaryPackage.setName("arrow");
        binaryPackage.setVersion("8.0.0");
        binaryPackage.setActive(true);
        binaryPackage.setDeleted(false);

        final String newBinaryPath = storage.moveToTrashDirectory(binaryPackage);
        final File actualBinaryFile = new File(newBinaryPath);
        final File extractedBinaryDir =
                new File(actualBinaryFile.getParentFile().getAbsoluteFile() + "/arrow");

        System.out.println("Path: " + newBinaryPath);
        String[] tokensBinary = newBinaryPath.split("/");
        int tbl = tokensBinary.length;
        String binaryFileName = tokensBinary[tbl - 1];
        int binaryRandomNumber = Integer.parseInt(tokensBinary[tbl - 2]);
        String repositoryIdFromBinary = tokensBinary[tbl - 3];
        String trashDirFromBinary = tokensBinary[tbl - 4];

        assertTrue(
                newPackagePath.startsWith(packageUploadDirectory.getAbsolutePath()),
                "File was not uploaded to Package Upload Directory!");
        assertTrue(randomNumber > -1, "Incorrect random waiting room number.");
        assertEquals("trash", trashDir, "Repositories dir should be called \"repositories\".");
        assertEquals("2", repositoryId, "Incorrect repository id in path.");
        assertEquals("bea.R_1.0.5.tar.gz", fileName, "Incorrect file name.");
        assertTrue(Files.exists(actualFile.toPath()), "File does not exist in correct directory");
        assertArrayEquals(fileContent, Files.readAllBytes(actualFile.toPath()), "File content is incorrect.");
        assertTrue(
                extractedDir.isDirectory() && extractedDir.getAbsolutePath().endsWith("bea.R"),
                "Package extracted content was not put into trash properly.");

        assertTrue(
                newBinaryPath.startsWith(packageUploadDirectory.getAbsolutePath()),
                "Binary file was not uploaded to Package Upload Directory!");
        assertTrue(binaryRandomNumber > -1, "Incorrect random waiting room number for binary.");
        assertEquals(
                "trash", trashDirFromBinary, "Repositories dir (in binary file) should be called \"repositories\".");
        assertEquals("2", repositoryIdFromBinary, "Incorrect repository id in path for binary file.");
        assertEquals("arrow_8.0.0.tar.gz", binaryFileName, "Incorrect binary file name.");
        assertTrue(Files.exists(actualBinaryFile.toPath()), "Binary file does not exist in correct directory");
        assertArrayEquals(
                binaryFileContent, Files.readAllBytes(actualBinaryFile.toPath()), "Binary file content is incorrect.");
        assertTrue(
                extractedBinaryDir.isDirectory()
                        && extractedBinaryDir.getAbsolutePath().endsWith("arrow"),
                "Binary package extracted content was not put into trash properly.");
    }

    @Test
    public void removePackageSource() throws Exception {
        final File packageFile = new File(packageUploadDirectory + "/repositories/2/89565416/bea.R_1.0.5.tar.gz");
        final RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        repository.setId(2);
        repository.setPublished(false);
        final User user = UserTestFixture.GET_PACKAGE_MAINTAINER();
        final RPackage packageBag = RPackageTestFixture.GET_FIXTURE_PACKAGE(repository, user);
        packageBag.setSource(packageFile.getAbsolutePath());
        packageBag.setName("bea.R");
        packageBag.setVersion("1.0.5");
        packageBag.setActive(true);
        packageBag.setDeleted(false);

        final String newPackagePath = storage.moveToTrashDirectory(packageBag);
        final File actualFile = new File(newPackagePath);
        final File extractedDir = new File(actualFile.getParentFile().getAbsoluteFile() + "/bea.R");

        storage.removePackageSource(newPackagePath);

        final File binaryPackageFile = new File(packageUploadDirectory + "/repositories/2/9955549/arrow_8.0.0.tar.gz");
        final RPackage binaryPackage = RPackageTestFixture.GET_FIXTURE_BINARY_PACKAGE(repository, user);
        binaryPackage.setSource(binaryPackageFile.getAbsolutePath());
        binaryPackage.setName("arrow");
        binaryPackage.setVersion("8.0.0");
        binaryPackage.setActive(true);
        binaryPackage.setDeleted(false);

        final String newBinaryPath = storage.moveToTrashDirectory(binaryPackage);
        final File actualBinaryFile = new File(newBinaryPath);
        final File extractedBinaryDir =
                new File(actualBinaryFile.getParentFile().getAbsoluteFile() + "/arrow");

        storage.removePackageSource(newBinaryPath);

        assertFalse(actualFile.exists(), "Package source file has not been removed");
        assertFalse(extractedDir.exists(), "Extracted source has not been removed");
        assertFalse(actualBinaryFile.exists(), "Binary package source file has not been removed");
        assertFalse(extractedBinaryDir.exists(), "Binary extracted source has not been removed");
    }
}

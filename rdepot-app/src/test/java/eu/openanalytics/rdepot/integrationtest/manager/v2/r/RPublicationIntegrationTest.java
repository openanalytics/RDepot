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
package eu.openanalytics.rdepot.integrationtest.manager.v2.r;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.openanalytics.rdepot.integrationtest.environment.BashScriptExecutor;
import eu.openanalytics.rdepot.integrationtest.manager.v2.IntegrationTest;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.MultiPartSpecification;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

public class RPublicationIntegrationTest extends IntegrationTest {
    public RPublicationIntegrationTest() {
        super("/api/v2/manager/r/submissions");
    }

    private static final String REPOSITORIES_ENDPOINT = "/api/v2/manager/r/repositories";
    private static final String PACKAGES_ENDPOINT = "/api/v2/manager/r/packages";
    private static final BashScriptExecutor bashScriptExecutor = new BashScriptExecutor();

    private static class ExamplePackages {
        static final File publicationTestPackageDirectory =
                new File("src/test/resources/itestPublicationTestPackages/r");
        static final File sourcePackagesDirectory = new File(publicationTestPackageDirectory, "source");
        static final File binaryPackagesDirectory = new File(publicationTestPackageDirectory, "binary");

        static class Source {
            static final File sourceLatestPackagesDirectory = new File(sourcePackagesDirectory, "latest");
            static final File sourceArchivePackagesDirectory = new File(sourcePackagesDirectory, "archive");

            static class Latest {
                static final File cards070 = new File(sourceLatestPackagesDirectory, "cards_0.7.0.tar.gz");
                static final File openssl234 = new File(sourceLatestPackagesDirectory, "openssl_2.3.4.tar.gz");
                static final File matchingR200 = new File(sourceLatestPackagesDirectory, "matchingR_2.0.0.tar.gz");
                static final File fmtr173 = new File(sourceLatestPackagesDirectory, "fmtr_1.7.3.tar.gz");
                static final File lama210 = new File(sourceLatestPackagesDirectory, "LaMa_2.1.0.tar.gz");
                static final File robustPrioritizr103 =
                        new File(sourceLatestPackagesDirectory, "robust.prioritizr_1.0.3.tar.gz");
                static final File redeAgroRadar011 =
                        new File(sourceLatestPackagesDirectory, "RedeAgroRadar_0.1.1.tar.gz");
                static final File extr110 = new File(sourceLatestPackagesDirectory, "extr_1.1.0.tar.gz");
                static final File warden202 = new File(sourceLatestPackagesDirectory, "WARDEN_2.0.2.tar.gz");
                static final File rush100 = new File(sourceLatestPackagesDirectory, "rush_1.0.0.tar.gz");
                static final File rlandfire202 = new File(sourceLatestPackagesDirectory, "rlandfire_2.0.2.tar.gz");
                static final File metrica211 = new File(sourceLatestPackagesDirectory, "metrica_2.1.1.tar.gz");
            }

            static class Archive {
                static final File matchingR130 = new File(sourceArchivePackagesDirectory, "matchingR_1.3.0.tar.gz");
                static final File matchingR132 = new File(sourceArchivePackagesDirectory, "matchingR_1.3.2.tar.gz");
                static final File openssl08 = new File(sourceArchivePackagesDirectory, "openssl_0.8.tar.gz");
                static final File openssl144 = new File(sourceArchivePackagesDirectory, "openssl_1.4.4.tar.gz");
                static final File lama100 = new File(sourceArchivePackagesDirectory, "LaMa_1.0.0.tar.gz");
                static final File lama200 = new File(sourceArchivePackagesDirectory, "LaMa_2.0.0.tar.gz");
                static final File lama201 = new File(sourceArchivePackagesDirectory, "LaMa_2.0.1.tar.gz");
                static final File lama202 = new File(sourceArchivePackagesDirectory, "LaMa_2.0.2.tar.gz");
                static final File metrica123 = new File(sourceArchivePackagesDirectory, "metrica_1.2.3.tar.gz");
                static final File metrica200 = new File(sourceArchivePackagesDirectory, "metrica_2.0.0.tar.gz");
                static final File metrica201 = new File(sourceArchivePackagesDirectory, "metrica_2.0.1.tar.gz");
            }
        }

        static class Binary {
            static final File binaryLatestPackagesDirectory = new File(binaryPackagesDirectory, "latest_4_5");
            static final File binaryArchivePackagesDirectory = new File(binaryPackagesDirectory, "archive_4_5");

            static class Latest {
                static final File car313 =
                        new File(binaryLatestPackagesDirectory, "car_3.1-3_R_x86_64-pc-linux-gnu.tar.gz");
                static final File ggplot2400 =
                        new File(binaryLatestPackagesDirectory, "ggplot2_4.0.0_R_x86_64-pc-linux-gnu.tar.gz");
                static final File lama100 =
                        new File(binaryLatestPackagesDirectory, "LaMa_1.0.0_R_x86_64-pc-linux-gnu.tar.gz");
                static final File metrica211 =
                        new File(binaryLatestPackagesDirectory, "metrica_2.1.1_R_x86_64-pc-linux-gnu.tar.gz");
                static final File warden202 =
                        new File(binaryLatestPackagesDirectory, "WARDEN_2.0.2_R_x86_64-pc-linux-gnu.tar.gz");
                static final File fmtr173 =
                        new File(binaryLatestPackagesDirectory, "fmtr_1.7.3_R_x86_64-pc-linux-gnu.tar.gz");
            }

            static class Archive {
                static final File car215 =
                        new File(binaryArchivePackagesDirectory, "car_2.1-5_R_x86_64-pc-linux-gnu.tar.gz");
                static final File ggplot2352 =
                        new File(binaryArchivePackagesDirectory, "ggplot2_3.5.2_R_x86_64-pc-linux-gnu.tar.gz");
                static final File ggplot2351 =
                        new File(binaryArchivePackagesDirectory, "ggplot2_3.5.1_R_x86_64-pc-linux-gnu.tar.gz");
                static final File metrica123 =
                        new File(binaryArchivePackagesDirectory, "metrica_1.2.3_R_x86_64-pc-linux-gnu.tar.gz");
                static final File metrica200 =
                        new File(binaryArchivePackagesDirectory, "metrica_2.0.0_R_x86_64-pc-linux-gnu.tar.gz");
                static final File metrica201 =
                        new File(binaryArchivePackagesDirectory, "metrica_2.0.1_R_x86_64-pc-linux-gnu.tar.gz");
            }
        }
    }

    static final String TEST_REPO_NAME = "my-test-repo-123";

    private void createTestRepo() {
        final String body = "{"
                + "\"name\": \"" + TEST_REPO_NAME + "\","
                + "\"publicationUri\":\"http://localhost/repo/" + TEST_REPO_NAME + "\","
                + "\"serverAddress\":\"http://oa-rdepot-repo:8080/" + TEST_REPO_NAME + "\""
                + "}";
        given().header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .when()
                .body(body)
                .post(REPOSITORIES_ENDPOINT)
                .then()
                .statusCode(HttpStatus.SC_CREATED);
    }

    private static MultiPartSpecification fileToUpload(File packageBag) throws IOException {
        return new MultiPartSpecBuilder(Files.readAllBytes(packageBag.toPath()))
                .fileName(packageBag.getName())
                .mimeType("application/gzip")
                .controlName("file")
                .build();
    }

    public void prepBigTest() throws Exception {
        final List<MultiPartSpecification> sourcesToUpload = List.of(
                fileToUpload(ExamplePackages.Source.Latest.cards070),
                fileToUpload(ExamplePackages.Source.Latest.matchingR200),
                fileToUpload(ExamplePackages.Source.Latest.openssl234),
                fileToUpload(ExamplePackages.Source.Latest.fmtr173),
                fileToUpload(ExamplePackages.Source.Latest.lama210),
                fileToUpload(ExamplePackages.Source.Latest.robustPrioritizr103),
                fileToUpload(ExamplePackages.Source.Latest.redeAgroRadar011),
                fileToUpload(ExamplePackages.Source.Latest.extr110),
                fileToUpload(ExamplePackages.Source.Latest.warden202),
                fileToUpload(ExamplePackages.Source.Latest.rush100),
                fileToUpload(ExamplePackages.Source.Latest.rlandfire202),
                fileToUpload(ExamplePackages.Source.Latest.metrica211),
                fileToUpload(ExamplePackages.Source.Archive.matchingR130),
                fileToUpload(ExamplePackages.Source.Archive.matchingR132),
                fileToUpload(ExamplePackages.Source.Archive.openssl08),
                fileToUpload(ExamplePackages.Source.Archive.openssl144),
                fileToUpload(ExamplePackages.Source.Archive.lama100),
                fileToUpload(ExamplePackages.Source.Archive.lama200),
                fileToUpload(ExamplePackages.Source.Archive.lama201),
                fileToUpload(ExamplePackages.Source.Archive.lama202),
                fileToUpload(ExamplePackages.Source.Archive.metrica123),
                fileToUpload(ExamplePackages.Source.Archive.metrica200),
                fileToUpload(ExamplePackages.Source.Archive.metrica201));
        final List<MultiPartSpecification> binariesToUpload = List.of(
                fileToUpload(ExamplePackages.Binary.Latest.car313),
                fileToUpload(ExamplePackages.Binary.Latest.ggplot2400),
                fileToUpload(ExamplePackages.Binary.Latest.lama100),
                fileToUpload(ExamplePackages.Binary.Latest.fmtr173),
                fileToUpload(ExamplePackages.Binary.Latest.warden202),
                fileToUpload(ExamplePackages.Binary.Latest.metrica211),
                fileToUpload(ExamplePackages.Binary.Archive.car215),
                fileToUpload(ExamplePackages.Binary.Archive.ggplot2352),
                fileToUpload(ExamplePackages.Binary.Archive.metrica123),
                fileToUpload(ExamplePackages.Binary.Archive.metrica200),
                fileToUpload(ExamplePackages.Binary.Archive.metrica201),
                fileToUpload(ExamplePackages.Binary.Archive.ggplot2351));

        createTestRepo();

        for (MultiPartSpecification file : sourcesToUpload) {
            given().header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                    .accept(ContentType.JSON)
                    .contentType("multipart/form-data")
                    .multiPart(file)
                    .multiPart("repository", TEST_REPO_NAME)
                    .multiPart("generateManual", "false")
                    .multiPart("replace", "false")
                    .multiPart("changes", "")
                    .multiPart("binary", "false")
                    .when()
                    .post(apiPath)
                    .then()
                    .statusCode(HttpStatus.SC_CREATED);
        }

        // Upload binaries
        for (MultiPartSpecification file : binariesToUpload) {
            given().header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                    .accept(ContentType.JSON)
                    .contentType("multipart/form-data")
                    .multiPart(file)
                    .multiPart("repository", TEST_REPO_NAME)
                    .multiPart("generateManual", "false")
                    .multiPart("replace", "false")
                    .multiPart("changes", "")
                    .multiPart("binary", "true")
                    .multiPart("architecture", "x86_64")
                    .multiPart("rVersion", "4.5")
                    .multiPart("distribution", "centos7")
                    .when()
                    .post(apiPath)
                    .then()
                    .statusCode(HttpStatus.SC_CREATED);
        }
        publishRepo();
    }

    public void prepTest() throws Exception {
        final List<MultiPartSpecification> sourcesToUpload = List.of(
                fileToUpload(ExamplePackages.Source.Latest.cards070),
                fileToUpload(ExamplePackages.Source.Latest.matchingR200),
                fileToUpload(ExamplePackages.Source.Latest.openssl234),
                fileToUpload(ExamplePackages.Source.Archive.matchingR130),
                fileToUpload(ExamplePackages.Source.Archive.matchingR132),
                fileToUpload(ExamplePackages.Source.Archive.openssl08),
                fileToUpload(ExamplePackages.Source.Archive.openssl144));
        final List<MultiPartSpecification> binariesToUpload = List.of(
                fileToUpload(ExamplePackages.Binary.Latest.car313),
                fileToUpload(ExamplePackages.Binary.Latest.ggplot2400),
                fileToUpload(ExamplePackages.Binary.Archive.car215),
                fileToUpload(ExamplePackages.Binary.Archive.ggplot2352),
                fileToUpload(ExamplePackages.Binary.Archive.ggplot2351));

        createTestRepo();

        for (MultiPartSpecification file : sourcesToUpload) {
            given().header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                    .accept(ContentType.JSON)
                    .contentType("multipart/form-data")
                    .multiPart(file)
                    .multiPart("repository", TEST_REPO_NAME)
                    .multiPart("generateManual", "false")
                    .multiPart("replace", "false")
                    .multiPart("changes", "")
                    .multiPart("binary", "false")
                    .when()
                    .post(apiPath)
                    .then()
                    .statusCode(HttpStatus.SC_CREATED);
        }

        // Upload binaries
        for (MultiPartSpecification file : binariesToUpload) {
            given().header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                    .accept(ContentType.JSON)
                    .contentType("multipart/form-data")
                    .multiPart(file)
                    .multiPart("repository", TEST_REPO_NAME)
                    .multiPart("generateManual", "false")
                    .multiPart("replace", "false")
                    .multiPart("changes", "")
                    .multiPart("binary", "true")
                    .multiPart("architecture", "x86_64")
                    .multiPart("rVersion", "4.5")
                    .multiPart("distribution", "centos7")
                    .when()
                    .post(apiPath)
                    .then()
                    .statusCode(HttpStatus.SC_CREATED);
        }
        publishRepo();
    }

    private void changePublish(boolean published) {
        given().contentType("application/json-patch+json")
                .accept(ContentType.JSON)
                .header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                .when()
                .body("[" + "{" + "\"op\": \"replace\"," + "\"path\":\"/published\"," + "\"value\":" + published + "}"
                        + "]")
                .patch(REPOSITORIES_ENDPOINT + "/13")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    private void unpublishRepo() {
        changePublish(false);
    }

    private void publishRepo() {
        changePublish(true);
    }

    @Test
    public void installSourcePackageFromArchive() throws Exception {
        prepBigTest();
        final String result = bashScriptExecutor
                .executeBashScript(
                        "src/test/resources/scripts/" + "checkIfPublishedArchivedOpensslPackageCanBeInstalled.sh")
                .output();
        assertTrue(result.contains("* DONE (openssl)"), "Package was not installed correctly.");
        assertTrue(result.contains("Linking to: OpenSSL"), "Package could not be loaded.");
    }

    @Test
    public void installBinaryPackageFromBigArchive() throws Exception {
        prepBigTest();
        final String result = bashScriptExecutor
                .executeBashScript(
                        "src/test/resources/scripts/" + "checkIfPublishedArchivedCarPackageCanBeInstalled.sh")
                .output();
        System.out.println("Result: " + result);
        assertTrue(
                result.contains("* installing *binary* package ‘car’ ..."),
                "Source package was installed instead of binary package.");
        assertTrue(result.contains("* DONE (car)"), "Package was not installed correctly.");
        assertFalse(result.contains("Error"), "Package could not be loaded.");
    }

    @Test
    public void installBinaryPackageFromArchive() throws Exception {
        prepBigTest();
        final String result = bashScriptExecutor
                .executeBashScript(
                        "src/test/resources/scripts/" + "checkIfPublishedArchivedCarPackageCanBeInstalled.sh")
                .output();
        System.out.println("Result: " + result);
        assertTrue(
                result.contains("* installing *binary* package ‘car’ ..."),
                "Source package was installed instead of binary package.");
        assertTrue(result.contains("* DONE (car)"), "Package was not installed correctly.");
        assertFalse(result.contains("Error"), "Package could not be loaded.");
    }

    @Test
    public void installSourcePackage() throws Exception {
        prepBigTest();
        final String result = bashScriptExecutor
                .executeBashScript("src/test/resources/scripts/" + "checkIfPublishedOpensslPackageCanBeInstalled.sh")
                .output();
        assertTrue(result.contains("* DONE (openssl)"), "Package was not installed correctly.");
        assertTrue(result.contains("Linking to: OpenSSL"), "Package could not be loaded.");
    }

    @Test
    public void installBinaryPackage() throws Exception {
        prepBigTest();
        final String result = bashScriptExecutor
                .executeBashScript("src/test/resources/scripts/" + "checkIfPublishedCarPackageCanBeInstalled.sh")
                .output();
        System.out.println("Result is:");
        System.out.println(result);
        assertTrue(
                result.contains("* installing *binary* package ‘car’ ..."),
                "Source package was installed instead of binary package.");
        assertTrue(result.contains("* DONE (car)"), "Package was not installed correctly.");
        assertFalse(result.contains("Error"), "Package could not be loaded.");
    }

    @Test
    public void listBigPopulatedRepository() throws Exception {
        prepBigTest();

        final String expectedSource =
                """
                        01bbc75fd8fa20dc04e24a92a5dc5d6d  /opt/rdepot/my-test-repo-123/src/contrib/Archive/LaMa/LaMa_2.0.2.tar.gz
                        07f573114830f8a7668dfd6509b195c3  /opt/rdepot/my-test-repo-123/src/contrib/LaMa_2.1.0.tar.gz
                        0f588d9575328e12c15981cbdc4e76ee  /opt/rdepot/my-test-repo-123/src/contrib/openssl_2.3.4.tar.gz
                        13b9c4b5f1e9215fa6426ba8f4d8d014  /opt/rdepot/my-test-repo-123/src/contrib/robust.prioritizr_1.0.3.tar.gz
                        1fde767f383722858484687b81d02d04  /opt/rdepot/my-test-repo-123/src/contrib/Archive/matchingR/matchingR_1.3.2.tar.gz
                        2170aaa76b9691c201a3cff4a42634ee  /opt/rdepot/my-test-repo-123/src/contrib/Archive/LaMa/LaMa_2.0.0.tar.gz
                        29798f5844f2cde1e12c4ca34dd2920d  /opt/rdepot/my-test-repo-123/src/contrib/Archive/metrica/metrica_1.2.3.tar.gz
                        2a7d165f1f89db913711c66457f4435a  /opt/rdepot/my-test-repo-123/src/contrib/Archive/LaMa/LaMa_2.0.1.tar.gz
                        2c8e0f331fdb3d89997d2c6ba442de38  /opt/rdepot/my-test-repo-123/src/contrib/rush_1.0.0.tar.gz
                        2ccae817cb0d1c6fbf1e7b08fc3766f7  /opt/rdepot/my-test-repo-123/src/contrib/Archive/LaMa/LaMa_1.0.0.tar.gz
                        3364c4f61981a493181be0fd7eb34753  /opt/rdepot/my-test-repo-123/src/contrib/cards_0.7.0.tar.gz
                        3ad347cffcf24b2bfd0d935fd353cdde  /opt/rdepot/my-test-repo-123/src/contrib/Archive/matchingR/index.html
                        3d2a439923ae3e4e2a6d20701b4b7bee  /opt/rdepot/my-test-repo-123/src/contrib/Archive/metrica/metrica_2.0.0.tar.gz
                        43b2727e11498d4d97cea24df823564a  /opt/rdepot/my-test-repo-123/src/contrib/Archive/metrica/index.html
                        4aa19b8fdb61dc19110b4b7ab7f64646  /opt/rdepot/my-test-repo-123/src/contrib/rlandfire_2.0.2.tar.gz
                        4fe29791c23247b2e94e8612ba9cb179  /opt/rdepot/my-test-repo-123/src/contrib/Archive/PACKAGES.gz
                        566169ec8431469f5daf6dd491a43644  /opt/rdepot/my-test-repo-123/src/contrib/Archive/index.html
                        57e9d6f827a02752a92cdf512b86d33b  /opt/rdepot/my-test-repo-123/src/contrib/PACKAGES.gz
                        5b75c427ef3491b967056db2ed2b31d8  /opt/rdepot/my-test-repo-123/src/contrib/matchingR_2.0.0.tar.gz
                        6154914d100cc961563131016caaffa4  /opt/rdepot/my-test-repo-123/src/contrib/extr_1.1.0.tar.gz
                        63eef62ee5d82e101930d582dfd10328  /opt/rdepot/my-test-repo-123/src/contrib/Archive/LaMa/index.html
                        6daafbe7ebf181c90dab400495e952c1  /opt/rdepot/my-test-repo-123/src/contrib/index.html
                        776971741ada234042f43013d7132461  /opt/rdepot/my-test-repo-123/src/contrib/Archive/openssl/openssl_0.8.tar.gz
                        a00115e84f0d05576b4e685988b5d126  /opt/rdepot/my-test-repo-123/src/contrib/metrica_2.1.1.tar.gz
                        beff812b6842cd02d019fd3df0733625  /opt/rdepot/my-test-repo-123/src/contrib/Archive/openssl/openssl_1.4.4.tar.gz
                        c41077a594f21307cb7cc188295467c2  /opt/rdepot/my-test-repo-123/src/contrib/RedeAgroRadar_0.1.1.tar.gz
                        ca1fae4abbf0f198722de1516a4188e6  /opt/rdepot/my-test-repo-123/src/contrib/Archive/PACKAGES
                        d1d652b13edd512bb1e5d8518049d9e6  /opt/rdepot/my-test-repo-123/src/contrib/Archive/matchingR/matchingR_1.3.0.tar.gz
                        d2a7533cfd9329b008df1b31a0f311de  /opt/rdepot/my-test-repo-123/src/contrib/WARDEN_2.0.2.tar.gz
                        dd035715845b314fdff50d105687025f  /opt/rdepot/my-test-repo-123/src/contrib/PACKAGES
                        eaae55a609e2dc8eeec77b817c7d0846  /opt/rdepot/my-test-repo-123/src/contrib/fmtr_1.7.3.tar.gz
                        f0ef7be76b633f882c05fa8a75704665  /opt/rdepot/my-test-repo-123/src/contrib/Archive/openssl/index.html
                        f80620917e994e915de672affa0ae0f2  /opt/rdepot/my-test-repo-123/src/contrib/Archive/metrica/metrica_2.0.1.tar.gz""";
        final String expectedBinary =
                """
                        0f1186a8055c7ae5192212afac72098f  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/ggplot2/ggplot2_3.5.2.tar.gz
                        15cc399ddefd598a67fe1113f032aa8c  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/PACKAGES.gz
                        307713fd93370729963aec37dc5659d2  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/PACKAGES
                        34e8a3546fe4b36ce466a73407122b96  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/ggplot2/ggplot2_3.5.1.tar.gz
                        394d591ade1e0d4ddbb9405e515e8df8  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/metrica/metrica_2.0.0.tar.gz
                        4938cc76404bf307dadc05e9e8b70b28  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/PACKAGES
                        5c348238049ce0ed38311d915aa57634  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/fmtr_1.7.3.tar.gz
                        6efe0ee1d8de8b676fb6ac8b94f0ee40  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/ggplot2_4.0.0.tar.gz
                        747ad46f39c7b83a67643b9da59b6c37  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/ggplot2/index.html
                        775ac578824ff09c9cad8e15419cd309  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/car/car_2.1-5.tar.gz
                        78f33955a28ca818639efd5be1528e4c  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/car/index.html
                        790fc873116ea6b21eb3a8848240e8bb  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/car_3.1-3.tar.gz
                        7b2cc1cc0e73c9400bf894e4e9fec389  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/metrica/metrica_1.2.3.tar.gz
                        877a9f6f0fc04ef8e62c179cc465af71  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/index.html
                        a5753d54a3de628d7357abe3d5375ad9  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/metrica/metrica_2.0.1.tar.gz
                        a607c387c7b97823c6927149d65ef871  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/metrica/index.html
                        ac54cd5a5a6c2133ad5b641d3708b338  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/LaMa_1.0.0.tar.gz
                        b870f0c2a52d8942879f25eca96dcf15  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/index.html
                        d7bbf308437dcc8a16d5af7d393132b1  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/PACKAGES.gz
                        e51b4cef2eec3984effe103535cd6feb  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/metrica_2.1.1.tar.gz
                        e53accdb1a464919f6331a9a0e1cd7bb  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/WARDEN_2.0.2.tar.gz""";

        final String actualSource = bashScriptExecutor
                .executeBashScript("src/test/resources/scripts/listSourcePackages.sh")
                .output();
        final String actualBinary = bashScriptExecutor
                .executeBashScript("src/test/resources/scripts/listBinaryPackages.sh")
                .output();
        assertEquals(expectedSource, actualSource, "Incorrect packages in /src/contrib directory.");
        assertEquals(expectedBinary, actualBinary, "Incorrect binary in /bin directory.");
    }

    @Test
    public void listPopulatedRepository() throws Exception {
        prepTest();
        final String expectedSource =
                """
                        0f588d9575328e12c15981cbdc4e76ee  /opt/rdepot/my-test-repo-123/src/contrib/openssl_2.3.4.tar.gz
                        1ca7b5bf2e86c8645567aa3e6ef71542  /opt/rdepot/my-test-repo-123/src/contrib/Archive/index.html
                        1fde767f383722858484687b81d02d04  /opt/rdepot/my-test-repo-123/src/contrib/Archive/matchingR/matchingR_1.3.2.tar.gz
                        28d9b0cc1cd4213cbbb22e7a33baddc8  /opt/rdepot/my-test-repo-123/src/contrib/Archive/matchingR/index.html
                        3364c4f61981a493181be0fd7eb34753  /opt/rdepot/my-test-repo-123/src/contrib/cards_0.7.0.tar.gz
                        34297a00eba60495678795f0d24fcfb0  /opt/rdepot/my-test-repo-123/src/contrib/PACKAGES
                        5b75c427ef3491b967056db2ed2b31d8  /opt/rdepot/my-test-repo-123/src/contrib/matchingR_2.0.0.tar.gz
                        71e871e8a07cc2a781237ee4f1f3829e  /opt/rdepot/my-test-repo-123/src/contrib/index.html
                        776971741ada234042f43013d7132461  /opt/rdepot/my-test-repo-123/src/contrib/Archive/openssl/openssl_0.8.tar.gz
                        7ea49d61d3da936505aa9985883bc09c  /opt/rdepot/my-test-repo-123/src/contrib/Archive/openssl/index.html
                        964f69ae8fe8bee05df189c3a68f1c98  /opt/rdepot/my-test-repo-123/src/contrib/Archive/PACKAGES
                        beff812b6842cd02d019fd3df0733625  /opt/rdepot/my-test-repo-123/src/contrib/Archive/openssl/openssl_1.4.4.tar.gz
                        d1d652b13edd512bb1e5d8518049d9e6  /opt/rdepot/my-test-repo-123/src/contrib/Archive/matchingR/matchingR_1.3.0.tar.gz
                        e0a0ad7ff389013782d2506ba8eb6544  /opt/rdepot/my-test-repo-123/src/contrib/PACKAGES.gz
                        eddaa7f7c8f8a452977f2d6fed543761  /opt/rdepot/my-test-repo-123/src/contrib/Archive/PACKAGES.gz""";
        final String expectedBinary =
                """
                        0f1186a8055c7ae5192212afac72098f  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/ggplot2/ggplot2_3.5.2.tar.gz
                        14d7742969ec64894a7372f6fab9e0ef  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/PACKAGES
                        226cecf866538e7a7e61e12431b7151d  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/car/index.html
                        34e8a3546fe4b36ce466a73407122b96  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/ggplot2/ggplot2_3.5.1.tar.gz
                        387f12b3d8575de692765c212257f9b5  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/index.html
                        3e4d7d98521b3ee1dda08a37fb60f84e  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/PACKAGES.gz
                        64d6834335f21aced6ff76d16c594665  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/ggplot2/index.html
                        6ed5bfc0ba8a1c12c8a3f53ee87f6b70  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/PACKAGES
                        6efe0ee1d8de8b676fb6ac8b94f0ee40  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/ggplot2_4.0.0.tar.gz
                        775ac578824ff09c9cad8e15419cd309  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/car/car_2.1-5.tar.gz
                        790fc873116ea6b21eb3a8848240e8bb  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/car_3.1-3.tar.gz
                        a900f2439df196ef40ba9404abf4af75  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/index.html
                        ad4794e3958667fcad88714399f6f67c  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/PACKAGES.gz""";

        final String actualSource = bashScriptExecutor
                .executeBashScript("src/test/resources/scripts/listSourcePackages.sh")
                .output();
        final String actualBinary = bashScriptExecutor
                .executeBashScript("src/test/resources/scripts/listBinaryPackages.sh")
                .output();
        assertEquals(expectedSource, actualSource, "Incorrect packages in /src/contrib directory.");
        assertEquals(expectedBinary, actualBinary, "Incorrect binary in /bin directory.");
    }

    @Test
    public void deleteAllPackagesAndRepublish() throws Exception {
        prepTest();
        unpublishRepo();

        for (int i = 48; i <= 59; i++) {
            given().contentType("application/json-patch+json")
                    .accept(ContentType.JSON)
                    .header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                    .when()
                    .body("[{\"op\": \"replace\",\"path\":\"/deleted\",\"value\": true}]")
                    .patch(PACKAGES_ENDPOINT + "/" + i)
                    .then()
                    .statusCode(HttpStatus.SC_OK);
        }
        publishRepo();
        final String expectedSource =
                """
                        10d1bf3cecf014071198090b66ade212  /opt/rdepot/my-test-repo-123/src/contrib/index.html
                        163be0a88c70ca629fd516dbaadad96a  /opt/rdepot/my-test-repo-123/src/contrib/Archive/PACKAGES.gz
                        163be0a88c70ca629fd516dbaadad96a  /opt/rdepot/my-test-repo-123/src/contrib/PACKAGES.gz
                        ae27bacc625c3a6333fa4aae7c08138e  /opt/rdepot/my-test-repo-123/src/contrib/Archive/index.html
                        d41d8cd98f00b204e9800998ecf8427e  /opt/rdepot/my-test-repo-123/src/contrib/Archive/PACKAGES
                        d41d8cd98f00b204e9800998ecf8427e  /opt/rdepot/my-test-repo-123/src/contrib/PACKAGES""";
        final String expectedBinary =
                """
                        10d1bf3cecf014071198090b66ade212  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/index.html
                        163be0a88c70ca629fd516dbaadad96a  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/PACKAGES.gz
                        163be0a88c70ca629fd516dbaadad96a  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/PACKAGES.gz
                        ae27bacc625c3a6333fa4aae7c08138e  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/index.html
                        d41d8cd98f00b204e9800998ecf8427e  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/PACKAGES
                        d41d8cd98f00b204e9800998ecf8427e  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/PACKAGES""";
        final String actualSource = bashScriptExecutor
                .executeBashScript("src/test/resources/scripts/listSourcePackages.sh")
                .output();
        final String actualBinary = bashScriptExecutor
                .executeBashScript("src/test/resources/scripts/listBinaryPackages.sh")
                .output();
        assertEquals(expectedSource, actualSource, "Incorrect packages in /src/contrib directory.");
        assertEquals(expectedBinary, actualBinary, "Incorrect binary in /bin directory.");
    }

    @Test
    public void deleteLatestPackage_latestArchivedVersionShouldBeInLatestDirectory() throws Exception {
        prepTest();
        unpublishRepo();
        given().contentType("application/json-patch+json")
                .accept(ContentType.JSON)
                .header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                .when()
                .body("[{\"op\": \"replace\",\"path\":\"/deleted\",\"value\": true}]")
                .patch(PACKAGES_ENDPOINT + "/55")
                .then()
                .statusCode(HttpStatus.SC_OK);
        given().contentType("application/json-patch+json")
                .accept(ContentType.JSON)
                .header(AUTHORIZATION, BASIC + ADMIN_TOKEN)
                .when()
                .body("[{\"op\": \"replace\",\"path\":\"/deleted\",\"value\": true}]")
                .patch(PACKAGES_ENDPOINT + "/50")
                .then()
                .statusCode(HttpStatus.SC_OK);
        publishRepo();

        final String expectedSource =
                """
                        1a2b57200dc394d37ac9487c73ba4f3b  /opt/rdepot/my-test-repo-123/src/contrib/Archive/index.html
                        1fde767f383722858484687b81d02d04  /opt/rdepot/my-test-repo-123/src/contrib/Archive/matchingR/matchingR_1.3.2.tar.gz
                        2aa7e44d7864e635b717b756b626512e  /opt/rdepot/my-test-repo-123/src/contrib/index.html
                        3364c4f61981a493181be0fd7eb34753  /opt/rdepot/my-test-repo-123/src/contrib/cards_0.7.0.tar.gz
                        5a093810e84fe6ba260d9559349fbec7  /opt/rdepot/my-test-repo-123/src/contrib/Archive/matchingR/index.html
                        5b75c427ef3491b967056db2ed2b31d8  /opt/rdepot/my-test-repo-123/src/contrib/matchingR_2.0.0.tar.gz
                        776971741ada234042f43013d7132461  /opt/rdepot/my-test-repo-123/src/contrib/Archive/openssl/openssl_0.8.tar.gz
                        88182949a8ef358c1cacd6adb483747b  /opt/rdepot/my-test-repo-123/src/contrib/Archive/openssl/index.html
                        889cc64a7e340077a47a289544c1fb22  /opt/rdepot/my-test-repo-123/src/contrib/PACKAGES.gz
                        9dbfb396c651f8738072378f9d563cf5  /opt/rdepot/my-test-repo-123/src/contrib/PACKAGES
                        ac08bb93f07b57d0b93502ad7d9268fb  /opt/rdepot/my-test-repo-123/src/contrib/Archive/PACKAGES.gz
                        beff812b6842cd02d019fd3df0733625  /opt/rdepot/my-test-repo-123/src/contrib/openssl_1.4.4.tar.gz
                        ce5248362b22762e9d2a76407098ddba  /opt/rdepot/my-test-repo-123/src/contrib/Archive/PACKAGES
                        d1d652b13edd512bb1e5d8518049d9e6  /opt/rdepot/my-test-repo-123/src/contrib/Archive/matchingR/matchingR_1.3.0.tar.gz""";
        final String expectedBinary =
                """
                        096405945094bbe46031333f38d9a45b  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/index.html
                        0f1186a8055c7ae5192212afac72098f  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/ggplot2/ggplot2_3.5.2.tar.gz
                        3393478fb81ae08697aad7b0f18ec5f1  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/ggplot2/index.html
                        34e8a3546fe4b36ce466a73407122b96  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/ggplot2/ggplot2_3.5.1.tar.gz
                        4c1c4a0459e18c619d7d8bf0e62cd217  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/index.html
                        505827eb93eab2c5d4f614c3813cd508  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/PACKAGES
                        6efe0ee1d8de8b676fb6ac8b94f0ee40  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/ggplot2_4.0.0.tar.gz
                        775ac578824ff09c9cad8e15419cd309  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/car_2.1-5.tar.gz
                        cd3d0ae879bb6a8033cfe7c150f9591f  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/PACKAGES.gz
                        d6929a5796b367b96b19248cfe4140b3  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/PACKAGES
                        e5524e574742a003017006cf5218a385  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/PACKAGES.gz""";

        final String actualSource = bashScriptExecutor
                .executeBashScript("src/test/resources/scripts/listSourcePackages.sh")
                .output();
        final String actualBinary = bashScriptExecutor
                .executeBashScript("src/test/resources/scripts/listBinaryPackages.sh")
                .output();
        assertEquals(expectedSource, actualSource, "Incorrect packages in /src/contrib directory.");
        assertEquals(expectedBinary, actualBinary, "Incorrect binary in /bin directory.");
    }
}

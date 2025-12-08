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
package eu.openanalytics.rdepot.integrationtest.manager.v2.r;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

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
            }

            static class Archive {
                static final File matchingR130 = new File(sourceArchivePackagesDirectory, "matchingR_1.3.0.tar.gz");
                static final File matchingR132 = new File(sourceArchivePackagesDirectory, "matchingR_1.3.2.tar.gz");
                static final File openssl08 = new File(sourceArchivePackagesDirectory, "openssl_0.8.tar.gz");
                static final File openssl144 = new File(sourceArchivePackagesDirectory, "openssl_1.4.4.tar.gz");
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
            }

            static class Archive {
                static final File car215 =
                        new File(binaryArchivePackagesDirectory, "car_2.1-5_R_x86_64-pc-linux-gnu.tar.gz");
                static final File ggplot2352 =
                        new File(binaryArchivePackagesDirectory, "ggplot2_3.5.2_R_x86_64-pc-linux-gnu.tar.gz");
                static final File ggplot2351 =
                        new File(binaryArchivePackagesDirectory, "ggplot2_3.5.1_R_x86_64-pc-linux-gnu.tar.gz");
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
        prepTest();
        final String result = bashScriptExecutor
                .executeBashScript(
                        "src/test/resources/scripts/" + "checkIfPublishedArchivedOpensslPackageCanBeInstalled.sh")
                .output();
        assertTrue(result.contains("* DONE (openssl)"), "Package was not installed correctly.");
        assertTrue(result.contains("Linking to: OpenSSL"), "Package could not be loaded.");
    }

    @Test
    public void installBinaryPackageFromArchive() throws Exception {
        prepTest();
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
        prepTest();
        final String result = bashScriptExecutor
                .executeBashScript("src/test/resources/scripts/" + "checkIfPublishedOpensslPackageCanBeInstalled.sh")
                .output();
        assertTrue(result.contains("* DONE (openssl)"), "Package was not installed correctly.");
        assertTrue(result.contains("Linking to: OpenSSL"), "Package could not be loaded.");
    }

    @Test
    public void installBinaryPackage() throws Exception {
        prepTest();
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
    public void listPopulatedRepository() throws Exception {
        prepTest();
        final String expectedSource =
                """
                        0f588d9575328e12c15981cbdc4e76ee  /opt/rdepot/my-test-repo-123/src/contrib/openssl_2.3.4.tar.gz
                        121e3ec6e040505670b2f5bafa9b88a5  /opt/rdepot/my-test-repo-123/src/contrib/PACKAGES.gz
                        151632666bb538dbe241f77fe9cb044d  /opt/rdepot/my-test-repo-123/src/contrib/Archive/index.html
                        1fde767f383722858484687b81d02d04  /opt/rdepot/my-test-repo-123/src/contrib/Archive/matchingR/matchingR_1.3.2.tar.gz
                        3364c4f61981a493181be0fd7eb34753  /opt/rdepot/my-test-repo-123/src/contrib/cards_0.7.0.tar.gz
                        38dbc0b354e36694ca01bb8c5e975bd6  /opt/rdepot/my-test-repo-123/src/contrib/Archive/PACKAGES
                        5b75c427ef3491b967056db2ed2b31d8  /opt/rdepot/my-test-repo-123/src/contrib/matchingR_2.0.0.tar.gz
                        5c7178042e319a8ebe654605775f1dcd  /opt/rdepot/my-test-repo-123/src/contrib/Archive/openssl/index.html
                        65710388fdf2104f8b87ada255bb921d  /opt/rdepot/my-test-repo-123/src/contrib/Archive/PACKAGES.gz
                        776971741ada234042f43013d7132461  /opt/rdepot/my-test-repo-123/src/contrib/Archive/openssl/openssl_0.8.tar.gz
                        beff812b6842cd02d019fd3df0733625  /opt/rdepot/my-test-repo-123/src/contrib/Archive/openssl/openssl_1.4.4.tar.gz
                        d1d652b13edd512bb1e5d8518049d9e6  /opt/rdepot/my-test-repo-123/src/contrib/Archive/matchingR/matchingR_1.3.0.tar.gz
                        dd6d2a7e49858711f99e650006764a7f  /opt/rdepot/my-test-repo-123/src/contrib/PACKAGES
                        ed0af3271393feac42aa4158c75fca3c  /opt/rdepot/my-test-repo-123/src/contrib/index.html
                        f884a54d426b00121594ec087e649950  /opt/rdepot/my-test-repo-123/src/contrib/Archive/matchingR/index.html""";
        final String expectedBinary =
                """
                        0f1186a8055c7ae5192212afac72098f  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/ggplot2/ggplot2_3.5.2.tar.gz
                        1e58863aed5b7aec948e446b8b6a1cc3  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/index.html
                        34e8a3546fe4b36ce466a73407122b96  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/ggplot2/ggplot2_3.5.1.tar.gz
                        43e5bd07a692b6db4504d19607d682ba  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/car/index.html
                        58c09eca454b79e0332e3d2f6c47ba52  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/PACKAGES.gz
                        6efe0ee1d8de8b676fb6ac8b94f0ee40  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/ggplot2_4.0.0.tar.gz
                        775ac578824ff09c9cad8e15419cd309  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/car/car_2.1-5.tar.gz
                        790fc873116ea6b21eb3a8848240e8bb  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/car_3.1-3.tar.gz
                        7cf88acde7e41f219c8a79e97ae35409  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/PACKAGES
                        8462b610ef6b4561218db6f3da42dcd9  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/ggplot2/index.html
                        a2be14b1d5cf8e66048b405f9188fac5  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/PACKAGES.gz
                        b84a2fa64733e376e2d6aaa34a0c74a3  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/index.html
                        fe48e1028314f7a6e5885d540b27e75b  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/PACKAGES""";

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
                        163be0a88c70ca629fd516dbaadad96a  /opt/rdepot/my-test-repo-123/src/contrib/Archive/PACKAGES.gz
                        163be0a88c70ca629fd516dbaadad96a  /opt/rdepot/my-test-repo-123/src/contrib/PACKAGES.gz
                        69b09cffa19fd6647c4933644bb74e35  /opt/rdepot/my-test-repo-123/src/contrib/index.html
                        883016ae2f777469ef9794a692cf4db4  /opt/rdepot/my-test-repo-123/src/contrib/Archive/index.html
                        d41d8cd98f00b204e9800998ecf8427e  /opt/rdepot/my-test-repo-123/src/contrib/Archive/PACKAGES
                        d41d8cd98f00b204e9800998ecf8427e  /opt/rdepot/my-test-repo-123/src/contrib/PACKAGES""";
        final String expectedBinary =
                """
                        163be0a88c70ca629fd516dbaadad96a  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/PACKAGES.gz
                        163be0a88c70ca629fd516dbaadad96a  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/PACKAGES.gz
                        69b09cffa19fd6647c4933644bb74e35  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/index.html
                        883016ae2f777469ef9794a692cf4db4  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/index.html
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
                        1fde767f383722858484687b81d02d04  /opt/rdepot/my-test-repo-123/src/contrib/Archive/matchingR/matchingR_1.3.2.tar.gz
                        2ac1f50ba71a42f6ca5a6ba57db180a1  /opt/rdepot/my-test-repo-123/src/contrib/Archive/matchingR/index.html
                        3364c4f61981a493181be0fd7eb34753  /opt/rdepot/my-test-repo-123/src/contrib/cards_0.7.0.tar.gz
                        4f9457af18e4d17d794da50c416d5005  /opt/rdepot/my-test-repo-123/src/contrib/Archive/PACKAGES.gz
                        5b75c427ef3491b967056db2ed2b31d8  /opt/rdepot/my-test-repo-123/src/contrib/matchingR_2.0.0.tar.gz
                        6e722414615f388c8bc8b8c5fae74f0f  /opt/rdepot/my-test-repo-123/src/contrib/index.html
                        75894b99dbb21546421c835c55bcd50d  /opt/rdepot/my-test-repo-123/src/contrib/Archive/index.html
                        776971741ada234042f43013d7132461  /opt/rdepot/my-test-repo-123/src/contrib/Archive/openssl/openssl_0.8.tar.gz
                        a7a484e0e6894ebc5d3cf903d9e35900  /opt/rdepot/my-test-repo-123/src/contrib/Archive/openssl/index.html
                        bc988cb79e865a65c57888025af6489d  /opt/rdepot/my-test-repo-123/src/contrib/PACKAGES.gz
                        beff812b6842cd02d019fd3df0733625  /opt/rdepot/my-test-repo-123/src/contrib/openssl_1.4.4.tar.gz
                        cc3c418f26eb579be99e8e5b9fec8444  /opt/rdepot/my-test-repo-123/src/contrib/PACKAGES
                        cdd194fb7e2765b64104ffc9daa50c40  /opt/rdepot/my-test-repo-123/src/contrib/Archive/PACKAGES
                        d1d652b13edd512bb1e5d8518049d9e6  /opt/rdepot/my-test-repo-123/src/contrib/Archive/matchingR/matchingR_1.3.0.tar.gz""";
        final String expectedBinary =
                """
                        0f1186a8055c7ae5192212afac72098f  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/ggplot2/ggplot2_3.5.2.tar.gz
                        34e8a3546fe4b36ce466a73407122b96  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/ggplot2/ggplot2_3.5.1.tar.gz
                        3d1790e06f4f9bcbd615f9ff7e8f2fcf  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/PACKAGES
                        428dc93e2775c709d3a2deecaa504527  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/index.html
                        6dda99d34e3dc04492c55591178017f9  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/PACKAGES.gz
                        6efe0ee1d8de8b676fb6ac8b94f0ee40  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/ggplot2_4.0.0.tar.gz
                        775ac578824ff09c9cad8e15419cd309  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/car_2.1-5.tar.gz
                        982de8ade2dbf2fa37948c7605d0c4f2  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/ggplot2/index.html
                        aa573b8efa47cbec9f447787df3ecc5c  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/PACKAGES
                        c762f89648370e790856a0c6e9ece0b4  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/index.html
                        db6485cd49a4f9d2cf697b466ba596de  /opt/rdepot/my-test-repo-123/bin/linux/centos7/x86_64/4.5/Archive/PACKAGES.gz""";

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

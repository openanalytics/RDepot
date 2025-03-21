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
package eu.openanalytics.rdepot.test.fixture;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageDto;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.time.DateProvider;
import eu.openanalytics.rdepot.r.entities.RPackage;
import eu.openanalytics.rdepot.r.entities.RRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

public class RPackageTestFixture {
    public static final String NAME = "TestPackage";
    public static final String DESCRIPTION = "Simple test package";
    public static final String AUTHOR = "Albert Einstein";
    public static final String LICENSE = "Some license";
    public static final String SOURCE = "test_package.tar.gz";
    public static final String TITLE = "Test Package";
    public static final String MD5SUM = "1234567";
    public static final String VERSION = "4.5.6";
    public static final Boolean ACTIVATED = true;
    public static final Boolean DELETED = false;
    public static final Boolean BINARY = false;

    public static List<RPackage> GET_FIXTURE_PACKAGES(
            RRepository repository, User user, int packageCount, int idShift) {
        List<RPackage> packages = new ArrayList<>();

        for (int i = idShift; i < packageCount + idShift; i++) {
            RPackage packageBag = new RPackage(
                    i,
                    repository,
                    user,
                    NAME + i,
                    DESCRIPTION + i,
                    AUTHOR + i,
                    LICENSE + i,
                    i + SOURCE,
                    TITLE + i,
                    MD5SUM + i,
                    ACTIVATED,
                    DELETED,
                    BINARY);

            packageBag.setVersion(VERSION);
            packageBag.setGenerateManuals(false);
            Submission submission = RSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
            submission.setId(i);
            submission.setCreatedDate(DateProvider.now());
            packageBag.setSubmission(submission);
            packages.add(packageBag);
        }

        return packages;
    }

    public static RPackage GET_FIXTURE_PACKAGE(RRepository repository, User user, Integer id) {
        return GET_FIXTURE_PACKAGES(repository, user, 1, id).get(0);
    }

    public static RPackage GET_FIXTURE_PACKAGE(RRepository repository, User user) {
        return GET_FIXTURE_PACKAGE(repository, user, 1);
    }

    public static RPackage GET_FIXTURE_BINARY_PACKAGE(RRepository repository, User user) {
        RPackage binaryPackage = GET_FIXTURE_PACKAGE(repository, user);
        binaryPackage.setBinary(true);
        binaryPackage.setBuilt("R 4.2; x86_64-pc-linux-gnu; 2022-06-07 00:49:30 UTC; unix");
        binaryPackage.setRVersion("4.2");
        binaryPackage.setArchitecture("x86_64");
        binaryPackage.setDistribution("centos7");

        Submission submission = RSubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, binaryPackage);
        submission.setId(binaryPackage.getSubmission().getId());
        submission.setCreatedDate(DateProvider.now());
        binaryPackage.setSubmission(submission);
        return binaryPackage;
    }

    public static Page<RPackage> GET_EXAMPLE_PACKAGES_PAGED(RRepository repository, User user) {
        return new PageImpl<>(GET_FIXTURE_PACKAGES(repository, user, 3, 100));
    }

    public static Page<RPackage> GET_EXAMPLE_PACKAGES_PAGED_DELETED() {
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        List<RPackage> packages = GET_FIXTURE_PACKAGES(repository, user, 3, 100);
        packages.forEach(p -> p.setDeleted(true));

        return new PageImpl<>(packages);
    }

    public static RPackage GET_EXAMPLE_PACKAGE() {
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        return GET_FIXTURE_PACKAGES(repository, user, 3, 100).get(0);
    }

    public static PackageDto GET_EXAMPLE_PACKAGE_DTO(Package packageBag) {
        return new PackageDto(packageBag);
    }

    public static List<PackageDto> GET_EXAMPLE_PACKAGE_DTOS(List<Submission> submissions) {
        List<PackageDto> packageDtos = new ArrayList<>();
        submissions.forEach(submission -> packageDtos.add(new PackageDto(submission.getPackageBag())));
        return packageDtos;
    }

    public static PackageDto GET_EXAMPLE_PACKAGE_DTO() {
        RRepository repository = RRepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        RPackage packageBag = GET_FIXTURE_PACKAGES(repository, user, 3, 100).get(0);

        return new PackageDto(packageBag);
    }

    /**
     * Returns the following packages:
     * <ol>
     *   <li>"Abc" version: "1.0.0"</li>
     *   <li>"Abc" version: "1.0.1"</li>
     *   <li>"Abc" version: "1.2.3"</li>
     *   <li>"X-Y-Z" version: "0.0.1"</li>
     *   <li>"X-Y-Z" version: "0.0.3"</li>
     *   <li>"D E F" version: "0.0.3"</li>
     * </ol>
     * @param repository
     * @param user
     * @return
     */
    public static List<RPackage> GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE(
            RRepository repository, User user) {
        final ArrayList<RPackage> packages = new ArrayList<>();

        // Package ABC
        final String PACKAGE_NAME_ABC = "Abc";
        final RPackage abcVersion1 = GET_FIXTURE_PACKAGE(repository, user, 1);
        abcVersion1.setName(PACKAGE_NAME_ABC);
        abcVersion1.setVersion("1.0.0");
        final RPackage abcVersion2 = GET_FIXTURE_PACKAGE(repository, user, 2);
        abcVersion2.setName(PACKAGE_NAME_ABC);
        abcVersion2.setVersion("1.0.1");
        final RPackage abcLatest = GET_FIXTURE_PACKAGE(repository, user, 3);
        abcLatest.setName(PACKAGE_NAME_ABC);
        abcLatest.setVersion("1.2.3");

        packages.add(abcVersion1);
        packages.add(abcVersion2);
        packages.add(abcLatest);

        // Package XYZ
        final String PACKAGE_NAME_XYZ = "X-Y-Z";
        final RPackage xyzVersion1 = GET_FIXTURE_PACKAGE(repository, user, 4);
        xyzVersion1.setName(PACKAGE_NAME_XYZ);
        xyzVersion1.setVersion("0.0.1");

        final RPackage xyzLatest = GET_FIXTURE_PACKAGE(repository, user, 5);
        xyzLatest.setName(PACKAGE_NAME_XYZ);
        xyzLatest.setVersion("0.0.3");

        packages.add(xyzVersion1);
        packages.add(xyzLatest);

        // Package DEF
        final String PACKAGE_NAME_DEF = "D E F";
        final RPackage defLatest = GET_FIXTURE_PACKAGE(repository, user, 6);
        defLatest.setName(PACKAGE_NAME_DEF);
        defLatest.setVersion("3.2.1");
        packages.add(defLatest);

        return packages;
    }

    public static List<RPackage> GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE_ONLY_LATEST(
            RRepository repository, User user) {
        final ArrayList<RPackage> packages = new ArrayList<>();

        // Package ABC
        final String PACKAGE_NAME_ABC = "Abc";
        final RPackage abcLatest = GET_FIXTURE_PACKAGE(repository, user, 3);
        abcLatest.setName(PACKAGE_NAME_ABC);
        abcLatest.setVersion("1.2.3");

        packages.add(abcLatest);

        // Package XYZ
        final String PACKAGE_NAME_XYZ = "X-Y-Z";
        final RPackage xyzLatest = GET_FIXTURE_PACKAGE(repository, user, 5);
        xyzLatest.setName(PACKAGE_NAME_XYZ);
        xyzLatest.setVersion("0.0.3");

        packages.add(xyzLatest);

        // Package DEF
        final String PACKAGE_NAME_DEF = "D E F";
        final RPackage defLatest = GET_FIXTURE_PACKAGE(repository, user, 6);
        defLatest.setName(PACKAGE_NAME_DEF);
        defLatest.setVersion("3.2.1");
        packages.add(defLatest);

        return packages;
    }

    public static List<RPackage> GET_BINARY_PACKAGES_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE(
            RRepository repository, User user) {
        final ArrayList<RPackage> packages = new ArrayList<>();

        final String PACKAGE_NAME_KLM = "Klm";
        final RPackage klmVersion1 = GET_FIXTURE_BINARY_PACKAGE(repository, user);
        klmVersion1.setName(PACKAGE_NAME_KLM);
        klmVersion1.setVersion("2.0.1");
        klmVersion1.setId(11);
        final RPackage klmVersion2 = GET_FIXTURE_BINARY_PACKAGE(repository, user);
        klmVersion2.setName(PACKAGE_NAME_KLM);
        klmVersion2.setVersion("2.1.0");
        klmVersion2.setId(12);
        final RPackage klmLatest = GET_FIXTURE_BINARY_PACKAGE(repository, user);
        klmLatest.setName(PACKAGE_NAME_KLM);
        klmLatest.setVersion("2.1.5");
        klmLatest.setId(13);

        packages.add(klmLatest);
        packages.add(klmVersion2);
        packages.add(klmVersion1);

        final String PACKAGE_NAME_PQR = "PQR";
        final RPackage pqrVersion1 = GET_FIXTURE_BINARY_PACKAGE(repository, user);
        pqrVersion1.setName(PACKAGE_NAME_PQR);
        pqrVersion1.setVersion("1.5");
        pqrVersion1.setId(14);
        final RPackage pqrLatest = GET_FIXTURE_BINARY_PACKAGE(repository, user);
        pqrLatest.setName(PACKAGE_NAME_PQR);
        pqrLatest.setVersion("1.6");
        pqrLatest.setId(15);

        packages.add(pqrLatest);
        packages.add(pqrVersion1);

        final String PACKAGE_NAME_BLA = "bla";
        final RPackage blaLatest = GET_FIXTURE_BINARY_PACKAGE(repository, user);
        blaLatest.setName(PACKAGE_NAME_BLA);
        blaLatest.setVersion("3.2.1");
        blaLatest.setId(16);

        packages.add(blaLatest);

        return packages;
    }

    public static List<RPackage> GET_BINARY_PACKAGES_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE_ONLY_LATEST(
            RRepository repository, User user) {
        final ArrayList<RPackage> packages = new ArrayList<>();

        final String PACKAGE_NAME_KLM = "Klm";
        final RPackage klmLatest = GET_FIXTURE_BINARY_PACKAGE(repository, user);
        klmLatest.setName(PACKAGE_NAME_KLM);
        klmLatest.setVersion("2.1.5");
        klmLatest.setId(13);

        packages.add(klmLatest);

        final String PACKAGE_NAME_PQR = "PQR";
        final RPackage pqrLatest = GET_FIXTURE_BINARY_PACKAGE(repository, user);
        pqrLatest.setName(PACKAGE_NAME_PQR);
        pqrLatest.setVersion("1.6");
        pqrLatest.setId(15);

        packages.add(pqrLatest);

        final String PACKAGE_NAME_BLA = "bla";
        final RPackage blaLatest = GET_FIXTURE_BINARY_PACKAGE(repository, user);
        blaLatest.setName(PACKAGE_NAME_BLA);
        blaLatest.setVersion("3.2.1");
        blaLatest.setId(16);

        packages.add(blaLatest);

        return packages;
    }

    /**
     *
     * @param names should follow the convention: id_packagename_1.2.3
     * @return
     */
    public static List<RPackage> GET_PACKAGES_FOR_MULTICHUNK_UPLOAD(
            RRepository repository, String sourcePrefix, String... names) {
        List<RPackage> packages = new ArrayList<>();

        User user = UserTestFixture.GET_ADMIN();

        for (String name : names) {
            String[] tokens = name.split("_");
            String id = tokens[0];
            String packageName = tokens[1];
            String version = tokens[2];

            RPackage packageBag = GET_FIXTURE_PACKAGE(repository, user);
            packageBag.setId(Integer.parseInt(id));
            packageBag.setName(packageName);
            packageBag.setAuthor("Author of package " + name);
            packageBag.setVersion(version);
            packageBag.setActive(true);
            packageBag.setDeleted(false);
            packageBag.setSource(sourcePrefix + "/" + packageName + "_" + version + ".tar.gz");
            packages.add(packageBag);
        }

        return packages;
    }
}

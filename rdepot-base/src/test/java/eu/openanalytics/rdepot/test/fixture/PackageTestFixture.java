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
package eu.openanalytics.rdepot.test.fixture;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageDto;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.time.DateProvider;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

public class PackageTestFixture {
    public static final String NAME = "TestPackage";
    public static final String DESCRIPTION = "Simple test package";
    public static final String AUTHOR = "Albert Einstein";
    public static final String SOURCE = "test_package.tar.gz";
    public static final String TITLE = "Test Package";
    public static final String VERSION = "4.5.6";
    public static final Boolean ACTIVATED = true;
    public static final Boolean DELETED = false;

    public static List<Package> GET_FIXTURE_PACKAGES(Repository repository, User user, int packageCount, int idShift) {
        List<Package> packages = new ArrayList<>();

        for (int i = idShift; i < packageCount + idShift; i++) {
            Package packageBag = new Package() {

                private static final long serialVersionUID = 1L;

                @Override
                public Repository getRepository() {
                    return repository;
                }
            };

            packageBag.setId(i);
            packageBag.setUser(user);
            packageBag.setName(NAME + Integer.toString(i));
            packageBag.setDescription(DESCRIPTION + Integer.toString(i));
            packageBag.setAuthor(AUTHOR + Integer.toString(i));
            packageBag.setSource(Integer.toString(i) + SOURCE);
            packageBag.setTitle(TITLE + Integer.toString(i));
            packageBag.setActive(ACTIVATED);
            packageBag.setDeleted(DELETED);
            packageBag.setVersion(VERSION);

            Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
            submission.setId(i);
            submission.setCreatedDate(DateProvider.getCurrentDate());
            packageBag.setSubmission(submission);
            packages.add(packageBag);
        }

        return packages;
    }

    public static Package GET_FIXTURE_PACKAGE(Repository repository, User user, Integer id) {
        return GET_FIXTURE_PACKAGES(repository, user, 1, id).get(0);
    }

    public static Package GET_FIXTURE_PACKAGE(Repository repository, User user) {
        return GET_FIXTURE_PACKAGE(repository, user, 1);
    }

    public static Page<Package> GET_EXAMPLE_PACKAGES_PAGED(Repository repository, User user) {
        return new PageImpl<>(GET_FIXTURE_PACKAGES(repository, user, 3, 100));
    }

    public static Page<Package> GET_EXAMPLE_PACKAGES_PAGED() {
        Repository repository = RepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        return GET_EXAMPLE_PACKAGES_PAGED(repository, user);
    }

    public static Page<Package> GET_EXAMPLE_PACKAGES_PAGED_DELETED() {
        Repository repository = RepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        List<Package> packages = GET_FIXTURE_PACKAGES(repository, user, 3, 100);
        packages.forEach(p -> p.setDeleted(true));

        return new PageImpl<>(packages);
    }

    public static Package GET_EXAMPLE_PACKAGE() {
        Repository repository = RepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        Package packageBag = GET_FIXTURE_PACKAGES(repository, user, 3, 100).get(0);

        return packageBag;
    }

    public static PackageDto GET_EXAMPLE_PACKAGE_DTO(Package packageBag) {
        PackageDto packageDto = new PackageDto(packageBag);
        return packageDto;
    }

    public static List<PackageDto> GET_EXAMPLE_PACKAGE_DTOS(List<Submission> submissions) {
        List<PackageDto> packageDtos = new ArrayList<PackageDto>();
        submissions.forEach(submission -> {
            packageDtos.add(new PackageDto(submission.getPackageBag()));
        });
        return packageDtos;
    }

    public static PackageDto GET_EXAMPLE_PACKAGE_DTO() {
        Repository repository = RepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        Package packageBag = GET_FIXTURE_PACKAGES(repository, user, 3, 100).get(0);

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
    public static List<Package> GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE(
            Repository repository, User user) {
        final ArrayList<Package> packages = new ArrayList<>();

        // Package ABC
        final String PACKAGE_NAME_ABC = "Abc";
        final Package abcVersion1 = GET_FIXTURE_PACKAGE(repository, user, 1);
        abcVersion1.setName(PACKAGE_NAME_ABC);
        abcVersion1.setVersion("1.0.0");
        final Package abcVersion2 = GET_FIXTURE_PACKAGE(repository, user, 2);
        abcVersion2.setName(PACKAGE_NAME_ABC);
        abcVersion2.setVersion("1.0.1");
        final Package abcLatest = GET_FIXTURE_PACKAGE(repository, user, 3);
        abcLatest.setName(PACKAGE_NAME_ABC);
        abcLatest.setVersion("1.2.3");

        packages.add(abcVersion1);
        packages.add(abcVersion2);
        packages.add(abcLatest);

        // Package XYZ
        final String PACKAGE_NAME_XYZ = "X-Y-Z";
        final Package xyzVersion1 = GET_FIXTURE_PACKAGE(repository, user, 4);
        xyzVersion1.setName(PACKAGE_NAME_XYZ);
        xyzVersion1.setVersion("0.0.1");

        final Package xyzLatest = GET_FIXTURE_PACKAGE(repository, user, 5);
        xyzLatest.setName(PACKAGE_NAME_XYZ);
        xyzLatest.setVersion("0.0.3");

        packages.add(xyzVersion1);
        packages.add(xyzLatest);

        // Package DEF
        final String PACKAGE_NAME_DEF = "D E F";
        final Package defLatest = GET_FIXTURE_PACKAGE(repository, user, 6);
        defLatest.setName(PACKAGE_NAME_DEF);
        defLatest.setVersion("3.2.1");
        packages.add(defLatest);

        return packages;
    }

    public static List<Package> GET_PACKAGES_WITH_MULTIPLE_VERSIONS_OF_THE_SAME_PACKAGE_ONLY_LATEST(
            Repository repository, User user) {
        final ArrayList<Package> packages = new ArrayList<>();

        // Package ABC
        final String PACKAGE_NAME_ABC = "Abc";
        final Package abcLatest = GET_FIXTURE_PACKAGE(repository, user, 1);
        abcLatest.setName(PACKAGE_NAME_ABC);
        abcLatest.setVersion("1.2.3");

        packages.add(abcLatest);

        // Package XYZ
        final String PACKAGE_NAME_XYZ = "X-Y-Z";
        final Package xyzLatest = GET_FIXTURE_PACKAGE(repository, user, 2);
        xyzLatest.setName(PACKAGE_NAME_XYZ);
        xyzLatest.setVersion("0.0.3");

        packages.add(xyzLatest);

        // Package DEF
        final String PACKAGE_NAME_DEF = "D E F";
        final Package defLatest = GET_FIXTURE_PACKAGE(repository, user, 3);
        defLatest.setName(PACKAGE_NAME_DEF);
        defLatest.setVersion("3.2.1");
        packages.add(defLatest);

        return packages;
    }

    /**
     *
     * @param names should follow the convention: id_packagename_1.2.3
     * @return
     */
    public static List<Package> GET_PACKAGES_FOR_MULTICHUNK_UPLOAD(
            Repository repository, String sourcePrefix, String... names) {
        List<Package> packages = new ArrayList<>();

        User user = UserTestFixture.GET_ADMIN();

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            String[] tokens = name.split("_");
            String id = tokens[0];
            String packageName = tokens[1];
            String version = tokens[2];

            Package packageBag = GET_FIXTURE_PACKAGE(repository, user);
            packageBag.setId(Integer.valueOf(id));
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

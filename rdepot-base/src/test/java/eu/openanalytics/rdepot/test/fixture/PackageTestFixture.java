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
package eu.openanalytics.rdepot.test.fixture;

import eu.openanalytics.rdepot.base.api.v2.dtos.PackageDto;
import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.Submission;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.base.time.DateProvider;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

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

                @Override
                public String getHash() {
                    return "fjkldsjflkdsjfreiotre4895734";
                }

                @Serial
                private static final long serialVersionUID = 1L;

                @Override
                public Repository getRepository() {
                    return repository;
                }
            };

            packageBag.setId(i);
            packageBag.setUser(user);
            packageBag.setName(NAME + i);
            packageBag.setDescription(DESCRIPTION + i);
            packageBag.setAuthor(AUTHOR + i);
            packageBag.setSource(i + SOURCE);
            packageBag.setTitle(TITLE + i);
            packageBag.setActive(ACTIVATED);
            packageBag.setDeleted(DELETED);
            packageBag.setVersion(VERSION);

            Submission submission = SubmissionTestFixture.GET_FIXTURE_SUBMISSION(user, packageBag);
            submission.setId(i);
            submission.setCreatedDate(DateProvider.now());
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

    public static Package GET_EXAMPLE_PACKAGE() {
        Repository repository = RepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        return GET_FIXTURE_PACKAGES(repository, user, 3, 100).get(0);
    }

    public static PackageDto GET_EXAMPLE_PACKAGE_DTO() {
        Repository repository = RepositoryTestFixture.GET_EXAMPLE_REPOSITORY();
        User user = UserTestFixture.GET_PACKAGE_MAINTAINER();

        Package packageBag = GET_FIXTURE_PACKAGES(repository, user, 3, 100).get(0);

        return new PackageDto(packageBag);
    }
}

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

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.User;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

public class PackageMaintainerTestFixture {
    public static final String PACKAGE_NAME = PackageTestFixture.NAME;
    public static final Boolean DELETED = false;

    public static List<PackageMaintainer> GET_EXAMPLE_PACKAGE_MAINTAINERS() {
        Repository repository = RepositoryTestFixture.GET_EXAMPLE_REPOSITORY(123);
        User user = UserTestFixture.GET_PACKAGE_MAINTAINER(111);

        List<PackageMaintainer> maintainers = new ArrayList<>();
        for (int i = 100; i < 103; i++) {
            PackageMaintainer maintainer = new PackageMaintainer(i, user, repository, PACKAGE_NAME + i, false);
            maintainers.add(maintainer);
        }

        maintainers.get(2).setDeleted(true);
        return maintainers;
    }

    public static PackageMaintainer GET_FIXTURE_PACKAGE_MAINTAINER() {
        return GET_EXAMPLE_PACKAGE_MAINTAINERS().get(0);
    }

    public static Page<PackageMaintainer> GET_EXAMPLE_PACKAGE_MAINTAINERS_PAGED() {
        return new PageImpl<PackageMaintainer>(GET_EXAMPLE_PACKAGE_MAINTAINERS());
    }

    public static PackageMaintainer GET_PACKAGE_MAINTAINER_FOR_REPOSITORY(int id, Repository repository) {
        PackageMaintainer maintainer = new PackageMaintainer();
        maintainer.setId(id);
        maintainer.setPackageName("test_package");
        maintainer.setRepository(repository);
        maintainer.setUser(UserTestFixture.GET_PACKAGE_MAINTAINER());
        return maintainer;
    }

    public static PackageMaintainer GET_PACKAGE_MAINTAINER_FOR_REPOSITORY(Repository repository) {
        return GET_PACKAGE_MAINTAINER_FOR_REPOSITORY(123, repository);
    }

    public static List<PackageMaintainer> GET_PACKAGE_MAINTAINERS_FOR_REPOSITORY(int count, Repository repository) {
        List<PackageMaintainer> maintainers = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            maintainers.add(GET_PACKAGE_MAINTAINER_FOR_REPOSITORY(123 + count, repository));
        }

        return maintainers;
    }

    /**
     * create a list of packages that will be used during tests as a packages that
     * needs to update package maintainer
     *
     * @param length
     * @param repository
     * @param user
     * @param packageName
     * @return
     */
    public static List<Package> GET_LIST_OF_PACKAGE_MAINTAINERS_FOR_REPOSITORY(
            int length, Repository repository, User user, String packageName) {

        List<Package> packageList = PackageTestFixture.GET_FIXTURE_PACKAGES(repository, user, length, 1);

        packageList.forEach(p -> p.setName(packageName));
        if (length > 0) {
            packageList.get(0).setName(packageName + "different_name");
        }
        return packageList.stream().map(p -> (Package) p).collect(Collectors.toList());
    }
}

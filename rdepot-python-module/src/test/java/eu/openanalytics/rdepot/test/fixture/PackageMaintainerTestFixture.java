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

import eu.openanalytics.rdepot.base.entities.Package;
import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import eu.openanalytics.rdepot.python.entities.PythonRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PackageMaintainerTestFixture {

    public static final String PACKAGE_NAME = PythonPackageTestFixture.NAME;

    public static List<PackageMaintainer> GET_EXAMPLE_PACKAGE_MAINTAINERS(PythonRepository repository) {
        User user = UserTestFixture.GET_PACKAGE_MAINTAINER(111);

        List<PackageMaintainer> maintainers = new ArrayList<>();
        for (int i = 100; i < 103; i++) {
            PackageMaintainer maintainer = new PackageMaintainer(i, user, repository, PACKAGE_NAME + i, false);
            maintainers.add(maintainer);
        }

        maintainers.get(2).setDeleted(true);
        return maintainers;
    }

    public static List<PackageMaintainer> GET_PACKAGE_MAINTAINERS_FOR_PACKAGE(Package packageBag) {
        PythonRepository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY(123);

        List<PackageMaintainer> maintainers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            User user = UserTestFixture.GET_PACKAGE_MAINTAINER(11 + i);

            PackageMaintainer maintainer = new PackageMaintainer(i, user, repository, packageBag.getName(), false);
            maintainer.setPackages(new HashSet<>(List.of(packageBag)));
            maintainers.add(maintainer);
        }

        maintainers.get(2).setDeleted(true);
        return maintainers;
    }
}

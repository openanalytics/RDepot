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

import eu.openanalytics.rdepot.base.entities.PackageMaintainer;
import eu.openanalytics.rdepot.r.entities.RRepository;
import java.util.ArrayList;
import java.util.List;

public class PackageMaintainerTestFixture {

    public static final Boolean DELETED = false;

    public static List<PackageMaintainer> GET_PACKAGE_MAINTAINERS_FOR_REPOSITORY(int count, RRepository repository) {
        List<PackageMaintainer> maintainers = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            PackageMaintainer maintainer = new PackageMaintainer();
            maintainer.setId(123 + i);
            maintainer.setPackageName("test_package");
            maintainer.setRepository(repository);
            maintainer.setUser(UserTestFixture.GET_PACKAGE_MAINTAINER());
            maintainers.add(maintainer);
        }

        return maintainers;
    }
}

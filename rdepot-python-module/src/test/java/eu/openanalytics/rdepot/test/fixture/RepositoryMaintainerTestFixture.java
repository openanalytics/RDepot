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

import eu.openanalytics.rdepot.base.entities.Repository;
import eu.openanalytics.rdepot.base.entities.RepositoryMaintainer;
import eu.openanalytics.rdepot.base.entities.User;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

public class RepositoryMaintainerTestFixture {

    public static List<RepositoryMaintainer> GET_EXAMPLE_REPOSITORY_MAINTAINERS() {
        User user = UserTestFixture.GET_REPOSITORY_MAINTAINER(111);

        List<RepositoryMaintainer> maintainers = new ArrayList<>();
        for (int i = 100; i < 103; i++) {
            Repository repository = PythonRepositoryTestFixture.GET_EXAMPLE_REPOSITORY(100);
            RepositoryMaintainer maintainer = new RepositoryMaintainer(i, user, repository, false);

            maintainers.add(maintainer);
        }

        maintainers.get(2).setDeleted(true);
        return maintainers;
    }

    public static Page<RepositoryMaintainer> GET_EXAMPLE_REPOSITORY_MAINTAINERS_PAGED() {
        return new PageImpl<RepositoryMaintainer>(GET_EXAMPLE_REPOSITORY_MAINTAINERS());
    }

    public static RepositoryMaintainer GET_FIXTURE_REPOSITORY_MAINTAINER() {
        return GET_EXAMPLE_REPOSITORY_MAINTAINERS().get(0);
    }
}
